package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AnaliticoAssuntoRemanescenteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AnaliticoAssuntoRemanescenteList extends AbstractProcessoDistribuidoAnaliticoAssunto {

	public static final String NAME = "analiticoAssuntoRemanescenteList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.processoTrf asc";

	@Override
	protected void addSearchFields() {
		super.addSearchFields();
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new Map(o.processoTrf as remanescente) from EstatisticaProcessoJusticaFederal o ");
		sb.append(getTotalRem());
		return sb.toString();
	}

	private String getTotalRem() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append(" where to_char(o.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("  and o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("  and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append(" and (not exists (select epjf2.processoTrf from EstatisticaProcessoJusticaFederal epjf2 ");
		sb.append("                    where o.classeJudicial = epjf2.classeJudicial ");
		sb.append("                      and to_char(epjf2.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                      and epjf2.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
     	sb.append("                                              #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                 ) or ");
		sb.append("                 (  (select max(epjf3.dtEvento) from EstatisticaProcessoJusticaFederal epjf3 ");
		sb.append("                       where o.classeJudicial = epjf3.classeJudicial ");
		sb.append("                         and to_char(epjf3.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                         and epjf3.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
		sb.append("                                                 #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                    ) < ");
		sb.append("                    (select max(epjf4.dtEvento) from EstatisticaProcessoJusticaFederal epjf4 ");
		sb.append("                      where o.classeJudicial = epjf4.classeJudicial ");
		sb.append("                        and to_char(epjf4.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                        and epjf4.codEvento in (#{parametroUtil.getEventoReativacaoProcessual().codEvento}) ");
		sb.append("                    ) ");
		sb.append("                 ) ");
		sb.append("     ) ");
		return sb.toString();
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
	public List<Map<String, ProcessoTrf>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
}