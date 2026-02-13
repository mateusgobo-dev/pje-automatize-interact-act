
package br.com.itx.component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.Crypto;

@Name("fileHome")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class FileHome implements Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] data;
	private String fileName;
	private Integer size;
	private String contentType;
	private String expression;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = ArrayUtil.copyOf(data);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String update() {
		return null;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFileType() {
		String ret = "";
		if (fileName != null) {
			ret = fileName.substring(fileName.lastIndexOf('.') + 1);
		}
		return ret;
	}

	public static FileHome instance() {
		return ComponentUtil.getComponent("fileHome");
	}

	public void clear() {
		this.data = null;
		this.fileName = null;
		this.size = null;
		this.contentType = null;
	}

	public String getMD5() {
		return Crypto.encodeMD5(data);
	}

	public void listener(UploadEvent ue) throws IOException {
		UploadItem ui = ue.getUploadItem();
		if(ui.isTempFile()){
			FileInputStream fis = new FileInputStream(ui.getFile());
			this.data = new byte[ui.getFileSize()];
			fis.read(this.data);
			fis.close();
		}else{
			this.data = ui.getData();
		}
		this.fileName = ui.getFileName();
		this.size = ui.getFileSize();
		this.contentType = ui.getContentType();
	}

	public void download() {
		if (data == null) {
			// TODO ver uma forma de identificar o motivo do erro
			return;
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType(getContentType());
		
		response.setContentLength(data.length);
		if(getContentType() != null && getContentType().equals("application/pdf")){
			response.setHeader("Content-disposition", "filename=\"" + getFileName() + "\"");
		}
		else{
			response.setHeader("Content-disposition", "attachment; filename=\"" + getFileName() + "\"");
		}
		
		try {
			OutputStream out = response.getOutputStream();
			out.write(data);
			out.flush();
			facesContext.responseComplete();
		} catch (IOException ex) {
			FacesMessages.instance().add("Erro ao descarregar o arquivo: " + fileName);
		}
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	/**
	 * Retorna a mensagem que informa o tipo e o tamanho máximo do arquivo 
	 * para upload de acordo com o MIME type informado.
	 * 
	 * @param mime MIME type
	 * @author Antonio Francisco Osorio Junior
	 */
	public String getMensagemTipoTamanhoMime(String mime) {
		try {
			return br.com.itx.util.FileUtil.getMensagemTamanhoMime(mime) + 
					"<br/>" + br.com.itx.util.FileUtil.getMensagemTipoMime(mime);
		} catch (PJeException ex) {
			return ex.getCode();
		}
	}

}