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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoNaturezaCletEnum;

@Entity
@Table(name = "tb_natureza_clet")
@org.hibernate.annotations.GenericGenerator(name = "gen_natureza_clet", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_natureza_clet"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class NaturezaClet implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<NaturezaClet,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idNaturezaClet;
	private TipoNaturezaCletEnum tipoNatureza;
	private String dsNatureza;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_natureza_clet")
	@NotNull
	@Column(name = "id_natureza_clet")
	public Integer getIdNaturezaClet() {
		return idNaturezaClet;
	}

	public void setIdNaturezaClet(Integer idNaturezaClet) {
		this.idNaturezaClet = idNaturezaClet;
	}

	@Column(name = "tp_natureza")
	@NotNull
	@Enumerated(EnumType.STRING)
	public TipoNaturezaCletEnum getTipoNatureza() {
		return tipoNatureza;
	}

	public void setTipoNatureza(TipoNaturezaCletEnum tipoNatureza) {
		this.tipoNatureza = tipoNatureza;
	}

	@Column(name = "ds_natureza")
	@NotNull
	public String getDsNatureza() {
		return dsNatureza;
	}

	public void setDsNatureza(String dsNatureza) {
		this.dsNatureza = dsNatureza;
	}

	@Column(name = "in_ativo")
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return dsNatureza;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idNaturezaClet == null) ? 0 : idNaturezaClet.hashCode());
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
		if (!(obj instanceof NaturezaClet)) {
			return false;
		}
		NaturezaClet other = (NaturezaClet) obj;
		if (idNaturezaClet == null) {
			if (other.idNaturezaClet != null) {
				return false;
			}
		} else if (!idNaturezaClet.equals(other.idNaturezaClet)) {
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends NaturezaClet> getEntityClass() {
		return NaturezaClet.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdNaturezaClet();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
