package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.RevisorProcessoTrfDevolvidoDAO;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrfDevolvido;

@Name(RevisorProcessoTrfDevolvidoManager.NAME)
public class RevisorProcessoTrfDevolvidoManager extends BaseManager<RevisorProcessoTrfDevolvido>{

	public static final String NAME = "revisorProcessoTrfDevolvidoManager";
	
	@In
	private RevisorProcessoTrfDevolvidoDAO revisorProcessoTrfDevolvidoDAO;
	
	@Override
	protected BaseDAO<RevisorProcessoTrfDevolvido> getDAO() {
		return this.revisorProcessoTrfDevolvidoDAO;
	}

}
