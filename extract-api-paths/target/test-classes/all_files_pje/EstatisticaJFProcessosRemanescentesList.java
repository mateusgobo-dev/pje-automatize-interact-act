package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;

@Name(EstatisticaJFProcessosRemanescentesList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaJFProcessosRemanescentesList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaJFProcessosRemanescentesList";
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
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("select distinct new Map(o.processoTrf as processo, o.classeJudicial as classe) from EstatisticaProcessoJusticaFederal o ");
		sb.append(" where o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.secao.cdSecaoJudiciaria} ");
		sb.append("     and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.orgaoJulgador} ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append(" and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.cargoJuiz} ");
		}
		if (ed.getCompetencia() != null) {
			sb.append(" and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.competencia} ");
		}
		sb.append("     and to_char(o.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("     and (not exists (select epjf2.processoTrf from EstatisticaProcessoJusticaFederal epjf2 ");
		sb.append("                         where to_char(epjf2.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                            and o.classeJudicial = epjf2.classeJudicial ");
		sb.append("                            and epjf2.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
     	sb.append("                                                    #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                     ) or ");
		sb.append("                     (  (select max(epjf3.dtEvento) from EstatisticaProcessoJusticaFederal epjf3 ");
		sb.append("                            where to_char(epjf3.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                               and o.classeJudicial = epjf3.classeJudicial ");
		sb.append("                               and epjf3.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
		sb.append("                                                       #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                         ) < ");
		sb.append("                         (select max(epjf4.dtEvento) from EstatisticaProcessoJusticaFederal epjf4 ");
		sb.append("                             where to_char(epjf4.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                                and o.classeJudicial = epjf4.classeJudicial ");
		sb.append("                                and epjf4.codEvento in (#{parametroUtil.getEventoReativacaoProcessual().codEvento}) ");
		sb.append("                         ) ");
		sb.append("                     ) ");
		sb.append("          ) ");
		return sb.toString();
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