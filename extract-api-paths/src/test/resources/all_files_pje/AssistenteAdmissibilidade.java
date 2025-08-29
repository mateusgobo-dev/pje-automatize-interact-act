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
package br.jus.pje.jt.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = AssistenteAdmissibilidade.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_assist_admis", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ass_admis"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AssistenteAdmissibilidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AssistenteAdmissibilidade,Integer> {

	public static final String TABLE_NAME = "tb_ass_admis";
	private static final long serialVersionUID = 1L;

	private int idAssistenteAdmissibilidade;
	private Boolean alcada = Boolean.FALSE;
	private Double valorCondenacao;
	private Date dataAjuizamentoAcao;
	private Double valorCausa;
	private Double valorFixadoSentenca;
	private Integer idDocumentoRecorrido;
	private Boolean alteradoCursoAcao = Boolean.FALSE;

	@Id
	@GeneratedValue(generator = "gen_assist_admis")
	@Column(name = "id_assis_admis", columnDefinition = "integer", nullable = false, unique = true)
	@NotNull
	public int getIdAssistenteAdmissibilidade() {
		return idAssistenteAdmissibilidade;
	}

	public void setIdAssistenteAdmissibilidade(int idAssistenteAdmissibilidade) {
		this.idAssistenteAdmissibilidade = idAssistenteAdmissibilidade;
	}
	
	@Column(name = "in_alcada", nullable = false)
	@NotNull
	public Boolean getAlcada() {
		return alcada;
	}

	public void setAlcada(Boolean alcada) {
		this.alcada = alcada;
	}
	
	@Column(name = "vl_condenacao")
	public Double getValorCondenacao() {
		return valorCondenacao;
	}

	public void setValorCondenacao(Double valorCondenacao) {
		this.valorCondenacao = valorCondenacao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ajuizamento_acao")	
	public Date getDataAjuizamentoAcao() {
		return dataAjuizamentoAcao;
	}

	public void setDataAjuizamentoAcao(Date dataAjuizamentoAcao) {
		this.dataAjuizamentoAcao = dataAjuizamentoAcao;
	}
	
	@Column(name = "vl_causa")
	public Double getValorCausa() {
		return valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}
	
	@Column(name = "vl_fix_sentenca")
	public Double getValorFixadoSentenca() {
		return valorFixadoSentenca;
	}

	public void setValorFixadoSentenca(Double valorFixadoSentenca) {
		this.valorFixadoSentenca = valorFixadoSentenca;
	}
	
	@Column(name = "id_doc_recorrido")
	public Integer getIdDocumentoRecorrido() {
		return idDocumentoRecorrido;
	}

	public void setIdDocumentoRecorrido(Integer idDocumentoRecorrido) {
		this.idDocumentoRecorrido = idDocumentoRecorrido;
	}

	@Column(name = "in_alter_cur_acao", nullable = false)
	@NotNull
	public Boolean getAlteradoCursoAcao() {
		return alteradoCursoAcao;
	}

	public void setAlteradoCursoAcao(Boolean alteradoCursoAcao) {
		this.alteradoCursoAcao = alteradoCursoAcao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AssistenteAdmissibilidade> getEntityClass() {
		return AssistenteAdmissibilidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAssistenteAdmissibilidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
