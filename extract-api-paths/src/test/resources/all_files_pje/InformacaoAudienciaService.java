package br.jus.cnj.pje.webservice;

import javax.ws.rs.core.Response;

public interface InformacaoAudienciaService {
	Response recuperarPauta(String dataInicioString, Integer idOrgaoJulgador, Integer idSalaAudiencia, String dataFimString);
}
