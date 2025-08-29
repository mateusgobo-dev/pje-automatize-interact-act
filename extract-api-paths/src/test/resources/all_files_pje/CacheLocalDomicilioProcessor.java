package br.com.infox.pje.processor;

import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.LogService;
import br.com.itx.component.Util;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.CacheLocalDomicilioService;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.PessoaDomicilioEletronicoDtoBuilder;
import br.jus.pje.nucleo.dto.domicilioeletronico.PessoaDomicilioEletronicoDTO;
import br.jus.pje.nucleo.entidades.LotePessoasDomicilioEletronico;

@Name(CacheLocalDomicilioProcessor.NAME)
@AutoCreate
public class CacheLocalDomicilioProcessor {
	public static final String NAME = "cachePessoaDomicilioProcessor";
	@Logger
	private Log log;

	@In
	private LogService logService;
	@In(create = true)
	private CacheLocalDomicilioService cacheLocalDomicilioService;

	@Asynchronous
	@Transactional
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		try {
			if (isJobHabilitado()) {
				processarCacheLocalDomicilio();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logService.enviarLogPorEmail(log, e, this.getClass(), NAME);
		} finally {
			log.info("Fim de atualização.");
		}
		return null;
	}

	private boolean isJobHabilitado() {
		return DomicilioEletronicoService.instance().isIntegracaoHabilitada()
				&& cacheLocalDomicilioService.isCacheLocalHabilitado();
	}

	private void processarCacheLocalDomicilio() throws PJeBusinessException {
		List<String> listaArquivos = cacheLocalDomicilioService.consultarNomesDeLotesPendentes();
		int qtdArquivos = 0;
		if (CollectionUtilsPje.isEmpty(listaArquivos)) {
			log.info("Não foram encontrados novos arquivos para processamento do cache do domicílio.");
		}
		for (String nomeArquivo : listaArquivos) {
			log.info(String.format("Processando arquivo [%s/%s]: %s ", ++qtdArquivos, listaArquivos.size(),
					nomeArquivo));
			try {
				if (!cacheLocalDomicilioService.isLoteJaProcessado(nomeArquivo)) {
					processarLote(nomeArquivo);
				}
			} catch (Exception e) {
				Util.rollbackAndOpenJoinTransaction();
				log.error(e);
			}
		}
	}

	private void processarLote(String nomeArquivo) {
		String url = cacheLocalDomicilioService.obterLinkDownloadLote(nomeArquivo);
		Util.beginAndJoinTransaction();
		log.info("Realizando download do arquivo: " + nomeArquivo);
		List<String> linhasArquivo = cacheLocalDomicilioService.readLinesFromDownloadFile(nomeArquivo, url);
		List<PessoaDomicilioEletronicoDTO> dtos = PessoaDomicilioEletronicoDtoBuilder.create(linhasArquivo);
		salvarLote(dtos, nomeArquivo);
		Util.commitAndOpenJoinTransaction();
	}

	private void salvarLote(List<PessoaDomicilioEletronicoDTO> dtos, String nomeArquivo) {
		try {
			Util.beginAndJoinTransaction();
			LotePessoasDomicilioEletronico lote = cacheLocalDomicilioService.registrarLote(nomeArquivo);
			cacheLocalDomicilioService.atualizarCachePessoas(dtos, lote);
			Util.commitAndOpenJoinTransaction();
			log.info("Cache atualizado");
		} catch (Exception e) {
			log.error("Erro ao atualizar cache de pessoas do domicílio eletrônico.", e);
			Util.rollbackAndOpenJoinTransaction();
		}
	}

}
