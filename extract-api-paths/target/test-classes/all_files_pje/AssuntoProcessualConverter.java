package br.jus.pje.api.converters;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.AssuntoLocal;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.AssuntoProcessual;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

public class AssuntoProcessualConverter {
	
	public AssuntoProcessual convertFrom(AssuntoTrf assuntoTrf) {
		AssuntoProcessual assunto = new AssuntoProcessual();
		
		assunto.setCodigoNacional(Integer.parseInt(assuntoTrf.getCodAssuntoTrf()));
		assunto.setAssuntoLocal(buildAssuntoLocal(assuntoTrf));
		assunto.setPrincipal(!assuntoTrf.getComplementar());
		
		return assunto;
	}
	
	public AssuntoLocal buildAssuntoLocal(AssuntoTrf assuntoTrf) {
		AssuntoLocal assuntoLocal = new AssuntoLocal();
		
		assuntoLocal.setDescricao(assuntoTrf.getAssuntoTrf());
		assuntoLocal.setCodigoAssunto(Integer.parseInt(assuntoTrf.getCodAssuntoTrf()));
		if(assuntoTrf.getAssuntoTrfSuperior() != null) {
			assuntoLocal.setCodigoPaiNacional(Integer.parseInt(assuntoTrf.getAssuntoTrfSuperior().getCodAssuntoTrf()));			
		}
		
		return assuntoLocal;
	}
}
