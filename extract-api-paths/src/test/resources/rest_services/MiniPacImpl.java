package br.jus.cnj.pje.webservice;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.BusinessProcess;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.integracao.RequisicaoWebServiceIP;
import br.jus.cnj.pje.nucleo.service.MiniPacService;
import br.jus.cnj.pje.webservice.api.IMiniPacService;
import br.jus.cnj.pje.webservice.util.RestUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("miniPacRest")
@Path("miniPac")
public class MiniPacImpl implements IMiniPacService {

	@In
	private MiniPacService miniPacService;
	
	@GET
	@Path("/processar/{idTarefa}/{idProcessoTrf}/{idProcessoDocumento}")
	@Consumes({ MediaType.TEXT_PLAIN })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	@Transactional
	public Response processar(@PathParam("idTarefa") Long idTarefa, @PathParam("idProcessoTrf") Integer idProcessoTrf, 
			@PathParam("idProcessoDocumento") Integer idProcessoDocumento, @Context HttpServletRequest contexto) {
		
		RequisicaoWebServiceIP ipRequisicao = new RequisicaoWebServiceIP(contexto.getRemoteAddr());
		if (!ipRequisicao.validar(ParametroUtil.instance().getIPsPermitidosPje2())) {
			return RestUtil.erroRequisicao("IP não permitido");
		}
		if (idTarefa == null) {
			return RestUtil.erroRequisicao("Tarefa não selecionada");
		}
		
		BusinessProcess.instance().resumeTask(idTarefa);
		this.miniPacService.processarMiniPac(EntityUtil.find(ProcessoTrf.class, idProcessoTrf), 
				EntityUtil.find(ProcessoDocumento.class, idProcessoDocumento), false);
		
		EntityUtil.getEntityManager().flush();
		
		return RestUtil.sucesso();
	}
	
}
