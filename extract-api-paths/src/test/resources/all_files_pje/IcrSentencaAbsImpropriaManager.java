package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaAbsImpropria;

@Name("icrSAIManager")
public class IcrSentencaAbsImpropriaManager extends InformacaoCriminalRelevanteManager<IcrSentencaAbsImpropria> {
	@Override
	protected void prePersist(IcrSentencaAbsImpropria entity) throws IcrValidationException {
		super.prePersist(entity);
		if (entity.getNrMesPrazo() == null) {
			entity.setNrMesPrazo(0);
		}
	}

	@Override
	public Date getDtPublicacao(IcrSentencaAbsImpropria entity) {
		// TODO Auto-generated method stub
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
