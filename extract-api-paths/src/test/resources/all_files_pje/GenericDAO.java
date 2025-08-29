package br.com.infox.core.dao;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

@Name(GenericDAO.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class GenericDAO {

	public static final String NAME = "genericDAO";
	
	@In
	protected EntityManager entityManager;
	
	@Logger
	protected Log logger;
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> getNamedResultList(String namedQuery,
			Map<String, Object> parameters) {
		Query q = getNamedQuery(namedQuery, parameters);
		return (List<T>) q.getResultList();
	}

	@SuppressWarnings("unchecked")
	protected <T> T getNamedSingleResult(String namedQuery,
			Map<String, Object> parameters) {
		Query q = getNamedQuery(namedQuery, parameters);
		return (T) getSingleResult(q);
	}

	protected Query getNamedQuery(String namedQuery,
			Map<String, Object> parameters) {
		Query q = entityManager.createNamedQuery(namedQuery);
		if(parameters != null) {
			for (Entry<String, Object> e : parameters.entrySet()) {
				q.setParameter(e.getKey(), e.getValue());
			}
		}
		return q;
	}
	
	public void persistVarios(Object... parametros){
		for (Object objeto : parametros){
			entityManager.persist(objeto);
		}
		entityManager.flush();
	}

	public void updateVarios(Object... parametros){
		for (Object objeto : parametros){
			entityManager.merge(objeto);
		}
		entityManager.flush();
	}
	
	public void removeVarios(Object... parametros){
		for (Object objeto : parametros){
			entityManager.remove(objeto);
		}
		entityManager.flush();
	}
	
	public <T> T persist(T object){
		entityManager.persist(object);
		entityManager.flush();
		return object;
	}
	
	public <T> T update(T object){
		entityManager.merge(object);
		entityManager.flush();
		return object;
	}
	
	public <T> T remove(T object){
		entityManager.remove(object);
		entityManager.flush();
		return object;
	}

	public <T> T find(Class<T> clazz, Object id) {
		return entityManager.find(clazz, id);
	}
	
	@Transactional
	public <T> boolean isManaged(T object){
		return object!=null && entityManager.contains(object);
	}
	
	public void clear(){
		entityManager.clear();
	}
	
	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	/**
	 * Atualiza o estado do objeto com as informações do banco de dados.
	 * 
	 * @param object Entidade.
	 */
	public <T> void refresh(T object) {
		EntityManager em = getEntityManager();
		if (object != null) {
			if (em.contains(object)) {
				em.refresh(object);
			}
		}
	}
	
	/**
	 * Retorna a entidade resultado da query passado por parâmetro.
	 * 
	 * @param query
	 * @return Entidade resultado da consulta.
	 */
	@SuppressWarnings("unchecked")
	protected <E> E getSingleResult(Query query) {
		E resultado = null;
		
		try{
			resultado = (E) query.getSingleResult();
		} catch (NoResultException e){
			logger.debug("Registro não encontrado", e);
		} catch (NonUniqueResultException e){
			String message = String.format("Há mais de um registro no sistema para a consulta. Erro: [%s].", e.getLocalizedMessage());
			logger.error(message, e);
			throw new IllegalStateException(message);
		}
		
		return resultado;
	}
}