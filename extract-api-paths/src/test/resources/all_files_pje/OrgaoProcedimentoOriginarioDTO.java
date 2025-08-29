package br.jus.pje.nucleo.dto;

public class OrgaoProcedimentoOriginarioDTO extends PJeServiceApiDTO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private TipoOrigemDTO tipoOrigem;
	private String dsCodOrigem;
	private String dsNomeOrgao;
	private String dsTelefone;
	private String dsDdd;
	private String cep;
	private MunicipioDTO municipio = new MunicipioDTO();
	private String nmBairro;
	private String nmLogradouro;
	private String nmComplemento;
	private String nmNumero;
	private Boolean ativo = Boolean.TRUE;
	
	public OrgaoProcedimentoOriginarioDTO() {
		super();
	}

	public OrgaoProcedimentoOriginarioDTO(TipoOrigemDTO tipoOrigem, String dsCodOrigem, String dsNomeOrgao,
			String dsTelefone, String dsDdd, String cep, MunicipioDTO municipio, String nmBairro, String nmLogradouro,
			String nmComplemento, String nmNumero, Boolean ativo) {
		super();
		this.tipoOrigem = tipoOrigem;
		this.dsCodOrigem = dsCodOrigem;
		this.dsNomeOrgao = dsNomeOrgao;
		this.dsTelefone = dsTelefone;
		this.dsDdd = dsDdd;
		this.cep = cep;
		this.municipio = municipio;
		this.nmBairro = nmBairro;
		this.nmLogradouro = nmLogradouro;
		this.nmComplemento = nmComplemento;
		this.nmNumero = nmNumero;
		this.ativo = ativo;
	}

	public OrgaoProcedimentoOriginarioDTO(Integer id, TipoOrigemDTO tipoOrigem, String dsCodOrigem, String dsNomeOrgao,
			String dsTelefone, String dsDdd, String cep, MunicipioDTO municipio, String nmBairro, String nmLogradouro,
			String nmComplemento, String nmNumero, Boolean ativo) {
		super();
		this.id = id;
		this.tipoOrigem = tipoOrigem;
		this.dsCodOrigem = dsCodOrigem;
		this.dsNomeOrgao = dsNomeOrgao;
		this.dsTelefone = dsTelefone;
		this.dsDdd = dsDdd;
		this.cep = cep;
		this.municipio = municipio;
		this.nmBairro = nmBairro;
		this.nmLogradouro = nmLogradouro;
		this.nmComplemento = nmComplemento;
		this.nmNumero = nmNumero;
		this.ativo = ativo;
	}

	public OrgaoProcedimentoOriginarioDTO(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDsCodOrigem() {
		return dsCodOrigem;
	}
	
	public void setDsCodOrigem(String dsCodOrigem) {
		this.dsCodOrigem = dsCodOrigem;
	}

	public TipoOrigemDTO getTipoOrigem() {
		return tipoOrigem;
	}
	
	public void setTipoOrigem(TipoOrigemDTO tipoOrigem) {
		this.tipoOrigem = tipoOrigem;
	}
	
	public String getDsNomeOrgao() {
		return dsNomeOrgao;
	}
	
	public void setDsNomeOrgao(String dsNomeOrgao) {
		this.dsNomeOrgao = dsNomeOrgao;
	}
	
	public String getCep() {
		return cep;
	}
	
	public void setCep(String cep) {
		this.cep = cep;
	}
	
	public String getNmLogradouro() {
		return nmLogradouro;
	}
	
	public void setNmLogradouro(String nmLogradouro) {
		this.nmLogradouro = nmLogradouro;
	}
	
	public String getNmBairro() {
		return nmBairro;
	}
	
	public void setNmBairro(String nmBairro) {
		this.nmBairro = nmBairro;
	}
	
	public String getNmNumero() {
		return nmNumero;
	}
	
	public void setNmNumero(String nmNumero) {
		this.nmNumero = nmNumero;
	}
	
	public String getNmComplemento() {
		return nmComplemento;
	}
	
	public void setNmComplemento(String nmComplemento) {
		this.nmComplemento = nmComplemento;
	}
	
	public String getDsTelefone() {
		return dsTelefone;
	}
	
	public void setDsTelefone(String dsTelefone) {
		this.dsTelefone = dsTelefone;
	}
	
	public String getDsDdd() {
		return dsDdd;
	}
	
	public void setDsDdd(String dsDdd) {
		this.dsDdd = dsDdd;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public MunicipioDTO getMunicipio() {
		return municipio;
	}

	public void setMunicipio(MunicipioDTO municipio) {
		this.municipio = municipio;
	}
	
	
}
