package br.jus.cnj.pje.webservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.transaction.SeSynchronizations;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.ComplementoBean;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.MovimentoBean;
import br.com.infox.ibpm.component.tree.ValorComplementoBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.utils.Constantes;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.integracao.RequisicaoWebServiceIP;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.cnj.pje.webservice.util.RestUtil;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.lancadormovimento.ComplementoSegmentado;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.nucleo.enums.AtividadesLoteEnum;
import br.jus.pje.nucleo.util.Crypto;

@Name("fluxoRest")
@Path("fluxo")
public class FluxoServiceImpl{
	
	@In
	private RestUtil restUtil;
	
	@In
	private FluxoManager fluxoManager;	
	
	@In
	private transient TramitacaoProcessualImpl tramitacaoProcessualService;
	
	@In(create=true)
	private UsuarioInformacaoImpl usuarioInformacaoRest;

	@In(create = true)
	private Authenticator authenticator;
	
	@In
	private UsuarioService usuarioService;

	@GET
	@Path("/listarTarefasFavoritasUsuario")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response listarTarefasFavoritasUsuario(@Context HttpServletRequest contexto) throws JSONException{
		return listarTarefasUsuario(contexto,true);
	}



	@GET
	@Path("/listarTarefasUsuario")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response listarTarefasUsuario(@Context HttpServletRequest contexto) throws JSONException{
		return listarTarefasUsuario(contexto,false);

	}
	
	@GET
	@Path("/listarTarefas")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response listarTarefas(@Context HttpServletRequest contexto){
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());

        if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2())) {
            return RestUtil.erroRequisicao("IP não permitido");
        }

        Response response = null;
        InformacaoUsuarioSessao informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
        try {
            List<String> carregarListaTarefas = fluxoManager.carregarListaTarefas(
                informacaoDoUsuario.getIdLocalizacaoModelo(),
                informacaoDoUsuario.getIdPapel());

            JSONArray json = new JSONArray(carregarListaTarefas);
            response = RestUtil.sucesso(json);
        } catch (Exception e) {
            response = restUtil.reportarErro(e);
        }

        return response;

	}

    private Response listarTarefasUsuario(HttpServletRequest contexto, Boolean somenteFavoritas) throws JSONException {
        RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());

        if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2())) {
            return RestUtil.erroRequisicao("IP não permitido");
        }

        Response response = null;
        InformacaoUsuarioSessao informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
        String numeroProcesso = contexto.getHeader("numeroProcesso");
        String competencia = contexto.getHeader("competencia");

        JSONArray etiquetas = new JSONArray(contexto.getHeader("etiquetas"));
        List<String> etiquetasList = new ArrayList<String>();
        for (int i = 0; i < etiquetas.length(); i++) {
            etiquetasList.add(etiquetas.getString(i));
        }
        try {
            Map<String, Long> carregarListaTarefasUsuario = fluxoManager.carregarListaTarefasUsuario(
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
                numeroProcesso,
                competencia,
                etiquetasList,
				informacaoDoUsuario.getCargoAuxiliar());

            JSONObject json = new JSONObject(carregarListaTarefasUsuario);
            response = RestUtil.sucesso(json);
        } catch (Exception e) {
            response = restUtil.reportarErro(e);
        }

        return response;
    }

	@GET
	@Path("/listarQuantidadeMinutasEmElaboracaoPorTipoDocumento")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response listarQuantidadeMinutasEmElaboracaoPorTipoDocumento(@Context HttpServletRequest contexto) {
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;
		InformacaoUsuarioSessao informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
		Map<String,Object> variaveisSessaoPje2 = usuarioInformacaoRest.recuperarVariaveisPje2();
		List<String> tag = null;
		if(variaveisSessaoPje2 != null){
			String tagString = (String) variaveisSessaoPje2.get("tagsUsuario");
			if(tagString != null && !tagString.isEmpty()){
				tag = Arrays.asList(tagString.split(","));
			}
		}
		try {
			Map<String, Long> recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento = fluxoManager
					.recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumentoService(
							informacaoDoUsuario.getIdOrgaoJulgadorColegiado(), 
							informacaoDoUsuario.isServidorExclusivoOJC(),
							informacaoDoUsuario.getIdsOrgaoJulgadorCargoVisibilidade(),
							null,
							informacaoDoUsuario.getIdUsuario(),
							informacaoDoUsuario.getIdsLocalizacoesFisicasFilhas(),
							informacaoDoUsuario.getIdLocalizacaoModelo(), 
							informacaoDoUsuario.getIdPapel(), 
							informacaoDoUsuario.getVisualizaSigiloso(),
							informacaoDoUsuario.getNivelAcessoSigilo(),
							tag,informacaoDoUsuario.getCargoAuxiliar());
			JSONObject json = new JSONObject(recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento);
			response = RestUtil.sucesso(json);
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}
	
	
	@POST
	@Path("/variavelTarefa/{idTarefa}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response gravarVariavelTarefa(@PathParam("idTarefa") Long idTarefa, 
			String jsonAtributos, @Context HttpServletRequest contexto){
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;
		try {
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			JSONObject json = new JSONObject(jsonAtributos.trim());
			Set<String> keySet = Sets.newHashSet(Iterators.filter(json.keys(), String.class));			
			for (String chave : keySet) {
				tramitacaoProcessualService.gravaVariavelTarefa(taskInstance,chave, json.get(chave));
			}
			response = RestUtil.criadoComSucesso();
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}


	@POST
	@Path("/variavelFluxo/{idTarefa}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response gravarVariavelFluxo(@PathParam("idTarefa") Long idTarefa,
										String jsonAtributos, @Context HttpServletRequest contexto){
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;
		try {
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			ProcessInstance pi = taskInstance.getProcessInstance();
			JSONObject json = new JSONObject(jsonAtributos.trim());
			Set<String> keySet = Sets.newHashSet(Iterators.filter(json.keys(), String.class));
			for (String chave : keySet) {
				pi.getContextInstance().setVariable(chave, json.get(chave));
				//tramitacaoProcessualService.gravaVariavel(taskInstance.getProcessInstance(),chave, json.get(chave));
			}
			response = RestUtil.criadoComSucesso();
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}

	@DELETE
	@Path("/variavelTarefa/{idTarefa}/{chave}")
	@Transactional
	public Response apagarVariavelTarefa(@PathParam("idTarefa") Long idTarefa,
			@PathParam("chave") String chave, @Context HttpServletRequest contexto) {
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;
		try {
			//TODO VERIFICAR SE O USUÁRIO TEM PERMISSÃO PARA FAZER ISSO. VERIFICAR SE O PROCESSO ESTÁ ASSOCIADO A TAREFA
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			tramitacaoProcessualService.apagaVariavelTarefa(taskInstance,chave);
			response = RestUtil.sucesso();
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}

	@DELETE
	@Path("/variavelFluxo/{idTarefa}/{chave}")
	@Transactional
	public Response apagarVariavelFluxo(@PathParam("idTarefa") Long idTarefa,
										 @PathParam("chave") String chave, @Context HttpServletRequest contexto) {
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;
		try {
			//TODO VERIFICAR SE O USUÁRIO TEM PERMISSÃO PARA FAZER ISSO. VERIFICAR SE O PROCESSO ESTÁ ASSOCIADO A TAREFA
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			tramitacaoProcessualService.apagaVariavel(taskInstance.getProcessInstance(),chave);
			response = RestUtil.sucesso();
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}

	@GET
	@Path("/transicoes/{idTarefa}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response recuperarSaidas(@PathParam("idTarefa") Long idTarefa, @Context HttpServletRequest contexto){
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;

		try {
			TaskInstanceUtil taskInstanceUtil = (TaskInstanceUtil) Component.getInstance(TaskInstanceUtil.class);
			List<String> transicoes = taskInstanceUtil.getTransitions(idTarefa);
			JSONArray json = new JSONArray(transicoes);
			response = RestUtil.sucesso(json);
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}

	@GET
	@Path("/finalizarTarefa/{idTarefa}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response finalizarTarefa(@PathParam("idTarefa") Long idTarefa, @QueryParam("nomeTransicao") String nomeTransicao, 
			@QueryParam("isAssinatura") Boolean isAssinatura, @Context HttpServletRequest contexto) {

		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());

		Util.beginTransaction();

		SeSynchronizations seSynchronizations = (SeSynchronizations) Component.getInstance("org.jboss.seam.transaction.synchronizations");
		seSynchronizations.afterTransactionBegin();

		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2())) {
			Util.commitTransction();
			return RestUtil.erroRequisicao("IP não permitido");
		}

		if(idTarefa == null){
			Util.commitTransction();
			return RestUtil.erroRequisicao("Tarefa não selecionada");
		}
		ProcessInstance pi = null;
		Object valor = null;
		Token tk = null;
		BusinessProcess.instance().resumeTask(idTarefa);
		TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();
		if(ti != null){

			pi = ti.getProcessInstance();
			tk = pi.getRootToken();

			if(pi.hasEnded()){
				Util.commitTransction();
				return RestUtil.erroRequisicao("Fluxo finalizado");
			}

			if(ti.hasEnded()){
				Util.commitTransction();
				return RestUtil.erroRequisicao("Tarefa finalizada");
			}
			ProcessoTrfHome.instance().setInstance(null);
			ProcessoTrfHome.instance().setId(pi.getContextInstance().getVariable("processo"));


			InformacaoUsuarioSessao informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
			ti.setActorId(informacaoDoUsuario.getIdUsuario().toString());

			String transition = null;

			if(nomeTransicao !=null && !nomeTransicao.isEmpty()){
				Crypto c = new Crypto(Constantes.CHAVE_PADRAO_CRIPTOGRAFIA);
				String chaveDecodificada = c.decodeDES(nomeTransicao);

				String[] valores = chaveDecodificada.split(":");
				if (valores.length == 2){
					transition = valores[0];
				}
			}
			else{
				transition = (String) ti.getVariableLocally(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			}

			Usuario usuarioLogado = (Usuario) Contexts.getSessionContext().get("usuarioLogado");

			try {
				Boolean lancarMovimentoTemporariamenteComAssinatura = false;
				String lancarNaAssinatura = (String) pi.getContextInstance().getVariable(Variaveis.VARIABLE_CONDICAO_LANCAMENTO_MOVIMENTOS_TEMPORARIO);
				if(lancarNaAssinatura != null && !lancarNaAssinatura.isEmpty()){
					try{
						lancarMovimentoTemporariamenteComAssinatura = (Boolean) Expressions.instance().createValueExpression(lancarNaAssinatura).getValue();
					}catch (Exception e) {
						if(lancarNaAssinatura.equalsIgnoreCase("true")){
							lancarMovimentoTemporariamenteComAssinatura = true;
						}
					}
				}
				if(!lancarMovimentoTemporariamenteComAssinatura){
					List<EventoBean> eventoBeanList = LancadorMovimentosService.instance().getMovimentosTemporarios(pi);

					if(eventoBeanList != null && !eventoBeanList.isEmpty()){
						Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ti);

						ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, idMinuta);
						for(EventoBean eventoBean : eventoBeanList){
							Evento evento = EntityUtil.find(Evento.class,eventoBean.getIdEvento());

							for (MovimentoBean movimentoBean : eventoBean.getMovimentoBeanList()) {
								List<ComplementoSegmentado> complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();

								preencherComplementoSegmentado(movimentoBean,
										complementoSegmentadoList);

								// Lançar o movimento
								LancadorMovimentosService lancadorMovimentosService = new LancadorMovimentosService();
								lancadorMovimentosService.lancarMovimento(evento, complementoSegmentadoList,
										pd, pd.getProcesso(), null, ti.getId(), pi.getId(),usuarioLogado);

							}
						}

					}
				}
			}
			catch(Exception e){
				JbpmUtil.clearAndClose(ManagedJbpmContext.instance());
				Util.rollbackTransaction();
				return RestUtil.erroRequisicao("Erro durante o lançamento de movimentação: " + e.getLocalizedMessage());
			}

			try{
				if(isAssinatura != null && isAssinatura){
					ProcessoDocumentoManager processoDocumentoManager = (ProcessoDocumentoManager)Component.getInstance(ProcessoDocumentoManager.class);
					Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ti);
					
					if(idMinuta == null){
						idMinuta = (Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
					}

					if(idMinuta != null){
						ProcessoDocumento pd = processoDocumentoManager.findById(idMinuta);
						if(pd.getTipoProcessoDocumento().getDocumentoAtoProferido()){
							pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO,idMinuta);

						}
						pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, idMinuta);
						pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
						pi.getContextInstance().deleteVariable(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
					}
				}
				if(transition != null){
					for(Transition t : tk.getAvailableTransitions()){
						if(t.getName().equals(transition)){
							tk.signal(t);
						}
					}
				}else if (isAssinatura != null && isAssinatura) {
					tk.signal();
				}
				Util.commitTransction();
				return RestUtil.sucesso();
			}catch (Exception e){
				JbpmUtil.clearAndClose(ManagedJbpmContext.instance());
				Util.rollbackTransaction();
				return RestUtil.erroRequisicao("Erro durante a sinalização do fluxo: " + e.getLocalizedMessage());
			}
		}
		Util.commitTransction();
		return RestUtil.erroRequisicao("Tarefa não localizada");
	}

	@GET
	@Path("/limparResponsavel/{idTarefa}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response limparResponsavel(@PathParam("idTarefa") Long idTarefa, @Context HttpServletRequest contexto){
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;

		try {
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			if(taskInstance.getEnd() == null && taskInstance.getActorId() != null){
				taskInstance.setActorId(null);
			}
			response = RestUtil.sucesso();
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}

	private void preencherComplementoSegmentado(MovimentoBean movimentoBean,
												List<ComplementoSegmentado> complementoSegmentadoList) {
		// Preencher a lista de ComplementosSegmentados
		for (ComplementoBean complementoBean : movimentoBean.getComplementoBeanList()) {
			TipoComplemento tp = EntityUtil.find(TipoComplemento.class,complementoBean.getIdTipoComplemento());
			for (int i = 0; i < complementoBean.getValorComplementoBeanList().size(); i++) {
				ValorComplementoBean vcb = complementoBean.getValorComplementoBeanList().get(i);
				ComplementoSegmentado complementoSegmentado = new ComplementoSegmentado();
				complementoSegmentado.setOrdem(i);
				complementoSegmentado.setTipoComplemento(tp);
				complementoSegmentado.setTexto(vcb.getCodigo());
				complementoSegmentado.setValorComplemento(vcb.getValor());
				complementoSegmentado.setMovimentoProcesso(null);
				complementoSegmentadoList.add(complementoSegmentado);
			}
		}
	}


	@GET
	@Path("/atributo/{idTarefa}/{somenteTarefa}/{nomeVariavel}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response recuperarVariavel(@PathParam("idTarefa") Long idTarefa, @PathParam("somenteTarefa") Boolean somenteTarefa,
									@PathParam("nomeVariavel") String nomeVariavel ,@Context HttpServletRequest contexto){
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;

		try {
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);
			Object variavel = null;
			if(somenteTarefa != null && somenteTarefa){
				variavel = taskInstance.getVariableLocally(nomeVariavel);
				if(variavel == null){
					variavel = taskInstance.hasVariableLocally(nomeVariavel);
				}
			}else{
				variavel = taskInstance.getVariable(nomeVariavel);
				if(variavel == null){
					variavel = taskInstance.hasVariable(nomeVariavel);
				}
			}

			if(variavel != null){
				response = Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(variavel).build();
			}
			else{
				response = RestUtil.sucesso();
			}
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}

	@GET
	@Path("/atividadesLote/{idTarefa}")
	@Produces({MediaType.APPLICATION_JSON})
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response recuperarAtividadesLote(@PathParam("idTarefa") Long idTarefa,@Context HttpServletRequest contexto) {
		List<String> ret = new ArrayList<String>();
		if(idTarefa == null){
			return null;
		}

		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTarefa);

		if (taskInstance == null ) {
			return null;
		}
		TaskController taskController = taskInstance.getTask().getTaskController();

		if (taskController != null && taskController.getVariableAccesses() != null) {
			String[] tokens;
			for (VariableAccess var : taskController.getVariableAccesses()) {
				if (var.isReadable() && (var.getMappedName() != null)) {
					tokens = var.getMappedName().split(":");
					if ((tokens != null) && (tokens.length > 0)) {
						if (tokens[0].equals("movimentarLote")) {
							ret.add(AtividadesLoteEnum.M.toString());
						}
						else if (tokens[0].equals("minutarLote")) {
							ret.add(AtividadesLoteEnum.E.toString());
						}
						else if (tokens[0].equals("assinarLote")) {
							ret.add(AtividadesLoteEnum.A.toString());
						}
						else if (tokens[0].equals("assinarInteiroTeorLote")) {
							ret.add(AtividadesLoteEnum.T.toString());
						}
						else if (tokens[0].equals("lancadorMovimentoLote")) {
							ret.add(AtividadesLoteEnum.MM.toString());
						}
                        else if (tokens[0].equals("designarAudienciaLote")) {
                            ret.add(AtividadesLoteEnum.DA.toString());
                        }
                        else if (tokens[0].equals("designarPericiaLote")) {
                            ret.add(AtividadesLoteEnum.DP.toString());
                        }
                        else if (tokens[0].equals("renajudLote")) {
                            ret.add(AtividadesLoteEnum.RE.toString());
                        }
					}
				}
				if(ret.size() == 4){
					break;
				}
			}
		}
		ret.add("0");

		JSONArray array = new JSONArray(ret);
		return RestUtil.sucesso(array);

	}
}
