package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ClientErrorException;

import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.model.dto.ProcessoParteCloudEvent;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.criminal.OrgaoProcedimentoOriginarioRestClient;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoProcedimentoOrigemRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoOrigemRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoProcedimentoOrigemRestClient;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

@Name("processoProcedimentoOrigemAction")
@Scope(ScopeType.CONVERSATION)
public class ProcessoProcedimentoOrigemAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<TipoOrigemDTO> tipoOrigemList = new ArrayList<>();
	private List<TipoProcedimentoOrigemDTO> tipoProcedimentoOrigemList = new ArrayList<>();
	private List<OrgaoProcedimentoOriginarioDTO> orgaoList = new ArrayList<>();

	private ProcessoProcedimentoOrigemDTO processoProcedimentoOrigemDTO = new ProcessoProcedimentoOrigemDTO();
	private ProcessoRascunho processoRascunho;
	private ProcessoCriminalDTO processoCriminalDTO = new ProcessoCriminalDTO();

	private ProcessoRascunhoManager processoRascunhoManager = ComponentUtil.getComponent(ProcessoRascunhoManager.NAME);

	private Estado estadoDelegacia;

	private List<ProcessoProcedimentoOrigemDTO> procedimentoRetombadoList = new ArrayList<>();
	
	@Logger
	private transient Log log;

	public static final String REGISTRO_INSERIDO_SUCESSO = "Registro inserido com sucesso.";
	public static final String REGISTRO_ALTERADO_SUCESSO = "Registro alterado com sucesso.";

	public static ProcessoProcedimentoOrigemAction instance() {
		return ComponentUtil.getComponent("processoProcedimentoOrigemAction");
	}
	
	@Create
	public void create()  {
		if (isRetificacao()) {
			ProcessoTrfHome.instance().iniciarClasseJudicial();
			
			if (ProcessoTrfHome.instance().isClasseCriminalOuInfracional()) {
				ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil
						.getComponent(ProcessoCriminalRestClient.class);
				
				try{
				processoCriminalDTO = processoCriminalRestClient
						.getResourceByProcesso(getProcessoTrf().getNumeroProcesso());
				}catch (PJeException pje) {
					FacesMessages.instance().add(Severity.ERROR, pje.getCode());
				}catch (Exception e) {
					FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro: " + e.getLocalizedMessage());
				}
			}
		} else {			
			recuperarDadosRascunhoProcessoCriminal();
		}
		
	}	
	
	private void recuperarDadosRascunhoProcessoCriminal() {
		processoRascunho = processoRascunhoManager.recuperarRascunhoPeloProcesso(getProcessoTrf());
		if (processoRascunho != null) {
			processoCriminalDTO = processoRascunhoManager.recuperarRascunhoProcessoCriminal(getProcessoTrf());

			if (processoCriminalDTO == null) {
				processoCriminalDTO = new ProcessoCriminalDTO();
			}
		} else {
			processoRascunho = new ProcessoRascunho();
			processoRascunho.setProcesso(getProcessoTrf());
		}		
	}
	
	/**
	 * Metodo responsavel por carregar os dados armazenados no servico do Criminal.
	 * Caso o processo nao esteja cadastrado no Criminal, cria-se um objeto vazio
	 * para evitar erros de Nullpointer no momento que a aba eh chamada.
	 */
	public void carregaProcessoCriminalDTO() {
		ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil.getComponent(ProcessoCriminalRestClient.class);
		
		try{
			if(isRetificacao() && Boolean.TRUE.equals(ProcessoTrfHome.instance().getEhClasseCriminal())) {
				processoCriminalDTO = processoCriminalRestClient.getResourceByProcesso(getProcessoTrf().getNumeroProcesso());
				
				if (processoCriminalDTO == null) {
					processoCriminalDTO = new ProcessoCriminalDTO();
					processoCriminalDTO.setNrProcesso(getProcessoTrf().getNumeroProcesso());
				}
			}
		}catch (PJeException pjeException) {
			FacesMessages.instance().add(Severity.ERROR, pjeException.getCode());
			log.error("Erro ao consultar o processo " + getProcessoTrf().getNumeroProcesso() + " no serviço do Criminal", pjeException);
		}catch (Exception exception) {
			FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro: " + exception.getLocalizedMessage());
			log.error("Erro ao consultar o processo " + getProcessoTrf().getNumeroProcesso() + " no serviço do Criminal", exception);
		}
	}
	

	public void atualizarCombosDependentes() {
		try {
			if (processoProcedimentoOrigemDTO != null && processoProcedimentoOrigemDTO.getTipoOrigem() != null) {
				TipoProcedimentoOrigemRestClient tipoProcedimentoOrigemRestClient = ComponentUtil
						.getComponent(TipoProcedimentoOrigemRestClient.NAME);
				OrgaoProcedimentoOriginarioRestClient orgaoProcedimentoOriginarioRestClient = ComponentUtil
						.getComponent(OrgaoProcedimentoOriginarioRestClient.NAME);
				if (processoProcedimentoOrigemDTO.getUf() != null) {
					orgaoList = CollectionUtilsPje.ordenarLista(
										orgaoProcedimentoOriginarioRestClient.findByTipoOrigemAndUf(
													processoProcedimentoOrigemDTO.getTipoOrigem().getId(),
													processoProcedimentoOrigemDTO.getUf()),
										"dsNomeOrgao");
				}
				tipoProcedimentoOrigemList = CollectionUtilsPje.ordenarLista(
												tipoProcedimentoOrigemRestClient.findByTipoOrigem(processoProcedimentoOrigemDTO.getTipoOrigem().getId()),
												"dsTipoProcedimento");
			} else {
				tipoProcedimentoOrigemList = null;
				orgaoList = null;
			}
		} catch (Exception ex) {
			throw new PJeRuntimeException("Serviço Criminal indisponível no momento! Erro: " + ex.getMessage(), ex);
		}
	}

	public void atualizarListaTipoOrigem() {
		if (getTipoOrigemList() == null || getTipoOrigemList().isEmpty()) {
			TipoOrigemRestClient client = ComponentUtil.getComponent(TipoOrigemRestClient.NAME);
			try {
				setTipoOrigemList(CollectionUtilsPje.ordenarLista(client.getResources(), "dsTipoOrigem" ));
			} catch (Exception e) {
				log.error("Erro ao atualizar a lista Tipo Origem", e);
				setTipoOrigemList(null);
			}
		}
	}

	public void atualizarDelegacias() throws ClientErrorException, PJeException {
		if (estadoDelegacia != null) {
			processoProcedimentoOrigemDTO.setUf(estadoDelegacia.getCodEstado());
			if (processoProcedimentoOrigemDTO.getTipoOrigem() != null) {
				OrgaoProcedimentoOriginarioRestClient orgaoProcedimentoOriginarioRestClient = ComponentUtil
						.getComponent(OrgaoProcedimentoOriginarioRestClient.NAME);
				orgaoList = orgaoProcedimentoOriginarioRestClient.findByTipoOrigemAndUf(
						processoProcedimentoOrigemDTO.getTipoOrigem().getId(), processoProcedimentoOrigemDTO.getUf());
			}
		}
	}

	public void atualizarProcedimentos() {
		if (processoCriminalDTO.getProcessoProcedimentoOrigemList() != null
				&& !processoCriminalDTO.getProcessoProcedimentoOrigemList().isEmpty()) {
			procedimentoRetombadoList = new ArrayList<>(processoCriminalDTO.getProcessoProcedimentoOrigemList());
		} else {
			procedimentoRetombadoList = new ArrayList<>();
		}
		if (processoProcedimentoOrigemDTO != null && processoProcedimentoOrigemDTO.getId() != null) {
			procedimentoRetombadoList.remove(processoProcedimentoOrigemDTO);
		}
	}

	public void atualizarCamposProcedimentoRetombado() throws Exception {
		processoProcedimentoOrigemDTO
				.setUf(processoProcedimentoOrigemDTO.getProcessoProcedimentoOrigemRetombado().getUf());
		updateEstadoDelegacia();
		processoProcedimentoOrigemDTO
				.setTipoOrigem(processoProcedimentoOrigemDTO.getProcessoProcedimentoOrigemRetombado().getTipoOrigem());
		atualizarCombosDependentes();
		processoProcedimentoOrigemDTO.setOrgaoProcedimentoOriginario(processoProcedimentoOrigemDTO
				.getProcessoProcedimentoOrigemRetombado().getOrgaoProcedimentoOriginario());
		processoProcedimentoOrigemDTO.setTipoProcedimentoOrigem(
				processoProcedimentoOrigemDTO.getProcessoProcedimentoOrigemRetombado().getTipoProcedimentoOrigem());
	}

	public void newInstance() {
		processoProcedimentoOrigemDTO = new ProcessoProcedimentoOrigemDTO();
		estadoDelegacia = null;
		carregaProcessoCriminalDTO(); 
		
		// Informa ao usuario que o processo nao esta cadastrado no Criminal e solicita que os dados
		// referentes ao Local do Fato sejam preenchidos antes do preenchimento da aba Procedimento de Origem.
		if(isRetificacao() && ( this.processoCriminalDTO == null || this.processoCriminalDTO.getId() == null) ) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "processoProcedimentoOrigem.erro.localFatoNaoPreenchido");
		}
		
		atualizarListaTipoOrigem();
		atualizarProcedimentos();
	}

	protected boolean beforePersistOrUpdate() {
		return validarProcessoCriminalDTO() && validarProcedimentoOrigemDTO();
	}


	private boolean validarProcedimentoOrigemDTO() {

		try{

			if (processoProcedimentoOrigemDTO == null || processoProcedimentoOrigemDTO.getTipoOrigem() == null) {
				throw new PJeRuntimeException("processoProcedimentoOrigem.erroTipoProcedimento");
			}

			if (Boolean.TRUE.equals(processoProcedimentoOrigemDTO.getTipoOrigem().getInObrigatorioNumeroOrigem())) {
				validarDataInstauracao();
				verificarPreenchimento();
				validarAno();
				validarProcedimentoOrigemList();
			}

			return true;
		}catch(PJeRuntimeException e){
			FacesMessages.instance().addFromResourceBundle(e.getCode());
			return false;
		}

	}

	private void validarDataInstauracao() {
		if (processoProcedimentoOrigemDTO.getDataInstauracao().after(new Date())) {
			throw new PJeRuntimeException("processoProcedimentoOrigem.erroDataInstauracaoSuperior");
		}
	}

	private void validarAno() {
		Calendar anoAtual = Calendar.getInstance();
		if (processoProcedimentoOrigemDTO.getAno() > anoAtual.get(Calendar.YEAR)) {
			throw new PJeRuntimeException("processoProcedimentoOrigem.errAnoProcedimentoSuperior");
		}
	}

	private void validarProcedimentoOrigemList() {
		if (!CollectionUtilsPje.isEmpty(processoCriminalDTO.getProcessoProcedimentoOrigemList())) {
			for (ProcessoProcedimentoOrigemDTO procProcedimentoOrigem : processoCriminalDTO
					.getProcessoProcedimentoOrigemList()) {
				if ( 	procProcedimentoOrigem.getTipoProcedimentoOrigem() != null &&
						procProcedimentoOrigem.getTipoProcedimentoOrigem().getId() !=null &&
						procProcedimentoOrigem.getNumero() != null &&
						procProcedimentoOrigem.getAno() != null &&
						processoProcedimentoOrigemDTO.getTipoProcedimentoOrigem() != null &&
						processoProcedimentoOrigemDTO.getTipoProcedimentoOrigem().getId() != null &&
						processoProcedimentoOrigemDTO.getNumero() != null &&
						this.processoProcedimentoOrigemDTO.getAno() != null &&
						procProcedimentoOrigem.getTipoProcedimentoOrigem().getId().equals(processoProcedimentoOrigemDTO.getTipoProcedimentoOrigem().getId()) &&
						procProcedimentoOrigem.getNumero().equals(processoProcedimentoOrigemDTO.getNumero()) &&
						procProcedimentoOrigem.getAno().equals(this.processoProcedimentoOrigemDTO.getAno())
				) {
					throw new PJeRuntimeException("processoProcedimentoOrigem.errNumAnoProcedSuperior");
				}
			}
		}
	}

	private boolean validarProcessoCriminalDTO() {
		if(isRetificacao() && ( this.processoCriminalDTO == null || this.processoCriminalDTO.getId() == null) ) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "processoProcedimentoOrigem.erro.localFatoNaoPreenchido");
			return false;
		}
		return true;
	}

	private void verificarPreenchimento() {
		if (Boolean.TRUE.equals(this.processoProcedimentoOrigemDTO.getTipoOrigem().getInObrigatorioNumeroOrigem())) {
			if (this.processoProcedimentoOrigemDTO.getOrgaoProcedimentoOriginario() == null) {
				throw new PJeRuntimeException("processoProcedimentoOrigem.orgaoProcedimentoOrigem");
			} else if (this.processoProcedimentoOrigemDTO.getTipoProcedimentoOrigem() == null) {
				throw new PJeRuntimeException("processoProcedimentoOrigem.tipoProcedimentoOrigem");
			} else if (this.processoProcedimentoOrigemDTO.getDataInstauracao() == null) {
				throw new PJeRuntimeException("processoProcedimentoOrigem.dataInstauracao");
			} else if (this.processoProcedimentoOrigemDTO.getNumero() == null) {
				throw new PJeRuntimeException("processoProcedimentoOrigem.numeroProcedimento");
			} else if (this.processoProcedimentoOrigemDTO.getAno() == null) {
				throw new PJeRuntimeException("processoProcedimentoOrigem.anoProcedimento");
			}
		}
	}

	public void incluir() throws ClientErrorException, PJeException {
		if (beforePersistOrUpdate()) {
			if (isRetificacao()) {
				processarRetificacao();
			} else {
				processarInclusao();
			}
			newInstance();
		}
	}

	private void processarRetificacao() throws PJeException {
		ProcessoProcedimentoOrigemRestClient procProcedimentoOrigemRestClient = ComponentUtil
				.getComponent(ProcessoProcedimentoOrigemRestClient.class);
		ProcessoCriminalDTO newProcesso = new ProcessoCriminalDTO();
		newProcesso.setNrProcesso(processoCriminalDTO.getNrProcesso());
		processoProcedimentoOrigemDTO.setProcesso(newProcesso);

		atualizarPartesPoloPassivoNoCriminal();

		if (Boolean.TRUE.equals(!processoProcedimentoOrigemDTO.getRetombamentoRedistribuicao())) {
			processoProcedimentoOrigemDTO.setProcessoProcedimentoOrigemRetombado(null);
		}
		if (!processoCriminalDTO.getProcessoProcedimentoOrigemList().contains(processoProcedimentoOrigemDTO)) {
			processoProcedimentoOrigemDTO = procProcedimentoOrigemRestClient
					.createResource(processoProcedimentoOrigemDTO);
			
			processoCriminalDTO.getProcessoProcedimentoOrigemList().add(processoProcedimentoOrigemDTO);
			
			recuperarDadosRascunhoProcessoCriminal();
			processoCriminalDTO.setPjeOrigem(processoRascunho.getJsonProcessoCriminal().getPjeOrigem());
			processoCriminalDTO.setTipoProcesso(processoRascunho.getJsonProcessoCriminal().getTipoProcesso());
			processarInclusao();
			FacesMessages.instance().add(Severity.INFO, REGISTRO_INSERIDO_SUCESSO);
		} else {
			processoRascunho = processoRascunhoManager.recuperarRascunhoPeloProcesso(getProcessoTrf());
			processoCriminalDTO.setPjeOrigem(processoRascunho.getJsonProcessoCriminal().getPjeOrigem());
			processoCriminalDTO.setTipoProcesso(processoRascunho.getJsonProcessoCriminal().getTipoProcesso());
			processarInclusao();
			
			processoProcedimentoOrigemDTO = procProcedimentoOrigemRestClient
					.updateResource(processoProcedimentoOrigemDTO);
			
			
			FacesMessages.instance().add(Severity.INFO, REGISTRO_ALTERADO_SUCESSO);
		}
	}

	private void processarInclusao() {
		if (processoRascunho == null || processoRascunho.getProcesso() == null) {
			processoRascunho = new ProcessoRascunho();
			processoRascunho.setProcesso(getProcessoTrf());
		}
		if (CollectionUtilsPje.isEmpty(processoCriminalDTO.getProcessoProcedimentoOrigemList())) {
			List<ProcessoProcedimentoOrigemDTO> lista = new ArrayList<>(0);
			lista.add(processoProcedimentoOrigemDTO);
			processoCriminalDTO.setProcessoProcedimentoOrigemList(lista);
			FacesMessages.instance().add(Severity.INFO, REGISTRO_INSERIDO_SUCESSO);
		} else if (!verificarSeRegistoExiste()) {
			processoCriminalDTO.getProcessoProcedimentoOrigemList().add(processoProcedimentoOrigemDTO);
			FacesMessages.instance().add(Severity.INFO, REGISTRO_INSERIDO_SUCESSO);
		} else {
			FacesMessages.instance().add(Severity.INFO, REGISTRO_ALTERADO_SUCESSO);
		}
		persistRascunho();
	}


	/**
	 * Metodo resposavel por atualizar as partes do polo passivo no servico do criminal
	 * ao salvar o Procedimento de Origem na retificacao do processo.
	 */
	private void atualizarPartesPoloPassivoNoCriminal() {
		for (ProcessoParte processoParte : getProcessoTrf().getListaPartePrincipalPassivo()) {
			AMQPEventManager amqpManager = AMQPEventManager.instance();
			amqpManager.enviarMensagem(processoParte, ProcessoParteCloudEvent.class, CloudEventVerbEnum.PATCH);
		}
	}

	private boolean verificarSeRegistoExiste() {
		if (processoProcedimentoOrigemDTO.getId() == null) {
			for (ProcessoProcedimentoOrigemDTO ppo : processoCriminalDTO.getProcessoProcedimentoOrigemList()) {
				if (ppo.hashCode() == processoProcedimentoOrigemDTO.hashCode()) {
					return true;
				}
			}
			return false;
		}
		return processoCriminalDTO.getProcessoProcedimentoOrigemList().contains(processoProcedimentoOrigemDTO);
	}

	private void persistRascunho() {
		try {
			processoRascunho.setJsonProcessoCriminal(processoCriminalDTO);
			processoRascunhoManager.persistAndFlush(processoRascunho);
		} catch (Exception e) {
			log.error("Não foi possível gravar Procedimento de Origem", e);
			throw new RuntimeException("Não foi possível gravar Procedimento de Origem! Erro: " + e.getMessage(), e);
		}
	}

	public void remove(ProcessoProcedimentoOrigemDTO dto) {
		if (isRetificacao()) {
			for (ProcessoProcedimentoOrigemDTO ppo : processoCriminalDTO.getProcessoProcedimentoOrigemList()) {
				if (ppo.getProcessoProcedimentoOrigemRetombado() != null
						&& ppo.getProcessoProcedimentoOrigemRetombado().getId() == dto.getId()) {
					FacesMessages.instance().add(Severity.ERROR, "O procedimento " + ppo.obterDescricao()
							+ " retomba este procedimento, logo não é possível excluí-lo.");
					return;
				}
			}
			processoCriminalDTO.getProcessoProcedimentoOrigemList().remove(dto);
			processoRascunho = processoRascunhoManager.recuperarRascunhoPeloProcesso(getProcessoTrf());
			processoCriminalDTO.setPjeOrigem(processoRascunho.getJsonProcessoCriminal().getPjeOrigem());
			processoCriminalDTO.setTipoProcesso(processoRascunho.getJsonProcessoCriminal().getTipoProcesso());			
			persistRascunho();
			ProcessoProcedimentoOrigemRestClient procProcedimentoOrigemRestClient = ComponentUtil
					.getComponent(ProcessoProcedimentoOrigemRestClient.class);
			procProcedimentoOrigemRestClient.inactivateResource(dto.getId());
		} else {
			processoCriminalDTO.getProcessoProcedimentoOrigemList().remove(dto);
			persistRascunho();
		}
		newInstance();
		FacesMessages.instance().add(Severity.INFO, "Registro removido com sucesso.");
	}

	public void setIdProcedimentoOrigem(ProcessoProcedimentoOrigemDTO dto) throws Exception {
		processoProcedimentoOrigemDTO = dto;
		updateEstadoDelegacia();
		atualizarListaTipoOrigem();
		atualizarCombosDependentes();
		atualizarProcedimentos();
	}

	private void updateEstadoDelegacia() {
		EstadoManager estadoManager = ComponentUtil.getComponent(EstadoManager.NAME);
		estadoDelegacia = estadoManager.findBySigla(processoProcedimentoOrigemDTO.getUf());
	}

	private ProcessoTrf getProcessoTrf() {
		return ProcessoTrfHome.instance().getInstance();
	}

	public boolean isRetificacao() {
		return getProcessoTrf().getProcessoStatus().equals(ProcessoStatusEnum.D);
	}

	public void setTipoOrigemList(List<TipoOrigemDTO> tipoOrigemList) {
		this.tipoOrigemList = tipoOrigemList;
	}

	public List<TipoOrigemDTO> getTipoOrigemList() {
		return tipoOrigemList;
	}

	public List<TipoProcedimentoOrigemDTO> getTipoProcedimentoOrigemList() {
		return tipoProcedimentoOrigemList;
	}

	public void setTipoProcedimentoOrigemList(List<TipoProcedimentoOrigemDTO> tipoProcedimentoOrigemList) {
		this.tipoProcedimentoOrigemList = tipoProcedimentoOrigemList;
	}

	public List<OrgaoProcedimentoOriginarioDTO> getOrgaoList() {
		return orgaoList;
	}

	public void setOrgaoList(List<OrgaoProcedimentoOriginarioDTO> orgaoList) {
		this.orgaoList = orgaoList;
	}

	public ProcessoProcedimentoOrigemDTO getProcessoProcedimentoOrigemDTO() {
		return processoProcedimentoOrigemDTO;
	}

	public void setProcessoProcedimentoOrigemDTO(ProcessoProcedimentoOrigemDTO processoProcedimentoOrigemDTO) {
		this.processoProcedimentoOrigemDTO = processoProcedimentoOrigemDTO;
	}

	public List<ProcessoProcedimentoOrigemDTO> getProcessoProcedimentoOrigemDtoList() {
		return processoCriminalDTO.getProcessoProcedimentoOrigemList();
	}

	public void setProcessoProcedimentoOrigemDtoList(
			List<ProcessoProcedimentoOrigemDTO> processoProcedimentoOrigemDtoList) {
		processoCriminalDTO.setProcessoProcedimentoOrigemList(processoProcedimentoOrigemDtoList);
	}

	public ProcessoCriminalDTO getProcessoCriminalDTO() {
		return processoCriminalDTO;
	}

	public void setProcessoCriminalDTO(ProcessoCriminalDTO processoCriminalDTO) {
		this.processoCriminalDTO = processoCriminalDTO;
	}

	public Estado getEstadoDelegacia() {
		return estadoDelegacia;
	}

	public void setEstadoDelegacia(Estado estadoDelegacia) {
		this.estadoDelegacia = estadoDelegacia;
	}

	public List<ProcessoProcedimentoOrigemDTO> getProcedimentoRetombadoList() {
		return procedimentoRetombadoList;
	}

	public void setProcedimentoRetombadoList(List<ProcessoProcedimentoOrigemDTO> procedimentoRetombadoList) {
		this.procedimentoRetombadoList = procedimentoRetombadoList;
	}

}
