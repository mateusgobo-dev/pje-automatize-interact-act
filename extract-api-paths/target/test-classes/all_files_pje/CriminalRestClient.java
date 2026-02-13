package br.jus.cnj.pje.webservice.client.criminal;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;

@Name(CriminalRestClient.NAME)
@Scope(ScopeType.EVENT)
public class CriminalRestClient {
	
	public static final String NAME = "criminalRestClient"; 
	
	private Client client;
	
	private WebTarget webTarget;
	
	private final String PATH_ORGAO_PROCEDIMENTO_ORIGINARIO = "orgaosProcedimentoOriginario";
	private final String PATH_TIPO_ORIGEM = "tiposOrigem";
	private final String PATH_TIPO_PROCEDIMENTO_ORIGEM = "tiposProcedimentoOrigem";
	private final String PATH_PESQUISAR = "pesquisar";

	public CriminalRestClient() {	
		this.client = ClientBuilder.newClient();
	}
	
	public String getGatewayPath(){
		return PjeEurekaRegister.instance().getUrlGatewayService(false) + "/criminal/";
	}
	
	public List<OrgaoProcedimentoOriginarioDTO> getOrgaosProcedimentoOriginario() {
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		List<OrgaoProcedimentoOriginarioDTO> lista = invocationBuilder.get(new GenericType<List<OrgaoProcedimentoOriginarioDTO>> () {});
		
		if(lista == null){
			lista = new ArrayList<OrgaoProcedimentoOriginarioDTO>(0);
		}
		
		return lista;
	}
	
	
	public List<OrgaoProcedimentoOriginarioDTO> getOrgaosProcedimentoOriginarioByTipoOrigem(Integer idTipoOrigem) {
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM+"/" + idTipoOrigem + "/" + PATH_ORGAO_PROCEDIMENTO_ORIGINARIO);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		List<OrgaoProcedimentoOriginarioDTO> lista = invocationBuilder.get(new GenericType<List<OrgaoProcedimentoOriginarioDTO>> () {});
		
		if(lista == null){
			lista = new ArrayList<OrgaoProcedimentoOriginarioDTO>(0);
		}
		
		return lista;
	}
	
	public OrgaoProcedimentoOriginarioDTO getOrgaoProcedimentoOriginarioById(Integer id){
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		OrgaoProcedimentoOriginarioDTO response = invocationBuilder.get(OrgaoProcedimentoOriginarioDTO.class);
		
		if(response == null){
			response = new OrgaoProcedimentoOriginarioDTO();
		} 
		
		return response;
	}
	
	public OrgaoProcedimentoOriginarioDTO getOrgaoProcedimentoOriginarioByNomeOrgaoProcedimento(String nomeOrgaoProcedimento){
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO + "/" + nomeOrgaoProcedimento);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		OrgaoProcedimentoOriginarioDTO response = invocationBuilder.get(OrgaoProcedimentoOriginarioDTO.class);
		
		if(response == null){
			response = new OrgaoProcedimentoOriginarioDTO();
		} 	
		
		return response;
	}
	
	public OrgaoProcedimentoOriginarioDTO createOrgaoProcedimentoOriginario(OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginario){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		OrgaoProcedimentoOriginarioDTO response = invocationBuilder.post(Entity.entity(orgaoProcedimentoOriginario, MediaType.APPLICATION_JSON), OrgaoProcedimentoOriginarioDTO.class);
		
		if(response == null){
			response = new OrgaoProcedimentoOriginarioDTO();
		} 		
		
		return response;
	}
	
	public OrgaoProcedimentoOriginarioDTO updateOrgaoProcedimentoOriginario(OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginario){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		OrgaoProcedimentoOriginarioDTO response = invocationBuilder.put(Entity.entity(orgaoProcedimentoOriginario, MediaType.APPLICATION_JSON), OrgaoProcedimentoOriginarioDTO.class);
		
		if(response == null){
			response = new OrgaoProcedimentoOriginarioDTO();
		} 			
		
		return response;
	}
	
	public EntityPageDTO<OrgaoProcedimentoOriginarioDTO> searchOrgaoProcedimentoOriginario(Integer page, Integer size){
		return this.searchOrgaoProcedimentoOriginario(page, size, null);
	}
	
	@SuppressWarnings("unchecked")
	public EntityPageDTO<OrgaoProcedimentoOriginarioDTO> searchOrgaoProcedimentoOriginario(Integer page, Integer size, OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginario){
		EntityPageDTO<OrgaoProcedimentoOriginarioDTO> ret = null;
		
		if(page != null && size != null){
			
			this.webTarget = this.client.target(getGatewayPath())
										.path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO)
										.queryParam("page", page)
										.queryParam("size", size);
			Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
			EntityPageDTO<OrgaoProcedimentoOriginarioDTO> response = invocationBuilder
					.post(Entity.entity(orgaoProcedimentoOriginario, MediaType.APPLICATION_JSON), EntityPageDTO.class);
			
			if(response != null){
				ret = response;
			}
		}
		
		return ret;
	}
	
	public Boolean deleteOrgaoProcedimentoOriginario(Integer id){
		boolean ret = false;
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_ORGAO_PROCEDIMENTO_ORIGINARIO + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		Boolean response = invocationBuilder.delete(Boolean.class);
		
		if(response != null){
			ret = response;
		}
		
		return ret;
	}
	
	public List<TipoOrigemDTO> getTiposOrigem(){
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		List<TipoOrigemDTO> lista = invocationBuilder.get(new GenericType<List<TipoOrigemDTO>> () {});
		
		if(lista == null){
			lista = new ArrayList<TipoOrigemDTO>(0);
		}
		
		return lista;		
	}
	
	public TipoOrigemDTO getTipoOrigemById(Integer id){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		TipoOrigemDTO response = invocationBuilder.get(TipoOrigemDTO.class);
		
		return response;		
	}
	
	public EntityPageDTO<TipoOrigemDTO> searchTipoOrigem(Integer page, Integer size){
		return this.searchTipoOrigem(page, size, null);
	}
	
	@SuppressWarnings("unchecked")
	public EntityPageDTO<TipoOrigemDTO> searchTipoOrigem(Integer page, Integer size, TipoOrigemDTO tipoOrigem){
		EntityPageDTO<TipoOrigemDTO> ret = new EntityPageDTO<TipoOrigemDTO>();
		

		if(page != null && size != null){
			this.webTarget = this.client.target(getGatewayPath())
										.path(PATH_TIPO_ORIGEM + "/" + PATH_PESQUISAR)
										.queryParam("page", page)
										.queryParam("size", size);
			Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
			EntityPageDTO<TipoOrigemDTO>response = null;
			if(tipoOrigem != null){
				response = invocationBuilder.post(Entity.entity(tipoOrigem, MediaType.APPLICATION_JSON), EntityPageDTO.class);
			} else {
				response = invocationBuilder.post(Entity.entity(Entity.json(null), MediaType.APPLICATION_JSON), EntityPageDTO.class);
			}
			if(response != null){
				ret = (EntityPageDTO<TipoOrigemDTO>)response;
			}
		}
		
		return ret;
	}
	
	public TipoOrigemDTO createTipoOrigem(TipoOrigemDTO tipoOrigem){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		TipoOrigemDTO response = invocationBuilder.post(Entity.entity(tipoOrigem, MediaType.APPLICATION_JSON), TipoOrigemDTO.class);
		
		return response;
	}
	
	public TipoOrigemDTO updateTipoOrigem(TipoOrigemDTO tipoOrigem){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		TipoOrigemDTO response = invocationBuilder.put(Entity.entity(tipoOrigem, MediaType.APPLICATION_JSON), TipoOrigemDTO.class);
		
		return response;
	}	
	
	
	public Boolean deleteTipoOrigem(Integer id){
		boolean ret = false;
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		Boolean response = invocationBuilder.delete(Boolean.class);
		
		if(response != null){
			ret = response;
		}
		
		return ret;
	}	

	public List<TipoProcedimentoOrigemDTO> getTiposProcedimentoOrigem(){
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_PROCEDIMENTO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		List<TipoProcedimentoOrigemDTO> response = invocationBuilder.get(new GenericType<List<TipoProcedimentoOrigemDTO>> () {});
		
		return response;		
	}
	
	public TipoProcedimentoOrigemDTO getTipoProcedimentoOrigemById(Integer id){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_PROCEDIMENTO_ORIGEM + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		TipoProcedimentoOrigemDTO response = invocationBuilder.get(TipoProcedimentoOrigemDTO.class);
		
		return response;		
	}
	

	public List<TipoProcedimentoOrigemDTO> getTiposProcedimentoOrigemByTipoOrigem(Integer idTipoOrigem) {
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_ORIGEM + "/" + idTipoOrigem + "/"+PATH_TIPO_PROCEDIMENTO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request("application/json;charset=UTF-8");
		
		List<TipoProcedimentoOrigemDTO> response = invocationBuilder.get(new GenericType<List<TipoProcedimentoOrigemDTO>> () {});
		
		return response;	
	}		
	
	
	
	public EntityPageDTO<TipoProcedimentoOrigemDTO> searchTipoProcedimentoOrigem(Integer page, Integer size){
		return this.searchTipoProcedimentoOrigem(page, size, null);
	}
	
	@SuppressWarnings("unchecked")
	public EntityPageDTO<TipoProcedimentoOrigemDTO> searchTipoProcedimentoOrigem(Integer page, Integer size, TipoProcedimentoOrigemDTO tipoProcedimentoOrigem){
		EntityPageDTO<TipoProcedimentoOrigemDTO> ret = null;
		
		if(page != null && size != null){
			this.webTarget = this.client.target(getGatewayPath())
										.path(PATH_TIPO_PROCEDIMENTO_ORIGEM)
										.queryParam("page", page)
										.queryParam("size", size);
			Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
			EntityPageDTO<TipoProcedimentoOrigemDTO> response = invocationBuilder
					.post(Entity.entity(tipoProcedimentoOrigem, MediaType.APPLICATION_JSON), EntityPageDTO.class);
			if(response != null){
				ret = response;
			}
		}
		
		return ret;
	}
	
	public TipoProcedimentoOrigemDTO createTipoProcedimentoOrigem(TipoProcedimentoOrigemDTO tipoProcedimentoOrigem){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_PROCEDIMENTO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		TipoProcedimentoOrigemDTO response = invocationBuilder.post(
				Entity.entity(tipoProcedimentoOrigem, MediaType.APPLICATION_JSON), TipoProcedimentoOrigemDTO.class);
		
		return response;
	}
	
	public TipoProcedimentoOrigemDTO updateTipoProcedimentoOrigem(TipoProcedimentoOrigemDTO tipoProcedimentoOrigem){
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_PROCEDIMENTO_ORIGEM);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		TipoProcedimentoOrigemDTO response = invocationBuilder.put(
				Entity.entity(tipoProcedimentoOrigem, MediaType.APPLICATION_JSON), TipoProcedimentoOrigemDTO.class);
		
		return response;
	}	
	
	
	public Boolean deleteTipoProcedimentoOrigem(Integer id){
		boolean ret = false;
		
		this.webTarget = this.client.target(getGatewayPath()).path(PATH_TIPO_PROCEDIMENTO_ORIGEM + "/" + id);
		
		Invocation.Builder invocationBuilder = this.webTarget.request(MediaType.APPLICATION_JSON);
		
		Boolean response = invocationBuilder.delete(Boolean.class);
		
		if(response!= null){
			ret = response;
		}
		
		return ret;
	}

}
