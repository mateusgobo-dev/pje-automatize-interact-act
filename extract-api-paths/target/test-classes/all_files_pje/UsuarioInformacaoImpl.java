package br.jus.cnj.pje.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.json.JSONObject;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.integracao.RequisicaoWebServiceIP;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.cnj.pje.webservice.util.RestUtil;


@Name("usuarioInformacaoRest")
@Path("pje-legacy/usuario")
@Restrict("#{identity.loggedIn}")
public class UsuarioInformacaoImpl{
	@In
	private RestUtil restUtil;
	
	@In
	private UsuarioService usuarioService;
	
	@GET
	@Path("/listarInformacaoUsuario")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response getInformacaoUsuario(@Context HttpServletRequest contexto) {
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2()))
			return RestUtil.erroRequisicao("IP não permitido");
		Response response = null;
		try {
			JSONObject json = new JSONObject(usuarioService.recuperarInformacaoUsuarioLogado());
			response = RestUtil.sucesso(json);
		} catch (Exception e) {
			response = restUtil.reportarErro(e);
		}

		return response;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> recuperarVariaveisPje2(){
		Map<String,Object> variaveis = (Map<String, Object>) Contexts.getSessionContext().get("pje2VariaveisSessao");
		if(variaveis == null){
			variaveis = new HashMap<String,Object>();
		}

		return variaveis;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/currentUser")
	public Response getCurrentUser(){
		InformacaoUsuarioSessao ius = null;
		
		if(Authenticator.getUsuarioLogado() != null){
			ius = usuarioService.recuperarInformacaoUsuarioLogado();
		}
		
		return Response.status(Response.Status.OK).entity(ius).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/isAuthenticated")
	public Response isAuthenticated(){
		boolean ret = false;
		
		if(Authenticator.getUsuarioLogado() != null){
			ret = true;
		}
		
		return Response.status(Response.Status.OK).entity(ret).build();
	}	

}
