package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsolvicaoSumariaJuri;

@Name("icrSAJManager")
public class IcrSentencaAbsolvicaoSumariaJuriManager extends
		InformacaoCriminalRelevanteManager<IcrSentencaAbsolvicaoSumariaJuri> {
	@Override
	public Date getDtPublicacao(IcrSentencaAbsolvicaoSumariaJuri entity) {
		// TODO Auto-generated method stub
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
