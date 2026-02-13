package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;



@Name(AplicacaoClasseList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class AplicacaoClasseList extends EntityList<AplicacaoClasse> {

	public static final String NAME = "aplicacaoClasseList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from AplicacaoClasse o where o.ativo = true";
	private static final String DEFAULT_ORDER = "aplicacaoClasse";
	
	private static final String R_ATIVO = "o.ativo = #{true}";
	
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

	public List<AplicacaoClasse> listAtivos() {
		addSearchField("ativo", SearchCriteria.contendo, R_ATIVO);
		return super.list();
	}
	
}
