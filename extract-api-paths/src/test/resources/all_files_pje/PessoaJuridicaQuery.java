package br.com.infox.pje.query;

public interface PessoaJuridicaQuery {

	String QUERY_PARAMETER_NOME = "nome";

	String PESSOA_JURIDICA_BY_NOME_QUERY = "select o from PessoaJuridica o where o.nome like :" + QUERY_PARAMETER_NOME;

}