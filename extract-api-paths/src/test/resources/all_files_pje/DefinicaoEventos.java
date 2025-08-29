package br.com.infox.trf.eventos;

/**
 * Classe responsável por organizar o nome dos agrupamentos de eventos que serão
 * disparados sem intervenção do usuário.
 * 
 * @author rodrigo
 * 
 */
public class DefinicaoEventos {
	public static final String DISTRIBUICAO_AUTOMATICA_INICIAL = "Distribuição Automática (Inicial)";
	public static final String RECEBIMENTO_DISTRIBUICAO_INICIAL = "Recebimento na Distribuição (Inicial)";
	public static final String INCLUSAO_PAUTA = "Inclusão em Pauta";
	public static final String REDISTRIBUICAO = "Redistribuição";
	public static final String DESMEMBRAMENTO_FEITO = "Desmembramento de Feitos";
	public static final int COD_MOVIMENTO_DESMEMBRADO_FEITO = 11008;

	public static final String DISTRIBUICAO_PREVENCAO = "Distribuição por Prevenção";
	public static final String COD_REDISTRIBUICAO_PREVENCAO = "36B";

	public static final String DISTRIBUICAO_DEPENDENCIA = "Distribuição por Dependência";
	public static final String COD_REDISTRIBUICAO_DEPENDENCIA = "36C";

	public static final String COD_REDISTRIBUICAO_SORTEIO = "36A";

	public static final String MAGISTRADO = "Magistrado";
	public static final String CONCLUSAO = "Conclusão";
	public static final String AUDIENCIA = "Audiência";

}