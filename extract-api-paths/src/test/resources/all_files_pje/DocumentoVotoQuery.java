package br.com.jt.pje.query;

public interface DocumentoVotoQuery {
    String QUERY_PARAMETER_VOTO = "voto";
    String QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO = "tipoProcessoDocumento";
    String QUERY_PARAMETER_PROCESSO = "processo";
    String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
    String QUERY_PARAMETER_SESSAO = "sessao";
    String DOCUMENTO_VOTO_BY_VOTO_QUERY = "select dv from DocumentoVoto dv " +
        "where dv.voto = :" + QUERY_PARAMETER_VOTO;
    String DOCUMENTO_VOTO_BY_VOTO_E_TIPO_QUERY = DOCUMENTO_VOTO_BY_VOTO_QUERY +
        " and dv.tipoProcessoDocumento = :" +
        QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO;
    String LIST_ULTIMO_DOCUMENTO_VOTO_ASSINADO_BY_TIPO_QUERY = DOCUMENTO_VOTO_BY_VOTO_E_TIPO_QUERY +
        " and dv.processo = :" + QUERY_PARAMETER_PROCESSO +
        " and dv.ativo = true " +
        " and dv.processoDocumentoBin in (select pdba.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura pdba" +
        "			  					   where pdba.dataAssinatura = (select MAX(pdba2.dataAssinatura) from ProcessoDocumentoBinPessoaAssinatura pdba2" +
        "										 						 where pdba2.processoDocumentoBin in (select pd2.processoDocumentoBin from ProcessoDocumento pd2" +
        "																									   where pd2.processo = dv.processo" +
        "																										 and pd2.tipoProcessoDocumento = dv.tipoProcessoDocumento" +
        "																										 and pd2.ativo = dv.ativo)))";
    String LIST_ULTIMO_DOCUMENTO_VOTO_BY_TIPO_QUERY = "select o from DocumentoVoto o where " +
        "o.tipoProcessoDocumento= :" + QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO +
        " and " + "o.processo = :" + QUERY_PARAMETER_PROCESSO +
        " and o.ativo=true and o.voto = :" + QUERY_PARAMETER_VOTO +
        " and o.dataInclusao = (  select MAX(o2.dataInclusao) from ProcessoDocumento o2 where " +
        "o2.idProcessoDocumento = o.idProcessoDocumento)";
}
