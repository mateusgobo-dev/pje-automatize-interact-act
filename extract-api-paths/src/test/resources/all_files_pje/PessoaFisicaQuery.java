package br.com.infox.pje.query;

public interface PessoaFisicaQuery {

	String QUERY_PARAMETER_NOME = "nome";

	String PESSOA_FISICA_BY_NOME_QUERY = "select o from PessoaFisica o where o.nome like :" + QUERY_PARAMETER_NOME;

}