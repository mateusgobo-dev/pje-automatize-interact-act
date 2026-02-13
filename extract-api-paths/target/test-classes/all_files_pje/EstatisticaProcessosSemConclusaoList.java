package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosSemConclusaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosSemConclusaoList extends EntityList<Object[]>{
	
	public static final String NAME = "estatisticaProcessosSemConclusaoList";
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_ORDER = "o.idProcessoTrf";
	
	private static final String R1 = "o in (select e.processoTrf from EstatisticaProcessoJusticaFederal e where e.secaoJudiciaria = #{estatisticaProcessosSemConclusaoAction.secaoJudiciaria.cdSecaoJudiciaria}) ";
	private static final String R2 = "o.orgaoJulgador = #{estatisticaProcessosSemConclusaoAction.orgaoJulgador} ";
	private static final String R3 = "o.cargo.idCargo = #{estatisticaProcessosSemConclusaoAction.cargo.idCargo} ";
	private static final String R4 = "o.classeJudicial in (#{estatisticaProcessosSemConclusaoAction.classeJudicialList}) ";
	private static final String R5 = "o in (select e.processoTrf from EstatisticaProcessoJusticaFederal e where e.competencia.idCompetencia = #{estatisticaProcessosSemConclusaoAction.competencia.idCompetencia}) ";
	
	private String ativos(){
		StringBuilder sb = new StringBuilder();
		sb.append("and (not exists(select a from EstatisticaProcessoJusticaFederal a ");
		sb.append("where a.processoTrf = o.idProcessoTrf and ");
		sb.append("cast(a.dtEvento as date) <= #{estatisticaProcessosSemConclusaoAction.dataFim} and ");
		sb.append("a.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
		sb.append("#{parametroUtil.eventoArquivamentoProvisorio.codEvento}, #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}, ");
		sb.append("#{parametroUtil.eventoSuspensaoDecisaoProcessual.codEvento}, #{parametroUtil.eventoSuspensaoDespachoProcessual.codEvento}, ");
		sb.append("#{parametroUtil.eventoRemetidoTrfProcessual.codEvento})) or ");
		sb.append("exists (select b from EstatisticaProcessoJusticaFederal b ");
		sb.append("where b.processoTrf = o.idProcessoTrf and ");
		sb.append("cast(b.dtEvento as date) <= #{estatisticaProcessosSemConclusaoAction.dataFim} and ");
		sb.append("b.codEvento = #{parametroUtil.eventoReativacaoProcessual.codEvento} and ");
		sb.append("not exists (select c from EstatisticaProcessoJusticaFederal c where ");
		sb.append("c.processoTrf = b.processoTrf and c.dtEvento > b.dtEvento and ");
		sb.append("c.codEvento in (#{parametroUtil.eventoArquivamentoDefinitivoProcessual.codEvento}, ");
		sb.append("#{parametroUtil.eventoArquivamentoProvisorio.codEvento}, #{parametroUtil.eventoBaixaDefinitivaProcessual.codEvento}, ");
		sb.append("#{parametroUtil.eventoSuspensaoDecisaoProcessual.codEvento}, #{parametroUtil.eventoSuspensaoDespachoProcessual.codEvento}, ");
		sb.append("#{parametroUtil.eventoRemetidoTrfProcessual.codEvento}))))");
		return sb.toString();
	}
	
	private String semConclusao(){
		String s = "select o.idProcessoTrf, " + 
		      	   "(select max(epjf.dtEvento) " +  
	      	   		"from EstatisticaProcessoJusticaFederal epjf, " + 
			              "Evento evp " + 
			        "where evp.codEvento = epjf.codEvento " +
			          "and (evp.caminhoCompleto like concat(#{parametroUtil.eventoDespacho.caminhoCompleto}, '%') OR " +
			               "evp.caminhoCompleto like concat(#{parametroUtil.eventoDecisao.caminhoCompleto}, '%') OR " +
			               "evp.caminhoCompleto like concat(#{parametroUtil.eventoJulgamento.caminhoCompleto}, '%')) " +  
			          "and epjf.processoTrf.idProcessoTrf = o.idProcessoTrf " + 
			          "and to_char(epjf.dtEvento, 'yyyy/MM/dd') <= #{estatisticaProcessosSemConclusaoAction.dataFimFormatada}) as dtSemConclusao, " +
			      "(select min(epjf.dtEvento) " + 
			          "from EstatisticaProcessoJusticaFederal epjf " + 
			          "where epjf.processoTrf.idProcessoTrf = o.idProcessoTrf) as dtPrimeiroEventoProcesso " +      
			 "from ProcessoTrf o " + 
			 "where exists (select 1 " +
			                 "from EstatisticaProcessoJusticaFederal epjf0 " +
			                 "where epjf0.processoTrf.idProcessoTrf = o.idProcessoTrf) " +
			   "AND ( " +
			       "not exists (select 1 " +
			                     "from EstatisticaProcessoJusticaFederal epjf1, " +
			                          "Evento evp1 " + 
			                     "where evp1.codEvento = epjf1.codEvento " +                       
			                     "and evp1.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%') " +
			                     "and to_char(epjf1.dtEvento, 'yyyy/MM/dd') <= #{estatisticaProcessosSemConclusaoAction.dataFimFormatada} " +
			                     "and epjf1.processoTrf.idProcessoTrf = o.idProcessoTrf) " +
			        "OR (" +
			        "exists (select 1 " +
			                  "from EstatisticaProcessoJusticaFederal ejpf2, " +
			                       "Evento evp2 " +
			                  "where evp2.codEvento = ejpf2.codEvento " +
			                  "and (evp2.caminhoCompleto like concat(#{parametroUtil.eventoDecisao.caminhoCompleto}, '%') OR " + 
			                       "evp2.caminhoCompleto like concat(#{parametroUtil.eventoDespacho.caminhoCompleto}, '%') OR " +
			                       "evp2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamento.caminhoCompleto}, '%')) " + 
			                  "and to_char(ejpf2.dtEvento, 'yyyy/MM/dd') <= #{estatisticaProcessosSemConclusaoAction.dataFimFormatada} " +
			                  "and ejpf2.processoTrf.idProcessoTrf = o.idProcessoTrf) AND " +
			       "(select max(ejpf3.dtEvento) " + 
			           "from EstatisticaProcessoJusticaFederal ejpf3, " +
			                "Evento evp3 " + 
			           "where evp3.codEvento = ejpf3.codEvento " +
			           "and (evp3.caminhoCompleto like concat(#{parametroUtil.eventoDecisao.caminhoCompleto}, '%') OR " + 
			                "evp3.caminhoCompleto like concat(#{parametroUtil.eventoDespacho.caminhoCompleto}, '%') OR " +
			                "evp3.caminhoCompleto like concat(#{parametroUtil.eventoJulgamento.caminhoCompleto}, '%')) " + 
			           "and to_char(ejpf3.dtEvento, 'yyyy/MM/dd') <= #{estatisticaProcessosSemConclusaoAction.dataFimFormatada} " +
			           "and ejpf3.processoTrf.idProcessoTrf = o.idProcessoTrf) > " +
			       "(select max(ejpf4.dtEvento) " +
			           "from EstatisticaProcessoJusticaFederal ejpf4, " +
			                "Evento evp4 " + 
			           "where evp4.codEvento = ejpf4.codEvento " +
			           "and evp4.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%') " + 
			           "and to_char(ejpf4.dtEvento, 'yyyy/MM/dd') <= #{estatisticaProcessosSemConclusaoAction.dataFimFormatada} " +
			           "and ejpf4.processoTrf.idProcessoTrf = o.idProcessoTrf))) ";
		return s;
	}
	
	
	@Override
	protected void addSearchFields() {
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("cargo", SearchCriteria.igual, R3);
		addSearchField("classeJudicial", SearchCriteria.igual, R4);
		addSearchField("competencia", SearchCriteria.igual, R5);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(semConclusao());
		sb.append(ativos());
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