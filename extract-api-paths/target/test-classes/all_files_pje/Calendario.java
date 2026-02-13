package br.jus.cnj.pje.servicos.prazos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

public class Calendario {

	private OrgaoJulgador orgaoJulgador;
	private List<CalendarioEvento> eventos;

	public Calendario(OrgaoJulgador orgaoJulgador, List<CalendarioEvento> eventos) {
		this.orgaoJulgador = orgaoJulgador;
		this.eventos = eventos;
	}
	
	/**
	 * Recupera os eventos que aconteceram na data informada
	 * @param data A data que sera utilizada para recuperar os eventos
	 * @return A lista de eventos que ocorreram na data 
	 */
	public List<CalendarioEvento> recuperarEventosNaData(Calendar data) {
		List<CalendarioEvento> resultado = new ArrayList<CalendarioEvento>();
		if (this.eventos != null && this.eventos.size() > 0) {		
			for (CalendarioEvento evento : this.eventos) {
				if (evento.estaNesteEvento(data)) {
					resultado.add(evento);
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Verifica se a data caiu em um dia que houve suspensao de prazo
	 * @param data A data que sera testada
	 * @return Verdadeiro se caiu em um dia que houve suspensao de prazo e falso caso nao
	 */
	public boolean isSuspensaoPrazo(Calendar data) {
		boolean resultado = false;
		if (this.eventos != null && this.eventos.size() > 0) {		
			for (CalendarioEvento evento : this.eventos) {
				if (evento.estaNesteEvento(data) && evento.getInSuspendePrazo()) {
					resultado = true;
					break;
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Verifica se a data caiu em um dia que houve feriado
	 * @param data A data que sera testada
	 * @return Verdadeiro se caiu em um dia que houve feriado e falso caso nao
	 */
	public boolean isFeriado(Calendar data) {
		boolean resultado = false;
		if (this.eventos != null && this.eventos.size() > 0) {		
			for (CalendarioEvento evento : this.eventos) {
				if (evento.estaNesteEvento(data) && (evento.getInFeriado() || evento.getInJudiciario())) {
					resultado = true;
					break;
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Verifica se a data caiu em um dia que houve indisponibilidade do sistema
	 * @param data A data que sera testada
	 * @return Verdadeiro se caiu em um dia que houve indisponibilidade do sistema e falso caso nao
	 */
	public boolean isIndisponibilidadeSistema(Calendar data) {
		boolean resultado = false;
		if (this.eventos != null && this.eventos.size() > 0) {		
			for (CalendarioEvento evento : this.eventos) {
				if (evento.estaNesteEvento(data) && evento.getIndisponibilidadeSistema()) {
					resultado = true;
					break;
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Verifica se a data caiu em um final de semana
	 * @param data A data que sera testada
	 * @return Verdadeiro se caiu em um final de semana e falso caso nao
	 */
	public boolean isFinalSemana(Calendar data) {
		int diaSemana = data.get(Calendar.DAY_OF_WEEK);
		return Calendar.SATURDAY == diaSemana || Calendar.SUNDAY == diaSemana;
	}
	
	/**
	 * Verifica se a data e um dia nao util ou dia que houve suspensao de prazo 
	 * @param data A data que sera testada
	 * @return Retorna verdadeiro se o dia nao for util ou que uma suspensao de prazo
	 */
	public boolean isDiaNaoUtilOuHouveSuspensaoPrazo(Calendar data) {
		boolean resultado = false;
		if (isFinalSemana(data)) {
			resultado = true;
		}
		else {
			List<CalendarioEvento> eventos = recuperarEventosNaData(data);

			for (CalendarioEvento evento : eventos) {
				if (evento.getInFeriado() || 
					evento.getInJudiciario() || 
					evento.getInSuspendePrazo() ||
					evento.getIndisponibilidadeSistema()) {
					resultado = true;
					break;
				}
			}
			return resultado;
		}
		return resultado;
	}
	
	/**
	 * Verifica se a data e um dia nao util ou dia que houve suspensao de prazo ou indisponibilidade do sistema 
	 * @param data A data que sera testada
	 * @return Retorna verdadeiro se o dia nao for util ou que uma suspensao de prazo ou indisponibilidade do sistema
	 */
	public boolean isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(Calendar data) {
		boolean resultado = false;
		if (isFinalSemana(data)) {
			resultado = true;
		}
		else {
			List<CalendarioEvento> eventos = recuperarEventosNaData(data);
			for (CalendarioEvento evento : eventos) {
				if (evento.getInFeriado() || evento.getInJudiciario() || evento.getInSuspendePrazo() || evento.getIndisponibilidadeSistema()) {
					resultado = true;
					break;
				}
			}
			return resultado;
		}
		return resultado;
	}
	
		
	public void ajustaParaFinalDoDia(Calendar data) {
		data.set(Calendar.HOUR_OF_DAY, 23);
		data.set(Calendar.MINUTE, 59);
		data.set(Calendar.SECOND, 59);
	}	

	public void ajustarParaInicioDoDia(Calendar data) {
		data.set(Calendar.HOUR_OF_DAY, 0);
		data.set(Calendar.MINUTE, 0);
		data.set(Calendar.SECOND, 0);
		data.set(Calendar.MILLISECOND, 0);		
	}
		
	public static Calendar converter(Date data) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(data);
		return calendar;
	}

	/**
	 * Recupera o proximo dia sem cair em dia nao util ou que houve suspensao de prazo ou indisponibilidade do sistema
	 * 
	 * @param dataReferencia A data de referencia
	 * @return A data do proximo dia que atende os requisitos
	 */
	public Calendar obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazoOuIndisponibilidadeSistema(Calendar dataReferencia) {
		Calendar dataFinal = (Calendar) dataReferencia.clone();			
		do {
			dataFinal.add(Calendar.DAY_OF_MONTH, 1);
		}
		while (isDiaNaoUtilOuHouveSuspensaoPrazoOuIndisponibilidadeSistema(dataFinal));		
		return dataFinal;
	}
	
	/**
	 * Recupera o proximo dia sem cair em dia nao util ou que houve suspensao de prazo
	 * 
	 * @param dataReferencia A data de referencia
	 * @return A data do proximo dia que atende os requisitos
	 */
	public Calendar obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(Calendar dataReferencia) {
		Calendar dataFinal = (Calendar) dataReferencia.clone();			
		do {
			dataFinal.add(Calendar.DAY_OF_MONTH, 1);
		}
		while (isDiaNaoUtilOuHouveSuspensaoPrazo(dataFinal));
		return dataFinal;
	}
	
	/**
	 * Recupera o proximo dia sem cair em dia nao util ou que houve suspensao de prazo
	 * 
	 * @param dataReferencia A data de referencia
	 * @return A data do proximo dia que atende os requisitos
	 */
	public Calendar obtemProximoDia(Calendar dataReferencia) {
		Calendar proximoDia = (Calendar) dataReferencia.clone();
		proximoDia.add(Calendar.DAY_OF_MONTH, 1);
		return proximoDia;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public List<CalendarioEvento> getEventos() {
		return eventos;
	}
}