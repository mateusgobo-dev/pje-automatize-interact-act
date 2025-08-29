package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;

@Name(EstatisticaJusticaFederalProcessosDistribuidosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaJusticaFederalProcessosDistribuidosList extends EntityList<Map<String, Object>>{
	
	public static final String NAME = "estatisticaJusticaFederalProcessosDistribuidosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial";
	private static final String GROUP_BY = "o.classeJudicial, o.processoTrf";
	
	
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
		sb.append("select distinct new map(o.classeJudicial as classe, ");
		sb.append(getTotalRem());
		sb.append(getTotalDistr());
		sb.append(getTotalDevolv());
		sb.append(getTotalReativ());
		sb.append(getTotalMudClassRee());
		sb.append(getTotalMudClassBaixa());
		sb.append(getTotalBaixad());
		sb.append(getTotalRedistr());
		sb.append(getTotalRemet());
		sb.append(getTotalSusp());
		sb.append(getTotalArq());
		sb.append(" ) from EstatisticaProcessoJusticaFederal o ");
		sb.append("where o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
		sb.append("and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("and to_char(o.dtEvento, 'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("and to_char(o.dtEvento, 'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		if (ed.getCompetencia() != null) {
			sb.append("and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		if (ed.getClasseJudicial() != null) {
			sb.append("and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
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
	
	private String getTotalRem(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep ");
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
		sb.append("     and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("     and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("     and to_char(ep.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("     and (not exists (select epjf2.processoTrf from EstatisticaProcessoJusticaFederal epjf2 ");
		sb.append("                         where to_char(epjf2.dtEvento,'yyyy-MM-dd') < #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("                            and epjf2.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
     	sb.append("                                                    #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                     ) or ");
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
	
	private String getTotalDistr(){
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
		sb.append("        and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("        and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("        and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("        and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("        and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                    where ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualDistribuicao.caminhoCompleto}, '%') ");
		sb.append("                                       or ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualRedistribuicao.caminhoCompleto}, '%') ");
		sb.append("                              ) ");
		sb.append(") as numProcessDistr, ");
		return sb.toString();
	}
	
	private String getTotalDevolv(){
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
		sb.append("        and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("        and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("        and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("        and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("        and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                      where ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoProcessual.caminhoCompleto}, '%') ");
		sb.append("                                         or ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoEmDiligenciaProcessual.caminhoCompleto}, '%') ");
		sb.append("                              ) ");
		sb.append(") as numProcessDevolv, ");
		return sb.toString();
	}
	
	private String getTotalReativ(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial  ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("       and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("       and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("       and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                    where ep2.caminhoCompleto like concat(#{parametroUtil.eventoReativacaoProcessual.caminhoCompleto}, '%') ");
		sb.append("                             ) ");
		sb.append(") as numProcessReativ, ");
		return sb.toString();
	}
	
	private String getTotalMudClassRee(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial  ");
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		sb.append("       and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("       and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("       and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                  where ep2.caminhoCompleto like concat(#{parametroUtil.eventoMudancaClasseProcessual.caminhoCompleto}, '%') ");
		sb.append("                               ) ");
		sb.append(") as numProcessMudClassRee, ");
		return sb.toString();
	}
	
	private String getTotalMudClassBaixa(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial  ");
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		sb.append("       and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("       and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("       and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                    where ep2.caminhoCompleto like concat(#{parametroUtil.eventoMudancaClasseProcessual.caminhoCompleto}, '%') ");
		sb.append("                              ) ");
		sb.append(") as numProcessMudClassBaixa, ");
		return sb.toString();
	}

	
	private String getTotalBaixad(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("   where o.classeJudicial = ep.classeJudicial  ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("       and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("       and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("       and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                  where ep2.caminhoCompleto like concat(#{parametroUtil.eventoArquivamentoDefinitivoProcessual.caminhoCompleto}, '%') ");
		sb.append("                                     or ep2.caminhoCompleto like concat(#{parametroUtil.eventoBaixaDefinitivaProcessual.caminhoCompleto}, '%') ");
		sb.append("                              ) ");
		sb.append(") as numProcessBaixad, ");
		return sb.toString();	
	}
	
	private String getTotalRedistr(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("   where o.classeJudicial = ep.classeJudicial  ");
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
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append(" and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append(" and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                            where ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualRedistribuicao.caminhoCompleto}, '%') ");
		sb.append("                       ) ");
		sb.append(") as numProcessRedistr, ");
		return sb.toString();	
	}
	
	private String getTotalRemet(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial  ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("         and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("         and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("         and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("         and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("         and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                       where ep2.caminhoCompleto like concat(#{parametroUtil.eventoRemetidoTrfProcessual.caminhoCompleto}, '%') ");
		sb.append("                                 ) ");
		sb.append(") as numProcessRemet, ");
		return sb.toString();
	}
	
	private String getTotalSusp(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("    where o.classeJudicial = ep.classeJudicial  ");
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		sb.append("       and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("       and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("         and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("         and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} " );
		sb.append("         and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                    where ep2.caminhoCompleto like concat(#{parametroUtil.eventoSuspensaoDecisaoProcessual.caminhoCompleto}, '%') ");
		sb.append("                                       or ep2.caminhoCompleto like concat(#{parametroUtil.eventoSuspensaoDespachoProcessual.caminhoCompleto}, '%') ");
		sb.append("                               ) ");
		sb.append(") as numProcessSusp, ");
		return sb.toString();	
	}
	
	private String getTotalArq(){
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep ");
		sb.append("   where  o.classeJudicial = ep.classeJudicial  ");
		if (ed.getCompetencia() != null) {
			sb.append("  and ep.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and ep.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		if (ed.getClasseJudicial() != null) {
			sb.append(" and ep.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		sb.append("       and ep.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("       and ep.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("       and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                   where ep2.codEvento like concat(#{parametroUtil.eventoArquivamentoProvisorio.caminhoCompleto}, '%') ");
		sb.append("                               ) ");
		sb.append(") as numProcessArq ");
		return sb.toString();	
	}
}