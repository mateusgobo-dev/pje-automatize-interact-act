package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaImpronuncia;

@Name("icrSEIManager")
public class IcrSentencaImpronunciaManager extends InformacaoCriminalRelevanteManager<IcrSentencaImpronuncia> {


	@Override
	public Date getDtPublicacao(IcrSentencaImpronuncia entity) {
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return true;
	}
}