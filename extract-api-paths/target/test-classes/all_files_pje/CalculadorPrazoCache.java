package br.jus.cnj.pje.servicos.prazos;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CalculadorPrazoCache {

	private Calendario calendario;
	private Map<Long,HashMap<Integer, Calendar>> cache = new HashMap<Long,HashMap<Integer, Calendar>>();

	public CalculadorPrazoCache(Calendario calendario) {
		this.calendario = calendario;
	}
	
	public Calendario getCalendario() {
		return calendario;
	}

	public Calendar recuperarEntrada(Calendar dataIntimacao, Integer prazo) {
		
		long time = getTime(dataIntimacao);
				
		if (this.cache.containsKey(time) && this.cache.get(time).containsKey(prazo)) {
			return this.cache.get(time).get(prazo);
		}
		else {
			return null;
		}
	}

	private long getTime(Calendar dataIntimacao) {
		
		Calendar data = (Calendar) dataIntimacao.clone();
		
		getCalendario().ajustarParaInicioDoDia(data);
		
		return data.getTime().getTime();
	}

	public void inserirEntrada(Calendar dataIntimacao, Integer prazo, Calendar dataPrazo) {

		long time = getTime(dataIntimacao);
		
		if (!this.cache.containsKey(time)) {
			this.cache.put(time, new HashMap<Integer,Calendar>());
		}
		
		this.cache.get(time).put(prazo, dataPrazo);		
	}	
}