package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorClasseJudicial;

@Name(CaixaAdvogadoProcuradorClasseJudicialDAO.NAME)
public class CaixaAdvogadoProcuradorClasseJudicialDAO extends BaseDAO<CaixaAdvogadoProcuradorClasseJudicial>{
	
	public static final String NAME = "caixaAdvogadoProcuradorClasseJudicialDAO";

	@Override
	public Object getId(CaixaAdvogadoProcuradorClasseJudicial e) {
		// TODO Auto-generated method stub
		return e.getIdCaixaAdvogadoProcuradorClasseJudicial();
	}

}
