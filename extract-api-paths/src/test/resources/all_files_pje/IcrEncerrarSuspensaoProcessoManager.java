package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrEncerrarSuspensaoProcesso;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrESPManager")
public class IcrEncerrarSuspensaoProcessoManager extends IcrAssociarIcrManager<IcrEncerrarSuspensaoProcesso>{

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrEncerrarSuspensaoProcesso entity){
		return entity.getIcrAfetada();
	}

	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos(){
		return IcrEncerrarSuspensaoProcesso.getTiposDeIcrAceitos();
	}

	@Override
	public Date getDtPublicacao(IcrEncerrarSuspensaoProcesso entity){
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao(){
		return false;
	}

	@Override
	protected String[] getFiltrosIcr(){
		return new String[]{"icr not in(select distinct(o.icrAfetada) from IcrEncerrarSuspensaoProcesso o where o.ativo=true)"};
	}

	@Override
	protected void ensureUniqueness(IcrEncerrarSuspensaoProcesso entity) throws IcrValidationException{
		StringBuilder sb = new StringBuilder();
		if (entity.getId() == null){ // PERSIST
			sb.append("SELECT o FROM IcrEncerrarSuspensaoProcesso o where o.ativo = true");
		}
		else if (entity.getId() != null){ // UPDATE
			sb.append("SELECT o FROM IcrEncerrarSuspensaoProcesso o where o.ativo = true and o.id <> " + entity.getId());
		}
		sb.append(" and o.processoParte.idProcessoParte =  :idProcessoParte");
		sb.append(" and ");
		sb.append(" (o.data = :dataIcr ");
		sb.append(" or o.dtDecisaoEncerramento = :dataDecisao");
		sb.append(" or o.icrAfetada.id = :icrAfetadaId)");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcessoParte", entity.getProcessoParte().getIdProcessoParte());
		query.setParameter("dataIcr", entity.getData());
		query.setParameter("dataDecisao", entity.getDtDecisaoEncerramento());
		query.setParameter("icrAfetadaId", entity.getIcrAfetada().getId());
		if (!query.getResultList().isEmpty()){
			throw new IcrValidationException("Registro informado já cadastrado no sistema.");
		}
	}
}
