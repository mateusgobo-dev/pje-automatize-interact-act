package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsPropria;

@Name("icrSAPManager")
public class IcrSentencaAbsPropriaMananger extends InformacaoCriminalRelevanteManager<IcrSentencaAbsPropria> {
	@Override
	public Date getDtPublicacao(IcrSentencaAbsPropria entity) {
		// TODO Auto-generated method stub
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
