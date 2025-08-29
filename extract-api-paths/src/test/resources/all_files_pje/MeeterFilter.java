/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.filter;

import java.io.IOException;
import java.io.Serializable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.ajax4jsf.webapp.BaseFilter;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.AbstractFilter;

import br.com.itx.component.MeasureTime;

// @Name("meeterFilter")
// @BypassInterceptors
// @Scope(ScopeType.APPLICATION)
// @Filter
public class MeeterFilter extends AbstractFilter implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(MeeterFilter.class);

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
			ServletException{
		MeasureTime sw = new MeasureTime(true);
		HttpServletRequest hsr = (HttpServletRequest) req;
		chain.doFilter(req, resp);
		String ajaxPushHeader = hsr.getHeader(BaseFilter.AJAX_PUSH_KEY_HEADER);
		long time = sw.getTime();
		if (ajaxPushHeader == null && time > 100){
			log.info(((HttpServletRequest) req).getRequestURI() + ": " + time);
		}
	}

	@Override
	public boolean isDisabled(){
		return !Init.instance().isDebug();
	}

}