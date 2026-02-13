package br.com.infox.pje.list;

import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

@Name(ExpedienteVinculadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ExpedienteVinculadoList extends EntityList<ProcessoExpediente> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "expedienteVinculadoList";

	private static final String DEFAULT_EJBQL = "select o from ProcessoParteExpediente o "
			+ "where o.pendencia is null and exists(select pde from ProcessoDocumentoExpediente pde"
			+ "             where pde.processoDocumento.processoDocumentoBin.certChain is not null "
			+ "               and pde.processoDocumento.processoDocumentoBin.signature is not null "
			+ "               and pde.anexo = false "
			+ "               and pde.processoDocumentoAto = #{processoDocumentoExpedienteHome.processoDocumento}"
			+ "               and pde.processoExpediente = o.processoExpediente)";

	private static final String DEFAULT_ORDER = "o";

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

	public ProcessoDocumento getDocumentoExpediente(ProcessoExpediente pe) {
		if (pe.getProcessoDocumento() == null) {
			String hql = "select pde.processoDocumento from ProcessoDocumentoExpediente pde "
					+ "where pde.anexo = false and pde.processoJudicial = #{processoTrfHome.instance} ";

			Query query = EntityUtil.getEntityManager().createQuery(hql);
			ProcessoDocumento p = EntityUtil.getSingleResult(query);
			pe.setProcessoDocumento(p);
		}
		return pe.getProcessoDocumento();
	}

}
