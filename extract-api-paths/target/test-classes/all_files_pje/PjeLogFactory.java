package br.jus.cnj.pje.auditoria;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

@Name("pjeLogFactory")
@Scope(ScopeType.APPLICATION)
public class PjeLogFactory {

	@Factory(scope=ScopeType.APPLICATION, value="pjeLogInstance")
	public PjeLog getInstance() {
		PjeLog log = null;
		String tipoLog = ConfiguracaoIntegracaoCloud.getAuditoriaTipoPersistencia();
		
		if(tipoLog == null || tipoLog.equalsIgnoreCase("DB")) {
			log = ComponentUtil.getComponent(PjeLogDB.NAME);
		}
		else if(tipoLog.equalsIgnoreCase("MQ")) {
			log = ComponentUtil.getComponent(PjeLogMQ.NAME);
		}
        return log;
	}
}
