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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_acompanhamento_tarefa_suspensao")
@org.hibernate.annotations.GenericGenerator(name = "gen_acomp_taref_susp", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_acompanhamento_tarefa_suspensao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AcompanhamentoTarefaSuspensao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AcompanhamentoTarefaSuspensao,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4627936971546225159L;

	private Integer id;

	private String descricao;

	private Date dataCumprimento;

	private TarefaSuspensao tarefaSuspensao;

	@Id
	@GeneratedValue(generator = "gen_acomp_taref_susp")
	@Column(name = "id_acompanhamento_tarefa_suspensao")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NotNull
	@Column(name = "ds_acompanhamento", nullable = false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_cumprimento", nullable = false)
	public Date getDataCumprimento() {
		return dataCumprimento;
	}

	public void setDataCumprimento(Date dataCumprimento) {
		this.dataCumprimento = dataCumprimento;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_tarefa_suspensao", nullable = false)
	public TarefaSuspensao getTarefaSuspensao() {
		return tarefaSuspensao;
	}

	public void setTarefaSuspensao(TarefaSuspensao tarefaSuspensao) {
		this.tarefaSuspensao = tarefaSuspensao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AcompanhamentoTarefaSuspensao> getEntityClass() {
		return AcompanhamentoTarefaSuspensao.class;
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
