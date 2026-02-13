package br.jus.cnj.pje.webservice;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.integracao.RequisicaoWebServiceIP;
import br.jus.cnj.pje.nucleo.manager.VisibilidadeUsuarioManager;
import br.jus.cnj.pje.webservice.util.RestUtil;

@Path("/visibilidades")
@Name("visibilidadeUsuarioService")
public class VisibilidadeUsuarioService {

	@In
	private VisibilidadeUsuarioManager visibilidadeUsuarioManager;

	@GET
	@Path("/validar-logado")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validarVisibilidadeUsuario(@Context HttpServletRequest contexto) {
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());

		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2())) {
			return RestUtil.erroRequisicao("IP não permitido");
		}

		Boolean possuiVisibilidade = visibilidadeUsuarioManager.isUsuarioLogadoComVisibilidade();
		return Response.ok(possuiVisibilidade).build();
	}

}
