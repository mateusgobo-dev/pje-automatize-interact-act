package br.com.infox.ibpm.component.tree;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(EventsTipoDocumentoTreeHandler.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventsTipoDocumentoTreeHandler extends EventsTreeHandler {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "eventsTipoDocumentoTree";

	public static EventsTipoDocumentoTreeHandler instance() {
		return (EventsTipoDocumentoTreeHandler) org.jboss.seam.Component
				.getInstance(EventsTipoDocumentoTreeHandler.NAME);
	}

	/**
	 * PJE-JT: David Vieira: [PJE-779] Não disparar o evento de registrar os
	 * movimentos no TarevaEvento
	 */
	protected boolean isRegisterAfterRegisterEvent() {
		return false;
	}
	
	/**
	 * Verifica se o evento já foi lançado.
	 * 
	 * @param documento
	 * @return
	 */
	public Boolean verificaRegistroEventos(ProcessoDocumento documento) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(o.processoDocumento) ");
		sql.append("from ProcessoEvento o where o.processo.idProcesso = :idProcesso ");
		sql.append("and o.processoDocumento.idProcessoDocumento = :idProcessoDoc ");
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("idProcesso", documento.getProcesso().getIdProcesso());
		q.setParameter("idProcessoDoc", documento.getIdProcessoDocumento());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}
}
