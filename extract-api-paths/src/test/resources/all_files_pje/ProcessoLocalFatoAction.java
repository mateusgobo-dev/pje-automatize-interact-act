package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ClientErrorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.model.dto.ProcessoParteCloudEvent;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.EstadosBrasileirosEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name("processoLocalFatoAction")
@Scope(ScopeType.CONVERSATION)
public class ProcessoLocalFatoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private ProcessoRascunho processoRascunho;
	private ProcessoCriminalDTO processoCriminalDTO = new ProcessoCriminalDTO();
	private ProcessoRascunhoManager processoRascunhoManager = ComponentUtil.getComponent(ProcessoRascunhoManager.NAME);
	private List<Municipio> municipioList = new ArrayList<>();
	private Estado estado;
	private Municipio municipio;
	
	@Logger
	private transient Log log;

	public static ProcessoLocalFatoAction instance() {
		return ComponentUtil.getComponent("processoLocalFatoAction");
	}

	@Create
	public void create() throws PJeException {
		if (isRetificacao()) {
			ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil
					.getComponent(ProcessoCriminalRestClient.class);
			try {
				processoCriminalDTO = processoCriminalRestClient
						.getResourceByProcesso(getProcessoTrf().getNumeroProcesso());
				
				// Caso o processo nao exista no Criminal, cria-se um objeto vazio
				// para que o usuario possa inserir os dados.
				if (processoCriminalDTO == null) {
					processoCriminalDTO = new ProcessoCriminalDTO();
					processoCriminalDTO.setNrProcesso(getProcessoTrf().getNumeroProcesso());
				}
				
			}catch (PJeException pje) {
				FacesMessages.instance().add(Severity.ERROR, pje.getCode());
				log.error("Erro ao consultar o processo " + getProcessoTrf().getNumeroProcesso() + " no serviço do Criminal", pje);
			}catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro: " + e.getLocalizedMessage());
				log.error("Erro ao consultar o processo " + getProcessoTrf().getNumeroProcesso() + " no serviço do Criminal", e);
			}
			salvarCep();
		} else {
			processoRascunho = processoRascunhoManager.recuperarRascunhoPeloProcesso(getProcessoTrf());
			if (processoRascunho != null) {
				processoCriminalDTO = processoRascunhoManager.recuperarRascunhoProcessoCriminal(getProcessoTrf());
				if (processoCriminalDTO == null) {
					processoCriminalDTO = new ProcessoCriminalDTO();
				}
				salvarCep();
			} else {
				processoRascunho = new ProcessoRascunho();
				processoRascunho.setProcesso(getProcessoTrf());
			}
		}
	}

	private void salvarCep() {
		CepService cepService = ComponentUtil.getComponent("cepService");
		if (processoCriminalDTO != null) {
			if (processoCriminalDTO.getCep() != null) {
				Cep cep = cepService.findByCodigo(processoCriminalDTO.getCep());
				if (cep != null) {
					CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
					cepSuggestBean.setInstance(cep);
					municipio = cep.getMunicipio();
					estado = municipio.getEstado();
					MunicipioManager municipioManager = ComponentUtil.getComponent(MunicipioManager.NAME);
					municipioList = municipioManager.findByUf(estado.getCodEstado());
					processarEstadoMunicipio();
				} else {
					preencherEstadoMunicipioSemCEP();
				}
			} else {
				preencherEstadoMunicipioSemCEP();
			}
		}
	}

	private void preencherEstadoMunicipioSemCEP() {
		if (processoCriminalDTO.getMunicipio() != null) {
			MunicipioManager municipioManager = ComponentUtil.getComponent(MunicipioManager.NAME);
			municipio = municipioManager.getMunicipioByCodigoIBGE(processoCriminalDTO.getMunicipio().getCodigoIbge());
			if (municipio != null) {
				estado = municipio.getEstado();
				municipioList = municipioManager.findByUf(estado.getCodEstado());
				processarEstadoMunicipio();
			}
		}
	}

	public void salvar() throws ClientErrorException, PJeException {
		if (beforePersistOrUpdate()) {
			formatarLocalFato();

			if (isRetificacao()) {
				ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil
						.getComponent(ProcessoCriminalRestClient.class);								

				enviarProcessoParaOCriminal(processoCriminalRestClient);

				atualizarPartesPoloPassivoNoCriminal();
				
				processoRascunho = processoRascunhoManager.recuperarRascunhoPeloProcesso(getProcessoTrf());
											
				if (processoRascunho != null) {
					processoCriminalDTO.setPjeOrigem(processoRascunho.getJsonProcessoCriminal().getPjeOrigem());
					processoCriminalDTO.setTipoProcesso(processoRascunho.getJsonProcessoCriminal().getTipoProcesso());
					formatarLocalFato();
					persistRascunho();
				} 
								
				FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso.");
			} else {
				if (processoRascunho == null || processoRascunho.getProcesso() == null) {
					processoRascunho = new ProcessoRascunho();
					processoRascunho.setProcesso(getProcessoTrf());
				}
				if (processoRascunho.getIdProcessoRascunho() == null) {
					FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso.");
				} else {
					FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso.");
				}
				persistRascunho();
			}
		}
	}

	/**
	 * Cria o processo no Criminal caso não exista ou atualiza o processo se não existir.
	 * @param processoCriminalRestClient
	 * @throws PJeException
	 */
	private void enviarProcessoParaOCriminal(ProcessoCriminalRestClient processoCriminalRestClient) throws PJeException {
		if(processoCriminalDTO != null && processoCriminalDTO.getId() != null) {
			// Cria o processo no Criminal caso nao exista
			processoCriminalDTO = processoCriminalRestClient.updateResource(processoCriminalDTO);
		}else {
			processoCriminalDTO = processoCriminalRestClient.createResource(processoCriminalDTO);
		}
	}

	/**
	 * Método responsável por formatar o dado de Local do Fato com as informações preenchidas
	 * nos campos de Município, CEP, Logradouro, Bairro e Complemento.
	 */
	private void formatarLocalFato() {
		if (StringUtil.isEmpty(processoCriminalDTO.getDsLocalFato())) {
			String localFato = "";
			if (estado != null) {
				localFato += estado.getCodEstado();
			}
			if (municipio != null) {
				localFato += (" - " + municipio.getMunicipio());
			}
			if (processoCriminalDTO.getCep() != null) {
				localFato += (", " + processoCriminalDTO.getCep());
			}
			if (processoCriminalDTO.getNmLogradouro() != null) {
				localFato += (", " + processoCriminalDTO.getNmLogradouro());
			}
			if (processoCriminalDTO.getNmBairro() != null) {
				localFato += (", " + processoCriminalDTO.getNmBairro());
			}
			if (processoCriminalDTO.getComplemento() != null) {
				localFato += (", " + processoCriminalDTO.getComplemento());
			}
			processoCriminalDTO.setDsLocalFato(localFato);
		}
	}

	/**
	 * Metodo resposavel por atualizar as partes do polo passivo no servico do criminal
	 * ao salvar o Local do Fato na retificacao do processo.
	 */
	private void atualizarPartesPoloPassivoNoCriminal() {
		for (ProcessoParte processoParte : getProcessoTrf().getListaPartePrincipalPassivo()) {
			AMQPEventManager amqpManager = AMQPEventManager.instance();
			amqpManager.enviarMensagem(processoParte, ProcessoParteCloudEvent.class, CloudEventVerbEnum.PATCH);
		}
	}
	
	protected boolean beforePersistOrUpdate() {
		if (processoCriminalDTO != null && processoCriminalDTO.getDtLocalFato() != null && processoCriminalDTO.getDtLocalFato().after(new Date())) {
			FacesMessages.instance().addFromResourceBundle("processoLocalFato.erroDataLocalFato");
			return false;
		}
		return true;
	}

	private void persistRascunho() {
		try {
			processoRascunho.setJsonProcessoCriminal(processoCriminalDTO);
			processoRascunhoManager.persistAndFlush(processoRascunho);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Não foi possível gravar Procedimento de Origem! Erro: " + e.getMessage(), e);
		}
	}

	public void atualizarDadosEndereco() {
		CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
		Cep cep = cepSuggestBean.getInstance();
		municipio = cep.getMunicipio();
		estado = municipio.getEstado();
		MunicipioManager municipioManager = ComponentUtil.getComponent(MunicipioManager.NAME);
		municipioList = municipioManager.findByUf(estado.getCodEstado());
		processarEstadoMunicipio();
		processoCriminalDTO.setCep(cep.getNumeroCep());
		processoCriminalDTO.setNmLogradouro(cep.getNomeLogradouro());
		processoCriminalDTO.setNmBairro(cep.getNomeBairro());
		processoCriminalDTO.setNmNumero(cep.getNumeroEndereco());
		processoCriminalDTO.setComplemento(cep.getComplemento());
		String localFato = "";
		localFato += municipio.getEstado().getCodEstado();
		localFato += (" - " + municipio.getMunicipio());
		localFato += (", " + cep.getNumeroCep());
		if (cep.getNomeLogradouro() != null) {
			localFato += (", " + cep.getNomeLogradouro());
		}
		if (cep.getNomeBairro() != null) {
			localFato += (", " + cep.getNomeBairro());
		}
		if (cep.getComplemento() != null) {
			localFato += (", " + cep.getComplemento());
		}
		processoCriminalDTO.setDsLocalFato(localFato);
	}

	public void newInstance() {

	}

	public void limpar() {
		processoCriminalDTO = new ProcessoCriminalDTO();
		municipioList = new ArrayList<>();
		estado = null;
		municipio = null;
	}

	public void atualizarMunicipios() {
		if (estado != null) {
			MunicipioManager municipioManager = ComponentUtil.getComponent(MunicipioManager.NAME);
			municipioList = municipioManager.findByUf(estado.getCodEstado());
		}
	}

	public void processarEstadoMunicipio() {
		processoCriminalDTO.getMunicipio().setMunicipio(municipio.getMunicipio());
		processoCriminalDTO.getMunicipio().setUf(municipio.getEstado().getCodEstado());
		processoCriminalDTO.getMunicipio().setCodigoIbge(municipio.getCodigoIbge());
	}

	public ProcessoTrf getProcessoTrf() {
		return ProcessoTrfHome.instance().getInstance();
	}

	public boolean isRetificacao() {
		return getProcessoTrf().getProcessoStatus().equals(ProcessoStatusEnum.D);
	}

	public ProcessoRascunho getProcessoRascunho() {
		return processoRascunho;
	}

	public void setProcessoRascunho(ProcessoRascunho processoRascunho) {
		this.processoRascunho = processoRascunho;
	}

	public ProcessoCriminalDTO getProcessoCriminalDTO() {
		return processoCriminalDTO;
	}

	public void setProcessoCriminalDTO(ProcessoCriminalDTO processoCriminalDTO) {
		this.processoCriminalDTO = processoCriminalDTO;
	}

	public ProcessoRascunhoManager getProcessoRascunhoManager() {
		return processoRascunhoManager;
	}

	public void setProcessoRascunhoManager(ProcessoRascunhoManager processoRascunhoManager) {
		this.processoRascunhoManager = processoRascunhoManager;
	}

	public List<Municipio> getMunicipioList() {
		return municipioList;
	}

	public void setMunicipioList(List<Municipio> municipioList) {
		this.municipioList = municipioList;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public String getDescricaoUf(String uf) {
		String ufCompleta = "";
		if (uf != null && !uf.isEmpty()) {
			EstadosBrasileirosEnum enumUfs = EstadosBrasileirosEnum.valueOf(uf);
			ufCompleta = enumUfs.getLabel();
		}
		return ufCompleta;
	}
	
}
