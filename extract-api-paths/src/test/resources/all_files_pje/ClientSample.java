package br.com.infox.pje.webservices.consultaoab.clientsample;

import java.io.Serializable;
import java.net.MalformedURLException;
import org.jboss.seam.annotations.Name;
import br.com.infox.pje.webservices.consultaoab.Authentication;
import br.com.infox.pje.webservices.consultaoab.Service;
import br.com.infox.pje.webservices.consultaoab.ServiceSoap;

@Name("clientSampleOabInfox")
public class ClientSample implements Serializable{

	private static final long serialVersionUID = 1L;

	public void executaTeste(){
		try{
			teste();
		} catch (MalformedURLException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws MalformedURLException{
		teste();
	}

	private static void teste() throws MalformedURLException{
		System.out.println("***********************");
		System.out.println("Create Web Service Client...");
		Authentication auth = new Authentication();
		auth.setKey("4a9edd58-5239-4da0-a8b0-c8a67e211790");
		Service service = new Service();
		System.out.println("Create Web Service...");
		ServiceSoap port = service.getServiceSoap();
		System.out.println("Call Web Service Operation...");

		System.out.println("Server said: " + port.consultaAdvogadoPorCpf("47468769672", auth));
		System.out.println("***********************");
		System.out.println("Call Over!");
	}
}
