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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
@Entity
@Table(name = DistanciaMaximaDistribuicao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_distancia_maxima_dist", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_distancia_maxima_dist"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DistanciaMaximaDistribuicao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DistanciaMaximaDistribuicao,Integer> {

	public static final String TABLE_NAME = "tb_distancia_maxima_dist";

	private Integer idDistancia;

	private Integer intervaloInicial;

	private Integer intervaloFinal;

	private Double distanciaMaxima;

	private Double maiorPesoProcesso = 150.0;

	@Id
	@GeneratedValue(generator = "gen_distancia_maxima_dist")
	@Column(name = "id_distancia_maxima", unique = true, nullable = false)
	public Integer getIdDistancia() {
		return idDistancia;
	}

	public void setIdDistancia(Integer idDistancia) {
		this.idDistancia = idDistancia;
	}

	@Column(name = "vl_intervalo_inicial", nullable = false)
	@NotNull
	public Integer getIntervaloInicial() {
		return intervaloInicial;
	}

	public void setIntervaloInicial(Integer intervaloInicial) {
		this.intervaloInicial = intervaloInicial;
	}

	@Column(name = "vl_intervalo_final", nullable = false)
	@NotNull
	public Integer getIntervaloFinal() {
		return intervaloFinal;
	}

	public void setIntervaloFinal(Integer intervaloFinal) {
		this.intervaloFinal = intervaloFinal;
	}

	@Column(name = "vl_distancia_maxima", nullable = false)
	@NotNull
	public Double getDistanciaMaxima() {
		return distanciaMaxima;
	}

	public void setDistanciaMaxima(Double distanciaMaxima) {
		this.distanciaMaxima = distanciaMaxima;
	}

	@Transient
	public Double getMaiorPesoProcesso() {
		return maiorPesoProcesso;
	}

	public void setMaiorPesoProcesso(Double maiorPesoProcesso) {
		this.maiorPesoProcesso = maiorPesoProcesso;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idDistancia == null) ? 0 : idDistancia.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DistanciaMaximaDistribuicao))
			return false;
		DistanciaMaximaDistribuicao other = (DistanciaMaximaDistribuicao) obj;
		if (getIdDistancia() == null) {
			if (other.getIdDistancia() != null)
				return false;
		} else if (!getIdDistancia().equals(other.getIdDistancia()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DistanciaMaximaDistribuicao> getEntityClass() {
		return DistanciaMaximaDistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdDistancia();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
