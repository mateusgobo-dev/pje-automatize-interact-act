package br.jus.cnj.pje.query;

import br.jus.pje.nucleo.enums.TipoConexaoEnum;

/**
 * PJEII-3755
 * Classe com constantes referentes as consultas da entidade ProcessoTrfConexao
 * 
 * @author lucio.ribeiro
 */
public interface ProcessoTrfConexaoQuery {
    
    String PROCESSO_TRF_PARAM = "procTrf";
    String PROCESSO_TRF_CONEXO_PARAM = "processoTrfConexo";
    
    String PROCESSO_TRF_CONEXAO_POR_PRINCIPAL_E_CONEXO_LIST = "select o from ProcessoTrfConexao o "
            + "where processoTrf = :" + PROCESSO_TRF_PARAM + " and processoTrfConexo = :" + PROCESSO_TRF_CONEXO_PARAM;
    
    
    String PROCESSO_TRF_ID_PARAM = "idProcessoTrf";
    
    String PROCESSO_TRF_CONEXAO_LIST = "SELECT DISTINCT o "
			                          + " FROM ProcessoTrfConexao AS o "
			                          + "      LEFT JOIN o.processoDocumento AS pd "
			                          + "      LEFT JOIN pd.processoDocumentoBin AS bin "
			                          + "WHERE o.processoTrf.idProcessoTrf = :idProcessoTrf "
			                          + "  AND o.tipoConexao != 'AS' "
	                                  + "  AND (pd IS NULL OR bin IS NULL OR bin.certChain IS NULL OR bin.certChain IS EMPTY)";
    
    String PROCESSO_TRF_CONEXAO_COUNT = "SELECT COUNT(*) "
            + " FROM ProcessoTrfConexao AS o "
            + "      LEFT JOIN o.processoDocumento AS pd "
            + "      LEFT JOIN pd.processoDocumentoBin AS bin "
            + "WHERE o.processoTrf.idProcessoTrf = :idProcessoTrf "
            + "  AND o.tipoConexao != 'AS' "
            + "  AND (pd IS NULL OR bin IS NULL OR bin.certChain IS NULL OR bin.certChain IS EMPTY)";

    String PROCESSO_TRF_CONEXAO_LIST_PREVENCAO = "SELECT DISTINCT o "
            + " FROM ProcessoTrfConexao AS o "
            + "      LEFT JOIN o.processoDocumento AS pd "
            + "      LEFT JOIN pd.processoDocumentoBin AS bin "
            + "WHERE o.processoTrf.idProcessoTrf = :idProcessoTrf "
            + "  AND o.tipoConexao = '"+TipoConexaoEnum.PR+"' "
            + "  AND (pd IS NULL OR bin IS NULL OR bin.certChain IS NULL OR bin.certChain IS EMPTY)";

	String PROCESSO_TRF_CONEXAO_COUNT_PREVENCAO = "SELECT COUNT(*) "
			+ " FROM ProcessoTrfConexao AS o "
			+ "      LEFT JOIN o.processoDocumento AS pd "
			+ "      LEFT JOIN pd.processoDocumentoBin AS bin "
			+ "WHERE o.processoTrf.idProcessoTrf = :idProcessoTrf "
			+ "  AND o.tipoConexao = '"+TipoConexaoEnum.PR+"' "
			+ "  AND (pd IS NULL OR bin IS NULL OR bin.certChain IS NULL OR bin.certChain IS EMPTY)";

}
