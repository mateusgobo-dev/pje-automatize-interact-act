/**
 * ProcessoTrfManifestacaoProcessualDAO.java
 * 
 * Data: 17/05/2016
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;

/**
 * Classe de responsável pela manipulação dos objetos relacionados à entidade
 * ProcessoTrfManifestacaoProcessual.
 * 
 * @author Adriano Pamplona
 */
@Name("processoTrfManifestacaoProcessualDAO")
public class ProcessoTrfManifestacaoProcessualDAO extends BaseDAO<ProcessoTrfManifestacaoProcessual> {

	@Override
	public Integer getId(ProcessoTrfManifestacaoProcessual e) {
		return e.getId();
	}

	/**
	 * Retorna o último registro inserido do processo passado por parâmetro. O registro refere-se
	 * à ultima remessa/retorno.
	 * 
	 * @param processo
	 * @return ProcessoTrfManifestacaoProcessual
	 */
	public ProcessoTrfManifestacaoProcessual obterUltimo(ProcessoTrf processo) {
		StringBuilder hql = new StringBuilder();
		hql.append("from ProcessoTrfManifestacaoProcessual ");
		hql.append("where ");
		hql.append("	processoTrf = :processoTrf ");
		hql.append("order by id desc");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setMaxResults(1);
		query.setParameter("processoTrf", processo);
		
		return getSingleResult(query);
		
	}

	/**
	 * Retorna true se o registro existir na base.
	 * 
	 * @param entity
	 * @return booleano
	 */
	public Boolean isExiste(ProcessoTrfManifestacaoProcessual entity) {
		EnderecoWsdl enderecoWsdl = entity.getEnderecoWsdl();
		String wsdlConsulta = enderecoWsdl.getWsdlConsulta();
		String wsdlIntercomunicacao = enderecoWsdl.getWsdlIntercomunicacao();
		
		StringBuilder hql = new StringBuilder();
		hql.append("select count(pmp) ");
		hql.append("from ProcessoTrfManifestacaoProcessual pmp ");
		hql.append("	left join pmp.enderecoWsdl ewsdl ");
		hql.append("where ");
		hql.append("	pmp.processoTrf = :processoTrf ");
		hql.append("	and pmp.numeroProcessoManifestacao = :numeroProcessoManifestacao ");
		hql.append("	and ewsdl.wsdlIntercomunicacao = :wsdlIntercomunicacao ");
		hql.append("	and ewsdl.wsdlConsulta = :wsdlConsulta ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("processoTrf", entity.getProcessoTrf());
		query.setParameter("numeroProcessoManifestacao", entity.getNumeroProcessoManifestacao());
		query.setParameter("wsdlIntercomunicacao", wsdlIntercomunicacao);
		query.setParameter("wsdlConsulta", wsdlConsulta);
		
		Number total = (Number) query.getSingleResult();
		return (total != null && total.intValue() > 0);
	}
}
