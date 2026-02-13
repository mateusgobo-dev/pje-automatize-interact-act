package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsSumaria;

@Name("icrSASManager")
public class IcrSentencaAbsSumariaManager extends InformacaoCriminalRelevanteManager<IcrSentencaAbsSumaria> {
	@Override
	public Date getDtPublicacao(IcrSentencaAbsSumaria entity) {
		// TODO Auto-generated method stub
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
