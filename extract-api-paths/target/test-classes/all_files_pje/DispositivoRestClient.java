package br.jus.cnj.pje.webservice.client;

import java.util.List;

import javax.ws.rs.core.GenericType;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.dto.DispositivoDTO;

@Name(DispositivoRestClient.NAME)
public class DispositivoRestClient extends BaseRestClient<DispositivoDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "dispositivoRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/dispositivos";
	}

	@Override
	public String getSearchPath() {
		return "pesquisar";
	}

	@Override
	public String getServiceUsername() {
		return ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_USERNAME;
	}

	@Override
	public String getServicePassword() {
		return ConfiguracaoIntegracaoCloud.PJE2_CRIMINAL_PASSWORD;
	}

	@Override
	public boolean isBasicAuth() {
		return true;
	}
	
	public List<DispositivoDTO> recuperarDispositivosPorIdNorma(Integer idNorma) {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath() + "/norma/" + idNorma);
		return getInvocationDefaults().get(new GenericType<List<DispositivoDTO>>() {});
	}
	
	public List<DispositivoDTO> recuperarDispositivosPorIdNormaIdTipoDispositivo(Integer idNorma, Integer idTipoDispositivo) {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath() + "/" + idNorma + "/" + idTipoDispositivo);
		return  getInvocationDefaults().get(new GenericType<List<DispositivoDTO>>() {});
	}
	
}
