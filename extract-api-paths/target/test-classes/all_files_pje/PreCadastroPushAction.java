package br.com.infox.pje.action;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.service.EmailService;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.ReCaptchaService;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PreCadastroPushAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PreCadastroPushAction {
	public static final String NAME = "preCadastroPushAction";

	private final String SUBJECT = "Confirmação de Pré-cadastro (Pje PUSH).";
	
	private String nrDocumento;
	private String email;
	private String confirmaEmail;
	private boolean documentoCPF = true;
	private boolean exibeModal;

	@In
	private EmailService emailService;
	
	@In
	private CadastroTempPushManager cadastroTempPushManager;
	
	@In
	private UsuarioManager usuarioManager;
	
	/**
	 * Método responsável por realizar o cadastro temporário do usuário no serviço push e enviar email de confirmação.
	 */
	public void confirmar() {
		if (ParametroUtil.instance().isReCaptchaAtivo() && 
				!ReCaptchaService.instance().validarResposta((String)Util.getRequestParameter("g-recaptcha-response"))) {
			
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "captcha.invalidCaptcha");
		} else {
			if(isDadosValidos()){
				if (isCadastroValido()) {
					TipoDocumentoIdentificacao tipoDocumentoIdentificacao = EntityUtil.getEntityManager().find(
							TipoDocumentoIdentificacao.class, this.documentoCPF ? "CPF" : "CPJ");
					
					try {
						CadastroTempPush cadastroTempPush = cadastroTempPushManager.criarNovoCadastro(
								this.email, this.nrDocumento, tipoDocumentoIdentificacao);
						
						cadastroTempPushManager.persistAndFlush(cadastroTempPush);
						FacesMessages.instance().add(Severity.INFO, "Pré-cadastro realizado com sucesso.");
						
						Usuario usuario = new Usuario();
						usuario.setEmail(cadastroTempPush.getDsEmail());
						
						this.emailService.enviarEmail(usuario, this.SUBJECT, this.montarTextoEmail(
								this.criarLinkConfirmacaoCadastro(cadastroTempPush)));
						
						inicializarVariaveis();
					} catch (PJeBusinessException ex) {
						FacesMessages.instance().add(
								Severity.ERROR, String.format("Ocorreu um erro no cadastro do serviço PJe PUSH. %s", ex.getMessage()));
					}
				}
			}
		}
	}
	
	/**
	 * Método responsável por validar os dados informados pelo usuário.
	 * 
	 * @return Verdadeiro se os dados são válidos. Falso, caso contrário.
	 */
	private boolean isDadosValidos() {
		if (!isNumeroDocumentoValido()) {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.push.numeroDocumentoInvalido"));
			return false;			
		}
		
		if (!isEmailConfirmacaoValido()) {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.push.emailConfirmacaoInvalido"));
			return false;
		}
		
		return true;
	}
	
	/**
	 * Método responsável por validar o número do documento informado pelo usuário.
	 * 
	 * @return Verdadeiro se o número do documento for válido. Falso, caso contrário.
	 */
	private boolean isNumeroDocumentoValido() {
		try {
			if (this.documentoCPF) {
				return InscricaoMFUtil.verificaCPF(this.nrDocumento);
			}
			return InscricaoMFUtil.verificaCNPJ(this.nrDocumento);
		} catch(IllegalArgumentException ex) {
			return false;
		}
	}
	
	/**
	 * Método responsável por verificar a igualdade dos valores informados pelo usuário nos dois campos de email.
	 * 
	 * @return Verdadeiro se os valores forem iguais. Falso, caso contrário.
	 */
	private boolean isEmailConfirmacaoValido() {
		if (StringUtils.equals(this.email, this.confirmaEmail)){
			return true;
		}
		return false;
	}
	
	
	/**
	 * Método responsável por verificar se as informações do pré cadastro fornecidas pelo usuário são válidas.
	 * 
	 * @return Verdadeiro se as informações do pré cadastro são válidas. Falso, caso contrário.
	 */
	private boolean isCadastroValido() {
		return !isNrDocumentoCadastrado() && !isEmailCadastrado() && !isUsuarioAtivoPJe();
	}
	
	/**
	 * Método responsável por verificar se o número do documento informado pelo usuário encontra-se cadastrado no serviço push.
	 * 
	 * @return Verdadeiro se o número do documento encontra-se cadastrado no serviço push. Falso, caso contrário.
	 */
	private boolean isNrDocumentoCadastrado() {
		CadastroTempPush cadastroTempPush = cadastroTempPushManager.recuperarCadastroTempPushByLogin(this.nrDocumento);
		if (cadastroTempPush != null) {
			if (cadastroTempPush.getConfirmado()) {
				FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "pje.push.usuarioCadastrado"));
			} else {
				this.exibeModal = true;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Método responsável por verificar se o email informado pelo usuário encontra-se cadastrado no serviço push.
	 * 
	 * @return Verdadeira se o email encontra-se cadastrado no serviço push. Falso, caso contrário.
	 */
	private boolean isEmailCadastrado() {
		CadastroTempPush cadastroTempPush = cadastroTempPushManager.recuperarCadastroTempPushByLogin(this.email);
		if(cadastroTempPush != null){
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.push.emailCadastrado"));
			return true;
		}
		return false;
	}
	
	/**
	 * Método responsável por verificar se o número do documento informado pelo usuário pertence a algum usuário ativo do PJe.
	 * 
	 * @return Verdadeiro se o número do documento pertence a algum usuário ativo do PJe. Falso, caso contrário.
	 */
	private boolean isUsuarioAtivoPJe() {
		
		if (usuarioManager.isUsuarioAtivoPje(this.nrDocumento)) {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.push.cadastroNaoRealizado"));
			return true;
		}
		return false;
	}

	/**
	 * Método responsável por reenviar o email de confirmação de cadastro no serviço push.
	 */
	public void reenviarEmail() {
		CadastroTempPush cadastroTempPush = this.cadastroTempPushManager.recuperarCadastroTempPushByLogin(this.nrDocumento);
		if (cadastroTempPush.getDsEmail().equals(this.email) || !isEmailCadastrado()) {
			this.cadastroTempPushManager.atualizarCadastro(cadastroTempPush, this.email);
			try {
				this.cadastroTempPushManager.persistAndFlush(cadastroTempPush);
				
				Usuario usuario = new Usuario();
				usuario.setEmail(cadastroTempPush.getDsEmail());
				
				this.emailService.enviarEmail(usuario, this.SUBJECT, this.montarTextoEmail(this.criarLinkConfirmacaoCadastro(cadastroTempPush)));
				inicializarVariaveis();
			} catch (PJeBusinessException ex) {
				FacesMessages.instance().add(
						Severity.ERROR, String.format("Ocorreu um erro na atualização do cadastro no serviço PJe PUSH. %s", ex.getMessage()));
			}			
		}

	}
	
	/**
	 * Método responsável por criar o link de confirmação de cadastro o qual o usuário deverá acessar para confirmar o seu cadastro no push. 
	 * 
	 * @param cadastroTempPush Objeto que contém as informações temporárias do usuário no cadastro do push.
	 * @return O link de confirmação de cadastro o qual o usuário deverá acessar para confirmar o seu cadastro no push.
	 */
	private String criarLinkConfirmacaoCadastro(CadastroTempPush cadastroTempPush) {
		return new Util().getUrlProject() + "/Push/confirmarCadastro.seam?hash=" + cadastroTempPush.getCdHash();
	}
	
	/**
	 * Método responsável por montar o texto do email que será enviado ao usuário.
	 * 
	 * @param link Link de confirmação do cadastro.
	 * 
	 * @return Texto do email que será enviado ao usuário.
	 */
	private String montarTextoEmail(String link) {
		final String linkFaleConosco = new Util().getUrlProject()+"/faleConosco.seam";
		
		StringBuilder textoEmail = new StringBuilder("Por favor, leia esta mensagem com atenção:<br/>");
		textoEmail.append(String.format("Você realizou um pré-cadastro no serviço PJe Push sob a conta de e-mail: %s <br/><br/>", email));
		textoEmail.append("Para completar seu cadastro e receber mensagens do PJe Push, é necessária a confirmação da sua assinatura clicando no endereço abaixo.");
		textoEmail.append(String.format("<a href='%s'>%s</a><br/><br/>", link, link));
		textoEmail.append(String.format("Você será redirecionado a uma página no Portal do %s para completar o seu cadastro.", ParametroUtil.getParametro(Parametros.NOME_SECAO_JUDICIARIA).toUpperCase()) );
		textoEmail.append("Caso você não seja redirecionado, copie e cole o link na barra de endereço do seu navegador, pressionando a tecla Enter em seguida.<br/><br/>");
		textoEmail.append("<b>Para a sua segurança, atenção:</b><br/>");
		textoEmail.append(String.format("<li>Certifique-se que a página para onde você foi redirecionado é realmente do Portal da %s;</li>", ParametroUtil.getParametro("subNomeSistema")));
		textoEmail.append("<li>Favor desconsiderar esta mensagem caso você não tenha solicitado essa operação. Em caso de persistência, favor entrar em contato conforme as instruções abaixo.</li><br/>");
		textoEmail.append("Este é um e-mail automático. Por favor não responda.<br/>");
		textoEmail.append(String.format("Para entrar em contato, utilize o serviço 'Fale Conosco' situado na página de acesso ao PJe Push no site <a href='%s'>%s</a><br/>", linkFaleConosco, linkFaleConosco));
		textoEmail.append("<b>Este é um serviço meramente informativo, não tendo, portanto, cunho oficial.</b>");
		
		return textoEmail.toString();
	}

	/**
	 * Método responsável sinalizar que a modal será fechada.
	 */
	public void fecharModal() {
		this.exibeModal = false;
	}
	
	/**
	 * Método responsável por inicializar os valores das variáveis de instância da classe.
	 */
	private void inicializarVariaveis() {
		this.nrDocumento = null;
		this.email = null;
		this.confirmaEmail = null;
		this.exibeModal = false;
	}
	
	// GETTERs AND SETTERs
	
	public String getNrDocumento() {
		return nrDocumento;
	}

	public void setNrDocumento(String nrDocumento) {
		if (!nrDocumento.equals(StringUtil.CPF_EMPTYMASK) && !nrDocumento.equals(StringUtil.CNPJ_EMPTYMASK)) {
			this.nrDocumento = nrDocumento;
		}
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getConfirmaEmail() {
		return confirmaEmail;
	}

	public void setConfirmaEmail(String confirmaEmail) {
		this.confirmaEmail = confirmaEmail;
	}

	public boolean isDocumentoCPF() {
		return documentoCPF;
	}

	public void setDocumentoCPF(boolean documentoCPF) {
		this.documentoCPF = documentoCPF;
	}

	public boolean isExibeModal() {
		return exibeModal;
	}
	
}
