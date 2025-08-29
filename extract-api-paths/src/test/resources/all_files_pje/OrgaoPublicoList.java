package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.PessoaJuridica;

@Name(OrgaoPublicoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoPublicoList extends EntityList<PessoaJuridica> {

	public static final String NAME = "orgaoPublicoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from PessoaJuridica o where o.orgaoPublico = true and o.ativo = true ";
	private static final String DEFAULT_ORDER = "o.nome";

	private static final String R1 = "lower(to_ascii(o.nome)) like lower(to_ascii(concat('%', #{preCadastroPessoaBean.nomePessoaJuridica}, '%')))";

	@Override
	protected void addSearchFields() {
		addSearchField("ramoAtividade", SearchCriteria.igual, R1);
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
