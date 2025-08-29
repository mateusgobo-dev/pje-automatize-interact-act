package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrDecisaoSuperiorSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrSCSManager")
public class IcrDecisaoSuperiorSentencaCondenatoriaManager extends
IcrDecisaoSuperiorManager<IcrDecisaoSuperiorSentencaCondenatoria> {
	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return IcrDecisaoSuperiorSentencaCondenatoria.getTiposDeIcrAceitos();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return true;
	}

	@Override
	public Date getDtPublicacao(IcrDecisaoSuperiorSentencaCondenatoria entity) {
		return entity.getDataPublicacao();
	}

	@Override
	public InformacaoCriminalRelevante getIcrAfetada(IcrDecisaoSuperiorSentencaCondenatoria entity) {
		return entity.getIcrAfetada();
	}

	@Override
	protected void ensureUniqueness(IcrDecisaoSuperiorSentencaCondenatoria entity) throws IcrValidationException {
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null) { // PERSIST
			sb.append("SELECT o FROM IcrDecisaoSuperiorSentencaCondenatoria o where o.ativo = true");
		} else if (entity.getId() != null) { // UPDATE
			sb.append("SELECT o FROM IcrDecisaoSuperiorSentencaCondenatoria o where o.ativo = true and o.id <> "
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
