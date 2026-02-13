package br.com.infox.cliente.component.suggest;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;

public class DimensaoPessoalSuggestBean extends AbstractSuggestBean<DimensaoPessoal> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from DimensaoPessoal o ");
		sb.append("where lower(o.dimensaoPessoal) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}
}