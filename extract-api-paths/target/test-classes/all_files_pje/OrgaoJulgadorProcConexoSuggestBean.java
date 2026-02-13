package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;

@Name("orgaoJulgadorProcConexoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class OrgaoJulgadorProcConexoSuggestBean extends AbstractSuggestBean<ProcessoTrfConexao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct o.orgaoJulgador from ProcessoTrfConexao o ");
		sb.append("where o.prevencao = 'PE' and o.tipoConexao = 'PR' and o.dtPossivelPrevencao != null ");
		sb.append("and lower(TO_ASCII(o.orgaoJulgador)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.orgaoJulgador");
		return sb.toString();
	}
}
