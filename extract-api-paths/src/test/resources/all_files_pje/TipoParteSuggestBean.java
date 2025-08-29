package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoParte;

@Name("tipoParteSuggest")
@BypassInterceptors
public class TipoParteSuggestBean extends AbstractSuggestBean<TipoParte> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoParte o where ");
		sb.append("lower(TO_ASCII(o.tipoParte)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.tipoParte");
		return sb.toString();
	}

}
