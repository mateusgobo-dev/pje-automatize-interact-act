package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.com.infox.pje.manager.ProcessoJtManager;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
@Name(ProcessoJtAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ProcessoJtAction implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoJtAction";
	
	@In
	private ProcessoJtManager processoJtManager;
	private Map<Integer, Usuario> cacheRelatorOriginario = new HashMap<Integer, Usuario>();

	public Boolean getMostrarRelatorOriginario(ProcessoTrf processoTrf) {
		Boolean retorno = false;
		if (getRelatorOriginario(processoTrf) != null) {
			retorno = true;
		}
		return retorno;
	}
	
	public Usuario getRelatorOriginario(ProcessoTrf processoTrf) {
		Usuario retorno = null;
		retorno = cacheRelatorOriginario.get(processoTrf.getIdProcessoTrf());
		
		if (ParametroJtUtil.instance().justicaTrabalho() 
				&& !cacheRelatorOriginario.containsKey(processoTrf.getIdProcessoTrf())){
			
			retorno = processoJtManager.getPessoaRelatorOriginario(processoTrf.getIdProcessoTrf());
			cacheRelatorOriginario.put(processoTrf.getIdProcessoTrf(), retorno);
		}
		return retorno;
	}
}
