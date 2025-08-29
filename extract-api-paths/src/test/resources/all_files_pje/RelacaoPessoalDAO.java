package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.RelacaoPessoal;

@Name(RelacaoPessoalDAO.NAME)
public class RelacaoPessoalDAO extends BaseDAO<RelacaoPessoal> {
	public static final String NAME = "relacaoPessoalDAO";
	@Override
	public Object getId(RelacaoPessoal e) {
		return e.getId();
	}
	
	

}
