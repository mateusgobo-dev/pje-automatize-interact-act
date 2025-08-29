package br.jus.cnj.pje.webservice.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface IMiniPacService {

	Response processar(Long idTarefa, Integer idProcessoTrf, Integer idProcessoDocumento, HttpServletRequest contexto);
	
}
