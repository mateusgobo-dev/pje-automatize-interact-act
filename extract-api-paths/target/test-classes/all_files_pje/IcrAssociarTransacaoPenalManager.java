package br.com.infox.cliente.home.icrrefactory;

import java.util.List;

import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public abstract class IcrAssociarTransacaoPenalManager<T extends InformacaoCriminalRelevante> extends
		InformacaoCriminalRelevanteManager<T> {
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> listarReusNoProcessoComTransacao(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select pp from ProcessoParte pp ");
		sb.append(" where pp in(	");
		sb.append(" select  ");
		sb.append("    transacao.processoParte ");
		sb.append("    from  IcrTransacaoPenal transacao  ");
		sb.append("    where transacao.ativo = true and transacao.processoParte.inParticipacao = 'P'  ");
		sb.append(" 	 	 	and transacao.processoParte.processoTrf.idProcessoTrf = :processoTrfId  ");
		sb.append(" and transacao not in (select ETP.transacaoPenal from IcrEncerramentoDeTransacaoPenal ETP) ");
		sb.append(" ) 	");
		sb.append(" order by  pp.pessoa.nome ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrfId", processoTrf.getIdProcessoTrf());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<IcrTransacaoPenal> listarTransacaoPorParte(ProcessoParte pp) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select transacao  from IcrTransacaoPenal transacao ");
		sb.append(" where transacao.processoParte.inParticipacao = 'P' ");
		sb.append(" and transacao.ativo = true ");
		sb.append(" and transacao.processoParte =:pp ");
		sb.append(" and transacao not in (");
		sb.append("select ETP.transacaoPenal from IcrEncerramentoDeTransacaoPenal ETP");
		sb.append(")");
		sb.append(" order by 	 ");
		sb.append(" 	 transacao.data DESC ");
		//
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pp", pp);
		List<IcrTransacaoPenal> result = q.getResultList();
		return result;
	}
}
