package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ModificacaoParametroDistribuicao;

@Name(ModificacaoParametroDistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ModificacaoParametroDistribuicaoList extends EntityList<ModificacaoParametroDistribuicao> {

	public static final String NAME = "modificacaoParametroDistribuicaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ModificacaoParametroDistribuicao o";

	private static final String DEFAULT_ORDER = "idModificacaoParametroDistribuicao";

	/*
	 * private static final String R1 =
	 * "o in(select oj.sala from SalaHorario oj " +
	 * "where oj.ativo = #{salaList.entity.ativo})";
	 * 
	 * private static final String R3 =
	 * "o.orgaoJulgador = #{salaAudienciaHome.orgaoJulgadorAtualSala}";
	 */

	@Override
	protected void addSearchFields() {
		addSearchField("descricao", SearchCriteria.contendo);
		addSearchField("atoNormativo", SearchCriteria.contendo);
		/*
		 * if(!Authenticator.getPapelAtual().getNome().contains("Administrador"))
		 * { addSearchField("orgaoJulgador", SearchCriteria.igual, R3);
		 * 
		 * } addSearchField("salaAudiencia", SearchCriteria.contendo);
		 * addSearchField("ativo", SearchCriteria.igual, R1);
		 */
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
