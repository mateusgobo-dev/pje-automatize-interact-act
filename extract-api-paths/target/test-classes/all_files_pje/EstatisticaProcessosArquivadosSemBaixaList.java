package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.pje.bean.EstatisticaProcessosArquivadosSemBaixaBean;

@Name(EstatisticaProcessosArquivadosSemBaixaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosArquivadosSemBaixaList extends EntityList<EstatisticaProcessosArquivadosSemBaixaBean> {

	public static final String NAME = "estatisticaProcessosArquivadosSemBaixaList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.dtEvento";

	private static final String R1 = "o.orgaoJulgador = #{estatisticaProcessosArquivadosSemBaixaAction.orgaoJulgador}";
	private static final String R2 = "cast(o.dtEvento as date) >= #{estatisticaProcessosArquivadosSemBaixaAction.dataInicio}";
	private static final String R3 = "cast(o.dtEvento as date) <= #{estatisticaProcessosArquivadosSemBaixaAction.dataFim}";
	private static final String R4 = "#{estatisticaProcessosArquivadosSemBaixaAction.entidade} in (select ppL.pessoa from ProcessoParte ppL where ppL.processoTrf = o.processoTrf)";
	private static final String DEFAULT_GROUP_BY = "o.dtEvento, o.processoTrf, o.codEvento";

	private boolean arquivados = false;
	private boolean arquivamento = true;
	private boolean suspensao = true;
	private boolean remessas = true;

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("dataInicio", SearchCriteria.igual, R2);
		addSearchField("dataFim", SearchCriteria.igual, R3);
		addSearchField("entidade", SearchCriteria.igual, R4);
	}

	@Override
	public void newInstance() {

	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new br.com.infox.pje.bean.EstatisticaProcessosArquivadosSemBaixaBean(o.processoTrf, ");
		sb.append("max(o.dtEvento), ");
		sb.append(getDataDesarquivamento());
		sb.append("as dataDesarquivamento, ");
		sb.append(getEventoProcessual());
		sb.append("from EstatisticaProcessoJusticaFederal o ");
		sb.append("where o.codEvento in ");
		sb.append(getEventosArquivadosSemBaixa());
		if (arquivados) {
			sb.append("and ");
			sb.append(getDataDesarquivamento());
			sb.append("is null");
		}

		return sb.toString();
	}

	private String getDataDesarquivamento() {
		String s = "(select min(p.dtEvento) from EstatisticaProcessoJusticaFederal p "
				+ "where p.processoTrf = o.processoTrf and " + "p.dtEvento > o.dtEvento "
				+ "and p.dtEvento >= #{estatisticaProcessosArquivadosSemBaixaAction.dataInicio} "
				+ "and p.dtEvento <= #{estatisticaProcessosArquivadosSemBaixaAction.dataFim} "
				+ "and p.codEvento in (#{parametroUtil.eventoDesarquivamentoProcessual.codEvento}, "
				+ "                    #{parametroUtil.eventoRecebimentoProcessual.codEvento})) ";
		return s;
	}

	private String getEventoProcessual() {
		String s = "(select ev.evento from Evento ev " + "where ev.codEvento = o.codEvento)) ";
		return s;
	}

	private String getEventosArquivadosSemBaixa() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("select evtP.codEvento from Evento evtP ");
		sb.append("where evtP.codEvento in ");
		if (arquivamento) {
			sb.append("(#{parametroUtil.eventoArquivamentoProvisorio.codEvento}) ");
		}

		if ((arquivamento && remessas) || (arquivamento && suspensao)) {
			sb.append("or evtP.codEvento in ");
		}

		if (remessas) {
			sb.append("(#{parametroUtil.eventoRemetidoTrf.codEvento}) ");
		}

		if (remessas && suspensao) {
			sb.append("or evtP.codEvento in ");
		}

		if (suspensao) {
			sb.append("(select evt.codEvento from Evento evt ");
			sb.append("where evt.caminhoCompleto like concat(#{parametroUtil.eventoSuspensaoDecisao.caminhoCompleto}, '%') ");
			sb.append("or evt.caminhoCompleto like concat(#{parametroUtil.eventoSuspensaoDespacho.caminhoCompleto}, '%'))");
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public List<EstatisticaProcessosArquivadosSemBaixaBean> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
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
	public String getGroupBy() {
		return DEFAULT_GROUP_BY;
	}

	public boolean isArquivados() {
		return arquivados;
	}

	public void setArquivados(boolean arquivados) {
		this.arquivados = arquivados;
	}

	public boolean isArquivamento() {
		return arquivamento;
	}

	public void setArquivamento(boolean arquivamento) {
		this.arquivamento = arquivamento;
	}

	public boolean isSuspensao() {
		return suspensao;
	}

	public void setSuspensao(boolean suspensao) {
		this.suspensao = suspensao;
	}

	public boolean isRemessas() {
		return remessas;
	}

	public void setRemessas(boolean remessas) {
		this.remessas = remessas;
	}
}