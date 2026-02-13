package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosTramitacaoRankingList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosTramitacaoRankingList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosTramitacaoRankingList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select e.codEstado, e.orgaoJulgador, e.jurisdicao, count(e.numeroProcesso) as totalProcessos "
			+ " from EstatisticaEventoProcesso e "
			+ " where e.codEvento not in (#{parametroUtil.getEventoArquivamentoDefinitivo().codEvento}, #{parametroUtil.getEventoBaixaDefinitiva().codEvento}) and "
			+ "       e.idEstatisticaProcesso in (select max(e2.idEstatisticaProcesso) from EstatisticaEventoProcesso e2 "
			+ "                                    where to_char(e2.dataInclusao, 'yyyy-MM') <= #{estatisticaTramitacaoRankingSecaoAction.dataFimFormatada} and "
			+ "                                          to_char(e2.dataInclusao, 'yyyy-MM') >= #{estatisticaTramitacaoRankingSecaoAction.dataInicioFormatada} and "
			+ "                                          e2.numeroProcesso = e.numeroProcesso) ";

	private static final String DEFAULT_ORDER = "e.codEstado, count(e.numeroProcesso) desc ";
	private static final String GROUP_BY = "e.codEstado, e.orgaoJulgador, e.jurisdicao ";

	private static final String R1 = " e.codEstado = #{estatisticaTramitacaoRankingSecaoAction.secaoJudiciaria.cdSecaoJudiciaria} ";

	@Override
	protected void addSearchFields() {
		addSearchField("codEstado", SearchCriteria.igual, R1);
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
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public String getGroupBy() {
		return GROUP_BY;
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