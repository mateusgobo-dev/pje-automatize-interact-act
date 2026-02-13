package br.jus.pje.nucleo.dto;

public class TipoDispositivoDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private String tipoDispositivo;
	
	private Boolean ativo;
	
	private String codigoTribunal;
	
	public TipoDispositivoDTO(Integer id, String tipoDispositivo, Boolean ativo) {
		super();
		this.id = id;
		this.tipoDispositivo = tipoDispositivo;
		this.ativo = ativo;
	}

	public TipoDispositivoDTO() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getTipoDispositivo() {
		return tipoDispositivo;
	}

	public void setTipoDispositivo(String tipoDispositivo) {
		this.tipoDispositivo = tipoDispositivo;
	}

	public String getCodigoTribunal() {
		return codigoTribunal;
	}

	public void setCodigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
	}
	
}
