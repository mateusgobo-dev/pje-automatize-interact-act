package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;

@Name(EstatisticaProcessosArquivadosRankingRegionalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosArquivadosRankingRegionalList extends EntityList<Object[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2154376469882531856L;
	public static final String NAME = "estatisticaProcessosArquivadosRankingRegionalList";
	private static final String DEFAULT_EJBQL = "select o.orgaoJulgador, o.jurisdicao, count(distinct o.numeroProcesso) as qtdProcesso "
			+ "from EstatisticaEventoProcesso o "
			+ "where to_char(o.dataInclusao,'yyyy-MM') >= #{arquivadosRankingRegionalAction.dataInicioFormatada} "
			+ "and to_char(o.dataInclusao,'yyyy-MM') <= #{arquivadosRankingRegionalAction.dataFimFormatada} "
			+ "and (o.codEvento = #{parametroUtil.getEventoArquivamento().codEvento} "
			+ "or o.codEvento = #{parametroUtil.getEventoArquivamentoDefinitivo().codEvento} "
			+ "or o.codEvento = #{parametroUtil.getEventoArquivamentoProvisorio().codEvento}) ";

	private static final String DEFAULT_ORDER = " count(distinct o.numeroProcesso) desc";
	private static final String GROUP_BY = " o.orgaoJulgador, o.jurisdicao";

	@Override
	protected void addSearchFields() {
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
	public List<Object[]> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	public String getGroupBy() {
		return GROUP_BY;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

}