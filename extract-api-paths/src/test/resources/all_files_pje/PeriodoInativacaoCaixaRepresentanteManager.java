package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PeriodoInativacaoCaixaRepresentanteDAO;
import br.jus.pje.nucleo.entidades.PeriodoInativacaoCaixaRepresentante;

@Name(PeriodoInativacaoCaixaRepresentanteManager.NAME)
@Scope(ScopeType.EVENT)
public class PeriodoInativacaoCaixaRepresentanteManager extends BaseManager<PeriodoInativacaoCaixaRepresentante>{

	@In
	private PeriodoInativacaoCaixaRepresentanteDAO periodoInativacaoCaixaRepresentanteDAO;

	public static final String  NAME = "periodoInativacaoCaixaRepresentanteManager";
	

	@Override
	protected BaseDAO<PeriodoInativacaoCaixaRepresentante> getDAO() {
		return periodoInativacaoCaixaRepresentanteDAO;
	}
		
}
