package br.jus.csjt.pje.business.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hibernate.LazyInitializationException;
import org.jboss.seam.security.Identity;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.pje.service.ItextHtmlConverterService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.PdfUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.service.LogAcessoAutosDownloadsService;
import br.jus.csjt.pje.commons.util.FileUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import net.vidageek.mirror.dsl.Mirror;

/**
 * Unificador de documentos do processo em um único PDF. Implementado inicialmente por Andrei Sá do TRT-13 e portado para o PJe por Levi Mota.
 * 
 * @author Andrei Sá (TRT-13)
 * @author Levi Mota (PJe / TRT-20)
 */
public class GeradorPdfUnificado{

	private static final float margemParaAssinatura = 30f;
	private static Font fontCabec;
	private static Font fontCabecBold;
	private static Font fontCorpo;
	private static Font fontCorpoBold;
	private static Font fontSublinhado;
	private static Font fontProc;
	private static Font fontProcBold;
	private static Font fontDocumento;
	private static Font fontDocumentoBold;
	private static Font red;
	private static BaseFont bf;
	private static BaseColor corCabecTabela;
	private static BaseColor corLinhaPar;
	private static BaseColor corLinhaImpar;
	private SimpleDateFormat dataHoraFormater;
	private SimpleDateFormat dataFormater;
	private boolean gerarIndiceDosDocumentos = true;
	private boolean gerarInfoClasseAtual = true;
	

	private static Logger log = Logger.getLogger(GeradorPdfUnificado.class);
	private String resourcePath;
	
	private static String HTML_VAZIO = "<p></p>";
	
	static{
		fontCabec = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
		fontCabecBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
		fontCorpo = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
		fontSublinhado = new Font(Font.FontFamily.HELVETICA, 10, Font.UNDERLINE, BaseColor.BLUE);
		fontCorpoBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
		fontProc = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL);
		fontProcBold = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
		fontDocumento = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
		fontDocumentoBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

		corCabecTabela = new BaseColor(220, 220, 220);
		corLinhaPar = new BaseColor(242, 242, 242);
		corLinhaImpar = new BaseColor(255, 255, 255);

		try{
			bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException e){
			log.error("Erro ao criar a fonte HELVETICA.", e);
		} catch (IOException e){
			log.error("A fonte HELVETICA nao existe.", e);
		}

	}

	public GeradorPdfUnificado(){
		dataHoraFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		dataFormater = new SimpleDateFormat("dd/MM/yyyy");

		red = FontFactory.getFont(BaseFont.HELVETICA, 9, Font.NORMAL, new BaseColor(0xFF, 0x00, 0x00));
	}

	/**
	 * Gera um PDF único contendo a lista de documentos passado por parâmetro.
	 * 
	 * @param processoTrf Processo dos documentos.
	 * @param processoDocumentos Lista dos documentos que serão mesclados em um único documento.
	 * 
	 * @return Array de bytes do PDF resultante.
	 */
	public byte[] gerarPdfUnificado(ProcessoTrf processoTrf, List<ProcessoDocumento> processoDocumentos)
			throws PdfException {
		byte[] result = null;

		ByteArrayOutputStream byteArray = null;

		try {
			byteArray = new ByteArrayOutputStream();
			gerarPdfUnificado(processoTrf, processoDocumentos, byteArray);
			result = byteArray.toByteArray();
		} finally {
			if (byteArray != null) {
				try {
					byteArray.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}
	
	/**
	 * Configura o documento
	 * 
	 * @param processoTrf Processo dos documentos.
	 * @param processoDocumentos Lista dos documentos que serão mesclados em um único documento.
	 * @param outputStream Retorno do documento PDF gerado.
	 * @throws PJeBusinessException, LazyInitializationException 
	 */
	private void configurarDocumentoPdf(ProcessoTrf processoTrf, ProcessoParteManager processoParteManager, Document document) throws PJeBusinessException, LazyInitializationException {
		AssuntoTrfManager asm = ComponentUtil.getComponent("assuntoTrfManager");
		// lista de atores
		int aux = 0;
		StringBuilder subject = new StringBuilder();
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>();
		try{
			partes = processoTrf.getListaParteAtivo();
		}catch(LazyInitializationException e){
			partes = processoParteManager.recuperaPartes(processoTrf, true, null, null);
		}
		for (ProcessoParte parte : partes){
			String titulo = parte.getPoloTipoParteStr() != null ? parte.getPoloTipoParteStr().toUpperCase() + ": " : "";
			if (++aux == partes.size()) {
				subject.append(titulo);
				subject.append(parte.getNomeParte());
			} else {
				subject.append(titulo);
				subject.append(parte.getNomeParte());
				subject.append("; ");
			}
		}

		// lista de palavras-chaves são os assuntos do CNJ
		String keywords = "";
		List<AssuntoTrf> assuntos = asm.findAssuntosTrfPorProcessoTrf(processoTrf);
		for (AssuntoTrf assunto : assuntos){
			keywords += assunto.getAssuntoCompleto() + "\n";
		}

		document.addTitle(criarTitulo(processoTrf));
		document.addAuthor("PJe");
		document.addSubject(subject.toString());
		document.addKeywords(keywords);
	}
	
	/**
	 * Gera um PDF único contendo a lista de documentos passado por parâmetro.
	 * 
	 * @param processoTrf Processo dos documentos.
	 * @param processoDocumentos Lista dos documentos que serão mesclados em um único documento.
	 * @param outputStream Retorno do documento PDF gerado.
	 */
	public void gerarPdfUnificado(ProcessoTrf processoTrf, List<ProcessoDocumento> processoDocumentos, 
			OutputStream outputStream) throws PdfException {

		Document document = new Document();
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent("processoParteManager");
		try{			
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			
			configurarDocumentoPdf(processoTrf, processoParteManager, document);
			
			document.open();

			PdfContentByte cb = writer.getDirectContent();

			writeCabecalho(document, writer, cb, ParametroUtil.getParametro(Parametros.NOME_SECAO_JUDICIARIA));
			writeInfoProcesso(processoTrf, document);
			blankLine(document);
			if (isGerarInfoClasseAtual()) {
				writeInfoClasseAtual(processoTrf, processoParteManager, document);
			}
			
			if (isGerarIndiceDosDocumentos()) {
				writeDocumentos(document, writer, processoDocumentos, cb);
			}			
			writeArquivos(document, writer, processoDocumentos, cb);			
		} catch (Exception e){
			String message = "Falha na geração do PDF. Processo: " + processoTrf.getNumeroProcesso() + ".";

			log.error(message, e);

			throw new PdfException(message, e);
		} finally {
			document.close();
		}
	}
	
	/**
	 * Gera um PDF único contendo a lista de documentos passado por parâmetro.
	 * 
	 * @param processoTrf Processo dos documentos.
	 * @param processoDocumentos Lista dos documentos que serão mesclados em um único documento.
	 * @param outputStream Retorno do documento PDF gerado.
	 */
	public void gerarPdfUnificadoDetalhes(ProcessoTrf processoTrf, 
			List<ProcessoDocumento> processoDocumentos,
			Boolean comPaginaDetalhes,
			List<Element> elementosIncioDocumento,
			List<Element> elementosFinalDocumento,
			OutputStream outputStream) throws PdfException {

		Document document = new Document();
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent("processoParteManager");
		try{			
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			
			configurarDocumentoPdf(processoTrf, processoParteManager, document);
			
			document.open();

			PdfContentByte cb = writer.getDirectContent();

			if (comPaginaDetalhes) {
				//Inicio Pagina de Detalhes
				//Cabeçalho da página
				writeCabecalho(document, writer, cb, ParametroUtil.getParametro(Parametros.NOME_SECAO_JUDICIARIA));
				
					//Informação do número do processo
				writeInfoProcesso(processoTrf, document);
				blankLine(document);
				
					//Informação de classe judicial
				if (isGerarInfoClasseAtual()) {
					writeInfoClasseAtual(processoTrf, processoParteManager, document);
				}
						
				//Inicio documentos
				if (isGerarIndiceDosDocumentos()) {
					writeDocumentos(document, writer, processoDocumentos, cb);
				}
			}
			
			writeArquivosDetalhes(document, writer, processoDocumentos, cb, elementosIncioDocumento, elementosFinalDocumento);
			if(!processoDocumentos.isEmpty()){	
				LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);
				logAutos.logarDownloadPdfUnificado(processoDocumentos.get(0), processoDocumentos.get(processoDocumentos.size() - 1));
			}	
		} catch (Exception e){
			e.printStackTrace();
			throw new PdfException("Falha na geração do PDF. Processo: " + processoTrf.getNumeroProcesso() + ".", e);
		} finally {
			document.close();
		}
	}
	
	/** Método que grava em um único PDF os documentos informados no parâmetro
	 * @param document - Documento que está sendo gravado
	 * @param writer - gravador de pdf
	 * @param processoDocumentos - lista de documentos a serem inseridos no PDF
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param elementosIncioDocumento - Elementos para entrarem no inicio do documento.
	 * @param elementosFinalDocumento - Elementos para entrarem no final do documento.
	 * @throws DocumentException
	 */
	private void writeArquivosDetalhes(Document document, 
			PdfWriter writer, 
			List<ProcessoDocumento> processoDocumentos,
			PdfContentByte cb,
			List<Element> elementosIncioDocumento,
			List<Element> elementosFinalDocumento) throws DocumentException{
		int pageOfCurrentReaderPDF = 0;
		int currentReader = 0;
		boolean pdfNulo = false;

		PdfReader pdfReader = null;
		Image figura = null;

		Map<Integer, String> nomesAssinaturasPorDocumento = ValidacaoAssinaturaProcessoDocumento.instance()
				.getNomesAssinaturas(processoDocumentos);

		// Lê cada um dos documentos da lista
		for (ProcessoDocumento processoDocumento : processoDocumentos){
			currentReader++; // sequencial do documento na lista
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			figura = null;
			pdfNulo = false;

			String nomesAssinaturas = nomesAssinaturasPorDocumento
					.get(processoDocumentoBin.getIdProcessoDocumentoBin());

			InputStream conteudoPdf = null;

			try {
				if (!processoDocumentoBin.isBinario()){
					// Caso o documento não seja binário, obtém o texto...
					String html = processoDocumentoBin.getModeloDocumento();
					if (resourcePath != null){
						//TODO essa solucao deve ser retirada apos a refatoracao no uso de imagens do sistema de forma referencial
						// Ajusta o caminho dos recursos						
						html = AjustarCaminhoRecursosImagem(html);
					}
					
					html = PdfUtil.formatarCSSEditor(html);
					
					html = ItextHtmlConverterService.instance().converteImagensHtml(html);
					
					if(html.trim().isEmpty()) {
						html = HTML_VAZIO;
					}
					
					conteudoPdf = HtmlParaPdf.converteParaInputStream(html);
					pdfReader = leDocumentoPDF(writer, conteudoPdf);
				}
				else{
					// Caso seja binário, obtem o conteudo.
					// Se não for explicitamente imagem, tenta converter para PDF, que é o tipo mais comum. Verificação devido à documentos importados via MNI que não levam a extensão.
					if( processoDocumentoBin.getExtensao() == null || !processoDocumentoBin.getExtensao().startsWith("image/")) {
						try {
							conteudoPdf = DocumentoBinManager.instance().getInputStream(processoDocumentoBin.getNumeroDocumentoStorage());
							pdfReader = leDocumentoPDF(writer, conteudoPdf);
						} catch(Exception e) {
							// Não conseguiu converter para PDF? Tenta imagem.
							figura = carregaImagem(processoDocumentoBin);
						}
					} else if (processoDocumentoBin.getExtensao().startsWith("image/")) {
						figura = carregaImagem(processoDocumentoBin);
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				pdfNulo = true;
			} finally {
				if (conteudoPdf != null) {
					try {
						conteudoPdf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			// Caso o conteúdo do pdf seja nulo (não conseguiu importar o documento para o pdf) insere uma página com essa informação
			if (pdfNulo){
				pageOfCurrentReaderPDF++;
				inserePaginaDocumentoNaoInserido(document, processoDocumento, currentReader, pageOfCurrentReaderPDF, cb, writer);
			} else {
				// Caso o documento tenha sido obtido (pdfNulo = false), verifica se é uma imagem (atualmente tratado apenas jpeg) ou um PDF
				if( figura != null ) {
					// Se for imagem
					inserePaginaDocumentoImagemDetalhes(document, cb, pageOfCurrentReaderPDF, figura, processoDocumento, processoDocumentoBin, writer, nomesAssinaturas,
							elementosIncioDocumento, elementosFinalDocumento);
				} else {
					// Se for PDF
					insereDocumentoPDFDetalhes(document, pageOfCurrentReaderPDF, cb, pdfReader, writer, processoDocumento, processoDocumentoBin, nomesAssinaturas,
							elementosIncioDocumento, elementosFinalDocumento);
				}
			}
			
			pageOfCurrentReaderPDF = 0;
		}
	}
	
	/** Método que insere uma página no PDF com um documento do tipo imagem (figura
	 * @param document - Documento que está sendo gravado 
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param pageOfCurrentReaderPDF - número da página atual
	 * @param figura - imagem que será inserida
	 * @param processoDocumento - processoDocumento que está sendo inserido
	 * @param processoDocumentoBin - processoDocumento que contem as assinaturas do documento que está sendo inserido
	 * @param writer - Objeto no qual é escrito o conteúdo do documento PDF
	 * @param nomesAssinaturas - lista dos nomes para a parte da assinatura
	 * @param elementosIncioDocumento - Elementos para entrarem no inicio do documento.
	 * @param elementosFinalDocumento - Elementos para entrarem no final do documento.
	 * @return void
	 * @throws DocumentException
	 */
	private void inserePaginaDocumentoImagemDetalhes(Document document, PdfContentByte cb,
			int pageOfCurrentReaderPDF, Image figura, ProcessoDocumento processoDocumento,
			ProcessoDocumentoBin processoDocumentoBin, PdfWriter writer, String nomesAssinaturas,
			List<Element> elementosIncioDocumento,
			List<Element> elementosFinalDocumento) throws DocumentException {
		// Insere nova página e define seu tamanho
		document.newPage();
		document.setPageSize(PageSize.A4);
		pageOfCurrentReaderPDF++;
		
		// Cria marcador para que o documento seja acessado pelo indice e pelo outline do Pdf 
		criaMarcadorEAncora(document, processoDocumento, cb.getRootOutline(), writer);
		
		// Redimensiona a imagem para que ela possa caber uma página A4					
		float escalaHorizontal = ((PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin() )  /  (figura.getWidth()));
		float escalaVertical = ((PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin() - margemParaAssinatura) /  (figura.getHeight()));
		float escala = escalaHorizontal > escalaVertical ? escalaVertical : escalaHorizontal;
		if( escala > 1 ) escala = 1;
		escala = escala *100;
		figura.scalePercent(escala);
		
		//Colocando elementos no inicio da pagina de detalhes
		if (elementosIncioDocumento != null) {
			for (Element e : elementosIncioDocumento) {
				document.add(e);
			}
		}
		
		// adiciona a imagem no documento
		document.add(figura);
		
		//Colocando elementos no final da pagina de detalhes
		if (elementosFinalDocumento != null) {
			for (Element e : elementosFinalDocumento) {
				document.add(e);
			}
		}
		
		// Insere informações da pagina que está sendo inserida
		insereInformacaoPaginaDocumento(pageOfCurrentReaderPDF, processoDocumento, cb);
		
		// Insere informação da assinatura do documento
		insereAssinatura(cb, processoDocumentoBin, nomesAssinaturas);
		return;
	}
	
	/** Método que insere uma documento PDF no PDF que está sendo gerado, página a página
	 * @param document - Documento que está sendo gravado
	 * @param pageOfCurrentReaderPDF - número da página atual
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param pdfReader - PDF que contem o documento que está sendo lido
	 * @param writer - Gravador de PDF
	 * @param processoDocumento - processoDocumento que está sendo inserido
	 * @param processoDocumentoBin - processoDocumento que contem as assinaturas do documento que está sendo inserido
	 * @param nomesAssinaturas - lista dos nomes para a parte da assinatura
	 * @param elementosIncioDocumento - Elementos para entrarem no inicio do documento.
	 * @param elementosFinalDocumento - Elementos para entrarem no final do documento.
	 * @return void
	 * @throws DocumentException
	 */
	private void insereDocumentoPDFDetalhes(Document document, int pageOfCurrentReaderPDF,
			PdfContentByte cb, PdfReader pdfReader, PdfWriter writer,
			ProcessoDocumento processoDocumento, ProcessoDocumentoBin processoDocumentoBin, String nomesAssinaturas,
			List<Element> elementosIncioDocumento,
			List<Element> elementosFinalDocumento) throws DocumentException {
		PdfImportedPage page;
		
		//Pega as informações das paginas
		int firstPage = pageOfCurrentReaderPDF + 1;
        int lastPage = pdfReader.getNumberOfPages() - 1;
        
		// Lê cada pagina do PDF lido e insere no novo PDF.
		while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()){
			pageOfCurrentReaderPDF++;
			page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
			
			// redimensiona a página para o tamanho A4, para padronização
			document.setPageSize(PageSize.A4);
			Rectangle r = pdfReader.getPageSize(pageOfCurrentReaderPDF); 
			float escalaHorizontal = ((PageSize.A4.getWidth() - document.rightMargin() - document.leftMargin()) /  (r.getWidth()));
			float escalaVertical = ((PageSize.A4.getHeight() - document.bottomMargin() - document.topMargin() -  margemParaAssinatura) /  (r.getHeight()));
			float escala = escalaHorizontal > escalaVertical ? escalaVertical : escalaHorizontal;
			if( escala > 1 ) escala = 1;
			
			// Insere a página
			document.newPage();

			//Detalhes no inicio
            if ((pageOfCurrentReaderPDF == firstPage) && (elementosIncioDocumento != null)) {
            	for (Element e : elementosIncioDocumento) {
        			document.add(e);
        		}
            }
			
			// Rotaciona a página caso tenha informação no pdf de origem
			int rotation = pdfReader.getPageRotation(pageOfCurrentReaderPDF);
			if( rotation == 180) {
				float angulo  = (float) (rotation * (Math.PI / 180));
				cb.addTemplate(page, (float) Math.cos(angulo) * escala, (float) Math.sin(angulo), 
						(float) Math.sin(angulo), (float) Math.cos(angulo)*escala, PageSize.A4.getWidth(),PageSize.A4.getHeight());
			} else {
				cb.addTemplate(page, escala, 0, 0, escala, 20,50);
			}
			
			//Detalhes no final
            if ((pageOfCurrentReaderPDF == lastPage) && (elementosFinalDocumento != null)) {
            	for (Element e : elementosFinalDocumento) {
        			document.add(e);
        		}
            }
			
			// Cria marcador para que o documento seja acessado pelo indice e pelo outline do Pdf
			if (pageOfCurrentReaderPDF == 1){
				criaMarcadorEAncora(document, processoDocumento, cb.getRootOutline(), writer);
			}
			// Insere Informações da pagina que está sendo inserida
			insereInformacaoPaginaDocumento(pageOfCurrentReaderPDF, processoDocumento, cb);
			
			// Insere informação da assinatura do documento
			insereAssinatura(cb, processoDocumentoBin, nomesAssinaturas);
		}
		return;
	}
	
	public void gerarPdfSimples(List<ProcessoDocumento> documentos, OutputStream outputStream) throws PdfException {

		Document document = new Document();
		try{			
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			writeArquivos(document, writer, documentos, cb);			
		} catch (Exception e){
			e.printStackTrace();
			throw new PdfException("Falha na geração do PDF", e);
		} finally {
			document.close();
		}
	}
	
	 public void gerarPdfCabecalho(String conteudo, OutputStream outputStream, ProcessoTrf processoTrf) throws PdfException {
        Document document = new Document();
        ProcessoParteManager processoParteManager = ComponentUtil.getComponent("processoParteManager");
        AssuntoTrfManager asm = ComponentUtil.getComponent("assuntoTrfManager");
        try{            
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            int aux = 0;
            StringBuilder subject = new StringBuilder();
            List<ProcessoParte> partes = new ArrayList<ProcessoParte>();
            try{
                partes = processoTrf.getListaParteAtivo();
            }catch(LazyInitializationException e){
                partes = processoParteManager.recuperaPartes(processoTrf, true, null, null);
            }
            for (ProcessoParte parte : partes){
                String titulo = parte.getPoloTipoParteStr() != null ? parte.getPoloTipoParteStr().toUpperCase() + ": " : "";
                if (++aux == partes.size()) {
                    subject.append(titulo);
                    subject.append(parte.getNomeParte());
                } else {
                    subject.append(titulo);
                    subject.append(parte.getNomeParte());
                    subject.append("; ");
                }
            }

            // lista de palavras-chaves s<E3>o os assuntos do CNJ
            String keywords = "";
            List<AssuntoTrf> assuntos = asm.findAssuntosTrfPorProcessoTrf(processoTrf);
            for (AssuntoTrf assunto : assuntos){
                keywords += assunto.getAssuntoCompleto() + "\n";
            }

            document.addTitle(criarTitulo(processoTrf));
            document.addAuthor("PJe");
            document.addSubject(subject.toString());
            document.addKeywords(keywords);
            
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            writeCabecalho(document, writer, cb, ParametroUtil.getParametro(Parametros.NOME_SECAO_JUDICIARIA), false);
            writeInfoProcesso(processoTrf, document);
            blankLine(document);
            if (isGerarInfoClasseAtual()) {
                writeInfoClasseAtual(processoTrf, processoParteManager, document);
            }
            writeArquivos(document, writer, conteudo, cb);          
        } catch (Exception e){
            e.printStackTrace();
            throw new PdfException("Falha na gera<E7><E3>o do PDF", e);
        } finally {
            document.close();
        }
    }

   private void writeCabecalho(Document document, PdfWriter writer, PdfContentByte cb, String justica, boolean comMarcador)
            throws MalformedURLException, IOException, DocumentException{
        // Criar um marcador (bookmark) para o CABECALHO
        if( comMarcador ) {
            criarMarcador(cb.getRootOutline(), writer, writer.getCurrentPageNumber(), "Cabe<E7>alho");
        }

        // CABECALHO
        float[] larguras = new float[]{12f, 88f};
        PdfPTable tabTitulo = new PdfPTable(larguras);
        tabTitulo.setWidthPercentage(100);

        Image brasao = Image.getInstance(FileUtil.getContent(getClass().getResourceAsStream(
                "/META-INF/images/brasaoMiniPDF.png")));

        brasao.scalePercent(60f);
        brasao.setAlignment(Image.LEFT | Image.TEXTWRAP);
        brasao.setIndentationRight(5f);
        brasao.setSpacingAfter(140);
        PdfPCell ce = new PdfPCell(brasao);
        ce.setBorderColor(BaseColor.WHITE);
        ce.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tabTitulo.addCell(ce);

        Paragraph tit = new Paragraph(justica, fontCabec);
        tit.add(new Phrase("\nPJe - Processo Judicial Eletr<F4>nico", fontCabec));
        PdfPCell cd = new PdfPCell(tit);
        cd.setBorderColor(BaseColor.WHITE);
        tabTitulo.addCell(cd);

        // Local e data
        Paragraph localData = new Paragraph(dataFormater.format(new Date()) + "\n", fontCorpo);
        localData.setAlignment(Paragraph.ALIGN_RIGHT);

        // Adiciona ao documento
        document.add(tabTitulo);
        verticalLine(document, writer);
        document.add(localData);

    }

	private String criarTitulo(ProcessoTrf processoTrf) {
		ClasseJudicial classe = processoTrf.getClasseJudicial();
		String descClasse = classe.getClasseJudicial() != null ? " - " + classe.getClasseJudicial().toUpperCase() : StringUtils.EMPTY;
		return "PROCESSO: " + processoTrf.getNumeroProcesso() + descClasse;
	}

	private void writeCabecalho(Document document, PdfWriter writer, PdfContentByte cb, String justica)
			throws MalformedURLException, IOException, DocumentException{
		// Criar um marcador (bookmark) para o CABECALHO
		criarMarcador(cb.getRootOutline(), writer, writer.getCurrentPageNumber(), "Cabeçalho");

		// CABECALHO
		float[] larguras = new float[]{12f, 88f};
		PdfPTable tabTitulo = new PdfPTable(larguras);
		tabTitulo.setWidthPercentage(100);

		Image brasao = Image.getInstance(FileUtil.getContent(getClass().getResourceAsStream(
				"/META-INF/images/brasaoMiniPDF.png")));

		brasao.scalePercent(60f);
		brasao.setAlignment(Image.LEFT | Image.TEXTWRAP);
		brasao.setIndentationRight(5f);
		brasao.setSpacingAfter(140);
		PdfPCell ce = new PdfPCell(brasao);
		ce.setBorderColor(BaseColor.WHITE);
		ce.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		tabTitulo.addCell(ce);

		Paragraph tit = new Paragraph(justica, fontCabec);
		tit.add(new Phrase("\nPJe - Processo Judicial Eletrônico", fontCabec));
		PdfPCell cd = new PdfPCell(tit);
		cd.setBorderColor(BaseColor.WHITE);
		tabTitulo.addCell(cd);

		// Local e data
		Paragraph localData = new Paragraph(dataFormater.format(new Date()) + "\n", fontCorpo);
		localData.setAlignment(Paragraph.ALIGN_RIGHT);

		// Adiciona ao documento
		document.add(tabTitulo);
		verticalLine(document, writer);
		document.add(localData);

	}

	private void writeInfoProcesso(ProcessoTrf proc, Document document) throws DocumentException{
		boolean naoAutuado = (proc.getNumeroProcesso() == null || proc.getNumeroProcesso().isEmpty());
		// Informacoes do processo
		Paragraph numProcesso = null;
		Paragraph infoProcessoLeft = null;
		if(naoAutuado){
			numProcesso = new Paragraph("Processo não autuado", fontProc);
			infoProcessoLeft = new Paragraph();
		}else{
			numProcesso = new Paragraph("Número: ", fontProc);
			numProcesso.add(new Phrase(proc.getNumeroProcesso(), fontProcBold));
			infoProcessoLeft = new Paragraph("Data Autuação: ", fontCorpo);
			infoProcessoLeft.add(new Phrase(dataFormater.format(proc.getDataAutuacao()), fontCorpoBold));
			infoProcessoLeft.setLeading(14f);
		}
		numProcesso.setSpacingAfter(2);

		float[] larguras = new float[]{60f, 40f};
		PdfPTable tabInfoProc = new PdfPTable(larguras);
		tabInfoProc.setWidthPercentage(100);
		tabInfoProc.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
		PdfPCell celInfoProcLeft = new PdfPCell(infoProcessoLeft);
		celInfoProcLeft.setBorderColor(BaseColor.WHITE);
		tabInfoProc.addCell(celInfoProcLeft);

		// Adiciona ao documento
		document.add(numProcesso);
		document.add(tabInfoProc);
	}

	private void writeInfoClasseAtual(ProcessoTrf proc, ProcessoParteManager processoParteManager, Document document) throws DocumentException, PJeBusinessException{
		document.add(getInfoClasse(proc, fontCabec, fontCabecBold, fontDocumento, fontDocumentoBold, true));
		blankLine(document);
		List<ProcessoParte> partesPrincipal = new ArrayList<ProcessoParte>(0);
		partesPrincipal.addAll(proc.getListaPartePrincipalAtivo());
		partesPrincipal.addAll(proc.getListaPartePrincipalPassivo());
		partesPrincipal.addAll(proc.getListaPartePrincipalTerceiro());

		document.add(getTablePartes(partesPrincipal));
	}

	private PdfPTable getTablePartes(List<ProcessoParte> partes){
		float[] larguras = new float[]{60f, 60f};
		PdfPTable tabPartes = new PdfPTable(larguras);
		tabPartes.setWidthPercentage(100);
		tabPartes.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
		PdfPCell celPartes = null;

		celPartes = new PdfPCell(new Phrase("Partes", fontDocumentoBold));
		celPartes.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		celPartes.setBackgroundColor(corCabecTabela);
		celPartes.setPaddingBottom(4f);
		tabPartes.addCell(celPartes);
		
		celPartes = new PdfPCell(new Phrase("Procurador/Terceiro vinculado", fontDocumentoBold));
		celPartes.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		celPartes.setBackgroundColor(corCabecTabela);
		celPartes.setPaddingBottom(4f);
		tabPartes.addCell(celPartes);

		for (ProcessoParte a : partes){			
			celPartes = new PdfPCell();
			celPartes.setPaddingTop(0);
			celPartes.setPaddingBottom(2f);
			celPartes.addElement(new Phrase(obterNomeParte(a)));
			tabPartes.addCell(celPartes);
			
			celPartes = new PdfPCell();
			celPartes.setPaddingTop(0);
			celPartes.setPaddingBottom(2f);
			celPartes.addElement(new Phrase(obterAdvogadosParte(a), fontDocumentoBold));
			tabPartes.addCell(celPartes);
		}
		return tabPartes;
	}
	
	private Chunk obterNomeParte(ProcessoParte processoParte) {
		Chunk chunk = new Chunk(String.format("%s (%s)", processoParte.getNomeParte(), processoParte.getPoloTipoParteStr()));		
		chunk.setFont(fontDocumentoBold);
		if (processoParte.getIsBaixado() || processoParte.getIsSuspenso()) {
			chunk.setUnderline(1f, 3f);
		}
		return chunk;
	}
	
	private String obterAdvogadosParte(ProcessoParte processoParte) {
		StringBuilder resultado = new StringBuilder();
		if (ProjetoUtil.isNotVazio(processoParte.getProcessoParteRepresentanteList())) {
			for (ProcessoParteRepresentante representante : processoParte.getProcessoParteRepresentanteList()) {
				if (representante.getInSituacao() == ProcessoParteSituacaoEnum.A) {
					resultado.append(String.format("%s (%s) \n", 
						representante.getParteRepresentante().getNomeParte(), representante.getParteRepresentante().getPoloTipoParteStr()));
				}
			}			
		}
		return resultado.toString();
	}

	private static Paragraph getInfoClasse(ProcessoTrf processoTrf, Font fontClasse, Font fontDescClasse,
			Font fontRelator, Font fontDescRelator, boolean atual){
		Paragraph classeAtual = new Paragraph();

		// Classe atual
		classeAtual.add(new Phrase("Classe: ", fontClasse));
		classeAtual.add(new Phrase(processoTrf.getClasseJudicialStr(), fontDescClasse));
		
		// Órgão julgador colegiado
		if(processoTrf.getOrgaoJulgadorColegiado() != null){
			classeAtual.add(new Phrase("\n Órgão julgador colegiado: ", fontClasse));
			classeAtual.add(new Phrase(processoTrf.getOrgaoJulgadorColegiado().toString(), fontDescClasse));
		}
		
		// Órgão julgador
		if(processoTrf.getOrgaoJulgador() != null){
			classeAtual.add(new Phrase("\n Órgão julgador: ", fontClasse));
			classeAtual.add(new Phrase(processoTrf.getOrgaoJulgador().toString(), fontDescClasse));
		}
		
		// Data de ajuizamento
		if (!atual){
			classeAtual.add(new Phrase("\n Data Autuação: ", fontClasse));

			Format formatter = new SimpleDateFormat("dd/MM/yyyy");
			String dtAjuizamento = formatter.format(processoTrf.getDataAutuacao());

			classeAtual.add(new Phrase(dtAjuizamento, fontDescClasse));
		}
		
		// Data distribuição
		if(processoTrf.getDataDistribuicao() != null){
			classeAtual.add(new Phrase("\n Última distribuição : ", fontClasse));

			Format formatter = new SimpleDateFormat("dd/MM/yyyy");
			String dtUltimaDistribuicao = formatter.format(processoTrf.getDataDistribuicao());

			classeAtual.add(new Phrase(dtUltimaDistribuicao, fontDescClasse));
		}
		
		// Valor da causa
		if(processoTrf.getValorCausa() != null){
			classeAtual.add(new Phrase("\n Valor da causa: ", fontClasse));
			classeAtual.add(new Phrase(StringUtil.formatarValorMoeda(processoTrf.getValorCausa(), true), fontDescClasse));
		}
		
		// Relator
		if (!ParametroUtil.instance().isPrimeiroGrau() && processoTrf.getPessoaRelator() != null && StringUtil.isSet(processoTrf.getPessoaRelator().getNome())){
			classeAtual.add(new Phrase("\n Relator: ", fontDescClasse));
			classeAtual.add(new Phrase(processoTrf.getPessoaRelator().getNome(), fontDescClasse));
		}
		
		// Processo Referência
		if(processoTrf.getDesProcReferencia() != null){
			classeAtual.add(new Phrase("\n Processo referência: ", fontClasse));
			classeAtual.add(new Phrase(processoTrf.getDesProcReferencia(), fontDescClasse));
		}
		
		//Assuntos
		if(processoTrf.getAssuntoTrfListStr() != null){
			classeAtual.add(new Phrase("\n Assuntos: ", fontClasse));
			int contador = 1;
			for(String assunto: processoTrf.getAssuntoTrfListStr()){
				classeAtual.add(new Phrase(assunto, fontDescClasse));
				if(contador < processoTrf.getAssuntoTrfListStr().size()){
					classeAtual.add(new Phrase(", ", fontDescClasse));
				}
				contador++;
			}
		}
		
		//Objeto do Processo
		if (processoTrf.getObjeto() != null && Identity.instance().hasRole(Papeis.PROCESSO_OBJETO_VISUALIZADOR)){
			classeAtual.add(new Phrase("\n Objeto do processo: ", fontClasse));
			classeAtual.add(new Phrase(processoTrf.getObjeto(), fontDescClasse));
		}
		
		//Segredo de justiça
		classeAtual.add(new Phrase("\n Segredo de justiça? ", fontClasse));
		if(processoTrf.getSegredoJustica() != null && processoTrf.getSegredoJustica()){
			classeAtual.add(new Phrase("SIM", fontDescClasse));
		}else{
			classeAtual.add(new Phrase("NÃO", fontDescClasse));
		}
		
		//Justiça gratuita
		classeAtual.add(new Phrase("\n Justiça gratuita? ", fontClasse));
		if(processoTrf.getJusticaGratuita() != null && processoTrf.getJusticaGratuita()){
			classeAtual.add(new Phrase("SIM", fontDescClasse));
		}else{
			classeAtual.add(new Phrase("NÃO", fontDescClasse));
		}
		
		//Pedido de liminar ou antecipação de tutela
		classeAtual.add(new Phrase("\n Pedido de liminar ou antecipação de tutela? ", fontClasse));
		if(processoTrf.getTutelaLiminar() != null && processoTrf.getTutelaLiminar()){	
			classeAtual.add(new Phrase("SIM", fontDescClasse));
		}else{
			classeAtual.add(new Phrase("NÃO", fontDescClasse));
		}
		
		return classeAtual;
	}

	private void writeDocumentos(Document document, PdfWriter writer, List<ProcessoDocumento> processoDocumentos,
			PdfContentByte cb) throws DocumentException{
		float[] larguras = new float[]{6f, 15f, 49f, 35f};
		PdfPTable tabDocumentos = new PdfPTable(larguras);
		tabDocumentos.setSpacingBefore(2f);
		tabDocumentos.setWidthPercentage(100);
		tabDocumentos.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
		PdfPCell celDocumento = null;

		celDocumento = createHeaderCell("Documentos", fontDocumentoBold, PdfPCell.ALIGN_CENTER);
		celDocumento.setColspan(larguras.length);
		tabDocumentos.addCell(celDocumento);

		celDocumento = createHeaderCell("Id.", fontDocumentoBold, PdfPCell.ALIGN_CENTER);
		tabDocumentos.addCell(celDocumento);

		// MODIFICADO PARA ISSUE PJEII-3621 em 13/11/2012 por Fernando Barreira
		celDocumento = createHeaderCell("Data da Assinatura", fontDocumentoBold, PdfPCell.ALIGN_CENTER);
		tabDocumentos.addCell(celDocumento);

		celDocumento = createHeaderCell("Documento", fontDocumentoBold, PdfPCell.ALIGN_LEFT);
		tabDocumentos.addCell(celDocumento);

		celDocumento = createHeaderCell("Tipo", fontDocumentoBold, PdfPCell.ALIGN_CENTER);
		tabDocumentos.addCell(celDocumento);

		// Ordenado por data
		boolean linhaPar = false;

		// criar um marcador (bookmark) para o índice
		criarMarcador(cb.getRootOutline(), writer, writer.getCurrentPageNumber(), "Índice");

		for (ProcessoDocumento documento : processoDocumentos){
			// Sequencial da Tramitacao
			String sequencial = String.valueOf(documento.getIdProcessoDocumento());
			celDocumento = createLinkDataCell(String.valueOf(sequencial), fontDocumento, PdfPCell.ALIGN_CENTER, sequencial);

			if (linhaPar){
				celDocumento.setBackgroundColor(corLinhaPar);
			}
			else{
				celDocumento.setBackgroundColor(corLinhaImpar);
			}
			tabDocumentos.addCell(celDocumento);
			/**
			 *  [PJEII-4112] Antonio Lucas: Se a Data da Assinatura for nula 
			 *  (quando o documento não foi ainda assinado por exemplo)
			 *  dá erro ao gerar o pdf.
			 */
			if (documento.getDataJuntada() != null)
				// Data da Assinatura (MODIFICADO PARA ISSUE PJEII-3621 em 13/11/2012 por Fernando Barreira)
				celDocumento = createLinkDataCell(dataHoraFormater.format(documento.getDataJuntada()), 
						fontDocumento, PdfPCell.ALIGN_LEFT, sequencial);
			if (linhaPar){
				celDocumento.setBackgroundColor(corLinhaPar);
			}
			else{
				celDocumento.setBackgroundColor(corLinhaImpar);
			}
			tabDocumentos.addCell(celDocumento);

			// Descricao da Tramitação
			celDocumento = createLinkDataCell(documento.getProcessoDocumento(), fontSublinhado, PdfPCell.ALIGN_LEFT,
					sequencial);

			if (linhaPar){
				celDocumento.setBackgroundColor(corLinhaPar);
			}
			else{
				celDocumento.setBackgroundColor(corLinhaImpar);
			}
			tabDocumentos.addCell(celDocumento);

			// Tipo
			celDocumento = createLinkDataCell(documento.getTipoProcessoDocumento().getTipoProcessoDocumento(),
					fontDocumento, PdfPCell.ALIGN_CENTER, sequencial);

			if (linhaPar){
				celDocumento.setBackgroundColor(corLinhaPar);
			}
			else{
				celDocumento.setBackgroundColor(corLinhaImpar);
			}
			tabDocumentos.addCell(celDocumento);

			linhaPar = !linhaPar;

		} // for

		document.add(tabDocumentos);
	}
	
    /** M<E9>todo que grava em um <FA>nico PDF os documentos informados no par<E2>metro
     * @param document - Documento que est<E1> sendo gravado
     * @param writer - gravador de pdf
     * @param conteudo - conteudo do documento a ser gerado
     * @param cb - PdfContentByte - Conte<FA>do do PDF
     * @throws DocumentException
     */
    private void writeArquivos(Document document, PdfWriter writer, String conteudo, PdfContentByte cb) throws DocumentException{
        int pageOfCurrentReaderPDF = 0;
        boolean pdfNulo = false;

        PdfReader pdfReader = null;

        InputStream conteudoPdf = null;

        try {
            if (resourcePath != null){
                conteudo = conteudo.replaceAll("src=\"(.*?)/img", "src=\"" + resourcePath + "/img");
            }
            conteudo = PdfUtil.formatarCSSEditor(conteudo);
            conteudo = ItextHtmlConverterService.instance().converteImagensHtml(conteudo);
            if(conteudo.trim().isEmpty()) {
                conteudo = HTML_VAZIO;
            }
                    
            conteudoPdf = HtmlParaPdf.converteParaInputStream(conteudo);
            pdfReader = leDocumentoPDF(writer, conteudoPdf);
        } catch (Exception e){
            e.printStackTrace();
            pdfNulo = true;
        } finally {
			if (conteudoPdf != null) {
				try {
					conteudoPdf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
            
        // Caso o conte<FA>do do pdf seja nulo (n<E3>o conseguiu importar o documento para o pdf) insere uma p<E1>gina com essa informa<E7><E3>o
        if (pdfNulo){
            //inserePaginaDocumentoNaoInserido(document, processoDocumento, currentReader, pageOfCurrentReaderPDF, cb, writer);
        } else {
            insereDocumentoPDF(document, pageOfCurrentReaderPDF, cb, pdfReader, writer);
        }
    }

			
	/** Método que grava em um único PDF os documentos informados no parâmetro
	 * @param document - Documento que está sendo gravado
	 * @param writer - gravador de pdf
	 * @param processoDocumentos - lista de documentos a serem inseridos no PDF
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @throws DocumentException
	 */
	private void writeArquivos(Document document, PdfWriter writer, List<ProcessoDocumento> processoDocumentos,
			PdfContentByte cb) throws DocumentException{
		int pageOfCurrentReaderPDF = 0;
		int currentReader = 0;
		boolean pdfNulo = false;

		PdfReader pdfReader = null;
		Image figura = null;

		Map<Integer, String> nomesAssinaturasPorDocumento = ValidacaoAssinaturaProcessoDocumento.instance()
				.getNomesAssinaturas(processoDocumentos);

		// Lê cada um dos documentos da lista
		for (ProcessoDocumento processoDocumento : processoDocumentos){
			currentReader++; // sequencial do documento na lista
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			figura = null;
			pdfNulo = false;

			String nomesAssinaturas = nomesAssinaturasPorDocumento
					.get(processoDocumentoBin.getIdProcessoDocumentoBin());

			InputStream conteudoPdf = null;

			try {
				if (!processoDocumentoBin.isBinario()){
					// Caso o documento não seja binário, obtém o texto...
					String html = processoDocumentoBin.getModeloDocumento();
					if (resourcePath != null){
						//TODO essa solucao deve ser retirada apos a refatoracao no uso de imagens do sistema de forma referencial
						// Ajusta o caminho dos recursos						
						html = AjustarCaminhoRecursosImagem(html);
					}
					
					html = PdfUtil.formatarCSSEditor(html);
					
					html = ItextHtmlConverterService.instance().converteImagensHtml(html);
					
					if(html.trim().isEmpty()) {
						html = HTML_VAZIO;
					}
					
					conteudoPdf = HtmlParaPdf.converteParaInputStream(html);
					pdfReader = leDocumentoPDF(writer, conteudoPdf);
				}
				else{
					// Caso seja binário, obtem o conteudo.
					// Se não for explicitamente imagem, tenta converter para PDF, que é o tipo mais comum. Verificação devido à documentos importados via MNI que não levam a extensão.
					if( processoDocumentoBin.getExtensao() == null || !processoDocumentoBin.getExtensao().startsWith("image/")) {
						try {
							conteudoPdf = DocumentoBinManager.instance().getInputStream(processoDocumentoBin.getNumeroDocumentoStorage());
							pdfReader = leDocumentoPDF(writer, conteudoPdf);
						} catch(Exception e) {
							// Não conseguiu converter para PDF? Tenta imagem.
							figura = carregaImagem(processoDocumentoBin);
						}
					} else if (processoDocumentoBin.getExtensao().startsWith("image/")) {
						figura = carregaImagem(processoDocumentoBin);
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				pdfNulo = true;
			} finally {
				if (conteudoPdf != null) {
					try {
						conteudoPdf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			// Caso o conteúdo do pdf seja nulo (não conseguiu importar o documento para o pdf) insere uma página com essa informação
			if (pdfNulo){
				pageOfCurrentReaderPDF++;
				inserePaginaDocumentoNaoInserido(document, processoDocumento, currentReader, pageOfCurrentReaderPDF, cb, writer);
			} else {
				// Caso o documento tenha sido obtido (pdfNulo = false), verifica se é uma imagem (atualmente tratado apenas jpeg) ou um PDF
				if( figura != null ) {
					// Se for imagem
					inserePaginaDocumentoImagem(document, cb, pageOfCurrentReaderPDF, figura, processoDocumento, processoDocumentoBin, writer, nomesAssinaturas);
				} else {
					// Se for PDF
					insereDocumentoPDF(document, pageOfCurrentReaderPDF, cb, pdfReader, writer, processoDocumento, processoDocumentoBin, nomesAssinaturas);
				}
			}
			
			pageOfCurrentReaderPDF = 0;
		}
	}
	
	/** Método que lê o binário de um processoDocumentoBin e tenta converter para imagem.
	 * 
	 * @param processoDocumentoBin ProcessoDocumentoBin que tem o binário a ser convertido para imagem
	 * @return
	 * @throws PJeBusinessException
	 * @throws BadElementException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private Image carregaImagem(ProcessoDocumentoBin processoDocumentoBin)
			throws PJeBusinessException, BadElementException,MalformedURLException, IOException {
		Image figura;
		byte[] conteudoImage;
		conteudoImage = DocumentoBinManager.instance().getData(processoDocumentoBin.getNumeroDocumentoStorage());
		figura = Image.getInstance( conteudoImage );
		return figura;
	}
	
	/** Método que converte o inputStream lido e converte em um PDFReader
	 * @param writer - Gravador de PDF
	 * @param conteudoPdf - InputStream com o documento que será convertido
	 * @return
	 * @throws IOException
	 * @throws COSVisitorException
	 */
	private PdfReader leDocumentoPDF(PdfWriter writer, InputStream conteudoPdf)
			throws IOException, COSVisitorException {
		PdfReader pdfReader;
		try {
			pdfReader = new PdfReader(conteudoPdf);
		} catch (Exception e){ 
			//Se deu erro ao criar um PdfReader, tentar consertar pdf usando o PDFBox
			conteudoPdf = this.tentarObterDocumentoPdfComPDFBox(conteudoPdf);
			pdfReader = new PdfReader(conteudoPdf);
		}
		if (!pdfReader.isOpenedWithFullPermissions()) {
			// pdf protegido contra modificação/ apenas impressão
			new Mirror().on(pdfReader).set().field("ownerPasswordUsed").withValue(true);
		}
		//tentar acessar primeira página
		writer.getImportedPage(pdfReader, 1);
		return pdfReader;
	}

    private void insereDocumentoPDF(Document document, int pageOfCurrentReaderPDF, PdfContentByte cb, PdfReader pdfReader, PdfWriter writer) throws DocumentException {
        PdfImportedPage page;
        // L<EA> cada pagina do PDF lido e insere no novo PDF.
        while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()){
            pageOfCurrentReaderPDF++;
            page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
            
            // redimensiona a p<E1>gina para o tamanho A4, para padroniza<E7><E3>o
            document.setPageSize(PageSize.A4);
            Rectangle r = pdfReader.getPageSize(pageOfCurrentReaderPDF); 
            float escalaHorizontal = ((PageSize.A4.getWidth() - document.rightMargin() - document.leftMargin()) /  (r.getWidth()));
            float escalaVertical = ((PageSize.A4.getHeight() - document.bottomMargin() - document.topMargin() -  margemParaAssinatura) /  (r.getHeight()));
            float escala = escalaHorizontal > escalaVertical ? escalaVertical : escalaHorizontal;
            if( escala > 1 ) escala = 1;
            
            // Insere a p<E1>gina
            document.newPage();
            
            // Rotaciona a p<E1>gina caso tenha informa<E7><E3>o no pdf de origem
            int rotation = pdfReader.getPageRotation(pageOfCurrentReaderPDF);
            if( rotation == 180) {
                float angulo  = (float) (rotation * (Math.PI / 180));
                cb.addTemplate(page, (float) Math.cos(angulo) * escala, (float) Math.sin(angulo), 
                        (float) Math.sin(angulo), (float) Math.cos(angulo)*escala, PageSize.A4.getWidth(),PageSize.A4.getHeight());
            } else {
                cb.addTemplate(page, escala, 0, 0, escala, 20,50);
            }
        }
    }
	
	/** Método que insere uma documento PDF no PDF que está sendo gerado, página a página
	 * @param document - Documento que está sendo gravado
	 * @param pageOfCurrentReaderPDF - número da página atual
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param pdfReader - PDF que contem o documento que está sendo lido
	 * @param writer - Gravador de PDF
	 * @param processoDocumento - processoDocumento que está sendo inserido
	 * @param processoDocumentoBin - processoDocumento que contem as assinaturas do documento que está sendo inserido
	 * @return void
	 * @throws DocumentException
	 */
	private void insereDocumentoPDF(Document document, int pageOfCurrentReaderPDF,
			PdfContentByte cb, PdfReader pdfReader, PdfWriter writer,
			ProcessoDocumento processoDocumento, ProcessoDocumentoBin processoDocumentoBin, String nomesAssinaturas) throws DocumentException {
		PdfImportedPage page;
		// Lê cada pagina do PDF lido e insere no novo PDF.
		while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()){
			pageOfCurrentReaderPDF++;
			page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
			
			// redimensiona a página para o tamanho A4, para padronização
			document.setPageSize(PageSize.A4);
			Rectangle r = pdfReader.getPageSize(pageOfCurrentReaderPDF); 
			float escalaHorizontal = ((PageSize.A4.getWidth() - document.rightMargin() - document.leftMargin()) /  (r.getWidth()));
			float escalaVertical = ((PageSize.A4.getHeight() - document.bottomMargin() - document.topMargin() -  margemParaAssinatura) /  (r.getHeight()));
			float escala = escalaHorizontal > escalaVertical ? escalaVertical : escalaHorizontal;
			if( escala > 1 ) escala = 1;
			
			// Insere a página
			document.newPage();
			
			// Rotaciona a página caso tenha informação no pdf de origem
			int rotation = pdfReader.getPageRotation(pageOfCurrentReaderPDF);
			if( rotation == 180) {
				float angulo  = (float) (rotation * (Math.PI / 180));
				cb.addTemplate(page, (float) Math.cos(angulo) * escala, (float) Math.sin(angulo), 
						(float) Math.sin(angulo), (float) Math.cos(angulo)*escala, PageSize.A4.getWidth(),PageSize.A4.getHeight());
			} else {
				cb.addTemplate(page, escala, 0, 0, escala, 20,50);
			}
			// Cria marcador para que o documento seja acessado pelo indice e pelo outline do Pdf
			if (pageOfCurrentReaderPDF == 1){
				criaMarcadorEAncora(document, processoDocumento, cb.getRootOutline(), writer);
			}
			// Insere Informações da pagina que está sendo inserida
			insereInformacaoPaginaDocumento(pageOfCurrentReaderPDF, processoDocumento, cb);
			
			// Insere informação da assinatura do documento
			insereAssinatura(cb, processoDocumentoBin, nomesAssinaturas);
		}
		return;
	}

	/** Método que insere uma página no PDF com um documento do tipo imagem (figura
	 * @param document - Documento que está sendo gravado 
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param pageOfCurrentReaderPDF - número da página atual
	 * @param figura - imagem que será inserida
	 * @param processoDocumento - processoDocumento que está sendo inserido
	 * @param processoDocumentoBin - processoDocumento que contem as assinaturas do documento que está sendo inserido
	 * @param writer - Objeto no qual é escrito o conteúdo do documento PDF
	 * @return void
	 * @throws DocumentException
	 */
	private void inserePaginaDocumentoImagem(Document document, PdfContentByte cb,
			int pageOfCurrentReaderPDF, Image figura, ProcessoDocumento processoDocumento,
			ProcessoDocumentoBin processoDocumentoBin, PdfWriter writer, String nomesAssinaturas) throws DocumentException {
		// Insere nova página e define seu tamanho
		document.newPage();
		document.setPageSize(PageSize.A4);
		pageOfCurrentReaderPDF++;
		
		// Cria marcador para que o documento seja acessado pelo indice e pelo outline do Pdf 
		criaMarcadorEAncora(document, processoDocumento, cb.getRootOutline(), writer);
		
		// Redimensiona a imagem para que ela possa caber uma página A4					
		float escalaHorizontal = ((PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin() )  /  (figura.getWidth()));
		float escalaVertical = ((PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin() - margemParaAssinatura) /  (figura.getHeight()));
		float escala = escalaHorizontal > escalaVertical ? escalaVertical : escalaHorizontal;
		if( escala > 1 ) escala = 1;
		escala = escala *100;
		figura.scalePercent(escala);
		
		// adiciona a imagem no documento
		document.add(figura);
		
		// Insere informações da pagina que está sendo inserida
		insereInformacaoPaginaDocumento(pageOfCurrentReaderPDF, processoDocumento, cb);
		
		// Insere informação da assinatura do documento
		insereAssinatura(cb, processoDocumentoBin, nomesAssinaturas);
		return;
	}

	/** Método que insere uma página quando o documento por algum motivo não pode ser adicionado no pdf.
	 * @param document - Documento que está sendo gravado 
	 * @param currentReader - sequencial do documento que está sendo lido dentro da lista de documentos	 * @param currentReader
	 * @param pageOfCurrentReaderPDF - número da página atual
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param writer - Objeto no qual é escrito o conteúdo do documento PDF
	 * @throws DocumentException
	 */
	private void inserePaginaDocumentoNaoInserido(
			Document document, ProcessoDocumento processoDocumento, int currentReader, 
			int pageOfCurrentReaderPDF, PdfContentByte cb, PdfWriter writer) throws DocumentException {
		document.newPage();
		
		String msgJuntada = "";
		if(processoDocumento.getDataJuntada() != null){
			msgJuntada = "\n Data da assinatura: " + dataFormater.format(processoDocumento.getDataJuntada()) ;
		}
		
		StringBuilder msgDocIndisponivel = new StringBuilder();
		msgDocIndisponivel.append("\n\n\n Tipo de documento: ");
		msgDocIndisponivel.append(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento());
		msgDocIndisponivel.append("\n Descrição do documento: " + processoDocumento.getProcessoDocumento());
		msgDocIndisponivel.append("\n Id: " + processoDocumento.getIdProcessoDocumento());
		msgDocIndisponivel.append(msgJuntada);
		msgDocIndisponivel.append("\n\n\n Atenção\n");
		msgDocIndisponivel.append("\n Por motivo técnico, este documento não pode ser adicionado à compilação selecionada pelo ");
		msgDocIndisponivel.append("usuário. Todavia, seu conteúdo pode ser acessado nos \'Autos Digitais\' e no menu \'Documentos\'.");

		document.add(new Chunk(dataHoraFormater.format(processoDocumento.getDataInclusao()), fontDocumento)
				.setLocalDestination(String.valueOf(currentReader)));
		document.add(new Paragraph("", fontDocumento));
		document.add(new Chunk(processoDocumento.getProcessoDocumento(), fontDocumento));
		document.add(new Paragraph("", fontDocumento));
		document.add(new Chunk(msgDocIndisponivel.toString(), red));

		criaMarcadorEAncora(document, processoDocumento, cb.getRootOutline(), writer);
		
		// Insere Informações da pagina que está sendo inserida
		insereInformacaoPaginaDocumento(pageOfCurrentReaderPDF, processoDocumento, cb);
	}

	/** Método que insere informações da assinatura do documento.
	 * @param cb - PdfContentByte - Conteúdo do PDF
	 * @param processoDocumentoBin - processoDocumentoBin onde estão as informações de assinatura 
	 * @return void
	 */
	private void insereAssinatura(PdfContentByte cb, ProcessoDocumentoBin processoDocumentoBin, String nomesAssinaturas) {
		ProcessoDocumentoBinHome.instance().setId(processoDocumentoBin.getIdProcessoDocumentoBin());
		
		// rodape de validacao da assinatura
		String urlValidacaoAssinatura = ValidacaoAssinaturaProcessoDocumento.instance().getUrlValidacao()
				+ "?x="
				+ ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento();

		if (nomesAssinaturas == null) {
			nomesAssinaturas = ValidacaoAssinaturaProcessoDocumento.instance()
					.getNomesAssinaturas(processoDocumentoBin);
		}

		String assinatura1 = "Assinado eletronicamente por: " + nomesAssinaturas;
		
		if (nomesAssinaturas != null && !nomesAssinaturas.trim().equals("")){
			insereQrcodeNoPdf(cb, processoDocumentoBin);
			
			cb.beginText();
			cb.setFontAndSize(bf, 7);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, assinatura1.toString(), 60, 33, 0);
			cb.endText();

			cb.beginText();
			cb.setFontAndSize(bf, 7);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, urlValidacaoAssinatura.toString(), 60, 23, 0);
			cb.endText();

			cb.beginText();
			cb.setFontAndSize(bf, 7);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Número do documento: "
				+ ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(), 60,
					13, 0);
			cb.endText();
		}
		
	}

	/** Método que cria no pdf um marcador e Ancora para que a pagina possa ser acessada pelo indice e pelo outline do pdf.
	 *  O marcador é identificado pelo tipo de documento, sequencia e data de inclusão  
	 * @param document - Documento que está sendo gravado
	 * @param processoDocumento - processoDocumento que está sendo lido
	 * @param root - raiz do pdf (para ir para a primeira página)
	 * @param writer - Objeto no qual é escrito o conteúdo do documento PDF
	 * @return void
	 * @throws DocumentException 
	 */
	private void criaMarcadorEAncora(Document document, ProcessoDocumento processoDocumento, PdfOutline root, PdfWriter writer) throws DocumentException {
		// Criar um marcador que referencia a primeira página do documento (que é utilizado para navegar pelo índice de documentos)
		document.add(new Chunk(" ").setLocalDestination(String.valueOf(processoDocumento.getIdProcessoDocumento())));
		
		StringBuffer titulo = new StringBuffer();
		titulo.append(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento())
				.append(" | NUM: ").append(processoDocumento.getIdProcessoDocumento()).append(" | ")
				.append(dataHoraFormater.format(processoDocumento.getDataInclusao()));
	
		// Criar uma entrada no marcador (bookmark) vinculada a este documento
		criarMarcador(root, writer, writer.getCurrentPageNumber(), titulo.toString());
	}

	/** Método que insere no pdf informações sobre a página que está sendo incluídoa. Id do documento incluído e o número da pagina
	 * @param pageOfCurrentReaderPDF - número da página atual
	 * @param processoDocumento - documento
	 * @param cb - PdfContentByte - Conteúdo do PDF 
	 */
	private void insereInformacaoPaginaDocumento(int pageOfCurrentReaderPDF, ProcessoDocumento processoDocumento, PdfContentByte cb){
	
		StringBuffer pagina = new StringBuffer();
		pagina.append("Num. ").append(processoDocumento.getIdProcessoDocumento()).append(" - Pág. ").append(pageOfCurrentReaderPDF);
	
		cb.beginText();
		cb.setFontAndSize(bf, 9);
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER, pagina.toString(), 540, 30, 0);
		cb.endText();
	}
	
	
    /////PJEII-4184
	//carregar documento PDF no formato InputStream com PDFBox e retornar byte[]
	private InputStream tentarObterDocumentoPdfComPDFBox(InputStream docPDF) throws IOException, COSVisitorException{
		PDDocument doc = PDDocument.load(docPDF, true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.save(baos);
		return new ByteArrayInputStream(baos.toByteArray());
	}
	/////FIM PJEII-4184

	private PdfPCell createLinkDataCell(String texto, Font font, int alignment, String link){
		PdfPCell celula = new PdfPCell(new Phrase(new Chunk(texto, font).setLocalGoto(link)));
		celula.setSpaceCharRatio(12);
		celula.setPaddingTop(1);
		celula.setPaddingBottom(3);
		celula.setHorizontalAlignment(alignment);
		return celula;
	}

	private PdfPCell createHeaderCell(String texto, Font font, int alignment){
		PdfPCell celula = new PdfPCell(new Phrase(texto, font));
		celula.setBackgroundColor(corCabecTabela);
		celula.setPaddingBottom(4);
		celula.setHorizontalAlignment(alignment);
		return celula;
	}

	private void verticalLine(Document d, PdfWriter writer) throws DocumentException{
		PdfContentByte cb = writer.getDirectContent();
		float pos = writer.getVerticalPosition(false);
		float marginRight = writer.getPageSize().getLeft();
		float marginLeft = writer.getPageSize().getRight();
		cb.moveTo(marginLeft - 30, pos - 3);
		cb.lineTo(marginRight + 30, pos - 3);
		cb.stroke();
	}

	private void blankLine(Document d) throws DocumentException{
		d.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL)));
	}

	public void setResurcePath(String resourcePath){
		this.resourcePath = resourcePath;

	}
	
	public boolean isGerarIndiceDosDocumentos() {
		return gerarIndiceDosDocumentos;
	}

	public void setGerarIndiceDosDocumentos(boolean gerarIndiceDosDocumentos) {
		this.gerarIndiceDosDocumentos = gerarIndiceDosDocumentos;
	}

	public boolean isGerarInfoClasseAtual() {
		return gerarInfoClasseAtual;
	}

	public void setGerarInfoClasseAtual(boolean gerarInfoClasseAtual) {
		this.gerarInfoClasseAtual = gerarInfoClasseAtual;
	}
	
	/**
	 * Método que insere a imagem QrCode no PDF
	 * @param cb PdfContentByte
	 * @param pd ProcessoDocumentoBin aonde será inserido o QrCode
	 */
	private void insereQrcodeNoPdf(PdfContentByte cb, ProcessoDocumentoBin pd){
		try {
			Image barcode = com.itextpdf.text.Image.getInstance(ValidacaoAssinaturaProcessoDocumento.instance().geraQRCodeComValidacao(pd).toByteArray());
			barcode.rotate();
			int widthCode = (int) (barcode.getWidth() * 0.6);
			int heightCode = (int) (barcode.getHeight() * 0.6);
			cb.addImage(barcode, widthCode, 0, 0, heightCode, -3, -3);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método responsável por criar um marcador (bookmark) para a página especificada
	 * @param root Marcador pai
	 * @param writer - Objeto no qual é escrito o conteúdo do documento PDF
	 * @param numeroPagina Número da página vinculada ao marcador
	 * @param titulo Título do marcador
	 */
	private void criarMarcador(PdfOutline root, PdfWriter writer, int numeroPagina, String titulo) {
		new PdfOutline(
			root, PdfAction.gotoLocalPage(numeroPagina, 
			new PdfDestination(PdfDestination.XYZ, -1, 10000, 0), writer),
			titulo);
	}
	
	/**
	 * Ajusta o caminho dos recursos de imagem enquanto n?o se usa a forma referencial.
	 * @param html
	 * @return html com o caminho dos recursos de imagem ajustados.
	 */
	private String AjustarCaminhoRecursosImagem(String html){
		Pattern ptn = Pattern.compile("<img[^>]+src=\"(http+([^>]+)>)");

		Matcher listaTagsImg = ptn.matcher(html);

		while (listaTagsImg.find()) {
			String tagImg = listaTagsImg.group();
			if (tagImg != null && !tagImg.toLowerCase().contains("base64")) {
				String taImgPathAjustado = tagImg.replaceAll("src=\"(.*?)/img", "src=\"" + resourcePath + "/img");
				html = html.replace(tagImg, taImgPathAjustado);
			}
		}

		return html;
    }
}
