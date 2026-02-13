package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrSentencaExtincaoPunibilidade;
import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

@Name("icrSentencaExtincaoPunibilidadeAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSentencaExtincaoPunibilidadeAction extends
		InformacaoCriminalRelevanteAction<IcrSentencaExtincaoPunibilidade, IcrSentencaExtincaoPunibilidadeManager> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2355225918687659972L;

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDtPublicacao(dtPublicacao);
	}

	/**
	 * Método utilizado pelo view para carregar a lista de Tipo de Extinção de
	 * Punibilidade
	 * 
	 * @return List<PrazoMinAnosMedidaSegurancaEnum>
	 */
	public List<TipoExtincaoPunibilidadeEnum> getlistTipoExtincao() {
		return Arrays.asList(TipoExtincaoPunibilidadeEnum.values());
	}

	@Override
	public boolean exigeTipificacaoDelito() {
		if(getHome().getTab().equals(InformacaoCriminalRelevanteHome.TAB_TIPIFICACAO_DELITO_ID)){
			return true;
		}
		else if (getInstance().getTipoExtincao() != null) {
			boolean returnValue = getInstance().getTipoExtincao().exigeTipificacaoDelito();
			if (returnValue == true) {
				inicializaTipificacaoDelito();
			}
			return returnValue;
		}
		return false;
	}

}
