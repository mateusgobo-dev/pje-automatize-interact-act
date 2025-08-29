package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;

@Name("tipoComplementoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class TipoComplementoSuggestBean extends AbstractSuggestBean<TipoComplemento>{

	private static final long serialVersionUID = 1L;

	public String getEjbql(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoComplemento o where ");
		sb.append(" lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("and ativo = true ");
		sb.append("order by o.codigo");
		return sb.toString();
	}

}
