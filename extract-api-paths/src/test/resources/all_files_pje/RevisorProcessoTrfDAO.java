package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.RevisorProcessoTrf;

@Name(RevisorProcessoTrfDAO.NAME)
public class RevisorProcessoTrfDAO extends BaseDAO<RevisorProcessoTrf>{

	public static final String NAME = "revisorProcessoTrfDAO";
	
	@Override
	public Object getId(RevisorProcessoTrf e) {
		return e.getIdRevisorProcessoTrf();
	}

}
