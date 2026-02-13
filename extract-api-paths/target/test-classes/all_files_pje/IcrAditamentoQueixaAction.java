package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrAditamentoQueixaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrAditamentoQueixaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrAditamentoQueixaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6924442770906175267L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
