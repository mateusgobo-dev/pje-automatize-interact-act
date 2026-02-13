package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSuspenderSuspensao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrSSPManager")
public class IcrSuspenderSuspensaoManager extends IcrAssociarIcrManager<IcrSuspenderSuspensao> {
	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return new TipoIcrEnum[] { TipoIcrEnum.SUS };
	}
	
	@Override
	protected String getMensagemDataIcrMenorQueDataSentenca(){
		return "icrSuspenderSuspensao.dataDaIcrMaiorQueADaSentenca";
	}	

	@Override
	protected String[] getFiltrosIcr() {
		String[] filtros = new String[2];
		// foi encerrada
		filtros[0] = ("icr not in(select distinct(o.icrAfetada) from IcrEncerrarSuspensaoProcesso o where o.ativo=true)");
		// já foi suspensa e não foi retomada
		filtros[1] = ("icr not in(select distinct(o.icrAfetada) from IcrSuspenderSuspensao o where o.ativo=true and o.id not in(select distinct(ret.icrAfetada) from IcrRetomarSuspensao ret where ret.ativo=true ))");
		return filtros;
	}

	@Override
	protected void prePersist(IcrSuspenderSuspensao entity) throws IcrValidationException {
		super.prePersist(entity);
		if (entity.getId() != null && verificaEncerramento(entity)) {
			throw new IcrValidationException("icrSuspenderSuspensao.erroSuspensaoEncerrada");
		}
		if (entity.getId() != null && !verificaSuspensaoNaoRetomada(entity)) {
			throw new IcrValidationException("icrSuspenderSuspensao.erroSuspensaoSuspensaENaoRetomada");
		}
	}

	@Override
	protected void preInactive(IcrSuspenderSuspensao entity) throws IcrValidationException {
		super.preInactive(entity);
		if (verificaEncerramento(entity)) {
			throw new IcrValidationException("icrSuspenderSuspensao.erroSuspensaoEncerrada");
		}
		if (verificaRetomada(entity)) {
			throw new IcrValidationException("icrSuspenderSuspensao.erroSuspensaoRetomada");
		}
	}

	private boolean verificaRetomada(IcrSuspenderSuspensao entity) {
		return !getEntityManager()
				.createQuery("select o from IcrRetomarSuspensao o where o.ativo=true and o.icrAfetada = :icr")
				.setParameter("icr", entity).getResultList().isEmpty();
	}

	private boolean verificaEncerramento(IcrSuspenderSuspensao entity) {
		return !getEntityManager()
				.createQuery("select o from IcrEncerrarSuspensaoProcesso o where o.ativo=true and o.icrAfetada = :icr")
				.setParameter("icr", entity.getIcrAfetada()).getResultList().isEmpty();
	}

	private boolean verificaSuspensaoNaoRetomada(IcrSuspenderSuspensao entity) {
		return !getEntityManager()
				.createQuery(
						"select o from IcrSuspenderSuspensao o where o.ativo=true  and o.icrAfetada = :icr and o.id not in(select distinct(ret.icrAfetada) from IcrRetomarSuspensao ret where ret.ativo=true )")
				.setParameter("icr", entity.getIcrAfetada()).getResultList().isEmpty();
	}

	@Override
	protected void ensureUniqueness(IcrSuspenderSuspensao entity) throws IcrValidationException {
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null) { // PERSIST
			sb.append("SELECT o FROM IcrSuspenderSuspensao o where o.ativo = true");
		} else if (entity.getId() != null) { // UPDATE
			sb.append("SELECT o FROM IcrSuspenderSuspensao o where o.ativo = true and o.id <> " + entity.getId());
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

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrSuspenderSuspensao entity) {
		return entity.getIcrAfetada();
	}

	@Override
	public Date getDtPublicacao(IcrSuspenderSuspensao entity) {
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return false;
	}
}
