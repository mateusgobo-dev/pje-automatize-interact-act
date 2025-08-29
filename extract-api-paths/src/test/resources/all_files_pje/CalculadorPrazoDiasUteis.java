package br.jus.cnj.pje.servicos.prazos;

import java.util.Calendar;

import br.jus.pje.nucleo.enums.ContagemPrazoEnum;



public class CalculadorPrazoDiasUteis extends CalculadorPrazo {

	private CalculadorPrazoCache cacheCiencia;
	private CalculadorPrazoCache cacheManifestacao;
	
	public CalculadorPrazoDiasUteis(Calendario calendario) {
		super(calendario);
		this.cacheCiencia = new CalculadorPrazoCache(calendario);
		this.cacheManifestacao = new CalculadorPrazoCache(calendario);
	}
		
	@Override
	public Calendar calcularEmDias(Calendar dataIntimacao, Integer prazo, ContagemPrazoEnum contagemPrazo) {
		if(contagemPrazo.equals(ContagemPrazoEnum.C)){
			return calcularEmDiasPrazoCiencia(dataIntimacao, prazo);
		}
		if(contagemPrazo.equals(ContagemPrazoEnum.M)){
			return calcularEmDiasPrazoManifestacao(dataIntimacao, prazo);
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
	 * @return soma à data de intimação a quantidade de dias de graça, a partir do dia seguinte e, desconsiderando
	 * dias de feriado judiciário, feriado e suspensação de prazo. Após obter o resultado, caso a data caia em dia útil,
	 * ela é retornada. Caso contrário, retorna o próximo dia útil.
	 */
	private Calendar contabilizaDiasGraca(Calendar dataIntimacao, Integer diasGraca){
		int contaPrazo = 0;
		while(contaPrazo < diasGraca){
			dataIntimacao = getCalendario().obtemProximoDia(dataIntimacao);
			if(!getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazo(dataIntimacao)){
				contaPrazo++;
			}
		}
		if(getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataIntimacao)){
			dataIntimacao = getCalendario().obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataIntimacao);
		}
		return dataIntimacao;
	}
	
	/**
	 * @param dataCiencia - Referência à data de ciência da parte no expediente
	 * @param prazoManifestacao - prazo para manifestação, em dias
	 * @return a partir do dia seguinte à data de ciência, calcula-se a quantidade de dias para o prazo
	 * de manifestação, considerando apenas dias em que não haja suspensação de prazo.
	 * Caso o resultado do cálcula caia em dia não útil, com suspensão de prazo ou com indisponibilidade do Sistema,
	 * o prazo é jogado para o próximo dia em que não esteja nas condições citadas.
	 */
	private Calendar contabilizaDiasParaManifestacao(Calendar dataCiencia, Integer prazoManifestacao){
		int contaPrazo = 0;
		while(contaPrazo < prazoManifestacao){
			dataCiencia = getCalendario().obtemProximoDia(dataCiencia);
			if(!getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazo(dataCiencia)){
				contaPrazo++;
			}
		}
		if(getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataCiencia)){
			dataCiencia = getCalendario().obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataCiencia);
		}
		return dataCiencia;
	}
}