package br.jus.cnj.pje.webservice.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CabecalhoProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ProcessoDocumentoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ProcessoEventoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ProcessoParteDTO;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;


@Name(ProcessoJudicialRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/processos")
@Restrict("#{identity.loggedIn}")
public class ProcessoJudicialRestController implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoJudicialRestController";
	
	@Logger
	private Log logger;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService; 
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;	

	@Create
	public void init(){
		
	}
	
	@GET
	@Path("/{idProcesso}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarProcessoJudicialAtual(@PathParam("idProcesso") Integer idProcessoTrf){
		Response res = Response.noContent().build();
		
		ProcessoTrf processoJudicial;
		try {
			processoJudicial = this.recuperarProcessoJudicial(idProcessoTrf);

			if(processoJudicial != null && processoJudicial.getIdProcessoTrf() != 0){
				CabecalhoProcesso proc = new CabecalhoProcesso();
				proc.setIdProcesso(new Long(processoJudicial.getIdProcessoTrf()));
				proc.setNumeroProcesso(processoJudicial.getNumeroProcesso());
				proc.setStatus(processoJudicial.getProcessoStatus().name());
				proc.setJurisdicao(processoJudicial.getJurisdicaoStr());
				proc.setDataDistribuicao(processoJudicial.getDataDistribuicao());
				if (processoJudicial.getOrgaoJulgador() != null) {
					proc.setOrgaoJulgador(processoJudicial.getOrgaoJulgador().getOrgaoJulgador());	
				}
				proc.setClasseJudicial(processoJudicial.getClasseJudicialStr());
				res = Response.ok(proc).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		
		return res;
	}
	
	@GET
	@Path("/numero-processo/{numero-processo}/validar")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validarAcessoAoProcesso(@PathParam("numero-processo") String numeroProcesso){
		Response res = Response.ok(0).build();
		ProcessoTrf processoJudicial;
		try {
			processoJudicial = this.recuperarProcessoJudicial(numeroProcesso);

			if(processoJudicial != null && processoJudicial.getIdProcessoTrf() != 0){
				res = Response.ok(processoJudicial.getIdProcessoTrf()).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		
		return res;
	}
	
	@GET
	@Path("/{idProcesso}/pode-manipular-informacao-criminal")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validarManipulacaoInformacoesCriminais(@PathParam("idProcesso") Integer idProcesso){
		Response res = Response.ok(Boolean.FALSE).build();
		ProcessoTrf processoJudicial;
		try {
			processoJudicial = this.recuperarProcessoJudicial(idProcesso);
			
			if(processoJudicial != null && processoJudicial.getIdProcessoTrf() != 0){
				
				if(podeManipularInformacaoCriminal(processoJudicial)) {
					res = Response.ok(Boolean.TRUE).build();	
				}else {
					throw new RuntimeException("Usuário não pode acessar as informações criminais.");
				}
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		
		return res;
	}

	private boolean podeManipularInformacaoCriminal(ProcessoTrf processoJudicial) {
		return Authenticator.hasRole(Papeis.MANIPULA_INFORMACAO_CRIMINAL) && isLocalizacaoVinculadaAoProcesso(processoJudicial);
	}

	private boolean isLocalizacaoVinculadaAoProcesso(ProcessoTrf processoJudicial) {
		UsuarioLocalizacao ul = Authenticator.getUsuarioLocalizacaoAtual();
		boolean permitido = false;
		
		if(Authenticator.isPapelAdministrador()) {
			permitido = true;
		} else if (ul != null && ul.getUsuarioLocalizacaoMagistradoServidor() != null) {
			UsuarioLocalizacaoMagistradoServidor ulm = ul.getUsuarioLocalizacaoMagistradoServidor();

			if (processoJudicial.getOrgaoJulgador().equals(ulm.getOrgaoJulgador())) {
				permitido = true;
			} else if (processoJudicial.getOrgaoJulgadorColegiado() != null
					&& processoJudicial.getOrgaoJulgadorColegiado().equals(ulm.getOrgaoJulgadorColegiado())) {
				permitido = true;
			}
			if(!permitido) {
				permitido = Authenticator.hasRole(Papeis.MANIPULA_INFORMACAO_CRIMINAL)
						&& processoJudicialService.existeFluxoDeslocadoParaLocalizacao(processoJudicial);
			}
		}
		return permitido;
	}


	@GET
	@Path("/{idProcesso}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarStatusProcessoJudicial(@PathParam("idProcesso") Integer idProcessoTrf){
		Response res = Response.noContent().build();
		
		try {
			ProcessoTrf processoJudicial = this.recuperarProcessoJudicial(idProcessoTrf);

			if(processoJudicial != null && processoJudicial.getIdProcessoTrf() != 0){
				ProcessoStatusEnum status = processoJudicial.getProcessoStatus();
				res = Response.ok(status).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		
		return res;	
	}
	
	@GET
	@Path("/{idProcesso}/poloPassivo")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response recuperarListaPoloPassivo(@PathParam("idProcesso") Integer idProcessoTrf){

		Response res = Response.noContent().build();
		
		ProcessoTrf processoJudicial;
		
		List<ProcessoParteDTO> lista = new ArrayList<ProcessoParteDTO>(0);
		try {
			processoJudicial = this.recuperarProcessoJudicial(idProcessoTrf);

			if(processoJudicial != null && processoJudicial.getIdProcessoTrf() != 0){
				for(ProcessoParte pp : processoJudicial.getListaPartePrincipalPassivo()){
					lista.add(this.processoParteToDTO(pp));
				}
				res = Response.ok(lista).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		
		return res;		
		
	}
	
	@GET
	@Path("/{idProcesso}/movimentacoes")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response recuperarListaProcessoEvento(@PathParam("idProcesso") Integer idProcessoTrf){
		Response res = Response.noContent().build();
		try {
			ProcessoTrf processo = this.recuperarProcessoJudicial(idProcessoTrf);
			if(processo != null && processo.getIdProcessoTrf() != 0){
				List<ProcessoEventoDTO> lista = new ArrayList<>(0);
				processo.getProcesso().getProcessoEventoList()
					.stream()
					.filter(ProcessoEvento::getAtivo)
					.filter(pe->Objects.isNull(pe.getProcessoEventoExcludente()))
					.forEach(pe->lista.add(processoEventoToDTO(pe)));
				res = Response.ok(lista).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
		}
		return res;		
	}
	
	@GET
	@Path("/{idProcesso}/ultimoMovimento")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response recuperarUltimoProcessoEvento(@PathParam("idProcesso") Integer idProcessoTrf){
		Response res = Response.noContent().build();
		try {
			ProcessoEventoManager processoEventoManager = ComponentUtil.getComponent(ProcessoEventoManager.class);
			ProcessoTrf processo = this.recuperarProcessoJudicial(idProcessoTrf);
			if (processo != null && processo.getIdProcessoTrf() != 0) {
				ProcessoEvento pe = processoEventoManager.recuperaUltimaMovimentacao(processo);
				if (pe != null) {
					res = Response.ok(processoEventoToDTO(pe)).build();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	@GET
	@Path("/{idProcesso}/tarefas")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response recuperarTarefasAbertas(@PathParam("idProcesso") Integer idProcessoTrf){
		Response res = Response.noContent().build();
		try {
			List<String> situacaoProcesso = ComponentUtil.getComponent(SituacaoProcessoManager.class).listTarefasByProcessoSemFiltros(idProcessoTrf);
			if (!CollectionUtilsPje.isEmpty(situacaoProcesso)) {
				res = Response.ok(situacaoProcesso).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	@GET
	@Path("/{idProcesso}/atosProcessuais")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarAtosProcessuais(@PathParam("idProcesso") Integer idProcessoTrf) {
		List<ProcessoDocumentoDTO> listDTO = new ArrayList<>();
		Response res = Response.noContent().build();
		try {
			ProcessoTrf processoTrf = recuperarProcessoJudicial(idProcessoTrf);
			List<ProcessoDocumento> processoDocumentoList = documentoJudicialService.getDocumentosPorTipos(
					processoTrf,
					ParametroUtil.instance().getTipoProcessoDocumentoSentenca().getIdTipoProcessoDocumento(),
					ParametroUtil.instance().getTipoProcessoDocumentoDecisao().getIdTipoProcessoDocumento());
			for (ProcessoDocumento pd : processoDocumentoList) {
				listDTO.add(processoDocumentoToDTO(pd, true));
			}
			res = Response.ok(listDTO).build();
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		return res;
	}
	
	@GET
	@Path("/{idProcesso}/atosProcessuais/{idProcessoDocumento}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarAtosProcessuais(@PathParam("idProcesso") Integer idProcessoTrf, @PathParam("idProcessoDocumento") Integer idProcessoDocumento) {
		ProcessoDocumento processoDocumento = null;
		Response res = Response.noContent().build();
		try {
			processoDocumento = documentoJudicialService.getDocumento(idProcessoDocumento);
			if (idProcessoTrf.equals(processoDocumento.getProcesso().getIdProcesso()) && (
				processoDocumento.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoSentenca()) ||
				processoDocumento.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoDecisao()))) {
				ProcessoDocumentoDTO dto = processoDocumentoToDTO(processoDocumento, true);
				res = Response.ok(dto).build();
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			res = Response.serverError().build();
		}
		return res;
	}
	
	private ProcessoEventoDTO processoEventoToDTO(ProcessoEvento procEvento) {
		return new ProcessoEventoDTO(procEvento.getIdProcessoEvento(), 
								     procEvento.getEvento().getCodEvento(), 
								     procEvento.getEvento().getEvento() , 
								     procEvento.getDataAtualizacao(), 
								     procEvento.getTextoFinalExterno());
	}
	
	private ProcessoParteDTO processoParteToDTO(ProcessoParte processoParte){
		ProcessoParteDTO dto = new ProcessoParteDTO();
		
		dto.setIdProcessoParte(processoParte.getIdProcessoParte());
		dto.setNomeParte(processoParte.getNomeParte());
		dto.setDocumentoIdentificatorio(processoParte.getPessoa().getDocumentoCpfCnpj());
		dto.setTipoParte(processoParte.getTipoParte().getTipoParte());
		dto.setTipoPessoa(processoParte.getPessoa().getInTipoPessoa());

		try {
			if(processoParte.getPessoa().getInTipoPessoa().equals(TipoPessoaEnum.F)){
				PessoaFisica pessoaFisica = pessoaFisicaManager.findById(processoParte.getPessoa().getIdPessoa());
				dto.setDataNascimento(pessoaFisica.getDataNascimento());
				dto.setFiliacoes(this.recuperarFiliacoes(pessoaFisica));
			} else if (processoParte.getPessoa().getInTipoPessoa().equals(TipoPessoaEnum.J)){
				// TODO: dados especificos para pessoa juridica
			}
		} catch (PJeBusinessException e) {
			logger.error("Ocorreu um erro ao tentar recuperar os dados da pessoa selecionada");
			e.printStackTrace();
		}
		
		return dto;
	}
	
	private List<String> recuperarFiliacoes(PessoaFisica pessoaFisica){
		List<String> filiacoes = new ArrayList<String>(0);
		
		if(pessoaFisica != null){
			filiacoes.add(pessoaFisica.getNomeGenitor());
			filiacoes.add(pessoaFisica.getNomeGenitora());
		}
		
		return filiacoes;
	}
	
	private ProcessoDocumentoDTO processoDocumentoToDTO(ProcessoDocumento pd, Boolean incluirConteudoDocumento) {
		String conteudoDocumento = incluirConteudoDocumento ? pd.getProcessoDocumentoBin().getModeloDocumento() : null;
		return new ProcessoDocumentoDTO(pd.getIdProcessoDocumento(),
										pd.getProcessoDocumento(),
										pd.getTipoProcessoDocumento().getTipoProcessoDocumento(),
										conteudoDocumento,
										pd.getNomeUsuarioJuntada(),
										pd.getDataJuntada(),
										pd.getDocumentoSigiloso());
	}
	
	private ProcessoTrf recuperarProcessoJudicial(Integer idProcessoTrf) throws PJeBusinessException{
		return this.processoJudicialManager.recuperarProcesso(idProcessoTrf, 
				(Identity) Component.getInstance("org.jboss.seam.security.identity"), 
				Authenticator.getPessoaLogada(), 
				Authenticator.getUsuarioLocalizacaoAtual(),
				false,
				null);
	}
	
	private ProcessoTrf recuperarProcessoJudicial(String numeroProcesso) throws PJeBusinessException{
		return this.processoJudicialManager.recuperarProcesso(null, 
				(Identity) Component.getInstance("org.jboss.seam.security.identity"), 
				Authenticator.getPessoaLogada(), 
				Authenticator.getUsuarioLocalizacaoAtual(),
				false,
				numeroProcesso);
	}	
	

}
