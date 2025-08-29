package br.com.infox.pje.query;

public interface CaixaFiltroQuery {

	String QUERY_PARAM_NOME_CAIXA = "nomeCaixa";
	String QUERY_PARAM_TAREFA = "tarefa";

	String COUNT_CAIXA_BY_NOME_AND_TAREFA_QUERY = "select o from CaixaFiltro o " + "where o.nomeCaixa like :"
			+ QUERY_PARAM_NOME_CAIXA + " and " + "o.tarefa = :" + QUERY_PARAM_TAREFA;

}