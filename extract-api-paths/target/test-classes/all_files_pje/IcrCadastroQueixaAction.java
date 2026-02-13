package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrCadastroQueixaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrCadastroQueixaAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrCadastroQueixaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4853983136928815242L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
