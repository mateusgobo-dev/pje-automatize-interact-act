package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import java.util.Date;
import java.util.Optional;
import javax.persistence.NoResultException;

@Name("sessaoProcessoDocumentoDAO")
public class SessaoProcessoDocumentoDAO extends BaseDAO<SessaoProcessoDocumento> {

	@Override
	public Integer getId(SessaoProcessoDocumento e) {
		return e.getIdSessaoProcessoDocumento();
	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> listSessaoProcessoDocumentoAtivoByTipoAndOrgaoJulgador(Sessao sessao,
			TipoProcessoDocumento tipoProcessoDocumento, OrgaoJulgador oj, List<Processo> processos) {
		String query = "SELECT spd FROM SessaoProcessoDocumento AS spd INNER JOIN spd.processoDocumento AS pd "
				+ "	WHERE spd.sessao = :sessao "
				+ "		AND spd.orgaoJulgador = :oj "
				+ "		AND pd.processo IN :procs "
				+ "		AND pd.dataExclusao IS NULL "
				+ "		AND pd.ativo = true "
				+ "		AND pd.tipoProcessoDocumento = :tipo"
				+ " order by spd.idSessaoProcessoDocumento desc";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("sessao", sessao);
		q.setParameter("tipo", tipoProcessoDocumento);
		q.setParameter("oj", oj);
		q.setParameter("procs", processos);
		List<SessaoProcessoDocumento> namedResultList = new ArrayList<SessaoProcessoDocumento>();
		namedResultList = q.getResultList();
		return namedResultList;
	}

    /**
     * [PJEII-9886]
     * Método verifica na lista de processos recebida como parâmetro quais
     * possuem documentos de sessão elaborados
     * 
     * @param listProcessos Lista de Processos a ser verificada
     * @return Lista de Processos que contêm documentos de sessão
     */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> listProcessosComDocumentosSessao(List<ProcessoTrf> listProcessos) {
		Set<Integer> ids = new HashSet<Integer>(listProcessos.size());
		for(ProcessoTrf p: listProcessos){
			ids.add(p.getIdProcessoTrf());
		}
		String query = "SELECT DISTINCT p FROM ProcessoTrf AS p, SessaoProcessoDocumento AS o "
				+ "	WHERE o.processoDocumento.ativo = true "
				+ "		AND o.processoDocumento.processo.idProcesso = p.idProcessoTrf "
				+ "		AND o.processoDocumento.processo.idProcesso IN (:idsProcessos)";
		Query q = getEntityManager().createQuery(query);
        q.setParameter("idsProcessos", ids);
		List<ProcessoTrf> lista = q.getResultList();
		if (lista == null || lista.isEmpty()) 
			return Collections.emptyList();
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> listProcessosComDocumentoPorTipo(List<ProcessoTrf> listProcessos, Integer idTipoProcessoDocumento) {
		Set<Integer> ids = new HashSet<Integer>(listProcessos.size());
		for(ProcessoTrf p: listProcessos){
			ids.add(p.getIdProcessoTrf());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select p from SessaoProcessoDocumento o "
				+ " join o.processoDocumento pd "
				+ " join pd.tipoProcessoDocumento tpd "
				+ " join pd.processoTrf p "
				+ " where pd.ativo = true "
				+ " and tpd.idTipoProcessoDocumento = :idTipo "
				+ " and p.idProcessoTrf in (:idsProcessos) "
				+ " order by p.idProcessoTrf");
		
		Query q = getEntityManager()
				.createQuery(sb.toString())
				.setParameter("idTipo", idTipoProcessoDocumento)
				.setParameter("idsProcessos", ids);
		List<ProcessoTrf> lista = q.getResultList();
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> retornaListaProcessoComDocumentoAssinadoPorSessaoAndTipo(List<ProcessoTrf> listProcessos, Integer idTipoProcessoDocumento, Integer idSessao) {
		Set<Integer> ids = new HashSet<Integer>(listProcessos.size());
		for(ProcessoTrf p: listProcessos){
			ids.add(p.getIdProcessoTrf());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select p from SessaoProcessoDocumento o "
				+ " join o.processoDocumento pd "
				+ " join pd.tipoProcessoDocumento tpd "
				+ " join pd.processoTrf p "
				+ " where pd.ativo = true "
				+ " and pd.dataJuntada is not null "
				+ " and o.sessao.idSessao = :idSessao "
				+ " and tpd.idTipoProcessoDocumento = :idTipo "
				+ " and p.idProcessoTrf in (:idsProcessos) "
				+ " order by p.idProcessoTrf");
		
		Query q = getEntityManager()
				.createQuery(sb.toString())
				.setParameter("idSessao", idSessao)
				.setParameter("idTipo", idTipoProcessoDocumento)
				.setParameter("idsProcessos", ids);
		List<ProcessoTrf> lista = q.getResultList();
		return lista;
	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> recuperarSessaoProcessoDocumentosSemSessaoDefinida(ProcessoTrf processoTrf) {

		// Atualiza os documentos que nao possuem documentos
		StringBuilder udtDocumento = new StringBuilder();
		udtDocumento.append("select spd  ");
		udtDocumento.append("  from SessaoProcessoDocumento spd ");
		udtDocumento.append("  join spd.processoDocumento pd ");
		udtDocumento.append(" where spd.sessao is null ");
		udtDocumento.append("   and pd.processo = :processo ");
		udtDocumento.append("   and pd.ativo = true ");
		udtDocumento.append(" order by spd.idSessaoProcessoDocumento desc");
				
		Query queryDocumento = getEntityManager().createQuery(udtDocumento.toString());
		queryDocumento.setParameter("processo", processoTrf.getProcesso());
		
		return queryDocumento.getResultList();
	}

	public SessaoProcessoDocumento recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		return recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(sessao, processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), orgaoJulgador);
	}

	public SessaoProcessoDocumento recuperarEmentaAtivaPorProcessoSessaoAnterior(Date ultimaDataJulgamento, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		return recuperarDocumentoMaisRecente(ultimaDataJulgamento, processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), orgaoJulgador);
	}
	
	public SessaoProcessoDocumento recuperarRelatorioAtivoPorSessaoEhProcesso(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		return recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(sessao, processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), orgaoJulgador);
	}

	public SessaoProcessoDocumento recuperarRelatorioAtivoPorProcessoSessaoAnterior(Date ultimaDataJulgamento, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		return recuperarDocumentoMaisRecente(ultimaDataJulgamento, processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), orgaoJulgador);
	}

	public SessaoProcessoDocumento recuperarNotasOraisAtivaPorSessaoEhProcessoEhOrgaoJulgador(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		return recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(sessao, processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais(), orgaoJulgador);
	}
	
	public SessaoProcessoDocumento recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(Sessao sessao, ProcessoTrf processoTrf, TipoProcessoDocumento tipoProcessoDocumento, OrgaoJulgador orgaoJulgador) {
		
		StringBuilder jpql = new StringBuilder(1000)
			.append(" select spd ")
			.append("   from SessaoProcessoDocumento spd ")
			.append("   join spd.processoDocumento pd ")
			.append("  where spd.sessao = :sessao ")
			.append("    and pd.processo = :processo ")
			.append("    and pd.tipoProcessoDocumento = :tipoProcessoDocumento ")
			.append("    and pd.ativo = true ");
		
		if (orgaoJulgador != null) {
			jpql.append("   and spd.orgaoJulgador = :orgaoJulgador ");
		}
		jpql.append(" order by spd.idSessaoProcessoDocumento desc");
					
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("sessao", sessao)
			.setParameter("processo", processoTrf.getProcesso())
			.setParameter("tipoProcessoDocumento", tipoProcessoDocumento);
		
		if (orgaoJulgador != null) {
			query.setParameter("orgaoJulgador", orgaoJulgador);
		}
		
		return EntityUtil.getSingleResult(query);
	}
	
	@SuppressWarnings("unchecked")
	public SessaoProcessoDocumento recuperarDocumentoMaisRecente(Date ultimaDataJulgamento, ProcessoTrf processoTrf, TipoProcessoDocumento tipoProcessoDocumento, OrgaoJulgador orgaoJulgador) {
		StringBuilder hql = new StringBuilder(1000)
				.append(" select spd \n")
				.append("   from SessaoProcessoDocumento spd \n")
				.append("   join spd.processoDocumento pd \n")
				.append("   left join spd.sessao s \n")
				.append("  where pd.processo = :processo \n")
				.append("    and pd.tipoProcessoDocumento = :tipoProcessoDocumento \n")
				.append("    and spd.orgaoJulgador = :orgaoJulgador \n")
				.append("    and pd.ativo = true \n")
				.append("    and ((s.dataSessao is null) or (s.dataSessao > :ultimaDataJulgamento)) \n")
				.append("  order by s.dataSessao desc, pd.idProcessoDocumento desc ");

		List<SessaoProcessoDocumento> lista = getEntityManager().createQuery(hql.toString())
				.setParameter("processo", processoTrf.getProcesso())
				.setParameter("tipoProcessoDocumento", tipoProcessoDocumento)
				.setParameter("orgaoJulgador", orgaoJulgador)
				.setParameter("ultimaDataJulgamento", ultimaDataJulgamento)
				.getResultList()
				;
		
		Optional<SessaoProcessoDocumento> optSemSessao = lista.stream().filter(spd->spd.getSessao()==null).findAny();
		if (optSemSessao.isPresent())
			return optSemSessao.get();
		
		return lista.isEmpty() ? null : lista.get(0);
	}
	
	public SessaoProcessoDocumento recuperarCertidaoJulgamentoPorSessaoPautaProcesso(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		SessaoProcessoDocumento certidao = null; 
		if (sessaoPautaProcessoTrf != null) {
			TipoProcessoDocumento tpd = ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() != null ? ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() : ParametroUtil.instance().getTipoProcessoDocumentoCertidao();

			certidao = recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(
					sessaoPautaProcessoTrf.getSessao(), sessaoPautaProcessoTrf.getProcessoTrf(),
					tpd, null);
		}
		return certidao;
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os SessaoProcessoDocumento que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento) {
		String query = "DELETE FROM SessaoProcessoDocumento s WHERE s.processoDocumento.idProcessoDocumento = :id";
		getEntityManager().createQuery(query)
			.setParameter("id", idProcessoDocumento)
			.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> getDocumentosSessao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.processo = :processo ");
		sb.append("and o.processoDocumento.ativo = true ");
		sb.append("  order by o.idSessaoProcessoDocumento desc ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
		q.setParameter("processo", sessaoPautaProcessoTrf.getProcessoTrf().getProcesso());
		List<SessaoProcessoDocumento> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista;
		}
		return Collections.emptyList();
	}
	
	/**
	 * @param sessaoPautaProcessoTrf - referência da pauta do processo numa sessão
	 * @return lista de votos vinculados à sessão, porém, que não possuam conteúdo (referência para ProcessoDocumento)
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> getVotosSomenteSinalizadosSessao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumentoVoto o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("	and o.processoDocumento is null ");
		sb.append("	and o.processoTrf = :processoTrf ");
		sb.append("  order by o.idSessaoProcessoDocumento desc ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
		q.setParameter("processoTrf", sessaoPautaProcessoTrf.getProcessoTrf());
		List<SessaoProcessoDocumento> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista;
		}
		return Collections.emptyList();
	}

	public boolean procuraVotoAntecipadoLiberado() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoProcessoDocumentoVoto o where ");
		sb.append("o.tipoInclusao = 'A' ");
		sb.append("and o.sessao is null ");
		sb.append("and o.liberacao = true ");
		sb.append("and o.processoDocumento.processo.idProcesso = :id ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
		sb.append("and o.processoDocumento.ativo = true ");
		sb.append("and o.orgaoJulgador = :oj ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("id", ProcessoHome.instance().getInstance().getIdProcesso());
		q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		q.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}
}
