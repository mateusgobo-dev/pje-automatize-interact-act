package br.com.infox.bpm.taskPage.remessacnj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.cliente.home.PessoaFisicaHome;
import br.com.infox.cliente.home.PessoaHome;
import br.com.infox.cliente.home.PessoaJuridicaHome;
import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.home.ProcessoParteRepresentanteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.itx.component.grid.GridQuery;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.TipoParteConfiguracaoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(OutrosDestinatariosManifestacaoProcessualTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class OutrosDestinatariosManifestacaoProcessualTaskPageAction extends
		TaskAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;

	public static final String NAME = "outrosDestinatariosManifestacaoProcessualTaskPageAction";

	public class EnderecoCheck extends Endereco {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private Boolean selecionado = true;

		public Boolean getSelecionado() {
			return selecionado;
		}

		public void setSelecionado(Boolean selecionado) {
			this.selecionado = selecionado;
		}
	}

	@In(create = true)
	private transient PreCadastroPessoaBean preCadastroPessoaBean;

	@In(required = false)
	private ManifestacaoProcessualMetaData manifestacaoProcessualMetaData;

	@In(create = true)
	private ManifestacaoProcessualMetaDataManager manifestacaoProcessualMetaDataManager;

	@In(create = true)
	private transient ProcessoTrfHome processoTrfHome;
	@In(create = true)
	private transient PessoaHome pessoaHome;
	@In(create = true)
	private transient PessoaFisicaHome pessoaFisicaHome;
	@In(create = true)
	private transient PessoaJuridicaHome pessoaJuridicaHome;
	@In(create = true)
	private transient PessoaAdvogadoHome pessoaAdvogadoHome;
	@In(create = true)
	private transient ProcessoParteRepresentanteHome processoParteRepresentanteHome;
	@In(create = true)
	private transient ProcessoParteHome processoParteHome;
	@In(required = false)
	private transient GridQuery processoParteVinculoPessoaEnderecoGrid;
	@In(create = true)
	private transient PessoaService pessoaService;
	@In(create = true)
	private transient TipoParteConfigClJudicialManager tipoParteConfigClJudicialManager;

	@RequestParameter
	private String[] partesVinculadas;

	private Pessoa pessoaSelecionada;
	private ProcessoParte parteSelecionada;
	private Boolean exibeModalOutroDestinatario;
	private Boolean exibeBotaoIncluirParte = true;

	private String polo;

	@Create()
	public void init() throws Exception {

		// recarregar partes na entityManager corrente

	}

	public Pessoa getPessoaSelecionada() {
		return pessoaSelecionada;
	}

	public void setPessoaSelecionada(Pessoa pessoaSelecionada) {
		this.pessoaSelecionada = pessoaSelecionada;
	}

	public ProcessoParte getParteSelecionada() {
		return parteSelecionada;
	}

	public void setParteSelecionada(ProcessoParte parteSelecionada) {
		this.parteSelecionada = parteSelecionada;
	}

	public Boolean getExibeModalOutroDestinatario() {
		return exibeModalOutroDestinatario;
	}

	public void setExibeModalOutroDestinatario(
			Boolean exibeModalOutroDestinatario) {
		this.exibeModalOutroDestinatario = exibeModalOutroDestinatario;
	}

	public Boolean getExibeBotaoIncluirParte() {
		return exibeBotaoIncluirParte;
	}

	public void setExibeBotaoIncluirParte(Boolean exibeBotaoIncluirParte) {
		this.exibeBotaoIncluirParte = exibeBotaoIncluirParte;
	}

	public String getPolo() {
		return polo;
	}

	public void setPolo(String polo) {
		this.polo = polo;
	}

	// modificações necessárias nas partes quando se muda a classe judicial
	public void iniciarPartes() {
		List<ProcessoParte> processoParteList = manifestacaoProcessualMetaData
				.getPolos();

		if (processoParteList.isEmpty()) {
			processoParteList = processoTrfHome.getInstance()
					.getProcessoParteList();
		}

		manifestacaoProcessualMetaDataManager.loadPartes(
				manifestacaoProcessualMetaData, processoParteList);
	}

	public List<TipoParte> getTiposParteNaoPrincipais(
			ProcessoParte processoParte) {
		List<TipoParte> returnValue = new ArrayList<TipoParte>();
		List<TipoParteConfigClJudicial> tpParteConfigClJudiciais = tipoParteConfigClJudicialManager.recuperarTipoParteConfiguracao(manifestacaoProcessualMetaData.getClasseJudicial());
		for (TipoParteConfigClJudicial tipoParteConfigClJudicial : tpParteConfigClJudiciais) {
			if(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(processoParte.getTipoParte())){
				if(tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo()){
					for (TipoParte tipo : manifestacaoProcessualMetaData.getClasseJudicial().getTipoParteList()) {
						if(tipo.getTipoParte().equals(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().getTipoParte())){
							returnValue.add(tipo);
						}
					}
					
				}
			}else{
				if(tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo()){
					for (TipoParte tipo : manifestacaoProcessualMetaData.getClasseJudicial().getTipoParteList()) {
						if(tipo.getTipoParte().equals(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().getTipoParte())){
							returnValue.add(tipo);
						}
					}
				}
			}
			
		}
		return returnValue;
	}

	public List<Pessoa> pesquisarPessoa(Object criterioPesquisaPessoa) {
		List<Pessoa> pessoas = new ArrayList<Pessoa>(0);
		try {
			String criterioPesquisaPessoaString = criterioPesquisaPessoa
					.toString();
			String criterioSemFormatacao = criterioPesquisaPessoaString
					.replaceAll("[\\.\\-/]", "");

			if (!NumberUtils.isNumber(criterioSemFormatacao)) {
				pessoas.addAll(pessoaService
						.findByName(criterioPesquisaPessoaString));

			}

			else {
				boolean isCPF = criterioSemFormatacao.length() == 11
						&& InscricaoMFUtil.verificaCPF(criterioSemFormatacao);
				boolean isCNPJ = criterioSemFormatacao.length() == 14
						&& InscricaoMFUtil.verificaCNPJ(criterioSemFormatacao);

				if (isCPF || isCNPJ) {
					pessoas.add(pessoaService
							.findByInscricaoMF(criterioSemFormatacao));
				}
			}
		} catch (PJeBusinessException e) {
			log.error("Ocorreu um erro ao pesquisar Pessoa", e);
			FacesMessages.instance().add(Severity.ERROR,
					"Não foi possível pesquisar: " + e.getCause());
		}

		return pessoas;
	}

	public void novoOutroDestinatario() throws Exception {
		polo = null;
		preCadastroPessoaBean.resetarBean();

		parteSelecionada = new ProcessoParte();

		parteSelecionada.setPessoa(getPessoaSelecionada());

		exibeModalOutroDestinatario = true;
		exibeBotaoIncluirParte = true;

	}

	public void preparaPreCadastro() throws Exception {

		pessoaHome.setInstance(pessoaSelecionada);
		processoParteHome.setInstance(parteSelecionada);

		processoParteRepresentanteHome.setCodInParticipacao(polo);

		if (pessoaSelecionada instanceof PessoaAutoridade) {
			preCadastroPessoaBean.setInTipoPessoa(TipoPessoaEnum.A);
			preCadastroPessoaBean
					.setPessoaAutoridade((PessoaAutoridade) pessoaSelecionada);
		}

		else if (pessoaSelecionada instanceof PessoaFisica) {
			preCadastroPessoaBean.setInTipoPessoa(TipoPessoaEnum.F);
			if (!parteSelecionada.getTipoParte().getTipoPrincipal()) {
				processoParteRepresentanteHome.setFlgMostrarDadosAdvogado(true);
			}

			if (Pessoa.instanceOf(pessoaSelecionada, PessoaAdvogado.class)) {
				pessoaAdvogadoHome
						.setInstance(((PessoaFisica) pessoaSelecionada)
								.getPessoaAdvogado());
			} else {
				pessoaFisicaHome.setInstance((PessoaFisica) pessoaSelecionada);

			}
			preCadastroPessoaBean
					.setPessoaFisica((PessoaFisica) pessoaSelecionada);
		}

		else if (pessoaSelecionada instanceof PessoaJuridica) {
			preCadastroPessoaBean.setInTipoPessoa(TipoPessoaEnum.J);
			preCadastroPessoaBean
					.setPessoaJuridica((PessoaJuridica) pessoaSelecionada);
			pessoaJuridicaHome.setInstance((PessoaJuridica) pessoaSelecionada);
		}

		preCadastroPessoaBean.confirmarPessoa();
	}

	public void verDetalhesParte(ProcessoParte processoParte) throws Exception {
		preCadastroPessoaBean.resetarBean();
		parteSelecionada = processoParte;
		pessoaSelecionada = processoParte.getPessoa();

		preparaPreCadastro();
		exibeModalOutroDestinatario = true;
		exibeBotaoIncluirParte = false;
		
		polo = parteSelecionada.getInParticipacao().name();

		// marcar endereço selecionado na aba de enderecos
		if (processoParteVinculoPessoaEnderecoGrid == null) {
			processoParteVinculoPessoaEnderecoGrid = (GridQuery) Component
					.getInstance("processoParteVinculoPessoaEnderecoGrid", true);
		}
		processoParteVinculoPessoaEnderecoGrid.getResultList();


		if(!parteSelecionada.getEnderecos().isEmpty()){
			// atualmente, é possível selecionar apenas um endereço
			assert parteSelecionada.getEnderecos().size() == 1 : "A parte possui mais de um endereço";
			processoParteVinculoPessoaEnderecoGrid.setSelectedRow(parteSelecionada
					.getEnderecos().get(0));
		}
	}

	public void atualizarOutroDestinatario() throws Exception {
		ProcessoParte[] partesVinculadasArray = preAddParte();
		manifestacaoProcessualMetaDataManager.updateParte(
				manifestacaoProcessualMetaData, parteSelecionada, partesVinculadasArray);
	}
	
	public void adicionarOutroDestinatario() throws Exception {
		ProcessoParte[] partesVinculadasArray = preAddParte();
		// adicionar parte
		manifestacaoProcessualMetaDataManager.addParte(
				manifestacaoProcessualMetaData, parteSelecionada,
				partesVinculadasArray);
		
		exibeModalOutroDestinatario = false;
	}
	
	private ProcessoParte[] preAddParte(){
		// atribuir polo
				parteSelecionada.setInParticipacao(getProcessoParteParticipacaoEnum());

				// montar partes vinculadas
				List<ProcessoParte> partesVinculadasList = new ArrayList<ProcessoParte>(
						0);

				if (partesVinculadas != null) {
					if (parteSelecionada.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
						for (String indexParteString : partesVinculadas) {
							partesVinculadasList.add(manifestacaoProcessualMetaData
									.getPoloAtivo().get(
											Integer.parseInt(indexParteString)));
						}
					} else {
						for (String indexParteString : partesVinculadas) {
							partesVinculadasList.add(manifestacaoProcessualMetaData
									.getPoloPassivo().get(
											Integer.parseInt(indexParteString)));
						}
					}
				}

				// vincular endereço selecionado
				processoParteVinculoPessoaEnderecoGrid.getResultList();
				parteSelecionada.getProcessoParteEnderecoList().clear();

				ProcessoParteEndereco processoParteEndereco = new ProcessoParteEndereco();
				processoParteEndereco.setProcessoParte(parteSelecionada);
				
				if(!processoParteHome.getInstance().getIsEnderecoDesconhecido()){
					processoParteEndereco
							.setEndereco((Endereco) processoParteVinculoPessoaEnderecoGrid
									.getSelectedRow());
					parteSelecionada.getProcessoParteEnderecoList().add(
							processoParteEndereco);
				}
				

				ProcessoParte[] partesVinculadasArray = new ProcessoParte[partesVinculadasList
						.size()];

				partesVinculadasArray = partesVinculadasList
						.toArray(partesVinculadasArray);
				
				return partesVinculadasArray;

	}

	private ProcessoParteParticipacaoEnum getProcessoParteParticipacaoEnum() {
		if (ProcessoParteParticipacaoEnum.A.toString().equals(polo)) {
			return ProcessoParteParticipacaoEnum.A;
		} else {
			return ProcessoParteParticipacaoEnum.P;
		}
	}

	public List<TipoParte> getTiposParte() throws Exception {
		List<TipoParte> tipos = new ArrayList<TipoParte>(0);

		if (manifestacaoProcessualMetaData.getClasseJudicial() == null
				|| manifestacaoProcessualMetaData.getClasseJudicial()
						.getTipoParteList().isEmpty()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Classe judicial não possui tipos de parte associados");
		} else {
			for (TipoParte tipoParte : manifestacaoProcessualMetaData
					.getClasseJudicial().getTipoParteList()) {
				
				List<TipoParteConfigClJudicial> recuperarTipoParteConfiguracao = tipoParteConfigClJudicialManager.recuperarTipoParteConfiguracao(manifestacaoProcessualMetaData.getClasseJudicial());	
				for (TipoParteConfigClJudicial tipoParteConfigClJudicial : recuperarTipoParteConfiguracao) {
					
					if (getProcessoParteParticipacaoEnum() == ProcessoParteParticipacaoEnum.A
							&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(tipoParte) && tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo()) {
						tipos.add(tipoParte);
					}

					if (getProcessoParteParticipacaoEnum() == ProcessoParteParticipacaoEnum.P
							&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(tipoParte) && tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo()) {
						tipos.add(tipoParte);
					}
				}
			}
		}
		return tipos;
	}

	public List<ProcessoParte> getPartesVinculadas() {
		List<ProcessoParte> returnValue = new ArrayList<ProcessoParte>(0);
		
		
		if (polo != null) {
			if (getProcessoParteParticipacaoEnum() == ProcessoParteParticipacaoEnum.A) {
				for (ProcessoParte parte : manifestacaoProcessualMetaData
						.getPoloAtivo()) {
					if (parte.getTipoParte().getTipoPrincipal()) {
						returnValue.add(parte);
					}
				}
			} else {
				for (ProcessoParte parte : manifestacaoProcessualMetaData
						.getPoloPassivo()) {
					if (parte.getTipoParte().getTipoPrincipal()) {
						returnValue.add(parte);
					}
				}
			}
		}
		
		return returnValue;
	}

	public void removerParte(ProcessoParte parte) {
		manifestacaoProcessualMetaDataManager.removeParte(
				manifestacaoProcessualMetaData, parte);
	}

	public boolean isParteVinculada(ProcessoParte parte,
			ProcessoParte representante) {
		for (ProcessoParteRepresentante processoParteRepresentante : representante
				.getProcessoParteRepresentanteList2()) {
			if (processoParteRepresentante.getProcessoParte() == parte) {
				return true;
			}
		}
		return false;
	}
}
