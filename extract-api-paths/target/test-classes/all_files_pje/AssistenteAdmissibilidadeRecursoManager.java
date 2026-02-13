package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.AssistenteAdmissiblidadeRecursoDAO;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidadeRecurso;

@Name(AssistenteAdmissibilidadeRecursoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class AssistenteAdmissibilidadeRecursoManager extends GenericManager{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "assistenteAdmissibilidadeRecursoManager";
	
	@In
	private AssistenteAdmissiblidadeRecursoDAO assistenteAdmissiblidadeRecursoDAO;
	
	public Integer getMaxPrioridadeBy(Integer idProcesso) {
		if(idProcesso == null){
			return null;
		}
		return assistenteAdmissiblidadeRecursoDAO.getMaxPrioridadeBy(idProcesso);
	}
	
	public List<AssistenteAdmissibilidadeRecurso> getRecursosBy(Integer idAssistenteAdmissibilidade){
		if(idAssistenteAdmissibilidade == null){
			return null;
		}
		return assistenteAdmissiblidadeRecursoDAO.getRecursosBy(idAssistenteAdmissibilidade);
	}
}
