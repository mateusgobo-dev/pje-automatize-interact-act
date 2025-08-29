package br.jus.cnj.pje.webservice.criminal.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.InformacaoCriminalRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.EventoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.FugaBean;
import br.jus.pje.nucleo.beans.criminal.IncidenciaPenalBean;
import br.jus.pje.nucleo.beans.criminal.PrisaoBean;
import br.jus.pje.nucleo.beans.criminal.SolturaBean;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminal;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminalEnum;
import br.jus.pje.nucleo.dto.criminal.TipoEventoCriminalDTO;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.ProcessoParteMin;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(InformacaoCriminalRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/informacoes-criminais")
@Restrict("#{identity.loggedIn}")
public class InformacaoCriminalRestController implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "informacoesCriminaisRestController";
	
	@Logger
	private Log logger;	
	
	@In
	private ProcessoRascunhoManager processoRascunhoManager;
	
	@In
	private InformacaoCriminalRascunhoManager informacaoCriminalRascunhoManager;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@Create
	public void init(){
		logger.info(InformacaoCriminalRestController.NAME + " inicializado!");
	}
	
	
	@GET
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarInformacoesCriminaisProcesso(@PathParam("idProcesso") Integer idProcessoJudicial, @PathParam("idProcessoParte") Integer idProcessoParte){
		Response res = Response.noContent().build();
		
		try {
			InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
			
			if(icRascunho != null){
				ProcessoParteMin pp = new ProcessoParteMin();
				pp.setId(icRascunho.getProcessoParte().getId());
				pp.setIdPessoa(icRascunho.getProcessoParte().getIdPessoa());
				pp.setIdProcessoTrf(icRascunho.getProcessoParte().getIdProcessoTrf());
				pp.setSituacao(icRascunho.getProcessoParte().getSituacao());
				
				res = Response.ok(new InformacaoCriminalRascunho(icRascunho.getId(), 
						null,
						pp, 
						icRascunho.getInformacaoCriminal())).build();
			}
			
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			res = Response.serverError().build();
		}
		
		return res;	
	}
	
	@GET
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/incidencias-penais")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarIncidenciasPenaisParte(@PathParam("idProcesso") Integer idProcessoJudicial, @PathParam("idProcessoParte") Integer idProcessoParte){
		Response res = Response.noContent().build();
		
		try {
			InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
			if(icRascunho == null){
				icRascunho = new InformacaoCriminalRascunho();
			} else { 
				icRascunho.setProcessoRascunho(null);
			}
			
			if(icRascunho.getInformacaoCriminal() != null && CollectionUtilsPje.isNotEmpty(icRascunho.getInformacaoCriminal().getIndiciamento())){
				res = Response.ok(icRascunho.getInformacaoCriminal().getIndiciamento().get(0).getTipificacoes()).build();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			res = Response.serverError().build();
		}
		
		return res;	
	}		

	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/incidencias-penais")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response criarIncidenciaPenal(@PathParam("idProcesso") Integer idProcessoJudicial, 
										 @PathParam("idProcessoParte") Integer idProcessoParte, 
										 List<IncidenciaPenalBean> listaIncidencias){
		Response res = Response.noContent().build();
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		TipoEventoCriminal tipoEventoCriminal = this.recuperarTipoEventoCriminalIncial(idProcessoJudicial);

		try {
			ProcessoParteMin parte = this.processoParteManager.recuperarProcessoParteMinPorId(new Long(idProcessoParte));
			EventoCriminalBean eventoCriminalInicial = new EventoCriminalBean(new Date(), tipoEventoCriminal, listaIncidencias, true);
			List<EventoCriminalBean> indiciamento = new ArrayList<EventoCriminalBean>(0);
			indiciamento.add(eventoCriminalInicial);

			if(icRascunho != null){
				if(icRascunho.getInformacaoCriminal() != null){
					icRascunho.getInformacaoCriminal().setIndiciamento(indiciamento);
					icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
					icRascunho.setProcessoRascunho(null);
				}					
			} else if (icRascunho == null){
				ProcessoRascunho procRascunho = this.recuperarOuCriarProcessoRascunho(idProcessoJudicial);
				if(procRascunho != null){
					ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean();
					
					conteudo.setIndiciamento(indiciamento);
					
					icRascunho = new InformacaoCriminalRascunho(null, procRascunho, parte, conteudo);
					icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
					
					this.informacaoCriminalRascunhoManager.flush();
					icRascunho.setProcessoRascunho(null);
				}
			}
			res = Response.ok(new InformacaoCriminalRascunho(icRascunho.getId(), 
					null,
					this.getProcessoParteNaoGerenciado(icRascunho.getProcessoParte()), 
					icRascunho.getInformacaoCriminal())).build();
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return res;
	}
	
	@GET
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/prisoes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPrisoesParte(@PathParam("idProcesso") Integer idProcessoJudicial, @PathParam("idProcessoParte") Integer idProcessoParte){
		Response res = Response.noContent().build();
		
		try {
			InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
			if(icRascunho == null){
				icRascunho = new InformacaoCriminalRascunho();
			} else { 
				icRascunho.setProcessoRascunho(null);
			}
			
			res = Response.ok(icRascunho.getInformacaoCriminal().getPrisoes()).build();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			res = Response.serverError().build();
		}
		
		return res;	
	}		
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/prisoes")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response criarPrisao(@PathParam("idProcesso") Integer idProcessoJudicial, 
										 @PathParam("idProcessoParte") Integer idProcessoParte, 
										 PrisaoBean prisao){
		Response res = Response.noContent().build();
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			ProcessoParteMin parte = this.processoParteManager.recuperarProcessoParteMinPorId(new Long(idProcessoParte));
			if(prisao != null){
				if(icRascunho != null){
					if(icRascunho.getInformacaoCriminal().getPrisoes() == null){
						List<PrisaoBean> listaPrisoes = new ArrayList<PrisaoBean>();
						listaPrisoes.add(prisao);
						icRascunho.getInformacaoCriminal().setPrisoes(listaPrisoes);;
					} else {
						icRascunho.getInformacaoCriminal().getPrisoes().add(prisao);
					}
					icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
					icRascunho.setProcessoRascunho(null);
				} else {
					ProcessoRascunho procRascunho = this.recuperarOuCriarProcessoRascunho(idProcessoJudicial);
					if(procRascunho != null){
						ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean();
						
						List<PrisaoBean> listaPrisoes = new ArrayList<PrisaoBean>();
						listaPrisoes.add(prisao);
						
						conteudo.setPrisoes(listaPrisoes);
						
						icRascunho = new InformacaoCriminalRascunho(null, procRascunho, parte, conteudo);
						icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
						
						this.informacaoCriminalRascunhoManager.flush();
						icRascunho.setProcessoRascunho(null);
					}
				}
			}
			res = Response.ok(new InformacaoCriminalRascunho(icRascunho.getId(), 
					null,
					this.getProcessoParteNaoGerenciado(icRascunho.getProcessoParte()), 
					icRascunho.getInformacaoCriminal())).build();
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return res;
	}	
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/incidencias-penais/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removerIncidenciaPenal(@PathParam("idProcesso") Integer idProcessoJudicial, 
			 						   @PathParam("idProcessoParte") Integer idProcessoParte, 
			 						   IncidenciaPenalBean incidencia){
		Response res = Response.noContent().build();
		
		Boolean deleted = Boolean.FALSE;
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			if(icRascunho != null){
				if(icRascunho.getInformacaoCriminal() != null &&
						!CollectionUtilsPje.isEmpty(icRascunho.getInformacaoCriminal().getIndiciamento()) &&
						!CollectionUtilsPje.isEmpty(icRascunho.getInformacaoCriminal().getIndiciamento().get(0).getTipificacoes())){
					List<IncidenciaPenalBean> listaIncidencias = icRascunho.getInformacaoCriminal().getIndiciamento().get(0).getTipificacoes();
					deleted = listaIncidencias.remove(incidencia);
					icRascunho.getInformacaoCriminal().getIndiciamento().get(0).setTipificacoes(listaIncidencias);
					
					this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		if(deleted){
			res = Response.ok(Boolean.TRUE).build();
		}
		
		return res;
		
	}
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/prisoes/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removerPrisaoParte(@PathParam("idProcesso") Integer idProcessoJudicial, 
			 						   @PathParam("idProcessoParte") Integer idProcessoParte, 
			 						   PrisaoBean prisao){
		Response res = Response.noContent().build();
		
		Boolean deleted = Boolean.FALSE;
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			if(icRascunho != null){
				if(icRascunho.getInformacaoCriminal() != null && !CollectionUtilsPje.isEmpty(icRascunho.getInformacaoCriminal().getPrisoes())){
					List<PrisaoBean> listaPrisoes = icRascunho.getInformacaoCriminal().getPrisoes();
					deleted = listaPrisoes.remove(prisao);
					icRascunho.getInformacaoCriminal().setPrisoes(listaPrisoes);
					
					this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		if(deleted){
			res = Response.ok(Boolean.TRUE).build();
		}
		
		return res;
		
	}
	
	@GET
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/solturas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarSolturasParte(@PathParam("idProcesso") Integer idProcessoJudicial, @PathParam("idProcessoParte") Integer idProcessoParte){
		Response res = Response.noContent().build();
		
		try {
			InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
			if(icRascunho == null){
				icRascunho = new InformacaoCriminalRascunho();
			} else { 
				icRascunho.setProcessoRascunho(null);
			}
			
			res = Response.ok(icRascunho.getInformacaoCriminal().getSolturas()).build();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			res = Response.serverError().build();
		}
		
		return res;	
	}		
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/solturas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response criarSoltura(@PathParam("idProcesso") Integer idProcessoJudicial, 
										 @PathParam("idProcessoParte") Integer idProcessoParte, 
										 SolturaBean soltura){
		Response res = Response.noContent().build();
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			ProcessoParteMin parte = this.processoParteManager.recuperarProcessoParteMinPorId(new Long(idProcessoParte));
			if(soltura != null){
				if(icRascunho != null){
					if(icRascunho.getInformacaoCriminal().getSolturas() == null){
						List<SolturaBean> listaSolturas = new ArrayList<SolturaBean>();
						listaSolturas.add(soltura);
						icRascunho.getInformacaoCriminal().setSolturas(listaSolturas);;
					} else {
						icRascunho.getInformacaoCriminal().getSolturas().add(soltura);
					}
					icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
					icRascunho.setProcessoRascunho(null);
				} else {
					ProcessoRascunho procRascunho = this.recuperarOuCriarProcessoRascunho(idProcessoJudicial);
					if(procRascunho != null){
						ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean();
						
						List<SolturaBean> listaSolturas = new ArrayList<SolturaBean>();
						listaSolturas.add(soltura);
						
						conteudo.setSolturas(listaSolturas);
						
						icRascunho = new InformacaoCriminalRascunho(null, procRascunho, parte, conteudo);
						icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
						
						this.informacaoCriminalRascunhoManager.flush();
						icRascunho.setProcessoRascunho(null);
					}
				}
			}
			res = Response.ok(new InformacaoCriminalRascunho(icRascunho.getId(), 
					null,
					this.getProcessoParteNaoGerenciado(icRascunho.getProcessoParte()), 
					icRascunho.getInformacaoCriminal())).build();
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return res;
	}
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/solturas/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removerSolturaParte(@PathParam("idProcesso") Integer idProcessoJudicial, 
			 						   @PathParam("idProcessoParte") Integer idProcessoParte, 
			 						   SolturaBean soltura){
		Response res = Response.noContent().build();
		
		Boolean deleted = Boolean.FALSE;
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			if(icRascunho != null){
				if(icRascunho.getInformacaoCriminal() != null && !CollectionUtilsPje.isEmpty(icRascunho.getInformacaoCriminal().getSolturas())){
					List<SolturaBean> listaSolturas = icRascunho.getInformacaoCriminal().getSolturas();
					deleted = listaSolturas.remove(soltura);
					icRascunho.getInformacaoCriminal().setSolturas(listaSolturas);
					
					this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		if(deleted){
			res = Response.ok(Boolean.TRUE).build();
		}
		
		return res;
		
	}	
	
	@GET
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/fugas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarFugasParte(@PathParam("idProcesso") Integer idProcessoJudicial, @PathParam("idProcessoParte") Integer idProcessoParte){
		Response res = Response.noContent().build();
		
		try {
			InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
			if(icRascunho == null){
				icRascunho = new InformacaoCriminalRascunho();
			} else { 
				icRascunho.setProcessoRascunho(null);
			}
			
			res = Response.ok(icRascunho.getInformacaoCriminal().getFugas()).build();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			res = Response.serverError().build();
		}
		
		return res;	
	}		
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/fugas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response criarFuga(@PathParam("idProcesso") Integer idProcessoJudicial, 
										 @PathParam("idProcessoParte") Integer idProcessoParte, 
										 FugaBean fuga){
		Response res = Response.noContent().build();
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			ProcessoParteMin parte = this.processoParteManager.recuperarProcessoParteMinPorId(new Long(idProcessoParte));
			if(fuga != null){
				if(icRascunho != null){
					if(icRascunho.getInformacaoCriminal().getFugas() == null){
						List<FugaBean> listaFugas = new ArrayList<FugaBean>();
						listaFugas.add(fuga);
						icRascunho.getInformacaoCriminal().setFugas(listaFugas);;
					} else {
						icRascunho.getInformacaoCriminal().getFugas().add(fuga);
					}
					icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
					icRascunho.setProcessoRascunho(null);
				} else {
					ProcessoRascunho procRascunho = this.recuperarOuCriarProcessoRascunho(idProcessoJudicial);
					if(procRascunho != null){
						ConteudoInformacaoCriminalBean conteudo = new ConteudoInformacaoCriminalBean();
						
						List<FugaBean> listaFugas = new ArrayList<FugaBean>();
						listaFugas.add(fuga);
						
						conteudo.setFugas(listaFugas);
						
						icRascunho = new InformacaoCriminalRascunho(null, procRascunho, parte, conteudo);
						icRascunho = this.informacaoCriminalRascunhoManager.persist(icRascunho);
						
						this.informacaoCriminalRascunhoManager.flush();
						icRascunho.setProcessoRascunho(null);
					}
				}
			}
			res = Response.ok(new InformacaoCriminalRascunho(icRascunho.getId(), 
					null,
					this.getProcessoParteNaoGerenciado(icRascunho.getProcessoParte()), 
					icRascunho.getInformacaoCriminal())).build();
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return res;
	}
	
	@POST
	@Path("/rascunhos/processo/{idProcesso}/{idProcessoParte}/fugas/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removerFugaParte(@PathParam("idProcesso") Integer idProcessoJudicial, 
			 						   @PathParam("idProcessoParte") Integer idProcessoParte, 
			 						   FugaBean fuga){
		Response res = Response.noContent().build();
		
		Boolean deleted = Boolean.FALSE;
		
		InformacaoCriminalRascunho icRascunho = informacaoCriminalRascunhoManager.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, new Long(idProcessoParte));
		
		try {
			if(icRascunho != null){
				if(icRascunho.getInformacaoCriminal() != null && !CollectionUtilsPje.isEmpty(icRascunho.getInformacaoCriminal().getFugas())){
					List<FugaBean> listaFugas = icRascunho.getInformacaoCriminal().getFugas();
					deleted = listaFugas.remove(fuga);
					icRascunho.getInformacaoCriminal().setFugas(listaFugas);
					
					this.informacaoCriminalRascunhoManager.persist(icRascunho);
					this.informacaoCriminalRascunhoManager.flush();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		if(deleted){
			res = Response.ok(Boolean.TRUE).build();
		}
		
		return res;
		
	}	
	
	private ProcessoRascunho recuperarOuCriarProcessoRascunho(Integer idProcessoJudicial){
		
		ProcessoRascunho procRascunho = this.processoRascunhoManager.recuperarRascunhoPorIdProcessoTrf(idProcessoJudicial);
		if(procRascunho == null){
			ProcessoTrf proc = new ProcessoTrf();
			proc.setIdProcessoTrf(idProcessoJudicial);
			
			procRascunho = new ProcessoRascunho();
			procRascunho.setProcesso(proc);
			
			try {
				procRascunho = this.processoRascunhoManager.persist(procRascunho);
			} catch (PJeBusinessException e) {
				logger.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		return procRascunho;
	}
	
	private ProcessoParteMin getProcessoParteNaoGerenciado(ProcessoParteMin processoParte){
		ProcessoParteMin pp = new ProcessoParteMin();
		pp.setId(processoParte.getId());
		pp.setIdPessoa(processoParte.getIdPessoa());
		pp.setIdProcessoTrf(processoParte.getIdProcessoTrf());
		pp.setSituacao(processoParte.getSituacao());
		
		return pp;
	}
	
	private TipoEventoCriminal recuperarTipoEventoCriminalIncial(Integer idProcessoJudicial) {
		TipoEventoCriminal tipoEventoCriminal = null;
		
		try {
			ProcessoTrf procTrf = this.processoJudicialManager.findById(idProcessoJudicial);
			tipoEventoCriminal = new TipoEventoCriminal(TipoEventoCriminalEnum.valueOf(procTrf.getClasseJudicial().getTipoEventoCriminalInicial()));
		} catch (PJeBusinessException e) {
			this.logger.error("Erro ao tentar recuperar o processo judicial", e);
		}
		
		return tipoEventoCriminal;
	}
	
}
