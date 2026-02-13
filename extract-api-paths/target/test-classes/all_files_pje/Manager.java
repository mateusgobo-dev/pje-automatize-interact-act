/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;

/**
 * @author cristof
 * 
 */
public interface Manager<T>{

	/**
	 * Inserts an given entity in the persistence storage, updating it's fields if it is already managed.
	 * 
	 * @param entity the entity to be persisted or updated
	 * @throws PJeBusinessException if some business rules has been violated
	 * @throws PJeDAOException if there was a violation while persisting
	 */
	public T persist(T entity) throws PJeBusinessException;

	/**
	 * Refresh an given entity, restoring it's fields with the values stored on the persistence unit.
	 * 
	 * @param entity the entity to be refreshed
	 * @return the refreshed entity, if it is already managed, its managed counterpart, or null, if the entity does not exist
	 * @throws PJeBusinessException if some business rules has been violated
	 * @throws PJeDAOException if there was a violation while refreshing
	 */
	public T refresh(T entity) throws PJeBusinessException;

	/**
	 * Removes an given entity from the persistence storage.
	 * 
	 * @param entity the entity to be removed
	 * @throws PJeBusinessException if some business rules has been violated
	 * @throws PJeDAOException if there was a violation while removing
	 */
	public void remove(T entity) throws PJeBusinessException;

	/**
	 * Finds all entities of the given type.
	 * 
	 * @return a list with all entities of the given type.
	 * @throws PJeBusinessException if some business rules has been violated
	 * @throws PJeDAOException if there was a violation while finding
	 */
	public List<T> findAll() throws PJeBusinessException;

	/**
	 * Return an entity of the given identifier.
	 * 
	 * @param id the identifier of the entity.
	 * @return the entity with the given id, or null, if it does not exists.
	 * @throws PJeBusinessException if some business rules has been violated
	 * @throws PJeDAOException if there was a violation while finding
	 */
	public T findById(Object id) throws PJeBusinessException;
}
