package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;

@Name("pessoaPeritoEspecialidadeSuggest")
@BypassInterceptors
public class PessoaPeritoEspecialidadeSuggestBean extends AbstractSuggestBean<PessoaPeritoEspecialidade> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return "select o from PessoaPeritoEspecialidade o where "
				+ "lower(TO_ASCII(o.pessoaPerito.nome)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) order by o.pessoaPerito.nome";
	}

}
