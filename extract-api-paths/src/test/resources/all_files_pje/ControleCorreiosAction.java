/**
 *  pje-web
 *  Copyright (C) 2014 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.RegistroIntimacaoManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RegistroIntimacao;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;
import java.util.ArrayList;


/**
 * Componente de controle de tela do frame de fluxo WEB-INF/xhtml/flx/exped/controleCorreios.xhtml
 * 
 * @author Thiago de Andrade Vieira
 * @author cristof
 *
 */
@Name(ControleCorreiosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ControleCorreiosAction extends TramitacaoFluxoAction implements Serializable, ArquivoAssinadoUploader{
	 
	private static final long serialVersionUID = -6903021077469426502L;

	public static final String VARIAVEL_EXPEDIENTE = "comunicacaoProcessualAction:idsExpedientes";
	
	public static final String NAME = "controleCorreiosAction";    
    
    @In(create = false)
    private ProcessInstance processInstance;
    
    private Map<Integer, String> mapaAr;
    
    @In
    private AtoComunicacaoService atoComunicacaoService;
    
    @In
    private ProcessoParteExpedienteManager processoParteExpedienteManager;
    
    @In
    private ProcessoExpedienteManager processoExpedienteManager;
    
    @In
    private RegistroIntimacaoManager registroIntimacaoManager;
    
    private ProcessoExpediente expedienteAtual;
    
    private Boolean alterar = Boolean.FALSE;
    
    private String link;
    
    private RegistroIntimacao registroIntimacaoAtual;
    
    private ProtocolarDocumentoBean protocolarDocumentoBean;
    
    private String urlAcessoECT;
    
    private static Map<String, String> prms;
    
    private Boolean editarNumeroAR = Boolean.TRUE;
    
    private Boolean registrarCiencia = Boolean.TRUE;
    
    private ProcessoExpediente expedienteSelecionado;
    
    private List<ProcessoExpediente> expedientesCorreios;
    
    static{
    	prms = new HashMap<String, String>();
    	prms.put("mapaAr", "pje:fluxo:correios:mapaAr");
    	prms.put("editarNumeroAR", "pje:fluxo:correios:edicaoNumeroAR");
    	prms.put("registrarCiencia", "pje:fluxo:correios:registroCiencia");
    	prms.put("urlAcessoECT", "pje:fluxo:correios:url:acesso");
    }

	@Override
	public void init() {
		super.init();
		if (getMapaAr() == null || CollectionUtils.isEmpty(getExpedientesCorreios())) {
			if(getMapaAr() == null){
				mapaAr = new HashMap<Integer, String>(0);
			}

			try {
				setExpedientesCorreios(processoExpedienteManager.recuperarExpedientesPendentes(Arrays.asList(processoJudicial), Arrays.asList(ExpedicaoExpedienteEnum.C)));
				for (ProcessoExpediente expediente : getExpedientesCorreios()) {
					if(!getMapaAr().containsKey(expediente.getIdProcessoExpediente())){
						getMapaAr().put(expediente.getIdProcessoExpediente(), "");
					}


				}				
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Não foi possível recuperar os atos de comunicação.", e.getMessage());
			}
		}
		if(urlAcessoECT == null || urlAcessoECT.isEmpty()){
			urlAcessoECT = "http://websro.correios.com.br/sro_bin/txect01$.QueryList?P_LINGUA=001&P_TIPO=001&P_COD_UNI=%s";
		}
	}
    
    /**
     * Grava na variável de fluxo "pje:fluxo:correios:mapaAr" o mapa de avisos de recebimento.
     */
    public void gravarArs(){
    	tramitacaoProcessualService.gravaVariavel("pje:fluxo:correios:mapaAr",getMapaAr());
    	setAlterar(false);
    	facesMessages.add(Severity.INFO,"Gravado com sucesso.");
    }
    

	/**
	 * Recupera o mapa de avisos de recebimento eventualmente gravado no fluxo.
	 * 
	 * @return o mapa de avisos de recebimento.
	 */
	public Map<Integer, String> getMapaAr() {
		return mapaAr;
	}

	/**
	 * Indica a possibilidade de alteração dos códigos de aviso de recebimento.
	 * 
	 * @return true, se for possível fazer a alteração
	 */
	public Boolean getAlterar() {
		return alterar;
	}

	/**
	 * Marca como passível de alteração o código do aviso de recebimento.
	 * 
	 * @param alterar o valor a ser definido
	 */
	public void setAlterar(Boolean alterar) {
		if(alterar){
			setLink(null);
		}
		this.alterar = alterar;
	}

	/**
	 * Recupera o link de acesso a informações de um objeto postal.
	 * 
	 * @return o link de acesso
	 */
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		
		this.link = link;
	}
	
	public void atualizaLink(String codigoObjeto){
		link = String.format(urlAcessoECT, codigoObjeto);
		expedienteAtual = null;
	}
	
	public void atualizaLinkAr(Integer idProcessoExpediente){
		try {
			expedienteAtual = processoExpedienteManager.findById(idProcessoExpediente);
			registroIntimacaoAtual = registroIntimacaoManager.getRegistroIntimacao();
			registroIntimacaoAtual.setNumeroAvisoRecebimento(mapaAr.get(idProcessoExpediente));
			protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(), 
					ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.VINCULAR_DATA_JUNTADA | 
					ProtocolarDocumentoBean.NAO_ASSINA_DOCUMENTO_PRINCIPAL,
					getActionName());
			protocolarDocumentoBean.setDocumentoPrincipal(expedienteAtual.getProcessoDocumento());
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar definir o código do objeto postal: {0}", e.getLocalizedMessage());
			return;
		}
		this.link = null;
	}
	
	public void finalizar(){
		try {
			if (protocolarDocumentoBean.getArquivos().isEmpty()) {
				facesMessages.add(Severity.WARN, "Nenhum registro de intimação adicionado.");
				return;
			}

            List<ProcessoDocumento> arquivos = new ArrayList<ProcessoDocumento>(protocolarDocumentoBean.getArquivos());

			getProtocolarDocumentoBean().concluirAssinatura();


			for (ProcessoDocumento doc : arquivos) {
				for (ProcessoParteExpediente ppe : expedienteAtual.getProcessoParteExpedienteList()) {
					registrarIntimacao(doc, ppe, registroIntimacaoAtual.getNumeroAvisoRecebimento(),
							registroIntimacaoAtual.getResultado(), registroIntimacaoAtual.getData());
				}
				lancarJuntadaAR(doc);
			}

			registroIntimacaoManager.flush();

			setExpedientesCorreios(processoExpedienteManager.recuperarExpedientesPendentes(Arrays.asList(processoJudicial), Arrays.asList(ExpedicaoExpedienteEnum.C)));
			expedienteAtual = null;
			protocolarDocumentoBean.getArquivos().clear();
			protocolarDocumentoBean.setDocumentoPrincipal(null);
			facesMessages.add(Severity.INFO, "Registro de intimação concluído com sucesso.");
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar registrar a intimação relativa ao objeto [{0}]", registroIntimacaoAtual.getNumeroAvisoRecebimento());
		}
	}
	
	private void registrarIntimacao(ProcessoDocumento doc, ProcessoParteExpediente ppe, String codigoAR, TipoResultadoAvisoRecebimentoEnum res, Date dataCiencia) throws PJeBusinessException{
		RegistroIntimacao ri = registroIntimacaoManager.getRegistroIntimacao();
		ri.setData(dataCiencia);
		ri.setNumeroAvisoRecebimento(codigoAR);
		ri.setProcessoDocumento(doc);
		ri.setProcessoParteExpediente(ppe);
		ri.setResultado(res);
		registroIntimacaoManager.persist(ri);
		if(res == TipoResultadoAvisoRecebimentoEnum.R){
			registrarCiencia(ppe, false);
		}else{
			registrarCiencia(ppe, true);
		}
	}
	
	private void lancarJuntadaAR(ProcessoDocumento doc){
		MovimentoAutomaticoService.preencherMovimento().
			deCodigo(581).
			comProximoComplementoVazio().
			doTipoLivre().
			preencherComTexto(doc.getTipoProcessoDocumento().getTipoProcessoDocumento().toLowerCase()).
			associarAoDocumento(doc).
			lancarMovimento();
	}
	
	private void registrarCiencia(ProcessoParteExpediente ppe, boolean intimacaoFrustrada){
		if(!intimacaoFrustrada && ppe.getDtCienciaParte() != null){
			facesMessages.add(Severity.WARN, "O ato já teve ciência pelo destinatário em {0}.", ppe.getDtCienciaParte());
			return;
		}
		Pessoa p = Authenticator.getPessoaLogada();
		ppe.setPessoaCiencia(p);
		ppe.setNomePessoaCiencia(p.getNome());
		if(intimacaoFrustrada){
			try {
				processoParteExpedienteManager.fecharExpediente(ppe);
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.FATAL, "Erro ao tentar fechar o expediente: %s.", e.getLocalizedMessage());
				e.printStackTrace();
				return;
			}
		}else{
			atoComunicacaoService.registraCienciaAutomatizada(registroIntimacaoAtual.getData(), false, Arrays.asList(ppe));
		}
	}
	

	public ProcessoExpediente getExpedienteAtual() {
		return expedienteAtual;
	}

	public void setExpedienteAtual(ProcessoExpediente expedienteAtual) {
		this.expedienteAtual = expedienteAtual;
	}

	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}

	public RegistroIntimacao getRegistroIntimacaoAtual() {
		return registroIntimacaoAtual;
	}

	public void setRegistroIntimacaoAtual(RegistroIntimacao registroIntimacaoAtual) {
		this.registroIntimacaoAtual = registroIntimacaoAtual;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}

	public Boolean getRegistrarCiencia() {
		return registrarCiencia;
	}
	
	public Boolean getEditarNumeroAR() {
		return editarNumeroAR;
	}

	public ProcessoExpediente getExpedienteSelecionado() {
		return expedienteSelecionado;
	}

	public void setExpedienteSelecionado(ProcessoExpediente expedienteSelecionado) {
		this.expedienteSelecionado = expedienteSelecionado;
	}

	public List<ProcessoExpediente> getExpedientesCorreios() {
		return expedientesCorreios;
	}

	public void setExpedientesCorreios(List<ProcessoExpediente> expedientesCorreio) {
		this.expedientesCorreios = expedientesCorreio;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
		
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public ProcessoTrf getProcessoTrf() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		return processoTrf;
	}

}