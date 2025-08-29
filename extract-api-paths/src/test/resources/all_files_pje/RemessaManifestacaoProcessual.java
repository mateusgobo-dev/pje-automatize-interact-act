package br.jus.cnj.pje.remessa;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.LogService;
import br.jus.cnj.pje.nucleo.service.RemessaManifestacaoProcessualService;
@Name("remessaManifestacaoProcessual")
@Scope(ScopeType.EVENT)
@AutoCreate
public class RemessaManifestacaoProcessual{

	@Logger
	private Log log;
	
	@In
	private LogService logService;

	@In
	private RemessaManifestacaoProcessualService remessaManifestacaoProcessualService;
	
	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		try {
			remessaManifestacaoProcessualService.remeterManifestacoesPendentes();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "remessaManifestacaoProcessual");
		}
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
