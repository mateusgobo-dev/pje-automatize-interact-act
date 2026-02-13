package br.com.infox.cliente.component.securitytoken;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.ibpm.entity.log.LogException;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.pje.webservices.validaracesso.ValidarAcessoService;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.ComponentUtil;

@Name(TokenManager.NAME)
@Scope(ScopeType.EVENT)
public class TokenManager implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tokenManager";

	private static final String VALIDAR_ACESSO_WSDL = "ValidarAcesso?wsdl";

	public static TokenManager instance(){
		return ComponentUtil.getComponent(NAME);
	}

	public String getRemoteToken(String link) throws MalformedURLException, IOException, LogException{
		String token = null;
		link = link.concat(VALIDAR_ACESSO_WSDL);
		if (UrlUtil.isUrlValida(link)){
			URL wsdl = new URL(link);
			ValidarAcessoService service = new ValidarAcessoService(wsdl);
			token = service.getValidarAcessoPort().validar(null, LogUtil.getIpRequest());
			SecurityTokenControler.instance().createToken(LogUtil.getIpRequest());
		}
		return token;
	}

	public void validateToken(String token){
		SecurityTokenControler securityTokenControler = ComponentUtil.getComponent(SecurityTokenControler.NAME);
		boolean tokenValido = false;
		try{
			tokenValido = securityTokenControler.isTokenValido(token, LogUtil.getIpRequest());
		} catch (LogException e){
			e.printStackTrace();
		}
		if (!tokenValido){
			lancarErro("Requisição invalida ou tempo expirado! Repita a operação.");
		}
	}

	private void lancarErro(String msg){
		FacesMessages.instance().add(Severity.ERROR, msg);
		Redirect.instance().setViewId("/EstatisticaProcessoJusticaFederal/error.seam");
		Redirect.instance().execute();
	}

}
