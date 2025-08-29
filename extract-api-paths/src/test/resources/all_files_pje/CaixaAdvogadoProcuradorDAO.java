package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;

/**
 * Componente de acesso a dados da entidade {@link CaixaAdvogadoProcurador}.
 * 
 * @author cristof
 *
 */
@Name("caixaAdvogadoProcuradorDAO")
public class CaixaAdvogadoProcuradorDAO extends BaseDAO<CaixaAdvogadoProcurador> {

	@In
	ProcessoParteExpedienteDAO processoParteExpedienteDAO;
	
	@In
	ProcessoJudicialDAO processoJudicialDAO;
	
	@Override
	public Integer getId(CaixaAdvogadoProcurador cx) {
		return cx.getIdCaixaAdvogadoProcurador();
	}
	
	public List<CaixaAdvogadoProcurador> list(Integer idJurisdicao, Integer idLocalizacao, Integer idPessoaFisica) {
		return list(idJurisdicao, idLocalizacao, idPessoaFisica, false);
	}
	
	@SuppressWarnings("unchecked")
	public List<CaixaAdvogadoProcurador> list(Integer idJurisdicao, Integer idLocalizacao, Integer idPessoaFisica, boolean ativas) {
		Map<String, Object> mapParametros = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT o FROM CaixaAdvogadoProcurador AS o ");
		
		if(idPessoaFisica != null) {
			query.append("INNER JOIN o.caixaRepresentanteList AS caixasRepresentantes ");
		}

		query.append("WHERE 1=1 ");
		
		if(idJurisdicao != null) {
			query.append(" AND o.jurisdicao.idJurisdicao = :idJurisdicao ");
			mapParametros.put("idJurisdicao", idJurisdicao);
		}
		
		if(idLocalizacao != null) {
			query.append("AND o.localizacao.idLocalizacao = :idLocalizacao ");
			mapParametros.put("idLocalizacao", idLocalizacao);
		}
		
		if(idPessoaFisica != null) {
			query.append("AND caixasRepresentantes.representante.idPessoa = :idPessoa ");
			mapParametros.put("idPessoa", idPessoaFisica);
		}
		
		if(ativas) {
			query.append("AND NOT EXISTS ( ");
			query.append("   select 1 from PeriodoInativacaoCaixaRepresentante pic ");
			query.append("   where pic.caixaAdvogadoProcurador = o ");
			query.append("   and current_date between pic.dataInicio and pic.dataFim ) ");
		}
		
		Query q = EntityUtil.getEntityManager().createQuery(query.toString());
		if(!mapParametros.isEmpty()) {
			for(Entry<String, Object> parametro : mapParametros.entrySet()) {
				q.setParameter(parametro.getKey(), parametro.getValue());
			}
		}
		
		return q.getResultList();
	}

	public List<CaixaAdvogadoProcurador> findByIdLocalizacao(Integer idLocalizacao) {
		return list(null, idLocalizacao, null);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Jurisdicao,List<CaixaAdvogadoProcurador>> getCaixas(Integer idLocalizacao, Integer idProcuradoria, Integer idPessoa, List<Integer> idsJurisdicoes, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		Map<Jurisdicao,List<CaixaAdvogadoProcurador>> mapCaixas = new HashMap<Jurisdicao,List<CaixaAdvogadoProcurador>>(0);
		List<CaixaAdvogadoProcurador> caixas = new ArrayList<CaixaAdvogadoProcurador>(0);
		boolean isProcuradoria = idProcuradoria != null ? true : false;
		idProcuradoria = idProcuradoria == null ? 0 : idProcuradoria;

		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT jur.id_jurisdicao, jur.ds_jurisdicao, cx.id_caixa_adv_proc, ");
		sql.append("       cx.nm_caixa || ' (' || CASE WHEN pcap.count IS NULL THEN 0 ELSE pcap.count END || ')' AS nm_caixa, ");
		sql.append("       CASE WHEN pic.count IS NULL OR pic.count = 0 THEN true ELSE false END AS in_ativo, cx.nm_caixa ");
		sql.append("FROM tb_caixa_adv_proc cx ");
		sql.append("INNER JOIN tb_jurisdicao jur ON jur.id_jurisdicao = cx.id_jurisdicao "
				 + "LEFT JOIN client.tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		
		if(atuacaoProcurador != null && atuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.P)){
			sql.append("INNER JOIN ");
		}
		else{
			sql.append("LEFT JOIN ");	
		}

		sql.append("( ");
		sql.append(getClausulasCaixas(isProcuradoria, atuacaoProcurador, tipoSituacaoExpediente));		
		sql.append(") AS pcap ON pcap.id_jurisdicao = jur.id_jurisdicao AND pcap.id_caixa_adv_proc = cx.id_caixa_adv_proc ");

		sql.append("LEFT JOIN ( ");
		sql.append("   SELECT id_caixa_adv_proc, COUNT(1) AS count ");
		sql.append("   FROM tb_periodo_inativ_caixa_rep ");
		sql.append("   WHERE current_date BETWEEN dt_inicio AND dt_fim ");
		sql.append("   GROUP BY id_caixa_adv_proc ");
		sql.append(") AS pic ON pic.id_caixa_adv_proc = cx.id_caixa_adv_proc ");
		sql.append("LEFT JOIN client.tb_pess_proc_jurisdicao ppj ON (ppj.id_jurisdicao = jur.id_jurisdicao AND ppj.in_ativo = TRUE) "
				 + "LEFT JOIN client.tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ");


		sql.append("WHERE jur.id_jurisdicao IN (:idJurisdicao) ");
		sql.append("AND cx.id_localizacao = :idLocalizacao "
				 +  "AND ("
				 + "      ("+isProcuradoria+" = TRUE "
				 + "       AND ("
				 + "             (pproc.id_pessoa = :idPessoa"
				 + "              AND pproc.id_procuradoria = :idProcuradoria"
				 + "             ) "
				 + "       OR pcap.id_caixa_adv_proc IS NOT NULL "
				 + "           ) "
				 + "      ) "
				 + "      OR "+isProcuradoria+" = FALSE "
				 + "      OR '"+atuacaoProcurador+"' = '"+RepresentanteProcessualTipoAtuacaoEnum.G+"' "
				 + "     ) ");	

		sql.append("ORDER BY jur.id_jurisdicao, cx.nm_caixa");
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		
		q.setParameter("idJurisdicao", idsJurisdicoes);
		q.setParameter("idLocalizacao", idLocalizacao);
		q.setParameter("idProcuradoria", idProcuradoria);
		
		if(idPessoa != null) {
			q.setParameter("idPessoa", idPessoa);
		}
		Integer idJur = 0;
		String dsJur = null;
		Jurisdicao jurisdicao = new Jurisdicao();
		
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes: resultList) {
			if(idJur != 0 && idJur != ((Integer)borderTypes[0]).intValue()) {
				jurisdicao = new Jurisdicao();
				jurisdicao.setIdJurisdicao(idJur);
				jurisdicao.setJurisdicao(dsJur);
				mapCaixas.put(jurisdicao, caixas);
				caixas = new ArrayList<CaixaAdvogadoProcurador>(0);
			}
			idJur = (Integer)borderTypes[0];
			dsJur = (String)borderTypes[1];
			CaixaAdvogadoProcurador caixa = new CaixaAdvogadoProcurador();
			caixa.setIdCaixaAdvogadoProcurador((Integer)borderTypes[2]);
			caixa.setNomeCaixaAdvogadoProcurador((String)borderTypes[3]);
			caixa.setAtiva((Boolean)borderTypes[4]);
			caixa.setDsCaixaAdvogadoProcurador((String)borderTypes[5]);
			caixas.add(caixa);
		}
		if(caixas.size() > 0) {
			jurisdicao = new Jurisdicao();
			jurisdicao.setIdJurisdicao(idJur);
			jurisdicao.setJurisdicao(dsJur);
			mapCaixas.put(jurisdicao, caixas);
		}
		
		return mapCaixas;
	}
	
	public boolean isCaixaAtiva(Integer idCaixa) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(1) AS count ");
		sql.append("FROM tb_periodo_inativ_caixa_rep ");
		sql.append("WHERE current_date BETWEEN dt_inicio AND dt_fim ");
		sql.append("AND id_caixa_adv_proc = :idCaixa ");

		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		q.setParameter("idCaixa", idCaixa);
		
		long result = ((BigInteger)q.getSingleResult()).longValue();
		return result == 0;
	}
	
	private String getClausulasCaixas(boolean isProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		int idLocalizacao = Authenticator.getIdLocalizacaoAtual();
		if(tipoSituacaoExpediente == null) {
			return getClausulasCaixasAcervo(isProcuradoria, atuacaoProcurador, idLocalizacao);
		}
		else {
			return getClausulasCaixasExpedientes(isProcuradoria, atuacaoProcurador, tipoSituacaoExpediente, idLocalizacao);
		}
	}

	private String getClausulasCaixasAcervo(boolean isProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, int idLocalizacao) {
		StringBuilder sql = new StringBuilder();

		sql.append("   SELECT ptf.id_jurisdicao, cx_proc.id_caixa_adv_proc, COUNT(DISTINCT ptf.id_processo_trf) AS count ");
		sql.append("   FROM client.tb_jurisdicao jr ");
		sql.append("   JOIN client.tb_cabecalho_processo ptf ON (jr.id_jurisdicao = ptf.id_jurisdicao) "
				 + "   LEFT JOIN client.tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_processo_trf = ptf.id_processo_trf) "
				 + "   LEFT JOIN tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cx_proc.id_caixa_adv_proc) "
				 + "   LEFT JOIN client.tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) "
				 + "   LEFT JOIN client.tb_pess_proc_jurisdicao ppj ON (ppj.id_jurisdicao = ptf.id_jurisdicao AND ppj.in_ativo = TRUE) "
				 + "   LEFT JOIN client.tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) "
				 + "   LEFT JOIN client.tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') "
				 + "   LEFT JOIN client.tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.in_situacao = 'A') ");
		sql.append("   WHERE ptf.cd_processo_status = 'D' ");
		sql.append("   AND ptf.id_jurisdicao IN (:idJurisdicao) "
				+ "    AND :idPessoa = :idPessoa ");	

		sql.append(processoJudicialDAO.limitarRepresentacao(isProcuradoria, atuacaoProcurador, idLocalizacao));
		sql.append(processoJudicialDAO.limitarVisibilidade(isProcuradoria));
		
		sql.append("GROUP BY ptf.id_jurisdicao, cx_proc.id_caixa_adv_proc ");
		
		return sql.toString();
	}

	private String getClausulasCaixasExpedientes(boolean isProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, TipoSituacaoExpedienteEnum tipoSituacaoExpediente, int idLocalizacao) {
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ptf.id_jurisdicao, cx_exp.id_caixa_adv_proc, COUNT(DISTINCT ppe.id_processo_parte_expediente) AS count ");
		sql.append("FROM  client.tb_jurisdicao jr "
				 + "JOIN client.tb_cabecalho_processo ptf ON (jr.id_jurisdicao = ptf.id_jurisdicao) "
				 + "JOIN client.tb_proc_parte_expediente ppe ON (ptf.id_processo_trf = ppe.id_processo_trf) "
				 + "JOIN client.tb_processo_expediente pe ON (pe.id_processo_expediente = ppe.id_processo_expediente) "
				 + "JOIN tb_tipo_processo_documento tpd ON tpd.id_tipo_processo_documento = pe.id_tipo_processo_documento "
				 + "LEFT JOIN client.tb_proc_parte_exp_caixa_adv_proc cx_exp ON (ppe.id_processo_parte_expediente = cx_exp.id_processo_parte_expediente) "
				 + "LEFT JOIN tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) "
				 + "LEFT JOIN client.tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) "
				 + "LEFT JOIN client.tb_processo_parte pp ON (pp.id_pessoa = ppe.id_pessoa_parte AND pp.id_processo_trf = ppe.id_processo_trf) "
				 + "LEFT JOIN client.tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte "
				 + "                                                    AND ppr.in_situacao = 'A') "
				 + "LEFT JOIN tb_resposta_expediente rex ON rex.id = ppe.id_resposta "
				 + "LEFT JOIN "
				 + "( "
				 + "   SELECT rgi.id, rgi.dt_registro, rgi.nr_aviso_recebimento, rgi.in_resultado, rgi.id_processo_parte_expediente "
				 + "   FROM tb_registro_intimacao rgi "
				 + "   WHERE rgi.id = (SELECT MIN(id) FROM tb_registro_intimacao WHERE id_processo_parte_expediente = rgi.id_processo_parte_expediente) "
				 + ") AS rgi ON rgi.id_processo_parte_expediente = ppe.id_processo_parte_expediente "
				 + "WHERE ptf.cd_processo_status = 'D' ");
		sql.append("AND ptf.id_jurisdicao IN (:idJurisdicao) ");
		
		sql.append(processoParteExpedienteDAO.limitarRepresentacao(isProcuradoria, atuacaoProcurador, idLocalizacao));
		sql.append(processoParteExpedienteDAO.limitarVisibilidade(isProcuradoria));
		sql.append(processoParteExpedienteDAO.limitarExpedientesPorSituacao(tipoSituacaoExpediente));

		sql.append("GROUP BY ptf.id_jurisdicao, cx_exp.id_caixa_adv_proc ");

		return sql.toString();
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
	public List<CaixaAdvogadoProcuradorVO> obterCaixasExpedientesJurisdicao(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisa) {
		
		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		
		String consultaExpedientesConsolidada = ComponentUtil.getComponent(ProcessoParteExpedienteDAO.class)
				.obterQueryConsolidadaIdsExpedientes(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa);
		
		List<String> consultasCaixas = new ArrayList<String>(0);
		
		// Se o usuário náo é gestor e é procurador ou assistente:
		// Deve buscar as caixas do procurador padrão (caixa) e também as caixas do procurador distribuidor (jurisdicao).
		if (!isProcuradorGestor && (TipoUsuarioExternoEnum.P.equals(tipoUsuarioExterno) || TipoUsuarioExternoEnum.AP.equals(tipoUsuarioExterno)) ) {
			consultasCaixas.add(this.obterQueryCaixasExpedientesProcuradorCaixa(idLocalizacao, idJurisdicao, idPessoa));
			consultasCaixas.add(this.obterQueryCaixasExpedientesProcuradorJurisdicao(idLocalizacao, idJurisdicao, idPessoa, idProcuradoria));
		} else {
			consultasCaixas.add(this.obterQueryCaixasExpedientesUsuarioLocalizacao(idLocalizacao, idJurisdicao));
		}

		String consultaCaixaExpedientes = this.obterQueryCaixasExpedientes(consultasCaixas, consultaExpedientesConsolidada, criteriosPesquisa.getApenasCaixasComResultados());
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(consultaCaixaExpedientes);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		List<CaixaAdvogadoProcuradorVO> result = new ArrayList<CaixaAdvogadoProcuradorVO>(resultList.size());

		for (Object[] borderTypes: resultList) {
			Integer idCaixa = (Integer)borderTypes[0];
			String nomeCaixa = (String)borderTypes[1];
			String descricaoCaixa = (String)borderTypes[2];
			Integer idJurisdicaoCaixa = (Integer)borderTypes[3];
			String nomeJurisdicaoCaixa = (String)borderTypes[4];
			Boolean isAdmin = (Integer)borderTypes[5] == 0 ? false : true;
			Boolean isAtivo = (Boolean)borderTypes[6];
			BigInteger contadorJurisdicao = (BigInteger)borderTypes[7];			
			
			result.add(new CaixaAdvogadoProcuradorVO(idCaixa, nomeCaixa, descricaoCaixa, 
					idJurisdicaoCaixa, nomeJurisdicaoCaixa, isAdmin, isAtivo, contadorJurisdicao));
		}
		return result;
	}
	
	private String obterQueryCaixasExpedientes(List<String> consultasCaixas, String consultaExpedientesConsolidada, Boolean apenasCaixasComResultados) {
		StringBuilder consultaCaixasExpedientes = new StringBuilder();

		consultaCaixasExpedientes.append("SELECT id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao ")
			.append(", MAX(in_admin_caixa) as admin_caixa, in_ativo, count(DISTINCT consulta_expedientes.id_processo_parte_expediente) as contador FROM (");
		
		consultaCaixasExpedientes.append("SELECT id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao ")
				.append(", in_admin_caixa, in_ativo, id_processo_parte_expediente FROM (");
		
		int count = 0;
		for (String consulta : consultasCaixas) {
			if (count > 0) {
				consultaCaixasExpedientes.append(" UNION ALL ");
			}
			consultaCaixasExpedientes.append("SELECT id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao ")
				.append(", in_admin_caixa, in_ativo, id_processo_parte_expediente ")
				.append(" FROM (")
			 	.append(consulta)
			 	.append(") tabela_intermediaria"+ count );
			
			 	count++;
		}
		consultaCaixasExpedientes.append( ") consulta_caixas_intermediaria_consolidada ")
			.append(" ) consulta_caixas_consolidada ");
		if(apenasCaixasComResultados) {
			consultaCaixasExpedientes.append(" INNER JOIN ( ");
		}else {
			consultaCaixasExpedientes.append(" LEFT JOIN ( ");
		}
		consultaCaixasExpedientes
			.append(consultaExpedientesConsolidada)
			.append( ") consulta_expedientes  ON (consulta_expedientes.id_processo_parte_expediente = consulta_caixas_consolidada.id_processo_parte_expediente) ")
			.append(" GROUP BY id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao, in_ativo ")
			.append(" ORDER BY nm_caixa ASC, contador DESC");

		return consultaCaixasExpedientes.toString();
	}
	
	/**
	 * Busca todas as caixas de uma dada jurisdição, dada a localização do usuário
	 * este usuário será admin das caixas
	 * 
	 * @param idLocalizacao
	 * @param idJurisdicao
	 * @return
	 */
	private String obterQueryCaixasExpedientesUsuarioLocalizacao(Integer idLocalizacao, Integer idJurisdicao) {
		StringBuilder consultaCaixas = new StringBuilder("SELECT cx.id_caixa_adv_proc, cx.nm_caixa, cx.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ")
				.append(", administracao_caixa.in_admin_caixa ")
				.append(", CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo")
				.append(", cx_exp.id_processo_parte_expediente ")
				.append(" FROM tb_caixa_adv_proc cx ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = cx.id_jurisdicao) ")
				.append(" LEFT JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) ")
				.append(" LEFT JOIN ( " + this.obterQueryInatividadeCaixa() + " ) cx_inativa ON (cx_inativa.id_caixa_adv_proc = cx.id_caixa_adv_proc)")
				.append(this.obterQueryAdminJurisdicao(true))
				.append(" WHERE cx.id_jurisdicao = "+idJurisdicao)
				.append(" AND cx.id_localizacao = "+ idLocalizacao);

		return consultaCaixas.toString();
	}
	
	/**
	 * Busca todas as caixas de uma jurisdicao, dada a localização do usuário que esteja vinculado a uma jurisdição e seja procurador
	 * Este usuário será admin das caixas
	 * 
	 * @param idLocalizacao
	 * @param idJurisdicao
	 * @param idPessoa
	 * @param idProcuradoria
	 * @return
	 */
	private String obterQueryCaixasExpedientesProcuradorJurisdicao(Integer idLocalizacao, Integer idJurisdicao, Integer idPessoa, Integer idProcuradoria) {
		StringBuilder consultaCaixas = new StringBuilder("SELECT cx.id_caixa_adv_proc, cx.nm_caixa, cx.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ")
				.append(", administracao_caixa.in_admin_caixa ")
				.append(", CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo")
				.append(", cx_exp.id_processo_parte_expediente ")
				.append(" FROM tb_caixa_adv_proc cx ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = cx.id_jurisdicao) ")
				.append(" LEFT JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) ")
				// Tabela que vincula o usuário à jurisdição
				.append(" INNER JOIN tb_pess_proc_jurisdicao ppj ON (ppj.id_jurisdicao = cx.id_jurisdicao AND ppj.in_ativo = true) ")
				.append(" INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ")
				.append(" LEFT JOIN ( " + this.obterQueryInatividadeCaixa() + " ) cx_inativa ON (cx_inativa.id_caixa_adv_proc = cx.id_caixa_adv_proc)")
				.append(this.obterQueryAdminJurisdicao(true))
				.append(" WHERE cx.id_jurisdicao = "+idJurisdicao)
				.append(" AND cx.id_localizacao = "+ idLocalizacao)
				.append(" AND pproc.id_pessoa = "+ idPessoa)
				.append(" AND pproc.id_procuradoria = "+ idProcuradoria);
						
		return consultaCaixas.toString();
	}

	/**
	 * Busca apenas as caixas em que o usuário esteja vinculado, dada uma jurisdição e a localização do usuario
	 * 
	 * @param idLocalizacao
	 * @param idJurisdicao
	 * @param idPessoa
	 * @return
	 */
	private String obterQueryCaixasExpedientesProcuradorCaixa(Integer idLocalizacao, Integer idJurisdicao, Integer idPessoa) {
		StringBuilder consultaCaixas = new StringBuilder("SELECT cx.id_caixa_adv_proc, cx.nm_caixa, cx.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ")
				.append(", administracao_caixa.in_admin_caixa ")
				.append(", CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo")
				.append(", cx_exp.id_processo_parte_expediente ")
				.append(" FROM tb_caixa_adv_proc cx ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = cx.id_jurisdicao) ")
				.append(" LEFT JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) ")
				.append(" INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
				.append(" LEFT JOIN ( " + this.obterQueryInatividadeCaixa() + " ) cx_inativa ON (cx_inativa.id_caixa_adv_proc = cx.id_caixa_adv_proc)")
				.append(this.obterQueryAdminJurisdicao(false))
				.append(" WHERE cx.id_jurisdicao = "+idJurisdicao)
				.append(" AND cx.id_localizacao = "+ idLocalizacao)
				.append(" AND cx_rep.id_pessoa_fisica = "+ idPessoa);

		return consultaCaixas.toString();		
	}
	
	private String obterQueryInatividadeCaixa() {
		StringBuilder consultaCaixas = new StringBuilder("SELECT DISTINCT cx_in.id_caixa_adv_proc FROM tb_periodo_inativ_caixa_rep cx_in ")
				.append(" WHERE CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim OR (cx_in.dt_inicio <= CURRENT_TIMESTAMP AND cx_in.dt_fim IS NULL) ");
		return consultaCaixas.toString();
	}
	
	private String obterQueryAdminJurisdicao(Boolean isAdminCaixa) {
		Integer valorIsAdmin = 0;
		if(isAdminCaixa) {
			valorIsAdmin = 1;
		}
		return "INNER JOIN (SELECT "+valorIsAdmin+" in_admin_caixa) as administracao_caixa ON (1=1) ";
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
	public List<CaixaAdvogadoProcuradorVO> obterCaixasAcervoJurisdicao(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisa) {
		
		Jurisdicao jur = new Jurisdicao();
		jur.setIdJurisdicao(idJurisdicao);
		criteriosPesquisa.setJurisdicao(jur);
		
		String consultaProcessosConsolidada = ComponentUtil.getComponent(ProcessoTrfDAO.class)
				.obterQueryConsolidadaIdsProcessos(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa);
		
		List<String> consultasCaixas = new ArrayList<String>(0);
		
		// Deve buscar as caixas do procurador padrão (caixa) e também as caixas do procurador distribuidor (jurisdicao).
		if (!isProcuradorGestor && (TipoUsuarioExternoEnum.P.equals(tipoUsuarioExterno) || TipoUsuarioExternoEnum.AP.equals(tipoUsuarioExterno)) ) {
			consultasCaixas.add(this.obterQueryCaixasProcessosProcuradorCaixa(idLocalizacao, idJurisdicao, idPessoa, false));
			consultasCaixas.add(this.obterQueryCaixasProcessosProcuradorJurisdicao(idLocalizacao, idJurisdicao, idPessoa, idProcuradoria, true));
		}else {
			consultasCaixas.add(this.obterQueryCaixasProcessosUsuarioLocalizacao(idLocalizacao, idJurisdicao, true));
		}

		String consultaCaixaProcessso = this.obterQueryCaixasProcessos(consultasCaixas, consultaProcessosConsolidada, criteriosPesquisa.getApenasCaixasComResultados());
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(consultaCaixaProcessso);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		List<CaixaAdvogadoProcuradorVO> result = new ArrayList<CaixaAdvogadoProcuradorVO>(resultList.size());

		for (Object[] borderTypes: resultList) {
			Integer idCaixa = (Integer)borderTypes[0];
			String nomeCaixa = (String)borderTypes[1];
			String descricaoCaixa = (String)borderTypes[2];
			Integer idJurisdicaoCaixa = (Integer)borderTypes[3];
			String nomeJurisdicaoCaixa = (String)borderTypes[4];
			Boolean isAdmin = (Integer)borderTypes[5] == 0 ? false : true;
			Boolean isAtivo = (Boolean)borderTypes[6];
			BigInteger contadorJurisdicao = (BigInteger)borderTypes[7];			
			
			result.add(new CaixaAdvogadoProcuradorVO(idCaixa, nomeCaixa, descricaoCaixa, 
					idJurisdicaoCaixa, nomeJurisdicaoCaixa, isAdmin, isAtivo, contadorJurisdicao));
		}
		return result;
	}
	
	/**
	 * 
	 * @param consultasCaixas
	 * @param consultaExpedientesConsolidada
	 * @return
	 */
	private String obterQueryCaixasProcessos(List<String> consultasCaixas, String consultaProcessosConsolidada, Boolean apenasCaixasComResultados) {
		StringBuilder consultaCaixas = new StringBuilder();

		consultaCaixas.append("SELECT id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao ")
			.append(", MAX(in_admin_caixa) as admin_caixa, in_ativo, count(DISTINCT consulta_processos.id_processo_trf) as contador FROM (");
		
		consultaCaixas.append("SELECT id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao ")
				.append(", in_admin_caixa, in_ativo, id_processo_trf FROM (");
		
		int count = 0;
		for (String consulta : consultasCaixas) {
			if (count > 0) {
				consultaCaixas.append(" UNION ALL ");
			}
			consultaCaixas.append("SELECT id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao ")
				.append(", in_admin_caixa, in_ativo, id_processo_trf ")
				.append(" FROM (")
			 	.append(consulta)
			 	.append(") tabela_intermediaria"+ count );
			
			 	count++;
		}
		consultaCaixas.append( ") consulta_caixas_intermediaria_consolidada ")
			.append(" ) consulta_caixas_consolidada ");
		if(apenasCaixasComResultados) {
			consultaCaixas.append(" INNER JOIN ( ");
		}else {
			consultaCaixas.append(" LEFT JOIN ( ");
		}
		// ordena a lista de caixas: primeiro as caixas com resultado, depois por ordem alfabetica
		consultaCaixas	
			.append(consultaProcessosConsolidada)
			.append( ") consulta_processos  ON (consulta_processos.id_processo_trf = consulta_caixas_consolidada.id_processo_trf) ")
			.append(" GROUP BY id_caixa_adv_proc, nm_caixa, ds_caixa, id_jurisdicao, ds_jurisdicao, in_ativo ")
			.append(" ORDER BY nm_caixa ASC, contador DESC");

		return consultaCaixas.toString();
	}
	
	/**
	 * Busca todas as caixas de uma dada jurisdição, dada a localização do usuário
	 * 
	 * @param idLocalizacao
	 * @param idJurisdicao
	 * @return
	 */
	private String obterQueryCaixasProcessosUsuarioLocalizacao(Integer idLocalizacao, Integer idJurisdicao, Boolean isAdminCaixa) {
		StringBuilder consultaCaixas = new StringBuilder("SELECT cx.id_caixa_adv_proc, cx.nm_caixa, cx.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ")
				.append(", administracao_caixa.in_admin_caixa ")
				.append(", CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo")
				.append(", cx_proc.id_processo_trf ")
				.append(" FROM tb_caixa_adv_proc cx ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = cx.id_jurisdicao) ")
				.append(" LEFT JOIN tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
				.append(" LEFT JOIN ( " + this.obterQueryInatividadeCaixa() + " ) cx_inativa ON (cx_inativa.id_caixa_adv_proc = cx.id_caixa_adv_proc)")
				.append(this.obterQueryAdminJurisdicao(isAdminCaixa))
				.append(" WHERE cx.id_jurisdicao = "+idJurisdicao)
				.append(" AND cx.id_localizacao = "+ idLocalizacao);

		return consultaCaixas.toString();
	}
	
	/**
	 * Busca todas as caixas de uma jurisdicao, dada a localização do usuário que esteja vinculado a uma jurisdição e seja procurador
	 * 
	 * @param idLocalizacao
	 * @param idJurisdicao
	 * @param idPessoa
	 * @param idProcuradoria
	 * @return
	 */
	private String obterQueryCaixasProcessosProcuradorJurisdicao(Integer idLocalizacao, Integer idJurisdicao, Integer idPessoa, Integer idProcuradoria, Boolean isAdminCaixa) {
		StringBuilder consultaCaixas = new StringBuilder("SELECT cx.id_caixa_adv_proc, cx.nm_caixa, cx.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ")
				.append(", administracao_caixa.in_admin_caixa ")
				.append(", CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo")
				.append(", cx_proc.id_processo_trf ")
				.append(" FROM tb_caixa_adv_proc cx ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = cx.id_jurisdicao) ")
				.append(" LEFT JOIN tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
				// Tabela que vincula o usuário à jurisdição
				.append(" INNER JOIN tb_pess_proc_jurisdicao ppj ON (ppj.id_jurisdicao = cx.id_jurisdicao AND ppj.in_ativo = true) ")
				.append(" INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ")
				.append(" LEFT JOIN ( " + this.obterQueryInatividadeCaixa() + " ) cx_inativa ON (cx_inativa.id_caixa_adv_proc = cx.id_caixa_adv_proc)")
				.append(this.obterQueryAdminJurisdicao(isAdminCaixa))
				.append(" WHERE cx.id_jurisdicao = "+idJurisdicao)
				.append(" AND cx.id_localizacao = "+ idLocalizacao)
				.append(" AND pproc.id_pessoa = "+ idPessoa)
				.append(" AND pproc.id_procuradoria = "+ idProcuradoria);
						
		return consultaCaixas.toString();
	}

	/**
	 * Busca apenas as caixas em que o usuário esteja vinculado, dada uma jurisdição e a localização do usuario
	 * 
	 * @param idLocalizacao
	 * @param idJurisdicao
	 * @param idPessoa
	 * @return
	 */
	private String obterQueryCaixasProcessosProcuradorCaixa(Integer idLocalizacao, Integer idJurisdicao, Integer idPessoa, Boolean isAdminCaixa) {
		StringBuilder consultaCaixas = new StringBuilder("SELECT cx.id_caixa_adv_proc, cx.nm_caixa, cx.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ")
				.append(", administracao_caixa.in_admin_caixa ")
				.append(", CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo")
				.append(", cx_proc.id_processo_trf ")
				.append(" FROM tb_caixa_adv_proc cx ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = cx.id_jurisdicao) ")
				.append(" LEFT JOIN tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
				.append(" INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
				.append(" LEFT JOIN ( " + this.obterQueryInatividadeCaixa() + " ) cx_inativa ON (cx_inativa.id_caixa_adv_proc = cx.id_caixa_adv_proc)")
				.append(this.obterQueryAdminJurisdicao(isAdminCaixa))
				.append(" WHERE cx.id_jurisdicao = "+idJurisdicao)
				.append(" AND cx.id_localizacao = "+ idLocalizacao)
				.append(" AND cx_rep.id_pessoa_fisica = "+ idPessoa);

		return consultaCaixas.toString();		
	}

}
