package br.jus.cnj.pje.integracao.pje2;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.web.AbstractFilter;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Contexts;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.util.StringUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.util.CarregarParametrosAplicacao;

/**
 * Filtro para controle de CORS no pje-legacy
 *
 */
@Name(CorsFilter.NAME)
@Scope(ScopeType.APPLICATION)
@Startup()
@Install(precedence = Install.APPLICATION, dependencies = {CarregarParametrosAplicacao.NAME, ParametroUtil.NAME})
@BypassInterceptors
@Filter(around = {"org.jboss.seam.web.contextFilter", "org.jboss.seam.web.identityFilter"})
public class CorsFilter extends AbstractFilter{

	public static final String NAME = "corsFilter";
	private String _UrlPje2Cliente;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
	}
	
	@Override
	public String getRegexUrlPattern() {
		return "/seam/resource/rest/pje-legacy/.*";
	}
	
	private String getUrlPje2Cliente() {
		if (_UrlPje2Cliente==null) {
			_UrlPje2Cliente = ConfiguracaoIntegracaoCloud.getUrlPje2Cliente();
		}
		return _UrlPje2Cliente;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		if(StringUtil.isEmpty(request.getHeader("x-pje-gateway-request"))) {
			String pathCliente = getUrlPje2Cliente();
			
			if(StringUtil.isEmpty(pathCliente)){
				pathCliente = "http://localhost:4200";
			}	
			
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", pathCliente);
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");				
			response.setHeader("Access-Control-Allow-Headers","origin, content-type, accept, authorization, x-pje-legacy-app, x-no-sso, X-pje-cookies, X-pje-usuario-localizacao, X-pje-authorization");
			request.setCharacterEncoding("ISO-8859-1");
			if(request.getMethod().equalsIgnoreCase("OPTIONS")){
				HttpSession session = request.getSession(false);
				if (session!=null && session.isNew()) {					
					//A sessão é invalidada pois do contrário a cada requisição do tipo OPTIONS uma nova sessão é criada e permanecerá até seu timeout. 
					//Nenhuma autenticação de usuário é feita com o método OPTIONS.
					if (Contexts.isSessionContextActive()) {
						org.jboss.seam.web.Session.instance().invalidate();
					} else {
						session.invalidate();
					}
				}
				
				response.setStatus(HttpServletResponse.SC_OK);
				response.flushBuffer();
				return;
			}
		}
		
		chain.doFilter(request, res);							
	}
}
