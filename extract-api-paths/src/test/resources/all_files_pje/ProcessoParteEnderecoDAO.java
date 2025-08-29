package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;

@Name(ProcessoParteEnderecoDAO.NAME)
public class ProcessoParteEnderecoDAO extends BaseDAO<ProcessoParteEndereco> {
	public static final String NAME = "processoParteEnderecoDAO";

	@Override
	public Object getId(ProcessoParteEndereco e) {
		return e.getIdProcessoParteEndereco();
	}

	@SuppressWarnings("unchecked")
	public ProcessoParteEndereco obter(ProcessoParte processoParte, Endereco endereco) {
		String hql = ""
			+ "from ProcessoParteEndereco as ppe "
			+ "where ppe.processoParte.idProcessoParte = :processoParte "
			+ "and ppe.endereco.idEndereco = :endereco";
		EntityManager entityManager = getEntityManager(); 
		Query query = entityManager.createQuery(hql);
		query.setParameter("processoParte", processoParte.getIdProcessoParte());
		query.setParameter("endereco", endereco.getIdEndereco());
		List<ProcessoParteEndereco> ppeList = query.getResultList();
		if (ProjetoUtil.getTamanho(ppeList) <= 0) {
			return null;
		}
		ProcessoParteEndereco ppe = ppeList.get(0);
		return ppe;
	}
	
	public ProcessoParteEndereco existeProcessoParteEndereco(ProcessoParte processoParte) {
		String hql = ""
			+ "from ProcessoParteEndereco as ppe "
			+ "where ppe.processoParte.idProcessoParte = :processoParte";
		EntityManager entityManager = getEntityManager(); 
		Query query = entityManager.createQuery(hql);
		query.setParameter("processoParte", processoParte.getIdProcessoParte());
		List<ProcessoParteEndereco> ppeList = query.getResultList();
		if (ProjetoUtil.getTamanho(ppeList) <= 0) {
			return null;
		}
		ProcessoParteEndereco ppe = ppeList.get(0);
		return ppe;
	}

	public void apagarEnderecos(ProcessoParte parte) {
		Query qryDelete = getEntityManager().createQuery(" delete from ProcessoParteEndereco o where o.processoParte.idProcessoParte = ? ");
		qryDelete.setParameter(1, parte.getIdProcessoParte());
		qryDelete.executeUpdate();
	}
}
