package br.com.infox.pje.query;

public interface PessoaProcuradoriaEntidadeQuery {

	String PESSOA_PARAM = "pessoa";

	String PESSOA_PROCURADORIA_ENTIDADE_POR_PESSOA_QUERY = "select o from PessoaProcuradoriaEntidade o"
			+ " where o.pessoa = :" + PESSOA_PARAM;
}