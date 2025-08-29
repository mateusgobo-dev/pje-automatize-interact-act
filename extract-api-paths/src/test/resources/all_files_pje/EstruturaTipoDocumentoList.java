package br.com.infox.editor.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name(EstruturaTipoDocumentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class EstruturaTipoDocumentoList extends EntityList<TipoProcessoDocumento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "estruturaTipoDocumentoList";
	
	private String tipoProcessoDocumento;
	
	private static final String DEFAULT_EJBQL = "select o from TipoProcessoDocumento o " +
												"where o.ativo = true " +
												"  and not exists(select p from EstruturaTipoDocumento p " +
												"				  where p.tipoProcessoDocumento = o and p.estruturaDocumento = #{estruturaDocumentoHome.instance}) ";
	
	private static final String DEFAULT_ORDER = "o.tipoProcessoDocumento";

	@Override
	protected void addSearchFields() {
		addSearchField("estruturaDocumento", SearchCriteria.igual, "lower(o.tipoProcessoDocumento) like lower(#{estruturaTipoDocumentoList.tipoProcessoDocumento}) || '%'");
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

	public void setTipoProcessoDocumento(String tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public String getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

}
