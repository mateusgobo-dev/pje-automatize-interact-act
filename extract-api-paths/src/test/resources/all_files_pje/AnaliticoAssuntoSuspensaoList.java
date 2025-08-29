package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.action.EstatisticaJusticaFederalProcessosDistribuidosAction;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AnaliticoAssuntoSuspensaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AnaliticoAssuntoSuspensaoList extends AbstractProcessoDistribuidoAnaliticoAssunto {

	public static final String NAME = "analiticoAssuntoSuspensaoList";
	private static final long serialVersionUID = 1L;
	static final String DEFAULT_ORDER = "o.processoTrf";

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
		sb.append("select distinct new Map(o.processoTrf as suspenso) from EstatisticaProcessoJusticaFederal o ");
		sb.append("     where o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("         and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		
		if (ed.getClasseJudicial() != null) {
			sb.append(" and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCompetencia() != null) {
			sb.append("  and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCompetencia()} ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append("  and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.getCargoJuiz()} ");
		}
		sb.append("      and to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");
		sb.append("      and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} " );
		sb.append("      and o.codEvento in  (select ep2.codEvento from Evento ep2 ");
		sb.append("                               where ep2.caminhoCompleto like concat(#{parametroUtil.eventoSuspensaoDecisaoProcessual.caminhoCompleto}, '%') ");
		sb.append("                                  or ep2.caminhoCompleto like concat(#{parametroUtil.eventoSuspensaoDespachoProcessual.caminhoCompleto}, '%') ");
		sb.append("                           ) ");
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

	public List<Map<String, ProcessoTrf>> getResultListRecursive(Evento decisaoProcessual,
			Evento despachoProcessual) {
		if (decisaoProcessual != null || despachoProcessual != null) {
			StringBuilder s = new StringBuilder();
			s.append("select new Map(o.processoTrf as suspenso) from EstatisticaProcessoJusticaFederal o where ");
			s.append("o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
			s.append("and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
			s.append("to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} "
					+ "and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
			s.append("and (");

			if (decisaoProcessual != null) {
				s.append("o.codEvento = '" + decisaoProcessual.getCodEvento() + "' ");
			}
			if (despachoProcessual != null) {
				s.append("or o.codEvento = '" + despachoProcessual.getCodEvento() + "' ");
			}

			s.append(") ");

			setEjbql(s.toString());
			List<Map<String, ProcessoTrf>> res = super.getResultList();
			if (res.isEmpty()) {
				return getResultListRecursive(
						EntityUtil
								.getEntityManager()
								.find(Evento.class,
										(decisaoProcessual != null ? (decisaoProcessual.getEventoSuperior() != null ? decisaoProcessual
												.getEventoSuperior().getIdEvento() : 0)
												: 0)),
						(EntityUtil.getEntityManager().find(
								Evento.class,
								despachoProcessual != null ? (despachoProcessual.getEventoSuperior() != null ? despachoProcessual
										.getEventoSuperior().getIdEvento() : 0)
										: 0)));
			} else {
				return res;
			}
		}
		return new ArrayList<Map<String, ProcessoTrf>>();
	}

	@Override
	public List<Map<String, ProcessoTrf>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	// @Override
	// public List<Map<String, ProcessoTrf>> getResultList() {
	// EventoProcessual eventoSuspensaoDespachoProcessual =
	// (EventoProcessual)EntityUtil.getEntityManager().find(EventoProcessual.class,
	// ParametroUtil.instance().getEventoSuspensaoDespachoProcessual().getIdEvento());
	// EventoProcessual eventoSuspensaoDecisaoProcessual =
	// ParametroUtil.instance().getEventoSuspensaoDecisaoProcessual();
	// List<Map<String, ProcessoTrf>> res =
	// getResultListRecursive(eventoSuspensaoDecisaoProcessual,
	// eventoSuspensaoDespachoProcessual);
	// return res;
	// }
}