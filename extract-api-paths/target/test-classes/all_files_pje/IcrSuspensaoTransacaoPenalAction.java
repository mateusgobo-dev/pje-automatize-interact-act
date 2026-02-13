package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSuspensaoTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrSuspensaoTransacaoPenalAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSuspensaoTransacaoPenalAction extends
		IcrAssociarIcrAction<IcrSuspensaoTransacaoPenal, IcrSuspensaoTransacaoPenalManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3528986193227740220L;

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getTransacaoPenal();
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setTransacaoPenal((IcrTransacaoPenal) icrAfetada);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "icrSuspensaoTransacaoPenal.erroNaoHaReus";
	}
}
