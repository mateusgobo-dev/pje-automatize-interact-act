package br.jus.csjt.pje.view.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.csjt.pje.business.service.PlantaoJudicialService;

@Name(PlantaoJudicialAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PlantaoJudicialAction implements Serializable {

	private static final long serialVersionUID = 6552124825210157391L;
	public static final String NAME = "plantaoJudicialAction";
	
	/**
	 * Define se está ocorrendo um plantão judicial
	 * @return true, caso esteja ocorrendo plantão no momento
	 */
	public boolean isHorarioDePlantao() {
		return PlantaoJudicialService.instance().verificarPlantao();
	}
}