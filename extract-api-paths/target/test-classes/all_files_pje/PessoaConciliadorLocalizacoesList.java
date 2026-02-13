package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name(PessoaConciliadorLocalizacoesList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaConciliadorLocalizacoesList extends EntityList<PessoaFisica> {

	public static final String NAME = "pessoaConciliadorLocalizacoesList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.localizacaoFisica.localizacao";

	private static final String R1 = "o.usuario.idUsuario = #{pessoaFisicaHome.instance.idUsuario}";

	@Override
	protected void addSearchFields() {
		addSearchField("usuario", SearchCriteria.igual, R1);
	}

	@Override
	protected String getDefaultEjbql() {
		Integer idLocalizacaoFisica = Authenticator.getIdLocalizacaoFisicaAtual();
		String idsLocalizacoesFisicasFilhas = Authenticator.getIdsLocalizacoesFilhasAtuais();

		if(idsLocalizacoesFisicasFilhas == null || idsLocalizacoesFisicasFilhas.trim().isEmpty()) {
			idsLocalizacoesFisicasFilhas = idLocalizacaoFisica.toString();
		}
		StringBuilder sb = new StringBuilder("select o from UsuarioLocalizacao o ")
				.append("	WHERE o.papel.identificador = '"+Papeis.CONCILIADOR+"'")
				.append("	AND o.localizacaoFisica.idLocalizacao IN ("+idsLocalizacoesFisicasFilhas+")");
				
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