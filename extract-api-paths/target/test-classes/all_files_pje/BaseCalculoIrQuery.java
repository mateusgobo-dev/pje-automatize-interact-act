package br.com.infox.pje.query;

public interface BaseCalculoIrQuery {

	String QUERY_PARAMETER_VALOR = "valor";

	String GET_BASE_CALCULO_IR_BY_VALOR_QUERY = "select bc from BaseCalculoIr bc where "
			+ "bc.vlBaseCalculoIrMinimo <= :" + QUERY_PARAMETER_VALOR + " and " + "(bc.vlBaseCalculoIrMaximo >= :"
			+ QUERY_PARAMETER_VALOR + " or " + "bc.vlBaseCalculoIrMaximo is null) and "
			+ "(bc.dataFimVigencia >= CURRENT_DATE and "
			+ "extract(year from bc.dataInicioVigencia) <= extract(year from CURRENT_DATE) "
			+ "and extract(year from bc.dataFimVigencia) >= extract(year from CURRENT_DATE))";
	
	String GET_BASE_CALCULO_IR_QUERY = "select bc from BaseCalculoIr bc where "+
	   "(bc.dataFimVigencia >= CURRENT_DATE and " +
	   "extract(year from bc.dataInicioVigencia) <= extract(year from CURRENT_DATE) " +
	   "and extract(year from bc.dataFimVigencia) >= extract(year from CURRENT_DATE))";
	
}
