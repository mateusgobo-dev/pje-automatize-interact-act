package br.jus.cnj.pje.editor.lool;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class ConversorDocClient {
	private String urlConversorDoc;

	public ConversorDocClient(String urlConversorDoc) {
		this.urlConversorDoc = urlConversorDoc;
	}
	
	public InputStream convert(InputStream inputStream, String origem, String destino) {
		Client client = ClientBuilder.newClient();
		String url = this.urlConversorDoc+"/"+origem+"/"+destino;
		return client.target(url).request().post(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM), InputStream.class);
	}


}
