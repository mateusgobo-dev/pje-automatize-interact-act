package br.jus.cnj.pje.nucleo;

public class CodigoMovimentoNacional {
	
	// juntada de documentos
	public static final String COD_MOVIMENTO_JUNTADA_DOCUMENTO = "581";
	public static final String COD_MOVIMENTO_JUNTADA_PETICAO = "85";
	

	// expedicao de documentos
	public static final String COD_MOVIMENTO_EXPEDICAO_DOCUMENTO = "60";
	
	public static final String NOME_COMPLEMENTO_TIPO_DOCUMENTO = "tipo_de_documento";
	public interface COD_COMPLEMENTO_TIPO_DOCUMENTO {
		String SENTENCA = "118";
		String DECISAO = "181";
		String ACORDAO = "117";
		String OFICIO = "79";
		String MANDADO = "78";
		String CERTIDAO = "107";
		String AVISO_RECEBIMENTO = "74";
		String ALVARA = "73";
		String OUTROS_DOCUMENTOS = "80";
	}


	
	// retificacao / exclusao de documentos / movimentos
	public static final String CODIGO_MOVIMENTO_RETIFICAR_MOVIMENTO = "11983";
	public static final String CODIGO_MOVIMENTO_EXCLUSAO_MOVIMENTO = "12291";
	public static final String CODIGO_MOVIMENTO_EXCLUSAO_DOCUMENTO = "12290";
	
	// conexao de processos
    public static final String CODIGO_MOVIMENTO_APENSAMENTO = "135";
    public static final String CODIGO_MOVIMENTO_DESAPENSAMENTO = "137";

	// audiencias
	public static final String COD_MOVIMENTO_AUDIENCIA = "970";
	
	// sessao de julgamento
	public static final String CODIGO_MOVIMENTO_SESSAO_INCLUSAO_PAUTA_PRESENCIAL="12115";
	public static final String CODIGO_MOVIMENTO_SESSAO_INCLUSAO_PAUTA_VIRTUAL="12116";
	public static final String CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_MERITO="12200";
	public static final String CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_LIMINAR="12201";
	public static final String CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_QUESTAO_ORDEM="12202";
	public static final String CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA="12205";
	public static final String CODIGO_MOVIMENTO_SESSAO_ADIADO="12203";
	public static final String CODIGO_MOVIMENTO_SESSAO_PEDIDO_VISTA="12204";
	public static final String CODIGO_MOVIMENTO_SESSAO_CONVERTIDO_DILIGENCIA="12273";
	public static final String CODIGO_MOVIMENTO_SESSAO_RETIFICADO_JULGAMENTO="12275";
	public static final String CODIGO_MOVIMENTO_SESSAO_SUSPENSO_SOBRESTADO="12274";

	// complementos genéricos
	public static final String NOME_COMPLEMENTO_DATAHORA="data_hora";
	public static final String NOME_COMPLEMENTO_DATA="data";
	public static final String NOME_COMPLEMENTO_LOCAL="local";
	public static final String NOME_COMPLEMENTO_NOME_PARTE="nome_da_parte";
	
	// distribuicao
	public static final String COD_MOVIMENTO_DISTRIBUICAO_DISTRIBUIDO = "26";
	/**
	 * Processo recebido pela equipe de distribuição: 
	 * 
	 * Inclui o recebimento de petição inicial do protocolo, além dos autos 
	 * encaminhados para os diversos registros de distribuição, como redistribuição, 
	 * cancelamento, etc. Marca o início da responsabilidade do Distribuidor pelo documento.
	 */
	public static final String COD_MOVIMENTO_DISTRIBUICAO_RECEBIDO = "981";
	
	// redistribuicao
	public static final String COD_MOVIMENTO_REDISTRIBUICAO = "36";
	
	public  static final String CODIGO_MOVIMENTO_DESENTRANHAMENTO_DOCUMENTO = "12270";
	
	public static final String NOME_COMPLEMENTO_MOTIVO_DA_REDISTRIBUICAO = "motivo_da_redistribuicao";
	public interface COD_COMPLEMENTO_MOTIVO_REDISTRIBUICAO {
		String ALTERACAO_COMPETENCIA_ORGAO = "84";
		String CRIACAO_UNIDADE_JUDICIARIA = "35";
		String DESAFORAMENTO = "28";
		String ERRO_MATERIAL = "29";
		String MODIFICACAO_DA_COMPETENCIA = "PENDENTE_189";
		String EXTINCAO_UNIDADE_JUDICIARIA = "89";
		String IMPEDIMENTO = "30";
		String INCOMPETENCIA = "83";
		String RECUSA_PREVENCAO_DEPENDENCIA = "87";
		String REMESSA_EXECUCAO_CIVEL = "88";
		String REUNIAO_EXECUCOES = "86";
		String SUCESSAO = "34";
		String SUSPEICAO = "31";
	}
	public static final String COD_MOVIMENTO_INCOMPETENCIA_REJEITADA = "374";


	public static final String NOME_COMPLEMENTO_TIPO_DE_DISTRIBUICAO_REDISTRIBUICAO = "tipo_de_distribuicao_redistribuicao";
	public interface COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO {
		String COMPETENCIA_EXCLUSIVA = "1";
		String DEPENDENCIA = "4";
		String PREVENCAO = "3";
		String SORTEIO = "2";
		String SORTEIO_MANUAL = "85";

		// COMPLEMENTOS NÃO EXISTEM NO SGT - FORAM CRIADOS NO TSE APENAS NO PJE
		String PREVENCAO_ART260_ELEICAO_ESTADUAL = "6003";
		String PREVENCAO_ART260_ELEICAO_MUNICIPAL = "6004";
	}
	
	// Expedientes
	public static final String CODIGO_MOVIMENTO_EXPEDIENTE_DECURSO_PRAZO="1051"; // "Decorrido prazo de #{nome_da_parte} em #{data}."
	
	// DJE
	public static final String CODIGO_MOVIMENTO_DJE_DISPONIBILIZACAO="1061"; // "Disponibilizado no DJ Eletrônico em  #(data)"
	public static final String CODIGO_MOVIMENTO_DJE_PUBLICACAO="92"; // "Publicado #{ato_publicado} em #{data}"
	
	// Central de mandados
	public static final String CODIGO_MOVIMENTO_COMUNICACAO_MANDADO_DEVOLVIDO="106"; // pje:movimento:codigo:mandadoDevolvido
	public static final String CODIGO_MOVIMENTO_COMUNICACAO_MANDADO_RECEBIDO="985";// "pje:movimento:codigo:recebidoMandadoCumprimento"
	
	// Remessa
	public static final String CODIGO_MOVIMENTO_COMUNICACAO_REMESSA="123";
	public static final String CODIGO_MOVIMENTO_COMUNICACAO_RECEBIMENTO="132";
	public static final String CODIGO_MOVIMENTO_COMUNICACAO_BAIXA_DEFINITIVA="22"; // codMovimentoBaixaDefinitivaDistribuicao
	
	// Arquivamento / desarquivamento
	public static final String CODIGO_MOVIMENTO_PROCESSO_ARQUIVAMENTO_DEFINITIVO="246"; // codMovimentoArquivamentoDefinitivo
	public static final String CODIGO_MOVIMENTO_PROCESSO_ARQUIVAMENTO_SUMARISSIMO="472"; // codMovimentoArquivamentoSumarissimo
	public static final String CODIGO_MOVIMENTO_PROCESSO_ARQUIVAMENTO_AUSENCIA_RECLAMANTE="473"; //codMovimentoAusenciaReclamante
	public static final String CODIGO_MOVIMENTO_PROCESSO_DESARQUIVAMENTO="893"; // codMovimentoDesarquivamento
	
	// Retificacao do processo
	public static final String CODIGO_MOVIMENTO_RETIFICACAO_CLASSE="14738";
	

	// DIVERSOS - CARACTERÍSTICAS DO PROCESSO
	public static final String CODIGO_MOVIMENTO_PROCESSO_CUSTAS="479"; // codMovimentoCustas
	public static final String CODIGO_MOVIMENTO_PROCESSO_LIQUIDACAO_HOMOLOGACAO="466"; // codMovimentoLiquidacaoHomologacao
	public static final String CODIGO_MOVIMENTO_PROCESSO_AJG_CONCEDIDA="11024"; // codMovimentoAssistenciaGratuita
	public static final String CODIGO_MOVIMENTO_PROCESSO_AJG_NAO_CONCEDIDA="334"; // codMovimentoNaoAssistenciaGratuita

	// Exclusivo JT
	public static final String CODIGO_MOVIMENTO_PROCESSO_BNDT="50085"; 
	
}

