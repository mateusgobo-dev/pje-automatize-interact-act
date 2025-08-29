package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrRetomarTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrSuspensaoTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrRetomarTransacaoPenalAction")
@Scope(ScopeType.CONVERSATION)
public class IcrRetomarTransacaoPenalAction extends
		IcrAssociarIcrAction<IcrRetomarTransacaoPenal, IcrRetomarTransacaoPenalManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8260903725392251282L;

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getSuspensaoTransacaoPenal();
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados Réus com Transação Penal Suspensa";
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setSuspensaoTransacaoPenal((IcrSuspensaoTransacaoPenal) icrAfetada);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataDecisao(dtPublicacao);
	}
}
