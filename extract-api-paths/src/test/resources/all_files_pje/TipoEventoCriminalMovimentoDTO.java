package br.jus.pje.nucleo.dto;

import java.io.Serializable;

public class TipoEventoCriminalMovimentoDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private TipoEventoCriminalDTO tipoEventoCriminal;
	private String codMovimento;
	private String codigoTribunal;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public TipoEventoCriminalDTO getTipoEventoCriminal() {
		return tipoEventoCriminal;
	}
	public void setTipoEventoCriminal(TipoEventoCriminalDTO tipoEventoCriminal) {
		this.tipoEventoCriminal = tipoEventoCriminal;
	}
	public String getCodMovimento() {
		return codMovimento;
	}
	public void setCodMovimento(String codMovimento) {
		this.codMovimento = codMovimento;
	}
	public String getCodigoTribunal() {
		return codigoTribunal;
	}
	public void setCodigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
	}
}
