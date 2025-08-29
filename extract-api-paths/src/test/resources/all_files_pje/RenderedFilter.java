package br.com.infox.pje.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.web.AbstractFilter;
import org.xhtmlrenderer.pdf.ITextRenderer;

import br.com.infox.bpm.parser.XmlEntitiesParser;
import br.com.infox.pje.servlet.ContentCaptureServletResponse;

/**
 * Classe que realiza o filtro das páginas que devem ser convertidas em outros
 * formatos como, pdf, imagem, etc.. No entanto, até agora só existe a
 * implementação para formatos do tipo PDF. Esta classe está registrada no
 * arquivo web.xml para que o servlet a reconheça.
 * 
 * @author Daniel
 * 
 */
@Name("renderedFilter")
@BypassInterceptors
@Scope(ScopeType.APPLICATION)
@Filter
public class RenderedFilter extends AbstractFilter {

	FilterConfig config;
	private static final String PATTERN = ".*/report/.*|.*/Report/.*|.*/REPORT/.*";

	@Override
	public String getRegexUrlPattern() {
		return PATTERN;
	}

	@Override
	public void destroy() {
	}

	/**
	 * Método de implementação obrigatória que é chamado quando uma requisição é
	 * feita.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		/*
		 * Obtém o conteúdo da requisição, deve deverá ser transformado em outro
		 * tipo
		 */
		ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(response);
		filterChain.doFilter(request, capContent);
		try {
			HttpServletRequest httpRequest = request;
			String path = httpRequest.getRequestURI();
			String fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")) + ".pdf";
			/*
			 * Transforma o conteúdo do xhtml para um documento que é legível
			 * pelo xhtmlRendered
			 */
			ITextRenderer renderer = new ITextRenderer();
			/*
			 * Retira os imports do richfaces pois ele gera um erro devido a
			 * ausencia do arquivo css e js no projeto (pois fica no jar do
			 * richfaces e a4j).
			 */
			String content = sanitizeXhtml(capContent.getContent());
		    renderer.setDocumentFromString(XmlEntitiesParser.parse(content));
			renderer.layout();
			response.setContentType("application/pdf");
			response.setHeader("Content-disposition", "attachment; filename=\" " + fileName + "\"");
			OutputStream browserStream = response.getOutputStream();
			renderer.createPDF(browserStream);
			return;
			/*
			 * Implementar aqui outros formatos caso existe necessidade, como
			 * imagens e etc...
			 */
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private String sanitizeXhtml(String content) {
		content = content.replaceAll("<script.*</script>", "");
		content = Normalizer.normalize(content, java.text.Normalizer.Form.NFD);
//		content = content.replaceAll("[^\\p{ASCII}]","");	
		content = content.replaceAll("<br>", "<br />");
		return content;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

}