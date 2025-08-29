package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;

import br.jus.pje.nucleo.util.StringUtil;

public class PaginatedDataModel<T> extends SerializableDataModel {
 
	private static final long serialVersionUID = -5630301692371701333L;
	private Serializable currentId;
    private Class<T> entityClass;
    private String query;
    private List<Serializable> wrappedKeys;
    private Map<Serializable, T> wrappedData = new HashMap<Serializable, T>();
    private String entityManagerName;
    private HashMap<String, Object> parameters;
    private Query _query;
    private String idAttribute;
    private Integer rowCount;
 
    public PaginatedDataModel(Class<T> entityClass, String entityManagerName, String query, HashMap<String, Object> parameters, String idAttribute) {
        super();
        load(entityClass, entityManagerName, query, parameters, idAttribute);
    }
 
    public PaginatedDataModel(Class<T> entityClass, StringBuffer query, HashMap<String, Object> parameters, String idAttribute) {
        super();
        load(entityClass, null, query.toString(), parameters, idAttribute);
    }
 
    public PaginatedDataModel(Class<T> entityClass, String query, HashMap<String, Object> parameters) {
        super();
        load(entityClass, null, query, parameters, null);
    }
 
    public PaginatedDataModel(Class<T> entityClass, StringBuffer query, HashMap<String, Object> parameters) {
        super();
        load(entityClass, null, query.toString(), parameters, null);
    }
 
    private void load(Class<T> entityClass, String entityManagerName, String query, HashMap<String, Object> parameters, String idAttribute) {
        this.entityClass = entityClass;
        if (StringUtil.isNotEmpty(entityManagerName)) {
            this.entityManagerName = entityManagerName;
        } else {
            this.entityManagerName = "entityManager";
        }
 
        if (StringUtil.isNotEmpty(idAttribute)) {
            this.idAttribute = idAttribute;
        } else {
            this.idAttribute = entityClass.getSimpleName().toLowerCase() + "Id";
        }
        this.parameters = parameters;
        this.query = query;
        this.rowCount = null;
    }
 
    private EntityManager getEntityManager() {
        return (EntityManager) Component.getInstance(entityManagerName, true);
    }
    
    private Query buildQueryCount() {
        _query = null;
        return buildQuery();
    }
    
    private Query buildQuery() {
        if (_query == null) {
            _query = getEntityManager().createQuery(query.toString());
            for (String key : parameters.keySet()) {
                _query.setParameter(key, parameters.get(key));
            }
        }
        return _query;
    }

    @Override
    public Object getRowKey() {
        return currentId;
    }
 
    /**
     * This method normally called by Visitor before request Data Row.
     */
    @Override
    public void setRowKey(Object key) {
        currentId = (Serializable) key;
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public void walk(FacesContext fCtx, DataVisitor visitor, Range range,
            Object argument) throws IOException {
        int firstResult = ((SequenceRange) range).getFirstRow();
        int maxResults = ((SequenceRange) range).getRows();
 
        buildQuery().setFirstResult(firstResult);
        buildQuery().setMaxResults(maxResults);
 
        List<T> list = buildQuery().getResultList();
 
        wrappedKeys = new ArrayList<Serializable>();
        wrappedData = new HashMap<Serializable, T>();
 
        for (T row : list) {
        	Serializable id = getId(row);
            wrappedKeys.add(id);
            wrappedData.put(id, row);
            visitor.process(fCtx, id, argument);
        }
    }
    
    @Override
    public int getRowCount() {
        if (rowCount==null) {
            rowCount = buildQueryCount().getResultList().size();
        }
        return rowCount;
    }
 
    @Override
    public Object getRowData() {
    	Object retorno = null;
        if (currentId != null) {
            T ret = wrappedData.get(currentId);
            if (ret != null) {
            	retorno = ret;
            }else{
            	ret = (T) getEntityManager().find(entityClass, currentId);
            	if (ret != null) {
            		wrappedKeys.add(currentId);
            		wrappedData.put(currentId, ret);
            		retorno = ret;
            	}
            }
        }
        return retorno;
    }
 
    @Override
    public int getRowIndex() {
        return 0;
    }
    
    @SuppressWarnings("unchecked")
	public List<T> getList(){
    	return buildQuery().getResultList();
    }
 
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public boolean isRowAvailable() {
    	boolean retorno = false;
        if (currentId != null) {
        	T ret = wrappedData.get(currentId);
        	if (ret!=null){
        		retorno = true;
        	}else{
        		T row = getEntityManager().find(entityClass, currentId);
        		if (row!=null && wrappedKeys != null){
        			wrappedKeys.add(currentId);
        			wrappedData.put(currentId, wrappedData.put(currentId, row));
        			retorno = true;
        		}
        	}
        }
        return retorno;
    }
 
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public void setRowIndex(int newRowIndex) {
    }
 
    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException();
    }
 
    private Serializable getId(T row) {
        Serializable id;
        try {
            id = (Serializable) PropertyUtils.getProperty(row, idAttribute);
        } catch (Exception e) {
            throw new javax.faces.FacesException("Failed to obtain row id", e);
        }
        return id;
    }

	@Override
	public void update() {
		
	}
}