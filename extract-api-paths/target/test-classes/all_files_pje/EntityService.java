/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.List;

/**
 * @author cristof
 * 
 */
public interface EntityService<T, ID> {

	public T getEntity(ID id);

	public int rowCount();

	public List<T> getEntities(int first, int numRows);

}
