package br.com.infox.view;

import java.io.Serializable;

import javax.faces.component.UIComponent;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.exceptions.ExceptionHandler;
import br.com.itx.util.ComponentUtil;

@Name(GenericAction.NAME)
@Scope(ScopeType.EVENT)
@ExceptionHandler
public class GenericAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1132374490998932945L;
	public static final String NAME = "GenericAction";
	@In
	protected GenericManager genericManager; 
	private String tab;
	private Integer id;
	
	
	public <T> T persist(T o) {
		T object = genericManager.persist(o);
		FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso.");
		return object;
	}
	
	public <T> T update(T o) {
		T object = genericManager.update(o);
		FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso.");
		return object;
	}
	
	public <T> void remove(T o) {
		genericManager.remove(o);
		FacesMessages.instance().add(Severity.INFO, "Registro removido com sucesso.");
	}
	
	public <T> T findById(Class<T> clazz, Object id) {
		return genericManager.find(clazz, id);
	}
	
	public String getName() {
		String name = null;
		Name nameAnnotation = this.getClass().getAnnotation(Name.class);
		if (nameAnnotation != null) {
			name = nameAnnotation.value();
		}
		return name ;
	}
	
	public void clearForm() {
		StringBuilder formName = new StringBuilder(this.getClass().getSimpleName());
		formName.replace(0, 1, formName.substring(0, 1).toLowerCase());
		formName.replace(formName.length() - 6, formName.length(), "");
		formName.append("Form");
		UIComponent form = ComponentUtil.getUIComponent(formName.toString());
		ComponentUtil.clearChildren(form);
	}
	
	public void onClickFormTab() {
	}
	
	public void onClickSearchTab() {
	}
	
	public String getTab() {
		return tab;
	}
	
	public void setTab(String tab) {
		this.tab = tab;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
}
