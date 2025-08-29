package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.FiltroTagDAO;
import br.jus.pje.nucleo.entidades.FiltroTag;

@Name(FiltroTagManager.NAME)
public class FiltroTagManager extends BaseManager<FiltroTag>{

	public static final String NAME = "filtroTagManager";

	@In
	private FiltroTagDAO filtroTagDAO;
	
	@Override
	protected BaseDAO<FiltroTag> getDAO() {
		return this.filtroTagDAO;
	}
	
	public FiltroTag findFiltroTagByIdTagAndIdFiltro(Integer idFiltro, Integer idTag){
		return this.filtroTagDAO.findFiltroTagByIdTagAndIdFiltro(idFiltro, idTag);
	}
}
