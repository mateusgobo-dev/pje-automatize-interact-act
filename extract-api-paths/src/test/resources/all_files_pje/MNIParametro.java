/**
 * MNIParametro.java.
 *
 * Data de criação: 02/09/2014
 */
package br.jus.cnj.pje.intercomunicacao.util.constant;

import java.util.ArrayList;

/**
 * Classe com as constantes de parâmetros usados no MNI.
 * 
 * @author Adriano Pamplona
 */
public final class MNIParametro {
	public static final String PARAM_DOCUMENTO_ATIVO				= "ativo";
	public static final String PARAM_DOCUMENTO_USUARIO_MOVIMENTO	= "responsavel_movimento";
	public static final String PARAM_NUM_PROC_1_GRAU				= "numeroProcesso1Grau";
	public static final String PARAM_DOCUMENTO_VALIDO				= "docValido";
	public static final String PARAM_ID_ARQUIVO_ORIGEM				= "idArquivoOrigem";
	public static final String PARAM_NOME_DOCUMENTO				= "nomeDocumento";
	public static final String PARAM_URL_ORIGEM_ENVIO				= "urlOrigemEnvio";
	public static final String PARAM_URL_ORIGEM_CONSULTA			= "urlOrigemConsulta";
	public static final String PARAM_ATIVIDADE_ECONOMICA			= "JTAtividadeEconomica";
	public static final String PARAM_MUNICIPIO_IBGE				= "JTMunicipioIBGE";
	public static final String PARAM_FORMATO_DATA_HORA				= "yyyyMMddHHmmss";
	public static final String PARAM_FORMATO_DATA					= "yyyyMMdd";
	public static final String PARAM_COMPLEMENTO_MOVIMENTO			= "complementoMovimento";
	public static final String PARAM_PROTOCOLO						= "protocolo";
	public static final String PARAM_INSTANCIA_DOCUMENTO			= "instanciaDocumento";
	public static final String PARAM_NOME_ARQUIVO					= "nomeArquivo";
	public static final String PARAM_PRIORIDADE_PROCESSUAL			= "prioridadeProcessual";
	public static final String PARAM_CRIADOR_ARQUIVO				= "criadorArquivo";
	public static final String PARAM_TIPO_DOCUMENTO_IDENTIFICACAO_CRIADOR_ARQUIVO = "mni:pje:documento:criadorArquivo:tipoDocumentoIdentificacao";
	public static final String PARAM_USUARIO_JUNTADA_ARQUIVO		= "usuarioJuntadaArquivo";
	public static final String PARAM_DOCUMENTO_IDENTIFICACAO_CRIADOR_ARQUIVO = "mni:pje:documento:criadorArquivo:documentoIdentificacao";
	public static final String PARAM_STORAGE_ID					= "mni:pje:documento:storageId";
	public static final String PARAM_DATA_JUNTADA 					= "mni:pje:documento:dataJuntada";
	public static final String PARAM_TIPO_DOCUMENTO_IDENTIFICACAO_USUARIO_JUNTADA_ARQUIVO = "mni:pje:documento:usuarioJuntadaArquivo:tipoDocumentoIdentificacao";
	public static final String PARAM_DOCUMENTO_IDENTIFICACAO_USUARIO_JUNTADA_ARQUIVO = "mni:pje:documento:usuarioJuntadaArquivo:documentoIdentificacao";
	public static final String PARAM_DATA_INCLUSAO 					= "mni:pje:documento:dataInclusao";
	public static final String PARAM_CPF_CNPJ_USUARIO 				= "mni:pje:usuario:cpf:cnpj";
	public static final String PARAM_LOGOU_COM_CERTIFICADO			= "mni:pje:logouComCertificado";
	public static final String PARAM_JE_ESTADO						= "JEEstado";
	public static final String PARAM_JE_MUNICIPIO						= "JEMunicipio";
	public static final String PARAM_JE_ELEICAO					= "JEEleicao";
	public static final String PARAM_JE_ANO_ELEICAO				= "JEAnoEleicao";
	public static final String PARAM_JE_TIPO_ELEICAO				= "JETipoEleicao";
	public static final String PARAM_RETORNO 						= "retorno";
	public static final String PARAM_REMESSA 						= "remessa";
    public static final String PARAM_LIMINAR_ANTECIPA_TUTELA 		= "mni:pje:pedidoLiminarOuAntecipacaoTutela";
	public static final String PARAM_NUMERO_DOCUMENTO = "mni:pje:documento:numero";
	public static final String PARAM_INSTANCIA_PROCESSO_ORIGEM = "mni:pje:instanciaProcessoOrigem";
	public static final String PARAM_DESCRICAO_INSTANCIA_PROCESSO_ORIGEM = "mni:pje:descricaoInstanciaProcessoOrigem";
	public static final String PARAM_NUMERO_ORDEM = "mni:pje:documento:numeroOrdem";
	public static final String PARAM_ATENDIMENTO_PLANTAO = "mni:pje:atendimentoPlantao";
	public static final String PARAM_EXPEDIENTE = "expediente";
	public static final String PARAM_IDENTIFICADOR_EXTERNO = "mni:pje:identificadorExterno";

	public static final String PARAM_MOTIVO_SEGREDO_JUSTICA = "mni:pje:motivoSegredoJustica";
	public static final String PARAM_OJC_CODIGO = "mni:pje:orgaoJulgadorColegiado:codigo";
	public static final String PARAM_OJC_DESCRICAO = "mni:pje:orgaoJulgadorColegiado:descricao";

	public static final String PARAM_COMUNICACAO_ENTRE_OJS = "mni:pje:comunicacaoEntreOrgaosJulgadores";
	public static final String PARAM_PESSOA_JUNTADA_ARQUIVO	= "pessoaJuntadaArquivo";
	public static final String PARAM_TIPO_DOCUMENTO_IDENTIFICACAO_PESSOA_JUNTADA_ARQUIVO = "mni:pje:documento:pessoaJuntadaArquivo:tipoDocumentoIdentificacao";
	public static final String PARAM_DOCUMENTO_IDENTIFICACAO_PESSOA_JUNTADA_ARQUIVO = "mni:pje:documento:pessoaJuntadaArquivo:documentoIdentificacao";
	public static final String PARAM_PREFIXO_GENERICO_PARA_FLUXO = "pje:fluxo:";
	public static final String PARAM_LINK_VALIDACAO = "mni:pje:linkValidacao";

	private static ArrayList<String> listIndiceParteSituacaoValor = new ArrayList<String>();
	private static ArrayList<String> listIndiceParteRepresentateSituacaoValor = new ArrayList<String>();
	private static ArrayList<String> listIndiceParteSigiloValor = new ArrayList<String>();
	private static ArrayList<String> listIndicePartePrincipalValor = new ArrayList<String>();
	private static ArrayList<String> listIndiceParteRepresentateSigiloValor = new ArrayList<String>();
	private static ArrayList<String> listIndiceParteRepresentatePrincipalValor = new ArrayList<String>();


	/**
	 * Parâmetro 'idsProcessoParteExpediente', esse parâmetro é usado para informar o(s) id(s) do(s) 
	 * expediente(s) que será(ão) respondido(s), os ID's deverão ser separados por ';', conforme 
	 * exemplo abaixo. <br/>
	 * Ex: <br/>
	 * <pre>
	 * Parametro parametro = new Parametro();
	 * parametro.setNome(MNIParametro.getIdsProcessoParteExpediente());
	 * parametro.setValor("155;166");
	 * </pre>
	 * 
	 * @return idsProcessoParteExpediente.
	 */
	public static String getIdsProcessoParteExpediente() {
		return "mni:idsProcessoParteExpediente";
	}

	/**
	 * Parâmetro 'isPJE', esse parâmetro indica que a requisição para a entrega de manifestação foi
	 * feita a partir de um PJE.
	 * Ex: <br/>
	 * <pre>
	 * Parametro parametro = new Parametro();
	 * parametro.setNome(MNIParametro.isPJE());
	 * parametro.setValor(true);
	 * </pre>
	 * 
	 * @return isPJE.
	 */
	public static String isPJE() {
		return "mni:isPJE";
	}
	
	public static String getIdRetornoProcessoParteExpedientePostado()
	{
		return "mni:retornoPostagem:idProcessoParteExpediente";
	}
	
	/**
	 * Parâmetro 'mni:indiceParteSituacao' receberá os índices da lista de Partes do MNI e vinculará
	 * ao código de situação da parte
	 * 
	 * Ex: Mapeamento K:V 
	 * 
	 *  'mni:indiceParteSituacao' -> 'AT:0:A, AT:1:I, AT:2:B, PA:0:A' 
	 *  	-> Polo (AT)ivo Parte (0)  está (A)tivo, ... , Polo (PA)ssivo Parte(0) está (A)tivo <- 
	 *
	 * 
	 * @return 'mni:indiceParteSituacao'
	 */
	public static String getIndiceParteSituacao(){
		return "mni:indiceParteSituacao";
	}

	/**
	 * Parâmetro 'mni:indiceParteRepresentanteSituacao' receberá os índices da lista de Partes e seus representantes
	 *  do MNI e vinculará ao código de situação da parte representante
	 * 
	 * Ex: Mapeamento K:V 
	 * 
	 *  'mni:indiceParteRepresentanteSituacao' -> 'AT:0:0:A, AT:1:0:I, AT:1:1:B, PA:0:0:A' 
	 *  	-> Polo (AT)ivo Parte (0) Representante (0)  está (A)tivo, ... , Polo (PA)ssivo Parte(0) Representante (0) está (A)tivo <- 
	 * 
	 * @return 'mni:indiceParteRepresentanteSituacao'
	 */
	public static String getIndiceParteRepresentanteSituacao(){
		return "mni:indiceParteRepresentanteSituacao";
	}

	/**
	 * Parâmetro 'mni:indiceParteSigilo' receberá os índices da lista de Partes do MNI e vinculará
	 * ao atributo 'parteSigilosa' da parte
	 * 
	 * Ex: Mapeamento K:V 
	 * 
	 *  'mni:indiceParteSigilo' -> 'AT:0:true, AT:1:false, AT:2:false, PA:0:false' 
	 *  	-> Polo (AT)ivo Parte (0)  é sigiloso, ... , Polo (PA)ssivo Parte(0) não é sigiloso <- 
	 *
	 * 
	 * @return 'mni:indiceParteSigilo'
	 */
	public static String getIndiceParteSigilo(){
		return "mni:indiceParteSigilo";
	}
	
	/**
	 * Parâmetro 'mni:indicePartePrincipal' receberá os índices da lista de Partes do MNI e vinculará
	 * ao atributo 'partePrincipal' da parte
	 * 
	 *  'mni:indicePartePrincipal' -> 'AT:0:true, AT:1:false, AT:2:false, PA:0:false'
	 * 
	 * @return 'mni:indicePartePrincipal'
	 */
	public static String getIndicePartePrincipal(){
		return "mni:indicePartePrincipal";
	}

	/**
	 * Parâmetro 'mni:indiceParteRepresentanteSigilo' receberá os índices da lista de Partes e seus representantes
	 *  do MNI e vinculará ao atributo 'parteSigilosa' do representante
	 * 
	 * Ex: Mapeamento K:V 
	 * 
	 *  'mni:indiceParteRepresentanteSituacao' -> 'AT:0:0:true, AT:1:0:false, AT:1:1:false, PA:0:0:false' 
	 *  	-> Polo (AT)ivo Parte (0) Representante (0)  é sigiloso, ... , Polo (PA)ssivo Parte(0) Representante (0) não é sigiloso<- 
	 * 
	 * @return 'mni:indiceParteRepresentanteSigilo'
	 */
	public static String getIndiceParteRepresentanteSigilo(){
		return "mni:indiceParteRepresentanteSigilo";
	}
	
	public static ArrayList<String> getListIndiceParteSituacao() {
		return listIndiceParteSituacaoValor;
	}

	public static ArrayList<String> getListIndiceParteRepresentateSituacaoValor() {
		return listIndiceParteRepresentateSituacaoValor;
	}
	
	public static ArrayList<String> getListIndiceParteSigiloValor() {
		return listIndiceParteSigiloValor;
	}
	
	public static ArrayList<String> getListIndicePartePrincipalValor() {
		return listIndicePartePrincipalValor;
	}
	
	public static ArrayList<String> getListIndiceParteRepresentateSigiloValor() {
		return listIndiceParteRepresentateSigiloValor;
	}
	public static ArrayList<String> getListIndiceParteRepresentatePrincipalValor() {
		return listIndiceParteRepresentatePrincipalValor;
	}

	/**
	 * Parâmetro 'isProcessoIncidental', esse parâmetro indica que o processo enviado é 'incidental'.
	 * Ex: <br/>
	 * <pre>
	 * Parametro parametro = new Parametro();
	 * parametro.setNome(MNIParametro.isProcessoIncidental());
	 * parametro.setValor(true);
	 * </pre>
	 * 
	 * @return idProcessoIncidental.
	 */
	public static String isProcessoIncidental() {
		return "mni:isProcessoIncidental";
	}
	
	/**
	 * Parâmetro 'numeroUnicoProcessoOriginario', número do processo originário para protocolo
	 * de processo incidental.
	 * Ex: <br/>
	 * <pre>
	 * Parametro parametro = new Parametro();
	 * parametro.setNome(MNIParametro.getNumeroUnicoProcessoOriginario());
	 * parametro.setValor("0000028-36.2016.2.00.0000");
	 * </pre>
	 * 
	 * @return numeroUnicoProcessoOriginario.
	 */
	public static String getNumeroUnicoProcessoOriginario() {
		return "mni:numeroUnicoProcessoOriginario";
	}
	
	/**
	 * Parâmetro 'situacaoProcesso' informa a situação do processo no PJe.
	 * Ex: <br/>
	 * <pre>
	 * Parametro parametro = new Parametro();
	 * parametro.setNome(MNIParametro.getSituacaoProcesso());
	 * parametro.setValor("Andamento");
	 * </pre>
	 * 
	 * @return numeroUnicoProcessoOriginario.
	 */
	public static String getSituacaoProcesso() {
		return "mni:situacaoProcesso";
	}

	/**
	 * Parâmetro 'idOrgaoRepresentacao', esse parâmetro representa o  
	 * CNPJ do Órgão de Representação (Defensoria/Procuradoria) informado
	 * pelo usuário do MNI.
	 * 
	 * Ex: <br/>
	 * <pre>
	 * Parametro parametro = new Parametro();
	 * parametro.setNome(MNIParametro.getIdOrgaoRepresentacao());
	 * parametro.setValor("09.284.001/0001-80");
	 * </pre>
	 * 
	 * @return idOrgaoRepresentacao.
	 */
	public static String getIdOrgaoRepresentacao() {
		return "mni:idOrgaoRepresentacao";
	}
}
