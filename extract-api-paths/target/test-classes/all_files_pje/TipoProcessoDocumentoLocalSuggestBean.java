package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name("tipoProcessoDocumentoLocalSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class TipoProcessoDocumentoLocalSuggestBean extends AbstractSuggestBean<TipoProcessoDocumento> {

    private static final long serialVersionUID = 631456730414961578L;

    @Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoProcessoDocumento o ");
		sb.append("where lower(o.tipoProcessoDocumento) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("and o.ativo = true ");
		sb.append("order by o.tipoProcessoDocumento");
		return sb.toString();
	}

}
