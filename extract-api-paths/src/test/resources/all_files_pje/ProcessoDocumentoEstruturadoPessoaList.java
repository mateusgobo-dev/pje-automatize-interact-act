package br.com.infox.editor.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;

@Name(ProcessoDocumentoEstruturadoPessoaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoDocumentoEstruturadoPessoaList extends EntityList<ProcessoDocumentoEstruturado> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoDocumentoEstruturadoPessoaList";
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumentoEstruturado o " +
												"where o.processoDocumentoTrfLocal.processoDocumento.usuarioInclusao = #{usuarioLogado} " +
												"order by o.idProcessoDocumentoEstruturado desc";
	private static final String DEFAULT_ORDER = "o.processoDocumentoTrfLocal.processoDocumento.processoDocumento";
	
	@Override
	protected void addSearchFields() {
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
}
