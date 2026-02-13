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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Entity
@Table(name = "tb_processo_peso_parte")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_peso_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_peso_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoPesoParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoPesoParte,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoPesoParte;
	private Integer numeroPartesInicial;
	private Integer numeroPartesFinal;
	private ProcessoParteParticipacaoEnum inPolo;
	private Double valorPeso;

	public ProcessoPesoParte() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_peso_parte")
	@Column(name = "id_processo_peso_parte", unique = true, nullable = false)
	public Integer getIdProcessoPesoParte() {
		return idProcessoPesoParte;
	}

	public void setIdProcessoPesoParte(Integer idProcessoPesoParte) {
		this.idProcessoPesoParte = idProcessoPesoParte;
	}

	@Column(name = "in_polo", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoParteParticipacaoEnum getInPolo() {
		return inPolo;
	}

	public void setInPolo(ProcessoParteParticipacaoEnum inPolo) {
		this.inPolo = inPolo;
	}

	@Column(name = "nr_partes_inicial", nullable = false)
	@NotNull
	public Integer getNumeroPartesInicial() {
		return this.numeroPartesInicial;
	}

	public void setNumeroPartesInicial(Integer numeroPartesInicial) {
		this.numeroPartesInicial = numeroPartesInicial;
	}

	@Column(name = "nr_partes_final")
	public Integer getNumeroPartesFinal() {
		return this.numeroPartesFinal;
	}

	public void setNumeroPartesFinal(Integer quantidadePartesFinal) {
		this.numeroPartesFinal = quantidadePartesFinal;
	}

	@Column(name = "vl_peso")
	public Double getValorPeso() {
		return this.valorPeso;
	}

	public void setValorPeso(Double valorPeso) {
		this.valorPeso = valorPeso;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoPesoParte> getEntityClass() {
		return ProcessoPesoParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoPesoParte();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
