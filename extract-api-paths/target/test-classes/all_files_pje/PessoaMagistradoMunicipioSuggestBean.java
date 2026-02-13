package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.PessoaMagistradoHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Municipio;

@Name("pessoaMagistradoMunicipioSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaMagistradoMunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	private static final long serialVersionUID = 1L;

	private static final int LIMIT_SUGGEST_DEFAULT = 50;
	
	@Override
	public String getEjbql() {
		getHome().getEstado();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Municipio o ");
		sb.append("where o.estado.estado = '");
		sb.append(getHome().getEstado());
		sb.append("' and ");
		sb.append("lower(TO_ASCII(o.municipio)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.municipio");
		return getHome().getEstado() == null ? null : sb.toString();
	}

	protected PessoaMagistradoHome getHome() {
		return (PessoaMagistradoHome) Component.getInstance("pessoaMagistradoHome");
	}
	
	@Override
	public Integer getLimitSuggest(){
		return LIMIT_SUGGEST_DEFAULT;
	}
	
}
