package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("orgaoJulgadorByOJCSuggest")
@Scope(ScopeType.CONVERSATION)
public class OrgaoJulgadorByOJCSuggestBean  extends AbstractSuggestBean<OrgaoJulgador> {

	private static final long serialVersionUID = 1L;
	
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador o ");
		sb.append("where o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} and o.orgaoJulgador.ativo = true and lower(o.orgaoJulgador.orgaoJulgador) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.orgaoJulgador.orgaoJulgadorOrdemAlfabetica");
		return sb.toString();
	}

}
