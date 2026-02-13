package br.com.infox.pje.query;

public interface PessoaExpedienteEnderecoQuery {

	String PESSOA_PARAM = "pessoa";

	String PESSOA_EXP_ENDERECO_POR_PESSOA_QUERY = "select o from PessoaExpedienteEndereco o"
			+ " where o.pessoaExpediente = :" + PESSOA_PARAM;

}