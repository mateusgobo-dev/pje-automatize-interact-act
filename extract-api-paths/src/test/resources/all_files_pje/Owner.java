/**
 * 
 */
package br.jus.pje.indexacao;

/**
 * @author cristof
 *
 */
public class Owner {
	
	private Object ownerId;
	
	private Class<?> ownerClass;

	private Object owned;
	
	private String path;
	
	public Owner(Object owned, String path, Object ownerId, Class<?> ownerClass) {
		super();
		this.owned = owned;
		this.path = path;
		this.ownerId = ownerId;
		this.ownerClass = ownerClass;
	}

	public Object getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Object ownerId) {
		this.ownerId = ownerId;
	}

	public Class<?> getOwnerClass() {
		return ownerClass;
	}

	public void setOwnerClass(Class<?> ownerClass) {
		this.ownerClass = ownerClass;
	}

	public Object getOwned() {
		return owned;
	}

	public void setOwned(Object owned) {
		this.owned = owned;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
