package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsSumaria;
import br.jus.pje.nucleo.enums.TipoCausaAbsolvicaoSumariaEnum;
import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

@Name("icrSentencaAbsSumariaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaAbsSumariaAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaAbsSumaria, IcrSentencaAbsSumariaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6026663573986107284L;

	public List<TipoCausaAbsolvicaoSumariaEnum> getTipoCausaAbsolvicaoSumariaEnum() {
		return Arrays.asList(TipoCausaAbsolvicaoSumariaEnum.values());
	}

	public TipoExtincaoPunibilidadeEnum[] getTipos() {
		return TipoExtincaoPunibilidadeEnum.values();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}
}
