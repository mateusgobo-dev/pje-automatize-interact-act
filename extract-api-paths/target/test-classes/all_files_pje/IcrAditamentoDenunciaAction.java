package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrAditamentoDenunciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrAditamentoDenunciaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrAditamentoDenunciaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2745545256041187600L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
