/**
 * CodificacaoCertificado.java.
 *
 * Data de criação: 04/11/2014
 */
package br.com.infox.core.certificado.util;

/**
 * Classe com os tipos de codificação de certificado disponíveis no PJe.
 * 
 * @author Adriano Pamplona
 */
public enum CodificacaoCertificado {

	PKI_PATH("PkiPath"),
	PEM("PEM");

	private String valor;

	/**
	 * Construtor.
	 * 
	 * @param valor
	 */
	CodificacaoCertificado(String valor) {
		this.valor = valor;
	}

	/**
	 * @return valor.
	 */
	public String getValor() {
		return this.valor;
	}

	/**
	 * Retorna e enum do valor passado por parametro.
	 * 
	 * @param valor
	 *            Valor do enum.
	 * @return Enum do valor passado por parametro.
	 */
	public static CodificacaoCertificado get(String valor) {
		CodificacaoCertificado resultado = null;

		CodificacaoCertificado[] enuns = values();
		for (int indice = 0; indice < enuns.length && resultado == null; indice++) {
			CodificacaoCertificado temp = enuns[indice];
			if (temp.getValor().equalsIgnoreCase(valor)) {
				resultado = temp;
			}
		}

		return resultado;
	}

	/**
	 * Retorna true se o valor passado por parâmetro for PEM.
	 * 
	 * @param valor
	 * @return Booleano
	 */
	public static Boolean isPEM(String valor) {
		return ((valor != null) && (
				PEM.getValor().equalsIgnoreCase(valor) ||
				PEM.toString().equalsIgnoreCase(valor)
				));
	}

	/**
	 * Retorna true se o valor passado por parâmetro for PKI_PATH.
	 * 
	 * @param valor
	 * @return Booleano
	 */
	public static Boolean isPkiPath(String valor) {
		return ((valor != null) && (
				PKI_PATH.getValor().equalsIgnoreCase(valor) || 
				PKI_PATH.toString().equalsIgnoreCase(valor)
				));
	}
	
	/**
	 * Retorna true se o enum for do tipo PEM.
	 * 
	 * @return Booleano
	 */
	public Boolean isPEM() {
		return ((getValor() != null) && (
				PEM.getValor().equalsIgnoreCase(getValor()) ||
				PEM.toString().equalsIgnoreCase(getValor())
				));
	}

	/**
	 * Retorna true se o enum for do tipo PKI_PATH.
	 * 
	 * @return Booleano
	 */
	public Boolean isPkiPath() {
		return ((getValor() != null) && (
				PKI_PATH.getValor().equalsIgnoreCase(getValor()) ||
				PKI_PATH.toString().equalsIgnoreCase(getValor())
				));
	}
}
