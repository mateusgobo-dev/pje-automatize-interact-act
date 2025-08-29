package br.jus.pje.api.converters;

import java.util.ArrayList;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadePoloProcessual;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Parte;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.PoloProcessual;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class PoloProcessualConverter {

	public List<PoloProcessual> convertFrom(ProcessoTrf processoTrf, ModalidadePoloProcessual modalidadePolo) {
		List<PoloProcessual> polo = new ArrayList<>();
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>();

		switch (modalidadePolo) {
			case AT:
				partes = processoTrf.getListaParteAtivo();
				break;
			case PA:
				partes = processoTrf.getListaPartePassivo();
				break;
			case TC:
				partes = processoTrf.getListaParteTerceiro();
				break;
			default:
				break;
		}
		
		polo.add(this.getPoloProcessual(modalidadePolo, partes));
		
		return polo;
	}
	
	public List<PoloProcessual> convertFrom(ProcessoTrf processoTrf) {
		
		List<ProcessoParte> ativo = processoTrf.getListaParteAtivo();
		List<ProcessoParte> passivo = processoTrf.getListaPartePassivo();
		List<ProcessoParte> terceiros = processoTrf.getListaParteTerceiro();
		
		List<PoloProcessual> polos = new ArrayList<>();
		
		polos.add(this.getPoloProcessual(ModalidadePoloProcessual.AT, ativo));
		polos.add(this.getPoloProcessual(ModalidadePoloProcessual.PA, passivo));
		polos.add(this.getPoloProcessual(ModalidadePoloProcessual.TC, terceiros));
		
		return polos;
	}
	
	private PoloProcessual getPoloProcessual(ModalidadePoloProcessual modalidade, List<ProcessoParte> partes) {
		PoloProcessual polo = new PoloProcessual();
		
		polo.setPolo(modalidade);
		polo.setParte(this.getPartes(partes));
		
		return polo;
	}
	
	private List<Parte> getPartes(List<ProcessoParte> partesLegacy){
		List<Parte> partes = new ArrayList<>();
		ParteConverter parteConverter = new ParteConverter();
		
		for (ProcessoParte processoParte : partesLegacy) {
			Parte parte = new Parte();
			parte = parteConverter.convertParteFrom(processoParte);
			partes.add(parte);
		}
		
		return partes;
	}
}
