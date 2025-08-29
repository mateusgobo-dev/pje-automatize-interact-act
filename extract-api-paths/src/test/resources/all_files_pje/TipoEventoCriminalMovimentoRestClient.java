package br.jus.cnj.pje.webservice.client;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.GenericType;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.criminal.error.PjeErrorDetail;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.pje.nucleo.dto.TipoEventoCriminalMovimentoDTO;

@Name(TipoEventoCriminalMovimentoRestClient.NAME)
public class TipoEventoCriminalMovimentoRestClient extends BaseRestClient<TipoEventoCriminalMovimentoDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoEventoCriminalMovimentoRestClient";

	@Override
	public String getServicePath() {
		return "/criminal/tiposEventoCriminalMovimento";
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
	
	public List<TipoEventoCriminalMovimentoDTO> recuperarPorCodMovimento(String codMovimento) {
		this.webTarget = this.client.target(getGatewayPath()).path(getServicePath() + "/evento/" + codMovimento);
		return getInvocationDefaults().get(new GenericType<List<TipoEventoCriminalMovimentoDTO>>() {});
	}
	
	@Override
	public Boolean deleteResource(Integer id) throws ClientErrorException {
		webTarget = this.client.target(getGatewayPath()).path(this.getServicePath() + "/" + id);
		ClientResponse resp = (ClientResponse) getInvocationDefaults().delete();
		if (resp.getStatus() == HttpStatus.SC_OK) {
			resp.close(); // <-- fechar o response para evitar BasicClientConnManager: connection still allocated.
			return true;
		} else {
			PjeErrorDetail erro = resp.readEntity(PjeErrorDetail.class);
			throw new PJeRuntimeException(erro.getMessage());
		}
	}
	
}
