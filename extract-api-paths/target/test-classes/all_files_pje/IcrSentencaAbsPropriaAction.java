package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsPropria;

@Name("icrSentencaAbsPropriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaAbsPropriaAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaAbsPropria, IcrSentencaAbsPropriaMananger> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -371021167263990339L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);

	}
}
