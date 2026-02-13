/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.indexacao.Indexer;
import br.jus.pje.indexacao.IndexingMapping;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Operator;
import br.jus.pje.search.Search;

/**
 * Classe abstrata definidora de chamadas de acesso a dados a serem indexados ou já indexados
 * em um repositório <a href="http://www.elasticsearch.org">ElasticSearch</a>.
 * 
 * Os componentes derivados desta classe têm escopo de evento. 
 * 
 * @author cristof
 *
 */
public abstract class ElasticDAO <E> {
	
	private Indexador indexador;
	
	private Indexer indexer = new Indexer();
	
	public void setIndexador(Indexador indexador){
		this.indexador = indexador;
	}
	
	public void setIndexer(Indexer indexer){
		this.indexer = indexer;
	}
	
	public JSONObject search(Search s) throws PJeBusinessException, JSONException{
		JSONObject q = buildQuery(s, indexador.getMapaIndexacao());
		try {
			IndexedEntity data = s.getEntityClass().getAnnotation(IndexedEntity.class);
			return indexador.getSearchProvider().search(data.value(), q);
		} catch (PJeRuntimeException e) {
			throw new PJeBusinessException(e.getCode(), e, e.getParams());
		} catch (Throwable e) {
			throw new PJeBusinessException("pje.search.elastic.error", e, e.getLocalizedMessage());
		}
	}
	
	protected JSONObject buildQuery(Search s, Map<Class<?>, IndexingMapping> mapa) throws JSONException{
		StringBuilder query = new StringBuilder();
		
		if(s.getEntityClass().toString().contains("ProcessoDocumento")){
			query.append("{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[]}},\"filter\":{\"bool\":{\"must\":[]}}}}");
			query.append(",\"highlight\":{\"fields\":{\"binario.modeloDocumento\":{\"fragment_size\":300}}}}");
		} else {
			query.append("{\"query\":{\"bool\":{\"must\":[]}},\"filter\":{\"bool\":{\"must\":[]}}}");
			//query.append("{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[]}},\"filter\":{\"bool\":{\"must\":[]}}}}}");
		}
		JSONObject ret = new JSONObject(query.toString());
		
		//JSONArray must = ret.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("bool").getJSONArray("must");
		//JSONArray filter = ret.getJSONObject("query").getJSONObject("filtered").getJSONObject("filter").getJSONObject("bool").getJSONArray("must");
		JSONArray must = ret.getJSONObject("query").getJSONObject("bool").getJSONArray("must");
		JSONArray filter = ret.getJSONObject("filter").getJSONObject("bool").getJSONArray("must");
		
		if(!s.getCriterias().isEmpty()){
			loadCriterias(s, must, filter, mapa);
		}
		if(s.getFirst() != null && s.getFirst() > 0){
			ret.put("from", s.getFirst());
		}
		if(s.getMax() != null && s.getMax() > 0){
			ret.put("size", s.getMax());
		}
		if(must.length() == 0){
			//ret.getJSONObject("query").getJSONObject("filtered").remove("query");
			ret.remove("query");
		}
		if(filter.length() == 0){
			//ret.getJSONObject("query").getJSONObject("filtered").remove("filter");
			ret.remove("filter");
		}
		return ret;
	}
	
	private void loadCriterias(Search s, JSONArray must, JSONArray filter, Map<Class<?>, IndexingMapping> mapa) throws JSONException {
		Set<Criteria> processed = new HashSet<Criteria>();
		for(Criteria c: s.getCriterias().values()){
			if(processed.contains(c) || c.getParent() != null){
				continue;
			}
			Operator op = c.getOperator();
			Class<?> clazz = s.getEntityClass();
			switch (op) {
			case and:
				JSONObject andclause = loadAnd(c, clazz, mapa, processed);
				includeCriteria(must, filter, andclause, c.isFilter());
				break;
			case or:
				JSONObject orclause = loadOr(c, clazz, mapa, processed);
				includeCriteria(must, filter, orclause, c.isFilter());
				break;
			case not:
				JSONObject notclause = loadNot(c, clazz, mapa, processed);
				includeCriteria(must, filter, notclause, true);
			default:
				break;
			}
		}
		for(Criteria c: s.getCriterias().values()){
			if(processed.contains(c)){
				continue;
			}
			Class<?> clazz = s.getEntityClass();
			JSONObject crit = loadCriteria_(c, clazz, mapa, processed);
			includeCriteria(must, filter, crit, c.isFilter() || c.getOperator() == Operator.isNull || c.getOperator() == Operator.empty || c.getOperator() == Operator.notEquals);
		}
	}
	
	private void includeCriteria(JSONArray must, JSONArray filter, JSONObject criteria, boolean asFilter){
		if(asFilter){
			filter.put(criteria);
		}else{
			must.put(criteria);
		}
	}
	
	private JSONObject loadAnd(Criteria c, Class<?> clazz, Map<Class<?>, IndexingMapping> mapa, Set<Criteria> processed) throws JSONException{
		if(c.getOperator() != Operator.and){
			throw new IllegalArgumentException("Incompatible operator: " + c.getOperator());
		}
		JSONObject query = new JSONObject("{\"bool\":{\"must\":[]}}");
		JSONArray andmust = query.getJSONObject("bool").getJSONArray("must");
		for(Object o: c.getValue()){
			Criteria and = (Criteria) o;
			JSONObject crit = loadCriteria_(and, clazz, mapa, processed);
			andmust.put(crit);
		}
		processed.add(c);
		return query;
	}
	
	private JSONObject loadOr(Criteria c, Class<?> clazz, Map<Class<?>, IndexingMapping> mapa, Set<Criteria> processed) throws JSONException{
		if(c.getOperator() != Operator.or){
			throw new IllegalArgumentException("Incompatible operator: " + c.getOperator());
		}
		JSONObject query = new JSONObject("{\"bool\":{\"should\":[]}}");
		JSONArray orshould = query.getJSONObject("bool").getJSONArray("should");
		for(Object o: c.getValue()){
			Criteria or = (Criteria) o;
			JSONObject crit = loadCriteria_(or, clazz, mapa, processed);
			orshould.put(crit);
		}
		processed.add(c);
		return query;
	}
	
	private JSONObject loadNot(Criteria c, Class<?> clazz, Map<Class<?>, IndexingMapping> mapa, Set<Criteria> processed) throws JSONException{
		if(c.getOperator() != Operator.not){
			throw new IllegalArgumentException("Incompatible operator: " + c.getOperator());
		}
		JSONObject not = new JSONObject();
		Criteria negated = (Criteria) c.getValue().get(0);
		negated.asFilter();
		not.put("not", loadCriteria_(negated, clazz, mapa, processed));
		processed.add(c);
		return not;
	}
	
	private JSONObject loadCriteria_(Criteria c, Class<?> clazz, Map<Class<?>, IndexingMapping> mapa, Set<Criteria> processed) throws JSONException{
		JSONObject crit = null;
		switch (c.getOperator()) {
		case or:
			crit = loadOr(c, clazz, mapa, processed);
			break;
		case and:
			crit = loadAnd(c, clazz, mapa, processed);
			break;
		case not:
			crit = loadNot(c, clazz, mapa, processed);
			break;
		case equals:
		case startsWith:
		case contains:
		case notEquals:
		case greater:
		case greaterOrEquals:
		case less:
		case lessOrEquals:
		case between:
		case in:
			crit = loadConcreteCriteria(c, clazz, mapa);
			break;
		default:
			break;
		}
		processed.add(c);
		return crit;
	}
	
	private JSONObject loadConcreteCriteria(Criteria c, Class<?> clazz, Map<Class<?>, IndexingMapping> mapa) throws JSONException{
		JSONObject q = new JSONObject();
		String path = indexer.translate(clazz, c.getAttribute(), mapa);
		if(!mapa.get(clazz).getPrimitivos().containsKey(c.getAttribute()) && !path.equals("id_")){
			String[] flds = c.getAttribute().split("\\.");
			if(flds.length > 1){
				for(String fld: flds){
					if(mapa.get(clazz).getObjetos().containsKey(fld)){
						JSONObject inner = new JSONObject();
						String nestedPath = indexer.translate(clazz, c.getAttribute(), mapa);
						inner.put("path", nestedPath.substring(0, nestedPath.lastIndexOf('.')));
						inner.put("query", createQuery_(c, path, true));
						q.put("nested", inner);
					}
				}
			}
		}else{
			q = createQuery_(c, path, false);
		}
		return q;
	}
	
	private JSONObject createQuery_(Criteria c, String path, boolean forceMatch) throws JSONException{
		Operator op = c.getOperator();
		switch (op) {
		case equals:
			return createMatch(c, path, forceMatch, true);
		case contains:
		case endsWith:
			return createMatch(c, path, forceMatch, false);
		case less:
		case lessOrEquals:
		case greater:
		case greaterOrEquals:
		case between:
			return createRangeCriteria(c, path);
		case in:
			return createInCriteria(c, path);
		case startsWith:
			return createPrefix(c, path);
		case empty:
		case isNull:
			return createNotExistsCriteria(c, path);
		case notEquals:
			return createNotEquals(c, path);
		case path:
			break;
		default:
		}
		throw new UnsupportedOperationException("Criteria " + op + " not supported in this method");
	}
		
	private JSONObject createMatch(Criteria c, String path, boolean forceMatch, boolean forceEquals) throws JSONException{
		String clause = c.isFilter() && !forceMatch ? "term" : (forceEquals ? "match_phrase" : "match");
		if(Collection.class.isAssignableFrom(c.getValue().getClass()) && ((Collection<?>) c.getValue()).size() > 1){
			return new JSONObject("{\"" + clause + "\":{\""+ path + "\":\""+StringUtils.join(c.getValue(), " ") + "\"}}");
		}else{
			JSONObject ret = new JSONObject();
			JSONObject innerpath = new JSONObject();
			Object value = getValue(c.getValue().get(0));
			if(value != null && value instanceof String && clause.equals("match") && ((String) value).contains(" ")){
				JSONObject opq = new JSONObject();
				opq.put("query", value);
				opq.put("operator", "and");
				innerpath.put(path, opq);
			}else{
				innerpath.put(path, value);
			}
			ret.put(clause, innerpath);
			return ret;
		}
	}
	
	private JSONObject createPrefix(Criteria c, String path) throws JSONException{
		String clause = "prefix";
		if(Collection.class.isAssignableFrom(c.getValue().getClass()) && ((Collection<?>) c.getValue()).size() > 1){
			return new JSONObject("{\"" + clause + "\":{\""+ path + "\":\""+StringUtils.join(c.getValue(), " ") + "\"}}");
		}else{
			JSONObject ret = new JSONObject();
			JSONObject inner = new JSONObject();
			inner.put(path, getValue(c.getValue().get(0)));
			ret.put(clause, inner);
			return ret;
		}
	}
	
	private JSONObject createRangeCriteria(Criteria c, String path) throws JSONException{
		Operator op = c.getOperator();
		JSONObject ret = new JSONObject("{\"range\":{\"" + path + "\":{}}}");
		JSONObject rpath = ret.getJSONObject("range").getJSONObject(path);
		switch (op) {
		case less:
			rpath.put("lt", getValue(c.getValue().get(0)));
			break;
		case lessOrEquals:
			rpath.put("lte", getValue(c.getValue().get(0)));
			break;
		case greater:
			rpath.put("gt", getValue(c.getValue().get(0)));
			break;
		case greaterOrEquals:
			rpath.put("gte", getValue(c.getValue().get(0)));
			break;
		case between:
			rpath.put("gte", getValue(c.getValue().get(0)));
			rpath.put("lte", getValue(c.getValue().get(1)));
			break;
		default:
			throw new IllegalArgumentException("Incompatible criteria to create a match query: " + c.getOperator());
		}
		return ret;
	}
	
	private JSONObject createInCriteria(Criteria c, String path) throws JSONException{
		JSONObject ret = new JSONObject("{\"bool\":{\"should\":[]}}");
		JSONArray should = ret.getJSONObject("bool").getJSONArray("should");
		for(Object o: c.getValue()){
			JSONObject aux = new JSONObject();
			aux.put(path, getValue(o));
			JSONObject match = new JSONObject();
			match.put("match", aux);
			should.put(match);
		}
		return ret;
	}
	
	private JSONObject createNotExistsCriteria(Criteria c, String path) throws JSONException{
		JSONObject exists = new JSONObject();
		exists.put("exists", new JSONObject("{\"field\":\"" + path + "\"}"));
		JSONObject ret = new JSONObject();
		ret.put("not", exists);
		return ret;
	}

	private JSONObject createNotEquals(Criteria c, String path) throws JSONException{
		JSONObject not = new JSONObject();
		c.asFilter();
		not.put("not", createMatch(c, path, false, true));
		return not;
	}
	
	private Object getValue(Object o){
		if(o == null){
			return null;
		}
		if(Calendar.class.isAssignableFrom(o.getClass())){
			return new Timestamp(((Calendar) o).getTimeInMillis());
		}else if(Date.class.isAssignableFrom(o.getClass())){
			return new Timestamp(((Date) o).getTime());
		}
		return o;
	}

}
