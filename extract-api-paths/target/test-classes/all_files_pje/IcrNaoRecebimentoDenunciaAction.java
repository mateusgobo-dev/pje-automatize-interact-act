package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrNaoRecebimentoDenunciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrNaoRecebimentoDenunciaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrNaoRecebimentoDenunciaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4016661937271657689L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
