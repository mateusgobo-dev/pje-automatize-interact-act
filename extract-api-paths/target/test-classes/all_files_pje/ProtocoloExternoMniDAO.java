package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.ProtocoloExternoMni;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("protocoloExternoMniDAO")
public class ProtocoloExternoMniDAO extends BaseDAO<ProtocoloExternoMni>{

	@Override
	public Object getId(ProtocoloExternoMni e) {
		return e.getIdProtocoloExternoMNI();
	}
	
	public ProtocoloExternoMni findByNumeroIdentificadorSistemaExternoAndProcuradoria(String numeroIdentificadorSistemaExterno, Procuradoria procuradoria){
		String query = "SELECT pe FROM ProtocoloExternoMni pe WHERE pe.numeroIdentificadorSistemaExterno = :numeroIdentificadorSistemaExterno AND pe.procuradoria = :procuradoria";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("numeroIdentificadorSistemaExterno", numeroIdentificadorSistemaExterno);
		q.setParameter("procuradoria", procuradoria);
		try {
			return (ProtocoloExternoMni) q.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}
	
	public ProtocoloExternoMni findByNumeroIdentificadorSistemaExternoAndUsuario(String identificadorSitemaExterno,Usuario usuario) {
		String query = "SELECT pe FROM ProtocoloExternoMni pe WHERE pe.numeroIdentificadorSistemaExterno = :numeroIdentificadorSistemaExterno AND pe.usuario = :usuario";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("numeroIdentificadorSistemaExterno", identificadorSitemaExterno);
		q.setParameter("usuario", usuario);
		try {
			return (ProtocoloExternoMni) q.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}
}
