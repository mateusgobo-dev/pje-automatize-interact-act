package br.com.infox.performance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;

/**
 * Classe responsável em manter um cache de objetos na conversação.
 * 
 * @author Adriano Pamplona
 */
@Name(ObjectConversationCache.NAME)
@Scope(ScopeType.CONVERSATION)
@SuppressWarnings("serial")
public class ObjectConversationCache implements Serializable {

	public static final String NAME = "objectConversationCache";

	private Map<Object, Object> mapa = new HashMap<Object, Object>();

	/**
	 * @return Instância da classe ObjectConversationCache.
	 */
	public static ObjectConversationCache instance() {
		return ComponentUtil.getComponent(NAME, ScopeType.CONVERSATION);
	}

	/**
	 * Retorna true se o objeto está em cache.
	 * 
	 * @param key
	 * @return boleano
	 */
	public Boolean isCache(Object key) {
		return getMapa().containsKey(key);
	}
	
	/**
	 * Retorna o objeto do cache.
	 * 
	 * @param key
	 * @return Objeto
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Object key) {
		return (T) getMapa().get(key);
	}
	
	/**
	 * Adiciona um objeto no cache.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value) {
		getMapa().put(key, value);
	}
	
	/**
	 * @return mapa.
	 */
	private Map<Object, Object> getMapa() {
		if (mapa == null) {
			mapa = new HashMap<Object, Object>();
		}
		return mapa;
	}

}