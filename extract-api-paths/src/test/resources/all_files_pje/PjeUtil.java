package br.jus.cnj.pje.view;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.RandomStringUtils;
import org.mindrot.jbcrypt.BCrypt;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.status.SSOHealthIndicator;
import br.jus.cnj.pje.status.Status;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPJCNJ;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.pje.nucleo.util.StringUtil;


@Name(value="pjeUtil")
@Scope(ScopeType.EVENT)
public class PjeUtil {
	
	public static final String NAME = "pjeUtil";

	public static PjeUtil instance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	/**
	 * Retorno verdadeiro se o modo de operacao for Applet e falso caso seja PJeOffice.
	 * 
	 * Para descrobrir o valor de retorno, o metodo ira primeiramente recuperar o valor do parametro habilitarPJeOffice.
	 * 
	 * Se o pjeOffice estiver desativado o modo de operacao sera Applet e o retorno sera verdadeiro
	 * Se o pjeOffice estiver habilitado o metodo ira recuperar o cookie MO que tera o valor 'A' para Applet ou 'P' para PJeOffice.
	 * Se o valor do cookie for A o retorno sera verdadeiro
	 * Se o valor do cookie for P o retorno sera falso.
	 * 
	 * Nos casos em que o metodo não conseguir recuperar o objeto FacesContext ou valor do cookie MO
	 * O valor padrao para o modo de operacao sera o Applet e o retorno sera verdadeiro.
	 */
    public boolean isModoOperacaoApplet() {
    	
	    	FacesContext fc = FacesContext.getCurrentInstance();
	
	    	boolean resultado = true;
	    	
	    	if (isHabilitadoPJeOffice()) {
	    	
		    	if (fc != null) {
			    	
		    		Map<String, Object> cookie = fc.getExternalContext().getRequestCookieMap();
			    	
			    	Cookie cookieMO = (Cookie) cookie.get(Constantes.COOKIE_MODO_OPERACAO);
			    	
			    	if (cookieMO != null) {
			    	
			    		String modoOperacao = cookieMO.getValue();
		
			    		resultado = Constantes.MODO_OPERACAO.APPLET.equals(modoOperacao);
			    	} else {
			    		PjeUtil.instance().setCookie(Constantes.COOKIE_MODO_OPERACAO, Constantes.MODO_OPERACAO.PJE_OFFICE, -1);
			    		
			    		resultado = false;
			    	}
		    	}
	    	}
	    	
	    	return resultado;    	
    }
    
	public boolean isHabilitadoPJeOffice() {
		return ParametroUtil.instance().isHabilitadoPJeOffice();
	}
	
	public String getPjeOfficeCodigoSeguranca() {
		return ParametroUtil.instance().getPjeOfficeCodigoSeguranca();
	}

	public String getSessaoTextoAssinatura() {
		return (String) Component.getInstance(Authenticator.SESSAO_TEXTO_ASSINATURA, ScopeType.SESSION);
	}
    
    /**
     * Recupera o jsessionid da sessao do usuario
     * @return
     */
	public String getJsessionid() {
		String resultado = "";
		//Tenta pegar o JSessionId primeiramente pelo cookie
		resultado = getJSessionIdFromCookie();
		if(resultado.equals("")) {
			resultado = getJSessionIdFromUserSession();
		}
		return resultado;
	}
	
	public String getJSessionIdFromUserSession() {
		FacesContext fc = FacesContext.getCurrentInstance();
		String result = null;
		HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
		if (session != null) {				
			result = session.getId();
		}
		return (result == null ? "" : result);
	}
	
	public String getJSessionIdFromCookie() {
		
		String jsessionid = "";
		
		if(FacesContext.getCurrentInstance() != null) {
			jsessionid = this.getCookieFromFacesContext("JSESSIONID");
		} else {
			jsessionid = this.getCookieFromResteasyProvider("JSESSIONID");
		}
		
		return jsessionid;
	}
	
	private String getCookieFromFacesContext(String cookieName) {
		FacesContext fc = FacesContext.getCurrentInstance();
		String result = null;
		Map<String, Object> cookies = fc.getExternalContext().getRequestCookieMap();
		Cookie cookie = (Cookie)cookies.get(cookieName);
		if(cookie != null && cookie.getValue() != null && !cookie.getValue().equals("")) {
			result = cookie.getValue();
		}
		return (result == null ? "" : result);
	}

	private String getCookieFromResteasyProvider(String cookieName) {
		String jsessionid = null;
		
		if(ResteasyProviderFactory.getContextData(HttpServletRequest.class) != null) {
			Cookie[] cookies = ResteasyProviderFactory.getContextData(HttpServletRequest.class).getCookies();
			for (Cookie cookie : cookies) {
				if(cookie.getName().toUpperCase().equals(cookieName.toUpperCase())) {
					jsessionid = cookie.getValue();
					break;
				}
			}
		}
		
		return (jsessionid == null ? "" : jsessionid);
	}
	
		
	
	/**
	 * Retorna o jsessionid em formato de url
	 * @return
	 */
	public String getParamJsessionid() {		
		return "jsessionid=" + getJsessionid();
	}
	
	/**
	 * Retorna o jsessionid em formato cookie
	 * @return
	 */
	public String getCookieJsessionid() {
		return "JSESSIONID=" + getJsessionid();
	}
	
	/**
	 * Retorna o caracter e comercial 
	 * @return
	 */
	public String getE() {
		return "&";
	}
		
	/**
	 * Retorna o context path da aplicacao ex: /pje-web
	 * @return
	 */
	public String getContextPath() {
		String contextPath = "";
		
		if(FacesContext.getCurrentInstance() != null){
			contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath(); 
		} else if(ServletLifecycle.getCurrentServletContext() != null) {
			contextPath = ServletLifecycle.getCurrentServletContext().getContextPath();
		}
		
		return contextPath;
	}
	
	/**
	 * Retorna o nome do sistema ex: Conselho Nacional de Justiça
	 * @return
	 */
	public String getNomeSistema() {
		return ParametroUtil.getParametro(Parametros.NOME_SISTEMA);
	}
	
	/**
	 * Retorna a raiz web do servidor ex: "http://www2.tjro.gov.br/projudi/"
	 * @return
	 */
	public String getWebRoot() {
		return getServerUrl() + getContextPath();
	}
	
	/**
	 * Retorna a raiz web do servidor sem o protocolo ex: "www2.tjro.gov.br/projudi/"
	 * @return
	 */	
	public  String getWebRootSemProtocolo() {
		return this.getServerName() + this.getContextPath();
	}	
	
	/**
	 * Retorna a url do servidor ex: http://www2.tjro.gov.br
	 * @return
	 */
	public String getServerUrl() {
		return getServerAddress();
	}
	
	/**
	 * Retorna o endereco do servidor ex: http://172.22.22.22
	 * @return
	 */
	public String getServerAddress() {
		
		ServletRequest request = (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		if (!this.isDefaultPort(request.getServerPort())) {
			return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort(); 
		}
		else {
			return request.getScheme() + "://" + request.getServerName();
		}
	}
	
	public String getServerName() {
		ServletRequest request = (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		
		String serverName = request.getServerName();
		
		if(!this.isDefaultPort(request.getServerPort())) {
			serverName = serverName + ":" + request.getServerPort();
		}
		
		return serverName;
	} 
	
	public boolean isDefaultPort(Integer port) {
		return port == 80 || port == 443;
	}
	
	/**
	 * Constroe uma string de parametros para ser passados na url.
	 * 
	 * Exemplo:
	 * 
	 *  - param1=xpto
	 *  - param1=xpto&param2=xyz
	 * 
	 * @param args
	 * @return
	 */
	public String params(String ... args) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int i=0, t=args.length; i < t; i++) {
			
			sb.append(args[i]);
			
			if (i % 2 == 0) {
				sb.append("=");
			}
			else if (i+1 < t) {
				sb.append("&");
			}			
		}
		
		return sb.toString();
	}
	
	/**
	 * Metodo utilitario utilizado para o componente pjeOffice
	 * Retorna o parametro urlDocsField formatado para o componente PJeOffice
	 * 
	 * O valor do parametro urlDocsField podera ser um string com os dados que serao assinados ou
	 * o nome de uma funcao javascript que sera executada para obter os dados que serao assinados. 
	 * 
	 * @param urlDocsField O valor do parametro definido pelo desenvolvedor
	 * @return o valor de urlDocsField formatado para o componente PJeOffice.
	 */
	public String getUrlDocsFieldFormatadoParaPJeOffice(String urlDocsField) {
						
		if (urlDocsField != null && urlDocsField.contains("()")) {
			return urlDocsField;
		}
		else {
			return "\"" + urlDocsField + "\"";
		}
	}
	
	/**
	 * Registra o cookie 'cookieTemporizadorDownload' para que a tela identifique o fim do 
	 * processamento de download.
	 * 
	 * @param response HttpServletResponse
	 */
	public void registrarCookieTemporizadorDownload(HttpServletResponse response) {
		Cookie cookie = new Cookie("cookieTemporizadorDownload", "finalizado");  
		cookie.setMaxAge(30); //tempo de vida de 30 segundos.
		response.addCookie(cookie);
	}
	
	public String getPje2UrlCliente() {
		String ret = new String("");
		
		ret = ConfiguracaoIntegracaoCloud.getUrlPje2Cliente();
		
		return ret;
	}
	
	public String getPjeCloudAppName() {
		String ret = new String("");
		
		ret = ConfiguracaoIntegracaoCloud.getAppName();
		
		return ret;
	}
	
	public String getPjeUrlGatewayService() {
		return PjeEurekaRegister.instance().getUrlGatewayService(false);
	}

	public boolean isSSOAuthenticationEnabled() {
		boolean ret = false;

		SSOHealthIndicator ssoHealthIndicator = new SSOHealthIndicator();

		if (!ssoHealthIndicator.doHealthCheck().getStatus().equals(Status.UP)) {
			ret = false;
		} else {
			ret = ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled();
		}

		return ret;
	}
	
	public boolean isCorsEnabled() {
		boolean ret = true;
		
		ret = ConfiguracaoIntegracaoCloud.isCorsEnabled();
		
		return ret;
	}	
	
	public void setCookie(String name, String value, int expiry) {
		if (name.startsWith(KeycloakServiceClient.STATE_COOKIE_NAME) || name.startsWith("JSESSIONID")) {
			value = valueToRFC2965HeaderStringCookie(value);
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();

		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		Cookie cookie = null;
		
		Cookie[] userCookies = request.getCookies();
		if (userCookies != null && userCookies.length > 0) {
			for (int i = 0; i < userCookies.length; i++) {
				if (userCookies[i].getName().equals(name)) {
					cookie = userCookies[i];
					break;
				}
			}
		}

		if (cookie != null) {
			cookie.setValue(value);
		} else {
			cookie = new Cookie(name, value);
			cookie.setPath(request.getContextPath());
		}

		cookie.setMaxAge(expiry);

		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.addCookie(cookie);
	}
	
	/**
	 * Gera uma senha seguindo a regra: possuir letras, numeros e ter de 8 a 64
	 * caracteres.
	 * 
	 * @return String
	 */
	public String gerarValorNovaSenha(){
		String alpha = RandomStringUtils.randomAlphabetic(5);
		String num   = RandomStringUtils.randomNumeric(3);
		return alpha + num;
	}
	
	/**
	 * Retorna o hash da senha
	 * @param senha
	 * @return
	 */
	public String hashSenha(String senha){
		return BCrypt.hashpw(new String(senha.getBytes()), BCrypt.gensalt(12));
	}
	
	/**
	 * Cria uma nova senha e retorna o seu hash 
	 * @return String
	 */
	public String gerarNovaSenha(){
		String novaSenha = gerarValorNovaSenha();
		return hashSenha(novaSenha);
	}
	
	/**
	 * Gera o codigo de ativacao da senha do usuario e retorna seu hash.
	 * Baseia-se no nome do usuario e em um random de strings de 20 posicoes.
	 * Esse codigo sera adicionado ao link de ativacao da senha presente no
	 * email enviado ao usuario
	 * 
	 * @param username: login do usuario
	 * @return String: o codigo de ativacao da senha em hash
	 */
	public String gerarHashAtivacao(String username){
		String cd = username + RandomStringUtils.randomAlphabetic(20);
		return BCrypt.hashpw(cd, BCrypt.gensalt(12));
	}

	/**
	 * Valida a senha segundo o criterio: senha deve possuir letras, numeros e
	 * ter de 8 a 64 caracteres.
	 * 
	 * @return String
	 */
	public boolean validarSenha(String senha){
		String sPattern = "((?=.*\\d)(?=.*[a-z]|[A-Z]).{8,64})";
		Pattern p = Pattern.compile(sPattern);
		Matcher m = p.matcher(senha);
		return m.matches();
	}
	
	public boolean isMockReceitaEnabled() {
		return (ComponentUtil.getComponent(ConsultaClienteReceitaPJCNJ.NAME) instanceof br.jus.cnj.pje.nucleo.service.ConsultaClienteReceitaPJMock);		
	}

	public String toRFC2965HeaderString(Cookie cookie) {
        StringBuilder sb = new StringBuilder();

        if(cookie.getValue() != null) {
        	if(cookie.getValue().contains(";")) {
        		sb.append(cookie.getName()).append("=\"").append(cookie.getValue()).append('"');
        	} else {
        		sb.append(cookie.getName()).append("=").append(cookie.getValue());
        	}
        }

        if (cookie.getPath() != null)
            sb.append(";$Path=\"").append(cookie.getPath()).append('"');
        if (cookie.getDomain() != null)
            sb.append(";$Domain=\"").append(cookie.getDomain()).append('"');

		if (!cookie.getValue().toLowerCase().contains("secure")) {
			sb.append("; Secure");
		}

		if (!cookie.getValue().toLowerCase().contains("samesite")) {
			sb.append("; SameSite=None");
		}

        return sb.toString();
    } 
	
	public String getVersaoLegacy() {
		return ConfiguracaoIntegracaoCloud.getAppVersion();
	}

	public Optional<String> getCookie(String name) {
		return Arrays.stream(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getCookies())
			.filter(c -> name.equals(c.getName())).map(Cookie::getValue).findAny();
		
	}


    public String getUrlPDPJMarketplace() {
    	if(Identity.instance().hasRole(Papeis.PDPJ_VISUALIZAR_MARKETPLACE)) {
    		return ParametroUtil.instance().getUrlPDPJMarketplace();
    	}
    	return null;
    }

	public boolean isAutenticacaoSSOSemIframe() {
		try {
			boolean isSSOModoSemFrame = isNavegadorIOs();
			return isSSOModoSemFrame;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isNavegadorIOs() {
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			if (fc == null || fc.getExternalContext() == null) {
				return false;
			}
			String userAgent = fc.getExternalContext().getRequestHeaderMap().get("user-agent");
			if (userAgent == null) {
				return false;
			}
			userAgent = userAgent.toLowerCase();
			boolean isNavegadorIOs = userAgent.contains("iphone") || userAgent.contains("macintosh");
			return isNavegadorIOs;
		} catch (Exception e) {
			return false;
		}
	}

	public static String valueToRFC2965HeaderStringCookie(String cookie) {
		if(StringUtil.isEmpty(cookie)) {
			return cookie;
		}
		StringBuilder novoCookie = new StringBuilder();
		novoCookie.append(cookie);
		if(ConfiguracaoIntegracaoCloud.isCookieSecure()) {
			if (!cookie.toLowerCase().contains("secure")) {
				novoCookie.append("; Secure");
			}
		}

		if (!cookie.toLowerCase().contains("samesite")) {
			novoCookie.append("; SameSite=None");
		}
		return novoCookie.toString();
	}
}