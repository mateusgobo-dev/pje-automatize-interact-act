/*
 * ComunicacaoEntreInstanciasAction.java
 * 
 * Data: 13/06/2016
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Action responsável pelas funcionalidades da comunicação entre instâncias.
 * 
 * @author Adriano Pamplona
 */
@Name(ComunicacaoEntreInstanciasAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ComunicacaoEntreInstanciasAction extends TramitacaoFluxoAction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "comunicacaoEntreInstanciasAction";

	@In (create = true, required = true)
	private EnderecoWsdlManager enderecoWsdlManager;
	
	@In (create = true, required = true)
	private ProcessoJudicialAction processoJudicialAction;
	
	@In (create = true, required = true)
	private FluxoManager fluxoManager;
	
	@In (create = true, required = true)
	private FluxoEL fluxoEL;
	
	private EnderecoWsdl enderecoWsdl;
	
	private String numeroProcesso;
	
	private static Map<String, String> prms;
	
    @In (required = false)
	@Out (required = false)
	private Boolean loginDeServico = Boolean.TRUE;
    
    @In (required = false)
    @Out (required = false)
    private UsuarioLogin usuarioLogin;
	
    @Create
    public void init() {
    	// Se o objeto "TaskInstance" não é nulo...
    	if (!tramitacaoProcessualService.isNullTaskInstance()) {
    		super.init();
    		
        	// Carregar os dados necessários para a comunicação entre instâncias
        	carregarDadosComunicacaoEntreInstancias();    		
    	}
    }

	/**
	 * @param processoTrf ProcessoTrf
	 * @return Retorna true se o ProcessoTrf for originado de outra instância.
	 */
	public Boolean isProcessoComOrigemEmOutraInstancia(ProcessoTrf processoTrf) {
		EnderecoWsdl enderecoWsdl = getEnderecoWsdlManager().obterEnderecoWsdl(processoTrf, false);
		return (enderecoWsdl != null);
	}
	
	/**
	 * Inicia o fluxo de comunicação entre instâncias.
	 * É preciso que exista a configuração do tipo de documento entre instâncias e que o tipo 
	 * de documento esteja configurado para iniciar o fluxo definido.
	 */
	public void iniciarFluxoComunicacaoEntreInstancias(){
		try {
			validarExistenciaTipoDocumentoComunicacaoEntreInstancias();			
			Fluxo fluxo = ParametroUtil.instance().getFluxoComunicacaoEntreInstancias();
			
			if (fluxo != null) {
				// Definir variável no fluxo que informe o início de um fluxo de comunicação entre instâncias
				Map<String, Object> variaveis = new HashMap<String, Object>();
				variaveis.put(Variaveis.PJE_FLUXO_INICIO_COMUNICACAO_ENTRE_INSTANCIAS, true);
				
				// Iniciar o novo fluxo
				getProcessoJudicialAction().iniciarNovoFluxo(
						fluxo.getCodFluxo(), 
						null, 
						"A atividade de comunicação entre instâncias foi criada e está pendente de execução pelo responsável.", 
						"Houve um erro ao tentar criar a atividade de comunicacação entre instâncias.",
						variaveis);
			}
		}catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getMessage());
		}
	}
	
	/**
	 * Valida se o tipo de documento usado na configuração entre instâncias foi configurado
	 * na tabela de paramentros.
	 * @throws PJeBusinessException 
	 */
	protected void validarExistenciaTipoDocumentoComunicacaoEntreInstancias() throws PJeBusinessException {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias();
		if (tipo == null) {
			String mensagem = "Não há tipo de documento configurado para 'Comunicação entre Instâncias'.";
			throw new PJeBusinessException(mensagem);
		}
	}

	/**
	 * Método responsável por enviar a comunicação para o processo especificado
	 */
	public void enviarComunicacao() {
		// Se o número do processo não foi informado...
		if (!StringUtils.isNotBlank(numeroProcesso)) {
			facesMessages.add(Severity.INFO, "Informe o número do processo no qual a comunicação será entregue.");
			return;
		}
		
		try {
			if (!getLoginDeServico()) {
				enderecoWsdl.setLogin(getUsuarioLogin().getLogin());
				enderecoWsdl.setSenha(getUsuarioLogin().getSenha());
			}
			
			ProcessoTrf processoTrf = getProcessoJudicialAction().getProcessoJudicial();
			ProcessoTrfManifestacaoProcessual manifestacaoProcessual = 
					fluxoEL.criarNovoProcessoTrfManifestacaoProcessualDoProcesso(enderecoWsdl, processoTrf, numeroProcesso);			

			// Enviar a comunicação para o processo especificado pelo usuário
			Boolean resultado = fluxoEL.enviarDocumentoParaInstanciaDeOrigemSeComunicacaoEntreInstancias(enderecoWsdl, manifestacaoProcessual, null);
			
			// Se a comunicação foi entregue com sucesso...
			if (resultado) {
				String transicaoSaida = (String)TaskInstanceUtil.instance().getVariable("frameDefaultLeavingTransition");
				
				// Se existe transição de saída cadastrada...
				if (transicaoSaida != null) {
					TaskInstanceHome.instance().end(transicaoSaida);
				} else {
					facesMessages.add(Severity.INFO, "Comunicação enviada com sucesso.");
				}
			} else {
				facesMessages.add(Severity.ERROR, "Não foi possível enviar a comunicação.");
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Não foi possível enviar a comunicação. {0}: {1}.", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}
	
	/**
	 * @return EnderecoWsdlManager.
	 */
	protected EnderecoWsdlManager getEnderecoWsdlManager() {
		return enderecoWsdlManager;
	}
	
	/**
	 * @return processoJudicialAction.
	 */
	protected ProcessoJudicialAction getProcessoJudicialAction() {
		return processoJudicialAction;
	}

	/**
	 * @return fluxoManager.
	 */
	protected FluxoManager getFluxoManager() {
		return fluxoManager;
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
	public EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}
	
	public void setEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
		if (enderecoWsdl != null) {
			getUsuarioLogin().setLogin(enderecoWsdl.getLogin());
			getUsuarioLogin().setSenha(enderecoWsdl.getSenha());
		} else {
			setUsuarioLogin(null);
		}
	}
	
	public List<EnderecoWsdl> getEnderecoWsdlItems() {
		try {
			return getEnderecoWsdlManager().findAll();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Não foi possível enviar a comunicação. {0}: {1}.", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
		
		return null;
	}

	/**
	 * Método responsável por carregar os dados necessários para a comunicação entre instâncias
	 */
	private void carregarDadosComunicacaoEntreInstancias() {		
		try {
			// Recuperar a última manifestação processual entregue no processo
			ProcessoTrfManifestacaoProcessual processoManifestacao = fluxoEL.obterProcessoTrfManifestacaoProcessualDoProcesso();		
			
			if (processoManifestacao != null) {
				numeroProcesso = processoManifestacao.getNumeroProcessoManifestacao();
				setEnderecoWsdl(processoManifestacao.getEnderecoWsdl());
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Não foi possível enviar a comunicação. {0}: {1}.", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}
	
	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}
	
	/**
	 * Exibe os campos de login/senha para que o usuário entre com suas credenciais da requisição MNI.
	 */
	public void atribuirLoginUsuario() {
		EnderecoWsdl wsdl = getEnderecoWsdl();
		if (wsdl != null) {
			setUsuarioLogin(null);
			
			setLoginDeServico(Boolean.FALSE);
		}
	}
	
	/**
	 * Oculta os campos de login/senha e faz uso do login de serviço para a requisição MNI.
	 */
	public void atribuirLoginAdministrador() {
		EnderecoWsdl wsdl = getEnderecoWsdl();
		if (wsdl != null) {
			setUsuarioLogin(null);
			
			EntityUtil.evict(wsdl);
			wsdl = EntityUtil.refreshEntity(wsdl);
			setEnderecoWsdl(wsdl);
			
			setLoginDeServico(Boolean.TRUE);
		}
	}

	/**
	 * @return loginDeServico.
	 */
	public Boolean getLoginDeServico() {
		if (loginDeServico == null) {
			loginDeServico = Boolean.TRUE;
		}
		return loginDeServico;
	}

	/**
	 * @param loginDeServico Atribui loginDeServico.
	 */
	public void setLoginDeServico(Boolean loginDeServico) {
		this.loginDeServico = loginDeServico;
	}

	/**
	 * @return usuarioLogin.
	 */
	public UsuarioLogin getUsuarioLogin() {
		if (usuarioLogin == null) {
			usuarioLogin = new UsuarioLogin();
		}
		return usuarioLogin;
	}

	/**
	 * @param usuarioLogin Atribui usuarioLogin.
	 */
	public void setUsuarioLogin(UsuarioLogin usuarioLogin) {
		this.usuarioLogin = usuarioLogin;
	}
}