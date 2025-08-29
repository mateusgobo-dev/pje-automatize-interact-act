package br.jus.csjt.pje.business.service;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.CalendarioEventoDAO;
import br.jus.pje.nucleo.entidades.CalendarioEvento;

@Name(CalendarioEventoService.NAME)
public class CalendarioEventoService {
	public static final String NAME = "calendarioEventoService";

	@In(create=true)
	private CalendarioEventoDAO calendarioEventoDAO;
	
	/**
	 * Carrega a lista de feriados
	 * @return lista de feriados (List<CalendarioEvento>)
	 */
	public List<CalendarioEvento> obterListaDeFeriados() {
		return calendarioEventoDAO.obterListaDeFeriadosRelevantes();
	}
	
	/**
	 * @return True se for para recalcular os prazos.
	 */
	public boolean isRecalcularPrazos() {
		return calendarioEventoDAO.isRecalcularPrazos();
	}
	
	/**
	 * Configura os eventos para que identifiquem que os prazos já foram recalculados.
	 */
	public void prazosRecalculados() {
		calendarioEventoDAO.prazosRecalculados();
	}
}
