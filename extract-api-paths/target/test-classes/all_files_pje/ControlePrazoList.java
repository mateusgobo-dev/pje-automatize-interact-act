package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

@Name(ControlePrazoList.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ControlePrazoList extends EntityList<ProcessoParteExpediente> {

	public static final String NAME = "controlePrazoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoParteExpediente o "
			+ "where o.pendencia is null " + "and o.processoExpediente.dtExclusao is null " + "and not exists( "
			+ "select ppe from ProcessoParteExpediente ppe " + "inner join ppe.processoExpediente pe "
			+ "inner join pe.processoDocumentoExpedienteList pdList " + "inner join pdList.processoDocumento pd "
			+ "inner join pd.processoDocumentoBin pdBin " + "where pdBin.signature is null and "
			+ "pdBin.certChain is null and " + "pdList.anexo = false and " + "ppe = o)";
	private static final String DEFAULT_ORDER = "processoExpediente.dtCriacao desc ";

	private static String R1 = "o.processoJudicial = " + "#{controlePrazoTaskPageAction.processoTrf}";

	@Override
	protected void addSearchFields() {
		addSearchField("o.processoJudicial", SearchCriteria.igual, R1);
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public static ControlePrazoList instance() {
		return (ControlePrazoList) Component.getInstance(NAME);
	}

}