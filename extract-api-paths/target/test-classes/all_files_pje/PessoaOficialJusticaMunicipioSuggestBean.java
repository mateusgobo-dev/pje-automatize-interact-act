package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.PessoaOficialJusticaHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Municipio;

@Name("pessoaOficialJusticaMunicipioSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaOficialJusticaMunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	private static final long serialVersionUID = 1L;
	
	private static final int LIMIT_SUGGEST_DEFAULT = 50;

	@Override
	public String getEjbql() {
		return getHome().getEstado() == null ? null : "select o from Municipio o " + "where o.estado.estado = '"
				+ getHome().getEstado() + "' and " + "lower(TO_ASCII(o.municipio)) like lower(concat('%',TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) order by o.municipio";
	}

	protected PessoaOficialJusticaHome getHome() {
		return (PessoaOficialJusticaHome) Component.getInstance("pessoaOficialJusticaHome");
	}

	@Override
	public Integer getLimitSuggest(){
		return LIMIT_SUGGEST_DEFAULT;
	}
	
}
