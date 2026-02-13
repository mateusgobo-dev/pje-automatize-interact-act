package br.jus.je.pje.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.component.suggest.MunicipioSuggestBean;
import br.jus.je.pje.home.ComplementoProcessoJEHome;
import br.jus.pje.nucleo.entidades.Estado;

@Name(MunicipioEleicaoSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
@Install(precedence = Install.FRAMEWORK)
public class MunicipioEleicaoSuggestBean extends MunicipioSuggestBean {

	private static final long	serialVersionUID	= 1L;

	public static final String	NAME				= "municipioEleicaoSuggest";

	@Override
	public Estado getEstado() {
		return ComplementoProcessoJEHome.instance().getEstado();
	}

	public static MunicipioEleicaoSuggestBean instance() {
		return (MunicipioEleicaoSuggestBean) Component.getInstance(MunicipioEleicaoSuggestBean.NAME);
	}

}
