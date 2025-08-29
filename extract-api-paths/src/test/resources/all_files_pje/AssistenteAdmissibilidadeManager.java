package br.com.jt.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.AssistenteAdmissiblidadeDAO;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidade;

@Name(AssistenteAdmissibilidadeManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class AssistenteAdmissibilidadeManager extends GenericManager{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "assistenteAdmissibilidadeManager";
	
	@In
	private AssistenteAdmissiblidadeDAO assistenteAdmissiblidadeDAO;
	
	public AssistenteAdmissibilidade getUltimoAssistenteBy(Integer idProcesso) {
		if(idProcesso == null){
			return null;
		}
		return assistenteAdmissiblidadeDAO.getUltimoAssistenteBy(idProcesso);
	}
}
