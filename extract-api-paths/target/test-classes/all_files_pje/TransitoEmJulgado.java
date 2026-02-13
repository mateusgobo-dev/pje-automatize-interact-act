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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_transito_julgado")
@org.hibernate.annotations.GenericGenerator(name = "gen_transito_julgado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_transito_julgado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TransitoEmJulgado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TransitoEmJulgado,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Date data;
	private InformacaoCriminalRelevante icr;
	private ProcessoParte processoParte;
	private Boolean ativo;

	public TransitoEmJulgado() {
		
	}

	public TransitoEmJulgado(Integer id) {
		this.id = id;
	}

	@Transient
	public String getDescricaoPessoaNome() {
		if (processoParte != null) {
			return processoParte.getPessoa().getNome();
		}
		return "";
	}

	@Transient
	public String getDescricaoTipoParte() {
		if (processoParte != null) {
			return processoParte.getTipoParte().getTipoParte();
		}
		return "";
	}

	@Id
	@GeneratedValue(generator = "gen_transito_julgado")
	@Column(name = "id_transito_julgado", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_transito", nullable = false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr", nullable = false)
	public InformacaoCriminalRelevante getIcr() {
		return icr;
	}

	public void setIcr(InformacaoCriminalRelevante icr) {
		this.icr = icr;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = true)
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
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
	public Class<? extends TransitoEmJulgado> getEntityClass() {
		return TransitoEmJulgado.class;
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
