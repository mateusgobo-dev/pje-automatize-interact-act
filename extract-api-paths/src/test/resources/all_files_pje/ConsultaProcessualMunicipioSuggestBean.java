package br.jus.je.pje.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.component.suggest.MunicipioSuggestBean;
import br.jus.cnj.pje.view.ConsultaProcessualAction;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(ConsultaProcessualMunicipioSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
@Install(precedence = Install.FRAMEWORK)
public class ConsultaProcessualMunicipioSuggestBean extends MunicipioSuggestBean {

	public static final String NAME = "consultaProcessualMunicipioSuggest";

	@Override
	public Estado getEstado() {
		return ((ConsultaProcessualAction) Component.getInstance("consultaProcessualAction")).getEstado();
	}

	public static ConsultaProcessualMunicipioSuggestBean instance() {
		return (ConsultaProcessualMunicipioSuggestBean) Component.getInstance(ConsultaProcessualMunicipioSuggestBean.NAME);
	}

	@Override
	public void setInstance(Municipio instance) {
		super.setInstance(instance);
		((ConsultaProcessualAction) Component.getInstance("consultaProcessualAction")).setMunicipio(instance);
	}

}