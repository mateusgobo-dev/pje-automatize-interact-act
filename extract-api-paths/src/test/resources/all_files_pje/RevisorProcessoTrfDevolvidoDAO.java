package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.RevisorProcessoTrfDevolvido;

@Name(RevisorProcessoTrfDevolvidoDAO.NAME)
public class RevisorProcessoTrfDevolvidoDAO extends BaseDAO<RevisorProcessoTrfDevolvido>{

	public static final String NAME = "revisorProcessoTrfDevolvidoDAO";
	
	@Override
	public Object getId(RevisorProcessoTrfDevolvido e) {
		return e.getIdRevisorProcessoTrfDevolvido();
	}

}
