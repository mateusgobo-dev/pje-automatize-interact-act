package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(ProcessoDocumentoRedistribuicaoEncaminhamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoDocumentoRedistribuicaoEncaminhamentoList extends EntityList<ProcessoDocumento> {

	public static final String NAME = "processoDocumentoRedistribuicaoEncaminhamentoList";

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumento o";
	private static final String DEFAULT_ORDER = "idProcessoDocumento desc";
	private static final String R1 = "o.processo.idProcesso = #{processoTrfHome.instance.idProcessoTrf} ";
	private static final String R2 = "o.tipoProcessoDocumento.documentoAtoProferido = #{true} ";

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual, R1);
		addSearchField("tipoProcessoDocumento.documentoAtoProferido", SearchCriteria.igual, R2);
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
