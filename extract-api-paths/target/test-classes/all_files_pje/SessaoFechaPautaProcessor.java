package br.com.infox.pje.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.infox.ibpm.service.LogService;
import br.com.infox.trf.eventos.DefinicaoEventos;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.Crypto;

@Name(SessaoFechaPautaProcessor.NAME)
@AutoCreate
public class SessaoFechaPautaProcessor implements Serializable{

	private static final long serialVersionUID = 1L;

	public final static String NAME = "sessaoFechaPautaProcessorProcessor";
	
	@Logger
	private static Log log;

	@In
	private LogService logService;
	
	private EntityManager entityManager;

	public SessaoFechaPautaProcessor(){
	}

	public EntityManager getEntityManager(){
		if (entityManager == null){
			entityManager = EntityUtil.getEntityManager();
		}
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}

	public static SessaoFechaPautaProcessor instance(){
		return (SessaoFechaPautaProcessor) Component.getInstance(NAME);
	}

	/**
	 * Metodo utilizado para fechamento de pauta automatico
	 * 
	 * @param inicio
	 * @param cron
	 * @return
	 * @throws SchedulerException
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle fecharPautaAutomatico(@Expiration Date inicio, @IntervalCron String cron){
		
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			fecharPautaAutomatico();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "fecharPautaAutomatico");
		}
		return null;
	}
	
	private Object fecharPautaAutomatico() {

		List<Sessao> sessoesDoDiaCorrente = getSessoesDoDiaCorrente();
		for (Sessao sessao : sessoesDoDiaCorrente){
			List<ProcessoTrf> listProcessosEmPauta = new ArrayList<ProcessoTrf>();

			for (SessaoPautaProcessoTrf sppt : listaSessaoPautaProcessoTrf(sessao)){
				listProcessosEmPauta.add(sppt.getProcessoTrf());
			}

			Evento evento = entityManager.find(Evento.class,
					Integer.parseInt(ParametroUtil.getFromContext("idProcessoIncluidoPauta", true)));

			for (ProcessoTrf processoTrf : listProcessosEmPauta){
				// Persist os Eventos
				try{
					RegistraEventoAction.instance().registrarEvento(processoTrf.getProcesso(), evento,
							sessao.getUsuarioInclusao(), new Date());
				} catch (Exception e){
					throw new AplicationException(AplicationException.createMessage("Erro ao registrar evento: "
							+ DefinicaoEventos.INCLUSAO_PAUTA, "validar()", this.getClass().getName(), "PJE"));
				}

				// Persist Processo Documento
				ProcessoDocumento processoDocumento = new ProcessoDocumento();
				processoDocumento.setProcessoDocumento(ParametroUtil.instance()
						.getTipoProcessoDocumentoIntimacaoPauta().getTipoProcessoDocumento());
				processoDocumento.setProcesso(processoTrf.getProcesso());
				processoDocumento.setUsuarioInclusao(sessao.getUsuarioInclusao());
				processoDocumento.setDataInclusao(sessao.getDataFechamentoPauta());
				processoDocumento.setAtivo(Boolean.TRUE);
				processoDocumento.setTipoProcessoDocumento(ParametroUtil.instance()
						.getTipoProcessoDocumentoIntimacaoPauta());
				processoDocumento.setDocumentoSigiloso(Boolean.FALSE);

				ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
				bin.setDataInclusao(new Date());
				bin.setModeloDocumento(processarModelo(ParametroUtil.instance().getModeloIntimacaoPauta()
						.getModeloDocumento()));
				bin.setUsuario(sessao.getUsuarioInclusao());

				entityManager.persist(bin);

				processoDocumento.setProcessoDocumentoBin(bin);

				entityManager.persist(processoDocumento);
				entityManager.flush();

				// Persist Processo Expediente
				ProcessoExpediente processoExpediente = new ProcessoExpediente();
				processoExpediente.setProcessoTrf(processoTrf);
				processoExpediente.setDtCriacao(sessao.getDataFechamentoPauta());
				processoExpediente.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.E);
				processoExpediente.setUrgencia(Boolean.FALSE);
				processoExpediente.setTipoProcessoDocumento(ParametroUtil.instance()
						.getTipoProcessoDocumentoIntimacaoPauta());

				getEntityManager().persist(processoExpediente);
				getEntityManager().flush();

				// GERA O PROCESSO EXPEDIENTE PARA O DOCUMENTO
				// PROCESSOPARTEEXPEDIENTE
				ProcessoDocumentoExpediente pde = new ProcessoDocumentoExpediente();
				pde.setProcessoDocumento(processoDocumento);
				pde.setProcessoExpediente(processoExpediente);
				getEntityManager().persist(pde);
				getEntityManager().flush();
				// ---------------------------------

				// Intima as Partes do processo
				ProcessoParteExpediente ppe = null;
				for (ProcessoParte parteAtivo : processoTrf.getListaParteAtivo()){
					ppe = new ProcessoParteExpediente();
					ppe.setPessoaParte(parteAtivo.getPessoa());
					ppe.setProcessoExpediente(processoExpediente);
					ppe.setPrazoLegal(sessao.getOrgaoJulgadorColegiado().getDiaCienciaInclusaoPauta());
					getEntityManager().persist(ppe);
					getEntityManager().flush();
				}

				for (ProcessoParte partePassivo : processoTrf.getListaPartePassivo()){
					ppe = new ProcessoParteExpediente();
					ppe.setPessoaParte(partePassivo.getPessoa());
					ppe.setProcessoExpediente(processoExpediente);
					ppe.setPrazoLegal(sessao.getOrgaoJulgadorColegiado().getDiaCienciaInclusaoPauta());
					getEntityManager().persist(ppe);
					getEntityManager().flush();
				}

				if (DomicilioEletronicoService.instance().isIntegracaoHabilitada()) {
					DomicilioEletronicoService.instance().enviarExpedientesAsync(Arrays.asList(processoExpediente));
				}
			}
		}
		return null;
	}

	/**
	 * Metodo que busca todas sessões que têm fechamento hoje e estão programadas para fechamento automático
	 */
	@SuppressWarnings("unchecked")
	private List<Sessao> getSessoesDoDiaCorrente(){
		String query = "select s from Sessao s "
				+ "where cast(s.dataFechamentoPauta as date) = cast(:dataFechamentoPauta as date) "
				+ "and s.orgaoJulgadorColegiado.fechamentoAutomatico = true ";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("dataFechamentoPauta", new Date());
		return q.getResultList();
	}

	/**
	 * Metodo que retorna uma lista de processos que estão em pauta
	 */
	@SuppressWarnings("unchecked")
	private List<SessaoPautaProcessoTrf> listaSessaoPautaProcessoTrf(Sessao sessao){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoPautaProcessoTrf o ");
		sb.append("where o.dataExclusaoProcessoTrf = null ");
		sb.append("and o.sessao = :sessao ");
		sb.append("and o.tipoInclusao = 'PA'");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessao);

		return q.getResultList();
	}

	/**
	 * Processa um modelo avaliando linha a linha.
	 * 
	 * @param modelo
	 * @return
	 */
	public static String processarModelo(String modelo){
		if (modelo != null){
			StringBuilder modeloProcessado = new StringBuilder();
			String[] linhas = modelo.split("\n");
			for (int i = 0; i < linhas.length; i++){
				if (modeloProcessado.length() > 0){
					modeloProcessado.append('\n');
				}
				String linha = linhas[i];
				try{
					linha = (String) Expressions.instance().createValueExpression(linhas[i]).getValue();
				} catch (RuntimeException e){
					log.warn("Erro ao avalizar expressão na linha: '" + linha + "': " + e.getMessage());
				}
				modeloProcessado.append(linha);
			}
			return modeloProcessado.toString();
		}
		return modelo;
	}
}
