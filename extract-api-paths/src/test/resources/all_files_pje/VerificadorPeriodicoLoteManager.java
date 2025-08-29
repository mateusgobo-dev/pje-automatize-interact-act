package br.jus.cnj.pje.nucleo.manager;

import java.util.UUID;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.VerificadorPeriodicoLoteDAO;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;

@Name(VerificadorPeriodicoLoteManager.NAME)
public class VerificadorPeriodicoLoteManager extends BaseManager<VerificadorPeriodicoLote> {

	public static final String NAME = "verificadorPeriodicoLoteManager";

	@In
	private VerificadorPeriodicoLoteDAO verificadorPeriodicoLoteDAO;

	protected VerificadorPeriodicoLoteDAO getDAO() {
		return verificadorPeriodicoLoteDAO;
	}

	public VerificadorPeriodicoLote find(Integer id) {
		return this.verificadorPeriodicoLoteDAO.find(id);
	}

	public Integer getJobsProcessadosPorLote(UUID lote) {
		return getDAO().getExpedientesProcessadosPorLote(lote);
	}

	public VerificadorPeriodicoLote getByIdJobAndLote(Integer idJob, UUID lote) {
		return getDAO().getByIdJobAndLote(idJob, lote);
	}

	public VerificadorPeriodicoLote getProcessado(Integer idRelatorio, String passo) {
		return getDAO().getProcessado(idRelatorio, passo);
	}

	public VerificadorPeriodicoLote persist(VerificadorPeriodicoLote verificadorPeriodicoLote) {
		return getDAO().persist(verificadorPeriodicoLote);
	}

	public VerificadorPeriodicoLote merge(VerificadorPeriodicoLote verificadorPeriodicoLote) {
		return getDAO().merge(verificadorPeriodicoLote);
	}
}