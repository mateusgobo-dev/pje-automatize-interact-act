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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = PessoaLocalizacaoMagistrado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_local_mgstrdo_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_local_mgstrdo_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaLocalizacaoMagistrado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaLocalizacaoMagistrado,Integer> {

	public static final String TABLE_NAME = "tb_pess_localz_mgstrdo_log";
	private static final long serialVersionUID = 1L;

	private int idPessoaLocalizacaoMagistrado;
	private String papel;
	private PessoaMagistrado magistrado;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private String cargo;
	private String cargoVisibilidade;
	private Date dataInicial;
	private Date dataFinal;
	private Date dataCriacao;
	private Date dataExclusao;
	private String norma;

	public PessoaLocalizacaoMagistrado() {
	}

	@Id
	@GeneratedValue(generator = "gen_pess_local_mgstrdo_log")
	@Column(name = "id_pess_localiz_magistrado_log", unique = true, nullable = false)
	public int getIdPessoaLocalizacaoMagistrado() {
		return this.idPessoaLocalizacaoMagistrado;
	}

	public void setIdPessoaLocalizacaoMagistrado(int idPessoaLocalizacaoMagistrado) {
		this.idPessoaLocalizacaoMagistrado = idPessoaLocalizacaoMagistrado;
	}

	@Column(name = "ds_papel", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getPapel() {
		return papel;
	}

	public void setPapel(String papel) {
		this.papel = papel;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicial")
	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_final")
	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado")
	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}

	public void setMagistrado(PessoaMagistrado magistrado) {
		this.magistrado = magistrado;
	}

	@Column(name = "ds_norma", length = 200)
	@Length(max = 200)
	public String getNorma() {
		return norma;
	}

	public void setNorma(String norma) {
		this.norma = norma;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Column(name = "ds_cargo", length = 200)
	@Length(max = 200)
	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	@Column(name = "ds_cargo_visibilidade", length = 200)
	@Length(max = 200)
	public String getCargoVisibilidade() {
		return cargoVisibilidade;
	}

	public void setCargoVisibilidade(String cargoVisibilidade) {
		this.cargoVisibilidade = cargoVisibilidade;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao")
	public Date getDataExclusao() {
		return dataExclusao;
	}

	public void setDataExclusao(Date dataExclusao) {
		this.dataExclusao = dataExclusao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaLocalizacaoMagistrado)) {
			return false;
		}
		PessoaLocalizacaoMagistrado other = (PessoaLocalizacaoMagistrado) obj;
		if (getIdPessoaLocalizacaoMagistrado() != other.getIdPessoaLocalizacaoMagistrado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaLocalizacaoMagistrado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaLocalizacaoMagistrado> getEntityClass() {
		return PessoaLocalizacaoMagistrado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaLocalizacaoMagistrado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
