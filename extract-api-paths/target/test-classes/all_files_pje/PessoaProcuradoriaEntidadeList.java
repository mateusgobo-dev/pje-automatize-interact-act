package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;

@Name(PessoaProcuradoriaEntidadeList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaProcuradoriaEntidadeList extends EntityList<PessoaProcuradoriaEntidade> {

	public static final String NAME = "pessoaProcuradoriaEntidadeList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from PessoaProcuradoriaEntidade o ";

	private static final String DEFAULT_ORDER = "procuradoria";

	private static final String R1 = "o.procuradoria = #{pessoaAssistenteProcuradoriaLocalHome.procuradoria}";
	private static final String R2 = "o.procuradoria.localizacao = #{authenticator.isProcurador() or "
			+ "Authenticator.isAssistenteProcurador() "
			+ "? usuarioLogadoLocalizacaoAtual.localizacaoFisica : null}";

	private static final String R3 = "o.procuradoria in (select pp.procuradoria from PessoaAssistenteProcuradoriaLocal pp "
			+ "where pp.usuario.idUsuario = #{pessoaAssistenteProcuradoriaHome.instance.idUsuario})";

	@Override
	protected void addSearchFields() {
		this.addSearchField("procuradoria", SearchCriteria.contendo, R1);
		this.addSearchField("procuradoria.localizacao", SearchCriteria.igual, R2);
		this.addSearchField("procuradoria.idProcuradoria", SearchCriteria.contendo, R3);
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