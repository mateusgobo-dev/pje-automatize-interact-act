package br.com.infox.pje.list;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.SearchCriteria;

@Name(SalaHorarioSessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SalaHorarioSessaoList extends SalaHorarioList {

	public static final String NAME = "salaHorarioSessaoList";

	private static final long serialVersionUID = 1L;

	private static final String R2 = "o.sala = #{sessaoHome.getSala()}";
	private static final String R3 = "o.ativo = #{true}";

	@Override
	protected void addSearchFields() {
		addSearchField("salaSessao", SearchCriteria.igual, R2);
		addSearchField("ativo", SearchCriteria.igual, R3);
	}

}