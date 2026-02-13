/**
 * 
 */
package br.jus.cnj.pje.servicos.prazos;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author cristof
 * 
 */
public class Feriado {

	private boolean suspendePrazo;

	private boolean intervalo;

	private boolean anual;

	private GregorianCalendar dataInicial;

	private GregorianCalendar dataFinal;

	private GregorianCalendar dataParadigma;

	public Feriado(Integer diaInicio, Integer mesInicio, Integer anoInicio, Integer diaFim, Integer mesFim,
			Integer anoFim, boolean suspendePrazo) {
		this.suspendePrazo = suspendePrazo;
		anual = false;
		if (anoInicio == null && anoFim == null) {
			anual = true;
		}
		GregorianCalendar data = new GregorianCalendar();
		data.setTime(new Date());
		dataInicial = new GregorianCalendar(anual ? data.get(GregorianCalendar.YEAR) : anoInicio,
				mesInicio.intValue() - 1, diaInicio);
		if (diaFim == null && mesFim == null) { // não é intervalo de datas
			intervalo = false;
			dataFinal = null;
		} else {
			intervalo = true;
			dataFinal = new GregorianCalendar(anual ? data.get(GregorianCalendar.YEAR) : anoFim, mesFim.intValue() - 1,
					diaFim);
			this.setDataParadigma(data);
		}
	}

	public GregorianCalendar getDataParadigma() {
		return dataParadigma;
	}

	public void setDataParadigma(GregorianCalendar dataParadigma) {
		this.dataParadigma = dataParadigma;
		if (anual) {
			int year = dataParadigma.get(GregorianCalendar.YEAR);
			dataInicial.set(GregorianCalendar.YEAR, year);
			if (intervalo) {
				dataFinal.set(GregorianCalendar.YEAR, year);
				if (dataFinal.compareTo(dataInicial) < 0) {
					if (dataParadigma.get(GregorianCalendar.MONTH) <= dataFinal.get(GregorianCalendar.MONTH)) {
						dataInicial.add(GregorianCalendar.YEAR, -1);
					} else {
						dataFinal.add(GregorianCalendar.YEAR, 1);
					}
				}
				// Issue 18907 - Caso a data paradigma fosse a mesma da data final, porém com preenchimento de horas, o ano ficava incorreto
				GregorianCalendar dataFinalComHora = new GregorianCalendar(dataFinal.get(GregorianCalendar.YEAR), dataFinal.get(GregorianCalendar.MONTH), dataFinal.get(GregorianCalendar.DAY_OF_MONTH), 23, 59, 59) ;
				if (dataFinalComHora.compareTo(dataParadigma) < 0) {
					dataInicial.add(GregorianCalendar.YEAR, 1);
					dataFinal.add(GregorianCalendar.YEAR, 1);
				}
			} else {
				if (dataInicial.compareTo(dataParadigma) < 0) {
					dataInicial.add(GregorianCalendar.YEAR, 1);
				}
			}
		}
	}

	public boolean isSuspendePrazo() {
		return suspendePrazo;
	}

	public GregorianCalendar getDataInicial() {
		return dataInicial;
	}

	public GregorianCalendar getDataFinal() {
		return dataFinal;
	}

	public boolean isIntervalo() {
		return intervalo;
	}

	public boolean isAnual() {
		return anual;
	}

	public boolean estaNesteFeriado(GregorianCalendar data) {
		this.setDataParadigma(data);
		GregorianCalendar d = new GregorianCalendar(data.get(GregorianCalendar.YEAR),
				data.get(GregorianCalendar.MONTH), data.get(GregorianCalendar.DAY_OF_MONTH));
		GregorianCalendar dInicial = new GregorianCalendar(dataInicial.get(GregorianCalendar.YEAR),
				dataInicial.get(GregorianCalendar.MONTH), dataInicial.get(GregorianCalendar.DAY_OF_MONTH));
		if (intervalo) {
			GregorianCalendar dFinal = new GregorianCalendar(dataFinal.get(GregorianCalendar.YEAR),
					dataFinal.get(GregorianCalendar.MONTH), dataFinal.get(GregorianCalendar.DAY_OF_MONTH));
			if (d.compareTo(dInicial) >= 0 && d.compareTo(dFinal) <= 0) {
				return true;
			}
		} else {
			if (d.compareTo(dInicial) == 0) {
				return true;
			}
		}
		return false;
	}

	public long diasIntercessao(Feriado f) {
		GregorianCalendar dInicial = new GregorianCalendar(dataInicial.get(GregorianCalendar.YEAR),
				dataInicial.get(GregorianCalendar.MONTH), dataInicial.get(GregorianCalendar.DAY_OF_MONTH));
		GregorianCalendar fInicial = new GregorianCalendar(f.dataInicial.get(GregorianCalendar.YEAR),
				f.dataInicial.get(GregorianCalendar.MONTH), f.dataInicial.get(GregorianCalendar.DAY_OF_MONTH));
		GregorianCalendar dFinal = null;
		GregorianCalendar fFinal = null;
		if (this.intervalo) {
			dFinal = new GregorianCalendar(dataFinal.get(GregorianCalendar.YEAR),
					dataFinal.get(GregorianCalendar.MONTH), dataFinal.get(GregorianCalendar.DAY_OF_MONTH));
		}
		if (f.intervalo) {
			fFinal = new GregorianCalendar(f.dataFinal.get(GregorianCalendar.YEAR),
					f.dataFinal.get(GregorianCalendar.MONTH), f.dataFinal.get(GregorianCalendar.DAY_OF_MONTH));
		}
		if (this.intervalo && f.intervalo) {
			if (dInicial.compareTo(fFinal) > 0 || dFinal.compareTo(fInicial) < 0) {
				return 0;
			} else { // há intercessão
				GregorianCalendar dataMenor = null;
				GregorianCalendar dataMaior = null;
				if (dInicial.compareTo(fInicial) <= 0) {
					dataMenor = fInicial;
				} else {
					dataMenor = dInicial;
				}
				if (dFinal.compareTo(fFinal) <= 0) {
					dataMaior = dFinal;
				} else {
					dataMaior = fFinal;
				}
				return (long) Math
						.ceil((((double) dataMaior.getTimeInMillis() - (double) dataMenor.getTimeInMillis()) / (1000D * 60D * 60D * 24D)) + 1D);
			}
		} else if (this.intervalo && !f.intervalo) {
			if (fInicial.compareTo(dInicial) < 0 || fInicial.compareTo(dFinal) > 0) {
				return 0;
			} else { // há intercessão
				return 1;
			}
		} else if (!this.intervalo && f.intervalo) {
			if (dInicial.compareTo(fInicial) < 0 || dInicial.compareTo(fFinal) > 0) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (dInicial.compareTo(fInicial) == 0) {
				return 1;
			}
		}
		return 0;
	}

	public long diasSuspensos(GregorianCalendar dataInicial, GregorianCalendar dataFinal) {
		long diasSuspensos = 0;
		this.setDataParadigma(dataInicial);
		if (this.suspendePrazo) {
			if (this.intervalo) {
				if (this.dataInicial.compareTo(dataFinal) > 0 || this.dataFinal.compareTo(dataInicial) < 0) {
					diasSuspensos = 0;
				} else {
					GregorianCalendar dataMenor = null;
					GregorianCalendar dataMaior = null;
					if (this.dataInicial.compareTo(dataInicial) <= 0) {
						dataMenor = dataInicial;
					} else {
						dataMenor = this.dataInicial;
					}
					if (this.dataFinal.compareTo(dataFinal) <= 0) {
						dataMaior = this.dataFinal;
					} else {
						dataMaior = dataFinal;
					}
					diasSuspensos = (long) Math.ceil(((((double) dataMaior.getTimeInMillis() - (double) dataMenor
							.getTimeInMillis()) / (1000D * 60D * 60D * 24D)) + 1D));
				}
			} else {
				if (this.dataInicial.compareTo(dataInicial) >= 0 || this.dataInicial.compareTo(dataFinal) <= 0) {
					diasSuspensos = 1;
				}
			}
		}
		return diasSuspensos;
	}

	public long diasSuspensos() {
		long intervaloDias = 0;
		if (this.suspendePrazo) {
			if (this.intervalo) {
				intervaloDias = (long) Math.ceil(((double) dataFinal.getTimeInMillis() - (double) dataInicial
						.getTimeInMillis()) / (1000D * 60D * 60D * 24D) + 1D);
			} else {
				intervaloDias = 1;
			}
		}
		return intervaloDias;
	}

	@Override
	public String toString() {
		return "Feriado [suspendePrazo="
				+ suspendePrazo
				+ ", intervalo="
				+ intervalo
				+ ", anual="
				+ anual
				+ ", dataInicial="
				+ dataInicial.get(GregorianCalendar.YEAR)
				+ "-"
				+ (dataInicial.get(GregorianCalendar.MONTH) + 1)
				+ "-"
				+ dataInicial.get(GregorianCalendar.DAY_OF_MONTH)
				+ ", dataFinal="
				+ (dataFinal == null ? "null" : dataFinal.get(GregorianCalendar.YEAR) + "-"
						+ (dataFinal.get(GregorianCalendar.MONTH) + 1) + "-"
						+ dataFinal.get(GregorianCalendar.DAY_OF_MONTH)) + "]";
	}

}
