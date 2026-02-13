package br.com.infox.pje.list;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.bean.EstatisticaProcessosConclusosBean;

@Name(EstatisticaProcessosConclusosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaProcessosConclusosList extends
		AbstractProcessosConclusosList<EstatisticaProcessosConclusosBean> {

	public static final String NAME = "estatisticaProcessosConclusosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = "select distinct new br.com.infox.pje.bean.EstatisticaProcessosConclusosBean(o.pessoaMagistrado) "
			+ "from EstatisticaProcessoJusticaFederal o " + "where 1=1 ";
	private static final String DEFAULT_GROUP_BY = "o.pessoaMagistrado";
	private static final String DEFAULT_ORDER = "o.pessoaMagistrado";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_EJBQL);
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
	public void newInstance() {
		entity = new EstatisticaProcessosConclusosBean();
	}

	@Override
	public List<EstatisticaProcessosConclusosBean> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
}