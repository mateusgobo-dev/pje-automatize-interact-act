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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_tarefa_suspensao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tarefa_suspensao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tarefa_suspensao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TarefaSuspensao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TarefaSuspensao,Integer> {

	private static final long serialVersionUID = -6181613864352405223L;

	public enum SituacaoEnum {
		A("Em Andamento"), C("Concluído"), P("Pendente");

		private String label;

		SituacaoEnum(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

	private Integer id;

	private IcrSuspensao icrSuspensao;

	private String assunto;

	private SituacaoEnum situacao;

	private Date dataInicio;

	private Date dataFim;

	@Id
	@GeneratedValue(generator = "gen_tarefa_suspensao")
	@Column(name = "id_tarefa_suspensao")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "id_icr_suspensao", nullable = false)
	public IcrSuspensao getIcrSuspensao() {
		return icrSuspensao;
	}

	public void setIcrSuspensao(IcrSuspensao icrSuspensao) {
		this.icrSuspensao = icrSuspensao;
	}

	@NotNull
	@Column(name = "ds_assunto", nullable = false)
	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "in_situacao", nullable = false)
	public SituacaoEnum getSituacao() {
		return situacao;
	}

	public void setSituacao(SituacaoEnum situacao) {
		this.situacao = situacao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_inicio", nullable = false)
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_fim")
	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TarefaSuspensao> getEntityClass() {
		return TarefaSuspensao.class;
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
