package br.jus.cnj.pje.business.dao;


import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;


/**
 * Componente de acesso a dados da entidade {@link BlocoJulgamento}.
 * 
 *
 */
@Name("blocoJulgamentoDAO")
public class BlocoJulgamentoDAO extends BaseDAO<BlocoJulgamento> {

	@Override
	public Object getId(BlocoJulgamento bloco) {
		return bloco.getIdBlocoJulgamento();
	}
	
	@SuppressWarnings("unchecked")
	public List<BlocoJulgamento> recuperarBlocosComProcessos(Sessao sessao, boolean especificarJulgados, boolean julgados) {
		StringBuilder strBlocos = new StringBuilder();
		strBlocos.append("select blocos from BlocoJulgamento blocos where blocos.sessao = :sessao and blocos.ativo = true and exists (select processoBloco from ProcessoBloco processoBloco " );
		strBlocos.append("where processoBloco.bloco = blocos and processoBloco.ativo = true and exists ");
		strBlocos.append("(select sessaoPauta from SessaoPautaProcessoTrf sessaoPauta where sessaoPauta.processoTrf = processoBloco.processoTrf and sessaoPauta.sessao = blocos.sessao )) ");
		if(especificarJulgados) {
			if(julgados) {
				strBlocos.append("AND blocos.situacaoJulgamento in('JG','NJ','AD') ");
			} else {
				strBlocos.append("AND blocos.situacaoJulgamento not in ('JG', 'NJ','AD') ");
			}
		}
		Query queryBlocos = getEntityManager().createQuery(strBlocos.toString());
		queryBlocos.setParameter("sessao", sessao);
		return queryBlocos.getResultList();		
	}
	
	@SuppressWarnings("unchecked")
	public List<BlocoJulgamento> findBySessao(Sessao sessao) {
		StringBuilder strBlocos = new StringBuilder();
		strBlocos.append("select blocos ");
		strBlocos.append("  from BlocoJulgamento blocos ");
		strBlocos.append(" where blocos.sessao = :sessao ");
		strBlocos.append("   and blocos.ativo = true ");
		Query queryBlocos = getEntityManager().createQuery(strBlocos.toString());
		queryBlocos.setParameter("sessao", sessao);
		return queryBlocos.getResultList();		
	}
	
	public BlocoJulgamento findByNome(String nome, Sessao sessao) {
		StringBuilder strBlocos = new StringBuilder();
		strBlocos.append("select bloco ");
		strBlocos.append("  from BlocoJulgamento bloco ");
		strBlocos.append(" where bloco.blocoJulgamento = :nome ");
		strBlocos.append("   and bloco.ativo = true ");
		strBlocos.append("   and bloco.sessao = :sessao ");
		Query queryBlocos = getEntityManager().createQuery(strBlocos.toString());
		queryBlocos.setParameter("nome", nome);
		queryBlocos.setParameter("sessao", sessao);
		return EntityUtil.getSingleResult(queryBlocos);
	}

	
	public BlocoJulgamento pesquisar(ProcessoTrf processo, Sessao sessao) {
		StringBuilder strBlocos = new StringBuilder();
		strBlocos.append("select processoBloco.bloco ");
		strBlocos.append("  from ProcessoBloco processoBloco ");
		strBlocos.append(" where processoBloco.processoTrf = :processo ");
		strBlocos.append("   and processoBloco.bloco.sessao = :sessao ");
		strBlocos.append("   and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		strBlocos.append("   and processoBloco.bloco.ativo = true ");
		Query queryBlocos = getEntityManager().createQuery(strBlocos.toString());
		queryBlocos.setParameter("processo", processo);
		queryBlocos.setParameter("sessao", sessao);
		BlocoJulgamento singleResult = EntityUtil.getSingleResult(queryBlocos);
		return singleResult;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> recuperarDocumentosParaAssinatura(BlocoJulgamento bloco) {
		StringBuilder str = new StringBuilder();
		str.append("select processoDocumento ");
		str.append("  from ProcessoBloco processoBloco inner join processoBloco.processoDocumento processoDocumento ");
		str.append(" where processoBloco.bloco = :bloco and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.bloco.ativo = true ");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("bloco", bloco);
		return q.getResultList();
	}
}
