package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrDesclassificacao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("icrDESManager")
public class IcrDesclassificacaoManager extends InformacaoCriminalRelevanteManager<IcrDesclassificacao> {
	@SuppressWarnings("unchecked")
	@Override
	public void prePersist(IcrDesclassificacao entity) throws IcrValidationException {
		super.prePersist(entity);
		ProcessoTrf processoTrf = InformacaoCriminalRelevanteHome.getHomeInstance().getProcessoTrf();
		List<InformacaoCriminalRelevante> listaIcr;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT icr FROM InformacaoCriminalRelevante icr INNER JOIN icr.processoParte pp ");
		sql.append(" WHERE icr.ativo = true");
		sql.append(" and icr.tipo = 'SPR'");
		sql.append(" and pp.processoTrf = :processoTrf");
		sql.append(" order by icr.data");
		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("processoTrf", processoTrf);
		listaIcr = query.getResultList();
		if (listaIcr == null || listaIcr.size() == 0) {
			throw new IcrValidationException("O réu não possui "
					+ "sentença de pronúncia, pré-requesito deste cadastro. Favor cadastra-la.");
		}
	}

	@Override
	public Date getDtPublicacao(IcrDesclassificacao entity) {
		// TODO Auto-generated method stub
		return entity.getDataPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
