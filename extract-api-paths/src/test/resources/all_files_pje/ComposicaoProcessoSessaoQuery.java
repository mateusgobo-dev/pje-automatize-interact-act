package br.com.jt.pje.query;

public interface ComposicaoProcessoSessaoQuery {
    String QUERY_PARAMETER_SESSAO = "sessao";
    String QUERY_PARAMETER_PROCESSO = "processoTrf";
    String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
    String QUERY_PARAMETER_OJ_LIST = "orgaoJulgadorList";
    String COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY = "select o from ComposicaoProcessoSessao o where " +
        "o.composicaoSessao.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and o.pautaSessao.processoTrf = :" + QUERY_PARAMETER_PROCESSO;
    String COUNT_COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY = "select count(o.idComposicaoProcessoSessao) from ComposicaoProcessoSessao o where " +
        "o.composicaoSessao.sessao = :" + QUERY_PARAMETER_SESSAO +
        " and o.pautaSessao.processoTrf = :" + QUERY_PARAMETER_PROCESSO;
    String COMPOSICAO_PROCESSO_SESSAO_QUERY = COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY +
        " and o.composicaoSessao.orgaoJulgador = :" +
        QUERY_PARAMETER_ORGAO_JULGADOR;
    String COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_OJ_LIST_QUERY = COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY +
        " and o.composicaoSessao.orgaoJulgador in (:" +
        QUERY_PARAMETER_OJ_LIST + ")";
}
