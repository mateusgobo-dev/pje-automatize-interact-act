package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.infox.pje.dao.ProcessoTrfDAO;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.IntervaloNumeroSequencialProcessoVO;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ArquivoAssinatura;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CabecalhoProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TagDTO;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.dto.FiltroProcessoSessaoDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.CompetenciaAreaDireito;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name("processoJudicialDAO")
public class ProcessoJudicialDAO extends BaseDAO<ProcessoTrf> {

	private static final int BIG_QUERY_SIZE = 1500;
	private static final String ID_LOCALIZACAO_FISICA = "idLocalizacaoFisica";
	private static final String AND = " AND ( ";
	private static final String TAG_IN_SISTEMA_TRUE = " tag.in_sistema = true ";
	private static final String OR_TAG_IN_PUBLICA_TRUE = " OR tag.in_publica = true ";
	private static final String OR_TAG_ID_LOCALIZACAO = " OR tag.id_localizacao = :idLocalizacaoFisica ";
	private static final String NAO_DEFINIDO = "não definido";
	private static final String AND_ID_TAREFA_TASK_INSTANCE = "AND proctar.id_processo_tarefa = :idTaskInstance ";
	private static final String ID_TASKINSTANCE = "idTaskInstance";
	private static final String SELECT_PROC_VISIBILIDADE_SEGREDO = "(SELECT 1 FROM tb_proc_visibilida_segredo vis ";
	private static final String ID_USUARIO = "idUsuario";
	private static final String NIVEL_ACESSO_USUARIO = "nivelAcessoUsuario";

	@In(create = true)
	private ProcessoTrfDAO processoTrfDAO;

	public List<ProcessoTrf> findByNU(String nu) throws PJeDAOException{
		if (nu.length() != 20){
			String message = String.format(
					"O número de processo informato [%s] tem número de caracteres diferente de 20.", nu);
			throw new IllegalArgumentException(message);
		}
		try {
			Integer numeroSequencia = Integer.parseInt(nu.substring(0, 7));
			Integer numeroDigitoVerificador = Integer.parseInt(nu.substring(7, 9));
			Integer ano = Integer.parseInt(nu.substring(9, 13));
			Integer segmento = Integer.parseInt(nu.substring(13, 14));
			Integer tribunal = Integer.parseInt(nu.substring(14, 16));
			Integer numeroOrigem = Integer.parseInt(nu.substring(16));
			return findByNumeracaoUnica(numeroSequencia, numeroDigitoVerificador, ano, segmento, tribunal,
					numeroOrigem);
		} catch (Exception e) {
			String message = String.format("Exception thrown when trying to access ProcessoTrf with NU [%s]: %s : %s",
					nu, e.getClass(), e.getLocalizedMessage());
			throw new PJeDAOException(message, e);
		}
	}

	public List<ProcessoTrf> findByNumeroDV(String numeroDV) {
		Integer numeroSequencia = Integer.parseInt(numeroDV.substring(0, numeroDV.length() - 2));
		Integer numeroDigitoVerificador = Integer.parseInt(numeroDV.substring(numeroDV.length() - 2));
		return this.findByNumeracaoUnica(numeroSequencia, numeroDigitoVerificador, null, null, null, null);
	}

	public List<ProcessoTrf> findByNumeroAno(String nu) {
		int tamanho = nu.length();
		if (tamanho < 7) {
			String message = String.format(
					"A consulta por numeração única parcial com ano exige pelo menos 7 caracteres [número dado: %s]",
					nu);
			throw new IllegalArgumentException(message);
		}
		Integer numero = Integer.parseInt(nu.substring(0, tamanho - 6));
		Integer dv = Integer.parseInt(nu.substring(tamanho - 6, tamanho - 4));
		Integer ano = Integer.parseInt(nu.substring(tamanho - 2));
		return this.findByNumeracaoUnica(numero, dv, ano, null, null, null);
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> findByNumeracaoUnica(Integer numero, Integer dv, Integer ano, Integer segmento,
			Integer tribunal, Integer origem) {
		if (numero == null && dv == null && ano == null && (segmento == null || tribunal == null) && origem == null) {
			throw new IllegalArgumentException(
					"A consulta segundo a numeração única exige ao menos um dos campos de sua composição.");
		}
		StringBuilder queryStr = new StringBuilder("SELECT p FROM ProcessoTrf AS p WHERE 1 = 1");
		if (numero != null)
			queryStr.append("	AND p.numeroSequencia = :numeroSequencia ");
		if (dv != null)
			queryStr.append("		AND p.numeroDigitoVerificador = :numeroDigitoVerificador ");
		Integer numeroOrgaoJustica = null;
		if (ano != null)
			queryStr.append("		AND p.ano = :ano ");
		if (segmento != null && tribunal != null) {
			numeroOrgaoJustica = segmento * 100 + tribunal;
			queryStr.append("		AND p.numeroOrgaoJustica = :numeroOrgaoJustica");
		}
		if (origem != null)
			queryStr.append("		AND p.numeroOrigem = :numeroOrigem");
		Query q = this.entityManager.createQuery(queryStr.toString());
		if (numero != null)
			q.setParameter("numeroSequencia", numero);
		if (dv != null)
			q.setParameter("numeroDigitoVerificador", dv);
		if (ano != null)
			q.setParameter("ano", ano);
		if (numeroOrgaoJustica != null)
			q.setParameter("numeroOrgaoJustica", numeroOrgaoJustica);
		if (origem != null)
			q.setParameter("numeroOrigem", origem);
		return q.getResultList();
	}

	@Override
	public Integer getId(ProcessoTrf e) {
		return e.getIdProcessoTrf();
	}

	/**
	 * Recupera a lista de identificadores dos fluxos de processo de negócio ativos
	 * vinculados a um processo judicial, filtrando por idOrgaoJulgador caso
	 * informado.
	 * 
	 * @param processoJudicial o processo cujos fluxos se pretende recuperar.
	 * @param idOrgaoJulgador  parâmetro opcional para filtrar somente fluxos do
	 *                         órgão julgador em questão
	 * @return a lista de fluxos ativos
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getBusinessProcessIds(ProcessoTrf processoJudicial, List<Integer> idsLocalizacoes) {
		StringBuilder query = new StringBuilder(
				"SELECT pi.idProcessoInstance FROM ProcessoInstance AS pi WHERE pi.idProcesso = :idProcesso AND pi.ativo = true ");

		if (idsLocalizacoes != null) {
			query.append(" and pi.idLocalizacao IN (:idsLocalizacoes) ");
		}

		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", processoJudicial.getIdProcessoTrf());

		if (idsLocalizacoes != null) {
			q.setParameter("idsLocalizacoes", idsLocalizacoes);
		}

		return (List<Long>) q.getResultList();
	}

	/**
	 * Verifica se o processo possui um determinado código de assunto de acordo com
	 * a tabela unificada de assuntos.
	 * 
	 * @param processo
	 * @param codigo   código do assunto processual
	 * @return true se houver o vínculo
	 * @category PJEII-3650
	 */
	public boolean possuiAssunto(ProcessoTrf processo, String codigo) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(pa.idProcessoAssunto) FROM ProcessoAssunto pa ");
		sb.append("WHERE pa.processoTrf = :processo AND pa.assuntoTrf.codAssuntoTrf = :codAssunto");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", processo);
		q.setParameter("codAssunto", codigo);

		Long qtde = (Long) q.getSingleResult();

		return qtde > 0;
	}

	/**
	 * Ajusta o orgao julgador do fluxo, pois com a alteracao do substituti eventual
	 * caso o processo seja redistribuido o 'ProcessoInstance' deve ser ajustado
	 * para apontar para o novo orgao julgador
	 * 
	 * @param idProcessoTrf
	 * @category PJEII-7481
	 */
	@SuppressWarnings("unchecked")
	public void ajustarFluxo(final Integer idProcessoTrf) {
		EntityManager entityManager = getEntityManager();

		// Verifica se o processo existe
		ProcessoTrf processoTrf = entityManager.find(ProcessoTrf.class, idProcessoTrf);
		if (processoTrf != null) {
			// Pega todas as instancias de fluxo para o processo informado
			Query querySituacaoProcesso = entityManager
					.createQuery("SELECT s FROM SituacaoProcesso AS s WHERE s.idProcesso = :idProcesso");
			querySituacaoProcesso.setParameter("idProcesso", idProcessoTrf);
			List<SituacaoProcesso> listSituacaoProcesso = querySituacaoProcesso.getResultList();
			// Percorre as instancias de fluxo
			for (SituacaoProcesso situacaoProcesso : listSituacaoProcesso) {
				Long idProcessInstance = situacaoProcesso.getIdProcessInstance();
				Query queryProcessoInstance = entityManager.createQuery(
						"SELECT p FROM ProcessoInstance AS p WHERE p.idProcessoInstance = :idProcessoInstance");
				queryProcessoInstance.setParameter("idProcessoInstance", idProcessInstance);
				List<ProcessoInstance> listProcessoInstance = queryProcessoInstance.getResultList();
				ajustaProcessosInstance(listProcessoInstance, processoTrf);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void ajustarFluxo(final Integer idProcessoTrf, Integer idLocalizacao, Integer idOrgaoJulgadorCargo,
			Integer idOrgaoJulgadorColegiado) {
		Query q = entityManager.createQuery("SELECT p FROM ProcessoInstance AS p WHERE p.idProcesso = :idProcesso "
				+ " and p.idLocalizacao = :idLocalizacao " + " and p.orgaoJulgadorCargo = :idOrgaoJulgadorCargo"
				+ " and p.orgaoJulgadorColegiado = :idOrgaoJulgadorColegiado");
		q.setParameter("idProcesso", idProcessoTrf);
		q.setParameter("idLocalizacao", idLocalizacao);
		q.setParameter("idOrgaoJulgadorCargo", idOrgaoJulgadorCargo);
		q.setParameter("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		List<ProcessoInstance> listProcessoInstance = q.getResultList();
		ProcessoTrf processoTrf = entityManager.find(ProcessoTrf.class, idProcessoTrf);
		ajustaProcessosInstance(listProcessoInstance, processoTrf);

	}

	private void ajustaProcessosInstance(List<ProcessoInstance> listProcessoInstance, ProcessoTrf processoTrf) {
		for (ProcessoInstance processoInstance : listProcessoInstance) {
			processoInstance.setIdLocalizacao(processoTrf.getOrgaoJulgador().getLocalizacao().getIdLocalizacao());
			processoInstance.setOrgaoJulgadorCargo(processoTrf.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
			processoInstance.setOrgaoJulgadorColegiado(processoTrf.getOrgaoJulgadorColegiado() != null
					? processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado()
					: null);
			entityManager.merge(processoInstance);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Pessoa> getMagistradosAtuantes(ProcessoTrf processo) {
		String q = "SELECT DISTINCT a.pessoa FROM ProcessoDocumentoBinPessoaAssinatura AS a "
				+ "	INNER JOIN a.processoDocumentoBin.processoDocumentoList AS d "
				+ "	WHERE d.processoTrf.idProcessoTrf = :idProcesso"
				+ "		AND EXISTS (SELECT 1 FROM PessoaMagistrado AS m WHERE m.id = a.pessoa.idPessoa)";
		Query query = getEntityManager().createQuery(q);
		query.setParameter("idProcesso", processo.getIdProcessoTrf());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, BigInteger> getContadoresPorJurisdicao(Integer idPessoa, Integer idProcuradoria,
			RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador) {

		Map<Integer, BigInteger> res = new HashMap<Integer, BigInteger>(0);
		boolean isProcuradoria = idProcuradoria != null ? true : false;
		int idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual() != null ? Authenticator.getIdLocalizacaoAtual()
				: 0;
		idProcuradoria = idProcuradoria == null ? 0 : idProcuradoria;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT processosAgrupados.id_jurisdicao, count(1) ");
		sql.append("FROM ( ");
		sql.append("SELECT ptf.id_jurisdicao, count(0) ");
		sql.append("FROM client.tb_jurisdicao jr "
				+ "JOIN client.tb_processo_trf ptf ON (jr.id_jurisdicao = ptf.id_jurisdicao) "
				+ "LEFT JOIN client.tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_processo_trf = ptf.id_processo_trf) "
				+ "LEFT JOIN client.tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cx_proc.id_caixa_adv_proc) "
				+ "LEFT JOIN client.tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		if (isProcuradoria) {
			sql.append(
					"LEFT JOIN client.tb_pess_proc_jurisdicao ppj ON (ppj.id_jurisdicao = ptf.id_jurisdicao AND ppj.in_ativo = true) "
							+ "LEFT JOIN client.tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ");
		}
		sql.append(
				"LEFT JOIN client.tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') "
						+ "LEFT JOIN client.tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.in_situacao = 'A') "
						+ "WHERE ptf.cd_processo_status = 'D' " + "AND :idPessoa = :idPessoa ");
		sql.append(limitarRepresentacao(isProcuradoria, atuacaoProcurador, idLocalizacaoAtual));
		sql.append(limitarVisibilidade(isProcuradoria));
		sql.append(" GROUP BY ptf.id_jurisdicao, ptf.id_processo_trf ");
		sql.append(" ) AS processosAgrupados " + "GROUP BY processosAgrupados.id_jurisdicao ");

		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());

		q.setParameter("idProcuradoria", idProcuradoria);

		if (idPessoa != null) {
			q.setParameter("idPessoa", idPessoa);
		}

		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			res.put((Integer) borderTypes[0], (BigInteger) borderTypes[1]);
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> getProcessosJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa,
			Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, Integer idLocalizacao,
			Search search) {

		Collection<Criteria> criteriosPesquisa = (search == null ? null : search.getCriterias().values());
		Map<String, Order> orderBy = (search == null ? null : search.getOrders());
		int first = (search == null ? 0 : search.getFirst());
		int max = (search == null ? 0 : search.getMax());

		List<ProcessoTrf> processos = new ArrayList<ProcessoTrf>(0);
		StringBuilder sql = new StringBuilder();
		Map<String, Object> parametrosPesquisa = new HashMap<String, Object>();

		boolean isProcuradoria = idProcuradoria != null ? true : false;
		int idLocalizacaoFisica = Authenticator.getLocalizacaoFisicaAtual() != null
				? Authenticator.getLocalizacaoFisicaAtual().getIdLocalizacao()
				: 0;
		idProcuradoria = idProcuradoria == null ? 0 : idProcuradoria;

		sql.append(
				"SELECT DISTINCT ptf.id_processo_trf, ptf.nr_processo, ptf.nr_sequencia, ptf.nr_ano, ptf.dt_distribuicao, ptf.in_prioridade, ");
		sql.append(
				"ptf.nm_pessoa_autor, ptf.qt_autor, ptf.nm_pessoa_reu, ptf.qt_reu, ptf.id_orgao_julgador_colegiado, ptf.ds_orgao_julgador_colegiado, ");
		sql.append(
				"ptf.id_orgao_julgador, ptf.ds_orgao_julgador, ptf.id_classe_judicial, ptf.ds_classe_judicial_sigla, ptf.ds_classe_judicial, ptf.in_segredo_justica, ptf.vl_peso_prioridade ");
		sql.append("FROM client.tb_jurisdicao jr "
				+ "JOIN client.tb_cabecalho_processo ptf ON (jr.id_jurisdicao = ptf.id_jurisdicao) "
				+ "LEFT JOIN client.tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_processo_trf = ptf.id_processo_trf) "
				+ "LEFT JOIN client.tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cx_proc.id_caixa_adv_proc) "
				+ "LEFT JOIN client.tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		if (isProcuradoria) {
			sql.append(
					"LEFT JOIN client.tb_pess_proc_jurisdicao ppj ON (ppj.id_jurisdicao = ptf.id_jurisdicao AND ppj.in_ativo = true) "
							+ "LEFT JOIN client.tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ");
		}
		sql.append(
				"LEFT JOIN client.tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') "
						+ "LEFT JOIN client.tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.in_situacao = 'A') "
						+ "WHERE ptf.cd_processo_status = 'D' " + "AND jr.id_jurisdicao = :idJurisdicao "
						+ "AND :idPessoa = :idPessoa ");
		sql.append(limitarRepresentacao(isProcuradoria, atuacaoProcurador, idLocalizacaoFisica));
		sql.append(limitarVisibilidade(isProcuradoria));
		sql.append(limitarProcessosEmCaixa(idCaixa));
		sql.append(limitarCriteriosPesquisa(criteriosPesquisa, parametrosPesquisa));
		sql.append(limitarOrdenacao(orderBy));

		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		q.setFirstResult(first);
		if (max != 0) {
			q.setMaxResults(max);
		}

		for (String key : parametrosPesquisa.keySet()) {
			q.setParameter(key, parametrosPesquisa.get(key));
		}

		q.setParameter("idJurisdicao", idJurisdicao);

		if (idCaixa == null) {
			q.setParameter("idLocalizacaoFisica", idLocalizacaoFisica);
		}

		q.setParameter("idProcuradoria", idProcuradoria);

		if (idPessoa != null) {
			q.setParameter("idPessoa", idPessoa);

		}

		if (idCaixa != null) {
			q.setParameter("idCaixa", idCaixa);
		}

		/**
		 * ------------------------------- Ordem dos campos no resultList:
		 * ------------------------------- 0 - id_processo_trf 1 - nr_processo 2 -
		 * nr_sequencia 3 - nr_ano 4 - dt_distribuicao 5 - in_prioridade 6 -
		 * nm_pessoa_autor 7 - qt_autor 8 - nm_pessoa_reu 9 - qt_reu 10 -
		 * id_orgao_julgador_colegiado 11 - ds_orgao_julgador_colegiado 12 -
		 * id_orgao_julgador 13 - ds_orgao_julgador 14 - id_classe_judicial 15 -
		 * ds_classe_judicial_sigla 16 - ds_classe_judicial 17 - in_segredo_justica 18 -
		 * vl_peso_prioridade
		 */
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			ProcessoTrf ptf = new ProcessoTrf();
			Processo pro = new Processo();
			ConsultaProcessoTrfSemFiltro cpt = new ConsultaProcessoTrfSemFiltro();
			ClasseJudicial cla = new ClasseJudicial();

			ptf.setIdProcessoTrf((Integer) borderTypes[0]);
			pro.setNumeroProcesso((String) borderTypes[1]);
			// ptf.setNumeroSequencia(((BigInteger)borderTypes[2]).intValue());
			// ptf.setAno(((BigInteger)borderTypes[3]).intValue());
			ptf.setDataDistribuicao((java.util.Date) borderTypes[4]);
			cpt.setPrioridade((Boolean) borderTypes[5]);
			cpt.setAutor((String) borderTypes[6]);
			cpt.setQtAutor(((BigInteger) borderTypes[7]).longValue());
			cpt.setReu((String) borderTypes[8]);
			cpt.setQtReu(((BigInteger) borderTypes[9]).longValue());
			cpt.setIdOrgaoJulgadorColegiado((Integer) borderTypes[10]);
			cpt.setOrgaoJulgadorColegiado((String) borderTypes[11]);
			cpt.setIdOrgaoJulgador((Integer) borderTypes[12]);
			cpt.setOrgaoJulgador((String) borderTypes[13]);
			cla.setIdClasseJudicial((Integer) borderTypes[14]);
			cla.setClasseJudicialSigla((String) borderTypes[15]);
			cla.setClasseJudicial((String) borderTypes[16]);

			ptf.setSegredoJustica((Boolean) borderTypes[17]);
			ptf.setProcesso(pro);
			ptf.setConsultaProcessoTrf(cpt);
			ptf.setClasseJudicial(cla);

			processos.add(ptf);
		}
		return processos;
	}

	public Long getCountProcessosJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa,
			Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, Integer idLocalizacao,
			Search search) {

		List<ProcessoTrf> processos = getProcessosJurisdicao(idJurisdicao, idCaixa, idPessoa, idProcuradoria,
				atuacaoProcurador, idLocalizacao, search);

		return (long) processos.size();
	}

	private String limitarProcessosEmCaixa(Integer idCaixa) {
		StringBuilder sql = new StringBuilder();
		if (idCaixa == null) {
			sql.append("AND NOT EXISTS ");
			sql.append("( ");
			sql.append("   SELECT 1 ");
			sql.append("   FROM tb_processo_caixa_adv_proc cxproc ");
			sql.append("   INNER JOIN tb_caixa_adv_proc cx ON cx.id_caixa_adv_proc = cxproc.id_caixa_adv_proc ");
			sql.append("   WHERE cxproc.id_processo_trf = ptf.id_processo_trf ");
			sql.append("   AND cx.id_jurisdicao = :idJurisdicao ");
			sql.append("   AND cx.id_localizacao = :idLocalizacaoFisica ");
			sql.append(") ");
		} else {
			sql.append("AND EXISTS ");
			sql.append("( ");
			sql.append("   SELECT 1 ");
			sql.append("   FROM tb_processo_caixa_adv_proc cxproc ");
			sql.append("   WHERE cxproc.id_processo_trf = ptf.id_processo_trf ");
			sql.append("   AND cxproc.id_caixa_adv_proc = :idCaixa ");
			sql.append(") ");
		}
		return sql.toString();
	}

	public String limitarRepresentacao(boolean isProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador,
			int idLocalizacaoAtual) {
		StringBuilder sql = new StringBuilder();

		sql.append(" AND ( (" + isProcuradoria + " = true ");
		if (isProcuradoria) {
			sql.append("      AND (pp.id_procuradoria = :idProcuradoria) " + "         AND ('" + atuacaoProcurador
					+ "' = '" + RepresentanteProcessualTipoAtuacaoEnum.G + "' "
					+ "              OR ((cx_rep.id_pessoa_fisica = :idPessoa"
					+ "                   AND cx.id_localizacao = " + idLocalizacaoAtual + ") "
					+ "                   AND NOT EXISTS (SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in "
					+ "                                   WHERE cx_in.id_caixa_adv_proc = cx.id_caixa_adv_proc "
					+ "                                   AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim "
					+ "                                   GROUP BY cx_in.id_caixa_adv_proc ) ) "
					+ "              OR (pproc.id_pessoa = :idPessoa "
					+ "                  AND pproc.id_procuradoria = :idProcuradoria "
					+ "                  AND (EXISTS (SELECT 1 FROM client.tb_pess_proc_jurisdicao ppj "
					+ "                              JOIN client.tb_pessoa_procuradoria pp ON (pp.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) "
					+ "                              WHERE ppj.in_ativo = true "
					+ "                              AND ppj.id_jurisdicao = ptf.id_jurisdicao "
					+ "                              AND pp.id_pessoa = :idPessoa "
					+ "                              AND pp.id_procuradoria = :idProcuradoria "
					+ "                              GROUP BY ppj.id_jurisdicao ) "
					+ "                       OR EXISTS " + "                             (SELECT 1 "
					+ "                              FROM tb_caixa_representante cairep "
					+ "                              INNER JOIN tb_caixa_adv_proc cai ON cai.id_caixa_adv_proc = cairep.id_caixa_adv_proc "
					+ "                              WHERE cai.id_jurisdicao = ptf.id_jurisdicao "
					+ "                              AND cairep.id_pessoa_fisica = :idPessoa "
					+ "                              AND cai.id_localizacao = " + idLocalizacaoAtual + " "
					+ "                              GROUP BY cai.id_caixa_adv_proc) ) " + "                 ) ) ");

		}
		sql.append("      ) " + " OR (" + isProcuradoria + " = false " + "      AND (pp.id_pessoa = :idPessoa "
				+ "      OR ((ppr.id_representante = :idPessoa "
				+ "           OR ( EXISTS (SELECT 1 FROM core.tb_usuario_localizacao ul "
				+ "                        JOIN client.tb_pessoa_localizacao pl ON (ul.id_localizacao_fisica = pl.id_localizacao "
				+ "                                                                 AND ul.id_usuario != pl.id_pessoa) "
				+ "                        WHERE ppr.id_representante=pl.id_pessoa "
				+ "                        AND ul.id_usuario=:idPessoa "
				+ "                        AND ul.id_localizacao_fisica=" + idLocalizacaoAtual + " "
				+ "  ) ) ) ) ) ) ) ");

		return sql.toString();
	}

	public String limitarVisibilidade(boolean isProcuradoria) {
		StringBuilder query = new StringBuilder();
		query.append(" AND (ptf.in_segredo_justica = false OR EXISTS (");
		query.append(" SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_processo_trf = ptf.id_processo_trf ");
		query.append(" AND (vis.id_pessoa = :idPessoa ");

		if (isProcuradoria) {
			query.append(" OR (vis.id_pessoa = pp.id_pessoa AND vis.id_procuradoria = :idProcuradoria) ");
		} else {
			query.append(" OR (vis.id_pessoa = pp.id_pessoa AND ppr.id_representante = :idPessoa) ");
		}
		query.append("))) ");

		return query.toString();
	}

	public String limitarVisibilidade(Integer idPessoa, TipoUsuarioExternoEnum tipoUsuarioExterno,
			Integer idProcuradoria, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();
		query.append(" AND (ptf.in_segredo_justica = false OR EXISTS (");
		query.append(" SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_processo_trf = ptf.id_processo_trf ");
		query.append(" AND (vis.id_pessoa = :idPessoa ");
		if (tipoUsuarioExterno.equals(TipoUsuarioExternoEnum.P)
				|| tipoUsuarioExterno.equals(TipoUsuarioExternoEnum.AP)) {
			query.append(" OR (vis.id_pessoa = pp.id_pessoa AND vis.id_procuradoria = :idProcuradoria) ");
			if (!params.containsKey("idProcuradoria")) {
				params.put("idProcuradoria", idProcuradoria);
			}
		} else if (tipoUsuarioExterno.equals(TipoUsuarioExternoEnum.A)
				|| tipoUsuarioExterno.equals(TipoUsuarioExternoEnum.AA)) {
			query.append(" OR (vis.id_pessoa = pp.id_pessoa AND ppr.id_representante = :idPessoa) ");
		}
		query.append("))) ");

		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}

		return query.toString();
	}

	private String limitarCriteriosPesquisa(Collection<Criteria> criteriosPesquisa,
			Map<String, Object> parametrosPesquisa) {
		if (criteriosPesquisa != null && criteriosPesquisa.size() > 0) {
			StringBuilder sbPesquisa = new StringBuilder();
			return loadNativeCriterias(sbPesquisa, criteriosPesquisa, parametrosPesquisa);
		}
		return "";
	}

	private String limitarOrdenacao(Map<String, Order> orderBy) {
		StringBuilder sql = new StringBuilder();

		sql.append(" ORDER BY ");

		if (orderBy != null && orderBy.size() > 0) {
			StringBuilder order = new StringBuilder();
			for (String key : orderBy.keySet()) {
				if (order.length() > 0) {
					order.append(", ");
				}
				order.append(key + " " + orderBy.get(key).toString());
			}
			sql.append(order.toString());
		} else {
			sql.append("ptf.dt_ultimo_movimento DESC");
		}
		return sql.toString();
	}

	private String loadNativeCriterias(StringBuilder sb, Collection<Criteria> criterias, Map<String, Object> params) {
		String str = null;
		if (criterias != null && criterias.size() > 0) {
			super.loadCriterias(sb, criterias, params);
			str = translateToNative(sb.toString());
		}
		return str;
	}

	private String translateToNative(String str) {
		Map<String, String> columns = new LinkedHashMap<String, String>();
		columns.put("o.processoTrf.dataDistribuicao", "ptf.dt_distribuicao");
		columns.put("o.processoTrf.orgaoJulgador.idOrgaoJulgador", "ptf.id_orgao_julgador");
		columns.put("o.processoTrf.classeJudicial.classeJudicialSigla", "ptf.ds_classe_judicial_sigla");
		columns.put("o.processoTrf.classeJudicial.classeJudicial", "ptf.ds_classe_judicial");
		columns.put("o.processoTrf.classeJudicial.codClasseJudicial", "ptf.cd_classe_judicial");
		columns.put("o.processoTrf.numeroSequencia", "ptf.nr_sequencia");
		columns.put("o.processoTrf.numeroDigitoVerificador", "ptf.nr_digito_verificador");
		columns.put("o.processoTrf.ano", "ptf.nr_ano");
		columns.put("o.processoTrf.numeroOrgaoJustica", "ptf.nr_identificacao_orgao_justica");
		columns.put("o.processoTrf.numeroOrigem", "ptf.nr_origem_processo");

		// ** colunas que necessitam de queries específicas: não possuem mapeamento
		// direto.
		columns.put(".idPrioridadeProcesso", "");
		columns.put(".assuntoTrf", "");
		columns.put(".numeroDocumento", "");
		columns.put(".pessoa.nome", "");
		columns.put(".numeroOAB", "");

		for (String key : columns.keySet()) {
			if (str.contains(key)) {
				if (key.equals(".idPrioridadeProcesso")) {
					str = replaceCriteriaPrioridadeProcesso(str);
				}
				if (key.equals(".assuntoTrf")) {
					str = replaceCriteriaAssunto(str);
				} else if (key.equals(".pessoa.nome")) {
					str = replaceCriteriaParte(str);
				} else if (key.equals(".numeroDocumento")) {
					str = replaceCriteriaCpfCnpj(str);
				} else if (key.equals(".numeroOAB")) {
					str = replaceCriteriaOab(str);
				} else {
					str = str.replaceAll(key, columns.get(key).toString());
				}
			}
		}

		return " AND " + str + " ";
	}

	private String replaceCriteriaPrioridadeProcesso(String str) {
		String key = ".idPrioridadeProcesso";
		String content = getCriteriaSubstring(str, key);
		String paramPattern = ":prm";

		int posParam1 = content.indexOf(paramPattern);

		String param1 = content.substring(posParam1);

		StringBuilder sql = new StringBuilder();
		sql.append("EXISTS ( ");
		sql.append("SELECT 1 FROM tb_proc_prioridde_processo ppp ");
		sql.append("WHERE ppp.id_processo_trf = ptf.id_processo_trf ");
		sql.append("AND ppp.id_prioridade_processo = " + param1 + ") ");

		return str.replace(content, sql.toString());
	}

	private String replaceCriteriaAssunto(String str) {
		String key = ".assuntoTrf";
		String content = getCriteriaSubstring(str, key);
		String paramPattern = ":prm";

		int posParam1 = content.indexOf(paramPattern);
		int posParam2 = content.lastIndexOf(paramPattern);

		String param1 = content.substring(posParam1);
		param1 = param1.substring(0, param1.indexOf(" "));

		String param2 = content.substring(posParam2);
		param2 = param2.substring(0, param2.indexOf(")"));

		StringBuilder sql = new StringBuilder();
		sql.append("EXISTS ( ");
		sql.append("SELECT 1 FROM tb_processo_assunto pa ");
		sql.append("INNER JOIN tb_assunto_trf ass ON pa.id_assunto_trf = ass.id_assunto_trf ");
		sql.append("WHERE pa.id_processo_trf = ptf.id_processo_trf ");
		sql.append("AND (ass.cd_assunto_trf = " + param1 + " ");
		sql.append("OR LOWER(to_ascii(ass.ds_assunto_trf)) LIKE LOWER(to_ascii(" + param2 + ")) )) ");

		return str.replace(content, sql.toString());
	}

	private String replaceCriteriaParte(String str) {
		String key = ".pessoa.nome";

		String content = getCriteriaSubstring(str, key);

		String paramPattern = ":prm";
		int posParam1 = content.indexOf(paramPattern);
		int posParam2 = content.lastIndexOf(paramPattern);

		String param1 = content.substring(posParam1);
		param1 = param1.substring(0, param1.indexOf(" "));

		String param2 = content.substring(posParam2);
		param2 = param2.substring(0, param2.indexOf(")"));

		StringBuilder sql = new StringBuilder();
		sql.append("EXISTS ( ");
		sql.append("SELECT 1 ");
		sql.append("FROM tb_processo_parte pp ");
		sql.append("INNER JOIN tb_usuario_login ul ON pp.id_pessoa = ul.id_usuario ");
		sql.append(
				"LEFT JOIN tb_pess_doc_identificacao pdi ON pdi.id_pessoa = ul.id_usuario AND pdi.in_ativo = true AND pdi.in_usado_falsamente = false ");
		sql.append("WHERE pp.in_situacao = 'A' ");
		sql.append("AND pp.id_processo_trf = ptf.id_processo_trf ");
		sql.append("AND (LOWER(to_ascii(ul.ds_nome)) LIKE LOWER(to_ascii(" + param1
				+ ")) OR LOWER(to_ascii(pdi.ds_nome_pessoa)) LIKE LOWER(to_ascii(" + param2 + "))) ) ");

		return str.replace(content, sql.toString());
	}

	private String replaceCriteriaCpfCnpj(String str) {
		String key = ".numeroDocumento";

		String content = getCriteriaSubstring(str, key);

		String paramPattern = ":prm";
		int posParam = content.indexOf(paramPattern);

		String param = content.substring(posParam);

		StringBuilder sql = new StringBuilder();
		sql.append("EXISTS ( ");
		sql.append("SELECT 1 ");
		sql.append("FROM tb_processo_parte pp ");
		sql.append("INNER JOIN tb_pess_doc_identificacao pdi ON pdi.id_pessoa = pp.id_pessoa ");
		sql.append("WHERE pp.id_processo_trf = ptf.id_processo_trf ");
		sql.append("AND pp.in_situacao = 'A' ");
		sql.append("AND pdi.in_ativo = true ");
		sql.append("AND pdi.in_usado_falsamente = false ");
		sql.append("AND pdi.cd_tp_documento_identificacao IN ('CPF','CPJ') ");
		sql.append("AND pdi.nr_documento_identificacao = " + param + " ) ");

		return str.replace(content, sql.toString());
	}

	/**
	 * Método responsável por retornar uma lista de órgãos Julgadores filtrados por
	 * OrgaoJulgadorColegiado e Jurisdicao.
	 * 
	 * @param ojc        Objeto da classe OrgaoJulgadorColegiado. Caso seja nulo, é
	 *                   ignorado.
	 * @param jurisdicao Objeto da classe Jurisdicao. Caso seja nulo, é ignorado.
	 * @return Lista de OrgaoJulgador filtrados conforme os valores informados nos
	 *         parâmetros.
	 */

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorListPorOjcJurisdicao(OrgaoJulgadorColegiado ojc, Jurisdicao jurisdicao) {
		StringBuilder sb = new StringBuilder();

		sb.append("select o from OrgaoJulgador o where o.ativo = true ");

		if (ojc != null) {
			sb.append(" and o in (select ojc.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ojc where ");
			sb.append(" ojc.orgaoJulgadorColegiado = :orgaoJulgadorColegiado) ");
		}

		if (jurisdicao != null) {
			sb.append(" and o.jurisdicao = :jurisdicao ");
		}

		sb.append(
				" ORDER BY CASE WHEN o.orgaoJulgador >= 'A' THEN upper((o.orgaoJulgador)) ELSE to_char(to_number(o.orgaoJulgador, '999'),'000') END, upper((o.orgaoJulgador)) ");

		EntityManager em = EntityUtil.getEntityManager();

		Query query = em.createQuery(sb.toString());
		if (ojc != null) {
			query.setParameter("orgaoJulgadorColegiado", ojc);
		}

		if (jurisdicao != null) {
			query.setParameter("jurisdicao", jurisdicao);
		}

		return query.getResultList();
	}

	private String replaceCriteriaOab(String str) {
		String key = ".numeroOAB";

		String content = getCriteriaSubstring(str, key);

		String paramPattern = ":prm";
		int posParam = content.indexOf(paramPattern);

		String param = content.substring(posParam);

		StringBuilder sql = new StringBuilder();
		sql.append("EXISTS ( ");
		sql.append("SELECT 1 ");
		sql.append("FROM tb_processo_parte pp ");
		sql.append("INNER JOIN tb_pess_doc_identificacao pdi ON pdi.id_pessoa = pp.id_pessoa ");
		sql.append("WHERE pp.id_processo_trf = ptf.id_processo_trf ");
		sql.append("AND pp.in_situacao = 'A' ");
		sql.append("AND pdi.in_ativo = true ");
		sql.append("AND pdi.in_usado_falsamente = false ");
		sql.append("AND pdi.cd_tp_documento_identificacao = 'OAB' ");
		sql.append("AND pdi.nr_documento_identificacao LIKE " + param + " ) ");

		return str.replace(content, sql.toString());
	}

	private String getCriteriaSubstring(String str, String key) {
		String conector = "AND ";

		int posKey = str.indexOf(key);
		int posInitConector = str.substring(0, posKey).lastIndexOf(conector);
		int posLastConector = str.substring(posKey).indexOf(conector);

		posInitConector = (posInitConector > -1 ? posInitConector : 0);
		posLastConector = (posLastConector > -1 ? posLastConector : 0);

		int begin = (posInitConector > 0 ? posInitConector + conector.length() : 0);
		int end = (posLastConector > 0 ? posLastConector + posKey : str.length());

		return str.substring(begin, end);
	}

	public String queryVisualizadorProcuradoria(String prefix, Integer idProcuradoria) {
		String hqlExists = "select 1 from " + " ProcessoVisibilidadeSegredo pvs "
				+ " where pvs.processo.idProcesso = o." + prefix + "idProcessoTrf "
				+ " and pvs.procuradoria.idProcuradoria = " + idProcuradoria + " "
				+ " and pvs.procuradoria.ativo =true";
		return hqlExists;
	}

	public String queryRepresentanteDeVisualizador(String prefix) {
		String hqlExists = "select 1 from " + "ProcessoVisibilidadeSegredo pvs, ProcessoParteRepresentante ppr "
				+ " where pvs.processo.idProcesso = o." + prefix + "idProcessoTrf "
				+ "   and pvs.idPessoa = ppr.processoParte.idPessoa " + "   and ppr.representante.idPessoa = "
				+ Authenticator.getIdUsuarioLogado();

		return hqlExists;
	}

	public List<ProcessoTrf> consultarProcessos(Search search) {
		return list(search);
	}

	public Long getCountProcessosJurisdicao(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);

		return this.getCountProcessos(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor,
				criteriosPesquisaGeral, searchLocal);
	}

	public Long getCountProcessosJurisdicaoCaixa(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			Integer idJurisdicao, Integer idCaixa, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);
		criteriosPesquisaGeral.setIdCaixaAdvProc(idCaixa);
		criteriosPesquisaGeral.setApenasSemCaixa(false);

		return this.getCountProcessos(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor,
				criteriosPesquisaGeral, searchLocal);
	}

	public List<ProcessoTrf> getProcessosJurisdicao(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);

		return this.getProcessosPainelExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria,
				isProcuradorGestor, criteriosPesquisaGeral, searchLocal);
	}

	public List<ProcessoTrf> getProcessosJurisdicaoCaixa(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			Integer idJurisdicao, Integer idCaixa, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);
		criteriosPesquisaGeral.setIdCaixaAdvProc(idCaixa);
		criteriosPesquisaGeral.setApenasSemCaixa(false);

		return this.getProcessosPainelExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria,
				isProcuradorGestor, criteriosPesquisaGeral, searchLocal);
	}

	public List<JurisdicaoVO> getJurisdicoesAcervo(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			ConsultaProcessoVO criteriosPesquisa) {

		List<JurisdicaoVO> jurisdicoes = new ArrayList<JurisdicaoVO>(0);
		Map<String, Object> parametrosPesquisa = new HashMap<String, Object>(0);

		StringBuilder query = new StringBuilder(
				"SELECT ptf.id_jurisdicao, ptf.ds_jurisdicao, MAX(in_admin_jurisdicao), COUNT(DISTINCT ptf.id_processo_trf) ");

		query.append(obterQueryUsuarioExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria,
				isProcuradorGestor, criteriosPesquisa, parametrosPesquisa));

		query = new StringBuilder(
				aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisa, parametrosPesquisa));

		query.append(limitarVisibilidade(idPessoa, tipoUsuarioExterno, idProcuradoria, parametrosPesquisa));

		query.append(" GROUP BY ptf.id_jurisdicao, ptf.ds_jurisdicao ");
		query.append(" ORDER BY ptf.ds_jurisdicao");

		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());

		for (String key : parametrosPesquisa.keySet()) {
			q.setParameter(key, parametrosPesquisa.get(key));
		}

		@SuppressWarnings("unchecked")
		List<Object[]> r = q.getResultList();
		for (Object[] o : r) {
			JurisdicaoVO vo = new JurisdicaoVO();
			boolean isAdmin = (o[2] != null && ((Integer) o[2]) == 1 ? true : false);
			vo.setId((Integer) o[0]);
			vo.setDescricao((String) o[1]);
			vo.setAdmin(isAdmin);
			vo.setContador((BigInteger) o[3]);
			jurisdicoes.add(vo);
		}
		return jurisdicoes;
	}

	public List<CaixaAdvogadoProcuradorVO> getCaixasAcervoJurisdicao(Integer idPessoa, Integer idLocalizacaoFisica,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisa) {

		Map<String, Object> parametrosPesquisa = new HashMap<String, Object>(0);
		final boolean apenasCaixasAtivas = criteriosPesquisa.getApenasCaixasAtivas();
		boolean isProcuradoria = idProcuradoria != null && idProcuradoria > 0;
		boolean numeroProcessoInformado = criteriosPesquisa.getNumeroProcesso() != null;

		criteriosPesquisa.setIdJurisdicao(null);

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT caixa.id_caixa_adv_proc, caixa.nm_caixa, caixa.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao, ");
		query.append("MAX(in_admin_jurisdicao) AS admin_caixa, ");
		if (apenasCaixasAtivas) {
			query.append(" TRUE AS in_ativo, ");
		} else {
			query.append(" CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo, ");
		}
		query.append("COUNT (DISTINCT ptf.id_processo_trf) ");
		query.append("FROM tb_caixa_adv_proc caixa ");
		query.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = caixa.id_jurisdicao) ");

		if (!isProcuradoria || isProcuradorGestor) {
			query.append(this.obterQueryAdminJurisdicao(true));
		} else {
			query.append("INNER JOIN ");
			query.append("( ");
			query.append("  SELECT CASE WHEN COUNT(1) > 0 THEN 1 ELSE 0 END AS in_admin_jurisdicao ");
			query.append("  FROM tb_pess_proc_jurisdicao ppj ");
			query.append(
					"  INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria AND pproc.id_procuradoria = :idProcuradoria AND pproc.id_pessoa = :idPessoa) ");
			query.append("  WHERE ppj.in_ativo IS TRUE ");
			query.append("  AND ppj.id_jurisdicao = :idJurisdicao ");
			query.append(") AS administracao_jurisdicao ON (1=1) ");

			if (!parametrosPesquisa.containsKey("idPessoa")) {
				parametrosPesquisa.put("idPessoa", idPessoa);
			}
		}

		String innerOuLeftJoin = criteriosPesquisa.getApenasCaixasComResultados() ? " INNER " : " LEFT ";

		query.append(innerOuLeftJoin
				+ " JOIN tb_processo_caixa_adv_proc cx_proc ON (cx_proc.id_caixa_adv_proc = caixa.id_caixa_adv_proc) ");

		query.append(innerOuLeftJoin + obterQueryProcessos(idPessoa, tipoUsuarioExterno, idProcuradoria,
				criteriosPesquisa, parametrosPesquisa, isProcuradoria, numeroProcessoInformado));

		query.append(innerOuLeftJoin + " JOIN tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf) ");

		query.append(obterQueryCaixasUsuarioExterno(idPessoa, idLocalizacaoFisica, tipoUsuarioExterno, idProcuradoria,
				isProcuradorGestor, parametrosPesquisa));

		if (apenasCaixasAtivas) {
			StringBuilder whereApenasCaixasAtivas = new StringBuilder();

			whereApenasCaixasAtivas.append(" AND NOT EXISTS ( ")
					.append(" 		SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in ")
					.append("		WHERE cx_in.id_caixa_adv_proc = caixa.id_caixa_adv_proc ").append("		AND ( ")
					.append("			 (CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim ) ")
					.append("             or (cx_in.dt_inicio < current_timestamp and cx_in.dt_fim is null) ")
					.append("		) ").append(" ) ");

			query.append(whereApenasCaixasAtivas.toString());

		} else {
			StringBuilder join = new StringBuilder();

			join.append(" LEFT JOIN ");
			join.append(" (SELECT DISTINCT cx_in.id_caixa_adv_proc ");
			join.append("  FROM tb_periodo_inativ_caixa_rep cx_in ");
			join.append("  WHERE CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim ");
			join.append("  OR (cx_in.dt_inicio <= CURRENT_TIMESTAMP AND cx_in.dt_fim IS NULL) ) ");
			join.append(" AS cx_inativa ON (cx_inativa.id_caixa_adv_proc = caixa.id_caixa_adv_proc) ");
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), join.toString(), "WHERE (1=1)"));
		}

		query = new StringBuilder(
				aplicarFiltros(query.toString(), idLocalizacaoFisica, criteriosPesquisa, parametrosPesquisa));

		query.append(" AND caixa.id_localizacao = :idLocalizacaoFisica ");
		query.append(" AND caixa.id_jurisdicao = :idJurisdicao ");
		query.append(
				" GROUP BY caixa.id_caixa_adv_proc, caixa.nm_caixa, caixa.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ");
		if (!apenasCaixasAtivas) {
			query.append(" , cx_inativa.id_caixa_adv_proc ");
		}
		query.append(" ORDER BY caixa.nm_caixa");

		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());

		if (!parametrosPesquisa.containsKey("idLocalizacaoFisica")) {
			parametrosPesquisa.put("idLocalizacaoFisica", idLocalizacaoFisica);
		}

		if (!parametrosPesquisa.containsKey("idJurisdicao")) {
			parametrosPesquisa.put("idJurisdicao", idJurisdicao);
		}

		if (isProcuradoria) {
			if (!parametrosPesquisa.containsKey("idProcuradoria")) {
				parametrosPesquisa.put("idProcuradoria", idProcuradoria);
			}
		}

		for (String key : parametrosPesquisa.keySet()) {
			q.setParameter(key, parametrosPesquisa.get(key));
		}

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		List<CaixaAdvogadoProcuradorVO> result = new ArrayList<CaixaAdvogadoProcuradorVO>(resultList.size());

		for (Object[] borderTypes : resultList) {
			Integer idCaixa = (Integer) borderTypes[0];
			String nomeCaixa = (String) borderTypes[1];
			String descricaoCaixa = (String) borderTypes[2];
			Integer idJurisdicaoCaixa = (Integer) borderTypes[3];
			String nomeJurisdicaoCaixa = (String) borderTypes[4];
			Boolean isAdmin = (Integer) borderTypes[5] == 0 ? false : true;
			Boolean isAtivo = (Boolean) borderTypes[6];
			BigInteger contadorJurisdicao = (BigInteger) borderTypes[7];

			result.add(new CaixaAdvogadoProcuradorVO(idCaixa, nomeCaixa, descricaoCaixa, idJurisdicaoCaixa,
					nomeJurisdicaoCaixa, isAdmin, isAtivo, contadorJurisdicao));
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	private Long getCountProcessos(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno,
			Integer idProcuradoria, boolean isProcuradorGestor, ConsultaProcessoVO criteriosPesquisaGeral,
			Search searchLocal) {

		Map<String, Object> parametrosPesquisa = new HashMap<String, Object>(0);

		StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT ptf.id_processo_trf) ");
		query.append(obterQueryUsuarioExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria,
				isProcuradorGestor, criteriosPesquisaGeral, parametrosPesquisa));

		query = new StringBuilder(
				aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisaGeral, parametrosPesquisa));

		query.append(limitarVisibilidade(idPessoa, tipoUsuarioExterno, idProcuradoria, parametrosPesquisa));

		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());

		for (String key : parametrosPesquisa.keySet()) {
			q.setParameter(key, parametrosPesquisa.get(key));
		}

		Long contagemTotal = 0L;
		List r = q.getResultList();
		if (r != null && r.get(0) != null) {
			contagemTotal = ((BigInteger) r.get(0)).longValue();
		}
		return contagemTotal;
	}

	private List<ProcessoTrf> getProcessosPainelExterno(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) {

		Map<String, Order> orderBy = (searchLocal == null ? null : searchLocal.getOrders());
		int first = (searchLocal == null ? 0 : searchLocal.getFirst());
		int max = (searchLocal == null ? 0 : (searchLocal.getMax() == null ? 0 : searchLocal.getMax()));

		Map<String, Object> parametrosPesquisa = new HashMap<String, Object>(0);

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT DISTINCT ptf.id_processo_trf, ptf.nr_processo, ptf.nr_sequencia, ptf.nr_ano, ptf.dt_distribuicao, ptf.in_prioridade, ");
		query.append(
				"ptf.nm_pessoa_autor, ptf.qt_autor, ptf.nm_pessoa_reu, ptf.qt_reu, ptf.id_orgao_julgador_colegiado, ptf.ds_orgao_julgador_colegiado, ");
		query.append(
				"ptf.id_orgao_julgador, ptf.ds_orgao_julgador, ptf.id_classe_judicial, ptf.ds_classe_judicial_sigla, ptf.ds_classe_judicial, ptf.in_segredo_justica, ");
		query.append("ptf.vl_peso_prioridade, ptf.ds_ultimo_movimento, ptf.dt_ultimo_movimento ");

		query.append(obterQueryUsuarioExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria,
				isProcuradorGestor, criteriosPesquisaGeral, parametrosPesquisa));

		query = new StringBuilder(
				aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisaGeral, parametrosPesquisa));

		query.append(limitarVisibilidade(idPessoa, tipoUsuarioExterno, idProcuradoria, parametrosPesquisa));

		query.append(limitarOrdenacao(orderBy));

		List<ProcessoTrf> processosPainel = new ArrayList<ProcessoTrf>(0);

		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());

		// Limita paginação pesquisa.
		q.setFirstResult(first);
		if (max != 0) {
			q.setMaxResults(max);
		}

		for (String key : parametrosPesquisa.keySet()) {
			q.setParameter(key, parametrosPesquisa.get(key));
		}

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			processosPainel.add(montaObjetoProcessoTrf(borderTypes));
		}
		return processosPainel;
	}

	private ProcessoTrf montaObjetoProcessoTrf(Object[] borderTypes) {
		ProcessoTrf ptf = new ProcessoTrf();
		Processo pro = new Processo();
		ConsultaProcessoTrfSemFiltro cpt = new ConsultaProcessoTrfSemFiltro();
		ClasseJudicial cla = new ClasseJudicial();

		ptf.setIdProcessoTrf((Integer) borderTypes[0]);
		pro.setNumeroProcesso((String) borderTypes[1]);
		ptf.setDataDistribuicao((java.util.Date) borderTypes[4]);
		cpt.setPrioridade((Boolean) borderTypes[5]);
		cpt.setAutor((String) borderTypes[6]);
		cpt.setQtAutor(((BigInteger) borderTypes[7]).longValue());
		cpt.setReu((String) borderTypes[8]);
		cpt.setQtReu(((BigInteger) borderTypes[9]).longValue());
		cpt.setIdOrgaoJulgadorColegiado((Integer) borderTypes[10]);
		cpt.setOrgaoJulgadorColegiado((String) borderTypes[11]);
		cpt.setIdOrgaoJulgador((Integer) borderTypes[12]);
		cpt.setOrgaoJulgador((String) borderTypes[13]);
		cpt.setUltimoMovimento(borderTypes[19] != null ? borderTypes[19].toString() : "");
		try {
			cpt.setDataUltimoMovimento(borderTypes[20] != null
					? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(borderTypes[20].toString())
					: null);
		} catch (ParseException e) {
			cpt.setDataUltimoMovimento(null);
		}
		cla.setIdClasseJudicial((Integer) borderTypes[14]);
		cla.setClasseJudicialSigla((String) borderTypes[15]);
		cla.setClasseJudicial((String) borderTypes[16]);

		ptf.setSegredoJustica((Boolean) borderTypes[17]);
		ptf.setProcesso(pro);
		ptf.setConsultaProcessoTrf(cpt);
		ptf.setClasseJudicial(cla);
		return ptf;
	}

	private String aplicarFiltros(String q, Integer idLocalizacaoFisica, ConsultaProcessoVO criteriosPesquisa,
			Map<String, Object> params) {
		StringBuilder query = new StringBuilder(q);

		if (criteriosPesquisa.getIdJurisdicao() != null) {
			query.append(" AND ptf.id_jurisdicao = :idJurisdicao ");
			if (!params.containsKey("idJurisdicao")) {
				params.put("idJurisdicao", criteriosPesquisa.getIdJurisdicao());
			}
		}

		// busca por um número do processo dado
		if (criteriosPesquisa.getNumeroProcesso() != null) {
			String or = criteriosPesquisa.getApenasCaixasComResultados() ? "" : " OR ptf.nr_processo IS NULL";
			query.append(" AND (ptf.nr_processo = :numeroProcesso " + or + ") ");
			if (!params.containsKey("numeroProcesso")) {
				params.put("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
			}
		} else {
			if (criteriosPesquisa.getNumerosProcessos() != null) {
				String or = criteriosPesquisa.getApenasCaixasComResultados() ? "" : " OR ptf.nr_processo IS NULL";
				query.append(" AND (ptf.nr_processo IN (:numerosProcessos) " + or + ") ");
				if (!params.containsKey("numerosProcessos")) {
					params.put("numerosProcessos", criteriosPesquisa.getNumerosProcessos());
				}
			} else {
				if (criteriosPesquisa.getNumeroSequencia() != null) {
					query.append(" AND ptf.nr_sequencia = :numeroSequencia ");
					if (!params.containsKey("numeroSequencia")) {
						params.put("numeroSequencia", criteriosPesquisa.getNumeroSequencia());
					}
				} else {
					if (!criteriosPesquisa.getIntervalosNumerosSequenciais().isEmpty()) {
						List<IntervaloNumeroSequencialProcessoVO> intervalosNumerosSequenciais = criteriosPesquisa
								.getIntervalosNumerosSequenciais();
						int numIntervaloSequencial = 0;
						StringBuilder queryIntervalosNrSeq = new StringBuilder(" AND ( 1=0 ");

						for (IntervaloNumeroSequencialProcessoVO intervaloNrSequenciais : intervalosNumerosSequenciais) {
							if (intervaloNrSequenciais.getIntervaloValido()) {
								String nomeParamTamanho = "tamanhoIntervalo" + numIntervaloSequencial;
								String nomeParamSeqInicio = "sequencialInicio" + numIntervaloSequencial;
								String nomeParamSeqTermino = "sequencialFim" + numIntervaloSequencial;
								queryIntervalosNrSeq.append(
										" OR CAST(SUBSTR(lpad(cast(ptf.nr_sequencia as text), 7, '0'), length(lpad(cast(ptf.nr_sequencia as text), 7, '0') ) - (cast(:"
												+ nomeParamTamanho + " AS INTEGER)  - 1), :" + nomeParamTamanho
												+ ") AS INTEGER) BETWEEN :" + nomeParamSeqInicio + " AND :"
												+ nomeParamSeqTermino + "");

								if (!params.containsKey(nomeParamTamanho)) {
									params.put(nomeParamTamanho, intervaloNrSequenciais.getTamanhoString());
								}
								if (!params.containsKey(nomeParamSeqInicio)) {
									params.put(nomeParamSeqInicio, intervaloNrSequenciais.getSequenciaInicial());
								}
								if (!params.containsKey(nomeParamSeqTermino)) {
									params.put(nomeParamSeqTermino, intervaloNrSequenciais.getSequenciaFinal());
								}

								numIntervaloSequencial++;
							}
						}
						queryIntervalosNrSeq.append(" ) ");
						if (numIntervaloSequencial > 0) {
							query.append(queryIntervalosNrSeq);
						}
					}
				}

				if (criteriosPesquisa.getDigitoVerificador() != null) {
					query.append(" AND ptf.nr_digito_verificador = :digitoVerificador ");
					if (!params.containsKey("digitoVerificador")) {
						params.put("digitoVerificador", criteriosPesquisa.getDigitoVerificador());
					}
				}
				if (criteriosPesquisa.getNumeroAno() != null) {
					query.append(" AND ptf.nr_ano = :numeroAno ");
					if (!params.containsKey("numeroAno")) {
						params.put("numeroAno", criteriosPesquisa.getNumeroAno());
					}
				}
				if (criteriosPesquisa.getNumeroOrgaoJustica() != null) {
					query.append(" AND ptf.nr_identificacao_orgao_justica = :numeroOrgaoJustica ");
					if (!params.containsKey("numeroOrgaoJustica")) {
						params.put("numeroOrgaoJustica", criteriosPesquisa.getNumeroOrgaoJustica());
					}
				}
				if (criteriosPesquisa.getNumeroOrigem() != null) {
					query.append(" AND ptf.nr_origem_processo = :numeroOrigem ");
					if (!params.containsKey("numeroOrigem")) {
						params.put("numeroOrigem", criteriosPesquisa.getNumeroOrigem());
					}
				}
			}
		}

		// busca pelo assunto
		if (criteriosPesquisa.getAssuntoJudicialObj() != null
				&& criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto() > 0) {
			String inner = " INNER JOIN tb_processo_assunto pas ON (pas.id_processo_trf = ptf.id_processo_trf) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND pas.id_assunto_trf = :idAssunto ");
			if (!params.containsKey("idAssunto")) {
				params.put("idAssunto", criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto());
			}
		} else {
			if (!criteriosPesquisa.getAssuntoTrfList().isEmpty()) {
				String inner = " INNER JOIN tb_processo_assunto pas ON (pas.id_processo_trf = ptf.id_processo_trf) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));

				int numAssunto = 0;
				StringBuilder queryListaAssuntos = new StringBuilder(" AND ( 1=0 ");
				for (AssuntoTrf assuntoTrf : criteriosPesquisa.getAssuntoTrfList()) {
					String nomeParamIdAssuntoTrf = "idAssunto" + numAssunto;
					queryListaAssuntos.append(" OR pas.id_assunto_trf = :" + nomeParamIdAssuntoTrf);
					if (!params.containsKey(nomeParamIdAssuntoTrf)) {
						params.put(nomeParamIdAssuntoTrf, assuntoTrf.getIdAssuntoTrf());
					}
					numAssunto++;
				}
				queryListaAssuntos.append(" ) ");
				if (numAssunto > 0) {
					query.append(queryListaAssuntos);
				}
			} else {
				if (criteriosPesquisa.getAssuntoJudicial() != null) {
					String inner = " INNER JOIN tb_processo_assunto pas ON (pas.id_processo_trf = ptf.id_processo_trf) "
							+ " INNER JOIN tb_assunto_trf assunto ON (pas.id_assunto_trf = assunto.id_assunto_trf) ";

					query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));

					query.append(" AND ( ").append("assunto.cd_assunto_trf = :codigoAssunto ")
							.append(" OR LOWER(TO_ASCII(assunto.ds_assunto_trf)) LIKE LOWER(TO_ASCII(:nomeAssunto)) ")
							.append(") ");

					if (!params.containsKey("codigoAssunto")) {
						params.put("codigoAssunto", criteriosPesquisa.getAssuntoJudicial());
					}
					if (!params.containsKey("nomeAssunto")) {
						params.put("nomeAssunto", "%" + criteriosPesquisa.getAssuntoJudicial() + "%");
					}
				}
			}
		}

		// busca pela classe
		if (criteriosPesquisa.getClasseJudicialObj() != null
				&& criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial() > 0) {
			query.append(" AND ptf.id_classe_judicial = :idClasse ");
			if (!params.containsKey("idClasse")) {
				params.put("idClasse", criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial());
			}
		} else {
			if (!criteriosPesquisa.getClasseJudicialList().isEmpty()) {
				int numClasse = 0;
				StringBuilder queryListaClasses = new StringBuilder(" AND ( 1=0 ");
				for (ClasseJudicial classeJudicial : criteriosPesquisa.getClasseJudicialList()) {
					String nomeParamIdClasseJudicial = "idClasse" + numClasse;
					queryListaClasses.append(" OR ptf.id_classe_judicial = :" + nomeParamIdClasseJudicial);
					if (!params.containsKey(nomeParamIdClasseJudicial)) {
						params.put(nomeParamIdClasseJudicial, classeJudicial.getIdClasseJudicial());
					}

					numClasse++;
				}
				queryListaClasses.append(" ) ");
				if (numClasse > 0) {
					query.append(queryListaClasses);
				}
			} else {
				if (criteriosPesquisa.getClasseJudicial() != null) {
					query.append(" AND ( ").append("ptf.cd_classe_judicial = :codigoClasse ")
							.append(" OR LOWER(ptf.ds_classe_judicial_sigla) = LOWER(:siglaClasse) ")
							.append(" OR LOWER(TO_ASCII(ptf.ds_classe_judicial)) LIKE LOWER(TO_ASCII(:nomeClasse)) ")
							.append(") ");
					if (!params.containsKey("codigoClasse")) {
						params.put("codigoClasse", criteriosPesquisa.getClasseJudicial());
					}
					if (!params.containsKey("siglaClasse")) {
						params.put("siglaClasse", criteriosPesquisa.getClasseJudicial());
					}
					if (!params.containsKey("nomeClasse")) {
						params.put("nomeClasse", "%" + criteriosPesquisa.getClasseJudicial() + "%");
					}
				}
			}
		}

		// prioridade
		if (criteriosPesquisa.getPrioridadeObj() != null
				&& criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso() > 0) {
			String inner = " INNER JOIN tb_proc_prioridde_processo ppp ON (ppp.id_processo_trf = ptf.id_processo_trf) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND ppp.id_prioridade_processo = :idPrioridade ");
			if (!params.containsKey("idPrioridade")) {
				params.put("idPrioridade", criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso());
			}

		} else {
			if (criteriosPesquisa.getApenasPrioridade()) {
				query.append(" AND ptf.in_prioridade = true ");
			}
		}

		// Data da distribuição do processo
		if (criteriosPesquisa.getDataDistribuicaoInicial() != null) {
			query.append(" AND ptf.dt_distribuicao > :dataDistribuicaoInicio ");
			if (!params.containsKey("dataDistribuicaoInicio")) {
				params.put("dataDistribuicaoInicio", criteriosPesquisa.getDataDistribuicaoInicial());
			}
		}
		if (criteriosPesquisa.getDataDistribuicaoFinal() != null) {
			query.append(" AND ptf.dt_distribuicao < :dataDistribuicaoFim ");
			if (!params.containsKey("dataDistribuicaoFim")) {
				params.put("dataDistribuicaoFim", criteriosPesquisa.getDataDistribuicaoFinal());
			}
		}

		// Data da autuacao do processo
		if (criteriosPesquisa.getDataAutuacaoInicial() != null) {
			query.append(" AND ptf.dt_autuacao > :dataAutuacaoInicio ");
			if (!params.containsKey("dataAutuacaoInicio")) {
				params.put("dataAutuacaoInicio", criteriosPesquisa.getDataAutuacaoInicial());
			}
		}
		if (criteriosPesquisa.getDataAutuacaoFinal() != null) {
			query.append(" AND ptf.dt_autuacao < :dataAutuacaoFim ");
			if (!params.containsKey("dataAutuacaoFim")) {
				params.put("dataAutuacaoFim", criteriosPesquisa.getDataAutuacaoFinal());
			}
		}

		// órgão jugador
		if (criteriosPesquisa.getOrgaoJulgadorObj() != null
				&& criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador() > 0) {
			query.append(" AND ptf.id_orgao_julgador = :idOrgaoJulgador ");
			if (!params.containsKey("idOrgaoJulgador")) {
				params.put("idOrgaoJulgador", criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador());
			}
		}

		// órgão jugador colegiado
		if (criteriosPesquisa.getOrgaoJulgadorColegiadoObj() != null
				&& criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado() > 0) {
			query.append(" AND ptf.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			if (!params.containsKey("idOrgaoJulgadorColegiado")) {
				params.put("idOrgaoJulgadorColegiado",
						criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado());
			}
		}

		// Dados de parte
		if ((criteriosPesquisa.getNomeParte() != null && !criteriosPesquisa.getNomeParte().isEmpty())
				|| (criteriosPesquisa.getDataNascimentoInicial() != null
						|| criteriosPesquisa.getDataNascimentoFinal() != null)
				|| ((criteriosPesquisa.getCpfParte() != null) || (criteriosPesquisa.getCnpjParte() != null)
						|| (criteriosPesquisa.getOutroDocumentoParte() != null))) {
			String innerPartes = " INNER JOIN tb_processo_parte parte ON (parte.id_processo_trf = ptf.id_processo_trf AND parte.in_parte_principal = true AND parte.in_situacao = 'A') ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), innerPartes, "WHERE (1=1)"));
			if (criteriosPesquisa.getNomeParte() != null && !criteriosPesquisa.getNomeParte().isEmpty()) {
				String inner = " INNER JOIN tb_usuario_login ul ON (parte.id_pessoa = ul.id_usuario) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
				query.append(" AND LOWER(TO_ASCII(ul.ds_nome)) LIKE LOWER(TO_ASCII(:nomeParte)) ");
				if (!params.containsKey("nomeParte")) {
					params.put("nomeParte", "%" + criteriosPesquisa.getNomeParte() + "%");
				}
			}

			// data nascimento
			if (criteriosPesquisa.getDataNascimentoInicial() != null
					|| criteriosPesquisa.getDataNascimentoFinal() != null) {
				String inner = " INNER JOIN tb_pessoa_fisica pf ON (parte.id_pessoa = pf.id_pessoa_fisica) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));

				if (criteriosPesquisa.getDataNascimentoInicial() != null) {
					query.append(" AND pf.dt_nascimento >= :dataNascimentoInicio ");
					if (!params.containsKey("dataNascimentoInicio")) {
						params.put("dataNascimentoInicio", criteriosPesquisa.getDataNascimentoInicial());
					}
				}

				if (criteriosPesquisa.getDataNascimentoFinal() != null) {
					query.append(" AND pf.dt_nascimento < :dataNascimentoFim ");
					if (!params.containsKey("dataNascimentoFim")) {
						params.put("dataNascimentoFim", criteriosPesquisa.getDataNascimentoFinal());
					}
				}
			}

			// documento de identificacao
			if (criteriosPesquisa.getCpfParte() != null) {
				String inner = " INNER JOIN tb_pess_doc_identificacao cpf ON (cpf.id_pessoa = parte.id_pessoa AND cpf.cd_tp_documento_identificacao = 'CPF' "
						+ " AND cpf.in_usado_falsamente = false AND cpf.in_ativo = true) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
				query.append(" AND cpf.nr_documento_identificacao = :numeroCpf ");
				if (!params.containsKey("numeroCpf")) {
					params.put("numeroCpf", criteriosPesquisa.getCpfParte());
				}
			}
			if (criteriosPesquisa.getCnpjParte() != null) {
				String inner = " INNER JOIN tb_pess_doc_identificacao cnpj ON (cnpj.id_pessoa = parte.id_pessoa AND cnpj.cd_tp_documento_identificacao = 'CPJ' "
						+ " AND cnpj.in_usado_falsamente = false AND cnpj.in_ativo = true) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
				query.append(" AND cnpj.nr_documento_identificacao = :numeroCnpj ");
				if (!params.containsKey("numeroCnpj")) {
					params.put("numeroCnpj", criteriosPesquisa.getCnpjParte());
				}
			}
			if (criteriosPesquisa.getOutroDocumentoParte() != null) {
				String inner = " INNER JOIN tb_pess_doc_identificacao outroDoc ON (outroDoc.id_pessoa = parte.id_pessoa AND outroDoc.cd_tp_documento_identificacao NOT IN ('CPF','CPJ') "
						+ " AND outroDoc.in_usado_falsamente = false AND outroDoc.in_ativo = true) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
				query.append(" AND outroDoc.nr_documento_identificacao = :numeroOutroDocumento ");
				if (!params.containsKey("numeroOutroDocumento")) {
					params.put("numeroOutroDocumento", criteriosPesquisa.getOutroDocumentoParte());
				}
			}

		}
		// OAB representante
		if (criteriosPesquisa.getOabRepresentanteParte() != null) {
			String innerRepresentante = " INNER JOIN tb_processo_parte representante ON (representante.id_processo_trf = ptf.id_processo_trf AND "
					+ " representante.in_parte_principal = false AND representante.in_situacao = 'A')"
					+ " INNER JOIN tb_pessoa_advogado pa ON (pa.id = representante.id_pessoa) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), innerRepresentante, "WHERE (1=1)"));
			query.append(" AND pa.nr_oab LIKE :numeroOab ");
			if (!params.containsKey("numeroOab")) {
				params.put("numeroOab", criteriosPesquisa.getOabRepresentanteParte() + "%");
			}
		}

		// dados de caixa
		if (criteriosPesquisa.getApenasSemCaixa()) {
			query.append(" AND NOT EXISTS ");
			query.append("(");
			query.append("	SELECT 1 ");
			query.append("	FROM tb_processo_caixa_adv_proc fcx_proc ");
			query.append(
					"	INNER JOIN tb_caixa_adv_proc caixa ON (caixa.id_caixa_adv_proc = fcx_proc.id_caixa_adv_proc AND caixa.id_localizacao = :idLocalizacaoFisica) ");
			query.append("	WHERE fcx_proc.id_processo_trf = ptf.id_processo_trf ");
			query.append(") ");

			if (!params.containsKey("idLocalizacaoFisica")) {
				params.put("idLocalizacaoFisica", idLocalizacaoFisica);
			}
		} else if (criteriosPesquisa.getIdCaixaAdvProc() != null && criteriosPesquisa.getIdCaixaAdvProc() > 0) {
			String inner = "INNER JOIN (SELECT caixa.id_caixa_adv_proc, fcx_proc.id_processo_trf FROM tb_processo_caixa_adv_proc fcx_proc "
					+ " INNER JOIN tb_caixa_adv_proc caixa ON (caixa.id_caixa_adv_proc = fcx_proc.id_caixa_adv_proc AND caixa.id_localizacao = :idLocalizacaoFisica)"
					+ " ) fcaixa ON (fcaixa.id_processo_trf = ptf.id_processo_trf) ";

			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));

			if (!params.containsKey("idLocalizacaoFisica")) {
				params.put("idLocalizacaoFisica", idLocalizacaoFisica);
			}

			query.append(" AND fcaixa.id_caixa_adv_proc = :idCaixa ");
			if (!params.containsKey("idCaixa")) {
				params.put("idCaixa", criteriosPesquisa.getIdCaixaAdvProc());
			}
		}

		return query.toString();
	}

	private String obterQueryUsuarioExterno(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			ConsultaProcessoVO criteriosPesquisa, Map<String, Object> params) {

		StringBuilder consultas = new StringBuilder();

		if (idProcuradoria != null && idProcuradoria != 0) {
			if (isProcuradorGestor) {
				consultas.append(obterQueryProcuradorGestor(idProcuradoria, idLocalizacao, params));
			} else {
				consultas.append(obterQueryProcuradorPadrao(idProcuradoria, idLocalizacao, idPessoa, params));
			}
		} else {
			if (TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryAdvogado(idPessoa, params));
			} else if (TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryAssistenteAdvogado(idPessoa, idLocalizacao, params));
			} else {
				consultas.append(obterQueryParte(idPessoa, params));
			}
		}
		return consultas.toString();
	}

	private String obterQueryCaixasUsuarioExterno(Integer idPessoa, Integer idLocalizacao,
			TipoUsuarioExternoEnum tipoUsuarioExterno, Integer idProcuradoria, boolean isProcuradorGestor,
			Map<String, Object> params) {

		StringBuilder consultas = new StringBuilder();

		if (idProcuradoria != null && idProcuradoria != 0) {
			if (isProcuradorGestor) {
				consultas.append(obterQueryCaixasProcuradorGestor());
			} else {
				consultas.append(obterQueryCaixasProcuradorPadrao(idLocalizacao, idPessoa, params));
			}
		} else {
			if (TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryCaixasAdvogado(idPessoa, params));
			} else if (TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryCaixasAssistenteAdvogado(idPessoa, idLocalizacao, params));
			} else {
				consultas.append(obterQueryCaixasParte(idPessoa, params));
			}
		}
		return consultas.toString();
	}

	private String obterQueryProcuradorGestor(Integer idProcuradoria, Integer idLocalizacao,
			Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append(" FROM tb_processo_parte pp ");
		query.append(
				" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append(this.obterQueryAdminJurisdicao(true));
		query.append(" WHERE (1=1) ");
		query.append(" AND ptf.cd_processo_status = 'D' AND pp.id_procuradoria = :idProcuradoria");

		if (params == null) {
			params = new HashMap<String, Object>(0);
		} else {
			if (!params.containsKey("idProcuradoria")) {
				params.put("idProcuradoria", idProcuradoria);
			}
		}
		return query.toString();
	}

	private String obterQueryProcuradorPadrao(Integer idProcuradoria, Integer idLocalizacao, Integer idPessoa,
			Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append("FROM tb_processo_parte pp ");
		query.append(
				" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append("  SELECT pp.id_processo_parte, cx.id_jurisdicao, 0 AS in_admin_jurisdicao ");
		query.append("  FROM tb_caixa_adv_proc cx ");
		query.append(
				"  INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		query.append(
				"  INNER JOIN tb_processo_caixa_adv_proc cx_proc ON (cx.id_caixa_adv_proc = cx_proc.id_caixa_adv_proc) ");
		query.append("  INNER JOIN tb_cabecalho_processo ptf ON (cx_proc.id_processo_trf = ptf.id_processo_trf) ");
		query.append(
				"  INNER JOIN tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("  WHERE (ptf.cd_processo_status = 'D' AND pp.id_procuradoria = :idProcuradoria) ");
		query.append("  AND (cx_rep.id_pessoa_fisica = :idPessoa) ");
		query.append("  AND (cx.id_localizacao = :idLocalizacaoFisica) ");
		query.append("  AND NOT EXISTS ( ");
		query.append(
				"  SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in WHERE cx_in.id_caixa_adv_proc = cx.id_caixa_adv_proc ");
		query.append("  AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim) ");
		query.append("  UNION ");
		query.append("  SELECT pp.id_processo_parte, ptf.id_jurisdicao, 1 AS in_admin_jurisdicao ");
		query.append("  FROM tb_cabecalho_processo ptf ");
		query.append(
				"  INNER JOIN tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("  WHERE ptf.cd_processo_status = 'D' ");
		query.append("  AND pp.id_procuradoria = :idProcuradoria ");
		query.append("  AND EXISTS ");
		query.append("  ( ");
		query.append("  	SELECT 1 ");
		query.append("  	FROM tb_pessoa_procuradoria pproc ");
		query.append(
				"  	INNER JOIN tb_pess_proc_jurisdicao ppj ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria AND ppj.in_ativo = true) ");
		query.append("  	WHERE pproc.id_pessoa = :idPessoa ");
		query.append("  	AND ppj.id_jurisdicao = ptf.id_jurisdicao ");
		query.append("  	AND pproc.id_procuradoria = pp.id_procuradoria ");
		query.append("  ) ");

		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append("WHERE (1=1) ");

		if (!params.containsKey("idProcuradoria")) {
			params.put("idProcuradoria", idProcuradoria);
		}
		if (!params.containsKey("idLocalizacaoFisica")) {
			params.put("idLocalizacaoFisica", idLocalizacao);
		}
		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}

		return query.toString();
	}

	private String obterQueryParte(Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append("FROM tb_processo_parte pp ");
		query.append(
				" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append(obterSelectParte(idPessoa, params));
		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append(this.obterQueryAdminJurisdicao(true));
		query.append("WHERE (1=1) ");

		return query.toString();
	}

	private String obterQueryAdvogado(Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append("FROM tb_processo_parte pp ");
		query.append(
				"INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append(obterSelectParte(idPessoa, params));
		query.append("  UNION ");
		query.append("  SELECT pp.id_processo_parte ");
		query.append("  FROM tb_proc_parte_represntante ppr ");
		query.append(
				"  INNER JOIN tb_processo_parte pp ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND pp.in_situacao = 'A' AND ppr.in_situacao = 'A') ");
		query.append("  INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf) ");
		query.append("  WHERE ptf.cd_processo_status = 'D' ");
		query.append("  AND ppr.id_representante = :idPessoa ");
		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append(this.obterQueryAdminJurisdicao(true));
		query.append(
				"LEFT JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND ppr.id_representante = :idPessoa) ");
		query.append("WHERE (1=1) ");

		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		if (!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado",
					Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}

		return query.toString();
	}

	private String obterQueryAssistenteAdvogado(Integer idPessoa, Integer idLocalizacaoFisica,
			Map<String, Object> params) {

		StringBuilder query = new StringBuilder();

		query.append("FROM tb_processo_parte pp ");
		query.append(
				" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append(obterSelectParte(idPessoa, params));
		query.append("  UNION ");
		query.append("  SELECT pp.id_processo_parte ");
		query.append("  FROM tb_processo_parte pp ");
		query.append("  INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf) ");
		query.append(
				"  INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND pp.in_situacao = 'A' AND ppr.in_situacao = 'A') ");
		query.append("  INNER JOIN tb_pessoa_localizacao pl ON (pl.id_pessoa = ppr.id_representante) ");
		query.append(
				"  INNER JOIN tb_usuario_localizacao ul ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) ");
		query.append("  WHERE ul.id_usuario = :idPessoa ");
		query.append("  AND ul.id_localizacao_fisica = :idLocalizacaoFisica AND ptf.cd_processo_status = 'D'");
		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append(this.obterQueryAdminJurisdicao(true));
		query.append(
				"LEFT JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND ppr.id_representante = :idPessoa) ");
		query.append("WHERE (1=1) ");

		if (!params.containsKey("idLocalizacaoFisica")) {
			params.put("idLocalizacaoFisica", idLocalizacaoFisica);
		}

		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		if (!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado",
					Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}

		return query.toString();
	}

	private String obterQueryCaixasProcuradorGestor() {
		// Nada a fazer. A consulta principal de recuperação das caixas já contempla as
		// cláusulas
		return " WHERE (1=1) ";
	}

	private String obterQueryCaixasProcuradorPadrao(Integer idLocalizacaoFisica, Integer idPessoa,
			Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append("INNER JOIN ");
		query.append("( ");
		query.append(
				"  SELECT cx.id_caixa_adv_proc, cx.id_jurisdicao, cx.id_localizacao, cx_rep.id_pessoa_fisica AS id_pessoa ");
		query.append("  FROM tb_caixa_adv_proc cx ");
		query.append(
				"  INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		query.append("  WHERE (cx_rep.id_pessoa_fisica = :idPessoa) ");
		query.append("  AND (cx.id_localizacao = :idLocalizacaoFisica) ");
		query.append("  UNION ");
		query.append("  SELECT cx.id_caixa_adv_proc, cx.id_jurisdicao, cx.id_localizacao, pproc.id_pessoa ");
		query.append("  FROM tb_pess_proc_jurisdicao ppj ");
		query.append(
				"  INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ");
		query.append("  INNER JOIN tb_caixa_adv_proc cx ON (cx.id_jurisdicao = ppj.id_jurisdicao) ");
		query.append("  WHERE cx.id_localizacao = :idLocalizacaoFisica ");
		query.append("  AND pproc.id_pessoa = :idPessoa ");
		query.append(
				") AS vw ON (vw.id_caixa_adv_proc = caixa.id_caixa_adv_proc AND vw.id_jurisdicao = caixa.id_jurisdicao AND vw.id_localizacao = caixa.id_localizacao AND vw.id_pessoa = :idPessoa) ");
		query.append("WHERE (1=1) ");

		if (!params.containsKey("idLocalizacaoFisica")) {
			params.put("idLocalizacaoFisica", idLocalizacaoFisica);
		}
		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}

		return query.toString();
	}

	private String obterQueryCaixasParte(Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append("LEFT JOIN ");
		query.append("( ");
		query.append(obterSelectParte(idPessoa, params));
		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append("WHERE (1=1) ");
		return query.toString();
	}

	private String obterQueryCaixasAdvogado(Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();

		query.append(" LEFT JOIN ");
		query.append("( ");
		query.append(obterSelectParte(idPessoa, params));
		query.append("  UNION ");
		query.append("  SELECT pp.id_processo_parte ");
		query.append("  FROM tb_proc_parte_represntante ppr ");
		query.append(
				"  INNER JOIN tb_processo_parte pp ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND pp.in_situacao = 'A' AND ppr.in_situacao = 'A') ");
		query.append("  AND ppr.id_representante = :idPessoa ");
		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append("WHERE (1=1) ");

		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		if (!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado",
					Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}

		return query.toString();
	}

	private String obterQueryCaixasAssistenteAdvogado(Integer idPessoa, Integer idLocalizacaoFisica,
			Map<String, Object> params) {
		StringBuilder query = new StringBuilder();
		query.append(" LEFT JOIN ");
		query.append("( ");
		query.append(obterSelectParte(idPessoa, params));
		query.append("  UNION ");
		query.append("  SELECT pp.id_processo_parte ");
		query.append("  FROM tb_processo_parte pp ");
		query.append(
				"  INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND ppr.in_situacao = 'A') ");
		query.append("  INNER JOIN tb_pessoa_localizacao pl ON (pl.id_pessoa = ppr.id_representante) ");
		query.append(
				"  INNER JOIN tb_usuario_localizacao ul ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) ");
		query.append("  WHERE ul.id_usuario = :idPessoa ");
		query.append("  AND ul.id_localizacao_fisica = :idLocalizacaoFisica ");
		query.append(") AS vw ON (vw.id_processo_parte = pp.id_processo_parte) ");
		query.append("WHERE (1=1) ");

		if (!params.containsKey("idLocalizacaoFisica")) {
			params.put("idLocalizacaoFisica", idLocalizacaoFisica);
		}

		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}

		if (!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado",
					Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}

		return query.toString();
	}

	private String obterSelectParte(Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();
		query.append("  SELECT pp.id_processo_parte ");
		query.append("  FROM tb_processo_parte pp ");
		query.append(
				"  INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append("  WHERE ptf.cd_processo_status = 'D' ");
		query.append("  AND pp.id_pessoa = :idPessoa ");

		if (!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		return query.toString();
	}

	private String obterQueryAdminJurisdicao(Boolean isAdminJurisdicao) {
		Integer valorIsAdmin = 0;
		if (isAdminJurisdicao) {
			valorIsAdmin = 1;
		}
		return "INNER JOIN (SELECT " + valorIsAdmin + " in_admin_jurisdicao) as administracao_jurisdicao ON (1=1) ";
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> pesquisarPautaMesa(FiltroProcessoSessaoDTO filtro) {
		StringBuilder queryStr = new StringBuilder(
				"SELECT DISTINCT p FROM ProcessoTrf p WHERE p.processoStatus = 'D' AND p.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");

		if (filtro.getSessao().getContinua()) {
			queryStr.append(" and p.classeJudicial.sessaoContinua = true ");
			queryStr.append(" and p.pautaVirtual = true ");
		} else {
			queryStr.append(" and p.pautaVirtual = false ");
		}

		// Processos com pedido de pauta e prontos
		queryStr.append(" AND (( (p.selecionadoPauta = true AND p.idProcessoTrf NOT IN ( ");
		queryStr.append("     SELECT pe.processo.idProcesso FROM ProcessoEvento pe ");
		queryStr.append(" 	   WHERE pe.ativo = true AND pe.evento = :evento ");
		queryStr.append("     ) ");
		queryStr.append(" AND p.classeJudicial.pauta = true) ");

		// Processos que não exigem pauta e prontos (mesa)
		queryStr.append(
				" OR ( p.idProcessoTrf in (select c.processo.idProcesso from ProcessoEvento c where c.ativo = true and c.evento = :eventoConclusao ) ");
		queryStr.append(" and p.classeJudicial.pauta = false ");
		queryStr.append(" and p.selecionadoJulgamento = true ");
		queryStr.append(" and p.orgaoJulgador.instancia in ('2','3'))  ");

		// incluir processos já pautados nessa sessão
		queryStr.append(
				" OR EXISTS (select c.processoTrf.idProcessoTrf from SessaoPautaProcessoTrf c where c.sessao = :sessao AND c.dataExclusaoProcessoTrf IS NULL and c.processoTrf.idProcessoTrf = p.idProcessoTrf  ) ) ");

		// excluir processos que estejam pautados em outras sessões não finalizadas ou
		// não retirados
		queryStr.append(" AND NOT EXISTS ");
		queryStr.append(" 		(");
		queryStr.append(
				"		  SELECT sessaoPautaProcessoTrf.processoTrf.idProcessoTrf FROM SessaoPautaProcessoTrf sessaoPautaProcessoTrf");
		queryStr.append("		  WHERE sessaoPautaProcessoTrf.sessao <> :sessao ");
		queryStr.append("		  AND sessaoPautaProcessoTrf.processoTrf.idProcessoTrf = p.idProcessoTrf ");
		queryStr.append(
				"		  AND (sessaoPautaProcessoTrf.dataExclusaoProcessoTrf IS NULL OR sessaoPautaProcessoTrf.sessao.dataRealizacaoSessao IS NULL)) ");

		if (filtro.getOrgaoJulgador() != null) {
			queryStr.append("	AND p.orgaoJulgador.idOrgaoJulgador = :orgao");
		}
		if (filtro.getTipoVoto() != null) {
			queryStr.append(
					"	AND p.idProcessoTrf in ( select voto.processoTrf.idProcessoTrf from SessaoProcessoDocumentoVoto voto where voto.tipoVoto = :tipo and voto.liberacao = true ) ");
		}
		if (filtro.getNumeroSequencia() != null) {
			queryStr.append("	AND p.numeroSequencia = :numeroSequencia");
		}
		if (filtro.getDigitoVerificador() != null) {
			queryStr.append("		AND p.numeroDigitoVerificador = :numeroDigitoVerificador");
		}
		if (filtro.getAno() != null) {
			queryStr.append("		AND p.ano = :ano ");
		}
		if (filtro.getRespectivoTribunal() != null && filtro.getRespectivoTribunal().trim().length() > 0) {
			queryStr.append("		AND p.numeroOrgaoJustica = :numeroOrgaoJustica");
		}
		if (filtro.getNumeroOrigem() != null) {
			queryStr.append("		AND p.numeroOrigem = :numeroOrigem");
		}

		if (filtro.getClasseJudicial() != null && filtro.getClasseJudicial().trim().length() > 0) {
			queryStr.append(
					"		AND LOWER(to_ascii(p.classeJudicial.classeJudicial)) LIKE LOWER(to_ascii( :classeJudicial)) )) ");
		}
		if (filtro.getNomeParte() != null && filtro.getNomeParte().trim().length() > 0) {
			queryStr.append(" AND EXISTS ( ");
			queryStr.append(" SELECT 1 ");
			queryStr.append(" FROM ProcessoParte pp ");
			queryStr.append(" WHERE pp.inSituacao = 'A' ");
			queryStr.append(" AND pp.processoTrf.idProcessoTrf = p.idProcessoTrf ");
			queryStr.append(" AND LOWER(to_ascii(pp.pessoa.nome)) LIKE LOWER(to_ascii( :nomeParte ))  ) ");
		}
		if (filtro.getAssunto() != null && filtro.getAssunto().trim().length() > 0) {
			queryStr.append(" AND EXISTS ( ");
			queryStr.append(" SELECT 1 FROM ProcessoAssunto pa ");
			queryStr.append(" INNER JOIN pa.assuntoTrf ass ");
			queryStr.append(" WHERE pa.processoTrf.idProcessoTrf = p.idProcessoTrf ");
			queryStr.append(" AND (ass.codAssuntoTrf = :assunto");
			queryStr.append(" OR LOWER(to_ascii(ass.assuntoTrf)) LIKE LOWER(to_ascii( :assunto)) )) ");
		}

		queryStr.append(" ) ");

		Query q = this.entityManager.createQuery(queryStr.toString());

		q.setParameter("evento", ParametroUtil.instance().getProcessoRetiradoPauta());
		q.setParameter("eventoConclusao", ParametroUtil.instance().getEventoConclusao());
		q.setParameter("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());
		q.setParameter("sessao", filtro.getSessao());

		if (filtro.getTipoVoto() != null) {
			q.setParameter("tipo", filtro.getTipoVoto());
		}

		if (filtro.getOrgaoJulgador() != null) {
			q.setParameter("orgao", filtro.getOrgaoJulgador().getIdOrgaoJulgador());
		}

		if (filtro.getNumeroSequencia() != null) {
			q.setParameter("numeroSequencia", filtro.getNumeroSequencia());
		}

		if (filtro.getDigitoVerificador() != null) {
			q.setParameter("numeroDigitoVerificador", filtro.getDigitoVerificador());
		}

		if (filtro.getAno() != null) {
			q.setParameter("ano", filtro.getAno());
		}

		if (StringUtils.isNotBlank(filtro.getRamoJustica()) && StringUtils.isNotBlank(filtro.getRespectivoTribunal())) {
			q.setParameter("numeroOrgaoJustica",
					Integer.parseInt(filtro.getRamoJustica() + filtro.getRespectivoTribunal()));
		}

		if (filtro.getNumeroOrigem() != null) {
			q.setParameter("numeroOrigem", filtro.getNumeroOrigem());
		}

		if (filtro.getClasseJudicial() != null && filtro.getClasseJudicial().trim().length() > 0) {
			q.setParameter("classeJudicial", filtro.getClasseJudicial());
		}

		if (filtro.getNomeParte() != null && filtro.getNomeParte().trim().length() > 0) {
			q.setParameter("nomeParte", filtro.getNomeParte());
		}

		if (filtro.getAssunto() != null && filtro.getAssunto().trim().length() > 0) {
			q.setParameter("assunto", filtro.getAssunto());
		}

		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public String getNomeRelator(ProcessoTrf processo) {
		if (processo == null) {
			return null;
		}

		boolean temOrgaoJulgadorColegiado = (processo.getOrgaoJulgadorColegiado() != null);
		boolean titular = false;
		String nomeRelator = null;
		StringBuilder sql = new StringBuilder();

		sql.append("select distinct ul.id_usuario, ul.ds_nome, ulms.in_magistrado_titular ");
		sql.append("from tb_usu_local_mgtdo_servdor ulms ");
		sql.append(
				"inner join tb_orgao_julgador_cargo ojc on ojc.id_orgao_julgador_cargo = ulms.id_orgao_julgador_cargo ");
		sql.append(
				"inner join tb_usuario_localizacao uloc on uloc.id_usuario_localizacao = ulms.id_usu_local_mgstrado_servidor ");
		sql.append("inner join tb_usuario_login ul on ul.id_usuario = uloc.id_usuario ");
		sql.append("where ojc.id_orgao_julgador = :idOrgaoJulgador ");
		sql.append("and ojc.in_recebe_distribuicao = :recebeDistribuicao ");

		if (temOrgaoJulgadorColegiado) {
			sql.append("and ulms.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
		}

		Query query = getEntityManager().createNativeQuery(sql.toString());

		query.setParameter("idOrgaoJulgador", processo.getOrgaoJulgador().getIdOrgaoJulgador());
		query.setParameter("recebeDistribuicao", Boolean.TRUE);
		if (temOrgaoJulgadorColegiado) {
			query.setParameter("idOrgaoJulgadorColegiado",
					processo.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
		}

		List<Object[]> resultList = query.getResultList();

		for (Object[] r : resultList) {
			titular = (Boolean) r[2];
			if (titular) {
				nomeRelator = (String) r[1];
				break;
			}
		}

		if (resultList.size() > 0 && !titular) {
			nomeRelator = (String) resultList.get(0)[1];
		}

		return nomeRelator;
	}

	public Long recuperarQtdProcessosAssinatura(InformacaoUsuarioSessao informacaoUsuario, Integer idTipoDocumento,
			CriterioPesquisa criteriosPesquisa) {
		return this.recuperarQtdProcessosAssinatura(informacaoUsuario, idTipoDocumento, criteriosPesquisa, null);
	}

	public Long recuperarQtdProcessosAssinatura(InformacaoUsuarioSessao informacaoUsuario, Integer idTipoDocumento,
			CriterioPesquisa criteriosPesquisa, Integer idTag) {
		Query q = montaSQLProcessosAssinatura(true, informacaoUsuario, idTipoDocumento, idTag, criteriosPesquisa);
		return ((BigInteger) q.getResultList().get(0)).longValue();
	}

	@SuppressWarnings("unchecked")
	public List<CabecalhoProcesso> recuperarListaProcessosAssinatura(InformacaoUsuarioSessao informacaoUsuario,
			Integer idTipoDocumento, Integer idTag, CriterioPesquisa criteriosPesquisa,
			Boolean conferidosComDocumentos) {

		Query q = montaSQLProcessosAssinatura(false, informacaoUsuario, idTipoDocumento, idTag, criteriosPesquisa);
		List<CabecalhoProcesso> retorno = new ArrayList<>();
		List<Object[]> resultList = q.getResultList();
		Object[] borderTypes = null;
		if (resultList.isEmpty()) {
			return retorno;
		}

		Map<Integer, List<ArquivoAssinatura>> anexos = processaAnexos(resultList);

		Integer tipoDocAcordao = ParametroUtil.instance().getTipoProcessoDocumentoAcordao()
				.getIdTipoProcessoDocumento();

		Set<Integer> idDocsList = new HashSet<>();
		for (int i = 0; i <= resultList.size(); i++) {
			if (i == 0) {
				borderTypes = resultList.get(i);
				continue;
			}
			Integer idDoc = (Integer) borderTypes[6];
			if (idDocsList.contains(idDoc)) {
				continue;
			}
			CabecalhoProcesso vo = new CabecalhoProcesso();
			Integer idProcesso = (Integer) borderTypes[0];
			String numeroProcesso = (String) borderTypes[1];
			vo.setIdProcesso(idProcesso.longValue());
			vo.setNumeroProcesso(numeroProcesso);
			vo.setDataChegada((Date) borderTypes[2]);
			vo.setIdTaskInstance(((BigInteger) borderTypes[3]).longValue());
			vo.setClasseJudicial((String) borderTypes[4]);

			vo.setConferido("T".equals(borderTypes[5]));
			vo.setTipoDocumentoAssinatura((String) borderTypes[15]);
			vo.setPodeAssinar(borderTypes[16] != null);
			vo.setDataAlteracaoDocumentoAssinatura((Date) borderTypes[17]);
			vo.setNomeUsuarioAlteracaoDocumentoAssinatura(borderTypes[18] != null ? (String) borderTypes[18] : null);
			vo.setSigiloso((Boolean) borderTypes[19]);
			vo.setAssuntoPrincipal((String) borderTypes[20]);

			ArquivoAssinatura docVO = new ArquivoAssinatura();
			docVO.setId(idDoc);
			docVO.setCodIniDate((Date) borderTypes[7]);
			docVO.setHash(borderTypes[8] == null ? StringUtils.EMPTY : (String) borderTypes[8]);
			docVO.setIsBin((Boolean) borderTypes[9]);
			docVO.setIdTarefa(((BigInteger) borderTypes[3]).longValue());

			if (anexos.containsKey(idDoc)) {
				for (ArquivoAssinatura anexo : anexos.get(idDoc)) {
					anexo.setIdTarefa(-1L); // impede que o sistema finalize a tarefa na assinatura do anexo
					if (!vo.getArquivos().contains(anexo)) {
						vo.getArquivos().add(anexo);
					}
				}
			}
			if (!vo.getArquivos().contains(docVO)) {
				vo.getArquivos().add(docVO);
			}

			if (borderTypes[24].equals(tipoDocAcordao)) {
				List<ArquivoAssinatura> docSessaoParaAssinatura = recuperarDocumentosSessao(idProcesso);
				for (ArquivoAssinatura anexoAcordao : docSessaoParaAssinatura) {
					anexoAcordao.setIdTarefa(-1L);
					if (!vo.getArquivos().contains(anexoAcordao))
						vo.getArquivos().add(anexoAcordao);
				}
			}

			vo.setOrgaoJulgador((String) borderTypes[10]);
			vo.setOrgaoJulgadorColegiado((String) borderTypes[11]);
			vo.setPoloAtivo(StringUtils.isEmpty((String) borderTypes[12]) ? "não definido" : (String) borderTypes[12]);
			vo.setPoloPassivo(
					StringUtils.isEmpty((String) borderTypes[13]) ? "não definido" : (String) borderTypes[13]);
			vo.setNomeTarefa((String) borderTypes[14]);
			vo.setCargoJudicial((String) borderTypes[23]);
			vo.setUltimoMovimento(borderTypes[25] != null ? (Date) borderTypes[25] : null);
			vo.setDescricaoUltimoMovimento(borderTypes[26] != null ? borderTypes[26].toString() : "ND");

			int indiceProximoItemNaLista = i + 1;
			if (indiceProximoItemNaLista < resultList.size() && resultList.get(indiceProximoItemNaLista)[3] != null) {
				vo.setIdTaskInstanceProximo(((BigInteger) resultList.get(indiceProximoItemNaLista)[3]).longValue());
			}

			if (i != resultList.size()) {
				borderTypes = resultList.get(i);
			}

			retorno.add(vo);

			idDocsList.add(idDoc);
		}
		if (conferidosComDocumentos != null && conferidosComDocumentos) {
			return filtrarConferidos(retorno);
		}
		return retorno;
	}

	private void preencherCabecalhoProcessoVo(Map<Long, List<ArquivoAssinatura>> anexos, Integer idDoc,
			CabecalhoProcesso vo, Object[] borderTypes, Integer tipoDocAcordao, int i, List<Object[]> resultList) {

		Integer idProcesso = (Integer) borderTypes[0];
		String numeroProcesso = (String) borderTypes[1];
		vo.setIdProcesso(idProcesso.longValue());
		vo.setNumeroProcesso(numeroProcesso);
		vo.setDataChegada((Date) borderTypes[2]);
		vo.setIdTaskInstance(((BigInteger) borderTypes[3]).longValue());
		vo.setClasseJudicial((String) borderTypes[4]);

		vo.setConferido("T".equals(borderTypes[5]));
		vo.setTipoDocumentoAssinatura((String) borderTypes[15]);
		vo.setPodeAssinar(borderTypes[16] != null);
		vo.setDataAlteracaoDocumentoAssinatura((Date) borderTypes[17]);
		vo.setNomeUsuarioAlteracaoDocumentoAssinatura(borderTypes[18] != null ? (String) borderTypes[18] : null);
		vo.setSigiloso((Boolean) borderTypes[19]);
		vo.setAssuntoPrincipal((String) borderTypes[20]);

		ArquivoAssinatura docVO = new ArquivoAssinatura();
		docVO.setId(idDoc);
		docVO.setCodIniDate((Date) borderTypes[7]);
		docVO.setHash(borderTypes[8] == null ? StringUtils.EMPTY : (String) borderTypes[8]);
		docVO.setIsBin((Boolean) borderTypes[9]);
		docVO.setIdTarefa(((BigInteger) borderTypes[3]).longValue());

		adicionarAnexos(anexos, idDoc, vo, docVO, borderTypes, idProcesso, tipoDocAcordao);

		vo.setOrgaoJulgador((String) borderTypes[10]);
		vo.setOrgaoJulgadorColegiado((String) borderTypes[11]);
		vo.setPoloAtivo(StringUtils.isEmpty((String) borderTypes[12]) ? NAO_DEFINIDO : (String) borderTypes[12]);
		vo.setPoloPassivo(StringUtils.isEmpty((String) borderTypes[13]) ? NAO_DEFINIDO : (String) borderTypes[13]);
		vo.setNomeTarefa((String) borderTypes[14]);
		vo.setCargoJudicial((String) borderTypes[23]);
		vo.setUltimoMovimento(borderTypes[25] != null ? (Date) borderTypes[25] : null);
		vo.setDescricaoUltimoMovimento(borderTypes[26] != null ? borderTypes[26].toString() : "ND");

		int indiceProximoItemNaLista = i + 1;
		atribuirIdTaskInstanceProximo(resultList, vo, indiceProximoItemNaLista);
		vo.setPoloPassivo((String) borderTypes[13]);
		isEmptyPoloPassivo(vo);
		vo.setNomeTarefa((String) borderTypes[14]);

	}

	private void adicionarAnexos(Map<Long, List<ArquivoAssinatura>> anexos, Integer idDoc, CabecalhoProcesso vo,
			ArquivoAssinatura docVO, Object[] borderTypes, Integer idProcesso, Integer tipoDocAcordao) {
		if (anexos.containsKey(idDoc)) {
			for (ArquivoAssinatura anexo : anexos.get(idDoc)) {
				anexo.setIdTarefa(-1L); // impede que o sistema finalize a tarefa na assinatura do anexo
				if (!vo.getArquivos().contains(anexo)) {
					vo.getArquivos().add(anexo);
				}
			}
		}
		if (!vo.getArquivos().contains(docVO)) {
			vo.getArquivos().add(docVO);
		}

		if (borderTypes[24].equals(tipoDocAcordao)) {
			List<ArquivoAssinatura> docSessaoParaAssinatura = recuperarDocumentosSessao(idProcesso);
			for (ArquivoAssinatura anexoAcordao : docSessaoParaAssinatura) {
				anexoAcordao.setIdTarefa(-1L);
				if (!vo.getArquivos().contains(anexoAcordao))
					vo.getArquivos().add(anexoAcordao);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private List<ArquivoAssinatura> recuperarDocumentosSessao(Integer idProcesso) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"select spp.id_sessao, spp.id_orgao_julgador_vencedor, spp.id_orgao_julgador_relator, p.id_orgao_julgador from client.tb_sessao_pauta_proc_trf spp inner join tb_processo_trf p on spp.id_processo_trf = p.id_processo_trf where spp.id_processo_trf = :idProcesso and spp.tp_situacao_julgamento='JG' ");
		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);

		Object[] borderTypes = null;
		List<Object[]> parametroResultSet = q.getResultList();
		Integer idSessao = null;
		Integer idOrgaoJulgadorVencedor = null;
		Integer idOrgaoJulgadorRelator = null;
		Integer idOrgaoJulgadorProcesso = null;
		Integer idOrgaoJulgadorEmenta = null;

		if (parametroResultSet != null && parametroResultSet.size() > 0) {
			borderTypes = parametroResultSet.get(0);
			idSessao = (Integer) borderTypes[0];
			if (borderTypes[1] != null) {
				idOrgaoJulgadorVencedor = (Integer) borderTypes[1];
			}
			if (borderTypes[2] != null) {
				idOrgaoJulgadorRelator = (Integer) borderTypes[2];
			}
			idOrgaoJulgadorProcesso = (Integer) borderTypes[3];
		}
		List<ArquivoAssinatura> docParaAssinatura = new ArrayList<ArquivoAssinatura>();
		if (idSessao != null) {
			sb = new StringBuilder()
					.append("select pd.id_processo_documento, pd.id_documento_principal,pd.dt_inclusao, ")
					.append(" case when in_binario or (ds_modelo_documento is null) then ds_md5_documento else md5(convert_to(ds_modelo_documento, 'UTF8'))  end as ds_md5_documento, ")
					.append(" in_binario,id_jbpm_task from core.tb_processo_documento pd inner join ")
					.append("core.tb_processo_documento_bin pdb on pd.id_processo_documento_bin = pdb.id_processo_documento_bin inner join client.tb_sessao_proc_documento spd on ")
					.append("pd.id_processo_documento = spd.id_processo_documento where pd.in_ativo = 't' and pd.dt_juntada is null and spd.id_sessao = :idSessao AND pd.id_processo = :idProcesso and ")
					.append("((pd.id_tipo_processo_documento = :idTipoProcDocEmenta and spd.id_orgao_julgador = :idOrgaoJulgador) or (pd.id_tipo_processo_documento != :idTipoProcDocEmenta)) and ")
					.append("pd.id_tipo_processo_documento in (select cast(vl_variavel as Integer) from core.tb_parametro where nm_variavel in('pje:tipoProcessoDocumento:notasOrais', 'idTipoProcessoDocumentoRelatorio', 'idTipoProcessoDocumentoEmenta', ")
					.append("'idTipoProcessoDocumentoVoto')) ");

			q = getEntityManager().createNativeQuery(sb.toString());
			q.setParameter("idSessao", idSessao);
			q.setParameter("idProcesso", idProcesso);
			q.setParameter("idTipoProcDocEmenta",
					ParametroUtil.instance().getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento());

			if (idOrgaoJulgadorVencedor == null) {
				if (idOrgaoJulgadorRelator == null) {
					idOrgaoJulgadorEmenta = idOrgaoJulgadorProcesso;
				} else {
					idOrgaoJulgadorEmenta = idOrgaoJulgadorRelator;
				}
			} else {
				idOrgaoJulgadorEmenta = idOrgaoJulgadorVencedor;
			}

			q.setParameter("idOrgaoJulgador", idOrgaoJulgadorEmenta);

			List<Object[]> res = q.getResultList();
			for (int j = 0; j < res.size(); j++) {
				Object[] obj = res.get(j);
				ArquivoAssinatura vo = new ArquivoAssinatura();
				vo.setId((Integer) obj[0]);
				vo.setCodIniDate((Date) obj[2]);
				vo.setHash((String) obj[3]);
				vo.setIsBin((Boolean) obj[4]);
				if (obj[5] != null)
					vo.setIdTarefa(((BigInteger) obj[5]).longValue());
				docParaAssinatura.add(vo);
			}
		}
		return docParaAssinatura;
	}

	public Long recuperarQtdProcessosTarefa(InformacaoUsuarioSessao informacaoUsuario, String tarefa, Integer idTag,
			CriterioPesquisa query) {
		Query q = montaSQLProcessosTarefa(true, informacaoUsuario, tarefa, idTag, query);
		return ((BigInteger) q.getResultList().get(0)).longValue();
	}

	private Query montaSQLProcessosTarefa(Boolean isCount, InformacaoUsuarioSessao informacaoUsuario, String tarefa,
			Integer idTag, CriterioPesquisa query) {
		StringBuilder sb = new StringBuilder(BIG_QUERY_SIZE);
		Map<String, Object> parametros = new HashMap<String, Object>();
		if (query == null) {
			query = new CriterioPesquisa();
		}

		if (isCount) {
			sb.append("SELECT COUNT(0) ");
		} else {
			appendCamposProcessosTarefa(sb);
		}

		sb.append("FROM tb_processo_tarefa proctar ");
		sb.append("INNER JOIN tb_cabecalho_processo cabpro ON cabpro.id_processo_trf = proctar.id_processo_trf ");
		sb.append(
				"INNER JOIN tb_orgao_julgador_cargo ojcargo ON ojcargo.id_orgao_julgador_cargo = cabpro.id_orgao_julgador_cargo ");
		sb.append("INNER JOIN tb_cargo car ON car.id_cargo = ojcargo.id_cargo ");

		if (StringUtils.isNotBlank(query.getCompetencia())) {
			sb.append("INNER JOIN tb_processo_trf proctrf ON proctrf.id_processo_trf = cabpro.id_processo_trf ");
			sb.append("LEFT JOIN client.tb_competencia comp ON proctrf.id_competencia = comp.id_competencia ");
		} else if (query.getDataAutuacao() != null) {
			sb.append("INNER JOIN tb_processo_trf proctrf ON proctrf.id_processo_trf = cabpro.id_processo_trf ");
		}

		sb.append("LEFT JOIN tb_usuario_login usuResp on usuResp.ds_login = proctar.nm_actorid ");
		if (this.hasFiltroJE(query)) {
			sb.append("LEFT JOIN tb_complemento_processo_je je ON proctar.id_processo_trf= je.id_processo_trf ");
		}

		List<Integer> idsLocalizacoesFisicas = informacaoUsuario.getIdsLocalizacoesFisicasFilhas();
		Integer idLocalizacaoFisica = informacaoUsuario.getIdLocalizacaoFisica();
		Integer idOrgaoJulgadorColegiado = informacaoUsuario.getIdOrgaoJulgadorColegiado();
		boolean isServidorExclusivoOJC = informacaoUsuario.isServidorExclusivoOJC();
		List<Integer> idsOrgaoJulgadorCargo = informacaoUsuario.getIdsOrgaoJulgadorCargoVisibilidade();
		Boolean visualizaSigiloso = informacaoUsuario.getVisualizaSigiloso();

		if (idTag != null && idTag != 0) {
			sb.append("INNER JOIN tb_processo_tag proctag ON proctag.id_processo = proctar.id_processo_trf ");
			sb.append("INNER JOIN tb_tag tag ON tag.id = proctag.id_tag ");

			sb.append(" AND ( ");
			sb.append(" tag.in_sistema = true ");
			sb.append(" OR tag.in_publica = true ");

			if (idLocalizacaoFisica != null) {
				sb.append(" OR tag.id_localizacao = :idLocalizacaoFisica ");
				if (!parametros.containsKey("idLocalizacaoFisica")) {
					parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
				}
			}
			sb.append(" ) ");
		}

		sb.append("WHERE EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("              WHERE tl.id_processo = proctar.id_processo_trf ");
		sb.append("              AND tl.id_task_jbpm = proctar.id_task ");
		sb.append("              AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("              AND tl.id_papel = :idPapel) ");

		parametros.put("idLocalizacaoModelo", informacaoUsuario.getIdLocalizacaoModelo());
		parametros.put("idPapel", informacaoUsuario.getIdPapel());

		if (tarefa != null) {
			sb.append("AND proctar.nm_tarefa = :tarefa ");
			parametros.put("tarefa", tarefa);
		} else if (query.getIdTaskInstance() != null) {
			sb.append(AND_ID_TAREFA_TASK_INSTANCE);
			parametros.put(ID_TASKINSTANCE, query.getIdTaskInstance());
		}

		if (query.getIdProcessoTrf() != null) {
			sb.append("AND proctar.id_processo_trf = :idProcessoTrf ");
			parametros.put("idProcessoTrf", query.getIdProcessoTrf());
		}

		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			sb.append("AND proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
			parametros.put("idsLocalizacoesFisicas", idsLocalizacoesFisicas);
		}

		if (idOrgaoJulgadorColegiado != null && idOrgaoJulgadorColegiado > 0) {
			sb.append("AND proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			parametros.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		}

		if (idsOrgaoJulgadorCargo != null && idsOrgaoJulgadorCargo.size() > 0) {
			sb.append("AND proctar.id_orgao_julgador_cargo in (:idOrgaoJulgadorCargo) ");
			parametros.put("idOrgaoJulgadorCargo", idsOrgaoJulgadorCargo);
		}

		if (query.getIdCargoJudicial() != null) {
			String nomeParametroIdCargo = "idCargo";
			sb.append("AND car.id_cargo = :" + nomeParametroIdCargo + " ");
			parametros.put(nomeParametroIdCargo, query.getIdCargoJudicial());
		}

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			sb.append("AND (proctar.in_segredo_justica = false OR EXISTS ");
			sb.append(SELECT_PROC_VISIBILIDADE_SEGREDO);
			sb.append("WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)) ");
			parametros.put(ID_USUARIO, informacaoUsuario.getIdUsuario());
		} else {
			// Monta o painel do usuário de acordo com as permisses de sigilo do usuário, no
			// permitindo que este visualize processos que estão com nível de acesso maior
			// do que o do usuário logado
			sb.append("AND (proctar.in_segredo_justica = false OR ");
			sb.append(
					"			(proctar.in_segredo_justica = true AND proctar.cd_nivel_acesso <= :nivelAcessoUsuario) OR");
			sb.append(
					"			EXISTS (SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)");
			sb.append("		) ");
			parametros.put(NIVEL_ACESSO_USUARIO, informacaoUsuario.getNivelAcessoSigilo());
			parametros.put(ID_USUARIO, informacaoUsuario.getIdUsuario());
		}

		if (informacaoUsuario.getCargoAuxiliar() != null && informacaoUsuario.getCargoAuxiliar()) {
			sb.append("AND ( ");
			sb.append(
					"		( NOT EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append("		OR ");
			sb.append(
					"     ( EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm where ptlm.id_processo_trf = proctar.id_processo_trf AND ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append(") ");

			parametros.put("usuLoc", informacaoUsuario.getIdUsuarioLocalizacaoMagistradoServidor());
		}

		if (idTag != null) {
			if (idTag == 0) {
				sb.append(
						" AND NOT EXISTS (SELECT 1 FROM tb_processo_tag proctag INNER JOIN tb_tag tag ON tag.id = proctag.id_tag ");

				sb.append(" AND ( ");
				sb.append(" tag.in_sistema = true ");
				sb.append(" OR tag.in_publica = true ");

				if (idLocalizacaoFisica != null) {
					sb.append(" and tag.id_localizacao = :idLocalizacaoFisica ");
					if (!parametros.containsKey("idLocalizacaoFisica")) {
						parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
					}
				}
				sb.append(" ) ");
				sb.append(" WHERE proctag.id_processo = proctar.id_processo_trf ) ");
			} else {
				sb.append("AND tag.id = :idTag ");
				parametros.put("idTag", idTag);
			}
		}

		if (query != null) {
			if (StringUtils.isNotEmpty(query.getNumeroProcesso())) {
				sb.append(
						"AND regexp_replace(cabpro.nr_processo, '\\D', '', 'g') LIKE '%' || regexp_replace(:numeroProcesso, '\\D', '', 'g') || '%' ");
				parametros.put("numeroProcesso", query.getNumeroProcesso());
			}
			if (StringUtils.isNotEmpty(query.getClasse())) {
				sb.append("AND (to_ascii(cabpro.ds_classe_judicial) ILIKE '%'|| to_ascii(:classeJudicial) || '%' ");
				sb.append("OR cabpro.ds_classe_judicial_sigla LIKE '%'|| :classeJudicial  || '%') ");
				parametros.put("classeJudicial", query.getClasse());
			}
			if (StringUtils.isNotEmpty(query.getPoloAtivo())) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
				sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
				sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
				sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
				sb.append("AND pp.in_participacao = 'A' ");
				sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloAtivo) || '%' ");
				sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloAtivo) || '%')) ");
				parametros.put("poloAtivo", query.getPoloAtivo());
			}
			if (StringUtils.isNotEmpty(query.getOrgao())) {
				sb.append("AND to_ascii(cabpro.ds_orgao_julgador) ILIKE '%' || to_ascii(:orgao) || '%' ");
				parametros.put("orgao", query.getOrgao());
			}
			if (StringUtils.isNotEmpty(query.getPoloPassivo())) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
				sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
				sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
				sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
				sb.append("AND pp.in_participacao = 'P' ");
				sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloPassivo) || '%' ");
				sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloPassivo) || '%')) ");
				parametros.put("poloPassivo", query.getPoloPassivo());
			}
			if (query.getTags() != null && query.getTags().length > 0) {
				List<String> tags = Arrays.asList(query.getTags()).stream()
						.map(p -> StringUtil.normalize(p).toLowerCase()).collect(Collectors.toList());

				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag tags ");
				sb.append("INNER JOIN tb_tag tag ON tags.id_tag = tag.id ");
				sb.append(
						"WHERE LOWER(TO_ASCII(tag.ds_tag)) IN (:tags) AND tags.id_processo = proctar.id_processo_trf) ");
				parametros.put("tags", tags);
			}

			if (query.getSomenteFavoritas() != null && query.getSomenteFavoritas()) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag pt " + "INNER JOIN tb_tag t on pt.id_tag = t.id "
						+ "INNER JOIN tb_tag_favorita tf on tf.id_tag = pt.id_tag "
						+ "WHERE tf.id_usuario = :idUsuarioFavorito AND pt.id_processo = proctar.id_processo_trf ");

				sb.append(" AND ( ");
				sb.append(" t.in_sistema = true ");
				sb.append(" OR t.in_publica = true ");

				if (idLocalizacaoFisica != null) {
					sb.append(" OR t.id_localizacao = :idLocalizacaoFisica ");
					if (!parametros.containsKey("idLocalizacaoFisica")) {
						parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
					}
				}
				sb.append(" ) ");

				sb.append(")");
				parametros.put("idUsuarioFavorito", informacaoUsuario.getIdUsuario());

			} else if (query.getSemEtiqueta() != null && query.getSemEtiqueta()) {
				sb.append("AND NOT EXISTS (SELECT 1 FROM tb_processo_tag pt "
						+ "                  INNER JOIN tb_tag t on pt.id_tag = t.id "
						+ "                  WHERE pt.id_processo = proctar.id_processo_trf ");

				sb.append(" AND ( ");
				sb.append(" t.in_sistema = true ");
				sb.append(" OR t.in_publica = true ");

				if (idLocalizacaoFisica != null) {
					sb.append(" OR t.id_localizacao = :idLocalizacaoFisica ");
					if (!parametros.containsKey("idLocalizacaoFisica")) {
						parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
					}
				}
				sb.append(" ) ");

				sb.append(")");
			}

			appendFiltroPrioridadeProcesso(query, sb, parametros);
			appendFiltroAssunto(query, sb, parametros);
			appendFiltroObjeto(query, sb, parametros);
			appendFiltroDataAutuacao(query, sb, parametros);
			appendFiltroRelacionadoParte(query, sb, parametros);
			appendFiltroOrgaoJulgador(query, sb, parametros);
			appendFiltroCompetencia(query, sb, parametros);
			appendFiltroSomenteSigiloso(query, sb, parametros);
			appendFiltroSomenteLembrete(query, sb, parametros);
			appendFiltroJE(query, sb, parametros);
		}

		if (!isCount) {
			sb.append("ORDER BY ");
			if (StringUtils.isNotEmpty(query.getOrdem()) && query.getOrdem().equals("ASC")) {
				sb.append("cabpro.vl_peso_prioridade ASC, proctar.id_processo_tarefa DESC");
			} else {
				sb.append("cabpro.vl_peso_prioridade DESC, proctar.id_processo_tarefa ASC");
			}
		}

		Query q = getEntityManager().createNativeQuery(sb.toString());
		setQueryParameters(q, parametros);

		appendCriteriosPage(isCount, query, q);
		return q;
	}

	public List<CabecalhoProcesso> recuperarMetadadosProcesso(InformacaoUsuarioSessao informacaoUsuario, String tarefa,
			Integer idTag, CriterioPesquisa query) {
		if ((tarefa == null || tarefa.isEmpty()) && (query == null || query.getIdTaskInstance() == null)) {
			return new ArrayList<CabecalhoProcesso>(0);
		}

		Query q = montaSQLProcessosTarefa(false, informacaoUsuario, tarefa, idTag, query);
		return recuperaMetadados(q);
	}

	private Query montaSQLProcessosAssinatura(Boolean isCount, InformacaoUsuarioSessao informacaoUsuario,
			Integer idTipoDocumento, Integer idTag, CriterioPesquisa criteriosPesquisa) {
		StringBuilder sb = new StringBuilder(BIG_QUERY_SIZE);

		Integer idOrgaoJulgadorColegiado = informacaoUsuario.getIdOrgaoJulgadorColegiado();
		boolean isServidorExclusivoOJC = informacaoUsuario.isServidorExclusivoOJC();
		List<Integer> idsOrgaoJulgadorCargo = informacaoUsuario.getIdsOrgaoJulgadorCargoVisibilidade();
		List<Integer> idsLocalizacoesFisicas = informacaoUsuario.getIdsLocalizacoesFisicasFilhas();
		Integer idLocalizacaoFisica = informacaoUsuario.getIdLocalizacaoFisica();
		Integer idLocalizacaoModelo = informacaoUsuario.getIdLocalizacaoModelo();
		Integer idPapel = informacaoUsuario.getIdPapel();
		Boolean visualizaSigiloso = informacaoUsuario.getVisualizaSigiloso();
		Integer nivelAcessoSigilo = informacaoUsuario.getNivelAcessoSigilo();
		Integer idUsuario = informacaoUsuario.getIdUsuario();
		Map<String, Object> parametros = new HashMap<>();

		appendSelect(isCount, sb);

		sb.append("FROM tb_processo_tarefa proctar ");
		sb.append("INNER JOIN tb_cabecalho_processo cabpro ON proctar.id_processo_trf = cabpro.id_processo_trf ");
		sb.append(
				"INNER JOIN tb_orgao_julgador_cargo ojcargo ON ojcargo.id_orgao_julgador_cargo = cabpro.id_orgao_julgador_cargo ");
		sb.append("INNER JOIN tb_cargo car ON car.id_cargo = ojcargo.id_cargo ");
		sb.append("INNER JOIN jbpm_task task ON task.id_ = proctar.id_task ");
		sb.append(
				"INNER JOIN jbpm_variableinstance vii on vii.processinstance_ = proctar.id_process_instance and vii.name_ IN ('"
						+ Variaveis.MINUTA_EM_ELABORACAO + "', '" + Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO
						+ "') ");
		if (criteriosPesquisa.getConferidos() != null && criteriosPesquisa.getConferidos()) {
			sb.append(
					"INNER JOIN jbpm_variableinstance viii on viii.taskinstance_ = proctar.id_processo_tarefa AND viii.name_ = '"
							+ Variaveis.CONFERIR_PROCESSO_ASSINATURA + "' ");
		}
		sb.append(
				"INNER JOIN tb_processo_documento pd on (pd.id_processo_documento = cast(vii.longvalue_ as integer) AND pd.dt_juntada IS NULL) ");
		sb.append(
				"INNER JOIN tb_processo_documento_bin pdb on pdb.id_processo_documento_bin = pd.id_processo_documento_bin ");
		sb.append(
				"INNER JOIN core.tb_tipo_processo_documento tpd on tpd.id_tipo_processo_documento = pd.id_tipo_processo_documento ");

		appendCriteriosUsuarioPapel(isCount, criteriosPesquisa, sb);

		appendTag(idTag, sb, idLocalizacaoFisica, parametros);

		sb.append("WHERE task.priority_ = 4 ");
		sb.append("AND EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("		       WHERE tl.id_processo = proctar.id_processo_trf ");
		sb.append("            AND tl.id_task_jbpm = proctar.id_task ");
		sb.append("            AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("            AND tl.id_papel = :idPapel) ");

		parametros.put("idLocalizacaoModelo", idLocalizacaoModelo);
		parametros.put("idPapel", idPapel);

		appendTipoDocumentoOrTasInstance(idTipoDocumento, criteriosPesquisa, sb, parametros);

		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			sb.append("AND proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
			parametros.put("idsLocalizacoesFisicas", idsLocalizacoesFisicas);
		}

		if (idOrgaoJulgadorColegiado != null) {
			sb.append("AND proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			parametros.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		}

		if (idsOrgaoJulgadorCargo != null && !idsOrgaoJulgadorCargo.isEmpty()) {
			sb.append("AND proctar.id_orgao_julgador_cargo in (:idOrgaoJulgadorCargo) ");
			parametros.put("idOrgaoJulgadorCargo", idsOrgaoJulgadorCargo);
		}

		appendCriterioVisualizacao(sb, visualizaSigiloso, nivelAcessoSigilo, idUsuario, parametros);

		if (informacaoUsuario.getCargoAuxiliar() != null && informacaoUsuario.getCargoAuxiliar()) {
			sb.append("AND ( ");
			sb.append(
					"		( NOT EXISTS (select null FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append("		OR ");
			sb.append(
					"     ( EXISTS (select null FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_processo_trf = proctar.id_processo_trf AND ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append(") ");

			parametros.put("usuLoc", informacaoUsuario.getIdUsuarioLocalizacaoMagistradoServidor());
		}

		if (idTag != null) {
			if (idTag == 0) {
				appendFiltroSemEtiqueta(sb, idLocalizacaoFisica, parametros);
			} else {
				sb.append("AND tag.id = :idTag ");
				parametros.put("idTag", idTag);
			}
		}

		if (criteriosPesquisa != null) {
			appendCriteriosPesquisa(criteriosPesquisa, sb, idLocalizacaoFisica, parametros);
		}

		appendFiltroSomenteComDocumentosNaoLidos(criteriosPesquisa, sb);
		appendFiltroAssunto(criteriosPesquisa, sb, parametros);
		appendFiltroObjeto(criteriosPesquisa, sb, parametros);
		appendFiltroDataAutuacao(criteriosPesquisa, sb, parametros);
		appendFiltroRelacionadoParte(criteriosPesquisa, sb, parametros);
		appendFiltroOrgaoJulgador(criteriosPesquisa, sb, parametros);
		appendFiltroCompetencia(criteriosPesquisa, sb, parametros);
		appendFiltroSomenteSigiloso(criteriosPesquisa, sb, parametros);
		appendFiltroSomenteLembrete(criteriosPesquisa, sb, parametros);
		appendExigibilidadeAssinatura(criteriosPesquisa, sb, parametros);

		appendOrderBy(isCount, criteriosPesquisa, sb);

		Query q = getEntityManager().createNativeQuery(sb.toString());

		setQueryParameters(q, parametros);

		appendCriteriosPage(isCount, criteriosPesquisa, q);
		return q;

	}

	private void appendCriterioVisualizacao(StringBuilder sb, Boolean visualizaSigiloso, Integer nivelAcessoSigilo,
			Integer idUsuario, Map<String, Object> parametros) {
		if (visualizaSigiloso != null && !visualizaSigiloso || (nivelAcessoSigilo == null || nivelAcessoSigilo == 0)) {
			sb.append("AND (cabpro.in_segredo_justica = false OR EXISTS ");
			sb.append(SELECT_PROC_VISIBILIDADE_SEGREDO);
			sb.append("WHERE vis.id_pessoa = :idUsuario and vis.id_processo_trf = proctar.id_processo_trf)) ");
			parametros.put(ID_USUARIO, idUsuario);
		} else {
			// Monta o painel do usuário de acordo com as permissões de sigilo do usuário,
			// no
			// permitindo que este visualize processos que estão com nível de acesso maior
			// do que o do usuário logado
			sb.append("AND (proctar.in_segredo_justica = false OR ( ");
			sb.append("proctar.in_segredo_justica = true AND proctar.cd_nivel_acesso <= :nivelAcessoUsuario) ");
			sb.append(") ");
			parametros.put(NIVEL_ACESSO_USUARIO, nivelAcessoSigilo);
		}
	}

	private void appendTag(Integer idTag, StringBuilder sb, Integer idLocalizacaoFisica,
			Map<String, Object> parametros) {
		if (idTag != null && idTag != 0) {
			sb.append(" JOIN client.tb_processo_tag proctag ON proctag.id_processo = proctar.id_processo_trf ");
			sb.append(" JOIN client.tb_tag tag ON tag.id = proctag.id_tag ");

			sb.append(AND);
			sb.append(TAG_IN_SISTEMA_TRUE);
			sb.append(OR_TAG_IN_PUBLICA_TRUE);

			if (idLocalizacaoFisica != null) {
				sb.append(OR_TAG_ID_LOCALIZACAO);
				parametros.computeIfAbsent(ID_LOCALIZACAO_FISICA, k -> idLocalizacaoFisica);
			}
			sb.append(" ) ");
		}
	}

	private void appendCriteriosUsuarioPapel(Boolean isCount, CriterioPesquisa criteriosPesquisa, StringBuilder sb) {
		if (Boolean.FALSE.equals(isCount)) {
			sb.append("LEFT JOIN tb_usuario_login usu on usu.id_usuario = pd.id_usuario_alteracao ");
			if (criteriosPesquisa.getConferidos() == null || !criteriosPesquisa.getConferidos()) {
				sb.append(
						"LEFT JOIN jbpm_variableinstance viii on viii.taskinstance_ = proctar.id_processo_tarefa AND viii.name_ = '"
								+ Variaveis.CONFERIR_PROCESSO_ASSINATURA + "' ");
			}
			sb.append(
					"LEFT JOIN tb_tipo_proc_doc_papel tpdp on tpdp.id_tipo_processo_documento = tpd.id_tipo_processo_documento and tpdp.id_papel = :idPapel ");
		}
	}

	private void appendSelect(Boolean isCount, StringBuilder sb) {
		if (Boolean.TRUE.equals(isCount)) {
			sb.append("SELECT COUNT(DISTINCT pd.id_processo_documento) ");
		} else {
			sb.append(
					"SELECT proctar.id_processo_trf, cabpro.nr_processo, proctar.dt_create_task, proctar.id_processo_tarefa, cabpro.ds_classe_judicial_sigla, viii.stringvalue_, ");
			sb.append("pd.id_processo_documento, pd.dt_inclusao, ");
			sb.append(
					" case when in_binario or (ds_modelo_documento is null) then ds_md5_documento else md5(convert_to(ds_modelo_documento, 'UTF8'))  end as ds_md5_documento, pdb.in_binario, ");
			sb.append("cabpro.ds_orgao_julgador, cabpro.ds_orgao_julgador_colegiado, ");
			sb.append(
					"cabpro.nm_pessoa_autor, cabpro.nm_pessoa_reu, proctar.nm_tarefa, tpd.ds_tipo_processo_documento, tpdp.id_papel, pd.dt_alteracao, usu.ds_nome, cabpro.in_segredo_justica, cabpro.ds_assunto_principal, ");
			sb.append(
					"cabpro.cd_situacao_autor, cabpro.cd_situacao_reu, car.ds_cargo, tpd.id_tipo_processo_documento, ");
			sb.append("cabpro.dt_ultimo_movimento, cabpro.ds_ultimo_movimento, cabpro.vl_peso_prioridade ");
		}
	}

	private void appendCriteriosPage(Boolean isCount, CriterioPesquisa criteriosPesquisa, Query q) {
		if (criteriosPesquisa != null) {
			if (criteriosPesquisa.getPage() != null && !isCount) {
				q.setFirstResult(criteriosPesquisa.getPage());
			}
			if (criteriosPesquisa.getMaxResults() != null && !isCount) {
				q.setMaxResults(criteriosPesquisa.getMaxResults());
			}
		}
	}

	private void appendOrderBy(Boolean isCount, CriterioPesquisa criteriosPesquisa, StringBuilder sb) {
		if (Boolean.FALSE.equals(isCount)) {
			sb.append("ORDER BY ");
			if (criteriosPesquisa != null && StringUtils.isNotEmpty(criteriosPesquisa.getOrdem())
					&& criteriosPesquisa.getOrdem().equals("ASC")) {
				sb.append("cabpro.vl_peso_prioridade ASC, proctar.id_processo_tarefa DESC");
			} else {
				sb.append("cabpro.vl_peso_prioridade DESC, proctar.id_processo_tarefa ASC");
			}
		}
	}

	private void appendTipoDocumentoOrTasInstance(Integer idTipoDocumento, CriterioPesquisa criteriosPesquisa,
			StringBuilder sb, Map<String, Object> parametros) {
		if (idTipoDocumento != null) {
			sb.append("AND tpd.id_tipo_processo_documento = :idTipoDocumento ");
			parametros.put("idTipoDocumento", idTipoDocumento);
		} else if (criteriosPesquisa.getIdTaskInstance() != null) {
			sb.append(AND_ID_TAREFA_TASK_INSTANCE);
			parametros.put(ID_TASKINSTANCE, criteriosPesquisa.getIdTaskInstance());
		}
	}

	private void appendCriteriosPesquisa(CriterioPesquisa criteriosPesquisa, StringBuilder sb,
			Integer idLocalizacaoFisica, Map<String, Object> parametros) {
		if (StringUtils.isNotEmpty(criteriosPesquisa.getNumeroProcesso())) {
			sb.append(
					"AND regexp_replace(cabpro.nr_processo, '\\D', '', 'g') LIKE '%' || regexp_replace(:numeroProcesso, '\\D', '', 'g') || '%' ");
			parametros.put("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
		}

		if (StringUtils.isNotEmpty(criteriosPesquisa.getClasse())) {
			sb.append(
					"and (lower(to_ascii(cabpro.ds_classe_judicial)) like '%'|| lower(to_ascii(:classeJudicial)) || '%' or lower(to_ascii(cabpro.ds_classe_judicial_sigla)) like '%'|| lower(to_ascii(:classeJudicial))  || '%') ");
			parametros.put("classeJudicial", criteriosPesquisa.getClasse());
		}

		if (criteriosPesquisa.getTags() != null && criteriosPesquisa.getTags().length > 0) {
			List<String> tags = Arrays.asList(criteriosPesquisa.getTags()).stream()
					.map(p -> StringUtil.normalize(p).toLowerCase()).collect(Collectors.toList());

			sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag tags ");
			sb.append("INNER JOIN tb_tag tag ON tags.id_tag = tag.id ");
			sb.append("WHERE LOWER(TO_ASCII(tag.ds_tag)) IN (:tags) AND tags.id_processo = proctar.id_processo_trf) ");
			parametros.put("tags", tags);
		}

		if (StringUtils.isNotEmpty(criteriosPesquisa.getPoloAtivo())) {
			sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
			sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
			sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
			sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
			sb.append("AND pp.in_participacao = 'A' ");
			sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloAtivo) || '%' ");
			sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloAtivo) || '%')) ");
			parametros.put("poloAtivo", criteriosPesquisa.getPoloAtivo());
		}
		if (StringUtils.isNotEmpty(criteriosPesquisa.getPoloPassivo())) {
			sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
			sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
			sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
			sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
			sb.append("AND pp.in_participacao = 'P' ");
			sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloPassivo) || '%' ");
			sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloPassivo) || '%')) ");
			parametros.put("poloPassivo", criteriosPesquisa.getPoloPassivo());
		}
		if (criteriosPesquisa.getSemEtiqueta() != null && criteriosPesquisa.getSemEtiqueta()) {
			sb.append("AND NOT EXISTS (SELECT 1 FROM tb_processo_tag pt ");
			sb.append("INNER JOIN tb_tag t on pt.id_tag = t.id ");
			sb.append("where pt.id_processo = proctar.id_processo_trf ");

			sb.append(" AND ( ");
			sb.append(" t.in_sistema = true ");
			sb.append(" OR t.in_publica = true ");

			if (idLocalizacaoFisica != null) {
				sb.append(" and t.id_localizacao = :idLocalizacaoFisica ");
				if (!parametros.containsKey("idLocalizacaoFisica")) {
					parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
				}
			}
			sb.append(" ) ");

			sb.append(")");
		}
	}

	private void appendFiltroSemEtiqueta(StringBuilder sb, Integer idLocalizacaoFisica,
			Map<String, Object> parametros) {
		if (sb != null) {
			sb.append(
					" AND NOT EXISTS (SELECT 1 FROM client.tb_processo_tag proctag JOIN client.tb_tag tag ON tag.id = proctag.id_tag ");
			sb.append(AND);
			sb.append(TAG_IN_SISTEMA_TRUE);
			sb.append(OR_TAG_IN_PUBLICA_TRUE);

			if (idLocalizacaoFisica != null) {
				sb.append(OR_TAG_ID_LOCALIZACAO);
				parametros.computeIfAbsent(ID_LOCALIZACAO_FISICA, k -> idLocalizacaoFisica);
			}
			sb.append(" ) ");

			sb.append(" WHERE proctag.id_processo = proctar.id_processo_trf   ) ");
		}
	}

	private void appendFiltroSomenteComDocumentosNaoLidos(CriterioPesquisa query, StringBuilder sb) {
		if (query.getNaoLidos() != null && query.getNaoLidos()) {
			List<Papel> papeisNaoLidos = ComponentUtil.getComponent(PapelManager.class)
					.getPapeisParaDocumentosNaoLidos();
			StringBuilder idsPapeis = new StringBuilder();
			idsPapeis.append("");

			for (Papel p : papeisNaoLidos) {
				idsPapeis.append(p.getIdPapel());
				idsPapeis.append(",");
			}
			// remove ultima virgula a mais
			if (!idsPapeis.toString().isEmpty())
				idsPapeis.append(idsPapeis.toString().substring(0, idsPapeis.length() - 1));

			sb.append(" AND EXISTS (");
			sb.append(" \t SELECT 1 FROM tb_processo_documento pd ");
			sb.append(" \t WHERE 1=1 ");
			sb.append(" \t AND pd.id_processo = cabpro.id_processo_trf ");
			sb.append(" \t AND pd.in_ativo is true AND pd.dt_juntada is not null ");
			sb.append(" \t AND pd.in_lido is false ");
			sb.append(" \t AND pd.dt_inclusao > cabpro.dt_autuacao ");
			sb.append(" \t AND pd.id_documento_principal is null ");
			sb.append(" \t AND pd.id_papel in ( " + idsPapeis.toString() + " ) ");
			if (query.getTipoProcessoDocumento() != null) {
				sb.append(" \t AND pd.id_tipo_processo_documento = " + query.getTipoProcessoDocumento());
			}
			sb.append(" ) ");
		}
	}

	private void appendFiltroAssunto(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && query.getAssunto() != null) {
			if (StringUtils.isNotBlank(query.getAssunto())) {
				sb.append(
						" AND lower(to_ascii(cabpro.ds_assunto_principal)) like '%' || lower(to_ascii(:assunto)) || '%' ");
				parametros.put("assunto", query.getAssunto());
			}
		}
	}

	private void appendFiltroObjeto(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && query.getObjeto() != null) {
			if (StringUtils.isNotBlank(query.getObjeto())) {
				sb.append(" and lower(to_ascii(cabpro.ds_objeto)) like '%' || lower(to_ascii(:objeto)) || '%' ");
				parametros.put("objeto", query.getObjeto());
			}
		}
	}

	private void appendFiltroDataAutuacao(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && query.getDataAutuacao() != null) {
			sb.append(" and cast(cabpro.dt_autuacao as date) = :dataAutuacao ");
			parametros.put("dataAutuacao", query.getDataAutuacao());
		}
	}

	private void appendFiltroRelacionadoParte(CriterioPesquisa query, StringBuilder sb,
			Map<String, Object> parametros) {
		if (query != null) {
			if (StringUtils.isNotBlank(query.getNomeParte()) || StringUtils.isNotBlank(query.getNumeroDocumento())) {
				sb.append(" AND EXISTS ( ");
				sb.append("  SELECT pp.id_processo_trf FROM tb_processo_parte pp ");
				sb.append("  LEFT JOIN tb_usuario_login usl ON usl.id_usuario = pp.id_pessoa ");
				sb.append("  LEFT JOIN tb_pess_doc_identificacao pdi ON pdi.id_pessoa = pp.id_pessoa ");
				sb.append("  WHERE pp.id_processo_trf = proctar.id_processo_trf ");

				if (StringUtils.isNotBlank(query.getNomeParte())) {
					sb.append(" AND lower(to_ascii(usl.ds_nome)) LIKE '%' || lower(to_ascii(:nomeParte)) || '%' ");
					parametros.put("nomeParte", query.getNomeParte());
				}

				if (StringUtils.isNotBlank(query.getNumeroDocumento())) {
					sb.append(" AND pdi.nr_documento_identificacao = :numeroDocumento");
					sb.append(" and pdi.cd_tp_documento_identificacao = :tipoDocumento ");
					parametros.put("numeroDocumento", query.getNumeroDocumento());
					parametros.put("tipoDocumento", query.isNumeroDocumentoCPF() ? "CPF" : "CPJ");
				}

				if (query.isNumeroDocumentoCPF() || StringUtils.isNotBlank(query.getNomeParte())) {
					sb.append(" AND pp.in_situacao = :situacao ");
					parametros.put("situacao", "A");
				}

				sb.append(" ) ");
			}
		}
	}

	private void appendFiltroCompetencia(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && StringUtils.isNotBlank(query.getCompetencia())) {
			sb.append(" AND lower(to_ascii(cabpro.ds_competencia)) ilike '%' || lower(to_ascii(:competencia)) || '%'");
			parametros.put("competencia", query.getCompetencia());
		}
	}

	private void appendFiltroOrgaoJulgador(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && query.getOrgaoJulgador() != null) {
			sb.append(" AND cabpro.id_orgao_julgador = :paramOrgaoJulgador ");
			parametros.put("paramOrgaoJulgador", query.getOrgaoJulgador());
		}
	}

	private void appendFiltroSomenteSigiloso(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && BooleanUtils.isTrue(query.getSomenteSigiloso())) {
			sb.append(" AND cabpro.in_segredo_justica = :somenteSigiloso ");
			parametros.put("somenteSigiloso", query.getSomenteSigiloso());
		}
	}

	private void appendFiltroSomenteLembrete(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && BooleanUtils.isTrue(query.getSomenteLembrete())) {
			sb.append(" and exists ( ");
			sb.append("  select lem.id_processo_trf from client.tb_lembrete lem ");
			sb.append("  where lem.in_ativo = :status ");
			sb.append("  and ( lem.dt_visivel_ate > now() or lem.dt_visivel_ate is null ) ");
			sb.append("  and lem.id_processo_trf = proctar.id_processo_trf ");
			sb.append(" ) ");

			parametros.put("status", true);
		}
	}

	private void appendExigibilidadeAssinatura(CriterioPesquisa query, StringBuilder sb,
			Map<String, Object> parametros) {
		if (query != null && query.getExigibilidadeAssinatura() != null
				&& !query.getExigibilidadeAssinatura().isEmpty()) {
			sb.append(" and tpdp.in_exigibilidade in (:exigibilidade) ");
			List<String> parametroExigibilidadeAssinatura = new ArrayList<>();
			query.getExigibilidadeAssinatura().stream()
					.forEach(e -> parametroExigibilidadeAssinatura.add(e.toString()));
			parametros.put("exigibilidade", parametroExigibilidadeAssinatura);
		}
	}

	private void appendCamposProcessosTarefa(StringBuilder query) {
		query.append(
				"SELECT proctar.id_processo_trf, cabpro.nr_processo, proctar.dt_create_task, proctar.id_processo_tarefa, cabpro.ds_classe_judicial_sigla, ");
		query.append("cabpro.ds_orgao_julgador, cabpro.ds_orgao_julgador_colegiado, ");
		query.append(
				"cabpro.nm_pessoa_autor, cabpro.nm_pessoa_reu, proctar.nm_tarefa, cabpro.in_segredo_justica, cabpro.in_prioridade, ");
		query.append(
				"proctar.nm_actorid, usuResp.ds_nome, cabpro.ds_assunto_principal, car.ds_cargo, cabpro.id_orgao_julgador, ");
		query.append(
				"cabpro.dt_ultimo_movimento, cabpro.ds_ultimo_movimento, cabpro.vl_peso_prioridade, cabpro.ds_nome_social_autor, cabpro.ds_nome_social_reu, ");
		query.append(
				"cabpro.nr_ano_eleicao, cabpro.ds_tipo_eleicao, cabpro.ds_municipio, cabpro.cd_estado, cabpro.cd_nivel_acesso, cabpro.ds_nome_parte_processo_autor, cabpro.ds_nome_parte_processo_reu ");
	}

	private void appendFiltroPrioridadeProcesso(CriterioPesquisa query, StringBuilder sb,
			Map<String, Object> parametros) {
		if (query.getPrioridadeProcesso() != null && query.getPrioridadeProcesso() > 0) {
			sb.append(
					" and exists (select 1 from tb_proc_prioridde_processo ppp where ppp.id_processo_trf = proctar.id_processo_trf and "
							+ "ppp.id_prioridade_processo = :prioridadeProcesso) ");
			parametros.put("prioridadeProcesso", query.getPrioridadeProcesso());
		}
	}

	private boolean hasFiltroJE(CriterioPesquisa query) {
		return (query != null && ((query.getEleicao() != null && query.getEleicao() > 0)
				|| (query.getEstado() != null && query.getEstado() > 0)
				|| (query.getMunicipio() != null && query.getMunicipio() > 0)));
	}

	private void appendFiltroJE(CriterioPesquisa query, StringBuilder sb, Map<String, Object> parametros) {
		if (query != null && query.getEleicao() != null && query.getEleicao() > 0) {
			sb.append(" and je.id_eleicao = :eleicao ");
			parametros.put("eleicao", query.getEleicao());
		}

		if (query != null && query.getEstado() != null && query.getEstado() > 0) {
			sb.append(" and je.id_estado = :estado ");
			parametros.put("estado", query.getEstado());
		}

		if (query != null && query.getMunicipio() != null && query.getMunicipio() > 0) {
			sb.append(" and je.id_municipio = :municipio ");
			parametros.put("municipio", query.getMunicipio());
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, List<ArquivoAssinatura>> processaAnexos(List<Object[]> resultList) {
		Map<Integer, List<ArquivoAssinatura>> anexos = new HashMap<>();
		List<Integer> idPds = new ArrayList<>(resultList.size());
		for (int i = 0; i < resultList.size(); i++) {
			idPds.add((Integer) resultList.get(i)[6]);
		}
		String hql = "select pd.id_processo_documento,pd.id_documento_principal,pd.dt_inclusao, "
				+ "  case when in_binario or (ds_modelo_documento is null) then ds_md5_documento else md5(convert_to(ds_modelo_documento, 'UTF8'))  end as ds_md5_documento, in_binario "
				+ " from tb_processo_documento pd inner join tb_processo_documento_bin pdb on pd.id_processo_documento_bin = pdb.id_processo_documento_bin "
				+ " where pd.id_documento_principal in (:documentosPrincipais) and pd.in_ativo = true";
		Query q = getEntityManager().createNativeQuery(hql);
		q.setParameter("documentosPrincipais", idPds);
		List<Object[]> res = q.getResultList();
		for (Object[] obj : res) {
			ArquivoAssinatura vo = new ArquivoAssinatura();
			vo.setId((Integer) obj[0]);
			vo.setCodIniDate((Date) obj[2]);
			vo.setHash((String) obj[3]);
			vo.setIsBin((Boolean) obj[4]);
			List<ArquivoAssinatura> arquivos = anexos.get(obj[1]);
			if (arquivos == null) {
				arquivos = new ArrayList<>();
				anexos.put((Integer) obj[1], arquivos);
			}
			arquivos.add(vo);
		}
		return anexos;
	}

	private List<CabecalhoProcesso> filtrarConferidos(List<CabecalhoProcesso> processos) {
		List<CabecalhoProcesso> conferidos = new ArrayList<CabecalhoProcesso>();
		for (CabecalhoProcesso cabecalhoProcesso : processos) {
			if (cabecalhoProcesso.isConferido()) {
				conferidos.add(cabecalhoProcesso);
			}
		}
		return conferidos;
	}

	@SuppressWarnings("unchecked")
	public List<TagDTO> recuperarEtiquetasQuantitativoProcessoTarefaPendente(InformacaoUsuarioSessao informacaoUsuario,
			String tarefa, CriterioPesquisa query) {
		if ((tarefa == null || tarefa.isEmpty()) && (query == null || query.getIdTaskInstance() == null)) {
			return new ArrayList<TagDTO>();
		}

		Query q = montaSQLEtiquetasProcessosTarefa(informacaoUsuario, tarefa, query);
		List<TagDTO> retorno = new ArrayList<TagDTO>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] colunas : resultList) {
			TagDTO et = new TagDTO();
			if (colunas[0] == null) {
				et.setId(0);
				et.setNomeTagCompleto("Sem etiqueta");
				et.setQtdeProcessos(new Integer(colunas[2].toString()));
				retorno.add(0, et);
			} else {
				et.setId(new Integer(colunas[0].toString()));
				et.setNomeTagCompleto(colunas[1].toString());
				et.setQtdeProcessos(new Integer(colunas[2].toString()));
				et.setPossuiFilhos(new Boolean(colunas[3].toString()));
				retorno.add(et);
			}

		}
		return retorno;
	}

	@SuppressWarnings("unchecked")
	public List<TagDTO> recuperarEtiquetasQuantitativoParaAssinatura(InformacaoUsuarioSessao usuarioSesssao,
			Integer tipoDocumento, CriterioPesquisa query) {
		if ((tipoDocumento == null) && (query == null || query.getIdTaskInstance() == null)) {
			return new ArrayList<TagDTO>();
		}

		Query q = montaSQLEtiquetasAssinatura(usuarioSesssao, tipoDocumento, query);
		List<TagDTO> retorno = new ArrayList<TagDTO>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] colunas : resultList) {
			TagDTO et = new TagDTO();
			if (colunas[0] == null) {
				et.setId(0);
				et.setNomeTag("Sem etiqueta");
				et.setQtdeProcessos(new Integer(colunas[2].toString()));
				retorno.add(0, et);
			} else {
				et.setId(new Integer(colunas[0].toString()));
				et.setNomeTag(colunas[1].toString());
				et.setQtdeProcessos(new Integer(colunas[2].toString()));
				et.setPossuiFilhos(new Boolean(colunas[3].toString()));
				retorno.add(et);
			}
		}
		return retorno;
	}

	private Query montaSQLEtiquetasAssinatura(InformacaoUsuarioSessao informacaoUsuario, Integer idTipoDocumento,
			CriterioPesquisa criteriosPesquisa) {
		StringBuilder sb = new StringBuilder(BIG_QUERY_SIZE);

		Integer idOrgaoJulgadorColegiado = informacaoUsuario.getIdOrgaoJulgadorColegiado();
		boolean isServidorExclusivoOJC = informacaoUsuario.isServidorExclusivoOJC();
		List<Integer> idsOrgaoJulgadorCargo = informacaoUsuario.getIdsOrgaoJulgadorCargoVisibilidade();
		List<Integer> idsLocalizacoesFisicas = informacaoUsuario.getIdsLocalizacoesFisicasFilhas();
		Integer idLocalizacaoFisica = informacaoUsuario.getIdLocalizacaoFisica();
		Integer idLocalizacaoModelo = informacaoUsuario.getIdLocalizacaoModelo();
		Integer idPapel = informacaoUsuario.getIdPapel();
		Boolean visualizaSigiloso = informacaoUsuario.getVisualizaSigiloso();
		Integer idUsuario = informacaoUsuario.getIdUsuario();
		Map<String, Object> parametros = new HashMap<String, Object>();

		sb.append("SELECT tag.id, tag.ds_tag, COUNT(*) ");
		sb.append("   , EXISTS(SELECT true FROM client.tb_tag t WHERE t.id_tag_pai=tag.id) ");
		sb.append(" FROM client.tb_processo_tarefa proctar ");
		sb.append(" JOIN jbpm_taskinstance taskinst ON (proctar.id_task_instance = taskinst.id_) ");
		sb.append(" JOIN jbpm_task task ON (task.id_ = taskinst.task_) ");
		sb.append(" JOIN client.tb_cabecalho_processo cabpro ON (cabpro.id_processo_trf = proctar.id_processo_trf) ");
		sb.append("JOIN jbpm_variableinstance vii on vii.processinstance_ = taskinst.procinst_ and vii.name_ IN ('"
				+ Variaveis.MINUTA_EM_ELABORACAO + "', '" + Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO + "') ");
		sb.append(
				"JOIN core.tb_processo_documento pd on (pd.id_processo_documento = cast(vii.longvalue_ as integer) AND pd.dt_juntada IS NULL) ");
		sb.append(
				"JOIN core.tb_processo_documento_bin pdb on pdb.id_processo_documento_bin = pd.id_processo_documento_bin ");
		sb.append(
				"JOIN core.tb_tipo_processo_documento tpd on tpd.id_tipo_processo_documento = pd.id_tipo_processo_documento ");

		sb.append(" LEFT JOIN ( SELECT tag.id, tag.ds_tag, tag.id_localizacao, ");
		sb.append(
				" proctag.id_processo FROM client.tb_processo_tag proctag INNER JOIN client.tb_tag tag ON tag.id = proctag.id_tag ");

		sb.append(AND);
		sb.append(TAG_IN_SISTEMA_TRUE);
		sb.append(OR_TAG_IN_PUBLICA_TRUE);

		if (idLocalizacaoFisica != null) {
			sb.append(OR_TAG_ID_LOCALIZACAO);
			parametros.computeIfAbsent(ID_LOCALIZACAO_FISICA, k -> idLocalizacaoFisica);
		}
		sb.append(" ) ");
		sb.append(" ) AS tag ON tag.id_processo = cabpro.id_processo_trf ");

		sb.append(" WHERE taskinst.issuspended_ = false AND taskinst.isopen_ = true and task.priority_ = 4 ");
		sb.append("and exists (select 1 from core.tb_proc_localizacao_ibpm tl ");
		sb.append("		where tl.id_processo = cabpro.id_processo_trf ");
		sb.append("and tl.id_task_jbpm = taskinst.task_ ");
		sb.append("and tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("and tl.id_papel = :idPapel) ");

		parametros.put("idLocalizacaoModelo", idLocalizacaoModelo);
		parametros.put("idPapel", idPapel);

		if (idTipoDocumento != null) {
			sb.append("and tpd.id_tipo_processo_documento = :idTipoDocumento ");
			parametros.put("idTipoDocumento", idTipoDocumento);
		} else if (criteriosPesquisa.getIdTaskInstance() != null) {
			sb.append("and taskinst.id_ = :idTaskInstance ");
			parametros.put(ID_TASKINSTANCE, criteriosPesquisa.getIdTaskInstance());
		}

		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			sb.append("AND proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
			parametros.put("idsLocalizacoesFisicas", idsLocalizacoesFisicas);
		}

		if (idOrgaoJulgadorColegiado != null) {
			sb.append("AND proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			parametros.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		}

		if (idsOrgaoJulgadorCargo != null && idsOrgaoJulgadorCargo.size() > 0) {
			sb.append("AND proctar.id_orgao_julgador_cargo in (:idOrgaoJulgadorCargo) ");
			parametros.put("idOrgaoJulgadorCargo", idsOrgaoJulgadorCargo);
		}

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			sb.append("and (proctar.in_segredo_justica = false or exists ");
			sb.append("(select 1 from client.tb_proc_visibilida_segredo vis ");
			sb.append("where vis.id_pessoa = :idUsuario and vis.id_processo_trf = proctar.id_processo_trf)) ");
			parametros.put(ID_USUARIO, idUsuario);
		}

		if (informacaoUsuario.getCargoAuxiliar() != null && informacaoUsuario.getCargoAuxiliar()) {
			sb.append("AND ( ");
			sb.append(
					"		( NOT EXISTS (select null FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append("		OR ");
			sb.append(
					"     ( EXISTS (select null FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_processo_trf = proctar.id_processo_trf AND ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append(") ");

			parametros.put("usuLoc", informacaoUsuario.getIdUsuarioLocalizacaoMagistradoServidor());
		}

		if (criteriosPesquisa != null) {
			if (StringUtils.isNotEmpty(criteriosPesquisa.getNumeroProcesso())) {
				sb.append(
						"AND regexp_replace(cabpro.nr_processo, '\\D', '', 'g') LIKE '%' || regexp_replace(:numeroProcesso, '\\D', '', 'g') || '%' ");
				parametros.put("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
			}

			if (StringUtils.isNotEmpty(criteriosPesquisa.getClasse())) {
				sb.append(
						"and (lower(to_ascii(cabpro.ds_classe_judicial)) like '%'|| lower(to_ascii(:classeJudicial)) || '%' or lower(to_ascii(cabpro.ds_classe_judicial_sigla)) like '%'|| lower(to_ascii(:classeJudicial))  || '%') ");
				parametros.put("classeJudicial", criteriosPesquisa.getClasse());
			}

			if (criteriosPesquisa.getTags() != null && criteriosPesquisa.getTags().length > 0) {
				List<String> tags = Arrays.asList(criteriosPesquisa.getTags()).stream()
						.map(p -> StringUtil.normalize(p).toLowerCase()).collect(Collectors.toList());

				sb.append("AND EXISTS (SELECT 1 FROM client.tb_processo_tag tags ");
				sb.append("INNER JOIN client.tb_tag tag ON tags.id_tag = tag.id ");
				sb.append(
						"WHERE LOWER(TO_ASCII(tag.ds_tag)) IN (:tags) AND tags.id_processo = proctar.id_processo_trf) ");
				parametros.put("tags", tags);
			}

			if (StringUtils.isNotEmpty(criteriosPesquisa.getPoloAtivo())) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
				sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
				sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
				sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
				sb.append("AND pp.in_participacao = 'A' ");
				sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloAtivo) || '%' ");
				sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloAtivo) || '%')) ");
				parametros.put("poloAtivo", criteriosPesquisa.getPoloAtivo());
			}
			if (StringUtils.isNotEmpty(criteriosPesquisa.getPoloPassivo())) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
				sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
				sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
				sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
				sb.append("AND pp.in_participacao = 'P' ");
				sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloPassivo) || '%' ");
				sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloPassivo) || '%')) ");
				parametros.put("poloPassivo", criteriosPesquisa.getPoloPassivo());
			}
			if (criteriosPesquisa.getSemEtiqueta() != null && criteriosPesquisa.getSemEtiqueta()) {
				appendFiltroSemEtiqueta(sb, idLocalizacaoFisica, parametros);
			}
		}

		appendFiltroAssunto(criteriosPesquisa, sb, parametros);
		appendFiltroObjeto(criteriosPesquisa, sb, parametros);
		appendFiltroDataAutuacao(criteriosPesquisa, sb, parametros);
		appendFiltroRelacionadoParte(criteriosPesquisa, sb, parametros);
		appendFiltroOrgaoJulgador(criteriosPesquisa, sb, parametros);
		appendFiltroCompetencia(criteriosPesquisa, sb, parametros);
		appendFiltroSomenteSigiloso(criteriosPesquisa, sb, parametros);
		appendFiltroSomenteLembrete(criteriosPesquisa, sb, parametros);

		sb.append("GROUP BY tag.id, tag.ds_tag ORDER BY tag.ds_tag ");

		Query q = getEntityManager().createNativeQuery(sb.toString());

		setQueryParameters(q, parametros);

		return q;
	}

	private Query montaSQLEtiquetasProcessosTarefa(InformacaoUsuarioSessao informacaoUsuario, String tarefa,
			CriterioPesquisa query) {
		StringBuilder sb = new StringBuilder(BIG_QUERY_SIZE);
		Map<String, Object> parametros = new HashMap<String, Object>();

		List<Integer> idsLocalizacoesFisicas = informacaoUsuario.getIdsLocalizacoesFisicasFilhas();
		Integer idLocalizacaoFisica = informacaoUsuario.getIdLocalizacaoFisica();
		Integer idOrgaoJulgadorColegiado = informacaoUsuario.getIdOrgaoJulgadorColegiado();
		boolean isServidorExclusivoOJC = informacaoUsuario.isServidorExclusivoOJC();
		List<Integer> idsOrgaoJulgadorCargo = informacaoUsuario.getIdsOrgaoJulgadorCargoVisibilidade();
		Boolean visualizaSigiloso = informacaoUsuario.getVisualizaSigiloso();

		sb.append("SELECT tag.id, tag.ds_tag_completo, COUNT(*) ");
		sb.append("   , EXISTS(SELECT true FROM client.tb_tag t WHERE t.id_tag_pai=tag.id) ");
		sb.append("FROM tb_processo_tarefa proctar ");
		sb.append("INNER JOIN tb_cabecalho_processo cabpro ON cabpro.id_processo_trf = proctar.id_processo_trf ");
		sb.append("LEFT JOIN ( SELECT tag.id, tag.ds_tag_completo, tag.id_localizacao, proctag.id_processo ");
		sb.append("            FROM tb_processo_tag proctag INNER JOIN tb_tag tag ON tag.id = proctag.id_tag ");

		sb.append(" AND ( ");
		sb.append(" tag.in_sistema = true ");
		sb.append(" OR tag.in_publica = true ");

		if (idLocalizacaoFisica != null) {
			sb.append(" OR tag.id_localizacao = :idLocalizacaoFisica ");
			if (!parametros.containsKey("idLocalizacaoFisica")) {
				parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
			}
		}
		sb.append(" ) ");
		if (query.getSomenteFavoritas() != null && query.getSomenteFavoritas()) {
			sb.append("INNER JOIN tb_tag_favorita tf ON tf.id_tag = proctag.id_tag ");
			sb.append("AND tf.id_usuario = :idUsuarioFavorito ");
		}
		sb.append(" ) AS tag ON tag.id_processo = proctar.id_processo_trf ");

		if (StringUtils.isNotBlank(query.getCompetencia())) {
			sb.append("LEFT JOIN tb_competencia comp ON cabpro.id_competencia = comp.id_competencia ");
		}

		sb.append("WHERE EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("              WHERE tl.id_processo = proctar.id_processo_trf ");
		sb.append("              AND tl.id_task_jbpm = proctar.id_task ");
		sb.append("              AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("              AND tl.id_papel = :idPapel) ");

		parametros.put("idLocalizacaoModelo", informacaoUsuario.getIdLocalizacaoModelo());
		parametros.put("idPapel", informacaoUsuario.getIdPapel());

		if (tarefa != null) {
			sb.append("AND proctar.nm_tarefa = :tarefa ");
			parametros.put("tarefa", tarefa);
		} else if (query.getIdTaskInstance() != null) {
			sb.append(AND_ID_TAREFA_TASK_INSTANCE);
			parametros.put(ID_TASKINSTANCE, query.getIdTaskInstance());
		}

		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			sb.append("AND proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
			if (!parametros.containsKey("idsLocalizacoesFisicas")) {
				parametros.put("idsLocalizacoesFisicas", idsLocalizacoesFisicas);
			}
		}

		if (idOrgaoJulgadorColegiado != null) {
			sb.append("AND proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			parametros.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		}

		if (idsOrgaoJulgadorCargo != null && idsOrgaoJulgadorCargo.size() > 0) {
			sb.append("AND proctar.id_orgao_julgador_cargo in (:idOrgaoJulgadorCargo) ");
			parametros.put("idOrgaoJulgadorCargo", idsOrgaoJulgadorCargo);
		}

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			sb.append("AND (proctar.in_segredo_justica = false OR exists ");
			sb.append(SELECT_PROC_VISIBILIDADE_SEGREDO);
			sb.append("WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)) ");
			parametros.put(ID_USUARIO, informacaoUsuario.getIdUsuario());
		}

		if (informacaoUsuario.getCargoAuxiliar() != null && informacaoUsuario.getCargoAuxiliar()) {
			sb.append("AND ( ");
			sb.append(
					"		( NOT EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm where ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append("		OR ");
			sb.append(
					"     ( EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm where ptlm.id_processo_trf = proctar.id_processo_trf and ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append(") ");

			parametros.put("usuLoc", informacaoUsuario.getIdUsuarioLocalizacaoMagistradoServidor());
		}

		if (query != null) {
			if (StringUtils.isNotEmpty(query.getNumeroProcesso())) {
				sb.append(
						"AND regexp_replace(cabpro.nr_processo, '\\D', '', 'g') LIKE '%' || regexp_replace(:numeroProcesso, '\\D', '', 'g') || '%' ");
				parametros.put("numeroProcesso", query.getNumeroProcesso());
			}
			if (StringUtils.isNotEmpty(query.getClasse())) {
				sb.append("AND (to_ascii(cabpro.ds_classe_judicial) ILIKE '%'|| to_ascii(:classeJudicial) || '%' ");
				sb.append("OR cabpro.ds_classe_judicial_sigla LIKE '%'|| :classeJudicial  || '%') ");
				parametros.put("classeJudicial", query.getClasse());
			}
			if (StringUtils.isNotEmpty(query.getPoloAtivo())) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
				sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
				sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
				sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
				sb.append("AND pp.in_participacao = 'A' ");
				sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloAtivo) || '%' ");
				sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloAtivo) || '%')) ");
				parametros.put("poloAtivo", query.getPoloAtivo());
			}
			if (StringUtils.isNotEmpty(query.getOrgao())) {
				sb.append("AND to_ascii(cabpro.ds_orgao_julgador) ILIKE '%' || to_ascii(:orgao) || '%') ");
				parametros.put("orgao", query.getOrgao());
			}
			if (StringUtils.isNotEmpty(query.getPoloPassivo())) {
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_parte pp ");
				sb.append("LEFT JOIN tb_pess_doc_identificacao as pdi ON (pp.id_pessoa = pdi.id_pessoa) ");
				sb.append("INNER JOIN tb_usuario_login ul ON (ul.id_usuario = pp.id_pessoa)");
				sb.append("WHERE pp.id_processo_trf = proctar.id_processo_trf ");
				sb.append("AND pp.in_participacao = 'P' ");
				sb.append("AND (to_ascii(pdi.ds_nome_pessoa) ilike '%' || to_ascii(:poloPassivo) || '%' ");
				sb.append("OR to_ascii(ul.ds_nome) ilike '%' || to_ascii(:poloPassivo) || '%')) ");
				parametros.put("poloPassivo", query.getPoloPassivo());
			}
			if (query.getTags() != null && query.getTags().length > 0) {
				List<String> tags = Arrays.asList(query.getTags()).stream()
						.map(p -> StringUtil.normalize(p).toLowerCase()).collect(Collectors.toList());

				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag tags ");
				sb.append("INNER JOIN client.tb_tag tag ON tags.id_tag = tag.id ");
				sb.append(
						"WHERE LOWER(TO_ASCII(tag.ds_tag_completo)) IN (:tags) AND tags.id_processo = proctar.id_processo_trf) ");
				parametros.put("tags", tags);
			}

			if (query.getSomenteFavoritas() != null && query.getSomenteFavoritas()) {
				sb.append("and exists (select 1 from tb_processo_tag pt "
						+ "inner join client.tb_tag t on pt.id_tag = t.id "
						+ "inner join client.tb_tag_favorita tf on tf.id_tag = pt.id_tag "
						+ "where tf.id_usuario = :idUsuarioFavorito and pt.id_processo = proctar.id_processo_trf ");

				if (idLocalizacaoFisica != null) {
					sb.append(" and t.id_localizacao = :idLocalizacaoFisica ");
					if (!parametros.containsKey("idLocalizacaoFisica")) {
						parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
					}
				}

				sb.append(")");
				parametros.put("idUsuarioFavorito", informacaoUsuario.getIdUsuario());

			} else if (query.getSemEtiqueta() != null && query.getSemEtiqueta()) {
				sb.append(
						"and not exists (select 1 from tb_processo_tag pt " + "inner join tb_tag t on pt.id_tag = t.id "
								+ "where pt.id_processo = proctar.id_processo_trf ");

				sb.append(" AND ( ");
				sb.append(" t.in_sistema = true ");
				sb.append(" OR t.in_publica = true ");

				if (idLocalizacaoFisica != null) {
					sb.append(" OR t.id_localizacao = :idLocalizacaoFisica ");
					if (!parametros.containsKey("idLocalizacaoFisica")) {
						parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
					}
				}
				sb.append(" ) ");

				sb.append(")");
			}

			appendFiltroAssunto(query, sb, parametros);
			appendFiltroObjeto(query, sb, parametros);
			appendFiltroDataAutuacao(query, sb, parametros);
			appendFiltroRelacionadoParte(query, sb, parametros);
			appendFiltroOrgaoJulgador(query, sb, parametros);
			appendFiltroCompetencia(query, sb, parametros);
			appendFiltroSomenteSigiloso(query, sb, parametros);
			appendFiltroSomenteLembrete(query, sb, parametros);
		}

		sb.append("GROUP BY tag.id, tag.ds_tag_completo ORDER BY tag.ds_tag_completo ");

		Query q = getEntityManager().createNativeQuery(sb.toString());

		setQueryParameters(q, parametros);

		return q;
	}
	
	@SuppressWarnings({ "unchecked"})
	public List<ProcessoTrf> recuperarProcessosUsuarios(
			Boolean isDistinct, 
			Boolean isJusPostulandi,
			String numeroProcesso,
			Integer idUsuario, 
			UsuarioLocalizacao localizacaoAtual,
			Boolean isMagistrado, 
			Boolean isVisualizaSigiloso, 
			Integer idProcuradoria,
			Boolean limitarProcessosProprios,
			Integer tipoParteAdvogado,
			RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoProcurador,
			Boolean isProcessoParadigma) {
		
		UsuarioLocalizacaoMagistradoServidor li = null;
		if (localizacaoAtual != null) {
			li = localizacaoAtual.getUsuarioLocalizacaoMagistradoServidor();
		}

		StringBuilder queryFinal = new StringBuilder();

		String distinct = isDistinct ? "DISTINCT" : "";

		StringBuilder queryPrincipal = new StringBuilder();
		queryPrincipal.append("SELECT " + distinct + "  trf.id_processo_trf ");
		queryPrincipal.append("FROM  ");
		queryPrincipal.append("    tb_processo_trf  trf ");
		queryPrincipal.append("    JOIN tb_processo_parte parte ON trf.id_processo_trf = parte.id_processo_trf ");
		queryPrincipal.append("    JOIN tb_processo processo ON processo.id_processo = trf.id_processo_trf ");
		queryPrincipal.append(
				"    LEFT OUTER JOIN tb_proc_visibilida_segredo visibilidade ON visibilidade.id_processo_trf = trf.id_processo_trf ");

		// Metodo "limitarSigilosos"
		if ((isMagistrado || isVisualizaSigiloso) && li != null) {
			if (li.getOrgaoJulgadorCargo() != null) {
				queryPrincipal.append(
						"JOIN tb_orgao_julgador_cargo cargo ON cargo.id_orgao_julgador = trf.id_orgao_julgador ");
			} else if (li.getOrgaoJulgador() != null) {
				queryPrincipal.append(
						"JOIN tb_orgao_julgador orgaoJulgador ON orgaoJulgador.id_orgao_julgador = trf.id_orgao_julgador ");
			} else if (li.getOrgaoJulgadorColegiado() != null) {
				queryPrincipal.append(
						"JOIN tb_orgao_julgador_colgiado colegiado ON colegiado.id_orgao_julgador_presidente = trf.id_orgao_julgador ");
			}
		}

		// Metodo: "limitarProcessosUsuario"
		if (limitarProcessosProprios) {
			if (idProcuradoria == null) {
				queryPrincipal.append("JOIN tb_pessoa pessoa ON pessoa.id_pessoa = parte.id_pessoa ");
			} else {
				queryPrincipal.append(
						"JOIN tb_pessoa_procuradoria pessoaProcuradoria ON pessoaProcuradoria.id_procuradoria = parte.id_procuradoria ");
				if (tipoAtuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.D)) {
					queryPrincipal.append(
							"LEFT OUTER JOIN tb_pess_proc_jurisdicao pessoaProcuradoriaJurisdicao ON pessoaProcuradoriaJurisdicao.id_pessoa_procuradoria = parte.id_procuradoria ");
				} else if (tipoAtuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.P)) {
					queryPrincipal.append(
							"LEFT OUTER JOIN tb_processo_caixa_adv_proc processoCaixaAdvogadoProcurador ON processoCaixaAdvogadoProcurador.id_processo_trf = trf.id_processo_trf ");
					queryPrincipal.append(
							"LEFT OUTER JOIN tb_caixa_adv_proc caixaAdvogadoProcurador ON caixaAdvogadoProcurador.id_caixa_adv_proc = processoCaixaAdvogadoProcurador.id_caixa_adv_proc ");
					queryPrincipal.append(
							"LEFT OUTER JOIN tb_caixa_representante caixaRepresentante ON caixaRepresentante.id_caixa_adv_proc = caixaAdvogadoProcurador.id_caixa_adv_proc ");
				}
			}
		}
		queryPrincipal.append("WHERE ");
		queryPrincipal.append("    trf.cd_processo_status = 'D' ");

		// numeroProcesso = numeroProcesso.replaceAll("[^a-zZ-Z0-9_]", "");

		numeroProcesso = NumeroProcessoUtil.retiraMascaraNumeroProcesso(numeroProcesso);

		if (numeroProcesso != null && StringUtil.isNotEmpty(numeroProcesso)) {
			if (NumeroProcessoUtil.numeroProcessoValido(numeroProcesso)) {
				queryPrincipal
						.append("AND regexp_replace(processo.nr_processo, '\\D', '', 'g') = '" + numeroProcesso + "' ");
			} else {
				queryPrincipal.append("AND regexp_replace(processo.nr_processo, '\\D', '', 'g') LIKE '%' ||  '"
						+ numeroProcesso + "' || '%' ");
			}
		}

		if (isJusPostulandi) {
			queryPrincipal.append("AND parte.id_pessoa = " + idUsuario + " ");
		}

		StringBuilder queryNaoSigiloso = new StringBuilder();
		queryNaoSigiloso.append("trf.in_segredo_justica = false ");

		if (localizacaoAtual != null) {
			StringBuilder querySigiloso = new StringBuilder();
			querySigiloso.append("trf.in_segredo_justica = true ");

			StringBuilder queryVisivelVisualizadorProcesso = new StringBuilder();
			queryVisivelVisualizadorProcesso.append("visibilidade.id_pessoa = " + idUsuario + " ");

			StringBuilder queryRepresentanteDeVisualizador = new StringBuilder();
			queryRepresentanteDeVisualizador.append("EXISTS( ");
			queryRepresentanteDeVisualizador.append("    SELECT 1 FROM ");
			queryRepresentanteDeVisualizador.append("    tb_proc_visibilida_segredo pvs ");
			queryRepresentanteDeVisualizador.append("    WHERE pvs.id_processo_trf = trf.id_processo_trf ");
			queryRepresentanteDeVisualizador.append("    AND pvs.id_pessoa = parte.id_pessoa ");
			queryRepresentanteDeVisualizador.append("    AND EXISTS ");
			queryRepresentanteDeVisualizador.append("    (");
			queryRepresentanteDeVisualizador.append("        SELECT 1 ");
			queryRepresentanteDeVisualizador.append("        FROM tb_proc_parte_represntante ppr ");
			queryRepresentanteDeVisualizador.append("        WHERE ppr.id_representante = " + idUsuario + " ");
			queryRepresentanteDeVisualizador.append("            AND ppr.id_processo_parte = parte.id_processo_parte ");
			queryRepresentanteDeVisualizador.append("    ) ");
			queryRepresentanteDeVisualizador.append(") ");

			StringBuilder queryVisualizadorProcuradoria = new StringBuilder();
			queryVisualizadorProcuradoria.append("EXISTS( ");
			queryVisualizadorProcuradoria.append("    SELECT 1 ");
			queryVisualizadorProcuradoria.append("    FROM tb_proc_visibilida_segredo pvs ");
			queryVisualizadorProcuradoria
					.append("    JOIN tb_procuradoria proc ON proc.id_procuradoria = pvs.id_procuradoria ");
			queryVisualizadorProcuradoria.append("    WHERE pvs.id_processo_trf = trf.id_processo_trf ");
			queryVisualizadorProcuradoria.append("        AND pvs.id_procuradoria = " + idProcuradoria + " ");
			queryVisualizadorProcuradoria.append("        AND proc.in_ativo = true");
			queryVisualizadorProcuradoria.append(") ");

			StringBuilder queryOrgao = new StringBuilder();
			if ((isMagistrado || isVisualizaSigiloso) && li != null) {
				if (li.getOrgaoJulgadorCargo() != null) {
					queryOrgao.append("cargo.id_orgao_julgador_cargo = "
							+ li.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo() + " ");
				} else if (li.getOrgaoJulgador() != null) {
					queryOrgao.append(
							"orgaoJulgador.id_orgao_julgador = " + li.getOrgaoJulgador().getIdOrgaoJulgador() + " ");
				} else if (li.getOrgaoJulgadorColegiado() != null) {
					queryOrgao.append("colegiado.id_orgao_julgador_colegiado = "
							+ li.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() + " ");
				}
			}

			List<StringBuilder> visibilidadeAtribuida = new ArrayList<StringBuilder>();
			if (idProcuradoria != null) {
				visibilidadeAtribuida.add(queryNaoSigiloso);
				visibilidadeAtribuida.add(queryVisualizadorProcuradoria);
				visibilidadeAtribuida.add(queryVisivelVisualizadorProcesso);
			} else {
				visibilidadeAtribuida.add(queryNaoSigiloso);
				visibilidadeAtribuida.add(queryRepresentanteDeVisualizador);
				visibilidadeAtribuida.add(queryVisivelVisualizadorProcesso);
			}

			if (!queryOrgao.toString().isEmpty()) {
				StringBuilder visibilidadeOrgaoJulgador = new StringBuilder();
				visibilidadeOrgaoJulgador.append("(" + querySigiloso);
				visibilidadeOrgaoJulgador.append("AND " + queryOrgao + ")");
				visibilidadeAtribuida.add(visibilidadeOrgaoJulgador);
			}

			// Metodo: "limitarProcessosUsuario"
			if (limitarProcessosProprios) {
				queryPrincipal.append("AND parte.in_situacao = 'A' ");

				if (idProcuradoria == null) {
					queryPrincipal.append("AND pessoa.id_pessoa = " + idUsuario + " ");
				} else {
					queryPrincipal.append("AND parte.id_tipo_parte <> " + tipoParteAdvogado + " ");
					queryPrincipal.append("AND parte.id_procuradoria = " + idProcuradoria + " ");
					queryPrincipal.append("AND pessoaProcuradoria.id_pessoa = " + idUsuario + " ");

					if (tipoAtuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.G)) {
						queryPrincipal.append("AND pessoaProcuradoria.in_chefe_procuradoria is true ");
					} else if (tipoAtuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.D)) {
						queryPrincipal.append("AND pessoaProcuradoriaJurisdicao.id_jurisdicao = trf.id_jurisdicao ");
						queryPrincipal.append("AND pessoaProcuradoriaJurisdicao.in_ativo = true ");
					} else {
						queryPrincipal.append("AND caixaRepresentante.id_pessoa_fisica = " + idUsuario + " ");
					}
				}
			}

			boolean union = false;
			for (StringBuilder queryOrs : visibilidadeAtribuida) {
				if (union) {
					queryFinal.append(" UNION ");
				}

				queryFinal.append(queryPrincipal);
				queryFinal.append("AND " + queryOrs);

				union = true;
			}

		} else {
			queryFinal.append(queryPrincipal + " AND " + queryNaoSigiloso);
		}

		Query query = entityManager.createNativeQuery(queryFinal.toString());
		List<Integer> idsProcessos = query.getResultList();
		
		if(idsProcessos !=null && idsProcessos.size() > 0) {
			if (isProcessoParadigma) {
				List<ProcessoTrf> processoTrf = new ArrayList<>();
				for (int i = 0; i < idsProcessos.size(); i++) {
					Integer idProcesso = idsProcessos.get(i);
					ProcessoTrf processo = processoTrfDAO.find(ProcessoTrf.class, idProcesso);
					processoTrf.add(processo);
				}
				return processoTrf;
			} else {
				StringBuilder queryStr = new StringBuilder("SELECT p FROM ProcessoTrf AS p WHERE p.idProcessoTrf in (:idsProcessos) ");
				Query q = this.entityManager.createQuery(queryStr.toString());
				q.setParameter("idsProcessos", idsProcessos);
				q.setMaxResults(15);
				return q.getResultList();
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public List<CaixaAdvogadoProcuradorVO> recuperarListaCaixasAcervoJurisdicao(int idJurisdicao) {
		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT caixa.id_caixa_adv_proc, caixa.nm_caixa, caixa.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ");
		query.append("FROM tb_caixa_adv_proc caixa ");
		query.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = caixa.id_jurisdicao) ");
		query.append("WHERE caixa.id_jurisdicao = :idJurisdicao ");
		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());
		q.setParameter("idJurisdicao", idJurisdicao);
		List<Object[]> resultList = q.getResultList();
		List<CaixaAdvogadoProcuradorVO> result = new ArrayList<CaixaAdvogadoProcuradorVO>(resultList.size());
		for (Object[] borderTypes : resultList) {
			Integer idCaixa = (Integer) borderTypes[0];
			String nomeCaixa = (String) borderTypes[1];
			String descricaoCaixa = (String) borderTypes[2];
			Integer idJurisdicaoCaixa = (Integer) borderTypes[3];
			String nomeJurisdicaoCaixa = (String) borderTypes[4];
			result.add(new CaixaAdvogadoProcuradorVO(idCaixa, nomeCaixa, descricaoCaixa, idJurisdicaoCaixa,
					nomeJurisdicaoCaixa, false, true, new BigInteger("0")));
		}
		return result;
	}

	public boolean isConcluso(ProcessoTrf processoJudicial, String tipoConclusao) {

		String sql = "select count(1) " + "from core.tb_processo_evento tpe "
				+ " inner join client.tb_complemento_segmentado tcs on tcs.id_movimento_processo = tpe.id_processo_evento "
				+ " inner join client.tb_tipo_complemento ttc on ttc.id_tipo_complemento = tcs.id_tipo_complemento "
				+ "where tpe.id_processo = :processo " + " and ttc.cd_tipo_complemento = :cdTipoComplemento ";

		BigInteger quantidade = (BigInteger) entityManager.createNativeQuery(sql)
				.setParameter("processo", processoJudicial.getIdProcessoTrf())
				.setParameter("cdTipoComplemento", tipoConclusao).getSingleResult();

		return quantidade != null && quantidade.intValue() > 0;

	}

	@SuppressWarnings("unchecked")
	public void copiarProcessoParaCaixa(ProcessoTrf processo, CaixaAdvogadoProcurador caixaDestino,
			ConsultaProcessoVO criterios) {
		Map<String, Object> parametrosPesquisa = new HashMap<String, Object>(0);
		StringBuilder query = new StringBuilder();
		query.append(" SELECT DISTINCT ptf.id_processo_trf ");
		query.append(" FROM tb_processo_parte pp ");
		query.append(
				" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ");
		query.append(" WHERE (1=1) AND ptf.cd_processo_status = 'D' ");
		query.append(" AND pp.in_participacao in ('A','T') ");
		query.append(" AND ptf.id_processo_trf = :idProcessoTrf ");
		query.append(" AND NOT EXISTS ( ");
		query.append(" SELECT 1 FROM tb_processo_caixa_adv_proc cx ");
		query.append(" WHERE cx.id_processo_trf = :idProcessoTrf ");
		query.append(" AND cx.id_caixa_adv_proc = :idCaixa) ");

		parametrosPesquisa.put("idProcessoTrf", processo.getIdProcessoTrf());
		parametrosPesquisa.put("idCaixa", caixaDestino.getIdCaixaAdvogadoProcurador());

		query = new StringBuilder(aplicarFiltros(query.toString(), null, criterios, parametrosPesquisa));

		Query q = entityManager.createNativeQuery(query.toString());

		for (String key : parametrosPesquisa.keySet()) {
			q.setParameter(key, parametrosPesquisa.get(key));
		}

		List<Object[]> resultList = q.getResultList();

		if (resultList != null && !resultList.isEmpty()) {
			ProcessoCaixaAdvogadoProcurador processoCaixaAdvogadoProcurador = new ProcessoCaixaAdvogadoProcurador();
			processoCaixaAdvogadoProcurador.setProcessoTrf(processo);
			processoCaixaAdvogadoProcurador.setCaixaAdvogadoProcurador(caixaDestino);
			entityManager.persist(processoCaixaAdvogadoProcurador);
			entityManager.flush();
			logger.info("[copiarProcessoParaCaixa] - Processo id: " + processo.getIdProcessoTrf()
					+ " copiado para a caixa id: " + caixaDestino.getIdCaixaAdvogadoProcurador());
		}
	}

	/***
	 * Método criado para contornar a ausência do atributo ProcessoTrf em
	 * processoDocumento por conta da inicializao Lazy desse atributo
	 */
	@SuppressWarnings("unchecked")
	public ProcessoTrf recuperarProcesso(ProcessoDocumento documento) {
		ProcessoTrf retorno = null;
		if (documento.getProcessoTrf() != null) {
			retorno = documento.getProcessoTrf();
		} else {
			StringBuilder queryStr = new StringBuilder(
					"SELECT pd.processoTrf FROM ProcessoDocumento pd WHERE pd = :documento ");
			Query q = this.entityManager.createQuery(queryStr.toString());
			q.setParameter("documento", documento);
			List<ProcessoTrf> processos = q.getResultList();
			if (processos != null && !processos.isEmpty()) {
				retorno = processos.get(0);
			}
		}
		return retorno;
	}

	private String obterQueryProcessos(Integer idPessoa, TipoUsuarioExternoEnum tipoUsuarioExterno,
			Integer idProcuradoria, ConsultaProcessoVO criteriosPesquisa, Map<String, Object> parametrosPesquisa,
			boolean isProcuradoria, boolean numeroProcessoInformado) {
		StringBuilder query = new StringBuilder();
		query.append(" JOIN tb_cabecalho_processo as ptf on ");
		query.append("(");
		query.append("    ptf.id_processo_trf = cx_proc.id_processo_trf ");
		query.append("    AND ptf.id_jurisdicao = :idJurisdicao ");
		query.append("    AND ptf.cd_processo_status = 'D' ");

		if (numeroProcessoInformado) {
			query.append("   AND ptf.nr_processo = :numeroProcesso ");

			if (!parametrosPesquisa.containsKey("numeroProcesso")) {
				parametrosPesquisa.put("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
			}
		}

		query.append("    AND exists ( select 1 from tb_processo_parte pp ");

		if (!isProcuradoria && (tipoUsuarioExterno.equals(TipoUsuarioExternoEnum.A)
				|| tipoUsuarioExterno.equals(TipoUsuarioExternoEnum.AA))) {
			query.append(
					"   LEFT JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_representante = :idPessoa) ");

			if (!parametrosPesquisa.containsKey("idPessoa")) {
				parametrosPesquisa.put("idPessoa", idPessoa);
			}
		}
		query.append("  WHERE pp.id_processo_trf = ptf.id_processo_trf ");
		query.append("    and pp.in_situacao = 'A' ");

		if (isProcuradoria) {
			query.append(" AND pp.id_procuradoria = :idProcuradoria ");

			if (!parametrosPesquisa.containsKey("idProcuradoria")) {
				parametrosPesquisa.put("idProcuradoria", idProcuradoria);
			}
		}

		query.append(limitarVisibilidade(idPessoa, tipoUsuarioExterno, idProcuradoria, parametrosPesquisa));
		query.append(") )     ");
		return query.toString();
	}

	public Boolean existeFluxoDeslocadoParaLocalizacaoUsuario(ProcessoTrf processoTrf, String idsLocalizacoesFisicas) {
		String query = queryExisteFluxoDeslocadoParaLocalizacaoUsuario(":", idsLocalizacoesFisicas);

		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());

		List<?> resultado = q.getResultList();

		return resultado.size() > 0;
	}

	public String queryExisteFluxoDeslocadoParaLocalizacaoUsuario(String prefix, String idsLocalizacoesFisicas) {
		String hqlExists = "select 1 from SituacaoProcesso sp " + " where sp.processoTrf.idProcessoTrf = " + prefix
				+ "idProcessoTrf " + " and (sp.idLocalizacao in (" + idsLocalizacoesFisicas + ") "
				+ "  or sp.cabecalhoProcesso.idLocalizacaoOj in (" + idsLocalizacoesFisicas + ") " + "  )";

		return hqlExists;
	}

	public String queryExisteFluxoDeslocadoParaLocalizacaoUsuario(String prefix, String idsLocalizacoesFisicas,
			Integer nivelAcesso) {
		String hqlExists = queryExisteFluxoDeslocadoParaLocalizacaoUsuario(prefix, idsLocalizacoesFisicas);
		hqlExists += (" and sp.processoTrf.nivelAcesso  <= " + nivelAcesso);

		return hqlExists;
	}

	public List<CabecalhoProcesso> recuperarMetadadosProcessosPorTag(InformacaoUsuarioSessao informacaoUsuario,
			Integer idTag) {

		Integer idLocalizacaoFisica = informacaoUsuario.getIdLocalizacaoFisica();
		Boolean visualizaSigiloso = informacaoUsuario.getVisualizaSigiloso();

		StringBuilder sb = new StringBuilder(BIG_QUERY_SIZE);
		Map<String, Object> parametros = new HashMap<String, Object>();

		sb.append(
				"SELECT proctar.id_processo_trf, cabpro.nr_processo, proctar.dt_autuacao, cast(null as bigint) as id_processo_tarefa, cabpro.ds_classe_judicial_sigla, ");
		sb.append("  cabpro.ds_orgao_julgador, cabpro.ds_orgao_julgador_colegiado, ");
		sb.append(
				"  cabpro.nm_pessoa_autor, cabpro.nm_pessoa_reu, cast(null as varchar) as nm_tarefa, cabpro.in_segredo_justica, cabpro.in_prioridade, ");
		sb.append(
				"  cast(null as varchar) as nm_actorid, cast(null as varchar) as ds_nome, cabpro.ds_assunto_principal, cabpro.ds_orgao_julgador_cargo, cabpro.id_orgao_julgador, ");
		sb.append(
				"  cabpro.dt_ultimo_movimento, cabpro.ds_ultimo_movimento, cabpro.vl_peso_prioridade, cabpro.ds_nome_social_autor, cabpro.ds_nome_social_reu, ");
		sb.append(
				"cabpro.nr_ano_eleicao, cabpro.ds_tipo_eleicao, cabpro.ds_municipio, cabpro.cd_estado, cabpro.cd_nivel_acesso, cabpro.ds_nome_parte_processo_autor, cabpro.ds_nome_parte_processo_reu ");
		sb.append("FROM tb_processo_trf proctar ");
		sb.append("INNER JOIN tb_cabecalho_processo cabpro ON cabpro.id_processo_trf = proctar.id_processo_trf ");
		sb.append("INNER JOIN tb_processo_tag proctag ON proctag.id_processo = proctar.id_processo_trf ");
		sb.append("INNER JOIN tb_tag tag ON tag.id = proctag.id_tag ");

		sb.append(" AND ( ");
		sb.append(" tag.in_sistema = true ");
		sb.append(" OR tag.in_publica = true ");

		if (idLocalizacaoFisica != null) {
			sb.append(" OR tag.id_localizacao = :idLocalizacaoFisica ");
			if (!parametros.containsKey("idLocalizacaoFisica")) {
				parametros.put("idLocalizacaoFisica", idLocalizacaoFisica);
			}
		}
		sb.append(" ) ");

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			sb.append("AND (proctar.in_segredo_justica = false OR EXISTS ");
			sb.append(SELECT_PROC_VISIBILIDADE_SEGREDO);
			sb.append("WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)) ");
			parametros.put(ID_USUARIO, informacaoUsuario.getIdUsuario());
		} else {
			// Monta o painel do usuário de acordo com as permisses de sigilo do usuário, no
			// permitindo que este visualize processos que estão com nível de acesso maior
			// do que o do usuário logado
			sb.append("AND (proctar.in_segredo_justica = false OR ");
			sb.append(
					"			(proctar.in_segredo_justica = true AND proctar.cd_nivel_acesso <= :nivelAcessoUsuario) OR");
			sb.append(
					"			EXISTS (SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)");
			sb.append("		) ");
			parametros.put(NIVEL_ACESSO_USUARIO, informacaoUsuario.getNivelAcessoSigilo());
			parametros.put(ID_USUARIO, informacaoUsuario.getIdUsuario());
		}

		sb.append(" WHERE tag.id = :idTag ");
		parametros.put("idTag", idTag);

		sb.append("ORDER BY cabpro.vl_peso_prioridade DESC, proctar.id_processo_trf DESC");

		Query q = getEntityManager().createNativeQuery(sb.toString());
		setQueryParameters(q, parametros);

		return recuperaMetadados(q);
	}

	@SuppressWarnings("unchecked")
	private List<CabecalhoProcesso> recuperaMetadados(Query q) {
		List<Object[]> resultList = q.getResultList();
		List<CabecalhoProcesso> retorno = new ArrayList<CabecalhoProcesso>();
		Object[] borderTypes = null;
		if (resultList.size() > 0) {
			for (int i = 0; i < resultList.size(); i++) {

				borderTypes = resultList.get(i);

				String nomeAutor = (String) borderTypes[7];
				String nomeReu = (String) borderTypes[8];
				String nomeSocialAutor = (String) borderTypes[20];
				String nomeSocialReu = (String) borderTypes[21];
				String nomeAltenativoAutor = (String) borderTypes[27];
				String nomeAltenativoReu = (String) borderTypes[28];

				CabecalhoProcesso vo = new CabecalhoProcesso();
				Integer idProcesso = (Integer) borderTypes[0];
				vo.setIdProcesso(idProcesso.longValue());
				vo.setNumeroProcesso((String) borderTypes[1]);
				vo.setDataChegada((Date) borderTypes[2]);
				atribuirIdTaskInstance(borderTypes, vo);
				vo.setClasseJudicial((String) borderTypes[4]);
				vo.setOrgaoJulgador((String) borderTypes[5]);
				vo.setOrgaoJulgadorColegiado((String) borderTypes[6]);
				atribuirPoloAtivo(nomeAutor, nomeSocialAutor, nomeAltenativoAutor, vo);
				isEmptyPoloAtivo(vo);
				atribuirPoloPassivo(nomeReu, nomeSocialReu, nomeAltenativoReu, vo);
				isEmptyPoloPassivo(vo);
				vo.setNomeTarefa((String) borderTypes[9]);
				vo.setSigiloso((Boolean) borderTypes[10]);
				vo.setPrioridade((Boolean) borderTypes[11]);
				vo.setLoginResponsavelTarefa((String) borderTypes[12]);
				vo.setNomeResponsavelTarefa((String) borderTypes[13]);
				vo.setAssuntoPrincipal((String) borderTypes[14]);
				vo.setCargoJudicial((String) borderTypes[15]);
				vo.setIdOrgaoJulgador((Integer) borderTypes[16]);
				atribuirUltimoMovimento(borderTypes, vo);
				atribuirDescricaoUltimoMovimento(borderTypes, vo);
				vo.setEleicao(String.valueOf(borderTypes[22]), (String) borderTypes[23]);
				vo.setMunicipioUfEleicao((String) borderTypes[24], (String) borderTypes[25]);
				atribuirNivelAcesso(borderTypes, vo);
				int indiceProximoItemNaLista = i + 1;
				atribuirIdTaskInstanceProximo(resultList, vo, indiceProximoItemNaLista);

				retorno.add(vo);

			}
		} else {
			return new ArrayList<CabecalhoProcesso>(0);
		}
		return retorno;
	}

	private void atribuirNivelAcesso(Object[] borderTypes, CabecalhoProcesso vo) {
		vo.setNivelAcesso(borderTypes[26] != null ? (Integer) borderTypes[26] : null);
	}

	private void atribuirDescricaoUltimoMovimento(Object[] borderTypes, CabecalhoProcesso vo) {
		vo.setDescricaoUltimoMovimento(borderTypes[18] != null ? borderTypes[18].toString() : "ND");
	}

	private void atribuirUltimoMovimento(Object[] borderTypes, CabecalhoProcesso vo) {
		vo.setUltimoMovimento(borderTypes[17] != null ? (Date) borderTypes[17] : null);
	}

	private void atribuirPoloPassivo(String nomeReu, String nomeSocialReu, String nomeAltenativoReu,
			CabecalhoProcesso vo) {
		vo.setPoloPassivo((nomeAltenativoReu != null && !nomeAltenativoReu.isEmpty())
				? StringUtil.retornarNomeExibicao(nomeAltenativoReu, nomeSocialReu)
				: StringUtil.retornarNomeExibicao(nomeReu, nomeSocialReu));
	}

	private void atribuirPoloAtivo(String nomeAutor, String nomeSocialAutor, String nomeAltenativoAutor,
			CabecalhoProcesso vo) {
		vo.setPoloAtivo((nomeAltenativoAutor != null && !nomeAltenativoAutor.isEmpty())
				? StringUtil.retornarNomeExibicao(nomeAltenativoAutor, nomeSocialAutor)
				: StringUtil.retornarNomeExibicao(nomeAutor, nomeSocialAutor));
	}

	private void atribuirIdTaskInstance(Object[] borderTypes, CabecalhoProcesso vo) {
		if (borderTypes[3] != null) {
			vo.setIdTaskInstance(((BigInteger) borderTypes[3]).longValue());
		}
	}

	private void atribuirIdTaskInstanceProximo(List<Object[]> resultList, CabecalhoProcesso vo,
			int indiceProximoItemNaLista) {
		if (indiceProximoItemNaLista < resultList.size() && resultList.get(indiceProximoItemNaLista)[3] != null) {
			vo.setIdTaskInstanceProximo(((BigInteger) resultList.get(indiceProximoItemNaLista)[3]).longValue());
		}
	}

	private void isEmptyPoloPassivo(CabecalhoProcesso vo) {
		if (StringUtils.isEmpty(vo.getPoloPassivo())) {
			vo.setPoloPassivo(NAO_DEFINIDO);
		}
	}

	private void isEmptyPoloAtivo(CabecalhoProcesso vo) {
		if (StringUtils.isEmpty(vo.getPoloAtivo())) {
			vo.setPoloAtivo(NAO_DEFINIDO);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Integer> pegaIdsLocalizacaoProcessoTarefa(Long idProcessoTrf) {
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT id_localizacao FROM client.tb_processo_tarefa WHERE id_processo_trf=:idProcessoTrf");
		Query query = getEntityManager().createNativeQuery(hql.toString());
		query.setParameter("idProcessoTrf", idProcessoTrf);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<CompetenciaAreaDireito> recuperarAreasDireito(Integer idMunicipio) {
		StringBuilder jpql = new StringBuilder(
				"SELECT new CompetenciaAreaDireito(o.idAreaDireito, o.nomeAreaDireito) FROM CompetenciaAreaDireito o ");

		if (idMunicipio != null) {
			jpql.append("JOIN o.jurisdicao p JOIN p.municipioList q WHERE q.municipio.idMunicipio = :idMunicipio ");
		}
		jpql.append("GROUP BY o.idAreaDireito, o.nomeAreaDireito ORDER BY o.nomeAreaDireito");

		Query query = this.entityManager.createQuery(jpql.toString());

		if (idMunicipio != null) {
			query.setParameter("idMunicipio", idMunicipio);
		}

		return query.getResultList();
	}

}
