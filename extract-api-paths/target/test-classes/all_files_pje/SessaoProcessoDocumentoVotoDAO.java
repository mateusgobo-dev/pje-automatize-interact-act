package br.jus.cnj.pje.business.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.OrdenarDocumentosVotoProcessoSessaoVO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.enums.ContextoVotoEnum;
import br.jus.pje.nucleo.enums.TipoVotoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import java.util.Date;
import java.util.Optional;
import javax.persistence.TypedQuery;

/**
 * Componente de acesso a dados da entidade {@link SessaoProcessoDocumentoVoto}.
 * 
 * @author cristof
 *
 */
@Name("sessaoProcessoDocumentoVotoDAO")
public class SessaoProcessoDocumentoVotoDAO extends BaseDAO<SessaoProcessoDocumentoVoto> {

	@Override
	public Object getId(SessaoProcessoDocumentoVoto e) {
		return e.getIdSessaoProcessoDocumento();
	}
	
	public Long qtdeVotoSessaoPorTipoProcesso(Sessao sessao, TipoVotoEnum tipoVoto, OrgaoJulgador oj, Processo processo) {
		String query = "select COUNT(spd.idSessaoProcessoDocumento) from SessaoProcessoDocumentoVoto spd " +
				"	where spd.sessao = :sessao " +
				"		and spd.tipoVoto.contexto=:contexto " +
				"		and spd.orgaoJulgador <> :orgaoJulgador" +
				"		and spd.processoTrf.idProcessoTrf  = :processo";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("contexto", tipoVoto.toString());
		q.setParameter("sessao", sessao);
		q.setParameter("orgaoJulgador", oj);
		q.setParameter("processo", processo.getIdProcesso());
		q.setMaxResults(1);
		Number ret = (Number) q.getSingleResult();
		return ret.longValue();
	}
	
	/**
	 * 
	 * @param sessao
	 * @param processo
	 * @param somenteLiberados
	 * @return recupera os votos proferidos para um processo em uma sessão, considerando apenas o último voto do orgão julgador,
	 * caso haja mais de um voto.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumentoVoto> votosProferidosSessao(Sessao sessao, ProcessoTrf processo, boolean somenteLiberados){
		StringBuilder hql = new StringBuilder(1000)
			.append("SELECT votoSessao FROM SessaoProcessoDocumentoVoto votoSessao ")
			.append("WHERE votoSessao.idSessaoProcessoDocumento in ")
			.append("(	SELECT MAX(voto.idSessaoProcessoDocumento) FROM SessaoProcessoDocumentoVoto voto ")
			.append(" WHERE voto.tipoVoto is not null ");
		
		if (processo!=null)
			hql.append("	AND voto.processoTrf.idProcessoTrf = :processoTrf ");
		
		if(sessao != null)
			hql.append("	AND voto.sessao = :sessao ");
				
		if(somenteLiberados)
			hql.append("AND (voto.liberacao is true OR voto.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador) ");
		
		hql.append("	GROUP BY voto.orgaoJulgador, voto.processoTrf ")
			.append(") ")
			.append(" ORDER BY votoSessao.idSessaoProcessoDocumento DESC ");
		
		Query query = EntityUtil.createQuery(getEntityManager(), hql, false, true, "SessaoProcessoDocumentoVotoDAO.votosProferidosSessao");
		
		if (processo!=null)
			query.setParameter("processoTrf", processo.getIdProcessoTrf());
		
		if(sessao != null)
			query.setParameter("sessao", sessao);
		
		if(somenteLiberados)
			query.setParameter("idOrgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual());
		
		List<SessaoProcessoDocumentoVoto> resultado;

		resultado = (List<SessaoProcessoDocumentoVoto>) query.getResultList();

		return resultado;
	}
	
	public SessaoProcessoDocumentoVoto recuperarVotoDoRelator(SessaoPautaProcessoTrf sppt) {

		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumentoVoto o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.processo.idProcesso = :processo ");
		sb.append("and o.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.processoDocumento.ativo = true ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sppt.getSessao());
		q.setParameter("processo", sppt.getProcessoTrf().getIdProcessoTrf());
		q.setParameter("orgaoJulgador", sppt.getProcessoTrf().getOrgaoJulgador());

		return (SessaoProcessoDocumentoVoto) EntityUtil.getSingleResult(q);		
	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumentoVoto> recuperarSessaoProcessoDocumentosVotosSemSessaoDefinida(ProcessoTrf processoTrf) {
		
		// Atualiza a sessao dos votos que nao possuem documentos  
		StringBuilder udtVoto = new StringBuilder();
		udtVoto.append("select spdv ");
		udtVoto.append("  from SessaoProcessoDocumentoVoto spdv ");
		udtVoto.append(" where spdv.sessao is null ");
		udtVoto.append("   and spdv.processoTrf = :processoTrf ");
		udtVoto.append("   and spdv.processoDocumento is null ");
				
		Query queryVoto = getEntityManager().createQuery(udtVoto.toString());
		queryVoto.setParameter("processoTrf", processoTrf);

		return queryVoto.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumentoVoto> recuperarSessaoVotosComDocumentosPorSessaoEhProcesso(Sessao sessao, ProcessoTrf processoTrf) {
		
		StringBuilder jpql = new StringBuilder();
		jpql.append(" select spdv ")
			.append("   from SessaoProcessoDocumentoVoto spdv ")
			.append("   join spdv.processoDocumento pd ")
			.append("  where spdv.sessao = :sessao ")
			.append("    and spdv.processoTrf = :processoTrf ");

		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("processoTrf", processoTrf);
		
		return query.getResultList();		
	}
	
	
	/**
	 * Recupera o voto de acordo com o argumento informado.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}
	 * @return Voto do processo.
	 */
	public SessaoProcessoDocumentoVoto recuperarVoto(ProcessoDocumento processoDocumento) {
		List<SessaoProcessoDocumentoVoto> result = null;
		Search search = new Search(SessaoProcessoDocumentoVoto.class);
		try {
			search.addCriteria(Criteria.equals("processoDocumento", processoDocumento));
			result = list(search);
		} catch (NoSuchFieldException e) {
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return !result.isEmpty() ? result.get(0) : null;
	}
	
	/**
	 * Recupera o voto de acordo com o argumento informado.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}
	 * @return Voto do processo.
	 */
	public SessaoProcessoDocumentoVoto recuperarVoto(ProcessoDocumento processoDocumento, boolean apenasVoto) {
		
		StringBuilder jpql = new StringBuilder();
		jpql.append(" select spdv ")
			.append("   from SessaoProcessoDocumentoVoto spdv ")
			.append("  where spdv.processoDocumento = :processoDocumento ");
			
		if (apenasVoto){
			jpql.append(" and spdv.tipoVoto.idTipoVoto is not null");
		}
		
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("processoDocumento", processoDocumento);
		query.setMaxResults(1);
		return EntityUtil.getSingleResult(query);
	}
	
	/**
	 * Recupera lista com votos de acordo com o argumento informado.
	 * @author rafaelmatos
	 * @param sessao {@link Sessao}
	 * @param processoTrf {@link ProcessoTrf}
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @since 09/06/2015
	 * @return lista com votos.
	 */
	@SuppressWarnings("unchecked")
	public List<OrdenarDocumentosVotoProcessoSessaoVO> recuperarSessaoVotosComDocumentosPorSessaoEhProcessoVO(Sessao sessao, ProcessoTrf processoTrf) {
		
		StringBuilder jpql = new StringBuilder();
		
		jpql.append(" select new br.jus.cnj.pje.entidades.vo.OrdenarDocumentosVotoProcessoSessaoVO(")
			.append(" m.id, spdv.idSessaoProcessoDocumento,tpd.tipoProcessoDocumento || ' ' || tv.tipoVoto,ojp.orgaoJulgador, ")
			.append(" oj.orgaoJulgador,spdv.dtVoto,m.ordemDocumento,pd.processoDocumentoBin)  ")
			.append("   from SessaoProcessoMultDocsVoto m ")
			.append("   join m.sessaoProcessoDocumentoVoto spdv ")
			.append("   join spdv.processoDocumento pd ")
			.append("   join pd.tipoProcessoDocumento tpd ")
			.append("   join spdv.tipoVoto tv ")
			.append("   join spdv.orgaoJulgador oj ")
			.append("   join spdv.processoTrf p ")
			.append("   left join p.orgaoJulgador ojp ")
			.append("  where spdv.sessao = :sessao ")
			.append("    and spdv.processoTrf = :processoTrf ")
			.append("    order by m.ordemDocumento asc ");

		Query query = getEntityManager().createQuery(jpql.toString()) ;
		query.setParameter("sessao", sessao);
		query.setParameter("processoTrf", processoTrf);
		
		return query.getResultList();
	}
	
	/**
	 * Metodo que atualiza a ordem do voto.
	 * @author rafaelmatos
	 * @param id da tabela SessaoProcessoMultDocsVoto
	 * @param ordem nova ordem do voto
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @since 09/06/2015
	 */
	public void atualizaOrdemVoto(Integer id, Integer ordem){
		
		String sql = "update SessaoProcessoMultDocsVoto set ordemDocumento = :ordem "
				+ " where id = :id ";
		
		getEntityManager().createQuery(sql)
				.setParameter("ordem", ordem)
				.setParameter("id", id)
				.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumentoVoto> recuperarSessaoVotosPorSessaoEhProcessos(Sessao sessao, List<ProcessoTrf> processos) {
		Set<Integer> ids = new HashSet<Integer>(processos.size());
		for(ProcessoTrf p: processos){
			ids.add(p.getIdProcessoTrf());
		}
		
		StringBuilder jpql = new StringBuilder();
		jpql.append("select spdv ")
			.append("from SessaoProcessoDocumentoVoto spdv ")
			.append("where spdv.sessao = :sessao ")
			.append("and spdv.processoTrf.idProcessoTrf in (:idsProcessos) ");

		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("idsProcessos", ids);
		
		List<SessaoProcessoDocumentoVoto> lista = query.getResultList();
		if (lista == null || lista.isEmpty()) { 
			return Collections.emptyList();
		}
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public OrgaoJulgador contagemMaioriaVotacao(Sessao sessao, ProcessoTrf processo) {
		StringBuilder jpql = new StringBuilder();
		jpql.append(" select oj ")
			.append(" from SessaoProcessoDocumentoVoto spdv ")
			.append(" join spdv.ojAcompanhado oj ")
			.append(" where spdv.sessao = :sessao ")
	
			.append("    and spdv.processoTrf = :processoTrf ")
		    .append(" group by oj")
	    	.append(" order by count(spdv.ojAcompanhado) desc");

		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("processoTrf", processo);		
		
		List<OrgaoJulgador> ojList = query.getResultList();
		OrgaoJulgador ojMaioria = null;
		
		if (!query.getResultList().isEmpty()) {
			ojMaioria = ojList.get(0);
		}
				
		return ojMaioria;
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os SessaoProcessoDocumentoVoto que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento) {
		String query = "DELETE FROM SessaoProcessoDocumentoVoto s  WHERE s.processoDocumento.idProcessoDocumento = :id";
		getEntityManager().createQuery(query)
			.setParameter("id", idProcessoDocumento)
			.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> recuperarOrgaosDivergentes(SessaoPautaProcessoTrf sessaoPauta) {
		StringBuilder str = new StringBuilder();
		str.append("select distinct oj  ");
		str.append("  from SessaoProcessoDocumentoVoto voto inner join voto.ojAcompanhado oj ");
		str.append(" where voto.processoTrf = :processo and voto.sessao = :sessao ");
		str.append(" and voto.tipoVoto.contexto = :contexto and voto.orgaoJulgador = voto.ojAcompanhado" );
		Query q = getEntityManager().createQuery(str.toString());
		q.setParameter("processo", sessaoPauta.getProcessoTrf());
		q.setParameter("sessao", sessaoPauta.getSessao());
		q.setParameter("contexto", (ContextoVotoEnum.D).getContexto());
		return q.getResultList();
	}

	public SessaoProcessoDocumentoVoto recuperarVoto(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		StringBuilder str = new StringBuilder(1000)
			.append("select voto from SessaoProcessoDocumentoVoto voto ")
			.append(" where voto.processoTrf = :processo and voto.sessao = :sessao ")
			.append("   and voto.orgaoJulgador = :orgaoJulgador " );
		
		return EntityUtil.getSingleResult(
				getEntityManager().createQuery(str.toString())
			.setParameter("processo", processoTrf)
			.setParameter("sessao", sessao)
			.setParameter("orgaoJulgador", orgaoJulgador)
		);		
	}

	@SuppressWarnings("unchecked")
	public SessaoProcessoDocumentoVoto recuperarVoto(Date ultimaDataJulgamento, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		StringBuilder hql = new StringBuilder(1000)
			.append("select voto ")
			.append(" from SessaoProcessoDocumentoVoto voto ")
			.append(" left join voto.sessao s ")
			.append(" where voto.processoTrf = :processo ")
			.append("   and voto.orgaoJulgador = :orgaoJulgador " )
			.append("    and ((s is null) or (s.dataSessao > :ultimaDataJulgamento)) ")
			.append("  order by s.dataSessao desc, voto.idSessaoProcessoDocumento desc ");

		List<SessaoProcessoDocumentoVoto> lista = getEntityManager().createQuery(hql.toString())
				.setParameter("processo", processoTrf)
				.setParameter("orgaoJulgador", orgaoJulgador)
				.setParameter("ultimaDataJulgamento", ultimaDataJulgamento)
				.getResultList();
		
		Optional<SessaoProcessoDocumentoVoto> optSemSessao = lista.stream().filter(spd->spd.getSessao()==null).findAny();
		if (optSemSessao.isPresent())
			return optSemSessao.get();
		
		return lista.isEmpty() ? null : lista.get(0);
	}

}
