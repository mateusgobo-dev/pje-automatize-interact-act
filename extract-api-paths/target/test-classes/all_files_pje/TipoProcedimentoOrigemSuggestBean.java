package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoProcedimentoOrigem;

@Name("tipoProcedimentoOrigemSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class TipoProcedimentoOrigemSuggestBean extends AbstractSuggestBean<TipoProcedimentoOrigem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return " select o from TipoProcedimentoOrigem o " + " where ativo = true "
				+ " and lower(TO_ASCII(o.dsTipoProcedimento)) like " + " lower(concat('%', TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) " + " order by o.dsTipoProcedimento ";
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}

}
