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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.PJeEnum;

@Entity
@Table(name = "tb_peso_prevencao")
@org.hibernate.annotations.GenericGenerator(name = "gen_peso_prevencao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_peso_prevencao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PesoPrevencao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<PesoPrevencao,Integer> {

	private static final long serialVersionUID = -8904228857785645903L;

	public enum TipoIntervalo implements PJeEnum {
		E("Entre"), A("Ate"), M("Maior ou igual");

		private String label;

		TipoIntervalo(String label) {
			this.label = label;
		}

		@Override
		public String getLabel() {
			return label;
		}
	}

	private Integer idPesoPrevencao;

	private TipoIntervalo tipoIntervalo;

	private Integer intervaloInicial;

	private Integer intervaloFinal;

	private Double valorPeso;

	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_peso_prevencao")
	@Column(name = "id_peso_prevencao", unique = true, nullable = false)
	public Integer getIdPesoPrevencao() {
		return idPesoPrevencao;
	}

	public void setIdPesoPrevencao(Integer idPesoPrevencao) {
		this.idPesoPrevencao = idPesoPrevencao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_intervalo", nullable = false)
	public TipoIntervalo getTipoIntervalo() {
		return tipoIntervalo;
	}

	public void setTipoIntervalo(TipoIntervalo tipoIntervalo) {
		this.tipoIntervalo = tipoIntervalo;
	}

	@Column(name = "vl_intervalo_inicial")
	public Integer getIntervaloInicial() {
		return intervaloInicial;
	}

	public void setIntervaloInicial(Integer intervaloInicial) {
		this.intervaloInicial = intervaloInicial;
	}

	@Column(name = "vl_intervalo_final")
	public Integer getIntervaloFinal() {
		return intervaloFinal;
	}

	public void setIntervaloFinal(Integer intervaloFinal) {
		this.intervaloFinal = intervaloFinal;
	}

	@Column(name = "vl_peso", nullable = false)
	public Double getValorPeso() {
		return valorPeso;
	}

	public void setValorPeso(Double valorPeso) {
		this.valorPeso = valorPeso;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Transient
	public String getDescricaoIntervalo() {
		String returnValue = "";
		if (tipoIntervalo != null) {
			if (tipoIntervalo == TipoIntervalo.A) {
				returnValue = "Até " + intervaloFinal;
			}
			if (tipoIntervalo == TipoIntervalo.M) {
				returnValue = "Maior ou igual a " + intervaloInicial;
			}
			if (tipoIntervalo == TipoIntervalo.E) {
				returnValue = "Entre " + intervaloInicial + " e " + intervaloFinal;
			}
		}
		return returnValue;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PesoPrevencao> getEntityClass() {
		return PesoPrevencao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPesoPrevencao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
