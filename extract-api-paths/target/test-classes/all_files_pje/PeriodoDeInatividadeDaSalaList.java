package br.jus.csjt.pje.persistence.dao;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.jt.entidades.PeriodoDeInatividadeDaSala;

@Name(PeriodoDeInatividadeDaSalaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PeriodoDeInatividadeDaSalaList extends EntityList<PeriodoDeInatividadeDaSala> {

	public static final String NAME = "periodoDeInatividadeDaSalaList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from PeriodoDeInatividadeDaSala o where o.sala.idSala = #{salaHome.id}";
	private static final String DEFAULT_ORDER = " o.inicio desc";

	public PeriodoDeInatividadeDaSalaList() {
		super();
		setOrder("o.ativo desc, o.inicio desc");
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
	protected void addSearchFields() {
		// addSearchField("sala.idSala", SearchCriteria.igual,
		// "#{salaHome.id}");
		// addSearchField("ativo", SearchCriteria.igual, "#{true}");

	}

}
