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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Caixa.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_caixa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_caixa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Caixa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Caixa,Integer> {

	public static final String TABLE_NAME = "tb_caixa";

	private static final long serialVersionUID = 1L;

	private int idCaixa;
	private String nomeCaixa;
	private String dsCaixa;
	private Tarefa tarefa;
	private Tarefa tarefaAnterior;
	private List<Processo> processoList = new ArrayList<Processo>(0);
	private Boolean inSistema = Boolean.FALSE;
	
	public Caixa() { }

	@Id
	@GeneratedValue(generator = "gen_caixa")
	@Column(name = "id_caixa", unique = true, nullable = false)
	public int getIdCaixa() {
		return idCaixa;
	}

	public void setIdCaixa(int idCaixa) {
		this.idCaixa = idCaixa;
	}

	@Column(name = "nm_caixa", length = 100)
	@Length(max = 100)
	public String getNomeCaixa() {
		return nomeCaixa;
	}

	public void setNomeCaixa(String nomeCaixa) {
		this.nomeCaixa = nomeCaixa;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_caixa")
	public String getDsCaixa() {
		return dsCaixa;
	}

	public void setDsCaixa(String dsCaixa) {
		this.dsCaixa = dsCaixa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa")
	public Tarefa getTarefa() {
		return tarefa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa_anterior")
	public Tarefa getTarefaAnterior() {
		return tarefaAnterior;
	}

	public void setTarefaAnterior(Tarefa tarefaAnterior) {
		this.tarefaAnterior = tarefaAnterior;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "caixa")
	public List<Processo> getProcessoList() {
		return processoList;
	}

	public void setProcessoList(List<Processo> processoList) {
		this.processoList = processoList;
	}

	@Column(name = "in_sistema", nullable = false)
	@NotNull
	public Boolean getInSistema() {
		return inSistema;
	}

	public void setInSistema(Boolean inSistema) {
		this.inSistema = inSistema;
	}

	@Override
	public String toString() {
		return nomeCaixa;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Caixa)) {
			return false;
		}
		Caixa other = (Caixa) obj;
		if (getIdCaixa() != other.getIdCaixa()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCaixa();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Caixa> getEntityClass() {
		return Caixa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCaixa());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
