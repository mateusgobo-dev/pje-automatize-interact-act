package br.com.infox.cliente.home;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

public class AbstractIcrAditamentoHome<T> extends AbstractHome<InformacaoCriminalRelevante> {

	private static final long serialVersionUID = 1L;

	public void setInformacaoCriminalRelevanteId(Integer id) {
		setId(id);
	}

	public Integer getInformacaoCriminalRelevanteId() {
		return (Integer) getId();
	}

	@Override
	public void setTab(String tab) {
		super.setTab(tab);
	}

	@Override
	public void setGoBackTab(String goBackTab) {
		super.setGoBackTab(goBackTab);
	}

	public void imprimirMensagem(String mensagem) {
		FacesMessages.instance().add(StatusMessage.Severity.ERROR, mensagem);
	}

}
