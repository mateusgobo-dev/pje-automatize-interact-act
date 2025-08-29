package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorAssuntoTrf;

@Name(CaixaAdvogadoProcuradorAssuntoTrfDAO.NAME)
public class CaixaAdvogadoProcuradorAssuntoTrfDAO extends BaseDAO<CaixaAdvogadoProcuradorAssuntoTrf>{

	public static final String NAME = "caixaAdvogadoProcuradorAssuntoTrfDAO";
	
	@Override
	public Object getId(CaixaAdvogadoProcuradorAssuntoTrf e) {
		return e.getIdCaixaAdvogadoProcuradorClasseJudicial();
	}

}
