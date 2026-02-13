package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;

@Name("icrSentencaCondenatoriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaCondenatoriaAction extends
		IcrAssociarPenaTotalAction<IcrSentencaCondenatoria, IcrSentencaCondenatoriaManager> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1213832904661100084L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}

}