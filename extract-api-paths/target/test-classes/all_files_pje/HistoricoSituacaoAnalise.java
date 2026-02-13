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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import br.jus.pje.jt.enums.SituacaoAnaliseEnum;
import br.jus.pje.nucleo.entidades.Usuario;

@Entity
@Table(name="tb_hist_situacao_analise", uniqueConstraints=@UniqueConstraint(columnNames={"id_pauta_sessao", "dt_situacao_analise"}))
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_situacao_analise", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_situacao_analise"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoSituacaoAnalise implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoSituacaoAnalise,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idHistoricoSituacaoAnalise;
	private PautaSessao pautaSessao;
	private SituacaoAnaliseEnum situacaoAnalise;
	private Date dataSituacaoAnalise;
	private Usuario usuarioSituacaoAnalise;

	@Id
	@GeneratedValue(generator = "gen_hist_situacao_analise")
	@Column(name = "id_hist_situacao_analise", unique = true, nullable = false)
	public Integer getIdHistoricoSituacaoAnalise() {
		return idHistoricoSituacaoAnalise;
	}

	public void setIdHistoricoSituacaoAnalise(Integer idHistoricoSituacaoAnalise) {
		this.idHistoricoSituacaoAnalise = idHistoricoSituacaoAnalise;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pauta_sessao", nullable = false)
	@NotNull
	public PautaSessao getPautaSessao() {
		return pautaSessao;
	}

	public void setPautaSessao(PautaSessao pautaSessao) {
		this.pautaSessao = pautaSessao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="in_situacao_analise", nullable=false)
	@NotNull
	public SituacaoAnaliseEnum getSituacaoAnalise() {
		return situacaoAnalise;
	}

	public void setSituacaoAnalise(SituacaoAnaliseEnum situacaoAnalise) {
		this.situacaoAnalise = situacaoAnalise;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dt_situacao_analise", nullable=false)
	@NotNull
	public Date getDataSituacaoAnalise() {
		return dataSituacaoAnalise;
	}

	public void setDataSituacaoAnalise(Date dataSituacaoAnalise) {
		this.dataSituacaoAnalise = dataSituacaoAnalise;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_situacao_analise", nullable = false)
	@NotNull
	public Usuario getUsuarioSituacaoAnalise() {
		return usuarioSituacaoAnalise;
	}

	public void setUsuarioSituacaoAnalise(Usuario usuarioSituacaoAnalise) {
		this.usuarioSituacaoAnalise = usuarioSituacaoAnalise;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoSituacaoAnalise> getEntityClass() {
		return HistoricoSituacaoAnalise.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdHistoricoSituacaoAnalise();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
