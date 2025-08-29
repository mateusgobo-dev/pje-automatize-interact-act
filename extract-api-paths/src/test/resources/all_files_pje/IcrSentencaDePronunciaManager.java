package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaDePronuncia;

@Name("icrSPRManager")
public class IcrSentencaDePronunciaManager extends InformacaoCriminalRelevanteManager<IcrSentencaDePronuncia> {


	@Override
	public Date getDtPublicacao(IcrSentencaDePronuncia entity) {
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return true;
	}
}
