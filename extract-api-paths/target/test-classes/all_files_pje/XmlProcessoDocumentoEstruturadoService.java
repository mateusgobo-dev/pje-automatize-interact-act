package br.com.infox.editor.service;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import br.com.infox.editor.manager.AnotacaoManager;
import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;


@Name(XmlProcessoDocumentoEstruturadoService.NAME)
@AutoCreate
public class XmlProcessoDocumentoEstruturadoService {

	public static final String NAME = "xmlProcessoDocumentoEstruturadoService";

	private static final String DIV_ATRIBUTOS_TEMPLATE = "<div id='id_%s_atributos' style='float: right; z-index:99; position: relative;'></div>";
	private static final String DIV_TITULO_TEMPLATE = "<div id='id_%s_titulo' style='display: inline;' tabIndex='%s'>%s</div>";
	private static final String DIV_CONTEUDO_TEMPLATE = "<div class='conteudo_editor' id='id_%s_conteudo' tabIndex='%s'>%s</div>";
	private static final String ABRIR_DIV_TITULO = "<div id='id_%s_titulo_completo' style='display: %s'>";
	private static final String FECHAR_DIV_TITULO = "</div>";

	private static final String DOCUMENTO_ELEMENT_NAME = "documento";
	private static final String CABECALHO_ELEMENT_NAME = "cabecalho";
	private static final String TOPICO_ELEMENT_NAME = "topico";
	private static final String ID_TOPICO_ELEMENT_NAME = "idTopico";
	private static final String ATRIBUTOS_ELEMENT_NAME = "atributos";
	private static final String NUMERACAO_ELEMENT_NAME = "numeracao";
	private static final String TITULO_ELEMENT_NAME = "titulo";
	private static final String CONTEUDO_ELEMENT_NAME = "conteudo";
	private static final String ABRIR_DIV_TITULO_ELEMENT_NAME = "abrirDivTitulo";
	private static final String FECHAR_DIV_TITULO_ELEMENT_NAME = "fecharDivTitulo";
	private static final String ANOTACAO_ELEMENT_NAME = "anotacao";
	private static final String ANOTACAO_CONTEUDO_ELEMENT_NAME = "conteudo";
	private static final String ANOTACAO_TITULO_ELEMENT_NAME = "titulo";
	private static final String ANOTACAO_TEXTO_RODAPE_ELEMENT_NAME = "textoRodape";

	@In
	private NumeracaoDocumentoService numeracaoDocumentoService;
	
	@In
	private AnotacaoManager anotacaoManager;
	
	private int tabIndex;
	private boolean exibirAnotacoes = false;

	public String criarXmlDocumento(ProcessoDocumentoEstruturado processoDocumentoEstruturado) throws TransformerException, ParserConfigurationException {
        Document doc = createDocument();
        doc.appendChild(criarDocumentoEstruturadoElement(doc, processoDocumentoEstruturado));
        return convertToString(doc);
	}
	
	public String criarXmlDocumento(ProcessoDocumentoEstruturado processoDocumentoEstruturado, List<ProcessoDocumentoEstruturadoTopico> whiteListTopicos) throws TransformerException, ParserConfigurationException {
        Document doc = createDocument();
        doc.appendChild(criarDocumentoEstruturadoElement(doc, processoDocumentoEstruturado, whiteListTopicos));
        return convertToString(doc);
	}

	private Element criarDocumentoEstruturadoElement(Document doc, ProcessoDocumentoEstruturado processoDocumentoEstruturado) {
		return criarDocumentoEstruturadoElement(doc, processoDocumentoEstruturado, Collections.EMPTY_LIST);
	}
	
	private Element criarDocumentoEstruturadoElement(Document doc, ProcessoDocumentoEstruturado processoDocumentoEstruturado, List<ProcessoDocumentoEstruturadoTopico> whiteListTopicos) {
		Element documentoElement = doc.createElement(DOCUMENTO_ELEMENT_NAME);

		Element cabecalhoElement = doc.createElement(CABECALHO_ELEMENT_NAME);
		cabecalhoElement.setNodeValue(processoDocumentoEstruturado.getCabecalho().getConteudo());
		documentoElement.appendChild(cabecalhoElement);

		tabIndex = 1;
        for (ProcessoDocumentoEstruturadoTopico topico: processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()) {
        	if (whiteListTopicos != null && whiteListTopicos.size() > 0) { // filtrando topicos
        		if (!whiteListTopicos.contains(topico)) {
        			continue;
        		}
        	}
        	Node topicoElement = criarTopicoElement(doc, topico);
        	documentoElement.appendChild(topicoElement);
        	if (exibirAnotacoes) {
        		List<Anotacao> anotacoesDoTopico = anotacaoManager.getAnotacoesDoTopico(topico);
        		Collections.sort(anotacoesDoTopico, new Comparator<Anotacao>() {
					@Override
					public int compare(Anotacao o1, Anotacao o2) {
						return o1.getDataCriacao().compareTo(o2.getDataCriacao());
					}
				});
        		for (Anotacao anotacao : anotacoesDoTopico) {
        			if (anotacao.getTopico().isHabilitado()) {
        				topicoElement.appendChild(criarAnotacaoElement(doc, anotacao));
        			}
        		}
        	}
        }
        
        if (exibirAnotacoes && (whiteListTopicos == null || whiteListTopicos.size() == 0)) {
        	List<Anotacao> anotacoesDoDocumento = anotacaoManager.getAnotacoesDoDocumento(processoDocumentoEstruturado);
        	Collections.sort(anotacoesDoDocumento, new Comparator<Anotacao>() {
				@Override
				public int compare(Anotacao o1, Anotacao o2) {
					return o1.getDataCriacao().compareTo(o2.getDataCriacao());
				}
			});
	        for (Anotacao anotacao : anotacoesDoDocumento) {
	        	if (!anotacao.getTopico().isAtivo() || !anotacao.getTopico().isHabilitado()) {
	        		documentoElement.appendChild(criarAnotacaoElement(doc, anotacao));
	        	}
	        }
        }
        
        return documentoElement;
	}

	private Node criarAnotacaoElement(Document doc, Anotacao anotacao) {
		Element anotacaoElement = doc.createElement(ANOTACAO_ELEMENT_NAME);
		
		Element conteudo = doc.createElement(ANOTACAO_CONTEUDO_ELEMENT_NAME);
		conteudo.setNodeValue(StringEscapeUtils.escapeXml(anotacao.getConteudo()));
		anotacaoElement.appendChild(conteudo);
		
		Element titulo = doc.createElement(ANOTACAO_TITULO_ELEMENT_NAME);
		titulo.setNodeValue(anotacaoManager.buildTituloAnotacao(anotacao) + " " + anotacaoManager.buildObservacaoAnotacao(anotacao));
		anotacaoElement.appendChild(titulo);
		
		Element textoRodape = doc.createElement(ANOTACAO_TEXTO_RODAPE_ELEMENT_NAME);
		textoRodape.setNodeValue(anotacaoManager.getNomePessoaCriacao(anotacao) + " em " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(anotacao.getDataAlteracaoStatus()));
		anotacaoElement.appendChild(textoRodape);
		
		return anotacaoElement;
	}

	private Node criarTopicoElement(Document document, ProcessoDocumentoEstruturadoTopico procDoctopico) {
		Element topicoElement = document.createElement(TOPICO_ELEMENT_NAME);

		Element idTopico = document.createElement(ID_TOPICO_ELEMENT_NAME);
		idTopico.setNodeValue(String.valueOf(procDoctopico.getCodIdentificador()));

		Element habilitado = document.createElement("habilitado");
		habilitado.setNodeValue(String.valueOf(procDoctopico.isHabilitado()));

		Element atributos = document.createElement(ATRIBUTOS_ELEMENT_NAME);
		String divAtributos = String.format(DIV_ATRIBUTOS_TEMPLATE, procDoctopico.getCodIdentificador());
		atributos.setNodeValue(StringEscapeUtils.escapeXml(divAtributos));

		Element numeracao = document.createElement(NUMERACAO_ELEMENT_NAME);
		numeracao.setNodeValue(numeracaoDocumentoService.getNumeracaoDocumento(procDoctopico));

		Element titulo = document.createElement(TITULO_ELEMENT_NAME);
		String divTitulo = String.format(DIV_TITULO_TEMPLATE, procDoctopico.getCodIdentificador(), tabIndex++, procDoctopico.getTitulo());
		titulo.setNodeValue(StringEscapeUtils.escapeXml(divTitulo));

		Element conteudo = document.createElement(CONTEUDO_ELEMENT_NAME);
		String divConteudo = String.format(DIV_CONTEUDO_TEMPLATE, procDoctopico.getCodIdentificador(), tabIndex++, procDoctopico.getConteudo());
		conteudo.setNodeValue(StringEscapeUtils.escapeXml(divConteudo));

		Element abrirDivTitulo = document.createElement(ABRIR_DIV_TITULO_ELEMENT_NAME);
		String abrirDiv = String.format(ABRIR_DIV_TITULO, procDoctopico.getCodIdentificador(), procDoctopico.isExibirTitulo() ? "block" : "none");
		abrirDivTitulo.setNodeValue(StringEscapeUtils.escapeXml(abrirDiv));

		Element fecharDivTitulo = document.createElement(FECHAR_DIV_TITULO_ELEMENT_NAME);
		String fecharDiv = String.format(FECHAR_DIV_TITULO, procDoctopico.getCodIdentificador());
		fecharDivTitulo.setNodeValue(StringEscapeUtils.escapeXml(fecharDiv));

		topicoElement.appendChild(idTopico);
		topicoElement.appendChild(habilitado);
		topicoElement.appendChild(atributos);
		topicoElement.appendChild(numeracao);
		topicoElement.appendChild(titulo);
		topicoElement.appendChild(conteudo);
		topicoElement.appendChild(abrirDivTitulo);
		topicoElement.appendChild(fecharDivTitulo);

		return topicoElement;
	}

	private String convertToString(Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);

        return result.getWriter().toString();
	}

	private Document createDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
		return db.newDocument();
	}

	public boolean isExibirAnotacoes() {
		return exibirAnotacoes;
	}
	
	public void setExibirAnotacoes(boolean exibirAnotacoes) {
		this.exibirAnotacoes = exibirAnotacoes;
	}
}
