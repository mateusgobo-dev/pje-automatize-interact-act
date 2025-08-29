/**
 * 
 */
package br.jus.cnj.pje.business.dao;


import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;


import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name(BlocoComposicaoDAO.NAME)
public class BlocoComposicaoDAO extends BaseDAO<BlocoComposicao> {

	public static final String NAME = "blocoComposicaoDAO";

	@Override
	public Integer getId(BlocoComposicao e) {
		return e.getIdBlocoComposicao();
	}

	@SuppressWarnings("unchecked")
	public List<BlocoComposicao> findByBlocoPresentes(BlocoJulgamento bloco){
		
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" select blocos from BlocoComposicao blocos ");
		sbQuery.append(" where blocos.bloco.idBlocoJulgamento = :bloco and blocos.presente = true ");
		sbQuery.append(" order by blocos.tipoAtuacaoMagistrado, blocos.magistradoPresente.nome ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());   
		query.setParameter("bloco", bloco.getIdBlocoJulgamento());
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<BlocoComposicao> findByBloco(BlocoJulgamento bloco){
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" select blocos from BlocoComposicao blocos ");
		sbQuery.append(" where blocos.bloco.idBlocoJulgamento = :bloco ");
		sbQuery.append(" order by blocos.tipoAtuacaoMagistrado, blocos.magistradoPresente.nome ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());   
		query.setParameter("bloco", bloco.getIdBlocoJulgamento());
		return query.getResultList();
	}

	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> recuperarOrgaoJulgadorPorBloco(BlocoJulgamento bloco){
		
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" select blocos.orgaoJulgador from BlocoComposicao blocos ");
		sbQuery.append(" where blocos.bloco.idBlocoJulgamento = :bloco and blocos.presente = true ");
		sbQuery.append(" order by blocos.tipoAtuacaoMagistrado, blocos.magistradoPresente.nome ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());   
		query.setParameter("bloco", bloco.getIdBlocoJulgamento());
		return query.getResultList();
	}

}
