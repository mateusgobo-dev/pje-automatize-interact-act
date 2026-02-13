package br.jus.pje.nucleo.beans.criminal;

import br.jus.pje.nucleo.dto.MunicipioDTO;

public class UnidadePrisionalBean extends BaseBean{
	
	private static final long serialVersionUID = 1L;
	
	private String dsUnidadePrisional;
	private String dsApelidoUnidade;
	private String dsJurisdicao;
	private String nrCep;
	private MunicipioDTO municipio = new MunicipioDTO();
	private String nmBairro;
	private String nmLogradouro;
	private String nrTelefone;
	private String nrFax;
	private String dsEmail;
	private Boolean ativo = Boolean.TRUE;
	
	public UnidadePrisionalBean() {
		super();
	}

	public UnidadePrisionalBean(String id) {
		super(id);
	}

	public UnidadePrisionalBean(String id, String dsUnidadePrisional, String dsApelidoUnidade, String dsJurisdicao,
			String nrCep, MunicipioDTO municipio, String nmBairro, String nmLogradouro, String nrTelefone, String nrFax,
			String dsEmail, Boolean ativo) {
		super(id);
		this.dsUnidadePrisional = dsUnidadePrisional;
		this.dsApelidoUnidade = dsApelidoUnidade;
		this.dsJurisdicao = dsJurisdicao;
		this.nrCep = nrCep;
		this.municipio = municipio;
		this.nmBairro = nmBairro;
		this.nmLogradouro = nmLogradouro;
		this.nrTelefone = nrTelefone;
		this.nrFax = nrFax;
		this.dsEmail = dsEmail;
		this.ativo = ativo;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
		this.id = id;
	}

	public String getDsUnidadePrisional() {
		return dsUnidadePrisional;
	}

	public void setDsUnidadePrisional(String dsUnidadePrisional) {
		this.dsUnidadePrisional = dsUnidadePrisional;
	}

	public String getDsApelidoUnidade() {
		return dsApelidoUnidade;
	}

	public void setDsApelidoUnidade(String dsApelidoUnidade) {
		this.dsApelidoUnidade = dsApelidoUnidade;
	}

	public String getDsJurisdicao() {
		return dsJurisdicao;
	}

	public void setDsJurisdicao(String dsJurisdicao) {
		this.dsJurisdicao = dsJurisdicao;
	}

	public String getNrCep() {
		return nrCep;
	}

	public void setNrCep(String nrCep) {
		this.nrCep = nrCep;
	}

	public MunicipioDTO getMunicipio() {
		return municipio;
	}

	public void setMunicipio(MunicipioDTO municipio) {
		this.municipio = municipio;
	}

	public String getNmBairro() {
		return nmBairro;
	}

	public void setNmBairro(String nmBairro) {
		this.nmBairro = nmBairro;
	}

	public String getNmLogradouro() {
		return nmLogradouro;
	}

	public void setNmLogradouro(String nmLogradouro) {
		this.nmLogradouro = nmLogradouro;
	}

	public String getNrTelefone() {
		return nrTelefone;
	}

	public void setNrTelefone(String nrTelefone) {
		this.nrTelefone = nrTelefone;
	}

	public String getNrFax() {
		return nrFax;
	}

	public void setNrFax(String nrFax) {
		this.nrFax = nrFax;
	}

	public String getDsEmail() {
		return dsEmail;
	}

	public void setDsEmail(String dsEmail) {
		this.dsEmail = dsEmail;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

}
