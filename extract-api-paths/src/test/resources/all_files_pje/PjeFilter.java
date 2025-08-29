package br.jus.cnj.pje.nucleo;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.servlet.SeamFilter;

@Name(PjeFilter.NAME)
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
public class PjeFilter extends SeamFilter {
	
	public static final String NAME = "pjeFilter";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {

		HttpServletResponse res = (HttpServletResponse) response;
		
		String path = ((HttpServletRequest) request).getRequestURI();
		
		if (path.contains("/servicoMensagem/") || path.contains("resource/upload")) {
			chain.doFilter(request, response);			
		} else {			
			super.doFilter(request, res, chain);
		}	
	}
	
}