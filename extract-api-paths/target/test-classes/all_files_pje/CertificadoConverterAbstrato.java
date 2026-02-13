/**
 * CertificadoConverterAbstrato.java.
 *
 * Data de criação: 05/11/2014
 */
package br.com.infox.core.certificado.converter;

import java.util.Collection;

import br.com.infox.core.certificado.CertificadoException;

/**
 * Classe responsável pela infra-estrutura dos convesores de certificado
 * digital. O conversor em questão é usado como um builder justificando-se na
 * construção de objetos complexos.
 * 
 * @author Adriano Pamplona
 */
public abstract class CertificadoConverterAbstrato<O, D> {

	/**
	 * Converte o objeto de origem para o objeto de destino.
	 * 
	 * @param objeto
	 *            Objeto de origem.
	 * @return Objeto de destino.
	 * @throws CertificadoException
	 */
	public abstract D converter(O objeto) throws CertificadoException;

	/**
	 * Retorna true se o objeto não tem referência, ou se for um array de
	 * objetos que pelo menos um objeto não tenha referência.
	 * 
	 * @param objetos
	 *            Objeto(s)
	 * @return true se o objeto não tiver referência.
	 */
	protected boolean isNull(Object... objetos) {
		boolean res = true;

		if (objetos != null) {
			res = false;
			for (int idx = 0; idx < objetos.length && (res == false); idx++) {
				res = (objetos[idx] == null || objetos[idx].equals(""));
			}
		}
		return res;
	}

	/**
	 * Retorna true se o objeto tem referência, ou se for um array de objetos
	 * que todos os objetos tenham referência.
	 * 
	 * @param objetos
	 *            Objeto(s)
	 * @return true se o(s) objeto(s) possuem referência.
	 */
	protected boolean isNotNull(Object... objetos) {
		boolean res = false;

		if (objetos != null) {
			res = true;
			for (int idx = 0; idx < objetos.length && (res == true); idx++) {
				res = (objetos[idx] != null && !objetos[idx].equals(""));
			}
		}
		return res;
	}

	/**
	 * Retorna true se a string for vazia, ou se for um array de strings que
	 * pelo menos uma esteja vazia.
	 * 
	 * @param strings
	 *            String(s)
	 * @return true se a string(s) for vazia.
	 */
	protected boolean isVazio(String... strings) {
		boolean res = true;

		if (strings != null) {
			res = false;
			for (int idx = 0; idx < strings.length && (res == false); idx++) {
				res = (strings[idx] == null || strings[idx].trim().equals(""));
			}
		}
		return res;
	}

	/**
	 * Retorna true se a string não for vazia, ou se for um array de strings que
	 * pelo menos uma não esteja vazia.
	 * 
	 * @param strings
	 *            String(s)
	 * @return true se a string(s) não for vazia.
	 */
	protected boolean isNotVazio(String... strings) {
		return !isVazio(strings);
	}

	/**
	 * Retorna true se a coleção for nula ou vazia.
	 * 
	 * @param colecao
	 * @return true se a coleção for nula ou vazia.
	 */
	protected boolean isVazio(Collection<?> colecao) {
		return (isNull(colecao) || colecao.size() == 0);
	}

	/**
	 * Retorna true se a coleção não for vazia.
	 * 
	 * @param colecao
	 * @return true se a coleção não for vazia.
	 */
	protected boolean isNotVazio(Collection<?> colecao) {
		return !isVazio(colecao);
	}
}
