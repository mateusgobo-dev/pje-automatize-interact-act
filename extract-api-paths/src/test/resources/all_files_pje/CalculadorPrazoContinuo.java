package br.jus.cnj.pje.servicos.prazos;

import java.util.Calendar;

import br.jus.pje.nucleo.enums.ContagemPrazoEnum;



public class CalculadorPrazoContinuo extends CalculadorPrazo {
	
	private CalculadorPrazoCache cacheCiencia;
	private CalculadorPrazoCache cacheManifestacao;
	
	public CalculadorPrazoContinuo(Calendario calendario) {
		super(calendario);
		this.cacheCiencia = new CalculadorPrazoCache(calendario);
		this.cacheManifestacao = new CalculadorPrazoCache(calendario);
	}
	
	@Override
	public Calendar calcularEmDias(Calendar dataReferencia, Integer prazo, ContagemPrazoEnum contagemPrazo) {
		if(contagemPrazo.equals(ContagemPrazoEnum.C)){
			return calcularEmDiasPrazoCiencia(dataReferencia, prazo);
		}
		if(contagemPrazo.equals(ContagemPrazoEnum.M)){
			return calcularEmDiasPrazoManifestacao(dataReferencia, prazo);
		}
		return null;
	}
	
	/**
	 * Cálculo de prazo em dias para categoria de prazos contínuos.
	 * A contagem deverá começar no dia seguinte à @dataFinal.
	 * Os dias serão computados independente de serem finais de semana, feriados, de haver indisponibilidade do Sistema ou suspensão de prazo.     
	 */
	private Calendar calcularEmDiasPrazoCiencia(Calendar dataIntimacao, Integer prazo) {
		Calendar dataCache = this.cacheCiencia.recuperarEntrada(dataIntimacao, prazo);
		if (dataCache != null) {
			return dataCache;
		}
		Calendar dataFinal = (Calendar) dataIntimacao.clone();
		dataFinal = contabilizaDiasGraca(dataFinal, prazo);
		getCalendario().ajustaParaFinalDoDia(dataFinal);
		this.cacheCiencia.inserirEntrada(dataIntimacao, prazo, dataFinal);
		return dataFinal;
	}
	
	private Calendar calcularEmDiasPrazoManifestacao(Calendar dataCiencia, Integer prazo){
		Calendar dataCache = this.cacheManifestacao.recuperarEntrada(dataCiencia, prazo);
		if (dataCache != null) {
			return dataCache;
		}
		Calendar dataFinal = (Calendar) dataCiencia.clone();
		dataFinal = contabilizaDiasParaManifestacao(dataFinal, prazo);
		getCalendario().ajustaParaFinalDoDia(dataFinal);
		this.cacheCiencia.inserirEntrada(dataCiencia, prazo, dataFinal);
		return dataFinal;
	}
	
	/**
	 * @param dataIntimacao - Referência à data de intimação do expediente
	 * @param diasGraca - quantidade de dias de graça
	 * @return soma à data de intimação a quantidade de dias de graça. Caso o resultado cai em dia em que não houve 
	 * suspensal de prazo, feriado, feriado judiciário, ou indisponibilidade de sistema, retorna a data, caso contrário,
	 * retorna o próximo dia emq eu não cai nas condições acima.
	 * retorna o próximo dia útil, ou seja, que não caia em fins de semana, feriados, ou dias com suspensão de prazo 
	 * ou em dias de indisponibilidade do Sistema.
	 */
	private Calendar contabilizaDiasGraca(Calendar dataIntimacao, Integer diasGraca){
		dataIntimacao.add(Calendar.DAY_OF_YEAR, diasGraca);
		if(getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataIntimacao)){
			dataIntimacao = getCalendario().obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataIntimacao);
		}
		return dataIntimacao;
	}
	
	/**
	 * @param dataCiencia - Referência à data de ciência da parte no expediente
	 * @param prazoManifestacao - prazo para manifestação, em dias
	 * @return a partir do dia útil seguinte à data de ciência, calcula-se a quantidade de dias para o prazo
	 * de manifestação, considerando apenas dias em que não haja suspensação de prazo.
	 * Caso o resultado do cálculo caia em dia não útil, com suspensão de prazo ou com indisponibilidade do Sistema,
	 * o prazo é jogado para o próximo dia em que não esteja nas condições citadas.
	 */
	private Calendar contabilizaDiasParaManifestacao(Calendar dataCiencia, Integer prazoManifestacao){
		Calendar dataLimite = calculaPrazoParaManifestacaoConsiderandoSuspensoes(getCalendario(), dataCiencia, prazoManifestacao);
		return dataLimite;
	}
}