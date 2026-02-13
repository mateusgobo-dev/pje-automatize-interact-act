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
@Table(name = ProcessoAlerta.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_processo_trf", "id_alerta" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_alerta", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_alerta"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoAlerta implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoAlerta,Integer> {

	public static final String TABLE_NAME = "tb_processo_alerta";
	private static final long serialVersionUID = 1L;

	private int idProcessoAlerta;
	private ProcessoTrf processoTrf;
	private Alerta alerta;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_processo_alerta")
	@Column(name = "id_processo_alerta", unique = true, nullable = false)
	public int getIdProcessoAlerta() {
		return idProcessoAlerta;
	}

	public void setIdProcessoAlerta(int idProcessoAlerta) {
		this.idProcessoAlerta = idProcessoAlerta;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_alerta", nullable = false)
	@NotNull
	public Alerta getAlerta() {
		return alerta;
	}

	public void setAlerta(Alerta alerta) {
		this.alerta = alerta;
	}

	@Override
	public String toString() {
		return processoTrf.toString();
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoAlerta> getEntityClass() {
		return ProcessoAlerta.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoAlerta());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
