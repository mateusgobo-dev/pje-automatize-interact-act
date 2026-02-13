package br.com.infox.pje.list;

import java.util.Date;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;

@Name(AudImportacaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class AudImportacaoList extends EntityList<AudImportacao> {

	private static final long serialVersionUID = -3637060500884261227L;

	public static final String NAME = "audImportacaoList";
	private static final String DEFAULT_ORDER = "o.dtInicio";
	private static final String R1 = "cast(o.dtInicio as date) >= #{audImportacaoList.entity.dtInicio}";
	private static final String R2 = "cast(o.dtInicio as date) <= #{audImportacaoList.entity.dtFim}";

	@Override
	protected void addSearchFields() {
		addSearchField("dtInicio", SearchCriteria.contendo, R1);
		addSearchField("dtFim", SearchCriteria.contendo, R2);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		StringBuilder sql = new StringBuilder();

		if (Pessoa.instanceOf(pessoaLogada, PessoaServidor.class) || Pessoa.instanceOf(pessoaLogada, PessoaMagistrado.class)) {
			sql.append("select o from AudImportacao o WHERE 1=1 and o.dtConsolidacao IS NULL ");
			sql.append(getOrgaoJulgadorEjbql());
		}

		return sql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	/**
	 * Foi necessário sobrecarregar esse método para colocar as datas padrões
	 * antes da pesquisa. Autor: Thiago Oliveira Data: 12/10/2011
	 */
	@Override
	public void newInstance() {
		super.newInstance();
		getEntity().setDtInicio(new Date());
		getEntity().setDtFim(new Date());
	}

	public String getOrgaoJulgadorEjbql() {
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
		if (orgaoJulgadorAtual == null) {
			return "";
		}
		return "and o.processoTrf.orgaoJulgador.idOrgaoJulgador = " + orgaoJulgadorAtual.getIdOrgaoJulgador();
	}

}
