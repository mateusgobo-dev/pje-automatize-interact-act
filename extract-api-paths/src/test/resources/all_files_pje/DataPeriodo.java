package br.com.infox.cliente.bean;

import java.util.Calendar;
import java.util.Date;

public class DataPeriodo {
	private Calendar dataInicial = Calendar.getInstance();
	private Calendar dataFinal = Calendar.getInstance();

	public DataPeriodo(Date dataInicial, Date dataFinal) {
		this.dataInicial.setTime(dataInicial);
		this.dataFinal.setTime(dataFinal);
	}

	public DataPeriodo(Calendar dataInicial, Calendar dataFinal) {
		this.dataInicial = (Calendar) dataInicial.clone();
		this.dataFinal = (Calendar) dataFinal.clone();
	}

	// FIXME: Melhorar nome. Seria se a data está dentro do periodo não o
	// contrario
	public boolean isBetween(Calendar data) {
		return data.equals(dataInicial) || (data.after(dataInicial) && data.before(dataFinal))
				|| data.equals(dataFinal);
	}

	public Calendar getDataInicial() {
		return (Calendar) dataInicial.clone();
	}

	public Calendar getDataFinal() {
		return (Calendar) dataFinal.clone();
	}
}
