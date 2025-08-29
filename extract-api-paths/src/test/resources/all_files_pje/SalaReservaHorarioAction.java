package br.com.infox.pje.action;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.business.dao.SalaReservaHorarioDAO;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaReservaHorario;

@Name(SalaReservaHorarioAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SalaReservaHorarioAction extends GenericCrudAction<SalaReservaHorario> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "salaReservaHorarioAction";

	public void persistOrUpdate() {
		if(!validaSalaReservaHorarioPersistUpdate()){
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("sala.reservaJaExiste"));
			return;
		}
		if (getInstance().getIdSalaReservaHorario() == 0){
			super.persist(getInstance());
		} else {
			super.update(getInstance());
		}
	}
	
	public List<SalaReservaHorario> getSalaReservaHorarioList(Sala sala){
		SalaReservaHorarioDAO dao = ComponentUtil.getComponent(SalaReservaHorarioDAO.class);
		return dao.findBySala(sala, false);
	}

	public void inativar(SalaReservaHorario salaReservaHorario) {
		setInstance(salaReservaHorario);
		getInstance().setAtivo(false);
		super.update(getInstance());
	}
	
	private boolean validaSalaReservaHorarioPersistUpdate(){
		SalaReservaHorarioDAO dao = ComponentUtil.getComponent(SalaReservaHorarioDAO.class);
		SalaReservaHorario salaReservaHorario = dao.find(getInstance().getReservaHorario(), getInstance().getIdentificadorReservaHorario(), getInstance().getSala(), true);
		return salaReservaHorario == null || salaReservaHorario.getIdSalaReservaHorario() == getInstance().getIdSalaReservaHorario();
	}
}
