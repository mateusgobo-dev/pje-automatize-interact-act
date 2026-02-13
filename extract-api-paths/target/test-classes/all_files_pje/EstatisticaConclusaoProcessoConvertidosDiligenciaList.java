package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.action.EstatisticaConclusaoAction;

@Name(EstatisticaConclusaoProcessoConvertidosDiligenciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaConclusaoProcessoConvertidosDiligenciaList extends
		AbstractEstatisticaConclusaoProcessoList<Map<String, Object>> {

	public static final String NAME = "estatisticaConclusaoProcessoConvertidosDiligenciaList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new Map(o.processoTrf as processo, o.classeJudicial as classe) from EstatisticaProcessoJusticaFederal o ");
		EstatisticaConclusaoAction ca = EstatisticaConclusaoAction.intance();
		if (ca.getSecao() != null && ca.getOrgaoJulgador() != null && ca.getDataInicio() != null) {
			sb.append("      where o.secaoJudiciaria = #{estatisticaConclusaoAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
			sb.append("          and o.orgaoJulgador = #{estatisticaConclusaoAction.getOrgaoJulgador()} ");
			sb.append("          and o.classeJudicial = o.classeJudicial ");
			sb.append("          and to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaConclusaoAction.dataInicio} ");
			sb.append("          and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaConclusaoAction.dataFim} ");
			sb.append("          and o.codEvento in  (select ep2.codEvento from Evento ep2 ");
			sb.append("                                   where ep2.caminhoCompleto like concat(#{parametroUtil.eventoJulgamentoEmDiligenciaProcessual.caminhoCompleto}, '%') ");
			sb.append("                                ) ");
		}
		return sb.toString();
	}

	@Override
	protected String getEntityName() {
		return NAME;
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
	public List<Map<String, Object>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected void addSearchFields() {
		// TODO Auto-generated method stub
		super.addSearchFields();
	}	
}