package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Procuradoria;

@Name("pessoaProcuradorDAO")
public class PessoaProcuradorDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaProcurador>{

	@Override
	public PessoaProcurador especializa(PessoaFisica pessoa) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getId(PessoaProcurador e) {
		return e.getIdUsuario();
	}

	@Override
	public PessoaProcurador desespecializa(PessoaFisica pessoa) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<PessoaProcurador> findByOrgaoRepresentacacao(Procuradoria p){
		String queryStr = "SELECT p FROM PessoaProcurador AS p, PessoaProcuradoria AS pp"
						+ " WHERE p.id = pp.pessoa"
						+ " AND pp.procuradoria = :p ";
		Query q = EntityUtil.getEntityManager().createQuery(queryStr);
		q.setParameter("p", p);
		return q.getResultList();
	}	

}
