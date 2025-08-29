package br.jus.cnj.pje.monitoramento;

import javax.ws.rs.core.Response;

public interface MonitoramentoService {

	Response monitorar(String recursom, String tipo);

}
