package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeJudicialCompletoSuggest")
@BypassInterceptors
public class ClasseJudicialCompletoSuggestBean extends AbstractSuggestBean<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return "select distinct(c.processoTrf.classeJudicial.classeJudicialCompleto) from ConsultaProcessoTrf c where "
				+ "lower(TO_ASCII(c.processoTrf.classeJudicial.classeJudicialCompleto)) like lower(concat('%',TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) order by c.processoTrf.classeJudicial.classeJudicialCompleto";
	}
}
