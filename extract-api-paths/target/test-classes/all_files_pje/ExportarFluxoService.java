package br.com.infox.ibpm.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import br.com.infox.ibpm.exception.ImportExportException;
import br.com.infox.ibpm.expression.ExpressionHandler;
import br.com.infox.ibpm.expression.ExpressionHandlerFactory;
import br.com.infox.ibpm.util.ZipFluxoUtil;

@Name(ExportarFluxoService.NAME)
@AutoCreate
public class ExportarFluxoService {

	public static final String ELEMENT_CODIGO = "codigo";
	public static final String ELEMENT_NOME = "nome";

	public static final String NAME = "exportarFluxoService";

	private Set<ExpressionHandler> handlerSet = new HashSet<ExpressionHandler>();
	private List<String> warningList = new ArrayList<String>();

	public String exportEntities(String xml, String codigoFluxo, String nomeFluxo) throws ImportExportException {
		StringTokenizer st = new StringTokenizer(xml, "#{}");
		st.nextToken();
		while (st.hasMoreTokens()) {
			String expressao = st.nextToken();
			ExpressionHandler eh = ExpressionHandlerFactory.getOutputHandler(expressao);
			eh.execute();
			handlerSet.add(eh);
			st.nextToken();
		}
		Element root = new Element("fluxo");
		Element cod = new Element(ELEMENT_CODIGO);
		cod.setAttribute("value", codigoFluxo);
		root.addContent(cod);
		Element nome = new Element(ELEMENT_NOME);
		nome.setAttribute("value", nomeFluxo);
		root.addContent(nome);
		for (ExpressionHandler ex : handlerSet) {
			Element e = ex.getXml();
			if (e != null) {
				root.addContent(e);
			}
			warningList.addAll(ex.getWarningList());
		}

		Format fmt = Format.getPrettyFormat();
		fmt.setEncoding("ISO-8859-1");

		XMLOutputter out = new XMLOutputter(fmt);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			out.output(new Document(root), baos);
		} catch (IOException e) {
			// nao deve ocorrer pois nao é arquivo
			e.printStackTrace();
		}

		if (!warningList.isEmpty()) {
			throw new ImportExportException();
		}

		return new String(baos.toByteArray());

	}

	public void exportar(String xml, String codigoFluxo, String nomeFluxo) throws ImportExportException {
		if (xml != null && !"".equals(xml)) {
			try {
				String exported = exportEntities(xml, codigoFluxo, nomeFluxo);
				byte[] zipXml = ZipFluxoUtil.zipXml(xml, exported);
				Date hoje = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String fileName = "Fluxo_" + codigoFluxo + "_" + sdf.format(hoje) + ".zip";
				FacesContext context = FacesContext.getCurrentInstance();
				HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
				response.setContentType("application/zip");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				response.setContentLength(zipXml.length);
				ServletOutputStream out = response.getOutputStream();
				out.write(zipXml);
				out.close();
				context.responseComplete();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		} else {
			throw new ImportExportException();
		}
	}

	public List<String> getWarningList() {
		return warningList;
	}

}
