package br.jus.cnj.pje.webservice.client;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.servlet.FilterSessionStore;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore.SerializableKeycloakAccount;
import org.keycloak.adapters.spi.KeycloakAccount;

import com.sun.syndication.io.impl.Base64;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

@Name(KeycloakSsoServiceClient.NAME)
@Scope(ScopeType.EVENT)
public class KeycloakSsoServiceClient implements SsoServiceClient {

	public static final String NAME = "ssoServiceClient";
	private static final String KEYCLOAK_CONFIG_PATH = "/WEB-INF/keycloak.json";
	private KeycloakDeployment kcDeployment;
	
	@Create
	public void init(){
		this.kcDeployment = this.getKeycloakConfig();
	}
	
	@Override
	public void logout() {
		
		if(ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled() && Authenticator.isSSOAuthentication()){
			
			if(this.kcDeployment == null){
				this.kcDeployment = this.getKeycloakConfig();
			}
			
			if(Contexts.getSessionContext().get("org.keycloak.KeycloakSecurityContext") != null){
				
				
				RefreshableKeycloakSecurityContext ksc = (RefreshableKeycloakSecurityContext)Contexts.getSessionContext()
																			.get("org.keycloak.KeycloakSecurityContext");
//				String refreshToken = ksc.getRefreshToken();
				
				HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

				if (httpSession != null) {
		            SerializableKeycloakAccount account = (SerializableKeycloakAccount) httpSession.getAttribute(KeycloakAccount.class.getName());
		            if (account != null) {
		                account.getKeycloakSecurityContext().logout(this.getKeycloakConfig());
		            }
		            cleanSession(httpSession);
		        }				
				
//				try{
//					String credentials = this.kcDeployment.getResourceName() + ":" + this.kcDeployment.getResourceCredentials().values().iterator().next();
//					//pje:80002c3b-1bfe-4607-9ae1-ebc6b9b181af
//					String webroot = URLEncoder.encode(this.getWebRoot(), "UTF-8");
//					URL url = this.getLogoutUrl(webroot);
//					HttpClient client = HttpClients.createDefault();
//					HttpPost post = this.getLogoutHttpPost(url.toString(), refreshToken, credentials);
//					client.execute(post);					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		}

	}
	
    protected void cleanSession(HttpSession session) {
        session.removeAttribute(KeycloakAccount.class.getName());
        session.removeAttribute(KeycloakSecurityContext.class.getName());
        session.removeAttribute(FilterSessionStore.REDIRECT_URI);
        session.removeAttribute(FilterSessionStore.SAVED_METHOD);
        session.removeAttribute(FilterSessionStore.SAVED_HEADERS);
        session.removeAttribute(FilterSessionStore.SAVED_BODY);
    }
	
	private URL getLogoutUrl(String webroot) throws MalformedURLException{
		return new URL("http://localhost:8080/auth/realms/pje-realm/protocol/openid-connect/logout?redirect_uri=" + webroot + "&client_id=pje");
	}
	
	private HttpPost getLogoutHttpPost(String url, String refreshToken, String credentials) throws UnsupportedEncodingException{
		HttpPost post = new HttpPost(url.toString());
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		
		params.add(new BasicNameValuePair("client_id", this.kcDeployment.getResourceName()));
		params.add(new BasicNameValuePair("refresh_token", refreshToken));
		post.setHeader("Content-type", "application/x-www-form-urlencoded");
		post.setHeader("Authorization", "Basic " + Base64.encode(credentials));
		post.addHeader("client_id", this.kcDeployment.getResourceName());
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		return post;
	}
	
	private KeycloakDeployment getKeycloakConfig(){
		FacesContext fc = FacesContext.getCurrentInstance();
		KeycloakDeployment keycloakDeployment = null;
		
		if(fc != null){
			InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(KeycloakSsoServiceClient.KEYCLOAK_CONFIG_PATH);
			
			keycloakDeployment = KeycloakDeploymentBuilder.build(is);
		}
		
		return keycloakDeployment;
	}
	
	private String getContextPath() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
	}
	
	private String getWebRoot() {
		return getServerUrl() + getContextPath();
	}
	
	private String getServerUrl() {
		return getServerAddress();
	}
	
	private String getServerAddress() {
		
		ServletRequest request = (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		if (request.getServerPort() != 80 && request.getServerPort() != 443) {
			return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort(); 
		}
		else {
			return request.getScheme() + "://" + request.getServerName();
		}
	}	

}
