package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.MandadoAlvara;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

//@Name(MandadoAlvaraDAO.NAME)
public abstract class MandadoAlvaraDAO<E extends MandadoAlvara> extends ProcessoExpedienteCriminalDAO<E>{

	//public static final String NAME = "mandadoAlvaraDAO";

	@SuppressWarnings("unchecked")
	public InformacaoCriminalRelevante recuperarUltimaIcrComDelitos(Pessoa pessoa, ProcessoTrf processoTrf){
		String hql = " select distinct o from InformacaoCriminalRelevante o "
			+ " inner join o.tipificacoes t "
			+ " where o.processoParte.pessoa.idUsuario = :idUsuario "
			+ " and o.processoParte.processoTrf.idProcessoTrf = :idProcessoTrf "
			+ " order by o.data desc ";

		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter("idUsuario", pessoa.getIdUsuario());
		qry.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());

		List<InformacaoCriminalRelevante> result = qry.getResultList();
		if (result != null && !result.isEmpty()){
			return result.get(0);
		}

		return null;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<ProcessoEvento> getMovimentacoesNaoVinculadas(MandadoAlvara mandadoAlvara, Date dataInicio, Date dataFim,
			Evento movimentacaoSelecionada){
		StringBuilder sql = new StringBuilder(1000);
		sql.append(" select distinct pe ");
		sql.append(" from 	ProcessoEvento pe ");
		sql.append(" 		inner join pe.evento ev ");
		sql.append(" 		inner join pe.processoDocumento pd ");
		sql.append(" 		inner join pd.tipoProcessoDocumento dpd ");
		sql.append(" where ");
		if (dataInicio != null && dataFim != null){
			if (dataFim.before(dataInicio)){
				throw new PJeDAOException("pje.mandadoAlvaraDAO.error.periodoInvalido");
			}
			else{
				sql.append(" ( ");
				sql.append(" 		pe.dataAtualizacao >= '" + dataInicio + "' ");
				sql.append(" 		and ");
				sql.append(" 		pe.dataAtualizacao <= '" + dataFim.toString().replace("00:00:00", "23:59:59") + "' ");
				sql.append(" ) ");
				sql.append(" 		and ");
			}
		}
		if (movimentacaoSelecionada != null && !movimentacaoSelecionada.getMovimento().equals("")){
			sql.append(" 		lower(pe.evento.evento) like lower('%" + movimentacaoSelecionada.toString() + "%') ");
			sql.append(" 		and ");
		}
		sql.append(" 		pe.processo.idProcesso = " + mandadoAlvara.getProcessoTrf().getProcesso().getIdProcesso());
		
		sql.append(" and pe.idProcessoEvento not in (select pEvento.idProcessoEvento " +
				   "                                 from MandadoAlvara ma " +
				   "                                 inner join ma.processoEventoList pEvento "+
				   "                                 where ma.id = "+mandadoAlvara.getId()+") ");
		
		sql.append("order by pe.dataAtualizacao DESC");
		Query q = getEntityManager().createQuery(sql.toString());
		return q.getResultList();
	}
}
