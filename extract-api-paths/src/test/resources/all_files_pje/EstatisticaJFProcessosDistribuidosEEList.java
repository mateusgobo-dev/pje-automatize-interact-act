package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;

@Name(EstatisticaJFProcessosDistribuidosEEList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaJFProcessosDistribuidosEEList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaJFProcessosDistribuidosEEList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "list.pessoa asc";
	private static final String GROUP_BY = "list.pessoa,o.classeJudicial,o.processoTrf,o.dtEvento";

	private static final String R1 = "o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList})";
	private static final String R2 = "o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.cargoJuiz} ";
	private static final String R3 = "o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.competencia} ";

	@Override
	protected void addSearchFields() {
		addSearchField("classeJudicial", SearchCriteria.igual, R1);
		addSearchField("cargo", SearchCriteria.igual, R2);
		addSearchField("competencia", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new map(list.pessoa as entidade, ");
		sb.append(getTotalRem());
		sb.append(getTotalDistr());
		sb.append(getTotalDevolv());
		sb.append(getTotalReativ());
		sb.append(getTotalBaixad());
		sb.append(getTotalRedistr());
		sb.append(getTotalRemet());
		sb.append(") from EstatisticaProcessoJusticaFederal o ");
		sb.append("inner join o.processoTrf.processoParteList list ");
		sb.append("where list.pessoa.atraiCompetencia = true ");
		sb.append("and o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
		sb.append("and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");

		sb.append("    and to_char(o.dtEvento, 'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("    and to_char(o.dtEvento, 'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		if (ed.getCompetencia() != null) {
			sb.append(" and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append(" and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		if (ed.getClasseJudicial() != null) {
			sb.append(" and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
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
	public List<Map<String, Object>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	private String getTotalRem() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep ");
		sb.append("   where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("     and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("     and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("     and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append("     and to_char(ep.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("     and (not exists (select epjf2.processoTrf from EstatisticaProcessoJusticaFederal epjf2 ");
		sb.append("                         where to_char(epjf2.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                            and epjf2.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
     	sb.append("                                                    #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                      ) or ");
		sb.append("                     (  (select max(epjf3.dtEvento) from EstatisticaProcessoJusticaFederal epjf3 ");
		sb.append("                            where to_char(epjf3.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                               and epjf3.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
		sb.append("                                                       #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                         ) < ");
		sb.append("                         (select max(epjf4.dtEvento) from EstatisticaProcessoJusticaFederal epjf4 ");
		sb.append("                             where to_char(epjf4.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                                and epjf4.codEvento in (#{parametroUtil.getEventoReativacaoProcessual().codEvento}) ");
		sb.append("                         ) ");
		sb.append("                     ) ");
		sb.append("          ) ");
		sb.append(") as numProcessRem, ");		
		return sb.toString();
	}

	private String getTotalDistr() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("  and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append("  and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("  and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("  and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("  and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("  and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                            where ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualDistribuicao.caminhoCompleto}, '%') ");
		sb.append("                               or ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualRedistribuicao.caminhoCompleto}, '%') ");
		sb.append("                       ) ");
		sb.append(") as numProcessDistr, ");
		return sb.toString();
	}

	private String getTotalDevolv() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append(" and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append(" and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append(" and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append(" and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                             where ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoProcessual.caminhoCompleto}, '%') ");
		sb.append("                                or ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoEmDiligenciaProcessual.caminhoCompleto}, '%') ");
		sb.append("                       ) ");
		sb.append(") as numProcessDevolv, ");
		return sb.toString();
	}

	private String getTotalReativ() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append(" and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append(" and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append(" and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append(" and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                  where ep2.caminhoCompleto like concat(#{parametroUtil.eventoReativacaoProcessual.caminhoCompleto}, '%') ");
		sb.append("                       ) ");
		sb.append(") as numProcessReativ, ");
		return sb.toString();
	}

	private String getTotalBaixad() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("   where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append(" and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append(" and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append(" and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append(" and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                              where ep2.caminhoCompleto like concat(#{parametroUtil.eventoArquivamentoDefinitivoProcessual.caminhoCompleto}, '%') ");
		sb.append("                                 or ep2.caminhoCompleto like concat(#{parametroUtil.eventoBaixaDefinitivaProcessual.caminhoCompleto}, '%') ");
		sb.append("                         ) ");
		sb.append(") as numProcessBaixad, ");
		return sb.toString();
	}

	private String getTotalRedistr() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("   where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("  and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append("  and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("  and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("  and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("  and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("  and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                               where ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualRedistribuicao.caminhoCompleto}, '%') ");
		sb.append("                        ) ");
		sb.append(") as numProcessRedistr, ");
		return sb.toString();
	}

	private String getTotalRemet() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append(" and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append(" and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append(" and ep.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa = list.pessoa) ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append(" and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                              where ep2.caminhoCompleto like concat(#{parametroUtil.eventoRemetidoTrfProcessual.caminhoCompleto}, '%') ");
		sb.append("                         ) ");
		sb.append(") as numProcessRemet ");
		return sb.toString();
	}
}
