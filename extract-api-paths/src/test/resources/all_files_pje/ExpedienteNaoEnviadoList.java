package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.home.ProcessoExpedienteHome;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

@Name(ExpedienteNaoEnviadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ExpedienteNaoEnviadoList extends EntityList<ProcessoExpediente>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "expedienteNaoEnviadoList";
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoExpediente o "+
												"where not exists(select pde from ProcessoDocumentoExpediente pde" +
												"             	  where pde.processoDocumento.processoDocumentoBin.certChain is not null " +
												"                   and pde.processoDocumento.processoDocumentoBin.signature is not null " +
												"                   and pde.anexo = false " +
												"               	and pde.processoExpediente = o) " +
												"  and o.processoTrf = #{processoTrfHome.instance}" +
												"  and o.dtExclusao is null" +
												"  and o.inTemporario = false " +
												"  and (o.meioExpedicaoExpediente != 'E' or " +
												"         not exists(select ppa from ProcessoParteExpediente ppa " +
												"			         where ppa.processoExpediente = o" +
												"				       and ppa.pendencia is not null))" +
												"  and o.tipoProcessoDocumento != #{parametroUtil.tipoProcessoDocumentoIntimacaoPauta}";
	
	private static final String DEFAULT_ORDER = "o";
	
	private static final String R1 = "o != #{processoExpedienteHome.definedInstance}";
	
	@Override
	public List<ProcessoExpediente> getResultList() {
		List<ProcessoExpediente> list = super.getResultList();
		if (list.size() > 0 && ProcessoExpedienteHome.instance().getExpedienteNaoEnviado() == null) {
			ProcessoExpedienteHome.instance().setExpedienteNaoEnviado(list.get(0));
		}
		return list;
	}
	
	@Override
	protected void addSearchFields() {
		addSearchField("processoExpediente", SearchCriteria.igual, R1);
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
