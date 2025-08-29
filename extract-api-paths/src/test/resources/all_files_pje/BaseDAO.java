package br.jus.cnj.pje.business.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.interceptor.PJeDAOIntercepted;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Operator;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;
import br.jus.pje.search.Search.JoinAlias;

/**
 * Classe genérica definidora de chamadas de acesso a dados.
 * Essa classe e suas herdeiras têm escopo de evento, mas fazem uso de um objeto injetado pelo
 * JBoss Seam que tem escopo de conversação (EntityManager), de modo que devem ser tomadas preucações
 * com vistas a evitar que o EntityManager fique populado com muitos objetos após as chamadas a métodos 
 * desta classe, especialmente se houver uma conversação longa ativa. 
 * 
 * @author Daniel Miranda
 *
 * @param <E>
 */
@PJeDAOIntercepted
@Scope(ScopeType.EVENT)
public abstract class BaseDAO<E> implements Serializable {

	/**
	 * Recupera o identificador do objeto dado.
	 * 
	 * @param e o objeto cujo identificador se pretende recuperar
	 * @return o identificador do objeto
	 */
	public abstract Object getId(E e);

	private Class<E> entityClass;

	@In
	protected EntityManager entityManager;
	
	@Logger
	protected Log logger;

	/**
	 * Identifica a classe tratada pela herdeira.
	 */
	@Create
	public void init(){
		this.entityClass = getEntityClass();
	}

	/**
	 * Envia as modificações nos objetos gerenciados pelo EntityManager ao banco de dados,
	 * que serão tornadas definitivas caso a transação seja completada com sucesso.
	 * 
	 * @see EntityManager#flush()
	 */
	public void flush(){
		getEntityManager().flush();
	}

	/**
	 * Recupera, do gerenciador de entidades ou do banco de dados, o objeto com id dado.
	 * 
	 * @param id o identificador do objeto a ser recuperado
	 * @return o objeto recuperado ou null, se inexistente.
	 * 
	 * @see EntityManager#find(Class, Object)
	 */
	public E find(Object id){
		return getEntityManager().find(entityClass, id);
	}

	/**
	 * Recupera todos os objetos existentes no banco do tipo.
	 * 
	 * @return a lista de objetos existentes.
	 */
	public List<E> findAll(){
		return findByRange(null, null, null, null);
	}

	/**
	 * Recupera os objetos dados existentes no banco de dados que estão vinculados a uma outra entidade pelo nome de propriedade
	 * dado, permitindo a paginação.
	 * 
	 * @param firstRow a primeira linha a ser recuperada, ou null para a primeira linha existente.
	 * @param maxRows o máximo de objetos a ser recuperado na chamada, ou null para não haver a paginação
	 * @param owner o objeto vinculado à entidade E como propriedade propertyName, ou null, para dispensar a restrição
	 * @param propertyName o nome da propriedade owner na entidade E, de modo a permitir fazer uma
	 * restrição de consulta tal que os objetos retornados sejam todos os E em que e.propertyName = owner, ou null,
	 * para dispensar a restrição
	 * @return a lista de objetos E que têm a propriedade propertyName == owner, paginados ou não.
	 * 
	 * @throws IllegalArgumentException caso tenha sido indicado o owner, mas não tenha sido indicado o nome
	 * da propriedade
	 */
	@SuppressWarnings("unchecked")
	public <O>List<E> findByRange(Integer firstRow, Integer maxRows, O owner, String propertyName){
		StringBuilder queryStr = new StringBuilder("SELECT o FROM " + entityClass.getCanonicalName() + " AS o ");
		Query q = null;
		if (owner != null){
			if (propertyName == null){
				throw new IllegalArgumentException("Não é possível encontrar entidades ["
					+ entityClass.getCanonicalName() + "] vinculadas à entidade ["
					+ owner.getClass().getCanonicalName() + "] sem a indicação da propriedade pertinente.");
			}
			else{

				queryStr.append("WHERE ");
				queryStr.append("o.");
				queryStr.append(propertyName);
				queryStr.append(" = :owner");
				q = EntityUtil.createQuery(getEntityManager(), queryStr, false, true, "BaseDAO.findByRange: " + entityClass.getSimpleName());
				q.setParameter("owner", owner);
			}
		}
		else{
			q = EntityUtil.createQuery(getEntityManager(), queryStr, false, true, "BaseDAO.findByRange: " + entityClass.getSimpleName());
		}
		if (firstRow != null && firstRow.intValue() > 0){
			q.setFirstResult(firstRow);
		}
		if (maxRows != null && maxRows.intValue() > 0){
			q.setMaxResults(maxRows);
		}
		return q.getResultList();
	}

	/**
	 * Torna uma entidade dada gerenciada pelo JPA.
	 * 
	 * @param e a entidade a ser gerenciada.
	 * @return a entidade já gerenciada
	 * @see EntityManager#persist(Object)
	 */
	public E persist(E e){
		getEntityManager().persist(e);
		return e;
	}

	/**
	 * Descarta a situação atual do objeto dado, recarregando seus dados do banco de dados.
	 * 
	 * @param e a entidade a ser recarregada.
	 * @return a entidade recarregada com os dados do banco
	 * 
	 * @see EntityManager#refresh(Object)
	 */
	public E refresh(E e){
		getEntityManager().refresh(e);
		return e;
	}

	/**
	 * Remove a entidade do gerenciamento objeto-relacional.
	 * 
	 * @param e a entidade a ser removida
	 * 
	 * @see EntityManager#remove(Object)
	 */
	public void remove(E e){
		getEntityManager().remove(e);
	}

	/**
	 * Traz novamente ao gerenciamento de entidades objeto-relacional uma entidade
	 * que tenha sido dele desligada, fazendo as atribuições pertinentes ao estado atual.
	 * 
	 * @param e a entidade a ser novamente gerenciada.
	 * @return a entidade gerenciada.
	 * 
	 * @see EntityManager#merge(Object)
	 */
	public E merge(E e){
		E r = getEntityManager().merge(e);
		return r;
	}

	/**
	 * Recupera a classe do objeto gerenciado pela DAO.
	 * 
	 * @return a classe do objeto gerenciado.
	 */
	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass(){
		if (entityClass == null){
			Type type = getClass().getGenericSuperclass();
			if (type instanceof ParameterizedType){
				ParameterizedType paramType = (ParameterizedType) type;
				if (paramType.getActualTypeArguments().length == 2){
					if (paramType.getActualTypeArguments()[1] instanceof TypeVariable){
						throw new IllegalArgumentException("Could not guess entity class by reflection");
					}
					else{
						entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
					}
				}
				else{
					entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
				}
			}
			else{
				throw new IllegalArgumentException("Could not guess entity class by reflection");
			}
		}
		return entityClass;
	}
	
	/**
	 * Permite incluir um mapa de parâmetros em uma consulta dada.
	 * 
	 * @param q a consulta a ser carregada com os parâmetros.
	 * @param parametros mapa contendo o nome e o valor dos parâmetros a serem carregados na consulta.
	 * @return a consulta atualizada com os parâmetros.
	 */
	protected Query loadParameters(Query q, Map<String, Object> parametros){
		for(String key: parametros.keySet()){
			q.setParameter(key, parametros.get(key));
		}
		return q;
	}

	/**
	 * Recupera o gerenciador de entidades objeto-relacional desta DAO.
	 *
	 * @return o gerenciador de entidades
	 */
	protected EntityManager getEntityManager(){
		return entityManager;
	}
	
	/**
	 * Recupera a lista de entidades que atendam aos critérios dados pelo objeto de consulta {@link Search}.
	 *  
	 * @param search o objeto de consulta
	 * @return a lista de objetos consultados
	 * @throws PJeBusinessException caso tenha havido algum erro ao realizar a consulta.
	 * @since 1.4.6.2.RC4
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> list(Search search){
		Map<String, Object> params = new HashMap<String, Object>();
		String query = createQueryString(search, params);
		Query q = EntityUtil.createQuery(getEntityManager(), query, search.isReadOnly(), search.isCacheable(), getClass().getSimpleName() + '§'+ search);
		loadParameters(q, params);
		if(search.getFirst() != null && search.getFirst().intValue() > 0){
			q.setFirstResult(search.getFirst());
		}
		if(search.getMax() != null && search.getMax().intValue() > 0){
			q.setMaxResults(search.getMax());
		}
		return (List<T>) q.getResultList();
	}
	
	/**
	 * Monta uma consulta JPQL a partir de um {@link Search} dado, inserindo os parâmetros
	 * pertinentes no mapa de parâmetros repassado.
	 *  
	 * @param search o componente de consulta
	 * @param params mapa no qual serão inseridos os parâmetros
	 * @return a consulta JPQL montada
	 * @throws NoSuchFieldException quando, nos critérios de consulta
	 * 
	 *  @see #loadParameters(Query, Map)
	 */
	protected final String createQueryString(Search search, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder("SELECT ");
		sb.append(createSelectClause(search));
		sb.append(" FROM " + search.getEntityClass().getCanonicalName() + " AS o ");
		if(search != null && search.getCriterias().size() > 0){
			loadJoinCriterias(sb, search);
			sb.append(" WHERE ");
			loadCriterias(sb, search, params);
		}
		return StringUtil.fullTrim(sb.toString());
	}
	
	/**
	 * Constrói uma cláusula SELECT básica para um dado {@link Search}.
	 * 
	 * @param search o objeto de consulta
	 * @return o conteúdo necessário para o conteúdo do select
	 */
	private String createSelectClause(Search search){
		StringBuilder sb = new StringBuilder();
		String retrieve = null;
		boolean distinct = search.isDistinct();
		boolean count = search.isCount();
		if (search.getRetrieveField() != null
				&& (search.getRetrieveField().toLowerCase().contains("new ") 
						|| search.getRetrieveField().toLowerCase().contains("count("))) {
			retrieve = search.getRetrieveField();
		}else{
			if(search.getRetrieveField() != null){
				retrieve = "o." + search.getRetrieveField();
			}else{
				retrieve = "o";
			}
		}
		if(count){
			if(distinct || search.getGroupBy() != null){
				sb.append(String.format("COUNT(DISTINCT %s)", retrieve));
			}else{
				sb.append(String.format("COUNT(%s)", retrieve));
			}
		}else{
			if(distinct){
				sb.append(String.format("DISTINCT %s", retrieve));
			}else{
				sb.append(retrieve);
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Inclui, em um StringBuilder que presuntivamente já contém a cláusula de SELECT, as referências de 
	 * JOINS e WHERE decorrentes do objeto de consulta, preenchendo, ainda, o mapa de parâmetros
	 * que deverão ser posteriormente repassados para a {@link Query}.
	 * 
	 * @param sb o StringBuilder onde serão acrescentados os campos pertinentes do JPQL
	 * @param search o objeto de consulta
	 * @param params mapa no qual serão inseridos os parâmetros para posterior inclusão na {@link Query}
	 */
	protected void loadCriterias(StringBuilder sb, Search search, Map<String, Object> params) {
		loadCriterias(sb, search.getCriterias().values(), params);

		if(search.getGroupBy() != null && !search.getGroupBy().isEmpty() && !search.isCount()){
			loadGroupBy(sb, search.getGroupBy());
		}
		
		if(!search.isCount()){
			loadOrderBy(sb, search.getOrders());
		}
	}
	
	protected void loadCriterias(StringBuilder sb, Collection<Criteria> criterias, Map<String, Object> params) {
		boolean first = true;
		for(Criteria c: criterias){
			Operator op = c.getOperator();
			if(op == Operator.not || (c.isChild() && c.getParent().getOperator() != Operator.not)){
				// O tratamento do NOT é feito na construção concreta do critério.
				continue;
			}else if(c.isChild()){
				Criteria tmp = c;
				boolean goon = false;
				while((tmp = tmp.getParent()) != null && !goon){
					if(tmp.getOperator() == Operator.and || tmp.getOperator() == Operator.or){
						goon = true;
					}
				}
				if(goon)
					continue;
			}
			if(c.getOperator() == Operator.or){
				loadOR(sb, params, c, !first);
			}else{
				loadCriteria(sb, params, c, !first);
			}
			if(first){
				first = false;
			}
		}
	}
	
	/**
	 * Inclui, no {@link StringBuilder} dado, as cláusulas de JOIN decorrentes do objeto de consulta.
	 * 
	 * @param sb o {@link StringBuilder} ao qual serão acrescentadas, se necessárias, as cláusulas de JOIN
	 * @param search o objeto de consulta
	 */
	private void loadJoinCriterias(StringBuilder sb, Search search){
		for(Entry<String, JoinAlias> entry: search.getJoins().entrySet()){
			JoinAlias join = entry.getValue();
			if(join.isRequired()){
				sb.append(" INNER JOIN ");
			}else{
				sb.append(" LEFT JOIN ");
			}
			sb.append(join.toString());
			sb.append(" AS ");
			sb.append(join.getAlias());
		}
	}
	
	/**
	 * Inclui, no {@link StringBuilder} dado, o critério limitador da consulta JPQL, carregando a lista
	 * de parâmetros e valores no mapa fornecido.
	 * 
	 * @param sb o {@link StringBuilder} no qual serão acrescentados os critérios limitadores
	 * @param params mapa no qual serão incluídos os parâmetros de consulta que devem ser posteriormente
	 * incluidos na {@link Query}
	 * @param criteria o critério limitador da consulta
	 * @param and boolean indicativo de que o critério deve ser carregado com uma precedente cláusula AND
	 */
	private void loadCriteria(StringBuilder sb, Map<String, Object> params, Criteria criteria, boolean and){
		String paramName = "prm" + params.size(); 
		Operator o = criteria.getOperator();
		if(criteria.getValue() == null || ((criteria.getValue().size() == 0 || (criteria.getValue().size() == 1	&& criteria.getValue().get(0) == null))
										&& o != Operator.empty 
										&& o != Operator.isNull
										&& o != Operator.path
										&& o != Operator.exists
										&& o != Operator.notExists)){
			return;
		}else if(o == Operator.not){
			criteria = (Criteria) criteria.getValue().get(0);
			o = criteria.getOperator();
		}
		if(and){
			sb.append(" AND ");
		}
		boolean negate = criteria.isChild() && criteria.getParent().getOperator() == Operator.not;
		boolean path = !criteria.getValue().isEmpty() && criteria.getValue().get(0) instanceof Criteria && ((Criteria) criteria.getValue().get(0)).getOperator() == Operator.path; 
		switch (o) {
		case startsWith:
		case endsWith:
		case contains:
			sb.append(" LOWER(to_ascii(");
			sb.append(criteria.getTranslate() != null ? criteria.getTranslate() : criteria.getConcreteAttribute());
			
			if(negate){
				sb.append(")) NOT LIKE :");
			}else{
				sb.append(")) LIKE :");
			}
			sb.append(paramName);
			String value = null;
			switch (o) {
			case startsWith:
				value = StringUtil.normalize(criteria.getValue().get(0).toString()).toLowerCase() + "%";
				break;
			case endsWith:
				value = "%" + StringUtil.normalize(criteria.getValue().get(0).toString()).toLowerCase();
				break;
			default:
				value = "%" + StringUtil.normalize(criteria.getValue().get(0).toString()).toLowerCase() + "%";
				break;
			}
			params.put(paramName, value);
			break;
		default:
			switch (o) {
			case equals:
				if(path){
					sb.append(((Criteria) criteria.getValue().get(0)).getConcreteAttribute());
					if(negate){
						sb.append(" != ");
					}else{
						sb.append(" = ");
					}
					sb.append(((Criteria) criteria.getValue().get(1)).getConcreteAttribute());
				}else{
					sb.append(criteria.getConcreteAttribute());
					if(negate){
						sb.append(" != :");
					}else{
						sb.append(" = :");
					}
					sb.append(paramName);
					params.put(paramName, criteria.getValue().get(0));
				}
				break;
			case notEquals:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" = :");
				}else{
					sb.append(" != :");
				}
				sb.append(paramName);
				params.put(paramName, criteria.getValue().get(0));
				break;
			case greater:
				if (path) {
					sb.append(((Criteria) criteria.getValue().get(0)).getConcreteAttribute());
					if(negate){
						sb.append(" <= ");
					}else{
						sb.append(" > ");
					}
					sb.append(((Criteria) criteria.getValue().get(1)).getConcreteAttribute());
				} else {
					sb.append(criteria.getConcreteAttribute());
					if(negate){
						sb.append(" <= :");
					}else{
						sb.append(" > :");
					}
					sb.append(paramName);
					params.put(paramName, criteria.getValue().get(0));
				}
				break;
			case greaterOrEquals:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" < :");
				}else{
					sb.append(" >= :");
				}
				sb.append(paramName);
				params.put(paramName, criteria.getValue().get(0));
				break;
			case less:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" >= :");
				}else{
					sb.append(" < :");
				}
				sb.append(paramName);
				params.put(paramName, criteria.getValue().get(0));
				break;
			case lessOrEquals:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" > :");
				}else{
					sb.append(" <= :");
				}
				sb.append(paramName);
				params.put(paramName, criteria.getValue().get(0));
				break;
			case in:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" NOT IN (:");
				}else{
					sb.append(" IN (:");
				}
				sb.append(paramName);
				sb.append(")");
				params.put(paramName, criteria.getValue());
				break;
			case empty:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" IS NOT EMPTY ");
				}else{
					sb.append(" IS EMPTY ");
				}
				break;
			case isNull:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" IS NOT NULL ");
				}else{
					sb.append(" IS NULL ");
				}
				break;
			case between:
				sb.append(criteria.getConcreteAttribute());
				if(negate){
					sb.append(" NOT BETWEEN :");
				}else{
					sb.append(" BETWEEN :");
				}
				sb.append(paramName);
				String param2 = "prm" + (params.size() + 1);
				sb.append(" AND :");
				sb.append(param2);
				params.put(paramName, criteria.getValue().get(0));
				params.put(param2, criteria.getValue().get(1));
				break;
			case bitwiseAnd:
				sb.append(" bitwise_and(");
				sb.append(criteria.getConcreteAttribute());
				sb.append(", :");
				sb.append(paramName);
				if(negate){
					sb.append(") != :");		
				}else{
					sb.append(") = :");
				}
				String param = "prm" + (params.size() + 1);
				sb.append(param);
				params.put(paramName, criteria.getValue().get(0));
				params.put(param, criteria.getValue().get(1));
				break;
			case fulltext:
				sb.append(" full_text(");
				sb.append(criteria.getConcreteAttribute());
				sb.append(", :");
				sb.append(paramName);
				sb.append(") = true");
				params.put(paramName, criteria.getValue().get(0));
				break;
			case exists:
				sb.append(" exists (");
				sb.append(criteria.getConcreteAttribute());
				sb.append(") ");
				break;
			case notExists:
				sb.append(" not exists (");
				sb.append(criteria.getConcreteAttribute());
				sb.append(") ");
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Inclui, no {@link StringBuilder} dado, o critério do tipo OU limitador da consulta JPQL, carregando a lista
	 * de parâmetros e valores no mapa fornecido.
	 * 
	 * @param sb o {@link StringBuilder} no qual serão acrescentados os critérios limitadores
	 * @param params mapa no qual serão incluídos os parâmetros de consulta que devem ser posteriormente
	 * incluidos na {@link Query}
	 * @param criteria o critério limitador da consulta
	 * @param and boolean indicativo de que o critério deve ser carregado com uma precedente cláusula AND
	 */
	private void loadOR(StringBuilder sb, Map<String, Object> params, Criteria criteria, boolean and){
		if(and){
			sb.append(" AND ");
		}
		boolean first = true;
		for(int i = 0; i < criteria.getValue().size(); i++){
			Criteria crit = (Criteria) criteria.getValue().get(i);
			if(first){
				sb.append("(");
			}else{
				sb.append(" OR ");
			}
			if(crit.getOperator() == Operator.and){
				boolean firstAnd = true;
				for(int j = 0; j < crit.getValue().size(); j++){
					Criteria andCrit = (Criteria) crit.getValue().get(j);
					if(firstAnd){
						sb.append("(");
					}
					loadCriteria(sb, params, andCrit, !firstAnd);
					firstAnd = false;
				}
				if(!firstAnd){
					sb.append(")");
				}
			}else if(crit.getOperator() == Operator.or){
				loadOR(sb, params, crit, false);
			}else{
				loadCriteria(sb, params, crit, false);
			}
			first=false;
		}
		sb.append(")");
		return;
	}
	
	/**
	 * Inclui, no {@link StringBuilder} dado, as cláusulas de ordenação indicadas.
	 * 
	 * @param sb o {@link StringBuilder} ao qual serão acrescentadas as cláusulas de ordenação
	 * @param orders mapa de campos e direção da ordenação a ser respeitada
	 */
	protected void loadOrderBy(StringBuilder sb, Map<String, Order> orders) {
		boolean ordered = false;
		for(Entry<String, Order> e: orders.entrySet()){
			if(!ordered){
				sb.append(" ORDER BY ");
				ordered = true;
			}else{
				sb.append(",");
			}
			sb.append(e.getKey());
			if(e.getValue() == Order.DESC){
				sb.append(" DESC");
			}
		}
	}
	
	
	private void loadGroupBy(StringBuilder sb, String groupBy) {
		sb.append(" GROUP BY ");
		sb.append(groupBy);
	}
	
	/**
	 * Recupera o número total de registros que atendem aos critérios do objeto de consulta.
	 * 
	 * @param search o objeto de consulta
	 * @return o número total de objetos que atendem aos critérios
	 * @throws IllegalArgumentException caso haja algum erro durante a execução
	 */
	public Long count(Search search){
		search.setCount(true);
		Integer first = search.getFirst();
		Integer rows = search.getMax();
		search.setFirst(null);
		search.setMax(1);
		List<Number> ret = list(search);
		Long count = 0L;
		if(!CollectionUtilsPje.isEmpty(ret)){
			count = ret.get(0).longValue();
		}
		search.setCount(false);
		search.setFirst(first);
		search.setMax(rows);
		return count;
	}

	/**
	 * Retorna a entidade resultado da query passado por parâmetro.
	 * 
	 * @param query
	 * @return Entidade resultado da consulta.
	 */
	@SuppressWarnings("unchecked")
	protected E getSingleResult(Query query) {
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
	
	public void setQueryParameters(Query query, Map<String, Object> parameters) {
		if (parameters != null) {
			for (Entry<String, Object> parameterEntry : parameters.entrySet()) {
				query.setParameter(parameterEntry.getKey(), parameterEntry.getValue());
			}
		}
	}	
}
