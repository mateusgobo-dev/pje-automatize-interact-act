package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@SuppressWarnings("serial")
@Name("papelSuggest")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class PapelSuggestBean extends AbstractSuggestBean<Papel> {
	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT papel FROM Papel papel ");
		sb.append(" WHERE lower(papel.nome) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append(" AND papel.identificador not like '/%' ");
		sb.append(" AND papel.identificador not like '%:%' ");
		sb.append(" AND papel.nome <> '' ");
		sb.append(" AND papel.nome not like '%.xhtml' ");
		sb.append(" AND papel.nome not like '%.seam' ");
		sb.append(" ORDER BY 1");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}
}