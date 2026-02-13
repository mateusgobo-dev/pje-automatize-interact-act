package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.PeriodoInativacaoCaixaRepresentante;

@Name("periodoInativacaoCaixaRepresentanteDAO")
public class PeriodoInativacaoCaixaRepresentanteDAO extends BaseDAO<PeriodoInativacaoCaixaRepresentante>{

	@Override
	public Object getId(PeriodoInativacaoCaixaRepresentante e) {
		return e.getIdPeriodoInativCaixaRep();
	}

}
