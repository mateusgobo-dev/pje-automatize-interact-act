/**
 * Transform.java.
 *
 * Data: 16/02/2018
 */
package br.jus.cnj.pje.business.dao.transform;

import java.util.List;

/**
 * Classe transformadora de objetos de consultas nativas.
 * 
 * @author Adriano Pamplona
 */
public interface Transform<T> {

	/**
	 * Transforma o array de objetos passado por parâmetro em um objeto definido
	 * pelo transformador.
	 * 
	 * @param objeto
	 *            Array de objetos de origem.
	 * @return Tipo definido pelo transformador.
	 */
	public T transform(Object[] objeto);

	/**
	 * Transforma uma coleção de arrays em uma coleção de objetos específicos
	 * definidos pelo transformador.
	 * 
	 * @param lista
	 *            Coleção dos objetos de origem.
	 * @return Coleção do tipo definido pelo transformador.
	 */
	public List<T> transformCollection(List<Object[]> lista);
}
