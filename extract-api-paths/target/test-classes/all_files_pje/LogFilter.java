package br.jus.cnj.pje.integracao.pje2;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.web.AbstractFilter;

import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Filtro para controle de CORS no pje-legacy
 *
 */
@Name(LogFilter.NAME)
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
@Filter(within="org.jboss.seam.web.contextFilter")
public class LogFilter extends AbstractFilter{

	private static final String NOME_PARAMETRO_LOG_CPF_USUARIO_LOGADO = "cpfUsuarioLogado";
	public static final String NAME = "logFilter";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
	}
	
	@Override
	public String getUrlPattern() {
		return  "/.*";
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
	    
		if(request != null && request.getSession() != null) {
			Object usuario = request.getSession().getAttribute("usuarioLogado");
			org.apache.log4j.MDC.put(NOME_PARAMETRO_LOG_CPF_USUARIO_LOGADO, usuario != null ? ((Usuario) usuario).getLogin() : "");
		}
			
		chain.doFilter(request, res);							
	}
}
