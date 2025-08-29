package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaDePronuncia;

@Name("icrSentencaDePronunciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaDePronunciaAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaDePronuncia, IcrSentencaDePronunciaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9144265932287547871L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}
}
