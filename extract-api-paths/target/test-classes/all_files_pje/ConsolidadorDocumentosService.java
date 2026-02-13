package br.jus.cnj.pje.servicos;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.service.LogService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DownloadBinarioArquivoManager;
import br.jus.cnj.pje.nucleo.manager.DownloadBinarioMNIManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.pje.mni.entidades.DownloadBinario;
import br.jus.pje.mni.entidades.DownloadBinarioArquivo;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;

@Name(ConsolidadorDocumentosService.NAME)
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class ConsolidadorDocumentosService {
	
	public static final String NAME = "consolidadorDocumentosService";
	
	@In
	private EntityManager entityManager;

	@In
	private LogService logService;

	@Logger
	private Log log;
	
	@In
	private DownloadBinarioMNIManager downloadBinarioMNIManager;
	
	@In
	private DownloadBinarioArquivoManager downloadBinarioArquivoManager;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In
	private ProcessoTrfManifestacaoProcessualManager processoTrfManifestacaoProcessualManager;
	
	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		try {
			realizaDownloadDocumentos();	
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "realizaDownloadDocumentos");
		}
		return null;
	}
	
//	@CustomJbpmTransactional
	public void realizaDownloadDocumentos() { 
		log.info("Iniciando recuperação diferida de documentos previamente enviados.");
		List<Integer> agendamentos = downloadBinarioMNIManager.recuperaIdentificadores();
		log.info("Disparando {0} recuperações diferidas de documentos. A operação pode demorar", agendamentos.size());
		for(Integer agd: agendamentos){
			Events.instance().raiseEvent("RECUPERACAO_DIFERIDA", agd);
		}
		log.info("Recuperações iniciadas.");
	}

	public void realizaDownloadDocumentos(String numeroProcessoTRF) {
		log.info("Iniciando recuperação diferida de documentos previamente enviados.");
		List<Integer> agendamentos = downloadBinarioMNIManager.recuperaIdentificadoresPorProcesso(numeroProcessoTRF);
		//log.info("Disparando {0} recuperações diferidas de documentos. A operação pode demorar", agendamentos.size());
		for(Integer agd: agendamentos){
			Events.instance().raiseEvent("RECUPERACAO_DIFERIDA", agd);
		}
		//log.info("Recuperações iniciadas.");
	}
	
	@Transactional
	@Observer(value="RECUPERACAO_DIFERIDA")
	public void recuperarDocumentoEnviado(Integer idDownloadAgendado){
		log.info("Iniciando recuperação diferida de documentos do agendamento [{0}].", idDownloadAgendado);
		
		try {
			DownloadBinario agd = downloadBinarioMNIManager.findById(idDownloadAgendado);
			if(agd != null){
				EnderecoWsdl wsdl = obterEnderecoWsdlDaManifestacao(agd.getNumeroProcesso());
				List<String> identificadoresRemotos = downloadBinarioMNIManager.recuperaIdentificadoresDocumentos(agd.getNumeroProcesso());
				
				LinkedList<Boolean> atualizouTodosDocumentos = new LinkedList<Boolean>();
				if(!identificadoresRemotos.isEmpty()){
					List<ProcessoDocumento> docs = recuperaDocumentosRemotos(agd.getNumeroProcesso(), wsdl, identificadoresRemotos);
					if(docs != null && !docs.isEmpty()){
						for(int i = 0; i < docs.size(); i += 10){
							try {
								log.info("Iniciando a atualização dos documentos do processo {0} sob o agendamento {1}", agd.getNumeroProcesso(),agd.getId());
								atualizouTodosDocumentos.add(atualizarDocumentos(agd, docs.subList(i, i + 10 > docs.size() ? docs.size() : i + 10)));
								
								
							} catch (Throwable t) {
								log.error("Erro ao tentar recuperar parte dos documentos do agendamento {0}, processo {1}", agd.getId(),agd.getNumeroProcesso());
								atualizouTodosDocumentos.add(false);
								t.printStackTrace();

							}
						}
						atualizouTodosDocumentos.add(identificadoresRemotos.size()==docs.size());
					}else
						atualizouTodosDocumentos.add(false);
					
				}
				if(!atualizouTodosDocumentos.contains(false)){
					synchronized (NAME) {
						downloadBinarioMNIManager.removerDownloadBinario(agd);
					}
						
				}
			}
			log.info("Finalizada a recuperação diferida de documentos do agendamento [{0}].", idDownloadAgendado);
		} catch (PJeBusinessException e) {
			log.error("Erro ao tentar obter documentos com recuperação diferida: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			log.error("A URL de recuperação do agendamento com identificador [{0}] é inválida: {1}", idDownloadAgendado, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private List<ProcessoDocumento> recuperaDocumentosRemotos(String numeroProcesso, EnderecoWsdl wsdl, List<String> identificadoresRemotos) throws MalformedURLException, PJeBusinessException{
		ConsultarProcessoRequisicaoDTO req = new ConsultarProcessoRequisicaoDTO();
		req.setNumeroProcesso(numeroProcesso);
		ConsultarProcessoRespostaDTO consultarProcesso = null;
		MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(wsdl);

		try {
			log.info("Iniciando consulta no servidor [{0}]", wsdl.getWsdlIntercomunicacao());
			int numeroDeConsultas = identificadoresRemotos.size() / 10;
			for(int i = 0; i <= numeroDeConsultas; i++) {
				req.getDocumento().clear();
				int inicioLista = i * 10;
				int finalLista = i == numeroDeConsultas ? identificadoresRemotos.size() : (i * 10) + 10;
				List<String> idsParaBuscar = identificadoresRemotos.subList(inicioLista, finalLista);
				if(idsParaBuscar.isEmpty()) {
					continue;
				}
				req.getDocumento().addAll(idsParaBuscar);
				log.debug("Consultando documentos [{0}] na origem: [{1}]", StringUtils.join(idsParaBuscar, ", "), wsdl.getWsdlIntercomunicacao());
				try {
					if(i == 0 || consultarProcesso==null) {
						consultarProcesso = mediator.consultarProcesso(req);
					} else {
						ConsultarProcessoRespostaDTO consultaDocumento = mediator.consultarProcesso(req);
						consultarProcesso.getProcessoDocumentoList().addAll(consultaDocumento.getProcessoDocumentoList());
					}
				} catch (Exception e) {
					log.error("Erro ao tentar recuperar os documentos na origem: {0}. Documentos: [{1}]. Erro: {2}", wsdl.getWsdlIntercomunicacao(), StringUtils.join(idsParaBuscar, ", "),e.getMessage());
					continue;
				}
				
			}			
		} catch (Throwable t) {
			log.error("Erro ao tentar recuperar os documentos na origem: {0}. Documentos: [{1}].", t.getLocalizedMessage(), StringUtils.join(identificadoresRemotos, ", "));
			t.printStackTrace();
			throw new PJeBusinessException(t);
		}
		List<ProcessoDocumento> docs =  null;
		if(consultarProcesso == null || consultarProcesso.getProcesso() == null) {
			return null;
		}else{
			docs = consultarProcesso.getProcessoDocumentoList();
			log.debug("Retornando {0} documentos", docs==null?0:docs.size());
			return docs;
		}
	}
	
	public void atualizarDocumentosDoProcesso(String numeroProcesso, List<String> idsDocumentos) throws Exception {
		log.info("iniciando download dos documentos do processo: " + numeroProcesso);
		
		Query query = entityManager.createQuery("from DownloadBinario where numeroProcesso = :np");
		query.setParameter("np", numeroProcesso);
		
		DownloadBinario db = EntityUtil.getSingleResult(query);
		boolean selecionouTodosArquivos = db.getArquivos().size() == idsDocumentos.size();
		
		List<String> ids = new ArrayList<String>();
		
		for (DownloadBinarioArquivo dba : db.getArquivos()) {
			if(idsDocumentos.contains(dba.getIdProcessoDocumentoBin())) {
				ids.add(String.valueOf(dba.getIdArquivoOrigem()));
			}
		}
		EnderecoWsdl wsdl = obterEnderecoWsdlDaManifestacao(numeroProcesso);
		List<ProcessoDocumento> documentos = consultarDocumentos(wsdl, db, ids);
		
		if(documentos == null || documentos.isEmpty()) {
			log.info("Nenhum documento encontrado. Cancelando operação.");
			return;
		} else {
			log.info("Download de documentos realizado com sucesso. Atualizando a base de dados.");
		}
		
		
		boolean erroEmArquivo = !atualizarDocumentos(db, documentos);
		
		if(erroEmArquivo) {
			String erro = String.format("Erro ao realizar o download dos binários para o processo [%s].", db.getNumeroProcesso());
			log.info(erro);
			throw new Exception(erro);
		} else {
			if(selecionouTodosArquivos) {
				entityManager.remove(db);
			} else {
				Query queryDelete = entityManager.createQuery("delete from DownloadBinarioArquivo where idProcessoDocumentoBin in (:ids)");
				queryDelete.setParameter("ids", idsDocumentos);
				queryDelete.executeUpdate();
				Iterator<DownloadBinarioArquivo> it = db.getArquivos().iterator();
				while(it.hasNext()) {
					DownloadBinarioArquivo dba = it.next();
					if(idsDocumentos.contains(dba.getIdProcessoDocumentoBin())) {
						it.remove();
					}
				}
				entityManager.merge(db);
			}
		}
		
		entityManager.flush();
	}
	
	private List<ProcessoDocumento> consultarDocumentos(EnderecoWsdl wsdl, DownloadBinario db, List<String> idsDocumentos) {
		// recuperar dados
		ConsultarProcessoRequisicaoDTO parameters = new ConsultarProcessoRequisicaoDTO();
		parameters.setNumeroProcesso(db.getNumeroProcesso());
		ConsultarProcessoRespostaDTO consultarProcesso = null;
		
		try {
			
			MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(wsdl);
			
			log.info("iniciando consulta no servidor [" + wsdl.getWsdlIntercomunicacao() + "]");
			
			int numeroDeConsultas = idsDocumentos.size() / 10;
			for(int i = 0; i <= numeroDeConsultas; i++) {
				parameters.getDocumento().clear();
				int inicioLista = i * 10;
				int finalLista = i == numeroDeConsultas ? idsDocumentos.size() : (i * 10) + 10;
				
				List<String> idsParaBuscar = idsDocumentos.subList(inicioLista, finalLista);
				
				if(idsParaBuscar.isEmpty()) {
					continue;
				}
				
				parameters.getDocumento().addAll(idsParaBuscar);
				
				log.debug("consultando documentos (processo documento) com ids (origem) [" + idsParaBuscar + "]");
				
				if(i == 0) {
					consultarProcesso = mediator.consultarProcesso(parameters);
				} else {
					ConsultarProcessoRespostaDTO consultaDocumento = mediator.consultarProcesso(parameters);
					consultarProcesso.getProcessoDocumentoList().addAll(consultaDocumento.getProcessoDocumentoList());
				}
			}			
		} catch (Exception e) {
			log.info("Erro ao realizar a consulta de documentos com ids (origem) (" + idsDocumentos + ")");
			e.printStackTrace();
		}
		
		if(consultarProcesso == null || consultarProcesso.getProcesso() == null) {
			return null;
		}
		
		List<ProcessoDocumento> docsProcesso =  consultarProcesso.getProcessoDocumentoList();
		List<ProcessoDocumento> docsParaGravar = new ArrayList<ProcessoDocumento>(docsProcesso);
		
		if(docsProcesso != null){
			for(ProcessoDocumento documento : docsProcesso){
				Integer idDocumento = documento.getIdProcessoDocumento();
				if(!idsDocumentos.contains(idDocumento)){
					docsParaGravar.remove(documento);
				}
			}
		}
		
		return docsParaGravar;
	}
	
	/**
	 * 
	 * @param db
	 * @param documentos
	 * @return true para atualizados com sucesso
	 */
	private boolean atualizarDocumentos(DownloadBinario db, List<ProcessoDocumento> documentos) {
		boolean erroEmArquivo = false;
		Thread th = Thread.currentThread();
		log.debug("Thread {0} iniciando atualização de documentos com o agendamento {1}", th.getId(), db.getId());
		boolean isHTML = false;
		
		LinkedList<Boolean> documentosAtualizados = new LinkedList<Boolean>();
		for(ProcessoDocumento dp : documentos) {
			ProcessoDocumentoBin bin = dp.getProcessoDocumentoBin();
			Object data = null;
			Integer idArquivoBin = null;
			int tamanho = 0;
			Date dataJuntada = null;
			
			DownloadBinarioArquivo arquivoDiferido = null;
			try {
				arquivoDiferido = downloadBinarioArquivoManager.recuperaPorIdentificadorOriginario(dp.getIdInstanciaOrigem());

			} catch (Exception e) {
				e.printStackTrace();
				log.error("Não foi possível recuperar o downloadBinarioArquivo pelo ProcessoDocumento {0}.", dp.getIdInstanciaOrigem());
				erroEmArquivo = true;
				documentosAtualizados.add(!erroEmArquivo);
				continue;
			}
			if(arquivoDiferido != null){
				dataJuntada = dp.getDataJuntada();
				idArquivoBin = arquivoDiferido.getIdProcessoDocumentoBin();
			
				byte[] bytes = bin.getProcessoDocumento();
				if(MimetypeUtil.isMimetypeHtml(bin.getExtensao())) {
					
					isHTML = true;
					try {
						data = new String(bytes, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						log.error("erro de encoding", e);
					}
				} else {					
					data = bytes;
					MimeUtilChecker mimeUtil = ComponentUtil.getComponent("mimeUtilChecker");
					String mimeFromBytes = mimeUtil.getMimeType(bytes);
					bin.setExtensao(mimeFromBytes);
					isHTML = false; 
				}
				
				String tipoDocumento = (dp.getTipoProcessoDocumento() != null ? dp.getTipoProcessoDocumento().getCodigoDocumento() : null);
				log.debug("Atualizando o documento de tipo (id) [" + tipoDocumento + "] com id binário [" + idArquivoBin + "] do formato [" + (isHTML ? "html" : "pdf") + "]");
				
				if (bin.getProcessoDocumento() == null || bytes == null  || bytes.length == 0) {
					log.error("Documento com id [" + dp.getIdProcessoDocumento() + "], id binário [" + idArquivoBin + "] e id do processo documento (origem) [" + arquivoDiferido.getIdArquivoOrigem() +"] sem conteúdo.");
					erroEmArquivo = true;
					documentosAtualizados.add(!erroEmArquivo);
					continue;
				}
				tamanho = bin.getProcessoDocumento().length;
			}
			if(idArquivoBin == null) { 
				log.error("Documento com id [" + dp.getIdProcessoDocumento() + "] não encontrado.");
				erroEmArquivo = true;
				documentosAtualizados.add(!erroEmArquivo);
				continue;
			} 
			
			erroEmArquivo = ! processoDocumentoManager.atualizarProcessoDocumentoComDownloadBinarioArquivo(arquivoDiferido, tamanho, data, dataJuntada, isHTML, bin.getExtensao());
			documentosAtualizados.add(!erroEmArquivo);
		
			
			if (!erroEmArquivo)
				log.info("Documento atualizado com sucesso. ProcessoDocumentoBin {0}",idArquivoBin );
		}
		
		return !documentosAtualizados.contains(false);
	}
	
	public static ConsolidadorDocumentosService instance() {
		return ComponentUtil.getComponent(ConsolidadorDocumentosService.NAME);
	}
	
	public boolean existemDocumentosParaConsolidar(ProcessoTrf processoTrf) {
		return downloadBinarioMNIManager.haAgendamentos(processoTrf);
	}
	
	// TODO Suprimir este método e sua chamada respectiva em ProcessoTrfHome.verificaSeDocumentoFoiConsolidado e em WEB-INF/xhtml/components/grid/processoDocumentoBin.xhtml(148)
	public boolean documentoFoiConsolidado(Integer idProcessoDocumentoBin) {  
		Query query = entityManager.createQuery("from DownloadBinarioArquivo dba where dba.idProcessoDocumentoBin = :idProcessoDocumentoBin");  
		query.setParameter("idProcessoDocumentoBin", idProcessoDocumentoBin);  
		DownloadBinarioArquivo arquivo = EntityUtil.getSingleResult(query);  
		return arquivo != null;  
	}
	
	public static EnderecoWsdl obterEnderecoWsdlDaManifestacao(String numeroProcesso) {
		EnderecoWsdl resposta = null;
		
		if (StringUtils.isNotBlank(numeroProcesso)) {
			ProcessoTrfManifestacaoProcessualManager manifestacaoManager = ProcessoTrfManifestacaoProcessualManager.instance();
			ProcessoTrfManifestacaoProcessual manifestacao = manifestacaoManager.obterUltimo(numeroProcesso);
			resposta = (manifestacao != null ? manifestacao.getEnderecoWsdl() : null);
			
			if (resposta == null) {
				resposta = ParametroUtil.instance().getEnderecoWsdlIntegracao();
			}
		}
		return resposta;
	}
	
	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String EXECUTE = "execute";
	}
}
