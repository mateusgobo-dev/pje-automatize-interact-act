/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.enums;

import java.util.Calendar;
import java.util.Date;

import br.jus.pje.servicos.prazos.ICalculadorPrazo;


public enum TipoPrazoEnum implements PJeEnum {
	
	/**
	 * Prazo em anos.
	 */
	A("anos") {
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo,	Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			return calculadorPrazo.calcularEmAnos(dataIntimacao, prazo);
		}
	}, 
	
	/**
	 * Prazo em meses.
	 */
	M("meses") {
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo,	Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			return calculadorPrazo.calcularEmMeses(dataIntimacao, prazo);
		}
	},
	
	/**
	 * Prazo em dias. É o valor mais comum.
	 */
	D("dias") {
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo,	Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			return calculadorPrazo.calcularEmDias(dataIntimacao, prazo, contagemPrazo);
		}
	},
	
	/**
	 * Prazo em horas.
	 */
	H("horas") {
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo,	Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			return calculadorPrazo.calcularEmHoras(dataIntimacao, prazo);
		}
	},
	
	/**
	 * Prazo em minutos.
	 */
	N("minutos") {
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo,Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			return calculadorPrazo.calcularEmMinutos(dataIntimacao, prazo);
		}
	},
	
	/**
 	 * O prazo de resposta é um momento certo.
 	 */
	C("data certa") {
		
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo, Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			throw new RuntimeException("Calculo para tipo de prazo não implementado!");
		}
	},
	
	/**
	 * Não há prazo para resposta.
	 */
	S("sem prazo") {
		
		@Override
		public Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo,	Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
			return null;
		}
	};
	
	private String label;

	TipoPrazoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
	
	public boolean isPrazoEmAnos(){
		return A.equals(this);
	}
	
	public boolean isPrazoEmMeses(){
		return M.equals(this);
	}
	
	public boolean isPrazoEmDias(){
		return D.equals(this);
	}
	
	public boolean isPrazoEmHoras(){
		return H.equals(this);
	}
	
	public boolean isPrazoEmMinutos(){
		return N.equals(this);
	}
	
	public boolean isSemPrazo(){
		return S.equals(this);
	}
	
	public boolean isPrazoDataCerta(){
		return C.equals(this);
	}
	
	public static TipoPrazoEnum obter(String tipo){
		TipoPrazoEnum retorno = null;
		for (TipoPrazoEnum tipoPrazo : values()) {
			if(tipoPrazo.name().equalsIgnoreCase(tipo)){
				retorno = tipoPrazo;
				break;
			}
		}
		return retorno;
	}
	
	public abstract Calendar calcularPrazo(ICalculadorPrazo calculadorPrazo, Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo);

	public Date calcularPrazo(ICalculadorPrazo calculadorPrazo, Date dataIntimacao, int prazo, ContagemPrazoEnum contagemPrazo) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataIntimacao);
		Calendar dataFinal = calcularPrazo(calculadorPrazo, calendar, prazo, contagemPrazo);
		return dataFinal.getTime();
	}
}