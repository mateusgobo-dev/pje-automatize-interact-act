package br.jus.cnj.pje.webservice.client;

import javax.ws.rs.core.GenericType;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.webservice.client.corporativo.OrgaoGenericoDTO;
import br.jus.pje.webservice.client.AbstractRestClient;
import br.jus.pje.webservice.util.Utils;

@Name("corporativoRestClient")
@Scope(ScopeType.EVENT)
public class CorporativoClient extends AbstractRestClient {
	
	private static final LogProvider log = Logging.getLogProvider(CorporativoClient.class);

	@Override
	protected String getBearerToken() {
		return null;
	}

	@Override
	protected String getServiceId() {
		return "corporativo-proxy";
	}
	
	public OrgaoGenericoDTO recuperaOrgao(String codigo) throws PJeBusinessException{

		OrgaoGenericoDTO result = null;

		try {
			PjeResponse<OrgaoGenericoDTO> response = this.getInvocationBuilder(this.getServiceId(), String.format("/api/v1/orgaos-genericos/%s", codigo), null)
					.get(new GenericType<PjeResponse<OrgaoGenericoDTO>> () {});

			if (response.getStatus().equals("OK")) {
				result = response.getResult();
				if(result == null) {
					throw new PJeBusinessException("Órgão de código " + codigo + " não encontrado no CNJ - Corporativo.");
				}
			} else {
				throw new PJeBusinessException(Utils.removeBracket(response.getMessages().toString()));
			}
		}
		catch (PJeBusinessException e) {
			throw e;
		}
		catch (Exception ex) {
			log.error(ExceptionUtils.getStackTrace(ex));
			throw new PJeBusinessException("Erro ao pesquisar o órgão de código " + codigo+". Favor tentar novamente.");
		}

		return result;
	}

}
