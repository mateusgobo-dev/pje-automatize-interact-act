package br.com.infox.pje.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ConsultaProcessoIbpmDAO;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.pje.nucleo.entidades.ConsultaProcessoFluxoAbertoTarefaFechadaIbpm;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade de
 * Processo em JBPM
 * 
 */
@Name(ProcessoJbpmManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoJbpmManager extends GenericManager {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoJbpmManager";

	@In
	private ProcessoTrfDAO processoTrfDAO;
	
	@In
	private ConsultaProcessoIbpmDAO consultaProcessoIbpmDAO;
	
	/**
     * @return Instância da classe.
     */
    public static ProcessoJbpmManager instance() {
        return ComponentUtil.getComponent(NAME);
    }
    
	/**
	 * Método responsável por realizar a listagem na view de processos com erro 
	 * de acordo com os parâmetros informados
	 * 
	 * @param numeroSequencia
	 * @param digitoVerificador
	 * @param ano
	 * @param numeroOrigem
	 * @param ramoJustica
	 * @param respectivoTribunal
	 * @param limiteConsulta
	 * @return uma <b>Lista</b> de ConsultaProcessoLimbo
	 */
	public List<ConsultaProcessoFluxoAbertoTarefaFechadaIbpm> processosFluxoAbertoTarefaFechadaIbpm(Integer numeroSequencia, Integer digitoVerificador,
			Integer ano, Integer numeroOrigem, String ramoJustica, String respectivoTribunal, Integer limiteConsulta) {
		
		return consultaProcessoIbpmDAO.processosFluxoAbertoTarefaFechadaIbpm(numeroSequencia, digitoVerificador, ano, numeroOrigem, 
				ramoJustica, respectivoTribunal, limiteConsulta);
	}

	/**
	 * Adiciona os objetos do mapa no Contexto do fluxo do ProcessoTrf.
	 * Serão adicionados somente as variáveis com prefixo "pje:fluxo:".
	 * 
	 * @param processo ProcessoTrf
	 * @param parametros Map<String, Object>
	 */
	public void adicionarVariaveis(ProcessoTrf processo, Map<String, Object> parametros) {
		if (processo != null && MapUtils.isNotEmpty(parametros)) {
			ContextInstance contextInstance = getContextInstance(processo);
			if (contextInstance != null) {
				Set<String> keys = parametros.keySet();
				for (String nome : keys) {
					Object valor = parametros.get(nome);
					
					if (StringUtils.startsWith(nome, Variaveis.VARIAVEL_PREFIXO_FLUXO) && !contextInstance.hasVariable(nome)) {
						contextInstance.setVariable(nome, valor);
					}
				}
			}
		}
	}
	
	/**
	 * Retorna as variáveis do JBPM vinculadas ao ProcessInstance do ProcessoTrf.
	 * 
	 * @param processo ProcessoTrf
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getJBPMVariables(ProcessoTrf processo) {
		Map<String, Object> resultado = new HashMap<>();
		
		ContextInstance contextInstance = getContextInstance(processo);
		if (contextInstance != null) {
			resultado = contextInstance.getVariables();
		}
		return resultado;
	}
	
	/**
	 * Retorna ContextInstance do fluxo do ProcessoTrf.
	 * 
	 * @param processo ProcessoTrf
	 * @return ContextInstance
	 */
	public ContextInstance getContextInstance(ProcessoTrf processo) {
		ContextInstance contextInstance = null;
		
		if (processo != null) {
			int idProcessoTrf = processo.getIdProcessoTrf();
			SituacaoProcessoManager situacaoProcessoManager = ComponentUtil.getComponent(SituacaoProcessoManager.class);
			SituacaoProcesso situacaoProcesso = situacaoProcessoManager.getByIdProcesso(idProcessoTrf);
			Long idProcessInstance = situacaoProcesso.getIdProcessInstance();
			ProcessInstance processInstance = ManagedJbpmContext.instance().getProcessInstance(idProcessInstance);
			contextInstance = (processInstance != null ? processInstance.getContextInstance() : null);
		}
		
		return contextInstance;
	}

	/**
	 * Retorna ProcessInstance do fluxo do ProcessoTrf.
	 * 
	 * @param processo ProcessoTrf
	 * @return ProcessInstance
	 */
	public ProcessInstance getProcessInstance(ProcessoTrf processo) {
		ProcessInstance processInstance = null;
		
		if (processo != null) {
			int idProcessoTrf = processo.getIdProcessoTrf();
			SituacaoProcessoManager situacaoProcessoManager = ComponentUtil.getComponent(SituacaoProcessoManager.class);
			SituacaoProcesso situacaoProcesso = situacaoProcessoManager.getByIdProcesso(idProcessoTrf);
			Long idProcessInstance = situacaoProcesso.getIdProcessInstance();
			processInstance = ManagedJbpmContext.instance().getProcessInstance(idProcessInstance);
		}
		
		return processInstance;
	}
	
}