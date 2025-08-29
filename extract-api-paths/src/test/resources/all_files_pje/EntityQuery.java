/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.itx.component.query;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.DataModel;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import br.com.itx.component.MeasureTime;
import br.com.itx.component.Util;
import br.com.itx.util.HibernateUtil;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;

public class EntityQuery extends org.jboss.seam.framework.EntityQuery{

	private static final LogProvider log = Logging.getLogProvider(EntityQuery.class);

	private static final long serialVersionUID = 1L;

	private List<String> conditions = new ArrayList<String>();

	private String persistenceContextName;

	private String countEjbql;

	private String beforeResultEvent;

	protected List fullList;

	protected Long resultCount;
	
	private String mostraConsultaSemFiltro;

	public List<String> getConditions(){
		return conditions;
	}

	public void setConditions(List<String> conditions){
		this.conditions = conditions;
	}

	@Override
	public String getPersistenceContextName(){
		if (persistenceContextName == null){
			return super.getPersistenceContextName();
		}
		return persistenceContextName;
	}

	public void setPersistenceContextName(String persistenceContextName){
		this.persistenceContextName = persistenceContextName;
	}

	public boolean isQueryCached() {
		return true;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	protected javax.persistence.Query createQuery() {
		javax.persistence.Query query = super.createQuery();

		if (isQueryCached()) {
			query
					.setHint("org.hibernate.cacheable", Boolean.TRUE.toString())
					.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.USE)
					.setHint("javax.persistence.cache.storeMode", CacheStoreMode.USE)
					.setHint("org.hibernate.cacheRegion", "Intempestivo");
		} else {
			query
					.setHint("org.hibernate.cacheable", Boolean.FALSE.toString())
					.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
					.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
		}

		if (isReadOnly()) {
			query.setHint("org.hibernate.readOnly", Boolean.TRUE);
		}

		if (getMaxResults() != null && getMaxResults() > 0) {
			query.setHint("org.hibernate.fetchSize", Math.min(500, getMaxResults()));
		}

		if (!query.getHints().containsKey(HibernateUtil.COMMENT_KEY))
			query.setHint(HibernateUtil.COMMENT_KEY, getQueryHint());
		
		return query;
	}

	protected String getQueryHint(){
		StringBuilder sb = new StringBuilder(getClass().getSimpleName())
			//.append(" - ")
			//.append((String) Util.instance().eval("#{org.jboss.seam.component.name}"))
			.append(" - ")
			.append(getRenderedEjbql());
		
		return sb.toString();
	}
	
	@Override
	public String getCountEjbql(){
		if (countEjbql == null){
			return super.getCountEjbql();
		}
		return countEjbql;
	}

	public void setCountEjbql(String countEjbql){
		this.countEjbql = countEjbql;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getResultList(){
		if (!(mostrarConsultaSemFiltro() || verificarFiltroPreenchido()) && checkConditions()) {
			return null;
		}
		MeasureTime mt = new MeasureTime(true);
		if (beforeResultEvent != null && !"".equals(beforeResultEvent)){
			Events.instance().raiseEvent(beforeResultEvent);
		}
		List resultListSuper = super.getResultList();
		mt.stop();
		return resultListSuper;
	}

	@Override
	public DataModel getDataModel(){
		MeasureTime mt = new MeasureTime(true);
		DataModel dataModel = super.getDataModel();
		mt.stop();
		log.info("getDataModel(): " + mt.getTime());
		return dataModel;
	}

	@SuppressWarnings("rawtypes")
	public List getFullList(){
		if (fullList != null){
			return fullList;
		}
		Integer firstResult = getFirstResult();
		Integer maxResults = getMaxResults();
		setFirstResult(null);
		setMaxResults(null);
		List listaCompleta = getResultList();
		setFirstResult(firstResult);
		setMaxResults(maxResults);
		fullList = listaCompleta;
		return fullList;
	}

	@Override
	public Object getSingleResult(){
		if (!(mostrarConsultaSemFiltro() || verificarFiltroPreenchido()) && checkConditions()) {
			return null;
		}
		return super.getSingleResult();
	}

	@Override
	public Long getResultCount(){
		if (!(mostrarConsultaSemFiltro() || verificarFiltroPreenchido()) && checkConditions()) {
			return 0L;
		}
		
		if (beforeResultEvent != null && !"".equals(beforeResultEvent)){
			Events.instance().raiseEvent(beforeResultEvent);
		}
		resultCount = super.getResultCount();
		return resultCount;
	}

	private boolean checkConditions(){
		if (!conditions.isEmpty()){
			for (String s : conditions){
				Boolean condition = new Util().eval(s);
				if (!condition){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Integer getFirstResult(){
		Integer i = super.getFirstResult();
		return (i == null ? 0 : i);
	}

	public String getBeforeResultEvent(){
		return beforeResultEvent;
	}

	public void setBeforeResultEvent(String beforeResultEvent){
		this.beforeResultEvent = beforeResultEvent;
	}

	@Override
	public void refresh() {
		super.refresh();
		fullList = null;
	}

	private boolean mostrarConsultaSemFiltro() {
		boolean resultado = true;
		if (StringUtils.isNotBlank(mostraConsultaSemFiltro)) {
			resultado = new Util().eval(mostraConsultaSemFiltro);
		}
		return resultado;
	}
	
	protected boolean verificarFiltroPreenchido() {
		boolean resultado = false;
		parseEjbql();
		for (int i=0; i < getRestrictions().size(); i++){
			Object parameterValue = ((ValueExpression<?>)getRestrictionParameters().get(i)).getValue();
			if (isRestrictionParameterSet(parameterValue)) {
				resultado = true;
				break;
			}
		}
		return resultado;
	}

	public void setMostraConsultaSemFiltro(String mostraConsultaSemFiltro) {
		this.mostraConsultaSemFiltro = mostraConsultaSemFiltro;
	}
	
}