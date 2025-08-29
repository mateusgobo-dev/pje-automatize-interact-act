package br.com.infox.cliente.component.suggest;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.Util;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.NaturezaClet;

@Name("naturezaExecucaoSuggest")
@BypassInterceptors
public class NaturezaExecucaoSuggestBean extends AbstractSuggestBean<NaturezaClet>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final LogProvider LOG = Logging.getLogProvider(NaturezaLiquidacaoSuggestBean.class);

	@Override
	public String getEjbql() {
		return "select o from NaturezaClet o where o.ativo = true and o.tipoNatureza = 'E' "
		+ "and lower(TO_ASCII(o.dsNatureza)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
		+ "), '%')) order by o.dsNatureza";
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<NaturezaClet> suggestList(Object typed){
		
		List<NaturezaClet> result = null;
		String q = getEjbql();
		
		if (q != null && Util.isStringSemCaracterUnicode(typed.toString())){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);			
			result = query.getResultList();
		}
		else{
			result = Collections.emptyList();
		}
		return result;
	}

}
