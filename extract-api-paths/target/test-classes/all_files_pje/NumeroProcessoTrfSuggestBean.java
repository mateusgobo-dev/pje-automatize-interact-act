package br.com.infox.cliente.component.suggest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("numeroProcessoTrfSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class NumeroProcessoTrfSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		String query = "SELECT o.processoTrf FROM SituacaoProcesso AS o "
				+ "	WHERE o.processoTrf.processoStatus = 'D' "
				+ "     AND replace(o.processoTrf.processo.numeroProcesso, '[\\.//-]*', '', 'g') like concat('%', replace(:input, '[\\.//-]*', '', 'g'), '%')"
				+ "		ORDER BY o.processoTrf.processo.numeroProcesso";
		return query;
	}
	
 	@Override
  	public List<ProcessoTrf> suggestList(Object typed){
 		List<ProcessoTrf> result = super.suggestList(typed);
 		
 		Set<ProcessoTrf> conjunto = new HashSet<ProcessoTrf>(result);
 		result = new ArrayList<ProcessoTrf>(conjunto);
 		
  		return result;
	}

	@Override
	public Integer getLimitSuggest() {
		return 10;
	}
}