package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

@Name(ProcessoAudienciaMarcadaList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class ProcessoAudienciaMarcadaList extends EntityList<ProcessoAudiencia> {

	public static final String NAME = "processoAudienciaMarcadaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoAudiencia o";
	private static final String DEFAULT_ORDER = "idProcessoAudiencia DESC";
	private static final String R1 = "o.processoTrf.idProcessoTrf = #{processoTrfHome.instance.idProcessoTrf} ";

	// + "and o.testemunha = true and "
	// + "o.pessoaRepresentante in (select p.pessoa from ProcessoParte p " +
	// "where (p.inParticipacao = 'A') and " +
	// "(p.processoTrf.idProcessoTrf = o.processoAudiencia.processoTrf.idProcessoTrf))";

	@Override
	protected void addSearchFields() {
		addSearchField("idProcessoAudiencia", SearchCriteria.contendo, R1);
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
