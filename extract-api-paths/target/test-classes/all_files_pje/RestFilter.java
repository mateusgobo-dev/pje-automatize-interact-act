package br.jus.cnj.pje.integracao.pje2;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.web.AbstractFilter;

import br.com.itx.util.FacesUtil;

/**
 * Filtro para controle das requisições REST.
 * 
 * @author Adriano Pamplona
 */
@Name(RestFilter.NAME)
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
@Filter(within = "org.jboss.seam.web.contextFilter")
public class RestFilter extends AbstractFilter {

	public static final String NAME = "restFilter";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
	}

	@Override
	public String getUrlPattern() {
		return "/seam/resource/rest/pje-legacy/.*";
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		FacesUtil.novoFacesContext(request, response);
		chain.doFilter(request, res);
	}
}
