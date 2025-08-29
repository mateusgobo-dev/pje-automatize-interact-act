package br.com.jt.pje.query;

public interface OrgaoJulgadorColegiadoOrgaoJulgadorQuery {
    String QUERY_PARAMETER_SESSAO = "sessao";
    String QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO = "orgaoJulgadorColegiado";
    String ORGAO_JULGADOR_SEM_COMPOSICAO_BY_COLEGIADO_SESSAO_QUERY = "select ojcoj.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ojcoj " +
        "where ojcoj.orgaoJulgadorColegiado = :" +
        QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO +
        " and ojcoj.orgaoJulgador.ativo = true " +
        " and ojcoj.orgaoJulgador not in (select cs.orgaoJulgador from ComposicaoSessao cs " +
        "								   where cs.sessao = :" + QUERY_PARAMETER_SESSAO +
        "								   and cs.sessao.orgaoJulgadorColegiado = ojcoj.orgaoJulgadorColegiado)";
}
