package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("orgaoJulgadorSemPostoAvancadoSuggest")
@Scope(ScopeType.CONVERSATION)
public class OrgaoJulgadorSemPostoAvancadoSuggestBean extends AbstractSuggestBean<OrgaoJulgador> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o ");
		sb.append("where lower(o.orgaoJulgador) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) and (o.postoAvancado is null or o.postoAvancado = false ) ");
		sb.append("order by o.orgaoJulgador");
		return sb.toString();
	}

}
