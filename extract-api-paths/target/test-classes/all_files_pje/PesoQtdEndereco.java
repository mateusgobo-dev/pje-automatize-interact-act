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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_peso_qtd_endereco")
@org.hibernate.annotations.GenericGenerator(name = "gen_peso_qtd_endereco", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_peso_qtd_endereco"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PesoQtdEndereco implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<PesoQtdEndereco,Integer> {

	private static final long serialVersionUID = -6818150573217003730L;

	private Integer idPesoQtdEndereco;
	private Integer nrEndereco;
	private Double vlPeso;
	private Boolean ativo = Boolean.TRUE;

	@Id
	@GeneratedValue(generator = "gen_peso_qtd_endereco")
	@Column(name = "id_peso_qtd_endereco")
	public Integer getIdPesoQtdEndereco() {
		return idPesoQtdEndereco;
	}

	public void setIdPesoQtdEndereco(Integer idPesoQtdEndereco) {
		this.idPesoQtdEndereco = idPesoQtdEndereco;
	}

	@Column(name = "nr_endereco", nullable = false)
	@NotNull
	public Integer getNrEndereco() {
		return nrEndereco;
	}

	public void setNrEndereco(Integer nrEndereco) {
		this.nrEndereco = nrEndereco;
	}

	@Column(name = "vl_peso", nullable = false)
	@NotNull
	public Double getVlPeso() {
		return vlPeso;
	}

	public void setVlPeso(Double vlPeso) {
		this.vlPeso = vlPeso;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PesoQtdEndereco> getEntityClass() {
		return PesoQtdEndereco.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPesoQtdEndereco();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
