package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name("tipoProcessoDocumentoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class TipoProcessoDocumentoSuggestBean extends AbstractSuggestBean<TipoProcessoDocumento> {	

    private static final long serialVersionUID = 5222941744078818466L;

    @Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoProcessoDocumento o ");
		sb.append("where lower(TO_ASCII(o.tipoProcessoDocumento)) ");
		sb.append(" like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("and o.ativo = true ");
		sb.append("order by o.tipoProcessoDocumento");
		return sb.toString();
	}

}
