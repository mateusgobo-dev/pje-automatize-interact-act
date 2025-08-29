package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoParte;

@Name("poloAtivoSuggest")
@BypassInterceptors
public class PoloAtivoSuggestBean extends AbstractSuggestBean<TipoParte> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return "select o from TipoParte o " + "where lower(TO_ASCII(o.tipoParte)) like lower(concat('%',TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) order by o.tipoParte";
	}

}
