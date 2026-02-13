package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoPichacao;

@Name("tipoPichacaoSuggest")
public class TipoPichacaoSuggestBean extends AbstractSuggestBean<TipoPichacao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoPichacao o where ");
		sb.append("lower(TO_ASCII(o.tipoPichacao)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.tipoPichacao");
		return sb.toString();
	}

}
