package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name(PessoaAdvogadoParteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaAdvogadoParteList extends EntityList<UsuarioLocalizacao> {

	public static final String NAME = "pessoaAdvogadoParteList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "idUsuarioLocalizacao";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder("SELECT ul FROM UsuarioLocalizacao ul ");
		sb.append("	WHERE 1=1 ")
			.append(" AND ul.responsavelLocalizacao = TRUE ")
			.append(" AND ul.papel.identificador in ('"+ Papeis.ADVOGADO +"','" + Papeis.PJE_ADVOGADO + "')")
			.append(" AND ul.localizacaoFisica.idLocalizacao = #{usuarioLogadoLocalizacaoAtual.localizacaoFisica.idLocalizacao}");
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