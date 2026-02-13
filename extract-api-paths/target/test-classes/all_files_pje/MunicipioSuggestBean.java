package br.com.infox.ibpm.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.CepHome;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(MunicipioSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
@Install(precedence = Install.FRAMEWORK)
public class MunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	public static final String NAME = "municipioSuggest";
	private static final long serialVersionUID = 1L;

	private static final int LIMIT_SUGGEST_DEFAULT = 50;
	
	@Override
	public String getEjbql() {
		Estado estado = getEstado();
		if (estado == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Municipio o ");
		sb.append("where o.estado.idEstado = ");
		sb.append(estado.getIdEstado());
		sb.append(" and ");
		sb.append("lower(TO_ASCII(o.municipio)) like lower(concat(TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%'))) ");
		sb.append("order by o.municipio");
		return sb.toString();
	}

	public Estado getEstado() {
		return CepHome.instance().getEstado();
	}

	@Override
	public Integer getLimitSuggest(){
		return LIMIT_SUGGEST_DEFAULT;
	}
	
}