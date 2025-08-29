package br.jus.cnj.pje.business.dao;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoBloco;

/**
 * Componente de acesso a dados da entidade {@link BlocoJulgamento}.
 */
@Name("notaSessaoBlocoDAO")
public class NotaSessaoBlocoDAO extends BaseDAO<NotaSessaoBloco> {

	@Override
	public Object getId(NotaSessaoBloco notaSessaoBloco) {
		return notaSessaoBloco.getIdNotaSessaoBloco();
	}

	@SuppressWarnings("unchecked")
	public List<NotaSessaoBloco> recuperar(BlocoJulgamento bloco) {
		StringBuilder hql = new StringBuilder("SELECT o FROM NotaSessaoBloco o ")
				.append("WHERE o.bloco = :bloco AND o.ativo = true AND o.bloco.ativo = true");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("bloco", bloco);

		return query.getResultList();
	}

}
