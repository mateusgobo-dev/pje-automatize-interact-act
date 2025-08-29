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

@Entity
@Table(name = "tb_proc_assnto_antecedente")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_assunto_antecedente", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_assunto_antecedente"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoAssuntoAntecedente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoAssuntoAntecedente,Integer> {

	private static final long serialVersionUID = 1L;

	private int id;
	private ProcessoAssunto processoAssunto;
	private AssuntoTrf assuntoTrf;

	@Id
	@GeneratedValue(generator = "gen_proc_assunto_antecedente")
	@Column(name = "id_proc_assunto_antecedente", unique = true, nullable = false)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_assunto")
	public ProcessoAssunto getProcessoAssunto() {
		return processoAssunto;
	}

	public void setProcessoAssunto(ProcessoAssunto processoAssunto) {
		this.processoAssunto = processoAssunto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_trf")
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoAssuntoAntecedente> getEntityClass() {
		return ProcessoAssuntoAntecedente.class;
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
