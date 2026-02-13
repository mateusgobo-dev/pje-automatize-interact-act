package br.jus.cnj.pje.webservice.client.corporativo;

public class TribunalDTO {
	private String codigoTribunal = null;
	private String jtr = null;
	private String nomeTribunal = null;
	private String segmentoJustica = null;

	public TribunalDTO codigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
		return this;
	}

	public String getCodigoTribunal() {
		return codigoTribunal;
	}

	public void setCodigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
	}

	public TribunalDTO jtr(String jtr) {
		this.jtr = jtr;
		return this;
	}

	public String getJtr() {
		return jtr;
	}

	public void setJtr(String jtr) {
		this.jtr = jtr;
	}

	public TribunalDTO nomeTribunal(String nomeTribunal) {
		this.nomeTribunal = nomeTribunal;
		return this;
	}

	public String getNomeTribunal() {
		return nomeTribunal;
	}

	public void setNomeTribunal(String nomeTribunal) {
		this.nomeTribunal = nomeTribunal;
	}

	public TribunalDTO segmentoJustica(String segmentoJustica) {
		this.segmentoJustica = segmentoJustica;
		return this;
	}

	public String getSegmentoJustica() {
		return segmentoJustica;
	}

	public void setSegmentoJustica(String segmentoJustica) {
		this.segmentoJustica = segmentoJustica;
	}

}
