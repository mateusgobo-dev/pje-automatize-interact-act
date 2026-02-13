/**
 * EnderecoWsdlDAO.java
 * 
 * Data: 02/05/2016
 */
package br.jus.cnj.pje.business.dao;

import java.util.Collection;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

/**
 * Classe de responsável pela manipulação dos objetos relacionados à entidade
 * EnderecoWsdl.
 * 
 * @author Adriano Pamplona
 */
@Name("enderecoWsdlDAO")
public class EnderecoWsdlDAO extends BaseDAO<EnderecoWsdl> {

	@Override
	public Integer getId(EnderecoWsdl e) {
		return e.getIdEnderecoWsdl();
	}
	
	/**
	 * Consulta os endereços wsdl de todas as instâncias, exceto a instância passada por parâmetro.
	 * 
	 * @param instanciaExcecao
	 * @return coleção de EnderecoWsdl
	 */
	@SuppressWarnings("unchecked")
	public Collection<EnderecoWsdl> consultarEnderecosExceto(String instanciaExcecao) {
		StringBuilder hql = new StringBuilder();
		hql.append("from EnderecoWsdl ");
		hql.append("where ");
		hql.append("	ativo = true and ");
		hql.append("	instancia <> :instancia");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("instancia", instanciaExcecao);
		
		return query.getResultList();
	}

	/**
	 * Retorna o endereço pelos endereços WSDL.
	 * 
	 * @param entity
	 * @return EnderecoWsdl
	 */
	public EnderecoWsdl obterPeloWsdl(EnderecoWsdl enderecoWsdl) {
		String wsdlConsulta = enderecoWsdl.getWsdlConsulta();
		String wsdlIntercomunicacao = enderecoWsdl.getWsdlIntercomunicacao();
		
		StringBuilder hql = new StringBuilder();
		hql.append("from EnderecoWsdl ewsdl ");
		hql.append("where ");
		hql.append("	ewsdl.ativo = true and ");
		hql.append("	ewsdl.wsdlIntercomunicacao = :wsdlIntercomunicacao ");

		if(wsdlConsulta != null){
			hql.append("	and ewsdl.wsdlConsulta = :wsdlConsulta ");	
		}	
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("wsdlIntercomunicacao", wsdlIntercomunicacao);

		if(wsdlConsulta != null){
			query.setParameter("wsdlConsulta", wsdlConsulta);	
		}
		
		
		return getSingleResult(query);
	}
	
	/**
	 * Retorna o endereço pelos endereços WSDL.
	 * 
	 * @param entity
	 * @return EnderecoWsdl
	 */
	public EnderecoWsdl obterPeloWsdlIntercomunicacao(String wsdlIntercomunicacao) {
		
		StringBuilder hql = new StringBuilder();
		hql.append("from EnderecoWsdl ewsdl ");
		hql.append("where ");
		hql.append("	ewsdl.wsdlIntercomunicacao = :wsdlIntercomunicacao ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("wsdlIntercomunicacao", wsdlIntercomunicacao);
		
		return getSingleResult(query);
	}
	
	/**
	 * Consulta todos os endereços wsdl, exceto o endereço wsdl local.
	 * 
	 * @return coleção de EnderecoWsdl
	 */
	@SuppressWarnings("unchecked")
	public Collection<EnderecoWsdl> consultarEnderecosExcetoLocal() {
		int idEnderecoLocal = ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem().getIdEnderecoWsdl();
		
		StringBuilder hql = new StringBuilder();
		hql.append("from EnderecoWsdl e ");
		hql.append("where ");
		hql.append("	e.ativo = true and ");
		hql.append("	e.idEnderecoWsdl <> :idEnderecoLocal");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("idEnderecoLocal", idEnderecoLocal);
		
		return query.getResultList();
	}
}
