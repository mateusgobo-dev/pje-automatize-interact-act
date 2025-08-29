package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosTramitacaoSecaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosTramitacaoSecaoList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosTramitacaoSecaoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o.codEstado, o.orgaoJulgador, count(distinct o.numeroProcesso) "
			+ "from EstatisticaEventoProcesso o "
			+ "  where cast(o.dataInclusao as date) <= #{estatisticaProcessosTramitacaoSecaoAction.data} "
			+ "  and o.codEvento not in (#{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}, #{parametroUtil.eventoArquivamentoDefinitivo.codEvento}) ";

	private static final String GROUP_BY = "o.codEstado, o.orgaoJulgador";
	private static final String DEFAULT_ORDER = "o.codEstado, o.orgaoJulgador";

	private static final String R1 = "cast(o.dataInclusao as date) <= #{estatisticaProcessosTramitacaoSecaoAction.data} ";
	private static final String R2 = "o.idEstatisticaProcesso in (select max(p.idEstatisticaProcesso) from EstatisticaEventoProcesso p "
			+ "where cast(p.dataInclusao as date) <= #{estatisticaProcessosTramitacaoSecaoAction.data} "
			+ "and o.numeroProcesso = p.numeroProcesso) ";
	private static final String R3 = "o.codEstado = #{parametroUtil.primeiroGrau ? parametroUtil.secao : estatisticaProcessosTramitacaoSecaoAction.secaoJudiciaria.cdSecaoJudiciaria} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusao", SearchCriteria.igual, R1);
		addSearchField("idEstatisticaProcesso", SearchCriteria.igual, R2);
		addSearchField("codEstado", SearchCriteria.igual, R3);
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

}