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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = EstatisticaEventoProcesso.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_est_evento_processo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_est_evento_processo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstatisticaEventoProcesso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstatisticaEventoProcesso,Integer> {

	public static final String TABLE_NAME = "tb_est_evento_processo";
	private static final long serialVersionUID = 1L;

	private int idEstatisticaProcesso;
	private int idProcessoTrf;
	private String orgaoJulgador;
	private String competencia;
	private String classeJudicial;
	private String codEvento;
	private String codEstado;
	private String jurisdicao;
	private Date dataInclusao;
	private Boolean documentoApelacao;
	private Boolean documentoSentenca;
	private boolean enviadoTrf;
	private String numeroProcesso;

	@Id
	@GeneratedValue(generator = "gen_est_evento_processo")
	@Column(name = "id_estatistica_processo", unique = true, nullable = false)
	public int getIdEstatisticaProcesso() {
		return this.idEstatisticaProcesso;
	}

	public void setIdEstatisticaProcesso(int idEstatisticaProcesso) {
		this.idEstatisticaProcesso = idEstatisticaProcesso;
	}

	@Column(name = "id_processo_trf")
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Column(name = "ds_orgao_julgador", length = 200, nullable = false)
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "ds_competencia", length = 200)
	public String getCompetencia() {
		return competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	@Column(name = "ds_classe_judicial", length = 200, nullable = false)
	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Column(name = "cd_evento", length = 30)
	public String getCodEvento() {
		return codEvento;
	}

	public void setCodEvento(String codTipoMovimentacao) {
		this.codEvento = codTipoMovimentacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", nullable = false)
	@NotNull
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Column(name = "cd_estado", length = 2)
	@Length(max = 2)
	public String getCodEstado() {
		return this.codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setJurisdicao(String jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@Column(name = "ds_jurisdicao", length = 30, nullable = false)
	public String getJurisdicao() {
		return jurisdicao;
	}

	public void setDocumentoApelacao(Boolean documentoApelacao) {
		this.documentoApelacao = documentoApelacao;
	}

	@Column(name = "in_doc_tipo_apelacao", nullable = false)
	public Boolean getDocumentoApelacao() {
		return documentoApelacao;
	}

	public void setDocumentoSentenca(Boolean documentoSentenca) {
		this.documentoSentenca = documentoSentenca;
	}

	@Column(name = "in_doc_tipo_sentenca", nullable = false)
	public Boolean getDocumentoSentenca() {
		return documentoSentenca;
	}

	@Column(name = "in_enviado_trf", nullable = false)
	public boolean getEnviadoTrf() {
		return enviadoTrf;
	}

	public void setEnviadoTrf(boolean enviadoTrf) {
		this.enviadoTrf = enviadoTrf;
	}

	@Column(name = "nr_processo", length = 200)
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstatisticaEventoProcesso> getEntityClass() {
		return EstatisticaEventoProcesso.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstatisticaProcesso());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
