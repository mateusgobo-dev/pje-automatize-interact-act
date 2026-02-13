package br.com.infox.editor.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;

@Name(ProcessoDocumentoEstruturadoTopicoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoDocumentoEstruturadoTopicoList extends EntityList<ProcessoDocumentoEstruturadoTopico> {

	public static final String NAME = "processoDocumentoEstruturadoTopicoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumentoEstruturadoTopico o " +
												"where o.processoDocumentoEstruturado = #{editorAction.processoDocumentoEstruturado}";
	private static final String DEFAULT_ORDER = "ordem";

	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected void addSearchFields() {
		// TODO Auto-generated method stub
		
	}
}