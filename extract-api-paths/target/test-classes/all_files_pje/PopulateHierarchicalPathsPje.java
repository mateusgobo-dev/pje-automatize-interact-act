package br.com.infox.temp;

import static br.com.itx.util.EntityUtil.getEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;

import br.com.infox.annotations.manager.RecursiveManager;
import br.com.infox.ibpm.service.LogService;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name("populateHierarchicalPathsPje")
@Scope(ScopeType.APPLICATION)
@Startup()
@Install()
public class PopulateHierarchicalPathsPje {

	private static final Class<?>[] classes = { Competencia.class,
			OrgaoJulgadorColegiado.class };
	@In
	private LogService logService;
	@Logger
	private Log log;

	@Create
	public void populate() {
		try {
			boolean b = Util.beginTransaction();
	
			EntityManager em = getEntityManager();
			em.setFlushMode(FlushModeType.COMMIT);
	
			for (Class<?> classe : classes) {
				RecursiveManager.populateAllHierarchicalPaths(classe);
				em.flush();
				em.clear();
			}
			if (b) {
				Util.commitTransction();
			}
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "populate");
		}	
	}

}
