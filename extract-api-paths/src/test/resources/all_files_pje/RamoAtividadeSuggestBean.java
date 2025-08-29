package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.RamoAtividade;

@Name("ramoAtividadeSuggest")
@BypassInterceptors
public class RamoAtividadeSuggestBean extends AbstractSuggestBean<RamoAtividade> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from RamoAtividade o where ");
		sb.append("lower(TO_ASCII(o.ramoAtividade)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.ramoAtividade");
		return sb.toString();
	}

}
