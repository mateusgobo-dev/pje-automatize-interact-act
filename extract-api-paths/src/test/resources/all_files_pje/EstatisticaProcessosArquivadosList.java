package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosArquivadosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosArquivadosList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaProcessosArquivadosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select distinct new map(o.codEstado as codEstado, "
			+ "(select count(distinct ep.numeroProcesso) from EstatisticaEventoProcesso ep where ep.codEstado = o.codEstado "
			+ "and to_char(ep.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosArquivadosAction.dataInicioFormatada} "
			+ "and to_char(ep.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosArquivadosAction.dataFimFormatada} "
			+ "and (ep.codEvento = #{parametroUtil.getEventoArquivamento().codEvento} or "
			+ "	  ep.codEvento = #{parametroUtil.getEventoArquivamentoProvisorio().codEvento} or "
			+ "     ep.codEvento = #{parametroUtil.getEventoArquivamentoDefinitivoProcessual().codEvento})) as qtd) "
			+ "from EstatisticaEventoProcesso o "
			+ "where (o.codEvento = #{parametroUtil.getEventoArquivamento().codEvento} or "
			+ "o.codEvento = #{parametroUtil.getEventoArquivamentoProvisorio().codEvento} or "
			+ "o.codEvento = #{parametroUtil.getEventoArquivamentoDefinitivoProcessual().codEvento}) ";

	private static final String DEFAULT_ORDER = "o.codEstado";

	private static final String R1 = "to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosArquivadosAction.dataInicioFormatada} ";
	private static final String R2 = "to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosArquivadosAction.dataFimFormatada} ";
	private static final String R3 = "o.codEstado = #{parametroUtil.primeiroGrau ? parametroUtil.secao : estatisticaProcessosArquivadosAction.secaoJudiciaria.cdSecaoJudiciaria} ";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
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
	public void newInstance() {
		entity = new HashMap<String, Object>();
	}

}