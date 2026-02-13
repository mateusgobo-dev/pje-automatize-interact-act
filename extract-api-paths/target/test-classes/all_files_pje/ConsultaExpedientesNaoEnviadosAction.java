package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.pje.list.ConsultaExpedienteNaoEnviadoList;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ConsultaExpedientesNaoEnviadosAction.NAME)
public class ConsultaExpedientesNaoEnviadosAction extends BaseAction<ProcessoExpediente> implements Serializable{

	private static final long serialVersionUID = 7432126403446083866L;
	private static final LogProvider log = Logging.getLogProvider(ConsultaExpedientesNaoEnviadosAction.class);
	public static final String NAME = "consultaExpedientesNaoEnviadosAction";

	@In
	private transient ProcessoExpedienteManager processoExpedienteManager;

	@In
	private transient TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	private boolean marcarDesmarcarChecks;
	private List<TipoProcessoDocumento> tipoExpedienteList;
	private List<TipoProcessoDocumento> tipoDocumentoAtoMagistradoList;
	private List<ProcessoExpediente> processoExpedienteListMarcado = new ArrayList<ProcessoExpediente>(0);

	public String getDescricaoTipoDocumentoAtoMagistrado(ProcessoExpediente processoExpediente){
		ProcessoDocumento processoDocumentoAto = processoExpedienteManager.getProcessoDocumentoAto(processoExpediente);
		return processoDocumentoAto != null ? processoDocumentoAto.getTipoProcessoDocumento()
				.getTipoProcessoDocumento() : "-";
	}

	public String getDestinatariosDoExpediente(ProcessoExpediente processoExpediente){
		List<Object> destinatarios = new ArrayList<Object>();
		for (ProcessoParteExpediente processoParteExpediente : processoExpediente.getProcessoParteExpedienteList()){
			if (processoParteExpediente.getPessoaParte() != null){
				destinatarios.add(processoParteExpediente.getPessoaParte().getNome());
			}
		}
		return StringUtil.concatList(destinatarios, ", ");
	}

	public List<TipoProcessoDocumento> getTipoDocumentoAtoMagistradoList(){
		if (tipoDocumentoAtoMagistradoList == null){
			tipoDocumentoAtoMagistradoList = tipoProcessoDocumentoManager.getTipoDocumentoAtoMagistradoList();
		}
		return tipoDocumentoAtoMagistradoList;
	}

	public List<TipoProcessoDocumento> getTipoExpedienteList(){
		if (tipoExpedienteList == null){
			tipoExpedienteList = tipoProcessoDocumentoManager.getTipoProcessoDocumentoExpedienteList();
		}
		return tipoExpedienteList;
	}

	public ExpedicaoExpedienteEnum[] getMeioExpedicaoValues(){
		return ExpedicaoExpedienteEnum.values();
	}

	public void remove(ProcessoExpediente processoExpediente){
		processoExpediente.setDtExclusao(new Date());
		try{
			processoExpedienteManager.persist(processoExpediente);
		} catch (PJeBusinessException e){
			log.error(e.getMessage());
		}
		FacesMessages.instance().add(Severity.INFO, "Registro excluído com sucesso.");
	}

	public void setMarcarDesmarcarChecks(boolean marcarDesmarcarChecks){
		this.marcarDesmarcarChecks = marcarDesmarcarChecks;

		List<ProcessoExpediente> processoExpedienteList = ConsultaExpedienteNaoEnviadoList.instance().list();
		for (ProcessoExpediente pe : processoExpedienteList){
			pe.setCheckado(marcarDesmarcarChecks);
		}

		getProcessoExpedienteListMarcado().clear();
		if (marcarDesmarcarChecks){
			getProcessoExpedienteListMarcado().addAll(processoExpedienteList);
		}
	}

	public boolean isMarcarDesmarcarChecks(){
		return marcarDesmarcarChecks;
	}

	public void atualizarListaExpedientes(ProcessoExpediente processoExpediente){
		if (processoExpediente.getCheckado()){
			getProcessoExpedienteListMarcado().add(processoExpediente);
		}
		else{
			getProcessoExpedienteListMarcado().remove(processoExpediente);
		}
	}

	public void setProcessoExpedienteListMarcado(List<ProcessoExpediente> processoExpedienteListMarcado){
		this.processoExpedienteListMarcado = processoExpedienteListMarcado;
	}

	public List<ProcessoExpediente> getProcessoExpedienteListMarcado(){
		return processoExpedienteListMarcado;
	}

	@Override
	protected ProcessoExpedienteManager getManager(){
		return this.processoExpedienteManager;
	}

	@Override
	public EntityDataModel<ProcessoExpediente> getModel() {
		throw new UnsupportedOperationException("Não implementado.");
	}

}