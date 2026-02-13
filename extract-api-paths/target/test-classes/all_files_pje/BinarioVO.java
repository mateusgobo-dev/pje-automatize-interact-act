package br.jus.je.pje.entity.vo;

import java.io.File;
import java.io.Serializable;

public class BinarioVO implements Serializable{
    
	private static final long serialVersionUID = 2282661463584361887L;

	private int idBinario;
	private String mimeType;
	private String nomeArquivo;
	private String numeroStorage;
	private String html;
	private File file;
	
	public int getIdBinario() {
		return idBinario;
	}
	
	public void setIdBinario(int idBinario) {
		this.idBinario = idBinario;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getNomeArquivo() {
		return nomeArquivo;
	}
	
	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}
	
	public String getNumeroStorage() {
		return numeroStorage;
	}

	public void setNumeroStorage(String numeroStorage) {
		this.numeroStorage = numeroStorage;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	
}
