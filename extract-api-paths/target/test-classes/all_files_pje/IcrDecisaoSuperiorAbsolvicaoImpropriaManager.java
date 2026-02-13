package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorAbsolvicaoImpropria;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrDAIManager")
public class IcrDecisaoSuperiorAbsolvicaoImpropriaManager extends
IcrDecisaoSuperiorManager<IcrDecisaoSuperiorAbsolvicaoImpropria> {
	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return IcrDecisaoSuperiorAbsolvicaoImpropria.getTiposDeIcrAceitos();
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrDecisaoSuperiorAbsolvicaoImpropria entity) {
		return entity.getIcrAfetada();
	}

	@Override
	public Date getDtPublicacao(IcrDecisaoSuperiorAbsolvicaoImpropria entity) {
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return true;
	}

	@Override
	protected void ensureUniqueness(IcrDecisaoSuperiorAbsolvicaoImpropria entity) throws IcrValidationException {
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null) { // PERSIST
			sb.append("SELECT o FROM IcrDecisaoSuperiorAbsolvicaoImpropria o where o.ativo = true");
		} else if (entity.getId() != null) { // UPDATE
			sb.append("SELECT o FROM IcrDecisaoSuperiorAbsolvicaoImpropria o where o.ativo = true and o.id <> "
					+ entity.getId());
		}
		sb.append(" and o.processoParte.idProcessoParte =  :idProcessoParte");
		sb.append(" and ");
		sb.append(" o.icrAfetada.id = :icrAfetadaId");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcessoParte", entity.getProcessoParte().getIdProcessoParte());
		query.setParameter("icrAfetadaId", entity.getIcrAfetada().getId());
		if (!query.getResultList().isEmpty()) {
			throw new IcrValidationException("Registro informado já cadastrado no sistema.");
		}
	}
}
