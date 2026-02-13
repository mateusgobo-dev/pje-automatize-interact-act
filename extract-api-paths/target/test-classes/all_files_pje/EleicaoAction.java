package br.jus.je.pje.action;


import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.view.GenericCrudAction;
import br.jus.je.pje.manager.EleicaoManager;
import br.jus.je.pje.manager.TipoEleicaoManager;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.je.entidades.TipoEleicao;

@Name(EleicaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EleicaoAction extends GenericCrudAction<Eleicao> implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "eleicaoAction";
	
	@In
	private EleicaoManager eleicaoManager;
	
	@In
	private TipoEleicaoManager tipoEleicaoManager;
	
	@Override
	public boolean isManaged() {
		return super.isManaged() && getInstance().getCodObjeto() != null;
	}

	public void inactive(Eleicao ae) {
		eleicaoManager.inactive(ae);
		FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso");
	}
	
	public void persist() {
		eleicaoManager.persist(getInstance());
		FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso");
	}
	public void update() {
		eleicaoManager.update(getInstance());
		FacesMessages.instance().add(Severity.INFO, "Registro atualizado com sucesso");
	}
	
	public List<TipoEleicao> tiposEleicao(){
		return tipoEleicaoManager.listTipoEleicao();
	}

}
