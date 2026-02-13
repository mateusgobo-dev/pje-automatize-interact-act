package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrCadastraRecebimentoQueixaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrCadastraRecebimentoQueixaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrCadastraRecebimentoQueixaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3873618212085671952L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
