package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(ProcessoDocumentoExternoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoDocumentoExternoList extends EntityList<ProcessoDocumento> {

	public static final String NAME = "processoDocumentoExternoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumento o where "
			+ "o.documentoSigiloso = false " + "and o.tipoProcessoDocumento.publico = true and exists (select pdb from "
			+ "ProcessoDocumentoBinPessoaAssinatura pdb where " + "pdb.processoDocumentoBin = o.processoDocumentoBin)";
	private static final String DEFAULT_ORDER = "o.dataInclusao desc";

	private static final String R1 = "o.processo.idProcesso = #{processoTrfHome.instance.idProcessoTrf} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processo", SearchCriteria.igual, R1);
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
