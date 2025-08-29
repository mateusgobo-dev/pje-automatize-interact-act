package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.pje.action.EstatisticaConclusaoAction;

@Name(EstatisticaConclusaoProcessoRemanescenteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaConclusaoProcessoRemanescenteList extends EntityList<Map<String, Object>> {
	
	public static final String NAME = "estatisticaConclusaoProcessoRemanescenteList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial";
	
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new Map(o.processoTrf as processo, o.classeJudicial as classe) from EstatisticaProcessoJusticaFederal o ");
		EstatisticaConclusaoAction ca = EstatisticaConclusaoAction.intance();
		if (ca.getSecao() != null && ca.getOrgaoJulgador() != null && ca.getDataInicio() != null) {
			sb.append("       where o.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
			if (ca.getCompetencia() != null) {
				sb.append(" and o.competencia = #{estatisticaConclusaoAction.competencia} ");
			}
			if (ca.getClasseJudicial() != null) {
				sb.append(" and o.classeJudicial in (#{estatisticaConclusaoAction.classeJudicialList}) ");
			}
			if (ca.getCargoJuiz() != null) {
				sb.append(" and o.cargo = #{estatisticaConclusaoAction.cargoJuiz} ");
			}
			if (ca.getJuiz() != null) {
				sb.append("and exists(select ulms from UsuarioLocalizacaoMagistradoServidor ulms ");
				sb.append("             where ulms.usuarioLocalizacao.usuario = #{estatisticaConclusaoAction.juiz} ");
				sb.append("                and  ulms.orgaoJulgadorCargo.cargo = o.processoTrf.cargo ");
				sb.append("                and  ulms.orgaoJulgador = o.processoTrf.orgaoJulgador ");
				sb.append("                and  ulms.usuarioLocalizacao.usuario.idUsuario in (select pm.idUsuario from PessoaMagistrado pm) ");
				sb.append("                and  ulms.dtInicio <= o.dtEvento ");
				sb.append("                and (ulms.dtFinal is null or (ulms.dtFinal >= o.dtEvento) ) ");
				sb.append("           ) ");
			}
			sb.append("          and o.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
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
	     	sb.append(" )" );
		}
		return sb.toString();	
	}
	
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
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
	protected void addSearchFields() {
		// TODO Auto-generated method stub
		
	}

}