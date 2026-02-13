package br.com.infox.ibpm.util;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.log.EntityLog;

@Name(GerenciadorCacheExecutor.NAME)
@Scope(ScopeType.APPLICATION)
public class GerenciadorCacheExecutor {

	@In(create=true)
	private GerenciadorCacheHibernate gerenciadorCacheHibernate;
	
	@In(create=true)
	private GerenciadorCacheParametro gerenciadorCacheParametro;
	
	@In(create=true)
	private GerenciadorCachePlacarSessao gerenciadorCachePlacarSessao;
	
	public static final String NAME = "gerenciadorCacheExecutor";
	
	@Observer(EntityLogWatcher.ULTIMAS_ENTIDADES_MODIFICADAS)
	public void execute(List<EntityLog> logs) {
		AbstractGerenciadorCache[] strategies = { gerenciadorCacheHibernate, 
												 gerenciadorCacheParametro,
												 gerenciadorCachePlacarSessao};
		for (AbstractGerenciadorCache strategy : strategies) {
			strategy.execute(logs);
        }
	}
}
