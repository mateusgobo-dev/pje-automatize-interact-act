package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;

@Name(EstatisticaMapaDistribuicaoVarasList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaMapaDistribuicaoVarasList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaMapaDistribuicaoVarasList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.codEstado";
	private static final String GROUP_BY = "o.codEstado, o.jurisdicao, o.orgaoJulgador";

	private static final String R1 = " to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaMapaDistribuicaoVarasAction.dataMesAnoFormatado} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.codEstado, o.orgaoJulgador as vara, o.jurisdicao as secao ");
		sb.append("from EstatisticaEventoProcesso o where ");
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append("o.codEstado = #{parametroUtil.secao}");
		} else {
			sb.append("1 = 1");
		}
		return sb.toString();
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