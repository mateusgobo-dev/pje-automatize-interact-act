package br.com.infox.pje.query;

public interface PessoaMagistradoQuery {

	String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	String QUERY_PARAMETER_PAPEL = "papel";

	String MAGISTRADO_RECEBE_DISTRIBUICAO = "magistradoRecebeDistribuicao";
	String MAGISTRADO_RECEBE_DISTRIBUICAO_QUERY = "select o from PessoaMagistrado o where "
			+ "o.idUsuario in (select ul.usuarioLocalizacao.usuario.idUsuario "
			+ "from UsuarioLocalizacaoMagistradoServidor ul where "
			+ "ul.orgaoJulgadorCargo.recebeDistribuicao = true and " + "ul.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR + " and ul.usuarioLocalizacao.papel = :" + QUERY_PARAMETER_PAPEL + ")";

}
