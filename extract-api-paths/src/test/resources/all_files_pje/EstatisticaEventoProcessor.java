package br.com.infox.pje.processor;

import java.util.Date;
import java.util.List;

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

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.service.LogService;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.EstatisticaEventoProcesso;
import br.jus.pje.nucleo.entidades.EstatisticaProcessoJusticaFederal;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Classe que irá processar a estatística dos eventos necessários.
 * 
 * @author Daniel
 * 
 */
@Name(EstatisticaEventoProcessor.NAME)
@AutoCreate
public class EstatisticaEventoProcessor {

	public static final String NAME = "estatisticaEventoProcessor";

	@In
	private LogService logService;

	@In
	private ProcessoEventoManager processoEventoManager;
	
	@In
	private CompetenciaManager competenciaManager;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@Logger
	private Log log;
	

	public static EstatisticaEventoProcessor instance() {
		return (EstatisticaEventoProcessor) Component.getInstance(NAME);
	}

	/**
	 * Método invocado pela trigger sempre que o intervalo da cron estipulado
	 * for concluído, ele também irá processar os eventos que devem ser
	 * incluídos nas estatísticas.
	 * 
	 * @param cron
	 * @return
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle processarEventos(@IntervalCron String cron) {
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			processarEventos();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "processarEventos");
		}
		return null;
	}
	
	private Object processarEventos() {
		List<ProcessoEvento> listEventoNaoProcessado = processoEventoManager.recuperaNaoContabilizadas();
		if (listEventoNaoProcessado != null && listEventoNaoProcessado.size() > 0) {
			Evento[] eventosCorregedoria = getEventosCorregedoria();
			Evento[] eventosJF = getEventosJusticaFederal();
			if (eventosCorregedoria != null && eventosJF != null) {
				for (ProcessoEvento pe : listEventoNaoProcessado) {
					boolean isEventoJF = true;
					for (int i = 0; i < eventosCorregedoria.length; i++) {
						if (pe.getEvento().isDescendentOf(eventosCorregedoria[i])) {
							ProcessoTrf procTrf = EntityUtil.find(ProcessoTrf.class, pe.getProcesso().getIdProcesso());

							// persistir EstatisticaEventoProcesso
							EstatisticaEventoProcesso evp = new EstatisticaEventoProcesso();
							evp.setIdProcessoTrf(procTrf.getIdProcessoTrf());
							evp.setNumeroProcesso(procTrf.getNumeroProcesso());
							evp.setClasseJudicial(procTrf.getClasseJudicial().getClasseJudicial());
							evp.setDataInclusao(pe.getDataAtualizacao());
							evp.setOrgaoJulgador(procTrf.getOrgaoJulgador().getOrgaoJulgador());
							evp.setCodEvento(eventosCorregedoria[i].getCodEvento());
							evp.setCodEstado(ParametroUtil.instance().getSecao());
							Competencia competencia = competenciaManager.getCompetenciaByProcessoTrf(procTrf);
							evp.setCompetencia(competencia == null ? null : competencia.getCompetencia());
							evp.setJurisdicao(procTrf.getOrgaoJulgador().getJurisdicao().getJurisdicao());
							TipoProcessoDocumento sentenca = ParametroUtil.instance()
									.getTipoProcessoDocumentoSentenca();
							TipoProcessoDocumento apelacao = ParametroUtil.instance()
									.getTipoProcessoDocumentoApelacao();
							evp.setDocumentoSentenca(processoDocumentoManager.existeProcessoDocumentoByTipo(
									pe.getProcesso(), sentenca));
							evp.setDocumentoApelacao(processoDocumentoManager.existeProcessoDocumentoByTipo(
									pe.getProcesso(), apelacao));
							estatisticaEventoProcessoManager.persist(evp);

							// persistir EstatisticaProcessoJusticaFederal
							Evento eventoProcessual = EntityUtil.find(Evento.class, 
									pe.getEvento().getIdEvento());
							gravarEstatisticaJF(procTrf, competencia, eventoProcessual.getCodEvento(), 
									pe.getDataAtualizacao());

							isEventoJF = false;
							pe.setProcessado(true);
							break;
						}
					}
					if (isEventoJF) {
						for (int i = 0; i < eventosJF.length; i++) {
							if (pe.getEvento().isDescendentOf(eventosJF[i])) {
								ProcessoTrf processo = EntityUtil.find(ProcessoTrf.class, pe.getProcesso()
										.getIdProcesso());
								Competencia competencia = competenciaManager.getCompetenciaByProcessoTrf(processo);
								Evento eventoProcessual = EntityUtil.find(Evento.class, 
										pe.getEvento().getIdEvento());
								gravarEstatisticaJF(processo, competencia, eventoProcessual.getCodEvento(), 
										pe.getDataAtualizacao());
								pe.setProcessado(true);
								break;
							}
						}
					}
					pe.setVerificadoProcessado(true);
					try {
						processoEventoManager.persist(pe);
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private void gravarEstatisticaJF(ProcessoTrf processoTrf, Competencia competencia, String codEvento,
			Date dataAtualizacao) {
		EstatisticaProcessoJusticaFederal epjf = new EstatisticaProcessoJusticaFederal();
		epjf.setCargo(processoTrf.getCargo());
		epjf.setClasseJudicial(processoTrf.getClasseJudicial());
		epjf.setCodEvento(codEvento);
		epjf.setCompetencia(competencia);
		epjf.setDataInclusao(new Date());
		epjf.setDtEvento(dataAtualizacao);
		epjf.setOrgaoJulgador(processoTrf.getOrgaoJulgador());
		epjf.setPessoaMagistrado(pessoaMagistradoManager.getMagistradoRecebeDistribuicao(processoTrf.getOrgaoJulgador()));
		epjf.setProcessoTrf(processoTrf);
		epjf.setSecaoJudiciaria(ParametroUtil.instance().getSecao());
		estatisticaProcessoJusticaFederalManager.persist(epjf);
	}

	/**
	 * Obtem através dos parametros os eventos que devem ser verficados e
	 * incluidos na estatisticas. Esses eventos servem tanto para os relatorios
	 * da corregedoria quanto para os relatorios da justica federal
	 * 
	 * @return
	 */
	private Evento[] getEventosCorregedoria() {
		Evento[] eventos = new Evento[11];
		ParametroUtil parametroUtil = ParametroUtil.instance();
		eventos[0] = parametroUtil.getEventoArquivamentoDefinitivoProcessual();
		eventos[1] = parametroUtil.getEventoArquivamentoProvisorio();
		eventos[2] = parametroUtil.getEventoArquivamento();
		eventos[3] = parametroUtil.getEventoDistribuicaoProcessual();
		eventos[4] = parametroUtil.getEventoJulgamentoProcessual();
		eventos[5] = parametroUtil.getEventoBaixaDefinitivaProcessual();
		eventos[6] = parametroUtil.getEventoSuspensaoDespachoProcessual();
		eventos[7] = parametroUtil.getEventoDesarquivamentoProcessual();
		eventos[8] = parametroUtil.getEventoReativacaoProcessual();
		eventos[9] = parametroUtil.getEventoRemetidoTrfProcessual();
		eventos[10] = parametroUtil.getEventoSuspensaoDecisaoProcessual();
		for (Evento evento : eventos) {
			if (evento == null) {
				return null;
			}
		}
		return eventos;
	}

	private Evento[] getEventosJusticaFederal() {
		Evento[] eventos = new Evento[9];
		ParametroUtil parametroUtil = ParametroUtil.instance();
		eventos[0] = parametroUtil.getEventoConclusao();
		eventos[1] = parametroUtil.getEventoSemResolucaoMerito();
		eventos[2] = parametroUtil.getEventoExtincaoPunibilidade();
		eventos[3] = parametroUtil.getEventoDecisao();
		eventos[4] = parametroUtil.getEventoProcessualRedistribuicao();
		eventos[5] = parametroUtil.getEventoJulgamentoEmDiligenciaProcessual();
		eventos[6] = parametroUtil.getEventoMudancaClasseProcessual();
		eventos[7] = parametroUtil.getEventoRecebimentoProcessual();
		eventos[8] = parametroUtil.getEventoDespacho();
		for (Evento evento : eventos) {
			if (evento == null) {
				return null;
			}
		}
		return eventos;
	}

}
