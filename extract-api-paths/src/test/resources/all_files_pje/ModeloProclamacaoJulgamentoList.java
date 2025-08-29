package br.com.infox.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;

@Name(ModeloProclamacaoJulgamentoList.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class ModeloProclamacaoJulgamentoList extends EntityList<ModeloProclamacaoJulgamento> {

	private static final long serialVersionUID = 2539557609486736561L;
	
	public static final String NAME = "modeloProclamacaoJulgamentoList";

	@Override
	protected void addSearchFields() {
		addSearchField("nomeModelo", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
	}

	@Override
	protected String getDefaultEjbql() {
		return "SELECT o FROM ModeloProclamacaoJulgamento o LEFT JOIN o.usuario u";
	}

	@Override
	protected String getDefaultOrder() {
		return "o.nomeModelo ASC";
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("nomeModelo", "o.nomeModelo");
		map.put("dataAtualizacao", "o.dataAtualizacao");
		map.put("ativo", "o.ativo");
		
		return map;
	}

}
