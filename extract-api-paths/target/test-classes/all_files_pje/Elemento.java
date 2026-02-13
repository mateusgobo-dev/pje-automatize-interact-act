package br.com.infox.trf.distribuicao;

import java.util.ArrayList;
import java.util.List;

public class Elemento<T> {
	private T objeto;
	private long peso;
	private int quantidadeTotal;

	protected long pesoPercent;
	protected List<Integer> numerosSorteio;

	public Elemento(T objeto, long peso, int quantidadeTotal) {
		this.objeto = objeto;
		this.peso = peso;
		this.setQuantidadeTotal(quantidadeTotal);
		numerosSorteio = new ArrayList<Integer>();
	}

	public long getPeso() {
		return peso;
	}

	public void setPeso(long peso) {
		this.peso = peso;
	}

	public T getObjeto() {
		return objeto;
	}

	@Override
	public String toString() {
		return objeto.toString() + " - " + pesoPercent;
	}

	public void setQuantidadeTotal(int quantidadeTotal) {
		this.quantidadeTotal = quantidadeTotal;
	}

	public int getQuantidadeTotal() {
		return quantidadeTotal;
	}
}