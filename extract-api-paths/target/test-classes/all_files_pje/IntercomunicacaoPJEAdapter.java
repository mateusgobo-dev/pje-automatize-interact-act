package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.home.AlertaHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.quartz.JobInfo;
import br.com.infox.component.quartz.QuartzJobsInfo;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.ProcessoJbpmManager;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.ManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.ConsolidadorDocumentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.mni.entidades.DownloadBinario;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

@Name(IntercomunicacaoPJEAdapter.NAME)
public class IntercomunicacaoPJEAdapter implements
		ManifestacaoProcessualHandler {

	public static final String NAME = "v222.intercomunicacaoPJEAdapter";
	
    @Logger
    private Log logger;
    
	@Override
	public void onBeforeEntregarManifestacaoProcessual(
			ManifestacaoProcessual manifestacaoProcessual) {

		/*
		 * Copia o paramêtro com o número de processo original 
		 * para dentro dos paramêtros de cada um dos documentos,
		 * dessa maneira não é preciso fazer alterações no 
		 * IntercomunicacaoService para tratar arquivos sem conteúdo
		 * enviados pelo PJe.
		 */
		String numProcesso = MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_NUM_PROC_1_GRAU);
		Parametro paramNumeroProcesso = new Parametro();
		paramNumeroProcesso.setNome(MNIParametro.PARAM_NUM_PROC_1_GRAU);
		paramNumeroProcesso.setValor(numProcesso);
		
		for(DocumentoProcessual documentoProcessual : manifestacaoProcessual.getDocumento()) {
			documentoProcessual.getOutroParametro().add(paramNumeroProcesso);
		}
	}

	@Override
	public void onAfterEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf, ProcessoDocumento documentoPrincipal) {
		boolean isRemessa = MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_URL_ORIGEM_ENVIO) != null;
		EntityManager entityManager = EntityUtil.getEntityManager();
		 
		/*
		 * Lança movimento de recebimento. Existe a exceção de que não se deve lançar o movimento
		 * caso seja o protocolo de um novo processo na segunda instância.
		 */
		if (isRemessa) {
			ProcessoJudicialService processoJudicialService = ComponentUtil.getProcessoJudicialService();
			
			if (documentoPrincipal != null &&
				ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias() != null &&
				ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias().equals(documentoPrincipal.getTipoProcessoDocumento())) {
				processoJudicialService.sinalizarFluxo(processoTrf, Variaveis.PJE_FLUXO_MNI_AGUARDA_COMUNICACAO_ENTRE_INSTANCIAS, true, false, true);
			} else {
				processoJudicialService.sinalizarFluxo(processoTrf, Variaveis.PJE_FLUXO_MNI_AGUARDA_REMESSA, true, false, true);
			}
			
			if (ParametroUtil.instance().isPrimeiroGrau()){
				AlertaHome.instance().inserirAlerta(processoTrf, "Processo recebido da instância superior.", CriticidadeAlertaEnum.I);
				EntityUtil.getEntityManager().flush();
			} else {
				MovimentoAutomaticoService.instance().
					deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_RECEBIMENTO).
					associarAoProcesso(processoTrf).lancarMovimento();
			}
			
			/*
			 * Associa o objeto de entrega do processoentity
			 * (br.com.infox.cliente.entity.ManifestacaoProcessual) com os arquivos
			 * que devem ser baixados.
			 */
			br.jus.pje.nucleo.entidades.ManifestacaoProcessual mp = ComponentUtil.getComponent(ManifestacaoProcessualManager.class).buscaSemWsdl(processoTrf);
			
			Query query = entityManager.createQuery("from DownloadBinario where numeroProcesso = :np and manifestacaoProcessual is null");
			query.setParameter("np", MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_NUM_PROC_1_GRAU));
			try {
				List<DownloadBinario> agends = (List<DownloadBinario>) query.getResultList();
				for(DownloadBinario db: agends){
					db.setManifestacaoProcessual(mp);
				}
			} catch (Exception e) {
				logger.error("Erro ao tentar atualizar as referências dos agendamentos de download do conteúdo binário.");
			}
			
			String enderecoOrigemEnvio = MNIParametroUtil.obterValor(manifestacaoProcessual,MNIParametro.PARAM_URL_ORIGEM_ENVIO);
			mp.setWsdlOrigemEnvio(enderecoOrigemEnvio);
			
			String instanciaOrigem = MNIParametroUtil.obterValor(manifestacaoProcessual,MNIParametro.PARAM_INSTANCIA_PROCESSO_ORIGEM);
			mp.setCodigoAplicacaoOrigem(instanciaOrigem);
			mp.setCodigoOrigem(instanciaOrigem);
			
			String enderecoOrigemConsulta = MNIParametroUtil.obterValor(manifestacaoProcessual,MNIParametro.PARAM_URL_ORIGEM_CONSULTA);
			mp.setWsdlOrigemConsulta(enderecoOrigemConsulta);
			
			entityManager.merge(mp);
		}
		salvarProcessoTrfManifestacao(manifestacaoProcessual, processoTrf);
		buscarDocumentos(processoTrf.getNumeroProcesso().replaceAll("\\D",""));
		adicionarParametrosNoFluxo(manifestacaoProcessual, processoTrf);
		entityManager.flush();
	}
	
	public void transitarNoFluxo(ProcessoTrf processoTrf){
        SituacaoProcesso situacaoProcesso = ComponentUtil.getComponent(SituacaoProcessoManager.class).getByIdProcesso(processoTrf.getIdProcessoTrf());
        Long idTaskInstance = situacaoProcesso.getIdTaskInstance();
        BusinessProcess.instance().setTaskId(idTaskInstance);
        TaskInstanceHome.instance().setTaskId(idTaskInstance);
        if (ManagedJbpmContext.instance().getTaskInstance(idTaskInstance).getStart() == null) {
            BusinessProcess.instance().startTask();
        }
       
        TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);
        String transicaoSaida = (String)TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
        if (transicaoSaida != null && !transicaoSaida.isEmpty() && TaskInstanceHome.instance() != null){           
            taskInstance.end(transicaoSaida);
        }
        else{
            FacesMessages.instance().add(Severity.WARN, "ATENÇÃO: Não foi definida uma transição de saída para este nó.");
        }
    }

	/**
	 * Salva a referência do processo remetido/retornado de outra instância.
	 * 
	 * @param manifestacao
	 * @param processo
	 */
	protected void salvarProcessoTrfManifestacao(ManifestacaoProcessual manifestacao, ProcessoTrf processo) {
		
		try {
			String enderecoOrigemEnvio = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_URL_ORIGEM_ENVIO);
			String enderecoOrigemConsulta = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_URL_ORIGEM_CONSULTA);
			String numeroProcessoOrigem = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_NUM_PROC_1_GRAU);
			
			if (StringUtils.isNotBlank(enderecoOrigemEnvio)) {
				
				if(numeroProcessoOrigem == null || numeroProcessoOrigem.equals("")){
					numeroProcessoOrigem = manifestacao.getDadosBasicos().getNumero().getValue();						
				}
				
				EnderecoWsdl enderecoWsdl = obterEnderecoWsdl(enderecoOrigemEnvio,enderecoOrigemConsulta);
				
				ProcessoTrfManifestacaoProcessual processoManifestacao = new ProcessoTrfManifestacaoProcessual();
				processoManifestacao.setEnderecoWsdl(enderecoWsdl);
				processoManifestacao.setProcessoTrf(processo);
				processoManifestacao.setNumeroProcessoManifestacao(numeroProcessoOrigem);

				getProcessoTrfManifestacaoProcessualManager().persist(processoManifestacao);
			}
		} catch (PJeBusinessException e) {
			throw new AplicationException(e);
		}
	}
	
	/**
	 * Chama o consolidador de documentos para buscar os documentos no caso da instalação não
	 * usar base unificada.
	 */
	protected void buscarDocumentos(String numeroProcesso) {
		if (!ParametroUtil.instance().isBaseBinariaUnificada()) {
			ConsolidadorDocumentosService consolidador = ComponentUtil.getComponent(ConsolidadorDocumentosService.class);
			consolidador.realizaDownloadDocumentos(numeroProcesso);

			/*QuartzJobsInfo action = ComponentUtil.getComponent(QuartzJobsInfo.class);
			JobInfo consolidador = action.getConsolidadorDocumentosJobInfo();
			action.triggerJob(consolidador);*/
		}
	}

	/**
	 * Retorna o endereço wsdl referentes aos parâmetros passados.
	 * 
	 * @param enderecoOrigemEnvio
	 * @param enderecoOrigemConsulta
	 * @return EnderecoWsdl
	 */
	protected EnderecoWsdl obterEnderecoWsdl(String enderecoOrigemEnvio, String enderecoOrigemConsulta) {
		EnderecoWsdl endereco = new EnderecoWsdl();
		endereco.setWsdlConsulta(enderecoOrigemConsulta);
		endereco.setWsdlIntercomunicacao(enderecoOrigemEnvio);
		
		return getEnderecoWsdlManager().getDAO().obterPeloWsdl(endereco);
	}

	/**
	 * @return ProcessoTrfManifestacaoProcessualManager
	 */
	protected ProcessoTrfManifestacaoProcessualManager getProcessoTrfManifestacaoProcessualManager() {
		return ProcessoTrfManifestacaoProcessualManager.instance();
	}
	
	/**
	 * @return EnderecoWsdlManager
	 */
	protected EnderecoWsdlManager getEnderecoWsdlManager() {
		return EnderecoWsdlManager.instance();
	}
	
	/**
	 * Adiciona os parâmetros com prefixo "pje:fluxo" ao fluxo criado para o processo.
	 * 
	 * @param manifestacaoProcessual ManifestacaoProcessual
	 * @param processoTrf ProcessoTrf
	 */
	protected void adicionarParametrosNoFluxo(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf) {
		Map<String, Object> parametros = MNIParametroUtil.converterParaMap(manifestacaoProcessual.getParametros());
		
		ProcessoJbpmManager manager = ProcessoJbpmManager.instance();
		manager.adicionarVariaveis(processoTrf, parametros);
	}
}
