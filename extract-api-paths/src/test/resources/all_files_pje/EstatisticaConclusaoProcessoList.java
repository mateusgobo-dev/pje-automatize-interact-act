package br.com.infox.pje.list;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.bean.EstatisticaJFConclusaoProcessoClasses;

@Name(EstatisticaConclusaoProcessoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaConclusaoProcessoList extends
		AbstractEstatisticaConclusaoProcessoList<EstatisticaJFConclusaoProcessoClasses> {

	public static final String NAME = "estatisticaConclusaoProcessoList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial";
	private static final String GROUP_BY = "o.classeJudicial";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new br.com.infox.pje.bean.EstatisticaJFConclusaoProcessoClasses(o.classeJudicial as classe, ");
		sb.append(getTotalEventoRemanescente());
		sb.append(getTotalEventoConclusosSentenca());
		sb.append(getTotalEventoDevolvidosSentenca());
		sb.append(getTotalEventoConvertidosDiligencia());
		sb.append(") from EstatisticaProcessoJusticaFederal o ");
		sb.append("where o.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
		sb.append("and o.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
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
	public List<EstatisticaJFConclusaoProcessoClasses> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	private String getTotalEventoRemanescente(){
		StringBuilder sb = new StringBuilder();
		sb.append(" ( select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep ");
		sb.append("       where ep.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
		sb.append("          and ep.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
		sb.append("          and (not exists (select epjf.processoTrf from EstatisticaProcessoJusticaFederal epjf ");
		sb.append("                              where to_char(epjf.dtEvento,'yyyy-MM-dd') < #{estatisticaConclusaoAction.dataInicio} ");
		sb.append("                                 and o.classeJudicial = epjf.classeJudicial");
		sb.append("                                 and epjf.codEvento in(#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
     	sb.append("                                                       #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
		sb.append("                           ) or ");
		sb.append("                           ( (select max(epjf2.dtEvento) from EstatisticaProcessoJusticaFederal epjf2 ");
		sb.append("                                   where to_char(epjf2.dtEvento,'yyyy-MM-dd') < #{estatisticaConclusaoAction.dataInicio} ");
		sb.append("                                     and o.classeJudicial = epjf2.classeJudicial ");
		sb.append("                                     and epjf2.codEvento in(#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
     	sb.append("                                                            #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}) ");
     	sb.append("                              ) < " );
     	sb.append("                              (select max(epjf3.dtEvento) from EstatisticaProcessoJusticaFederal epjf3  ");
     	sb.append("                                   where to_char(epjf3.dtEvento,'yyyy-MM-dd') < #{estatisticaConclusaoAction.dataInicio} ");
     	sb.append("                                      and o.classeJudicial = epjf3.classeJudicial ");
     	sb.append("                                      and epjf3.codEvento in (#{parametroUtil.getEventoReativacaoProcessual().codEvento}) ");
     	sb.append("                              ) ");
     	sb.append("                           ) ");
     	sb.append("               ) ");
 	    sb.append(" ) as numProcessRemanescente, " );
		return sb.toString();
	}
	
	
	private String getTotalEventoConclusosSentenca(){
		StringBuilder sb = new StringBuilder();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep ");
		sb.append("    where ep.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
		sb.append("       and ep.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
		sb.append("       and ep.classeJudicial = o.classeJudicial ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaConclusaoAction.dataInicio} ");
		sb.append("       and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaConclusaoAction.dataFim} ");
		sb.append("       and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                              where ep2.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%') ");
		sb.append("                            ) ");
		sb.append(") as numProcessConclusosSentenca, ");
		return sb.toString();
	}
	
	
	private String getTotalEventoDevolvidosSentenca(){
		StringBuilder sb = new StringBuilder();
		sb.append("(select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep  ");
		sb.append("     where ep.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
		sb.append("         and ep.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
		sb.append("         and ep.classeJudicial = o.classeJudicial ");
		sb.append("         and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaConclusaoAction.dataInicio} ");
		sb.append("         and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaConclusaoAction.dataFim} ");
		sb.append("         and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                   where ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoProcessual.caminhoCompleto}, '%') ");
		sb.append("                              ) ");
		sb.append(") as numProcessDevolvidosSentenca, ");
		return sb.toString();
	}
	
	
	private String getTotalEventoConvertidosDiligencia(){
		StringBuilder sb = new StringBuilder();
		sb.append(" (select count(distinct ep.processoTrf) from EstatisticaProcessoJusticaFederal ep ");
		sb.append("      where ep.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
		sb.append("          and ep.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
		sb.append("          and ep.classeJudicial = o.classeJudicial ");
		sb.append("          and to_char(ep.dtEvento,'yyyy-MM-dd') >= #{estatisticaConclusaoAction.dataInicio} ");
		sb.append("          and to_char(ep.dtEvento,'yyyy-MM-dd') <= #{estatisticaConclusaoAction.dataFim} ");
		sb.append("          and ep.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                   where ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoEmDiligenciaProcessual.caminhoCompleto}, '%') ");
		sb.append("                                ) ");
		sb.append(") as numProcessConvertidosDiligencia ");
		return sb.toString();
	}
}