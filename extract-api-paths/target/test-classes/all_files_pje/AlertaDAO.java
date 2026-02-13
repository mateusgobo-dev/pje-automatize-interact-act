/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Alerta;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

/**
 * Componente de acesso a dados da entidade {@link Alerta}.
 * 
 * @author cristof
 *
 */
@Name("alertaDAO")
public class AlertaDAO extends BaseDAO<Alerta> {

	@Override
	public Integer getId(Alerta al) {
		return al.getIdAlerta();
	}


	/**
	 * Método que recupera um alerta pelo seu texto e criticidade.
	 * Existe um indice único no banco por esses campos, este método permite recuperar o alerta por este 
	 * indice para posteriormente cadastrar processos para este alerta 
	 * @param textoAlerta Texto do alerta
	 * @param criticidade Criticidade do alerta
	 * @return Alerta
	 */
	public Alerta findByTextoECriticidade(String textoAlerta, CriticidadeAlertaEnum criticidade) {
		String queryString = "from Alerta o where o.alerta = :textoAlerta and o.inCriticidade = :criticidade";
		
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("textoAlerta", textoAlerta);
		query.setParameter("criticidade", criticidade);
		try {
			return (Alerta)query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}	

}
