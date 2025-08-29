package br.jus.pje.nucleo.dto;

public class MunicipioDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String municipio;
	private String codigoIbge;
	private String uf;

	public MunicipioDTO() {
		super();
	}

	public MunicipioDTO(Integer id, String municipio, String codigoIbge, String uf) {
		super();
		this.id = id;
		this.municipio = municipio;
		this.codigoIbge = codigoIbge;
		this.uf = uf;
	}
	
	public MunicipioDTO(int id, String municipio) {
		super();
		this.id = new Integer(id);
		this.municipio = municipio;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getCodigoIbge() {
		return codigoIbge;
	}

	public void setCodigoIbge(String codigoIbge) {
		this.codigoIbge = codigoIbge;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Override
	public String toString() {
		return municipio;
	}

}
