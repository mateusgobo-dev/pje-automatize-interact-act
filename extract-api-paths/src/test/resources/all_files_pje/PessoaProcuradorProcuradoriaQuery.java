package br.com.infox.pje.query;

public interface PessoaProcuradorProcuradoriaQuery {

	String PESSOA_PROCURADORIA_ENTIDADE_PARAM = "pessoaProcuradoriaEntidade";

	String PESSOA_PROCURADOR_PROCURADORIA_POR_ENTIDADE_LIST_QUERY = "select o from PessoaProcuradorProcuradoria o"
			+ " where o.pessoaProcuradoriaEntidade = :" + PESSOA_PROCURADORIA_ENTIDADE_PARAM;

}