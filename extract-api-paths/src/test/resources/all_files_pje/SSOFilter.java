package br.jus.cnj.pje.nucleo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.ajax4jsf.webapp.FilterServletResponseWrapper;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.springframework.http.HttpStatus;

import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.cnj.pje.webservice.client.keycloak.SSOConfigResolver;

public class SSOFilter extends KeycloakOIDCFilter implements Filter{
	
	public SSOFilter() {
		super(new SSOConfigResolver());
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		try{

			boolean isSSOAuthEnabled = ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled();
			boolean isSSOAuthenticated = this.isKeycloakAuthenticated(request.getCookies());
			boolean skip = this.shouldSkip(request);
			
			if(!request.getMethod().equalsIgnoreCase("OPTIONS") &&
					((!skip && isSSOAuthEnabled) || 
				     (!skip && isSSOAuthenticated))){
				if(request.getHeader("X-no-sso") == null) {
					super.doFilter(req, res, chain);
					checkResourceRestriction(req, res, chain);
				} else {
					chain.doFilter(req, res);
				}
			} else {
				chain.doFilter(req, res);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			chain.doFilter(req, res);
		}
	}

	private void checkResourceRestriction(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IllegalAccessException, InvocationTargetException, IOException,
			ServletException {
		try {
			Method getResponseMethod = res.getClass().getMethod("getResponse");
			FilterServletResponseWrapper servletResponse = (FilterServletResponseWrapper)((ServletResponse)getResponseMethod.invoke(res, new Object[] {}));
			Map<String, Object> headers = servletResponse.getHeaders();
			String headerSetCookie = (String) headers.get("Set-Cookie");
			
			configureKeycloakStateCookie(servletResponse, headerSetCookie);
			
			if(servletResponse.getStatus() == HttpStatus.MOVED_TEMPORARILY.value() 
					&& headers.containsKey("Location") 
					&& !((String)headers.get("Location")).contains("authenticateSSO.seam")
					&& !((String)headers.get("Location")).contains("authenticateSSO_semIframe.seam")) {
				chain.doFilter(req, res);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// swallow
			e.printStackTrace();
		}

	}

	private boolean isKeycloakAuthenticated(Cookie[] cookies) {
		boolean authenticated = false;
		
		if ( cookies!=null ) {
			for (Cookie cookie : cookies) {
				if(cookie.getName().equals(Constantes.SSO_COOKIE_NAME)) {
					authenticated = true;
					break;
				}
			}		
		}
		
		return authenticated;
	}
	
    private boolean shouldSkip(HttpServletRequest request) throws ServletException, IOException {
    	
        if (skipPattern == null) {
            return false;
        }

        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        
        final Boolean[] isLoginRequired = {true};

        if(requestPath.contains(".seam") && !requestPath.contains("authenticateSSO.seam") && !requestPath.contains("authenticateSSO_semIframe.seam")) {
        	new ContextualHttpServletRequest(request)
        	{
        		@Override
        		public void process() throws ServletException, IOException, LoginException
        		{      
        			isLoginRequired[0] = Pages.instance().isLoginRedirectRequired(FacesContext.getCurrentInstance());       			
        		}
        	}.run();        	
        }
        
        return skipPattern.matcher(requestPath).matches() || !isLoginRequired[0];
    }
	
	private void configureKeycloakStateCookie(FilterServletResponseWrapper servletResponse, String headerSetCookie) {
		if(headerSetCookie != null && headerSetCookie.contains(KeycloakServiceClient.STATE_COOKIE_NAME)){
			List<String> listaCookies = Arrays.asList(headerSetCookie.split(";"));
			
			listaCookies.forEach(item -> {
				if(item.startsWith(KeycloakServiceClient.STATE_COOKIE_NAME)) {
					int indicie = listaCookies.indexOf(item);
					listaCookies.set(indicie, PjeUtil.valueToRFC2965HeaderStringCookie(item));
				}
			});
			
			String setCookieResponse= listaCookies.stream().map(item -> item).collect(Collectors.joining(";"));
			
			servletResponse.setHeader("Set-Cookie", setCookieResponse );
		}
	}
}
