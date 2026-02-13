package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorExtincaoDaPunibilidade;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.enums.EfeitoSobreSentencaAnteriorEnum;
import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

@Name("icrDecisaoSuperiorExtincaoDaPunibilidadeAction")
@Scope(ScopeType.CONVERSATION)
public class IcrDecisaoSuperiorExtincaoDaPunibilidadeAction extends
		IcrAssociarIcrAction<IcrDecisaoSuperiorExtincaoDaPunibilidade, IcrDecisaoSuperiorExtincaoDaPunibilidadeManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8861474726087872979L;

	@Override
	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada) {
		getInstance().setIcrAfetada(icrAfetada);
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada() {
		return getInstance().getIcrAfetada();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		getInstance().setDataPublicacao(dtPublicacao);
	}

	public EfeitoSobreSentencaAnteriorEnum[] getEfeitos() {
		return EfeitoSobreSentencaAnteriorEnum.values();
	}

	public TipoExtincaoPunibilidadeEnum[] getTipos() {
		return TipoExtincaoPunibilidadeEnum.values();
	}

	@Override
	public String getTextNaoHaReusComSentencaParaAssociar() {
		return "Não foram encontrados Réus com Sentenças passíveis de Extinção da Punibilidade";
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
