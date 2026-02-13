package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

@Name(DetalheProcessoParteExpedienteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class DetalheProcessoParteExpedienteList extends EntityList<ProcessoExpediente> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "detalheProcessoParteExpedienteList";

	private static final String DEFAULT_EJBQL = "select o from ProcessoParteExpediente o "+
												"where o.processoExpediente = #{processoExpedienteHome.instance}  " +
												"and o.pendencia is null " +
												"and o.processoExpediente.inTemporario is false " +
												"and o.processoExpediente.dtExclusao is null " +
												"and not exists (select ppe from ProcessoParteExpediente ppe " +
												"inner join ppe.processoExpediente pe " +
												"inner join pe.processoDocumentoExpedienteList pdList  " +
												"inner join pdList.processoDocumento pd " +
												"inner join pd.processoDocumentoBin pdBin " +
												"where (pdBin.signature is null or pdBin.signature = '') and " +
												"(pdBin.certChain is null or pdBin.certChain = '') and " +
												"pdList.anexo = false and ppe = o) ";

	private static final String DEFAULT_ORDER = "o.processoExpediente.dtCriacao";

	@Override
	protected void addSearchFields() {
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
