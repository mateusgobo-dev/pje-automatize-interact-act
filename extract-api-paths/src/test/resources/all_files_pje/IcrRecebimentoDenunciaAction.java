package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrRecebimentoDenunciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrRecebimentoDenunciaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrRecebimentoDenunciaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6918635746629150892L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}