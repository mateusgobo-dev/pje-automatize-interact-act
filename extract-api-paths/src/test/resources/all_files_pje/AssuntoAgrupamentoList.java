package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.AssuntoAgrupamento;

@Name(AssuntoAgrupamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AssuntoAgrupamentoList extends EntityList<AssuntoAgrupamento>{

	public static final String NAME = "assuntoAgrupamentoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = " select o from AssuntoAgrupamento o where o.agrupamento.idAgrupamento = "
		+ "#{agrupamentoClasseJudicialHome.instance.idAgrupamento}";
	private static final String DEFAULT_ORDER = "assunto.assuntoCompleto";

	@Override
	protected void addSearchFields(){
/*		addSearchField("assunto.assuntoCompleto", SearchCriteria.contendo);
		addSearchField("assunto.codAssuntoTrf", SearchCriteria.contendo);*/		
	}

	@Override
	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder(){
		// TODO Auto-generated method stub
		return null;
	}

}
