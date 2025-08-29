package br.jus.cnj.pje.webservice.controller.cadastropartes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.EscolaridadeManager;
import br.com.infox.pje.manager.EstadoCivilManager;
import br.com.infox.pje.manager.EtniaManager;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.PaisManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaFiliacaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProfissaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.cadastropartes.dto.EnderecoDTO;
import br.jus.cnj.pje.webservice.controller.cadastropartes.dto.PessoaDocumentoIdentificacaoDTO;
import br.jus.cnj.pje.webservice.controller.cadastropartes.dto.PessoaFisicaDTO;
import br.jus.cnj.pje.webservice.controller.cadastropartes.dto.ProcessoParteDTO;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Escolaridade;
import br.jus.pje.nucleo.entidades.EstadoCivil;
import br.jus.pje.nucleo.entidades.Etnia;
import br.jus.pje.nucleo.entidades.Pais;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFiliacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Profissao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;


@Name(CadastroPartesRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/cadastro-partes")
@Restrict("#{identity.loggedIn and s:hasRole('pje:criminal:admin')}")
public class CadastroPartesRestController {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "cadastroPartesRestController";
	
	@Logger
	private Log logger;
	
	@In
	private PaisManager paisManager;
	
	@In
	private EstadoCivilManager estadoCivilManager;
	
	@In
	private EscolaridadeManager escolaridadeManager;
	
	@In
	private EtniaManager etniaManager;
	
	@In
	private PessoaFisicaService pessoaFisicaService;
	
	@In
	private ProfissaoManager profissaoManager;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@GET
	@Path("/carregar")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarDadosIniciaisFormulario(){
		Map<String, Object> mapa = new LinkedHashMap<>();
		try {
			
			List<EstadoCivil> estadoCivilList = this.estadoCivilManager.estadoCivilItems();
			List<Escolaridade> escolaridadeList = this.escolaridadeManager.escolaridadeItems();
			List<Etnia> etniaList = this.etniaManager.etniaItems();
			List<Pais> paisList = this.paisManager.findAll();

			mapa.put("estadoCivilList", estadoCivilList);
			mapa.put("escolaridadeList", escolaridadeList);
			mapa.put("etniaList", etniaList);
			mapa.put("paisList", paisList);
			
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
		return Response.ok(mapa).build();
	}
	
	@GET
	@Path("/profissoes/{profissao}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarProfissoes(@PathParam("profissao") String profissao){
		List<Profissao> profissaoList = this.profissaoManager.recuperarProfissoesPorParteNomeProfissao(profissao);
		return Response.ok(profissaoList).build();
	}
	
	@GET
	@Path("/pessoas/cpf/{cpf}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPessoaFisica(@PathParam("cpf") String cpf) {
		Response res = Response.noContent().build();
		
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.findByInscricaoMF(cpf);
			PessoaDocumentoIdentificacao pdi = pessoaDocumentoIdentificacaoManager.recuperarDocumentoPrincipal(pessoaFisica);
			PessoaFisicaDTO dto = new PessoaFisicaDTO(pessoaFisica);
			dto.setDocumentoIdentificacao(new PessoaDocumentoIdentificacaoDTO(pdi));
			res = Response.ok(dto).build();
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
		
		return res;
	}
	
	@GET
	@Path("/pessoas/fisica/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPessoaFisicaPorId(@PathParam("id") Long id) {
		Response res = Response.noContent().build();
		PessoaFisicaManager pessoaFisicaManager = (PessoaFisicaManager)ComponentUtil.getComponent(PessoaFisicaManager.NAME);
		
		if(id != null){
			try {
				PessoaFisica pessoaFisica = pessoaFisicaManager.findById(id.intValue());
				PessoaFisicaDTO dto = new PessoaFisicaDTO(pessoaFisica);
				res = Response.ok(dto).build();
			} catch (PJeBusinessException e) {
				logger.error(e);
			}			
		}
		
		return res;
	}	
	
	@POST
	@Path("/pessoas/fisica/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public Response incluirPessoaFisica(PessoaFisicaDTO pessoaFisicaDTO) {
		Response res = Response.noContent().build();
		if(pessoaFisicaDTO != null){
			PessoaFisica pessoaFisica = new PessoaFisica();
			
			try {
				pessoaFisica = (PessoaFisica) this.pessoaService.findByInscricaoMF(pessoaFisicaDTO.getDocumentoPrincipal());
				
				pessoaFisica = this.convertPessoaFisicaDTOToPessoaFisica(pessoaFisica, pessoaFisicaDTO);
				
				pessoaFisica = this.pessoaFisicaService.persist(pessoaFisica);
				
			} catch (PJeBusinessException e) {
				logger.error(e);
			}
			
			pessoaFisicaDTO = new PessoaFisicaDTO(pessoaFisica);
			res = Response.ok(pessoaFisicaDTO).build();		
		}
		
		return res;
	}
	
	@POST
	@Path("/processo-partes/{idProcesso}/fisica")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional	
	public Response incluirProcessoPartePessoaFisica(@PathParam("idProcesso")Long idProcesso, ProcessoParteDTO processoParte) {
		Response res = Response.noContent().build();
		PessoaFisica pf = null;
		TipoParte tipoParte = null;
		ProcessoTrf processoJudicial = null;
		ProcessoParteParticipacaoEnum participacaoEnum = null;
		
		try {
			if(processoParte.getTipoParte() != null
					&& processoParte.getIdProcesso() != null
					&& processoParte.getPessoaFisica() != null
					&& processoParte.getParticipacao() != null) {
				
				tipoParte = this.recuperarTipoPartePorId(processoParte.getTipoParte().getIdTipoParte());
				processoJudicial = this.processoJudicialService.findById(processoParte.getIdProcesso().intValue());
				participacaoEnum = processoParte.getParticipacao();
				if(processoParte.getIdPessoa() != null) {
					pf = this.pessoaFisicaService.find(processoParte.getIdPessoa().intValue());					
				}
				if(tipoParte != null && processoJudicial != null){
					pf = this.convertPessoaFisicaDTOToPessoaFisica(pf, processoParte.getPessoaFisica());
					pf = this.pessoaFisicaService.persist(pf);
					ProcessoParte entity;
					if(processoParte.getId() == null){
						entity = this.incluirProcessoParte(pf, processoJudicial, tipoParte, participacaoEnum);
					} else {
						entity = this.processoParteManager.findById(processoParte.getId().intValue());
					}
					if(entity != null){
						processoParte = new ProcessoParteDTO(entity);
						res = Response.ok(processoParte).build();
						this.processoParteManager.flush();
					}
				}
				
			}
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
		
		return res;
	}
	
	@GET
	@Path("/processos/{idProcesso}/partes/ativo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPartesPoloAtivo(@PathParam("idProcesso") Long idProcesso) {
		return this.recuperarPartesProcesso(idProcesso, ProcessoParteParticipacaoEnum.A);
	}

	@GET
	@Path("/processos/{idProcesso}/partes/passivo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPartesPoloPassivo(@PathParam("idProcesso") Long idProcesso) {
		return this.recuperarPartesProcesso(idProcesso, ProcessoParteParticipacaoEnum.P);
	}
	
	@GET
	@Path("/processos/{idProcesso}/partes/passivo-sem-advogado")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPartesPoloPassivoSemAdvogado(@PathParam("idProcesso") Long idProcesso) {
		return this.recuperarPartesProcesso(idProcesso, ProcessoParteParticipacaoEnum.P, Boolean.TRUE);
	}
	
	@GET
	@Path("/processos/{idProcesso}/partes/outros")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPartesPoloOutros(@PathParam("idProcesso") Long idProcesso) {
		return this.recuperarPartesProcesso(idProcesso, ProcessoParteParticipacaoEnum.T);
	}
	
	@GET
	@Path("/processos/{idProcesso}/partes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPartes(@PathParam("idProcesso") Long idProcesso) {
		return this.recuperarPartesProcesso(idProcesso, null);
	}
	
	@GET
	@Path("/pessoas/{idPessoa}/documentos-identificacao")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPessoaDocumentoIdentificacao(@PathParam("idPessoa") Long idPessoa, @QueryParam("page") Long page, @QueryParam("pageSize") Long pageSize){
		Response res = Response.noContent().build();
		
		List<PessoaDocumentoIdentificacao> listaDocumentos = new ArrayList<>();
		List<PessoaDocumentoIdentificacaoDTO> listaDTO = new ArrayList<>();
		
		if(idPessoa != null && page != null && pageSize != null) {
			listaDocumentos = this.pessoaDocumentoIdentificacaoManager.recuperarDocumentosIdentificacaoPaginados(idPessoa.intValue(), Boolean.TRUE, page.intValue(), pageSize.intValue());
		}
		
		if(!CollectionUtilsPje.isEmpty(listaDocumentos)){
			for (PessoaDocumentoIdentificacao pdi : listaDocumentos) {
				listaDTO.add(new PessoaDocumentoIdentificacaoDTO(pdi));
			}
			res = Response.ok(listaDTO).build();
		}
		
		return res;
	}
	
	@GET
	@Path("/processos/{idProcesso}/tipo-partes/{participacao}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarTipoPartesProcessoJudicial(@PathParam("idProcesso") Long idProcesso, @PathParam("participacao") String participacao){
		Response res = Response.noContent().build();
		List<TipoParte> tipoParteList = new ArrayList<>();
		ProcessoParteParticipacaoEnum participacaoEnum = ProcessoParteParticipacaoEnum.valueOf(participacao);
		
		try {
			ProcessoTrf processoJudicial = this.processoJudicialService.findById(idProcesso.intValue());
			if(processoJudicial != null){
				tipoParteList = this.carregarTipoPartes(processoJudicial, participacaoEnum);
			}
			
			if(!CollectionUtilsPje.isEmpty(tipoParteList)){
				res = Response.ok(tipoParteList).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
		
		return res;
	}
	
	@GET
	@Path("/pessoas/{idPessoa}/enderecos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarEnderecosPessoa(@PathParam("idPessoa") Long idPessoa){
		Response res = Response.noContent().build();
		if(idPessoa != null){
			List<Endereco> listaEnderecos = this.getEnderecoManager().recuperarEnderecosPessoa(idPessoa.intValue());
			if(!CollectionUtilsPje.isEmpty(listaEnderecos)){
				List<EnderecoDTO> enderecoList = new ArrayList<>();
				EnderecoDTO dto = null;
				for (Endereco end : listaEnderecos) {
					dto = new EnderecoDTO(end);
					enderecoList.add(dto);
				}
				res = Response.ok(enderecoList).build();
			}
		}
		
		return res;
	}
	
	private Response recuperarPartesProcesso(Long idProcesso, ProcessoParteParticipacaoEnum participacao, Boolean somentePartePrincipal){
		Response res = Response.noContent().build();
		List<ProcessoParteDTO> listaRet = new ArrayList<ProcessoParteDTO>();
		Map<String, Object> mapaPartes = new LinkedHashMap<>();
		
		if(idProcesso != null){
			try {
				if(participacao == null) {
					List<ProcessoParteDTO> poloAtivoList = this.recuperarListaProcessoParteDTO(idProcesso, ProcessoParteParticipacaoEnum.A);
					List<ProcessoParteDTO> poloPassivoList = this.recuperarListaProcessoParteDTO(idProcesso, ProcessoParteParticipacaoEnum.P);
					List<ProcessoParteDTO> poloOutrosList = this.recuperarListaProcessoParteDTO(idProcesso, ProcessoParteParticipacaoEnum.T);
					
					mapaPartes.put("poloAtivoList", poloAtivoList);
					mapaPartes.put("poloPassivoList", poloPassivoList);
					mapaPartes.put("poloOutrosList", poloOutrosList);
					
					res = Response.ok(mapaPartes).build();
					
				} else {
					List<ProcessoParte> listaPartes = this.processoParteManager.recuperaPartesParaExibicao(idProcesso.intValue(), true, null, null, participacao, somentePartePrincipal);
					for (ProcessoParte processoParte : listaPartes) {
						ProcessoParteDTO dto = new ProcessoParteDTO(processoParte);
						dto.setPodeVisualizar(processoParteManager.podeVisualizarNomeDoPolo(processoParte));
						verificarSigiloProcessoParteDTO(dto);
						Pessoa pessoa = processoParte.getPessoa();
						
						if(pessoa instanceof PessoaFisica) {
							PessoaFisicaManager pessoaFisicaManager = (PessoaFisicaManager) ComponentUtil.getComponent(PessoaFisicaManager.NAME);
							PessoaFisica pessoaFisica = pessoaFisicaManager.findById(pessoa.getIdPessoa());
							PessoaFisicaDTO pessoaFisicaDTO = new PessoaFisicaDTO(pessoaFisica);
							dto.setPessoaFisica(pessoaFisicaDTO);
							
							List<String> alcunhas = filtrarNomeAlternativoPorTipo(pessoaFisica.getPessoaNomeAlternativoList(), TipoNomeAlternativoEnum.A);
							dto.getPessoaFisica().setAlcunhas(alcunhas);
							
							List<String> outrosNomes = filtrarNomeAlternativoPorTipo(pessoaFisica.getPessoaNomeAlternativoList(), TipoNomeAlternativoEnum.O);
							dto.getPessoaFisica().setOutrosNomes(outrosNomes);
							
							dto.getPessoaFisica().setNomePai(pessoaFisica.getNomeGenitor());
							dto.getPessoaFisica().setNomeMae(pessoaFisica.getNomeGenitora());
							
							List<String> outasFiliacoes = getOutrasFiliacoes(pessoa);
							dto.getPessoaFisica().setOutrasFiliacoes(outasFiliacoes);

							List<String> caracteristicasFisicas = getCaracteristicasFisicas(pessoaFisica.getCaracteristicasFisicas());
							dto.getPessoaFisica().setCaracteristicasFisicas(caracteristicasFisicas);
							
							dto.getPessoaFisica().setNaturalidade(pessoaFisica.getMunicipioNascimento() != null ? pessoaFisica.getMunicipioNascimento().getMunicipio() : "");
							dto.getPessoaFisica().setEstado(pessoaFisica.getMunicipioNascimento() != null ? pessoaFisica.getMunicipioNascimento().getEstado().getEstado() : "");
							dto.getPessoaFisica().setCodEstado(pessoaFisica.getMunicipioNascimento() != null ? pessoaFisica.getMunicipioNascimento().getEstado().getCodEstado() : "");
							
						}
						listaRet.add(dto);
					}
					
					if(!CollectionUtilsPje.isEmpty(listaRet)){
						res = Response.ok(listaRet).build();
					}					
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			} 
		}
		
		return res;		
	}
	
	private ProcessoParteDTO adicionaPessoaFisicaProcessoParte(ProcessoParteDTO dto, Pessoa pessoa) {
		if(pessoa instanceof PessoaFisica) {
			try {
				PessoaFisicaManager pessoaFisicaManager = (PessoaFisicaManager) ComponentUtil.getComponent(PessoaFisicaManager.NAME);
				PessoaFisica pessoaFisica = pessoaFisicaManager.findById(pessoa.getIdPessoa());
				PessoaFisicaDTO pessoaFisicaDTO = new PessoaFisicaDTO(pessoaFisica);
				dto.setPessoaFisica(pessoaFisicaDTO);
				
				List<String> alcunhas = filtrarNomeAlternativoPorTipo(pessoaFisica.getPessoaNomeAlternativoList(), TipoNomeAlternativoEnum.A);
				dto.getPessoaFisica().setAlcunhas(alcunhas);
				
				List<String> outrosNomes = filtrarNomeAlternativoPorTipo(pessoaFisica.getPessoaNomeAlternativoList(), TipoNomeAlternativoEnum.O);
				dto.getPessoaFisica().setOutrosNomes(outrosNomes);
				
				dto.getPessoaFisica().setNomePai(pessoaFisica.getNomeGenitor());
				dto.getPessoaFisica().setNomeMae(pessoaFisica.getNomeGenitora());
				
				List<String> outasFiliacoes = getOutrasFiliacoes(pessoa);
				dto.getPessoaFisica().setOutrasFiliacoes(outasFiliacoes);

				List<String> caracteristicasFisicas = getCaracteristicasFisicas(pessoaFisica.getCaracteristicasFisicas());
				dto.getPessoaFisica().setCaracteristicasFisicas(caracteristicasFisicas);
				
				dto.getPessoaFisica().setNaturalidade(pessoaFisica.getMunicipioNascimento() != null ? pessoaFisica.getMunicipioNascimento().getMunicipio() : "");
				dto.getPessoaFisica().setEstado(pessoaFisica.getMunicipioNascimento() != null ? pessoaFisica.getMunicipioNascimento().getEstado().getEstado() : "");
				dto.getPessoaFisica().setCodEstado(pessoaFisica.getMunicipioNascimento() != null ? pessoaFisica.getMunicipioNascimento().getEstado().getCodEstado() : "");
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(e);
			}
										
		}
		return dto;
	}

	private List<String> filtrarNomeAlternativoPorTipo(List<PessoaNomeAlternativo> pessoaNomeAlternativoList, TipoNomeAlternativoEnum tipo) {
		List<String> nomesAlternativos = new ArrayList<>();
		for (PessoaNomeAlternativo p : pessoaNomeAlternativoList) {
			if(p.getTipoNomeAlternativo().equals(tipo)) {
				nomesAlternativos.add(p.getPessoaNomeAlternativo());
			}						
		}
		return nomesAlternativos;
	}
	
	private void verificarSigiloProcessoParteDTO(ProcessoParteDTO dto) {
		if (!dto.getPodeVisualizar().booleanValue()) {
			dto.setNomePessoa("(Em segredo de justia)");
			dto.setDocumentoPrincipal(null);
		}
	}
	
	private List<String> getOutrasFiliacoes(Pessoa pessoa) {
		PessoaFiliacaoManager pessoaFiliacaoManager = (PessoaFiliacaoManager)ComponentUtil.getComponent(PessoaFiliacaoManager.NAME);
		
		List<String> outrasFiliacoes = new ArrayList<>();
		for (PessoaFiliacao pf : pessoaFiliacaoManager.recuperaFiliacoes(pessoa)) {
			StringBuilder sbOutraFiliacao = new StringBuilder();
			sbOutraFiliacao.append(pf.getFiliacao());
			sbOutraFiliacao.append(" (");
			sbOutraFiliacao.append(pf.getTipoFiliacao().getLabel());
			sbOutraFiliacao.append(")");
			outrasFiliacoes.add(sbOutraFiliacao.toString());
		}
		return outrasFiliacoes;
	}
	
	private List<String> getCaracteristicasFisicas(List<br.jus.pje.nucleo.entidades.CaracteristicaFisica> caracteristicasFisicas) {
		
		List<String> caracteristicasFisicasLabel = new ArrayList<>();
		for (br.jus.pje.nucleo.entidades.CaracteristicaFisica cf : caracteristicasFisicas) {
			caracteristicasFisicasLabel.add(cf.getCaracteristicaFisica().getLabel());
		}
		return caracteristicasFisicasLabel;
	}

	private Response recuperarPartesProcesso(Long idProcesso, ProcessoParteParticipacaoEnum participacao){
		return recuperarPartesProcesso(idProcesso, participacao, Boolean.FALSE);
	}
	
	private List<ProcessoParteDTO> recuperarListaProcessoParteDTO(Long idProcesso, ProcessoParteParticipacaoEnum participacao) throws PJeBusinessException{
		ProcessoParteRepresentanteManager procParteRepManager = ComponentUtil.getComponent(ProcessoParteRepresentanteManager.NAME);
		List<ProcessoParteDTO> poloList = new ArrayList<>();
		
		List<ProcessoParte>listaPartes = this.processoParteManager.recuperaPartesParaExibicao(idProcesso.intValue(), true, null, null, participacao, Boolean.FALSE);
		for (ProcessoParte processoParte : listaPartes) {
			if(processoParte.getPartePrincipal()){
				ProcessoParteDTO parteDTO = new ProcessoParteDTO(processoParte);
				parteDTO.setPodeVisualizar(processoParteManager.podeVisualizarNomeDoPolo(processoParte));
				verificarSigiloProcessoParteDTO(parteDTO);
				List<ProcessoParte> representantes = procParteRepManager.recuperarRepresentantesParaExibicao(processoParte.getIdProcessoParte(), true);

				for(ProcessoParte representante : representantes){
					ProcessoParteDTO representanteDTO = new ProcessoParteDTO(representante);
					parteDTO.getRepresentantes().add(representanteDTO);
				}
				
				poloList.add(parteDTO);
			}
		}
		
		return poloList;
	}
	
	private PessoaFisica convertPessoaFisicaDTOToPessoaFisica(PessoaFisica pessoaFisica, PessoaFisicaDTO pessoaFisicaDTO){
		if(pessoaFisica == null){
			pessoaFisica = new PessoaFisica();
		}
		
		pessoaFisica.setNome(pessoaFisicaDTO.getNome());
		pessoaFisica.setNomeGenitora(pessoaFisicaDTO.getNomeMae());
		pessoaFisica.setNomeGenitor(pessoaFisicaDTO.getNomePai());
		pessoaFisica.setSexo(pessoaFisicaDTO.getSexo());
		pessoaFisica.setDataNascimento(pessoaFisicaDTO.getDataNascimento());
		pessoaFisica.setPaisNascimento(pessoaFisicaDTO.getPais());
		pessoaFisica.setEtnia(pessoaFisicaDTO.getEtnia());
		pessoaFisica.setEstadoCivil(pessoaFisicaDTO.getEstadoCivil());
		pessoaFisica.setEscolaridade(pessoaFisicaDTO.getEscolaridade());
		pessoaFisica.setProfissao(pessoaFisicaDTO.getProfissao());
		
		return pessoaFisica;
	}
	
	private TipoParte recuperarTipoPartePorId(Integer idTipoParte) throws PJeBusinessException {
		TipoParteManager tipoParteManager = (TipoParteManager)Component.getInstance(TipoParteManager.NAME);
		return tipoParteManager.findById(idTipoParte);
	}
	
	private List<TipoParte> carregarTipoPartes(ProcessoTrf processoJudicial, ProcessoParteParticipacaoEnum participacao) {
	    List<TipoParteConfigClJudicial> tipoParteConfigClJudicialList = processoJudicial.getClasseJudicial().getTipoParteConfigClJudicial();
	    List<TipoParte> tipoParteList = new ArrayList<>();
	    for (TipoParteConfigClJudicial config : tipoParteConfigClJudicialList) {
	    	incluirTipoParte(config, participacao, tipoParteList);
	    }

	    return tipoParteList;
	}
	
	private void incluirTipoParte(TipoParteConfigClJudicial config, ProcessoParteParticipacaoEnum participacao, List<TipoParte> tipoParteList) {
		TipoParte tipoParte = config.getTipoParteConfiguracao().getTipoParte();
		if (tipoParte.getTipoPrincipal() && tipoParte.getAtivo() && !tipoParteList.contains(tipoParte)) {
	        switch (participacao) {
	        case A:
	            if (config.getTipoParteConfiguracao().getPoloAtivo().booleanValue()) {
	                tipoParteList.add(tipoParte);
	            }
	            break;
	        case P:
	            if (config.getTipoParteConfiguracao().getPoloPassivo().booleanValue()) {
	                tipoParteList.add(tipoParte);
	            }
	            break;
	        case T:
	            if (config.getTipoParteConfiguracao().getOutrosParticipantes().booleanValue()) {
	                tipoParteList.add(tipoParte);
	            }
	            break;
	        default:
	            break;
	        }
		}
	}
	
	private ProcessoParte incluirProcessoParte(Pessoa pessoa, ProcessoTrf processoJudicial, TipoParte tipoParte, ProcessoParteParticipacaoEnum participacaoEnum) throws PJeBusinessException{
		ProcessoParte pp = new ProcessoParte();
		
		pp.setPessoa(pessoa);
		pp.setProcessoTrf(processoJudicial);
		pp.setPartePrincipal(true);
		pp.setInParticipacao(participacaoEnum);
		pp.setTipoParte(tipoParte);
		
		ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager)Component.getInstance("procuradoriaManager");
		List<Procuradoria> procuradorias = procuradoriaManager.getlistProcuradorias(pessoa);

		if(procuradorias != null && procuradorias.size() == 1){
			pp.setProcuradoria(procuradorias.get(0));
		}
		
		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		
		if (Authenticator.isAdvogado() && participacaoEnum.equals(ProcessoParteParticipacaoEnum.A)) {
			logger.info(pessoaLogada.getNome());
		}
		
		habilitarVisualizacaoSegredoJustica(pessoa, processoJudicial);
		
		return this.processoParteManager.persist(pp);
	}
	
	private boolean habilitarVisualizacaoSegredoJustica(Pessoa pessoa, ProcessoTrf processoJudicial){
		boolean ret = false;
		
		if(processoJudicial.getSegredoJustica().booleanValue()){
			try {
				ret = processoJudicialService.habilitarVisibilidadeSePartePoloAtivoFiscalLei(processoJudicial, pessoa, null) > 0;
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(e);
			}
		}
		
		return ret;
	}
	
	private EnderecoManager getEnderecoManager(){
		return ComponentUtil.getComponent(EnderecoManager.NAME);
	}
	
	/**
	* Recupera uma lista das partes de um dado processo judicial a ser usada em situaes de leitura (detached).
	* 
	* @param processo o processo judicial
	* @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	* @param first indicao do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	* @param maxResults indicao do mximo de resultados que se pretende recuperar (nulo para recuperar todos)
	* @return a lista de partes
	* @throws PJeBusinessException
	*/
	public List<ProcessoParte> recuperaPartesParaExibicao(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults, ProcessoParteParticipacaoEnum tipoParticipacao, Boolean somentePartePrincipal) throws PJeBusinessException{
		return this.processoParteManager.recuperaPartesParaExibicao(processo.getIdProcessoTrf(), somenteAtivas, first, maxResults, tipoParticipacao, somentePartePrincipal);
	}
	
	public List<ProcessoParte> recuperaPartesParaExibicao(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults) throws PJeBusinessException{
		return this.processoParteManager.recuperaPartesParaExibicao(processo.getIdProcessoTrf(), somenteAtivas, first, maxResults);
	}	
	
}
