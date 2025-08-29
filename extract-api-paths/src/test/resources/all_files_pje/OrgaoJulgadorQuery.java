package br.com.infox.pje.query;

public interface OrgaoJulgadorQuery {

	String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	String QUERY_PARAMETER_PAPEL_DIRETOR_SECRETARIA = "papelDiretorSecretaria";
	String QUERY_PARAMETER_ORGAO_JULGADOR_JUIZ = "orgaoJulgadorJuiz";
	String QUERY_PARAMETER_PAPEL_MAGISTRADO = "papelMagistrado";

	/* Traz Diretor da Vara (OrgaoJulgador) */
	String GET_DIRETOR_VARA_BY_ORGAO_JULGADOR_QUERY = " select u.usuario " + " from OrgaoJulgador o "
			+ " left join o.localizacao.usuarioLocalizacaoList u " + " where u.papel = :"
			+ QUERY_PARAMETER_PAPEL_DIRETOR_SECRETARIA + " and o = :" + QUERY_PARAMETER_ORGAO_JULGADOR
			+ " order by u.idUsuarioLocalizacao desc";

	/* Traz Juiz Federal da Vara (OrgaoJulgador) */
	String GET_JUIZ_FEDERAL_BY_ORGAO_JULGADOR_QUERY = " select u.usuario " + " from OrgaoJulgador o "
			+ " left join o.localizacao.usuarioLocalizacaoList u " + " where u.papel = :"
			+ QUERY_PARAMETER_PAPEL_MAGISTRADO + " and o = :" + QUERY_PARAMETER_ORGAO_JULGADOR_JUIZ
			+ " and u.usuario in " + " (select ulm.usuarioLocalizacao.usuario "
			+ " from UsuarioLocalizacaoMagistradoServidor ulm " + " where o = ulm.orgaoJulgador "
			+ " and ulm.orgaoJulgadorCargo.recebeDistribuicao = true "
			+ " and (ulm.dtFinal > current_date() or ulm.dtFinal is null)) " + " order by u.idUsuarioLocalizacao desc";

}
