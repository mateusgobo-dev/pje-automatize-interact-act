package br.jus.cnj.pje.webservice.client.bnmp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FileUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.OrgaoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.PecaMinDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.StatusDTO;
import br.jus.pdpj.commons.utils.NumeroProcessoUtil;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.StringUtil;	
import io.restassured.RestAssured;

@Name(PecaMinBnmpRestClient.NAME)
public class PecaMinBnmpRestClient extends BnmpRestClient<PecaMinDTO> {

	public static final int EM_ELABORACAO = 1;
	
	public static final int AGUARDANDO_ASSINATURA = 4;

	public static final String NAME = "pecaMinBnmpRestClient"; 
	
	private static final long serialVersionUID = 1L;
	
	private transient Logger log = Logger.getLogger(PecaMinBnmpRestClient.class);
	
	
	public List<PecaMinDTO> obterPecaEmElaboracao(ProcessoTrf processoTrf, Integer idPeca) {
		return obterPeca(processoTrf, EM_ELABORACAO, idPeca);
	}
	
	public List<PecaMinDTO> obterPecaAguardandoAssinatura(ProcessoTrf processoTrf, Integer idPeca) {
		return obterPeca(processoTrf, AGUARDANDO_ASSINATURA, idPeca);
	}
	
	public List<PecaMinDTO> obterPeca(ProcessoTrf processoTrf, int idStatus, Integer idPeca) {
		try {
			if(processoTrf.getOrgaoJulgador().getCodigoCorporativo() == null) {
				return Collections.emptyList();
			}
			List<PecaMinDTO> listaPecas  =	obterPecasPorOrgaoEStatus(processoTrf.getOrgaoJulgador().getCodigoCorporativo(),idStatus);

			if(listaPecas != null) {
				return listaPecas.stream()
					    .filter(p -> String.valueOf(p.getId()).startsWith(String.valueOf(idPeca)))
					    .collect(Collectors.toList());	
			}
		}catch (Exception e) {
			log.error("Erro ao tentar obter a peça com status do processo: "+e.getLocalizedMessage());
		}
		return Collections.emptyList();
	}

	public List<PecaMinDTO> obterPecasPendentesDeAssinatura(ProcessoTrf processoTrf, Integer idLocalizacao) {
		try {
			List<PecaMinDTO> listaPecas = null;
			Integer numeroCnj = obterCodigoCnj(idLocalizacao);				
			if (numeroCnj!=null) {
				listaPecas  = obterPecasPorOrgaoEStatus(numeroCnj, AGUARDANDO_ASSINATURA);
			}
			String nrProcesso = NumeroProcessoUtil.retiraMascaraNumeroProcesso(processoTrf.getNumeroProcesso());
		
			if(!CollectionUtils.isEmpty(listaPecas)) {
				return listaPecas.stream().filter(p -> p.getNumeroPeca().startsWith(nrProcesso)).collect(Collectors.toList());				
			}
		}catch (Exception e) {
			log.error("Erro ao tentar obter as peças pendentes de assinatura do processo: "+e.getLocalizedMessage());
		}
		return Collections.emptyList();
	}

	private Integer obterCodigoCnj(Integer idLocalizacao) throws PJeBusinessException {
		if (idLocalizacao != null) {
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getOrgaoJulgadorManager();
			LocalizacaoManager localizacaoManager = ComponentUtil.getComponent(LocalizacaoManager.class);
			OrgaoJulgador orgaoJulgador = orgaoJulgadorManager.getOrgaoJulgadorByLocalizacaoExata(localizacaoManager.findById(idLocalizacao));
			if (orgaoJulgador !=null ) {
				return orgaoJulgador.getCodigoCorporativo();
			}
		}
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			return Authenticator.getOrgaoJulgadorColegiadoAtual().getNumeroCnj();
		}
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			return Authenticator.getOrgaoJulgadorAtual().getCodigoCorporativo();
		}
		return null;
	}
			
	public List<PecaMinDTO> obterPecasPendentesDeAssinatura(ProcessoTrf processoTrf) {
		return obterPecasPendentesDeAssinatura(processoTrf, null);
	}
	
	public List<PecaMinDTO> obterPecasPendentesDeAssinatura(Integer codigoOj) throws PJeException {
	return obterPecasPorOrgaoEStatus(codigoOj, AGUARDANDO_ASSINATURA);
	}
	
	public EntityPageDTO<PecaMinDTO> obterPecasPendentesDeAssinatura(Integer codigoOj, int page) throws PJeException {
	return obterPecasPorOrgaoEStatus(codigoOj, page, AGUARDANDO_ASSINATURA);
	}
	
	public List<PecaMinDTO> obterPecasPorOrgaoEStatus(Integer codigoOj, int idStatus) throws PJeException {
		List<PecaMinDTO> listaPecas = new ArrayList<>();
		EntityPageDTO<PecaMinDTO>  entityPage = null;

		int i = 0;
		do{
			entityPage = obterPecasPorOrgaoEStatus(codigoOj, i++, idStatus);
			if(entityPage == null) {
				break;
			}
			listaPecas.addAll(entityPage.getContent());
		}while(Boolean.FALSE.equals(entityPage.getLast()));
		
		return listaPecas;
	}
	
	protected EntityPageDTO<PecaMinDTO> obterPecasPorOrgaoEStatus(Integer codigoOj, int page, int idStatus) throws PJeException {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath()).path(getSearchPath());
		
		StatusDTO statusPendenteAssinatura = new StatusDTO();
		statusPendenteAssinatura.setId(idStatus);
		
		OrgaoDTO orgaoExpedidor = new OrgaoDTO();
		orgaoExpedidor.setId(codigoOj.longValue());

		PecaMinDTO pecaDTO = new PecaMinDTO();
		pecaDTO.setStatus(statusPendenteAssinatura);
		pecaDTO.setOrgaoExpeditor(orgaoExpedidor);
		
		EntityPageDTO<PecaMinDTO> response = null;
									
		try { 
			Map<String, Object> parametros = new HashMap<>();

			parametros.put("size", 50);
			parametros.put("sort", "asc");
			parametros.put("page", page);
								
			response = getInvocationDefaults(parametros)
				.post(Entity.entity(pecaDTO, MediaType.APPLICATION_JSON), EntityPageDTO.class);
							
			if(response != null){
				ObjectMapper mapper = new ObjectMapper();
						   mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
									false);
					List<PecaMinDTO> lista = mapper.readValue(mapper.writeValueAsString(response.getContent()), mapper.getTypeFactory().constructCollectionType(ArrayList.class, this.getEntityClass()));
					response.setContent(lista);
			}
		}catch (Exception e) {
			log.error("Erro ao obter peças pendentes de assinatura. "+e.getMessage());
			throw new PJeException(String.format("Erro ao obter peças pendentes de assinatura. %s", e.getMessage()));

		}	
		return response;
	}
	
	
	public File downloadPeca(String urlPeca) throws PJeException {
		String authorizationToken = ConfiguracaoIntegracaoCloud.getBnmpApiToken();
		String urlBaseBnmp = ConfiguracaoIntegracaoCloud.getBnmpApiUrl() +"/"+ getServicePath(); 
		io.restassured.response.Response response = null;
		
		if(StringUtil.isEmpty(authorizationToken)) {
			throw new PJeException(String.format("Token de Autorização do BNMP não cadastrado. Verifique o parâmetro: %s.", ConfiguracaoIntegracaoCloud.ENV_BNMP_API_CREDENTIALS_TOKEN));
		}		
		if(StringUtil.isEmpty(urlBaseBnmp)) {
			throw new PJeException(String.format("URL base do BNMP não configurada. Verifique o parâmetro: %s.", ConfiguracaoIntegracaoCloud.ENV_BNMP_API_URL));
		}
		
		urlBaseBnmp += urlPeca;
		
		try {
	        response = RestAssured.given()
	                .header("Authorization", "Bearer " + authorizationToken)
	                .post(urlBaseBnmp);
	        
	        if (response.getStatusCode() == 200) {
	            byte[] doc = response.body().asByteArray();
	 			return FileUtil.createTempFile(doc);
	        }
	        
			throw new PJeException(String.format("Erro ao baixar o arquivo do BNMP. Código HTTP: %d. URL: %s ",response.getStatusCode(), urlPeca));
		}catch (Exception e) {
			log.error("Erro ao baixar o arquivo do BNMP. Erro: "+e.getMessage());
			throw new PJeException(String.format("Erro ao baixar o arquivo do BNMP. Erro: %s ", e.getMessage()));
		}
	}
	
	@Override
	public String getServicePath() {
		return "bnmpservice/api";
	}

	@Override
	public String getSearchPath() {
		return "pecas/light-filter";
	}
}
