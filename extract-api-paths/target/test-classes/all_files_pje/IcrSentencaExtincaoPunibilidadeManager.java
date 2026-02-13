package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaExtincaoPunibilidade;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrSEPManager")
public class IcrSentencaExtincaoPunibilidadeManager extends
		InformacaoCriminalRelevanteManager<IcrSentencaExtincaoPunibilidade> {
	@Override
	protected void prePersist(IcrSentencaExtincaoPunibilidade entity) throws IcrValidationException {
		super.prePersist(entity);
		if (DateUtil.isDataMenor(entity.getDtPublicacao(), entity.getData())) {
			throw new IcrValidationException("informacaoCriminalRelevante.dataPublicacaoInvalida");
		}
	}

	@Override
	protected boolean exigeTipificacaoDelito(IcrSentencaExtincaoPunibilidade entity) {
		return entity.getTipoExtincao().exigeTipificacaoDelito();
	}

	@Override
	public Date getDtPublicacao(IcrSentencaExtincaoPunibilidade entity) {
		// TODO Auto-generated method stub
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
