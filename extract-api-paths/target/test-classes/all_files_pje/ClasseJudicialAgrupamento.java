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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * @author thiago.vieira
 */
@Entity
@Table(name = ClasseJudicialAgrupamento.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_agrupamento", "id_classe_judicial" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_agrupamento_classes", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_agrupamento_classes"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ClasseJudicialAgrupamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ClasseJudicialAgrupamento,Integer> {

	public static final String TABLE_NAME = "tb_agrupamento_classes";
	private static final long serialVersionUID = 1L;

	private int idAgrupamentoClasses;
	private AgrupamentoClasseJudicial agrupamento;
	private ClasseJudicial classe;

	@Id
	@GeneratedValue(generator = "gen_agrupamento_classes")
	@Column(name = "id_agrupamento_classes", unique = true, nullable = false)
	public int getIdAgrupamentoClasses() {
		return idAgrupamentoClasses;
	}

	public void setIdAgrupamentoClasses(int idAgrupamentoClasses) {
		this.idAgrupamentoClasses = idAgrupamentoClasses;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_agrupamento", nullable = false)
	@NotNull
	public AgrupamentoClasseJudicial getAgrupamento() {
		return agrupamento;
	}

	public void setAgrupamento(AgrupamentoClasseJudicial agrupamento) {
		this.agrupamento = agrupamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	@Override
	public String toString() {
		return agrupamento.toString();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ClasseJudicialAgrupamento> getEntityClass() {
		return ClasseJudicialAgrupamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAgrupamentoClasses());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
