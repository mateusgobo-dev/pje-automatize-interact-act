package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;

@Name(EstatisticaJFProcessosReativadosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaJFProcessosReativadosList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaJFProcessosReativadosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.processoTrf asc";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new Map(o.processoTrf as processo, o.classeJudicial as classe) from EstatisticaProcessoJusticaFederal o ");
		sb.append(" where o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.secao.cdSecaoJudiciaria} ");
		sb.append("   and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.orgaoJulgador} ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append(" and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.cargoJuiz} ");
		}
		if (ed.getCompetencia() != null) {
			sb.append(" and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.competencia} ");
		}
		sb.append("   and to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("   and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("   and o.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                             where ep2.caminhoCompleto like concat(#{parametroUtil.eventoReativacaoProcessual.caminhoCompleto}, '%') ");
		sb.append("                        ) ");
		return sb.toString();
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public List<Map<String, Object>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}