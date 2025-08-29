package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.ProcessoParteExpedienteHome;
import br.com.infox.pje.list.ConsultaPrazosList;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.SimNaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ConsultaPrazosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConsultaPrazosAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6731202989391581101L;

	public static final String NAME = "consultaPrazosAction";

	@In
	private transient ProcessoTrfManager processoTrfManager;
	@In
	private transient ProcessoExpedienteManager processoExpedienteManager;
	@In
	private transient ProcessoParteExpedienteManager processoParteExpedienteManager;
	@In
	private transient TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	private boolean checkMarcarTodos = false;
	private List<TipoProcessoDocumento> tipoDocumentoAtoMagistradoList;
	private List<ProcessoParteExpediente> expedienteMarcadoList = new ArrayList<ProcessoParteExpediente>();
	// variável que armazena os expedientes marcados para serem fechados que ainda não estão vencidos para sinalizar para o usuário
	private List<ProcessoParteExpediente> expedientePrazoNaoVencidoList = new ArrayList<ProcessoParteExpediente>();

	public List<TipoProcessoDocumento> getTipoDocumentoAtoMagistradoList(){
		if (tipoDocumentoAtoMagistradoList == null){
			tipoDocumentoAtoMagistradoList = tipoProcessoDocumentoManager.getTipoDocumentoAtoMagistradoList();
		}
		return tipoDocumentoAtoMagistradoList;
	}

	public String getPrioridadesDoProcesso(ProcessoTrf processoTrf){
		List<Object> prioridades = new ArrayList<Object>();
		for (PrioridadeProcesso prioridadeProcesso : processoTrf.getPrioridadeProcessoList()){
			prioridades.add(prioridadeProcesso.getPrioridade());
		}
		return StringUtil.concatList(prioridades, ", ");
	}

	public String getNomeTarefaAtual(ProcessoTrf processoTrf){
		return processoTrfManager.getNomeTarefaAtual(processoTrf);
	}


	public String getDescricaoTipoDocumentoAtoMagistrado(ProcessoExpediente processoExpediente){
		ProcessoDocumento processoDocumentoAto = processoExpedienteManager.getProcessoDocumentoAto(processoExpediente);
		return processoDocumentoAto != null ? processoDocumentoAto.getTipoProcessoDocumento()
				.getTipoProcessoDocumento() : "-";
	}

	public SimNaoEnum[] situacaoPrazoValues(){
		return SimNaoEnum.values();
	}

	public SimNaoEnum[] situacaoExpedienteValues(){
		return SimNaoEnum.values();
	}

	/**
	 * Método responsável por deixar marcado/desmarcado o valor referente ao checkbox do expediente passado como parâmetro
	 * @param expediente expediente cujo checkbox terá seu valor alterado
	 */
	public void atualizarExpedienteMarcado(ProcessoParteExpediente expediente){
		if (!expedienteMarcadoList.contains(expediente)){
			expedienteMarcadoList.add(expediente);
			if (processoParteExpedienteManager.isExpedienteNaoVencido(expediente)){
				expedientePrazoNaoVencidoList.add(expediente);
			}
		} else{
			expedienteMarcadoList.remove(expediente);
			expedientePrazoNaoVencidoList.remove(expediente);
		}
	}

	/**
	 * Método responsável por selecionar todos os expedientes abertos listados na última consulta realizada 
	 */
	public void atualizarTodosExpedientesMarcados(boolean checkMarcarTodos){
		List<ProcessoParteExpediente> expedienteList = ConsultaPrazosList.instance().list();
		expedienteMarcadoList.clear();
		expedientePrazoNaoVencidoList.clear();
		setCheckMarcarTodos(checkMarcarTodos);
		if (checkMarcarTodos) {
			for (ProcessoParteExpediente expediente : expedienteList) {
				if (Boolean.FALSE.equals(expediente.getFechado())) {
					atualizarExpedienteMarcado(expediente);
				}
			}
		}
	}

	public boolean contemExpedienteNaoVencido(){
		return expedientePrazoNaoVencidoList.size() > 0;
	}

	public void fecharExpedientes(){
		try{
			for (ProcessoParteExpediente expediente : expedienteMarcadoList){
				processoParteExpedienteManager.fecharExpediente(expediente);
			}
			FacesMessages.instance().add(Severity.INFO,"Expedientes fechados com sucesso");
		} catch( PJeBusinessException excecao ) {
			FacesMessages.instance().add(Severity.INFO,"Erro ao tentar fechar os expedientes");
		}
	}

	public void cancelarExpedientes() {
		try {
			Integer expedientesCancelados = 0;
			Integer expedientesNaoCancelados = 0;

			FacesMessages.instance().clear();

			for (ProcessoParteExpediente expediente : expedienteMarcadoList) {
				try {
					if (ProcessoParteExpedienteHome.instance().isPodeCancelaPublicacao(expediente)) {
						ProcessoParteExpedienteHome.instance().cancelaPublicacao(expediente);

						++expedientesCancelados;
					} else {
						String mensagem = "Expediente com id '" + expediente.getIdProcessoParteExpediente()
								+ "' não pode ser enviado ao diário eletrônico para o cancelamento. Prováveis motivos: "
								+ "1) O meio do expediente não é do tipo Diário Eletrônico "
								+ "2) O expediente já foi publicado "
								+ "3) A data de expectativa de publicação do expediente já foi alcançada "
								+ "4) O envio do expediente já foi cancelado ";

						FacesMessages.instance().add(Severity.ERROR, mensagem);

						++expedientesNaoCancelados;
					}
				} catch (PontoExtensaoException | InterruptedException e) {
					++expedientesNaoCancelados;

					e.printStackTrace();
				}
			}

			if (expedientesCancelados > 0 && expedientesNaoCancelados == 0) {
				FacesMessages.instance().add(Severity.INFO,
						"Envio dos cancelamentos dos expedientes enviados ao diário eletrônico com sucesso");
			} else if (expedientesCancelados > 0 && expedientesNaoCancelados > 0) {
				FacesMessages.instance().add(Severity.WARN,
						"Somente alguns expedientes tiveram o envio dos cancelamentos dos expedientes enviados ao diário eletrônico com sucesso");
			}
		} catch (PJeBusinessException excecao) {
			FacesMessages.instance().add(Severity.INFO, "Erro ao tentar cancelar os expedientes");
		}
	}

	public void setCheckMarcarTodos(boolean checkMarcarTodos){
		this.checkMarcarTodos = checkMarcarTodos;
	}

	public boolean isCheckMarcarTodos(){
		return checkMarcarTodos;
	}

	public void setExpedienteMarcadoList(List<ProcessoParteExpediente> expedienteMarcadoList){
		this.expedienteMarcadoList = expedienteMarcadoList;
	}

	public List<ProcessoParteExpediente> getExpedienteMarcadoList(){
		return expedienteMarcadoList;
	}
	
	public boolean podeFecharExpedientes() {
		return Identity.instance().hasRole(Papeis.PODE_FECHAR_EXPEDIENTES);
	}
}