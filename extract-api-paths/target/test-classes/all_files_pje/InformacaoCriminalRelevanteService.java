package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante;

@Name("informacaoCriminalRelevanteService")
public class InformacaoCriminalRelevanteService {
	public static final String NAME = "informacaoCriminalRelevanteService";

	protected EntityManager getEntityManager() {
		return (EntityManager) Component.getInstance("entityManager");
	}

	@SuppressWarnings("unchecked")
	private IcrBaseManager<InformacaoCriminalRelevante> getManager(InformacaoCriminalRelevante entity) {
		TipoInformacaoCriminalRelevante tipoIcr = entity.getTipo();
		IcrBaseManager<InformacaoCriminalRelevante> manager = null;
		if (tipoIcr != null) {
			String managerName = "icr" + tipoIcr.getCodigo() + "Manager";
			manager = (IcrBaseManager<InformacaoCriminalRelevante>) Component.getInstance(managerName);
		} else {
			manager = (IcrBaseManager<InformacaoCriminalRelevante>) Component
					.getInstance("informacaoCriminalRelevanteManager");
		}
		return manager;
	}

	public void validate(InformacaoCriminalRelevante entity) throws IcrValidationException {
		getManager(entity).validate(entity);
	}

	public boolean exisits(InformacaoCriminalRelevante entity) throws IcrValidationException {
		return getManager(entity).exists(entity);
	}

	public void inactive(InformacaoCriminalRelevante entity) throws IcrValidationException {
		getManager(entity).inactive(entity);
	}

	public void persist(InformacaoCriminalRelevante entity) throws IcrValidationException {
		getManager(entity).persist(entity);
	}

	public void persistAll(List<InformacaoCriminalRelevante> entityList) throws IcrValidationException {
		getManager(entityList.get(0)).persistAll(entityList);
	}

	public void refresh(InformacaoCriminalRelevante entity) {
		getManager(entity).refresh(entity);
	}

	public List<TipoInformacaoCriminalRelevante> getTipoInformacaoCriminalRelevanteList() {
		return getManager(new InformacaoCriminalRelevante()).getTipoInformacaoCriminalRelevanteList();
	}

	public List<ProcessoEvento> getMovimentacoes(ProcessoTrf processoTrf, Date dataInicio, Date dataFim,
			Evento movimentacaoSelecionada) throws IcrValidationException {
		return getManager(new InformacaoCriminalRelevante()).getMovimentacoes(processoTrf, dataInicio, dataFim,
				movimentacaoSelecionada);
	}
}
