package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.CaixaAdvogadoProcuradorAssuntoTrfDAO;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorAssuntoTrf;

@Name(CaixaAdvogadoProcuradorAssuntoTrfManager.NAME)
public class CaixaAdvogadoProcuradorAssuntoTrfManager extends BaseManager<CaixaAdvogadoProcuradorAssuntoTrf>{

	public static final String NAME = "caixaAdvogadoProcuradorAssuntoTrfManager";

	@In
	CaixaAdvogadoProcuradorAssuntoTrfDAO caixaAdvogadoProcuradorAssuntoTrfDAO;
	
	@Override
	protected CaixaAdvogadoProcuradorAssuntoTrfDAO getDAO() {
		return caixaAdvogadoProcuradorAssuntoTrfDAO;
	}
	
}
