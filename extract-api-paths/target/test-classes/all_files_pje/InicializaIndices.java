package br.com.infox.cliente.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.indexacao.Indexador;

@Name("inicializaIndices")
@Scope(ScopeType.EVENT)
public class InicializaIndices {

	private static boolean enabled = false;
	private static boolean indiceCriado = false;	
	private static boolean instanciaElastic = ConfiguracaoIntegracaoCloud.isElasticInstanceReindex();

	@In
	private Context applicationContext;

	@In
	private Indexador indexador;
	
	@Logger
	private Log log;

	public void startProcess() {
		enabled = true;
		
		try {
			indexador.reindex();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public Long getCurrentValue() {
		enabled = applicationContext.isSet("pje:elasticsearch:reindex:started");
		if (isEnabled()) {
			final Object percent = applicationContext.get("pje:elasticsearch:reindex:percent");
			return percent != null ? (long)Double.parseDouble(percent.toString()) : -1L;
		} else{
			return -1L;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isIndiceCriado() {
		return indiceCriado;
	}	
	
	
	public boolean isInstanciaElastic() {
		return instanciaElastic;
	}	
	
	
	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		enabled = true;
//		this.reindex();
		return null;
	}

	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String EXECUTE = "execute";
	}
}
