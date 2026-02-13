package br.com.infox.pje.list;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.bean.EstatisticaProcessosConclusosTipoConclusaoBean;

@Name(EstatisticaProcessosConclusosTipoConclusaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosConclusosTipoConclusaoList extends
		AbstractProcessosConclusosList<EstatisticaProcessosConclusosTipoConclusaoBean> {

	public static final String NAME = "estatisticaProcessosConclusosTipoConclusaoList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_GROUP_BY = "o.codEvento";
	private static final String DEFAULT_ORDER = "o.codEvento ";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new br.com.infox.pje.bean.EstatisticaProcessosConclusosTipoConclusaoBean(o.codEvento) ");
		sb.append("from EstatisticaProcessoJusticaFederal o  ");
		sb.append("where 1=1 ");
		sb.append(getRetornarEventoConclusaoCaminhoCompleto());
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public String getGroupBy() {
		return DEFAULT_GROUP_BY;
	}

	@Override
	public List<EstatisticaProcessosConclusosTipoConclusaoBean> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	public void newInstance() {
		entity = new EstatisticaProcessosConclusosTipoConclusaoBean();
	}
}