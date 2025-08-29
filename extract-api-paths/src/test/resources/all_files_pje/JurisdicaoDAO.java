/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.util.GenericComparator;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.JurisdicaoDTO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;

@Name(JurisdicaoDAO.NAME)
@Scope(ScopeType.EVENT)
public class JurisdicaoDAO extends BaseDAO<Jurisdicao> implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "jurisdicaoDAO";
	
	@Override
	public Object getId(Jurisdicao e) {
		return e.getIdJurisdicao();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Jurisdicao> findAll(){
		String queryStr = "SELECT o FROM Jurisdicao AS o WHERE o.ativo = true ";
		Query q = EntityUtil.getEntityManager().createQuery(queryStr);
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<JurisdicaoDTO> findAllJurisdicaoDTO(){
		StringBuilder sb = new StringBuilder("");
		
		sb.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.JurisdicaoDTO(j.idJurisdicao, j.jurisdicao) ");
		sb.append("FROM Jurisdicao j ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		
		return q.getResultList();
	}

	public Jurisdicao getJurisdicaoByOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		String queryStr = "SELECT o FROM Jurisdicao AS o WHERE o.ativo = true AND o.idJurisdicao = :idJurisdicao ";
		Query q = EntityUtil.getEntityManager().createQuery(queryStr);
		q.setParameter("idJurisdicao", orgaoJulgador.getJurisdicao().getIdJurisdicao());
		Jurisdicao singleResult = EntityUtil.getSingleResult(q);
		return singleResult;
	}

	public List<Jurisdicao> getJurisdicoesExpedientesPorPessoaOuProcuradoria(Integer idPessoa, 
		Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador){
		int idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual() != null ?  Authenticator.getIdLocalizacaoAtual() : 0;
		
		StringBuilder strQuery = new StringBuilder();
		if (idProcuradoria != null) {
			strQuery.append(obterQueryJurisdicoesExpedientes())
				.append("AND ppe.id_procuradoria = :idProcuradoria "
				  	  + "AND ppe.in_intima_pessoal = false ");
			
			limitarRepresentacaoProcuradoria(atuacaoProcurador, strQuery); 
			
			strQuery.append("ORDER BY jur.ds_jurisdicao");
		} else {
			strQuery.append("SELECT DISTINCT * FROM ( ")
				.append(obterQueryJurisdicoesExpedientes())
				.append("AND ppe.id_pessoa_parte = :idPessoa ")
				.append("UNION ALL ")
				.append(obterQueryJurisdicoesExpedientes())
				.append("AND ppe.in_intima_pessoal = FALSE ")
				.append("AND EXISTS ( ")
				.append("SELECT 1 FROM tb_processo_parte parte ")
				.append("INNER JOIN tb_proc_parte_represntante rep ON rep.id_processo_parte = parte.id_processo_parte ")
				.append("WHERE parte.id_processo_trf = ptf.id_processo_trf ")
				.append("AND parte.id_pessoa = ppe.id_pessoa_parte ")
				.append("AND rep.in_situacao = 'A' ")
				.append("AND (rep.id_representante = :idPessoa "
						+ "   OR EXISTS "
						+ "   (SELECT 1 FROM core.tb_usuario_localizacao ul "
						+ "    JOIN client.tb_pessoa_localizacao pl ON (ul.id_localizacao_fisica = pl.id_localizacao "
						+ "                                             AND ul.id_usuario != pl.id_pessoa) "
						+ "    WHERE rep.id_representante = pl.id_pessoa "
						+ "    AND ul.id_usuario = :idPessoa "
						+ "    AND ul.id_localizacao_fisica= "+idLocalizacaoAtual+") )"
						+ ") ")
				.append(") unionJurisdicao ")
				.append("ORDER BY unionJurisdicao.ds_jurisdicao");
		}	
		
		return montaResultadoJurisdicoes(idPessoa, idProcuradoria, atuacaoProcurador, strQuery.toString());
	}

	/**
	 * Método responsável por retornar a query capaz de recuperar as Jurisdições da aba Expedientes.
	 * 
	 * @return Query capaz de recuperar as Jurisdições da aba Expedientes.
	 */
	private String obterQueryJurisdicoesExpedientes() {
		StringBuilder strQuery = new StringBuilder("SELECT DISTINCT jur.id_jurisdicao, jur.ds_jurisdicao ")
		.append("FROM tb_processo_trf ptf ")
		.append("INNER JOIN  tb_proc_parte_expediente ppe on ppe.id_processo_trf = ptf.id_processo_trf ")
		.append("INNER JOIN tb_jurisdicao jur ON ptf.id_jurisdicao = jur.id_jurisdicao ")
		.append("WHERE ptf.cd_processo_status = 'D' ")
        .append("AND :idPessoa = :idPessoa ");

		return strQuery.toString();

	}
	
	public List<Jurisdicao> getJurisdicoesAcervoPorPessoaOuProcuradoria(Integer idPessoa, 
		Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador){
		int idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual() != null ?  Authenticator.getIdLocalizacaoAtual() : 0;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT jur.id_jurisdicao, jur.ds_jurisdicao ");
		sql.append("FROM tb_processo_trf ptf ");
		sql.append("INNER JOIN tb_jurisdicao jur ON ptf.id_jurisdicao = jur.id_jurisdicao "
				 + "LEFT JOIN client.tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') "
				 + "LEFT JOIN client.tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.in_situacao = 'A') ");
		sql.append("WHERE ptf.cd_processo_status = 'D' ");
		sql.append("AND :idPessoa = :idPessoa ");
		sql.append("AND EXISTS ( ");
		sql.append("   SELECT 1 FROM tb_processo_parte parte ");
		sql.append("   WHERE parte.id_processo_trf = ptf.id_processo_trf ");
		sql.append("   AND parte.in_situacao = 'A' "
				+ "    AND :idPessoa = :idPessoa ");

		if(idProcuradoria != null) {
			sql.append("   AND parte.id_procuradoria = :idProcuradoria) ");
			limitarRepresentacaoProcuradoria(atuacaoProcurador, sql);
			sql.append("   AND ( " );
			sql.append("      ptf.in_segredo_justica = false " );
			sql.append("      OR ( " );
			sql.append("         ptf.in_segredo_justica = true ");
			sql.append("         AND EXISTS ( ");
			sql.append("            SELECT 1 ");
			sql.append("            FROM tb_proc_visibilida_segredo pvis ");
			sql.append("            INNER JOIN tb_processo_parte pp ON pp.id_pessoa = pvis.id_pessoa AND pp.id_processo_trf = pvis.id_processo_trf ");
			sql.append("            WHERE pvis.id_processo_trf = ptf.id_processo_trf ");
			sql.append("            AND pp.in_situacao = 'A' ");
			sql.append("            AND pp.id_procuradoria = :idProcuradoria");
			sql.append("         ) ");
			sql.append("      ) ");
			sql.append("   ) ");
		} else {
			sql.append("   AND parte.id_pessoa = :idPessoa) ");
			sql.append("   AND (( ");
			sql.append("      ptf.in_segredo_justica = false ");
			sql.append("      OR ( ");
			sql.append("         EXISTS (SELECT 1 FROM tb_proc_visibilida_segredo vis ");
			sql.append("            WHERE vis.id_processo_trf = ptf.id_processo_trf ");
			sql.append("            	AND (id_pessoa = :idPessoa ");
			sql.append("					OR (vis.id_pessoa = pp.id_pessoa AND ppr.id_representante = :idPessoa))))) ");
			sql.append("      OR EXISTS (SELECT 1 FROM core.tb_usuario_localizacao ul ");
			sql.append("              JOIN client.tb_pessoa_localizacao pl ON (ul.id_localizacao_fisica = pl.id_localizacao ");
			sql.append("                                                       AND ul.id_usuario != pl.id_pessoa) ");
			sql.append("              WHERE ppr.id_representante=pl.id_pessoa ");
			sql.append("              AND ul.id_usuario=:idPessoa ");
			sql.append("              AND ul.id_localizacao_fisica=");
			sql.append(idLocalizacaoAtual);
			sql.append(")) ");
		}
		
		sql.append("ORDER BY jur.ds_jurisdicao");
		return montaResultadoJurisdicoes(idPessoa, idProcuradoria, atuacaoProcurador, sql.toString());

	}
	
	@SuppressWarnings("unchecked")
	private List<Jurisdicao> montaResultadoJurisdicoes(Integer idPessoa,Integer idProcuradoria,
			RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, String sql) {
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		
		if(idProcuradoria != null){
			q.setParameter("idProcuradoria", idProcuradoria);
		}
		
		if(idPessoa != null){
			q.setParameter("idPessoa", idPessoa);
		}
		
		List<Jurisdicao> jurisdicoes = new ArrayList<Jurisdicao>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes: resultList) {
			Jurisdicao jur = new Jurisdicao();
			jur.setIdJurisdicao((Integer)borderTypes[0]);
			jur.setJurisdicao((String)borderTypes[1]);
			jurisdicoes.add(jur);
		}
		return jurisdicoes;
	}

	private void limitarRepresentacaoProcuradoria(RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, StringBuilder sql) {
		int idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual() != null ?  Authenticator.getIdLocalizacaoAtual() : 0;
		
		if(atuacaoProcurador != null && !atuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.G)) {
			sql.append("   AND ( EXISTS ( ");
			sql.append("      SELECT 1 FROM tb_pess_proc_jurisdicao ppj ");
			sql.append("      INNER JOIN tb_pessoa_procuradoria ppr ON ppr.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria ");
			sql.append("      AND ppj.in_ativo = true ");
			sql.append("      AND ppr.id_pessoa = :idPessoa ");
			sql.append("      AND ptf.id_jurisdicao = ppj.id_jurisdicao ");
			sql.append("      AND ppr.id_procuradoria = :idProcuradoria "
					+ "       GROUP BY ppj.id_jurisdicao) ");
			sql.append("OR EXISTS ( ");
			sql.append("   SELECT 1 ");
			sql.append("   FROM tb_caixa_representante cairep ");
			sql.append("   INNER JOIN tb_caixa_adv_proc cai ON cai.id_caixa_adv_proc = cairep.id_caixa_adv_proc ");
			sql.append("   WHERE cai.id_jurisdicao = ptf.id_jurisdicao ");
			sql.append("   AND cairep.id_pessoa_fisica = :idPessoa "
					+ "    AND cai.id_localizacao = "+idLocalizacaoAtual+" "
					+ "    GROUP BY cai.id_caixa_adv_proc ");
			sql.append(") )");
		}

	}
	
	/**
	 * 
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param tipoUsuarioExterno
	 * @param idProcuradoria
	 * @param isProcuradorGestor
	 * @return
	 */
	public List<JurisdicaoVO> obterJurisdicoesAcervo(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, ConsultaProcessoVO criteriosPesquisa) {
		List<Map<String, String>> consultas = new ArrayList<Map<String, String>>(0);
		
		consultas = ComponentUtil.getComponent(ProcessoTrfDAO.class)
				.obterQueryProcessos(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa);
		
		Map<String, String> restricoes = ComponentUtil.getComponent(ProcessoTrfDAO.class)
				.obterQueryRestricoesProcessos(idPessoa, idLocalizacao, idProcuradoria, criteriosPesquisa);
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(this.consolidarConsultaProcessos(consultas, restricoes));
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		List<JurisdicaoVO> result = new ArrayList<JurisdicaoVO>(resultList.size());

		for (Object[] borderTypes: resultList) {
			Integer idJurisdicao = (Integer)borderTypes[0];
			String nomeJurisdicao = (String)borderTypes[1];
			Boolean isAdmin = (Integer)borderTypes[2] == 0 ? false : true;
			BigInteger contadorJurisdicao = (BigInteger)borderTypes[3];
			result.add(new JurisdicaoVO(idJurisdicao, nomeJurisdicao, isAdmin, contadorJurisdicao));
		}
		return result;
	}
	
	/**
	 * 
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param tipoUsuarioExterno
	 * @param idProcuradoria
	 * @param isProcuradorGestor
	 * @return
	 */
	public List<JurisdicaoVO> obterJurisdicoesExpedientes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisa) {
		List<Map<String, String>> consultas = new ArrayList<Map<String, String>>(0);
		
		consultas = ComponentUtil.getComponent(ProcessoParteExpedienteDAO.class).
				obterQueryExpedientes(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa);
		Map<String, String> restricoes = ComponentUtil.getComponent(ProcessoParteExpedienteDAO.class)
				.obterQueryRestricoesExpedientes(idPessoa, idLocalizacao, idProcuradoria, criteriosPesquisa);
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(this.consolidarConsultaExpedientes(idLocalizacao, consultas, restricoes));
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		List<JurisdicaoVO> result = new ArrayList<JurisdicaoVO>(resultList.size());

		for (Object[] borderTypes: resultList) {
			Integer idJurisdicao = (Integer)borderTypes[0];
			String nomeJurisdicao = (String)borderTypes[1];
			Boolean isAdmin = (Integer)borderTypes[2] == 0 ? false : true;
			BigInteger contadorJurisdicao = (BigInteger)borderTypes[3];
			Boolean temCaixas = (Boolean)borderTypes[4];
			result.add(new JurisdicaoVO(idJurisdicao, nomeJurisdicao, isAdmin, contadorJurisdicao, temCaixas));
		}
		return result;
	}
	
	/**
	 * 
	 * @param consultas
	 * @param restricoes
	 * @return
	 */
	private String consolidarConsultaExpedientes(Integer idLocalizacao, List<Map<String, String>> consultas, Map<String, String> restricoes) {
		StringBuilder consultaConsolidada = new StringBuilder();

		consultaConsolidada.append("SELECT id_jurisdicao, ds_jurisdicao, MAX(in_admin_jurisdicao) as admin_jurisdicao, count(DISTINCT id_processo_parte_expediente) as contador, ");
		consultaConsolidada.append("(SELECT CASE WHEN COUNT(1) = 0 THEN false ELSE true END from tb_caixa_adv_proc cx WHERE cx.id_jurisdicao = id_jurisdicao AND cx.id_localizacao = " + idLocalizacao + ") AS in_caixa FROM (");
		
		int count = 0;
		for (Map<String, String> consulta : consultas) {
			if (count > 0) {
				consultaConsolidada.append(" UNION ALL ");
			}
			consultaConsolidada.append("SELECT jur.id_jurisdicao, jur.ds_jurisdicao, in_admin_jurisdicao, ppe.id_processo_parte_expediente ");
			if(consulta.get("FROM") != null) {
				consultaConsolidada.append(consulta.get("FROM"));
			}
			if(restricoes.get("FROM") != null) {
				consultaConsolidada.append(restricoes.get("FROM"));
			}
			if(consulta.get("WHERE") != null) {
				consultaConsolidada.append(consulta.get("WHERE"));
			}
			if(restricoes.get("WHERE") != null){
				consultaConsolidada.append(restricoes.get("WHERE"));
			}		
		 	count++;
		}
		consultaConsolidada.append(") as resultado_union")
			.append(" GROUP BY id_jurisdicao, ds_jurisdicao")
			.append(" ORDER BY ds_jurisdicao, contador DESC");

		return consultaConsolidada.toString();
	}

	/**
	 * 
	 * @param consultas
	 * @param restricoes
	 * @return
	 */
	private String consolidarConsultaProcessos(List<Map<String, String>> consultas, Map<String, String> restricoes) {
		StringBuilder consultaConsolidada = new StringBuilder();

		consultaConsolidada.append("SELECT id_jurisdicao, ds_jurisdicao, MAX(in_admin_jurisdicao) as admin_jurisdicao, count(DISTINCT id_processo_trf) as contador FROM (");
		
		int count = 0;
		for (Map<String, String> consulta : consultas) {
			if (count > 0) {
				consultaConsolidada.append(" UNION ALL ");
			}
			consultaConsolidada.append("SELECT jur.id_jurisdicao, jur.ds_jurisdicao, in_admin_jurisdicao, ptf.id_processo_trf ");
			if(consulta.get("FROM") != null) {
				consultaConsolidada.append(consulta.get("FROM"));
			}
			if(restricoes.get("FROM") != null) {
				consultaConsolidada.append(restricoes.get("FROM"));
			}
			if(consulta.get("WHERE") != null) {
				consultaConsolidada.append(consulta.get("WHERE"));
			}
			if(restricoes.get("WHERE") != null){
				consultaConsolidada.append(restricoes.get("WHERE"));
			}
		 	count++;
		}
		consultaConsolidada.append(") as resultado_union")
			.append(" GROUP BY id_jurisdicao, ds_jurisdicao")
			.append(" ORDER BY ds_jurisdicao, contador DESC");

		return consultaConsolidada.toString();
	}
	
	/**
	 * Retorna a jurisdição pelo número de origem.
	 * 
	 * @param numeroOrigem Número de origem.
	 * @return Jurisdição.
	 */
	public Jurisdicao obterPorNumeroOrigem(Integer numeroOrigem) {
		StringBuilder hql = new StringBuilder();
		hql.append("from Jurisdicao jurisdicao ");
		hql.append("where ");
		hql.append("jurisdicao.ativo = true and ");
		hql.append("jurisdicao.numeroOrigem = :numeroOrigem ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("numeroOrigem", numeroOrigem);
		
		return getSingleResult(query);
	}

	/**
	 * Método responsável por recuperar as Jurisdições filtrando pelos itens: idAreaDireito e/ou idMunicipio
	 * 
	 * @param idAreaDireito Identificador da área de direito. Pode ser nulo.
	 * @param idMunicipio Identificador do município. Pode ser nulo
	 * @return Lista de jurisdições.
	 */
	@SuppressWarnings("unchecked")
	public List<Jurisdicao> recuperarJurisdicoes(Integer idAreaDireito, Integer idMunicipio) {
		StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM CompetenciaAreaDireito o JOIN o.jurisdicao p ");
		if (idMunicipio != null) {
			jpql.append("JOIN p.municipioList q ");
		}

		jpql.append("WHERE 1 = 1 AND p.ativo = true ");
		
		boolean isUsuarioInterno = Authenticator.isUsuarioInterno();
		
		if(!isUsuarioInterno) {
			jpql.append("AND p.isJuridicaoExterna = true ");
		}

		if (idMunicipio != null) {
			jpql.append("AND q.municipio.idMunicipio = :idMunicipio ");
		}

		if (idAreaDireito != null) {
			jpql.append("AND o.idAreaDireito = :idAreaDireito ");
		}

		Query query = getEntityManager().createQuery(jpql.toString());
		if (idMunicipio != null) {
			query.setParameter("idMunicipio", idMunicipio);
		}

		if (idAreaDireito != null) {
			query.setParameter("idAreaDireito", idAreaDireito);
		}

		List<Jurisdicao> result = query.getResultList();
		Collections.sort(result, new GenericComparator());

		return result;
	}

}
