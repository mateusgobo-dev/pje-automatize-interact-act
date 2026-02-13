package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;

@Name(PessoaDocumentoIdentificacaoCadastroList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaDocumentoIdentificacaoCadastroList extends EntityList<PessoaDocumentoIdentificacao> {

	public static final String NAME = "pessoaDocumentoIdentificacaoCadastroList";

	private static final String R1 = "o.pessoa.idUsuario = #{pessoaHome.instance.idUsuario}";
	private static final long serialVersionUID = 1L;

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaDocumentoIdentificacao o where o.ativo = true ");
		String papel = Authenticator.getPapelAtual().getIdentificador();
		if ((papel.equals("advogado") || papel.equals("procurador") || papel.equals("assistProcuradoria")
				|| papel.equals("assistAdvogado") || papel.equals("assistGestorAdvogado"))) {

			sb.append(" and o.tipoDocumento.tipoDocumento = 'CPF'");
		}
		return sb.toString();
	}

	private static final String DEFAULT_ORDER = "idDocumentoIdentificacao";

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields() {
		addSearchField("classeJudicial", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

}