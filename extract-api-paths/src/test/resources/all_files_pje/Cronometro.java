package br.jus.pje.nucleo.util;

public class Cronometro {

	private long inicio = 0;

	public Cronometro() {
		inicio = System.currentTimeMillis();
	}

	public int getAtual() {
		long mili = System.currentTimeMillis() - inicio;
		return (int) Math.round(mili / 1000.0);
	}
}