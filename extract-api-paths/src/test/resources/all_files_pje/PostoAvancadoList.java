package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name(PostoAvancadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PostoAvancadoList extends EntityList<OrgaoJulgador> {

	public static final String NAME = "postoAvancadoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o.varasAtendidas from OrgaoJulgador o "
			+ "where o = #{orgaoJulgadorHome.instance} ";

	private static final String DEFAULT_ORDER = "o";
	private Long resultCount;
	
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
		return new HashMap<String, String>();
	}

	@Transactional
	@Override
	public Long getResultCount() {
		if (isAnyParameterDirty()) {
			refresh();
		}
		
		javax.persistence.Query query = createQuery();
		resultCount = query == null ? null : Long.valueOf(query.getResultList().size());
		
		return resultCount;
	}
}