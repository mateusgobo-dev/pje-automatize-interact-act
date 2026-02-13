package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("modeloLocalizacaoEstruturadaSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ModeloLocalizacaoEstruturadaSuggestBean extends AbstractSuggestBean<Localizacao> {

	private static final long serialVersionUID = 4376637547740125114L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Localizacao o ");
		sb.append("where lower(TO_ASCII(o.localizacao)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("and o.ativo = true ");
		sb.append("and o.estrutura = true ");
		sb.append("order by o.localizacao");
		return sb.toString();
	}
}