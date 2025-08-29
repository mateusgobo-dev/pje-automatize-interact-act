package br.com.infox.utils;

import java.util.HashMap;
import java.util.Map;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.Parametros;

/**
 * Classe responsável pelas constantes utilizadas no sistema PJe.
 */
public class Constantes {

	public static final Integer MAX_DISTRIBUICOES_PROCESSOS_LOTE = 500;
	public static final String COD_PARAMETRO_ABA_ALTERCAO = "ABAALTERACAO";

	public static final String COD_AGRUPAMENTO_CRIMINAL = "CRI";
	public static final String COD_AGRUPAMENTO_AMBIENTAL = "AMB";
	public static final String COD_AGRUPAMENTO_ATO_INFRACIONAL = "AAI";
	public static final String COD_AGRUPAMENTO_CRIME_ANTECEDENTE = "ACA";
	public static final String NAME_CHECKBOX_EVENTS_TREE = "checkIcr";
	
	public static final String AUTHORIZATION_TOKEN = "Authorization";
    public static final String CSRF_TOKEN = "X-XSRF-TOKEN";
    public static final String NOME_TOKEN = "Bearer ";
    public static final String COOKIE = "Cookie";
    
	/**
	 * Para maior segurança, configurar o parâmetro {@link Parametros#CHAVE_VOLATIL_CRIPTOGRAFIA} 
	 * que pode ser alterado pelos administradores na própria aplicação
	 */
	public static final String CHAVE_PADRAO_CRIPTOGRAFIA = "CHAVEPADRAOCRIPTOGRAFIA!"; 
    
    public static final String SSO_CONTEXT_NAME = "org.keycloak.KeycloakSecurityContext";
    public static final String SSO_COOKIE_NAME = "KEYCLOAK_IDENTITY";
	public static final String SSO_CLAIM_REATIVAR_COM_CERTIFICADO = "reativadoComCertificado";
    public static final String COD_AGRUPAMENTO_EXECUCAO_FISCAL = "AEF";
    
	public interface ServidorJNDI {
		public static final String JNDI_NOME_TOPICO_SESSAO_JULGAMENTO = ParametroUtil.getParametro("jndi.topicoSessao");
		public static final String URL_JNDI = ParametroUtil.getParametro("jndi.url");
		public static final String JNDI_USER = ParametroUtil.getParametro("jndi.user");
		public static final String JNDI_PASSWORD = ParametroUtil.getParametro("jndi.password");
	}
	
	public interface AudImportacao{
		public static final String COD_MOVIMENTO_INCONPETENCIA_REJEITADA = CodigoMovimentoNacional.COD_MOVIMENTO_INCOMPETENCIA_REJEITADA;
		public static final String TOTAL = "Total";
		public static final String ACOLHIDA = "Acolhida";
		public static final String REJEITADA = "Rejeitada";


	}
	public static Map<String, Integer> MapaTipoAudienciaAudParaPje = new HashMap<String, Integer>();
	static {
		MapaTipoAudienciaAudParaPje.put("001", 3); // inicial
		MapaTipoAudienciaAudParaPje.put("002", 6); // de instrução
		MapaTipoAudienciaAudParaPje.put("003", 4); // de julgamento
		MapaTipoAudienciaAudParaPje.put("008", 5); // una
		MapaTipoAudienciaAudParaPje.put("201", 2); // de conciliação (fase de execução)
		MapaTipoAudienciaAudParaPje.put("999", 6); // de instrução
	}

	/**
	 * Interface para constantes relativas ao Grau de Jurisdição utilizadas no sistema.
	 */
	public interface GRAU_JURISDICAO {
		String PRIMEIRO_GRAU = "1";
		String SEGUNDO_GRAU = "2";
		String TERCEIRO_GRAU = "3";
	}

	public interface COD_APLICACAO_CLASSE{
		String PRIMEIRO_GRAU = "1G";
		String SEGUNDO_GRAU = "2G";
		String TERCEIRO_GRAU = "3G";
		String QUARTO_GRAU = "4G";
	}
	/**
	 * Interface para constantes relativas aos nomes das classes do pacote MANAGER.
	 */
	public interface MANAGER {
		String ORGAO_JULGADOR = "orgaoJulgadorManager";
	}
	
	/**
	 * Interface para constantes relativas ao Tipo de Justiça. 
	 */
	public interface TIPO_JUSTICA {
		String ELEITORAL = "JE";
		String TRABALHO = "JT";
		String CONSELHO_SUPERIOR_TRABALHO = "JT";
		String CONSELHO_NACIONAL_JUSTICA = "CNJ";
		String MILITAR_ESTADUAL = "JME";
		String MILITAR_UNIAO = "JMU";
		String FEDERAL = "JF";
		String COMUM = "JC";
	}

	/**
	 * Interface para constantes relativas as URL's, de forma geral.
	 */
	public interface URL_GERAL {
		String PARAM_CHAVE_ACESSO = "&ca=";
		String PARAM_ID = "?id=";
	}

	/**
	 * Cookie utilizado para definir se o modo de operacao e applet ou pjeOffice
	 */
	public static final String COOKIE_MODO_OPERACAO = "MO";
	
	/**
	 * Interface para constantes relativas ao Modo de Operacao
	 */
	public interface MODO_OPERACAO {
		String APPLET = "A";
		String PJE_OFFICE = "P";
	}	

	/**
	 * Interface para constantes relativas as URL's de acesso aos detalhes do processo
	 */
	public interface URL_DETALHE_PROCESSO {
		String PROCESSO_VISUALIZACAO = "/Processo/ConsultaProcesso/Detalhe/detalheProcessoVisualizacao.seam";
		String CONSULTA_PUBLICA = "/ConsultaPublica/DetalheProcessoConsultaPublica/listView.seam";
		String PROCESSO_COMPLETO_ADVOGADO = "/Processo/ConsultaProcesso/Detalhe/listProcessoCompletoAdvogado.seam";
		String PROCESSO_COMPLETO = "/Processo/ConsultaProcesso/Detalhe/listProcessoCompleto.seam";
		String PARAM_CHAVE_ACESSO = "&ca=";
		String PARAM_ID = "?id=";
	}
	
	/**
	 * Interface para constantes relativas as URL's para tomar ciência e resposta do expediente
	 */
	public interface URL_TOMAR_CIENCIA_RESPOSTA_EXPEDIENTE {
		String TOMAR_CIENCIA_EXPEDIENTE = "/Painel/painel_usuario/popup/visualizarExpediente.seam";
		String RESPOSTA_EXPEDIENTE = "/pages/resposta/resposta.seam";
		String PARAM_CHAVE_ACESSO = "&ca=";
		String PARAM_ID = "?id=";
		String PARAM_PROCESSO_JUDICIAL_ID = "&processoJudicialId=";
		String PARAM_EXPEDIENTE_ID = "&expedienteId=";
	}

	
	/**
	 * Distância mínima em dias entre a data atual e a data de início da pesquisa para marcação de audiência.
	 */
	public static final Integer DISTANCIA_MINIMA_PESQUISA_AUDIENCIA = 10;
	
	public interface DESCRICAO_POLO_JUNTADA {
		String ATIVO = "POLO ATIVO";
		String PASSIVO = "POLO PASSIVO";
		String OUTROS_INTERESSADOS = "OUTROS INTERESSADOS";
	}
	
	public static final Integer MAXIMO_FALHAS_SUCESSIVAS_LOGIN = 3;
	
	public static final String CODIGO_AREA_DIREITO_CONSUMO = "1156";
	public static final String CODIGO_ASSUNTO_CONSUMIDOR_GOV_BR = "7777";
	public static final String COD_TIPO_DOCUMENTO_ANEXO_CONSUMIDOR_GOV_BR = "99001";
	public static final String CONCORDA_TERMOS_INTEGRACAO_PJE_CONSUMIDOR = "ConcordaTermosIntegracaoPjeConsumidor";

	/**
	 * Número máximo de pesquisas no DJE por uma matéria publicada, após esse período considera-se que a matéria não será mais publicada e houve um erro
	 */
	public static final Integer MAX_TENTATIVAS_VERIFICACAO_PUBLICACAO_DJE=35;

	public interface REQUISITORIO {
		public static final int EXPEDICAO_DOCUMENTOS = 60;	
		public static final int RPV = 50002;
		public static final int PRECATORIO = 50003;
		public static final int TIPO_DOCUMENTO = 4;
		public static final int DESTINO = 7;
	}

	public static final String TOKEN_SSO_SEM_USUARIO_ASSOCIADO = "tokenSsoSemUsuarioAssociado";
	
	public static final String COD_CLASSE_PAI_CRIMINAL = "268";
	public static final String COD_CLASSE_PAI_INFRACIONAL = "1459";

	public static final String ORIGEM_SISTEMA_PJE = "PJe";
	public static final String ORIGEM_SISTEMA_DCP = "DCP";
}

