package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorEnum;

@Name("icrDecisaoSuperiorSentencaCondenatoriaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrDecisaoSuperiorSentencaCondenatoriaAction extends
		IcrAssociarIcrAction<IcrDecisaoSuperiorSentencaCondenatoria, IcrDecisaoSuperiorSentencaCondenatoriaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6864626371219546791L;

	public List<EfeitoSobreSentencaAnteriorEnum> getEfeitoSobreSentencaAnteriorList() {
		return Arrays.asList(EfeitoSobreSentencaAnteriorEnum.values());
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataPublicacao(dtPublicacao);
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getIcrAfetada();
	}

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setIcrAfetada(icrAfetada);
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados Réus com Sentença Condenatória ativa";
	}
}
