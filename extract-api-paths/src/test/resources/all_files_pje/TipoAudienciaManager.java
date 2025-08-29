package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.TipoAudienciaDAO;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(TipoAudienciaManager.NAME)
public class TipoAudienciaManager extends BaseManager<TipoAudiencia> {
	
	public static final String NAME = "tipoAudienciaManager";
	
	@In
	private TipoAudienciaDAO tipoAudienciaDAO;

	@Override
	protected BaseDAO<TipoAudiencia> getDAO() {
		return tipoAudienciaDAO;
	}
	
	public List<TipoAudiencia> getTipoAudienciaList(){
		return tipoAudienciaDAO.getTipoAudienciaList();
	}
}
