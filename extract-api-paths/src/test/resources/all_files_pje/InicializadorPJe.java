package br.jus.cnj.pje.nucleo;

import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.security.Provider.Service;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.util.CarregarParametrosAplicacao;
import br.jus.pje.nucleo.Eventos;

@Name("inicializadorPJe")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Startup(depends = { "org.jboss.seam.async.dispatcher", CarregarParametrosAplicacao.NAME })
@Install(dependencies = { "org.jboss.seam.async.dispatcher", CarregarParametrosAplicacao.NAME })
public class InicializadorPJe {
	
	@In
	private Events events;
	
	@Logger
	private Log logger;
	
	@Create
	public void init(){
		try {
			initProviders();
		} catch (Throwable e) {
			logger.warn("Não foi possível incluir os apelidos de algoritmos de certificação no chaveiro da aplicação.");
		}
		events.raiseEvent(Eventos.AGENDA_SERVICOS);
	}
	
	private void initProviders() throws Throwable {
		Provider[] providers = Security.getProviders();
		for(Provider provider: providers){
			Service SHA1withRSA = provider.getService("Signature", "SHA1withRSA");
			Service SHA1 = provider.getService("MessageDigest", "SHA1");
			Service SHA256withRSA = provider.getService("Signature", "SHA256withRSA");
			Service SHA256 = provider.getService("MessageDigest", "SHA-256");
			Method m = Service.class.getDeclaredMethod("addAlias", String.class);
			m.setAccessible(true);
			Method m2 = Provider.class.getDeclaredMethod("putService", Provider.Service.class);
			m2.setAccessible(true);
			if(SHA1withRSA != null){
				m.invoke(SHA1withRSA, "1.2.840.113549.1.1.5");
				m2.invoke(provider, SHA1withRSA);
			}
			if(SHA1 != null){
				m.invoke(SHA1, "1.3.14.3.2.26");
				m2.invoke(provider, SHA1);
			}
			if(SHA256withRSA != null){
				m.invoke(SHA256withRSA, "1.2.840.113549.1.1.11");
				m2.invoke(provider, SHA256withRSA);
			}
			if(SHA256 != null){
				m.invoke(SHA256, "2.16.840.1.101.3.4.2.1");
				m2.invoke(provider, SHA256);
			}
		}
	}

}
