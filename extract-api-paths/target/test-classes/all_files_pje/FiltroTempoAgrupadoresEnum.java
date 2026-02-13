package br.jus.pje.nucleo.enums;

import java.util.Date;

import br.jus.pje.nucleo.util.DateUtil;

public enum FiltroTempoAgrupadoresEnum implements PJeEnum {
	
	ULTIMOS_15_DIAS("Últimos 15 dias"), 
	ULTIMO_MES("Último mês"), 
	ULTIMOS_2_MESES("Últimos 2 meses"), 
	ULTIMOS_3_MESES("Últimos 3 meses"),
	ULTIMOS_6_MESES("Últimos 6 meses"),
	ULTIMO_ANO("Último ano"),
	SEMPRE("Sempre");
	
	private String label;
	
	private FiltroTempoAgrupadoresEnum(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	public static FiltroTempoAgrupadoresEnum getFiltroPadrao() {
		return ULTIMOS_15_DIAS;
	}
	
	/**
	 * 
	 * @return A data referência do filtro indicado a partir da data atual
	 */
	public Date getDataReferenciaFiltroTempo() {
		Date dataReferenciaFiltro = null;
		switch(this) {
			case ULTIMOS_15_DIAS:
				dataReferenciaFiltro = DateUtil.defineData(-15,null, null, 0, 0);
				break;
			case ULTIMO_MES:
				dataReferenciaFiltro = DateUtil.defineData(null,-1, null, 0, 0);
				break;
			case ULTIMOS_2_MESES:
				dataReferenciaFiltro = DateUtil.defineData(null, -2, null, 0, 0);
				break;
			case ULTIMOS_3_MESES:
				dataReferenciaFiltro = DateUtil.defineData(null, -3, null, 0, 0);
				break;
			case ULTIMOS_6_MESES:
				dataReferenciaFiltro = DateUtil.defineData(null, -6, null, 0, 0);
				break;
			case ULTIMO_ANO:
				dataReferenciaFiltro = DateUtil.defineData(null, null, -1, 0, 0);
				break;
			default:
				break;
		}

		return dataReferenciaFiltro;
	}
}