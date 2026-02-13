package br.jus.cnj.pje.nucleo;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;

import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.webservice.client.JWTsUtil;

public class RefreshAcessTokenFilter implements Filter{
	
	private static final LogProvider log = Logging.getLogProvider(RefreshAcessTokenFilter.class);
	public final static String ATUALIZACAO_TOKEN_SSO_EM_PROGRESSO = "ATUALIZACAO_TOKEN_SSO_EM_PROGRESSO";
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		try {
			boolean isSSOAuthEnabled = ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled();
			if(isSSOAuthEnabled) {
				HttpSession httpSession = ((HttpServletRequest) request).getSession(false);
				if(httpSession != null) {
					KeycloakSecurityContext ksc = (KeycloakSecurityContext) httpSession.getAttribute(Constantes.SSO_CONTEXT_NAME);
					if(ksc != null) {
						if(!isTokenValido(ksc)) {
							RefreshableKeycloakSecurityContext rksc = (RefreshableKeycloakSecurityContext) ksc;
							if(!JWTsUtil.createInstance().isRefreshTokenSSOExpired(rksc.getRefreshToken())) {
								if(!existeAtualizacaoTokenSSOemProgresso(httpSession)) {
									httpSession.setAttribute(ATUALIZACAO_TOKEN_SSO_EM_PROGRESSO, true);
									CompletableFuture.runAsync(() -> {
											try {
												log.info("Processando requisição de atualização de token do SSO");
												if(rksc.refreshExpiredToken(false)) {
													httpSession.setAttribute(Constantes.SSO_CONTEXT_NAME, rksc);
													log.info("Atualizada a sessão com acesstoken/refresh do SSO");
													log.info("Token do SSO atualizado com sucesso");
												}else {
													log.error("O acess/refresh token do SSO não foi atualizado");
												}
												setAtualizacaoTokenSSOEmProgressoNaSessaoFalse(httpSession);
											}catch(Exception ex) {
												log.error("Erro ao atualizar o token do usuário utilizando o SSO");
												setAtualizacaoTokenSSOEmProgressoNaSessaoFalse(httpSession);
											}
											
										});
								}
							}
							else{
								log.error("O refreshToken informado do SSO está expirado");
							}
							
						}
					}
				}
			}
			
		}catch(Exception ex) {
			log.error("Ocorreu um erro ao atualizar o acessToken/refreshToken do usuário - Erro: " +  ex.getLocalizedMessage());
			HttpSession httpSession = ((HttpServletRequest) request).getSession(false);
			setAtualizacaoTokenSSOEmProgressoNaSessaoFalse(httpSession);
			
		}finally {
			chain.doFilter(request, response);
		}
		
		
	}
	
	private boolean existeAtualizacaoTokenSSOemProgresso(HttpSession httpSession) {
		Boolean atualizacaoemprogresso = (Boolean) httpSession.getAttribute(ATUALIZACAO_TOKEN_SSO_EM_PROGRESSO);
		if(atualizacaoemprogresso != null) {
			if(atualizacaoemprogresso) {
				return true;
			}
		}
		return false;
	}
	
	private void setAtualizacaoTokenSSOEmProgressoNaSessaoFalse(HttpSession httpSession) {
		if(httpSession != null) {
			if(existeAtualizacaoTokenSSOemProgresso(httpSession)){
				httpSession.setAttribute(ATUALIZACAO_TOKEN_SSO_EM_PROGRESSO, false);
			}
		}
	}

	private boolean isTokenValido(KeycloakSecurityContext ksc) {
		RefreshableKeycloakSecurityContext rksc = (RefreshableKeycloakSecurityContext) ksc;
		if (rksc.isActive() && rksc.isTokenTimeToLiveSufficient(ksc.getToken())) {
			return true;
		} 
		return false;
		
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
