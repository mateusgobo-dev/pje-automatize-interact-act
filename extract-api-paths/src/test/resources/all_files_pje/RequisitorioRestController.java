package br.jus.pje.api.controllers.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponseStatus;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Assinatura;
import br.jus.pje.api.converters.AssinaturaConverter;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;



@Name(RequisitorioRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1/requisitorios")
@Restrict("#{identity.loggedIn}")
public class RequisitorioRestController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "requisitorioRestControllerV1";
	private static final String VARIAVEL_ID_PD_REQUISITORIO = "idPDRequisitorio";

	@Logger
	private Log logger;

	@In
	private FluxoManager fluxoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;


	@Create
	public void init() {
		// Autenticar?
		// Aqui deveria haver um mecanismo de autenticação mais leve do que o praticado
		// pelo Authenticator
	}

	@Destroy
	public void destroy() {
		// Logout?
	}

	/**
	 * Mover requisitório para transição de saída do fluxo
	 * @param idRequisitorio
	 * @return 
	 */
	@DELETE
	@Path("/{id-requisitorio}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public PjeResponse<Boolean> moverParaTransicaoSaida(@PathParam("id-requisitorio") Integer idRequisitorio) {
		PjeResponse<Boolean> response = new PjeResponse<>(PjeResponseStatus.OK, "200", null, Boolean.FALSE);

		try {
			Long idTaskInstance = fluxoManager.recuperarTaskInstancePorVariavel("idRequisitorio", idRequisitorio.toString());		
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);			
	        String transicaoSaida = (String) taskInstance.getVariable(Variaveis.NOME_VARIAVEL_END_TRANSITION);
	        if (transicaoSaida != null && !transicaoSaida.isEmpty() && TaskInstanceHome.instance() != null){           
	            taskInstance.end(transicaoSaida);
	        }
			
			response.setResult(Boolean.TRUE);
		} catch (Exception e) {
			List<String> messages = new ArrayList<>();
			logger.error(e.getLocalizedMessage());
			messages.add(e.getMessage());
			response = new PjeResponse<>(PjeResponseStatus.ERROR, "415", messages, null);
		}
		return response;
	}
	
	/**
	 * Recuperar dados da assinatura do ofcio
	 * @param idRequisitorio
	 * @return 
	 */
	@GET
	@Path("/{id-requisitorio}/assinatura")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public PjeResponse<Assinatura> recuperarOficio(@PathParam("id-requisitorio") Integer idRequisitorio) {
		PjeResponse<Assinatura> response = new PjeResponse<>(PjeResponseStatus.OK, "200", null, null);
		AssinaturaConverter converter = new AssinaturaConverter(); 
		Assinatura assinatura = new Assinatura();
		
		try {
			Long idTaskInstance = fluxoManager.recuperarTaskInstancePorVariavel("idRequisitorio", idRequisitorio.toString());		
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);			
	        Integer idProcessoDocumento  = (Integer) taskInstance.getVariable(VARIAVEL_ID_PD_REQUISITORIO);
        	if(idProcessoDocumento != null) {
        		ProcessoDocumento oficio = documentoJudicialService.getDocumento(idProcessoDocumento);
        		assinatura = converter.convertFrom(oficio.getProcessoDocumentoBin());
        	}    
			
			response.setResult(assinatura);
		} catch (Exception e) {
			List<String> messages = new ArrayList<>();
			logger.error(e.getLocalizedMessage());
			messages.add(e.getMessage());
			response = new PjeResponse<>(PjeResponseStatus.ERROR, "415", messages, null);
		}
		return response;
	}	
}
