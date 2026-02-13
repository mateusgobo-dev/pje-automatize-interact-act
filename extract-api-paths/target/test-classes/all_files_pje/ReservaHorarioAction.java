package br.com.infox.pje.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.quartz.CronExpression;

import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.business.dao.ReservaHorarioDAO;
import br.jus.pje.nucleo.entidades.ReservaHorario;

@Name(ReservaHorarioAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ReservaHorarioAction extends GenericCrudAction<ReservaHorario> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "reservaHorarioAction";	

	public void persistOrUpdate() {
		if (!validaExpressaoCronInicio()){
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("reservaHorario.cronInicioInvalida"));
			return;			
		}
		if (!validaExpressaoCronTermino()){
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("reservaHorario.cronTerminoInvalida"));
			return;			
		}		
		if (getInstance().getIdReservaHorario() == 0){
			if(!validaReservaHorarioPersist()){
				FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("reservaHorario.entidadeJaExiste"));
				return;
			}
			super.persist(getInstance());
		} else {
			if(!validaReservaHorarioUpdate()){
				FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("reservaHorario.entidadeJaExiste"));
				return;
			}
			super.update(getInstance());
		}
	}

	public void inativar(ReservaHorario reservaHorario) {
		setInstance(reservaHorario);
		getInstance().setAtivo(false);
		super.update(getInstance());
	}
	
	private boolean validaReservaHorarioPersist(){
		ReservaHorarioDAO dao = ComponentUtil.getComponent(ReservaHorarioDAO.class);
		return dao.findByExpressoes(getInstance().getDsExpressaoCronInicio(), getInstance().getDsExpressaoCronTermino()) == null;
	}
	
	private boolean validaReservaHorarioUpdate(){
		ReservaHorarioDAO dao = ComponentUtil.getComponent(ReservaHorarioDAO.class);
		ReservaHorario expressao = dao.findByExpressoes(getInstance().getDsExpressaoCronInicio(), getInstance().getDsExpressaoCronTermino());
		return (expressao == null || (expressao != null && expressao.getIdReservaHorario() == getInstance().getIdReservaHorario()));
	}
	
	private boolean validaExpressaoCronInicio(){
		return CronExpression.isValidExpression(getInstance().getDsExpressaoCronInicio());
	}
	
	private boolean validaExpressaoCronTermino(){
		return CronExpression.isValidExpression(getInstance().getDsExpressaoCronTermino());
	}	
	
	public String getMensagemAjuda(){
		return FacesUtil.getMessage("reservaHorario.ajuda");
	}
}
