package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrIndiciamentoAction")
@Scope(ScopeType.CONVERSATION)
public class IcrIndiciamentoAction extends
		InformacaoCriminalRelevanteAction<InformacaoCriminalRelevante, IcrIndiciamentoManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2036593878484471894L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
