package br.jus.cnj.pje.nucleo.identity;

import static org.jboss.seam.ScopeType.SESSION;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Contexts;

@Name("pjeSessionCache")
@Scope(SESSION)
@Startup
public class PjeSessionCache {
	private static final String PJE_SESSION_CACHE_NAME = "pje:session:cache:name";
	private Map<String, PJeAttribute> attributes = new HashMap<String, PJeAttribute>(0);
	
	public Object getAttribute(String name) {
		PJeAttribute pjeAttribute = attributes.get(name);
		if(timeout(pjeAttribute)) {
			pjeAttribute = null;
			attributes.remove(name);
			Contexts.getSessionContext().set(PjeSessionCache.PJE_SESSION_CACHE_NAME, attributes);
		}
		return pjeAttribute == null ? null : pjeAttribute.value;
	}
	
	public void setAttribute(String name, Object value) {
		setAttribute(name, value, 0);
	}

	public void setAttribute(String name, Object value, int ttl) {
		setAttribute(name, value, ttl, false);
	}

	public void setAttribute(String name, Object value, int ttl, boolean longTerm) {
		setAttribute(name, value, ttl, longTerm, false);
	}

	public void setAttribute(String name, Object value, int ttl, boolean longTerm, boolean forceUpdate) {
		validateAttribute(name, value);
		if(setOrUpdate(name, forceUpdate)) {
			PJeAttribute pjeAttribute = new PJeAttribute(value, ttl, new Date(), longTerm);
			attributes.remove(name);
			attributes.put(name, pjeAttribute);
			Contexts.getSessionContext().set(PjeSessionCache.PJE_SESSION_CACHE_NAME, attributes);
		}
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
		Contexts.getSessionContext().set(PjeSessionCache.PJE_SESSION_CACHE_NAME, attributes);
	}

	@SuppressWarnings("rawtypes")
	public void removeAll() {
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry pair = (Map.Entry)it.next();
		    boolean longTerm = ((PJeAttribute)pair.getValue()).longTerm;
		    if(!longTerm) {
		    	it.remove();
		    }
		}
		if(attributes.size() == 0) {
			Contexts.getSessionContext().remove(PjeSessionCache.PJE_SESSION_CACHE_NAME);
		}
		else {
			Contexts.getSessionContext().set(PjeSessionCache.PJE_SESSION_CACHE_NAME, attributes);
		}
	}
	
	private void validateAttribute(String name, Object value) {
		if(name == null) {
			throw new RuntimeException("[PJeSessionCache] Nome de atributo nulo");
		}
		
		if(!name.startsWith("pje:")) {
			throw new RuntimeException("[PJeSessionCache] Nome de atributo inválido: obrigatório incluir prefixo 'pje:'");
		}
		
		if(value == null) {
			throw new RuntimeException("[PJeSessionCache] Valor de atributo nulo");
		}
	}

	private boolean setOrUpdate(String name, boolean forceUpdate) {
		if(attributes.containsKey(name) && !forceUpdate) {
			return false;
		}
		return true;
	}

	private boolean timeout(PJeAttribute pjeAttribute) {
		if(pjeAttribute == null) {
			return true;
		}
		if(pjeAttribute.ttl <= 0) {
			return false;
		}
		
		Calendar limit = new GregorianCalendar();
		limit.setTime(pjeAttribute.creationTime);
		limit.add(Calendar.SECOND, pjeAttribute.ttl);
		return new Date().after(limit.getTime());
	}
	
	private class PJeAttribute {
		private Object value;
		private int ttl;
		private Date creationTime;
		private boolean longTerm;
		
		private PJeAttribute(Object value, int ttl, Date creationTime, boolean longTerm) {
			this.value = value;
			this.ttl = ttl;
			this.creationTime = creationTime;
			this.longTerm = longTerm;
		}
	}
	
}
