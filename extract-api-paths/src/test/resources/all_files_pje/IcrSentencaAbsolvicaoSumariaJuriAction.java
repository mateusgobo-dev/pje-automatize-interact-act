package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsolvicaoSumariaJuri;
import br.jus.pje.nucleo.enums.TipoCausaAbsolvicaoSumariaJuriEnum;

@Name("icrSentencaAbsolvicaoSumariaJuriAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaAbsolvicaoSumariaJuriAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaAbsolvicaoSumariaJuri, IcrSentencaAbsolvicaoSumariaJuriManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3617843286903410019L;

	public List<TipoCausaAbsolvicaoSumariaJuriEnum> getTipoCausaAbsolvicaoSumariaJuriEnum() {
		return Arrays.asList(TipoCausaAbsolvicaoSumariaJuriEnum.values());
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}
}
