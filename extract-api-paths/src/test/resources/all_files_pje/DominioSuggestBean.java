package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;

@Name("dominioSuggest")
@BypassInterceptors
public class DominioSuggestBean extends AbstractSuggestBean<Dominio>{

	private static final long serialVersionUID = 1L;

	public String getEjbql(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Dominio o where ");
		sb.append("lower(TO_ASCII(o.nomeDominio)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.nomeDominio");
		return sb.toString();
	}

}
