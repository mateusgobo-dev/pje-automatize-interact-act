package br.com.infox.trf.distribuicao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sorteio<T> {

	private List<Elemento<T>> elementos;
	private static final int PERCENT_FACTOR = 1000;
	private int qtPrefenciaSorteio = 5;
	private int qtMenor;
	private int totalPesoPercent = 0;

	public Sorteio(List<Elemento<T>> elementos) {
		this.elementos = elementos;
		generatePesoPercent();
	}

	private void generatePesoPercent() {
		long totalPesos = 0;
		long pesoMaximo = 0;
		qtMenor = Integer.MAX_VALUE;
		for (Elemento<T> elemento : elementos) {
			pesoMaximo = elemento.getPeso() > pesoMaximo ? elemento.getPeso() : pesoMaximo;
			qtMenor = elemento.getQuantidadeTotal() < qtMenor ? elemento.getQuantidadeTotal() : qtMenor;
		}
		for (Elemento<T> elemento : elementos) {
			elemento.setPeso(pesoMaximo - elemento.getPeso() + 1);
		}
		for (Elemento<T> elemento : elementos) {
			totalPesos += elemento.getPeso();
		}
		for (Elemento<T> elemento : elementos) {
			elemento.pesoPercent = (int) ((elemento.getPeso() / (float) totalPesos) * PERCENT_FACTOR);
			elemento.pesoPercent = elemento.pesoPercent == 0 ? 1 : elemento.pesoPercent;
		}
		for (Elemento<T> elemento : elementos) {
			totalPesoPercent += elemento.pesoPercent;
		}
		List<Integer> listRandon = getListRandon(totalPesoPercent);
		int i = 0;
		for (Elemento<T> elemento : elementos) {
			while (elemento.numerosSorteio.size() < elemento.pesoPercent) {
				elemento.numerosSorteio.add(listRandon.get(i));
				i++;
			}
		}
	}

	private Elemento<T> sortear(int tentativas) {
		tentativas++;
		Random random = new Random();
		int numero = random.nextInt(totalPesoPercent);
		for (Elemento<T> elemento : elementos) {
			if (elemento.numerosSorteio.contains(numero)) {
				if (tentativas >= 5 || isDentroLimite(elemento)) {
					return elemento;
				}
			}
		}
		return sortear(tentativas);
	}

	private boolean isDentroLimite(Elemento<T> elemento) {
		return elemento.getQuantidadeTotal() < qtMenor + qtPrefenciaSorteio;
	}

	public Elemento<T> sortearElemento() {
		int nListaSorteio = 20;
		List<Elemento<T>> elementosSorteio = new ArrayList<Elemento<T>>(nListaSorteio);
		while (elementosSorteio.size() < nListaSorteio) {
			elementosSorteio.add(sortear(0));
		}
		Random random = new Random();
		int x = random.nextInt(elementosSorteio.size() - 1);
		return elementosSorteio.get(x);
	}

	private List<Integer> getListRandon(int tamanho) {
		List<Integer> listaTemp = new ArrayList<Integer>();
		List<Integer> lista = new ArrayList<Integer>();
		Random random = new Random();

		for (int i = 0; i < tamanho; i++) {
			listaTemp.add(i);
		}

		while (listaTemp.size() > 0) {
			int x = listaTemp.size() > 1 ? random.nextInt(listaTemp.size() - 1) : 0;
			int numero = listaTemp.remove(x);
			lista.add(numero);
		}
		return lista;
	}

	public void setQtPrefenciaSorteio(int qtPrefenciaSorteio) {
		this.qtPrefenciaSorteio = qtPrefenciaSorteio;
	}

	public int getQtPrefenciaSorteio() {
		return qtPrefenciaSorteio;
	}

}