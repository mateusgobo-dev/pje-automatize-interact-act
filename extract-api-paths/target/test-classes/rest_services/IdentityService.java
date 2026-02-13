package br.jus.cnj.pje.webservice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.IdentityStore;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.integracao.RequisicaoWebServiceIP;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.webservice.client.AuthenticationRestFilter;
import br.jus.cnj.pje.webservice.criminal.dto.PjeUser;
import br.jus.cnj.pje.webservice.util.RestUtil;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

/**
 * REST para obter informações relacionadas ao Identity.
 */
@Path("pje-legacy/identity")
@Name("identityService")
public class IdentityService {

    private IdentityStore identityStore = IdentityManager.instance().getIdentityStore();

    @In (scope = ScopeType.APPLICATION, required = false)
	private Map<String, UsuarioLocalizacao> mapa = new HashMap<>();
    
    @GET
    @Path("/roles/{login}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validarVisibilidadeUsuario(@PathParam("login") String login, @Context HttpServletRequest request) {
        RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(request.getRemoteAddr());

        if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2())) {
            return RestUtil.erroRequisicao("IP não permitido");
        }

        Set<String> roles = new HashSet<String>(identityStore.getImpliedRoles(login));

        return Response.ok(roles).build();
    }
    
    @GET
    @Path("/auth")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticarPjeLegacy(@Context HttpServletRequest request) {
    	Response res = Response.status(Status.UNAUTHORIZED).build();
    	
    	if(Authenticator.getUsuarioLogado() != null) {
    		Usuario usuario = Authenticator.getUsuarioLogado();
    		PjeUser user = new PjeUser(usuario.getLogin(), usuario.getSenha(), usuario.getBloqueio() && usuario.getAtivo(), usuario.getNome(), usuario.getEmail());
    		res = Response.ok(user).build();
    	} else {
			try {
				String preAuthToken = request.getHeader(AuthenticationRestFilter.PRE_AUTH_TOKEN_HEADER);
				PjeUser user = Authenticator.obterUsuarioToken(preAuthToken);
				Identity.instance().logout();
				res = Response.ok(user).build();
			} catch (Exception e1) {
				res = Response.status(Status.INTERNAL_SERVER_ERROR).build();
				e1.printStackTrace();
			}
    	}
    	return res;
    }
    
    @GET
    @Path("/auth/{login}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticarLogin(@PathParam("login") String login, @Context HttpServletRequest request) {
    	Response res = Response.status(Status.UNAUTHORIZED).build();    	
    	PjeUser user = new PjeUser();

    	try {
	    	UsuarioManager usuarioManager = ComponentUtil.getComponent(UsuarioManager.class);
	    	Usuario usuario = usuarioManager.findByLogin(login.toUpperCase());
	    	if(usuario != null){	    			    		
	    		user = new PjeUser(usuario.getLogin(), null, !usuario.getBloqueio() && usuario.getAtivo(), usuario.getNome(), usuario.getEmail());
	    		res = Response.ok(user).build();
	    	}else {
	    		res = Response.status(Status.NOT_FOUND).build();
	    	}
    	}catch (Exception e1) {
			res = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	
    	return res;
    }

}
