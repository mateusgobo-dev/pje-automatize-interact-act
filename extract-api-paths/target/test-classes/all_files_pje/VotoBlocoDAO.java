package br.jus.cnj.pje.business.dao;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.VotoBloco;
import br.jus.pje.nucleo.enums.ContextoVotoEnum;

/**
 * Componente de acesso a dados da entidade {@link BlocoJulgamento}.
 * 
 *
 */
@Name("votoBlocoDAO")
public class VotoBlocoDAO extends BaseDAO<VotoBloco> {

	@Override
	public Object getId(VotoBloco votoBloco) {
		return votoBloco.getIdVotoBloco();
	}
	
	@SuppressWarnings("unchecked")
	public OrgaoJulgador contagemMaioriaVotacao(BlocoJulgamento bloco) {
		StringBuilder str = new StringBuilder();
		str.append("select oj  ");
		str.append("  from VotoBloco votoBloco inner join votoBloco.ojAcompanhado oj ");
		str.append(" where votoBloco.bloco = :bloco ");
		str.append("   group by oj ");
		str.append("   order by count(votoBloco.ojAcompanhado) desc ");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("bloco", bloco);
		List<OrgaoJulgador> ret = q.getResultList();
		OrgaoJulgador ojMaioria = null;
		if (!ret.isEmpty()) {
			ojMaioria = ret.get(0);
		}
		return ojMaioria;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> recuperarOrgaosDivergentes(BlocoJulgamento bloco) {
		StringBuilder str = new StringBuilder();
		str.append("select distinct oj  ");
		str.append("  from VotoBloco votoBloco inner join votoBloco.ojAcompanhado oj ");
		str.append(" where votoBloco.bloco = :bloco ");
		str.append(" and votoBloco.tipoVoto.contexto = :contexto and votoBloco.orgaoJulgador = votoBloco.ojAcompanhado" );
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("bloco", bloco);
		q.setParameter("contexto", (ContextoVotoEnum.D).getContexto());
		return q.getResultList();
	}

}
