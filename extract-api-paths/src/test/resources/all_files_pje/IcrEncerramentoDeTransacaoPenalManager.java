package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CondicaoIcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrEncerramentoDeTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.MotivoEncerramentoTransacaoPenal;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.enums.SituacaoAcompanhamentoIcrTransacaoPenalEnum;

@Name("icrETPManager")
public class IcrEncerramentoDeTransacaoPenalManager extends IcrAssociarIcrManager<IcrEncerramentoDeTransacaoPenal> {
	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return new TipoIcrEnum[] { TipoIcrEnum.TRP };
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrEncerramentoDeTransacaoPenal entity) {
		// TODO Auto-generated method stub
		return entity.getTransacaoPenal();
	}

	@Override
	public Date getDtPublicacao(IcrEncerramentoDeTransacaoPenal entity) {
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return false;
	}

	@Override
	public List<ProcessoParte> recuperarReusNoProcesso(ProcessoTrf processoTrf) {
		return listarReusNoProcessoComTransacao(processoTrf);
	}

	@Override
	public List<InformacaoCriminalRelevante> recuperarIcrPorParteEtipo(ProcessoParte pp) {
		return new ArrayList<InformacaoCriminalRelevante>(listarTransacaoPorParte(pp));
	}

	@Override
	protected void prePersist(IcrEncerramentoDeTransacaoPenal entity) throws IcrValidationException {
		super.prePersist(entity);
		// if (DateUtil.isDataMenor(entity.getData(), entity.getTransacaoPenal()
		// .getData())) {
		// throw new IcrValidationException(
		// "A data do Encerramento da Transação Penal não"
		// + " poderá ser inferior à data da Transação Penal  selecionada.");
		// }
		
		if (possuiCondicaoNaoCumprida(entity)) {
			throw new IcrValidationException("#{IcrEncerramentoDeTransacaoPenal.label_condicao_nao_cumprida}");
		}
	}

	private boolean possuiCondicaoNaoCumprida(IcrEncerramentoDeTransacaoPenal entity) {
		IcrTransacaoPenal transacao = entity.getTransacaoPenal();
		for (CondicaoIcrTransacaoPenal c : transacao.getCondicaoIcrTransacaoList()) {
			
			//Caso exista uma sistuação de acompanhamento
			if (c.getSituacaoAcompanhamentoIcrTransacao() != null) {
				//Se a situação não form "cumprimento" 
				if (!c.getSituacaoAcompanhamentoIcrTransacao().equals(SituacaoAcompanhamentoIcrTransacaoPenalEnum.CUMPR)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void ensureUniqueness(IcrEncerramentoDeTransacaoPenal entity) throws IcrValidationException {
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null) { // PERSIST
			sb.append("SELECT o FROM IcrEncerramentoDeTransacaoPenal" + " o where o.processoParte = :pp ");
		} else if (entity.getId() != null) { // UPDATE
			sb.append("SELECT o FROM IcrEncerramentoDeTransacaoPenal" + " o where o.processoParte = :pp and o.id <> "
					+ entity.getId());
		}
		sb.append(" and o.transacaoPenal.id = :idTransacaoPenal");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pp", entity.getProcessoParte());
		q.setParameter("idTransacaoPenal", entity.getTransacaoPenal().getId());
		if (!q.getResultList().isEmpty()) {
			throw new IcrValidationException("Registro informado já cadastrado no sistema.");
		}
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParte> listarReusNoProcessoComTransacao(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		// sb.append(" select distinct(pp) from ProcessoParte pp ");
		// sb.append(" where pp in(	");
		sb.append(" select distinct(transacao.processoParte) ");
		sb.append("  from  IcrTransacaoPenal transacao  ");
		sb.append("  where transacao.ativo = true ");
		sb.append("  and transacao.processoParte.inParticipacao = 'P'  ");
		sb.append("  and transacao.processoParte.processoTrf = :processoTrf  ");
		sb.append(" and transacao not in (select o.transacaoPenal from IcrEncerramentoDeTransacaoPenal o where o.ativo=true) ");
		// sb.append(" ) 	");
		// sb.append(" order by  pp.pessoa.nome ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrf);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<IcrTransacaoPenal> listarTransacaoPorParte(ProcessoParte pp) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select transacao  from IcrTransacaoPenal transacao ");
		sb.append(" where 	");
		sb.append(" transacao.processoParte.inParticipacao = 'P' ");
		sb.append(" and transacao.ativo = true ");
		sb.append(" and transacao.processoParte =:pp ");
		sb.append(" and transacao not in (");
		sb.append("select o.transacaoPenal from IcrEncerramentoDeTransacaoPenal o where o.ativo=true ");
		sb.append(")");
		sb.append(" order by transacao.data DESC ");
		//
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pp", pp);
		List<IcrTransacaoPenal> result = q.getResultList();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<MotivoEncerramentoTransacaoPenal> listarMotivosEncerramento() {
		Query q = getEntityManager()
				.createQuery("select M from MotivoEncerramentoTransacaoPenal  M where ativo = true");
		return q.getResultList();
	}
}
