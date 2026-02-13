package br.jus.cnj.pje.webservice.mobile.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.service.EmailService;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.AssinadorA1;
import br.jus.cnj.pje.nucleo.manager.UsuarioMobileManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.mobile.JwtTokenUtil;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.PlataformaDispositivoEnum;

@Name(AutenticacaoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/api/v1/mobile/autenticacao")
public class AutenticacaoRestController extends AbstractRestController {
	
	public static final String NAME = "autenticacaoRestController";
	@In(create = true, required = false)
	private AssinadorA1 assinadorA1;

	private static final Logger logger = LoggerFactory.getLogger(AutenticacaoRestController.class);

	@PUT
	@Path("/parear-dispositivo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response parearDispositivo(String inputJson) {

		JSONObject jsonOut = new JSONObject();
		
		try {
			
			Util.beginTransaction();
			
			JSONObject jsonIn = new JSONObject(inputJson);
			String cpf = (String) jsonIn.get("cpf");
			String email = (String) jsonIn.get("email");
			String codigoPareamento = (String) jsonIn.get("codigoPareamento");
			String codigoConfirmacao = (String) jsonIn.get("codigoConfirmacao");
			String plataforma = (String) jsonIn.opt("plataforma");
			String versaoPlataforma = (String) jsonIn.opt("versaoPlataforma");
			String nomeDispositivo = (String) jsonIn.opt("nomeDispositivo");
			
			
			PlataformaDispositivoEnum pDispositivo = null;
			if("android".equalsIgnoreCase(plataforma)){
				pDispositivo = PlataformaDispositivoEnum.A;
			} else if("ios".equalsIgnoreCase(plataforma)){
				pDispositivo = PlataformaDispositivoEnum.I;
			}
			
			UsuarioMobileManager usuarioManager = ComponentUtil.getComponent(UsuarioMobileManager.class);
			UsuarioMobile usuarioMobile = usuarioManager.getUsuarioMobileParaPareamento(codigoPareamento, cpf, email);
			
			if(usuarioMobile == null){
				throw new Exception("Nenhum registro encontrado para o pareamento");
			} 
			
			String pass = usuarioManager.gerarTokenContador(usuarioMobile.getCodigoPareamento());
			
			if ( !pass.equals( codigoConfirmacao ) ) {
				throw new Exception("Código de confirmação inválido");
			}
			
			EmailService emailService = new EmailService();
			List<UsuarioLogin> usuarioEmailList = new ArrayList<>();
			UsuarioLogin usr = new UsuarioLogin();
			usr.setEmail(email);
			usuarioEmailList.add(usr);

			StringBuilder bodyEmail = new StringBuilder();
			String tribunal = PjeUtil.instance().getNomeSistema();

			bodyEmail.append(tribunal).append("<br/><br/>");
			bodyEmail.append("Dispositivo pareado:").append("<br/><br/>");
			bodyEmail.append("<ul><li>Sistema Operacional - ").append(plataforma).append("</li>");
			bodyEmail.append("<li>Versão do Sistema - ").append(versaoPlataforma).append("</li>");
			bodyEmail.append("<li>Dispositivo - ").append(nomeDispositivo).append("</li></ul>");
			emailService.enviarEmail(usuarioEmailList, "Dispositivo pareado com sucesso", bodyEmail.toString());
			
			usuarioManager.parearDispositivo(pDispositivo, usuarioMobile, versaoPlataforma, nomeDispositivo);
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			JSONObject jsonData = new JSONObject();
			jsonData.put("hora", new Date().getTime());
			jsonOut.put("data", jsonData);
			
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.error("Erro no pareamento do dispositivo mobile. Json recebido: '" + inputJson + "'. Exceção: "
						+ e.getLocalizedMessage());

				jsonOut.put("status", "error");
				jsonOut.put("code", 500);
				jsonOut.put("messages", new String[]{e.getMessage()});
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		Util.commitTransction();
		
		return Response.status(200).entity(jsonOut.toString()).build();

	}
	
	@POST
	@Path("/autenticar-usuario")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response autenticarUsuario(String inputJson) throws JSONException {

		JSONObject jsonOut = new JSONObject();
		try {
			
			JSONObject jsonIn = new JSONObject(inputJson);
		
			String cpf = (String) jsonIn.get("cpf");
			String password = (String) jsonIn.get("senha");
			String codigoPareamento = (String) jsonIn.get("codigoPareamento");
			
			UsuarioMobile usuarioMobile = autenticarUsuario(cpf.replaceAll("[\\.-]", ""), password, codigoPareamento);
			
			String jwt  = JwtTokenUtil.gerarJwt(usuarioMobile);
			
			List<UsuarioLocalizacao> list = Authenticator.instance().getLocalizacoes();
			
			if ( list==null || list.size()==0 ) {
				throw new Exception("Usuário sem localização");
			}
			
			Object localizacaoPadrao = Authenticator.instance().getLocalizacaoAtualCombo(); 
			
			if ( localizacaoPadrao!=null ) {
				for (int i=0 ; i<list.size() ; i++) {
					if ( list.get(i).equals( localizacaoPadrao ) ) {
						UsuarioLocalizacao loc = list.remove(i);
						list.add(0, loc);
						break;
					}
				}
			}
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			
			JSONObject jsonData = new JSONObject();
			jsonData.put("jwt", jwt);
			jsonData.put("assinadorHabilitado", assinadorA1!=null);
			
			JSONArray arrayLocalizacoes = new JSONArray();
			
			for (UsuarioLocalizacao loc: list) {
				JSONObject jsonLoc = new JSONObject();
				jsonLoc.put("id", loc.getIdUsuarioLocalizacao());
				jsonLoc.put("nome", loc.toString());
				arrayLocalizacoes.put(jsonLoc);
			}
			
			jsonData.put("localizacoes", arrayLocalizacoes);
			jsonOut.put("data", jsonData);
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			jsonOut.put("messages", new String[]{"Senha inválida"});
		} catch (Exception e) {
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			if ( e.getCause()!=null && e.getCause() instanceof LoginException ) {
				jsonOut.put("messages", new String[]{e.getCause().getMessage()});
			} else {
				jsonOut.put("messages", new String[]{e.getMessage()});
			}
		} finally {
			Identity.instance().logout();
		}
		
		return Response.status(201).entity(jsonOut.toString()).build();
	}
	
	private UsuarioMobile autenticarUsuario(String cpf, String password, String codigoPareamento) throws Exception {
		UsuarioMobileManager usuarioManager = ComponentUtil.getComponent(UsuarioMobileManager.class);
		UsuarioMobile usuarioMobile = usuarioManager.getUsuarioMobilePareado(codigoPareamento);
		
		if(usuarioMobile == null){
			throw new Exception("Dispositivo não encontrado");
		}
		
		Authenticator.instance().authenticateMobile(cpf, password);
		
		return usuarioMobile;

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tokens")
	public Response gerarToken() throws JSONException {
		
		JSONObject jsonOut = new JSONObject();

		try {
			UsuarioMobile usuarioMobile = validarJwt();
			
			Date data = new Date();
			
			String otpToken = usuarioMobileManager.gerarTokenTempo(usuarioMobile.getCodigoPareamento(), data);
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			
			JSONObject jsonData = new JSONObject();
			jsonData.put("token", otpToken);
			jsonData.put("hora", data.getTime());
			
			jsonOut.put("data", jsonData);
			
		} catch (Exception e) {
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			jsonOut.put("messages", new String[]{e.getMessage()});
		}
		

		return Response.status(200).entity(jsonOut.toString()).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/validar-jwt")
	public Response isJwtValido() throws JSONException {
		
		JSONObject jsonOut = new JSONObject();

		try {
			UsuarioMobile usuarioMobile = validarJwt();
			
			String jwt  = JwtTokenUtil.gerarJwt(usuarioMobile);
			
			List<UsuarioLocalizacao> list = ComponentUtil.getComponent(UsuarioService.class).getLocalizacoesAtivas(usuarioMobile.getUsuario());
			
			if ( list==null || list.size()==0 ) {
				throw new Exception("Usuário sem localização");
			}
			
			
			jsonOut.put("status", "ok");
			jsonOut.put("code", 200);
			
			JSONObject jsonData = new JSONObject();
			jsonData.put("jwt", jwt);
			jsonData.put("assinadorHabilitado", assinadorA1!=null);
			
			JSONArray arrayLocalizacoes = new JSONArray();
			
			for (UsuarioLocalizacao loc: list) {
				JSONObject jsonLoc = new JSONObject();
				jsonLoc.put("id", loc.getIdUsuarioLocalizacao());
				jsonLoc.put("nome", loc.toString());
				arrayLocalizacoes.put(jsonLoc);
			}
			
			jsonData.put("localizacoes", arrayLocalizacoes);
			jsonOut.put("data", jsonData);
			
		} catch (Exception e) {
			e.printStackTrace();
			jsonOut.put("status", "error");
			jsonOut.put("code", 500);
			jsonOut.put("messages", new String[]{e.getMessage()});
		}
		

		return Response.status(200).entity(jsonOut.toString()).build();
	}
	
	




}
