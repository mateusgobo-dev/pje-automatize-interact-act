package br.com.infox.pje.processor;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
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
import br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client.ColetorEventoProcessoVara;
import br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client.ColetorEventoProcessoVaraService;
import br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client.NoSuchAlgorithmException_Exception;
import br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client.UnsupportedEncodingException_Exception;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.EstatisticaEventoProcesso;
import br.jus.pje.nucleo.entidades.HistoricoEstatisticaEventoProcesso;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;

@Name(VerificaSecaoEnvioProcessoEstatisticaProcessor.NAME)
@AutoCreate
public class VerificaSecaoEnvioProcessoEstatisticaProcessor {

	public static final String NAME = "verificaSecaoEnvioProcessoEstatisticaProcessor";
	private static final String WSDL_COLETOR_EVENTO = "ColetorEventoProcessoVara?wsdl";

	private EntityManager em = EntityUtil.getEntityManager();
	
	@In
	private LogService logService;

	@Logger
	private Log log;

	@Asynchronous
	@Transactional
	public QuartzTriggerHandle buscaEventosProcessos(@IntervalCron String cron) {
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			executarBuscaEventosProcessos();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "buscaEventosProcessos");
		}
		return null;
	}

	// Foi criado para poder ser executado a qualquer momento
	public void executarBuscaEventosProcessos() {
		for (SecaoJudiciaria secaoJudiciaria : listaSecaoJudiciariaTRF()) {
			try {
				if (secaoJudiciaria.getUrlAplicacao() != null) {
					verificaSecao(secaoJudiciaria);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Método que faz a consulta e grava ao banco os eventos que as varas enviam
	 * ao TRF
	 * 
	 * @param secaoJudiciaria
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException_Exception
	 * @throws NoSuchAlgorithmException_Exception
	 * @throws NoSuchAlgorithmException_Exception
	 * @throws UnsupportedEncodingException_Exception
	 */
	private void verificaSecao(SecaoJudiciaria secaoJudiciaria) throws MalformedURLException,
			NoSuchAlgorithmException_Exception, UnsupportedEncodingException_Exception {
		URL urlServidor = new URL(secaoJudiciaria.getUrlAplicacao().concat(WSDL_COLETOR_EVENTO));
		ColetorEventoProcessoVaraService instanciaServidor = new ColetorEventoProcessoVaraService(urlServidor);
		ColetorEventoProcessoVara varaPort = instanciaServidor.getColetorEventoProcessoVaraPort();
		List<br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client.EstatisticaEventoProcessoBean> estatisticaEventoProcessoBeanList = varaPort
				.obterEventosProcessos();
		for (br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client.EstatisticaEventoProcessoBean o : estatisticaEventoProcessoBeanList) {
			EstatisticaEventoProcesso estatisticaEventoProcesso = new EstatisticaEventoProcesso();
			estatisticaEventoProcesso.setClasseJudicial(o.getClasseJudicial());
			estatisticaEventoProcesso.setCodEstado(o.getCodEstado());
			estatisticaEventoProcesso.setCompetencia(o.getCompetencia());
			estatisticaEventoProcesso.setDataInclusao(o.getDataInclusao().toGregorianCalendar().getTime());
			estatisticaEventoProcesso.setJurisdicao(o.getJurisdicao());
			estatisticaEventoProcesso.setOrgaoJulgador(o.getOrgaoJulgador());
			estatisticaEventoProcesso.setNumeroProcesso(o.getNumeroProcessoTrf());
			estatisticaEventoProcesso.setCodEvento(o.getCodEvento());
			estatisticaEventoProcesso.setDocumentoApelacao(o.isDocumentoApelacao());
			estatisticaEventoProcesso.setDocumentoSentenca(o.isDocumentoSentenca());
			estatisticaEventoProcesso.setEnviadoTrf(true);
			estatisticaEventoProcesso.setIdProcessoTrf(o.getIdProcessoTrf());
			em.persist(estatisticaEventoProcesso);
		}

		// gera histórico de atualização
		atualizaHistoricoEventoProcessoVara(secaoJudiciaria);

		em.flush();
	}

	/**
	 * Atualiza o histórico da última conexão com a vara
	 * 
	 * @param secaoJudiciaria
	 */
	@SuppressWarnings("unchecked")
	private void atualizaHistoricoEventoProcessoVara(SecaoJudiciaria secaoJudiciaria) {
		List<HistoricoEstatisticaEventoProcesso> listHist = EntityUtil
				.getEntityManager()
				.createQuery(
						"select o from HistoricoEstatisticaEventoProcesso o where o.secaoJudiciaria.cdSecaoJudiciaria = :cod")
				.setParameter("cod", secaoJudiciaria.getCdSecaoJudiciaria()).getResultList();
		if (listHist.size() == 0) {
			// caso não exista histórico para a seção será criada o primeiro
			// histórico
			HistoricoEstatisticaEventoProcesso o = new HistoricoEstatisticaEventoProcesso();
			o.setDtUltimaAtualizacao(new Date());
			o.setSecaoJudiciaria(secaoJudiciaria);
			em.persist(o);
		} else {
			if (listHist.size() == 1) {
				// se existir histórico para a seção é atualizado o existente
				HistoricoEstatisticaEventoProcesso o = listHist.get(0);
				o.setDtUltimaAtualizacao(new Date());
				em.merge(o);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<SecaoJudiciaria> listaSecaoJudiciariaTRF() {
		return EntityUtil
				.getEntityManager()
				.createQuery(
						"select o from SecaoJudiciaria o where o not in (select a.secaoJudiciaria from HistoricoEstatisticaEventoProcesso a where to_char(a.dtUltimaAtualizacao, 'yyyy-MM-dd') = :dataHoje)")
				.setParameter("dataHoje", new SimpleDateFormat("yyyy-MM-dd").format(new Date())).getResultList();
	}

	public static VerificaSecaoEnvioProcessoEstatisticaProcessor instance() {
		return (VerificaSecaoEnvioProcessoEstatisticaProcessor) Component.getInstance(NAME);
	}

}
