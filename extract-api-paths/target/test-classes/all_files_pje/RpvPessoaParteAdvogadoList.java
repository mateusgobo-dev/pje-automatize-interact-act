package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;

@Name(RpvPessoaParteAdvogadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class RpvPessoaParteAdvogadoList extends EntityList<RpvPessoaParte> {

	public static final String NAME = "rpvPessoaParteAdvogadoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from RpvPessoaParte o";
	private static final String DEFAULT_ORDER = "o.pessoa.nome";

	private static final String R1 = "o.rpv.idRpv = #{rpvAction.rpv.idRpv}";
	private static final String R2 = "o.tipoParte = #{parametroUtil.getTipoParteAdvogado()}";

	@Override
	protected void addSearchFields() {
		addSearchField("rpv", SearchCriteria.igual, R1);
		addSearchField("tipoPessoa", SearchCriteria.igual, R2);

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
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

}