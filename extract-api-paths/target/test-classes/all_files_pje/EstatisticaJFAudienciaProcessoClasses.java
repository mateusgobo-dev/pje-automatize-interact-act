package br.com.infox.pje.bean;

import br.jus.pje.nucleo.entidades.ClasseJudicial;

/**
 * 
 * @author Wilson
 * 
 */
public class EstatisticaJFAudienciaProcessoClasses {
	private ClasseJudicial classe;
	private int totalEventoRemanescente;
	private int totalEventoConclusosSentenca;
	private int totalEventoDevolvidosSentenca;
	private int totalEventoConvertidosDiligencia;

	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	public int getTotalEventoRemanescente() {
		return totalEventoRemanescente;
	}

	public void setTotalEventoRemanescente(int totalEventoRemanescente) {
		this.totalEventoRemanescente = totalEventoRemanescente;
	}

	public int getTotalEventoConclusosSentenca() {
		return totalEventoConclusosSentenca;
	}

	public void setTotalEventoConclusosSentenca(int totalEventoConclusosSentenca) {
		this.totalEventoConclusosSentenca = totalEventoConclusosSentenca;
	}

	public int getTotalEventoDevolvidosSentenca() {
		return totalEventoDevolvidosSentenca;
	}

	public void setTotalEventoDevolvidosSentenca(int totalEventoDevolvidosSentenca) {
		this.totalEventoDevolvidosSentenca = totalEventoDevolvidosSentenca;
	}

	public int getTotalEventoConvertidosDiligencia() {
		return totalEventoConvertidosDiligencia;
	}

	public void setTotalEventoConvertidosDiligencia(int totalEventoConvertidosDiligencia) {
		this.totalEventoConvertidosDiligencia = totalEventoConvertidosDiligencia;
	}
}