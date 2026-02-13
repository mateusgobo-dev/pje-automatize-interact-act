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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_estabelcmento_prisional")
@org.hibernate.annotations.GenericGenerator(name = "gen_estabelcimento_prisional", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_estabelcimento_prisional"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstabelecimentoPrisional implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstabelecimentoPrisional,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idEstabelecimentoPrisional;
	private String dsEstabelecimentoPrisional;
	private String dsApelidoEstabelecimento;
	private String dsJurisdicao;
	private String dsEndereco;
	private String dsCidade;
	private String uf;
	private String nrCep;
	private String nrTelefone;
	private String nrFax;
	private String dsEmail;
	private Boolean ativo;

	public EstabelecimentoPrisional() {

	}

	public EstabelecimentoPrisional(Integer idEstabelecimentoPrisional) {
		this.idEstabelecimentoPrisional = idEstabelecimentoPrisional;
	}

	@Id
	@GeneratedValue(generator = "gen_estabelcimento_prisional")
	@Column(name = "id_estabelecimento_prisional")
	public Integer getIdEstabelecimentoPrisional() {
		return idEstabelecimentoPrisional;
	}

	public void setIdEstabelecimentoPrisional(Integer idEstabelecimentoPrisional) {
		this.idEstabelecimentoPrisional = idEstabelecimentoPrisional;
	}

	@Column(name = "ds_estabelecimento_prisional", nullable = false)
	@NotNull
	public String getDsEstabelecimentoPrisional() {
		return dsEstabelecimentoPrisional;
	}

	public void setDsEstabelecimentoPrisional(String dsEstabelecimentoPrisional) {
		this.dsEstabelecimentoPrisional = dsEstabelecimentoPrisional;
	}

	@Column(name = "ds_apelido_estabelecimento", nullable = true)
	public String getDsApelidoEstabelecimento() {
		return dsApelidoEstabelecimento;
	}

	public void setDsApelidoEstabelecimento(String dsApelidoEstabelecimento) {
		this.dsApelidoEstabelecimento = dsApelidoEstabelecimento;
	}

	@Column(name = "ds_jurisdicao", nullable = false)
	@NotNull
	public String getDsJurisdicao() {
		return dsJurisdicao;
	}

	public void setDsJurisdicao(String dsJurisdicao) {
		this.dsJurisdicao = dsJurisdicao;
	}

	@Column(name = "ds_endereco", nullable = true)
	public String getDsEndereco() {
		return dsEndereco;
	}

	public void setDsEndereco(String dsEndereco) {
		this.dsEndereco = dsEndereco;
	}

	@Column(name = "ds_cidade", nullable = false)
	@NotNull
	public String getDsCidade() {
		return dsCidade;
	}

	public void setDsCidade(String dsCidade) {
		this.dsCidade = dsCidade;
	}

	@Column(name = "cd_uf", nullable = false)
	@NotNull
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(name = "nr_cep", nullable = false)
	@NotNull
	public String getNrCep() {
		return nrCep;
	}

	public void setNrCep(String nrCep) {
		this.nrCep = nrCep;
	}

	@Column(name = "nr_telefone", nullable = true)
	public String getNrTelefone() {
		return nrTelefone;
	}

	public void setNrTelefone(String nrTelefone) {
		this.nrTelefone = nrTelefone;
	}

	@Column(name = "nr_fax", nullable = true)
	public String getNrFax() {
		return nrFax;
	}

	public void setNrFax(String nrFax) {
		this.nrFax = nrFax;
	}

	@Column(name = "ds_email", nullable = true)
	public String getDsEmail() {
		return dsEmail;
	}

	public void setDsEmail(String dsEmail) {
		this.dsEmail = dsEmail;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return uf + "/" + dsCidade + "/" + dsEstabelecimentoPrisional;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstabelecimentoPrisional> getEntityClass() {
		return EstabelecimentoPrisional.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdEstabelecimentoPrisional();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
