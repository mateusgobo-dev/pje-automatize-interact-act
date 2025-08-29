package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.RpvUnidadeGestora;

@Name("rpvUnidadeGestoraSuggest")
@BypassInterceptors
public class RpvUnidadeGestoraSuggestBean extends AbstractSuggestBean<RpvUnidadeGestora> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from RpvUnidadeGestora o ");
		sb.append("where (cast(o.codigoUnidadeGestora as string) like concat(:");
		sb.append(INPUT_PARAMETER);
		sb.append(",'%')) ");
		sb.append("or (lower(o.unidadeGestora) like concat('%',lower(:");
		sb.append(INPUT_PARAMETER);
		sb.append("),'%')) ");
		sb.append(" order by o.codigoUnidadeGestora");
		return sb.toString();
	}

}