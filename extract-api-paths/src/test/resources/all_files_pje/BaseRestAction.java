package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.Constants;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.pje.nucleo.dto.EntityPageDTO;

@Scope(ScopeType.CONVERSATION)
public abstract class BaseRestAction<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Logger
	protected Log logger;
	
	@In
	protected FacesMessages facesMessages;
	
	@In(required = false)
	protected FacesContext facesContext;	
	
	protected T instance;
	
	protected T searchInstance;
	
	protected EntityPageDTO<T> page;
	
	protected abstract BaseRestClient<T> getRestClient();
	
	public abstract Integer getPageSize();
	
	public abstract Integer getCurrentPage();
	
	public List<T> getModel(){
		List<T> list = new ArrayList<T>(0);
		
		if(this.searchInstance == null) {
			this.page = this.getRestClient().searchResources(this.getCurrentPage(), this.getPageSize());
		} else {
			this.page = this.getRestClient().searchResources(this.getCurrentPage(), this.getPageSize(), this.getSearchInstance());
		}
		
		if(this.page != null && this.page.getContent()!= null && !this.page.getContent().isEmpty()) {
			list = this.page.getContent();
		}
		
		return list;
	}
	
	public void persist(){
		persist(false);
	}
	
	public void persist(boolean newInstance) {
		try {
			instance = getRestClient().createResource(instance);
			if (newInstance) {
				newInstance();
			}
			facesMessages.addFromResourceBundle(Severity.INFO, "alerta.dadosGravadosComSucesso");
		} catch (Exception e) {
			reportMessage(e);
			logger.error("Erro ao persistir " + instance.getClass().getName(), e);
		}
	}
	
	public void update(boolean newInstance) {
		try {
			getRestClient().updateResource(instance);
			if (newInstance) {
				newInstance();
			}
			facesMessages.addFromResourceBundle(Severity.INFO, "alerta.dadosGravadosComSucesso");
		} catch (Exception e) {
			reportMessage(e);
			logger.error("Erro ao persistir " + instance.getClass().getName(), e);
		}
	}

	public void remove(Integer id){
		try{
			getRestClient().deleteResource(id);
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		} catch (Exception e){
			reportMessage(e);
			logger.error("Erro ao remover " + instance.getClass().getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public T newInstance(){
		try{
			instance = (T) getGenericClass(0).newInstance();
			return instance;
		} catch (InstantiationException e){
			e.printStackTrace();
		} catch (IllegalAccessException e){
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T newSearchInstance(){
		try{
			searchInstance = (T) getGenericClass(0).newInstance();
			return searchInstance;
		} catch (InstantiationException e){
			e.printStackTrace();
		} catch (IllegalAccessException e){
			e.printStackTrace();
		}
		return null;
	}	

	public void setInstance(T instance){
		this.instance = instance;
	}

	public T getInstance(){
		return instance;
	}
	
	public T getSearchInstance() {
		return searchInstance;
	}
	
	public void setSearchInstance(T searchInstance) {
		this.searchInstance = searchInstance;
	}

	private Class<?> getGenericClass(int index){
		Type type = ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[index];
		return (Class<?>) type;
	}

	public List<T> findAll(){
		try{
			return getRestClient().getResources();
		} catch (Exception e){
			reportMessage(e);
			logger.error("Erro no findAll " + instance.getClass().getName(), e);
		}
		return null;
	}

	public T findById(Integer id){
		try{
			return (T) getRestClient().getResourceById(id);
		} catch (Exception e){
			reportMessage(e);
			logger.error("Erro no findById " + instance.getClass().getName(), e);
		}
		return null;
	}

	public void clearForm(String formName) {
		if (formName != null && !formName.trim().isEmpty()) {
			facesContext.getViewRoot().findComponent(formName).getChildren().clear();
		}
	}

	public void reportMessage(String code, Object... params){
		if (code.contains(Constants.PREFIXO_ERROR)){
			logger.error(code, params);
			facesMessages.addFromResourceBundle(Severity.ERROR, code, params);
		} else if (code.contains(Constants.PREFIXO_INFO)){
			logger.info(code, params);
			facesMessages.addFromResourceBundle(Severity.INFO, code, params);
		} else if (code.contains(Constants.PREFIXO_WARN)){
			logger.warn(code, params);
			facesMessages.addFromResourceBundle(Severity.WARN, code, params);
		} else if (code.contains(Constants.PREFIXO_FATAL)){
			logger.fatal(code, params);
			facesMessages.addFromResourceBundle(Severity.FATAL, code, params);
		} else{
			logger.warn(code, params);
			facesMessages.addFromResourceBundle(Severity.WARN, code, params);
		}
	}

	public void reportMessage(Exception e){
		e.printStackTrace();
		if (e instanceof PJeException){
			reportMessage(((PJeException) e).getCode(), ((PJeException) e).getParams());
		}else{
			reportMessage(Constants.PJE_DEFAULT_ERROR_MSG, e);
		}
	}

	public boolean isManaged(){
		return (instance != null);
	}
  	
  	public void onClickSearchTab() {
  		newInstance();
  	}
  	
  	public void onClickFormTab() {
  		
  	}	
  	
  	public EntityPageDTO<T> getPage() {
		return page;
	}
  	
  	public void setPage(EntityPageDTO<T> page) {
		this.page = page;
	}
}