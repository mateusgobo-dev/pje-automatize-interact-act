package br.com.infox.editor.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import br.jus.csjt.pje.commons.util.Base64;

@Name(Base64ImageUpload.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class Base64ImageUpload implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "base64ImageUpload";
	
	private String base64;
	
	public void listener(UploadEvent e) {
		UploadItem item = e.getUploadItem();
		byte[] conteudoArquivo = null;
		
		if(item.isTempFile()) {
			try {
				File file = item.getFile();
				FileInputStream input = new FileInputStream(file);
				conteudoArquivo = new byte[input.available()];
				input.read(conteudoArquivo);
				input.close();
			} catch(Exception exc) {
				exc.printStackTrace();
			}
		} else {
			conteudoArquivo = item.getData();
		}
		
		StringBuilder base64Builder = new StringBuilder("data:")
			.append(item.getContentType())
			.append(";base64,")
			.append(Base64.encodeBytes(conteudoArquivo));
		base64 = base64Builder.toString();
	}
	
	public String getBase64() {
		return base64;
	}
}
