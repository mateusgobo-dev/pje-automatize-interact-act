package br.com.infox.pje.list;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.bean.EstatisticaProcessosSubListaTipoConclusosBean;

@Name(EstatisticaProcessosSubListaTipoConclusosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosSubListaTipoConclusosList extends
		AbstractProcessosConclusosList<EstatisticaProcessosSubListaTipoConclusosBean> {

	public static final String NAME = "estatisticaProcessosSubListaTipoConclusosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.classeJudicial ";
	private static final String GROUP_BY = "o.classeJudicial, o.processoTrf, o.dtEvento";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new br.com.infox.pje.bean.EstatisticaProcessosSubListaTipoConclusosBean(o.classeJudicial, o.processoTrf, o.dtEvento) ");
		sb.append("from EstatisticaProcessoJusticaFederal o  ");
		sb.append("where 1=1  ");
		sb.append(getRetornarEventoConclusaoCaminhoCompleto());
		return sb.toString();
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
	public List<EstatisticaProcessosSubListaTipoConclusosBean> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected String getCountEjbql() {
		return "select count(distinct o.processoTrf) from EstatisticaProcessoJusticaFederal o";
	}

	@Override
	public void newInstance() {
		entity = new EstatisticaProcessosSubListaTipoConclusosBean();
	}
}