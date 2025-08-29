package br.jus.cnj.pje.util;

import org.hibernate.Session;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.JbpmContext;
import org.jbpm.persistence.db.DbPersistenceService;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.Util;

/**
 * Adaptação do worker do Seam
 * Performs work in a JTA transaction.
 * 
 * @author Gavin King
 */
public abstract class CustomTransactionalWork<T> {
	
	private boolean jbpmTransaction;
	
	/**
	 * Cria um trabalho transacional.
	 * Inicia e encerra uma transação commitando no final.
	 * Caso ocorra um erro realiza rollback da transação, liberando a thread associada a mesma. 
	 * @param jbpmTransaction indica se o contexto bpm deve ser tratado nesse método.
	 */
	public CustomTransactionalWork(boolean jbpmTransaction) {
		super();
		this.jbpmTransaction = jbpmTransaction;
	}

	public CustomTransactionalWork() {
		super();	
		this.jbpmTransaction = true;
	}

	protected abstract T work() throws Exception;

	public final T workInTransaction() throws Exception  {

		// Iniciar uma transação, se não houver transação ativa.
		Util.beginTransaction();
		JbpmContext currentJbpmContext = null;
		DbPersistenceService dbPersistenceService = null;
		if (jbpmTransaction){
			// Obtém o contexto JBPM gerenciado pelo Seam: ManagedJbpmContext
			currentJbpmContext = ManagedJbpmContext.instance();
			dbPersistenceService = (DbPersistenceService) currentJbpmContext
					.getServices().getPersistenceService();
		}

		try {

			T result = work();

			if (jbpmTransaction){
				// Obtém a sessão Hibernate do JBPM e envia as modificações ao
				// banco. Dessa forma, se na próxima iteração
				// ocorrer um erro, poderá ser feito o session.clear() para limpar
				// as modificação da "transação" JBPM atual.
				Session s = dbPersistenceService.getSession();
				s.flush();
			}

			// Commita a transação do Seam e remove a associação entra a thread
			// corrente e a transação.
			// Por conta disso, antes de qualquer outra consulta a banco, é
			// necessário chamar o Util.beginTransaction();
			Util.commitTransction();

			return result;

		} catch (Exception e) {
			if (jbpmTransaction){
				// Mesma solução desenvolvida para não "sumir" os processos na
				// movimentação.
				JbpmUtil.clearAndClose(currentJbpmContext);
			}
			try {
				// Libera a associação entre a transação (do Seam) e a thread
				// corrente. Dessa forma o
				// Util.beginTransaction pode iniciar outra transação e associar
				// à thread corrente.
				//
				// Não foi usado o Util.rollbackTransction, pois ele checa se a
				// transação está ativa, porém, dependendo da exceção (método
				// Work.isRollbackRequired()),
				// o interceptor do Seam pode "marcar" a transação para
				// rollback, o que deixa a transação como inativa,
				// mas deixa a thread associada à transação inativa (o que
				// prejudica a próxima iteração, quando ocorrer o
				// beginTransaction - exceção:
				// "thread is already associated with a transaction!").
				Transaction.instance().rollback();
			} catch (Exception e1) {
			}

			// Remove dos contextos do Seam o contexto do JBPM
			// (ManagedJbpmContext.instance()), dessa forma, na próxima
			// iteração, o Seam irá criar outro contexto JBPM
			// (ManagedJbpmContext.create()), associado a uma sessão nova do
			// Hibernate,
			// pois ao dar "rollback" no contexto JBPM a sessão tem que ser
			// fechada (JbpmUtil.clearAndClose()).
			// Isso é necessário, pois a implementação do JBPM do Seam não usa
			// JTA.
			Contexts.removeFromAllContexts("org.jboss.seam.bpm.jbpmContext");

			throw new Exception(e);
		}
		finally{
			/**
			 * [PJEII-7328] Antonio Lucas
			 * Iniciar uma transação, se não houver transação ativa.
			 * É necessário criar uma nova transação, pois pode haver algum interceptor
			 * que também vai tentar commitar a transação.
			 * 
			 */
			Util.beginTransaction();
		}

	}
}
