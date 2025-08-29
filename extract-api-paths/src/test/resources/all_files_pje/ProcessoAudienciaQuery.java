package br.com.infox.pje.query;

public interface ProcessoAudienciaQuery {

	static final String QUERY_PARAMETER_DATA_INCIO = "dataInicio";
	static final String QUERY_PARAMETER_DATA_FIM = "dataFim";
	static final String QUERY_PARAMETER_JUIZ = "juiz";
	static final String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";

	static final String TOTAL_ACORDOS_HOMOLOGADOS_QUERY_JUIZ = "select count(*) from ProcessoAudiencia o where "
			+ "o.statusAudiencia in ('F') and o.inAcordo = true and "
			+ "o.processoTrf.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and "
			+ "to_char(o.dtInicio,'yyyy-MM-dd') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and "
			+ "to_char(o.dtInicio,'yyyy-MM-dd') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "exists (select ulms from UsuarioLocalizacaoMagistradoServidor ulms "
			+ "         where ulms.usuarioLocalizacao.usuario.nome = :"
			+ QUERY_PARAMETER_JUIZ
			+ "           and ulms.orgaoJulgadorCargo.cargo = o.processoTrf.cargo "
			+ "           and ulms.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "           and ulms.usuarioLocalizacao.usuario.idUsuario in (select pm.idUsuario from PessoaMagistrado pm) "
			+ "           and ulms.dtInicio <= o.dtInicio "
			+ "           and (ulms.dtFinal is null or (ulms.dtFinal >= o.dtInicio))) ";

	static final String VALOR_ACORDOS_HOMOLOGADOS_QUERY_JUIZ = "select sum(o.vlAcordo) from ProcessoAudiencia o where "
			+ "o.statusAudiencia in ('F') and o.inAcordo = true and o.vlAcordo is not null and "
			+ "o.processoTrf.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and "
			+ "to_char(o.dtInicio,'yyyy-MM-dd') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and "
			+ "to_char(o.dtInicio,'yyyy-MM-dd') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "exists (select ulms from UsuarioLocalizacaoMagistradoServidor ulms "
			+ "         where ulms.usuarioLocalizacao.usuario.nome = :"
			+ QUERY_PARAMETER_JUIZ
			+ "           and ulms.orgaoJulgadorCargo.cargo = o.processoTrf.cargo "
			+ "           and ulms.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "           and ulms.usuarioLocalizacao.usuario.idUsuario in (select pm.idUsuario from PessoaMagistrado pm) "
			+ "           and ulms.dtInicio <= o.dtInicio "
			+ "           and (ulms.dtFinal is null or (ulms.dtFinal >= o.dtInicio))) ";

	static final String TOTAL_ACORDOS_HOMOLOGADOS_QUERY = "select count(*) from ProcessoAudiencia o where "
			+ "o.statusAudiencia = 'F' and o.inAcordo = true and " + "o.processoTrf.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR + " and " + "to_char(o.dtInicio,'yyyy-MM-dd') >= :"
			+ QUERY_PARAMETER_DATA_INCIO + " and " + "to_char(o.dtInicio,'yyyy-MM-dd') <= :" + QUERY_PARAMETER_DATA_FIM;

	static final String VALOR_ACORDOS_HOMOLOGADOS_QUERY = "select sum(o.vlAcordo) from ProcessoAudiencia o where "
			+ "o.statusAudiencia = 'F' and o.inAcordo = true and o.vlAcordo is not null and "
			+ "o.processoTrf.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR + " and "
			+ "to_char(o.dtInicio,'yyyy-MM-dd') >= :" + QUERY_PARAMETER_DATA_INCIO + " and "
			+ "to_char(o.dtInicio,'yyyy-MM-dd') <= :" + QUERY_PARAMETER_DATA_FIM;
}
