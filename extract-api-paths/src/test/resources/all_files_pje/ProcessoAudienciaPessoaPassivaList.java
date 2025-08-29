package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoAudienciaPessoa;

@Name(ProcessoAudienciaPessoaPassivaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoAudienciaPessoaPassivaList extends EntityList<ProcessoAudienciaPessoa> {

	public static final String NAME = "processoAudienciaPessoaPassivaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoAudienciaPessoa o";
	private static final String DEFAULT_ORDER = "idProcessoAudienciaPessoa";
	private static final String R1 = "o.processoAudiencia.idProcessoAudiencia = #{processoAudienciaHome.instance.idProcessoAudiencia} "
			+ "and o.testemunha = true and "
			+ "o.pessoaRepresentante in (select p.pessoa from ProcessoParte p "
			+ "where (p.inParticipacao = 'P') and "
			+ "(p.processoTrf.idProcessoTrf = o.processoAudiencia.processoTrf.idProcessoTrf))";

	@Override
	protected void addSearchFields() {
		addSearchField("pessoa", SearchCriteria.contendo, R1);
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