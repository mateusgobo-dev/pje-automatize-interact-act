package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSuspensaoTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrSTPManager")
public class IcrSuspensaoTransacaoPenalManager extends IcrAssociarIcrManager<IcrSuspensaoTransacaoPenal> {
	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return IcrSuspensaoTransacaoPenal.getTiposDeIcrAceitos();
	}

	@Override
	protected void prePersist(IcrSuspensaoTransacaoPenal entity) throws IcrValidationException {
		super.prePersist(entity);
//		if (entity.getId() != null && verificaEncerramento(entity)) {
//			throw new IcrValidationException("icrSuspensaoTransacaoPenal.erroTransacaoEncerrada");
//		}
//		if (entity.getId() != null && verificaSuspensaoNaoRetomada(entity)) {
//			throw new IcrValidationException("icrSuspensaoTransacaoPenal.erroTransacaoSuspensaENaoRetomada");
//		}
	}

	@Override
	protected void preInactive(IcrSuspensaoTransacaoPenal entity) throws IcrValidationException {
		if (verificaEncerramento(entity)) {
			throw new IcrValidationException("icrSuspensaoTransacaoPenal.erroTransacaoEncerrada");
		}
		if (verificaRetomada(entity)) {
			throw new IcrValidationException("icrSuspensaoTransacaoPenal.erroTransacaoRetomada");
		}
		super.preInactive(entity);
	}

	private boolean verificaSuspensaoNaoRetomada(IcrSuspensaoTransacaoPenal entity) {
		return !getEntityManager()
				.createQuery(
						"select o from IcrSuspensaoTransacaoPenal o where o.ativo=true  and o.transacaoPenal = :transacao and o.id not in(select distinct(ret.suspensaoTransacaoPenal) from IcrRetomarTransacaoPenal ret where ret.ativo=true )")
				.setParameter("transacao", entity.getTransacaoPenal()).getResultList().isEmpty();
	}

	private boolean verificaRetomada(IcrSuspensaoTransacaoPenal entity) {
		return !getEntityManager()
				.createQuery(
						"select o from IcrRetomarTransacaoPenal o where o.ativo=true and o.suspensaoTransacaoPenal = :suspensao")
				.setParameter("suspensao", entity).getResultList().isEmpty();
	}

	private boolean verificaEncerramento(IcrSuspensaoTransacaoPenal entity) {
		return !getEntityManager()
				.createQuery(
						"select o from IcrEncerramentoDeTransacaoPenal o where o.ativo=true and o.transacaoPenal = :transacao")
				.setParameter("transacao", entity.getTransacaoPenal()).getResultList().isEmpty();
	}

	@Override
	protected String[] getFiltrosIcr() {
		String[] filtros = new String[2];
		// foi encerrada
		filtros[0] = ("icr not in(select distinct(o.transacaoPenal) from IcrEncerramentoDeTransacaoPenal o where o.ativo=true )");
		// já foi suspensa e não foi retomada
		filtros[1] = ("icr not in(select distinct(o.transacaoPenal) from IcrSuspensaoTransacaoPenal o where o.ativo=true and o.id not in(select distinct(ret.suspensaoTransacaoPenal) from IcrRetomarTransacaoPenal ret where ret.ativo=true ))");
		return filtros;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return false;
	}

	@Override
	public Date getDtPublicacao(IcrSuspensaoTransacaoPenal entity) {
		return null;
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrSuspensaoTransacaoPenal entity) {
		return entity.getTransacaoPenal();
	}
}
