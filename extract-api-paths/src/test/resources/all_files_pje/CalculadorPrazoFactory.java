package br.jus.cnj.pje.servicos.prazos;

import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.servicos.prazos.ICalculadorPrazo;

public class CalculadorPrazoFactory {

	public static ICalculadorPrazo novoCalculadorPrazo(CategoriaPrazoEnum categoriaPrazo, Calendario calendario) {

		switch(categoriaPrazo) {
			case C :
				return new CalculadorPrazoContinuo(calendario);
			case U :
				return new CalculadorPrazoDiasUteis(calendario);
		}
			
		throw new RuntimeException("Categoria de prazo inválida!");
	}	
}
