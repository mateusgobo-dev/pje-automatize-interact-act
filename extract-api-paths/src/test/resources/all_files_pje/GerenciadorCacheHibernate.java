package br.com.infox.ibpm.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Cache;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Name("gerenciadorCacheHibernate")
@Scope(ScopeType.APPLICATION)
public class GerenciadorCacheHibernate extends AbstractGerenciadorCache {
	
	private static final Logger logger = Logger.getLogger(EntityLogWatcher.class.getName());
	@SuppressWarnings("rawtypes")
	private final Map<String, Class> namedEntitiesClasses = new HashMap<String, Class>(371);
	
	@SuppressWarnings("rawtypes")
	public void execute(List<EntityLog> logs) {
		Cache cache = null;
		for (EntityLog log : logs) {
			if (cache == null) {
				final EntityManager em = (EntityManager) Component.getInstance("entityManager");
				cache = em.getEntityManagerFactory().getCache();
			}
			if(!log.getTipoOperacao().equals(TipoOperacaoLogEnum.I)) {
				final Class entityClass = findEntityClass(log.getNomeEntidade(), log.getNomePackage());
				if (entityClass == null) {
					continue;
				}
				try {
					cache.evict(entityClass, Long.valueOf(log.getIdEntidade()));
				} catch (NumberFormatException ex) {
					// Nothing to do.
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Class findEntityClass(String entityName, String packageName) {
		Class entityClass = namedEntitiesClasses.get(entityName);
		if ((entityClass == null) && !namedEntitiesClasses.containsKey(entityName)) {
			String className = new StringBuilder(packageName.length() + 1 + entityName.length())
					.append(packageName).append('.').append(entityName)
					.toString();
			try {
				entityClass = EntityLog.class.getClassLoader().loadClass(className);
			} catch (ClassNotFoundException ex) {
				logger.log(Level.WARNING, "[LogCheckerCacheStrategy] Entidade não encontrada: " + entityName, ex);
			}
			namedEntitiesClasses.put(entityName, entityClass);
		}
		return entityClass;
	}
}
