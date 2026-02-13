package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AnaliticoAssuntoRedistribuidoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AnaliticoAssuntoRedistribuidoList extends AbstractProcessoDistribuidoAnaliticoAssunto {

	public static final String NAME = "analiticoAssuntoRedistribuidoList";
	private static final long serialVersionUID = 1L;

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected void addSearchFields() {
		super.addSearchFields();
	}	
	
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("select new Map(o.processoTrf as redistribuido) from EstatisticaProcessoJusticaFederal o ");
		sb.append("     where o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("        and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		
		if (ed.getClasseJudicial() != null) {
			sb.append("    and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("    and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("    and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("		   and to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("        and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		sb.append("        and o.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                                 where ep2.caminhoCompleto like concat(#{parametroUtil.eventoProcessualRedistribuicao.caminhoCompleto}, '%') ");
		sb.append("                             ) ");
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