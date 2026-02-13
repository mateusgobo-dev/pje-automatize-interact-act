package br.com.infox.ibpm.home;

import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.Hibernate;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.IdentityStore;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.util.Strings;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore.SerializableKeycloakAccount;
import org.keycloak.adapters.spi.KeycloakAccount;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.AlertaProximidadeExpiracaoCertificado;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.CertificadoLog;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.component.ControlePaginaInicialUsuario;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.util.ExceptionUtil;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.infox.utils.Constantes;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.certificado.CertificadoPessoaFisica;
import br.jus.cnj.certificado.CertificadoPessoaJuridica;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.entidades.vo.ParametroEventoRegistroLoginVO;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.identity.PjeIdentity;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.LogAcessoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLoginManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioMobileManager;
import br.jus.cnj.pje.nucleo.service.CertificadoDigitalService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.ReCaptchaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.PjeIdentityUtil;
import br.jus.cnj.pje.view.ArquivoAssinadoUpload;
import br.jus.cnj.pje.view.CadastroUsuarioAction;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.cnj.pje.webservice.criminal.dto.PjeUser;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import br.jus.pje.nucleo.entidades.acesso.TokenSso;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(Authenticator.NAME)
@Install(precedence = Install.APPLICATION)
public class Authenticator{
	private static final String CLIENT_SECRET = "client_secret";
	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_CREDENTIALS = "client_credentials";
	private static final String GRANT_TYPE = "grant_type";
	public static final String SESSAO_TEXTO_ASSINATURA = "sessao.textoAssinatura";
	public static final String NAME = "authenticator";
	private static final LogProvider log = Logging.getLogProvider(Authenticator.class);
	private String newPassword1;
	private String newPassword2;
	private String login;
	private String assinatura;
	private String certChain;
	private String certChainStringLog;
	private boolean isWS = false;
	private boolean logouComCertificado;
	private boolean ssoAuthenticationInProgress = false;
	private boolean bearerTokenAuthentication = false;
	private Integer bearerUsuarioLocalizacao;
	
	// Variaveis de sessão
	public static final String USUARIO_BNMP_LOGADO = "usuarioBnmpLogado";
	public static final String PAPEIS_USUARIO_LOGADO = "papeisUsuarioLogado";
	public static final String USUARIO_LOGADO = "usuarioLogado";
	public static final String PESSOA_PUSH_LOGADA = "pessoaPushLogada";
	public static final String USUARIO_LOCALIZACAO_ATUAL = "usuarioLogadoLocalizacaoAtual";
	public static final String ID_LOCALIZACAO_ATUAL = "idLocalizacaoAtual";
	public static final String ID_LOCALIZACAO_MODELO_ATUAL = "idLocalizacaoModeloAtual";
	public static final String ID_PROCURADORIA_ATUAL = "idProcuradoriaAtual";
	public static final String TIPO_PROCURADORIA_ATUAL = "tipoProcuradoriaAtual";
	public static final String USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL = "usuarioLogadoLocalizacaoMagistradoServidorAtual";
	public static final String USUARIO_LOCALIZACAO_LIST = "usuarioLocalizacaoList";
	public static final String USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_LIST = "usuarioLocalizacaoMagistradoServidorList";
	public static final String INDENTIFICADOR_PAPEL_ATUAL = "identificadorPapelAtual";
	public static final String ID_PAPEL_ATUAL = "idPapelAtual";
	public static final String IDENTIFICADOR_PAPEL_ATUAL = "identificadorPapelAtual";
	public static final String LOCALIZACOES_FILHAS_ATUAIS = "localizacoesFilhasAtuais";
	public static final String ID_LOCALIZACOES_FILHAS_ATUAIS = "idLocalizacoesFilhasAtuais";
	public static final String ID_LOCALIZACOES_FILHAS_ATUAIS_LIST = "idLocalizacoesFilhasAtuaisList";
	public static final String ORGAOJULGADOR_ATUAL = "orgaoJulgadorAtual";
	public static final String ORGAOJULGADOR_COLEGIADO_ATUAL = "orgaoJulgadorColegiadoAtual";
	public static final String SERVIDOR_EXCLUSIVO_COLEGIADO = "isServidorExclusivoColegiado";
	public static final String ORGAOJULGADOR_CARGO_ATUAL = "orgaoJulgadorCargoAtual";
	public static final String ATUACAO_PROCURADORIA_USUARIO_LOGADO = "atuacaoProcuradoriaUsuarioLogado";
	public static final String IP_PJE2 = "ipPje2";
	public static final String PORTA_PJE2 = "portaPje2";
	public static final String TEM_VISIBILIDADE = "temVisibilidade";
	public static final String TEM_ORGAO_VISIVEL = "temOrgaoVisivel";
	public static final String LOCALIZACAO_ORGAOJULGADOR_ATUAL = "localizacaoOrgaoJulgadorAtual";
	public static final String AUTENTICACAO_WS = "autenticacaoWS";
	public static final String AUTENTICACAO_SSO = "autenticacaoSSO";
	public static final String SSO_LOGIN_COM_CERTIFICADO = "loginComCertificado";
	public static final String CENTRAL_AUDIENCIA_CUSTODIA = "Central de Audiência de Custódia";
	public static final String LOGIN_DOMICILIO = "domicilio";
	
	
	// Eventos
	public static final String SET_USUARIO_LOCALIZACAO_LIST_EVENT = "authenticator.setUsuarioLocalizacaoListEvent";
	public static final String SET_USUARIO_LOCALIZACAO_EVENT = "authenticator.setUsuarioLocalizacaoEvent";
	public static final Integer ID_ATENDENTE_N1 = 5978;
	
	@Factory(autoCreate=true, value=SESSAO_TEXTO_ASSINATURA, scope=ScopeType.SESSION)
	public String getTextoAssinatura(){
		String texto = RandomStringUtils.random(32, 0, 0, true, true, null, new SecureRandom());
		return texto;
	}
	
	/**
	 * @return token de login que sera disponibilizado via requisicao Ajax (Seam remoting)
	 */
	@WebRemote
	public String getTextoAssinaturaRemote() {
		return Util.instance().eval(SESSAO_TEXTO_ASSINATURA);
	}

	public String getNewPassword1(){
		return newPassword1;
	}

	public static Authenticator instance(){
		return ComponentUtil.getComponent(Authenticator.NAME);
	}

	public void setNewPassword1(String newPassword1){
		if (this.newPassword1 == null || !this.newPassword1.equals(newPassword1)){
			this.newPassword1 = newPassword1;
		}
	}

	public String getNewPassword2(){
		return newPassword2;
	}

	public void setNewPassword2(String newPassword2){
		if (this.newPassword2 == null || !this.newPassword2.equals(newPassword2)){
			this.newPassword2 = newPassword2;
		}
	}

	@Observer(Identity.EVENT_POST_AUTHENTICATE)
	public void postAuthenticate() throws LoginException, PJeBusinessException{
		String username = Identity.instance().getCredentials().getUsername();
		try {
			postAuthenticate(username);	
			this.setCloudCookies();
			Usuario usuario = this.obterUsuario(username);
			if(isSSOAuthentication() && usuario.getAtualizaSso()) {
				this.atualizarGruposSSO(usuario.getLogin());
				this.getUsuarioService().normalizaCadastroSSO(usuario);
			}
		}catch (LoginException e) {
			Identity.instance().logout();
			if(!redirectToCallbackLogin(e.getMessage())) {
				throw e;
			}
		}catch(Exception e) {
			Identity.instance().logout();
			String mensagem = "Erro ao realizar a autenticação do usuário: " + e.getMessage();
			
			if(!redirectToCallbackLogin(e.getMessage())) {
				throw new RuntimeException(e);
			}else {
				log.error(mensagem);
				log.error(e);
			}
		}
	}

	private void atualizarGruposSSO(String username) {
		KeycloakServiceClient keycloakServiceClient = ComponentUtil.getComponent(KeycloakServiceClient.NAME);
		keycloakServiceClient.syncUserGroups(Identity.instance(), username);

		SerializableKeycloakAccount account = (SerializableKeycloakAccount) Contexts.getSessionContext().get(KeycloakAccount.class.getName());
		account.getKeycloakSecurityContext().refreshExpiredToken(false);

		if(PjeUtil.instance().isAutenticacaoSSOSemIframe() ) {
			ControlePaginaInicialUsuario.instance().redirectToPainel();
		}else {
			ControlePaginaInicialUsuario.instance().redirectToSSOCallback();
		}
		this.setSsoAuthenticationInProgress(false);
	}
	
	private void setCloudCookies() {
		FacesContext context = FacesContext.getCurrentInstance();
		if(context != null){
			PjeUtil.instance().setCookie(ConfiguracaoIntegracaoCloud.getAppName().toUpperCase() + "-StickySessionRule", PjeEurekaRegister.instance().getInstanceId(), -1);
			
			if(isSSOAuthentication() && !isBearerTokenAuthentication()) {
				KeycloakSecurityContext ksc = (KeycloakSecurityContext)Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME);
				PjeUtil.instance().setCookie(Constantes.SSO_COOKIE_NAME, ksc.getIdTokenString(), -1);				
			} else {
				PjeUtil.instance().setCookie(Constantes.SSO_COOKIE_NAME, "", 0);
			}
			
			HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
			HttpServletResponse res = (HttpServletResponse) context.getExternalContext().getResponse();
			Cookie[] cookies = req.getCookies();
			if(cookies != null) {
				for (Cookie cookie : cookies) {
					if(!this.shouldBypassCookie(cookie.getName())) {
						res.addHeader("Set-cookie", PjeUtil.instance().toRFC2965HeaderString(cookie));
					}
				}
			}
		}		
	}
	
	private boolean shouldBypassCookie(String cookieName) {
		boolean bypass = false;
		
		if(cookieName.equals(KeycloakServiceClient.STATE_COOKIE_NAME)) {
			bypass = true;
		}
		
		return bypass;
	}
	
	public void postAuthenticate(String login) throws LoginException{
		String errorMessage = null;		
		if (login != null){
			Usuario usuario = getUsuarioService().findByLogin(login);
			if (usuario == null){
				errorMessage = "O usuário '" + login + "' não está corretamente cadastrado no sistema.";
			} else if (!usuario.getAtivo()){
				errorMessage = "O usuário " + usuario.getNome() + " não está ativo.";
			}
			
			if(usuario != null && !logouComCertificado){
				if(usuario.getStatusSenha() == StatusSenhaEnum.I){
					errorMessage = "Senha inativa. Use o link enviado ao seu email ou solicite uma nova senha";
				} else if(usuario.getStatusSenha() == StatusSenhaEnum.B){
					errorMessage = "A senha foi bloqueada. Solicite uma nova senha para efetuar o desbloqueio";
				}else if(usuario.getDataValidadeSenha() == null || usuario.getDataValidadeSenha().before(Util.getDataAtualFormatada("dd/MM/yyyy"))){
					errorMessage = "Senha expirada. Solicite uma nova senha";
				}
			}

			if (getNewPassword2() != null && !getNewPassword2().trim().isEmpty()){
				String msg = trocarSenha(usuario);
				if (!msg.equals("")){
					errorMessage = msg;
				}
			} else{
				assinatura = null;
			}

			if (errorMessage == null){
				setUsuarioLogadoSessao(usuario);
				((PjeIdentity)Identity.instance()).setLogouComCertificado(logouComCertificado);
				selecionarLocalizacaoAtual(usuario);
				verificarProximidadeExpiracaoCertificadoDigital(usuario, logouComCertificado);
				Util.commitTransction();
				Actor.instance().setId(usuario.getLogin());
				boolean deveBloquearSenha = verificarIndicacaoBloqueioSenhaPeloSSO(usuario);
				registraLogon(usuario, deveBloquearSenha);
			}else{
				Identity.instance().unAuthenticate();
				log.error(errorMessage + " - " + login);
				throw new LoginException(errorMessage);
			}
			
		}
	}
	
	private void registraLogon(Usuario usuario, Boolean deveBloquearSenha) {
		Boolean inicializaFalhasAutenticacao = usuario.getFalhasSucessivas() != 0;
		ParametroEventoRegistroLoginVO parametroVo = new ParametroEventoRegistroLoginVO();
		parametroVo.setIdUsuario(usuario.getIdUsuario());
		parametroVo.setInicializaFalhasAutenticacao(inicializaFalhasAutenticacao);
		parametroVo.setIp(LogUtil.getIpRequest(100));
		parametroVo.setDeveBloquearSenha(deveBloquearSenha);
		parametroVo.setLogouComCertificado(Authenticator.isLogouComCertificado());
		parametroVo.setTemCertificado(usuario.getTemCertificado());
		Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_LOGIN_SSO_REGISTRAR, parametroVo);
	}

	public String getAlertasUsuario(){
		Usuario usuario = getUsuarioLogado();
		String mensagem = "";
		if (usuario != null) {
			int qdeDiasExpirarSenha = usuario.getDiasExpirarSenha();		
			if(qdeDiasExpirarSenha > 0 && qdeDiasExpirarSenha <= 45){
				mensagem = "Seu acesso via CPF/CNPJ e senha expira em "+qdeDiasExpirarSenha+" dias. Solicite nova senha na tela de login do PJe.";
			}else if(qdeDiasExpirarSenha == 0){
				mensagem = "Atenção! Seu acesso via CPF/CNPJ e senha expira hoje. Solicite nova senha na tela de login do PJe.";
			}
		}
		
		
		return mensagem;
	}
	
	public void authenticateSSO() {
		KeycloakSecurityContext ksc = (KeycloakSecurityContext)Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME);
		this.authenticateSSO(true, ksc, null);
	}
	
	public void authenticateSSO(boolean loginPage, KeycloakSecurityContext ksc, Integer idUsuarioLocalizacao) {
		if(!Identity.instance().isLoggedIn() && ksc != null) {

			String username = (String)ksc.getToken().getPreferredUsername();
			IdentityManager identityManager = IdentityManager.instance();
			boolean userExists = identityManager.getIdentityStore().userExists(username);
			if(ksc.getToken().getOtherClaims().get(SSO_LOGIN_COM_CERTIFICADO) != null) {
				this.logouComCertificado = (Boolean)ksc.getToken().getOtherClaims().get(SSO_LOGIN_COM_CERTIFICADO);
			}

			if(loginPage){
				if(username != null && !username.isEmpty()){
					if (userExists){
						autenticaManualmenteNoSeamSecurity(username);
						this.setSSOAuthentication(true);
						this.setSsoAuthenticationInProgress(true);
						Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE, new Object[1]);
						Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL, new Object[1]);
					} else {
						this.setSSOAuthentication(true);
						Identity.instance().logout();
						CadastroUsuarioAction.carregarVariaveisDeSessao(username, this.dataPadraoCadastro(), null);
						((PjeIdentity)Identity.instance()).setLogouComCertificado(this.logouComCertificado);
						ControlePaginaInicialUsuario.instance().redirectToSSOCadastroCallback();
					}			
				}
			} else if(userExists){
				this.autenticaManualmenteNoSeamSecurity(username);
				this.setBearerTokenAuthentication(true);
				this.setSSOAuthentication(true);
				this.setBearerUsuarioLocalizacao(idUsuarioLocalizacao);
				Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE, new Object[1]);
				Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL, new Object[1]);
			}
		}
	}

	public void login(){
		 
		if(Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME) != null){
			this.setSsoAuthenticationInProgress(true);
			this.authenticateSSO();
		} else if (camposLoginValidos()) {
			bindCamposTelaLogin();
			Identity.instance().login();
		}
	}
	
	private boolean camposLoginValidos() {
		boolean valid = true;
		
        String userName = Util.getRequestParameter("username");
        if (Strings.isEmpty(userName)) {
            valid = false;
            FacesMessages.instance().addToControl("username", StatusMessage.Severity.ERROR, "Campo CPF/CNPJ é obrigatório");
        }
 
        String password = Util.getRequestParameter("password");
        if (Strings.isEmpty(password)) {
            valid = false;
            FacesMessages.instance().addToControl("password", StatusMessage.Severity.ERROR, "Campo senha é obrigatório");
        }
        
        if (ParametroUtil.instance().isReCaptchaAtivo() && 
        		!ReCaptchaService.instance().validarResposta((String)Util.getRequestParameter("g-recaptcha-response"))) {
        	
			valid = false;
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "captcha.invalidCaptcha");
		} 
        
        return valid;
	}
	
	private void bindCamposTelaLogin() {
		String userName = Util.getRequestParameter("username");
		String password = Util.getRequestParameter("password");
		String newPassword1 = Util.getRequestParameter("newPassword1");
		String newPassword2 = Util.getRequestParameter("newPassword2");
		if (newPassword1 != null && newPassword1.trim().equals("")) {
			newPassword1 = null;
			newPassword2 = null;
		}
		String signature = Util.getRequestParameter("signature");
		String certChain = Util.getRequestParameter("certChain");
		String certChainStringLog = Util.getRequestParameter("certChainStringLog");
		
		Credentials credentials = ((Credentials) ComponentUtil.getComponent("org.jboss.seam.security.credentials"));
		credentials.setUsername(userName);
		credentials.setPassword(password);
        
        // Binds dos campos de Trocar Senha
        this.setNewPassword1(newPassword1);
        this.setNewPassword2(newPassword2);
        
        // Bind dos campos de login com certificado
		this.setAssinatura(signature);
		this.setCertChain(certChain);
		this.setCertChainStringLog(certChainStringLog);
		
		if(assinatura != null && !assinatura.trim().isEmpty()){
			logouComCertificado = true;
		}else{
			logouComCertificado = false;
		}

		
		/* se a aplicação estiver em modo teste e o usuário pediu pra logar com
		 * simulação de certificado (usado pelos desenvolvedores) é preciso
		 * setar a variável para que o sistema interprete o modo de login
		 */
		if (!ParametroUtil.instance().isAplicacaoModoProducao()) {
			String loginCertificadoSimulado = Util.getRequestParameter("loginCertificadoSimulado");
			if(Boolean.TRUE.toString().equals(loginCertificadoSimulado)) {
				log.warn(String.format("Aplicação em modo teste e usuário %s realizando login com login+senha simulando acesso com certificado digital", userName));
				logouComCertificado = true;
			}
		}
	}

	@Observer(Identity.EVENT_LOGIN_FAILED)
	public void loginFailed(Object obj) throws LoginException {
		if(obj instanceof Exception) {
			((Exception) obj).printStackTrace();
		}
		try {
			if (Identity.instance().getCredentials().getUsername() != null) {
				UsuarioLogin usuario = getUsuario(Identity.instance().getCredentials().getUsername());
				String inscricaoMF = (String) Util.getRequestParameter("username");
				if (usuario == null && inscricaoMF != null && VerificaCertificado.instance().isModoTesteCertificado()) {
					if (inscricaoMF.length() == 11 && InscricaoMFUtil.verificaCPF(inscricaoMF)) {
						CadastroUsuarioAction.redirectParaCadastro(inscricaoMF, this.dataPadraoCadastro(), null);
						return;
					} else if (InscricaoMFUtil.verificaCNPJ(inscricaoMF)) {
						CadastroUsuarioAction.redirectParaCadastro(inscricaoMF, this.dataPadraoCadastro(), "02184786403");
						return;
					}
				}

				if (usuario != null) {
					if (!usuario.getAtivo()) {
						throw new LoginException("Este usuário não está ativo.");
					}
					try {
						this.getUsuarioService().registraFalhaLogon(usuario);
					} catch (PJeBusinessException ex) {
						throw new LoginException(ex.getLocalizedMessage());
					}

					throw new LoginException("Usuário ou senha inválidos.");
				}
			}
		} catch (Exception e) {
			log.error(String.format("Erro ao realizar login: %s - %s", 
				e.getLocalizedMessage(), Util.getRequestParameter("username")));
			
			throw new LoginException(e.getLocalizedMessage());
		} finally {
			Util.commitTransction();
		}
		
	}

	@Observer(Identity.EVENT_LOGGED_OUT)
	public void limparContexto(){
		Credentials credentials = (Credentials) Component.getInstance(Credentials.class);
		credentials.clear();
		Context context = Contexts.getSessionContext();
		context.remove(USUARIO_BNMP_LOGADO);
		context.remove(USUARIO_LOGADO);
		context.remove(USUARIO_LOCALIZACAO_ATUAL);
		context.remove(ID_LOCALIZACAO_ATUAL);
		context.remove(ID_LOCALIZACAO_MODELO_ATUAL);
		context.remove(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL);
		context.remove(PAPEIS_USUARIO_LOGADO);
		context.remove(INDENTIFICADOR_PAPEL_ATUAL);
		context.remove(ID_PAPEL_ATUAL);
		context.remove(IDENTIFICADOR_PAPEL_ATUAL);
		context.remove(LOCALIZACOES_FILHAS_ATUAIS);
		context.remove(ID_LOCALIZACOES_FILHAS_ATUAIS);
		context.remove(ID_LOCALIZACOES_FILHAS_ATUAIS_LIST);
		context.remove(ORGAOJULGADOR_ATUAL);
		context.remove(ORGAOJULGADOR_COLEGIADO_ATUAL);
		context.remove(SERVIDOR_EXCLUSIVO_COLEGIADO);
		context.remove(ORGAOJULGADOR_CARGO_ATUAL);
		context.remove(USUARIO_LOCALIZACAO_LIST);
		context.remove(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_LIST);
		context.remove(PESSOA_PUSH_LOGADA);
		context.remove(ID_PROCURADORIA_ATUAL);
		context.remove(TIPO_PROCURADORIA_ATUAL);
		context.remove(Variaveis.POSSUI_PENDENCIA_CADASTRO);
		context.remove(TEM_VISIBILIDADE);
		context.remove(TEM_ORGAO_VISIVEL);
		context.remove(LOCALIZACAO_ORGAOJULGADOR_ATUAL);
	}

	public UsuarioLogin getUsuario(String login){
		Usuario user = getUsuarioService().findByLogin(login);
		if(user != null){
			return user;
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<PessoaFisica> getPessoaFisicaList(String cpf){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaFisica o ");
		sb.append("inner join o.pessoaDocumentoIdentificacaoList doc ");
		sb.append("where doc.numeroDocumento = :cpf and doc.tipoDocumento.codTipo = 'CPF' ");
		sb.append("and doc.usadoFalsamente = false");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("cpf", cpf);
		return query.getResultList();
	}

	public void unAuthenticate(){
		
		KeycloakSecurityContext ksc = null;
		
		if(Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME) != null){
			ksc = (KeycloakSecurityContext)Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME);
			if(ksc != null){
				Contexts.getSessionContext().set(Constantes.SSO_CONTEXT_NAME, ksc);
				ControlePaginaInicialUsuario.instance().redirectToPainel();
			}
		} else {		
			Identity.instance().unAuthenticate();
			limparContexto();
			String actorId = Actor.instance().getId();
			anulaActorId(actorId);
		}
		
	}

	public String getAccessToken() {
		RefreshableKeycloakSecurityContext ksc = (RefreshableKeycloakSecurityContext)Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME);
		return ksc != null ? ksc.getTokenString() : null; 
	}
	
	public String getRefreshToken() {
		RefreshableKeycloakSecurityContext ksc = (RefreshableKeycloakSecurityContext)Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME);
		return ksc != null ? ksc.getRefreshToken() : null; 
	}
	/**
	 * Ao encerrar uma sessao, limpa os processos que o servidor estava trabalhando Obs.: usando session do hibernate pq o EM da erro de transação
	 */
	public void anulaActorId(String login){
		if (login != null && !isWS()){
			String query = "update jbpm_taskinstance set actorid_ = null where actorid_ = :actorId and end_ is null";
			HibernateUtil.getSession().createSQLQuery(query)
					.addSynchronizedQuerySpace("jbpm_taskinstance")
					.setParameter("actorId", login).executeUpdate();
		}
		if (isWS()) {
			Context context = Contexts.getSessionContext();
			context.remove(AUTENTICACAO_WS);
		}
	}

	/**
	 * Ao ligar a aplicação, limpa todos os actorIds dos processos
	 */
	@Observer("org.jboss.seam.postInitialization")
	public void anulaTodosActorId(){
		try {
			String query = "update tb_processo set nm_actor_id = null where nm_actor_id is not null";
			HibernateUtil.getSession().createSQLQuery(query)
					.addSynchronizedQuerySpace("tb_processo")
					.executeUpdate();
		} catch (Exception e) {
			log.error("Erro ao executar Authenticator.anulaTodosActorId", e);
		}
	}

	/**
	 * Método responsável por colocar o usuario logado na sessão.
	 * 
	 * @param usuario Usuário logado.
	 */
	private void setUsuarioLogadoSessao(Usuario usuario) {
		inicializarPropriedadesLazy(usuario);
		Contexts.getSessionContext().set(USUARIO_LOGADO, usuario);
		Contexts.getSessionContext().set(USUARIO_LOCALIZACAO_LIST, getLocalizacoes());
	}
	
	/**
	 * Método responsável por inicializar as propriedades lazy do objeto {@link Usuario}.
	 * 
	 * @param usuario {@link Usuario}.
	 */
	private void inicializarPropriedadesLazy(Usuario usuario) {
		Hibernate.initialize(usuario.getCertChain());
 		Hibernate.initialize(usuario.getUsuarioLocalizacaoList());
 		Hibernate.initialize(usuario.getBloqueioUsuarioList());
 		Hibernate.initialize(usuario.getEnderecoList());
 		Hibernate.initialize(usuario.getAssinatura());
 		Hibernate.initialize(usuario.getPapelSet());
	}
	
	private void selecionarLocalizacaoAtual(Usuario usuario) throws LoginException{
		UsuarioLocalizacao usuarioLocalizacaoInicial;
		
		if(this.bearerUsuarioLocalizacao != null) {
			usuarioLocalizacaoInicial = usuario.getUsuarioLocalizacaoList()
													.stream()
													.filter(ul -> ul.getIdUsuarioLocalizacao() == this.bearerUsuarioLocalizacao)
													.findAny()
													.orElse(usuario.getUsuarioLocalizacaoInicial());
		} else {
			usuarioLocalizacaoInicial = usuario.getUsuarioLocalizacaoInicial();
		}
		if (isUsuarioLocalizacaoMagistadoServidor()){
			List<UsuarioLocalizacaoMagistradoServidor> listUsuarioLoc = getUsuarioLocalizacaoMagistradoServidorListItems();
			if (listUsuarioLoc != null && listUsuarioLoc.size() > 0) {
				UsuarioLocalizacaoMagistradoServidor loc = null;
				if (usuarioLocalizacaoInicial != null && usuarioLocalizacaoInicial.getUsuarioLocalizacaoMagistradoServidor() != null) {
					loc = usuarioLocalizacaoInicial.getUsuarioLocalizacaoMagistradoServidor();
				} else {
					loc = listUsuarioLoc.get(0); 
				}
				setLocalizacaoAtual(loc.getUsuarioLocalizacao());
			} else {
				throw new LoginException("O usuário " + usuario + " não possui Localização");
			}
		} else {
			List<UsuarioLocalizacao> listUsuarioLoc = getUsuarioLocalizacaoListItems();
			if (listUsuarioLoc != null && listUsuarioLoc.size() > 0){
				UsuarioLocalizacao loc = null;
				if (usuarioLocalizacaoInicial != null) {
					loc = usuarioLocalizacaoInicial;
				} else {
					loc = listUsuarioLoc.get(0);
				}
				UsuarioLocalizacaoMagistradoServidor localServidor = loc.getUsuarioLocalizacaoMagistradoServidor();
				setLocalizacaoAtual(loc, localServidor);
			} else {
				if (PessoaAdvogadoHome.instance().isAdvogado(usuario.getIdUsuario())) {  
					throw new LoginException("O usuário " + usuario + " não foi credenciado nesse sistema. "
							+ "Para realizar o credenciamento do usuário clique no botão 'Informações' "
							+ "e acesse a opção 'Credenciamento de Advogados'");  
				} else {  
					throw new LoginException("O usuário " + usuario + " não possui Localização");  
				}  
			}
		}
	}

	public String trocarSenha(Usuario usuario){
		if (newPassword1 == null || newPassword1.trim().equals("")){
			return "";
		}
		
		if (!newPassword1.equals(newPassword2)){
			return "As senhas não são coincidentes.";
		}
		
		IdentityStore identityStore = IdentityManager.instance().getIdentityStore();
		try{				
			identityStore.changePassword(usuario.getLogin(), newPassword1);
			org.jboss.seam.transaction.UserTransaction ut = Transaction.instance();
			if (ut != null && ut.isActive()) {
				ut.commit();
			}
			newPassword1 = "";
			newPassword2 = "";
			return "Senha alterada com sucesso.";
		}catch(IllegalArgumentException e){
			return e.getLocalizedMessage();
		}catch (Exception e) {
			return "Houve um erro ao tentar trocar a senha: " + e.getLocalizedMessage();
		}		
	}

	/**
	 * Muda a localização do usuário logado, removendo todos os roles da localização anterior 
	 * (se hover) e atribuindo os roles da nova localização recursivamente.
	 * 
	 * @param loc
	 */
	private void setLocalizacaoAtual(UsuarioLocalizacao loc){
		setLocalizacaoAtual(loc, null);
	}

	@SuppressWarnings("unchecked")
	private void setLocalizacaoAtual(UsuarioLocalizacao loc, UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor){
		Contexts.getSessionContext().set(USUARIO_LOCALIZACAO_ATUAL, loc);
		Localizacao localizacaoFisica = loc.getLocalizacaoFisica();
		if(localizacaoFisica == null) {
			throw new RuntimeException("Usuário " + loc.getUsuario().getLogin() +" não possui uma localização física definida");
		}
		Contexts.getSessionContext().set(ID_LOCALIZACAO_ATUAL, localizacaoFisica.getIdLocalizacao());
		Contexts.getSessionContext().set(ID_LOCALIZACAO_MODELO_ATUAL, loc.getLocalizacaoModelo() != null ? loc.getLocalizacaoModelo().getIdLocalizacao() : 0);
		Contexts.getSessionContext().set(INDENTIFICADOR_PAPEL_ATUAL, loc.getPapel().getIdentificador());
		Contexts.getSessionContext().set(ID_PAPEL_ATUAL, loc.getPapel().getIdPapel());
		Contexts.getSessionContext().set(IDENTIFICADOR_PAPEL_ATUAL, loc.getPapel().getIdentificador());
		List<Localizacao> localizacoesFilhas = getLocalizacaoManager().getArvoreDescendente(loc.getLocalizacaoFisica().getIdLocalizacao(), true);
		Contexts.getSessionContext().set(LOCALIZACOES_FILHAS_ATUAIS, localizacoesFilhas);
		String idsLocalizacoesFilhas = LocalizacaoUtil.converteLocalizacoesList(localizacoesFilhas);
		Contexts.getSessionContext().set(ID_LOCALIZACOES_FILHAS_ATUAIS, idsLocalizacoesFilhas);
		Contexts.getSessionContext().set(ID_LOCALIZACOES_FILHAS_ATUAIS_LIST, CollectionUtilsPje.convertStringToIntegerList(idsLocalizacoesFilhas));

		if(loc.getPapel().equals(ParametroUtil.instance().getPapelProcurador()) || 
				(loc.getPapel().equals(ParametroUtil.instance().getPapelProcuradorGestor()))){
			
			PessoaProcuradoria pessoaProcuradoria = null;
			Usuario usuario = (Usuario) HibernateUtil.deproxy(loc.getUsuario(), Usuario.class);
			ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager) Component.getInstance(ProcuradoriaManager.NAME);
			Procuradoria procuradoriaAtual = procuradoriaManager.recuperaPorLocalizacao(loc.getLocalizacaoFisica());
			
			if (PessoaFisica.class.isAssignableFrom(usuario.getClass()) && ((PessoaFisica)usuario).getPessoaProcurador() != null && procuradoriaAtual != null) {
				PessoaProcurador pessoaProcurador = ((PessoaFisica)usuario).getPessoaProcurador();
				PessoaProcuradorManager pessoaProcuradorManager = (PessoaProcuradorManager) Component.getInstance(PessoaProcuradorManager.NAME);
				PessoaProcuradoriaManager pessoaProcuradoriaManager = (PessoaProcuradoriaManager) Component.getInstance(PessoaProcuradoriaManager.NAME);
				pessoaProcuradoria = pessoaProcuradoriaManager.recuperaPessoaProcuradoria(
							usuario.getIdUsuario(), procuradoriaAtual.getIdProcuradoria());

					try {
						pessoaProcuradorManager.merge(pessoaProcurador);
						pessoaProcuradorManager.flush();
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
			}
			
			if (procuradoriaAtual != null) {
					Contexts.getSessionContext().set(ID_PROCURADORIA_ATUAL, procuradoriaAtual.getIdProcuradoria());
					Contexts.getSessionContext().set(TIPO_PROCURADORIA_ATUAL, procuradoriaAtual.getTipo());
				Contexts.getSessionContext().set(ATUACAO_PROCURADORIA_USUARIO_LOGADO, (pessoaProcuradoria != null ? pessoaProcuradoria.getAtuacaoReal() : RepresentanteProcessualTipoAtuacaoEnum.G));
			} else {
				Contexts.getSessionContext().remove(ID_PROCURADORIA_ATUAL);
				Contexts.getSessionContext().remove(TIPO_PROCURADORIA_ATUAL);
				Contexts.getSessionContext().remove(ATUACAO_PROCURADORIA_USUARIO_LOGADO);
			}
		} else {
			if (loc.getPapel().equals(ParametroUtil.instance().getPapelAssistenteProcuradoria())) {
				Usuario usu = (Usuario) HibernateUtil.deproxy(loc.getUsuario(), Usuario.class);
				
				if(PessoaFisica.class.isAssignableFrom(usu.getClass()) && ((PessoaFisica)usu).getPessoaAssistenteProcuradoria() != null){
					ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager) Component.getInstance("procuradoriaManager");
					Procuradoria procuradoriaAtual = procuradoriaManager.recuperaPorLocalizacao(loc.getLocalizacaoFisica());
					
					Contexts.getSessionContext().set(ID_PROCURADORIA_ATUAL, procuradoriaAtual.getIdProcuradoria());
					Contexts.getSessionContext().set(TIPO_PROCURADORIA_ATUAL, procuradoriaAtual.getTipo());
					Contexts.getSessionContext().set(ATUACAO_PROCURADORIA_USUARIO_LOGADO, RepresentanteProcessualTipoAtuacaoEnum.P);
				}
			} else {
				Contexts.getSessionContext().remove(ID_PROCURADORIA_ATUAL);
				Contexts.getSessionContext().remove(TIPO_PROCURADORIA_ATUAL);
				Contexts.getSessionContext().remove(ATUACAO_PROCURADORIA_USUARIO_LOGADO);
			}
		}
		
		// remove os papeis que estejam na sessao do usuario das roles no objeto identity
		Set<String> roleSet = (Set<String>) Contexts.getSessionContext().get(PAPEIS_USUARIO_LOGADO);

		if (roleSet != null) {
			for (String r : roleSet) {
				Identity.instance().removeRole(r);
			}
		} else {
			ComponentUtil.getComponent(PjeIdentityUtil.class).removeRoles();
		}

		// identifica quais sao as roles necessarias para o usuario e coloca de volta na sessao
		IdentityStore identityStore = ((IdentityStore) Component.getInstance("org.jboss.seam.security.identityStore"));
		roleSet = new HashSet<String>(identityStore.getImpliedRoles(loc.getUsuario().getLogin()));
		for (String r : roleSet){
			Identity.instance().addRole(r);
		}
		Contexts.getSessionContext().set(PAPEIS_USUARIO_LOGADO, roleSet);
		OrgaoJulgador orgaoJulgador = null;
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = null;
		OrgaoJulgadorCargo orgaoJulgadorCargo = null;
		boolean isServidorExclusivoOJC = false;

		if (usuarioLocalizacaoMagistradoServidor == null){
			usuarioLocalizacaoMagistradoServidor = EntityUtil.find(UsuarioLocalizacaoMagistradoServidor.class, loc.getIdUsuarioLocalizacao());
		}
		
		if (usuarioLocalizacaoMagistradoServidor != null){
			Contexts.getSessionContext().set(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL,	usuarioLocalizacaoMagistradoServidor);

			orgaoJulgador = usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador();
			orgaoJulgador = HibernateUtil.deproxy(orgaoJulgador, OrgaoJulgador.class);
					
			orgaoJulgadorColegiado = usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado();
			orgaoJulgadorColegiado = HibernateUtil.deproxy(orgaoJulgadorColegiado, OrgaoJulgadorColegiado.class);

			orgaoJulgadorCargo = usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorCargo();
			orgaoJulgadorCargo = HibernateUtil.deproxy(orgaoJulgadorCargo, OrgaoJulgadorCargo.class);
			
		} else {
			if (loc.getLocalizacaoFisica() != null) {
				orgaoJulgador = consultarOrgaoJulgador(loc.getLocalizacaoFisica());
			}
		}
		isServidorExclusivoOJC = (orgaoJulgador == null && orgaoJulgadorColegiado != null);

		Contexts.getSessionContext().set(USUARIO_BNMP_LOGADO,null);
		Contexts.getSessionContext().set(ORGAOJULGADOR_ATUAL, orgaoJulgador);
		Contexts.getSessionContext().set(ORGAOJULGADOR_COLEGIADO_ATUAL, orgaoJulgadorColegiado);
		Contexts.getSessionContext().set(SERVIDOR_EXCLUSIVO_COLEGIADO, isServidorExclusivoOJC);
		Contexts.getSessionContext().set(ORGAOJULGADOR_CARGO_ATUAL, orgaoJulgadorCargo);
		Contexts.getSessionContext().set(LOCALIZACAO_ORGAOJULGADOR_ATUAL, getLocalizacaoOjOjcUsuarioLogado());
		Contexts.getSessionContext().remove("mainMenu");
		Contexts.removeFromAllContexts("tarefasTree");
		Contexts.removeFromAllContexts("painelUsuarioHome");
		Contexts.removeFromAllContexts("isJusPostulandi");
		Contexts.removeFromAllContexts("pje2:sessao:permissoesHtml");
		Contexts.getSessionContext().remove(TEM_VISIBILIDADE);
		Contexts.getSessionContext().remove(TEM_ORGAO_VISIVEL);
		
		Context conversationContext = Contexts.getConversationContext();
		String[] names = conversationContext.getNames();
		for(String name: names){
			Object component = conversationContext.get(name);
				
			if(component instanceof org.jboss.seam.framework.Query){ 
				conversationContext.remove(name);
			}
		}	
		
		if (isSSOAuthentication() && isSsoAuthenticationInProgress() && !isBearerTokenAuthentication()) {
			this.setSsoAuthenticationInProgress(false);
			if (PjeUtil.instance().isAutenticacaoSSOSemIframe()) {
				ControlePaginaInicialUsuario.instance().redirectToPainel();
			} else {
				ControlePaginaInicialUsuario.instance().redirectToSSOCallback();
			}
		} else if (!isWS && !isBearerTokenAuthentication()){
			ControlePaginaInicialUsuario.instance().redirectToPainel();
		} 
	}

	@SuppressWarnings("unchecked")
	public static List<Localizacao> getLocalizacoesFilhasAtuais(){
		return (List<Localizacao>) Contexts.getSessionContext().get(LOCALIZACOES_FILHAS_ATUAIS);
	}
	
	public static String getIdsLocalizacoesFilhasAtuais() {
		return (String) Contexts.getSessionContext().get(ID_LOCALIZACOES_FILHAS_ATUAIS);
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> getIdsLocalizacoesFilhasAtuaisList() {
		return (List<Integer>) Contexts.getSessionContext().get(ID_LOCALIZACOES_FILHAS_ATUAIS_LIST);
	}
	
	public OrgaoJulgador consultarOrgaoJulgador(Localizacao localizacao){
		String query = "SELECT o FROM OrgaoJulgador o " + "WHERE o.localizacao.idLocalizacao = :idLocalizacao ";

		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("idLocalizacao", localizacao.getIdLocalizacao());
		return EntityUtil.getSingleResult(q);
	}

	/**
	 * @return a UsuarioLocalizacao atual do usuário logado
	 */
	public static UsuarioLocalizacao getUsuarioLocalizacaoAtual() {
		Context context = Contexts.getSessionContext();

		if (context != null) {
			UsuarioLocalizacao usuarioLocalizacao = (UsuarioLocalizacao) context.get(USUARIO_LOCALIZACAO_ATUAL);

			if (usuarioLocalizacao != null) {
				return EntityUtil.getEntityManager().find(UsuarioLocalizacao.class,
						usuarioLocalizacao.getIdUsuarioLocalizacao());
			}
		}

		return null;
	}

	public static Integer getIdUsuarioLocalizacaoAtual(){
		UsuarioLocalizacao ul = (UsuarioLocalizacao) Contexts.getSessionContext().get(USUARIO_LOCALIZACAO_ATUAL);
		if (ul != null){
			return ul.getIdUsuarioLocalizacao();
		}
		return null;
	}		
	
	/**
	 * @param idLocalizacao
	 * @return o UsuarioLocalizacao pela localizacao
	 */
	public static UsuarioLocalizacao getUsuarioLocalizacaoPorIdLocalizacao(Localizacao localizacao) {
		if (localizacao != null && localizacao.getIdLocalizacao() > 0) {
			StringBuilder hql = new StringBuilder();
			hql.append("SELECT ul ");
			hql.append("FROM UsuarioLocalizacao ul ");
			hql.append("WHERE ul.localizacaoFisica.idLocalizacao = :idLocalizacao ");
			hql.append("AND ul.responsavelLocalizacao = true ");
			Query query = EntityUtil.getEntityManager().createQuery(hql.toString());
			query.setParameter("idLocalizacao", localizacao.getIdLocalizacao());
			return EntityUtil.getSingleResult(query);
		}
		return null;
	}
	
	/**
	* Método que retorna a localização do órgão julgador do órgão julgador colegiado
	* ao qual o usuário logado está vinculado.
	* @author Ronny Paterson
	* @since 1.4.7
	* @return Localizacao do órgão julgador colegiado ao qual o usuário logado está vinculado.
	*/
	public static Localizacao getLocalizacaoOjcUsuarioLogado() {
		Localizacao localizacao = null;
		if(!ParametroUtil.instance().isPrimeiroGrau()) {
			if((Authenticator.getOrgaoJulgadorColegiadoAtual() != null)
					&& (Authenticator.getOrgaoJulgadorColegiadoAtual().getLocalizacao() != null)) {
				localizacao = Authenticator.getOrgaoJulgadorColegiadoAtual().getLocalizacao();
			}
		}
		return localizacao;
	}
	
	/**
	 * Método que retorna a localização do órgão julgador ou do órgão julgador colegiado 
	 * ao qual o usuário logado está vinculado.
	 * @author Ronny Paterson
	 * @since 1.4.7
	 * @return Localizacao do órgão julgador ou do órgão julgador colegiado ao qual o usuário logado está vinculado.
	 */
	public static Localizacao getLocalizacaoOjOjcUsuarioLogado() {
		Localizacao localizacao = null;
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			if((Authenticator.getOrgaoJulgadorAtual() != null)
					&& (Authenticator.getOrgaoJulgadorAtual().getLocalizacao() != null)) {
				localizacao = Authenticator.getOrgaoJulgadorAtual().getLocalizacao();
			}
		}else {
			if((Authenticator.getOrgaoJulgadorAtual() != null)
					&& (Authenticator.getOrgaoJulgadorAtual().getLocalizacao() != null)) {
				localizacao = Authenticator.getOrgaoJulgadorAtual().getLocalizacao();
			} else if((Authenticator.getOrgaoJulgadorColegiadoAtual() != null) 
					&& (Authenticator.getOrgaoJulgadorColegiadoAtual().getLocalizacao() != null)) {
				localizacao = Authenticator.getOrgaoJulgadorColegiadoAtual().getLocalizacao();
			}
		}
		return localizacao;
	}
	
	public static Integer getIdLocalizacaoOrgaoJulgadorAtual() {
		Localizacao loc = (Localizacao) Contexts.getSessionContext().get(LOCALIZACAO_ORGAOJULGADOR_ATUAL);
		if (loc != null){
			return loc.getIdLocalizacao();
		}
		return null;
	}
	
	public static Localizacao getLocalizacaoEditorTexto() {
		Localizacao localizacao = null;
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			if((Authenticator.getOrgaoJulgadorAtual() != null)
					&& (Authenticator.getOrgaoJulgadorAtual().getLocalizacao() != null)) {
				localizacao = Authenticator.getOrgaoJulgadorAtual().getLocalizacao();
			}
		}else {
			if((Authenticator.getOrgaoJulgadorAtual() != null)
					&& (Authenticator.getOrgaoJulgadorAtual().getLocalizacao() != null)) {
				localizacao = Authenticator.getOrgaoJulgadorAtual().getLocalizacao();
			} 
		}
		return localizacao;
	}
	
	/**
	 * Método que retorna a localização do órgão julgador ou do órgão julgador colegiado 
	 * ao qual o usuário logado está vinculado.
	 * @author Ronny Paterson
	 * @since 1.4.7
	 * @return Localizacao do órgão julgador ou do órgão julgador colegiado ao qual o usuário logado está vinculado.
	 */
	public static Localizacao getLocalizacaoUsuarioLogado() {
		Localizacao localizacao = getLocalizacaoOjOjcUsuarioLogado();
		if(localizacao == null) {
			localizacao = Authenticator.getLocalizacaoAtual();
		}
		return localizacao;
	}
	
	public static Localizacao getLocalizacaoUsuarioLogadoEditor() {
		Localizacao localizacao =  getLocalizacaoEditorTexto();
		if(localizacao == null) {
			localizacao = Authenticator.getLocalizacaoAtual();
		}
		return localizacao;		
	}

	/**
	 * @return a UsuarioLocalizacaoMagistradoServidor atual do usuário logado
	 */
	public static UsuarioLocalizacaoMagistradoServidor getUsuarioLocalizacaoMagistradoServidorAtual(){
		UsuarioLocalizacaoMagistradoServidor usuarioLocalizacao = (UsuarioLocalizacaoMagistradoServidor) Contexts
				.getSessionContext().get(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL);
		if (usuarioLocalizacao != null){
			usuarioLocalizacao = EntityUtil.getEntityManager().find(UsuarioLocalizacaoMagistradoServidor.class,
					usuarioLocalizacao.getIdUsuarioLocalizacaoMagistradoServidor());
		}
		return usuarioLocalizacao;
	}

	public static OrgaoJulgador getOrgaoJulgadorAtual(){
		OrgaoJulgador oj = (OrgaoJulgador) Contexts.getSessionContext().get(ORGAOJULGADOR_ATUAL);
		if (oj != null){
			oj = EntityUtil.getEntityManager().find(OrgaoJulgador.class, oj.getIdOrgaoJulgador());
		}
		return oj;
	}
	
	public static Integer getIdOrgaoJulgadorAtual(){
		OrgaoJulgador oj = (OrgaoJulgador) Contexts.getSessionContext().get(ORGAOJULGADOR_ATUAL);
		if (oj != null){
			return oj.getIdOrgaoJulgador();
		}
		return null;
	}		

	public static OrgaoJulgadorCargo getOrgaoJulgadorCargoAtual(){
		OrgaoJulgadorCargo ojc = (OrgaoJulgadorCargo) Contexts.getSessionContext().get(ORGAOJULGADOR_CARGO_ATUAL);
		if (ojc != null){
			ojc = EntityUtil.getEntityManager().find(OrgaoJulgadorCargo.class, ojc.getIdOrgaoJulgadorCargo());
		}
		return ojc;
	}

	public static Integer getIdOrgaoJulgadorCargoAtual(){
		OrgaoJulgadorCargo ojc = (OrgaoJulgadorCargo) Contexts.getSessionContext().get(ORGAOJULGADOR_CARGO_ATUAL);
		if (ojc != null){
			return ojc.getIdOrgaoJulgadorCargo();
		}
		return null;
	}		

	public static Boolean isCargoAuxiliar(){
		OrgaoJulgadorCargo ojc = (OrgaoJulgadorCargo) Contexts.getSessionContext().get(ORGAOJULGADOR_CARGO_ATUAL);
		if (ojc != null){
			return ojc.getAuxiliar();
		}
		return null;
	}		

	public static OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoAtual(){
		OrgaoJulgadorColegiado ojc = (OrgaoJulgadorColegiado) Contexts.getSessionContext().get(
				ORGAOJULGADOR_COLEGIADO_ATUAL);
		if (ojc != null){
			ojc = EntityUtil.getEntityManager().find(OrgaoJulgadorColegiado.class, ojc.getIdOrgaoJulgadorColegiado());
		}
		return ojc;
	}
	
	public static Integer getIdOrgaoJulgadorColegiadoAtual(){
		OrgaoJulgadorColegiado ojc = (OrgaoJulgadorColegiado) Contexts.getSessionContext().get(ORGAOJULGADOR_COLEGIADO_ATUAL);
		if (ojc != null){
			return ojc.getIdOrgaoJulgadorColegiado();
		}
		return null;
	}
	
	public static boolean isServidorExclusivoColegiado() {
		Boolean isServidorExclusivoOJC = (Boolean) Contexts.getSessionContext().get(SERVIDOR_EXCLUSIVO_COLEGIADO);
		if (isServidorExclusivoOJC == null){
			isServidorExclusivoOJC = (getIdOrgaoJulgadorAtual() == null && getIdOrgaoJulgadorColegiadoAtual() != null);
			Contexts.getSessionContext().set(SERVIDOR_EXCLUSIVO_COLEGIADO, isServidorExclusivoOJC);
		}
		return isServidorExclusivoOJC;
	}

	/**
	 * Atalho para a localização física atual
	 * 
	 * @return localização atual do usuário logado
	 */
	public static Localizacao getLocalizacaoAtual(){
		return getLocalizacaoFisicaAtual();
	}
	
	public static Localizacao getLocalizacaoFisicaAtual() {
		UsuarioLocalizacao usuarioLocalizacaoAtual = getUsuarioLocalizacaoAtual();
		if (usuarioLocalizacaoAtual != null){
			return usuarioLocalizacaoAtual.getLocalizacaoFisica();
		}
		return null;
	}

	public static Papel getPapelAtual(){
		UsuarioLocalizacao usuarioLocalizacaoAtual = getUsuarioLocalizacaoAtual();
		if (usuarioLocalizacaoAtual != null){
			return usuarioLocalizacaoAtual.getPapel();
		}
		return null;
	}
	
	public static Localizacao getLocalizacaoModeloAtual() {
		UsuarioLocalizacao usuarioLocalizacaoAtual = getUsuarioLocalizacaoAtual();
		if (usuarioLocalizacaoAtual != null){
			return usuarioLocalizacaoAtual.getLocalizacaoModelo();
		}
		return null;
	}
	
	
	public static Usuario getUsuarioSistema() {
		UsuarioService usuarioService = (UsuarioService) Component.getInstance(UsuarioService.class, true);
		Usuario usuario = null;
		try {
			usuario = usuarioService.getUsuarioSistema();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return usuario;
	}

	/**
	 * Recupera o usuário logado na sessão.
	 * 
	 * @return Usuário logado na sessão.
	 */
	public static Usuario getUsuarioLogado() {
		return (Usuario) Contexts.getSessionContext().get(USUARIO_LOGADO);
	}

	public static Pessoa getPessoaLogada(){
		Usuario usuario = (Usuario) Contexts.getSessionContext().get(USUARIO_LOGADO);
		Pessoa pessoa = null;
		if(usuario != null){
			EntityManager em = EntityUtil.getEntityManager();
			if (!em.contains(pessoa)){
				pessoa = em.find(Pessoa.class, usuario.getIdUsuario());
			}
		}
		return pessoa;
	}
	
	/**
	 * Método responsável por retornar o objeto {@link PessoaPush} o qual representa o usuário logado na funcionalidade push.
	 * 
	 * @return Objeto {@link PessoaPush} o qual representa o usuário logado na funcionalidade push.
	 */
	public static PessoaPush getPessoaPushLogada() {
		PessoaPush pessoaPush = (PessoaPush) Contexts.getSessionContext().get(PESSOA_PUSH_LOGADA);
		if(pessoaPush != null){
			EntityManager entityManager = EntityUtil.getEntityManager();
			if (!entityManager.contains(pessoaPush)){
				pessoaPush = entityManager.find(PessoaPush.class, pessoaPush.getIdPessoaPush());
			}
		}
		return pessoaPush;
	}

	/**
	 * Método a ser utilizado para autenticação do WebService do AUD 
	 * 
	 * @return true(o usuário foi autenticado)
	 */
	public boolean authenticateSCWS(String assinatura, String hcert) throws LoginException{
		setWS(true);
		
		setAssinatura(assinatura);
		setCertChain(obterCertChainHashCertificado(hcert));
		
		String textoAssinatura = (String) Component.getInstance(SESSAO_TEXTO_ASSINATURA, ScopeType.SESSION);
		Usuario usuario = null;
		try {
			 byte[] sig = SigningUtilities.base64Decode(assinatura);

			 CertificadoICP certificado = recuperarCertificadoICP(getCertChain());
			 
			 boolean verified = Signer.verify(certificado.getX509Certificate().getPublicKey(), Signer.SignatureAlgorithm.SHA1withRSA, textoAssinatura.getBytes(), sig);

			 if(verified){
				 log.debug("Assinatura confirmada.");
				Pessoa p = getPessoaService().findByInscricaoMF(certificado.getInscricaoMF());
				if (p != null) {
					usuario = EntityUtil.getEntityManager().find(Usuario.class, p.getIdUsuario());
					if (!usuario.getAtivo()) {
						throw new LoginException("O usuário " + p.getDocumentoCpfCnpj() + " não está ativo.");
					}
				}else{
					throw new LoginException("não foi possível confirmar a identidade.");
				}
			}else{
				throw new LoginException("não foi possível confirmar a identidade.");
			}
		} catch (Exception e) {
			throw new LoginException("Não foi possível realizar a autenticação: " + e.getLocalizedMessage());
		}

		String login = usuario.getLogin();

		IdentityManager identityManager = IdentityManager.instance();
		boolean userExists = identityManager.getIdentityStore().userExists(login);
		if (userExists){
			autenticaManualmenteNoSeamSecurity(login);
			Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE, new Object[1]);
			Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL, new Object[1]);
			return true;
		}
		
		((PjeIdentity)Identity.instance()).setLogouComCertificado(true);
		return true;
	}

	/**
	 * Método a ser utilizado pelos webservices para verificar se o certificado
	 * passado por parâmetro é um certificado válido.
	 * 
	 * @param icp Certificado de um usuário do sistema.
	 * @return usuário autenticado.
	 */
	public Usuario authenticateWS(CertificadoICP icp) throws LoginException {
		setWS(true);
		
		Pessoa pessoa = obterPessoa(icp);
		Usuario usuario = obterUsuario(pessoa);
		autenticaManualmenteNoSeamSecurity(usuario.getLogin());

		setUsuarioLogadoSessao(usuario);
		selecionarLocalizacaoAtual(usuario);
		
		if (isAdvogado(usuario)) {
			Events.instance().raiseEvent(ControleFiltros.INICIALIZAR_FILTROS_CONSULTA_ADVOGADO);
		} else {
			Events.instance().raiseEvent(ControleFiltros.INICIALIZAR_FILTROS);
		}
		
		((PjeIdentity)Identity.instance()).setLogouComCertificado(true);
		
		return usuario;
	}
	
	/**
	 * Método de autenticação do usuário de sistema
	 * 
	 * @return
	 * @throws LoginException
	 */
	public Usuario authenticateWSUsuarioSistema() throws LoginException {
		UsuarioService usuarioService = (UsuarioService) Component.getInstance(UsuarioService.class, true);
		Usuario usuario = null;
		try {
			usuario = usuarioService.getUsuarioSistema();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		setWS(true);
		
		autenticaManualmenteNoSeamSecurity(usuario.getLogin());
		setUsuarioLogadoSessao(usuario);
		selecionarLocalizacaoAtual(usuario);
		
		return usuario;
	}
	
	/**
	 * Autentica o usuário passando cpf/cnpj e senha.
	 * 
	 * @param login CPF/CNPJ.
	 * @param senha Senha do usuário.
	 * @param localizacao Localização que será usada.
	 * @throws LoginException
	 */
	public Usuario authenticateWS(String login, String senha, UsuarioLocalizacao usuarioLocalizacao, String idOrgaoRepresentacao) throws LoginException {
		setWS(true);
		Usuario usuario = null;
		
		if (ProjetoUtil.isNaoVazio(login, senha)) {
			atribuirCredentials(login, senha);
			Identity.instance().login();
			
			usuario = obterUsuario(login);
			autenticaManualmenteNoSeamSecurity(usuario.getLogin());

			setUsuarioLogadoSessao(usuario);
			
			//Definir localizacao do Órgão de Representação passado por parâmetro
			if(usuarioLocalizacao == null && idOrgaoRepresentacao != null){
				PessoaJuridicaManager pessoaJuridicaManager = ComponentUtil.getComponent(PessoaJuridicaManager.class);
				PessoaJuridica pessoaJuridica = pessoaJuridicaManager.findByCNPJ(idOrgaoRepresentacao);
				Procuradoria procuradoria = null; 
				
				if(pessoaJuridica != null){
					ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
					procuradoria = procuradoriaManager.findByPessoaJuridica(pessoaJuridica);	
				}
				
				
				if(procuradoria != null){
					usuarioLocalizacao = getUsuarioLocalizacaoManager().findByUsuarioLocalizacaoPapel(usuario, procuradoria.getLocalizacao(), getPapelAtual());	
				}
				
			}
			
			setLocalizacaoAtualComValidacaoDeUsuario(usuarioLocalizacao, usuario);
		} else {
			throw new LoginException("Usuário ou senha inválidos.");
		}
		
		((PjeIdentity)Identity.instance()).setLogouComCertificado(true);
		
		return usuario;
	}
	
	public Usuario authenticateMobile(String login, String senha) throws LoginException {
		setWS(true);
		Usuario usuario = null;
		
		if (ProjetoUtil.isNaoVazio(login, senha)) {
			atribuirCredentials(login, senha);
			Identity.instance().login();
			
			usuario = obterUsuario(login);
			autenticaManualmenteNoSeamSecurity(usuario.getLogin());

			setUsuarioLogadoSessao(usuario);
			
		} else {
			throw new LoginException("Usuário ou senha inválidos.");
		}
		
		((PjeIdentity)Identity.instance()).setLogouComCertificado(true);
		
		return usuario;
	}
	
	public Usuario authenticateMobile(UsuarioMobile usuarioMobile, Integer idUsuarioLocalizacao) throws Exception {
		setWS(true);
		autenticaManualmenteNoSeamSecurity(usuarioMobile.getUsuario().getLogin());
		Usuario usuario = obterUsuario(usuarioMobile.getUsuario().getLogin());
		try {
			setUsuarioLogadoSessao(getPessoaService().findById(usuario.getIdUsuario()));
		} catch (PJeBusinessException e) {
			throw new LoginException("Pessoa não encontrada");
		}
		selecionarLocalizacaoAtual(usuario);
		if ( idUsuarioLocalizacao!=null ) {
			UsuarioLocalizacao localizacao = getUsuarioLocalizacaoManager().findById(idUsuarioLocalizacao);
			setLocalizacaoAtual(localizacao);
		}
		
		return usuario;
	}
	
	/**
	 * Obtém o objeto UsuarioLogin a partir do hash do certChain Issue PJE-110
	 * 
	 * @author Ricardo Maia
	 * @since 1.4.0
	 * @category PJE-JT
	 * @Issue [CSJT] [PJE-110], [PJE-1342][PJE-1344]
	 * @return certChain
	 */
	public String obterCertChainHashCertificado(String hcert){
		UsuarioLogin usuarioLogin;
		String sql = "select o from br.jus.pje.nucleo.entidades.identidade.UsuarioLogin o where MD5(o.certChain) = :hashCertChain";
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql);
		query.setParameter("hashCertChain", hcert);
		usuarioLogin = EntityUtil.getSingleResult(query);
		
		return usuarioLogin.getCertChain();
	}

	/**
	 * Verifica se determinado usuário possui acesso a um recurso. Será utilizado para definir autorização do WebService do AUD
	 * 
	 * @author Ricardo Maia
	 * @since 1.4.0
	 * @category PJE-JT
	 * @Issue [CSJT] [PJE-110], [PJE-1342][PJE-1344]
	 * @return true( caso o usuário possua o recurso AudienciaService)
	 */
	public boolean possuiAcessoRecurso(String recurso) throws LoginException{
		for (UsuarioLocalizacao usuarioLocalizacao : getUsuarioLogado().getUsuarioLocalizacaoList()){
			for (Papel p : usuarioLocalizacao.getPapel().getGrupos()) {
				if (p.getNome() != null && p.getNome().equals(recurso))
					return true;
			}
		}

		return false;
	}
	
	public boolean possuiAcessoRecursoPorIdentificador(String identificadorRecurso) throws LoginException{
		for (UsuarioLocalizacao usuarioLocalizacao : getUsuarioLogado().getUsuarioLocalizacaoList()){
			for (Papel p : usuarioLocalizacao.getPapel().getGrupos()) {
				if (p.getIdentificador() != null && p.getIdentificador().equals(identificadorRecurso))
					return true;
			}
		}

		return false;
	}

	public String recuperarCPF() throws LoginException{
		bindCamposTelaLogin();
		
        if ( (assinatura==null) || (assinatura.equals("")) ) {
        	FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível realizar a autenticação: Verifique se o cartão ou token estão conectados.");
        	return "erro";
        }
        
		String textoAssinatura = (String) Component.getInstance(SESSAO_TEXTO_ASSINATURA, ScopeType.SESSION);
		Contexts.getSessionContext().remove(SESSAO_TEXTO_ASSINATURA);
		
		try{
			 byte[] sig = SigningUtilities.base64Decode(assinatura);

			 CertificadoICP certificado = recuperarCertificadoICP(certChain);

			 boolean verified = Signer.verify(certificado.getX509Certificate().getPublicKey(), Signer.SignatureAlgorithm.MD5withRSA, textoAssinatura.getBytes(), sig);
			 
			 if(verified) {
				log.debug("Assinatura confirmada.");
				String cpf = certificado.getInscricaoMF();
				
				if(cpf != null && cpf.length() == 11) {
					cpf = InscricaoMFUtil.acrescentaMascaraMF(cpf);
				}
				
				boolean cadastrado = PessoaAdvogadoHome.instance().checkCPF(cpf, null);
 				if(cadastrado) {
					FacesMessages.instance().clear();
					FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Pesquisa em nossa base de dados indica que o CPF associado ao cartão de identidade digital já se encontra cadastrado como advogado. Caso V.Sª não haja realizado o credenciamento anterior, por favor, entrar em contato com o órgão julgador competente!");
		        	return "erro";
				}
				
				Contexts.getConversationContext().set("cpfAdvogado", cpf);
			} else {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível confirmar a identidade.");
	        	return "erro";
			}
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível realizar a autenticação: " + e.getLocalizedMessage());
        	return "erro";
		}
		
		return "cadastrar";
	}
	
	/**
	 * Autentica o usuário manualmente.
	 * 
	 * @param login
	 * @param identityManager
	 */
	public void autenticaManualmenteNoSeamSecurity(String login, IdentityManager identityManager){
		Principal principal = new SimplePrincipal(login);
		Identity identity = Identity.instance();
		identity.acceptExternallyAuthenticatedPrincipal(principal);
		Credentials credentials = (Credentials) Component.getInstance(Credentials.class);
		credentials.clear();
		credentials.setUsername(login);
		identity.getCredentials().clear();
		identity.getCredentials().setUsername(login);
		List<String> roles = identityManager.getImpliedRoles(login);
		if (roles != null){
			for (String role : roles){
				identity.addRole(role);
			}
		}
	}

	/**
	 * Autentica o usuário manualmente.
	 * 
	 * @param login
	 */
	public void autenticaManualmenteNoSeamSecurity(String login){
		IdentityManager identityManager = IdentityManager.instance();
		autenticaManualmenteNoSeamSecurity(login, identityManager);
	}

	public void setLogin(String login){
		this.login = login;
	}

	public String getLogin(){
		return login;
	}

	public void setAssinatura(String assinatura){
		this.assinatura = assinatura;
	}

	public String getAssinatura(){
		return assinatura;
	}

	public void setCertChain(String certChain){
		this.certChain = certChain;
	}

	public String getCertChain(){
		return certChain;
	}

	public void setCertChainStringLog(String certChainStringLog){
		this.certChainStringLog = certChainStringLog;
	}

	public String getCertChainStringLog(){
		return certChainStringLog;
	}

	public void executeLogCertificadoInvalido(){
		String msg = "Login utilizado: " + login + " / " + certChainStringLog;
		log.warn(msg);
		CertificadoLog.executeLog(msg);
	}

	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> getUsuarioLocalizacaoListItems(){
		List<UsuarioLocalizacao> list = (List<UsuarioLocalizacao>) Contexts.getSessionContext().get(USUARIO_LOCALIZACAO_LIST);
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<UsuarioLocalizacaoMagistradoServidor> getUsuarioLocalizacaoMagistradoServidorListItems(){
		List<UsuarioLocalizacaoMagistradoServidor> list = (List<UsuarioLocalizacaoMagistradoServidor>) Contexts
				.getSessionContext().get(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_LIST);
		return list;
	}
	
	public List<UsuarioLocalizacao> getLocalizacoes() {
		List<UsuarioLocalizacao> localizacoes = new ArrayList<UsuarioLocalizacao>(0);
		
		Usuario usuario = getUsuarioLogado();
		if (usuario != null) {
			localizacoes = getUsuarioService().getLocalizacoesAtivas(usuario);
		}
		return localizacoes;
	}

	public List<?> getUsuarioLocalizacaoComboListItems(){
		if (isUsuarioLocalizacaoMagistadoServidor()){
			return getUsuarioLocalizacaoMagistradoServidorListItems();
		}
		else{
			return getUsuarioLocalizacaoListItems();
		}
	}

	public static boolean isUsuarioLocalizacaoMagistadoServidor(){
		List<UsuarioLocalizacaoMagistradoServidor> localizacaoMagistradoServidorListItems = getUsuarioLocalizacaoMagistradoServidorListItems();
		return CollectionUtilsPje.isNotEmpty(localizacaoMagistradoServidorListItems);
	}

	public void setLocalizacaoAtualCombo(Object loc){
		if (loc instanceof UsuarioLocalizacao){
			setLocalizacaoAtual((UsuarioLocalizacao) loc);
		}
		if (loc instanceof UsuarioLocalizacaoMagistradoServidor){
			UsuarioLocalizacaoMagistradoServidor magistradoServidor = (UsuarioLocalizacaoMagistradoServidor) loc;
			setLocalizacaoAtual(magistradoServidor.getUsuarioLocalizacao(), magistradoServidor);
		}
		Events.instance().raiseEvent(SET_USUARIO_LOCALIZACAO_EVENT);
	}

	public Object getLocalizacaoAtualCombo(){
		if (isUsuarioLocalizacaoMagistadoServidor()){
			return Contexts.getSessionContext().get(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL);
		}
		else{
			return Contexts.getSessionContext().get(USUARIO_LOCALIZACAO_ATUAL);
		}
	}

	/**
	 * Verifica se o usuário é externo
	 * 
	 * @return
	 */
	public static boolean isUsuarioExterno(){
		return isUsuarioExterno(getIdentificadorPapelAtual());
	}
	
	public static boolean isUsuarioExterno(Papel papel) {
		boolean retorno = false;
		
		if(papel != null) {
			retorno = isUsuarioExterno(papel.getIdentificador());
		}		
		return retorno;
	}

	public static boolean isUsuarioPush(){
		Papel papelPush = ParametroUtil.instance().getPapelUsuarioPush();
		return isPapelIgual(papelPush.getIdentificador(), getIdentificadorPapelAtual()) 
				|| Identity.instance().hasRole(Papeis.PUSH);
	}
	
	public static TipoVinculacaoUsuarioEnum getTipoUsuarioInternoAtual() {
		TipoVinculacaoUsuarioEnum tipoUsuarioInterno = null;
		if(isUsuarioInterno()) {
			tipoUsuarioInterno = TipoVinculacaoUsuarioEnum.O;
			if(getOrgaoJulgadorAtual() != null) {
				tipoUsuarioInterno = TipoVinculacaoUsuarioEnum.EGA;
			}else if(getOrgaoJulgadorColegiadoAtual() != null) {
				tipoUsuarioInterno = TipoVinculacaoUsuarioEnum.COL;
			}
		}
		return tipoUsuarioInterno;
	}
	
	public static TipoUsuarioExternoEnum getTipoUsuarioExternoAtual() {
		if(isAdvogado()) {
			return TipoUsuarioExternoEnum.A;
		}
		if(isAssistenteAdvogado()) {
			return TipoUsuarioExternoEnum.AA;
		}
		if(isProcurador() || isRepresentanteGestor()) {
			return TipoUsuarioExternoEnum.P;
		}
		if(isAssistenteProcurador() || isAssistenteGestorProcurador() ) {
			return TipoUsuarioExternoEnum.AP;
		}
		return TipoUsuarioExternoEnum.O;
	}

	private static boolean isUsuarioExterno(String identificadorPapel) {
		return isAdvogado(identificadorPapel)
				|| isAssistenteAdvogado(identificadorPapel)
				|| isJusPostulandi(identificadorPapel)
				|| isPerito(identificadorPapel)
				|| isProcurador(identificadorPapel)
				|| isAssistenteProcurador(identificadorPapel);
	}
	
	public static boolean isProcurador(){
		return isProcurador(getIdentificadorPapelAtual());
	}
	
	public static boolean isProcurador(Papel papel) {
		boolean retorno = false;
		
		if(papel != null) {
			retorno = isProcurador(papel.getIdentificador());
		}		
		return retorno;
	}
	
	private static boolean isProcurador(String identificadorPapel) {
		Papel papelProc = ParametroUtil.instance().getPapelProcurador();
		Papel papelProcMP = ParametroUtil.instance().getPapelProcuradorMP();
		return isPapelIgual(identificadorPapel,
					(papelProc != null ? papelProc.getIdentificador() : "-1"),
					(papelProcMP != null ? papelProcMP.getIdentificador() : "-1"),
					Papeis.REPRESENTANTE_PROCESSUAL,
					Papeis.PROCURADOR_MP)
				|| isRepresentanteGestor(identificadorPapel);
	}
		
	/**
	 * @return true se o papel do usuário logado for 'Advogado'.
	 */
	public static boolean isAdvogado() {
		return isAdvogado(getIdentificadorPapelAtual());
	}
	
	public static boolean isAdvogado(String identificadorPapelParadigma) {
		Papel papelAdv = ParametroUtil.instance().getPapelAdvogado();
		return isPapelIgual(identificadorPapelParadigma, 
					(papelAdv != null ? papelAdv.getIdentificador() : "-1"), 
					Papeis.ADVOGADO,
					Papeis.PJE_ADVOGADO);
	}
	
	/**
	 * @return true se o papel do usuário logado for 'Assistente de advogado'.
	 */
	public static boolean isAssistenteAdvogado() {
		return isAssistenteAdvogado(getIdentificadorPapelAtual());
	}
	
	public static boolean isAssistenteAdvogado(String identificadorPapelParadigma) {
		ParametroUtil parametro = ParametroUtil.instance();
		return 	isPapelIgual(identificadorPapelParadigma,
					Papeis.ASSISTENTE_ADVOGADO,
					Papeis.PJE_ASSISTENTE_ADVOGADO,
					Papeis.ASSISTENTE_GESTOR_ADVOGADO,
					(parametro.getPapelAssistenteAdvogado() != null ? parametro.getPapelAssistenteAdvogado().getIdentificador() : "-1"), 
					(parametro.getPapelAssistenteGestorAdvogado() != null ? parametro.getPapelAssistenteGestorAdvogado().getIdentificador() : "-1")
					);
	}
	/**
	 * @return true se o papel do usuário logado for 'Assistente de procurador'.
	 */
	public static boolean isAssistenteProcurador() {
		return isAssistenteProcurador(getIdentificadorPapelAtual());
	}
	
	public static boolean isAssistenteProcurador(String identificadorPapelParadigma) {
		ParametroUtil parametro = ParametroUtil.instance();
		return 	isPapelIgual(identificadorPapelParadigma,
					Papeis.ASSISTENTE_PROCURADORIA,
					Papeis.PJE_ASSISTENTE_PROCURADOR,
					(parametro.getPapelAssistenteProcuradoria() != null ? parametro.getPapelAssistenteProcuradoria().getIdentificador() : "-1") 
				)
				|| isAssistenteGestorProcurador();
	}

	public static boolean isAssistenteGestorProcurador() {
		return isAssistenteGestorProcurador(getIdentificadorPapelAtual());
	}
	
	public static boolean isAssistenteGestorProcurador(String identificadorPapelParadigma) {
		return 	isPapelIgual(identificadorPapelParadigma,
					Papeis.ASSISTENTE_GESTOR_PROCURADORIA,
					Papeis.PJE_ASSISTENTE_GESTOR_PROCURADOR
				);
	}
	
	/**
	 * Verifica se o identificador do papel atual = (admin ou administrador) 
	 * @return true se for admin ou administrador
	 */
	public static boolean isPapelAdministrador(){
		return isPapelAdministrador(getPapelAtual());
	}
	
	public static boolean isPapelAdministrador(Papel papelParadigma) {
		return isPapelAdministrador(papelParadigma.getIdentificador());
	}

	public static boolean isPapelAdministrador(String identificadorPapelParadigma) {
		return isPapelIgual(identificadorPapelParadigma, Parametros.IDENTIFICADOR_PAPEL_ADMIN, Parametros.IDENTIFICADOR_PAPEL_ADMINISTRADOR, Papeis.ADMINISTRADOR);
	}
	
	/**
	 * Verifica se o papel atual é de oficial de justica. 
	 * 
	 * @return true se for oficial de justica
	 */
	public static boolean isPapelOficialJustica(){
		return 	isPapelAtual(Papeis.OFICIAL_JUSTICA, Papeis.PJE_OFICIAL_JUSTICA) ||
				isPapelAtual(ParametroUtil.instance().getPapelOficialJustica());
	}
	
	/**
	 * Verifica se o papel atual é de perito.
	 * 
	 * @return true se for perito.
	 */
	public static boolean isPapelPerito() {
		return isPerito();
	}
	
	/**
	 * Verifica se o papel atual é de oficial de justica distribuidor. 
	 * 
	 * @return true se for oficial de justica distribuidor
	 */
	public static boolean isPapelOficialJusticaDistribuidor(){
		return 	isPapelAtual(Papeis.OFICIAL_JUSTICA_DISTRIBUIDOR, Papeis.PJE_OFICIAL_JUSTICA_DISTRIBUIDOR);
	}
	
	/**
     * Verifica se o papel atual é de analista judiciário.
     * 
     * @return true se for analista judiciário.
     */
    public static boolean isPapelAnalistaJudiciario() {
    	return isPapelAtual(Papeis.ANALISTA_JUDICIARIO);
    }
    
	/**
	 * Retorna true se o papel do usuário logado for perito.
	 * 
	 * @return boleano
	 */
    public static boolean isPerito() {
		return isPerito(getIdentificadorPapelAtual());
	}
	
	public static boolean isPerito(String identificadorPapelParadigma) {
		Papel papelPerito = ParametroUtil.instance().getPapelPerito();
		return isPapelIgual(identificadorPapelParadigma,
					(papelPerito != null ? papelPerito.getIdentificador() : "-1"),
					Papeis.PJE_PERITO,
					Papeis.PERITO);
	}
	
	/**
	 * Retorna true se o papel do usuário logado for SecretarioSessao.
	 * 
	 * @return boleano
	 */
	public static boolean isSecretarioSessao() {
		return isPapelAtual("idSecretarioSessao", Papeis.SECRETARIO_SESSAO);
	}
	
	/**
	 * Retorna true se o papel do usuário logado possui permissões de secretário da sessão
	 * @return
	 */
	public static boolean isPapelPermissaoSecretarioSessao() {
		return (isSecretarioSessao() ||  
				Identity.instance().hasRole(Papeis.SECRETARIO_SESSAO) || 
				Identity.instance().hasRole(Parametros.ID_PAPEL_SECRETARIO_SESSAO) ||
				Identity.instance().hasRole(Parametros.PERMITE_INCLUIR_PROCESSO));
	}
	
	/**
	 * Retorna true se o usuário passado por parâmetro for um advogado.
	 * 
	 * @param usuario
	 * @return true se o usuário for um advogado.
	 */
	public boolean isAdvogado(Usuario usuario) {
		return PessoaService.instanceOf(usuario, PessoaAdvogado.class);
	}
	
	/**
	 * @return true se o papel do usuário logado for 'Diretor de Secretaria'.
	 */
	public static boolean isPapelAtualDiretorSecretaria(){
		return isDiretorSecretaria();
	}
	
	public static boolean isDiretorSecretaria(){
		return isDiretorSecretaria(getIdentificadorPapelAtual());
	}
	
	public static boolean isDiretorSecretaria(String identificadorPapelParadigma) {
		Papel papelDirSec = ParametroUtil.instance().getPapelDiretorSecretaria();
		return isPapelIgual(identificadorPapelParadigma,
					(papelDirSec != null ? papelDirSec.getIdentificador() : "-1"),
					Papeis.DIRETOR_SECRETARIA);
	}

	public static boolean isDiretorDistribuicao(){
		return isPerito(getIdentificadorPapelAtual());
	}
	
	public static boolean isDiretorDistribuicao(String identificadorPapelParadigma) {
		return isPapelIgual(identificadorPapelParadigma,
					Papeis.DIRETOR_DISTRIBUICAO);
	}
	
	/**
	 * Verifica se o papel do usuario logado é de Assessor-Chefe
	 * @return verdadeiro se o papel atual for o de Assessor-Chefe
	 */
	public static boolean isPapelAssessor(){
		return isPapelAssessor(getIdentificadorPapelAtual());
	}
	
	public static boolean isPapelAssessor(String identificadorPapelParadigma) {
		return isPapelIgual(identificadorPapelParadigma,
					Papeis.ASSESSOR,
					Parametros.ID_PAPEL_ASSESSOR_CHEFE);
	}

	/**
	 * Verifica se o papel do usuario logado é de Magistrado
	 * @return
	 */
	public static boolean isPapelAtualMagistrado() {		
		return isMagistrado(getIdentificadorPapelAtual());
	}
	
	public static boolean isMagistrado() {
		return isMagistrado(getIdentificadorPapelAtual());
	}
	
	public static boolean isMagistrado(String identificadorPapelParadigma) {
		Papel papelMagistrado = ParametroUtil.instance().getPapelMagistrado();
		return isPapelIgual(identificadorPapelParadigma,
					(papelMagistrado != null ? papelMagistrado.getIdentificador() : "-1"),
					Papeis.MAGISTRADO,
					Papeis.PJE_MAGISTRADO);
	}

	
	/**
	 * Retorna a pessoa cadastrada no sistema referênte ao certificado passado por parâmetro.
	 * @param certificado CertificadoICP de uma pessoa do sistema.
	 * @return Pessoa devidamente cadastrada no sistema.
	 * @throws LoginException
	 */
	protected Pessoa obterPessoa(CertificadoICP certificado) throws LoginException {
		Pessoa pessoa = null;
		
		if (certificado != null) {
			try {
				if(certificado instanceof CertificadoPessoaFisica){
					String mf = certificado.getInscricaoMF();
					pessoa = getPessoaService().findByInscricaoMF(mf, mf);
				} else {
					CertificadoPessoaJuridica pj = (CertificadoPessoaJuridica) certificado;
					String mf = certificado.getInscricaoMF();
					String mfResponsavel = pj.getInscricaoMFResponsavel();
					pessoa = getPessoaService().findByInscricaoMF(mf, mfResponsavel);
				} 
				
				if (pessoa == null) {
					throw new LoginException("O usuário '" + certificado.getNome() + "' não está corretamente cadastrado no sistema.");
				}
			} catch (PJeBusinessException e) {
				throw new LoginException(e.getMessage());
			}
		} else {
			throw new LoginException("Certificado inválido!");
		}
		return pessoa;
	}
	
	/**
	 * Retorna o usuário da pessoa passada por parâmetro.
	 * @param pessoa Pessoa devidamente cadastrada no sistema.
	 * @return Usuário do sistema.
	 * @throws LoginException
	 */
	protected Usuario obterUsuario(Pessoa pessoa) throws LoginException {
		Usuario usuario = null;
		
		if (pessoa != null && pessoa.getIdUsuario() != null) {
			usuario = EntityUtil.getEntityManager().find(Usuario.class, pessoa.getIdUsuario());
			validarUsuario(usuario, pessoa.getDocumentoCpfCnpj());
		} else {
			throw new LoginException("Login inválido!");
		}
		return usuario;
	}
	
	/**
	 * Retorna o usuário da pessoa passada por parâmetro.
	 * @param login CPF/CNPJ da pessoa devidamente cadastrada no sistema.
	 * @return Usuário do sistema.
	 * @throws LoginException
	 */
	protected Usuario obterUsuario(String login) throws LoginException {
		Usuario usuario = null;
		
		if (StringUtils.isNotBlank(login)) {
			usuario = getUsuarioService().findByLogin(login);
			validarUsuario(usuario, login);
		} else {
			throw new LoginException("Login inválido!");
		}
		return usuario;
	}
	
	/**
	 * Valida se o usuário está logado.
	 * @param usuario Usuário que será validado.
	 * @param identificador Identificador usado na consulta do Usuário.
	 * @throws LoginException
	 */
	protected void validarUsuario(Usuario usuario, String identificador) throws LoginException {
		if (usuario == null){
			throw new LoginException("O usuário '" + identificador + "' não está corretamente cadastrado no sistema.");
		} else if (!usuario.getAtivo()){
			throw new LoginException("O usuário " + identificador + " não está ativo.");
		}
	}
	
	/**
	 * Atribui usuário e senha no Credentials do seam.
	 * 
	 * @param usuario
	 * @param senha
	 * @see Credentials
	 */
	protected void atribuirCredentials(String usuario, String senha) {
		Credentials credentials = ComponentUtil.getComponent(Credentials.class);
		credentials.setUsername(usuario);
		credentials.setPassword(senha);
	}
	
	/**
	 * Verifica se o usuario logou com certificado digital.
	 * @return Boolean: TRUE se logou com certificado
	 */
	public static Boolean isLogouComCertificado(){
		return ((PjeIdentity)Identity.instance()).isLogouComCertificado();
	}
	
	private void verificarProximidadeExpiracaoCertificadoDigital(Usuario usuario, boolean isLogouComCertificado) {
		if (isLogouComCertificado) {
			if (isCertificadoProximoDeExpirar(usuario)) {
				AlertaProximidadeExpiracaoCertificado componenteAlerta = getComponentAlertaProximidadeExpiracaoCertificado();
				componenteAlerta.exibirAlertaParaUsuario();
			}
		}
	}

	private AlertaProximidadeExpiracaoCertificado getComponentAlertaProximidadeExpiracaoCertificado() {
		return (AlertaProximidadeExpiracaoCertificado) Component
				.getInstance(AlertaProximidadeExpiracaoCertificado.class);
	}

	private boolean isCertificadoProximoDeExpirar(Usuario usuario) {
		try {
			return VerificaCertificado.isCertificadoProximoDeExpirar(usuario
					.getCertChain());
		} catch (CertificadoException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Procuradoria getProcuradoriaAtualUsuarioLogado() {
		Integer idProcuradoriaAtual = (Integer) Contexts.getSessionContext().get(Authenticator.ID_PROCURADORIA_ATUAL);
		if(idProcuradoriaAtual != null) {
			try {
				ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.NAME);
				return procuradoriaManager.findById(idProcuradoriaAtual);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
						"Ocorreu um erro ao tentar recuperar a procuradoria atual: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Procuradoria getReferenciaProcuradoriaAtualUsuarioLogado() {
		return Authenticator.getIdProcuradoriaAtualUsuarioLogado() != null ? EntityUtil.getEntityManager().getReference(Procuradoria.class, Authenticator.getIdProcuradoriaAtualUsuarioLogado()) : null;
	}
	
	/**
	 * Verifica se o usuário tem a permissão de cadastro de usuários em todos os papéis
	 * 
	 * @return
	 */
	public static Boolean isPermissaoCadastroTodosPapeis(){
		return isPermissaoCadastroTodosPapeis(getIdentificadorPapelAtual());
	}
	
	public static Boolean isPermissaoCadastroTodosPapeis(String identificadorPapelParadigma) {
		return isPapelIgual(identificadorPapelParadigma, Parametros.IDENTIFICADOR_PAPEL_ADMIN, Parametros.IDENTIFICADOR_PAPEL_ADMINISTRADOR, Papeis.ADMINISTRADOR, 
				Papeis.ADMINISTRADOR_CADASTRO_USUARIO) || 
					Identity.instance().hasRole(Papeis.ADMINISTRADOR_CADASTRO_USUARIO);
	}
	
	/**
	 * Verifica se o usurio tem a permisso para visualizar a lista de procuradoria/defensoria
	 * 
	 * @return True se o usurio possuir permisso. False, caso contrrio. 
	 */
	public static boolean isPermiteVisualizarProcuradoriaDefensoria(){
		return isPermissaoCadastroTodosPapeis() || Identity.instance().hasRole(Papeis.PJE_ADMINISTRADOR_PROCURADORIA);
	}
	
	public static boolean isAdministradorProcuradoriadefensoria() {
		return isPermiteVisualizarProcuradoriaDefensoria();
	}

    /**
     * Retorna true se o usuário logado for jus postulandi.
     * 
     * @return boleano.
     */
    public static boolean isJusPostulandi() {
		return isJusPostulandi(getIdentificadorPapelAtual());
	}
	
	public static boolean isJusPostulandi(String identificadorPapelParadigma) {
		Papel papelJusPost = ParametroUtil.instance().getPapelJusPostulandi();
		return isPapelIgual(identificadorPapelParadigma,
					(papelJusPost != null ? papelJusPost.getIdentificador() : "-1"),
					Papeis.JUS_POSTULANDI);
	}

    /**
     * Retorna true se o usuário logado for um usuário interno.
     * 
     * @return booleano.
     */
    public static boolean isUsuarioInterno(){
		return isUsuarioInterno(getIdentificadorPapelAtual());
	}

	public static boolean isUsuarioInterno(String identificadorPapelParadigma) {
		return !isUsuarioExterno(identificadorPapelParadigma);
	}
    
    /**
     * Retorna true se o usuário logado for um representante gestor (procurador ou defensor com permissão de gestor).
     * 
     * @return booleano.
     */
    public static boolean isRepresentanteGestor() {
		return isRepresentanteGestor(getIdentificadorPapelAtual());
	}
	
	public static boolean isRepresentanteGestor(String identificadorPapelParadigma) {
		Papel papelProcGestor = ParametroUtil.instance().getPapelProcuradorGestor();
		Papel papelProcMPGestor = ParametroUtil.instance().getPapelProcuradorMPGestor();
		return isPapelIgual(identificadorPapelParadigma,
					(papelProcGestor != null ? papelProcGestor.getIdentificador() : "-1"),
					(papelProcMPGestor != null ? papelProcMPGestor.getIdentificador() : "-1"),
					Papeis.REPRESENTANTE_PROCESSUAL_GESTOR,
					Papeis.PROCURADOR_CHEFE_MP);
	}
    
	/**
	 * Método boleano que verifica se o papel atual do usuário logado possui
	 * acesso ao recurso passado via parâmetro.
	 * 
	 * @param identificadorRecurso
	 *            identificador do recurso a ser verificado
	 * @return <code>true</code> se o parâmetro existir no grupo de
	 *         funcionalidades.
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-18468">PJEII-18468</a>
	 */
	public static boolean papelAtualPossuiAcessoRecursoPorIdentificador(String identificadorRecurso) {
		for (Papel papel : getUsuarioLocalizacaoAtual().getPapel().getGrupos()) {
			if ((papel.getIdentificador() != null && 
					papel.getIdentificador().equals(identificadorRecurso)) || 
						isPermissaoCadastroTodosPapeis()) {
				return true;
			}
		}
		return false;
	}

	public static Integer getIdProcuradoriaAtualUsuarioLogado() {
		return (Integer) Contexts.getSessionContext().get(Authenticator.ID_PROCURADORIA_ATUAL);
	}

	public static TipoProcuradoriaEnum getTipoProcuradoriaAtualUsuarioLogado() {
		return (TipoProcuradoriaEnum) Contexts.getSessionContext().get(Authenticator.TIPO_PROCURADORIA_ATUAL);
	}
	
	public static Integer getIdUsuarioLogado(){
		if (Contexts.getSessionContext()==null) {
			log.warn("Não há um contexto de sessão ativo, mas Authenticator.getIdUsuarioLogado foi chamado.");
			return null;
		}
		Usuario usuario = (Usuario) Contexts.getSessionContext().get(USUARIO_LOGADO);
		if(usuario != null) {
			return usuario.getIdUsuario();
		}
		return null;
	}

	public static Integer getIdPapelAtual() {
		return (Integer) Contexts.getSessionContext().get(Authenticator.ID_PAPEL_ATUAL);
	}

	public static String getIdentificadorPapelAtual() {
		return (String) Contexts.getSessionContext().get(Authenticator.IDENTIFICADOR_PAPEL_ATUAL);
	}

	public static RepresentanteProcessualTipoAtuacaoEnum getTipoAtuacaoProcurador() {
		return (RepresentanteProcessualTipoAtuacaoEnum) Contexts.getSessionContext().get(Authenticator.ATUACAO_PROCURADORIA_USUARIO_LOGADO);
	}

	/**
	 * Retorna o ID da localização física atual
	 * @return
	 */
	public static Integer getIdLocalizacaoAtual(){
		return getIdLocalizacaoFisicaAtual();	
	}
	
	public static Integer getIdLocalizacaoFisicaAtual(){
		return (Integer) Contexts.getSessionContext().get(Authenticator.ID_LOCALIZACAO_ATUAL);	
	}

	/**
	 * Retorna o ID da localização modelo atual
	 * @return
	 */
	public static Integer getIdLocalizacaoModeloAtual(){
		Integer id_localizacao_modelo = (Integer) Contexts.getSessionContext().get(Authenticator.ID_LOCALIZACAO_MODELO_ATUAL);
		return id_localizacao_modelo == null ? 0 : id_localizacao_modelo; 	
	}

	/**
	 * Método responsável por desconectar a instância atual se o usuario passado
	 * for igual ao usuário atualmente logado no sistema.
	 * 
	 * @param usuario
	 *            Usuário do registro que se está removendo/inativando
	 * @param comMensagem
	 *            Mensagem a ser mostrada ao deslogar o usuário. Se
	 *            <code>null</code> nenhuma mensagem será mostrada.
	 */
	public static void deslogar(Usuario usuario, String comMensagem) {
		if (getUsuarioLogado().getIdUsuario() == usuario.getIdUsuario()) {
			instance().unAuthenticate();
			Redirect redirect = Redirect.instance();
			redirect.setViewId("/login.xhtml");
			redirect.execute();				
			if (StringUtil.isNotEmpty(comMensagem)) {
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, comMensagem);
			}
		}
	}
	/**
	 * verifica se o usuário logado tem permissão de visualizar sigilogos
	 * @return
	 */
	public static boolean isVisualizaSigiloso(){
		return Identity.instance().hasRole(Papeis.VISUALIZA_SIGILOSO) || Identity.instance().hasRole(Papeis.MANIPULA_SIGILOSO);
	}

	/**
	 * Atribui a localização se pertencer ao usuário passado por parâmetro.
	 * 
	 * @param localizacao UsuarioLocalizacao que será atribuído.
	 * @param usuario Usuário ao qual a localização deve pertencer.
	 * 
	 * @throws LoginException 
	 */
	private void setLocalizacaoAtualComValidacaoDeUsuario(UsuarioLocalizacao localizacao,
			Usuario usuario) throws LoginException {

		//se for passado a localização então será verificado se a localização pertence ao usuário e 
		//em caso afirmativo a localização será atribuída.
		if (localizacao != null && usuario != null) {
			try {
				localizacao = getUsuarioLocalizacaoManager().findById(localizacao.getIdUsuarioLocalizacao());
				Usuario usuarioLocalizacao = localizacao.getUsuario();
				
				if (usuarioLocalizacao.getIdUsuario() == usuario.getIdUsuario()) {
					setLocalizacaoAtual(localizacao);
				} else {
					String mensagem = "A localização %s não pertence ao usuário %s.";
					throw new LoginException(String.format(
							mensagem, 
							localizacao.getIdUsuarioLocalizacao(), 
							usuario.getNome()));
				}
			} catch (PJeBusinessException e) {
				throw new LoginException(e.getLocalizedMessage());
			}
		} else { //Se a localização não for passada então a localização padrão será atribuída.
			selecionarLocalizacaoAtual(usuario);
		}
	}

	
	/**
	 * Método retorna verdadeiro se a autenticação do usuário 
	 * for marcada como tendo alguma pendência, como por exemplo,
	 * na assinatura do termo de compromisso
	 * 
	 * @return verdadeiro quando for marcado como possuindo pendência
	 */
	public boolean isPossuiPendenciaCadastro() {
		Boolean possuiPendencia =  (Boolean)Contexts.getSessionContext().get(Variaveis.POSSUI_PENDENCIA_CADASTRO);
		if( possuiPendencia != null){
			return possuiPendencia;
		}
		else{
			return false;
		}
	}

	public void setPossuiPendenciaCadastro(boolean possuiPendenciaCadastro) {
		Contexts.getSessionContext().set(Variaveis.POSSUI_PENDENCIA_CADASTRO, new Boolean(possuiPendenciaCadastro) );
	}
	
	public void authenticatePJeOffice() throws LoginException, Exception {
		try {
			String assinatura = Util.getRequestParameter("assinatura");
			String certChain = Util.getRequestParameter("cadeiaCertificado");

			if (StringUtil.isNullOrEmpty(assinatura) || StringUtil.isNullOrEmpty(certChain)) {
				throw new LoginException("Não foi possível realizar a autenticação: Verifique se o cartão ou token estão conectados.");
			}
			
			this.setAssinatura(assinatura);
			this.setCertChain(certChain);
	
			this.logouComCertificado = true;
			
			String textoAssinatura = (String) Component.getInstance(SESSAO_TEXTO_ASSINATURA, ScopeType.SESSION);
			
			Usuario usuario = null;
				
			byte[] sig = SigningUtilities.base64Decode(assinatura);
			
			CertificadoICP certificado = recuperarCertificadoICP(certChain);
			 
			getCertificadoDigitalService().validarDataExpiracaoEhHabilitacaoDoCertificadoDigital(certificado.getX509Certificate());
				
			boolean verified = Signer.verify(certificado.getX509Certificate().getPublicKey(), Signer.SignatureAlgorithm.MD5withRSA, textoAssinatura.getBytes(), sig);

			if (!verified) {
				throw new LoginException("Assinatura não verificada.");
			}

			log.debug("Assinatura confirmada.");
			
			Pessoa p = null;
			
			((PjeIdentity)Identity.instance()).setLogouComCertificado(true);
			
			p = getPessoaService().findByInscricaoMF(certificado.getInscricaoMF(), certificado);
			
			boolean isPessoaComCertChainELocalizacoesVazio = VerificaCertificadoPessoa.verificaCertificadoELocalizacaoPessoa(p);
			
			boolean usuarioMigrado = StatusSenhaEnum.M.equals(p.getStatusSenha());
			
			if (certificado instanceof CertificadoPessoaFisica && (isPessoaComCertChainELocalizacoesVazio || usuarioMigrado)){
					CadastroUsuarioAction.carregarVariaveisDeSessao(
							certificado.getInscricaoMF(), 
							((CertificadoPessoaFisica) certificado).getDataNascimento(), 
							null);
					 setPossuiPendenciaCadastro(true);
					 imprimirResposta("Sucesso");
					 return;
			} else if(isPessoaComCertChainELocalizacoesVazio) {					 
					CadastroUsuarioAction.carregarVariaveisDeSessao(
							certificado.getInscricaoMF(), 
							((CertificadoPessoaJuridica) certificado).getDataNascimentoResponsavel(), 
							((CertificadoPessoaJuridica) certificado).getInscricaoMFResponsavel());
					imprimirResposta("Sucesso");
					return;
			}


			if (certChain != null && (p.getCertChain() == null || !p.getCertChain().equals(certChain))) {
				p.setCertChain(certChain);
				getPessoaService().persist(p);
				EntityUtil.getEntityManager().flush();
			}
			usuario = (Usuario) EntityUtil.getEntityManager().find(Usuario.class, p.getIdUsuario());
			
			if (!usuario.getAtivo()) {
				((PjeIdentity)Identity.instance()).setLogouComCertificado(false);
				throw new LoginException("O usuário " + p.getDocumentoCpfCnpj() + " não está ativo.");
			}
			
			String login = usuario.getLogin();

			IdentityManager identityManager = IdentityManager.instance();
			
			boolean userExists = identityManager.getIdentityStore().userExists(login);
			
			if (userExists) {
				autenticaManualmenteNoSeamSecurity(login);
				Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE, new Object[1]);
				Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL, new Object[1]);			
			}
			
			imprimirResposta("Sucesso");
		} catch (Exception e) {
			e.printStackTrace();
			imprimirResposta(String.format("Erro:Não foi possível realizar a autenticação (%s) (%s): %s", 
				InetAddress.getLocalHost().getCanonicalHostName(), InetAddress.getLocalHost().getHostAddress(), ExceptionUtil.getStackTrace(e)));
		}
	}

	private void imprimirResposta(String resposta) throws Exception {

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest(); 

		String charset = !StringUtil.isNullOrEmpty(request.getCharacterEncoding()) ? request.getCharacterEncoding() : ArquivoAssinadoUpload.CHARSET_PJE_OFFICE;
		
		response.setContentType("text/plain;charset=" + charset);
		response.setContentLength(resposta.length());
	
		OutputStream out = response.getOutputStream();
		out.write(resposta.getBytes(Charset.forName(charset)));
		out.flush();
		facesContext.responseComplete();		
	}
	
	public String redirectPJeOffice() {
		String inscricaoMF = (String)Contexts.getSessionContext().get(Variaveis.INSCRICAO_MF_CADASTRO);
		if (StringUtils.isNotEmpty(inscricaoMF)) {
			if (isPossuiPendenciaCadastro()) {
 				CadastroUsuarioAction.redirectParaCadastro(
					(String) Contexts.getSessionContext().get(Variaveis.INSCRICAO_MF_CADASTRO), 
					(Date) Contexts.getSessionContext().get(Variaveis.DATA_NASCIMENTO_CADASTRO), 
					(String) Contexts.getSessionContext().get(Variaveis.INSCRICAO_CONSULENTE_CADASTRO));
 			} else {
 				getControlePaginaInicialUsuario().redirectToPainel();				
 			}
		}
		else {
			this.setCloudCookies();
			getControlePaginaInicialUsuario().redirectToPainel();
		}
		return null;		
	}
	
	/**
	 * Recupera o certificado icp-br do usuario final
	 *  
	 * @param certChain Cadeia de certificado
	 * @return O certificado icp-br do usuario final 
	 * @throws Exception
	 */
	public CertificadoICP recuperarCertificadoICP(String certChain) throws Exception {
		try {
			VerificaCertificado.verificaDataValidade(certChain);
		} catch (CertificateExpiredException cee) {
			throw new PJeBusinessException(cee.getMessage());
		}

		Certificate[] certs = SigningUtilities.getCertChain(getCertChain());
		
		CertificadoICP certificadoICP = null;			
		
		for (Certificate c : certs) {
			certificadoICP = CertificadoICPBrUtil.getInstance((X509Certificate) c);
			if (certificadoICP != null) {
				break;
			}
		}

		if (certificadoICP == null) {
			throw new Exception("Certificado não encontrado.");
		}

		return certificadoICP;
	}
	
	/**
	 * Verifica se o papel passado por parametro eh um papel de Conciliador. Para isso, eh necessario que o papel seja
	 * a string "conciliador".
	 * @param 	papel
	 * @return	verdadeiro se papel for igual a "conciliador", ignorando o caseSensitive
	 */
	public static boolean isPapelConciliador(Papel papelParadigma){
		return isPapelIgual(papelParadigma, Papeis.CONCILIADOR);
	}
	
	/**
	 * @param papeis
	 * @return true se o papel atual for um dos papeis passado por parâmetro.
	 */
	public static boolean isPapelAtual(Papel... papeis) {
		Papel papelAtual = getPapelAtual();
		List<String> papeisStr = new ArrayList<String>();
		for (int indice = 0; indice < papeis.length; indice++) {
			Papel papel = papeis[indice];
			if(papel != null) {
				papeisStr.add(papel.getIdentificador());
			}
		}
		return isPapelIgual(papelAtual, papeisStr.toArray(new String[papeisStr.size()]));
	}
	
	/**
	 * @param papeis
	 * @return true se o papel atual for um dos papeis passado por parâmetro.
	 */
	public static boolean isPapelAtual(String... papeis) {
		Papel papelAtual = getPapelAtual();
		return isPapelIgual(papelAtual, papeis);
	}
	
	public static boolean isPapelIgual(Papel papelParadigma, String... papeis) {
		String identificadorPapel = null;
		if (papelParadigma != null && papeis != null) {
			identificadorPapel = papelParadigma.getIdentificador();
		}
		return isPapelIgual(identificadorPapel, papeis);
	}
	
	public static boolean isPapelIgual(String identificadorPapelParadigma, String... papeis) {
		Boolean resultado = Boolean.FALSE;
		if (identificadorPapelParadigma != null && papeis != null) {
			for (int indice = 0; indice < papeis.length && !resultado; indice++) {
				String papel = papeis[indice];
				resultado = (
						papel != null && 
						papel.equalsIgnoreCase(identificadorPapelParadigma));
			}
		}
		return resultado;		
	}

	public static boolean hasRole(String... papeis) {
		boolean hasRole = false;

		if (papeis != null) {
			for (String papel : papeis) {
				if (Identity.instance().hasRole(papel)) {
					hasRole = true;
					break;
				}
			}
		}

		return hasRole;
	}
	
	private PessoaService getPessoaService() {
		return ComponentUtil.getComponent(PessoaService.class);
	}
	
	private UsuarioService getUsuarioService() {
		return ComponentUtil.getComponent(UsuarioService.class);
	}
	
	private UsuarioLocalizacaoManager getUsuarioLocalizacaoManager() {
		return ComponentUtil.getComponent(UsuarioLocalizacaoManager.class);
	}
	
	private LocalizacaoManager getLocalizacaoManager() {
		return ComponentUtil.getComponent(LocalizacaoManager.class);
	}
	
	private CertificadoDigitalService getCertificadoDigitalService() {
		return ComponentUtil.getComponent(CertificadoDigitalService.class);
	}
	
	private ControlePaginaInicialUsuario getControlePaginaInicialUsuario() {
		return ComponentUtil.getComponent(ControlePaginaInicialUsuario.class, true);
	}

	public static Integer getIdUsuarioLocalizacaoMagistradoServidorAtual(){
		UsuarioLocalizacaoMagistradoServidor ulms = (UsuarioLocalizacaoMagistradoServidor) Contexts.getSessionContext().get(USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL);
		if (ulms != null){
			return ulms.getIdUsuarioLocalizacaoMagistradoServidor();
		}
		return null;
	}		

	public static Boolean temVisibilidade() {
		Boolean temVisibilidade = (Boolean)Contexts.getSessionContext().get(TEM_VISIBILIDADE);
		if(temVisibilidade == null) {
			Date hoje = DateUtils.truncate(DateService.instance().getDataHoraAtual(), Calendar.DATE);
			
			String query = "SELECT COUNT(loc.idUsuarioLocalizacaoVisibilidade) FROM UsuarioLocalizacaoVisibilidade AS loc " +
					"	WHERE loc.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :loc " +
					"	AND loc.dtInicio <= :data" +
					"	AND (loc.dtFinal IS NULL OR loc.dtFinal >= :data)";
					
			Query q = EntityUtil.getEntityManager().createQuery(query);
			q.setParameter("loc", getIdUsuarioLocalizacaoMagistradoServidorAtual());
			q.setParameter("data", hoje);
			q.setMaxResults(1);
			Number cont = (Number) q.getSingleResult();
			temVisibilidade = (cont.longValue() > 0);
			Contexts.getSessionContext().set(TEM_VISIBILIDADE, temVisibilidade);
		}
		return temVisibilidade;
	}

	public static Boolean temOrgaoVisivel() {
		Boolean temOrgaoVisivel = (Boolean)Contexts.getSessionContext().get(TEM_ORGAO_VISIVEL);
		if(temOrgaoVisivel == null) {
			Date hoje = DateUtils.truncate(DateService.instance().getDataHoraAtual(), Calendar.DATE);
			
			String query = "SELECT COUNT(loc.idUsuarioLocalizacaoVisibilidade) FROM UsuarioLocalizacaoVisibilidade AS loc " +
					"	WHERE loc.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :loc " +
					"		AND loc.dtInicio <= :data" +
					"		AND (loc.dtFinal IS NULL OR loc.dtFinal >= :data) " +
					"		AND loc.orgaoJulgadorCargo IS NOT NULL ";
			Query q = EntityUtil.getEntityManager().createQuery(query);
			q.setParameter("loc", getIdUsuarioLocalizacaoMagistradoServidorAtual());
			q.setParameter("data", hoje);
			q.setMaxResults(1);
			Number cont = (Number) q.getSingleResult();
			temOrgaoVisivel = (cont.longValue() > 0);
			Contexts.getSessionContext().set(TEM_ORGAO_VISIVEL, temOrgaoVisivel);
		}
		return temOrgaoVisivel;
	}
	
	public boolean isPermiteCadastrarParteSemDocumento() {
		return isPapelAdministrador() || Identity.instance().hasRole(Papeis.CADASTRA_PARTE_SEM_DOCUMENTO);
	}
	
	public boolean isPermiteAproveitarAdvogados() {
		return isPapelAdministrador() || Identity.instance().hasRole(Papeis.APROVEITAR_ADVOGADOS);
	}

	
	/**
	 * Método responsável por verificar se o usuário possui (ou não) permissão para alteração do campo email.
	 * 
	 * @return Verdadeiro se o usuário cadastrador possuir o perfil "pje:papel:permissaoCadastroUsuarioTodosPapeis" ou 	 
	 * outro usuário cadastrador e o usuário que está sendo cadastrado:
	 * -- ainda não tiver feito login no sistema.
	 * -- ou já possuir uma localizacação diferente da localização do advogado 
	 * Falso, para os demais casos.
	 */
	public static boolean isPermiteAlterarEmail(Pessoa pessoaParaVerificacao) {
		LogAcessoManager logAcessoManager = ComponentUtil.getComponent(LogAcessoManager.NAME);		
		UsuarioLocalizacaoManager usuarioLocalizacaoManager = ComponentUtil.getComponent(UsuarioLocalizacaoManager.NAME);
		UsuarioManager usuarioManager = ComponentUtil.getComponent(UsuarioManager.NAME);
		return pessoaParaVerificacao != null && (
				Authenticator.isPermissaoCadastroTodosPapeis() || 
				(
					StringUtils.isBlank(logAcessoManager.recuperarUltimoLogin(usuarioManager.encontrarPorPessoa(pessoaParaVerificacao).getIdUsuario()))
					&&
					!usuarioLocalizacaoManager.verificaOutraLocalizacaoAssociada(pessoaParaVerificacao, Authenticator.getLocalizacaoAtual())
				));
	}

	public static boolean isWS() {
		boolean isWS = false;
		if(Contexts.getSessionContext().get(Authenticator.AUTENTICACAO_WS) != null) {
			isWS = (Boolean) Contexts.getSessionContext().get(Authenticator.AUTENTICACAO_WS);
		}
		return isWS;
	}

	private void setWS(boolean isWS) {
		this.isWS = isWS;
		Contexts.getSessionContext().set(Authenticator.AUTENTICACAO_WS, isWS);
	}
	
	public static boolean isSSOAuthentication() {
		boolean ssoAuthentication = false;
		if(Contexts.getSessionContext().get(Authenticator.AUTENTICACAO_SSO) != null) {
			ssoAuthentication = (Boolean) Contexts.getSessionContext().get(Authenticator.AUTENTICACAO_SSO);
		}
		return ssoAuthentication;
	}
	
	private void setSSOAuthentication(boolean ssoAuthentication) {
		Contexts.getSessionContext().set(Authenticator.AUTENTICACAO_SSO, ssoAuthentication);		
	}
	
	public boolean isBearerTokenAuthentication() {
		return bearerTokenAuthentication;
	}
	
	public void setBearerTokenAuthentication(boolean bearerTokenAuthentication) {
		this.bearerTokenAuthentication = bearerTokenAuthentication;
	}
	
	public boolean isPermiteModificarOrgaoJulgador(){
		return isPapelAdministrador() || Identity.instance().hasRole(Papeis.PJE_ADMINISTRADOR_ORGAO_JULGADOR);
	}

	public static Boolean isTokenValido(){
		return ((PjeIdentity)Identity.instance()).isTokenValido();
	}
	
	public static Boolean isCancelouToken(){
		return ((PjeIdentity)Identity.instance()).isCancelouToken();
	}
	
	public static Boolean isUsuarioMobile() {
		Usuario usuarioLogado = getUsuarioLogado();
		return ComponentUtil.getComponent(UsuarioMobileManager.class).listaUsuarioMobile(usuarioLogado).size()>0;
	}

	public boolean isSsoAuthenticationInProgress() {
		return ssoAuthenticationInProgress;
	}

	public void setSsoAuthenticationInProgress(boolean ssoAuthenticationInProgress) {
		this.ssoAuthenticationInProgress = ssoAuthenticationInProgress;
	}
	
	public Integer getBearerUsuarioLocalizacao() {
		return bearerUsuarioLocalizacao;
	}
	
	public void setBearerUsuarioLocalizacao(Integer bearerUsuarioLocalizacao) {
		this.bearerUsuarioLocalizacao = bearerUsuarioLocalizacao;
	}
	
	/**
	 * Recupera o nível de acesso do usuário logado. Utilizado em processos sigilosos.
	 * 
	 * @return Nível de acesso do usuário. Valor numérico entre 1 e 5, sendo 5 o nível de Sigilo Absoluto.
	 */
	public static Integer recuperarNivelAcessoUsuarioLogado() {
		int resultado = 0;
		String nivelAcesso = "pje:sigilo:nivelAcesso";
		for (int iNivel = 5; iNivel >= 1; iNivel--) {
			if (hasRole(nivelAcesso + iNivel)) {
				resultado = new Integer(ParametroUtil.getParametro(nivelAcesso + iNivel));
				break;
			}
		}
		return resultado;
	}
	
	/**
	 * Verifica se o papel atual do usuario logado é o papel de administrador de autuacao 
	 *
	 * @return Boolean
	 */
	public static Boolean isAdministradorAutuacao() {
		return Identity.instance().hasRole(Papeis.PJE_ADMINISTRADOR_AUTUACAO);
	}

	
	private Date dataPadraoCadastro() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, 1969);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 20);
		
		return cal.getTime();
	}
	
	/**
     * Retorna o usuario pje extraido do token de acesso
     * 
     * @param preAuthToken
     * @return
     * @throws Exception
     */
    public static PjeUser obterUsuarioToken( String preAuthToken ) throws Exception{
    	try {
    		Integer idUsuarioLocalizacao = new Integer(SecurityTokenControler.instance().validarChaveAcessoGenerica(preAuthToken));
			
			UsuarioLocalizacaoManager ulm = ComponentUtil.getComponent(UsuarioLocalizacaoManager.NAME);
			UsuarioLocalizacao ul = ulm.findById(idUsuarioLocalizacao);
			UsuarioMobile um = new UsuarioMobile();
			um.setUsuario(ul.getUsuario());
			Usuario usuario = Authenticator.instance().authenticateMobile(um, ul.getIdUsuarioLocalizacao());
			
    		return new PjeUser(usuario.getLogin(), usuario.getSenha(), usuario.getBloqueio() && usuario.getAtivo(), usuario.getNome(), usuario.getEmail());			
		} catch (Exception e1) {
			throw new Exception("Não foi possível validar o token de autenticação.");
		}
	}
	
	public static List<Integer> getIdsLocalizacoesFilhas() {
		List<Integer> lista = new ArrayList<>(0);
		
		for (Localizacao localizacao : getLocalizacoesFilhasAtuais()) {
			lista.add(localizacao.getIdLocalizacao());
		}
		
		return lista;
	}

	private boolean redirectToCallbackLogin(String message) {
		if(!Authenticator.isWS()) {
            if(!Authenticator.isSSOAuthentication()){
                return true;
            }
			ControlePaginaInicialUsuario.instance().redirectToSSOLoginCallback(message);
			return true;
		}
		return false;
	}


	private boolean verificarIndicacaoBloqueioSenhaPeloSSO(Usuario usuario) {
		boolean isSenhaAtiva = StatusSenhaEnum.A.equals(usuario.getStatusSenha());
		boolean deveBloquearSenha = isLogouComCertificado() && isSSOAuthentication()
				&& isContaSSOReativadaComCertificado() && isSenhaAtiva;
		return deveBloquearSenha;
	}

	public boolean isContaSSOReativadaComCertificado() {
		boolean isReativadoComCertificadoPorSSO = false;
		Object reativadoComCertificadoClaim = null;
		KeycloakSecurityContext ksc = (KeycloakSecurityContext) Contexts.getSessionContext()
				.get(Constantes.SSO_CONTEXT_NAME);

		if (ksc.getToken() != null) {
			reativadoComCertificadoClaim = ksc.getToken().getOtherClaims()
					.get(Constantes.SSO_CLAIM_REATIVAR_COM_CERTIFICADO);
		}

		if (reativadoComCertificadoClaim == null) {
			isReativadoComCertificadoPorSSO = false;
		} else {
			isReativadoComCertificadoPorSSO = Boolean.valueOf(reativadoComCertificadoClaim.toString());
		}
		return isReativadoComCertificadoPorSSO;
    }

	public void authenticateUsuarioSistema() throws LoginException{
		authenticateUsuarioSistema(true);
	}
	
	public void authenticateUsuarioSistema(Boolean configuraLocalizacao) throws LoginException{
		UsuarioService usuarioService = (UsuarioService) Component.getInstance(UsuarioService.class, true);
		Usuario usuario = null;
		try {
			usuario = usuarioService.getUsuarioSistema();
			authenticateUsuarioAutomaticamente(usuario, configuraLocalizacao);
		} catch (PJeBusinessException e) {
			log.error(e);
		}

	}
	
	public void authenticateUsuarioDomicilio() throws LoginException{
		
		authenticateUsuarioAutomaticamente(recuperaUsuarioDomicilio(), false);
	}
	
	public Usuario recuperaUsuarioDomicilio() {
		UsuarioLoginManager usuarioManager = (UsuarioLoginManager) Component.getInstance(UsuarioLoginManager.class, true);
		return (Usuario)usuarioManager.findByLogin(LOGIN_DOMICILIO);
	}
	
	public void authenticateUsuarioAutomaticamente(Usuario usuario,Boolean configuraLocalizacao) throws LoginException{
				
		if(usuario != null) {
			setWS(true);
			autenticaManualmenteNoSeamSecurity(usuario.getLogin());
			setUsuarioLogadoSessao(usuario);
			if(BooleanUtils.isTrue(configuraLocalizacao)) {
				selecionarLocalizacaoAtual(usuario);
			}

			Actor.instance().setId(usuario.getLogin());
		}
		else {
			throw new LoginException();
		}
		
	}

	public static Serializable getUsuarioBNMPLogado() {
		if (Contexts.getSessionContext().get(USUARIO_BNMP_LOGADO) != null) {
			return (Serializable) Contexts.getSessionContext().get(USUARIO_BNMP_LOGADO);
		}
		return null;
	}

	public static void setUsuarioBNMPLogado(Serializable auth) {
		Contexts.getSessionContext().set(USUARIO_BNMP_LOGADO, auth);
	}
	
	public TokenSso resgatarTokenSemUsuarioAssociado() {
		TokenSso token = (TokenSso) Contexts.getApplicationContext().get(Constantes.TOKEN_SSO_SEM_USUARIO_ASSOCIADO);

		if (token == null || LocalDateTime.now().isAfter(token.getExpirationDateTime())) {
			token = gerarNovoTokenSemUsuarioAssociado();
			Contexts.getApplicationContext().set(Constantes.TOKEN_SSO_SEM_USUARIO_ASSOCIADO, token);
		}

		return token;
	}

	private TokenSso gerarNovoTokenSemUsuarioAssociado() {
		Form form = new Form();
		form.param(GRANT_TYPE, CLIENT_CREDENTIALS);
		form.param(CLIENT_ID, ConfiguracaoIntegracaoCloud.getSSOClientId());
		form.param(CLIENT_SECRET, ConfiguracaoIntegracaoCloud.getSSOClientSecret());

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(ConfiguracaoIntegracaoCloud.getSSOAuthServerUrl())
				.path(ParametroUtil.getParametro(Parametros.PJE_SSO_ENDPOINT_TOKEN));
		Invocation.Builder invocationBuilder = webTarget.request();
		TokenSso token = invocationBuilder.post(Entity.form(form), TokenSso.class);

		if (token == null || token.getAccessToken() == null) {
			throw new PJeRuntimeException("Não foi possível obter as credenciais no SSO. Tente novamente mais tarde.");
		}

		return token;
	}
	
	public Usuario authenticateMigration(String login, String senha) throws LoginException {
		setWS(true);
		Usuario usuario = null;
		
		if (ProjetoUtil.isNaoVazio(login, senha)) {
			atribuirCredentials(login, senha);
			Identity.instance().login();
			
			usuario = obterUsuario(login);
		} else {
			throw new LoginException("Usuário ou senha inválidos.");
		}
		return usuario;
	}
}