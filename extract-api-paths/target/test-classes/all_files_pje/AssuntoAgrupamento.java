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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = AssuntoAgrupamento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_assunto_agrup", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_assunto_agrupamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AssuntoAgrupamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AssuntoAgrupamento,Integer>{

	public static final String TABLE_NAME = "tb_assunto_agrupamento";
	private static final long serialVersionUID = 1L;

	private int id;
	private AgrupamentoClasseJudicial agrupamento;
	private AssuntoTrf assunto;

	@Id
	@GeneratedValue(generator = "gen_assunto_agrup")
	@Column(name = "id_assunto_agrupamento", unique = true, nullable = false)
	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_agrupamento", nullable = false)
	public AgrupamentoClasseJudicial getAgrupamento(){
		return agrupamento;
	}

	public void setAgrupamento(AgrupamentoClasseJudicial agrupamento){
		this.agrupamento = agrupamento;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_assunto_trf", nullable = false)
	public AssuntoTrf getAssunto(){
		return assunto;
	}

	public void setAssunto(AssuntoTrf assunto){
		this.assunto = assunto;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AssuntoAgrupamento> getEntityClass() {
		return AssuntoAgrupamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
