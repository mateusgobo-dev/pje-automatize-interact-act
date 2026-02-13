package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.transaction.Transaction;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.action.ExpedientesAction;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RegistroIntimacao;
import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;

@Name(RegistroIntimacaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class RegistroIntimacaoAction implements ArquivoAssinadoUploader, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "registroIntimacaoAction";

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	private RegistroIntimacao registroIntimacao;
	
	private ProcessoParteExpediente processoParteExpediente;
	
	private ProcessoTrf processoTrf;
	
	private PrazosProcessuaisService prazosProcessuaisService;
	
	private boolean registroIntimacaoFinalizado = false;
	
	@RequestParameter(value="id")
	private Integer idProcessoParteExpediente;
	
	@Create
	public void init(){
		if(prazosProcessuaisService ==  null){
			prazosProcessuaisService = (PrazosProcessuaisService) Component.getInstance("prazosProcessuaisService");
		}
		if(idProcessoParteExpediente != null){
			try {
				processoParteExpediente = ComponentUtil.getProcessoParteExpedienteManager().findById(idProcessoParteExpediente);
				if(ComponentUtil.getRegistroIntimacaoManager().recuperarRegistroIntimacaoPorProcessoParteExpediente(processoParteExpediente) != null){
					setRegistroIntimacaoFinalizado(true);
					FacesMessages.instance().add(Severity.WARN, "Este expediente já foi registrado e encontra-se fechado");
				}
				protocolarDocumentoBean = new ProtocolarDocumentoBean(processoParteExpediente.getProcessoJudicial().getIdProcessoTrf(), true, false, true, true, false, true, false, getActionName());
				registroIntimacao = new RegistroIntimacao();
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu erro ao inicializar o componente RegistroIntimacaoAction: " + e.getLocalizedMessage());
			}
		}
	}
	
	public void concluirRegistroIntimacao(){
		try {

			ComponentUtil.getDocumentoJudicialService().gravarAssinaturaDeProcessoDocumento(this.protocolarDocumentoBean.getArquivosAssinados(), 
																				this.protocolarDocumentoBean.getProcessoDocumentosParaAssinatura());
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			if(resultado == false){
				throw new Exception("Não foi possível concluir a assinatura do documento!");
			} else {
				FacesMessages.instance().add(Severity.INFO, "Assinatura realizada");
				registroIntimacaoFinalizado = true;
			}
			
			ProcessoTrfHome.instance().refreshGrid("processoParteExpedienteMenuGrid");
		} catch (Exception e) {
			this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());
			
			try{
				Transaction.instance().rollback();
			} catch (Exception e1){
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu erro realizar rollback na transação ao concluir registro de intimação: " + e1.getLocalizedMessage());
			}
			
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Ocorreu erro ao concluir registro de intimação: " + e.getLocalizedMessage());
		}			
	}
	
	private boolean validaObrigatoriedadeAnexos(){
		boolean valido = true;
		
		if(this.registroIntimacao.getResultado().equals(TipoResultadoAvisoRecebimentoEnum.R) && this.protocolarDocumentoBean.getArquivos().size() ==0){
			valido = false;
			this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());
			FacesMessages.instance().add(Severity.ERROR, "Para expedientes recebidos é obrigatório o anexo contendo o AR (Aviso de recebimento)");
		}
		
		return valido;
	}
	
	public void registrarIntimacao(){		
		try {
			if(validarCampos() && validaObrigatoriedadeAnexos()){
				registroIntimacao.setProcessoDocumento(protocolarDocumentoBean.getDocumentoPrincipal());
				registroIntimacao.setProcessoParteExpediente(processoParteExpediente);
				ComponentUtil.getRegistroIntimacaoManager().persist(registroIntimacao);
				if(TipoResultadoAvisoRecebimentoEnum.R.equals(this.registroIntimacao.getResultado())) {
					processoParteExpediente.setPessoaCiencia(Authenticator.getPessoaLogada());
					Calendario calendario = prazosProcessuaisService.obtemCalendario(processoParteExpediente.getProcessoJudicial().getOrgaoJulgador());
					ComponentUtil.getProcessoParteExpedienteManager().registraCiencia(processoParteExpediente, registroIntimacao.getData(), false, calendario);
				} else {
				 	processoParteExpediente.setFechado(true);
				 	Events.instance().raiseEvent(Eventos.EVENTO_EXPEDIENTE_FECHADO, processoParteExpediente.getProcessoJudicial());
				 	ComponentUtil.getProcessoParteExpedienteManager().persistAndFlush(processoParteExpediente);
				}
				ExpedientesAction expedientesAction = (ExpedientesAction) ComponentUtil.getComponent(ExpedientesAction.NAME);
				expedientesAction.pesquisar();
				concluirRegistroIntimacao();
			}			
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR,"Ocorreu um erro ao registrar intimação: " + e.getLocalizedMessage());
		}	
	}
	
	public boolean validarCampos(){
		
		boolean retorno = true;
		
		if(registroIntimacao.getNumeroAvisoRecebimento() == null ||
				registroIntimacao.getNumeroAvisoRecebimento().isEmpty()){
			retorno = false;
		}
		
		if(registroIntimacao.getResultado() == null){
			retorno = false;
		}
		
		if(registroIntimacao.getData() == null){
			retorno = false;
		}
		return retorno;
	}
	
	public TipoResultadoAvisoRecebimentoEnum[] getResultados() {
		List<TipoResultadoAvisoRecebimentoEnum> values = Arrays.asList(TipoResultadoAvisoRecebimentoEnum.values());
		Collections.sort(values, new Comparator<TipoResultadoAvisoRecebimentoEnum> () {
			
			@Override
			public int compare (TipoResultadoAvisoRecebimentoEnum o1, TipoResultadoAvisoRecebimentoEnum o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});		
		return (TipoResultadoAvisoRecebimentoEnum []) values.toArray();
	}	
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest,
			ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public RegistroIntimacao getRegistroIntimacao() {
		return registroIntimacao;
	}
	
	public void setRegistroIntimacao(RegistroIntimacao registroIntimacao) {
		this.registroIntimacao = registroIntimacao;
	}
	
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}
	
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}
	
	public boolean isFormValido() {
		return validarCampos();
	}
	
	public boolean isRegistroIntimacaoFinalizado() {
		return registroIntimacaoFinalizado;
	}
	
	public void setRegistroIntimacaoFinalizado(
			boolean registroIntimacaoFinalizado) {
		this.registroIntimacaoFinalizado = registroIntimacaoFinalizado;
	}
}
