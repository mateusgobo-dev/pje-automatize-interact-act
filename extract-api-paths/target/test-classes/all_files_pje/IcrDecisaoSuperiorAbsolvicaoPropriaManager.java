package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorAbsolvicaoPropria;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrDAPManager")
public class IcrDecisaoSuperiorAbsolvicaoPropriaManager extends
IcrDecisaoSuperiorManager<IcrDecisaoSuperiorAbsolvicaoPropria> {
	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return IcrDecisaoSuperiorAbsolvicaoPropria.getTiposDeIcrAceitos();
	}

	@Override
	public Date getDtPublicacao(IcrDecisaoSuperiorAbsolvicaoPropria entity) {
		return entity.getDataPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return true;
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrDecisaoSuperiorAbsolvicaoPropria entity) {
		return entity.getIcrAfetada();
	}

	@Override
	protected void ensureUniqueness(IcrDecisaoSuperiorAbsolvicaoPropria entity) throws IcrValidationException {
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null) { // PERSIST
			sb.append("SELECT o FROM IcrDecisaoSuperiorAbsolvicaoPropria o where o.ativo = true");
		} else if (entity.getId() != null) { // UPDATE
			sb.append("SELECT o FROM IcrDecisaoSuperiorAbsolvicaoPropria o where o.ativo = true and o.id <> "
					+ entity.getId());
		}
		sb.append(" and o.processoParte.idProcessoParte =  :idProcessoParte");
		sb.append(" and ");
		sb.append("o.icrAfetada.id = :icrAfetadaId");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcessoParte", entity.getProcessoParte().getIdProcessoParte());
		query.setParameter("icrAfetadaId", entity.getIcrAfetada().getId());
		if (!query.getResultList().isEmpty()) {
			throw new IcrValidationException("Registro informado já cadastrado no sistema.");
		}
	}
}
