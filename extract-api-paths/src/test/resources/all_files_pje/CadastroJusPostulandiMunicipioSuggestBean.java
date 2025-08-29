package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.view.action.CadastroJusPostulandiHome;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(CadastroJusPostulandiMunicipioSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class CadastroJusPostulandiMunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	public static final String NAME = "cadastroJusPostulandiMunicipioSuggest";
	private static final long serialVersionUID = 1L;
	
	private static final int LIMIT_SUGGEST_DEFAULT = 50;
	
	@Override
	public String getEjbql() {
			return getHome().getEstado() == null ? null : "select o from Municipio o " + "where o.estado.codEstado = '"
				+ getHome().getEstado().getCodEstado() + "' and "
				+ "lower(TO_ASCII(o.municipio)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) order by o.municipio";
	}

	protected CadastroJusPostulandiHome getHome() {
		return CadastroJusPostulandiHome.instance();
	}

	public static CadastroJusPostulandiMunicipioSuggestBean instance() {
		return ComponentUtil.getComponent(NAME);
	}

	@Override
	public Integer getLimitSuggest(){
		return LIMIT_SUGGEST_DEFAULT;
	}
	
}
