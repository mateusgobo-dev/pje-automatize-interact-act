package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosTramitacaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosTramitacaoList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaProcessosTramitacaoList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select distinct new map(o.codEstado as secao, "
			+ "count(distinct o.numeroProcesso) as numProcess) "
			+ "from EstatisticaEventoProcesso o "
			+ "  where to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosTramitacaoAction.dataFimFormatada}"
			+ "      and o.codEvento not in (#{parametroUtil.getEventoBaixaDefinitivaProcessual().codEvento}, #{parametroUtil.getEventoArquivamentoDefinitivoProcessual().codEvento}) "
			+ "      and o.idEstatisticaProcesso in (select max(ep.idEstatisticaProcesso) from EstatisticaEventoProcesso ep "
			+ "                                          where to_char(ep.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosTramitacaoAction.dataInicioFormatada} "
			+ "                                          and to_char(ep.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosTramitacaoAction.dataFimFormatada} "
			+ "                                          and ep.numeroProcesso = o.numeroProcesso)";
	private static final String GROUP_BY = "o.codEstado";
	private static final String DEFAULT_ORDER = "o.codEstado";

	private static final String R1 = "to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosTramitacaoAction.dataInicioFormatada} ";
	private static final String R2 = "to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosTramitacaoAction.dataFimFormatada} ";

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

}