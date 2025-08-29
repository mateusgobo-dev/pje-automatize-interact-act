package br.jus.cnj.pje.business.dao;



import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;



/**
 * Componente de acesso a dados da entidade {@link BlocoJulgamento}.
 * 
 *
 */
@Name("processoBlocoDAO")
public class ProcessoBlocoDAO extends BaseDAO<ProcessoBloco> {

	@Override
	public Object getId(ProcessoBloco processoBloco) {
		return processoBloco.getIdProcessoBloco();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> pesquisarProcessosEmBlocos(Sessao sessao) {
		StringBuilder str = new StringBuilder();
		str.append("select processoBloco.processoTrf ");
		str.append("  from ProcessoBloco processoBloco ");
		str.append(" where (processoBloco.bloco.sessao = :sessao or (processoBloco.bloco.sessao <> :sessao and processoBloco.bloco.sessao.dataRealizacaoSessao is null)) ");
		str.append("   and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.bloco.ativo = true ");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("sessao", sessao);
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> pesquisarProcessosPautadosEmBlocos(Sessao sessao) {
		StringBuilder str = new StringBuilder();
		str.append("select processoBloco.processoTrf ");
		str.append("  from ProcessoBloco processoBloco ");
		str.append(" where processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.bloco.ativo = true and (processoBloco.bloco.sessao = :sessao or (processoBloco.bloco.sessao <> :sessao and processoBloco.bloco.sessao.dataRealizacaoSessao is null ");
		str.append("   ");
		str.append(" AND ( ");
		str.append(" EXISTS(");
		str.append(" 				SELECT sessaoPautaProcessoTrf.processoTrf.idProcessoTrf FROM  SessaoPautaProcessoTrf sessaoPautaProcessoTrf");
		str.append(" 				WHERE sessaoPautaProcessoTrf.processoTrf.idProcessoTrf = processoBloco.processoTrf.idProcessoTrf");
		str.append(" 				AND sessaoPautaProcessoTrf.dataExclusaoProcessoTrf IS NULL");
		str.append("				AND sessaoPautaProcessoTrf.sessao.dataRealizacaoSessao IS NULL");
		str.append("				AND sessaoPautaProcessoTrf.sessao.dataExclusao IS NULL");
		str.append("           )");
		str.append("     )))");

		
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("sessao", sessao);
		return q.getResultList();
	}

	
	public long recuperarQuantidadeProcessos(BlocoJulgamento bloco) {
		StringBuilder str = new StringBuilder();
		str.append("select count(processoBloco) ");
		str.append("  from ProcessoBloco processoBloco ");
		str.append(" where processoBloco.bloco = :bloco ");
		str.append("   and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.bloco.ativo = true ");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("bloco", bloco);
		return EntityUtil.getSingleResult(q);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoBloco> recuperarProcessos(BlocoJulgamento bloco) {
		StringBuilder str = new StringBuilder();
		str.append("select processoBloco ");
		str.append("  from ProcessoBloco processoBloco ");
		str.append(" where processoBloco.bloco = :bloco ");
		str.append("   and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.bloco.ativo = true ");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("bloco", bloco);
		return q.getResultList();
	}

	public ProcessoBloco recuperarProcessoBloco(BlocoJulgamento bloco, ProcessoTrf processo) {
		StringBuilder str = new StringBuilder();
		str.append("select processoBloco ");
		str.append("  from ProcessoBloco processoBloco ");
		str.append(" where processoBloco.bloco = :bloco ");
		str.append("   and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.processoTrf = :processo");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("bloco", bloco);
		q.setParameter("processo", processo);
		return EntityUtil.getSingleResult(q);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperaProcessosBlocosNaoPautados(Sessao sessao) {
		StringBuilder str = new StringBuilder();
		str.append("select processoBloco.processoTrf ");
		str.append("  from ProcessoBloco processoBloco ");
		str.append(" where processoBloco.bloco.ativo = true ");
		str.append("   and processoBloco.bloco.sessao = :sessao ");
		str.append("   and processoBloco.ativo = true and processoBloco.dataExclusao is null ");
		str.append("   and processoBloco.processoTrf not in ");
		str.append("(select o.processoTrf from SessaoPautaProcessoTrf o ");
		str.append("where o.dataExclusaoProcessoTrf = null ");
		str.append("and o.sessao = :sessao )");
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("sessao", sessao);
		return q.getResultList();
	}


}
