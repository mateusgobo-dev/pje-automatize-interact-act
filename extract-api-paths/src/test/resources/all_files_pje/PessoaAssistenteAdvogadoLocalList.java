package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogadoLocal;

@Name(PessoaAssistenteAdvogadoLocalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaAssistenteAdvogadoLocalList extends EntityList<PessoaAssistenteAdvogadoLocal> {

	public static final String NAME = "pessoaAssistenteAdvogadoLocalList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "dataPosse";

	private static final String R1 = "o.usuario.idUsuario = #{pessoaAssistenteAdvogadoHome.instance.idUsuario}";

	@Override
	protected void addSearchFields() {
		addSearchField("usuario", SearchCriteria.igual, R1);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder("SELECT o FROM PessoaAssistenteAdvogadoLocal o ");
		if (!Authenticator.isPapelAdministrador()) {
			sb.append("WHERE o.localizacaoFisica.idLocalizacao = #{authenticator.getIdLocalizacaoAtual()}");
		}
		return sb.toString();
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