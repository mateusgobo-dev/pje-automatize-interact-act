package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

@Name("aplicacaoClasseEventoSuggest")
@BypassInterceptors
public class AplicacaoClasseEventoSuggestBean extends AbstractSuggestBean<AplicacaoClasse> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return "select o from AplicacaoClasse o where "
				+ "o not in (select ace.aplicacaoClasse from AplicacaoClasseEvento ace where ace.evento = #{eventoHome.instance}) "
				+ "and lower(TO_ASCII(o.aplicacaoClasse)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) order by o.aplicacaoClasse";
	}
}
