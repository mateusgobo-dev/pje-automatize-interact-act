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
package br.jus.pje.jt.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;

/**
 * @author Rafael Barros / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class VaraItinerante
 * @description Entidade que representa município atendido por vara itinerante
 *              em uma jurisdição
 */
@Entity
@Table(name = VaraItinerante.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_vara_itinerante", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_vara_itinerante"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class VaraItinerante implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<VaraItinerante,Integer> {

	public static final String TABLE_NAME = "tb_vara_itinerante";
	private static final long serialVersionUID = 1L;

	private Integer idVaraItinerante;
	private JurisdicaoMunicipio jurisdicaoMunicipio;

	public VaraItinerante() {
	}

	@Id
	@GeneratedValue(generator = "gen_vara_itinerante")
	@Column(name = "id_vara_itinerante", unique = true, nullable = false)
	public Integer getIdVaraItinerante() {
		return idVaraItinerante;
	}

	public void setIdVaraItinerante(Integer idVaraItinerante) {
		this.idVaraItinerante = idVaraItinerante;
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_jurisdicao_municipio", nullable = false)
	@ForeignKey(name = "fk_tb_vara_itinerante_tb_jurisdicao_municipio")
	@NotNull
	public JurisdicaoMunicipio getJurisdicaoMunicipio() {
		return jurisdicaoMunicipio;
	}

	public void setJurisdicaoMunicipio(JurisdicaoMunicipio jurisdicaoMunucipio) {
		this.jurisdicaoMunicipio = jurisdicaoMunucipio;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdVaraItinerante() == null) ? 0 : getIdVaraItinerante().hashCode());
		result = prime * result + ((getJurisdicaoMunicipio() == null) ? 0 : getJurisdicaoMunicipio().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VaraItinerante))
			return false;
		VaraItinerante other = (VaraItinerante) obj;
		if (getIdVaraItinerante() == null) {
			if (other.getIdVaraItinerante() != null)
				return false;
		} else if (getIdVaraItinerante().equals(other.getIdVaraItinerante()))
			return true;
		if (getJurisdicaoMunicipio() == null) {
			if (other.getJurisdicaoMunicipio() != null)
				return false;
		} else if (!getJurisdicaoMunicipio().equals(other.getJurisdicaoMunicipio()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends VaraItinerante> getEntityClass() {
		return VaraItinerante.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdVaraItinerante();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
