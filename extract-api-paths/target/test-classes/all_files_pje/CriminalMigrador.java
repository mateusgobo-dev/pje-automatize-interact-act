package br.jus.cnj.pje.webservice.migrador;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ClientErrorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoProcedimentoOrigemManager;
import br.jus.cnj.pje.nucleo.manager.migrador.OrgaoProcedimentoOriginarioLegacyManager;
import br.jus.cnj.pje.nucleo.manager.migrador.TipoOrigemLegacyManager;
import br.jus.cnj.pje.nucleo.manager.migrador.TipoProcedimentoOrigemLegacyManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.LoggingFilterRestPje;
import br.jus.cnj.pje.webservice.client.criminal.OrgaoProcedimentoOriginarioRestClient;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoOrigemRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoProcedimentoOrigemRestClient;
import br.jus.cnj.pje.webservice.client.criminal.UsuarioAutenticadoRestClient;
import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.dto.InformacaoCriminalDTO;
import br.jus.pje.nucleo.dto.MunicipioDTO;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;
import br.jus.pje.nucleo.dto.ParteDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoProcedimentoOriginario;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoProcedimentoOrigem;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoOrigem;
import br.jus.pje.nucleo.entidades.TipoProcedimentoOrigem;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

@Name(CriminalMigrador.NAME)
@Scope(ScopeType.PAGE)
public class CriminalMigrador {

	public static final String NAME = "criminalMigrador";
	
	private static final Logger logger = LoggerFactory.getLogger(LoggingFilterRestPje.class);
	
	private static final Integer NUM_REQUISICOES_TESTE_CONEXAO = 10;
	
	public static final Integer BATCHSIZE = 150;

	private List<Integer> idsProcessosCriminaisLegados = new ArrayList<>(0);
	
	private ProcessoProcedimentoOrigemManager processoProcedimentoOrigemManager;
	
	private ProcessoJudicialManager processoJudicialManager;
	
	private ProcessoCriminalRestClient processoCriminalRestClient;
	
	private ProcessoParteManager processoParteManager;
	
	private TipoOrigemLegacyManager tipoOrigemLegacyManager;
	
	private TipoProcedimentoOrigemLegacyManager tipoProcedimentoOrigemLegacyManager;
	
	private OrgaoProcedimentoOriginarioLegacyManager orgaoProcedimentoOriginarioLegacyManager;
	
	private OrgaoProcedimentoOriginarioRestClient orgaoProcedimentoOriginarioRestClient;
	
	private TipoProcedimentoOrigemRestClient tipoProcedimentoOrigemRestClient;
	
	private TipoOrigemRestClient tipoOrigemRestClient;
	
	private UsuarioAutenticadoRestClient usuarioAutenticadoRestClient;
	
	private List<TipoProcedimentoOrigemDTO> listaTiposProcedimentoOrigem;
	
	private List<TipoOrigemDTO> listaTiposOrigem;
	
	private Boolean situacaoConexaoSemAutenticacao = false;

	private Boolean situacaoConexaoComAutenticacao = false;

	private Integer numProcessosPendentes = null;
	
	private Integer numProcedimentosPendentes = null;
	
	private Map<Integer, OrgaoProcedimentoOriginarioDTO> cacheListaOrgaoProcedimentoOriginario = new HashMap<Integer, OrgaoProcedimentoOriginarioDTO>();
	
	private Map<Integer, List<ProcessoProcedimentoOrigem>> cacheListaProcessoProcedimentoOrigem = new HashMap<>();

	@Create
	public void init() {
		usuarioAutenticadoRestClient = ComponentUtil.getComponent(UsuarioAutenticadoRestClient.NAME);
		orgaoProcedimentoOriginarioRestClient = ComponentUtil.getComponent(OrgaoProcedimentoOriginarioRestClient.NAME);
		processoCriminalRestClient = ComponentUtil.getComponent(ProcessoCriminalRestClient.NAME);
		processoParteManager = ComponentUtil.getProcessoParteManager();
		processoJudicialManager = ComponentUtil.getProcessoJudicialManager();
		tipoOrigemLegacyManager = ComponentUtil.getComponent(TipoOrigemLegacyManager.NAME);
		tipoProcedimentoOrigemLegacyManager = ComponentUtil.getComponent(TipoProcedimentoOrigemLegacyManager.NAME);
		orgaoProcedimentoOriginarioLegacyManager = ComponentUtil.getComponent(OrgaoProcedimentoOriginarioLegacyManager.NAME);
		
		processoProcedimentoOrigemManager = ComponentUtil.getComponent(ProcessoProcedimentoOrigemManager.NAME);
		tipoProcedimentoOrigemRestClient = ComponentUtil.getComponent(TipoProcedimentoOrigemRestClient.NAME);
		tipoOrigemRestClient = ComponentUtil.getComponent(TipoOrigemRestClient.NAME);
		
		numProcessosPendentes = this.processoProcedimentoOrigemManager.countProcessosCriminaisLegadosPendentes();
		numProcedimentosPendentes = this.processoProcedimentoOrigemManager.countProcedimentosCriminaisLegadosPendentes();
		atualizarSituacaoConexao();
	}

	public void migrarCriminalLegacy() {
		// migrar meta-dados
		this.migrarMetaDados();
		
		// migrar dados
		this.migrarProcessosLegados();
		
		numProcessosPendentes = this.processoProcedimentoOrigemManager.countProcessosCriminaisLegadosPendentes();
		numProcedimentosPendentes = this.processoProcedimentoOrigemManager.countProcedimentosCriminaisLegadosPendentes();
	}

	private void migrarMetaDados() {
		// 1. migrar dados de tipos de origem
		if(tipoOrigemLegacyManager.isPendenteMigracao()) {
			this.migrarTipoOrigem();
		}
		// 2. migrar dados de tb_tp_procedimento_origem
		if(tipoProcedimentoOrigemLegacyManager.isPendenteMigracao()) {
			this.migrarTipoProcedimentoOrigem();
		}
		
		// 3. migrar dados de tb_org_prcdmnto_originario
		if(orgaoProcedimentoOriginarioLegacyManager.isPendenteMigracao()) {
			this.migrarOrgaoProcedimentoOriginario();
		}
	}
	
	private void migrarTipoOrigem() {
		List<TipoOrigem> tipoOrigemLegacyList = tipoOrigemLegacyManager.recuperarPendentesMigracao();
		
		if(CollectionUtilsPje.isNotEmpty(tipoOrigemLegacyList)) {
			this.listaTiposOrigem = tipoOrigemRestClient.recuperarTiposOrigem();
			Util.beginAndJoinTransaction();

			for (TipoOrigem tipoOrigemLegacy : tipoOrigemLegacyList) {
				Integer codigoNacional = null;
				if(CollectionUtilsPje.isNotEmpty(this.listaTiposOrigem)) {
					for (TipoOrigemDTO tipoOrigem : this.listaTiposOrigem) {
						if(tipoOrigemLegacy.getDsTipoOrigem().equalsIgnoreCase(tipoOrigem.getDsTipoOrigem())) {
							codigoNacional = tipoOrigem.getId(); // encontrou
							break;
						}
					}
					if(codigoNacional == null) { // nao encontrou
						// deve-se encaminhar a informacao para o repositorio nacional
						TipoOrigemDTO novoTipoOrigem = new TipoOrigemDTO(
								tipoOrigemLegacy.getDsTipoOrigem(), 
								tipoOrigemLegacy.getInObrigatorioNumeroOrigem(), 
								tipoOrigemLegacy.getAtivo());
						
						try {
							TipoOrigemDTO criado = tipoOrigemRestClient.createResource(novoTipoOrigem);
							if(criado != null && criado.getId() != null) {
								codigoNacional = criado.getId();
							}
						} catch (ClientErrorException | PJeException e) {
							e.printStackTrace();
						}
					}
					if(codigoNacional != null) {
						try {
							tipoOrigemLegacy.setCodigoNacional(codigoNacional);
							tipoOrigemLegacyManager.persist(tipoOrigemLegacy);
						} catch (PJeBusinessException e) {
							e.printStackTrace();
						}
					}
				}
			}
			HibernateUtil.getSession().flush();
			Util.commitTransction();
			FacesMessages.instance().clear();
		}
	}
	
	private void migrarTipoProcedimentoOrigem() {
		List<TipoProcedimentoOrigem> tipoProcedimentoOrigemLegacyList = tipoProcedimentoOrigemLegacyManager.recuperarPendentesMigracao();
		
		if(CollectionUtilsPje.isNotEmpty(tipoProcedimentoOrigemLegacyList)) {
			this.listaTiposProcedimentoOrigem = tipoProcedimentoOrigemRestClient.recuperarTiposProcedimentoOrigem();
			Util.beginAndJoinTransaction();
			for (TipoProcedimentoOrigem tipoProcedimentoOrigemLegacy : tipoProcedimentoOrigemLegacyList) {
				Integer codigoNacional = null;
				if(CollectionUtilsPje.isNotEmpty(this.listaTiposProcedimentoOrigem)) {
					for (TipoProcedimentoOrigemDTO tipoProcedimentoOrigem : this.listaTiposProcedimentoOrigem) {
						if(tipoProcedimentoOrigemLegacy.getDsTipoProcedimento().equalsIgnoreCase(tipoProcedimentoOrigem.getDsTipoProcedimento())) {
							codigoNacional = tipoProcedimentoOrigem.getId(); // encontrou
							break;
						}
					}
					if(codigoNacional == null) { // nao encontrou
						// deve-se encaminhar a informacao para o repositorio nacional
						TipoProcedimentoOrigemDTO novoTipoProcedimentoOrigem = new TipoProcedimentoOrigemDTO(
								tipoProcedimentoOrigemLegacy.getDsTipoProcedimento(), 
								tipoProcedimentoOrigemLegacy.getAtivo());
						
						try {
							TipoProcedimentoOrigemDTO criado = tipoProcedimentoOrigemRestClient.createResource(novoTipoProcedimentoOrigem);
							if(criado != null && criado.getId() != null) {
								codigoNacional = criado.getId();
							}
						} catch (ClientErrorException | PJeException e) {
							e.printStackTrace();
						}
					}
					if(codigoNacional != null) {
						try {
							tipoProcedimentoOrigemLegacy.setCodigoNacional(codigoNacional);
							tipoProcedimentoOrigemLegacyManager.persist(tipoProcedimentoOrigemLegacy);
						} catch (PJeBusinessException e) {
							e.printStackTrace();
						}
					}
				}
			}
			HibernateUtil.getSession().flush();
			Util.commitTransction();
			FacesMessages.instance().clear();
		}
	}
	
	private void migrarOrgaoProcedimentoOriginario() {
		List<OrgaoProcedimentoOriginario> orgaoProcedimentoOriginarioLegacyList = orgaoProcedimentoOriginarioLegacyManager.recuperarPendentesMigracao();
		
		if(CollectionUtilsPje.isNotEmpty(orgaoProcedimentoOriginarioLegacyList)) {
			Util.beginAndJoinTransaction();
			for (OrgaoProcedimentoOriginario orgaoProcedimentoOriginarioLegacy : orgaoProcedimentoOriginarioLegacyList) {
				Integer idOpoLocal = orgaoProcedimentoOriginarioLegacy.getId();
				OrgaoProcedimentoOriginarioDTO opoNacional = null;

				if (this.cacheListaOrgaoProcedimentoOriginario != null && !this.cacheListaOrgaoProcedimentoOriginario.isEmpty()) {
					opoNacional = this.cacheListaOrgaoProcedimentoOriginario.get(idOpoLocal);
				}

				if(opoNacional == null) {
					opoNacional = recuperarOrgaoProcedimentoOriginarioDTO(orgaoProcedimentoOriginarioLegacy);
					if(opoNacional != null) {
						this.cacheListaOrgaoProcedimentoOriginario.put(idOpoLocal, opoNacional);
					}
				}
				
				if(opoNacional == null) { // nao encontrou
					// deve-se encaminhar a informacao para o repositorio nacional
					TipoOrigem tipoOrigemLocal = orgaoProcedimentoOriginarioLegacy.getTipoOrigem();
					if(tipoOrigemLocal != null) {
						TipoOrigemDTO tipoOrigemNacional = this.tipoOrigemLegacyManager.converteEmTipoOrigemNacional(orgaoProcedimentoOriginarioLegacy.getTipoOrigem().getId());
						if(tipoOrigemNacional != null) {
							MunicipioDTO municipio = orgaoProcedimentoOriginarioLegacyManager.getMunicipioDTO(idOpoLocal);
							OrgaoProcedimentoOriginarioDTO novoOpoNacional = new OrgaoProcedimentoOriginarioDTO(
									tipoOrigemNacional,
									orgaoProcedimentoOriginarioLegacy.getDsCodOrigem(),
									orgaoProcedimentoOriginarioLegacy.getDsNomeOrgao(),
									orgaoProcedimentoOriginarioLegacy.getDsTelefone(),
									orgaoProcedimentoOriginarioLegacy.getDsDdd(),
									orgaoProcedimentoOriginarioLegacy.getNrCep(),
									municipio,
									orgaoProcedimentoOriginarioLegacy.getNmBairro(),
									orgaoProcedimentoOriginarioLegacy.getNmLogradouro(),
									orgaoProcedimentoOriginarioLegacy.getNmComplemento(),
									orgaoProcedimentoOriginarioLegacy.getNmNumero(),
									orgaoProcedimentoOriginarioLegacy.getAtivo()
									);

							try {
								OrgaoProcedimentoOriginarioDTO criado = orgaoProcedimentoOriginarioRestClient.createResource(novoOpoNacional);
								if(criado != null) {
									opoNacional = criado;
									this.cacheListaOrgaoProcedimentoOriginario.put(idOpoLocal, opoNacional);
								}
							} catch (ClientErrorException | PJeException e) {
								e.printStackTrace();
							}
						}
					}
					
				}
				if(opoNacional != null) {
					try {
						orgaoProcedimentoOriginarioLegacy.setCodigoNacional(opoNacional.getId());
						orgaoProcedimentoOriginarioLegacyManager.persist(orgaoProcedimentoOriginarioLegacy);
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
				}
			}
			HibernateUtil.getSession().flush();
			Util.commitTransction();
			FacesMessages.instance().clear();
		}
	}
	
	private void migrarProcessosLegados() {
		this.idsProcessosCriminaisLegados = this.processoProcedimentoOrigemManager.recuperarIdsProcessosCriminaisLegadosPendentes();

		if(!CollectionUtilsPje.isEmpty(this.idsProcessosCriminaisLegados)) {
			Integer quantidade = Integer.valueOf(0);
			Integer quantidadeNaoMigrados = Integer.valueOf(0);
			this.listaTiposOrigem = tipoOrigemRestClient.recuperarTiposOrigem();
			this.listaTiposProcedimentoOrigem = tipoProcedimentoOrigemRestClient.recuperarTiposProcedimentoOrigem();

			Util.beginAndJoinTransaction();
			// Inicia a migração dos dados criminais para o serviço criminal
			for (Integer idProcesso : idsProcessosCriminaisLegados) {
				try {
					// Recuperar o ProcessoTrf
					ProcessoTrf procTrf = processoJudicialManager.findById(idProcesso);
					// Verifica se o processo ja existe no servido criminal
					ProcessoCriminalDTO processoMigrado = recuperaProcessoMigrado(procTrf);
					if (processoMigrado == null) {
						// Criar o MunicipioDTO
						MunicipioDTO municipioDTO = this.getMunicipioDTO(procTrf);
						if(procTrf != null && municipioDTO != null) {
							// Criar o processo criminal
							ProcessoCriminalDTO novoProcessoMigrado = this.criarProcessoCriminal(procTrf, municipioDTO);
							if(novoProcessoMigrado != null) {
								// Inserir as informações criminais referentes as partes do polo passivo
								this.incluirInformacoesCriminaisNoProcessoCriminal(novoProcessoMigrado, procTrf);
								processoMigrado = novoProcessoMigrado;
							}
						}
					}
					if(processoMigrado != null) {
						List<ProcessoProcedimentoOrigem> ppoList = this.processoProcedimentoOrigemManager.recuperarPorIdProcessTrf(procTrf.getIdProcessoTrf());
						if(CollectionUtilsPje.isNotEmpty(ppoList)) {
							for (ProcessoProcedimentoOrigem processoProcedimentoOrigem : ppoList) {
								if(processoProcedimentoOrigem.getCodigoNacional() != null && !processoProcedimentoOrigem.getCodigoNacional().equals(processoMigrado.getId())) {
									throw new BusinessException("Um procedimento de origem já migrado não pode ser migrado novamente para outro lugar! Id: {0}, CodigoNcional para migracao: {0}", processoProcedimentoOrigem.getId(), processoMigrado.getId());
								}else {
									processoProcedimentoOrigem.setCodigoNacional(processoMigrado.getId());
									this.processoProcedimentoOrigemManager.persist(processoProcedimentoOrigem);
								}
							}
							quantidade++;
							logger.debug("-----Quantidade migrados: " + quantidade);
							
							if((quantidade % BATCHSIZE) == 0) {
					        	HibernateUtil.getSession().flush();
					            Util.commitAndOpenJoinTransaction();
					            EntityUtil.getEntityManager().clear();
							}
						}
					}else {
						quantidadeNaoMigrados++;
						logger.debug("-----Quantidade NAO migrados: " + quantidadeNaoMigrados);
					}
				} catch (PJeRuntimeException e) {
					throw e;
				} catch (Exception e) {
					e.printStackTrace();
					throw new PJeRuntimeException(e);
				}
			}
			HibernateUtil.getSession().flush();
			Util.commitTransction();
			FacesMessages.instance().clear();

			if (quantidade == 0) {
				FacesMessages.instance().add(
					StatusMessage.Severity.WARN, 
					"Não há processos para migração.", 
					quantidade);
			} else {
				FacesMessages.instance().add(
					StatusMessage.Severity.INFO, 
					"{0} processo(s) migrado(s). Migração realizada com sucesso.", 
					quantidade);
			}
		}
	}
	
	private ProcessoCriminalDTO criarProcessoCriminal(ProcessoTrf procTrf, MunicipioDTO municipioDTO) throws PJeException {
		Processo processo = procTrf.getProcesso();
		String municipio = municipioDTO.getMunicipio();
		String uf = municipioDTO.getUf();
		String localFato = municipio.concat(" - ").concat(uf);
		
		// Criar o processo criminal
		ProcessoCriminalDTO procCriminal = new ProcessoCriminalDTO();
		procCriminal.setPjeOrigem(ConfiguracaoIntegracaoCloud.getAppName());
		procCriminal.setNrProcesso((processo != null ? processo.getNumeroProcesso() : null));
		procCriminal.setDsLocalFato(localFato);
		procCriminal.setMunicipio(municipioDTO);
		// Vincular os ProcessoProcedimentoOrigem ao processo criminal
		procCriminal.setProcessoProcedimentoOrigemList(this.recuperarListaProcessoProcedimentoOrigem(procTrf.getIdProcessoTrf()));
		procCriminal.setDtLocalFato(this.recuperarDataFatoProcesso(procTrf));
		// Enviar o processo criminal para o serviço criminal
		procCriminal = this.processoCriminalRestClient.createResource(procCriminal);
		
		return procCriminal;
	}
	
	private List<Integer> incluirInformacoesCriminaisNoProcessoCriminal(ProcessoCriminalDTO processoCriminal, ProcessoTrf procTrf) throws PJeException{
		List<Integer> ret = new ArrayList<>();
		
		List<ProcessoParte> listaPartesPassivo = this.processoParteManager.recuperaListaPartePrincipalPassivo(procTrf, false);
		List<InformacaoCriminalDTO> listaInfo = new ArrayList<InformacaoCriminalDTO>();
		for (ProcessoParte pp : listaPartesPassivo) {
			InformacaoCriminalDTO info = new InformacaoCriminalDTO();
			info.setConteudo(this.criarConteudoInformacaoCriminal());
			info.setParte(this.criarParteCriminalDTO(pp));
			listaInfo.add(info);
		}
		
		if(!CollectionUtilsPje.isEmpty(listaInfo)){
			ret = processoCriminalRestClient.inserirInformacoesCriminaisAoProcessoCriminal(processoCriminal, listaInfo);
		}
		
		return ret;
	}
	
	private ConteudoInformacaoCriminalBean criarConteudoInformacaoCriminal() {
		ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean();
		return conteudo;
	}
	
	private ParteDTO criarParteCriminalDTO(ProcessoParte pp) {
		ParteDTO resultado = null;
		if (pp != null) {
			Long idProcessoParteLegacy = Long.valueOf(pp.getIdProcessoParte());
			Long idPessoaLegacy = Long.valueOf(pp.getIdPessoa());
			ProcessoParteSituacaoEnum inSituacao = pp.getInSituacao();
			resultado = new ParteDTO(idProcessoParteLegacy, idPessoaLegacy, null, inSituacao);
		}
		return resultado;
	}
	
	private List<ProcessoProcedimentoOrigem> recuperarPorIdProcessoTrf(Integer idProcessoTrf){
		List<ProcessoProcedimentoOrigem> ppoList = null;
		if(cacheListaProcessoProcedimentoOrigem != null && !cacheListaProcessoProcedimentoOrigem.isEmpty()) {
			ppoList = cacheListaProcessoProcedimentoOrigem.get(idProcessoTrf);
		}
		if(ppoList == null) {
			ppoList = this.processoProcedimentoOrigemManager.recuperarPorIdProcessTrf(idProcessoTrf);
			cacheListaProcessoProcedimentoOrigem.put(idProcessoTrf, ppoList);
		}
		return ppoList;
	}
	
	private List<ProcessoProcedimentoOrigemDTO> recuperarListaProcessoProcedimentoOrigem(Integer idProcessoTrf) {
		List<ProcessoProcedimentoOrigem> ppoList = this.recuperarPorIdProcessoTrf(idProcessoTrf);
		List<ProcessoProcedimentoOrigemDTO> ppoDtoList = new ArrayList<>();
		
		for (ProcessoProcedimentoOrigem ppo : ppoList) {
			ProcessoProcedimentoOrigemDTO ppoDTO = this.getProcessoProcedimentoOrigemDTO(ppo);
			ppoDtoList.add(ppoDTO);
		}
		
		return ppoDtoList;
	}
	
	private Date recuperarDataFatoProcesso(ProcessoTrf procTrf) {
		List<ProcessoProcedimentoOrigem> ppoList = this.recuperarPorIdProcessoTrf(procTrf.getIdProcessoTrf());
		Date dataLocalFato = null;
		
		for (ProcessoProcedimentoOrigem ppo : ppoList) {
			if(ppo.getDtLocalFato() != null) {
				dataLocalFato = ppo.getDtLocalFato();
				break;
			}
		}
		if(dataLocalFato == null) {
			dataLocalFato = procTrf.getDataAutuacao();
		}
		
		return dataLocalFato;
	}

	private MunicipioDTO getMunicipioDTO(ProcessoTrf processo) {
		MunicipioDTO dto = null;
		if (processo != null && processo.getMunicipioFatoPrincipal() != null) {
			Municipio municipio = processo.getMunicipioFatoPrincipal();
			Estado estado = municipio.getEstado();
			
			dto = new MunicipioDTO();
			dto.setCodigoIbge(municipio.getCodigoIbge());
			dto.setMunicipio(municipio.getMunicipio());
			dto.setUf((estado != null ? estado.getCodEstado() : null));
		}
		return dto;
	}
	
	private ProcessoProcedimentoOrigemDTO getProcessoProcedimentoOrigemDTO(ProcessoProcedimentoOrigem ppo) {
		ProcessoProcedimentoOrigemDTO ppoDTO = null;
		
		ppoDTO = new ProcessoProcedimentoOrigemDTO();
		ppoDTO.setAno(ppo.getAno());
		ppoDTO.setAtivo(ppo.getAtivo());
		ppoDTO.setDataInstauracao(ppo.getDataInstauracao());
		ppoDTO.setDataLavratura(ppo.getDataInstauracao());
		ppoDTO.setNrProtocoloPolicia(ppo.getNrProtocoloPolicia());
		ppoDTO.setNumero(ppo.getNumero());
		ppoDTO.setRetombamentoRedistribuicao(false);
		ppoDTO.setTipoOrigem(getTipoOrigem(ppo.getTipoOrigem()));
		ppoDTO.setTipoProcedimentoOrigem(getTipoProcedimentoOrigemDTO(ppo.getTipoProcedimentoOrigem()));
		ppoDTO.setUf(ppo.getUf());

		// Recupera o órgão originário do serviço criminal (delegacias)
		OrgaoProcedimentoOriginarioDTO opoDTO = this.recuperarOrgaoProcedimentoOriginarioDTO(ppo);
		if(opoDTO != null) {
			ppoDTO.setOrgaoProcedimentoOriginario(opoDTO);
		}
		
		return ppoDTO;
	}
	
	private OrgaoProcedimentoOriginarioDTO recuperarOrgaoProcedimentoOriginarioDTO(OrgaoProcedimentoOriginario opo) {
		OrgaoProcedimentoOriginarioDTO ret = null;
		if (opo != null) {
			if(opo.getCodigoNacional() != null) {
				ret = this.orgaoProcedimentoOriginarioRestClient.findByCodigoNacional(opo.getCodigoNacional());
			}
			if(ret == null){
				OrgaoProcedimentoOriginarioDTO example = new OrgaoProcedimentoOriginarioDTO();
				example.setDsNomeOrgao(opo.getDsNomeOrgao());
				example.setCep(opo.getNrCep());
				
				EntityPageDTO<OrgaoProcedimentoOriginarioDTO> page = this.orgaoProcedimentoOriginarioRestClient.searchResources(0, 1, example);
				
				if(page != null && !CollectionUtilsPje.isEmpty(page.getContent())) {
					ret = page.getContent().get(0);
				}
			}
		}
		return ret;
	}
	
	private OrgaoProcedimentoOriginarioDTO recuperarOrgaoProcedimentoOriginarioDTO(ProcessoProcedimentoOrigem ppo) {
		OrgaoProcedimentoOriginario opo = ppo.getOrgaoProcedimentoOriginario();
		return recuperarOrgaoProcedimentoOriginarioDTO(opo);
	}
	
	public List<Integer> getIdsProcessosCriminaisLegados() {
		return idsProcessosCriminaisLegados;
	}
	
	public void setIdsProcessosCriminaisLegados(List<Integer> idsProcessosCriminaisLegados) {
		this.idsProcessosCriminaisLegados = idsProcessosCriminaisLegados;
	}

	protected TipoProcedimentoOrigemDTO getTipoProcedimentoOrigemDTO(TipoProcedimentoOrigem tpo) {
		TipoProcedimentoOrigemDTO resultado = null;
		
		if (tpo != null && this.listaTiposProcedimentoOrigem != null) {
			for (TipoProcedimentoOrigemDTO tipoProcedimentoOrigem : this.listaTiposProcedimentoOrigem) {
				if(tpo.getCodigoNacional().equals(tipoProcedimentoOrigem.getId())) {
					resultado = tipoProcedimentoOrigem;
					break;
				}
			}
		}
		
		return resultado;
	}
	
	protected TipoOrigemDTO getTipoOrigem(TipoOrigem to) {
		TipoOrigemDTO resultado = null;
		
		if (to != null && this.listaTiposOrigem != null) {
			for (TipoOrigemDTO tipoOrigem : this.listaTiposOrigem) {
				if(to.getCodigoNacional().equals(tipoOrigem.getId())) {
					resultado = tipoOrigem;
					break;
				}
			}
		}
		
		return resultado;
	}
	
	protected ProcessoCriminalDTO recuperaProcessoMigrado(ProcessoTrf processo) throws PJeException {
		ProcessoCriminalDTO processoCriminal = null;
		
		if (processo != null) {
			try {
				String numero = processo.getNumeroProcesso();
				processoCriminal = processoCriminalRestClient.getResourceByProcesso(numero);
			} catch (PJeException e) {
				throw e;
			}
		}
		return processoCriminal;
	}
	
	public void atualizarSituacaoConexao() {
		Integer requisicoesBemSucedidasSemAutenticacao = 0;
		for(int i=0; i < NUM_REQUISICOES_TESTE_CONEXAO; i++) {
			Integer totalTiposOrigem = null;
			try {
				totalTiposOrigem = tipoOrigemRestClient.recuperarTotalTiposOrigem();
			}catch(Exception e) {
				// swallow
			}
			if(totalTiposOrigem != null && totalTiposOrigem > 0) {
				requisicoesBemSucedidasSemAutenticacao++;
			}else {
				break;
			}
		}
		this.setSituacaoConexaoSemAutenticacao(requisicoesBemSucedidasSemAutenticacao == NUM_REQUISICOES_TESTE_CONEXAO);

		if(requisicoesBemSucedidasSemAutenticacao != NUM_REQUISICOES_TESTE_CONEXAO) {
			this.setSituacaoConexaoComAutenticacao(false);
		}else {
			Integer requisicoesBemSucedidasComAutenticacao = 0;
			for(int i=0; i < NUM_REQUISICOES_TESTE_CONEXAO; i++) {
				String usuarioAutenticado = null;
				try {
					usuarioAutenticado = usuarioAutenticadoRestClient.recuperarUsuarioAutenticado();
				}catch(Exception e) {
					// swallow
				}
				if(usuarioAutenticado != null) {
					requisicoesBemSucedidasComAutenticacao++;
				}else {
					break;
				}
			}
			this.setSituacaoConexaoComAutenticacao(requisicoesBemSucedidasComAutenticacao == NUM_REQUISICOES_TESTE_CONEXAO);
		}
	}
	
	public Boolean getSituacaoConexaoSemAutenticacao() {
		return situacaoConexaoSemAutenticacao;
	}

	public void setSituacaoConexaoSemAutenticacao(Boolean situacaoConexaoSemAutenticacao) {
		this.situacaoConexaoSemAutenticacao = situacaoConexaoSemAutenticacao;
	}

	public Boolean getSituacaoConexaoComAutenticacao() {
		return situacaoConexaoComAutenticacao;
	}

	public void setSituacaoConexaoComAutenticacao(Boolean situacaoConexaoComAutenticacao) {
		this.situacaoConexaoComAutenticacao = situacaoConexaoComAutenticacao;
	}

	public Integer getNumProcessosPendentes() {
		return numProcessosPendentes;
	}

	public void setNumProcessosPendentes(Integer numProcessosPendentes) {
		this.numProcessosPendentes = numProcessosPendentes;
	}

	public Integer getNumProcedimentosPendentes() {
		return numProcedimentosPendentes;
	}

	public void setNumProcedimentosPendentes(Integer numProcedimentosPendentes) {
		this.numProcedimentosPendentes = numProcedimentosPendentes;
	}
}
