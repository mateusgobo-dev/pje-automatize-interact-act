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
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;

@Entity
@Table(name="tb_pena_individualizada")
@PrimaryKeyJoinColumn(name="id_pena_individualizada")
public class PenaIndividualizada extends Pena {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2303935152018901905L;

	private Integer anosPenaAcrescimo;
	private Integer mesesPenaAcrescimo;
	private Integer diasPenaAcrescimo;
	private Integer horasPenaAcrescimo;

	private Integer anosTotal = 0;
	private Integer mesesTotal = 0;
	private Integer diasTotal = 0;
	private Integer horasTotal = 0;

	@Max(9999)
	@Column(name = "qd_anos_pena_acrescimo")
	public Integer getAnosPenaAcrescimo() {
		return anosPenaAcrescimo;
	}

	public void setAnosPenaAcrescimo(Integer anosPenaAcrescimo) {
		this.anosPenaAcrescimo = anosPenaAcrescimo;
	}

	@Max(11)
	@Column(name = "qd_meses_pena_acrescimo")
	public Integer getMesesPenaAcrescimo() {
		return mesesPenaAcrescimo;
	}

	public void setMesesPenaAcrescimo(Integer mesesPenaAcrescimo) {
		this.mesesPenaAcrescimo = mesesPenaAcrescimo;
	}

	@Max(29)
	@Column(name = "qd_dias_pena_acrescimo")
	public Integer getDiasPenaAcrescimo() {
		return diasPenaAcrescimo;
	}

	public void setDiasPenaAcrescimo(Integer diasPenaAcrescimo) {
		this.diasPenaAcrescimo = diasPenaAcrescimo;
	}

	@Max(23)
	@Column(name = "qd_horas_pena_acrescimo")
	public Integer getHorasPenaAcrescimo() {
		return horasPenaAcrescimo;
	}

	public void setHorasPenaAcrescimo(Integer horasPenaAcrescimo) {
		this.horasPenaAcrescimo = horasPenaAcrescimo;
	}

	@Transient
	public String getDetalheDelito(){
		return "";
	}

	@Transient
	public Integer getAnosTotal() {
		return anosTotal;
	}

	@Transient
	public Integer getMesesTotal() {
		return mesesTotal;
	}

	@Transient
	public Integer getDiasTotal() {
		return diasTotal;
	}

	@Transient
	public Integer getHorasTotal() {
		return horasTotal;
	}

	@Transient
	private void calcularPenaTotal() {
		Integer anosPenaFinal = 0;
		if (getAnosPenaInicial() != null) {
			anosPenaFinal = getAnosPenaInicial();
		}

		if (getAnosPenaAcrescimo() != null) {
			anosPenaFinal += getAnosPenaAcrescimo();
		}

		Integer mesesPenaFinal = 0;
		if (getMesesPenaInicial() != null) {
			mesesPenaFinal = getMesesPenaInicial();
		}

		if (getMesesPenaAcrescimo() != null) {
			mesesPenaFinal += getMesesPenaAcrescimo();
		}

		Integer diasPenaFinal = 0;
		if (getDiasPenaInicial() != null) {
			diasPenaFinal = getDiasPenaInicial();
		}

		if (getDiasPenaAcrescimo() != null) {
			diasPenaFinal += getDiasPenaAcrescimo();
		}

		Integer horasPenaFinal = 0;
		if (getHorasPenaInicial() != null) {
			horasPenaFinal = getHorasPenaInicial();
		}

		if (getHorasPenaAcrescimo() != null) {
			horasPenaFinal += getHorasPenaAcrescimo();
		}

		int anos = 0;
		int meses = 0;
		int dias = 0;
		int horas = 0;

		if (horasPenaFinal > 23) {
			horas = horasPenaFinal % 24;
			dias = horasPenaFinal / 24;
			dias = dias + diasPenaFinal;
		} else {
			dias = diasPenaFinal;
		}

		if (dias > 29) {
			dias = diasPenaFinal % 30;
			meses = diasPenaFinal / 30;
			meses = meses + mesesPenaFinal;
		} else {
			meses = mesesPenaFinal;
		}

		if (meses > 11) {
			meses = (mesesPenaFinal % 12);
			anos = (mesesPenaFinal / 12);
			anos = anos + anosPenaFinal;
		} else {
			anos = anosPenaFinal;
		}

		anosTotal = anos;
		mesesTotal = meses;
		diasTotal = dias;
		horasTotal = horas;
	}

	@Override
	@Transient
	public String getDescricaoTotalPena() {
		calcularPenaTotal();

		String returnValue = "";

		if (getAnosTotal() > 0) {
			returnValue += getAnosTotal() + " Ano(s)";
		}

		if (getMesesTotal() > 0) {
			returnValue += (returnValue.isEmpty() ? "" : ", ") +  getMesesTotal() + " Mese(s)";
		}

		if (getDiasTotal() > 0) {
			returnValue += (returnValue.isEmpty() ? "" : ", ") + getDiasTotal() + " Dia(s)";
		}

		if (getHorasTotal() > 0) {
			returnValue += (returnValue.isEmpty() ? "" : ", ") + getHorasTotal() + " Hora(s)";
		}
		return returnValue;
	}

	@Transient
	@Override
	public Class<? extends Pena> getEntityClass() {
		return PenaIndividualizada.class;
	}
}
