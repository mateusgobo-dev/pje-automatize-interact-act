package br.jus.csjt.pje.business.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.activemq.util.ByteArrayInputStream;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import br.com.infox.cliente.util.HttpUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;

public class HtmlParaPdf {

	private static final String ERRO_AO_CONVERTER_HTML_PARA_PDF_EM_API_EXTERNA = "Erro ao converter Html para PDF em API externa: ";

	public static byte[] converte(String html) throws PdfException{
		try {
			return converteSemTratamentoPeloJsoup(html);
		} catch (Exception e) {
			String textoHtmlCorrigidoPeloJsoup = toDocumentJsoup(html).outerHtml();
			String textoHtmlCorrigidoPeloJsoupESemTabStopsNegativo = obterTextoHtmlSemTabStopsNegativo(textoHtmlCorrigidoPeloJsoup);
			return converteSemTratamentoPeloJsoup(textoHtmlCorrigidoPeloJsoupESemTabStopsNegativo);
		}
	}
	
	public static byte[] converteSemTratamentoPeloJsoup(String html) throws PdfException {
		html = removeCssNaoSuportado(html);

		org.jsoup.nodes.Document document = toDocumentJsoup(html);

		removeTabelasVazias(document);

		adicionarPropriedadeCssNasTabelasDoDocumento(document, "word-break: break-all");

		Map<String, String> novosElementos = corrigirPalavrasGrandesEmTabelasHTML(document, 26);

		html = document.outerHtml();

		for (Map.Entry<String, String> entry : novosElementos.entrySet()) {
			html = html.replace(entry.getKey(), entry.getValue());
		}

		if ("true".equalsIgnoreCase(ParametroUtil.getParametro(Parametros.PJE_CONVERSAO_HTML_PDF_EXTERNA_ENABLE))) {
			return conversorhtml2pdf(html);
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Document doc = new com.itextpdf.text.Document();

			try {
				PdfWriter writer = PdfWriter.getInstance(doc, baos);

				doc.open();

				XMLWorkerHelper.getInstance().parseXHtml(writer, doc, new ByteArrayInputStream(html.getBytes()));
			} catch (Exception e) {
				throw new PdfException(e.getLocalizedMessage());
			} finally {
				if (doc != null) {
					doc.close();
				}
			}

			return baos.toByteArray();
		} catch (Exception e1) {
			throw new PdfException(e1.getLocalizedMessage());
		}
	}

	private static byte[] conversorhtml2pdf(String html) throws PdfException {
		try {
			RequestSpecification request = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON)
					.body(html);

			String url = ParametroUtil.getParametro(Parametros.PJE_CONVERSAO_HTML_PDF_EXTERNA_URL);

			io.restassured.response.Response response = request.request(Method.POST, url);

			if (HttpUtil.isStatus2xx(response.getStatusCode())) {
				byte[] body = response.getBody().asByteArray();

				return decompress(body);
			} else {
				throw new PdfException(ERRO_AO_CONVERTER_HTML_PARA_PDF_EM_API_EXTERNA + response.getStatusCode());
			}
		} catch (PdfException pdfe) {
			throw new PdfException(pdfe);
		} catch (Exception e) {
			throw new PdfException(ERRO_AO_CONVERTER_HTML_PARA_PDF_EM_API_EXTERNA + e.getLocalizedMessage(), e);
		}
	}

	private static byte[] decompress(final byte[] bytesCompressed) throws IOException {
		if ((bytesCompressed == null) || (bytesCompressed.length == 0)) {
			return null;
		}

		if (isGZIPStream(bytesCompressed)) {
			try (GZIPInputStream gzipIs = new GZIPInputStream(new ByteArrayInputStream(bytesCompressed))) {
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					byte[] buffer = new byte[1024];

					int len;

					while ((len = gzipIs.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}

					return out.toByteArray();
				}
			}
		}

		return null;
	}

	private static boolean isGZIPStream(byte[] bytes) {
		return bytes[0] == (byte) GZIPInputStream.GZIP_MAGIC && bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> 8);
	}

	private static org.jsoup.nodes.Document toDocumentJsoup(String html) {
		org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(html);
		document.outputSettings().syntax( org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document;
	}
	
	public static String obterTextoHtmlSemTabStopsNegativo(String textoHtml) {
		StringBuilder novoHtmlBuilder = new StringBuilder(textoHtml);
		Pattern patternToGetTabStops = Pattern.compile("tab-stops:((pt|cm|list|right|\\d| |[.]|-)*)(\"|;)+");
		Matcher matcherToHInit = patternToGetTabStops.matcher(textoHtml);
		while(matcherToHInit.find()) {
			int posicaoValorGetTabStopsInicio = matcherToHInit.start(1);
			int posicaoValorGetTabStopsFim = matcherToHInit.end(1);
			novoHtmlBuilder.replace(posicaoValorGetTabStopsInicio, posicaoValorGetTabStopsFim, atribuirZeroParaValorNegativo(novoHtmlBuilder.substring(posicaoValorGetTabStopsInicio, posicaoValorGetTabStopsFim)));
					
					return novoHtmlBuilder.substring(0,posicaoValorGetTabStopsFim) + obterTextoHtmlSemTabStopsNegativo(novoHtmlBuilder.substring(posicaoValorGetTabStopsFim, novoHtmlBuilder.length()));
				
			
		}
		return novoHtmlBuilder.toString();
	}


	private static String atribuirZeroParaValorNegativo(String substring) {
		return substring.replaceAll("-[0-9.]*", "0");
	}

	
	private static Map<String, String> corrigirPalavrasGrandesEmTabelasHTML(org.jsoup.nodes.Document document,
			Integer limiteCaracteresParaPalavra) {
	    Map<String, String> novosElementos = new HashMap<String, String>();
		if (seExistemElementosComPalavrasGrandes(document, "td", limiteCaracteresParaPalavra)) {
			adicionarPropriedadeCssNasTabelasDoDocumento(document, "width: 740px !important");
			novosElementos = realizarNasPalavrasGrandes(document, "td", limiteCaracteresParaPalavra, (palavra) -> {
				String novaPalavra = palavra.replace("_", " ");
				if(novaPalavra != null && seTextoPossuiPalavrasMaioresQue(novaPalavra, limiteCaracteresParaPalavra)) {
					novaPalavra = obterTextoComEspacoEntrePalavrasIdentificadas(palavra, "[a-zA-Z]+");
				}
				if(novaPalavra != null && seTextoPossuiPalavrasMaioresQue(novaPalavra, limiteCaracteresParaPalavra)) {
					novaPalavra = obterTextoComEspacoEntrePalavrasIdentificadas(novaPalavra, "[A-Z][a-z]+");
				}
				return novaPalavra;
			});

		}
		return novosElementos;
	}

	public static Boolean seExistemElementosComPalavrasGrandes(org.jsoup.nodes.Document document, String tagToVerify,
			Integer qntdCaracteresLimite) {
		Elements elements = document.getElementsByTag(tagToVerify);
		for (Element element : elements) {
			String textoSemTagsHtml = element.text();
			if(seTextoPossuiPalavrasMaioresQue(textoSemTagsHtml, qntdCaracteresLimite)) {
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	private static Boolean seTextoPossuiPalavrasMaioresQue(String texto, Integer qntdCaracteresLimite) {
		String[] palavras = texto.split(" ");
		for (String palavraIt : palavras) {
			if (palavraIt != null && palavraIt.length() > qntdCaracteresLimite) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}

	public static Map<String, String> realizarNasPalavrasGrandes(org.jsoup.nodes.Document document, String tagToVerify, Integer qntdCaracteresLimite,
			Function<String, String> acaoEmCadaPalavra) {
		Map<String, String> novosElementos = new HashMap<String, String>();
		Elements elements = document.getElementsByTag(tagToVerify);
		for (Element element : elements) {
			String htmlElemento = element.outerHtml();
			String textoSemTagsHtml = element.text();
			String[] palavras = textoSemTagsHtml.split(" ");
			Boolean flagPalavraMudou = Boolean.FALSE;
			for (String palavraIt : palavras) {
				if (palavraIt != null && palavraIt.length() > qntdCaracteresLimite && !palavraIt.matches("^[0-9]+$")) {
					String novaPalavra = acaoEmCadaPalavra.apply(palavraIt);
					htmlElemento = htmlElemento.replace(palavraIt, novaPalavra);
					flagPalavraMudou = Boolean.TRUE;
				}
			}
			if (flagPalavraMudou) {
				novosElementos.put(element.outerHtml(), htmlElemento);
			}
		}

		return novosElementos;
	}
	
	private static String obterTextoComEspacoEntrePalavrasIdentificadas(String palavraIt, String regex) {
		StringBuilder novaPalavra = new StringBuilder(palavraIt);
		Matcher matcher = Pattern.compile(regex).matcher(palavraIt);
		List<Integer> posicoesParaInserirEspaco = new ArrayList<Integer>();
		while (matcher.find()) {
			if (matcher.start() != 0) {
				posicoesParaInserirEspaco.add(matcher.start());
			}

			if (matcher.end() != palavraIt.length()) {
				posicoesParaInserirEspaco.add(matcher.end());
			}
		}

		for (int i = 0; i < posicoesParaInserirEspaco.size(); i++) {
			Integer posicaoParaInserirEspaco = posicoesParaInserirEspaco.get(i);
			novaPalavra.insert(posicaoParaInserirEspaco + i, " ");
		}
		return novaPalavra.toString();
	}

	public static Boolean seTextoContem(String texto, String regex) {
		return Pattern.compile(regex).matcher(texto).find();
	}

	private static void removeTabelasVazias(org.jsoup.nodes.Document document) {
		Elements elements = document.getElementsByTag("table");

		for (Element table : elements) {
			Elements rows = table.select("tr");

			int rowSize = rows == null ? 0 : rows.size();

			if (rowSize > 0) {
				for (int i = 1; i < rowSize; i++) {
					Element row = rows.get(i);

					Elements cols = row.select("td");

					if (cols == null || cols.size() == 0) {
						table.remove();

						break;
					}
				}
			} else {
				table.remove();
			}
		}
	}

	private static String removeCssNaoSuportado(String html) {
		html = html.replaceAll("font-size:\\s*calc\\(var\\(--scale-factor\\)\\*\\d+\\.\\d+px\\);?", "");

		return html.replaceAll("font-size:\\s*var\\(--fontSizeBase300\\);?", "");
	}

	private static void adicionarPropriedadeCssNasTabelasDoDocumento(org.jsoup.nodes.Document document, String propriedadeCss) {
		Elements elements = document.getElementsByTag("table");
		for (Element element : elements) {
			element.attr("style", element.attr("style") + "; " + propriedadeCss + ";");
		}
	}
	
	/**
	 * Converte a String passada por parmetro para um InputStream.
	 * 
	 * @param html String do HTML.
	 * @return InputStream ABERTO.
	 * @throws PdfException
	 */
	public static InputStream converteParaInputStream(String html) throws PdfException {
		return new ByteArrayInputStream(converte(html));
	}

}