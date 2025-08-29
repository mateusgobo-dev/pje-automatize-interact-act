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

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.enums.TipoParteEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_tipo_parte")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class TipoParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoParte,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoParte;
	private String tipoParte;
	private Boolean ativo;
	private Boolean tipoPrincipal;
	private TipoParteEnum tipoPoloMNI;

	public TipoParte() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_parte")
	@Column(name = "id_tipo_parte", unique = true, nullable = false)
	public int getIdTipoParte() {
		return this.idTipoParte;
	}

	public void setIdTipoParte(int idTipoParte) {
		this.idTipoParte = idTipoParte;
	}

	@Column(name = "ds_tipo_parte", unique = true, nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getTipoParte() {
		return this.tipoParte;
	}

	public void setTipoParte(String tipoParte) {
		this.tipoParte = tipoParte;
		if (this.tipoParte !=null) 
			this.tipoParte = this.tipoParte.toUpperCase();
	}


	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return tipoParte;
	}
	
	@Column(name = "in_tipo_principal")
	public Boolean getTipoPrincipal() {
		return tipoPrincipal;
	}

	public void setTipoPrincipal(Boolean inTipoPrincipal) {
		this.tipoPrincipal = inTipoPrincipal;
	}
	

	@Column(name = "in_tipo_polo_mni", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoParteEnum getTipoPoloMNI() {
		return tipoPoloMNI;
	}

	public void setTipoPoloMNI(TipoParteEnum tipoPoloMNI) {
		this.tipoPoloMNI = tipoPoloMNI;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoParte)) {
			return false;
		}
		TipoParte other = (TipoParte) obj;
		if (getIdTipoParte() != other.getIdTipoParte()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoParte();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoParte> getEntityClass() {
		return TipoParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoParte());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
