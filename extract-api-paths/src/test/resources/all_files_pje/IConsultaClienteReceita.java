package br.com.infox.trf.webservice;

import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoa;

public interface IConsultaClienteReceita {

	public DadosReceitaPessoa consultaDados(String nrDocumento, boolean forceUpdate) throws Exception;
	
	public DadosReceitaPessoa consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception;
	
}
