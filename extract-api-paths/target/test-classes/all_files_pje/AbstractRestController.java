package br.jus.cnj.pje.webservice.mobile.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.jboss.seam.annotations.In;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.manager.UsuarioMobileManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.cnj.pje.webservice.mobile.DadosJwt;
import br.jus.cnj.pje.webservice.mobile.JwtTokenUtil;
import br.jus.pje.nucleo.entidades.UsuarioMobile;

public class AbstractRestController {
	@Context HttpServletRequest httpRequest;
	@In UsuarioMobileManager usuarioMobileManager;
	@In(create = true)
	private UsuarioService usuarioService;
	private InformacaoUsuarioSessao informacaoDoUsuario;
	
	protected UsuarioMobile autenticarUsuarioToken(Integer idLocalizacao) throws Exception {
		UsuarioMobile usuarioMobile = validarJwt();
		Authenticator.instance().authenticateMobile(usuarioMobile, idLocalizacao);
		return usuarioMobile;
	}
	
	protected UsuarioMobile autenticarUsuarioToken() throws Exception {
		return autenticarUsuarioToken(null);
	}
	
	protected UsuarioMobile validarJwt() throws Exception {
		String bearer = httpRequest.getHeader("Authorization");
		String jwt = bearer.split(" ")[1];
		DadosJwt dadosToken = JwtTokenUtil.getDadosFromJwt(jwt);
		UsuarioMobile usuarioMobile = usuarioMobileManager.getUsuarioMobilePareado(dadosToken.getCodigoPareamento());
		if(usuarioMobile == null ){
			throw new Exception("Dispositivo desconhecido");
		}
		
		if(!JwtTokenUtil.validateToken(jwt)){
			throw new Exception("Sua sessão expirou, efetue novamente o login para continuar.");
		}
		return usuarioMobile;
	}
	
	
	protected InformacaoUsuarioSessao getUsuarioSesssao() {
		if (this.informacaoDoUsuario == null) {
			this.informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
		}
		return informacaoDoUsuario;
	}
	
	protected String getResourcePath() {
		return  httpRequest.getScheme() + "://"
				+ httpRequest.getServerName() + ":" + httpRequest.getServerPort()
				+ httpRequest.getContextPath();
		
	}

	
	
}
