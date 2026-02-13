package br.com.infox.cliente.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.ibpm.component.FileData;
import br.com.infox.ibpm.component.FileUpload;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.FileHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RegistroIntimacao;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;

@Name("registroIntimacaoHome")
public class RegistroIntimacaoHome extends AbstractHome<RegistroIntimacao> {

	private static final long serialVersionUID = 1L;
	private static final String AVISO_REGISTRO_INTIMACAO = "RegistroIntimacaoHome.avisoRegistroIntimacao";
	private Boolean isGravado = Boolean.FALSE;
	private ProcessoJudicialManager processoJudicialManager = (ProcessoJudicialManager) Component
			.getInstance(ProcessoJudicialManager.class);
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	public static RegistroIntimacaoHome instance() {
		return ComponentUtil.getComponent("registroIntimacaoHome");
	}

	public void setRegistroIntimacaoIdRegistroIntimacao(Integer id) {
		setId(id);
	}

	public Integer getRegistroIntimacaoIdRegistroIntimacao() {
		return (Integer) getId();
	}
	
	private AnexarDocumentos anexarDocumentos = (AnexarDocumentos) ComponentUtil.getComponent("anexarDocumentos");
	private ProcessoDocumento pdPdf = new ProcessoDocumento();
	private ProcessoDocumentoBin pdbPdf = new ProcessoDocumentoBin();
	
	@Logger
	private Log logger;
	
	@In
	private PessoaFisicaService pessoaFisicaService;
	
	@In
	private UsuarioService usuarioService;

	@Override
	public void newInstance() {
		iniciarDocumentos();
		super.newInstance();
		setIsGravado(Boolean.FALSE);
		// Comentado na reintegração da capela_1.2.0.M6 e descanso_1.4.0.M4
		// ProcessoDocumentoHome.instance().setModeloSemNewInstance(false);
		ProcessoDocumentoHome.instance().setModelo(false, false);
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		ProcessoParteExpedienteHome ppeh = ProcessoParteExpedienteHome.instance();
		ProcessoParteExpediente ppe = ppeh.getInstance();
		processoTrfHome.setInstance(ppe.getProcessoJudicial());

		//anexarDocumentos.newInstance();
	}
	
	public void registrarIntimacao(){
		
		anexarDocumentos.setPdPdf(pdPdf);		
		anexarDocumentos.setPdbPdf(pdbPdf);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateWithoutTime = sdf.parse(sdf.format(getInstance().getData()));
			if( dateWithoutTime.after(new Date()) ) {
				FacesMessages.instance().add(Severity.ERROR, "A 'Data de recebimento' não pode ser posterior a data atual.");
				return;
			}
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		
		RegistroIntimacaoHome registroIntimacaoHome = RegistroIntimacaoHome.instance();
		RegistroIntimacao registroIntimacao = registroIntimacaoHome.getInstance();
		
		ProcessoParteExpedienteHome ppeh = ProcessoParteExpedienteHome.instance();
		ProcessoParteExpediente ppe = ppeh.getInstance();
		
		ProcessoDocumentoBinHome pdbh = ProcessoDocumentoBinHome.instance();
		//pdbh.setCertChain(certChain)
		
		ProcessoTrf proc;
		try {
			proc = processoJudicialManager.findById((ppe.getProcessoJudicial()).getIdProcessoTrf());
		} catch (PJeBusinessException e1) {
			String msgErro = "Erro na recuperação do processo: " + e1.getMessage();
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			logger.error("Erro na recuperação do processo: " + e1.getMessage());
			return;
		}
		
		ProcessoHome.instance().setInstance(proc.getProcesso());
		ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();
		pdHome.setModelo(true);
		pdHome.setIdDocumentoDestacar(0);
		//AnexarDocumentos.instance().actionAbaAnexar();		

		
		if (verificarDataRecebimento()) { 
			if(!anexarDocumentos.validarPdf(FileHome.instance())){
				return;
			}
			if (pdHome.persist() != null) {			
				//pdPdf.setTipoProcessoDocumento(pdHome.getInstance().getTipoProcessoDocumento());
				//pdPdf.setProcessoDocumento(pdHome.getInstance().getProcessoDocumento());
				anexarDocumentos.setPdHtml(pdHome.getInstance());
				anexarDocumentos.setPdbHtml(pdbh.getInstance());
				anexarDocumentos.addPdf();
				
				anexarDocumentos.setPdPdf(pdPdf);		
				anexarDocumentos.setPdbPdf(pdbPdf);
				
				
				anexarDocumentos.setSignature(pdbh.getInstance().getSignature());
				anexarDocumentos.setCertChain(pdbh.getInstance().getCertChain());
				anexarDocumentos.assinar();
				Contexts.getEventContext().set("byPassValidacaoAssinatura", true);
				anexarDocumentos.assinarPDF();
				
				//pdHome.assinarDocumentoHabilitacaoAutos();
				
				if (pdHome != null){
					PrazosProcessuaisService prazosProcessuaisService = (PrazosProcessuaisService) Component.getInstance("prazosProcessuaisService");
					ProcessoParteExpedienteManager processoParteExpedienteManager = (ProcessoParteExpedienteManager) Component.getInstance("processoParteExpedienteManager");
					registroIntimacao.setProcessoDocumento(pdHome.getInstance());
									
					
					registroIntimacao.setProcessoParteExpediente(ppe);
					registroIntimacaoHome.persist();				

					// Registra a pessoa que realizou a ciencia
					Pessoa pess = pessoaFisicaService.find(usuarioService.getUsuarioLogado().getIdUsuario());
					if (registroIntimacao != null && registroIntimacao.getResultado() != null &&
						  	registroIntimacao.getResultado().equals(TipoResultadoAvisoRecebimentoEnum.R) && ppe.getDtCienciaParte() == null){
						ppe.setPessoaCiencia(pess);
						Calendario calendario = prazosProcessuaisService.obtemCalendario(ppe.getProcessoJudicial().getOrgaoJulgador());
					 	processoParteExpedienteManager.registraCiencia(ppe, registroIntimacao.getData(), false, calendario);
					} else {
						ppe.setFechado(true);
					    Events.instance().raiseEvent(Eventos.EVENTO_EXPEDIENTE_FECHADO, ppe.getProcessoJudicial());
					}

			 		//Gera movimento de juntada
			 		gerarMovimentoDeJuntada();
				 	
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, "Documento(s) inserido(s) com sucesso");
				}
			}
		}else{
			javax.faces.context.FacesContext.getCurrentInstance().addMessage("intimacaoForm:dataDecoration:dataRecebimento", new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR,null,"Data de recebimento menor que a data de expediente."));
		} 	
		ProcessoParteExpedienteHome.instance().limparSearchDoAgrupador();
		ProcessoParteExpedienteHome.instance().limparNumeroProcesso();
	}

	/**
	 * Método que verifica se a data de Recebimento é menor que a data de expediente.
	 * @author Carlos Lisboa
	 * @since 26/11/2013
	 * @return true ou false
	 */
	public boolean verificarDataRecebimento(){
		Date dataExpediente = new Date();
		Date dataRecebimento = new Date();
		//dataExpediente = ProcessoParteExpedienteHome.instance().getInstance().getProcessoExpediente().getProcessoTrf().getIdProcessoTrf();
		dataExpediente = ProcessoParteExpedienteHome.instance().getInstance().getDataDisponibilizacao();
		dataRecebimento = instance.getData();
		if(dataRecebimento.getTime() < dataExpediente.getTime()){
			return false;			
		}else{
			return true;
		}
	}
	/**
	 * Método utilizado para gerar movimento de juntada 
	 * @author Eduardo Videira Paulo
	 * @since 26/11/2013
	 * @return void
	 */
	private void gerarMovimentoDeJuntada(){
		MovimentoAutomaticoService.preencherMovimento().
		deCodigo(581).
		comProximoComplementoVazio().
		doTipoLivre().
		preencherComTexto(pdPdf.getTipoProcessoDocumento().getTipoProcessoDocumento().toLowerCase()).
		associarAoDocumento(pdPdf).
		lancarMovimento();
	}
	
	public void iniciarDocumentos() {
		ProcessoDocumentoExpedienteHome.instance().newInstance();
		
		ProcessoTrfHome pth = ProcessoTrfHome.instance();
		pth.setId(ProcessoParteExpedienteHome.instance().getInstance().getProcessoExpediente().getProcessoTrf().getIdProcessoTrf());
		
		ProcessoDocumentoHome processoDocumentoHome = ProcessoDocumentoHome.instance();
		processoDocumentoHome.newInstance();
		processoDocumentoHome.getInstance().setProcessoDocumento("Aviso de Recebimento");
		//processoDocumentoHome.getInstance().setTipoProcessoDocumento(
			//	ParametroUtil.instance().getTipoProcessoDocumentoAvisoRecebimento());
		processoDocumentoHome.getInstance().setProcesso(
				ProcessoParteExpedienteHome.instance().getInstance().getProcessoExpediente().getProcessoTrf()
						.getProcesso());
		processoDocumentoHome.onSelectProcessoDocumento();
		
		//AnexarDocumentos.instance().actionAbaAnexar();
		
	}

	public String inserirAtualizarDoc() {
		ProcessoDocumento texto = null;
		ProcessoDocumento anexo = null;
		getInstance().setProcessoParteExpediente(ProcessoParteExpedienteHome.instance().getInstance());
		ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome.instance();
		ProcessoDocumentoBinHome processoDocBinHome = ProcessoDocumentoBinHome.instance();
		FileData fileData = FileUpload.instance().getFile();
		FileUpload.instance().clearUpload();

		if ((processoDocBinHome.getInstance() == null || processoDocBinHome.getInstance().getModeloDocumento() == null || processoDocBinHome
				.getInstance().getModeloDocumento().isEmpty())
				&& (fileData.getFileName() == null || fileData.getFileName().isEmpty())) {
			FacesMessages.instance().add(Severity.ERROR, "É obrigatório a edição e/ou a anexação de um documento");
			return "problem";

		}

		if (processoDocHome.getModelo() && processoDocBinHome.getInstance() != null
				&& processoDocBinHome.getInstance().getModeloDocumento() != null
				&& !processoDocBinHome.getInstance().getModeloDocumento().isEmpty()) {

			if (processoDocHome.persistComAssinatura() != null) {

				texto = processoDocHome.getInstance();

				ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome.instance()
						.getInstance();
				processoDocumentoExpediente.setAnexo(Boolean.TRUE);
				processoDocumentoExpediente.setProcessoDocumento(texto);
				processoDocumentoExpediente.setProcessoExpediente(ProcessoParteExpedienteHome.instance().getInstance()
						.getProcessoExpediente());
				getEntityManager().persist(processoDocumentoExpediente);
				Events.instance().raiseEvent(EventsTreeHandler.REGISTRA_EVENTO_EVENT_SEM_BPM, texto);
			}

			Boolean sigilo = texto.getDocumentoSigiloso();
			String certChain = processoDocBinHome.getInstance().getCertChain();
			String signature = processoDocBinHome.getInstance().getSignature();

			iniciarDocumentos();
			processoDocHome.getInstance().setDocumentoSigiloso(sigilo);
			processoDocBinHome.getInstance().setCertChain(certChain);
			processoDocBinHome.getInstance().setSignature(signature);
		}

		if (fileData.getFileName() != null && !fileData.getFileName().isEmpty()) {

			FileUpload.instance().setFile(fileData);
			// Comentado na reintegração da capela_1.2.0.M6 e descanso_1.4.0.M4
			// ProcessoDocumentoHome.instance().setModeloSemNewInstance(Boolean.FALSE);
			ProcessoDocumentoHome.instance().setModelo(false, false);
			if (fileData.getFileName() == null || fileData.getFileName().isEmpty()) {
				FacesMessages.instance().add(Severity.ERROR, "Selecione algum arquivo .pdf");
				return "problem";
			} else if (!fileData.getExtensao().equalsIgnoreCase(".pdf")) {
				FacesMessages.instance().add(Severity.ERROR, "O arquivo deve ser pdf");
				return null;
			}
			if (processoDocHome.persistComAssinatura() != null) {
				anexo = processoDocHome.getInstance();

				ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome.instance()
						.getInstance();
				processoDocumentoExpediente.setAnexo(Boolean.TRUE);
				processoDocumentoExpediente.setProcessoDocumento(anexo);
				processoDocumentoExpediente.setProcessoExpediente(ProcessoParteExpedienteHome.instance().getInstance()
						.getProcessoExpediente());
				getEntityManager().persist(processoDocumentoExpediente);

				Events.instance().raiseEvent(EventsTreeHandler.REGISTRA_EVENTO_EVENT_SEM_BPM, anexo);
			}
		}

		if (anexo == null) {
			getInstance().setProcessoDocumento(texto);
		} else {
			getInstance().setProcessoDocumento(anexo);
		}

		persist();

		if (getInstance().getResultado().equals(TipoResultadoAvisoRecebimentoEnum.R)) {
			ProcessoParteExpediente ppe = ProcessoParteExpedienteHome.instance().getInstance();
			if (ppe.getDtCienciaParte() == null || ppe.getDtCienciaParte().after(getInstance().getData())) {
				ppe.setDtCienciaParte(getInstance().getData());
				ppe.setCienciaSistema(Boolean.FALSE);
				PrazosProcessuaisService prazosService = new PrazosProcessuaisServiceImpl();
				OrgaoJulgador o = ppe.getProcessoJudicial().getOrgaoJulgador();
				Calendario calendario = prazosService.obtemCalendario(o);
				Date dataFinal = prazosService.calculaPrazoProcessual(ppe.getDtCienciaParte(), ppe.getPrazoLegal(),
						TipoPrazoEnum.D, calendario, ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(),
						ContagemPrazoEnum.C);
				ppe.setDtPrazoLegal(dataFinal);
				getEntityManager().merge(ppe);
				getEntityManager().flush();
			}
		}

		newInstance();

		Events.instance().raiseEvent(AVISO_REGISTRO_INTIMACAO);
		setIsGravado(Boolean.TRUE);
		refreshGrid("expedientePendenteOrgaoJulgadorGrid");
		return "persisted";

	}

	public TipoResultadoAvisoRecebimentoEnum[] getTipoResultadoAvisoRecebimentoItems() {
		List<TipoResultadoAvisoRecebimentoEnum> values = Arrays.asList(TipoResultadoAvisoRecebimentoEnum.values());
		Collections.sort(values, new Comparator<TipoResultadoAvisoRecebimentoEnum> () {
			
			@Override
			public int compare (TipoResultadoAvisoRecebimentoEnum o1, TipoResultadoAvisoRecebimentoEnum o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});		
		return (TipoResultadoAvisoRecebimentoEnum []) values.toArray();
	}

	public void setIsGravado(Boolean isGravado) {
		this.isGravado = isGravado;
	}

	public Boolean getIsGravado() {
		return isGravado;
	}
	
	public String inserirAtualizarDoc2() throws PJeBusinessException {
		ProcessoDocumento texto = null;
		ProcessoDocumento anexo = null;
		getInstance().setProcessoParteExpediente(ProcessoParteExpedienteHome.instance().getInstance());
		ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome.instance();
		ProcessoDocumentoBinHome processoDocBinHome = ProcessoDocumentoBinHome.instance();
		//TipoProcessoDocumento tipoProcessoDocumento = tipoProcessoDocumentoManager.findByCodigoTipoProcessoDocumento("116");
		//processoDocHome.getInstance().setTipoProcessoDocumento(tipoProcessoDocumento);
		
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		processoTrfHome.setInstance(ProcessoParteExpedienteHome.instance().getInstance().getProcessoJudicial());		
		
		FileData fileData = FileUpload.instance().getFile();
		FileUpload.instance().clearUpload();

		if ((processoDocBinHome.getInstance() == null)
				&& (fileData.getFileName() == null || fileData.getFileName().isEmpty())) {
			FacesMessages.instance().add(Severity.ERROR, "É obrigatório a edição e/ou a anexação de um documento");
			return "problem";

		}

		if (processoDocBinHome.getInstance() != null) {

			if (processoDocHome.persistComAssinatura() != null) {

				texto = processoDocHome.getInstance();

				ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome.instance()
						.getInstance();
				processoDocumentoExpediente.setAnexo(Boolean.TRUE);
				processoDocumentoExpediente.setProcessoDocumento(texto);
				processoDocumentoExpediente.setProcessoExpediente(ProcessoParteExpedienteHome.instance().getInstance()
						.getProcessoExpediente());
				getEntityManager().persist(processoDocumentoExpediente);
				Events.instance().raiseEvent(EventsTreeHandler.REGISTRA_EVENTO_EVENT_SEM_BPM, texto);
			}

			Boolean sigilo = texto.getDocumentoSigiloso();
			String certChain = processoDocBinHome.getInstance().getCertChain();
			String signature = processoDocBinHome.getInstance().getSignature();

			iniciarDocumentos();
			processoDocHome.getInstance().setDocumentoSigiloso(sigilo);
			processoDocBinHome.getInstance().setCertChain(certChain);
			processoDocBinHome.getInstance().setSignature(signature);
		}

		if (fileData.getFileName() != null && !fileData.getFileName().isEmpty()) {

			FileUpload.instance().setFile(fileData);
			// Comentado na reintegração da capela_1.2.0.M6 e descanso_1.4.0.M4
			// ProcessoDocumentoHome.instance().setModeloSemNewInstance(Boolean.FALSE);
			ProcessoDocumentoHome.instance().setModelo(false, false);
			if (fileData.getFileName() == null || fileData.getFileName().isEmpty()) {
				FacesMessages.instance().add(Severity.ERROR, "Selecione algum arquivo .pdf");
				return "problem";
			} else if (!fileData.getExtensao().equalsIgnoreCase(".pdf")) {
				FacesMessages.instance().add(Severity.ERROR, "O arquivo deve ser pdf");
				return null;
			}
			if (processoDocHome.persistComAssinatura() != null) {
				anexo = processoDocHome.getInstance();

				ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome.instance()
						.getInstance();
				processoDocumentoExpediente.setAnexo(Boolean.TRUE);
				processoDocumentoExpediente.setProcessoDocumento(anexo);
				processoDocumentoExpediente.setProcessoExpediente(ProcessoParteExpedienteHome.instance().getInstance()
						.getProcessoExpediente());
				getEntityManager().persist(processoDocumentoExpediente);

				Events.instance().raiseEvent(EventsTreeHandler.REGISTRA_EVENTO_EVENT_SEM_BPM, anexo);
			}
		}

		if (anexo == null) {
			getInstance().setProcessoDocumento(texto);
		} else {
			getInstance().setProcessoDocumento(anexo);
		}

		persist();

		if (getInstance().getResultado().equals(TipoResultadoAvisoRecebimentoEnum.R)) {
			ProcessoParteExpediente ppe = ProcessoParteExpedienteHome.instance().getInstance();
			if (ppe.getDtCienciaParte() == null || ppe.getDtCienciaParte().after(getInstance().getData())) {
				ppe.setDtCienciaParte(getInstance().getData());
				ppe.setCienciaSistema(Boolean.FALSE);
				PrazosProcessuaisService prazosService = new PrazosProcessuaisServiceImpl();
				OrgaoJulgador o = ppe.getProcessoJudicial().getOrgaoJulgador();
				Calendario calendario = prazosService.obtemCalendario(o);
				Date dataFinal = prazosService.calculaPrazoProcessual(ppe.getDtCienciaParte(), ppe.getPrazoLegal(),
						TipoPrazoEnum.D, calendario, ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(),
						ContagemPrazoEnum.C); 
				ppe.setDtPrazoLegal(dataFinal);
				getEntityManager().merge(ppe);
				getEntityManager().flush();
			}
		}

		newInstance();

		Events.instance().raiseEvent(AVISO_REGISTRO_INTIMACAO);
		setIsGravado(Boolean.TRUE);
		refreshGrid("expedientePendenteOrgaoJulgadorGrid");
		return "persisted";

	}
	
	public AnexarDocumentos getAnexarDocumentos() {
		return anexarDocumentos;
	}

	public void setAnexarDocumentos(AnexarDocumentos anexarDocumentos) {
		this.anexarDocumentos = anexarDocumentos;
	}

	public ProcessoDocumento getPdPdf() {
		return pdPdf;
	}

	public void setPdPdf(ProcessoDocumento pdPdf) {
		this.pdPdf = pdPdf;
	}

	public ProcessoDocumentoBin getPdbPdf() {
		return pdbPdf;
	}

	public void setPdbPdf(ProcessoDocumentoBin pdbPdf) {
		this.pdbPdf = pdbPdf;
	}
	
}
