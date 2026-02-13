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

import br.jus.pje.nucleo.enums.PJeEnum;

@SuppressWarnings("serial")
@Entity
@javax.persistence.Cacheable(true)
@Table(name = DimensaoAlcada.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_dimensao_alcada", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dimensao_alcada"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DimensaoAlcada implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DimensaoAlcada,Integer> {

	public static final String TABLE_NAME = "tb_dimensao_alcada";

	public enum TipoIntervalo implements PJeEnum {
		N("Numérico"), T("Tempo");

		private String label;

		TipoIntervalo(String label) {
			this.label = label;
		}

		@Override
		public String getLabel() {
			return this.label;
		}
	};

	public enum TipoCompetencia {
		C("Cível"), R("Criminal");

		private String label;

		TipoCompetencia(String label) {
			this.label = label;
		}

		public String getLabel() {
			return this.label;
		}
	};

	private Integer idDimensaoAlcada;

	private Double intervaloInicial;

	private Double intervaloFinal;

	private TipoIntervalo tipoIntervalo;

	private TipoCompetencia tipoCompetencia;

	@Id
	@GeneratedValue(generator = "gen_dimensao_alcada")
	@Column(name = "id_dimensao_alcada", unique = true, nullable = false)
	public Integer getIdDimensaoAlcada() {
		return idDimensaoAlcada;
	}

	public void setIdDimensaoAlcada(Integer idDimensaoAlcada) {
		this.idDimensaoAlcada = idDimensaoAlcada;
	}

	@Column(name = "vl_intervalo_inicial")
	public Double getIntervaloInicial() {
		return intervaloInicial;
	}

	public void setIntervaloInicial(Double intervaloInicial) {
		this.intervaloInicial = intervaloInicial;
	}

	@Column(name = "vl_intervalo_final")
	public Double getIntervaloFinal() {
		return intervaloFinal;
	}

	public void setIntervaloFinal(Double intervaloFinal) {
		this.intervaloFinal = intervaloFinal;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_intervalo", nullable = false)
	@NotNull
	public TipoIntervalo getTipoIntervalo() {
		return tipoIntervalo;
	}

	public void setTipoIntervalo(TipoIntervalo tipoIntervalo) {
		this.tipoIntervalo = tipoIntervalo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_competencia", nullable = false)
	@NotNull
	public TipoCompetencia getTipoCompetencia() {
		return tipoCompetencia;
	}

	public void setTipoCompetencia(TipoCompetencia tipoCompetencia) {
		this.tipoCompetencia = tipoCompetencia;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DimensaoAlcada> getEntityClass() {
		return DimensaoAlcada.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdDimensaoAlcada();
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
