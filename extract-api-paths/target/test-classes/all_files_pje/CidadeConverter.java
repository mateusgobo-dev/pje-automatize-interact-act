package br.jus.pje.api.converters;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Cidade;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeUnidadeFederacao;
import br.jus.pje.nucleo.entidades.Municipio;

public class CidadeConverter {
	public Cidade convertFrom(Municipio municipio) {
		Cidade cidade = new Cidade();
		
		if(municipio != null) {
			cidade.setCodigoIBGE(municipio.getCodigoIbge());
			cidade.setMunicipio(municipio.getMunicipio());
			cidade.setUnidadeFederacao(ModalidadeUnidadeFederacao.valueOf(municipio.getEstado().getCodEstado().toUpperCase()));
		}
		
		return cidade;
	}
}
