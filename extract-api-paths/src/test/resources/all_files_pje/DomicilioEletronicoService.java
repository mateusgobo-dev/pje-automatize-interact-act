package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.PessoaInvalidaException;
import br.jus.cnj.pje.nucleo.PessoaNaoEncontradaCacheException;
import br.jus.cnj.pje.nucleo.PjeDomicilioBuscaApiDesativadaException;
import br.jus.cnj.pje.nucleo.PjeDomicilioOfflineException;
import br.jus.cnj.pje.nucleo.PjeDomicilioPessoaNaoEncontradaException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.DomicilioEletronicoRestClient;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDomicilioEletronico;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Camada de servio para integrao com o Domicílio Eletrnico. A integrao com o
 * Domicílio Eletrnico habilitada atravs do parmetro
 * Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO.<br/>
 * 
 * @author Adriano Pamplona
 */
@Name(DomicilioEletronicoService.NAME)
@Transactional
public class DomicilioEletronicoService extends BaseService implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "domicilioEletronicoService";
	
	@Logger
	private Log log;
	
	public  static final int QTD_DIAS_GRACA_PJ_DIREITO_PUBLICO = 10;
	public static final int QTD_DIAS_GRACA_PF_PJ_DIR_PRIVADO = 3;
	public static final  int QTD_DIAS_INTIMACAO =10;
	public static final int DIAS_ACRESCIMO_CITACAO_DOMICILIO = 5;
	public static  final String COMUNICACAO_ABERTA = "ComunicacaoAberta";
	public static final String CITACAO_EXPIRADA = "CitacaoExpirada";

    @In(create = true)
    private transient PrazosProcessuaisService  prazosProcessuaisService;

	/**
	 * @return Instncia da classe.
	 */
	public static DomicilioEletronicoService instance() {
		return ComponentUtil.getComponent(DomicilioEletronicoService.class);
	}

	/**
	 * Efetua o login no Domicílio Eletrônico do tipo 'client-credentials'.
	 * 
	 * @return Token.
	 */
	public String login() {
		return DomicilioEletronicoRestClient.instance().login();
	}

	/**
	 * Envia os expedientes para o Domicílio.
	 * 
	 * @param expedientes List<ProcessoExpediente>
	 * @throws PJeBusinessException 
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	public void enviarExpedientes(List<ProcessoExpediente> expedientes)
			throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {

		if (CollectionUtils.isEmpty(expedientes)) {
			return;
		}

		for (ProcessoExpediente pe : expedientes) {
			boolean isExisteBloqueioParaCompetencia = verificaBloqueioPorCompetencia(pe.getProcessoTrf());

			if (!isExisteBloqueioParaCompetencia) {
				boolean isMandado       = (pe.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() == 144);
				boolean isSentenca      = (pe.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() == 62);
				boolean isJEC           = (pe.getProcessoTrf().getCompetencia().getIdCompetencia() == 13);
				boolean isSentencaJEC   = isJEC && isSentenca;

	            // Só processa se não for Mandado e não for Sentença de JEC
				if (!isMandado && !isSentencaJEC) {
					for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
						boolean isExisteBloqueioEnvio = existeBloqueioEnvioDomicilio(ppe.getPessoaParte());

						if (!isExisteBloqueioEnvio) {
							enviarExpediente(ppe);
						}
					}
				}
			}
		}
	}

	/**
	 * @param processoTrf
	 * @return
	 */
	public boolean verificaBloqueioPorCompetencia(ProcessoTrf processoTrf) {
		String parametroBloqueioAtivo = ComponentUtil.getParametroDAO().valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_BLOQUEIA_COMPETENCIAS).trim();

		boolean isBloqueioAtivo = StringUtils.equalsIgnoreCase(parametroBloqueioAtivo, "true");

		if (isBloqueioAtivo) {
			String paranetroCompetenciasBloqueadas = ComponentUtil.getParametroDAO()
					.valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_COMPETENCIAS_BLOQUEADAS);

			if (paranetroCompetenciasBloqueadas != null && !paranetroCompetenciasBloqueadas.trim().isEmpty()) {

				Pattern pattern = Pattern.compile("\\d+");

				int[] idsCompetenciasBloqueadas = Arrays
						.stream(paranetroCompetenciasBloqueadas.split(","))
						.map(String::trim)
						.filter(s -> pattern.matcher(s).matches())
						.mapToInt(Integer::parseInt)
						.toArray();

				int idCompetencia = processoTrf.getCompetencia().getIdCompetencia();

				return Arrays.stream(idsCompetenciasBloqueadas).anyMatch(id -> id == idCompetencia);
			}
		}
		return false;
	}

	/**
	 * Verifica se existe algum bloqueio de envio ao Domicílio.
	 * @param pessoa
	 * @return
	 */
	public boolean existeBloqueioEnvioDomicilio(Pessoa pessoa) {
		if ((pessoa instanceof PessoaFisica) && verificaBloqueioPessoaFisica())
			return true;
		else if ((pessoa instanceof PessoaJuridica) && (((PessoaJuridica) pessoa).getOrgaoPublico())) {
			PessoaJuridica pessoaJuridica = (PessoaJuridica) pessoa;
			return verificaBloqueioOrgaoPublico() || verificaOrgaoPublicoBloqueado(pessoaJuridica);
		}

		return false;
	}

	/**
	 * Verifica bloqueio de envio de Pessoa Física ao Domicílio.
	 * @return
	 */
	public boolean verificaBloqueioPessoaFisica() {
		String parametroBloqueioAtivo = ComponentUtil.getParametroDAO().valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_BLOQUEIA_PESSOA_FISICA).trim();
		return StringUtils.equalsIgnoreCase(parametroBloqueioAtivo, "true");
	}

	/**
	 * Verifica bloqueio de envio de Órgão Público ao Domicílio.
	 * @return
	 */
	public boolean verificaBloqueioOrgaoPublico() {
		String parametroBloqueioAtivo = ComponentUtil.getParametroDAO().valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_BLOQUEIA_ORGAO_PUBLICO).trim();
		return StringUtils.equalsIgnoreCase(parametroBloqueioAtivo, "true");
	}

	/**
	 * Verifica bloqueio de envio de uma lista de Órgãos Públicos ao Domicílio.
	 * @param pessoaJuridica
	 * @return
	 */
	public boolean verificaOrgaoPublicoBloqueado(PessoaJuridica pessoaJuridica) {
		String parametroOrgaosBloqueados = ComponentUtil.getParametroDAO().valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_ORGAOS_PUBLICOS_BLOQUEADOS);

		if (parametroOrgaosBloqueados != null && !parametroOrgaosBloqueados.trim().isEmpty() && !parametroOrgaosBloqueados.equals("-1")) {
			Pattern pattern = Pattern.compile("\\d+");
			int[] idsOrgaosBloqueados = Arrays.stream(parametroOrgaosBloqueados.split(",")).map(String::trim).filter(s -> pattern.matcher(s).matches()).mapToInt(Integer::parseInt).toArray();

			int idPessoaJuridica = pessoaJuridica.getIdPessoaJuridica();

			return Arrays.stream(idsOrgaosBloqueados).anyMatch(id -> id == idPessoaJuridica);
		}
		return false;
	}

	private boolean isParteExpedienteDomicilio(ProcessoParteExpediente parteExpediente) {
		ProcessoParteExpedienteManager processoParteExpedienteManager = ProcessoParteExpedienteManager.instance();
		return parteExpediente != null
				&& processoParteExpedienteManager.isMeioExpedicaoSistema(parteExpediente)
				&& isPessoaHabilitada(parteExpediente.getPessoaParte());
	}

	/**
	 * Envia os expedientes para o Domicílio de forma assíncrona.
	 * 
	 * @param expedientes List<ProcessoExpediente>
	 */
	public void enviarExpedientesAsync(List<ProcessoExpediente> expedientes) {

		if (CollectionUtils.isNotEmpty(expedientes)) {
			Events.instance().raiseTransactionSuccessEvent(Eventos.EVENTO_DOMICILIO_ELETRONICO_ENVIAR_EXPEDIENTES, expedientes);
		}
	}

	/**
	 * Envia o expediente para o Domicílio.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 * @throws PJeBusinessException 
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	public void enviarExpediente(ProcessoParteExpediente parteExpediente) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		Usuario usuario = Authenticator.getUsuarioLogado();
		if (usuario == null) {
			usuario = Authenticator.getUsuarioSistema();
		}

		String login = usuario.getLogin().trim();

		if (login.matches("\\d+") && login.length() > 11) {
			usuario = Authenticator.getUsuarioSistema();
		}

		enviarExpediente(parteExpediente, usuario);
	}
	
	/**
	 * Envia o expediente para o Domicílio de forma assíncrona.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 */
	public void enviarExpedienteAsync(ProcessoParteExpediente parteExpediente) {
		enviarExpedienteAsync(parteExpediente, Authenticator.getUsuarioLogado());
	}
	
	/**
	 * Envia o expediente para o Domicílio.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 * @param usuario 
	 * @throws PjeRestClientException 
	 * @throws PJeBusinessException 
	 * @throws PessoaInvalidaException 
	 */
	public void enviarExpediente(ProcessoParteExpediente parteExpediente, Usuario usuario) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		if (isIntegracaoHabilitada() && isParteExpedienteDomicilio(parteExpediente)) {
			if (ProcessoExpedienteManager.instance().isCitacao(parteExpediente.getProcessoExpediente())) {
				parteExpediente.setDtPrazoLegal(obterPrazoCiencia(parteExpediente));
			}

			parteExpediente.setIntimacaoPessoal(false);

			boolean isEnviadoComSucesso = DomicilioEletronicoRestClient.instance().enviarExpediente(parteExpediente, usuario);

			if (isEnviadoComSucesso) {
				parteExpediente.setEnviadoDomicilio(Boolean.TRUE);
			}

			ProcessoParteExpedienteManager.instance().mergeAndFlush(parteExpediente);
		}
	}

	/**
	 * Envia o expediente para o Domicílio de forma assíncrona.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 * @param usuario 
	 * @throws PJeBusinessException
	 */
	public void enviarExpedienteAsync(ProcessoParteExpediente parteExpediente, Usuario usuario) {
		if (isIntegracaoHabilitada() && isParteExpedienteDomicilio(parteExpediente)) {
			Events.instance().raiseTransactionSuccessEvent(Eventos.EVENTO_DOMICILIO_ELETRONICO_ENVIAR_EXPEDIENTE, parteExpediente, usuario);
		}
	}

	/**
	 * Altera a data final de ciência para a comunicação.
	 * 
	 * @param expediente Expediente com IdProcessoParteExpediente, dtPrazoLegal e
	 *                   processoJudicial.numeroProcesso
	 * @param dtPrazoLegal Prazo final
	 */
	public void alterarDataFinalCiencia(ProcessoParteExpediente expediente, Date dtPrazoLegal) {

		if (isIntegracaoHabilitada() && expediente != null
				&& ProcessoParteExpedienteManager.instance().isMeioExpedicaoSistema(expediente)
				&& expediente.isEnviadoDomicilio()) {
			DomicilioEletronicoRestClient.instance().alterarDataFinalCiencia(expediente, dtPrazoLegal);
		}
	}

	/**
	 * Registra ciência para a comunicação.
	 * 
	 * @param expediente Expediente com IdProcessoParteExpediente e
	 *                   processoJudicial.numeroProcesso
	 */
	public void registraCiencia(ProcessoParteExpediente expediente) {

		if (isIntegracaoHabilitada() && expediente != null && expediente.isEnviadoDomicilio()) {
			DomicilioEletronicoRestClient.instance().registraCiencia(expediente);
		}
	}

	/**
	 * Altera os representantes de um processo.
	 * 
	 * @param processo ProcessoTrf
	 */
	public void alterarRepresentantesAsync(ProcessoTrf processo) {

		if (isIntegracaoHabilitada() && 
				processo != null && 
				processo.getIdProcessoTrf() > 0 &&
				ProcessoParteRepresentanteManager.instance().isPermitirAtualizacaoDomicilioEletronico(processo)) {
			Events.instance().raiseTransactionSuccessEvent(Eventos.EVENTO_DOMICILIO_ELETRONICO_ALTERAR_REPRESENTANTE, processo);
		}
	}

	/**
	 * Altera os representantes de um processo.
	 * 
	 * @param processo ProcessoTrf
	 */
	public void alterarRepresentantes(ProcessoTrf processo) {

		if (isIntegracaoHabilitada() && processo != null) {
			List<ProcessoParteRepresentante> representantes = ProcessoParteRepresentanteManager.instance().getRepresentantesAtivos(processo);
			alterarRepresentantes(processo, representantes);
		}
	}
	
	/**
	 * Altera os representantes de um processo.
	 * 
	 * @param processo ProcessoTrf
	 * @param representantes Lista de representantes
	 */
	public void alterarRepresentantes(ProcessoTrf processo, List<ProcessoParteRepresentante> representantes) {

		if (isIntegracaoHabilitada() && processo != null && ProjetoUtil.isNotVazio(representantes)) {
			DomicilioEletronicoRestClient.instance().alterarRepresentantes(processo, representantes);
		}
	}
	
	/**
	 * Verifica se a pessoa está habilitada no Domicílio Eletrônico.
	 * 
	 * @param pessoa Pessoa que será validada
	 * @return Booleano
	 */
	public boolean isPessoaHabilitada(Pessoa pessoa) {
		return isPessoaHabilitada((pessoa != null ? pessoa.getDocumentoCpfCnpj() : null));
	}
	
	/**
	 * Verifica se a pessoa está habilitada no Domicílio Eletrônico.
	 * 
	 * @param cnpjCpf Pessoa que será validada
	 * @return Booleano
	 */	
	public boolean isPessoaHabilitada(String cnpjCpf) {
		try {
			return getPessoa(cnpjCpf).isHabilitado();
		} catch (PjeRestClientException | PessoaInvalidaException e) {
			return false;
		}
	}

	/**
	 * Verifica se a pessoa é jurídica de direito público no Domicílio Eletrônico.
	 * 
	 * @param cnpjCpf Pessoa que será validada
	 * @return Booleano
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	public boolean isPessoaJuridicaDeDireitoPublico(Pessoa pessoa) throws PjeRestClientException, PessoaInvalidaException {
		return isPessoaJuridicaDeDireitoPublico((pessoa != null ? pessoa.getDocumentoCpfCnpj() : null));
	}

	/**
	 * Verifica se a pessoa é jurídica de direito público no Domicílio Eletrônico.
	 * 
	 * @param cnpjCpf Pessoa que será validada
	 * @return Booleano
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */	
	public boolean isPessoaJuridicaDeDireitoPublico(String cnpjCpf) throws PjeRestClientException, PessoaInvalidaException {
		return getPessoa(cnpjCpf).isPessoaJuridicaDireitoPublico();
	}

	/**
	 * Busca dos caches de primeiro e segundo nível (se ativo) e, caso não
	 * encontrada, da API do Domicílio Eletrônico a pessoa que possui o documento
	 * informado.
	 * 
	 * @param cnpjCpf documento da pessoa a ser buscada.
	 * @return a pessoa buscada, caso ela exista nos caches ou no Domicílio;
	 *         {@link PjeRestClientException} caso ocorra algum problema (Domicílio
	 *         offline ou caso o Domicílio esteja desativado, por exemplo);
	 *         {@link PessoaInvalidaException} caso o CPF ou CNPJ passado seja
	 *         inválido.
	 * @throws PjeRestClientException
	 * @throws PessoaInvalidaException
	 */
	public PessoaDomicilioEletronico getPessoa(String cnpjCpf) throws PjeRestClientException, PessoaInvalidaException {
		CacheLocalDomicilioService cacheService = CacheLocalDomicilioService.instance();

		try {
			return cacheService.buscarNoCache(cnpjCpf);
		} catch (PessoaNaoEncontradaCacheException e1) {
			/*
			 * Essa verificação de habilitação só é feita após buscar no cache para evitar
			 * que seja impossível recuperar dados de expedientes já enviados ao Domicílio
			 * após desativação da integração. Em tese, se ele foi enviado quando havia
			 * integração, ao desativá-la, a pessoa estará no cache e o VerificadorPeriódico
			 * ainda será capaz de processá-lo mesmo com a integração desativada.
			 */
			if (isIntegracaoHabilitada() && isDomicilioEletronicoCacheLocalConsultaOnlineHabilitada()) {
				if (isOnline()) {
					try {
						return cacheService.salvarNoCache(DomicilioEletronicoRestClient.instance().getPessoa(cnpjCpf));
					} catch (PjeDomicilioPessoaNaoEncontradaException e2) {
						cacheService.salvarInexistenteNoCache(cnpjCpf);
						throw e2;
					}
				}
				throw new PjeDomicilioOfflineException(MessageFormat.format(
						"O Domicílio Eletrônico está fora do ar e a pessoa de documento {0} não foi encontrada no cache (ou o cache está desabilitado).",
						cnpjCpf));
			}
			throw new PjeDomicilioBuscaApiDesativadaException(MessageFormat.format(
					"O Domicílio Eletrônico ou a busca na API estão desabilitados e a pessoa de documento {0} não foi encontrada no cache (ou o cache está desabilitado).",
					cnpjCpf));
		}
	}
	
	private boolean isDomicilioEletronicoCacheLocalConsultaOnlineHabilitada() {
		return DomicilioEletronicoRestClient.instance().isDomicilioEletronicoCacheConsultaOnlineHabilitada();
	}

	/**
	 * Retorna true se a integração com o Domicílio Eletrônico estiver habilitada.
	 * 
	 * @return Boleano
	 */
	public boolean isIntegracaoHabilitada() {
		return DomicilioEletronicoRestClient.instance().isIntegracaoHabilitada();
	}

	/**
	 * Retorna true se o Domicílio Eletrônico estiver online. Seguem as verificações
	 * efetuadas. 1) URL do Domicílio existe? 2) URL da Comunicação Processual
	 * existe?
	 * 
	 * @return Boleano
	 */
	public boolean isOnline() {
		return DomicilioEletronicoRestClient.instance().isOnline();
	}

	/**
	 * Registra a ciência do expediente.
	 * 
	 * @param ppe
	 * @throws PJeBusinessException 
	 */
	public void registrarCienciaPessoal(ProcessoParteExpediente ppe) throws PJeBusinessException {
		if(ppe.getDtCienciaParte() == null ) {
			AtoComunicacaoService.instance().registraCienciaPessoal(ppe, true);
			juntarCertidaoCienciaDomicilio(ppe);
		}
	}
	
	public void juntarCertidaoCienciaDomicilio(ProcessoParteExpediente ppe) throws PJeBusinessException {

		ModeloDocumento modeloCertidao = ParametroUtil.instance().getModeloDocumentoCertidaoCienciaDomicilio();
		TipoProcessoDocumento tipoDocumento = ParametroUtil.instance().getTipoProcessoDocumentoCertidao();
		if (modeloCertidao != null) {
			Integer idPd = DocumentoJudicialService.instance().gerarMinuta(ppe.getIdProcessoJudicial(), null, null,
					tipoDocumento.getIdTipoProcessoDocumento(), modeloCertidao.getIdModeloDocumento());
			DocumentoJudicialService.instance().juntarDocumento(idPd, null);
			MovimentoAutomaticoService.preencherMovimento().deCodigo(60).associarAoDocumentoDeId(idPd)
					.comComplementoDeCodigo(4).doTipoDominio().preencherComElementoDeCodigo(107).lancarMovimento();
		}
	}
	
	/**
	 * Observador para enviar expedientes para o Domicílio.
	 * 
	 * @param expedientes List<ProcessoExpediente>
	 * @throws PJeBusinessException 
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	@Observer(Eventos.EVENTO_DOMICILIO_ELETRONICO_ENVIAR_EXPEDIENTES)
	@Transactional
	public void enviarExpedientesEvento(List<ProcessoExpediente> expedientes) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException  {
		enviarExpedientes(expedientes);
	}

	/**
	 * Observador para enviar expediente para o Domicílio.
	 * 
	 * @param expediente ProcessoParteExpediente
	 * @throws PJeBusinessException 
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	@Observer(Eventos.EVENTO_DOMICILIO_ELETRONICO_ENVIAR_EXPEDIENTE)
	@Transactional
	public void enviarExpedienteEvento(ProcessoParteExpediente parteExpediente, Usuario usuario) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		enviarExpediente(parteExpediente, usuario);
	}
	
	@Observer(value={Eventos.EVENTO_DOMICILIO_ELETRONICO_CITACAO_EXPIRADA})
	@Transactional
	public void criarFluxoCitacaoExpiradaEvento(ProcessoTrf processoJudicial, Integer idProcessoParteExpediente) {
		String cdFluxo = ParametroUtil.getParametro(Parametros.PJE_DOMICILIO_ELETRONICO_FLUXO_CITACAO_EXPIRADA);
		
		if(StringUtil.isNotEmpty(cdFluxo)) {
			Fluxo fluxo = ComponentUtil.getComponent(FluxoManager.class).findByCodigo(cdFluxo);
			boolean existeFluxoTratCitacaoAtivo = ComponentUtil.getComponent(FluxoManager.class).existeProcessoNoFluxoEmExecucao(
					processoJudicial.getIdProcessoTrf(), fluxo.getFluxo());

			if(!existeFluxoTratCitacaoAtivo) {
				Map<String, Object> variaveis = new HashMap<>();
				variaveis.put("pje:evento:objetoRelacionado", idProcessoParteExpediente);
				try {
					ProcessoJudicialService.instance().incluirNovoFluxo(processoJudicial, cdFluxo, variaveis);
				} catch (PJeBusinessException e) {
					this.log.error(e);
				}
			}
		}
	}
	
	@Observer(value={Eventos.EVENTO_DOMICILIO_ELETRONICO_ALTERAR_REPRESENTANTE})
	@Transactional
	public void alterarRepresentantesEvento(ProcessoTrf processoJudicial) {
		alterarRepresentantes(processoJudicial);
	}
	
	/**
	 * Cancela uma lista de expedientes no Domicílio Eletrônico.
	 * 
	 * @param expedientes List<ProcessoParteExpediente>
	 */
	public void cancelarExpediente(List<ProcessoParteExpediente> expedientes) {
		if (CollectionUtils.isNotEmpty(expedientes)) {
			for (ProcessoParteExpediente ppe : expedientes) {
				cancelarExpediente(ppe);
			}
		}
	}
	
	/**
	 * Registra o cancelamento da comunicação no Domicilio Eletrônico.
	 *
	 * @param processoParteExpediente Expediente com IdProcessoParteExpediente e
	 *                   processoJudicial.numeroProcesso
	 */
	public void cancelarExpediente(ProcessoParteExpediente processoParteExpediente) {
		if (processoParteExpediente != null
				&& processoParteExpediente.isEnviadoDomicilio()
				&& processoParteExpediente.getDtCienciaParte() == null
				&& isIntegracaoHabilitada()) {
			DomicilioEletronicoRestClient.instance().cancelarExpediente(processoParteExpediente);
		}
	}

	/**
	 * Atualiza o atributo ProcessoParteExpediente.enviadoDomicilio. O atributo em questão identifica 
	 * se o expediente foi ou não enviado ao Domicílio Eletrônico.
	 * 
	 * @param numeroComunicacao
	 * @param novoValor
	 */
	public void atualizaFlagEnviadoDomicilio(String numeroComunicacao, Boolean novoValor) {
		try {
			ProcessoParteExpedienteManager processoParteExpedienteManager = ProcessoParteExpedienteManager.instance();
			ProcessoParteExpediente processoParteExpediente = processoParteExpedienteManager.findById(Integer.valueOf(numeroComunicacao));

			boolean isMesmoValor = novoValor.equals(processoParteExpediente.isEnviadoDomicilio());

			if (!isMesmoValor) {
				processoParteExpediente.setEnviadoDomicilio(novoValor);
				processoParteExpedienteManager.mergeAndFlush(processoParteExpediente);
			}
		} catch (Exception e) {
			this.log.error(e);
		}
	}

	/**
	 * Cria um fluxo para uma citação fechada.
	 * 
	 * @param ppe ProcessoParteExpediente
	 */
	public void criarFluxoCitacaoExpiradaAsync(ProcessoParteExpediente ppe) {
		if (ppe != null &&
			ppe.getFechado() && 
			ppe.isEnviadoDomicilio() && 
			ProcessoExpedienteManager.instance().isCitacao(ppe.getProcessoExpediente()) && 
			ProcessoParteExpedienteManager.instance().isMeioExpedicaoSistema(ppe)) {
			
			Events.instance().raiseEvent(Eventos.EVENTO_DOMICILIO_ELETRONICO_CITACAO_EXPIRADA, 
					ppe.getProcessoExpediente().getProcessoTrf(), 
					ppe.getIdProcessoParteExpediente());
		}
	}
	
	/**
	 * Calcula o prazo de ciência para os expedientes que forem enviados ao Domicílio Eletrônico.
	 * 
	 * @param ppe ProcessoParteExpediente
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	public Date obterPrazoCiencia(ProcessoParteExpediente parteExpediente) throws PjeRestClientException, PJeBusinessException, PessoaInvalidaException {
		ProcessoExpediente expediente = parteExpediente.getProcessoExpediente();
		Pessoa pessoaDestinatario = parteExpediente.getPessoaParte();		

		boolean isDestinatarioPessoaJuridicaDireitoPublico = isPessoaJuridicaDeDireitoPublico(pessoaDestinatario.getDocumentoCpfCnpj());
		TipoPrazoEnum tipoPrazo = ParametroUtil.getTipoPrazoParametro(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_TIPO_PRAZO, TipoPrazoEnum.D);
		Integer prazoCitacao = (isDestinatarioPessoaJuridicaDireitoPublico ? 
				ParametroUtil.getPrazoParametro(tipoPrazo, Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_CITACAO_PJ_DIREITO_PUBLICO, QTD_DIAS_GRACA_PJ_DIREITO_PUBLICO) : 
					ParametroUtil.getPrazoParametro(tipoPrazo, Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_CITACAO_PF_PJ_DIR_PRIVADO, QTD_DIAS_GRACA_PF_PJ_DIR_PRIVADO));
		Integer prazoIntimacao = ParametroUtil.getPrazoParametro(tipoPrazo, Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_INTIMACAO, QTD_DIAS_INTIMACAO);
		CategoriaPrazoEnum categoriaPrazoCitacao = (isDestinatarioPessoaJuridicaDireitoPublico ? CategoriaPrazoEnum.C : CategoriaPrazoEnum.U);
		CategoriaPrazoEnum categoriaPrazoIntimacao = CategoriaPrazoEnum.C;
		boolean isCitacao = ProcessoExpedienteManager.instance().isCitacao(expediente);
		
		PrazosProcessuaisService service = PrazosProcessuaisServiceImpl.instance();
		Calendario calendario = service.obtemCalendario(expediente.getOrgaoJulgador());
		return service.calculaPrazoProcessual(
				new Date(), 
				(isCitacao ? prazoCitacao : prazoIntimacao), 
				tipoPrazo, 
				calendario,
				(isCitacao ? categoriaPrazoCitacao : categoriaPrazoIntimacao), 
				ContagemPrazoEnum.C
		);
	}

	public boolean isCitacaoEnviadaDomicilioEletronico(ProcessoParteExpediente ppe) {

		return  Objects.nonNull(ppe) && ppe.isEnviadoDomicilio()
				&& ProcessoExpedienteManager.instance().isCitacao(ppe.getProcessoExpediente())
				&& ProcessoParteExpedienteManager.instance().isMeioExpedicaoSistema(ppe);

	}
	public Date calculaReferenciaDataPrazoLegalParaCitacaoEnviadaAoDomicilioEletronico(Calendario calendario, Date dataReferencia) {
		Date dataCalculada = new Date(dataReferencia.getTime());

		for (int i=0;i<DomicilioEletronicoService.DIAS_ACRESCIMO_CITACAO_DOMICILIO;i++) {
			dataCalculada = prazosProcessuaisService.obtemDiaUtilSeguinte(dataCalculada, calendario, false);
		}

		return dataCalculada;
	}	

	/**
	 * Retorna true se é para consultar se o Domicílio está online e exibir mensagem fixa de alerta.
	 *
	 * @return Boleano
	 */
	public boolean isUtilizaAlertaDomicilioOffline() {
       return BooleanUtils.toBoolean(ParametroUtil.getParametro(Parametros.PJE_DOMICILIO_ELETRONICO_UTILIZAR_ALERTA_OFFLINE));
	}

	/**
	 * Retorna o texto da mensagem de alerta sobre possível atraso de envio de expedientes ao Domicílio.
	 *
	 * @return String
	 */
	public String msgAlertaPossivelAtrasoEnvioDomicilio() {
       return ParametroUtil.getParametro(Parametros.PJE_DOMICILIO_ELETRONICO_MSG_POSSIVEL_ATRASO_ENVIO);
	}
}
