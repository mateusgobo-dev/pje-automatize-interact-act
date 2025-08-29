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

import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.Usuario;

@Entity
@Table(name = "tb_hist_situacao_sessao", uniqueConstraints=@UniqueConstraint(columnNames={"id_sessao", "dt_situacao_sessao"}))
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_situacao_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_situacao_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoSituacaoSessao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoSituacaoSessao,Integer> {

	private static final long serialVersionUID = 1L;
	
	private Integer idHistoricoSituacaoSessao;
	private SessaoJT sessao;
	private SituacaoSessaoEnum situacaoSessao;
	private Date dataSituacaoSessao;
	private Usuario usuarioSituacaoSessao;

	@Id
	@GeneratedValue(generator = "gen_hist_situacao_sessao")
	@Column(name = "id_hist_situacao_sessao", unique = true, nullable = false)
	public Integer getIdHistoricoSituacaoSessao() {
		return idHistoricoSituacaoSessao;
	}

	public void setIdHistoricoSituacaoSessao(Integer idHistoricoSituacaoSessao) {
		this.idHistoricoSituacaoSessao = idHistoricoSituacaoSessao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao", nullable = false)
	@NotNull
	public SessaoJT getSessao() {
		return sessao;
	}

	public void setSessao(SessaoJT sessao) {
		this.sessao = sessao;
	}
	
	@Column(name = "in_situacao_sessao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public SituacaoSessaoEnum getSituacaoSessao() {
		return situacaoSessao;
	}

	public void setSituacaoSessao(SituacaoSessaoEnum situacaoSessao) {
		this.situacaoSessao = situacaoSessao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_situacao_sessao", nullable = false)
	@NotNull
	public Date getDataSituacaoSessao() {
		return dataSituacaoSessao;
	}

	public void setDataSituacaoSessao(Date dataSituacaoSessao) {
		this.dataSituacaoSessao = dataSituacaoSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_situacao_sessao", nullable = false)
	@NotNull
	public Usuario getUsuarioSituacaoSessao() {
		return usuarioSituacaoSessao;
	}

	public void setUsuarioSituacaoSessao(Usuario usuarioSituacaoSessao) {
		this.usuarioSituacaoSessao = usuarioSituacaoSessao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoSituacaoSessao> getEntityClass() {
		return HistoricoSituacaoSessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdHistoricoSituacaoSessao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
