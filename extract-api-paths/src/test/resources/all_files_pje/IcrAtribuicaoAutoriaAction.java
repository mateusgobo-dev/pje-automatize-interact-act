package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrAtribuicaoAutoria;

@Name("icrAtribuicaoAutoriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrAtribuicaoAutoriaAction extends
		InformacaoCriminalRelevanteAction<IcrAtribuicaoAutoria, IcrAtribuicaoAutoriaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7464998355847178157L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataPublicacao(dtPublicacao);
	}
}
