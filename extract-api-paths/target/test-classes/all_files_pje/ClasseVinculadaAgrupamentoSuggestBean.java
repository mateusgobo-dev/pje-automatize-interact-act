package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeVinculadaAgrupamentoSuggest")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class ClasseVinculadaAgrupamentoSuggestBean extends AbstractSuggestBean<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return "select cj from ClasseJudicial cj where "
				+ "cj not in (select cja.classe from ClasseJudicialAgrupamento cja "
				+ "where cj = cja.classe and cja.agrupamento = #{agrupamentoClasseJudicialHome.instance}) "
				+ "and lower(TO_ASCII(cj.classeJudicial)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) order by cj.classeJudicial";
	}

}