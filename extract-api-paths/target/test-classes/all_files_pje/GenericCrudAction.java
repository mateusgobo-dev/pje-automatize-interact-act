package br.com.infox.view;

import java.lang.reflect.ParameterizedType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.exceptions.ExceptionHandler;

@Name(GenericCrudAction.NAME)
@Scope(ScopeType.EVENT)
@ExceptionHandler
public class GenericCrudAction<T> extends GenericAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4166556102809039257L;
	public static final String NAME = "genericCrudAction";
	private T instance;
	
	
	public boolean isManaged(){
		boolean isIdDefined = getId() != null && !"".equals( getId().toString() );
		if(getInstance() != null 
				&& isIdDefined 
				&& !genericManager.isManaged(getInstance())){
			setIdInstance(getId());
		}
		return genericManager.isManaged(getInstance());
	}
	
	public void newInstance(){
		if(isManaged()){
			genericManager.clear();
		}
		setId(null);
		clearForm();
		instance = createInstance();
	}
	
	public void persist(){
		super.persist(getInstance());
	}
	
	public void update(){
		super.update(getInstance());
	}
	
	public void remove(){
		super.remove(getInstance());
	}
	
	public T getInstance(){
		if (instance == null) {
			if (this.getId() != null) {
				instance = this.findById(getClassType(),this.getId());
			} else {
				instance = createInstance();
			}
		}
		return instance;
	}
	
	public T createInstance() {
		try {
			return getClassType().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void setInstance(T instance) {
		this.instance = instance;
	}
	
	
	public void setIdInstance(Integer id){
		setInstance(this.genericManager.find(getClassType(), id));
		setId(id);
	}
	
	@SuppressWarnings({"unchecked" })
	private Class<T> getClassType() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}
	
	@Override
	public void onClickSearchTab() {
		newInstance();
	}
	
	@Override
	public void onClickFormTab() {
	}
	
}
