package br.com.itx.util;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.hibernate.AssertionFailure;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateQuery;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Naming;
import org.jboss.seam.util.Reflections;

import br.jus.pje.nucleo.entidades.IEntidade;

public final class EntityUtil implements Serializable {

	public static final String ENTITY_MANAGER_NAME = "entityManager";
	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(EntityUtil.class);

	private static final Map<Class<?>, PropertyDescriptor> cacheIds = new HashMap<Class<?>, PropertyDescriptor>();

	private static final Package STRING_PACKAGE = String.class.getPackage();

	private EntityUtil() {
	}

	/**
	 * Metodo que recebe um objeto, que representa um Id composto de uma
	 * entidade, e retorna uma String com os valores dos fields do id separados
	 * pelo char '-'
	 *
	 * @param O objeto que representa um id composto
	 * @return
	 */
	public static String getCompositeId(Object objId) {
		StringBuilder sb = new StringBuilder("");
		if (objId != null) {
			PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(objId);
			for (int i = 0; i < pds.length; i++) {
				try {
					PropertyDescriptor pd = pds[i];
					if (pd.getName().equals("class")) {
						continue;
					}
					if (sb.length() > 0) {
						sb.append('-');
					}
					sb.append(getProperty(objId, pd));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	private static String getProperty(Object objId, PropertyDescriptor pd) throws Exception {
		Class<?> cl = pd.getPropertyType();
		Object value = null;
		Method m = pd.getReadMethod();
		if (m != null) {
			value = m.invoke(objId);
		}
		if (value != null) {
			if (cl.isAnnotationPresent(Entity.class)) {
				PropertyDescriptor pd2 = getId(value);
				return getProperty(value, pd2);
			} else {
				return value.toString();
			}
		}
		return "";
	}

	public static void setCompositeId(Object objId, String id) {
		if (id != null && !id.equals("")) {
			PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(objId);
			int cnt = 0;
			String[] piece = id.split("-");
			for (int i = 0; i < pds.length; i++) {
				try {
					PropertyDescriptor pd = pds[i];
					if (pd.getName().equals("class")) {
						continue;
					}
					if (cnt < piece.length) {
						String value = piece[cnt];
						if (!value.trim().equals("")) {
							setProperty(objId, pd, value);
						}
					}
					cnt++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void setProperty(Object objId, PropertyDescriptor pd, String strValue) throws Exception {
		Class<?> cl = pd.getPropertyType();
		Object value = null;
		if (cl.isAnnotationPresent(Entity.class)) {
			value = cl.newInstance();
			PropertyDescriptor pd2 = getId(value);
			setProperty(value, pd2, strValue);
		} else {
			value = cl.getConstructor(String.class).newInstance(strValue);
		}
		Method m = pd.getWriteMethod();
		if (m != null) {
			m.invoke(objId, value);
		}
	}

	/**
	 * Metodo que recebe uma entidade e devolve o PropertyDescriptor do campo id
	 * procurando pela anotação @id
	 *
	 * @param objId Entidade
	 * @return
	 */
	public static PropertyDescriptor getId(Object objId) {
		if (objId == null) {
			return null;
		}
		if (!EntityUtil.isEntity(objId)) {
			throw new IllegalArgumentException("O objeto não é uma entidade: " + objId.getClass().getName());
		}
		Class<?> cl = objId.getClass();
		if (cl.getName().indexOf("javassist") > -1 || cl.getName().indexOf("jvst") > -1) {
			cl = cl.getSuperclass();
		}

		return getId(cl);
	}

	/**
	 * Metodo que recebe um Class e devolve o PropertyDescriptor do campo id
	 * procurando pela anotações @id e @EmbeddedId
	 *
	 * @param objId Entidade
	 * @return
	 */
	public static PropertyDescriptor getId(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}

		PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(clazz);
		if (cacheIds.containsKey(clazz)) {
			return cacheIds.get(clazz);
		}
		for (int i = 0; i < pds.length; i++) {
			PropertyDescriptor pd = pds[i];
			if (isId(pd)) {
				cacheIds.put(clazz, pd);
				return pd;
			}
		}
		return null;
	}

	/**
	 * Testa de o objeto possui a anotação @Entity
	 *
	 * @param obj
	 * @return
	 */
	public static final boolean isEntity(final Object obj) {
		if (obj == null) {
			return false;
		}
		final Class<?> cl = getEntityClass(obj);
		return isEntity(cl);
	}

	/**
	 * Testa de a classe possui a anotação @Entity
	 *
	 * @param obj
	 * @return
	 */
	public static final boolean isEntity(final Class<?> cl) {
		if (cl == null) {
			return false;
		}
		if (cl.isPrimitive() || STRING_PACKAGE.equals(cl.getPackage())) {
			return false;
		}
		return cl.isAnnotationPresent(Entity.class);
	}

	public static final boolean isAnnotationPresent(final Object obj, final Class<? extends Annotation> clazz) {
		if (obj == null) {
			return false;
		}
		final Class<?> cl = getEntityClass(obj);
		return cl.isAnnotationPresent(clazz);
	}

	/**
	 * Metodo que recebe um objeto de uma entidade e pega por reflexão o objeto
	 * com o id desta entidade.
	 *
	 * @param entidade
	 * @return
	 */
	public static Object getEntityIdObject(final Object entidade) {
		if (entidade == null) {
			return null;
		}

		if (entidade instanceof IEntidade) {
			return ((IEntidade) entidade).getEntityIdObject();
		}

		if (!EntityUtil.isEntity(entidade)) {
			throw new IllegalArgumentException("O objeto não é uma entidade: " + entidade.getClass().getName());
		}

		final Class<? extends Object> cl = entidade.getClass();
		if (!cl.isPrimitive() && !cl.getPackage().equals(STRING_PACKAGE)) {
			PropertyDescriptor id = getId(entidade);
			if (id != null) {
				Method readMethod = id.getReadMethod();
				try {
					return readMethod.invoke(entidade, new Object[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				log.error("Não foi encontrado um PropertyDescriptor para o " + "Id da entidade "
						+ entidade.getClass().getName());
			}
		}
		return null;
	}

	/**
	 * Metodo que devolve a classe da entidade. Caso a entidade seja um proxy
	 * (javassist), retorna a classe pai usando o
	 * {@link java.lang.Class#getSuperclass() getSuperclass}
	 *
	 * @param entity
	 * @return
	 */
	public static Class<?> getEntityClass(Object entity) {
		if (entity == null) {
			return null;
		}

		if (entity instanceof IEntidade) {
			try {
				return ((IEntidade) entity).getEntityClass();
			} catch(EntityNotFoundException ex) {
				log.trace("[EntityUtil] Ignorando validação de entidade inexistente do jpa", ex);
			}
		}

		Class<?> cl = entity.getClass();
		if (cl.isPrimitive() || STRING_PACKAGE.equals(cl.getPackage())) {
			return null;
		}

		if (isHibernateProxy(cl)) {
			cl = cl.getSuperclass();
		}
		return cl;
	}

	public static boolean isHibernateProxy(Class<?> cl) {
		if (cl == null) {
			return false;
		}
		return cl.getName().indexOf("javassist") > -1 || cl.getName().indexOf("jvst") > -1;
	}

	/**
	 * Metodo que recebe uma entidade e cria um objeto do mesmo tipo e copia os
	 * atributos para esta nova entidade.
	 *
	 * @param <E>
	 * @param origem
	 * @param copyLists
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static <E> E cloneEntity(E origem, boolean copyLists) throws InstantiationException, IllegalAccessException {
		Class<?> cl = getEntityClass(origem);
		E destino = (E) cl.newInstance();
		PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(cl);
		for (PropertyDescriptor pd : pds) {
			if ((!isId(pd) && isAceptable(pd)) || (isRelacao(pd) && copyLists)) {
				Method rm = pd.getReadMethod();
				Method wm = pd.getWriteMethod();
				if (wm != null) {
					Object value = Reflections.invokeAndWrap(rm, origem, new Object[0]);
					Reflections.invokeAndWrap(wm, destino, removeProxy(value));
				}
			}
		}
		return destino;
	}

	public static <E> List<E> cloneListEntity(List<E> origem, boolean copyLists) throws InstantiationException,
			IllegalAccessException {
		List<E> destino = new ArrayList<E>();
		for (E entity : origem) {
			destino.add(cloneEntity(entity, copyLists));
		}
		return destino;
	}

	public static Object removeProxy(Object object) {
		if (object instanceof HibernateProxy) {
			object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
		}
		return object;
	}

	private static final Object[] EMPTY_ARRAY_OF_OBJECTS = new Object[0];

	// TODO metodo de teste pois o acina estava dando erro em: oldEntity = (T)
	// EntityUtil.cloneEntity(instance, false);
	public static final Object cloneObject(final Object origem, final boolean copyLists) throws InstantiationException,
			IllegalAccessException {
		final Class<?> cl = getEntityClass(origem);
		final Object destino = cl.newInstance();
		final PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(cl);
		for (final PropertyDescriptor pd : pds) {
			if ((!isId(pd) && isAceptable(pd)) || (copyLists && isRelacao(pd))) {
				final Method wm = pd.getWriteMethod();
				if (wm != null) {
					final Method rm = pd.getReadMethod();
					final Object value = Reflections.invokeAndWrap(rm, origem, EMPTY_ARRAY_OF_OBJECTS);
					Reflections.invokeAndWrap(wm, destino, value);
				}
			}
		}
		return destino;
	}

	private static boolean isId(final PropertyDescriptor pd) {
		return pd != null
				&& (ComponentUtil.hasAnnotation(pd, Id.class) || ComponentUtil.hasAnnotation(pd, EmbeddedId.class));
	}

	private static boolean isAceptable(final PropertyDescriptor pd) {
		return pd != null && !ComponentUtil.hasAnnotation(pd, Transient.class) && 
			(ComponentUtil.hasAnnotation(pd, Column.class) || ComponentUtil.hasAnnotation(pd, ManyToOne.class));
	}

	private static boolean isRelacao(final PropertyDescriptor pd) {
		return pd != null
				&& (ComponentUtil.hasAnnotation(pd, ManyToMany.class) || ComponentUtil.hasAnnotation(pd,
				OneToMany.class));
	}

	/**
	 * Metodo que recebe uma entidade e seta null no atributo que corresponde ao
	 * id. Caso o tipo deste campo seja primitivo coloca o numero 0. Isto é
	 * utilizado porque o hibernate aloca um Id para a entidade antecipadamente
	 * e com isso caso ocorra um erro, como de violação de contraint, a entidade
	 * fica com um id inválido e ocorre um erro ao persiti essa entidade.
	 *
	 * @param entidade
	 */
	public static void setNullOnEntityId(Object entidade) throws Exception {
		PropertyDescriptor pd = EntityUtil.getId(entidade);
		Method writeMethod = pd.getWriteMethod();
		Class<?> propertyType = pd.getPropertyType();
		writeMethod.invoke(entidade, propertyType.isPrimitive() ? 0 : new Object[1]);
	}

	/**
	 * Metodo que cria um novo ArrayList para os atributos List de
	 * relacionamento da entidade. Esto é feito pois em caso de um erro na
	 * persistencia, os List ficam com referencia para a Entidade que deveria
	 * ter sido persistida (O hibernate gera um id pra estidade antes de inserir
	 * e em uma execeção, os list (PersistentBags) apontam para este id que não
	 * existe.
	 *
	 * @param entidade
	 * @throws Exception
	 */
	public static void clearEntityLists(Object entidade) throws Exception {
		List<PropertyDescriptor> descriptors = getPropertyDescriptors(entidade, OneToMany.class);
		for (PropertyDescriptor pd : descriptors) {
			Class<?> type = pd.getPropertyType();
			type.getGenericSuperclass();
			if (type.equals(List.class)) {
				pd.getWriteMethod().invoke(entidade, new ArrayList<Object>(0));
			}
		}
	}

	/**
	 * Metodo que devolve todos os PropertyDescriptor de uma entidade que
	 * contenham determinada Annotation. O metodo faz um teste se a classe foi
	 * criada por proxy, caso sim pega a classe pai, para buscar pelas anotações
	 *
	 * @param entidade
	 * @param annotationClass
	 * @return
	 */
	public static List<PropertyDescriptor> getPropertyDescriptors(Object entidade,
			Class<? extends Annotation> annotationClass) {
		List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		Class<?> cl = getEntityClass(entidade);
		PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(cl);
		for (PropertyDescriptor pd : pds) {
			if (ComponentUtil.hasAnnotation(pd, annotationClass)) {
				descriptors.add(pd);
			}
		}
		return descriptors;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getSingleResult(String jpaQuery) {
		return (T)getSingleResult(getEntityManager().createQuery(jpaQuery));
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getSingleResult(EntityManager em, String jpaQuery) {
		return (T)getSingleResult(em.createQuery(jpaQuery));
	}

	/**
	 * Retorna o primeiro objeto do resultado da query
	 *
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T getSingleResult(final Query query) {
		query.setMaxResults(1);
		query.setHint("org.hibernate.fetchSize", 2);
		final List<?> list = query.getResultList();
		if (list == null || list.size() == 0) {
			return null;
		}
		try {
			return (T) list.get(0);
		} catch (NoResultException ex) {
			return null;
		}
	}

	/**
	 * Retorna o primeiro objeto do resultado da query
	 *
	 * @param query
	 * @return
	 */
	public static final <T> T getSingleResult(final TypedQuery<T> query) {
		query.setMaxResults(1);
		query.setHint("org.hibernate.fetchSize", 2);
		final List<T> list = query.getResultList();
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public static Long getSingleResultCount(Query query) {
		Long result = getSingleResult(query);
		return result == null ? 0 : result;
	}

	public static EntityManager getEntityManager() {
		return ComponentUtil.getComponent(ENTITY_MANAGER_NAME);
	}

	public static org.hibernate.SQLQuery createNativeQuery(CharSequence sqlString) {
		EntityManager em = getEntityManager();

		Query qry = em.createNativeQuery(sqlString.toString());
		String comment = getOrigenInvoke() + ": " + sqlString;
		qry.setHint(HibernateUtil.COMMENT_KEY, comment);

		org.hibernate.Query query = ((HibernateQuery) qry).getHibernateQuery();
		return (org.hibernate.SQLQuery) query;
	}

	public static Query createNativeQuery(CharSequence sqlString, String... evictedTable) {
		EntityManager em = getEntityManager();
		return createNativeQuery(em, sqlString, evictedTable);
	}

	public static Query createNativeQuery(EntityManager em, CharSequence sqlString, String... evictedTable) {
		Query qry = em.createNativeQuery(sqlString.toString());
		String comment = getOrigenInvoke() + ": " + sqlString;
		qry.setHint(HibernateUtil.COMMENT_KEY, comment);

		org.hibernate.Query query = ((HibernateQuery) qry).getHibernateQuery();

		if (evictedTable != null) {
			for (String tbl : evictedTable) {
				((org.hibernate.SQLQuery) query).addSynchronizedQuerySpace(tbl);
			}
		}

		return qry;
	}

	public static Query createQuery(String hql) {
		String comment = getOrigenInvoke() + ": " + hql;
		return createQuery(hql, false, false, comment);
	}

	public static Query createQuery(String hql, String comment) {
		return createQuery(hql, false, false, comment);
	}

	public static Query createQuery(EntityManager entityManager, CharSequence hql, boolean readOnly, boolean cacheQuery, String comment) {
		Query query = entityManager.createQuery(hql.toString());

		if ((comment == null) || (comment.isEmpty())) {
			comment = getOrigenInvoke() + ": " + hql;
		}
		query.setHint(HibernateUtil.COMMENT_KEY, comment);

		if (cacheQuery) {
			query
					.setHint("org.hibernate.cacheable", Boolean.TRUE)
					.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.USE)
					.setHint("javax.persistence.cache.storeMode", CacheStoreMode.USE)
					.setHint("org.hibernate.cacheRegion", "Tempestivo");
		} else {
			query
					.setHint("org.hibernate.cacheable", Boolean.FALSE)
					.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
					.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
		}

		if (readOnly) {
			query.setHint("org.hibernate.readOnly", Boolean.TRUE);
		}

		query.setHint("org.hibernate.fetchSize", 50);

		return query;
	}

	public static Query createQuery(CharSequence hql, boolean readOnly, boolean cacheQuery, String comment) {
		return createQuery(getEntityManager(), hql, readOnly, cacheQuery, comment);
	}

	public static final String getOrigenInvoke() {
		return MySecurityManager.instance().getCallerClassName(3);
	}

	/**
	 * A custom security manager that exposes the getClassContext() information
	 */
	private static final class MySecurityManager extends SecurityManager {

		private static MySecurityManager INSTANCE;

		public final String getCallerClassName(final int callStackDepth) {
			final Class[] classContext = getClassContext();
			if (classContext.length > callStackDepth) {
				return classContext[callStackDepth].getName();
			}
			return null;
		}

		static final MySecurityManager instance() {
			if (INSTANCE == null) {
				INSTANCE = new MySecurityManager();
			}
			return INSTANCE;
		}
	}

	public static void flush() {
		try {
			flush(getEntityManager());
		} catch (AssertionFailure e) {
			/* bug hibernate */
		}
	}

	public static void flush(EntityManager em) {
		try {
			em.flush();
		} catch (AssertionFailure e) {
			/* bug hibernate */
		}
	}

	/**
	 * Devolve um List com todos os elementos de uma determinada entidade. Ex:
	 * <code>List{@literal <E>} resultList = EntityUtil.getEntityList(Parametro.class)<code>;
	 *
	 * @param <E> O type da Entidade
	 * @param clazz
	 * @return
	 */
	public static final <E> List<E> getEntityList(final Class<E> clazz) {
		return getEntityList(clazz, getEntityManager());
	}

	/**
	 * Devolve um List com todos os elementos de uma determinada entidade. Ex:
	 * <code>List{@literal <E>} resultList =
	 * EntityUtil.getEntityList(Parametro.class, entityManager)<code>;
	 *
	 * @param <E>
	 * @param clazz
	 * @param entityManager
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <E> List<E> getEntityList(final Class<E> clazz, final EntityManager entityManager) {
		if (clazz == null) {
			throw new NullPointerException("clazz");
		}

		final String sEntityName = clazz.getName();
		final String sComment = "EntityUtil.getEntityList: " + sEntityName;
		final Object emDelegate = entityManager.getDelegate();
		if (emDelegate instanceof Session) {
			final Session session = (Session) emDelegate;
			final List list = session.createCriteria(clazz)
					.setCacheable(true)
					.setCacheMode(CacheMode.NORMAL)
					.setComment(sComment + " (Session.createCriteria)")
					.setFetchSize(100)
					.list();
			return list;
		}

		final StringBuilder sb = new StringBuilder(120);
		sb.append("select e from ").append(sEntityName).append(" e");
		return entityManager
				.createQuery(sb.toString())
				.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.USE)
				.setHint("javax.persistence.cache.storeMode", CacheStoreMode.USE)
				.setHint("org.hibernate.fetchSize", 100)
				.setHint("org.hibernate.comment", sComment + " (EntityManager.createQuery)")
				.setHint("org.hibernate.cacheRegion", "Tempestivo")
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}

	/**
	 * Atalho para busca de entidades pelo id
	 *
	 * @param <E>
	 * @param clazz classe da entidade a ser pesquisada
	 * @param id
	 * @return
	 */
	public static <E> E find(Class<E> clazz, Object id) {
		if (id == null) {
			return null;
		}
		return getEntityManager().find(clazz, id);
	}

	/**
	 * Retorna o entityManager do JPA para quando não for possível acessar o do
	 * Seam.
	 *
	 * @param persistenceUnitJndiName Nome do Unit que será criado o
	 * entityManager pelo Factory
	 * @return EntityManager
	 */
	public static EntityManager createEntityManagerFactory(String persistenceUnitJndiName) {
		try {
			EntityManagerFactory emf = (EntityManagerFactory) Naming.getInitialContext()
					.lookup(persistenceUnitJndiName);
			return emf.createEntityManager();
		} catch (NamingException e) {
			throw new IllegalArgumentException("EntityManagerFactory not found in JNDI : " + persistenceUnitJndiName, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> Class<E> getParameterizedTypeClass(Class<E> clazz) {
		Class<E> entityClass;
		java.lang.reflect.Type type = clazz.getGenericSuperclass();
		if (type instanceof java.lang.reflect.ParameterizedType) {
			java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) type;
			entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
		} else {
			throw new IllegalArgumentException("Não foi possivel pegar a Entidade por reflexão");
		}
		return entityClass;
	}

	public static <E> E newInstance(Class<E> clazz) {
		try {
			return getParameterizedTypeClass(clazz).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Object> getIdsFromList(final List<?> listaObj) {
		if (listaObj == null) {
			return Collections.emptyList();
		}

		final List<Object> list = new ArrayList<Object>(listaObj.size());
		for (Object object : listaObj) {
			final Object entityIdObject = getEntityIdObject(object);
			if (entityIdObject != null) {
				list.add(entityIdObject);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <E> E refreshEntity(E entity) {
		if (entity == null) {
			return null;
		}
		Object id = getEntityIdObject(entity);
		if (id == null || ((id instanceof Integer) && ((Integer) id) == 0)) {
			return entity;
		}
		return (E) find(entity.getClass(), id);
	}

	/**
	 * Remove a entidade da sessão do hibernate.
	 *
	 * @param entity Entidade.
	 */
	public static <E> void evict(E entity) {
		if (entity != null) {
			getSession().evict(entity);
		}
	}

	/**
	 * @return Session
	 */
	public static Session getSession() {
		return (Session) getEntityManager().getDelegate();
	}

	public static <V> V getSingleResultFromNamedQuery(String namedQuery, Object... params) {
		Query qry = getEntityManager().createNamedQuery(namedQuery);

		String comments = namedQuery + " @ " + getOrigenInvoke();
		qry.setHint(comments, comments);

		if (params != null) {
			int idxParam = 0;
			for (Object param : params) {
				qry.setParameter(++idxParam, param);
			}
		}

		V v = getSingleResult(qry);
		return v;
	}

	public static <V> V getSingleResultFromNamedQuery(String namedQuery, Class<V> type, Object... params) {
		Object obj = getSingleResultFromNamedQuery(namedQuery, params);
		return type.cast(obj);
	}

}
