package br.jus.pje.api.converters;

import br.jus.pje.nucleo.dto.ParametroDTO;
import br.jus.pje.nucleo.entidades.Parametro;

public class ParametroConverter {

	public ParametroDTO convertFrom(Parametro parametro) {
		ParametroDTO dto = new ParametroDTO();
		
		dto.setIdParametro(parametro.getIdParametro());
		dto.setDescricaoVariavel(parametro.getDescricaoVariavel());
		dto.setNomeVariavel(parametro.getNomeVariavel());
		dto.setValorVariavel(parametro.getValorVariavel());
		dto.setAtivo(parametro.getAtivo());
		
		return dto;
	}
	
}
