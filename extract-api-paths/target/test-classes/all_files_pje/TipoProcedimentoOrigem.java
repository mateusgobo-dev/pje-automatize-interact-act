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

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "tb_tp_procedimento_origem")
@SequenceGenerator(allocationSize = 1, name = "gen_tp_procedimento_origem", sequenceName = "sq_tb_tp_procedimento_origem")
public class TipoProcedimentoOrigem implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoProcedimentoOrigem,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String dsTipoProcedimento;
	private Boolean ativo;
	private Integer codigoNacional;

	private List<TipoOrigem> tipoOrigemList = new ArrayList<TipoOrigem>(0);

	public TipoProcedimentoOrigem() {

	}

	public TipoProcedimentoOrigem(Integer idTipoProcedimentoOrigem) {
		this.id = idTipoProcedimentoOrigem;
	}
	
	@Id
	@GeneratedValue(generator = "gen_tp_procedimento_origem")
	@Column(name = "id_tipo_procedimento_origem", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer idTipoProcedimentoOrigem) {
		this.id = idTipoProcedimentoOrigem;
	}

	@Column(name = "ds_tipo_procedimento", length = 30)
	@Length(max = 100)
	@NotNull
	public String getDsTipoProcedimento() {
		return dsTipoProcedimento;
	}

	public void setDsTipoProcedimento(String dsTipoProcedimento) {
		this.dsTipoProcedimento = dsTipoProcedimento;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "cd_nacional", nullable = true)
	public Integer getCodigoNacional() {
		return codigoNacional;
	}

	public void setCodigoNacional(Integer codigoNacional) {
		this.codigoNacional = codigoNacional;
	}

	public void setTipoOrigemList(List<TipoOrigem> tipoOrigemList) {
		this.tipoOrigemList = tipoOrigemList;
	}

	@ManyToMany
	@JsonBackReference
	@JoinTable(name = "tb_tp_org_tp_proced_origem", joinColumns = {
			@JoinColumn(name = "id_tipo_procedimento_origem") }, inverseJoinColumns = {
					@JoinColumn(name = "id_tipo_origem") })
	public List<TipoOrigem> getTipoOrigemList() {
		return tipoOrigemList;
	}

	@Override
	public String toString() {
		return getDsTipoProcedimento();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result + ((dsTipoProcedimento == null) ? 0 : dsTipoProcedimento.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoProcedimentoOrigem)) {
			return false;
		}
		if(getId() == null){
			return false;
		}
		TipoProcedimentoOrigem other = (TipoProcedimentoOrigem) obj;
		if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends TipoProcedimentoOrigem> getEntityClass() {
		return TipoProcedimentoOrigem.class;
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
