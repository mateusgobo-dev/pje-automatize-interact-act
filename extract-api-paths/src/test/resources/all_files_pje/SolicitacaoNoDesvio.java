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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = SolicitacaoNoDesvio.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_solicitacao_no_desvio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_solicitacao_no_desvio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SolicitacaoNoDesvio implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SolicitacaoNoDesvio,Integer> {

	public static final String TABLE_NAME = "tb_solicitacao_no_desvio";
	private static final long serialVersionUID = 1L;

	private Integer idSolicitacaoNoDesvio;
	private ProcessoTrf processoTrf;
	private Date dataSolicitacao;
	private Usuario usuario;
	private String justificativa;
	private Tarefa tarefa;

	public SolicitacaoNoDesvio() {
	}

	@Id
	@GeneratedValue(generator = "gen_solicitacao_no_desvio")
	@Column(name = "id_solicitacao_no_desvio", unique = true, nullable = false)
	public Integer getIdSolicitacaoNoDesvio() {
		return this.idSolicitacaoNoDesvio;
	}

	public void setIdSolicitacaoNoDesvio(Integer idSolicitacaoNoDesvio) {
		this.idSolicitacaoNoDesvio = idSolicitacaoNoDesvio;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "dt_solicitacao", nullable = false)
	public Date getDataSolicitacao() {
		return dataSolicitacao;
	}

	public void setDataSolicitacao(Date dataSolicitacao) {
		this.dataSolicitacao = dataSolicitacao;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario() {
		return usuario;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_justificativa")
	public String getJustificativa() {
		return this.justificativa;
	}

	public void setJustificativa(String justificativa) {
		this.justificativa = justificativa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tarefa")
	public Tarefa getTarefa() {
		return tarefa;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SolicitacaoNoDesvio> getEntityClass() {
		return SolicitacaoNoDesvio.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdSolicitacaoNoDesvio();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
