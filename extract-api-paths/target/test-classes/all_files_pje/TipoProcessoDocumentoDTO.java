package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;


public class TipoProcessoDocumentoDTO {

	private int idTipoProcessoDocumento;
	private String tipoProcessoDocumento;
	
	public TipoProcessoDocumentoDTO() {
		super();
	}

	public TipoProcessoDocumentoDTO(int idTipoProcessoDocumento, String tipoProcessoDocumento) {
		super();
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public int getIdTipoProcessoDocumento() {
		return idTipoProcessoDocumento;
	}

	public void setIdTipoProcessoDocumento(int idTipoProcessoDocumento) {
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
	}

	public String getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(String tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	
	
	
}
