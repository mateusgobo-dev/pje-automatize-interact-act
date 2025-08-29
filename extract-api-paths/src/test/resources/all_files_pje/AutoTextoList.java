package br.com.infox.editor.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.editor.AutoTexto;

@Name(AutoTextoList.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class AutoTextoList extends EntityList<AutoTexto> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "autoTextoList";

	private static final String DEFAULT_EJBQL = "select o from AutoTexto o";
	private static final String DEFAULT_ORDER = "o.descricao";

	@Override
	protected void addSearchFields() {
		addSearchField("descricao", SearchCriteria.contendo);
		addSearchField("localizacao", SearchCriteria.igual, "o.localizacao = #{autoTextoList.entity.localizacao} and o.publico = true");
		addSearchField("usuario", SearchCriteria.igual, "o.usuario = #{autoTextoList.entity.usuario} and o.publico = false");
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
	
	@Override
	public List<AutoTexto> getResultList() {
//		if (Strings.isEmpty(entity.getDescricao())) {
//			return Collections.emptyList();
//		}
		return super.getResultList();
	}
}
