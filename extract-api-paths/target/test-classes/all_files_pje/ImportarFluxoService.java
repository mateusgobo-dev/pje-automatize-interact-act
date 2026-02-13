package br.com.infox.ibpm.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import br.com.infox.ibpm.exception.ImportExportException;
import br.com.infox.ibpm.util.ZipFluxoUtil;
import br.com.infox.ibpm.validator.AbstractValidator;
import br.com.infox.ibpm.validator.LocalizacaoValidator;
import br.com.infox.ibpm.validator.PapelValidator;
import br.com.infox.ibpm.validator.RegistraEventoValidator;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Classe que ajusta no fluxo os ids das entidades referenciadas, baseando-se no
 * xml exportado na base original.
 * 
 */
@Name(value = ImportarFluxoService.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ImportarFluxoService implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String READING_FILE_MESSAGE = "Erro ao ler o arquivo, verifique se o mesmo não foi alterado desde a exportação.";

	public static final String NAME = "importarFluxoService";

	private String xmlFluxo;
	private String xmlExported;
	private List<String> warningList = new ArrayList<String>();

	public ImportarFluxoService() {

	}

	/**
	 * @param xmlFluxo
	 *            fluxo original a ser ajustado
	 * @param xmlExported
	 *            xml exportado na base de origem
	 */
	public ImportarFluxoService(String xmlFluxo, String xmlExported) {
		this.setXmlFluxo(xmlFluxo);
		this.xmlExported = xmlExported;
	}

	/**
	 * Executa a troca dos ids baseando-se no xml exportado e na base atual
	 * 
	 * @return xml do fluxo com os ids alterados
	 * @throws ImportExportException
	 *             se houver inconsistencia no fluxo
	 */
	public String execute() throws ImportExportException {
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(new StringReader(getXmlExported()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Element registraEvento = document.getRootElement().getChild("registraEventoAction");
		if (registraEvento != null) {
			validateAll(registraEvento.getChildren("agrupamento"), new RegistraEventoValidator());
		}
		Element verificaEvento = document.getRootElement().getChild("verificaEventoAction");
		if (verificaEvento != null) {
			validateAll(verificaEvento.getChildren("agrupamento"), new RegistraEventoValidator());
		}
		Element localizacaoRoot = document.getRootElement().getChild("localizacaoAssignment");
		if (localizacaoRoot != null) {
			Element localizacao = localizacaoRoot.getChild("localizacoes");
			if (localizacao != null) {
				validateAll(localizacao.getChildren("localizacao"), new LocalizacaoValidator());
			}
			Element papel = localizacaoRoot.getChild("papeis");
			if (papel != null) {
				validateAll(papel.getChildren("papel"), new PapelValidator());
			}
		}
		if (!getWarningList().isEmpty()) {
			throw new ImportExportException();
		}
		return getXmlFluxo();
	}

	@SuppressWarnings("unchecked")
	private void validateAll(List children, AbstractValidator validator) {
		for (Iterator<Element> it = children.iterator(); it.hasNext();) {
			Element e = it.next();
			int id = Integer.parseInt(e.getAttributeValue("id"));
			String text = e.getText();
			validator.put(id, text);
		}
		setXmlFluxo(validator.validate(getXmlFluxo()));
		getWarningList().addAll(validator.getWarningList());
	}

	public void importar(Fluxo fluxo, byte[] file) throws ImportExportException {
		ByteArrayInputStream bais = new ByteArrayInputStream(file);
		try {
			String[] unzipedFiles = ZipFluxoUtil.unzipXml(bais);
			if (unzipedFiles != null) {
				setXmlExported(unzipedFiles[1]);
				setXmlFluxo(unzipedFiles[0]);
				setWarningList(new ArrayList<String>());
				String importedFluxo = execute();
				fluxo.setCodFluxo(getCodFluxoFromXml(unzipedFiles[1]));
				fluxo.setFluxo(getNomeFluxoFromXml(unzipedFiles[1]) + " "
						+ DateUtil.getDataFormatada(new Date(), "dd/MM/yyyy HH:mm"));
				fluxo.setXml(importedFluxo);
				fluxo.setDataInicioPublicacao(new Date());
				fluxo.setAtivo(true);
			} else {
				ImportExportException iex = new ImportExportException();
				iex.setDescription(READING_FILE_MESSAGE);
				throw iex;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getNomeFluxoFromXml(String xml) {
		Element codigo = getElementValue(xml, ExportarFluxoService.ELEMENT_NOME);
		return codigo.getAttributeValue("value");
	}

	public String getCodFluxoFromXml(String xml) {
		Element codigo = getElementValue(xml, ExportarFluxoService.ELEMENT_CODIGO);
		return codigo.getAttributeValue("value");
	}

	private Element getElementValue(String xml, String elementName) {
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(new StringReader(xml));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Element codigo = document.getRootElement().getChild(elementName);
		return codigo;
	}

	public String getXmlFluxo() {
		return xmlFluxo;
	}

	public List<String> getWarningList() {
		return warningList;
	}

	public void setXmlFluxo(String xmlFluxo) {
		this.xmlFluxo = xmlFluxo;
	}

	public String getXmlExported() {
		return xmlExported;
	}

	public void setXmlExported(String xmlExported) {
		this.xmlExported = xmlExported;
	}

	public void setWarningList(List<String> warningList) {
		this.warningList = warningList;
	}
}
