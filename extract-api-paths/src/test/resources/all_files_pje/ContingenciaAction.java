package br.com.infox.pje.action;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.pje.manager.ContingenciaService;
import br.jus.csjt.pje.commons.util.FileUtil;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;


@Name(ContingenciaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ContingenciaAction implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public static final String NAME = "contingenciaAction";
	
	@In
	private ContingenciaService contingenciaService;
	@In
	private ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
	
	public void exportarArquivos(Integer idProcessoDocumento) throws Exception{
		
		ProcessoDocumentoEstruturado processoDocumentoEstruturado = processoDocumentoEstruturadoManager.find(ProcessoDocumentoEstruturado.class, idProcessoDocumento);
		
		if(processoDocumentoEstruturado != null){
			byte[] arquivoPdfDocumentosProcesso =  contingenciaService.exportarDocumentosProcesso(processoDocumentoEstruturado.getProcessoTrf());
			byte[] arquivoPdfSentenca =  contingenciaService.exportarSentenca(processoDocumentoEstruturado);
			byte[] arquivoPdfAutoTexto =  contingenciaService.getPdfAutoTexto();
			
			List<byte[]> arquivosParaCompactar = new ArrayList<byte[]>();
			arquivosParaCompactar.add(arquivoPdfDocumentosProcesso);
			arquivosParaCompactar.add(arquivoPdfSentenca);
			arquivosParaCompactar.add(arquivoPdfAutoTexto);
			
			List<String> nomeArquivosCompactados = new ArrayList<String>();
			nomeArquivosCompactados.add("documentosProcesso.pdf");
			nomeArquivosCompactados.add("sentenca.rtf");
			nomeArquivosCompactados.add("autotexto.pdf");
			
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			
			FileUtil.zipBytes(arquivosParaCompactar, nomeArquivosCompactados, bas);
			
			uploadArquivo(processoDocumentoEstruturado.getProcesso().getNumeroProcesso()+".zip", bas.toByteArray(),"zip");
		}
		else{
			FacesMessages.instance().add(StatusMessage.Severity.INFO,"Não existem documentos estruturados associados ao documento.");
		}
	}
	

	private void uploadArquivo(String nomeArquivo, byte[] output, String tipoArquivo) throws Exception {
		FacesContext context = FacesContext.getCurrentInstance();  
	    HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
	    response.setContentType("application/"+tipoArquivo);
	    response.setContentLength(output.length);
	    response.addHeader("Content-Disposition", "attachment; filename=\""+nomeArquivo+"\"");
	    ServletOutputStream out = response.getOutputStream();
	    out.write(output);
	    out.close();
	    context.responseComplete();
	}


	

}
