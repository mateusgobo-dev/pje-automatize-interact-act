/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table(name = "tb_org_prcdmnto_originario")
@org.hibernate.annotations.GenericGenerator(name = "gen_org_prcdmnto_originario", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_org_prcdmnto_originario"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoProcedimentoOriginario implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoProcedimentoOriginario,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private TipoOrigem tipoOrigem;
	private String dsCodOrigem;
	private String dsNomeOrgao;
	private Cep cep;
	private String nmLogradouro;
	private String nmBairro;
	private String nmCidade;
	private String nmNumero;
	private String nmComplemento;
	private String cdUf;
	private String dsTelefone;
	private String dsDdd;
	private Boolean ativo = true;
	private Integer codigoNacional;
	private Integer idMunicipio;
	private String nrCep;

	public OrgaoProcedimentoOriginario() {
	}

	public OrgaoProcedimentoOriginario(Integer id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(generator = "gen_org_prcdmnto_originario")
	@Column(name = "id_org_procedimento_originario", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setId(String id) {
		if (id != null) {
			this.id = Integer.parseInt(id);
		}
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_tipo_origem", nullable = false)
	public TipoOrigem getTipoOrigem() {
		return tipoOrigem;
	}

	public void setTipoOrigem(TipoOrigem tipoOrigem) {
		this.tipoOrigem = tipoOrigem;
	}

	@Column(name = "ds_cod_origem", nullable = true)
	@Length(max = 10)
	public String getDsCodOrigem() {
		return dsCodOrigem;
	}

	public void setDsCodOrigem(String dsCodOrigem) {
		this.dsCodOrigem = dsCodOrigem;
	}

	@Column(name = "ds_nome_orgao", nullable = false)
	@Length(max = 100)
	@NotNull
	public String getDsNomeOrgao() {
		return dsNomeOrgao;
	}

	public void setDsNomeOrgao(String dsNomeOrgao) {
		this.dsNomeOrgao = dsNomeOrgao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_cep", nullable = false)
	@NotNull
	public Cep getCep() {
		return cep;
	}

	public void setCep(Cep cep) {
		this.cep = cep;
	}

	@Column(name = "nm_logradouro", nullable = false)
	@Length(max = 200)
	@NotNull
	public String getNmLogradouro() {
		return nmLogradouro;
	}

	public void setNmLogradouro(String nmLogradouro) {
		this.nmLogradouro = nmLogradouro;
	}

	@Column(name = "nm_bairro", nullable = false)
	@Length(max = 100)
	@NotNull
	public String getNmBairro() {
		return nmBairro;
	}

	public void setNmBairro(String nmBairro) {
		this.nmBairro = nmBairro;
	}

	@Column(name = "nm_cidade", nullable = false)
	@Length(max = 100)
	@NotNull
	public String getNmCidade() {
		return nmCidade;
	}

	public void setNmCidade(String nmCidade) {
		this.nmCidade = nmCidade;
	}

	@Column(name = "cd_uf", nullable = false)
	@Length(max = 2)
	@NotNull
	public String getCdUf() {
		return cdUf;
	}

	public void setCdUf(String cdUf) {
		this.cdUf = cdUf;
	}

	@Column(name = "ds_ddd", nullable = true)
	@Length(max = 2)
	public String getDsDdd() {
		return dsDdd;
	}

	public void setDsDdd(String dsDdd) {
		this.dsDdd = dsDdd;
	}

	@Column(name = "ds_telefone", nullable = true)
	@Length(max = 10)
	public String getDsTelefone() {
		return dsTelefone;
	}

	public void setDsTelefone(String dsTelefone) {
		this.dsTelefone = dsTelefone;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return getDsNomeOrgao();
	}

	@Column(name = "nm_numero", nullable = true)
	@Length(max = 10)
	public String getNmNumero() {
		return nmNumero;
	}

	public void setNmNumero(String nmNumero) {
		this.nmNumero = nmNumero;
	}

	@Column(name = "nm_complemento", nullable = true)
	@Length(max = 70)
	public String getNmComplemento() {
		return nmComplemento;
	}

	public void setNmComplemento(String nmComplemento) {
		this.nmComplemento = nmComplemento;
	}
	
	@Column(name = "cd_nacional", nullable = true)
	public Integer getCodigoNacional() {
		return codigoNacional;
	}

	public void setCodigoNacional(Integer codigoNacional) {
		this.codigoNacional = codigoNacional;
	}

	@Column(name = "id_municipio", nullable = true)
	public Integer getIdMunicipio() {
		return idMunicipio;
	}

	public void setIdMunicipio(Integer idMunicipio) {
		this.idMunicipio = idMunicipio;
	}

	@Column(name = "nr_cep", nullable = true)
	public String getNrCep() {
		return nrCep;
	}

	public void setNrCep(String nrCep) {
		this.nrCep = nrCep;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoProcedimentoOriginario> getEntityClass() {
		return OrgaoProcedimentoOriginario.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
