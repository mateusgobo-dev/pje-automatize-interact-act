package br.jus.cnj.pje.servicos.prazos;

import java.util.Calendar;

import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.servicos.prazos.ICalculadorPrazo;

public abstract class CalculadorPrazo implements ICalculadorPrazo {

	private Calendario calendario;
	
	public CalculadorPrazo(Calendario calendario) {
		this.calendario = calendario;
	}

	public Calendario getCalendario() {
		return calendario;
	}
			
	@Override
	public Calendar calcularEmHoras(Calendar dataIntimacao, Integer prazo) {
		return calcularEmDiasOuHoras(dataIntimacao, prazo, Calendar.HOUR);
	}
	
	@Override
	public Calendar calcularEmMinutos(Calendar dataIntimacao, Integer prazo) {
		return calcularEmDiasOuHoras(dataIntimacao, prazo, Calendar.MINUTE);
	}
	
	@Override
	public Calendar calcularEmMeses(Calendar dataIntimacao, Integer prazo) {
		return calcularEmMesesOuAnos(dataIntimacao, prazo, Calendar.MONTH);
	}
	
	@Override
	public Calendar calcularEmAnos(Calendar dataIntimacao, Integer prazo) {	
		return calcularEmMesesOuAnos(dataIntimacao, prazo, Calendar.YEAR);
	}
	
	private Calendar calcularEmDiasOuHoras(Calendar dataIntimacao, Integer prazo, int tipoTempo) {
		
		Calendar dataFinal = (Calendar) dataIntimacao.clone();
		
		// Caso a data de intimacao nao tenha registro de hora/minuto, ajusta para
		// o dia util seguinte, no inicio legal de horario para a pratica de atos processuais.
		// Ver CPC, art. 172
		if (!(dataFinal.get(Calendar.HOUR_OF_DAY) > 0 || dataFinal.get(Calendar.MINUTE) > 0)) {
			dataFinal = getCalendario().obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataFinal);
			getCalendario().ajustarParaInicioDoDia(dataFinal);
			dataFinal.set(Calendar.HOUR_OF_DAY, 6);
		}
		
		// em caso de prazos em minutos ou horas, ignora-se a existencia de feriados, exceto no caso anterior
		dataFinal.add(tipoTempo, prazo);

		return dataFinal;
	}
	
	private Calendar calcularEmMesesOuAnos(Calendar dataIntimacao, Integer prazo, int tipoTempo) {
				
		Calendar dataIntimacaoJuridica = (Calendar) dataIntimacao.clone();
		
		// Recupera a data de intimacao juridica, a data de intimacao juridica nao pode ser um dia nao util ou que houve suspensao de prazo
		if (getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazo(dataIntimacaoJuridica)) {
			dataIntimacaoJuridica = getCalendario().obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataIntimacaoJuridica); 
		}
		
		Calendar dataFinal = (Calendar) dataIntimacaoJuridica.clone();
		
		// Considera-se a data inicial a data da intimação juridica, e não o dia útil seguinte a ela
		// seguindo o previsto no Código Civil, art. 132, § 3.º.
		dataFinal.add(tipoTempo, prazo);
		
		Integer prazoEmDias = (int) DateUtil.diferencaDias(dataFinal.getTime(), dataIntimacaoJuridica.getTime());
		
		dataFinal = calculaPrazoParaManifestacaoConsiderandoSuspensoes(getCalendario(), (Calendar) dataIntimacaoJuridica.clone(), prazoEmDias);

		if(dataFinal.getActualMaximum(Calendar.DAY_OF_MONTH) < dataIntimacaoJuridica.get(Calendar.DAY_OF_MONTH)){
			dataFinal.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		// Verifica se o ultimo dia e util e nao houve suspensao de prazo ou indisponibilidade do sistema
		if (getCalendario().isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataFinal)) {
			dataFinal = getCalendario().obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataFinal);
		}

		getCalendario().ajustaParaFinalDoDia(dataFinal);

		return dataFinal;
	}
	
	/**
	 * @param calendario - objeto {@link Calendario} com os eventos a serem considerados
	 * @param dataCiencia - Referência à data de ciência da parte no expediente
	 * @param prazoManifestacao - prazo para manifestação, em dias
	 * @return a partir do dia útil seguinte à data de ciência, calcula-se a quantidade de dias para o prazo
	 * de manifestação, considerando apenas dias em que não haja suspensação de prazo.
	 * Caso o resultado do cálculo caia em dia não útil, com suspensão de prazo ou com indisponibilidade do Sistema,
	 * o prazo é jogado para o próximo dia em que não esteja nas condições citadas.
	 */
	protected Calendar calculaPrazoParaManifestacaoConsiderandoSuspensoes(Calendario calendario, Calendar dataCiencia, Integer prazoManifestacao){
		Calendar dataLimite = calendario.obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataCiencia);
		int contaPrazo = 1;
		while(contaPrazo < prazoManifestacao){
			if(!calendario.isSuspensaoPrazo(dataLimite)){
				contaPrazo++;
			}
			dataLimite = calendario.obtemProximoDia(dataLimite);
		}
		if(calendario.isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataLimite)){
			dataLimite = calendario.obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataLimite);
		}
		return dataLimite;
	}	
}