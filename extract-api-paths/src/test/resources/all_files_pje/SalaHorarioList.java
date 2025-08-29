package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.SalaHorario;

@Name(SalaHorarioList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class SalaHorarioList extends EntityList<SalaHorario> {

	public static final String NAME = "salaHorarioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from SalaHorario o";
	private static final String DEFAULT_ORDER = "idSalaHorario";

	private static final String R1 = "o.sala = #{salaHome.definedInstance}";
	private static final String R2 = "o.sala = #{sessaoHome.getSala()}";
	private static final String R3 = "o.sala = #{sessaoAction.sala}";

	@Override
	protected void addSearchFields() {
		addSearchField("sala", SearchCriteria.igual, R1);
		addSearchField("salaSessao", SearchCriteria.igual, R2);
		addSearchField("salaSessaoJT", SearchCriteria.igual, R3);
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

}