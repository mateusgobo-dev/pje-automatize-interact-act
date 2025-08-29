/**
 * FluxoEL.java
 * 
 * Data: 22/04/2016
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.ManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Classe com as EL's disponíveis para serem usadas nos fluxos.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings("unchecked")
@Name(FluxoEL.NAME)
@Scope(ScopeType.APPLICATION)
public class FluxoEL implements Serializable {

	public static final String NAME = "fluxoEL";

	/**
	 * @return Instância de FluxoEL.
	 */
	public static FluxoEL instance(){
		return (FluxoEL) Component.getInstance(FluxoEL.NAME);
	}
	
	public Boolean enviarDocumentoParaInstanciaDeOrigemSeComunicacaoEntreInstancias() throws PJeBusinessException {
		// Recuperar o endereço WSDL do servidor para o qual a comunicação será entregue
		EnderecoWsdl enderecoWsdl = obterEnderecoWsdl();
		
		// Recuperar o objeto ProcessoTrfManifestacaoProcessual que representa a instância de origem do ProcessoTrf
		ProcessoTrfManifestacaoProcessual processoManifestacao = obterProcessoTrfManifestacaoProcessualDoProcesso();
		
		return this.enviarDocumentoParaInstanciaDeOrigemSeComunicacaoEntreInstancias(enderecoWsdl, processoManifestacao, null);
	}
	/**
	 * Envia um documento para a instância de origem do processo.
	 * 
	 * @param enderecoWsdl Endereço WSDL da instância do PJE para a qual o documento será enviado
	 * @param processoManifestacao Dados da manifestação processual a ser entegue
	 * @param idProcessoDocumento Identificador do documento a ser enviado
	 * @throws PJeBusinessException
	 */
	public Boolean enviarDocumentoParaInstanciaDeOrigemSeComunicacaoEntreInstancias(
			EnderecoWsdl enderecoWsdl, 
			ProcessoTrfManifestacaoProcessual processoManifestacao, 
			Integer idProcessoDocumento) throws PJeBusinessException {
		Boolean resultado = Boolean.TRUE;
		ProcessoDocumento processoDocumento = null;
		TipoProcessoDocumento tipoComunicacaoEntreInstancias = ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias();
		
		if (tipoComunicacaoEntreInstancias == null) {
			String mensagem = "É necessário configurar o parâmetro 'idTipoProcessoDocumentoComunicacaoEntreInstancias' "
					+ "com o tipo de documento usado na comunicação entre instâncias.";
			throw new PJeBusinessException(mensagem);
		}
		
		if (idProcessoDocumento != null) {
			ProcessoDocumento processoDocumentoOriginal = EntityUtil.getEntityManager().find(ProcessoDocumento.class, idProcessoDocumento);
			
			try {
				processoDocumento = EntityUtil.cloneEntity(processoDocumentoOriginal, true);
				processoDocumento.setProcessoDocumento("Comunicação de " + processoDocumentoOriginal.getTipoProcessoDocumento().getTipoProcessoDocumento());
				processoDocumento.setTipoProcessoDocumento(tipoComunicacaoEntreInstancias);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new PJeBusinessException("Erro ao tratar a entidade");
			}
			
		} else {
			processoDocumento = getAtoProferido();	
		}
		
		if (processoDocumento == null) {
			processoDocumento = ComponentUtil.getProcessoDocumentoManager().getUltimoProcessoDocumentoAssinado(
					tipoComunicacaoEntreInstancias, processoManifestacao.getProcessoTrf().getProcesso());
		}
		
		TipoProcessoDocumento tipoProcessoDocumento = processoDocumento.getTipoProcessoDocumento();
		
		if (tipoComunicacaoEntreInstancias.getIdTipoProcessoDocumento() == tipoProcessoDocumento.getIdTipoProcessoDocumento()) {
			ManifestacaoProcessualRequisicaoDTO manifestacao = novaManifestacaoProcessualDaMinuta(enderecoWsdl, processoManifestacao, processoDocumento);
			MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(enderecoWsdl);
			ManifestacaoProcessualRespostaDTO resposta = mediator.entregarManifestacaoProcessual(manifestacao);
			
			if (!resposta.getSucesso()) {
				FacesMessages.instance().add(Severity.ERROR, resposta.getMensagem());
				throw new PJeBusinessException(resposta.getMensagem());
			}
		}
		
		return resultado;
	}
	
	/**
	 * Retorna o objeto ProcessoTrf presente como variável de fluxo.
	 * 
	 * @return ProcessoTrf
	 * @throws PJeBusinessException
	 */
	public ProcessoTrf getProcessoTrf() throws PJeBusinessException {
		ProcessoTrf resultado = null;
		Integer idProcessoTrf = getVariable(Variaveis.VARIAVEL_PROCESSO);
		
		if (idProcessoTrf != null) {
			resultado = getProcessoJudicialService().findById(idProcessoTrf);
		}
		
		return resultado;
	}

	/**
	 * Retorna o objeto ProcessoDocumento presente como variável de fluxo 'minutaEmElaboracao'.
	 *  
	 * @return ProcessoDocumento
	 * @throws PJeBusinessException
	 */
	public ProcessoDocumento getMinutaEmElaboracao() throws PJeBusinessException {
		ProcessoDocumento resultado = null;

		Integer idProcessoDocumento = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(this.getTaskInstance());
		if (idProcessoDocumento != null) {
			resultado = getDocumentoJudicialService().getDocumento(idProcessoDocumento);
		}
		
		return resultado;
	}
	
	/**
	 * Retorna o objeto ProcessoDocumento presente como variável de fluxo 'Variaveis.ATO_PROFERIDO'.
	 *  
	 * @return ProcessoDocumento
	 * @throws PJeBusinessException
	 */
	public ProcessoDocumento getAtoProferido() throws PJeBusinessException {
		ProcessoDocumento resultado = null;
		Integer idProcessoDocumento = null;

		Object variable = getVariable(Variaveis.ATO_PROFERIDO);
		if (variable instanceof Long) {
			idProcessoDocumento = Math.toIntExact((Long)variable);
		} else {
			idProcessoDocumento = (Integer)variable;
		}

		if (idProcessoDocumento != null) {
			resultado = getDocumentoJudicialService().getDocumento(idProcessoDocumento);
		}
		
		return resultado;
	}

	
	/**
	 * Retorna o objeto ProcessoDocumento presente como variável de fluxo 'Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO', retornará o último documento juntado por fluxo
	 *  
	 * @return ProcessoDocumento
	 * @throws PJeBusinessException
	 */
	public ProcessoDocumento getUltimoDocumentoJuntadoNesteFluxo() throws PJeBusinessException {
		ProcessoDocumento resultado = null;
		
		Integer idProcessoDocumento = getVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO);
		if (idProcessoDocumento != null) {
			resultado = getDocumentoJudicialService().getDocumento(idProcessoDocumento);
		}
		return resultado;
	}

	/**
	 * @param nome
	 * @return variável do fluxo.
	 */
	public <T> T getVariable(String nome) {
		T resultado = (T) getTaskContextInstance().getVariable(nome);
		
		if (resultado == null) {
			resultado = (T) getProcessContextInstance().getVariable(nome);
		}
		
		return resultado;
	}
	
	/**
	 * @param nome
	 * @param valor
	 */
	public void setVariable(String nome, Object valor) {
		getTaskContextInstance().setVariableLocally(nome, valor);
	}
	
	/** 
	 * Retorna o objeto ProcessoTrfManifestacaoProcessual que representa a instância de origem do 
	 * ProcessoTrf.
	 * 
	 * @return ProcessoTrfManifestacaoProcessual
	 * @throws PJeBusinessException
	 */
	public ProcessoTrfManifestacaoProcessual obterProcessoTrfManifestacaoProcessualDoProcesso() throws PJeBusinessException {
		ProcessoTrf processoTrf = getProcessoTrf();
		ProcessoTrfManifestacaoProcessual processoTrfManifestacao = 
				getProcessoTrfManifestacaoProcessualManager().obterUltimo(processoTrf);
		if (processoTrfManifestacao == null) {
			EnderecoWsdl enderecoWsdl = obterEnderecoWsdl();
			String numeroProcesso = processoTrf.getDesProcReferencia();
			
			if (StringUtils.isBlank(numeroProcesso)) {
				numeroProcesso = processoTrf.getNumeroProcesso();
			}
			
			processoTrfManifestacao = criarNovoProcessoTrfManifestacaoProcessualDoProcesso(enderecoWsdl, processoTrf, numeroProcesso);
		}
		
		return processoTrfManifestacao;
	}
	
	/**
	 * Método responsável por criar um novo objeto da classe "ProcessoTrfManifestacaoProcessual"
	 * @param enderecoWsdl
	 * @param processoTrf
	 * @param numeroProcesso
	 * @return
	 */
	public ProcessoTrfManifestacaoProcessual criarNovoProcessoTrfManifestacaoProcessualDoProcesso(
			EnderecoWsdl enderecoWsdl, 
			ProcessoTrf processoTrf,
			String numeroProcesso) {
		ProcessoTrfManifestacaoProcessual processoTrfManifestacao = new ProcessoTrfManifestacaoProcessual();
		processoTrfManifestacao.setEnderecoWsdl(enderecoWsdl);
		processoTrfManifestacao.setProcessoTrf(processoTrf);
		processoTrfManifestacao.setNumeroProcessoManifestacao(numeroProcesso);
		
		return processoTrfManifestacao;
	}
	
	/**
	 * Retorna o EnderecoWsdl de onde o processo foi recebido. A procura se faz na seguinte ordem:
	 * 1) Na tabela ProcessoTrfManifestacaoProcessual (alimentada na remessa/retorno depois da implementação dos multiplos endpoints);
	 * 2) Na tabela ManifestacaoProcessual (alimentada na remessa/retorno);
	 * 3) N tabela de Parametro (retorno o endereço configurado como endereço de integração).
	 * 
	 * @return EnderecoWsdl
	 * @throws PJeBusinessException
	 */
	protected EnderecoWsdl obterEnderecoWsdl() throws PJeBusinessException {
		ProcessoTrf processoTrf = getProcessoTrf();
		EnderecoWsdl resultado = getEnderecoWsdlManager().obterEnderecoWsdl(processoTrf, true);
		
		if (resultado == null) {
			String mensagem = "Não foi possível recuperar o endpoint de destino para o processo %s";
			throw new PJeBusinessException(String.format(mensagem, processoTrf.getNumeroProcesso()));
		}
		return resultado;
	}
	
	/**
	 * Nova ManifestacaoProcessual do ProcessoTrf.
	 * 
	 * @param processoManifestacao
	 * @return ManifestacaoProcessual
	 * @throws PJeBusinessException
	 */
	protected ManifestacaoProcessualRequisicaoDTO novaManifestacaoProcessualDaMinuta(EnderecoWsdl enderecoWsdl, ProcessoTrfManifestacaoProcessual processoManifestacao, ProcessoDocumento processoDocumento) 
			throws PJeBusinessException {
		ProcessoTrf processoTrf = getProcessoTrf();
		if (enderecoWsdl == null) {
			enderecoWsdl = ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem();
		}
		
		ManifestacaoProcessualRequisicaoDTO manifestacao = new ManifestacaoProcessualRequisicaoDTO();
		manifestacao.setProcessoTrf(processoTrf);
		manifestacao.setNumeroProcesso(processoManifestacao.getNumeroProcessoManifestacao());
		manifestacao.addParametro(MNIParametro.PARAM_INSTANCIA_PROCESSO_ORIGEM, ParametroUtil.instance().getInstancia());
		manifestacao.addParametro(MNIParametro.PARAM_NUM_PROC_1_GRAU, processoTrf.getNumeroProcesso());
		manifestacao.addParametro(MNIParametro.PARAM_DESCRICAO_INSTANCIA_PROCESSO_ORIGEM, ParametroUtil.instance().getSiglaTribunal() + " " + ParametroUtil.instance().getInstancia());
		manifestacao.addParametro(MNIParametro.PARAM_URL_ORIGEM_ENVIO, enderecoWsdl.getWsdlIntercomunicacao());
		manifestacao.addParametro(MNIParametro.PARAM_URL_ORIGEM_CONSULTA, enderecoWsdl.getWsdlConsulta());
		manifestacao.addParametro(MNIParametro.isPJE(), "true");
		manifestacao.setIsRequisicaoPJE(Boolean.TRUE);
		manifestacao.addProcessoDocumento(processoDocumento);

		return manifestacao;
	}
	
	/**
	 * @return TaskInstance
	 */
	protected TaskInstance getTaskInstance() {
		return org.jboss.seam.bpm.TaskInstance.instance();
	}
	
	/**
	 * @return ProcessInstance
	 */
	protected ProcessInstance getProcessInstance() {
		return org.jboss.seam.bpm.ProcessInstance.instance();
	}
	
	/**
	 * @return ContextInstance
	 */
	protected ContextInstance getTaskContextInstance() {
		return getTaskInstance().getContextInstance();
	}
	
	/**
	 * @return ContextInstance
	 */
	protected ContextInstance getProcessContextInstance() {
		return getProcessInstance().getContextInstance();
	}
	
	/**
	 * @return DocumentoJudicialService
	 */
	protected DocumentoJudicialService getDocumentoJudicialService() {
		return DocumentoJudicialService.instance();
	}
	
	/**
	 * @return ProcessoJudicialService
	 */
	protected ProcessoJudicialService getProcessoJudicialService() {
		return ComponentUtil.getComponent(ProcessoJudicialService.class);
	}
	
	/**
	 * @return ProcessoTrfConexaoManager
	 */
	protected ProcessoTrfConexaoManager getProcessoTrfConexaoManager() {
		return ComponentUtil.getComponent(ProcessoTrfConexaoManager.class);
	}
	
	/**
	 * @return ProcessoTrfManifestacaoProcessualManager
	 */
	protected ProcessoTrfManifestacaoProcessualManager getProcessoTrfManifestacaoProcessualManager() {
		return ProcessoTrfManifestacaoProcessualManager.instance();
	}

	/**
	 * @return ManifestacaoProcessualManager
	 */
	protected ManifestacaoProcessualManager getManifestacaoProcessualManager() {
		return ManifestacaoProcessualManager.instance();
	}
	
	/**
	 * @return EnderecoWsdlManager
	 */
	protected EnderecoWsdlManager getEnderecoWsdlManager() {
		return EnderecoWsdlManager.instance();
	}
}