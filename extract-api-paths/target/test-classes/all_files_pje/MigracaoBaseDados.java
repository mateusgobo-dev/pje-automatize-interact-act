package br.com.infox.ibpm.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.BooleanUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.pje.startup.check.PJeCheckException;

@Name(MigracaoBaseDados.NAME)
@Scope(ScopeType.APPLICATION)
@Startup()
@Install()
public class MigracaoBaseDados {
	
	public static final String NAME = "migracaoBaseDados";
	
	@Logger
	private Log logger;

	@Create
	public void migrate() throws PJeCheckException {
		if (BooleanUtils.toBoolean(ParametroUtil.getParametro(Variaveis.ENV_PJE_FLYWAY_ENABLE_ONSTARTUP))) {
			try {
				Flyway flyway = Flyway.configure()
					.dataSource((DataSource) new InitialContext().lookup("pjeDS"))
					.locations("classpath:migrations")
					.table("schema_version")
					.validateOnMigrate(Boolean.FALSE)
					.sqlMigrationPrefix("PJE_")
					.outOfOrder(Boolean.TRUE)
					.baselineOnMigrate(Boolean.TRUE)
					.schemas("public", "client", "core", "jt", "criminal", "acl")
					.load();
				
				flyway.migrate();
			}catch (NamingException|FlywayException e) {
				this.logger.error(e.getLocalizedMessage());
				throw new PJeCheckException(e.getLocalizedMessage());
			}
		}
	}

}
