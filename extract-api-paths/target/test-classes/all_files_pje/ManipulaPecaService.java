package br.jus.cnj.pje.webservice.client.bnmp;

import javax.ws.rs.ClientErrorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

import com.google.gson.JsonObject;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaListDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaResponseDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.filter.PessoaFilter;
import br.jus.cnj.pje.util.JsonHelper;
import br.jus.cnj.pje.webservice.client.bnmp.dto.AuthenticationResponseDTO;
import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;
import br.jus.pje.nucleo.dto.EntityPageDTO;

@Name(ManipulaPecaService.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ManipulaPecaService extends BaseService {

	public static final String NAME = "manipulaPecaService";

	@In(create = false, required = true)
	private FacesMessages facesMessages;

	public JsonHelper conteudoPecas;
	
	@In(create=true)
	private PessoaRestClient pessoaRestClient;
	
	@In(create=true)
	private AutenticaRestClient autenticaRestClient;
	
	@In(create=true)
	private FactoryPecaBnmpRestClient factoryPecaBnmpRestClient;
	

	public EntityPageDTO<PessoaListDTO> pesquisarPessoas(int page, int size, PessoaFilter pessoaFilter) {
		return getPessoaRestClient().searchDuplicidades(page, size, pessoaFilter);		
	}

	public PessoaResponseDTO salvarPessoa(PessoaDTO pessoa) throws ClientErrorException, PJeException {
		PessoaResponseDTO pessoaDTOResponse = pessoaRestClient.createResource(pessoa);
//		try {
//			MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
//			parameters.add("pessoa", pessoa);
//			pessoaDTOResponse = requestUtil.doPostForObject(urlIntegracaoUtil.getBnmpCriarPessoaURL(), PessoaDTO.class,
//					parameters, getAuthenticationHeaders(false, MediaType.MULTIPART_FORM_DATA));
//		} catch (Exception e) {
//			try {
//				MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
//				parameters.add("pessoa", pessoa);
//				pessoaDTOResponse = requestUtil.doPostForObject(urlIntegracaoUtil.getBnmpCriarPessoaURL(),
//						PessoaDTO.class, parameters, getAuthenticationHeaders(true, MediaType.MULTIPART_FORM_DATA));
//			} catch (CustomHttpServerErrorException ee) {
//				facesMessages.add(Severity.ERROR, "Erro ao tentar salvar a pessoa no BNMPII.");
//				ee.printStackTrace();
//			}
//		}
		return pessoaDTOResponse;
	}

	public void populaConteudoPeca(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP, Number id) throws ClientErrorException, PJeException {
				String result = getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).getJSONResourceById(id);
				JsonObject jsonResult = JsonHelper.converterParaJsonObject(result);
				setConteudoPecas(new JsonHelper(jsonResult));
	}

	
	public String recuperaURLComTokenJWT() throws ClientErrorException, PJeException  {
		if(Authenticator.getUsuarioBNMPLogado() !=null && ((AuthenticationResponseDTO)Authenticator.getUsuarioBNMPLogado()).getJWT() !=null) {
			return ConfiguracaoIntegracaoCloud.getBnmpWebUrl()+"/#/login-success/ticket/"+((AuthenticationResponseDTO)Authenticator.getUsuarioBNMPLogado()).getJWT();
		}
		return null;
	}
	
	public String getJWT(String username, String password, String codigoOrgao) throws ClientErrorException, PJeException {
			return autenticaRestClient.login(username, password, codigoOrgao).getJWT(); 
	}

	public JsonHelper getConteudoPecas() {
		return conteudoPecas;
	}

	public void setConteudoPecas(JsonHelper conteudoPecas) {
		this.conteudoPecas = conteudoPecas;
	}

	public EntityPageDTO<PecaDTO> getPecasPor(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP, PecaDTO filtro) {
		
		EntityPageDTO<PecaDTO> resultado = getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).searchResources(0, 10, filtro);
		
		return resultado;
	}

	public EntityPageDTO<PessoaListDTO> buscaPorDuplicidade(int listaPagePadrao, int listaSizePadrao,
			PessoaFilter filtro) {
		return getPessoaRestClient().searchDuplicidades(0, 10, filtro);
	}

	public PessoaRestClient getPessoaRestClient() {
		return pessoaRestClient;
	}

	public void setPessoaRestClient(PessoaRestClient pessoaRestClient) {
		this.pessoaRestClient = pessoaRestClient;
	}

	public FactoryPecaBnmpRestClient getFactoryPecaBnmpRestClient() {
		return factoryPecaBnmpRestClient;
	}

	public void setFactoryPecaBnmpRestClient(FactoryPecaBnmpRestClient factoryPecaBnmpRestClient) {
		this.factoryPecaBnmpRestClient = factoryPecaBnmpRestClient;
	}

	public void assinarMagistrado(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP, String idPeca) throws ClientErrorException, PJeException {
		getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).assinaMagistrado(idPeca);
	}
	
	public void assinarServidor(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP, String idPeca) throws ClientErrorException, PJeException {
		getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).assinaServidor(idPeca);
	}
	
	public String recuperaURLWebPor(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP) {
		return ConfiguracaoIntegracaoCloud.getBnmpWebUrl()+getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).getWebPath();
	}
	
	public String recuperaURLWebSearchPathPor(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP) {
		return ConfiguracaoIntegracaoCloud.getBnmpWebUrl()+getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).getWebSearchPath();
	}
	
	public void autentica(String password) throws ClientErrorException, PJeException {
		AuthenticationResponseDTO authenticationResponseDTO = autenticaRestClient.login(password);
		authenticationResponseDTO.setPassword(password);
		Authenticator.setUsuarioBNMPLogado(authenticationResponseDTO);
	}
	
	public EntityPageDTO<PecaDTO> getPecasPorPessoaRJIAndTipoPecaAndStatusPeca(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP, PecaDTO filtro) {
		EntityPageDTO<PecaDTO> resultado = factoryPecaBnmpRestClient.getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).pesquisaByPessoaRJIByTipoPecaAndByStatusPeca(0, 10, filtro);
		return resultado;
	}
	
	public String getAcaoCadastro(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP) {
		
		return getFactoryPecaBnmpRestClient().getPecaBnmpRestClientPor(tipoProcessoDocumentoBNMP).getAcaoCadastro();
	}
}
