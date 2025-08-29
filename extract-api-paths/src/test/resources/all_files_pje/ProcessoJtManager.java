package br.com.infox.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.ProcessoJtDAO;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Classe Manager contem algumas regras sobre o ProcessoJt
 * 
 */
@Name(ProcessoJtManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ProcessoJtManager extends GenericManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "processoJtManager";
	
	@In
	private ProcessoJtDAO processoJtDAO;
	
	public ProcessoJT getProcessoJtPorId(int idProcessoJt) {
		return processoJtDAO.getProcessoJtPorId(idProcessoJt);
	}

	public Usuario getPessoaRelatorOriginario(int idProcessoTrf){
		return processoJtDAO.getPessoaRelatorOriginario(idProcessoTrf);
	}
	
}