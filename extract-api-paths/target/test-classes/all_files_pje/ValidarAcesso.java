package br.com.infox.pje.webservices;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.jboss.seam.contexts.Lifecycle;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.itx.util.ComponentUtil;

@WebService
public class ValidarAcesso {

	@WebMethod
	public String validar(String key, String ip) {
		Lifecycle.beginCall();
		SecurityTokenControler securityTokenControler = ComponentUtil.getComponent(SecurityTokenControler.NAME);
		String token = securityTokenControler.createToken(ip);
		Lifecycle.endCall();
		return token;
	}

}
