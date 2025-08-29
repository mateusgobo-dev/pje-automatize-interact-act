package br.jus.pje.nucleo.dto;

public class TipoNormaDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String tipoNorma;

	private Boolean ativo;
	
	private String codigoTribunal;
	

	public TipoNormaDTO(Integer id, String tipoNorma, Boolean ativo) {
		super();
		this.id = id;
		this.tipoNorma = tipoNorma;
		this.ativo = ativo;
	}

	public TipoNormaDTO() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTipoNorma() {
		return tipoNorma;
	}

	public void setTipoNorma(String tipoNorma) {
		this.tipoNorma = tipoNorma;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getCodigoTribunal() {
		return codigoTribunal;
	}

	public void setCodigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
	}
	
	
}
