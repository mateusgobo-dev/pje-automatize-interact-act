package br.com.infox.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.jus.cnj.pje.business.dao.SalaReservaHorarioDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ReservaHorario;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaReservaHorario;

@Name(SalaReservaHorarioManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SalaReservaHorarioManager extends GenericManager {

	/**
	 * 
	 */
	public static final String NAME = "salaReservaHorarioManager";

	private static final long serialVersionUID = 1L;

	@In
	private SalaReservaHorarioDAO salaReservaHorarioDAO;

	public SalaReservaHorario find(ReservaHorario reservaHorario, Sala sala, boolean somenteAtivos) {

		return salaReservaHorarioDAO.find(reservaHorario, sala, somenteAtivos);

	}

	public SalaReservaHorario find(ReservaHorario reservaHorario, String identificador, Sala sala, boolean somenteAtivos) {

		return salaReservaHorarioDAO.find(reservaHorario, identificador, sala, somenteAtivos);
	}

	public List<SalaReservaHorario> find(String identificador, OrgaoJulgador orgaoJulgador, boolean somenteAtivos) {

		return salaReservaHorarioDAO.find(identificador, orgaoJulgador, somenteAtivos);
	}

	public List<SalaReservaHorario> findByIdentificador(String identificador, boolean somenteAtivos) {

		return salaReservaHorarioDAO.findByIdentificador(identificador, somenteAtivos);
	}

	public List<SalaReservaHorario> findBySala(Sala sala, boolean somenteAtivos) {

		return salaReservaHorarioDAO.findBySala(sala, somenteAtivos);

	}
}
