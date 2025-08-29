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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table(name = "tb_tipo_origem")
@SequenceGenerator(allocationSize = 1, name = "gen_tipo_origem", sequenceName = "sq_tb_tipo_origem")
public class TipoOrigem implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoOrigem,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String dsTipoOrigem;
	private Boolean inObrigatorioNumeroOrigem;
	private Boolean ativo;
	private Integer codigoNacional;
	private List<TipoProcedimentoOrigem> tipoProcedimentoOrigemList = new ArrayList<TipoProcedimentoOrigem>(0);

	public TipoOrigem() {
		
	}

	public TipoOrigem(Integer id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_origem")
	@Column(name = "id_tipo_origem", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "ds_tipo_origem", nullable = false)
	@NotNull
	@Length(max=100)
	public String getDsTipoOrigem() {
		return dsTipoOrigem;
	}

	public void setDsTipoOrigem(String dsTipoOrigem) {
		this.dsTipoOrigem = dsTipoOrigem;
	}

	@Column(name = "in_obrigatorio_numero_origem", nullable = false)
	@NotNull
	public Boolean getInObrigatorioNumeroOrigem() {
		return inObrigatorioNumeroOrigem;
	}

	public void setInObrigatorioNumeroOrigem(Boolean inObrigatorioNumeroOrigem) {
		this.inObrigatorioNumeroOrigem = inObrigatorioNumeroOrigem;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "cd_nacional", nullable = false)
	public Integer getCodigoNacional() {
		return codigoNacional;
	}

	public void setCodigoNacional(Integer codigoNacional) {
		this.codigoNacional = codigoNacional;
	}

	@ManyToMany
	@JoinTable(name = "tb_tp_org_tp_proced_origem", joinColumns = { @JoinColumn(name = "id_tipo_origem") }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_procedimento_origem") })
	public List<TipoProcedimentoOrigem> getTipoProcedimentoOrigemList() {
		return tipoProcedimentoOrigemList;
	}

	public void setTipoProcedimentoOrigemList(List<TipoProcedimentoOrigem> tipoProcedimentoOrigemList) {
		this.tipoProcedimentoOrigemList = tipoProcedimentoOrigemList;
	}

	public String toString() {
		return this.getDsTipoOrigem();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoOrigem> getEntityClass() {
		return TipoOrigem.class;
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
