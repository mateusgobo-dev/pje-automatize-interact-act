package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("orgaoJulgadorSuggest")
@Scope(ScopeType.CONVERSATION)
public class OrgaoJulgadorSuggestBean extends AbstractSuggestBean<OrgaoJulgador> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o ");
		sb.append("where lower(TO_ASCII(o.orgaoJulgador)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.orgaoJulgadorOrdemAlfabetica");
		return sb.toString();
	}

}
