package br.jus.cnj.pje.nucleo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PessoaInvalidaException;
import br.jus.cnj.pje.nucleo.PessoaNaoEncontradaCacheException;
import br.jus.cnj.pje.nucleo.manager.LotePessoasDomicilioEletronicoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDomicilioEletronicoManager;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.DomicilioEletronicoRestClient;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.PessoaDomicilioEletronicoMapper;
import br.jus.pje.nucleo.dto.domicilioeletronico.PessoaDomicilioEletronicoDTO;
import br.jus.pje.nucleo.entidades.LotePessoasDomicilioEletronico;
import br.jus.pje.nucleo.entidades.PessoaDomicilioEletronico;
import br.jus.pje.nucleo.util.StringUtil;

@Name(CacheLocalDomicilioService.NAME)
@Transactional
public class CacheLocalDomicilioService {
	public static final String NAME = "cacheLocalDomicilioService";
	private static final String NOME_ARQUIVO_LOTE_AVULSO = "LOTE-AVULSO-PESSOAS-DOMICILIO";
	private static final String NOME_ARQUIVO_LOTE_PESSOAS_INEXISTENTES = "LOTE-PESSOAS-INEXISTENTES-DOMICILIO";
	private static final String PARAMETRO_DOMICILIO_ELETRONICO_CACHE_LOCAL_HABILITADO = "pdpj:integracao:DomicilioEletronico:cacheLocal:habilitado";
	private static final String PARAMETRO_DOMICILIO_ELETRONICO_CACHE_SALVAR_INEXISTENTES = "pdpj:integracao:DomicilioEletronico:cacheLocal:salvarInexistentes";
	private static final String PARAMETRO_DOMICILIO_ELETRONICO_CACHE_EXPIRAR_INEXISTENTES = "pdpj:integracao:DomicilioEletronico:cacheLocal:expirarInexistentes";
	private static final String PARAMETRO_DOMICILIO_ELETRONICO_CACHE_EXPIRAR_EXISTENTES = "pdpj:integracao:DomicilioEletronico:cacheLocal:expirarExistentes";
	private static final String PARAMETRO_DOMICILIO_ELETRONICO_CACHE_TEMPO_EXPIRACAO = "pdpj:integracao:DomicilioEletronico:cacheLocal:tempoExpiracao";
	private static final long TEMPO_PADRAO_EXPIRACAO_CACHE_MS = 3600000;  // 1 hora em milissegundos

	@Logger
	private Log log;
	@In(create = true)
	private DomicilioEletronicoRestClient domicilioEletronicoRestClient;
	@In(create = true)
	private PessoaDomicilioEletronicoManager pessoaDomicilioEletronicoManager;
	@In(create = true)
	private LotePessoasDomicilioEletronicoManager lotePessoasDomicilioEletronicoManager;

	@In(scope = ScopeType.CONVERSATION, required = false)
	@Out(scope = ScopeType.CONVERSATION, required = false)
	private Map<String, PessoaDomicilioEletronico> cacheTemporario = new HashMap<>();

	public List<String> consultarNomesDeLotesPendentes() {
		List<String> nomesArquivos = domicilioEletronicoRestClient.consultarNomesLotesPessoas();
		List<String> nomesArquivosPendentes = filtrarArquivosPendentes(nomesArquivos);
		Collections.sort(nomesArquivosPendentes);
		return nomesArquivosPendentes;
	}

	private List<String> filtrarArquivosPendentes(List<String> nomesArquivos) {
		List<String> nomesArquivosProcessados = lotePessoasDomicilioEletronicoManager.findAllNomesLotesProcessados();
		nomesArquivos.removeIf(nomesArquivosProcessados::contains);
		return nomesArquivos;
	}

	public String obterLinkDownloadLote(String chave) {
		return domicilioEletronicoRestClient.obterLinkDownloadLotePessoas(chave);
	}

	public List<String> readLinesFromDownloadFile(String nomeArquivo, String url) {
		try {
			File arquivo = domicilioEletronicoRestClient.downloadArquivo(nomeArquivo, url);
			Path path = arquivo.toPath();
			List<String> lines = Files.readAllLines(path);
			Files.delete(path);
			return lines;

		} catch (IOException e) {
			throw new AplicationException(e);
		}
	}

	public void atualizarCachePessoas(List<PessoaDomicilioEletronicoDTO> dtos, LotePessoasDomicilioEletronico lote)
			throws PJeBusinessException, PessoaInvalidaException {
		for (PessoaDomicilioEletronicoDTO dto : dtos) {
			atualizarCacheSegundoNivel(dto, lote, false);
		}
		this.pessoaDomicilioEletronicoManager.flush();
	}

	private boolean isCacheDivergente(PessoaDomicilioEletronicoDTO dto, PessoaDomicilioEletronico pessoa) {
		return pessoa.isPessoaJuridicaDireitoPublico() != dto.isPessoaJuridicaDireitoPublico()
				|| !pessoa.getTipoDocumento().equals(dto.getTipoDocumento())
				|| pessoa.isHabilitado() != dto.isHabilitado()
				|| isCacheExpirado(pessoa);
	}

	public LotePessoasDomicilioEletronico registrarLote(String nomeArquivo) {
		LotePessoasDomicilioEletronico registro = new LotePessoasDomicilioEletronico();
		registro.setDataProcessamento(new Date());
		registro.setNomeArquivo(nomeArquivo);
		try {
			this.lotePessoasDomicilioEletronicoManager.persistAndFlush(registro);
		} catch (PJeBusinessException e) {
			log.error("Erro ao criar lote de pessoas inexistentes.", e);
		}
		return registro;
	}

	/**
	 * Verifica se o cache está expirado. Caso a pessoa habilitada ultrapasse um
	 * período no cache, será buscada novamente. É possível habilitar ou desabilitar
	 * essa funcionalidade através dos parâmetros utilizados no método.
	 * 
	 * @param pde
	 * @return true, se a expiração de cache estiver ativa e o cache da pessoa
	 *         estiver expirado; false, caso contrário.
	 */
	private boolean isCacheExpirado(PessoaDomicilioEletronico pde) {
		return (isCacheExpirarExistentes(pde) || isExpirarInexistentes(pde)) && isDataAtualizacaoExpirada(pde);
	}

	private boolean isCacheExpirarExistentes(PessoaDomicilioEletronico pde) {
		return ParametroUtil.getParametroBoolean(PARAMETRO_DOMICILIO_ELETRONICO_CACHE_EXPIRAR_EXISTENTES)
				&& pde.getLote() != null
				&& !pde.getLote().getNomeArquivo().equals(getLoteInexistente().getNomeArquivo());
	}

	private boolean isExpirarInexistentes(PessoaDomicilioEletronico pde) {
		return ParametroUtil.getParametroBoolean(PARAMETRO_DOMICILIO_ELETRONICO_CACHE_EXPIRAR_INEXISTENTES)
				&& pde.getLote() != null
				&& pde.getLote().getNomeArquivo().equals(getLoteInexistente().getNomeArquivo());
	}

	private boolean isDataAtualizacaoExpirada(PessoaDomicilioEletronico pde) {
		return pde.getDataAtualizacao().getTime() < new Date().getTime() - getTempoExpiracaoCache();
	}

	public long getTempoExpiracaoCache() {
		try {
			return Long.parseLong(ParametroUtil.getParametro(PARAMETRO_DOMICILIO_ELETRONICO_CACHE_TEMPO_EXPIRACAO));
		} catch (NumberFormatException e) {
			return TEMPO_PADRAO_EXPIRACAO_CACHE_MS;
		}
	}

	@SuppressWarnings("unused")
	private PessoaDomicilioEletronico buscarPessoaHabilitadaNoCache(String documento, IBuscaCacheLocalPessoaDomicilio cache) throws PessoaNaoEncontradaCacheException, PessoaInvalidaException {
		if (isDocumentoValido(documento)) {
			PessoaDomicilioEletronico pde = cache.buscarDocumento(getDocumentoFormatado(documento));
			if (pde != null && !isCacheExpirado(pde)) {
				return pde;
			}
			throw new PessoaNaoEncontradaCacheException("Pessoa não encontrada no cache de segundo nível.");
		}
		throw new PessoaInvalidaException("Pessoa não encontrada no cache de primeiro nível devido ao documento inválido passado.");
	}

	private String getDocumentoFormatado(String documento) {
		return InscricaoMFUtil.acrescentaMascaraMF(documento);
	}

	public static CacheLocalDomicilioService instance() {
		return ComponentUtil.getComponent(CacheLocalDomicilioService.class);
	}

	public boolean isLoteJaProcessado(String nomeArquivo) {
		return this.lotePessoasDomicilioEletronicoManager.isLoteJaProcessado(nomeArquivo);
	}

	/**
	 * Retorna o mapa (CPF/CNPJ e isHabilitado) das pessoas habilitadas no Domicílio
	 * Eletrônico.
	 * 
	 * @return cachePessoaHabilitada
	 */
	protected Map<String, PessoaDomicilioEletronico> getCacheTemporarioPrimeiroNivel() {
		if (cacheTemporario == null) {
			cacheTemporario = new HashMap<>();
		}
		return cacheTemporario;
	}

	public boolean isCacheLocalHabilitado() {
		return ParametroUtil.getParametroBoolean(PARAMETRO_DOMICILIO_ELETRONICO_CACHE_LOCAL_HABILITADO);
	}

	public PessoaDomicilioEletronico atualizarCachePrimeiroNivel(PessoaDomicilioEletronicoDTO dto, LotePessoasDomicilioEletronico lote) throws PessoaInvalidaException {
		if (dto != null && isDocumentoValido(dto.getDocumento())) {
			PessoaDomicilioEletronico pessoa = PessoaDomicilioEletronicoMapper.readValue(dto, lote);
			atualizarCachePrimeiroNivel(pessoa);
			return pessoa;
		}

		throw new PessoaInvalidaException("Uma pessoa inválida foi passada como parâmetro (documento: {0}) e não foi possível encontrá-la.");
	}

	private void atualizarCachePrimeiroNivel(PessoaDomicilioEletronico pessoa) {
		if (pessoa != null && isDocumentoValido(pessoa.getNumeroDocumento())) {
			getCacheTemporarioPrimeiroNivel().put(pessoa.getNumeroDocumento(), pessoa);
		}
	}

	public PessoaDomicilioEletronico atualizarCacheSegundoNivel(PessoaDomicilioEletronicoDTO dto, LotePessoasDomicilioEletronico lote, boolean flush) throws PessoaInvalidaException, PJeBusinessException {
		if (dto != null && isDocumentoValido(dto.getDocumento())) {
			PessoaDomicilioEletronico pessoa = this.pessoaDomicilioEletronicoManager
					.findByNumeroDocumento(dto.getDocumento());
	
			if (pessoa == null || isCacheDivergente(dto, pessoa)) {
				pessoa = PessoaDomicilioEletronicoMapper.readValue(dto, pessoa, lote != null ? lote : getLoteAvulso());
	
				if (pessoa.getId() != null) { // Atualizar cache divergente
					pessoa = this.pessoaDomicilioEletronicoManager.merge(pessoa);
				} else { // Inserir pessoa que não existia
					pessoa = this.pessoaDomicilioEletronicoManager.persist(pessoa);
				}

				if (flush) {
					this.pessoaDomicilioEletronicoManager.flush();
				}
			}
			return pessoa;
		}
		throw new PessoaInvalidaException("Uma pessoa inválida foi passada como parâmetro (documento: {0}) e não foi possível salvá-la.");
	}

	public PessoaDomicilioEletronico atualizarCacheSegundoNivel(PessoaDomicilioEletronicoDTO dto, LotePessoasDomicilioEletronico lote) throws PJeBusinessException, PessoaInvalidaException {
		return atualizarCacheSegundoNivel(dto, lote, true);
	}

	private LotePessoasDomicilioEletronico getLoteAvulso() {
		LotePessoasDomicilioEletronico lote = 
				lotePessoasDomicilioEletronicoManager.findLote(NOME_ARQUIVO_LOTE_AVULSO);
		if(lote == null) {
			lote = registrarLote(NOME_ARQUIVO_LOTE_AVULSO);
		}
		return lote;
	}

	private LotePessoasDomicilioEletronico getLoteInexistente() {
		LotePessoasDomicilioEletronico lote = 
				lotePessoasDomicilioEletronicoManager.findLote(NOME_ARQUIVO_LOTE_PESSOAS_INEXISTENTES);
		if(lote == null) {
			lote = registrarLote(NOME_ARQUIVO_LOTE_PESSOAS_INEXISTENTES);
		}
		return lote;
	}

	/**
	 * Busca uma pessoa no cache local (primeiro e segundo níveis) do Domicílio Eletrônico.
	 *
	 * @param documento {@link java.lang.String} representando o documento (CPF/CNPJ) a ser buscado.
	 * @return {@link PessoaDomicilioEletronico} encontrada.
	 * @throws PessoaNaoEncontradaCacheException Se não for encontrada no cache.
	 * @throws PessoaInvalidaException           Se o documento for considerado inválido.
	 */
	public PessoaDomicilioEletronico buscarNoCache(String documento)
			throws PessoaNaoEncontradaCacheException, PessoaInvalidaException {

		if (!isDocumentoValido(documento)) {
			throw new PessoaInvalidaException("Pessoa não encontrada no cache devido ao documento inválido passado. O documento informado é inválido: "
							+ documento);
			}

		PessoaDomicilioEletronico pessoaCache1 = getCacheTemporarioPrimeiroNivel().get(documento);

		if (pessoaCache1 != null && isCacheExpirado(pessoaCache1) == false) {
			return pessoaCache1;
		} else {
			log.warn("Pessoa não encontrada ou expirada no cache de primeiro nível, para o documento: " + documento);
		}

		PessoaDomicilioEletronico pessoaCache2 = pessoaDomicilioEletronicoManager.findByNumeroDocumento(documento);

		if (pessoaCache2 != null && isCacheExpirado(pessoaCache2) == false) {
			atualizarCachePrimeiroNivel(pessoaCache2);
			return pessoaCache2;
		}

		throw new PessoaNaoEncontradaCacheException("Pessoa não encontrada ou expirada no cache de segundo nível, para o documento: " + documento);
	}

	public boolean isDocumentoValido(String documento) {
		if (StringUtil.isNotEmpty(documento)) {
			String documentoSemMascara = InscricaoMFUtil.retiraMascara(documento);
			return InscricaoMFUtil.isCnpjValido(documentoSemMascara) || InscricaoMFUtil.isCpfValido(documentoSemMascara);
		}
		return false;
	}

	public PessoaDomicilioEletronico salvarNoCache(PessoaDomicilioEletronicoDTO dto) throws PessoaInvalidaException {
		return salvarNoCache(dto, null);
	}

	public PessoaDomicilioEletronico salvarNoCache(PessoaDomicilioEletronicoDTO dto, LotePessoasDomicilioEletronico lote) throws PessoaInvalidaException {
		PessoaDomicilioEletronico pessoa = atualizarCachePrimeiroNivel(dto, lote);

		if (isCacheLocalHabilitado()) {
			try {
				pessoa = atualizarCacheSegundoNivel(dto, lote);
			} catch (PJeBusinessException e) {
				log.error("[DomicioEletronicoService.salvarNoCache] Ocorreu um erro ao salvar PessoaDomicilioEletronico no cache de segundo nível para a pessoa {0}.", dto, e);
			}
		}

		return pessoa;
	}

	public void salvarInexistenteNoCache(String documento) throws PessoaInvalidaException {
		if (isCacheSalvarInexistentes()) {
			PessoaDomicilioEletronicoDTO dto = new PessoaDomicilioEletronicoDTO(documento, InscricaoMFUtil.getTipoDocumentoCNPJouCPF(documento), false, false);
			salvarNoCache(dto, getLoteInexistente());
		}
	}

	private boolean isCacheSalvarInexistentes() {
		return ParametroUtil.getParametroBoolean(PARAMETRO_DOMICILIO_ELETRONICO_CACHE_SALVAR_INEXISTENTES);
	}

}
