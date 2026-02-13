package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.SeSynchronizations;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.CargoManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.TipoSessaoManager;
import br.jus.cnj.pje.business.dao.ListProcessoCompletoBetaDAO;
import br.jus.cnj.pje.editor.lool.LibreOfficeManager;
import br.jus.cnj.pje.extensao.AssinadorA1;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.ResultadoAssinatura;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.CriterioFiltroManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.FiltroManager;
import br.jus.cnj.pje.nucleo.manager.FiltroTagManager;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.LembreteManager;
import br.jus.cnj.pje.nucleo.manager.ListProcessoCompletoBetaManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTagManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.TagManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLoginManager;
import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.cnj.pje.nucleo.service.FiltroService;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.webservice.UsuarioInformacaoImpl;
import br.jus.cnj.pje.webservice.api.IPainelUsuarioInternoService;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AplicarFiltroDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ArquivoAssinatura;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AssuntoTrfDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CabecalhoProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CargoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ClasseJudicialDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioFiltroDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Etiqueta;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.EtiquetaProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.FiltroDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.JurisdicaoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.MovimentoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.OrgaoJulgadorColegiadoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.OrgaoJulgadorDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.PagedQueryResult;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.PrioridadeProcessoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.SaidaTarefa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TagDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaPendente;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaPendenteAssinatura;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoProcessoDocumentoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoSessaoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TransicaoDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.UsuarioResponsavelDTO;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessaoPainelUsuario;
import br.jus.je.pje.manager.EleicaoManager;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.CriterioFiltro;
import br.jus.pje.nucleo.entidades.Filtro;
import br.jus.pje.nucleo.entidades.FiltroTag;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Tag;
import br.jus.pje.nucleo.entidades.TagMin;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.entidades.min.SessaoJulgamentoMin;
import br.jus.pje.nucleo.util.Crypto;

@Name(PainelUsuarioInternoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/painelUsuario")
@Restrict("#{identity.loggedIn}")
public class PainelUsuarioInternoRestController implements IPainelUsuarioInternoService {

	public static final String NAME = "painelUsuarioInternoServiceController";
	public static final String ERRO = "Erro: ";

	@In(create = true, required = false)
	private AssinadorA1 assinadorA1;
	
	@In
	private FluxoManager fluxoManager;

	@In
	private FluxoService fluxoService;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoTagManager processoTagManager;

	@In(create = true)
	private UsuarioInformacaoImpl usuarioInformacaoRest;

	@In
	private transient TramitacaoProcessualImpl tramitacaoProcessualService;

	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;

	@In
	private CargoManager cargoManager;

	@In
	private EleicaoManager eleicaoManager;

	@In
	private EstadoManager estadoManager;

	@In
	private MunicipioManager municipioManager;

	@In
	private ParametroService parametroService;

	@In
	private PrioridadeProcessoManager prioridadeProcessoManager;

	@In
	private SecurityTokenControler securityTokenControler;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@In
	private FiltroManager filtroManager;

	@In
	private FiltroService filtroService;

	@In
	private TagManager tagManager;

	@In
	private AssuntoTrfManager assuntoTrfManager;

	@In
	private ClasseJudicialManager classeJudicialManager;

	@In
	private CriterioFiltroManager criterioFiltroManager;

	@In
	private JurisdicaoManager jurisdicaoManager;

	@In
	private FiltroTagManager filtroTagManager;
	
	@In
	private TipoSessaoManager tipoSessaoManager;

	@In
	private UsuarioLoginManager usuarioLoginManager;
	
	@In
	private SessaoJulgamentoManager sessaoJulgamentoManager;
	
	@In
	private EventoManager eventoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private AutomacaoTagService automacaoTagService;

	@Logger
	private Log log;

	private InformacaoUsuarioSessao informacaoDoUsuario;

	@POST
	@Path("/tarefas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TarefaPendente> listarTarefasUsuario(JsonObject pesquisa) {
		List<TarefaPendente> retorno = this.listarTarefasUsuarioWorker(pesquisa, false);
		return retorno;
	}

	@GET
	@Path("/tarefas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TarefaDTO> listarTarefasLocalizacao() {
		List<TarefaDTO> retorno = this.fluxoManager.carregarListaTarefasLocalizacao(getUsuarioSesssao().getIdLocalizacaoModelo());
		return retorno;
	}

	@POST
	@Path("/tarefasFavoritas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TarefaPendente> listarTarefasFavoritasUsuario(JsonObject pesquisa) {
		List<TarefaPendente> retorno = this.listarTarefasUsuarioWorker(pesquisa, true);
		return retorno;
	}

	@GET
	@Path("/tarefas/minutas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TarefaPendenteAssinatura> listarQuantidadeMinutasEmElaboracaoPorTipoDocumento() {

		Map<String, Long> minutas = fluxoManager.recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumentoService(
				getUsuarioSesssao().getIdOrgaoJulgadorColegiado(),
				getUsuarioSesssao().isServidorExclusivoOJC(),
				getUsuarioSesssao().getIdsOrgaoJulgadorCargoVisibilidade(),
				true,
				getUsuarioSesssao().getIdUsuario(),
				getUsuarioSesssao().getIdsLocalizacoesFisicasFilhas(),
				getUsuarioSesssao().getIdLocalizacaoModelo(), 
				getUsuarioSesssao().getIdPapel(),
				getUsuarioSesssao().getVisualizaSigiloso(), 
				getUsuarioSesssao().getNivelAcessoSigilo(),
				null, getUsuarioSesssao().getCargoAuxiliar());
		List<TarefaPendenteAssinatura> tarefas = new ArrayList<TarefaPendenteAssinatura>();
		for (String minuta : minutas.keySet()) {
			String[] tarefaAtributos = minuta.split(":");
			String tipoDocumento = tarefaAtributos[0];
			Integer idTipoDocumento = Integer.parseInt(tarefaAtributos[1]);
			tarefas.add(new TarefaPendenteAssinatura(new TipoProcessoDocumento(idTipoDocumento, tipoDocumento),
					minutas.get(minuta).intValue()));
		}
		return tarefas;
	}

	@POST
	@Path("/recuperarProcessosTarefaAssinaturaComCriterios/{tipoDocumento}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PagedQueryResult<CabecalhoProcesso> recuperarProcessos(@PathParam("tipoDocumento") Integer tipoDocumento,
			CriterioPesquisa criterios) {
		PagedQueryResult<CabecalhoProcesso> processosMetadados = processoJudicialManager
				.recuperarMetadadosProcessoParaAssinatura(getUsuarioSesssao(), tipoDocumento, criterios);
		return processosMetadados;
	}

	@POST
	@Path("/recuperarProcessosTarefaPendenteComCriterios/{tarefa}/{somenteFavoritas}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PagedQueryResult<CabecalhoProcesso> recuperarProcessos(@PathParam("tarefa") String tarefa,
			@PathParam("somenteFavoritas") Boolean somenteFavoritas, CriterioPesquisa crit) {

		if (crit!=null) {
			crit.setSomenteFavoritas(somenteFavoritas);
		}
		
		PagedQueryResult<CabecalhoProcesso> processosMetadados = processoJudicialManager
				.recuperarMetadadosProcesso(getUsuarioSesssao(), tarefa, crit);

		return processosMetadados;
	}
	
	@POST
	@Path("/recuperarEtiquetasQuantitativoProcessoTarefaPendente/{tarefa}/{somenteFavoritas}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recuperarEtiquetasQuantitativoProcessoTarefaPendente(@PathParam("tarefa") String tarefa,
			@PathParam("somenteFavoritas") Boolean somenteFavoritas, CriterioPesquisa crit) {

		crit.setSomenteFavoritas(somenteFavoritas);

		List<TagDTO> resultado = processoJudicialManager
				.recuperarEtiquetasQuantitativoProcessoTarefaPendente(getUsuarioSesssao(), tarefa, crit);

		return Response.ok(resultado).build();
	}

	@POST
	@Path("/recuperarProcessosTarefaPendentePorEtiqueta/{tarefa}/{idTag}/{somenteFavoritas}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recuperarProcessosTarefaPorEtiqueta(@PathParam("tarefa") String tarefa,
			@PathParam("idTag") Integer idTag, @PathParam("somenteFavoritas") Boolean somenteFavoritas,
			CriterioPesquisa crit) {
		

		PagedQueryResult<CabecalhoProcesso> processos = processoJudicialManager
				.recuperarProcessoTarefaPorEtiqueta(getUsuarioSesssao(), tarefa, idTag, crit);

		return Response.ok(processos).build();
	}
	
    @POST
    @Path("/recuperarEtiquetasQuantitativoProcessoAssinaturaPendente/{tipoDocumento}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recuperarEtiquetasQuantitativoProcessoAssinaturaPendente(@PathParam("tipoDocumento")Integer tipoDocumento,CriterioPesquisa crit) {
        List<TagDTO> processosMetadados = processoJudicialManager.recuperarEtiquetasQuantitativoParaAssinatura(getUsuarioSesssao(), tipoDocumento, crit);

        return Response.ok(processosMetadados).build();
    }
	
    @POST
    @Path("/recuperarProcessosTarefaAssinaturaPendentePorEtiqueta/{tipoDocumento}/{idTag}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recuperarProcessosTarefaAssinaturaPorEtiqueta(
    	@PathParam("tipoDocumento") Integer tipoDocumento,
        @PathParam("idTag") Integer idTag,
        CriterioPesquisa crit) {

        PagedQueryResult<CabecalhoProcesso> processos = processoJudicialManager.recuperarProcessoTarefaAssinaturaPorEtiqueta(
            getUsuarioSesssao(), tipoDocumento, idTag, crit);
        
        return Response.ok(processos).build();
    }

	@POST
	@Path("/processoTags/listarTagsUsuario")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listarTagsUsuario(CriterioPesquisa crit) {

		Integer idLocalizacao = getUsuarioSesssao().getIdLocalizacaoFisica();
		PagedQueryResult<Tag> resString = processoTagManager.listarTagsArvoreUsuarioPaginado(crit,
				idLocalizacao);
		List<TagDTO> etiquetas = new ArrayList<TagDTO>();
		List<TagMin> etiquetasSessao = getTagsFavoritasUsuario();
		for (Tag s : resString.getEntities()) {
			TagDTO t = new TagDTO(s);

	        if(etiquetasSessao != null){
	        	for (TagMin tagSessao: etiquetasSessao) {
	        		if ( s.getId().equals(tagSessao.getId()) ) {
	        			t.setFavorita(true);
	        		}
	        	}
	        }
			
			etiquetas.add(t);
		}
		return Response.ok(new PagedQueryResult<TagDTO>(resString.getCount(), etiquetas)).build();
	}

	private List<TagMin> getTagsFavoritasUsuario() {
		return processoTagManager.listarTagsFavoritasUsuario(null, getUsuarioSesssao().getIdLocalizacaoFisica(),
				getUsuarioSesssao().getIdUsuario());
	}

	@GET
	@Path("/dadosUsuario")
	@Produces(MediaType.APPLICATION_JSON)
	public InformacaoUsuarioSessaoPainelUsuario getUsuarioSesssaoPainel() {
		InformacaoUsuarioSessao usuarioSessao = getUsuarioSesssao();
		InformacaoUsuarioSessaoPainelUsuario usuario = new InformacaoUsuarioSessaoPainelUsuario(usuarioSessao);
		usuario.setPodeEditarTags(hasRoleEdicaoTags());
		usuario.setPodeVisualizarPainelMagistradoSessao(hasRolePainelMagistradoSessao());
		return usuario;
	}

	@GET
	@Path("/tagEdicao/{idTag}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listarTagParaEdicao(@PathParam("idTag") Integer idTag) {
		if (!getUsuarioSesssaoPainel().getPodeEditarTags()) {
			return Response.serverError().status(Response.Status.FORBIDDEN).build();
		}
		return Response.ok(new TagDTO(processoTagManager.listarTagParaEdicao(idTag))).build();
	}

	@GET
	@Path("/recuperarProcesso/{idTaskInstance}/{assinatura}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CabecalhoProcesso recuperarProcesso(@PathParam("idTaskInstance") Long idTaskInstance,
			@PathParam("assinatura") Boolean assinatura) {
		CriterioPesquisa crit = new CriterioPesquisa();
		crit.setMaxResults(1);
		crit.setIdTaskInstance(idTaskInstance);
		PagedQueryResult<CabecalhoProcesso> processoMetadados = null;
		if (assinatura) {
			processoMetadados = processoJudicialManager.recuperarMetadadosProcessoParaAssinatura(getUsuarioSesssao(),
					null, crit);
		} else {
			processoMetadados = processoJudicialManager.recuperarMetadadosProcesso(getUsuarioSesssao(), null, crit);
		}

		if(CollectionUtilsPje.isNotEmpty(processoMetadados.getEntities())) {
			return processoMetadados.getEntities().get(0);
		}
		return null;
	}
	
	@GET
	@Path("/recuperarProcessoPorTarefaIdentificador/{nomeTarefa}/{idProcesso}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CabecalhoProcesso recuperarProcessoEspecifico(@PathParam("nomeTarefa") String nomeTarefa, @PathParam("idProcesso") Long idProcesso) {
		CriterioPesquisa crit = new CriterioPesquisa();
		crit.setMaxResults(1);
		crit.setIdProcessoTrf(idProcesso.intValue());
		PagedQueryResult<CabecalhoProcesso> processoMetadados = processoJudicialManager.recuperarMetadadosProcesso(getUsuarioSesssao(), nomeTarefa, crit);
		if (CollectionUtilsPje.isNotEmpty(processoMetadados.getEntities())) {
			return processoMetadados.getEntities().get(0);
		}
		return null;
	}

	@GET
	@Path("/transicoes/{idTarefa}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SaidaTarefa> retornaTransicoes(@PathParam("idTarefa") Long idTarefa) {
		List<String> transicoes = fluxoService.recuperarSaidasTarefa(idTarefa);
		List<SaidaTarefa> saidas = new ArrayList<SaidaTarefa>();

		for (String transicao : transicoes) {
			SaidaTarefa saida = new SaidaTarefa();
			saida.setIdTarefa(idTarefa);
			saida.setNomeSaida(transicao);
			saidas.add(saida);
		}

		if (saidas != null) {
			saidas.sort(Comparator.comparing(SaidaTarefa::getNomeSaida));
		}
		return saidas;
	}

	@POST
	@Path("/conferenciaProcesso/{idTarefa}/{idProcesso}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response conferirProcesso(@PathParam("idTarefa") Long idTarefa, @PathParam("idProcesso") Long idProcesso) {
		Response res = Response.ok(true).build();

		try {
			Util.beginTransaction();
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			this.tramitacaoProcessualService.gravaVariavelTarefa(taskInstance,
					Variaveis.CONFERIR_PROCESSO_ASSINATURA, "T");
		} catch (Exception e) {
			Util.rollbackTransaction();
			res = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		Util.commitTransction();
		return res;
	}

	@DELETE
	@Path("/conferenciaProcesso/{idTarefa}/{idProcesso}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response desconferirProcesso(@PathParam("idTarefa") Long idTarefa,
			@PathParam("idProcesso") Long idProcesso) {
		Response res = Response.ok(true).build();
		try {
			Util.beginTransaction();
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			this.tramitacaoProcessualService.apagaVariavelTarefa(taskInstance,
					Variaveis.CONFERIR_PROCESSO_ASSINATURA);
		} catch (Exception e) {
			Util.rollbackTransaction();
			res = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		Util.commitTransction();
		return res;
	}

	@GET
	@Path("/tarefa/limparResponsavel/{idTarefa}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response limparResponsavel(@PathParam("idTarefa") Long idTarefa) {
		Util.beginTransaction();
		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
		if (taskInstance.getEnd() == null && taskInstance.getActorId() != null) {
			if(!taskInstance.getActorId().contentEquals(Authenticator.getUsuarioLogado().getLogin())) {
				taskInstance.setActorId(null);
			}
		}
		Util.commitTransction();
		return Response.ok().build();
	}
	
	@GET
	@Path("/processo/buscarResponsavel/{idTaskInstance}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response buscarResponsavel(@PathParam("idTaskInstance") Long idTaskInstance) {
		Util.beginTransaction();
		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);
		String actorId = taskInstance.getActorId();
		InformacaoUsuarioSessao usuarioSesssao = getUsuarioSesssao();
		Util.commitTransction();
		if (actorId != null) {
			UsuarioResponsavelDTO dto = new UsuarioResponsavelDTO();
			boolean processoBloqueado = !usuarioSesssao.getLogin().equals(actorId);
			dto.setBloqueado(processoBloqueado);
			if (processoBloqueado) {
				UsuarioLogin usuarioResponsavel = usuarioLoginManager.findByLogin(actorId);
				if (usuarioResponsavel != null) {
					dto.setResponsavel(usuarioResponsavel.getNome());
				}
			}
			return Response.ok(dto).build();
		} else {
			return Response.ok(null).build();
		}
	}

	@GET
	@Path("/movimentar/{idTarefa}/{nomeTransicao}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response movimentarProcesso(@PathParam("idTarefa") Long idTarefa,
			@PathParam("nomeTransicao") String nomeTransicao) {
		boolean retorno = false;

		TaskInstanceHome tih = ComponentUtil.getComponent(TaskInstanceHome.NAME);
		tih.setTransicaoSaida(nomeTransicao);

		try {
			retorno = this.fluxoService.finalizarTarefa(idTarefa, false, nomeTransicao);
		} catch (PJeBusinessException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		if (!retorno) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} else {
			Util.commitTransction();
			return Response.ok().build();
		}
	}
	
	@GET
	@Path("/movimentarIndividual/{idTarefa}/{nomeTransicao}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response movimentarProcessoIndividual(@PathParam("idTarefa") Long idTarefa, @PathParam("nomeTransicao") String nomeTransicao) {
		TransicaoDTO retorno = new TransicaoDTO();
		retorno = this.fluxoService.finalizarTarefaIndividual(idTarefa, nomeTransicao);
		return Response.ok(retorno).build();
	}

	@GET
	@Path("/orgaosJulgadores")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterTodosOrgaosJulgadores() {

		List<OrgaoJulgador> orgaos = this.orgaoJulgadorManager.findAll();
		List<OrgaoJulgadorDTO> orgaosDTO = new ArrayList<OrgaoJulgadorDTO>();

		for (OrgaoJulgador orgaoJulgador : orgaos) {
			orgaosDTO.add(new OrgaoJulgadorDTO(orgaoJulgador.getIdOrgaoJulgador(), orgaoJulgador.getOrgaoJulgador()));
		}

		return Response.ok(orgaosDTO).build();
	}
	
	@GET
	@Path("/orgaosJulgadoresColegiado")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterTodosOrgaosJulgadoresColegiados() {
		List<OrgaoJulgadorColegiadoDTO> orgaos = this.orgaoJulgadorColegiadoManager.findAllDTO(); 
		return Response.ok(orgaos).build();
	}

	@GET
	@Path("/cargos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterTodosCargosJudiciais() {

		List<Cargo> listaDeCargosJudiciais = this.cargoManager.cargoItems();
		List<CargoDTO> listaDeCargosJudiciaisDTO = new ArrayList<CargoDTO>();

		for (Cargo cargo : listaDeCargosJudiciais) {
			listaDeCargosJudiciaisDTO.add(new CargoDTO(new Long(cargo.getIdCargo()), cargo.getCargo()));
		}

		return Response.ok(listaDeCargosJudiciaisDTO).build();
	}

	/**
	 * Obtém todas as eleições cadastradas ativas no sistema, ordenadas pelo ano.
	 * 
	 * @return todos as eleições.
	 */
	@GET
	@Path("/eleicoes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterEleicoesAtivas() {
		return Response.ok(this.eleicaoManager.findEleicoes(true)).build();
	}

	/**
	 * Obtém todos os estados ativos.
	 * 
	 * @return todos os estados.
	 */
	@GET
	@Path("/estados")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterEstadosAtivos() {
		return Response.ok(estadoManager.estadoItems()).build();
	}

	/**
	 * Obtém todos os municipios ativos do estado.
	 * 
	 * @return todos os municipios.
	 */
	@GET
	@Path("/municipios/{idEstado}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response retornaMunicipioAtivosPorIdEstado(@PathParam("idEstado") Integer idEstado) {
		return Response.ok(this.municipioManager.findAllByIdEstadoDTO(idEstado)).build();
	}

	/**
	 * Obtém o valor do parâmetro informado.
	 * 
	 * @param nomeParametro
	 *            - Nome do parâmetro.
	 * @return Valor do parâmetro.
	 */
	@GET
	@Path("/parametro/{nomeParametro}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterValorParametro(@PathParam("nomeParametro") String nomeParametro) {

		return Response.ok(new Object[] { parametroService.valueOf(nomeParametro) }).build();
	}

	@GET
	@Path("/prioridades/recuperar")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPrioridades() {
		try {
			List<PrioridadeProcesso> prioridades = prioridadeProcessoManager.listActive();
			List<PrioridadeProcessoDTO> prioridadesDTO = new ArrayList<PrioridadeProcessoDTO>();
			for (PrioridadeProcesso prioridadeProcesso : prioridades) {
				prioridadesDTO.add(new PrioridadeProcessoDTO(prioridadeProcesso.getIdPrioridadeProcesso(),
						prioridadeProcesso.getPrioridade()));
			}
			return Response.ok(prioridadesDTO).build();
		} catch (PJeBusinessException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/gerarChaveAcessoProcesso/{idProcesso}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response gerarChaveAcessoProcesso(@PathParam("idProcesso") Long idProcesso,
			@Context HttpServletRequest contexto) {
		Response res = null;

		String jsessionid = this.recoverJsessionidFromRequestCookies(contexto.getCookies());

		if (idProcesso != null && !jsessionid.isEmpty()) {
			res = Response.ok(this.securityTokenControler.gerarChaveAcessoProcesso(idProcesso.intValue(), jsessionid))
					.build();
		} else {
			res = Response.ok(Status.NO_CONTENT).build();
		}

		return res;
	}
	
	@POST
	@Path("/uploadArquivoAssinado")
	@Produces(MediaType.TEXT_PLAIN)
	public Response uploadArquivoAssinado(@FormParam("id") String id, @FormParam("hash") String hash,
			@FormParam("codIni") String codIni, @FormParam("assinatura") String hashAssinado,
			@FormParam("cadeiaCertificado") String certChain, @FormParam("idTarefa") String idTarefa) {
		ArquivoAssinatura assinatura = new ArquivoAssinatura();
		Response res = Response.ok("Sucesso").build();

		try {
			assinatura.setId(Integer.parseInt(id));
			assinatura.setHash(hash);
			assinatura.setCodIni(codIni);
			assinatura.setIsBin(false);
			assinatura.setHashAssinatura(hashAssinado);
			assinatura.setCertChain(certChain);

			ProcessoDocumento pd = this.documentoJudicialService.getDocumento(assinatura.getId());

			if (pd != null) {
				Util.beginTransaction();

				try {
					documentoJudicialService.validaPendenciaMovimentacaoParaAssinatura(pd, Long.parseLong(idTarefa));

					Util.commitTransction();
				} catch (Exception e) {
					e.printStackTrace();

					Util.rollbackTransaction();

					res = Response.serverError().entity(ERRO + e.getLocalizedMessage()).build();

					return res;
				}

				if (idTarefa != null) {
					assinatura.setIdTarefa(Long.parseLong(idTarefa));
					pd.setIdJbpmTask(assinatura.getIdTarefa());
					pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
				}
				if (Authenticator.getPessoaLogada() != null) {
					SeSynchronizations seSynchronizations = (SeSynchronizations) Component
							.getInstance("org.jboss.seam.transaction.synchronizations");

					seSynchronizations.afterTransactionBegin();

					Util.beginTransaction();

					try {
						assinarDocumento(assinatura);
						assinarDocumentosAnexos(pd);
						registrarJuntadaDocumentoEAnexos(pd);

						if (assinatura.getIdTarefa() != null && !assinatura.getIdTarefa().equals(-1L)) {
							Boolean finalizado = this.fluxoService
									.finalizarTarefaAssociandoDocumentoAoMovimentoSemTransacao(assinatura.getIdTarefa(),
											true, null, pd);

							if (!finalizado) {
								res = Response.notModified("A tarefa não foi transitada").build();
							}
						}

						seSynchronizations.beforeTransactionCommit();

						Util.commitTransction();

						seSynchronizations.afterTransactionCommit(true);
					} catch (Exception e) {
						this.log.error(e.getLocalizedMessage());

						res = Response.serverError().entity("Erro: " + e.getLocalizedMessage()).build();

						Util.rollbackTransaction();

						return res;
					}
				}
			} else {
				res = Response.notModified("Não houve alterações na tarefa e documento").build();
			}
		} catch (Exception e) {
			this.log.error(e.getLocalizedMessage());

			res = Response.serverError().entity("Erro: " + e.getLocalizedMessage()).build();
		}

		return res;
	}

	private Response finalizarAssinatura(ArquivoAssinatura assinatura, String idTarefa) throws Exception {
		Response res = Response.ok().build();

		try {
			ProcessoDocumento pd = this.documentoJudicialService.getDocumento(assinatura.getId());

			if (pd != null) {
				String nomeDocumentoWopi = pd.getProcessoDocumentoBin().getNomeDocumentoWopi();
				LibreOfficeManager libreOfficeManager = null;
				if ( nomeDocumentoWopi!=null ) {
					libreOfficeManager = new LibreOfficeManager(nomeDocumentoWopi);
					pd.getProcessoDocumentoBin().setModeloDocumento( libreOfficeManager.getHtmlContent() );
					pd.getProcessoDocumentoBin().setNomeDocumentoWopi(null);
					ComponentUtil.getComponent(ListProcessoCompletoBetaDAO.class).recuperarConteudoBinario(pd.getProcessoDocumentoBin());
				}

				if (idTarefa != null) {
					assinatura.setIdTarefa(Long.parseLong(idTarefa));
					pd.setIdJbpmTask(assinatura.getIdTarefa());
					JbpmUtil.resumeTask(Long.parseLong(idTarefa));
					pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
				}
				if (Authenticator.getPessoaLogada() != null) {
					SeSynchronizations seSynchronizations = (SeSynchronizations) Component
							.getInstance("org.jboss.seam.transaction.synchronizations");

					seSynchronizations.afterTransactionBegin();

					Util.beginTransaction();

					try {
						assinarDocumento(assinatura);
						assinarDocumentosAnexos(pd);
						registrarJuntadaDocumentoEAnexos(pd);

						if (assinatura.getIdTarefa() != null && !assinatura.getIdTarefa().equals(-1L)) {
							Boolean finalizado = this.fluxoService
									.finalizarTarefaAssociandoDocumentoAoMovimentoSemTransacao(assinatura.getIdTarefa(),
											true, null, pd);

							if (!finalizado) {
								res = Response.notModified("A tarefa não foi transitada").build();
							} else if (libreOfficeManager != null) {
								libreOfficeManager.apagarDocumento();
							}
						}

						seSynchronizations.beforeTransactionCommit();

						Util.commitTransction();

						seSynchronizations.afterTransactionCommit(true);
					} catch (Exception e) {
						this.log.error(e.getLocalizedMessage());

						res = Response.serverError().entity("Erro: " + e.getLocalizedMessage()).build();

						Util.rollbackTransaction();

						return res;
					}
				}
			} else {
				res = Response.notModified("Não houveram alterações na tarefa e documento").build();
			}
		} catch (Exception e) {
			this.log.error(e.getLocalizedMessage());

			res = Response.serverError().entity("Erro: " + e.getLocalizedMessage()).build();
		}
		
		return res;
	}

	private void registrarJuntadaDocumentoEAnexos(ProcessoDocumento processoDocumento) throws PJeBusinessException {
		if (processoDocumento != null) {
			for (ProcessoDocumento anexo : processoDocumento.getDocumentosVinculados()) {
				if (isDocumentoAssinado(anexo)) {
					this.documentoJudicialService.registrarJuntadaDocumento(anexo);
				} else {
					throw new PJeBusinessException(MessageFormat.format("O documento anexo {0} no est assinado.",
							anexo.getIdProcessoDocumento()));
				}
			}
			this.documentoJudicialService.registrarJuntadaDocumento(processoDocumento);
			this.documentoJudicialService.flush();
		}
	}

	private void assinarDocumento(ArquivoAssinatura assinatura) throws Exception {
		if (assinatura != null) {
			this.documentoJudicialService.gravarAssinatura(assinatura.getId().toString(), assinatura.getCodIni(),
					assinatura.getHash(), assinatura.getHashAssinatura(), assinatura.getCertChain(),
					Authenticator.getPessoaLogada());
		}
	}

	private void assinarDocumento(ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		if (arquivoAssinadoHash != null) {
			this.documentoJudicialService.gravarAssinatura(arquivoAssinadoHash.getId(), arquivoAssinadoHash.getCodIni(),
					arquivoAssinadoHash.getHash(), arquivoAssinadoHash.getAssinatura(),
					arquivoAssinadoHash.getCadeiaCertificado(), Authenticator.getPessoaLogada());
		}
	}

	private ArquivoAssinadoHash assinarDocumento(ProcessoDocumento processoDocumento)
			throws Exception {
		String hash = null;

		if (processoDocumento.getProcessoDocumentoBin().isBinario()) {
			hash = Crypto.encodeMD5(getConteudoBinario(processoDocumento));
		} else {
			hash = Crypto.encodeMD5(processoDocumento.getProcessoDocumentoBin().getModeloDocumento());
		}

		if (assinadorA1 == null) {
			throw new Exception("Não foi encontrado nenhum conector para assinatura do documento");
		}

		ResultadoAssinatura res = assinadorA1.assinarHash(hash);

		ArquivoAssinadoHash arquivoAssinadoHash = new ArquivoAssinadoHash();

		arquivoAssinadoHash.setAssinatura(res.getAssinatura());
		arquivoAssinadoHash.setCadeiaCertificado(res.getCadeiaCertificado());
		arquivoAssinadoHash.setId(processoDocumento.getIdProcessoDocumento() + "");

		return arquivoAssinadoHash;
	}

	private byte[] getConteudoBinario(ProcessoDocumento processoDocumento) throws FileNotFoundException, IOException {
		ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class)
				.recuperarConteudoBinario(processoDocumento.getProcessoDocumentoBin());
		return IOUtils.toByteArray(new FileInputStream(processoDocumento.getProcessoDocumentoBin().getFile()));
	}

	private void assinarDocumentosAnexos(ProcessoDocumento pd)
			throws PJeBusinessException, IOException, PontoExtensaoException, Exception {
		for (ProcessoDocumento documentoVinculados : pd.getDocumentosVinculados()) {
			ArquivoAssinadoHash arquivoAssinadoHash = assinarDocumento(documentoVinculados);

			assinarDocumento(arquivoAssinadoHash);
		}
	}

	private boolean isDocumentoAssinado(ProcessoDocumento processoDocumento) {
		return processoDocumento.getAtivo() && processoDocumento.getDataExclusao() == null
				&& ComponentUtil.getAssinaturaDocumentoService().isProcessoDocumentoAssinado(processoDocumento);
	}

	@PUT
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response alterarTag(TagDTO tagDTO) {
		Response response = null;
		try {
			Tag tag = this.tagManager.findById(tagDTO.getId());
			tag.setNomeTag(tagDTO.getNomeTag());
			tag.setNomeTagCompleto(tagDTO.getNomeTagCompleto());
			this.tagManager.persist(tag);
			this.tagManager.flush();
			response = Response.ok(tagDTO).build();
		} catch (Exception e) {
			e.printStackTrace();
			response = Response.serverError().build();
		}
		return response;
	}

	@POST
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response inserirTag(TagDTO parametro) {
		Tag tag = tagManager.criarTag(parametro);

		TagDTO tagDTO = new TagDTO(tag);
		tagDTO.setIdPai(parametro.getIdPai());

		return Response.ok(tagDTO).build();
	}

	@GET
	@Path("/processoTags/todas")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response pesquisarTodas() {
		List<TagDTO> listaDto = new ArrayList<TagDTO>();
		List<TagMin> listaTags = filtrarTagsUsuario(null, false);

		for (TagMin tagMin : listaTags) {
			TagDTO tagDTO = new TagDTO(tagMin);
			listaDto.add(tagDTO);
		}

		return Response.ok(listaDto).build();
	}

	@DELETE
	@Path("/tags/{idTag}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response excluirTag(@PathParam("idTag") Integer idTag) {
		if (!getUsuarioSesssaoPainel().getPodeEditarTags()) {
			return Response.serverError().status(Response.Status.FORBIDDEN).build();
		}
		processoTagManager.excluirHierarquiaTag(idTag);
		try {
			processoTagManager.flush();
			return Response.ok(idTag).build();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
	
	@POST
	@Path("/tags/excluir-lote")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response excluirTagsEmLote(List<Integer> ids) {
		if (!getUsuarioSesssaoPainel().getPodeEditarTags()) {
			return Response.serverError().status(Response.Status.FORBIDDEN).build();
		}
		for (Integer idTag : ids) {
			processoTagManager.excluirHierarquiaTag(idTag);
		}
		try {
			processoTagManager.flush();
			return Response.ok().build();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}	
	
	@DELETE
    @Path("/processoTags/remover/{idTag}/{idProcesso}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void removerTag(@PathParam("idTag") Integer idTag,@PathParam("idProcesso") Long idProcesso) {
		Util.beginTransaction();
        processoTagManager.removerTag(idProcesso, idTag);
        try {
			processoTagManager.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
        Util.commitTransction();
    }

	@GET
	@Path("/tagSessaoUsuario/adicionar/{idTag}")
	@Produces(MediaType.APPLICATION_JSON)
	public void adicionarTagUsuario(@PathParam("idTag") Integer idTag) {
		Util.beginTransaction();
		processoTagManager.adicionarTagSessaoUsuario(idTag, getUsuarioSesssao());
		try {
			processoTagManager.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		Util.commitTransction();
	}

	@GET
	@Path("/tagSessaoUsuario/remover/{idTag}")
	@Produces(MediaType.APPLICATION_JSON)
	public void removerTagUsuario(@PathParam("idTag") Integer idTag) {
		Util.beginTransaction();
		processoTagManager.removerTagSessaoUsuario(idTag, getUsuarioSesssao());
		try {
			processoTagManager.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		Util.commitTransction();
	}

	@POST
	@Path("/processoTags/inserir")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarProcessotag(JsonObject object) {
		Integer idLocalizacaoFisicaUsuario = getUsuarioSesssao().getIdLocalizacaoFisica();
		Integer idUsuario = getUsuarioSesssao().getIdUsuario();
		Long idProcesso = new Long(object.getString("idProcesso"));
		String nomeTag = object.getString("tag");
		ProcessoTag t;
		try {
			t = processoTagManager.criarProcessoTag(idProcesso, nomeTag, idLocalizacaoFisicaUsuario,
					idUsuario);
			processoTagManager.flush();
			EtiquetaProcesso etiqueta = new EtiquetaProcesso(t.getTag().getId(), t.getTag().getNomeTag(),
					t.getIdUsuarioInclusao(), idProcesso);
			return Response.ok(etiqueta).build();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	@POST
	@Path("/processoTags/remover")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response excluirProcessoTag(JsonObject object) {
		Response res = Response.ok().build();
		
		try {
			Long idProcesso = Long.valueOf(object.getInt("idProcesso"));
			Integer idTag = object.getInt("idTag");
			this.processoTagManager.removerTag(idProcesso, idTag);
			this.processoTagManager.flush();
			res = Response.ok(idTag).build();
		} catch (Exception e) {
			res = Response.serverError().build();
		}
		
		return res;
	}

	@GET
	@Path("/processoTags/listar/{idProcesso}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listarTags(@PathParam("idProcesso") Long idProcesso) {
		Integer idLocalizacao = getUsuarioSesssao().getIdLocalizacaoFisica();
		return Response.ok(processoTagManager.listarTags(idProcesso, idLocalizacao, null))
				.build();
	}

	@GET
	@Path("/processoTags/filtrarFavoritas/{nomeTag}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filtarTagsFavoritas(@PathParam("nomeTag") String nomeTag) {
		return filtrarTextoTagsUsuario(nomeTag, true);
	}

	@GET
	@Path("/processoTags/filtrarFavoritas")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filtrarTagsFavoritas() {
		return filtarTagsFavoritas(null);
	}

	@GET
	@Path("/processoTags/filtrar/{nomeTag}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filtarTags(@PathParam("nomeTag") String nomeTag) {
		return filtrarTextoTagsUsuario(nomeTag, false);
	}

	@GET
	@Path("/processoTags/filtrar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filtrarTags() {
		return filtarTags(null);
	}

	@POST
	@Path("/filtros/listar")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listarFiltros(CriterioPesquisa crit) {
		List<FiltroDTO> listaFiltroDTO = new ArrayList<FiltroDTO>();

		Integer idLocalizacao = getUsuarioSesssao().getIdLocalizacaoFisica();

		PagedQueryResult<Filtro> filtros = filtroManager.listarFiltros(crit, idLocalizacao);

		for (Filtro fil : filtros.getEntities()) {
			FiltroDTO dto = new FiltroDTO(fil);
			listaFiltroDTO.add(dto);
		}

		PagedQueryResult<FiltroDTO> pagedFiltros = new PagedQueryResult<FiltroDTO>(filtros.getCount(), listaFiltroDTO);

		return Response.ok(pagedFiltros).build();
	}

	@GET
	@Path("/filtros/listar")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listarTodosFiltros() {
		Integer idLocalizacao = getUsuarioSesssao().getIdLocalizacaoFisica();
		List<Filtro> filtros = filtroManager.listarFiltros(idLocalizacao);
		return Response.ok(filtros).build();
	}

	@POST
	@Path("/filtros")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response salvarFiltro(FiltroDTO filtroDto) {
		Util.beginTransaction();
		Response res = Response.serverError().build();

		filtroDto.setIdLocalizacao(getUsuarioSesssao().getIdLocalizacaoFisica());

		try {
			Filtro filtro = new Filtro();
			if (filtroDto.getId() != null) {
				filtro = this.filtroManager.findById(filtroDto.getId());
			}
			filtro.setNomeFiltro(filtroDto.getNomeFiltro());
			filtro.setIdLocalizacao(getUsuarioSesssao().getIdLocalizacaoFisica());

			filtro = filtroService.criarFiltro(filtro);

			FiltroDTO filtroDTO = new FiltroDTO(filtro);
			res = Response.ok(filtroDTO).build();

			Util.commitTransction();
		} catch (PJeBusinessException e) {
			Util.rollbackTransaction();
			e.printStackTrace();
		}

		return res;
	}

	@POST
	@Path("/filtros/aplicar")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response aplicarFiltrosEtiqueta(AplicarFiltroDTO aplicarFiltro) {
		try {
			filtroService.aplicarFiltros(aplicarFiltro.getIdEtiqueta(), aplicarFiltro.getIdsFiltros());
			return Response.ok().build();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

	@GET
	@Path("/filtroEdicao/{idFiltro}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listarFiltroParaEdicao(@PathParam("idFiltro") Integer idFiltro) {
		return Response.ok(filtroManager.listarFiltroParaEdicao(idFiltro)).build();
	}

	@DELETE
	@Path("/filtro/excluir/{idFiltro}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response excluirFiltro(@PathParam("idFiltro") Integer idFiltro) {
		filtroService.excluirFiltro(idFiltro);
		return Response.ok(idFiltro).build();
	}

	@GET
	@Path("/assuntos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssuntos() {
		Response res = null;

		List<AssuntoTrfDTO> listaAssuntosTrf = this.assuntoTrfManager.findAssuntoTrfFolhaDTO();

		res = Response.ok(listaAssuntosTrf).build();

		return res;
	}

	@GET
	@Path("/classesJudiciais")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassesJudiciais() {
		Response res = null;

		List<ClasseJudicialDTO> listaClasses = this.classeJudicialManager.findAllClasseJudicialDTO();

		res = Response.ok(listaClasses).build();

		return res;
	}

	@GET
	@Path("/filtros/{idFiltro}/criterios")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCriterios(@PathParam("idFiltro") Integer idFiltro) {
		List<CriterioFiltroDTO> listaCriterios = this.criterioFiltroManager.recuperarCriteriosPorIdFiltro(idFiltro);

		return Response.ok(listaCriterios).build();
	}

	@POST
	@Path("/filtros/{idFiltro}/criterios")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response criarCriterio(List<CriterioFiltroDTO> criterios) {
		Response res = null;

		try {
			Filtro filtro = null;
			List<CriterioFiltroDTO> resList = new ArrayList<CriterioFiltroDTO>();

			for (CriterioFiltroDTO criterio : criterios) {
				CriterioFiltro cf = new CriterioFiltro();
				cf.setTipoCriterio(criterio.getTipoCriterio());
				cf.setTextoCriterio(criterio.getTextoCriterio());
				cf.setValorCriterio(criterio.getValorCriterio());
				if (filtro == null) {
					filtro = this.filtroManager.findById(criterio.getIdFiltro());
				}
				cf.setFiltro(filtro);
				cf = this.criterioFiltroManager.persist(cf);
				resList.add(new CriterioFiltroDTO(cf));
			}

			this.criterioFiltroManager.flush();

			res = Response.ok(resList).build();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}

		return res;
	}

	@GET
	@Path("/filtros/{idFiltro}/etiquetas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarEtiquetasFiltro(@PathParam("idFiltro") Integer idFiltro) {
		Response res = null;

		List<Tag> listaTags = new ArrayList<Tag>();

		try {
			listaTags = this.filtroManager.findById(idFiltro).getTags();
			res = Response.ok(listaTags).build();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}

		return res;
	}
	
	@GET
	@Path("/etiquetas/{idEtiqueta}/filtros")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarFiltros(@PathParam("idEtiqueta") Integer idEtiqueta) {
		Response res = null;

		List<FiltroDTO> lista = new ArrayList<FiltroDTO>();

		try {
			lista = this.filtroManager.listarFiltrosByTag(idEtiqueta);
			res = Response.ok(lista).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}

		return res;
	}
	
	@GET
	@Path("/etiquetas/{idEtiqueta}/subetiquetas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarEtiquetasDescendentes(@PathParam("idEtiqueta") Integer idEtiqueta) {
		Response res = null;

		List<TagDTO> lista = new ArrayList<TagDTO>();

		try {
			lista = this.tagManager.listarTodasSubetiquetasDTO(idEtiqueta);
			res = Response.ok(lista).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}

		return res;
	}
	
	@GET
	@Path("/etiquetas/{idEtiqueta}/ascendentes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarEtiquetasAscendentes(@PathParam("idEtiqueta") Integer idEtiqueta) {
		Response res = null;

		List<TagDTO> lista = new ArrayList<TagDTO>();

		try {
			TagDTO pai = this.tagManager.retornaEtiquetaPaiDTO(idEtiqueta);
			if (pai != null) {
				lista.add(pai);
				TagDTO avo = this.tagManager.retornaEtiquetaPaiDTO(pai.getId());
				if (avo!=null) {
					lista.add(avo);	
				}
			}
			res = Response.ok(lista).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}

		return res;
	}
	
	@GET
	@Path("/etiquetas/{idEtiqueta}/nomeTagCompleto")
	@Produces(MediaType.TEXT_PLAIN)
	public Response recuperarNomeTagCompleto(@PathParam("idEtiqueta") Integer idEtiqueta) {
		Response res = null;
		try {
			Tag tag = this.tagManager.findById(idEtiqueta);
			String nomeTagCompleto = "";
			if (tag.getPai()==null) {
				nomeTagCompleto = tag.getNomeTag();
			}
			else {
				if (tag.getPai().getPai()==null) {
					nomeTagCompleto = tag.getPai().getNomeTag() + " > " + tag.getNomeTag();
				}
				else {
					nomeTagCompleto = tag.getPai().getPai().getNomeTag() + " > " + tag.getPai().getNomeTag() + " > " + tag.getNomeTag();
				}
			}
			res = Response.ok(nomeTagCompleto).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}
		return res;
	}
	
	@POST
	@Path("/etiquetas/{idEtiqueta}/nomeTagCompleto")
	@Produces(MediaType.TEXT_PLAIN)
	public Response atualizarNomeTagCompleto(@PathParam("idEtiqueta") Integer idEtiqueta, String nomeTagCompleto) {
		Response res = null;
		try {
			this.tagManager.atualizarNomeTagCompleto(idEtiqueta, nomeTagCompleto);
			res = Response.ok().build();
		}
		catch (Exception e) {
			e.printStackTrace();
			res = Response.serverError().build();
		}
		return res;
	}

	@DELETE
	@Path("/criterios/{idCriterio}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deletarCriterio(@PathParam("idCriterio") Integer idCriterio) {
		Response res = null;

		try {
			CriterioFiltro cf = this.criterioFiltroManager.findById(idCriterio);
			if (cf.getFiltro().getIdLocalizacao() == getUsuarioSesssao().getIdLocalizacaoFisica()) {
				this.criterioFiltroManager.remove(cf);
				this.criterioFiltroManager.flush();
			}

			res = Response.ok(idCriterio).build();
		} catch (PJeBusinessException e) {
			res = Response.serverError().build();
			e.printStackTrace();
		}

		return res;
	}

	@GET
	@Path("/jurisdicoes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarJurisdicoes() {
		Response res = null;

		List<JurisdicaoDTO> jurisdicoes = this.jurisdicaoManager.recuperarJurisdicoesDTO();

		if (jurisdicoes == null || jurisdicoes.isEmpty()) {
			res = Response.ok(new JurisdicaoDTO()).build();
		} else {
			res = Response.ok(jurisdicoes).build();
		}

		return res;
	}
	
	@POST
	@Path("/etiquetas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recuperarEtiquetasUsuario(CriterioPesquisa crit) {
		Response res = Response.ok().build();

		PagedQueryResult<Etiqueta> etiquetas = this.processoTagManager.listarEtiquetasUsuario(crit,
				getUsuarioSesssao().getIdLocalizacaoFisica(), getUsuarioSesssao().getIdUsuario());
		res = Response.ok(etiquetas).build();

		return res;
	}
	
	@GET
	@Path("/etiquetas/{idEtiqueta}/processos")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recuperarProcessosPorEtiqueta(@PathParam("idEtiqueta") Integer idEtiqueta) {
		List<CabecalhoProcesso> processos = this.processoJudicialManager.recuperarProcessosPorEtiqueta(getUsuarioSesssao(), idEtiqueta);
		return Response.ok(processos).build();
	}
	
	@GET
	@Path("/etiquetas/automacao/lista")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recuperarEtiquetasParaAutomacaoUsuario() {
		Response res = Response.ok().build();
		
		List<Integer> idsLocalizacoes = new ArrayList<Integer>();
		idsLocalizacoes.add(getUsuarioSesssao().getIdLocalizacaoFisica());
		List<Tag> tags = this.tagManager.listarTagsParaAutomacao(idsLocalizacoes);
		
		List<TagDTO> tagsDTO = new ArrayList<TagDTO>();
		for (Tag tag: tags) {
			TagDTO tagDTO = new TagDTO(tag.getId(), tag.getNomeTag(), tag.getNomeTagCompleto(), tag.getPai()!=null ? tag.getPai().getId() : null);
			tagsDTO.add(tagDTO);
		}
		
		res = Response.ok(tagsDTO).build();

		return res;
	}
	
	@POST
	@Path("/etiquetas/{idEtiqueta}/automacao-executar")
	@Produces(MediaType.TEXT_PLAIN)
	public Response executarAutomacaoEtiqueta(@PathParam("idEtiqueta") Integer idEtiqueta, Integer idProcesso) {
		Response res = Response.ok().build();
		try {
			String retorno = "";
			ProcessoTrf processo = processoJudicialManager.findById(idProcesso);
			Tag tag = tagManager.findById(idEtiqueta);
			if (automacaoTagService.processoFiltradoEmTag(processo, tag, null)) {
				automacaoTagService.addTagProcesso(tag, processo);
				retorno = tag.getNomeTag();
			}
			res = Response.ok(retorno).build();
		}
		catch (Exception e) {
			res = Response.serverError().build();
			e.printStackTrace();
		}
		return res;
    }
    
    @POST
	@Path("/etiquetas/{idEtiqueta}/automacao-executar-lista")
	@Produces(MediaType.APPLICATION_JSON)
	public Response executarAutomacaoEtiquetaProcessos(@PathParam("idEtiqueta") Integer idEtiqueta, Integer[] idsProcessos) {
		ArrayList<String> nomesTags = new ArrayList<String>();
		for (Integer idProcesso: idsProcessos) {
			String retorno = this.executarAutomacaoEtiqueta(idEtiqueta, idProcesso).getEntity().toString();
			if (retorno!=null && retorno.length()>0) {
				nomesTags.add(retorno);
			}
		}
		return Response.ok(nomesTags).build();
	}
	
	@POST
	@Path("/filtrosTags")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response salvarFiltroTag(FiltroTag filtroTag) {
		Response res = Response.ok().build();

		try {
			FiltroTag persisted = this.filtroTagManager.persist(filtroTag);
			this.filtroTagManager.flush();
			res = Response.ok(persisted).build();
		} catch (PJeBusinessException e) {
			res = Response.serverError().build();
			e.printStackTrace();
		}

		return res;
	}

	@POST
	@Path("/filtrosTags/excluir")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response excluirFiltroTag(FiltroTag filtroTag) {
		Response res = Response.ok(new FiltroTag()).build();

		if (filtroTag.getIdTag() != null && filtroTag.getIdFiltro() != null) {
			FiltroTag ft = this.filtroTagManager.findFiltroTagByIdTagAndIdFiltro(filtroTag.getIdFiltro(),
					filtroTag.getIdTag());
			if (ft != null) {
				try {
					this.filtroTagManager.remove(ft);
					this.filtroTagManager.flush();
				} catch (PJeBusinessException e) {
					res = Response.serverError().build();
					e.printStackTrace();
				}
			}
		}

		return res;
	}
	
    @GET
    @Path("/tiposSessao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retornaTiposSessao(){
    		Response ret = Response.ok().build();
    	
    		List<TipoSessaoDTO> listaTiposSessao = this.tipoSessaoManager.getTipoSessaoDTOItems();
    		
    		if(listaTiposSessao != null && !listaTiposSessao.isEmpty()) {
    			ret = Response.ok(listaTiposSessao).build();
    		}
    		
        return ret;
    }
	
    @POST
    @Path("/sessoesComCriterios")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buscaSessoes(CriterioPesquisa criterios){
        Long qtdSessoes = sessaoJulgamentoManager.buscarQtdSessoes(getUsuarioSesssao(), criterios);
        List<SessaoJulgamentoMin> sessoes = sessaoJulgamentoManager.buscarSessoes(getUsuarioSesssao(), criterios);
        return Response.ok(new PagedQueryResult<SessaoJulgamentoMin>(qtdSessoes,sessoes)).build();
    }
    
    @GET
    @Path("/historicoTarefas/{idProcesso}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retornaHistoricoTarefas(@PathParam("idProcesso") Integer idProcesso){
    	TaskInstanceManager tim = ComponentUtil.getComponent(TaskInstanceManager.NAME);
        return Response.ok(tim.getHistoricoTarefas(idProcesso)).build();
    }
    
    @GET
    @Path("/breadcrumb/{idTaskInstance}/{idProcesso}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterBreadcrumbTaskInstance(
        @PathParam("idTaskInstance") Long idTaskInstance,
        @PathParam("idProcesso") Long idProcesso) {
        return Response.ok(this.obterNomeNosTarefas(idTaskInstance, idProcesso)).build();
    }
    

    private List<String> obterNomeNosTarefas(Long idTaskInstance, Long idProcesso) {
    	FluxoManager fluxoManager = ComponentUtil.getComponent(FluxoManager.NAME);
    	TaskInstanceManager tim = ComponentUtil.getComponent(TaskInstanceManager.NAME);
    	
        List<String> nosTarefa = tim.obterNomeNosTarefas(idTaskInstance);

        if (idProcesso != null) {
            Fluxo fluxo = fluxoManager.obterFluxoDoProcesso(idProcesso);
            nosTarefa.add(0, fluxo.getFluxo());
        }

        return nosTarefa;
    }
    
    @DELETE
    @Path("/lembretes/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response excluirLembrete(@PathParam("id") Integer id) {
    	LembreteManager lembreteManager = ComponentUtil.getComponent(LembreteManager.NAME);
    	lembreteManager.inativaLembretesPorId(id);
        return Response.ok().build();
    }

	private Boolean hasRolePainelMagistradoSessao() {
		return hasRole("/pages/Painel/painel_usuario/Painel_Usuario_Magistrado_2_Grau/listView.seam");
	}

	private Boolean hasRoleEdicaoTags() {
		return hasRole("/pages/Caixa/listView.seam");
	}

	private boolean hasRole(String role) {
		return Identity.instance().hasRole(role);
	}

	private String recoverJsessionidFromRequestCookies(Cookie[] cookies) {

		String jsessionid = new String("");

		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase("JSESSIONID")) {
				jsessionid = cookie.getValue();
				break;
			}
		}

		return jsessionid;
	}

	private List<TarefaPendente> listarTarefasUsuarioWorker(JsonObject pesquisa, boolean somenteFavoritas) {
		InformacaoUsuarioSessao informacaoDoUsuario = getUsuarioSesssao();
		List<TarefaPendente> retorno = new ArrayList<TarefaPendente>();
		if (informacaoDoUsuario != null && pesquisa != null) {
			String numeroProcesso = pesquisa.getString("numeroProcesso");
			String competencia = pesquisa.getString("competencia");
			JsonArray etqs = pesquisa.getJsonArray("etiquetas");

			List<String> etiquetas = new ArrayList<String>();

			if (etqs != null) {
				for (int i = 0; i < etqs.size(); i++) {
					etiquetas.add(etqs.getString(i));
				}
			}

			Map<String, Long> tarefasPendentes = this.fluxoManager.carregarListaTarefasUsuario(
					informacaoDoUsuario.getIdOrgaoJulgadorColegiado(),
					informacaoDoUsuario.isServidorExclusivoOJC(),
					informacaoDoUsuario.getIdsOrgaoJulgadorCargoVisibilidade(),
					informacaoDoUsuario.getIdUsuario(),
					informacaoDoUsuario.getIdsLocalizacoesFisicasFilhas(),
					informacaoDoUsuario.getIdLocalizacaoFisica(),
					informacaoDoUsuario.getIdLocalizacaoModelo(),
					informacaoDoUsuario.getIdPapel(),
					informacaoDoUsuario.getVisualizaSigiloso(), 
					informacaoDoUsuario.getNivelAcessoSigilo(),
					somenteFavoritas,
					numeroProcesso, competencia, etiquetas, informacaoDoUsuario.getCargoAuxiliar());
			
			if (tarefasPendentes != null && tarefasPendentes.size() > 0) {
				for (Map.Entry<String, Long> entry : tarefasPendentes.entrySet()) {
					String[] tarefaAtributos = entry.getKey().split(":");
					String nomeTarefa = tarefaAtributos[0];
					Long idTarefa = Long.parseLong(tarefaAtributos[1]);
					retorno.add(new TarefaPendente(idTarefa, nomeTarefa, entry.getValue().intValue()));
				}
			}
		}
		return retorno;
	}

	private InformacaoUsuarioSessao getUsuarioSesssao() {
		if (this.informacaoDoUsuario == null) {
			this.informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
		}
		return informacaoDoUsuario;
	}

	private List<TagMin> filtrarTagsUsuario(String nomeTag, Boolean somenteFavoritas) {
		Integer idLocalizacao = getUsuarioSesssao().getIdLocalizacaoFisica();
		CriterioPesquisa crit = new CriterioPesquisa();
		crit.setTagsString(nomeTag);
		List<TagMin> tags = new ArrayList<TagMin>();
		if (somenteFavoritas) {
			tags = processoTagManager.listarTagsFavoritasUsuario(crit, idLocalizacao,
					getUsuarioSesssao().getIdUsuario());
		} else {
			tags = processoTagManager.listarTagsUsuario(crit, idLocalizacao);
		}
		return tags;
	}

	private Response filtrarTextoTagsUsuario(String nomeTag, Boolean somenteFavoritas) {
		List<TagMin> tags = filtrarTagsUsuario(nomeTag, somenteFavoritas);
		List<String> tagsString = new ArrayList<String>(tags.size());
		for (TagMin tag : tags) {
			tagsString.add(tag.getNomeTagCompleto());
		}
		return Response.ok(tagsString).build();
	}
	
	@POST
	@Path("/assinarTarefa")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assinarTarefa(JsonObject jsonArquivo) {
		ArquivoAssinatura assinatura = new ArquivoAssinatura();
		Response res = null;
		try {
			Integer id = jsonArquivo.getInt("id");
			String hash = jsonArquivo.getString("hash");
			String codIni = jsonArquivo.getString("codIni");
			Long idTarefa = jsonArquivo.getJsonNumber("idTarefa").longValue();
			
			ProcessoDocumento pd = this.documentoJudicialService.getDocumento(id);

			Util.beginTransaction();

			try {
				documentoJudicialService.validaPendenciaMovimentacaoParaAssinatura(pd, idTarefa);

				Util.commitTransction();
			} catch (Exception e) {
				e.printStackTrace();

				Util.rollbackTransaction();

				res = Response.serverError().entity(ERRO + e.getLocalizedMessage()).build();

				return res;
			}

			if (assinadorA1 == null) {
				throw new Exception("Não foi encontrado nenhum conector para assinatura do documento");
			}

			ResultadoAssinatura resAss = assinadorA1.assinarHash(hash);
			
			String hashAssinado = resAss.getAssinatura();
			String certChain = resAss.getCadeiaCertificado();
			
			assinatura.setId(id);
			assinatura.setHash(hash);
			assinatura.setCodIni(codIni);
			assinatura.setIsBin(false);
			assinatura.setHashAssinatura(hashAssinado);
			assinatura.setCertChain(certChain);

			res = finalizarAssinatura(assinatura, idTarefa+"");
		} catch (Exception e) {
			e.printStackTrace();

			res = Response.serverError().entity(ERRO + e.getLocalizedMessage()).build();
		}
		return res;
	}
	
	@GET
	@Path("/todas-tarefas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TarefaDTO> listarTodasTarefas() {
		List<TarefaDTO> listaTarefaDTO = new ArrayList<>();

		List<Tarefa> tarefaList = EntityUtil.getEntityList(Tarefa.class).stream()
				.sorted(Comparator.comparing(Tarefa::getTarefa))
				.collect(Collectors.toList());
		tarefaList.forEach(t->listaTarefaDTO.add(new TarefaDTO(Long.valueOf(t.getIdTarefa()), t.getTarefa())));
		
		return listaTarefaDTO;		
	}
	
	@GET
	@Path("/movimentos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMovimentos() {
		Response res = null;

		List<MovimentoDTO> listaMovimentos = this.eventoManager.getEventosAtivosDTO();

		res = Response.ok(listaMovimentos).build();

		return res;
	}
	
	@GET
	@Path("/documentos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentos() {
		Response res = null;

		List<TipoProcessoDocumentoDTO> listaDocumentos = this.tipoProcessoDocumentoManager.consultarTodosDisponiveisDTO();

		res = Response.ok(listaDocumentos).build();

		return res;
	}
}