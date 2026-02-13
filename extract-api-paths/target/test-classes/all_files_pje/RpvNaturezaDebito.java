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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = RpvNaturezaDebito.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv_natureza_debito", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv_natureza_debito"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RpvNaturezaDebito implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RpvNaturezaDebito,Integer> {

	public static final String TABLE_NAME = "tb_rpv_natureza_debito";
	private static final long serialVersionUID = 1L;

	private int idRpvNaturezaDebito;
	private String rpvNaturezaDebito;
	private String codigoTipoDocumento;
	private String codigoRpvNaturezaDebito;

	public RpvNaturezaDebito() {
	}

	@Id
	@GeneratedValue(generator = "gen_rpv_natureza_debito")
	@Column(name = "id_rpv_natureza_debito", unique = true, nullable = false)
	public int getIdRpvNaturezaDebito() {
		return this.idRpvNaturezaDebito;
	}

	public void setIdRpvNaturezaDebito(int idRpvNaturezaDebito) {
		this.idRpvNaturezaDebito = idRpvNaturezaDebito;
	}

	@Column(name = "ds_rpv_natureza_debito", length = 200, nullable = false)
	@Length(max = 200)
	@NotNull
	public String getRpvNaturezaDebito() {
		return rpvNaturezaDebito;
	}

	public void setRpvNaturezaDebito(String rpvNaturezaDebito) {
		this.rpvNaturezaDebito = rpvNaturezaDebito;
	}

	@Column(name = "cd_tipo_documento", length = 1, nullable = false)
	@Length(max = 1)
	@NotNull
	public String getCodigoTipoDocumento() {
		return codigoTipoDocumento;
	}

	public void setCodigoTipoDocumento(String codigoTipoDocumento) {
		this.codigoTipoDocumento = codigoTipoDocumento;
	}

	@Column(name = "cd_rpv_natureza_debito", length = 30, nullable = false)
	@Length(max = 30)
	@NotNull
	public String getCodigoRpvNaturezaDebito() {
		return codigoRpvNaturezaDebito;
	}

	public void setCodigoRpvNaturezaDebito(String codigoRpvNaturezaDebito) {
		this.codigoRpvNaturezaDebito = codigoRpvNaturezaDebito;
	}

	@Override
	public String toString() {
		return rpvNaturezaDebito;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RpvNaturezaDebito)) {
			return false;
		}
		RpvNaturezaDebito other = (RpvNaturezaDebito) obj;
		if (getIdRpvNaturezaDebito() != other.getIdRpvNaturezaDebito()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpvNaturezaDebito();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RpvNaturezaDebito> getEntityClass() {
		return RpvNaturezaDebito.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpvNaturezaDebito());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
