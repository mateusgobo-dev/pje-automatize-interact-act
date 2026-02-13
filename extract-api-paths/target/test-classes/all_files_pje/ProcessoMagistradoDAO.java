package br.jus.cnj.pje.business.dao;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.enums.TipoAtuacaoDetalhadaMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoRelacaoProcessoMagistradoEnum;

@Name("processoMagistradoDAO")
public class ProcessoMagistradoDAO extends BaseDAO<ProcessoMagistrado> {
	
	@Override
	public Object getId(ProcessoMagistrado e) {
		return e.getIdProcessoMagistrado();
	}
	
	/**
	 * Recupera uma lista contendo os vinculos entre magistrados e o processo
	 * ordenados pela data de vinculação da mais recente para a mais antiga
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} que será utilizado para pesquisar os
	 *            magistrados vinculados.
	 * 
	 * @param tipoRelacaoMagistrado
	 *            {@link TipoRelacaoProcessoMagistradoEnum} que representa o
	 *            tipo de relação que será pesquisada. Use <code>null</code>
	 *            para não realizar filtro por este atributo.
	 * 
	 * @param tipoAtuacaoMagistrado
	 *            {@link TipoAtuacaoMagistradoEnum} que representa o atuação do
	 *            magitrado (relator, revisor, vogal). Use <code>null</code>
	 *            para não realizar filtro por este atributo.
	 * 
	 * @param magistradoTitular
	 *            <code>true</code> traz vinculos realizados somente por
	 *            magistrados titular do OJ <code>false</code> traz vinculos
	 *            realizados somente por magistrados substitutos/auxiliar do OJ
	 *            <code>null</code> não realiza filtragem, trazendo ambos os
	 *            casos
	 * 
	 * @param ativo
	 *            <code>true</code> traz somente vinculos que ainda estão
	 *            ativos. <code>false</code> traz somente vinculos inativos
	 *            <code>null</code> não realiza filtragem, trazendo ambos os
	 *            casos
	 * 
	 * @return {@link List} de {@link ProcessoMagistrado} de acordo com os
	 *         parâmetros informados.
	 * 
	 */
	public List<ProcessoMagistrado> obterMagistradosRelacionados(ProcessoTrf processo,
			TipoRelacaoProcessoMagistradoEnum tipoRelacaoMagistrado, TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado,
			Boolean magistradoTitular, Boolean ativo, Integer idOrgaoJulgador) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT o FROM ProcessoMagistrado o ");
		sb.append("WHERE o.processo = :processo ");		
		if (tipoRelacaoMagistrado != null) {
			sb.append("AND o.tipoRelacaoProcessoMagistrado = :tipoRelacao ");
		}
		if (tipoAtuacaoMagistrado != null) {
			sb.append("AND o.tipoAtuacaoMagistrado = :tipoAtuacaoMagistrado ");
		}
		if (magistradoTitular != null) {
			sb.append("AND o.magistradoTitular = :magistradoTitular ");
		}
		if (ativo != null) {
			sb.append("AND o.ativo = :ativo ");
		}
		
		if (idOrgaoJulgador != null){
			sb.append("AND o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador "); 
		}
		
		sb.append("ORDER BY o.dataVinculacao DESC");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", processo);
		if (tipoRelacaoMagistrado != null) {
			q.setParameter("tipoRelacao", tipoRelacaoMagistrado);
		}
		if (tipoAtuacaoMagistrado != null) {
			q.setParameter("tipoAtuacaoMagistrado", tipoAtuacaoMagistrado);
		}
		if (magistradoTitular != null) {
			q.setParameter("magistradoTitular", magistradoTitular );
		}
		if (ativo != null) {
			q.setParameter("ativo", ativo );
		}
		
		if (idOrgaoJulgador != null){
			q.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		}
		
		@SuppressWarnings("unchecked")
		List<ProcessoMagistrado> resultList = q.getResultList();		
		return resultList;
	}
	
	/**
	 * Obtém de forma mais perfomática usando query nativa o identificador do
	 * magistrado (id e nome) que se vinculou por ultimo ao processo filtrando de
	 * acordo com os parâmetros informados.
	 * 
	 * @param processoTrf {@link ProcessoTrf} que será utilizado para 
	 * pesquisar os magistrados vinculados.
	 * 
	 * @param tipoRelacaoMagistrado {@link TipoRelacaoProcessoMagistradoEnum} que 
	 * representa o tipo de relação que será pesquisada. Use <code>null</code> para 
	 * não realizar filtro por este atributo.
	 * 
	 * @param tipoAtuacaoMagistrado {@link TipoAtuacaoMagistradoEnum} que representa 
	 * o atuação do magitrado (relator, revisor, vogal). Use <code>null</code> para 
	 * não realizar filtro por este atributo.
	 * 
	 * @param magistradoTitular <code>true</code> traz vinculos realizados 
	 * somente por magistrados titular do OJ <code>false</code> traz vinculos realizados 
	 * somente por magistrados substitutos/auxiliar do OJ <code>null</code> não realiza 
	 * filtragem, trazendo ambos os casos
	 * 
	 * @return Um {@link Entry} contendo o id {@link Integer} de pessoa do 
	 * magistrado e seu nome {@link String};
	 */
	@SuppressWarnings("unchecked")
	public Entry<Integer, String> obterIdentificadorMagistradoUltimaVinculacao(ProcessoTrf processoTrf, TipoRelacaoProcessoMagistradoEnum tipoRelacaoMagistrado, 
			TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado, Boolean magistradoTitular) {

		Entry<Integer, String> retorno = null;
		if (processoTrf != null) {
			StringBuilder hql = new StringBuilder("select pm.id_magistrado, ul.ds_nome ")
				.append("from tb_processo_magistrado pm ")
				.append("inner join tb_usuario_login ul on pm.id_magistrado = ul.id_usuario ")
				.append("where pm.in_ativo = true and pm.id_processo_trf = :idProcessoTrf ");
			
			if (tipoRelacaoMagistrado != null) {
				hql.append("and pm.tp_relacao_processo_magistrado = :tipoRelacao ");
			}
			if (tipoAtuacaoMagistrado != null) {
				hql.append("and pm.tp_atuacao_magistrado = :tipoAtuacaoMagistrado ");
			}
			if (magistradoTitular != null) {
				hql.append("and pm.in_magistrado_titular = :magistradoTitular ");
			}
			
			hql.append("order by dt_vinculacao desc limit 1");
	
			Query query = getEntityManager().createNativeQuery(hql.toString());
			query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
	
			if (tipoRelacaoMagistrado != null) {
				query.setParameter("tipoRelacao", tipoRelacaoMagistrado.name());
			}
			if (tipoAtuacaoMagistrado != null) {
				query.setParameter("tipoAtuacaoMagistrado", tipoAtuacaoMagistrado.name());
			}
			if (magistradoTitular != null) {
				query.setParameter("magistradoTitular", magistradoTitular);
			}
	
			List<Object[]> resultList = query.getResultList();
			if (!resultList.isEmpty()) {
				retorno = new AbstractMap.SimpleEntry<Integer, String>((Integer) resultList.get(0)[0], (String) resultList.get(0)[1]);
			}
		}
		return retorno;
	}

	/**
	 *@see {@link ProcessoMagistradoManager#obterAtuacaoDetalhadaMagistradosRelacionados(ProcessoTrf)} 
	 */	
	@SuppressWarnings("unchecked")
	public Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> obterAtuacaoDetalhadaMagistradosRelacionados(ProcessoTrf processo) {
		Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> retorno = new HashMap<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>>();
		if (processo == null) {
			return retorno;
		}		
		StringBuilder sql = new StringBuilder();
		sql.append("select pm.id_magistrado, ul.ds_nome, pm.tp_relacao_processo_magistrado, pm.tp_atuacao_magistrado, pm.in_magistrado_titular ");
		sql.append("from tb_processo_magistrado pm ");
		sql.append("inner join tb_usuario_login ul on (pm.id_magistrado = ul.id_usuario) ");
		sql.append("where pm.in_ativo = true ");
		sql.append("and pm.id_processo_trf = :idProcessoTrf "); 
		sql.append("order by dt_vinculacao ");

		Query query  = getEntityManager().createNativeQuery(sql.toString());
		query.setParameter("idProcessoTrf", processo.getIdProcessoTrf());
		
		List<Object[]> resultList = query.getResultList();		
		for (Object[] r: resultList) {			
			TipoRelacaoProcessoMagistradoEnum tpVinculacao= TipoRelacaoProcessoMagistradoEnum.valueOf((String)r[2]);
			TipoAtuacaoMagistradoEnum tpAtuacao = TipoAtuacaoMagistradoEnum.valueOf((String)r[3]);
			boolean titular = (Boolean) r[4];			
			TipoAtuacaoDetalhadaMagistradoEnum tipoAtuacaoDetalhadaMagistrado = TipoAtuacaoDetalhadaMagistradoEnum
					.valueOf(tpVinculacao, tpAtuacao, titular);			
			if (tipoAtuacaoDetalhadaMagistrado != null) {
				retorno.put(tipoAtuacaoDetalhadaMagistrado, new AbstractMap.SimpleEntry<Integer, String>((Integer)r[0], (String)r[1]));
			}
		}		
		return retorno;
	}

	public void removerVinculacoes(ProcessoTrf processo) {
		String strQry = "DELETE FROM ProcessoMagistrado AS pm WHERE pm.processo = :processo" ;
		
		Query qry = entityManager.createQuery(strQry);
		qry.setParameter("processo", processo);
		qry.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoMagistrado> obterAssociacoesMagistradoSubstituto(SubstituicaoMagistrado substituicaoMagistrado) {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" SELECT pm  ");
		strQuery.append(" FROM   ProcessoMagistrado pm ");
		strQuery.append(" WHERE  substituicaoMagistradoVigente = :substituicao and ");
		strQuery.append("        tipoAtuacaoMagistrado = :tipoAtuacao and ");
		strQuery.append("        magistradoTitular = :titular and ");
		strQuery.append("        tipoRelacaoProcessoMagistrado in (  ");
		strQuery.append(		 getTiposRelacao());
		strQuery.append(" 		 ) ");
		strQuery.append(" ORDER BY dataVinculacao desc ");
		
		Query query = getEntityManager().createQuery(strQuery.toString());
		query.setParameter("substituicao", substituicaoMagistrado);
		query.setParameter("tipoAtuacao", TipoAtuacaoMagistradoEnum.RELAT);
		query.setParameter("titular", Boolean.FALSE);
		
		return query.getResultList();
	}

	private String getTiposRelacao() {
		StringBuilder tpRelacao = new StringBuilder();
		TipoRelacaoProcessoMagistradoEnum tipoRelacao[] = new TipoRelacaoProcessoMagistradoEnum[]{TipoRelacaoProcessoMagistradoEnum.REGIM, TipoRelacaoProcessoMagistradoEnum.RESER};
		for (TipoRelacaoProcessoMagistradoEnum tipo : tipoRelacao) {
			tpRelacao.append("'");
			tpRelacao.append(tipo);
			tpRelacao.append("',");
		}
		return (new StringBuilder(tpRelacao.substring(0, tpRelacao.lastIndexOf(",")))).toString();
	}
	
	/**
	 * Método que recupera os processos que não possuem vinculação regimental e de reserva de relator em um determinado Orgão julgador 
	 * @param substituicaoMagistrado
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> obterProcessosSemVinculacaoRelator(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT a ") 
				.append(" FROM ProcessoTrf a ")
				.append(" WHERE a.orgaoJulgador = :orgaoJulgador ")
				.append(" AND a.processoStatus = 'D' ")
				.append(" AND a.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ")
				.append(" AND a.idProcessoTrf NOT IN ( ")
				.append("	SELECT o.processo.idProcessoTrf ") 
				.append("	FROM ProcessoMagistrado AS o ")
				.append("	WHERE o.processo = a ")
				.append("	AND o.ativo IS TRUE ")
				.append("	AND o.tipoRelacaoProcessoMagistrado in (:tipoRelacao) ")
				.append("	AND o.tipoAtuacaoMagistrado = :tipoAtuacaoMagistradoRel ")
				.append(" ) ORDER BY a.dataDistribuicao DESC ");
		
		List<TipoRelacaoProcessoMagistradoEnum> relacoes = new ArrayList<TipoRelacaoProcessoMagistradoEnum>();
		relacoes.add(TipoRelacaoProcessoMagistradoEnum.REGIM);
		relacoes.add(TipoRelacaoProcessoMagistradoEnum.RESER);
		
		Query query = EntityUtil.createQuery(jpql.toString());
		query.setParameter("orgaoJulgador", orgaoJulgador);
		query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		query.setParameter("tipoRelacao", relacoes);
		query.setParameter("tipoAtuacaoMagistradoRel", TipoAtuacaoMagistradoEnum.RELAT);

		return query.getResultList();
		
	}
	
	public List<Integer> obterResponsaveis(ProcessoTrf processo) {
		ConsultaMagistradoResponsavel consulta = new ConsultaMagistradoResponsavel(getEntityManager());
		return consulta.obterMagistradosResponsaveis(processo.getOrgaoJulgador().getIdOrgaoJulgador(), null);
	}
}
