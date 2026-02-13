package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosTramitacaoRankingRegionalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosTramitacaoRankingRegionalList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosTramitacaoRankingRegionalList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select e.orgaoJulgador, e.jurisdicao, count(distinct e.numeroProcesso) as totalProcessos "
			+ " from EstatisticaEventoProcesso e "
			+ " where e.codEvento != #{parametroUtil.getEventoArquivamentoDefinitivoProcessual().codEvento} "
			+ " and e.codEvento != #{parametroUtil.getEventoBaixaDefinitivaProcessual().codEvento} "
			+ " and e.idEstatisticaProcesso in (select max(e2.idEstatisticaProcesso) from EstatisticaEventoProcesso e2 "
			+ "                                 where to_char(e2.dataInclusao, 'yyyy-MM') <= #{tramitacaoRankingRegionalAction.dataFimFormatada} "
			+ "                                 and e2.numeroProcesso = e.numeroProcesso) ";

	private static final String DEFAULT_ORDER = "count(distinct e.numeroProcesso) asc ";
	private static final String GROUP_BY = "e.orgaoJulgador, e.jurisdicao ";

	private static final String R1 = " to_char(e.dataInclusao, 'yyyy-MM') >= #{tramitacaoRankingRegionalAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(e.dataInclusao, 'yyyy-MM') <= #{tramitacaoRankingRegionalAction.dataFimFormatada} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	public List<Object[]> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	public String getGroupBy() {
		return GROUP_BY;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public Object[] getEntity() {
		if (entity == null) {
			entity = (Object[]) Contexts.getConversationContext().get(getEntityComponentName());
			if (entity == null) {
				entity = new Object[0];
			}
		}
		return entity;
	}

	@Override
	public void newInstance() {
		entity = new Object[0];
	}

}