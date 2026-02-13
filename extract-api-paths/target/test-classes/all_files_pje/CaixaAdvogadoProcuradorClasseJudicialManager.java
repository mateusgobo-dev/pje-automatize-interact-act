package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.CaixaAdvogadoProcuradorClasseJudicialDAO;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorClasseJudicial;

@Name(CaixaAdvogadoProcuradorClasseJudicialManager.NAME)
public class CaixaAdvogadoProcuradorClasseJudicialManager extends BaseManager<CaixaAdvogadoProcuradorClasseJudicial>{
	
	public static final String NAME = "caixaAdvogadoProcuradorClasseJudicialManager";

	@In
	private CaixaAdvogadoProcuradorClasseJudicialDAO caixaAdvogadoProcuradorClasseJudicialDAO;
	
	@Override
	protected BaseDAO<CaixaAdvogadoProcuradorClasseJudicial> getDAO() {
		// TODO Auto-generated method stub
		return caixaAdvogadoProcuradorClasseJudicialDAO;
	}

}
