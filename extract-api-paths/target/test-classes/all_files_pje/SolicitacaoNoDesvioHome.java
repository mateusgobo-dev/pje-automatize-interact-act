package br.com.infox.cliente.home;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.JbpmException;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.access.SecurityUtil;
import br.com.infox.cliente.component.suggest.ProcessoTrfNoDesvioSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.servicos.NoDeDesvioService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.SolicitacaoNoDesvio;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.Usuario;

/*
 * PJE-JT: Ricardo Scholz : PJE-785 - 2011-11-02 Alteracoes feitas pela JT.
 * Refactoring para uso da classe ProcessoTrfNoDesvioSuggestBean em
 * substituição à classe ProcessoTrfSuggestBean. Vide comentário na classe
 * ProcessoTrfNoDesvioSuggestBean.
 */
@Name("solicitacaoNoDesvioHome")
@BypassInterceptors
public class SolicitacaoNoDesvioHome extends AbstractHome<SolicitacaoNoDesvio> {

	private static final long serialVersionUID = 1L;
	private boolean showView = false;

	public void setSolicitacaoNoDesvioIdSolicitacaoNoDesvio(Integer id) {
		setId(id);
	}

	public Integer getSolicitacaoNoDesvioIdSolicitacaoNoDesvio() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("processoTrfNoDesvioSuggest");
		super.newInstance();
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		ProcessoTrf proc = getInstance().getProcessoTrf();
		setShowView(true);
		if (changed) {
			getProcessoTrfNoDesvioSuggest().setInstance(proc);
		}
		if (id == null) {
			setShowView(false);
			getProcessoTrfNoDesvioSuggest().setInstance(proc);
		}
	}

	public static SolicitacaoNoDesvioHome instance() {
		return ComponentUtil.getComponent("solicitacaoNoDesvioHome");
	}

	@Override
	public String persist() {
		ProcessoTrf suggestInstance = getProcessoTrfNoDesvioSuggest().getInstance();
		
		
		/*
		 * PJE-JT: Sérgio Ricardo: [PJEII-819] : Alteracoes feitas pela JT: 2012-04-10 Verificação da existência do Processo digitado/escolhido no suggest
		 */
		if (suggestInstance == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Processo não existe !");
			
			return "error";
		}
		/*
		 * PJE-JT: FIM [PJEII-819]
		 */
		
		instance.setProcessoTrf(suggestInstance);
		ProcessoHome.instance().setInstance(suggestInstance.getProcesso());
		
		/*
		 * PJE-JT: Ricardo Scholz e João Afonso: PJEII-3402 - 2012-10-18 Alteracoes feitas pela JT.
		 * Verifica se as variáveis do JBPM estão consistentes, impedindo a transição para o nó de
		 * desvio, caso não estejam.
		 * Veja mais detalhes em SolicitacaoNoDesvioHome#isSituacaoProcessoConsistente().
		 */
		SituacaoProcesso sitProc = this.getSituacaoProcesso();
		if(sitProc == null || !isSituacaoProcessoConsistente(sitProc)){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Há inconsistências no processo " +
					" que impossibilitam chamá-lo à ordem. Por favor, se o problema persistir," +
					" entre em contato com o suporte.");
			return "error";
		}
		/*
		 * PJE-JT: Fim.
		 */
		
		/*
		 * PJE-JT: Ricardo Scholz e João Afonso: PJEII-3402 - 2012-10-18 Alteracoes feitas pela JT.
		 * Refactoring de código de forma a utilizar método que encapsula recuperação da instância
		 * de SituacaoProcesso.
		 */
		Integer idTarefa = sitProc.getIdTarefa();
		
		if(idTarefa != null){
			Tarefa tarefa = EntityUtil.getEntityManager().find(Tarefa.class, idTarefa);
			instance.setTarefa(tarefa);
		}
		
		/*
		 * PJE-JT: Fim.
		 */
		instance.setUsuario((Usuario) Contexts.getSessionContext().get("usuarioLogado"));
		instance.setDataSolicitacao(new Date());

		try {
			/*
			 * PJE-JT: Ricardo Scholz e João Afonso: PJEII-3402 - 2012-10-18 Alteracoes feitas pela JT.
			 * Refactoring de código de forma a utilizar método que encapsula recuperação da instância
			 * de SituacaoProcesso.
			 */
			ProcessInstance pi = ManagedJbpmContext.instance().getProcessInstance(
					sitProc.getIdProcessInstance());
			if(pi == null) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Não foi possível localizar a" +
						" instância do processo no fluxo. Por favor, se o problema persistir," +
						" entre em contato com o suporte.");
				return "error";
			}
			/*
			 * PJE-JT: Fim.
			 */

			pi.getRootToken().signal(NoDeDesvioService.getNomeNoDesvio(pi.getProcessDefinition()));
			try {
				this.ajustaVariavel(Variaveis.VARIABLE_CONDICAO_LANCAMENTO_MOVIMENTOS_TEMPORARIO, "${false}", sitProc.getIdProcessInstance());
			} catch (Exception e){
				e.printStackTrace();
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Não foi possível realizar a " +
						"atualização de variáveis de fluxo. Por favor, se o problema persistir," +
						" entre em contato com o suporte.");
				return "error";
			}
		} catch (JbpmException e) {
			FacesMessages.instance().clear();
			FacesMessages
					.instance()
					.add("Não existe nó de desvio definido para o fluxo em que se encontra o processo, ou o processo já se encontra neste nó.");
			return null;
		}
		String ret = super.persist();
		newInstance();
		return ret;
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {

		// refreshGrid("solicitacaoNoDesvioGrid");
		// setTab("search");
		return super.afterPersistOrUpdate(ret);

	}

	public void setarProcesso(ProcessoTrf processo) {
		getInstance().setProcessoTrf(processo);
		getProcessoTrfNoDesvioSuggest().setInstance(getInstance().getProcessoTrf());
	}

	private ProcessoTrfNoDesvioSuggestBean getProcessoTrfNoDesvioSuggest() {
		ProcessoTrfNoDesvioSuggestBean processoTrfNoDesvioSuggestBean = (ProcessoTrfNoDesvioSuggestBean) Component
				.getInstance("processoTrfNoDesvioSuggest");
		return processoTrfNoDesvioSuggestBean;
	}

	public void setShowView(boolean showView) {
		this.showView = showView;
	}

	public boolean isShowView() {
		return showView;
	}

	public boolean isTelaDisponivel() {
		boolean checkPage = SecurityUtil.instance().checkPage();
		if (checkPage) {
			String verifica = ParametroUtil.getFromContext("flagUtilizacaoNoDesvio", false);
			if (verifica != null && verifica.equals("true")) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * PJE-JT: Ricardo Scholz e João Afonso: PJEII-3402 - 2012-10-18 Alteracoes feitas pela JT.
	 * Métodos auxiliares, visando maior encapsulamento do código.
	 */
	
	/**
	 * Recupera a instância de SituacaoProcesso a partir do ID do ProcessoTrf.
	 * Este método executa um SELECT no banco de dados, motivo pelo qual sua chamada
	 * deve ser realizada com moderação. Sempre que possível, a instância recuperada
	 * na primeira chamada deve ser guardada em variáveis temporárias, evitando
	 * novas chamadas.
	 * @return
	 */
	private SituacaoProcesso getSituacaoProcesso(){
		try {
			// Recupera a instância de SituacaoProcesso a partir do ID do ProcessoTrf
			String hql = "select o from SituacaoProcesso o where o.idProcesso = :idProcesso";
			Query q = EntityUtil.createQuery(hql);
			q.setParameter("idProcesso", instance.getProcessoTrf().getIdProcessoTrf());
			// Caso mais de um resultado seja retornado, uma exceção será disparada e o 
			// método retornará 'null'.
			return (SituacaoProcesso) q.getSingleResult();
		} catch (Exception e){
			// Caso qualquer exceção seja disparada, retorna 'null'.
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Verifica se o processo encontra-se em situação consistente, por meio da verificação da
	 * existência e unicidade dos documentos apontados pelas variáveis 'minutaEmElaboracao' e
	 * 'pje:atoProferido', do JBPM. Caso ocorra qualquer exceção, o processo é considerado
	 * inconsistente.
	 * @return	<code>true</code> se o processo estiver consistente.
	 */
	private boolean isSituacaoProcessoConsistente(SituacaoProcesso sitProc){
		boolean resultado = false;
		try {
			// Verifica as variáveis 'minutaEmElaboracao' e 'pje:atoProferido', retornando 'true'
			// apenas se ambas estiverem consistentes.
			resultado = isVariavelConsistente(Variaveis.MINUTA_EM_ELABORACAO, sitProc.getIdProcessInstance()) && 
					isVariavelConsistente(Variaveis.ATO_PROFERIDO, sitProc.getIdProcessInstance());
		} catch (Exception e){
			//Em caso de erro, retorna 'false', indicando que o processo está inconsistente
			resultado = false;
			e.printStackTrace();
		}
		return resultado;
	}

	/**
	 * Verifica se o documento relacionado a uma determinada variável do JBPM existe e 
	 * se a variável é única.
	 * @param variavel				nome da variável do JBPM que se deseja verificar.
	 * @param idProcessInstance		identificador do process instance (vide modelagem JBPM).
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isVariavelConsistente(String variavel, long idProcessInstance) {
		boolean resultado = false;
		// Recupera o 'longvalue_' da 'jbpm_variableinstance', de acordo com a
		// variável e o ID do ProcessInstance recebidos como parâmetro.
		String sql = "select longvalue_ from public.jbpm_variableinstance where " +
				"processinstance_ = :idProcessInstance " +
				"and name_ = '" + variavel + "' " +
				"and taskinstance_ is null " +
				"and longvalue_ is not null";
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql);
		q.setParameter("idProcessInstance", idProcessInstance);
		List<BigInteger> longValues = (List<BigInteger>) q.getResultList();
		// Nenhum resultado encontrado indica que não há inconsistência
		if(longValues.size() == 0){
			resultado = true;
		} 
		// Se exatamente um resultado for encontrado, é preciso checar se o documento
		// referenciado existe na base de dados.
		else if (longValues.size() == 1){
			// Tenta recuperar o documento referenciado pelo seu ID
			ProcessoDocumento doc = EntityUtil.find(ProcessoDocumento.class, 
					longValues.get(0).intValue());
			// Caso o documento exista, retorna 'true'. Caso contrário, retorna 'false'.
			resultado = (doc != null);
		}
		return resultado;
	}
	
	/**
	 * Ajusta o valor de uma variável JBPM. Na tabela 'public.jbpm_variableinstance',
	 * seta a coluna 'stringvalue_' com o valor da variável <code>valor</code> quando 
	 * as colunas 'processinstance_' e 'name_' apresentam os valores dos atributos 
	 * <code>idProcessInstance</code> e <code>name</code>, respectivamente.
	 * @param variavel				nome da variável a ser ajustada
	 * @param valor					novo valor da variável
	 * @param idProcessInstance		ID do process instance
	 */
	private void ajustaVariavel(String variavel, String valor, long idProcessInstance){
		String sql = "update public.jbpm_variableinstance set stringvalue_ = :valor" +
				" where processinstance_ = :idProcessInstance and name_ = :variavel";
		Query q = EntityUtil.createNativeQuery(sql, "jbpm_variableinstance");
		q.setParameter("valor", valor);
		q.setParameter("variavel", variavel);
		q.setParameter("idProcessInstance", idProcessInstance);
		q.executeUpdate();
	}
	/*
	 * PJE-JT: Fim.
	 */
}
