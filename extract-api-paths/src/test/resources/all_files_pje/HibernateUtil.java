package br.com.itx.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.convert.ConverterException;
import javax.persistence.Id;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.seam.persistence.FullTextHibernateSessionProxy;
import org.jboss.seam.ui.converter.NoSelectionConverter;

public final class HibernateUtil{

	public static final String COMMENT_KEY = "org.hibernate.comment";

	private HibernateUtil(){
	}

	public static void disableFilters(String... names){
		Session session = getSession();
		for (String name : names){
			session.disableFilter(name);
		}
	}

	public static void enableFilters(String... names){
		Session session = getSession();
		for (String name : names){
			session.enableFilter(name);
		}
	}

	public static void setFilterParameter(String filterName, String parameterName, Object paremeterValue){
		Filter enabledFilter = getEnabledFilter(filterName);
		enabledFilter.setParameter(parameterName, paremeterValue);
	}

	public static void setFilterParameterList(String filterName, String parameterName, Collection<?> paremeterValues){
		Filter enabledFilter = getEnabledFilter(filterName);
		enabledFilter.setParameterList(parameterName, paremeterValues);
	}

	public static void setFilterParameterList(String filterName, String parameterName, Object[] paremeterValues){
		Filter enabledFilter = getEnabledFilter(filterName);
		enabledFilter.setParameterList(parameterName, paremeterValues);
	}

	public static Filter getEnabledFilter(String filterName){
		Filter enabledFilter = getSession().getEnabledFilter(filterName);
		if (enabledFilter == null){
			getSession().enableFilter(filterName);
			enabledFilter = getSession().getEnabledFilter(filterName);
		}
		return enabledFilter;
	}

	public static Session getSession(){
		return (Session) EntityUtil.getEntityManager().getDelegate();
	}
	
	public static Map<String, Filter> getEnabledFilters(Session session) {
		return ((SessionImplementor)session).getLoadQueryInfluencers().getEnabledFilters();
	}

	public static void disableAllFilters(){
		FullTextHibernateSessionProxy session = (FullTextHibernateSessionProxy) getSession();
		Map m = ((SessionImplementor)session).getLoadQueryInfluencers().getEnabledFilters();
		
		List<String> toRemove = new ArrayList<String>();
		/*
		 * PJE-JT: David Vieira : PJE-392 - 2011-10-27 Alteracoes feitas pela JT. Correção do método que desabilita os filtros.
		 */
		for (Object s : m.keySet()){
			toRemove.add(s.toString());
		}
		for (String string : toRemove){
			session.disableFilter(string);
		}
		/*
		 * PJE-JT: Fim
		 */
	}

	public static void disableAllFiltersConcurrent(){

		FullTextHibernateSessionProxy session = (FullTextHibernateSessionProxy) getSession();
		Map m = ((SessionImplementor)session).getLoadQueryInfluencers().getEnabledFilters();
		List<String> filtros = new ArrayList<String>(m.size());
		for (Object s : m.keySet()){
			filtros.add(s.toString());
		}
		for (String str : filtros){
			session.disableFilter(str);
		}
	}

	/**
	 * Método utilitário que tenta inicializar o proxy Hibernate CGLIB.
	 * 
	 * Desde modo pode-se fazer o cast para as subclasses.
	 * 
	 * @param <T>
	 * @param maybeProxy -- the possible Hibernate generated proxy
	 * @param baseClass -- the resulting class to be cast to.
	 * @return the object of a class <T>
	 * @throws ClassCastException
	 */
	public static <T>T deproxy(Object maybeProxy, Class<T> baseClass) throws ClassCastException{
		if (maybeProxy instanceof HibernateProxy){
			return baseClass.cast(((HibernateProxy) maybeProxy).getHibernateLazyInitializer().getImplementation());
		}
		return baseClass.cast(maybeProxy);
	}

	public static Object getHibernateProxyImplementation(Object obj){
		Object retorno = obj;
		if (obj instanceof HibernateProxy){
			retorno = ((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
		}
		return retorno;
	}

	/**
	 * Método utilitário para retornar o id da entidade como String
	 * 
	 * Utilizado para preencher os complementos do tipo dinâmico.
	 * 
	 * @param entity Entidade JPA
	 * @return Id da entidade no formato String, ou null caso não seja entidade
	 * @throws ClassCastException
	 */
	public static String getIdAsString(Object entity) {
		Field idField = findIdField(entity);
		Method idMethod = findIdProperty(entity);
		if (idField != null) {
			try {
				Object id = idField.get(entity);
				if (id == null) {
					return NoSelectionConverter.NO_SELECTION_VALUE;
				} else {
					return id.toString();
				}
			} catch (Exception e) {
				throw new ConverterException("");
			}
		} else if (idMethod != null) {
			Object[] params = {};
			try {
				Object id = idMethod.invoke(entity, params);
				if (id == null) {
					return NoSelectionConverter.NO_SELECTION_VALUE;
				} else {
					return id.toString();
				}
			} catch (Exception e) {
				throw new ConverterException("");
			}
		} else {
			throw new ConverterException("");
		}
	}
	
	/**
	 * Find the Id property (getter method) on an entity
	 * 
	 * @param entity
	 *            The entity to find the id property
	 * @return The Id property, null if not found
	 * @throws ConverterException
	 *             if the Class is not an entity
	 */
	private static Method findIdProperty(Object entity) {
		return findIdMethod(entity.getClass());
	}

	/**
	 * Utility method to find Id field on an entity
	 * 
	 * @param entity
	 *            The entity to find the id field on
	 * @return The Id field, null if not found
	 * @throws ConverterException
	 *             if the Class is not an entity
	 */
	private static Field findIdField(Object entity) {
		@SuppressWarnings("rawtypes")
		Class clazz = entity.getClass();
		return findIdField(clazz);
	}

	/**
	 * Find Id field on an entity
	 * 
	 * @param clazz
	 *            The clazz of the entity to find the id field on
	 * @return The Id field, null if not found
	 * @throws ConverterException
	 *             if the Class is not an entity
	 */
	@SuppressWarnings("rawtypes")
	private static Field findIdField(Class clazz) {
		for (Class currClazz = clazz; !currClazz.equals(Object.class); currClazz = currClazz.getSuperclass()) {
			// Iterate over the fields and find the Id field
			for (Field f : currClazz.getDeclaredFields()) {
				if (!(f.isAccessible()))
					f.setAccessible(true);
				if (f.isAnnotationPresent(Id.class)) {
					return f;
				}
			}
		}
		return null;
	}

	/**
	 * Find the Id property (getter method) on an entity
	 * 
	 * @param clazz
	 *            The entity to find the id property on
	 * @return The Id property, null if not found
	 * @throws ConverterException
	 *             if the Class is not an entity
	 */
	@SuppressWarnings("rawtypes")
	private static Method findIdMethod(Class clazz) {
		for (Class currClazz = clazz; !currClazz.equals(Object.class); currClazz = currClazz.getSuperclass()) {
			// Iterate over the fields and find the Id field
			for (Method m : currClazz.getDeclaredMethods()) {
				if (!(m.isAccessible()))
					m.setAccessible(true);
				if (m.isAnnotationPresent(Id.class)) {
					return m;
				}
			}
		}
		return null;
	}

}
