/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.component;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.IOException;
import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Base64;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import br.jus.pje.nucleo.util.Crypto;

import com.lowagie.text.pdf.PdfReader;

@Name("fileUpload")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
@Install(precedence = FRAMEWORK)
public class FileUpload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileData file;

	public FileUpload() {
		file = new FileData();
		System.out.println("Inicializando: " + this.getClass().getName());
	}

	public void listener(UploadEvent e) {
		UploadItem uit = e.getUploadItem();
		setData(uit.getData());
		setFileName(uit.getFileName());
		setExtensao(getFileName().substring(getFileName().lastIndexOf(".")));
		setTamanho(getData().length);
	}

	public static FileUpload instance() {
		return (FileUpload) Component.getInstance("fileUpload");
	}

	private boolean checkIsPDF(byte[] data) {
		if (data == null) {
			return false;
		}

		try {
			new PdfReader(data);
		} catch (IOException e) {
			return false;
		}
		return true;
		/*
		 * if(data == null || data.length <=4){ return false; } if(data[0] !=
		 * 0x25){// % return false; } if(data[1] != 0x50){// P return false; }
		 * if(data[2] != 0x44){// D return false; } if(data[3] != 0x46){// F
		 * return false; } if(data[4] != 0x2D){// - return false; }
		 * 
		 * return true;
		 */
		/*
		 * if(data[5]==0x31 && data[6]==0x2E && data[7]==0x33) // version is 1.3
		 * ? { // file terminator Assert.AreEqual(data[data.Length-7],0x25); //
		 * % Assert.AreEqual(data[data.Length-6],0x25); // %
		 * Assert.AreEqual(data[data.Length-5],0x45); // E
		 * Assert.AreEqual(data[data.Length-4],0x4F); // O
		 * Assert.AreEqual(data[data.Length-3],0x46); // F
		 * Assert.AreEqual(data[data.Length-2],0x20); // SPACE
		 * Assert.AreEqual(data[data.Length-1],0x0A); // EOL return; }
		 * 
		 * if(data[5]==0x31 && data[6]==0x2E && data[7]==0x34) // version is 1.4
		 * ? { // file terminator Assert.AreEqual(data[data.Length-6],0x25); //
		 * % Assert.AreEqual(data[data.Length-5],0x25); // %
		 * Assert.AreEqual(data[data.Length-4],0x45); // E
		 * Assert.AreEqual(data[data.Length-3],0x4F); // O
		 * Assert.AreEqual(data[data.Length-2],0x46); // F
		 * Assert.AreEqual(data[data.Length-1],0x0A); // EOL return; }
		 * 
		 * Assert.Fail("Unsupported file format");
		 */
	}

	public byte[] getData() {
		return getFile().getData();
	}

	public String getBase64Data() {
		byte[] data = getData();
		return data == null ? null : Base64.encodeBytes(getData());
	}

	public void setBase64Data(String base64enconded) {
		return;
	}

	public void setData(byte[] data) {
		if (checkIsPDF(data)) {
			this.getFile().setTamanho(data == null ? 0 : data.length);
			this.getFile().setData(data);
		} else {
			this.getFile().setTamanho(-1);
			this.getFile().setData(null);
		}
	}

	public String getFileName() {
		return getFile().getFileName();
	}

	public void setFileName(String fileName) {
		if (fileName != null && !fileName.isEmpty()) {
			setExtensao(fileName.substring(fileName.lastIndexOf(".")));
		}
		this.getFile().setFileName(fileName);
	}

	public String getExtensao() {
		return getFile().getExtensao();
	}

	public void setExtensao(String extensao) {
		this.getFile().setExtensao(extensao);
	}

	public int getTamanho() {
		return getFile().getTamanho();
	}

	public void setTamanho(int tamanho) {
		this.getFile().setTamanho(tamanho);
	}

	public String getMD5() {
		return Crypto.encodeMD5(getData());
	}

	public void clearUpload() {
		file = new FileData();
	}

	public void setFile(FileData file) {
		this.file = file;
	}

	public FileData getFile() {
		return file != null ? file : new FileData();
	}

}