package br.jus.cnj.pje.webservice.controller.documento;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.cnj.pje.nucleo.service.LogAcessoAutosDownloadsService;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

@Name(DocumentoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/documento")
@Restrict("#{identity.loggedIn}")
public class DocumentoRestController {

	public static final String NAME = "documentoServiceController";
	
	private static final Logger logger = Logger.getLogger(DocumentoRestController.class.getName());
	private static final String APPLICATION_PDF = "application/pdf";
	private static final String TEXT_HTML = "text/html";


	@In
	private DocumentoBinManager documentoBinManager;
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;

	@GET
	@Path("/download/{idDocumento}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@SuppressWarnings("all")
	public Response download(@PathParam("idDocumento") Integer idDocumento, @Context HttpServletRequest request) {
		String idsDocumentos = (String)request.getSession().getAttribute("idsDocumentos");
		if(!idsDocumentos.contains(idDocumento.toString())){
			return null;
		}
		
		return doDownload(idDocumento, request);
	}

	private Response doDownload(Integer idDocumento, HttpServletRequest request) {
		ProcessoDocumentoBin documento = processoDocumentoBinManager.recuperar(idDocumento);
		if (documento == null) {
			return null;
		}
		
		String extensao = "text/html";
		String html = null;
				
		if(!documento.isBinario()) {
			html = documento.getModeloDocumento();
		}else{
			extensao = documento.getNomeArquivo() != null && documento.getNomeArquivo().toLowerCase().endsWith(".pdf") ? "application/pdf" : documento.getExtensao();
		}
		 
		boolean isHtml = extensao.equals("text/html") && html != null;
		boolean isPdf = extensao.equals("application/pdf") || extensao.equals("text/html"); 
		
		byte[] conteudo = null;
		try {
			if(isHtml) {
				html = obterConteudoHtml(html);
				conteudo = html.getBytes(StandardCharsets.UTF_8);
			}
			else {
				conteudo = documentoBinManager.getData(documento.getNumeroDocumentoStorage());
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		CacheControl cc = new CacheControl();
		
		if (conteudo == null) {
			isHtml = true;
			html = getDocumentNotFoundMessageTemplate(idDocumento);
			html = obterConteudoHtml(html);
			conteudo = html.getBytes(StandardCharsets.UTF_8);

			extensao = "text/html";

			cc.setNoCache(true);
			cc.setNoStore(true);
			cc.setMustRevalidate(true);
			cc.setMaxAge(0);
			String msg = MessageFormat.format("Documento requisitado para download n�o foi encontrado. Id: {0}; N�mero Storage: {1}", documento.getIdProcessoDocumentoBin(), documento.getNumeroDocumentoStorage());
			logger.log(Level.WARNING, msg);			
		} else {
			cc.setMaxAge(2592000);
			cc.setPrivate(true);
		}

		return montaRespostaDownload(documento, extensao, isHtml, conteudo, request);
	}

	private Response montaRespostaDownload(ProcessoDocumentoBin documento, String extensao, boolean isHtml, byte[] conteudo, HttpServletRequest request) {
		boolean isPdf = extensao.equals(APPLICATION_PDF) || extensao.equals(TEXT_HTML);
		String header = "";
		if(!isHtml) {
			if(isPdf){
				header = "filename=\"" + documento.getNomeArquivo() + "\"";
			}
			else{
				header = "attachment; filename=\"" + documento.getNomeArquivo() + "\"";
			}
		}
		
		LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);
		logAutos.logarDownload(documento.getProcessoDocumentoList().get(0), request);

	    ResponseBuilder builder = Response.ok(conteudo);
		builder.type(extensao);
		return builder.header("Content-Disposition", header).build();
	}
	
	private String getDocumentNotFoundMessageTemplate(Integer binarioId) {
		StringBuilder documentNotFoundMessageTemplate = new StringBuilder();
		documentNotFoundMessageTemplate.append("<div style='color: red;font-size: 1.2em;font-family: Verdana, sans-serif'>O documento de ID " + binarioId + " n�o pode ser recuperado. Tente novamente mais tarde.</div>");
		documentNotFoundMessageTemplate.append("<div style='color: red;font-size: 1.2em;font-family: Verdana, sans-serif'>Se o problema persistir, abra uma solicita��o para �rea t�cnica,</div>");
		documentNotFoundMessageTemplate.append("<div style='color: red;font-size: 1.2em;font-family: Verdana, sans-serif'>informando n�mero do processo e id do documento.</div>");
		return documentNotFoundMessageTemplate.toString();
	}
	
	@SuppressWarnings("all")
	private String obterConteudoHtml(String html) {		
		// Remove o conteudo que nao seja o conteudo dentro do body para ser exibido em tela
		String regexBodyAbertura = "<body[^>]*>";
		Pattern pattern = Pattern.compile(regexBodyAbertura + "([^<]*(?:(?!<\\/?body)<[^<]*)*)<\\/body\\s*>",Pattern.DOTALL);

		Matcher matcher = pattern.matcher(html);
		try {
			while(matcher.find()) {
				html = matcher.group(1);
				break;
			}					
		} catch (java.lang.StackOverflowError e) {
			Integer inicioConteudoBody = null;
			Integer finalConteudoBody = null;
			Matcher matcherTagBodyAbertura = Pattern.compile(regexBodyAbertura).matcher(html);
			if(matcherTagBodyAbertura.find()) {
				inicioConteudoBody = matcherTagBodyAbertura.end();
			}
			
			Matcher matcherTagBodyFechamento = Pattern.compile("</body\\s*>").matcher(html);
			if(matcherTagBodyFechamento.find()) {
				finalConteudoBody = matcherTagBodyFechamento.start();
			}
			if(inicioConteudoBody != null && finalConteudoBody != null) {
				html = html.substring(inicioConteudoBody, finalConteudoBody);
			}

		}
		return html;
	}
}
