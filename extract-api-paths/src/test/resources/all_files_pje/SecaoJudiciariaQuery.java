package br.com.infox.pje.query;

public interface SecaoJudiciariaQuery {

	String QUERY_PARAMETER_SECAO = "secao";

	String SECAO_JUDICIARIA_QUERY = "select o from SecaoJudiciaria o order by o.cdSecaoJudiciaria";

	String SECAO_JUDICIARIA_1_GRAU_QUERY = "select o from SecaoJudiciaria o where " + "o.cdSecaoJudiciaria = :"
			+ QUERY_PARAMETER_SECAO;

	String LIST_SECAO_JUDICIARIA_1_GRAU_QUERY = "select o.secaoJudiciaria from SecaoJudiciaria o where "
			+ "o.cdSecaoJudiciaria = :" + QUERY_PARAMETER_SECAO;
}