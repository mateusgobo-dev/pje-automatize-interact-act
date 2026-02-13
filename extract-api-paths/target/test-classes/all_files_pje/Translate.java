package br.jus.pje.search;

/**
 * Classe auxiliar responsável por permitir a substituição de caracteres no atributo de pesquisa.
 * É utilizada a função translate(String text, from text, to text) do postgresql.
 */
public class Translate {

	private String atribute;
	
	private String from;
	
	private String to;
	
	public Translate(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public String getAtribute() {
		return atribute;
	}

	public void setAtribute(String atribute) {
		this.atribute = atribute;
	}
	
	@Override
	public String toString() {
		return String.format(" translate(%s, '%s', '%s') ", atribute, from, to);
	}
	
}
