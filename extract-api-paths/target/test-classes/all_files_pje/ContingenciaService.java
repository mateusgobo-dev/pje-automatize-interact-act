package br.com.infox.pje.manager;

import java.io.IOException;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.dao.AutoTextoDao;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.service.ItextHtmlConverterService;
import br.com.itx.util.HtmlParaRtf;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoDAO;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.HtmlParaPdf;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.editor.AutoTexto;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;

import com.lowagie.text.DocumentException;


@Name(ContingenciaService.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ContingenciaService  extends GenericManager {
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "contingenciaService";
	
	@In
	private AutoTextoDao autoTextoDao;
	
	@In
	private ProcessoDocumentoDAO processoDocumentoDAO;
	
	@In
	private ItextHtmlConverterService itextHtmlConverterService;
	

	public byte[] getPdfAutoTexto() throws DocumentException, IOException, PdfException {
		
		String linhaHorizontal = "<hr/>";
		
		StringBuilder s = new StringBuilder();
		s.append("<div style=\"text-align:center\"><h2>MODELOS DE AUTOTEXTO<h2></div>");
		s.append("<br/><br/><br/>");
		s.append("<div style=\"text-align:center\"><h3>MODELOS PESSOAIS</h3></div>");
		s.append(linhaHorizontal);
		List<AutoTexto> listAutoTextoUsuario = autoTextoDao.getAutoTextoPorUsuarioList(Authenticator.getUsuarioLogado());
		for(AutoTexto autoTexto : listAutoTextoUsuario){
			s.append("<div style=\"text-align:left\"><b>"+autoTexto.getDescricao()+"</b></div>");
			s.append("<br/><br/>");
			s.append(autoTexto.getConteudo());
			s.append(linhaHorizontal);
		}
		
		s.append("<br/><br/><br/>");
		s.append("<div style=\"text-align:center\"><h3>MODELOS DO LOCAL</h3></div>");
		s.append(linhaHorizontal);
		List<AutoTexto> listAutoTextoLocal = autoTextoDao.getAutoTextoPorLocalizacaoList(Authenticator.getLocalizacaoAtual());
		for(AutoTexto autoTexto : listAutoTextoLocal){
			s.append("<b>"+autoTexto.getDescricao()+"</b>");
			s.append("<br/><br/>");
			s.append("<b>"+autoTexto.getConteudo()+"</b>");
			s.append(linhaHorizontal);
		}
		
		return HtmlParaPdf.converte(s.toString());
	}

	public byte[] exportarDocumentosProcesso(ProcessoTrf processoTrf) throws PdfException {
		ProcessoHome.instance().getInstance().setIdProcesso(processoTrf.getIdProcessoTrf());
		List<ProcessoDocumento> processoDocumentoList =  processoDocumentoDAO.listProcessoDocumentoAssinadosMagistrado(processoTrf);
		GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
		byte[] pdf = geradorPdf.gerarPdfUnificado(processoTrf,
				processoDocumentoList);
		return pdf;
	}

	public byte[] exportarSentenca(ProcessoDocumentoEstruturado processoDocumentoEstruturado) throws DocumentException, IOException {
		
		if(processoDocumentoEstruturado != null &&
		   processoDocumentoEstruturado.getProcessoDocumentoTrfLocal() != null &&
		   processoDocumentoEstruturado.getProcessoDocumentoTrfLocal().getProcessoDocumento() != null &&
		   processoDocumentoEstruturado.getProcessoDocumentoTrfLocal().getProcessoDocumento().getProcessoDocumentoBin()	!= null	){
			
			String modeloDocumento = processoDocumentoEstruturado.getProcessoDocumentoTrfLocal().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
			modeloDocumento = itextHtmlConverterService.converteImagensHtml(modeloDocumento);
			modeloDocumento = modeloDocumento.replace("file:", "");
			return HtmlParaRtf.converte(modeloDocumento);
		}
		return null;
	}
	
}
