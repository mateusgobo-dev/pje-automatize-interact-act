package br.com.infox.pje.query;

public interface PessoaExpedienteMeioContatoQuery {

	String PESSOA_PARAM = "pessoa";

	String PESSOA_EXP_MEIO_CONTATO_POR_PESSOA_QUERY = "select o from PessoaExpedienteMeioContato o"
			+ " where o.pessoaExpediente = :" + PESSOA_PARAM;

}