package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.ProcessoPericia;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("processoPericiaDAO")
@AutoCreate
public class ProcessoPericiaDAO extends BaseDAO<ProcessoPericia> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object getId(ProcessoPericia e) {
		return e.getIdProcessoPericia();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoPericia> recuperarPericias(PessoaPerito perito, List<PericiaStatusEnum> status, Date data) {
		StringBuilder jpql = new StringBuilder("SELECT pp FROM ProcessoPericia pp ")
			.append("WHERE pp.status IN (:status) ")
			.append("AND pp.pessoaPerito.idUsuario = :idPerito ")
			.append("AND pp.dataMarcacao = :dataMarcacao");
		
		Query query = this.entityManager.createQuery(jpql.toString())
			.setParameter("status", status)
			.setParameter("idPerito", perito.getIdUsuario())
			.setParameter("dataMarcacao", DateUtil.getDataSemHora(data));
		
		return query.getResultList();
	}

}
