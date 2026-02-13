package br.com.infox.ibpm.util;

import java.io.Serializable;

import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

@Name(ConfigurarLogAplicacao.NAME)
@Scope(ScopeType.APPLICATION)
@Install()
@Startup()
@BypassInterceptors
public class ConfigurarLogAplicacao implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "configurarLogAplicacao";
	private static final LogProvider log = Logging.getLogProvider(ConfigurarLogAplicacao.class);

	@Create
	public void init() {
		try {
			DOMConfigurator.configure( this.getClass().getClassLoader().getResource ( "log4jMovimentarFluxo.xml" ) );
		} catch (Exception e) {
			log.error("Erro ao carregar configuração de log da aplicaÁ?o", e);
		}
	}

}