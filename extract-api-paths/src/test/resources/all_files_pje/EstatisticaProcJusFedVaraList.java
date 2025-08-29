package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.pje.bean.EstatisticaProcessoEntidadesVaraBean;

@Name(EstatisticaProcJusFedVaraList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcJusFedVaraList extends EntityList<EstatisticaProcessoEntidadesVaraBean> {

	public static final String NAME = "estatisticaProcJusFedVaraList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select new br.com.infox.pje.bean.EstatisticaProcessoEntidadesVaraBean(ppl.pessoa.nome as entidade, "
			+ "count(distinct epj.processoTrf) as totalProcEntidade) "
			+ "from EstatisticaProcessoJusticaFederal epj inner join epj.processoTrf.processoParteList ppl "
			+ "where ppl.pessoa.atraiCompetencia = true ";
	private static final String DEFAULT_ORDER = "ppl.pessoa.nome";
	private static final String DEFAULT_GROUP_BY = "ppl.pessoa.nome";

	private static final String R1 = "epj.secaoJudiciaria = #{estatisticaProcJusFedVaraAction.secaoJudiciaria.cdSecaoJudiciaria} ";
	private static final String R2 = "epj.processoTrf.orgaoJulgador = #{estatisticaProcJusFedVaraAction.orgaoJulgador} ";
	private static final String R3 = "ppl.pessoa.nome = #{estatisticaProcJusFedVaraAction.pessoa.nome} ";

	@Override
	protected void addSearchFields() {
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("pessoal", SearchCriteria.igual, R3);
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
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public String getGroupBy() {
		return DEFAULT_GROUP_BY;
	}

}
