package br.jus.cnj.pje.webservice.client.domicilioeletronico;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.json.JSONObject;

import br.com.infox.cliente.util.HttpUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.UrlUtil;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PjeDomicilioPessoaNaoEncontradaException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.nucleo.service.TribunalService;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.AtualizacaoDataCienciaDTO;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.ComunicacaoProcessualDTO;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.RegistroCienciaDTO;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.RepresentantesDTO;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.cnj.pje.webservice.util.RestUtil;
import br.jus.pje.nucleo.dto.domicilioeletronico.DownloadLotePessoasDTO;
import br.jus.pje.nucleo.dto.domicilioeletronico.LotePessoasDTO;
import br.jus.pje.nucleo.dto.domicilioeletronico.PessoaDomicilioEletronicoDTO;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;
import io.restassured.http.Method;
import io.restassured.response.Response;

/**
 * Camada de integração com o Domicílio Eletrônico.
 * 
 * @author Adriano Pamplona
 */
@Name(DomicilioEletronicoRestClient.NAME)
@Scope(ScopeType.EVENT)
public class DomicilioEletronicoRestClient {

	public static final String NAME = "domicilioEletronicoRestClient";

	public static final String DOMICILIO_ELETRONICO_TOKEN = "KEYCLOAK_DOMICILIO_ELETRONICO_TOKEN";
	
	/**
	 * Construtor.
	 */
	public DomicilioEletronicoRestClient() {
		super();
	}

	/**
	 * @return Instância da classe.
	 */
	public static DomicilioEletronicoRestClient instance() {
		return ComponentUtil.getComponent(DomicilioEletronicoRestClient.class);
	}

	/**
	 * Efetua o login no Domicílio Eletrônico do tipo 'client-credentials'.
	 * 
	 * @return Token.
	 */
	public String login() {
		String token = (String) Contexts.getSessionContext().get(DOMICILIO_ELETRONICO_TOKEN);
		if (isIntegracaoHabilitada() && StringUtils.isBlank(token)) {
			KeycloakServiceClient keycloak = KeycloakServiceClient.instance();
			String clientId = ParametroUtil.instance().getDomicilioEletronicoClientId();
			String secret = ParametroUtil.instance().getDomicilioEletronicoSecret();

			token = keycloak.login(clientId, secret);
			Contexts.getSessionContext().set(DOMICILIO_ELETRONICO_TOKEN, token);
		}

		return token;
	}
	
	/**
	 * Endpoint /eu. Retorna as informações do usuário do token. É usado para testar
	 * o endpoint.
	 */
	public void eu() {
		String url = PjeEurekaRegister.instance().getURLDomicilioEletronico("/api/v1/eu");
		String token = DomicilioEletronicoService.instance().login();

		RestUtil.get(url, null, JSONObject.class, token);
	}

	/**
	 * Endpoint /api/v1/comunicacoes. Envia o expediente para o Domicílio
	 * Eletrônico.
	 * 
	 * @param expediente
	 * @param usuario 
	 * @param callbacks Callback de sucesso e callback de exceção.
	 * @return boolean
	 */
	public boolean enviarExpediente(ProcessoParteExpediente expediente, Usuario usuario) {
		return enviarExpediente(new ComunicacaoProcessualDTO(expediente, usuario));
	}
	
	/**
	 * Endpoint /api/v1/comunicacoes. Envia o expediente para o Domicílio
	 * Eletrônico.
	 * 
	 * @param expediente
	 * @param callbacks Callback de sucesso e callback de exceção.
	 * @return boolean
	 */
	public boolean enviarExpediente(ComunicacaoProcessualDTO expediente) {
		String url = PjeEurekaRegister.instance().getURLComunicacaoProcessual("/api/v1/comunicacoes");
		String token = TribunalService.instance().login();
		
		JSONObject result = RestUtil.postAsync(url, expediente, JSONObject.class, token);

		return result != null;
	}

	/**
	 * Recupera uma pessoa da API do Domicílio Eletrônico. Caso a pessoa buscada não
	 * exista, retorna erro. Esse erro pode ser ignorado (apenas subir a exceção ou
	 * catch sem realizar ações) se antes for validada a habilitação da parte.
	 * 
	 * @param cpfCnpj documento da pessoa buscada.
	 * @return pessoa buscada, ou erro, caso não exista na API.
	 * @throws PjeRestClientException caso a parte não exista no Domicílio.
	 */
	public PessoaDomicilioEletronicoDTO getPessoa(String cpfCnpj) throws PjeDomicilioPessoaNaoEncontradaException {
		if (cpfCnpj != null) {
			String url = PjeEurekaRegister.instance().getURLDomicilioEletronico("/api/v1/pessoas?documento=" + cpfCnpj);
			String token = TribunalService.instance().login();
			PessoaDomicilioEletronicoDTO pessoa = RestUtil.get(url, null, PessoaDomicilioEletronicoDTO.class, token);
			if (pessoa != null) { // Não foi retornada a pessoa diretamente para evitar retornar nulo
				return pessoa;
			}
		}

		throw new PjeDomicilioPessoaNaoEncontradaException(
				MessageFormat.format("Pessoa de documento {0} não existe no Domicílio Eletrônico.", cpfCnpj));
	}

	/**
	 * Endpoint /api/v1/comunicacoes/{numeroComunicacao}/processo/{numeroProcesso}.
	 * Altera a data final de ciência para a comunicação.
	 * 
	 * @param expediente Expediente com IdProcessoParteExpediente, dtPrazoLegal e
	 *                   processoJudicial.numeroProcesso
	 * @param prazoFinal Prazo final
	 * @throws PJeBusinessException 
	 */
	public void alterarDataFinalCiencia(ProcessoParteExpediente expediente, Date prazoFinal) {
		AtualizacaoDataCienciaDTO dto = new AtualizacaoDataCienciaDTO(prazoFinal);
		dto.setIdProcessoParteExpediente(expediente.getIdProcessoParteExpediente());
		dto.setNumeroProcesso(expediente.getProcessoJudicial().getNumeroProcesso());
		
		alterarDataFinalCiencia(dto);
	}
	
	/**
	 * Endpoint /api/v1/comunicacoes/{numeroComunicacao}/processo/{numeroProcesso}.
	 * Altera a data final de ciência para a comunicação.
	 * 
	 * @param expediente Expediente com IdProcessoParteExpediente, dtPrazoLegal e
	 *                   processoJudicial.numeroProcesso
	 * @param prazoFinal Prazo final
	 * @throws PJeBusinessException 
	 */
	public void alterarDataFinalCiencia(AtualizacaoDataCienciaDTO dto) {
		String numeroProcesso = StringUtil.limparCharsNaoNumericos(dto.getNumeroProcesso());
		String endpoint = String.format("/api/v1/comunicacoes/%s/processo/%s",
				dto.getIdProcessoParteExpediente(), numeroProcesso);
		String url = PjeEurekaRegister.instance().getURLComunicacaoProcessual(endpoint);
		String token = TribunalService.instance().login();
		
		//Ignorar 400: A comunicação não pode ser atualizada pois não está em curso. Status da comunicação: Ciente
		Consumer<Response> callbackException = RestUtil.newCallbackExceptionLog(url, Method.PATCH, token, dto, newPredicateTestException(400));
		RestUtil.requestAsync(url, dto, JSONObject.class, token, Method.PATCH, callbackException);
	}

	/**
	 * Endpoint
	 * /api/v1/tribunais/{jtr}/processos/{numeroProcesso}/comunicacoes/{numeroComunicacao}.
	 * Registra ciência para a comunicação.
	 * 
	 * @param expediente Expediente com IdProcessoParteExpediente e
	 *                   processoJudicial.numeroProcesso
	 */
	public void registraCiencia(ProcessoParteExpediente expediente) {
			    
		Usuario usuario = getUsuario();
		
		RegistroCienciaDTO dto = new RegistroCienciaDTO();
		dto.setIdProcessoParteExpediente(expediente.getIdProcessoParteExpediente());
		dto.setNumeroProcesso(expediente.getProcessoJudicial().getNumeroProcesso());
		dto.setUsuario(usuario.getLogin());
		dto.setNomeUsuario(usuario.getNome());
		dto.setDataCiencia(expediente.getDtCienciaParte());
		
		registraCiencia(dto);
	}
	
	/**
	 * Endpoint
	 * /api/v1/tribunais/{jtr}/processos/{numeroProcesso}/comunicacoes/{numeroComunicacao}.
	 * Registra ciência para a comunicação.
	 * 
	 * @param dto Objeto com as informações necessárias para o registro da ciência.
	 */
	public void registraCiencia(RegistroCienciaDTO dto) {
		String endpoint = String.format("/api/v1/tribunais/%s/processos/%s/comunicacoes/%s",
				ParametroUtil.instance().recuperarNumeroOrgaoJustica(),
				dto.getNumeroProcesso(), 
				dto.getIdProcessoParteExpediente());
		String url = PjeEurekaRegister.instance().getURLComunicacaoProcessual(endpoint);
		String token = TribunalService.instance().login();
		
		// Ignorar 422 = A comunicação do processo X e numero da comunicação X já foi aceita.
		Consumer<Response> callbackException = RestUtil.newCallbackExceptionLog(url, Method.PUT, token, dto, newPredicateTestException(422));
		RestUtil.requestAsync(url, dto, JSONObject.class, token, Method.PUT, callbackException);
	}

	/**
	 * Endpoint /api/v1/processos/{numeroProcesso}. Altera os representantes de um
	 * processo.
	 * 
	 * @param processo ProcessoTrf
	 */
	public void alterarRepresentantes(ProcessoTrf processo, List<ProcessoParteRepresentante> representantes) {

		RepresentantesDTO dto = new RepresentantesDTO(representantes);
		dto.setNumeroProcesso(processo.getNumeroProcesso());
		alterarRepresentantes(dto);
	}
	
	/**
	 * Endpoint /api/v1/processos/{numeroProcesso}. Altera os representantes de um
	 * processo.
	 * 
	 * @param dto Dados com as informações necessárias para a alteração dos representantes.
	 */
	public void alterarRepresentantes(RepresentantesDTO dto) {
		String endpoint = String.format("/api/v1/processos/%s", dto.getNumeroProcesso());
		String url = PjeEurekaRegister.instance().getURLComunicacaoProcessual(endpoint);
		String token = TribunalService.instance().login();

		RestUtil.patchAsync(url, dto, JSONObject.class, token);
	}

	/**
	 * Endpoint
	 * /api/v1/tribunais/{jtr}/processos/{numeroProcesso}/comunicacoes/{numeroComunicacao}.
	 * Cancela uma comunicação enviada anteriormente.
	 *
	 * @param processoParteExpediente Objeto com as informações necessárias para o registro da ciência.
	 */
	public void cancelarExpediente(ProcessoParteExpediente processoParteExpediente) {
		String numeroProcesso = StringUtil.limparCharsNaoNumericos(processoParteExpediente.getProcessoJudicial().getNumeroProcesso());

		String endpoint = String.format("/api/v1/tribunais/%s/processos/%s/comunicacoes/%s",
				ParametroUtil.instance().recuperarNumeroOrgaoJustica(),
				numeroProcesso,
				processoParteExpediente.getIdProcessoParteExpediente());

		String url = PjeEurekaRegister.instance().getURLComunicacaoProcessual(endpoint);
		String token = TribunalService.instance().login();

		// Ignorar 422 = O cancelamento já foi realizado.
		Consumer<Response> callbackException = RestUtil.newCallbackExceptionLog(url, Method.DELETE, token, null, newPredicateTestException(422));
		RestUtil.requestAsync(url, null, JSONObject.class, token, Method.DELETE, callbackException);
	}
	
	/**
	 * @param status
	 * @return True se a requisição tiver retornando um erro negocial. (400 ou 420)
	 */
	public boolean isStatusErroNegocial(Integer status) {
		return (status == 400 || status == 420);
	}
	
	/**
	 * @param status
	 * @return True se a requisição tiver retornando um erro de sistema.
	 */
	public boolean isStatusErroSistema(Integer status) {
		return (!HttpUtil.isStatus2xx(status) && !isStatusErroNegocial(status));
	}

	/**
	 * Retorna true se a integrao com o Domicílio Eletrônico estiver habilitada.
	 * 
	 * @return Boleano
	 */
	public boolean isIntegracaoHabilitada() {
		return ParametroUtil.instance().isDomicilioEletronicoHabilitado();
	}

	/**
	 * Retorna true se a busca por pessoa no Domicílio Eletrônico estiver habilitada.
	 * 
	 * @return Boleano
	 */
	public boolean isDomicilioEletronicoCacheConsultaOnlineHabilitada() {
		return ParametroUtil.instance().isDomicilioEletronicoCacheConsultaOnlineHabilitada();
	}

	/**
	 * Retorna true se o Domicílio Eletrônico estiver online. Seguem as verificações
	 * efetuadas. 1) URL do Domicílio existe? 2) URL da Comunicação Processual
	 * existe?
	 * 
	 * @return Boleano
	 */
	public boolean isOnline() {
		Boolean isOnline = Boolean.FALSE;

		PjeEurekaRegister eureka = PjeEurekaRegister.instance();
		String urlDomicilioEletronico = eureka.getURLDomicilioEletronico(
				ParametroUtil.instance().getDomicilioEletronicoServiceOnlineCheck());
		String urlComunicacaoProcessual = eureka.getURLComunicacaoProcessual(
				ParametroUtil.instance().getDomicilioEletronicoComunicacaoProcessualServiceOnlineCheck());

		try {
			isOnline = UrlUtil.isHttpOK(urlDomicilioEletronico) && UrlUtil.isHttpOK(urlComunicacaoProcessual);
		} catch (Exception e) {
			isOnline = Boolean.FALSE;
		}
		return isOnline;
	}
	
	/**
	 * Retorna o usuário logado. Se não tiver usuário logado então será efetuado o login do usuário 'Domicílio'.
	 * 
	 * @return Usuário logado.
	 */
	protected Usuario getUsuario() {
		Usuario usuario = Authenticator.getUsuarioLogado();
		if (usuario == null) {
			usuario = Authenticator.instance().recuperaUsuarioDomicilio();
		}
		return usuario;
	}
	
	/**
	 * Predicate que testa se o Response retornou um erro de sistema.
	 * @param codeIgnores
	 * @return Predicate que testa o response.
	 */
	protected Predicate<io.restassured.response.Response> newPredicateTestException(int... codeIgnores) {
		return response -> {
			Integer code = response.getStatusCode();
			return !ArrayUtils.contains(codeIgnores, code) && isStatusErroSistema(code);
		};
	}
	
	public List<String> consultarNomesLotesPessoas() {
		String url = PjeEurekaRegister.instance().getURLDomicilioEletronico("/api/v1/objetos/");
		LotePessoasDTO dto = HttpUtil.getEntityFromGET(url, TribunalService.instance().login(), LotePessoasDTO.class);
		
		return (dto != null ? dto.getData().getChaves() : null);
	}

	public String obterLinkDownloadLotePessoas(String chave) {
		String url = PjeEurekaRegister.instance().getURLDomicilioEletronico("/api/v1/objetos/" + chave);

		DownloadLotePessoasDTO dto = HttpUtil.getEntityFromGET(url, TribunalService.instance().login(), DownloadLotePessoasDTO.class);
		
		return (dto != null ? dto.getData() : null);
	}

	public File downloadArquivo(String nomeArquivo, String url) {
		File file = HttpUtil.getFileFromGET(url, TribunalService.instance().login(), nomeArquivo);
		
		if (file == null) {
			throw new AplicationException("Não foi possível consultar o arquivo: " + nomeArquivo);
		}
		return file;
	}
}
