package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;

@Name(PessoaAssistenteProcuradoriaLocalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaAssistenteProcuradoriaLocalList extends EntityList<PessoaAssistenteProcuradoriaLocal> {

	public static final String NAME = "pessoaAssistenteProcuradoriaLocalList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from PessoaAssistenteProcuradoriaLocal o ";
	private static final String DEFAULT_ORDER = "dataPosse";

	private static final String R1 = "o.usuario.idUsuario = #{pessoaAssistenteProcuradoriaHome.instance.idUsuario}";
	private static final String R2 = "o.localizacaoFisica = #{authenticator.isPermissaoCadastroTodosPapeis() ? null : usuarioLogadoLocalizacaoAtual.localizacaoFisica}";

	@Override
	protected void addSearchFields() {
		this.addSearchField("procuradoria", SearchCriteria.contendo);
		this.addSearchField("dataPosse", SearchCriteria.contendo);
		this.addSearchField("responsavelLocalizacao", SearchCriteria.igual, R1);
		this.addSearchField("localizacaoFisica", SearchCriteria.igual, R2);
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
	
	public static PessoaAssistenteProcuradoriaLocalList instance() {
		return ComponentUtil.getComponent(NAME);
	}

}