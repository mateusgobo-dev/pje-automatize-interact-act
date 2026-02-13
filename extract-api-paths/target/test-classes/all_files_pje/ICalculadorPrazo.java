package br.jus.pje.servicos.prazos;

import java.util.Calendar;

import br.jus.pje.nucleo.enums.ContagemPrazoEnum;

public interface ICalculadorPrazo {
		
	public Calendar calcularEmMinutos(Calendar dataIntimacao, Integer prazo);
	public Calendar calcularEmHoras(Calendar dataIntimacao, Integer prazo);
	public Calendar calcularEmDias(Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo);
	public Calendar calcularEmMeses(Calendar dataIntimacao, Integer prazo);
	public Calendar calcularEmAnos(Calendar dataIntimacao, Integer prazo);
	
}
