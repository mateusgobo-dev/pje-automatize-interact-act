package br.com.infox.pje.query;

public interface TipoContatoQuery {

	String TIPO_PESSOA = "tipoPessoaTipoContato";

	String TIPO_CONTATO_POR_TIPO_PESSOA_QUERY = "select o from TipoContato o"
			+ " where o.tipoPessoa = 'A' or o.tipoPessoa = :" + TIPO_PESSOA + " and o.ativo is true";

}