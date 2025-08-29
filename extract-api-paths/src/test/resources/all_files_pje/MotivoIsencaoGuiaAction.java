package br.jus.cnj.pje.view;

import java.io.Serializable;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.view.GenericCrudAction;
import br.jus.cnj.pje.nucleo.manager.MotivoIsencaoGuiaManager;
import br.jus.pje.nucleo.entidades.MotivoIsencaoGuia;

@Name(MotivoIsencaoGuiaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class MotivoIsencaoGuiaAction extends GenericCrudAction<MotivoIsencaoGuia> implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String NAME = "motivoIsencaoGuiaAction";
	@In
	private MotivoIsencaoGuiaManager motivoIsencaoManager;

	@Override
	public boolean isManaged() {
		return super.isManaged() && getInstance().getId() != null;
	}

	public void inactive(MotivoIsencaoGuia ae) {
		motivoIsencaoManager.inactive(ae);
		FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso");
	}

	public void persist() {
		motivoIsencaoManager.persist(getInstance());
		FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso");
	}

	public void update() {
		motivoIsencaoManager.update(getInstance());
		FacesMessages.instance().add(Severity.INFO, "Registro atualizado com sucesso");
	}
}