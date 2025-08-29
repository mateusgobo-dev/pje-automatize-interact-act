package br.com.jt.pje.query;

public interface SalaQuery {
	
	String QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO = "orgaoJulgadorColegiado";

	String SALA_SESSAO_ITEMS_QUERY = "select o from Sala o where o.tipoSala = 'S' " +
								     "order by o.sala";
	
	String SALA_SESSAO_ITEMS_BY_ORGAO_JULGADOR_COLEGIADO_QUERY = "select o from Sala o where o.tipoSala = 'S' " +
																 "and o.orgaoJulgadorColegiado = :"+
																 QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO+
								     							 " order by o.sala";
	
}