package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;

@Name(RpvCessionarioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class RpvCessionarioList extends EntityList<RpvPessoaParte> {

	public static final String NAME = "rpvCessionarioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from RpvPessoaParte o "
			+ "where o.rpv.idRpv = #{rpvAction.rpv.idRpv} "
			+ " and o.tipoParte = #{parametroUtil.tipoParteCessionario}";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return null;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

}