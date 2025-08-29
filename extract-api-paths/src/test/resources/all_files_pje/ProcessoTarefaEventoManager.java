/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoTarefaEventoDAO;
import br.jus.pje.nucleo.entidades.ProcessoTarefaEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link ProcessoTarefaEvento}.
 * 
 * @author cristof
 *
 */
@Name("processoTarefaEventoManager")
public class ProcessoTarefaEventoManager extends BaseManager<ProcessoTarefaEvento> {
	
	@In
	private ProcessoTarefaEventoDAO processoTarefaEventoDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoTarefaEventoDAO getDAO() {
		return processoTarefaEventoDAO;
	}
	
	/**
	 * Indica se uma tarefa de um dado processo exige um lançamento manual de movimentação.
	 * 
	 * @param processoJudicial o processo no qual se está realizando a tarefa
	 * @param nomeTarefa o nome da tarefa que se pretende verificar
	 * @return true, se a tarefa exigir a escolha de uma movimentação e ela ainda não estiver registrado
	 */
	public boolean exigeLancamentoManual(ProcessoTrf processoJudicial, String nomeTarefa){
		Search s = new Search(ProcessoTarefaEvento.class);
		addCriteria(s, 
				Criteria.equals("processo.idProcesso", processoJudicial.getIdProcessoTrf()),
				Criteria.equals("tarefaEvento.tarefa.tarefa", nomeTarefa),
				Criteria.equals("registrado", false));
		s.setMax(1);
		return count(s) > 0;
	}

	/**
	 * Indica se uma tarefa de um dado processo teve registrado o lançamento manual de movimentação.
	 * 
	 * @param processoJudicial o processo no qual se está realizando a tarefa
	 * @param nomeTarefa o nome da tarefa que se pretende verificar
	 * @return true, se a tarefa exigir a escolha de uma movimentação e seu lançamento já estiver registrado
	 */
	public boolean temMovimentoLancado(ProcessoTrf processoJudicial, String nomeTarefa){
		Search s = new Search(ProcessoTarefaEvento.class);
		addCriteria(s, 
				Criteria.equals("processo.idProcesso", processoJudicial.getIdProcessoTrf()),
				Criteria.equals("tarefaEvento.tarefa.tarefa", nomeTarefa),
				Criteria.equals("registrado", true));
		s.setMax(1);
		return count(s) > 0;
	}
	
}
