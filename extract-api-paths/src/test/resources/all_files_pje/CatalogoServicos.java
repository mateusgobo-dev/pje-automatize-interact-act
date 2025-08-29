package br.jus.cnj.pje.intercomunicacao.seguranca;

import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.catalogoservicos.dto.EnderecoDTO;
import br.jus.cnj.catalogoservicos.dto.InstalacaoDTO;
import br.jus.cnj.catalogoservicos.dto.TokenDTO;
import br.jus.cnj.catalogoservicos.rest.TokenResource;
import br.jus.cnj.pje.intercomunicacao.v223.seguranca.CatalogoServicosMNIClientLogicalHandler;

/**
 * <pre>
 * Gerente de Autenticação para os webservices que utilizam o Catalogo de Servicos.
 * Devem ser configurados os seguintes parametros PJE:
 * urlCatalogoServicos: endereço do Catálogo de Serviços
 * catalogoServicosKeyStoreLocation: endereço fisico da keystore contendo o certificado cliente
 * catalogoServicosKeyStorePass: senha do keystore
 * catalogoServicosTrustStoreLocation: endereço fisico da truststore contendo o certificado do Catálogo de Serviços. Opcional quando configurado o jssecacerts/cacerts
 * catalogoServicosTrustStorePass: senha do truststore. Opcional quando configurado o jssecacerts/cacerts
 * 
 * 
 * Exemplo de cliente e serviço que utilizam o catálogo: 
 * 
 * Cliente.java
 * ...
 * CatalogoServicos catalogoServicos = new CatalogoServicos(<codigoUnidade>_<codigoInstalacao>);
 * <code>//recupearar os endereços retornados pelo catalogo</code>
 * List<EnderecoDTO> enderecos = catalogoServicos.getEnderecos();
 * ...
 * InterfaceWebService port = service.getPort(Intercomunicacao.class);
 * <code>//anexar as credenciais (usuário e token) a chamada do ws</code>
 * catalogoServicos.anexarCredenciais((BindingProvider)port);
 * ...
 * 
 * Server.java
 * ...
 * <code>@WebService</code>
 * <b>@HandlerChain(file="catalogo-servicos-handler-chain.xml")</b><code>//Configuração para verificação automática das credenciais do serviço.</i>
 * ...
 * public class Server implements InterfaceWebService
 * ...
 * </pre>
 * 
 * @author rodrigo
 * 
 */
public class CatalogoServicos {

	public static final String TOKEN_PARAM_NAME = "token";

	public static final String ENDERECOS_PARAM_NAME = "enderecos";

	private static final String SERVICE_URL_PARAM_NAME = "urlCatalogoServicos";

	private static final String KEY_STORE_PASS_PARAM_NAME = "catalogoServicosKeyStorePass";
	private static final String KEY_STORE_LOCATION_PARAM_NAME = "catalogoServicosKeyStoreLocation";

	private static final String TRUST_STORE_PASS_PARAM_NAME = "catalogoServicosTrustStorePass";
	private static final String TRUST_STORE_LOCATION_PARAM_NAME = "catalogoServicosTrustStoreLocation";

	private String codigo;

	public CatalogoServicos(String codigo) {
		if(codigo == null || codigo.isEmpty())
			throw new IllegalArgumentException("Código do Serviço Inválido");
		this.codigo = codigo;
	}


	/**
	 * Verifica se o token foi gerado por este usuario
	 * @param usuario
	 * @return
	 */
	public boolean autenticar(String usuario) {
		TokenDTO token = requisitarToken();
		
		return token !=null && token.isValido() && token.getUsuario().equals(usuario);

	}

	
	/**
	 * Anexa as informações de credenciais na chamada do serviço
	 * @param port
	 */
	@SuppressWarnings("rawtypes")
	public void anexarCredenciais(BindingProvider port) {
		Binding binding = port.getBinding();
		
		List<Handler> handlerList = binding.getHandlerChain();
		handlerList.add(new CatalogoServicosClientSOAPHandler(this));
		handlerList.add(new CatalogoServicosMNIClientLogicalHandler(this));
		
		binding.setHandlerChain(handlerList);
		
	}

	/**
	 * Requisista token para acesso ao serviço
	 * @return
	 */
	/*
	 * Todo token gerado é armazenado no contexto de aplicacao e mantido como
	 * cache até a sua expiração
	 */
	public TokenDTO requisitarToken() {
		if(!Contexts.isApplicationContextActive()){
			Lifecycle.beginCall();
		}
		TokenDTO token = (TokenDTO) Contexts.getApplicationContext().get(
				getTokenCacheId(codigo));
		if (token != null) {
			if (token.isValido()) {
				return token;
			} 
		}
		token = newToken(codigo);
		return token;
	}

	/**
	 * Recupera os endereços do serviço
	 * @return
	 */
	public List<EnderecoDTO> getEnderecos() {
		String codigoInstalacao = codigo.substring(codigo.lastIndexOf("_") + 1);
		TokenDTO token = requisitarToken();

		for (InstalacaoDTO instalacao : token.getServicoDTO().getInstalacoes()) {
			if (instalacao.getCodInstalacao().equals(codigoInstalacao)) {
				return instalacao.getEnderecosDTO();
			}
		}

		return null;
	}

	/*
	 * Realiza nova chamada ao servidor STS 
	 */
	private TokenDTO newToken(String codigo) {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
		System.setProperty("javax.net.ssl.keyStore",
				ParametroUtil.getParametro(KEY_STORE_LOCATION_PARAM_NAME));
		System.setProperty("javax.net.ssl.keyStorePassword",
				ParametroUtil.getParametro(KEY_STORE_PASS_PARAM_NAME));

		if (ParametroUtil.getParametro(TRUST_STORE_PASS_PARAM_NAME) != null) {
			System.setProperty("javax.net.ssl.trustStore",
					ParametroUtil.getParametro(TRUST_STORE_LOCATION_PARAM_NAME));
			System.setProperty("javax.net.ssl.trustStorePassword",
					ParametroUtil.getParametro(TRUST_STORE_PASS_PARAM_NAME));
		}

		TokenResource client = ProxyFactory.create(TokenResource.class,
				ParametroUtil.getParametro(SERVICE_URL_PARAM_NAME));
		TokenDTO tokenDTO = null;

		try {
			tokenDTO = client.getToken(codigo);
		} catch (Exception e) {
			throw new RuntimeException(
					"Não foi possível recuperar o token para autenticação no Catalogo de Serviços",
					e);
		}

		//cache
		Contexts.getApplicationContext().set(getTokenCacheId(codigo), tokenDTO);
		return tokenDTO;
	}

	private String getTokenCacheId(String codigo) {
		Identity identity = Identity.instance();
		if (identity.isLoggedIn()) {
			return identity.getCredentials().getUsername() + "_" + codigo;
		} else {
			return codigo;
		}
	}

}
