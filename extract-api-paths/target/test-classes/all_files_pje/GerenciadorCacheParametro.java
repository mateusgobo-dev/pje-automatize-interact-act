package br.com.infox.ibpm.util;

import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.log.EntityLog;

@Name("gerenciadorCacheParametro")
@Scope(ScopeType.APPLICATION)
public class GerenciadorCacheParametro extends AbstractGerenciadorCache {
	
	public void execute(List<EntityLog> logs) {
		for (EntityLog log : logs) {
			if(log.getNomeEntidade().equalsIgnoreCase("parametro")) {
				removerParametro(Long.valueOf(log.getIdEntidade()));
			}
		}
	}

	private void removerParametro(Long idParametro) {
		String sql = "select nm_variavel, vl_variavel from tb_parametro where id_parametro = :idParametro";
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql)
						.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
						.setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH)
						.setParameter("idParametro", idParametro);
		
		Object[] r = EntityUtil.getSingleResult(q);
		if(r != null) {
			String param = (String)r[0];
			Context appContext = Contexts.getApplicationContext();
			if (appContext.isSet(param)) {
				String value = (String)r[1];
				appContext.set(param, value);
			}
		}
	}
}
