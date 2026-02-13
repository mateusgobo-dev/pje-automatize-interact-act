package br.com.infox.pje.query;

public interface PessoaExpedienteDocIdentificacaoQuery {

	String PESSOA_PARAM = "pessoa";

	String PESSOA_EXP_DOC_ID_POR_PESSOA_QUERY = "select o from PessoaExpedienteDocIdentificacao o"
			+ " where o.pessoaExpediente = :" + PESSOA_PARAM + " order by o.tipoDocumento";

}