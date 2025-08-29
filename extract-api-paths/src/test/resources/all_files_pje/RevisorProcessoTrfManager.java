package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.RevisorProcessoTrfDAO;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrf;

@Name(RevisorProcessoTrfManager.NAME)
public class RevisorProcessoTrfManager extends BaseManager<RevisorProcessoTrf>{

	public static final String NAME = "revisorProcessoTrfManager";
	
	@In
	private RevisorProcessoTrfDAO revisorProcessoTrfDAO;
	
	@Override
	protected BaseDAO<RevisorProcessoTrf> getDAO() {
		return this.revisorProcessoTrfDAO;
	}

}
