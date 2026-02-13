package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "vs_competencia_area_direito")
public class CompetenciaAreaDireito implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idAreaDireito;
	private String nomeAreaDireito;
	private String codAreaDireito;
	private Integer idJurisdicao;
	private String nomeJurisdicao;
	private Integer idClasseJudicial;
	private String codigoClasseJudicial;
	private String nomeClasseJudicial;
	private String siglaClasseJudicial;
	private Integer idCompetencia;
	private String nomeCompetencia;
	private Jurisdicao jurisdicao;
	private ClasseJudicial classeJudicial;

	public CompetenciaAreaDireito() {
		super();
	}

	public CompetenciaAreaDireito(Integer idAreaDireito, String nomeAreaDireito) {
		//super();
		this.idAreaDireito = idAreaDireito;
		this.nomeAreaDireito = nomeAreaDireito;
	}

	@Id
	@Column(name = "id_area_direito", insertable = false, updatable = false)
	public Integer getIdAreaDireito() {
		return idAreaDireito;
	}

	public void setIdAreaDireito(Integer idAreaDireito) {
		this.idAreaDireito = idAreaDireito;
	}

	@Column(name = "ds_area_direito", insertable = false, updatable = false)
	public String getNomeAreaDireito() {
		return nomeAreaDireito;
	}

	public void setNomeAreaDireito(String nomeAreaDireito) {
		this.nomeAreaDireito = nomeAreaDireito;
	}

	@Column(name = "cd_area_direito", insertable = false, updatable = false)
	public String getCodAreaDireito() {
		return codAreaDireito;
	}

	public void setCodAreaDireito(String codAreaDireito) {
		this.codAreaDireito = codAreaDireito;
	}

	@Column(name = "id_jurisdicao", insertable = false, updatable = false)
	public Integer getIdJurisdicao() {
		return idJurisdicao;
	}

	public void setIdJurisdicao(Integer idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}

	@Column(name = "ds_jurisdicao", insertable = false, updatable = false)
	public String getNomeJurisdicao() {
		return nomeJurisdicao;
	}

	public void setNomeJurisdicao(String nomeJurisdicao) {
		this.nomeJurisdicao = nomeJurisdicao;
	}

	@Column(name = "id_classe_judicial", insertable = false, updatable = false)
	public Integer getIdClasseJudicial() {
		return idClasseJudicial;
	}

	public void setIdClasseJudicial(Integer idClasseJudicial) {
		this.idClasseJudicial = idClasseJudicial;
	}

	@Column(name = "cd_classe_judicial", insertable = false, updatable = false)
	public String getCodigoClasseJudicial() {
		return codigoClasseJudicial;
	}

	public void setCodigoClasseJudicial(String codigoClasseJudicial) {
		this.codigoClasseJudicial = codigoClasseJudicial;
	}

	@Column(name = "ds_classe_judicial", insertable = false, updatable = false)
	public String getNomeClasseJudicial() {
		return nomeClasseJudicial;
	}

	public void setNomeClasseJudicial(String nomeClasseJudicial) {
		this.nomeClasseJudicial = nomeClasseJudicial;
	}

	@Column(name = "ds_classe_judicial_sigla", insertable = false, updatable = false)
	public String getSiglaClasseJudicial() {
		return siglaClasseJudicial;
	}

	public void setSiglaClasseJudicial(String siglaClasseJudicial) {
		this.siglaClasseJudicial = siglaClasseJudicial;
	}

	@Column(name = "id_competencia", insertable = false, updatable = false)
	public Integer getIdCompetencia() {
		return idCompetencia;
	}

	public void setIdCompetencia(Integer idCompetencia) {
		this.idCompetencia = idCompetencia;
	}

	@Column(name = "ds_competencia", insertable = false, updatable = false)
	public String getNomeCompetencia() {
		return nomeCompetencia;
	}

	public void setNomeCompetencia(String nomeCompetencia) {
		this.nomeCompetencia = nomeCompetencia;
	}

	@OneToOne
	@JoinColumn(name = "id_jurisdicao", insertable = false, updatable = false)
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@OneToOne
	@JoinColumn(name = "id_classe_judicial", insertable = false, updatable = false)
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

}
