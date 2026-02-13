package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorAnulacaoDeSentenca;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrAnulacaoSentencaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrDecisaoSuperiorAnulacaoSentencaAction extends
		IcrAssociarIcrAction<IcrDecisaoSuperiorAnulacaoDeSentenca, IcrDecisaoSuperiorAnulacaoDeSentencaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1390863187383765293L;

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setIcrAfetada(icrAfetada);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataPublicacao(dtPublicacao);
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados Réus com Sentenças passíveis de anulação";
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getIcrAfetada();
	}
}
