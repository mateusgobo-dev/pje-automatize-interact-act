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
@Table(name = "tb_multa_pena_privativa")
@org.hibernate.annotations.GenericGenerator(name = "gen_multa_pena_privativa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_multa_pena_privativa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MultaPenaPrivativa implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MultaPenaPrivativa,Integer> {
	
	public static final MultaPenaPrivativa ISOLADA = new MultaPenaPrivativa(2);
	public static final MultaPenaPrivativa CUMULATIVA = new MultaPenaPrivativa(3);
	public static final MultaPenaPrivativa ALTERNATIVA = new MultaPenaPrivativa(3);

	private static final long serialVersionUID = 1L;

	private Integer idMultaPenaPrivativa;
	private String dsMultaPenaPrivativa;
	private Boolean inAtivo;

	public MultaPenaPrivativa() {

	}

	public MultaPenaPrivativa(Integer id) {
		this.idMultaPenaPrivativa = id;
	}

	// GETTER'S AND SETTER'S
	@Id
	@GeneratedValue(generator = "gen_multa_pena_privativa")
	@Column(name = "id_multa_pena_privativa", unique = true, nullable = false)
	public Integer getIdMultaPenaPrivativa() {
		return idMultaPenaPrivativa;
	}

	public void setIdMultaPenaPrivativa(Integer idMultaPenaPrivativa) {
		this.idMultaPenaPrivativa = idMultaPenaPrivativa;
	}

	@Column(name = "ds_multa_pena_privativa", nullable = false)
	@NotNull
	public String getDsMultaPenaPrivativa() {
		return dsMultaPenaPrivativa;
	}

	public void setDsMultaPenaPrivativa(String dsMultaPenaPrivativa) {
		this.dsMultaPenaPrivativa = dsMultaPenaPrivativa;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getInAtivo() {
		return inAtivo;
	}

	public void setInAtivo(Boolean inAtivo) {
		this.inAtivo = inAtivo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDsMultaPenaPrivativa() == null) ? 0 : dsMultaPenaPrivativa.hashCode());
		result = prime * result + ((getIdMultaPenaPrivativa() == null) ? 0 : idMultaPenaPrivativa.hashCode());
		result = prime * result + ((getInAtivo() == null) ? 0 : inAtivo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MultaPenaPrivativa))
			return false;
		MultaPenaPrivativa other = (MultaPenaPrivativa) obj;
		if (getDsMultaPenaPrivativa() == null) {
			if (other.getDsMultaPenaPrivativa() != null)
				return false;
		} else if (!dsMultaPenaPrivativa.equals(other.getDsMultaPenaPrivativa()))
			return false;
		if (getIdMultaPenaPrivativa() == null) {
			if (other.getIdMultaPenaPrivativa() != null)
				return false;
		} else if (!idMultaPenaPrivativa.equals(other.getIdMultaPenaPrivativa()))
			return false;
		if (getInAtivo() == null) {
			if (other.getInAtivo() != null)
				return false;
		} else if (!inAtivo.equals(other.getInAtivo()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MultaPenaPrivativa> getEntityClass() {
		return MultaPenaPrivativa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdMultaPenaPrivativa();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
