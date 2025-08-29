package br.jus.cnj.pje.editor.lool;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class WopiClient {
	private String libreOfficeWopiUrlExterno;

	public WopiClient(String libreOfficeWopiUrlExterno) {
		this.libreOfficeWopiUrlExterno = libreOfficeWopiUrlExterno;
	}
	
	public InputStream getContent(String folder, String file, String access) {
		Client client = ClientBuilder.newClient();
		String url = this.libreOfficeWopiUrlExterno+folder+"-"+file+"/contents?access_header="+access;
		return client.target(url).request(MediaType.APPLICATION_OCTET_STREAM).get(InputStream.class);
	}
	
	public void save(String folder, String file, InputStream inputStream, String access) {
		Client client = ClientBuilder.newClient();
		String url = this.libreOfficeWopiUrlExterno+folder+"-"+file+"/contents?access_header="+access;
		client.target(url).request().post(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM));
	}
	
	public void delete(String folder, String file, String access) {
		Client client = ClientBuilder.newClient();
		String url = this.libreOfficeWopiUrlExterno+folder+"-"+file+"?access_header="+access;
		client.target(url).request().delete();
	}

	
}
