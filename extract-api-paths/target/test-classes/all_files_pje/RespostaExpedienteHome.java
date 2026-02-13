package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.component.FileData;
import br.com.infox.ibpm.component.FileUpload;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.RespostaExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;

@Name("respostaExpedienteHome")
@BypassInterceptors
public class RespostaExpedienteHome extends AbstractHome<RespostaExpediente> {

	private static final long serialVersionUID = 1L;
	private Boolean isGravado = Boolean.FALSE;
	private List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>(0);
	private List<ProcessoParteExpediente> processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>(0);
	private List<ProcessoParteExpediente> processoParteExpedienteRespondidosList = new ArrayList<ProcessoParteExpediente>(
			0);

	public static RespostaExpedienteHome instance() {
		return ComponentUtil.getComponent("respostaExpedienteHome");
	}

	public void setRespostaExpedienteIdRespostaExpediente(Integer id) {
		setId(id);
	}

	public Integer getRespostaExpedienteIdRespostaExpediente() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		iniciarDocumentos();
		super.newInstance();
		setIsGravado(Boolean.FALSE);
		getDocumentos().clear();
		processoParteExpedienteRespondidosList.clear();
		processoParteExpedienteList.clear();

	}

	public void iniciarDocumentos() {
		ProcessoDocumentoExpedienteHome.instance().newInstance();

		ProcessoDocumentoHome processoDocumentoHome = ProcessoDocumentoHome.instance();
		processoDocumentoHome.newInstance();
		ProcessoDocumentoBinHome.instance().newInstance();
		processoDocumentoHome.getInstance().setProcessoDocumento("Petição");
		// processoDocumentoHome.getInstance().setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoPeticaoTerceiro());
		//processoDocumentoHome.getInstance().setProcesso(
			//	ProcessoParteExpedienteHome.instance().getInstance().getProcessoExpediente().getProcessoTrf()
				//		.getProcesso());
		ProcessoHome.instance().setId(ProcessoParteExpedienteHome.instance().getInstance().getProcessoExpediente().getProcessoTrf()
						.getProcesso().getIdProcesso());
	}

	public String inserirAtualizarDoc() {
		int count = 0;
		for (ProcessoParteExpediente ppe : getProcessoParteExpedienteList()) {
			if (ppe.getCheck()) {
				count++;
				break;
			}
		}
		if (count == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione ao menos um expediente.");
			return "problem";
		}
		// ProcessoDocumento texto = null;
		// ProcessoDocumento anexo = null;
		// getInstance().setProcessoParteExpediente(ProcessoParteExpedienteHome.instance().getInstance());
		ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome.instance();
		ProcessoDocumentoBinHome processoDocBinHome = ProcessoDocumentoBinHome.instance();
		FileData fileData = FileUpload.instance().getFile();

		if (processoDocBinHome.getInstance() != null && processoDocBinHome.getInstance().getModeloDocumento() != null
				&& !processoDocBinHome.getInstance().getModeloDocumento().isEmpty()) {

			ProcessoDocumentoHome.instance().setModelo(Boolean.TRUE);

		}

		if (fileData.getFileName() != null && !fileData.getFileName().isEmpty()) {
			ProcessoDocumentoHome.instance().setModelo(Boolean.FALSE);
			if (fileData.getFileName() == null || fileData.getFileName().isEmpty()) {
				FacesMessages.instance().add(Severity.ERROR, "Selecione algum arquivo .pdf");
				return "problem";
			} else {
				if (!fileData.getExtensao().equalsIgnoreCase(".pdf")) {
					FacesMessages.instance().add(Severity.ERROR, "O arquivo deve ser pdf");
					return null;
				}
				if (fileData.getTamanho() > 5242880) {
					FacesMessages.instance().add(StatusMessage.Severity.ERROR,
							"O documento deve ter o tamanho máximo de 5MB!");
					return "";
				}
			}
		}

		if (isGravado && (fileData.getFileName() == null || fileData.getFileName().isEmpty())) {
			FacesMessages.instance().addToControl("upload", StatusMessage.Severity.ERROR, "Escolha um arquivo");
			return null;
		}

		if (processoDocHome.persistComAssinatura() != null) {
			getDocumentos().add(processoDocHome.getInstance());
			Events.instance().raiseEvent(AutomaticEventsTreeHandler.REGISTRA_EVENTO_EVENT_SEM_BPM,
					processoDocHome.getInstance());

			if (ProcessoDocumentoHome.instance().getModelo()) {
				ProcessoDocumentoExpediente processoDocumentoExpediente = ProcessoDocumentoExpedienteHome.instance()
						.getInstance();
				processoDocumentoExpediente.setAnexo(Boolean.TRUE);
				processoDocumentoExpediente.setProcessoDocumento(processoDocHome.getInstance());
				processoDocumentoExpediente.setProcessoExpediente(ProcessoParteExpedienteHome.instance().getInstance()
						.getProcessoExpediente());
				getEntityManager().persist(processoDocumentoExpediente);
				getInstance().setProcessoDocumento(processoDocHome.getInstance());
				getInstance().setData(new Date());
				persist();
				for (ProcessoParteExpediente ppe : getProcessoParteExpedienteList()) {
					if (ppe.getCheck()) {
						// ProcessoParteExpediente ppe =
						// ProcessoParteExpedienteHome.instance().getInstance();
						if (ppe.getDtCienciaParte() == null) {
							ppe.setDtCienciaParte(getInstance().getData());
							ppe.setCienciaSistema(Boolean.FALSE);
							ppe.setFechado(true);
							ppe.setPendenteManifestacao(false);
						}
						if (ppe.getPrazoLegal() != null && ppe.getPrazoLegal() > 0 && ppe.getDtPrazoLegal() == null) {
							PrazosProcessuaisService prazosService = new PrazosProcessuaisServiceImpl();
							OrgaoJulgador o = ppe.getProcessoJudicial().getOrgaoJulgador();
							Calendario calendario = prazosService.obtemCalendario(o);
							Date dataFinal = prazosService.calculaPrazoProcessual(ppe.getDtCienciaParte(),
									ppe.getPrazoLegal(), TipoPrazoEnum.D, calendario, ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(),
									ContagemPrazoEnum.M);
							ppe.setDtPrazoLegal(dataFinal);
						}
						ppe.setResposta(getInstance());
						getEntityManager().merge(ppe);
						getEntityManager().flush();
						Events.instance().raiseEvent(Eventos.EVENTO_PRECLUSAO_MANIFESTACAO, ppe.getProcessoJudicial());
						getProcessoParteExpedienteRespondidosList().add(ppe);
					}
				}
				// ppe.setDtPrazoLegal(new Date());
				setIsGravado(Boolean.TRUE);
			}

			TipoProcessoDocumento tp = processoDocHome.getInstance().getTipoProcessoDocumento();
			Boolean sigilo = processoDocHome.getInstance().getDocumentoSigiloso();

			iniciarDocumentos();
			ProcessoDocumentoHome.instance().getInstance().setTipoProcessoDocumento(tp);
			ProcessoDocumentoHome.instance().onSelectProcessoDocumento();
			ProcessoDocumentoHome.instance().getInstance().setDocumentoSigiloso(sigilo);

			refreshGrid("expedientePendenteGrid");
			refreshGrid("expedienteConfirmadoIntimadoGrid");
			refreshGrid("expedienteConfirmadoSistemaGrid");
			refreshGrid("expedienteSemPrazoGrid");
			refreshGrid("expedienteRespondidoGrid");
			return "persisted";
		} else {
			return "problem";
		}

	}

	public TipoResultadoAvisoRecebimentoEnum[] getTipoResultadoAvisoRecebimentoItems() {
		return TipoResultadoAvisoRecebimentoEnum.values();
	}

	public void setIsGravado(Boolean isGravado) {
		this.isGravado = isGravado;
	}

	public Boolean getIsGravado() {
		return isGravado;
	}

	public void setDocumentos(List<ProcessoDocumento> documentos) {
		this.documentos = documentos;
	}

	public List<ProcessoDocumento> getDocumentos() {
		return documentos;
	}

	public boolean validaPagina() {
		ProcessoParteExpediente ppe = ProcessoParteExpedienteHome.instance().getInstance();
		if (ppe != null && ppe.getResposta() != null) {
			return false;
		}
		return true;
	}

	public void setProcessoParteExpedienteList(List<ProcessoParteExpediente> processoParteExpedienteList) {
		this.processoParteExpedienteList = processoParteExpedienteList;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getProcessoParteExpedienteList() {
		if (processoParteExpedienteList.size() == 0) {
			String consultaProcessosRepresentados = "SELECT DISTINCT ppe " + "	FROM ProcessoParteExpediente ppe "
					+ "	WHERE ppe.resposta is null " + "   AND ppe.pessoaParte IN (:participacoes) "
					+ "	AND ppe.processoJudicial = :processoTrf";
			Query q = getEntityManager().createQuery(consultaProcessosRepresentados);
			List<Pessoa> pessoasRepresentadas = PessoaAdvogadoHome.instance().getPessoasRepresentadas();
			q.setParameter("participacoes", Util.isEmpty(pessoasRepresentadas)?null:pessoasRepresentadas);
			q.setParameter("processoTrf", ProcessoParteExpedienteHome.instance().getInstance().getProcessoExpediente()
					.getProcessoTrf());
			processoParteExpedienteList = q.getResultList();
			ProcessoParteExpedienteHome.instance().getInstance().setCheck(Boolean.TRUE);
		}

		return processoParteExpedienteList;
	}

	public void setProcessoParteExpedienteRespondidosList(
			List<ProcessoParteExpediente> processoParteExpedienteRespondidosList) {
		this.processoParteExpedienteRespondidosList = processoParteExpedienteRespondidosList;
	}

	public List<ProcessoParteExpediente> getProcessoParteExpedienteRespondidosList() {
		return processoParteExpedienteRespondidosList;
	}

}
