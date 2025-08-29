package br.jus.cnj.pje.webservice.controller;

import java.io.Serializable;
import java.net.MalformedURLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.dto.CabecalhoSistemaDTO;

@Name(ParametrosRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/parametros")
@Restrict("#{identity.loggedIn}")
public class ParametrosRestController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "parametrosRestController";

	@GET
	@Path("/cabecalhoSistema")
	@Produces(MediaType.APPLICATION_JSON)
	public CabecalhoSistemaDTO recuperaCabecalhoSistema() throws MalformedURLException {
		String caminhoImagem = ParametroUtil.getParametro(Parametros.LOGO_TRIBUNAL);
		return new CabecalhoSistemaDTO(ParametroUtil.getParametro(Parametros.NOME_SISTEMA),
				ParametroUtil.getParametro(Parametros.NOME_SECAO_JUDICIARIA), Util.encodeBase64FromFile(caminhoImagem),
				ParametroUtil.getParametro(Parametros.SUB_NOME_SISTEMA).toUpperCase(),
				ParametroUtil.getParametro(Parametros.NUMERO_REGIONAL));
	}
}
