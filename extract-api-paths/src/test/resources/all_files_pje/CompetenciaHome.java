package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoAlcada;

@Name("competenciaHome")
@BypassInterceptors
public class CompetenciaHome extends AbstractCompetenciaHome<Competencia> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6269388294055169463L;

	public static CompetenciaHome instance() {
		return ComponentUtil.getComponent("competenciaHome");
	}

	@Override
	public String remove(Competencia obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		String update = super.update();
		return update;
	}
	
	public String removeAlcada(){
		/*
		 * [PJEII-4950] Rodrigo S. Menezes: Correção da lógica e mensagem
		 * de retorno para o usuário na remoção de dimensão de alçada.
		 */
		DimensaoAlcada dimensaoAlcada = getInstance().getDimensaoAlcada();
		getInstance().setDimensaoAlcada(null);
		persist();
		DimensaoAlcadaHome.instance().remove(dimensaoAlcada);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "competenciaDimensaoAlcada_deleted"));
		DimensaoAlcadaHome.instance().newInstance();
		return "updated";
	}

}
