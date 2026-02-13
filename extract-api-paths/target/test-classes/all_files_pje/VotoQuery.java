package br.com.jt.pje.query;

public interface VotoQuery {
    String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
    String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
    String QUERY_PARAMETER_SESSAO = "sessao";
    String QUERY_PARAMETER_TIPO_VOTO_DIVERGE_EM_PARTE = "tipoVotoDivergeEmParte";
    String QUERY_PARAMETER_TIPO_VOTO_DIVERGERGENTE = "tipoVotoDivergente";
    String QUERY_PARAMETER_TIPO_VOTO_ACOMPANHA_RELATOR = "tipoVotoAcompanhaRelator";
    String QUERY_PARAMETER_TIPO_VOTO_NAO_CONHECE = "tipoVotoNaoConhece";
    String RESTRICTION_COMPOSICAO_PROCESSO = "exists (" + " select cp" +
        " from ComposicaoProcessoSessao cp where" +
        " cp.composicaoSessao.orgaoJulgador = o.orgaoJulgador" +
        " and cp.pautaSessao.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and cp.pautaSessao.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        ")";
    String VOTOS_BY_PROCESSO_QUERY = "select o from Voto o " +
        "where o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF;
    String VOTOS_BY_PROCESSO_SESSAO_QUERY = VOTOS_BY_PROCESSO_QUERY +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO;
    String VOTOS_COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY = VOTOS_BY_PROCESSO_SESSAO_QUERY +
        " and " + RESTRICTION_COMPOSICAO_PROCESSO +
        " and o.orgaoJulgador != :" + QUERY_PARAMETER_ORGAO_JULGADOR;
    String ORGAO_JULGADOR_COM_VOTO_LIBERADO_QUERY = "select o.orgaoJulgador from Voto o where " +
        "o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and o.liberacao = true " +
        "and o.orgaoJulgador in (select cs.orgaoJulgador from " +
        "ComposicaoSessao cs where cs.sessao = o.sessao) " +
        "order by o.orgaoJulgador.orgaoJulgador";
    String VOTOS_PROCESSO_SEM_SESSAO_BY_ORGAO_JULGADOR_QUERY = VOTOS_BY_PROCESSO_QUERY +
        " and o.sessao is null " + " and o.orgaoJulgador in " +
        " (select cs.orgaoJulgador from ComposicaoSessao cs " +
        " where cs.sessao = :" + QUERY_PARAMETER_SESSAO + ")";
    String VOTO_PROCESSO_SEM_SESSAO_BY_ORGAO_JULGADOR_QUERY = VOTOS_BY_PROCESSO_QUERY +
        " and o.sessao is null " + " and o.orgaoJulgador = :" +
        QUERY_PARAMETER_ORGAO_JULGADOR;
    String VOTO_PROCESSO_BY_ORGAO_JULGADOR_SESSAO_QUERY = VOTOS_BY_PROCESSO_SESSAO_QUERY +
        " and o.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR;
    String ULTIMO_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_QUERY = VOTOS_BY_PROCESSO_QUERY +
        " and o.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR +
        " and o.idVoto in (select max(v.idVoto) from Voto v " +
        " where v.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR +
        " and v.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF + " )";
    String ULTIMO_VOTO_BY_ORGAO_JULGADOR_PROCESSO_SESSAO_QUERY = VOTO_PROCESSO_BY_ORGAO_JULGADOR_SESSAO_QUERY +
        " and o.idVoto in (select max(v.idVoto) from Voto v " +
        "				     where v.sessao = :" + QUERY_PARAMETER_SESSAO +
        "					   and v.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        "					   and v.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR +
        "					)";
    String QUANTIDATE_VOTOS_DIVERGENTES_BY_PROCESSO_SESSAO_QUERY = "select count(o) from Voto o where " +
        "o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO + " and o.tipoVoto in (:" +
        QUERY_PARAMETER_TIPO_VOTO_DIVERGE_EM_PARTE + ", :" +
        QUERY_PARAMETER_TIPO_VOTO_DIVERGERGENTE + ")" + " and " +
        RESTRICTION_COMPOSICAO_PROCESSO;
    String QUANTIDATE_VOTOS_SEM_CONCLUSAO_BY_PROCESSO_SESSAO_QUERY = "select count(o) from Voto o where " +
        "o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and o.tipoVoto is null" + " and " + RESTRICTION_COMPOSICAO_PROCESSO;
    String QUANTIDATE_VOTOS_NAO_CONHECE_BY_PROCESSO_SESSAO_QUERY = "select count(o) from Voto o where " +
        "o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO + " and o.tipoVoto = :" +
        QUERY_PARAMETER_TIPO_VOTO_NAO_CONHECE + " and " +
        RESTRICTION_COMPOSICAO_PROCESSO;
    String QUANTIDATE_VOTOS_ACOMPANHAM_RELATOR_BY_PROCESSO_SESSAO_QUERY = "select count(o) from Voto o where " +
        "o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO + " and o.tipoVoto = :" +
        QUERY_PARAMETER_TIPO_VOTO_ACOMPANHA_RELATOR + " and " +
        RESTRICTION_COMPOSICAO_PROCESSO;
    String VOTOS_NAO_LIBERADOS_BY_PROCESSO_SESSAO_QUERY = VOTOS_BY_PROCESSO_SESSAO_QUERY +
        " and o.liberacao = false";
    String EXISTE_VOTO_COM_DIVERGENCIA_QUERY = VOTOS_BY_PROCESSO_SESSAO_QUERY +
        " and o.marcacaoDivergencia = true ";
    String EXISTE_VOTO_COM_DESTAQUE_QUERY = VOTOS_BY_PROCESSO_SESSAO_QUERY +
        " and o.marcacaoDestaque = true ";
    String EXISTE_VOTO_COM_OBSERVACAO_QUERY = VOTOS_BY_PROCESSO_SESSAO_QUERY +
        " and o.marcacaoObservacao = true ";
}
