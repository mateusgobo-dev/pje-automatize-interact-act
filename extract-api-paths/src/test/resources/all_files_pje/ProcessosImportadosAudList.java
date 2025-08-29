package br.jus.csjt.pje.view.action.component.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;

@Name(ProcessosImportadosAudList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessosImportadosAudList extends EntityList<Object> {

	public static final String NAME = "processosImportadosAudList";

	private static final long serialVersionUID = 1L;
	
	private Long qtdProcessosImportados; 

	private static final String DEFAULT_EJBQL = "SELECT pajt, sp "
			+ "FROM ProcessoAudienciaJT pajt, SituacaoProcesso sp "
			+ "WHERE pajt.processoAudiencia.processoTrf.idProcessoTrf = sp.idProcesso AND pajt.verificada = false "
			+ " 		AND exists ( from ProcessoDocumentoBinPessoaAssinatura pdb "
			+ "                 where pdb.processoDocumentoBin = pajt.processoAudiencia.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin ) ";

	private static final String DEFAULT_ORDER = "pajt.idProcessoAudienciaJt";

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
	
	@Override
	public Long getResultCount() {
		if (qtdProcessosImportados == null) {
			qtdProcessosImportados = super.getResultCount();
		}
		
		return qtdProcessosImportados;
	}
	
}
