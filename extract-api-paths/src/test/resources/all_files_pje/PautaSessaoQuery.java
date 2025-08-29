package br.com.jt.pje.query;

public interface PautaSessaoQuery {
    static final String QUERY_PARAMETER_SESSAO = "sessao";
    static final String QUERY_PARAMETER_PROCESSO = "processoTrf";
    static final String QUERY_PARAMETER_RESULTADO_VOTACAO = "resultadoVotacao";
    static final String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
    static final String QUERY_PARAMETER_CLASSIFICACAO = "classificacao";
    static final String QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_APREGOADO = "tipoSituacaoPautaApregoado";
    static final String QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_PENDENTE = "tipoSituacaoPautaPendente";
    static final String QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_JULGADO = "tipoSituacaoPautaJulgado";

    //TODO remover
    static final String QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO = "tipoProcessoDocumento";
    static final String GET_PAUTA_BY_PROCESSO_QUERY = "select o from PautaSessao o " +
        "where o.processoTrf = :" + QUERY_PARAMETER_PROCESSO;
    static final String PAUTA_SESSAO_BY_SESSAO_QUERY = "select o from PautaSessao o where " +
        "o.sessao = :" + QUERY_PARAMETER_SESSAO;
    static final String COUNT_PAUTA_SESSAO_BY_SESSAO_QUERY = "select count(o) from PautaSessao o  " +
        "where o.sessao = :" + QUERY_PARAMETER_SESSAO;
    static final String DATA_ULTIMA_SESSAO_BY_PROCESSO_QUERY = "select o.sessao.dataSessao from PautaSessao o where " +
        "o.processoTrf = :" + QUERY_PARAMETER_PROCESSO +
        " and o.idPautaSessao in (select max(p.idPautaSessao) from PautaSessao p " +
        "							where p.processoTrf = :" + QUERY_PARAMETER_PROCESSO + ")";
    static final String PROCESSOS_PAUTA_SESSAO_INCLUSAO_PA_QUERY = PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoInclusao = 'PA'";
    static final String QUANTIDADE_PROCESSOS_BY_ORGAO_JULGADOR_QUERY = COUNT_PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.processoTrf.orgaoJulgador = :" +
        QUERY_PARAMETER_ORGAO_JULGADOR;
    static final String QUANTIDADE_PROCESSOS_RESULTADO_VOTACAO_QUERY = COUNT_PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.resultadoVotacao = :" + QUERY_PARAMETER_RESULTADO_VOTACAO;
    static final String ORGAO_JULGADOR_REDATOR_BY_PROCESSO_SESSAO_QUERY = "select o.orgaoJulgadorRedator from PautaSessao o where " +
        "o.sessao.idSessao = :" + QUERY_PARAMETER_SESSAO +
        " and o.processoTrf = :" + QUERY_PARAMETER_PROCESSO;
    static final String GET_PAUTA_SESSAO_EM_ANDAMENTO_BY_PROCESSO_QUERY = GET_PAUTA_BY_PROCESSO_QUERY +
        " and o.sessao.situacaoSessao != 'F'";
    static final String QUANTIDADE_PROCESSOS_BY_SESSAO_CLASSIFICACAO_QUERY = COUNT_PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoSituacaoPauta.classificacao = :" +
        QUERY_PARAMETER_CLASSIFICACAO;
    static final String GET_PAUTA_PROCESSO_APREGOADO_BY_SESSAO_QUERY = PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoSituacaoPauta = :" +
        QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_APREGOADO;
    static final String EXISTE_PROCESSO_PENDENTE_QUERY = COUNT_PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoSituacaoPauta in (:" +
        QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_APREGOADO + ", :" +
        QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_PENDENTE + ")";
    static final String EXISTE_PROCESSO_JULGADO_SEM_CONCLUSAO_QUERY = COUNT_PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoSituacaoPauta = :" +
        QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_JULGADO +
        " and exists (select v from Voto v where " + " v.sessao = :" +
        QUERY_PARAMETER_SESSAO + " and v.processoTrf = o.processoTrf " +
        " and v.tipoVoto is null and v in (" +
        "select v2 from Voto v2 where v2.processoTrf = o.processoTrf and v2.sessao = :" +
        QUERY_PARAMETER_SESSAO + " and exists (" + " select cp" +
        " from ComposicaoProcessoSessao cp where" +
        " cp.composicaoSessao.orgaoJulgador = v2.orgaoJulgador" +
        " and cp.pautaSessao.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and cp.pautaSessao.processoTrf = o.processoTrf))) ";
    static final String PROCESSOS_RETIRADO_PAUTA_OU_DELIBERADO_QUERY = PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoSituacaoPauta.classificacao in ('R', 'D')";
    static final String PROCESSOS_JULGADOS_QUERY = PAUTA_SESSAO_BY_SESSAO_QUERY +
        " and o.tipoSituacaoPauta.classificacao = 'J'";

    //TODO remover
    static final String EXISTE_DOCUMENTO_ACORDAO_QUERY = "Select count(o) from PautaSessao o " +
        "where o.processoTrf = :" + QUERY_PARAMETER_PROCESSO +
        " and o.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and exists (select pd from ProcessoDocumento pd " +
        " 			  where pd.processo.idProcesso = o.processoTrf.idProcessoTrf " +
        " 			  and pd.tipoProcessoDocumento = :" +
        QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO + " 			  and pd.ativo = true)";
    static final String GET_ULTIMA_PAUTA_BY_PROCESSO_QUERY = GET_PAUTA_BY_PROCESSO_QUERY +
        " and o.idPautaSessao in (select max(p.idPautaSessao) from PautaSessao p " +
        "							where p.processoTrf = :" + QUERY_PARAMETER_PROCESSO + ")";
}
