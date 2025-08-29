package br.jus.cnj.pje.view;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.je.pje.entity.vo.BinarioVO;

@Name("downloadAction")
@Scope(ScopeType.EVENT)
public class DownloadAction {

	@In
	private DocumentoBinManager documentoBinManager;
	
	public void download() {
		BinarioVO binario = (BinarioVO)Contexts.getSessionContext().get("download-binario");

		if (binario == null) {
			return;
		}
		
		boolean isHtml = binario.getMimeType().equals("text/html");
		boolean isPdf = binario.getMimeType().equals("application/pdf") || binario.getMimeType().equals("text/html"); 
		
		byte[] conteudo = null;
		try {
			if(isHtml) {
				conteudo = binario.getHtml().getBytes(Charset.forName("ISO-8859-1"));
			} else if ( binario.getFile()!=null ) {
				conteudo = IOUtils.toByteArray(new FileInputStream(binario.getFile()));
			}
			else {
				conteudo = documentoBinManager.getData(binario.getNumeroStorage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType(binario.getMimeType());
		
		response.setContentLength(conteudo.length);

		if(!isHtml) {
			if(isPdf){
				response.setHeader("Content-disposition", "inline; filename=\"" + binario.getNomeArquivo() + "\"");
			}
			else{
				response.setHeader("Content-disposition", "attachment; filename=\"" + binario.getNomeArquivo() + "\"");
			}
	        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	        response.setHeader("Pragma", "no-cache");
	        response.setHeader("Expires", "0");
		}
		
		try {
			OutputStream out = response.getOutputStream();
			out.write(conteudo);
			out.flush();
			facesContext.responseComplete();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		Contexts.getSessionContext().remove("download-binario");
	}
}