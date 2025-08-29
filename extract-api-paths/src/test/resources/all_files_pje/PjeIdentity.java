package br.jus.cnj.pje.nucleo.identity;

import static org.jboss.seam.ScopeType.SESSION;

import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.client.AuthenticationRestFilter;
import br.jus.cnj.pje.webservice.client.SsoServiceClient;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
@Startup
public class PjeIdentity extends Identity{
	
	private static final long serialVersionUID = 5535606116243327131L;
	
	public Boolean logouComCertificado;
	public Boolean tokenValido;
	public Boolean cancelouToken;

	
	private SsoServiceClient ssoServiceClient;
	
	@Override
	@Create
	public void create() {	
		super.create();
		logouComCertificado = false;
		this.ssoServiceClient = ComponentUtil.getComponent("keycloakServiceClient");
		tokenValido = false;
		cancelouToken = false;

	}
	
	@Override
	public void unAuthenticate() {
		logouComCertificado = false;
		tokenValido = false;
		cancelouToken = false;
		super.unAuthenticate();
	}
	
	@Override
	public void logout() {
		if(ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled()){
			ssoServiceClient.logout();
		}
		super.logout();
	}
	
	@Override
	public void checkRestriction(String expr) {
		if(Authenticator.getUsuarioLogado() == null) {
			String token = null;
			if(ResteasyProviderFactory.getContextData(HttpServletRequest.class) != null 
					&& ResteasyProviderFactory.getContextData(HttpServletRequest.class).getHeader(AuthenticationRestFilter.PRE_AUTH_TOKEN_HEADER) != null) {
				token = ResteasyProviderFactory.getContextData(HttpServletRequest.class).getHeader(AuthenticationRestFilter.PRE_AUTH_TOKEN_HEADER);
				try {
					Authenticator.obterUsuarioToken(token);
					super.checkRestriction(expr);
					Identity.instance().logout();
				}
				catch(Exception ex){
					throw new AuthorizationException(String.format(
				               "Authorization check failed for token [%s]", AuthenticationRestFilter.PRE_AUTH_TOKEN_HEADER));
				}
			} else {
				super.checkRestriction(expr);
			}
		}
		else{
			super.checkRestriction(expr);
		}
	}

	public Boolean isLogouComCertificado() {
		return logouComCertificado;
	}

	public void setLogouComCertificado(Boolean logouComCertificado) {
		this.logouComCertificado = logouComCertificado;
	}

	public Boolean isTokenValido() {
		return tokenValido;
	}

	public void setTokenValido(Boolean tokenValido) {
		this.tokenValido = tokenValido;
	}

	public Boolean isCancelouToken() {
		return cancelouToken;
	}

	public void setCancelouToken(Boolean cancelouToken) {
		this.cancelouToken = cancelouToken;
	}

}
