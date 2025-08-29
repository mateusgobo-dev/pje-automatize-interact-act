package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrOferecimentoDenunciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrOferecimentoDenunciaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrOferecimentoDenunciaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1487640928726283960L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
