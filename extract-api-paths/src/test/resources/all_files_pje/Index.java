package br.jus.cnj.pje.util.formatadorLista;

/**
 * Classe utilizada para encapsular informação referente ao índice da lista (posição) 
 * a ser mostrado pelo formatador de listas.   
 *
 */
public class Index {
	
	private int value;
	
	public Index(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public int next() {
		return ++this.value;
	}

}
