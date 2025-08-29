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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = "tb_mandado_alvara")
@PrimaryKeyJoinColumn(name = "id_mandado_alvara")
public class MandadoAlvara extends ProcessoExpedienteCriminal {

	private static final long serialVersionUID = 1L;

	private Date dataDelito;
	private Date dataCumprimento;
	private Boolean inDataDelitoDesconhecida;
	private String observacoesCumprimento;
	private AssuntoTrf assuntoPrincipal;
	private List<ProcessoProcedimentoOrigem> processoProcedimentoOrigemList = new ArrayList<ProcessoProcedimentoOrigem>(
			0);
	private List<ProcessoEvento> processoEventoList = new ArrayList<ProcessoEvento>(0);

	@Column(name = "dt_delito")
	public Date getDataDelito() {
		return dataDelito;
	}

	public void setDataDelito(Date dataDelito) {
		this.dataDelito = dataDelito;
	}

	@Column(name = "dt_cumprimento")
	public Date getDataCumprimento() {
		return dataCumprimento;
	}

	public void setDataCumprimento(Date dataCumprimento) {
		this.dataCumprimento = dataCumprimento;
	}

	@Column(name = "in_dt_delito_desconhecida")
	public Boolean getInDataDelitoDesconhecida() {
		return inDataDelitoDesconhecida;
	}

	public void setInDataDelitoDesconhecida(Boolean inDataDelitoDesconhecida) {
		this.inDataDelitoDesconhecida = inDataDelitoDesconhecida;
	}

	@Length(max = 500)
	@Column(name = "ds_obs")
	public String getObservacoesCumprimento() {
		return observacoesCumprimento;
	}

	public void setObservacoesCumprimento(String observacoesCumprimento) {
		this.observacoesCumprimento = observacoesCumprimento;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_assunto_trf", nullable = false)
	public AssuntoTrf getAssuntoPrincipal() {
		return assuntoPrincipal;
	}

	public void setAssuntoPrincipal(AssuntoTrf assuntoPrincipal) {
		this.assuntoPrincipal = assuntoPrincipal;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_mndo_alvara_proc_origem", joinColumns = { @JoinColumn(name = "id_mandado_alvara", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_proc_origem", nullable = false, updatable = false) })
	public List<ProcessoProcedimentoOrigem> getProcessoProcedimentoOrigemList() {
		return processoProcedimentoOrigemList;
	}

	public void setProcessoProcedimentoOrigemList(List<ProcessoProcedimentoOrigem> processoProcedimentoOrigemList) {
		this.processoProcedimentoOrigemList = processoProcedimentoOrigemList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_mndo_alvara_proc_evento", joinColumns = { @JoinColumn(name = "id_mandado_alvara", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_evento", nullable = false, updatable = false) })
	public List<ProcessoEvento> getProcessoEventoList() {
		return processoEventoList;
	}

	public void setProcessoEventoList(List<ProcessoEvento> processoEventoList) {
		this.processoEventoList = processoEventoList;
	}

	@Override
	@Transient
	public Class<? extends ProcessoExpedienteCriminal> getEntityClass() {
		return MandadoAlvara.class;
	}
}
