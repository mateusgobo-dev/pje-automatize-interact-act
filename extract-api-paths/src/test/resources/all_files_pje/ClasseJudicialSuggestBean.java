package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeJudicialSuggest")
@BypassInterceptors
public class ClasseJudicialSuggestBean extends AbstractSuggestBean<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return "select o from ClasseJudicial o where "
				+ "lower(TO_ASCII(o.classeJudicial)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) order by o.classeJudicial";
	}
}
