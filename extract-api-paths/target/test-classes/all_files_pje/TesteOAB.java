package br.com.infox.cliente.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import br.org.oab.www5.consultanacionalws.ConsultaNacionalWs;
import br.org.oab.www5.consultanacionalws.ConsultaNacionalWsSoap;

public class TesteOAB {

	public static void teste() throws RemoteException, MalformedURLException {
		ConsultaNacionalWs service1 = new ConsultaNacionalWs(new URL(
				"http://www5.oab.org.br/ConsultaNacionalWS/ConsultaNacionalWs.asmx"));
		ConsultaNacionalWsSoap port1 = service1.getConsultaNacionalWsSoap();

		String retornarDadosAdvogadoByCPF = port1.retornarDadosAdvogadoByCPF("47468769672");
		System.out.println(retornarDadosAdvogadoByCPF);
	}

	public static void main(String[] args) throws RemoteException, MalformedURLException {
		teste();
	}

}
