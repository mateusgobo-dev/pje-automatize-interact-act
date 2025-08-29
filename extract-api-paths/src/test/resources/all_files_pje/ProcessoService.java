package br.com.infox.pje.webservices;

import javax.jws.WebMethod;

import br.jus.pje.nucleo.entidades.Processo;

//@WebService
public class ProcessoService {

	@WebMethod()
	public Processo getProcesso(String numero) {
		Processo processo = new Processo();
		return processo;
	}
}
