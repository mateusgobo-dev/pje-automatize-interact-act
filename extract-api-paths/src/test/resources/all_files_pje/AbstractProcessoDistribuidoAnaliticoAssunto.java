package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@BypassInterceptors
@Scope(ScopeType.PAGE)
abstract class AbstractProcessoDistribuidoAnaliticoAssunto extends EntityList<Map<String, ProcessoTrf>> {

	static final long serialVersionUID = 1L;
	static final String DEFAULT_ORDER = "o.codEvento";

	static final String R1 = "o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList})";
	static final String R2 = "o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.cargoJuiz} ";	
	static final String R3 = "o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.competencia} ";
	static final String R4 = "o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.secao.cdSecaoJudiciaria} ";
	static final String R5 = "o.processoTrf.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.orgaoJulgador} ";


	@Override
	protected void addSearchFields() {
		addSearchField("classeJudicial", SearchCriteria.igual, R1);
		addSearchField("cargo", SearchCriteria.igual, R2);
		addSearchField("competencia", SearchCriteria.igual, R3);
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R4);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R5);
	}
}