package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select o.codEstado, o.orgaoJulgador, o.jurisdicao, "
			+
			// Processos Distribuídos
			"(select count(distinct pd.numeroProcesso) from EstatisticaEventoProcesso pd where pd.codEstado = o.codEstado "
			+ "and pd.orgaoJulgador = o.orgaoJulgador "
			+ "and to_char(pd.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataInicioFormatada} "
			+ "and to_char(pd.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataFimFormatada} "
			+ "and pd.codEvento = #{parametroUtil.getEventoDistribuicao().codEvento}) as procDistribuidos, "
			+
			// Processos Julgados
			"(select count(distinct pj.numeroProcesso) from EstatisticaEventoProcesso pj where pj.codEstado = o.codEstado "
			+ "and pj.orgaoJulgador = o.orgaoJulgador "
			+ "and to_char(pj.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataInicioFormatada} "
			+ "and to_char(pj.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataFimFormatada} "
			+ "and pj.codEvento = #{parametroUtil.getEventoJulgamento().codEvento}) as procJulgados, "
			+
			// Processos Arquivados
			"(select count(distinct pa.numeroProcesso) from EstatisticaEventoProcesso pa where (pa.codEstado = o.codEstado) "
			+ "and (pa.orgaoJulgador = o.orgaoJulgador) "
			+ "and to_char(pa.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataInicioFormatada} "
			+ "and to_char(pa.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataFimFormatada} "
			+ "and (pa.codEvento = #{parametroUtil.getEventoArquivamento().codEvento} "
			+ "or   pa.codEvento = #{parametroUtil.getEventoArquivamentoDefinitivo().codEvento} "
			+ "or   pa.codEvento = #{parametroUtil.getEventoArquivamentoProvisorio().codEvento}) ) as procArquivados, "
			+
			// Processos Em Tramitação
			"(select count(distinct e.numeroProcesso) from EstatisticaEventoProcesso e where"
			+ " e.codEstado = o.codEstado "
			+ "and e.orgaoJulgador = o.orgaoJulgador "
			+ "and e.jurisdicao = o.jurisdicao "
			+ "and to_char(e.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataFimFormatada} "
			+ "and e.codEvento not in (#{parametroUtil.getEventoArquivamentoDefinitivo().codEvento}, #{parametroUtil.getEventoBaixaDefinitiva().codEvento}) "
			+ "and e.idEstatisticaProcesso in  (select max(e2.idEstatisticaProcesso) from EstatisticaEventoProcesso e2 "
			+ "                                  where to_char(e2.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataFimFormatada} "
			+ "                                        and to_char(e2.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataInicioFormatada} "
			+ "                                        and e2.numeroProcesso = e.numeroProcesso)) as procTramitacao "
			+ "from EstatisticaEventoProcesso o where 1 = 1 ";

	private static final String DEFAULT_ORDER = "o.codEstado, o.orgaoJulgador";
	private static final String GROUP_BY = "o.codEstado, o.orgaoJulgador, o.jurisdicao";

	private static final String R1 = " to_char(o.dataInclusao,'yyyy-MM') >= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataInicioFormatada} ";
	private static final String R2 = " to_char(o.dataInclusao,'yyyy-MM') <= #{estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.dataFimFormatada} ";
	private static final String R3 = "o.codEstado = #{parametroUtil.primeiroGrau ? parametroUtil.secao : estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.secaoJudiciaria.cdSecaoJudiciaria}";

	@Override
	protected void addSearchFields() {
		addSearchField("dataInclusaoInicio", SearchCriteria.igual, R1);
		addSearchField("dataInclusaoFim", SearchCriteria.igual, R2);
		addSearchField("codEstado", SearchCriteria.igual, R3);
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
	public String getGroupBy() {
		return GROUP_BY;
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