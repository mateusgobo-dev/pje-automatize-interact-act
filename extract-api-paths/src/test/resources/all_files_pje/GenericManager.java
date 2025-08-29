package br.com.infox.core.manager;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;

@Name(GenericManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "genericManager";
	
	@In
	private GenericDAO genericDAO;
	
	public <T> T persist(T o) {
		return genericDAO.persist(o);
	}
	
	public <T> T update(T o) {
		return genericDAO.update(o);
	}
	
	public <T> T remove(T o) {
		return genericDAO.remove(o);
	}
	
	public <T> T find(Class<T> clazz, Object id) {
		return genericDAO.find(clazz, id);
	}
	
	public <T> boolean isManaged(T object){
		return genericDAO.isManaged(object);
	}
	
	public void persistVarios(Object... parametros){
		genericDAO.persistVarios(parametros);
	}

	public void updateVarios(Object... parametros){
		genericDAO.updateVarios(parametros);
	}
	
	public void removeVarios(Object... parametros){
		genericDAO.removeVarios(parametros);
	}
	
	public void clear(){
		genericDAO.clear();
	}
	
}