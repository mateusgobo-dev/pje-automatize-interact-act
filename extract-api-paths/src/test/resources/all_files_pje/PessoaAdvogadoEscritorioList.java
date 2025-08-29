package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name(PessoaAdvogadoEscritorioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaAdvogadoEscritorioList extends EntityList<UsuarioLocalizacao> {

	public static final String NAME = "pessoaAdvogadoEscritorioList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from UsuarioLocalizacao o ";
	private static final String DEFAULT_ORDER = "localizacao";

	private static String R1 = "o.usuario.idUsuario = #{pessoaAdvogadoHome.instance.idUsuario}";

	@Override
	protected void addSearchFields() {
		addSearchField("usuario", SearchCriteria.igual, R1);
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