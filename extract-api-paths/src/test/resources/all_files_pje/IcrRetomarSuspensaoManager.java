package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrRetomarSuspensao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrRSPManager")
public class IcrRetomarSuspensaoManager extends IcrAssociarIcrManager<IcrRetomarSuspensao>{

	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos(){
		return new TipoIcrEnum[]{TipoIcrEnum.SSP};
	}

	protected String getMensagemDataIcrMenorQueDataSentenca(){
		return "icrRetomarSuspensao.data_retomada_menor_data_suspensao";
	}

	@Override
	protected String[] getFiltrosIcr(){
		String[] filtros = new String[2];
		// foi encerrada
		filtros[0] = ("icr not in(select distinct(o.icrAfetada) from IcrEncerrarSuspensaoProcesso o where o.ativo=true)");
		// já foi retomada
		filtros[1] = ("icr not in(select distinct(o.icrAfetada) from IcrRetomarSuspensao o where o.ativo=true)");
		return filtros;
	}

	@Override
	protected void prePersist(IcrRetomarSuspensao entity) throws IcrValidationException{
		super.prePersist(entity);
		if (entity.getId() != null && verificaEncerramento(entity)){
			throw new IcrValidationException("icrRetomarSuspensao.erroSuspensaoEncerrada");
		}
	}

	@Override
	protected void preInactive(IcrRetomarSuspensao entity) throws IcrValidationException{
		super.preInactive(entity);
		if (verificaEncerramento(entity)){
			throw new IcrValidationException("icrRetomarSuspensao.erroSuspensaoEncerrada");
		}
	}

	private boolean verificaEncerramento(IcrRetomarSuspensao entity){
		return !getEntityManager()
				.createQuery("select o from IcrEncerrarSuspensaoProcesso o where o.ativo=true and o.icrAfetada = :icr")
				.setParameter("icr", entity.getIcrAfetada()).getResultList().isEmpty();
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrRetomarSuspensao entity){
		return entity.getIcrAfetada();
	}

	@Override
	public Date getDtPublicacao(IcrRetomarSuspensao entity){
		return entity.getDataDecisao();
	}

	@Override
	public Boolean possuiDataPublicacao(){
		return false;
	}
}
