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

@Name(AnaliticoAssuntoDevolvidoTrfList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AnaliticoAssuntoDevolvidoTrfList extends AbstractProcessoDistribuidoAnaliticoAssunto {

	public static final String NAME = "analiticoAssuntoDevolvidoTrfList";
	private static final long serialVersionUID = 1L;

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		EstatisticaJusticaFederalProcessosDistribuidosAction ed = EstatisticaJusticaFederalProcessosDistribuidosAction.intance();
		sb.append("select new Map(o.processoTrf as delvovidos) from EstatisticaProcessoJusticaFederal o ");
		sb.append("    where to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} ");						
		sb.append("    and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
		if (ed.getClasseJudicial() != null) {
			sb.append(" and o.classeJudicial in (#{estatisticaJusticaFederalProcessosDistribuidosAction.classeJudicialList}) ");
		}
		if (ed.getCargoJuiz() != null) {
			sb.append(" and o.cargo = #{estatisticaJusticaFederalProcessosDistribuidosAction.cargoJuiz} ");
		}
		if (ed.getCompetencia() != null) {
			sb.append(" and o.competencia = #{estatisticaJusticaFederalProcessosDistribuidosAction.competencia} ");
		}
		sb.append("    and o.secaoJudiciaria = #{estatisticaJusticaFederalProcessosDistribuidosAction.getSecao().getCdSecaoJudiciaria()} ");
        sb.append("    and o.orgaoJulgador = #{estatisticaJusticaFederalProcessosDistribuidosAction.getOrgaoJulgador()} ");
		sb.append("    and o.codEvento in  (select ep2.codEvento from Evento ep2 ");				
		sb.append("                               where ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoProcessual.caminhoCompleto}, '%') ");				
		sb.append("                                  or ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoEmDiligenciaProcessual.caminhoCompleto}, '%') ");
		sb.append("                         ) ");
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields() {
		super.addSearchFields();
	}	
	
	@Override
	protected String getEntityName() {
		return NAME;
	}

	/**
	 * método recursivo para procurar processo para o evento de
	 * julgamentoProcessual ou diligencia ou seus filhos
	 * 
	 * @param julgamentoProcessual
	 * @param diligenciaProcessual
	 * @return lista
	 */
	@SuppressWarnings("unused")
	private List<Map<String, ProcessoTrf>> getResultListRecursive(Evento julgamentoProcessual,
			Evento diligenciaProcessual) {
		if (julgamentoProcessual != null || diligenciaProcessual != null) {
			StringBuilder s = new StringBuilder(
					"select new Map(o.processoTrf as delvovidos) from EstatisticaProcessoJusticaFederal o where ");
			s.append("to_char(o.dtEvento, 'yyyy-MM-dd') >= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataInicioFormatada} and to_char(o.dtEvento, 'yyyy-MM-dd') <= #{estatisticaJusticaFederalProcessosDistribuidosAction.dataFimFormatada} ");
			s.append("and ( ");
			if (julgamentoProcessual != null) {
				s.append("o.codEvento = '" + julgamentoProcessual.getCodEvento() + "' ");
			}
			if (diligenciaProcessual != null) {
				s.append("or o.codEvento = '" + diligenciaProcessual.getCodEvento() + "' ");
			}
			s.append(") ");
			setEjbql(s.toString());
			List<Map<String, ProcessoTrf>> res = super.getResultList();
			if (res.isEmpty()) {
				return getResultListRecursive(
						EntityUtil
								.getEntityManager()
								.find(Evento.class,
										(julgamentoProcessual != null ? (julgamentoProcessual.getEventoSuperior() != null ? julgamentoProcessual
												.getEventoSuperior().getIdEvento() : 0)
												: 0)),
						(EntityUtil.getEntityManager().find(
								Evento.class,
								diligenciaProcessual != null ? (diligenciaProcessual.getEventoSuperior() != null ? diligenciaProcessual
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

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		// TODO Auto-generated method stub
		return null;
	}
}