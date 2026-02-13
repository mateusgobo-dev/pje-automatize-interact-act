package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("orgaoJulgadorCPSuggest")
@Scope(ScopeType.CONVERSATION)
public class OrgaoJulgadorCPSuggestBean extends AbstractSuggestBean<OrgaoJulgador> {
 
	private static final long serialVersionUID = 1L;

	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o ");
		sb.append("where o.ativo = true and lower(TO_ASCII(o.orgaoJulgador)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		if(!ParametroUtil.instance().isPrimeiroGrau()){			
			sb.append("and o in (select ojcoj.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ojcoj where ");
			sb.append("ojcoj.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()})"); 
		}
		sb.append("order by o.orgaoJulgadorOrdemAlfabetica");
		return sb.toString();
	}

}