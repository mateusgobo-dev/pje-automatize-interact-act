package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;

@Name(DetalheConsultaPublicaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class DetalheConsultaPublicaAction implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(DetalheConsultaPublicaAction.class);
	public static final String NAME = "detalheConsultaPublicaAction";

	@In
	private ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager;

	private Map<Integer, Boolean> canSeeDocumentoMap;

	/**
	 * Método que verifica se o documento pode ou não ser visualizado na
	 * consulta pública.
	 * 
	 * @param pd
	 * @return
	 */
	public boolean canSeeDocumento(ProcessoDocumento pd) {
		if (pd == null) {
			return false;
		}
		if (canSeeDocumentoMap == null) {
			canSeeDocumentoMap = new HashMap<Integer, Boolean>();
		}
		Boolean value = canSeeDocumentoMap.get(pd.getIdProcessoDocumento());
		if (value == null) {
			ProcessoDocumentoTrfLocal pdTrfLocal = null;
			pdTrfLocal = processoDocumentoTrfLocalManager.find(ProcessoDocumentoTrfLocal.class,
					pd.getIdProcessoDocumento());
			if (pdTrfLocal == null) {
				value = false;
			} else {
				value = processoDocumentoTrfLocalManager.canSeeConsultaPublica(pdTrfLocal);
			}
			canSeeDocumentoMap.put(pd.getIdProcessoDocumento(), value);
		}
		return value;
	}

}