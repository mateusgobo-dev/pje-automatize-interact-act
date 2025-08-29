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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * @author thiago.vieira
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = AgrupamentoClasseJudicial.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "cd_agrupamento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_agrup_class_jud", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_agrupamento_classe"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AgrupamentoClasseJudicial implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AgrupamentoClasseJudicial,Integer> {

	public static final String TABLE_NAME = "tb_agrupamento_classe";
	private static final long serialVersionUID = 1L;

	private int idAgrupamento;
	private String codAgrupamento;
	private String agrupamento;
	private Boolean ativo;
	private List<ClasseJudicialAgrupamento> classeJudicialAgrupamentoList = new ArrayList<ClasseJudicialAgrupamento>(0);
	private List<AssuntoAgrupamento> assuntoAgrupamentoList = new ArrayList<AssuntoAgrupamento>(0);

	@Id
	@GeneratedValue(generator = "gen_agrup_class_jud")
	@Column(name = "id_agrupamento", unique = true, nullable = false)
	public int getIdAgrupamento() {
		return idAgrupamento;
	}

	public void setIdAgrupamento(int idAgrupamento) {
		this.idAgrupamento = idAgrupamento;
	}

	@Column(name = "cd_agrupamento", nullable = false, length = 3)
	@NotNull
	@Length(max = 3)
	public String getCodAgrupamento() {
		return codAgrupamento;
	}

	public void setCodAgrupamento(String codAgrupamento) {
		this.codAgrupamento = codAgrupamento;
	}

	@Column(name = "ds_agrupamento", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getAgrupamento() {
		return agrupamento;
	}

	public void setAgrupamento(String agrupamento) {
		this.agrupamento = agrupamento;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "agrupamento")
	public List<ClasseJudicialAgrupamento> getClasseJudicialAgrupamentoList() {
		return classeJudicialAgrupamentoList;
	}

	public void setClasseJudicialAgrupamentoList(List<ClasseJudicialAgrupamento> classeAgrupamentoList) {
		this.classeJudicialAgrupamentoList = classeAgrupamentoList;
	}
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "agrupamento")
	public List<AssuntoAgrupamento> getAssuntoAgrupamentoList(){
		return assuntoAgrupamentoList;
	}
	
	public void setAssuntoAgrupamentoList(List<AssuntoAgrupamento> assuntoAgrupamentoList){
		this.assuntoAgrupamentoList = assuntoAgrupamentoList;
	}	

	@Override
	public String toString() {
		return agrupamento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AgrupamentoClasseJudicial> getEntityClass() {
		return AgrupamentoClasseJudicial.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAgrupamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
