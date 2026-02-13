package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoOrigemRestClient;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.EstadosBrasileirosEnum;

@Name(ProcessoCriminalAction.NAME)
@Scope(ScopeType.PAGE)
public class ProcessoCriminalAction extends BaseRestAction<ProcessoCriminalDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoCriminalAction";
	
	@In(create=true)
	private TipoOrigemRestClient tipoOrigemRestClient;
	
	@In(create=true)
	private ProcessoCriminalRestClient processoCriminalRestClient;
	
	private ProcessoCriminalDTO processoCriminalDTO;
	
	@Logger
	private transient Log log;

	@Create
	public void create() throws PJeException{
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if(processoTrf != null){
			try {
				processoCriminalDTO = processoCriminalRestClient.getResourceByProcesso(processoTrf.getNumeroProcesso());
			} catch (PJeException e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}
	
	@Override
	protected BaseRestClient<ProcessoCriminalDTO> getRestClient() {
		return processoCriminalRestClient;
	}

	@Override
	public Integer getPageSize() {
		return null;
	}

	@Override
	public Integer getCurrentPage() {
		return null;
	}

	public ProcessoCriminalDTO getProcessoCriminalDTO() {
		return processoCriminalDTO;
	}

	public void setProcessoCriminalDTO(ProcessoCriminalDTO processoCriminalDTO) {
		this.processoCriminalDTO = processoCriminalDTO;
	}
	
	public String getDescricaoUf(String uf) {
		String ufCompleta = "";
		if (uf != null && !uf.isEmpty()) {
			EstadosBrasileirosEnum enumUfs = EstadosBrasileirosEnum.valueOf(uf);
			ufCompleta = enumUfs.getLabel();
		}
		return ufCompleta;
	}
	
}
