package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaImpronuncia;

@Name("icrSentencaImpronunciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaImpronunciaAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaImpronuncia, IcrSentencaImpronunciaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3979688236324250316L;

	@Override
	public void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}
}
