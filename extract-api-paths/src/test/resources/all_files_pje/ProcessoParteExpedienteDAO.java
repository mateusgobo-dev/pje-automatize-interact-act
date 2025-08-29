package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.query.ProcessoParteExpedienteQuery;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.business.dao.transform.ProcessoParteExpedienteTransform;
import br.jus.cnj.pje.entidades.vo.IntervaloNumeroSequencialProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UnificacaoPessoas;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoDiarioEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * @author cristof
 * 
 */
@Name(ProcessoParteExpedienteDAO.NAME)
public class ProcessoParteExpedienteDAO extends BaseDAO<ProcessoParteExpediente> implements ProcessoParteExpedienteQuery{

	public static final String NAME = "processoParteExpedienteDAO";
	private static final String WHERE = "WHERE (1=1)";
	
	public static enum CriterioPesquisa {
		/**
		 * Destina-se a identificar os atos de comunicação que ainda não foram conhecidos por seu destinatário ou pelos representantes de seu destinatário,
		 * seja esse conhecimento concreto (acesso ao conteúdo), seja ficto (decurso de prazo edilício).
		 */
		INTIMACAO_PENDENTE,
		/**
		 * Destina-se a identificar os atos de comunicação que foram considerados como conhecidos pelo destinatário por decurso de prazo edilício.
		 */
		INTIMACAO_AUTOMATICA,
		/**
		 * Destina-se a identificar os atos de comunicação que foram conhecidos concretamente pelo destinatário ou por seu representante. 
		 */
		INTIMACAO_INTERESSADO,
		/**
		 * Destina-se a identificar os atos de comunicação cujo prazo para apresentação de alguma resposta se deu em até 10 dias antes da consulta.
		 */
		ENCERRADO_MENOS_10,
		/**
		 * Destina-se a identificar os atos de comunicação que não têm prazo para apresentação de resposta definido.
		 */
		SEM_PRAZO,
		
		/**
		 * Destina-se a identificar os atos de comunicação, fechados ou não, que não tiveram resposta.
		 */
		INTIMACAO_NAO_RESPONDIDA;
	}
	 
/*	private String SQL_PENDENTE = "SELECT ppe FROM ProcessoParteExpediente AS ppe " + 
			"	INNER JOIN ppe.processoJudicial proc " +
			"	INNER JOIN proc.processoParteList parte " +
			"	LEFT OUTER JOIN parte.processoParteRepresentanteList rep " +
			"	WHERE proc.processoStatus = 'D' " +
			"	AND ppe.dtCienciaParte IS NULL AND ppe.resposta IS NULL AND ppe.fechado = false " +
			"	AND (( ppe.pessoaParte = parte.pessoa AND rep.parteRepresentante.pessoa = :advogado) OR ppe.pessoaParte IN (:destinatarios))"; */

	private String SQL_PENDENTE = "SELECT DISTINCT ppe FROM ProcessoParteExpediente AS ppe " +
			"	INNER JOIN ppe.processoJudicial proc " +
			"	INNER JOIN proc.processoParteList parte " +
			"	LEFT OUTER JOIN parte.processoParteRepresentanteList rep LEFT JOIN rep.representante pes WITH pes.idUsuario = :advogado " +
			"	WHERE proc.processoStatus = 'D' " +
			"	AND ppe.dtCienciaParte IS NULL AND ppe.resposta IS NULL AND ppe.fechado = false " +
			"	AND ((parte.processoParteRepresentanteList IS NOT EMPTY AND ppe.pessoaParte = parte.pessoa AND rep.representante.idUsuario = :advogado) OR ppe.pessoaParte IN (:destinatarios)) ";

	/*
	 * [PJEII-3117] Destina-se a identificar os documentos que têm pendência de ciência, ou seja, não poderão ter seu teor visualizado por usuário externo.
	 * 
	 * # Via Sistema: com a ciência do destinatário ou confirmação do expediente pelo sistema (no 10º dia);
     * # Diário Eletrônico: com a publicação do expediente. Essa informação é enviada pelo sistema do Diário Eletrônico e recebida pelo PJe via web service.
     * # Central de Mandados: com a devolução do mandado pelo Oficial de Justiça, desde que o tipo de resultado da diligência registrado 
     * não corresponda aos seguintes: "não cumprido", "para redistribuição", "não entregue ao destinatário".
     * # Correios: deve ser criado um campo no PJe para indicação da data de entrega da correspondência. Já há uma ISSUE (PJEII-2967) solicitando 
     * a inclusão dessa funcionalidade. A ciência de um expediente enviado pelo meio de comunicação "Correios" deverá ocorrer, então, com a 
     * indicação da data de entrega por um usuário interno. Após o convênio com a EBCT, a confirmação da entrega poderá ser feita pelo correios via web service.
     * # Carta: por enquanto, este meio de comunicação NÃO deverá bloquear a visualização do expediente pelos usuários externos, por não haver como indicar a ciência de um 
     * expediente por este meio de comunicação.
	 */
	private String SQL_COUNT_PENDENTE_PARA_VISUALIZACAO_TEOR = 
			"SELECT count(ppe) FROM ProcessoParteExpediente AS ppe " +
					"   WHERE " +
					"   ppe.dtCienciaParte IS NULL AND ppe.fechado = false " + 
					"	AND (ppe.processoExpediente.processoDocumentoVinculadoExpediente = :documento " +
					" 	OR ppe.processoExpediente.processoDocumento = :documento) "+
					")";
				
	private String SQL_NAO_RESPONDIDO = "SELECT DISTINCT ppe FROM ProcessoParteExpediente AS ppe " +
			"	INNER JOIN ppe.processoJudicial proc " +
			"	INNER JOIN proc.processoParteList parte " +
			"	LEFT OUTER JOIN parte.processoParteRepresentanteList rep LEFT JOIN rep.representante pes WITH pes.idUsuario = :advogado " +
			"	WHERE proc.processoStatus = 'D' " +
			"	AND ppe.resposta IS NULL AND (ppe.tipoPrazo = 'S' OR ppe.fechado = false) " +
			"	AND ((parte.processoParteRepresentanteList IS NOT EMPTY AND ppe.pessoaParte = parte.pessoa AND rep.representante.idUsuario = :advogado) OR ppe.pessoaParte IN (:destinatarios)) ";

	private static String SQL_INTIMACAO_AUTOMATICA = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.cienciaSistema = true "
		+ "		AND o.dtCiencia IS NOT NULL " + "		AND o.dtPrazoLegal >= :agora " + "		AND o.dtPrazoLegal IS NOT NULL"
		+ "		AND o.resposta IS NULL";

	private static String SQL_INTIMACAO_INTERESSADO = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.cienciaSistema = false "
		+ "		AND o.dtCiencia IS NOT NULL " + "		AND o.dtPrazoLegal >= :agora " + "		AND o.dtPrazoLegal IS NOT NULL"
		+ "		AND o.resposta IS NULL";

	private static String SQL_ENCERRADO_MENOS_10 = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.dtPrazoLegal IS NOT NULL"
		+ "		AND o.dtPrazoLegal >= :dataLimite " + "		AND o.dtPrazoLegal < :agora" + "		AND o.resposta IS NULL";

	private static String SQL_SEM_PRAZO = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE (o.prazoLegal IS NULL OR o.prazoLegal = 0) " + "		AND o.resposta IS NULL";
	
	private static String COUNT_MAPA_PENDENTES= "SELECT id_pd, sum(pend) FROM (   "
			+ "SELECT pe2.id_processo_documento as id_pd,count(1) AS pend "
			+ "FROM tb_proc_parte_expediente ppe2 "
			+ "INNER JOIN tb_processo_expediente pe2 ON ppe2.id_processo_expediente = pe2.id_processo_expediente "
			+ "WHERE (ppe2.dt_ciencia_parte IS NULL) "
			+ "		AND ppe2.in_fechado = FALSE "
			+ "		AND pe2.id_processo_documento IN (:numDocs) "
			+ "GROUP BY pe2.id_processo_documento "

			+ ") AS tabubd "
			+ "GROUP BY id_pd ";
	
	@Override
	public Integer getId(ProcessoParteExpediente e){
		return e.getIdProcessoParteExpediente();
	}

	// ----------------------------INFOX-----------------------------------------------
	/**
	 * Conta todos os registros que as partes estão cientes.
	 * 
	 * @param pe ProcessoExpediente que se deseja contar as partes cientes.
	 * @return Qntdade de partes cientes
	 */
	public Long countPartesNaoCientes(ProcessoExpediente pe){
		String query = "SELECT COUNT(ppe) FROM ProcessoParteExpediente AS ppe WHERE ppe.processoExpediente = :pe AND ppe.dtCienciaParte IS NULL";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("pe", pe);
		Long result = EntityUtil.getSingleResult(q);
		return result;
	}

	public Long countPartesIntimacaoAutomaticaPendente(ProcessoTrf p){
		String query = "SELECT COUNT(ppe) FROM ProcessoParteExpediente AS ppe " +
				"WHERE ppe.pendencia IS NOT NULL AND ppe.processoExpediente.meioExpedicaoExpediente = 'E' AND ppe.processoExpediente.processoTrf = :proc";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("proc", p);
		Long result = EntityUtil.getSingleResult(q);
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> listPartesIntimacaoAutoPendente(int p){
		String query = "SELECT ppe from ProcessoParteExpediente AS ppe " +
				"WHERE ppe.pendencia IS NOT NULL AND ppe.processoExpediente.meioExpedicaoExpediente = 'E' AND ppe.processoExpediente.processoTrf = :proc";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("proc", p);
		List<ProcessoParteExpediente> list = q.getResultList();
		return list;
	}

	/**
	 * Conta os expediente que possuem alguma entidade intimada.
	 * 
	 * @param pessoa ProcessoTrf que deseja obter os expedientes
	 * @return
	 */
	public Long countExpedienteEntidadeIntimacao(ProcessoTrf processoJudicial, Pessoa pessoa){
		String query = "SELECT COUNT(ppe) from ProcessoParteExpediente AS ppe " +
				"	INNER JOIN ppe.processoExpediente AS pe " +
				"	INNER JOIN ppe.pessoaParte AS pp " +
				"	INNER JOIN pp.pessoaProcuradoriaEntidadeList AS ent " +
				"	INNER JOIN ent.pessoaProcuradorProcuradoriaList AS pppList " +
				" WHERE pe.processoTrf = :proc" +
				"	AND ppe.dtCienciaParte IS NOT NULL " +
				"	AND (pp = :pessoa " +
				"				OR " +
				"			 pp IN (SELECT ppp.pessoaProcuradoriaEntidade.pessoa FROM PessoaProcuradorProcuradoria AS ppp WHERE ppp.pessoaProcurador = :pessoa)" +
				"			)";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("proc", processoJudicial);
		q.setParameter("pessoa", pessoa);
		Long result = EntityUtil.getSingleResult(q);
		return result;
	}

	/**
	 * Verifica se o advogado informado no parametro foi intimado em algum expediente do processo.
	 * 
	 * @param processoJudicial
	 * @param pessoa
	 * @return
	 */
	public Long countAdvogadoIntimadoExpediente(ProcessoTrf processoJudicial, Pessoa pessoa, TipoParte tipoParte){
		String query = "SELECT COUNT(ppe) FROM ProcessoParteExpediente AS ppe " +
				"	WHERE ppe.processoExpediente.processoTrf = :proc " +
				"	AND ppe.dtCienciaParte IS NOT NULL " +
				"	AND (ppe.pessoaParte = :pessoa " +
				"				OR" +
				"			  ppe.pessoaParte " +
				"				IN (SELECT pp FROM ProcessoParte AS pp " +
				"						INNER JOIN pp.processoParteRepresentanteList AS ppList " +
				"						INNER JOIN ppList.processoParteRepresentante AS ppr " +
				"					  WHERE ppList.tipoRepresentante = :tipoParte AND ppr.pessoa = :pessoa)" +
				"			)";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("proc", processoJudicial);
		q.setParameter("pessoa", pessoa);
		q.setParameter("tipoParte", tipoParte);
		Long result = EntityUtil.getSingleResult(q);
		return result;
	}

	public Long countExpedienteNaoFechado(ProcessoTrf processoJudicial){
		String query = "select count(o) from ProcessoParteExpediente o "
		        + "where o.processoJudicial = :processo "
		        + "  and o.fechado = false "
				+ "  and not exists(select pde from ProcessoDocumentoExpediente pde"
				+ "    where pde.processoDocumento.processoDocumentoBin.certChain is null "
				+ "    and pde.processoDocumento.processoDocumentoBin.signature is null "
				+ "    and pde.anexo = false " 
				+ "    and pde.processoExpediente = o.processoExpediente) ";
		Query q = getEntityManager().createQuery(query);
		q.setParameter(ProcessoTrf.ATTR.PROCESSO, processoJudicial);
		return EntityUtil.getSingleResult(q);
	}
	
	/**
	 * Método que retorna a quantidade de expedientes que necessitam de ciência, de acordo com as regras da [PJEII-3117]
	 * @param documento
	 * @return return > 0 -> existe pendencia de ciência
	 */
	public Long contagemExpedientesPendentesCiencia(ProcessoDocumento documento, Pessoa pessoa){
		return contagemExpedientesPendentesCiencia(documento);
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> mapaExpedientesPendentes(List<Integer> lista){
		
		String str = COUNT_MAPA_PENDENTES;
		List<Integer> listaRet = new ArrayList<Integer>();
		
		Query q = getEntityManager().createNativeQuery(str);
		q.setParameter("numDocs", lista);
		
		List<Object[]> listaObj = q.getResultList();
		
		if(listaObj != null && !listaObj.isEmpty()){
			for(Object[]obj : listaObj){
				listaRet.add((Integer)obj[0]);
			}
		}
		return listaRet;
	}
	
	/**
	 * Retorna quantidade de expedientes pendentes de ciência para os procuradores representantes 
	 * @param documento
	 * @param pessoa
	 * @param ids
	 * @return return > 0 -> existe pendencia de ciência
	 */
	public Long contagemExpedientesPendentesCienciaProcuradoria(ProcessoDocumento documento, Pessoa pessoa, List<Integer> ids){
		String str = SQL_COUNT_PENDENTE_PARA_VISUALIZACAO_TEOR;
		Query q = getEntityManager().createQuery(str);
		q.setParameter("documento", documento);
		Long result = EntityUtil.getSingleResultCount(q);
		return result;
	}
	
	/**
	 * Método que retorna a quantidade de expedientes que necessitam de ciência, de acordo com as regras da [PJEII-3117]
	 * @param documento
	 * @return return > 0 -> existe pendencia de ciência
	 */
	@SuppressWarnings("unchecked")
	public Long contagemExpedientesPendentesCiencia(ProcessoDocumento documento){
		String str = COUNT_MAPA_PENDENTES;
		List<ProcessoDocumento> listaPd = new ArrayList<ProcessoDocumento>();
		listaPd.add(documento);
		
		Query q = getEntityManager().createNativeQuery(str);
		q.setParameter("numDocs", listaPd);
		
		List<Object[]> listaObj = q.getResultList();
		
		return listaObj != null ? new Long(listaObj.size()) : 0;
	}

	/**
	 * Método que retorna todas as partes a partir de uma lista de idprocesso, verificando todas as condições necessárias, entre elas se a parte deu
	 * ciencia, se o meio de expedição do expediente é 'E', se o prazo legal não está venciado e se o processo tem ao menos um documento que seja
	 * acórdão ou sentença.
	 * 
	 * @param dataAtual
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> listarPartesTransitoJulgadoAuto(List<Integer> idProcessoList, Date dataAtual,
			TipoProcessoDocumento acordao, TipoProcessoDocumento sentenca){
		Query q = getEntityManager().createQuery(LIST_EXPEDIENTE_BY_ID_PROCESSO_LIST_QUERY);
		q.setParameter(QUERY_PARAM_ID_PROCESSO_LIST, idProcessoList);
		q.setParameter(QUERY_PARAM_DATA_ATUAL, dataAtual);
		q.setParameter(QUERY_PARAM_TIPO_PD_ACORDAO, acordao);
		q.setParameter(QUERY_PARAM_TIPO_PD_SENTENCA, sentenca);
		List<ProcessoParteExpediente> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> processoParteExpedienteComDocumentoList(ProcessoTrf processoTrf){
		Query q = getEntityManager().createQuery(PROCESSO_PARTE_EXPEDIENTE_COM_DOCUMENTO_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_TRF, processoTrf);
		List<ProcessoParteExpediente> result = q.getResultList();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacao(Pessoa advogado, ProcessoTrf processoJudicial, int firstRow, int maxRows, CriterioPesquisa criterio, Pessoa...representados){
		String query = null;
		Query q = null;
		switch (criterio) {
		case INTIMACAO_PENDENTE:
			query = SQL_PENDENTE;
			if(processoJudicial != null){
				query += " AND proc = :processoJudicial";
			}
			q = entityManager.createQuery(query);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			if(processoJudicial != null){
				q.setParameter("processoJudicial", processoJudicial);
			}
			break;
		case INTIMACAO_NAO_RESPONDIDA:
			query = SQL_NAO_RESPONDIDO;
			if(processoJudicial != null){
				query += " AND proc = :processoJudicial";
			}
			q = entityManager.createQuery(query);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			if(processoJudicial != null){
				q.setParameter("processoJudicial", processoJudicial);
			}
			break;
		default:
			break;
		}
		if (firstRow > 0){
			q.setFirstResult(firstRow);
		}
		if (maxRows > 0){
			q.setMaxResults(maxRows);
		}
		try{
			return q.getResultList();
		} catch (Exception e){
			throw new PJeDAOException(e.getLocalizedMessage(), e);
		}
	}

	public long contagemAtos(Pessoa advogado, ProcessoTrf processoJudicial, CriterioPesquisa criterio, Pessoa...representados){
		Query q = null;
		Date agora = null;
		if (representados.length == 0){
			throw new IllegalArgumentException(
					"A lista de destinatários para pesquisa deve conter pelo menos um destinatário.");
		}
		String sql = null;
		switch (criterio){
		case INTIMACAO_PENDENTE:
			sql = SQL_PENDENTE.replace("SELECT DISTINCT ppe FROM ", "SELECT count(DISTINCT ppe.idProcessoParteExpediente) FROM ");
			if(processoJudicial != null){
				sql += " AND proc =:proc ";
			}
			q = getEntityManager().createQuery(sql);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			if(processoJudicial != null){
				q.setParameter("proc", processoJudicial);
			}
			break;
		case INTIMACAO_NAO_RESPONDIDA:
			sql = SQL_NAO_RESPONDIDO.replace("SELECT DISTINCT ppe FROM ", "SELECT count(DISTINCT ppe.idProcessoParteExpediente) FROM ");
			if(processoJudicial != null){
				sql += " AND proc = :proc ";
			}
			q = getEntityManager().createQuery(sql);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			if(processoJudicial != null){
				q.setParameter("proc", processoJudicial);
			}
		case INTIMACAO_AUTOMATICA:
			sql = SQL_INTIMACAO_AUTOMATICA.replace("SELECT DISTINCT ppe FROM ", "SELECT count(DISTINCT ppe.idProcessoParteExpediente) FROM ");
			agora = new Date();
			q = getEntityManager().createQuery(sql);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			q.setParameter("agora", agora);
			break;
		case INTIMACAO_INTERESSADO:
			sql = SQL_INTIMACAO_INTERESSADO.replace("SELECT DISTINCT ppe FROM ", "SELECT count(DISTINCT ppe.idProcessoParteExpediente) FROM ");
			agora = new Date();
			q = getEntityManager().createQuery(sql);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			q.setParameter("agora", agora);
			break;
		case ENCERRADO_MENOS_10:
			sql = SQL_ENCERRADO_MENOS_10.replace("SELECT DISTINCT ppe FROM ", "SELECT count(DISTINCT ppe.idProcessoParteExpediente) FROM ");
			q = getEntityManager().createQuery(sql);
			agora = new Date();
			Date date = DateUtil.dataMenosDias(agora, 10);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Util.isEmpty(Arrays.asList(representados))?null:Arrays.asList(representados));
			q.setParameter("agora", agora);
			q.setParameter("dataLimite", date);
			break;
		case SEM_PRAZO:
			sql = SQL_SEM_PRAZO.replace("SELECT DISTINCT ppe FROM ", "SELECT count(DISTINCT ppe.idProcessoParteExpediente) FROM ");
			q = getEntityManager().createQuery(sql);
			q.setParameter("advogado", advogado.getIdUsuario());
			q.setParameter("destinatarios", Arrays.asList(representados));
			break;
		}
		return (Long) q.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacaoPendentesCiencia(ProcessoTrf...processos){
		boolean ilimitada = (processos == null || processos.length == 0); 
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe WHERE ppe.dtCienciaParte IS NULL AND " +
				" ((ppe.processoExpediente.meioExpedicaoExpediente = 'E') OR (ppe.processoExpediente.meioExpedicaoExpediente = 'C')) " +
				" AND ppe.fechado = false");
		if(!ilimitada){
			query.append(" AND ppe.processoJudicial IN (:processos)");
		}
		Query q = entityManager.createQuery(query.toString());
		if(!ilimitada){
			q.setParameter("processos", Arrays.asList(processos));
		}
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getAtosComunicacaoPendentesCiencia(List<ExpedicaoExpedienteEnum> meios){
		String query = "SELECT ppe.idProcessoParteExpediente FROM ProcessoParteExpediente AS ppe " +
				"	WHERE ppe.dtCienciaParte IS NULL " +
				"		AND ppe.processoExpediente.meioExpedicaoExpediente IN (:meios) " +
				"		AND ppe.fechado = false";
		Query q = entityManager.createQuery(query); 
		q.setParameter("meios", meios);
		return q.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacaoPendentes(ProcessoTrf...processos){
		boolean ilimitada = (processos == null || processos.length == 0); 
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe WHERE ppe.fechado = false AND (ppe.tipoPrazo != 'S' AND ((ppe.dtPrazoLegal IS NOT NULL) OR (ppe.prazoLegal IS NOT NULL AND ppe.prazoLegal > 0)))");
		if(!ilimitada){
			query.append(" AND ppe.processoJudicial IN (:processos)");
		}
		Query q = entityManager.createQuery(query.toString());
		if(!ilimitada){
			q.setParameter("processos", Arrays.asList(processos));
		}
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacaoEletronicoPendentes(ProcessoTrf...processos){
		boolean ilimitada = (processos == null || processos.length == 0); 
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe WHERE ppe.fechado = false AND ppe.processoExpediente.meioExpedicaoExpediente = 'E' AND (ppe.tipoPrazo != 'S' AND (ppe.prazoLegal IS NOT NULL AND ppe.prazoLegal > 0 OR ppe.dtPrazoLegal IS NOT NULL))");
		if(!ilimitada){
			query.append(" AND ppe.processoJudicial IN (:processos)");
		}
		Query q = entityManager.createQuery(query.toString());
		if(!ilimitada){
			q.setParameter("processos", Arrays.asList(processos));
		}
		return q.getResultList();
	}

	/**
	 * Recupera a lista de atos de comunicação com data certa cuja data ocorrerá em 
	 * até um número de dias dado a partir do momento atual.
	 * 
	 * @param diasAntes o número de dias a partir do qual o ato de comunicação deve ser considerado incluído na lista
	 * @param dataReferencia a data de referência
	 * @param processos os processos a serem investigados
	 * @return
	 */
	public List<ProcessoParteExpediente> getAtosComunicacaoDataCerta(long diasAntes, ProcessoTrf...processos){
		return getAtosComunicacaoDataCerta(diasAntes, new Date(), processos); 
	}
	
	/**
	 * Recupera a lista de atos de comunicação com data certa cuja data concreta ocorreu ou ocorrerá
	 * até um número de dias dado, contados da data de referência.
	 * 
	 * @param diasAntes o número de dias a partir do qual o ato de comunicação deve ser considerado incluído na lista
	 * @param dataReferencia a data de referência
	 * @param processos os processos a serem investigados
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacaoDataCerta(long diasAntes, Date dataReferencia, ProcessoTrf...processos){
		boolean ilimitada = (processos == null || processos.length == 0);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dataReferencia);
		cal.add(Calendar.DAY_OF_YEAR, (int) diasAntes);
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe WHERE ppe.fechado = false " +
				"AND ppe.tipoPrazo IN ('C') AND ppe.dtPrazoLegal < :dataReferencia");
		if(!ilimitada){
			query.append(" AND ppe.processoJudicial IN (:processos)");
		}
		Query q = entityManager.createQuery(query.toString());
		if(!ilimitada){
			q.setParameter("processos", Arrays.asList(processos));
		}
		q.setParameter("dataReferencia", cal.getTime());
		return q.getResultList();
	}
	
	/**
	 * Recupera a lista de atos de comunicação que não têm prazo para resposta e cuja data de criação dista 
	 * um determinado número de dias da data atual.
	 * 
	 * @param diasEspera o número de dias a partir dos quais um ato de comunicação deve ser considerado como apto a entrar na lista
	 * @param processos lista de processos em relação aos quais se pretende investiar os atos de comunicação 
	 * @return a lista de atos incluídos nos critérios
	 */
	public List<ProcessoParteExpediente> getAtosComunicacaoSemPrazo(long diasEspera, ProcessoTrf...processos){
		return getAtosComunicacaoSemPrazo(diasEspera, new Date(), processos);
	}
	
	/**
	 * Recupera a lista de atos de comunicação que não têm prazo para resposta e cuja data de criação dista 
	 * um determinado número de dias da data dada.
	 * 
	 * @param diasEspera o número de dias a partir dos quais um ato de comunicação deve ser considerado como apto a entrar na lista
	 * @param dataReferencia a data de referência
	 * @param processos lista de processos em relação aos quais se pretende investiar os atos de comunicação 
	 * @return a lista de atos incluídos nos critérios
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacaoSemPrazo(long diasEspera, Date dataReferencia, ProcessoTrf...processos){
		boolean ilimitada = (processos == null || processos.length == 0);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dataReferencia);
		cal.add(Calendar.DAY_OF_YEAR, (int) - diasEspera);
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe " +
				"WHERE ppe.fechado = false " +
				"AND (ppe.tipoPrazo IN ('S') OR ppe.prazoLegal is null OR ppe.prazoLegal = 0) " +
				"AND ppe.processoExpediente.dtCriacao < :dataReferencia");
		if(!ilimitada){
			query.append(" AND ppe.processoJudicial IN (:processos)");
		}
		Query q = entityManager.createQuery(query.toString());
		if(!ilimitada){
			q.setParameter("processos", Arrays.asList(processos));
		}
		q.setParameter("dataReferencia", cal.getTime());
		return q.getResultList();
	}

	/**
	 * Retorna a lista de expedientes existentes no sistema ou nos processos dados que já tiveram o prazo transcorrido.
	 * 
	 * @param processos
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getAtosComunicacaoDecorridos(Date dataReferencia, ProcessoTrf...processos){
		boolean ilimitada = (processos == null || processos.length == 0);
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe " +
				"WHERE ppe.fechado = false AND " +
				"	(ppe.dtCienciaParte IS NOT NULL AND ppe.tipoPrazo NOT IN ('S') AND ppe.prazoLegal > 0 AND ppe.dtPrazoLegal < :dataReferencia)");
		if(!ilimitada){
			query.append(" AND ppe.processoJudicial IN (:processos)");
		}
		Query q = entityManager.createQuery(query.toString());
		if(!ilimitada){
			q.setParameter("processos", Arrays.asList(processos));
		}
		q.setParameter("dataReferencia", dataReferencia);
		return q.getResultList();
	}
	
	/**
	 * Consulta os expedientes pendentes para a parte a partir da data informada.
	 * 
	 * @param pessoa Parte
	 * @param dataInicioPrazoLegal Data de início do prazo legal.
	 * @return coleção de expedientes pendentes para a parte.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> consultarExpedientesPendentes(Pessoa pessoa, Date dataInicioPrazoLegal) {
		org.hibernate.Criteria criteria = novoCriteriaConsultarExpedientesPendentes(dataInicioPrazoLegal);
		criteria.add(Restrictions.eq("pessoaParte", pessoa));
		
		return criteria.list();
	}

	/**
	 * Consulta os expedientes pendentes para a parte a partir da data informada.
	 * 
	 * @param pessoas Partes
	 * @param dataInicioPrazoLegal Data de início do prazo legal.
	 * @return coleção de expedientes pendentes para a parte.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> consultarExpedientesPendentes(List<Pessoa> pessoas, Date dataInicioPrazoLegal) {
		org.hibernate.Criteria criteria = novoCriteriaConsultarExpedientesPendentes(dataInicioPrazoLegal);
		
		Disjunction ouPessoas = Restrictions.disjunction();
		for (Pessoa pessoa : pessoas) {
			ouPessoas.add(Restrictions.eq("pessoaParte", pessoa));
		}
		criteria.add(ouPessoas);
		return criteria.list();
	}

	/**
	 * Consulta os expedientes pendentes do processo a partir da data informada.
	 * 
	 * @param idProcesso Id do processo
	 * @param dataInicioPrazoLegal Data de início do prazo legal.
	 * @return coleção de expedientes pendentes para a parte.
	 */	 
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> consultarExpedientesPendentes(Integer idProcesso, Date dataInicioPrazoLegal) {
		org.hibernate.Criteria criteria = novoCriteriaConsultarExpedientesPendentes(dataInicioPrazoLegal);
		criteria.add(Restrictions.eq("processoJudicial.idProcessoTrf", idProcesso));		
		return criteria.list();
	}
	
	/**
	 * Consulta todos os processo parte expediente de uma determinada pessoa.
	 * 
	 * @param pessoa Parte
	 * @return coleção de expedientes da pessoa parte.
	 * */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> consultarExpedientesPorPessoa(Pessoa pessoa) {		
		StringBuilder query = new StringBuilder("SELECT proc FROM ProcessoParteExpediente AS proc ");
		query.append("WHERE proc.pessoaParte = :pessoaParte");
		
		Query q = getEntityManager().createQuery(query.toString());		
	
		q.setParameter("pessoaParte", pessoa);		
		return q.getResultList();
	}
	
	/**
	 * Consulta todos os processo parte expediente de uma determinada pessoa unificada.
	 * 
	 * @param pessoa Parte
	 * @return coleção de expedientes da pessoa parte.
	 * */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> consultarExpedientesUnificados(UnificacaoPessoas ups) {		
		StringBuilder query = new StringBuilder("SELECT proc FROM ProcessoParteExpediente AS proc ");
		query.append("JOIN proc.processoParteExpedienteHistoricoList AS hist  ");		
		query.append("WHERE hist.unificacao = :unificacaoPessoas");
		
		Query q = getEntityManager().createQuery(query.toString());		
	
		q.setParameter("unificacaoPessoas", ups);		
		return q.getResultList();
	}
	
	/**
	 * Retorna o critério de expedientes pendentes a partir da data informada.
	 * 
	 * @param dataInicioPrazoLegal
	 * @return criteria de consulta de expedientes pendentes.
	 */
	protected org.hibernate.Criteria novoCriteriaConsultarExpedientesPendentes(Date dataInicioPrazoLegal) {
		Session session = HibernateUtil.getSession();
		
		org.hibernate.Criteria criteria = session.createCriteria(ProcessoParteExpediente.class);
		criteria.add(Restrictions.isNull("dtCienciaParte"));
		criteria.add(Restrictions.isNull("resposta"));
		criteria.add(Restrictions.eq("fechado", Boolean.FALSE));
		if (dataInicioPrazoLegal != null) {
			criteria.add(Restrictions.ge("dtPrazoLegal", dataInicioPrazoLegal));
		} else {
			criteria.add(Restrictions.isNull("dtPrazoLegal"));
		}

		return criteria;
	}
	
	/**
	 * Indica se algum dos documentos de um expediente dado é parte de algum outro ato de comunicação
	 * cuja ciência ainda não foi dada.
	 * 
	 * @param expediente o expediente a respeito do qual se pretende verificar
	 * @return true, se o expediente dado tem pelo menos um documento que seja parte de outro ato de comunicação em relação ao qual
	 * ainda não houve ciência pelo interessado.
	 */
	public boolean temDocumentoPendenteCiencia(ProcessoParteExpediente expediente){
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 1 FROM ProcessoParteExpediente ppe ");
		sb.append("INNER JOIN ppe.processoExpediente pe ");
		sb.append("WHERE pe.idProcessoExpediente != :expediente ");
		sb.append("AND pe.processoDocumento = (SELECT o.processoDocumento FROM ProcessoExpediente o WHERE o.idProcessoExpediente = :expediente) ");
		sb.append("AND ppe.fechado = false ");
		
		Query q = entityManager.createQuery(sb.toString());
		q.setParameter("expediente", expediente.getProcessoExpediente().getIdProcessoExpediente());
		try {
			q.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}
	
	/**
	 * Verifica se todos os expedientes de um processo tiveram ciência dada.
	 * @param processoTrf
	 * @return
	 */
	public boolean todosTomaramCiencia(ProcessoTrf processoTrf) {
		StringBuilder query = new StringBuilder("select count(*) from ProcessoParteExpediente as ppe ");
		query.append(" where ppe.dtCienciaParte is null ");
		query.append(" and ppe.processoJudicial = :processoJudicial ");
		query.append(" and ppe.tipoPrazo != 'S' ");
		query.append(" and ppe.fechado = false ");
		
		Query q = entityManager.createQuery(query.toString());
		q.setParameter(ProcessoParteExpediente.ATTR.PROCESSO_JUDICIAL, processoTrf);
		
		Long count = (Long) q.getSingleResult();
		
		return count == 0;
	}
	
	/**
	 * Método que retorna as partes do expediente vinculado a um documento
	 * @param idDocumento
	 * @param apenasDocumentoPrincipalExpediente
	 * @return A lista com as partes do expediente, ou nulo em caso de nenhum resultado  
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getPartesDoExpedienteBy(Integer idDocumento, boolean apenasDocumentoPrincipalExpediente){
		StringBuffer sb = new StringBuffer();		
		sb.append("select ppe from ProcessoParteExpediente ppe ");
		sb.append("where exists (select 1 from ProcessoDocumentoExpediente pde ");
		sb.append("where ppe.processoExpediente.idProcessoExpediente = pde.processoExpediente.idProcessoExpediente ");
		sb.append("and pde.processoDocumento.idProcessoDocumento = :idProcessoDocumento ");
        if (apenasDocumentoPrincipalExpediente) {
            sb.append("and pde.anexo = false ");             
        }
        sb.append(") ");

		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoDocumento", idDocumento);		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> recuperaProcessoParteExpedientePeloCodigoDePublicacaoDJEIds(String... codigos) {
		List<Integer> lista = Collections.EMPTY_LIST;
		try {
			Query q = getQueryRecuperaProcessoParteExpedientePeloCodigoDePublicacaoDJE(true, true, codigos);
			lista = q.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;

    }

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> recuperaProcessoParteExpedientePeloCodigoDePublicacaoDJEList(String... codigos) {
		List<ProcessoParteExpediente> lista = Collections.EMPTY_LIST;
		try {
			Query q = getQueryRecuperaProcessoParteExpedientePeloCodigoDePublicacaoDJE(false, true, codigos);
			lista = q.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
    }

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> recuperaProcessoParteExpedientePorMateriaPublicadaDJEList(boolean consultaPeloRecibo, String... codigos) {
		List<ProcessoParteExpediente> lista = Collections.EMPTY_LIST;
		try {
			Query q = getQueryRecuperaProcessoParteExpedientePeloCodigoDePublicacaoDJE(false, consultaPeloRecibo, codigos);
			lista = q.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
    }

	@SuppressWarnings("unchecked")
	public List<Integer> recuperaProcessoParteExpedientePorMateriaPublicadaDJEIds(boolean consultaPeloRecibo, List<Integer> codigos) {
		List<Integer> lista = Collections.EMPTY_LIST;
		try {
			Query q = getQueryRecuperaProcessoParteExpedientePeloCodigoDePublicacaoDJE(true, consultaPeloRecibo, codigos);
			lista = q.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
    }

	private Query getQueryRecuperaProcessoParteExpedientePeloCodigoDePublicacaoDJE(boolean returnJustId, boolean consultaPeloRecibo, List<Integer> codigos) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		if(returnJustId) {
			sb.append(" ppe.idProcessoParteExpediente ");
		}else {
			sb.append(" ppe ");
		}
		sb.append("  from ProcessoParteExpediente ppe ");
		if(consultaPeloRecibo) {
			sb.append("  , PublicacaoDiarioEletronico dje ");
		}
		sb.append(" where 1=1 ");
		if(consultaPeloRecibo) {
			sb.append(" AND dje.situacao != :materiaPublicada ");
			sb.append(" AND ppe.idProcessoParteExpediente = dje.processoParteExpediente.idProcessoParteExpediente ");
			sb.append(" AND dje.reciboPublicacaoDiarioEletronico in (:reciboPublicacaoDiarioEletronico) ");
		}else {
			sb.append(" AND ppe.processoExpediente.idProcessoExpediente in (:codigoMateria) ");
		}
		sb.append(" ORDER BY ppe.idProcessoParteExpediente asc");

		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		if(consultaPeloRecibo) {
			q.setParameter("reciboPublicacaoDiarioEletronico", codigos);
			q.setParameter("materiaPublicada", SituacaoPublicacaoDiarioEnum.P);
		}else {
			q.setParameter("codigoMateria", codigos);
		}

		return q;
	}

	private Query getQueryRecuperaProcessoParteExpedientePeloCodigoDePublicacaoDJE(boolean returnJustId, boolean consultaPeloRecibo, String... codigos) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		if(returnJustId) {
			sb.append(" ppe.idProcessoParteExpediente ");
		}else {
			sb.append(" ppe ");
		}
		sb.append("  from ProcessoParteExpediente ppe ");
		if(consultaPeloRecibo) {
			sb.append("  , PublicacaoDiarioEletronico dje ");
		}
		sb.append(" where 1=1 ");
		if(consultaPeloRecibo) {
			sb.append(" AND dje.situacao != :materiaPublicada ");
			sb.append(" AND ppe.idProcessoParteExpediente = dje.processoParteExpediente.idProcessoParteExpediente ");
			sb.append(" AND dje.reciboPublicacaoDiarioEletronico in (:reciboPublicacaoDiarioEletronico) ");
		}else {
			sb.append(" AND ppe.processoExpediente.idProcessoExpediente in (:codigoMateria) ");
		}
		sb.append(" ORDER BY ppe.idProcessoParteExpediente asc");

		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		if(consultaPeloRecibo) {
			q.setParameter("reciboPublicacaoDiarioEletronico", Arrays.asList(codigos));
			q.setParameter("materiaPublicada", SituacaoPublicacaoDiarioEnum.P);
		}else {
			q.setParameter("codigoMateria", CollectionUtilsPje.convertListStringToListInteger(Arrays.asList(codigos)));
		}

		return q;
	}
	
	public String recuperaQuantidadeExpediente() {
		StringBuilder sql = new StringBuilder();
		Query query = null;
		sql.append("  select count(*) from ProcessoParteExpediente o where o.processoExpediente.meioExpedicaoExpediente = 'C' and not exists (select ri.processoParteExpediente from RegistroIntimacao ri where ri.processoParteExpediente.idProcessoParteExpediente = o.idProcessoParteExpediente) and ");

		if(Authenticator.getOrgaoJulgadorColegiadoAtual()==null) {
			sql .append(" o.processoJudicial.orgaoJulgador.idOrgaoJulgador = :orgaoJulgador");
			query = EntityUtil.getEntityManager().createQuery(sql.toString());
			query.setParameter("orgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual());
		} else {
			sql .append(" o.processoJudicial.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :orgaoJulgador"); 
			query = EntityUtil.getEntityManager().createQuery(sql.toString());
			query.setParameter("orgaoJulgador", Authenticator.getIdOrgaoJulgadorColegiadoAtual());
		}	
		return query.getSingleResult() + ""; 
	}
	

	/**
	 * Consulta todos os expedientes abertos de uma determinada pessoa que faz parte de um determinado processo.
	 * 
	 * @param processoParte Parte do processo
	 * @return coleção de expedientes do processoParte.
	 * */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> recuperaExpedientesAbertosPorProcessoParte(ProcessoParte processoParte) {		
		StringBuilder query = new StringBuilder("SELECT o FROM ProcessoParteExpediente AS o ");
		query.append("WHERE o.pessoaParte = :pessoaParte ");
		query.append("AND o.processoJudicial = :processoJudicial ");
		query.append("AND o.fechado = false ");
		Query q = getEntityManager().createQuery(query.toString());		
		q.setParameter("pessoaParte", processoParte.getPessoa());
		q.setParameter("processoJudicial", processoParte.getProcessoTrf());
		return q.getResultList();
	}
	
	/**
	 * Consulta todos os expedientes abertos dos meios de comunicação informados e de um determinado processo.
	 * 
	 * @param meiosComunicacao meios de comunicação que serão filtrados na consulta
	 * @param processoTrf processo que será recuperado os expedientes abertos
	 * @return coleção de expedientes.
	 * */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> recuperaExpedientesAbertosPorMeiosComunicacaoAndProcessoTrf(List<ExpedicaoExpedienteEnum> meiosComunicacao, ProcessoTrf processoTrf) {		
		if(processoTrf != null) {
			StringBuilder query = new StringBuilder("SELECT DISTINCT ppe FROM ProcessoParteExpediente AS ppe ")
					.append("JOIN FETCH ppe.processoExpediente pe ")
					.append("JOIN FETCH pe.processoDocumentoExpedienteList pdList ")
					.append("JOIN FETCH pdList.processoDocumento pd ")
					.append("WHERE pd.dataJuntada is not NULL AND ppe.fechado = false AND ppe.pendencia is null ")
					.append("AND ppe.processoExpediente.inTemporario is false AND ppe.processoExpediente.dtExclusao is null ")
					.append("AND ppe.processoJudicial = :processoJudicial ");
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("processoJudicial", processoTrf);
			
			if(meiosComunicacao != null && meiosComunicacao.size() > 0) {
				query.append("AND pe.meioExpedicaoExpediente IN (:meiosComunicacao) ");
				params.put("meiosComunicacao", meiosComunicacao);
			}

			Query q = getEntityManager().createQuery(query.toString());
			
			for(Entry<String, Object> parametro : params.entrySet()) {
				q.setParameter(parametro.getKey(), parametro.getValue());
			}
			return q.getResultList();
		}
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Método responsável por verificar se a {@link Pessoa} possui expediente aberto no {@link ProcessoTrf}.
	 * 
	 * @param processoTrf {@link ProcessoTrf}.
	 * @param pessoa {@link Pessoa}.
	 * @return Verdadeiro se a {@link Pessoa} possui expediente aberto no {@link ProcessoTrf}. Falso, caso contrário.
	 */
	public boolean verificarExpedienteAberto(ProcessoTrf processoTrf, Pessoa pessoa) {
		StringBuilder jpql = new StringBuilder("select count(ppe) from ProcessoParteExpediente ppe ");
		jpql.append("where ppe.processoJudicial.idProcessoTrf = :idProcessoTrf ");
		jpql.append("and ppe.pessoaParte.idPessoa = :idPessoa ");
		jpql.append("and (ppe.dtCienciaParte is null or ppe.resposta is null)");
		
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf())
			.setParameter("idPessoa", pessoa.getIdPessoa());
		
		Long qtdExpedientesAbertos = (Long) query.getSingleResult();
		
		return qtdExpedientesAbertos > 0;
	}
	
	/**
	 * Recupera a quantidade de processos pendentes de manifestação.
	 * 
	 * @param idJurisdicao
	 * @param idCaixa
	 * @param idPessoa
	 * @param idProcuradoria
	 * @param tipoSituacaoExpediente
	 * @param search
	 * @return O número de processos pendentes de manifestação.
	 */
	public Long getCountProcessosJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, 
			Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoRepresentante, TipoSituacaoExpedienteEnum tipoSituacaoExpediente, Search search) {
		
		Long result = 0L;

		result = getCountProcessosJurisdicaoAdvogadoProcurador(idJurisdicao, idCaixa, idPessoa, idProcuradoria, tipoAtuacaoRepresentante, tipoSituacaoExpediente, search);

		return result;
	}
		
	/**
	 * Recupera a quantidade de processos pendentes de manifestação para os advogados e procuradores.
	 * 
	 * @param idJurisdicao
	 * @param idCaixa
	 * @param idPessoa
	 * @param idProcuradoria
	 * @param tipoSituacaoExpediente
	 * @param criteriosPesquisa
	 * @return O número de processos pendentes de manifestação para os advogados.
	 */
	private Long getCountProcessosJurisdicaoAdvogadoProcurador(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, 
			Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoRepresentante, TipoSituacaoExpedienteEnum tipoSituacaoExpediente, Search search) {
		
		Long contador = 0L;
		
		contador = (long) this.getExpedientesJurisdicao(idJurisdicao, idCaixa, idPessoa, idProcuradoria, tipoAtuacaoRepresentante, tipoSituacaoExpediente, search).size();

		return contador;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getExpedientesJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoRepresentante, TipoSituacaoExpedienteEnum tipoSituacaoExpediente, Search search){
		Collection<br.jus.pje.search.Criteria> criteriosPesquisa = (search == null ? null : search.getCriterias().values());
		Map<String, Order> orderBy = (search == null ? null : search.getOrders());
		int first = (search == null ? 0 : search.getFirst());
		int max = (search == null ? 0 : (search.getMax() == null ? 0 : search.getMax()));

		StringBuilder sql = new StringBuilder();
		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>();

		int idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual() != null ? Authenticator.getIdLocalizacaoAtual() : 0;
		boolean isProcuradoria = idProcuradoria != null ? true : false;	
		idProcuradoria = idProcuradoria == null ? 0 : idProcuradoria;
		
		sql.append("SELECT DISTINCT ptf.id_processo_trf, ptf.nr_processo, ptf.nr_sequencia, ptf.nr_ano, ptf.dt_distribuicao, ptf.in_prioridade, "); 
		sql.append("ptf.nm_pessoa_autor, ptf.qt_autor, ptf.nm_pessoa_reu, ptf.qt_reu, ptf.id_orgao_julgador_colegiado, ptf.ds_orgao_julgador_colegiado, ");
		sql.append("ptf.id_orgao_julgador, ptf.ds_orgao_julgador, ptf.id_classe_judicial, ptf.ds_classe_judicial_sigla, ptf.ds_classe_judicial, ");
		sql.append("ppe.id_processo_parte_expediente, ppe.dt_ciencia_parte, ppe.dt_prazo_legal_parte, ppe.in_fechado, ppe.nm_pessoa_ciencia, ppe.nm_pessoa_parte, ");
		sql.append("ppe.qt_prazo_legal_parte, ppe.in_tipo_prazo, pe.id_processo_expediente, pe.dt_criacao_expediente, pe.in_meio_expedicao_expediente, rgi.id, ");
		sql.append("rgi.dt_registro, rgi.in_resultado, rex.id, rex.dt_registro, tpd.id_tipo_processo_documento, tpd.ds_tipo_processo_documento, ptf.in_segredo_justica, ptf.vl_causa, ptf.dt_autuacao");

		sql.append("FROM  tb_jurisdicao jr "
				 + "JOIN tb_cabecalho_processo ptf ON (jr.id_jurisdicao = ptf.id_jurisdicao) "
				 + "JOIN tb_proc_parte_expediente ppe ON (ptf.id_processo_trf = ppe.id_processo_trf) "
				 + "JOIN tb_processo_expediente pe ON (pe.id_processo_expediente = ppe.id_processo_expediente) "
				 + "JOIN tb_tipo_processo_documento tpd ON tpd.id_tipo_processo_documento = pe.id_tipo_processo_documento ");
		if(isProcuradoria){
			sql.append("LEFT JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (ppe.id_processo_parte_expediente = cx_exp.id_processo_parte_expediente) "
					 + "LEFT JOIN tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) "
					 + "LEFT JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");			
		}
		sql.append("LEFT JOIN tb_processo_parte pp ON (pp.id_pessoa = ppe.id_pessoa_parte AND pp.id_processo_trf = ppe.id_processo_trf) "
				 + "LEFT JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte "
				 + "                                                    AND ppr.in_situacao = 'A') "
				 + "LEFT JOIN tb_resposta_expediente rex ON rex.id = ppe.id_resposta "
				 + "LEFT JOIN "
				 + "( "
				 + "   SELECT rgi.id, rgi.dt_registro, rgi.nr_aviso_recebimento, rgi.in_resultado, rgi.id_processo_parte_expediente "
				 + "   FROM tb_registro_intimacao rgi "
				 + "   WHERE rgi.id = (SELECT MIN(id) FROM tb_registro_intimacao WHERE id_processo_parte_expediente = rgi.id_processo_parte_expediente) "
				 + ") AS rgi ON rgi.id_processo_parte_expediente = ppe.id_processo_parte_expediente "
				 + "WHERE ptf.cd_processo_status = 'D' ");
		sql.append("AND ptf.id_jurisdicao = :idJurisdicao ");

		sql.append(limitarRepresentacao(isProcuradoria, tipoAtuacaoRepresentante, idLocalizacaoAtual));
		sql.append(limitarVisibilidade(isProcuradoria));
		sql.append(limitarProcessosEmCaixa(idCaixa));
		sql.append(limitarExpedientesPorSituacao(tipoSituacaoExpediente));
		sql.append(limitarCriteriosPesquisa(criteriosPesquisa, parametrosPesquisa));
		sql.append(limitarOrdenacao(orderBy));
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		q.setFirstResult(first);
		if(max > 0){
			q.setMaxResults(max);
		}
		
		for(String key: parametrosPesquisa.keySet()){
			q.setParameter(key, parametrosPesquisa.get(key));
		}
		
		q.setParameter("idJurisdicao", idJurisdicao);

		q.setParameter("idProcuradoria", idProcuradoria);

		if(idCaixa != null) {
			q.setParameter("idCaixa", idCaixa);
		}
		
		if(idPessoa != null) {
			q.setParameter("idPessoa", idPessoa);
		}
		
		
		List<Object[]> resultList = q.getResultList();
		return new ProcessoParteExpedienteTransform().transformCollection(resultList);
	}

	/**
	 * Verifica se o parâmetro 'objeto' é objeto de um ENUM.
	 * 
	 * @param objeto
	 * @return isEnum
	 *
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18526
	 */
	private boolean isEnum(Object objeto) {
		return objeto != null && objeto.getClass().isEnum();
	}
	
	private String limitarCriteriosPesquisa(Collection<br.jus.pje.search.Criteria> criteriosPesquisa, Map<String, Object> parametrosPesquisa) {
		if(criteriosPesquisa != null && criteriosPesquisa.size() > 0) {
			StringBuilder sbPesquisa = new StringBuilder();
			return loadNativeCriterias(sbPesquisa, criteriosPesquisa, parametrosPesquisa);
		}
		return "";
	}

	private String loadNativeCriterias(StringBuilder sb, Collection<br.jus.pje.search.Criteria> criterias, Map<String, Object> params) {
		String str = null;
		if(criterias != null && criterias.size() > 0) {
			super.loadCriterias(sb, criterias, params);
			translateToNativeParameters(params);
			str = translateToNative(sb.toString());
		}
		return str;
	}
	
	private String translateToNative(String str) {
		Map<String, String> columns = new LinkedHashMap<String, String>();
		columns.put("o.processoJudicial.dataDistribuicao", "ptf.dt_distribuicao");
		columns.put("o.processoJudicial.orgaoJulgador.idOrgaoJulgador", "ptf.id_orgao_julgador");
		columns.put("o.processoJudicial.classeJudicial.classeJudicialSigla", "ptf.ds_classe_judicial_sigla");
		columns.put("o.processoJudicial.classeJudicial.classeJudicial", "ptf.ds_classe_judicial");
		columns.put("o.processoJudicial.classeJudicial.codClasseJudicial", "ptf.cd_classe_judicial");
		columns.put("o.processoJudicial.numeroSequencia", "ptf.nr_sequencia");
		columns.put("o.processoJudicial.numeroDigitoVerificador", "ptf.nr_digito_verificador");
		columns.put("o.processoJudicial.ano", "ptf.nr_ano");
		columns.put("o.processoJudicial.numeroOrgaoJustica", "ptf.nr_identificacao_orgao_justica");
		columns.put("o.processoJudicial.numeroOrigem", "ptf.nr_origem_processo");
		columns.put("o.nomePessoaParte", "ppe.nm_pessoa_parte");
		columns.put("o.processoExpediente.dtCriacao", "pe.dt_criacao_expediente");
		columns.put("o.processoExpediente.meioExpedicaoExpediente", "pe.in_meio_expedicao_expediente");
		columns.put("o.processoExpediente.tipoProcessoDocumento.idTipoProcessoDocumento", "tpd.id_tipo_processo_documento");
		columns.put("o.pessoaParte", "ppe.id_pessoa_parte");
		columns.put("o.processoJudicial", "pe.id_processo_trf");
		columns.put("o.idProcessoParteExpediente", "ppe.id_processo_parte_expediente");

		
		//** colunas que necessitam de queries específicas: não possuem mapeamento direto.
		columns.put(".idPrioridadeProcesso","");
		columns.put(".assuntoTrf", "");
		columns.put(".numeroDocumento","");
		columns.put(".pessoa.nome","");
		
		for(String key: columns.keySet()){
	        if(str.contains(key)) {
	        	if(key.equals(".idPrioridadeProcesso")) {
	        		str = replaceCriteriaPrioridadeProcesso(str);
	        	}
	        	if(key.equals(".assuntoTrf")) {
	        		str = replaceCriteriaAssunto(str);
	        	}
	        	else if(key.equals(".pessoa.nome")) {
	        		str = replaceCriteriaParte(str);
	        	}
	        	else if(key.equals(".numeroDocumento")) {
	        		str = replaceCriteriaNumeroDocumento(str);
	        	}
	        	else {
	        		str = str.replaceAll(key, columns.get(key).toString());
	        	}
	        }
		}

	    return " AND " + str + " ";
	}


	/**
	 * Converte parâmetros(parametrosPesquisa) de consulta para que eles possam ser lidos em uma query nativa.
	 * 
	 * @param parametrosPesquisa
	 *	
     * @link http://www.cnj.jus.br/jira/browse/PJEII-18526
	 */
	private void translateToNativeParameters(Map<String, Object> parametrosPesquisa) {
		for (String key : parametrosPesquisa.keySet()) {
			Object parametro = parametrosPesquisa.get(key);
			if (isEnum(parametro)) {
				parametrosPesquisa.put(key, parametro.toString());
			}
		}
	}
	
	private String replaceCriteriaPrioridadeProcesso(String str) {
		String key = "p0.idPrioridadeProcesso";
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
		String key = "p0.assuntoTrf.codAssuntoTrf";
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
		sql.append("LEFT JOIN tb_pess_doc_identificacao pdi ON pdi.id_pessoa = ul.id_usuario AND pdi.in_ativo = true AND pdi.in_usado_falsamente = false ");
		sql.append("WHERE pp.in_situacao = 'A' ");
		sql.append("AND pp.id_processo_trf = ptf.id_processo_trf ");
		sql.append("AND (LOWER(to_ascii(ul.ds_nome)) LIKE LOWER(to_ascii(" + param1 + ")) OR LOWER(to_ascii(pdi.ds_nome_pessoa)) LIKE LOWER(to_ascii(" + param2 + "))) ) ");

		return str.replace(content, sql.toString());
	}

	private String replaceCriteriaNumeroDocumento(String str) {
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
		sql.append("AND pdi.cd_tp_documento_identificacao IN ('CPF','CPJ', 'OAB') ");
		sql.append("AND pdi.nr_documento_identificacao = " + param + " ) ");

		return str.replace(content, sql.toString());
	}

	private String getCriteriaSubstring(String str, String key) {
		String conector ="AND ";
		
		int posKey = str.indexOf(key);
		int posInitConector = str.substring(0, posKey).lastIndexOf(conector);
		int posLastConector = str.substring(posKey).indexOf(conector);
		
		posInitConector = (posInitConector > -1 ? posInitConector : 0);
		posLastConector = (posLastConector > -1 ? posLastConector : 0);
		
		int begin = (posInitConector > 0 ? posInitConector + conector.length() : 0);
		int end = (posLastConector > 0 ? posLastConector + posKey : str.length());
		
		return str.substring(begin, end);
	}

	private String limitarOrdenacao(Map<String, Order> orderBy) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("ORDER BY ");
		
		if(orderBy != null && orderBy.size() >0) {
			StringBuilder order = new StringBuilder();
			for(String key: orderBy.keySet()){
		        if(order.length() > 0) {
		        	order.append(", ");
		        }
		        order.append(key + " " + orderBy.get(key).toString());
			}
		    sql.append(order.toString());
		}
		else {
			sql.append("ppe.dt_prazo_legal_parte ASC");
		}
		return sql.toString();
	}

	public String limitarRepresentacao(boolean isProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, int idLocalizacaoAtual) {
		StringBuilder sql = new StringBuilder();
		
		if (isProcuradoria){
			sql.append("AND	( "
					 + "	  ppe.id_procuradoria = :idProcuradoria AND ppe.in_intima_pessoal = false "
					 + "      AND ( '"+atuacaoProcurador+"' = '"+RepresentanteProcessualTipoAtuacaoEnum.G+"' "
					 + "           OR ((cx_rep.id_pessoa_fisica = :idPessoa) "
					 + "                AND NOT EXISTS (SELECT 1 FROM  tb_periodo_inativ_caixa_rep cx_in "
					 + "                                WHERE  cx_in.id_caixa_adv_proc = cx.id_caixa_adv_proc "
					 + "                                AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim "
					 + "                                GROUP BY cx_in.id_caixa_adv_proc)) "
					 + "                OR (EXISTS (SELECT 1 FROM tb_pess_proc_jurisdicao ppj "
					 + "                            JOIN tb_pessoa_procuradoria pp ON (pp.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) "
					 + "                            WHERE ppj.in_ativo = true "
					 + "                            AND ppj.id_jurisdicao = ptf.id_jurisdicao "
					 + "                            AND pp.id_pessoa = :idPessoa "
					 + "                            AND pp.id_procuradoria = :idProcuradoria "
					 + "                            GROUP BY ppj.id_jurisdicao) "
					 + "					   ) "
					 + "				      ) "
					 + "		) ");	
		}else{
			sql.append("AND	( "
					 + "       (ppe.id_pessoa_parte = :idPessoa "
					 + "                      OR (ppe.in_intima_pessoal = false "
					 + "                          AND (ppr.id_representante = :idPessoa "
					 + "                               OR (EXISTS ( "
					 + "                                   SELECT 1 FROM core.tb_usuario_localizacao ul "
					 + "                                   JOIN tb_pessoa_localizacao pl ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) "
					 + "                                   WHERE ppr.id_representante=pl.id_pessoa "
					 + "                                   AND ul.id_usuario = :idPessoa "
					 + "                                   AND ul.id_localizacao_fisica="+idLocalizacaoAtual+")) "
					 + "						) "
					 + "					) "
					 + "				) "
					 + "		) ");			
		}
		
		return sql.toString();
	}
	
	public String limitarVisibilidade(boolean isProcuradoria){
		StringBuilder sb = new StringBuilder();
		//busca dados de visibilidade relacionada
		sb.append( "AND ( "
				 + "	 ptf.in_segredo_justica = false "
				 + "      OR (EXISTS (SELECT 1 FROM tb_proc_visibilida_segredo vis "
				 + "                  WHERE (vis.id_pessoa = :idPessoa)					"
				 + "                  OR ("+isProcuradoria+" = true "
				 + "                       AND (vis.id_pessoa = ppe.id_pessoa_parte "
				 + "                       AND ppe.id_procuradoria = :idProcuradoria)"
				 + "                      ) "
				 + "                  AND vis.id_processo_trf = ptf.id_processo_trf) "
				 + "                ) "
				 + "	) "); 
		
		return sb.toString();
	}


	private String limitarProcessosEmCaixa(Integer idCaixa) {
		StringBuilder sql = new StringBuilder();
		if(idCaixa == null) {
			sql.append("AND NOT EXISTS ");
			sql.append("( ");
			sql.append("SELECT 1 ");
			sql.append("FROM tb_proc_parte_exp_caixa_adv_proc cxppe ");
			sql.append("INNER JOIN tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cxppe.id_caixa_adv_proc) ");
			sql.append("INNER JOIN tb_usuario_localizacao ul ON (ul.id_localizacao_fisica = cx.id_localizacao) ");
			sql.append("WHERE cxppe.id_processo_parte_expediente = ppe.id_processo_parte_expediente ");
			sql.append("AND ul.id_usuario = :idPessoa ");
			sql.append(") ");
		}
		else {
			sql.append("AND EXISTS ");
			sql.append("( ");
			sql.append("   SELECT 1 ");
			sql.append("   FROM tb_proc_parte_exp_caixa_adv_proc cxppe ");
			sql.append("   WHERE cxppe.id_processo_parte_expediente = ppe.id_processo_parte_expediente ");
			sql.append("   AND cxppe.id_caixa_adv_proc = :idCaixa ");
			sql.append(") ");
		}
		return sql.toString();
	}

	public String limitarExpedientesPorSituacao(TipoSituacaoExpedienteEnum tipoSituacaoExpediente){
		StringBuilder sb = new StringBuilder();
		String dataLimite = DateUtil.dateToString(DateUtil.getBeginningOfDay(DateUtil.dataMenosDias(new Date(), 10)), "yyyy-MM-dd");

		if(tipoSituacaoExpediente == null){
			tipoSituacaoExpediente = TipoSituacaoExpedienteEnum.PENDENTES_CIENCIA_RESPOSTA;
		}
		
		switch (tipoSituacaoExpediente) {			
			case PENDENTES_CIENCIA: //Pendentes de ciência - ainda abertos, sem registro de ciencia - sem prazo ou cujo prazo legal ainda não passou
				sb.append(" AND ppe.in_fechado = false ")
					.append(" AND ppe.dt_ciencia_parte IS NULL ")
					.append(" AND (ppe.in_tipo_prazo = 'S' ")
					.append(" OR ppe.dt_prazo_legal_parte >= CURRENT_TIMESTAMP ) ");
				break;
			
			case CIENCIA_DESTINATARIO: // Ciência dada pelo destinatário direto ou indireto e dentro do prazo
				sb.append("AND ppe.in_fechado = false ");
				sb.append("AND ppe.dt_prazo_legal_parte >= CURRENT_TIMESTAMP ");
				sb.append("AND ppe.dt_ciencia_parte IS NOT NULL ");
				sb.append("AND ppe.in_ciencia_sistema = false ");
				break;
			
			case CIENCIA_JUDICIARIO: // Ciência dada pelo Judiciário e dentro do prazo
				sb.append("AND ppe.in_fechado = false ");
				sb.append("AND ppe.dt_prazo_legal_parte >= CURRENT_TIMESTAMP ");
				sb.append("AND ppe.dt_ciencia_parte IS NOT NULL ");
				sb.append("AND ppe.in_ciencia_sistema = true ");
				break;
			
			case PRAZO: // Cujos prazos expiraram sem resposta nos últimos 10 dias.
				sb.append("AND ppe.in_fechado = true ");
				sb.append("AND ppe.in_tipo_prazo != 'S' ");
				sb.append("AND ppe.id_resposta IS NULL ");
				sb.append("AND ppe.dt_prazo_legal_parte BETWEEN '" + dataLimite + "' AND CURRENT_TIMESTAMP ");
				break;
			
			case SEM_PRAZO: // Sem prazo
				sb.append("AND ppe.in_fechado = false ");
				sb.append("AND ppe.in_tipo_prazo = 'S' ");
				sb.append("AND ppe.dt_ciencia_parte is not null ");
				break;
			
			case RESPONDIDOS: // Respondidos nos últimos 10 dias
				sb.append("AND ppe.in_fechado = true ");
				sb.append("AND ppe.id_resposta is not null ");
				sb.append("AND EXISTS ( ");
				sb.append("SELECT 1 FROM tb_resposta_expediente re ");
				sb.append("WHERE re.id = ppe.id_resposta ");
				sb.append("AND cast(re.dt_registro as date) BETWEEN '" + dataLimite + "' AND CURRENT_DATE) ");
				break;
			
			case PENDENTES_CIENCIA_RESPOSTA: // Pendentes de ciência e resposta (Pendentes de manifestação)
			default: // Pendentes de ciência e resposta (Pendentes de manifestação)
				sb.append(" AND ppe.in_fechado = false ")
					.append(" AND (ppe.dt_ciencia_parte IS NULL ")
					.append(" OR ppe.id_resposta IS NULL) ")
					.append(" AND (ppe.in_tipo_prazo = 'S' ")
					.append(" OR ppe.dt_prazo_legal_parte >= CURRENT_TIMESTAMP ) ");
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> getParteExpedienteFromProcesso(ProcessoTrf processo) {
		List<ProcessoParteExpediente> expedientes = new ArrayList<ProcessoParteExpediente>();
		if(processo != null){
			StringBuilder query = new StringBuilder
				("SELECT ppe " + 
			     "FROM ProcessoParteExpediente AS ppe "
			        + "inner join fetch ppe.processoJudicial AS pt " +
				 "WHERE pt.idProcessoTrf = :idProcesso ");
			Query q = entityManager.createQuery(query.toString());
			q.setParameter("idProcesso", processo.getIdProcessoTrf());
			return q.getResultList();
		}
		return expedientes;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> recuperaExpedientesNaoFechados(ProcessoTrf processo){
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe WHERE ppe.fechado = false AND "
				+ "ppe.processoJudicial = :processoJudicial");

		Query q = entityManager.createQuery(query.toString());
		q.setParameter(ProcessoParteExpediente.ATTR.PROCESSO_JUDICIAL, processo);

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer,BigInteger> getContadoresPorJurisdicao(Integer idPessoa,Integer idProcuradoria,
			RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, TipoSituacaoExpedienteEnum tipoSituacaoExpediente){

		StringBuilder sql = new StringBuilder();
		int idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual() != null ? Authenticator.getIdLocalizacaoAtual() : 0;
		boolean isProcuradoria = idProcuradoria != null ? true : false;
		idProcuradoria = idProcuradoria == null ? 0 : idProcuradoria;

		sql.append("SELECT jr.id_jurisdicao, count(DISTINCT ppe.id_processo_parte_expediente) AS bigint "); 
		sql.append("FROM  tb_jurisdicao jr "
				 + "JOIN tb_processo_trf ptf ON (jr.id_jurisdicao = ptf.id_jurisdicao) "
				 + "JOIN tb_proc_parte_expediente ppe ON (ptf.id_processo_trf = ppe.id_processo_trf) "
				 + "JOIN tb_processo_expediente pe ON (pe.id_processo_expediente = ppe.id_processo_expediente) "
				 + "LEFT JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (ppe.id_processo_parte_expediente = cx_exp.id_processo_parte_expediente) "
				 + "LEFT JOIN tb_caixa_adv_proc cx ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) "
				 + "LEFT JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) "
				 + "LEFT JOIN tb_processo_parte pp ON (pp.id_pessoa = ppe.id_pessoa_parte AND pp.id_processo_trf = ppe.id_processo_trf) "
				 + "LEFT JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte "
				 + "                                                    AND ppr.in_situacao = 'A') "
				 + "WHERE ptf.cd_processo_status = 'D' ");

		sql.append(limitarRepresentacao(isProcuradoria, atuacaoProcurador, idLocalizacaoAtual));
		sql.append(limitarVisibilidade(isProcuradoria));
		sql.append(limitarExpedientesPorSituacao(tipoSituacaoExpediente));
		sql.append("GROUP BY jr.id_jurisdicao");
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		
		q.setParameter("idProcuradoria", idProcuradoria);	

		if (idPessoa != null) {
			q.setParameter("idPessoa", idPessoa);	
		}	
		
		Map<Integer,BigInteger> result = new HashMap<Integer,BigInteger>(0);
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes: resultList) {
			result.put((Integer)borderTypes[0], (BigInteger)borderTypes[1]);
		}
		return result;
	}
	
	public String obterQueryConsolidadaIdsExpedientes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisa) {
		
		List<Map<String, String>> consultasExpedientes = this.obterQueryExpedientes(idPessoa, idLocalizacao, tipoUsuarioExterno, 
				idProcuradoria, isProcuradorGestor, criteriosPesquisa);

		Map<String, String> restricoesExpedientes = this.obterQueryRestricoesExpedientes(idPessoa, idLocalizacao, idProcuradoria, criteriosPesquisa);
		
		return consolidarConsultaExpedientesIds(consultasExpedientes, restricoesExpedientes);		
	}
	public List<Map<String, String>> obterQueryExpedientes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisa){
		
		List<Map<String, String>> consultas = new ArrayList<Map<String, String>>(0);
		
		if (idProcuradoria != null && idProcuradoria != 0) {
			if (isProcuradorGestor) {
				consultas.add(this.obterQueryExpedientesProcuradorGestor(idProcuradoria));
			}else{
				// Usuário logado é procurador padrão ou distribuidor.
				// A query deve ser a união da consulta de procuradores padrão (caixa) com procuradores distribuidores (jurisdição).
				consultas.add(this.obterQueryExpedientesProcuradorCaixa(idPessoa, idLocalizacao, idProcuradoria));
				consultas.add(this.obterQueryExpedientesProcuradorJurisdicao(idPessoa, idProcuradoria));
			}
		} else {
			// Se não for de procuradoria, deve retornar sempre os expedientes do destinatário.
			consultas.add(this.obterQueryExpedientesDestinatario(idPessoa));
			// Se a pessoa atual é advogado ou assistente de advogado - deve-se juntar a consulta do perfil com a de parte
			if (TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno) || TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExterno)) {
				consultas.add(TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno) ?
					this.obterQueryExpedientesRepresentanteDestinatario(idPessoa) : 
					this.obterQueryExpedientesAssistenteRepresentanteDestinatario(idPessoa, idLocalizacao));
			}
		}
		return consultas;
	}
	
	/**
	 * 
	 * 
	 * @param idPessoa
	 * @param idProcuradoria
	 * @return
	 */
	private String obterQueryRestricoesVisibilidade(Integer idPessoa, Integer idProcuradoria) {
		StringBuilder strQuery = new StringBuilder(" AND (ptf.in_segredo_justica = false OR EXISTS (")
			.append(" SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_processo_trf = ptf.id_processo_trf ")
			.append(" AND (vis.id_pessoa = " + idPessoa);
		
		if (idProcuradoria != null) {
			strQuery.append(" OR vis.id_procuradoria = " + idProcuradoria + " ");
		}
		strQuery.append("))) ");

		return strQuery.toString();
	}
	
	private String obterQueryAdminJurisdicao(Boolean isAdminJurisdicao) {
		Integer valorIsAdmin = 0;
		if(isAdminJurisdicao) {
			valorIsAdmin = 1;
		}
		return "INNER JOIN (SELECT "+valorIsAdmin+" in_admin_jurisdicao) as administracao_jurisdicao ON (1=1) ";
	}
	
	/**
	 * 
	 * @param idProcuradoria
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryExpedientesProcuradorGestor(Integer idProcuradoria) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_proc_parte_expediente ppe ")
			.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.cd_processo_status = 'D') ")	
			.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
			.append(this.obterQueryAdminJurisdicao(true));
		
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ppe.in_intima_pessoal = false AND ppe.id_procuradoria = " + idProcuradoria + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param idProcuradoria
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryExpedientesProcuradorCaixa(Integer idPessoa, Integer idLocalizacao, Integer idProcuradoria) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_caixa_adv_proc cx ")
			// Tabelas relacionadas ao procurador da caixa.
			.append("INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
			.append("INNER JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) ")
			// Tabelas para retornar resultado.
			.append("INNER JOIN tb_proc_parte_expediente ppe ON (ppe.id_processo_parte_expediente = cx_exp.id_processo_parte_expediente) ")				
			.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.cd_processo_status = 'D') ")
			.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")	
			.append(this.obterQueryAdminJurisdicao(false));
	
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ppe.in_intima_pessoal = false AND ppe.id_procuradoria = " + idProcuradoria + " ")
				// Restricoes relacionadas ao procurador da caixa.
			.append(" AND (cx_rep.id_pessoa_fisica = "+idPessoa+") ")
			.append(" AND (cx.id_localizacao = "+idLocalizacao+") ")
				// Retira as caixas que estiverem com o periodo de inatividade no momento atual
			.append(" AND NOT EXISTS (")
			.append(" SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in WHERE cx_in.id_caixa_adv_proc = cx.id_caixa_adv_proc ")
			.append(" AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim) ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param idProcuradoria
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryExpedientesProcuradorJurisdicao(Integer idPessoa, Integer idProcuradoria) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_proc_parte_expediente ppe ")
			// Tabelas relacionadas ao procurador vinculado as jurisdições
			.append(" INNER JOIN tb_pessoa_procuradoria pproc0 ON (pproc0.id_pessoa = "+ idPessoa +") ")
			.append(" INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_procuradoria = ppe.id_procuradoria AND pproc.id_pessoa_procuradoria = pproc0.id_pessoa_procuradoria) ")
			.append(" INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.cd_processo_status = 'D') ")
			.append(" INNER JOIN tb_pess_proc_jurisdicao ppj ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria AND ptf.id_jurisdicao = ppj.id_jurisdicao AND ppj.in_ativo is true) ")
			// Tabelas básicas de retorno de informação
			.append(" INNER JOIN tb_jurisdicao jur ON (ptf.id_jurisdicao = jur.id_jurisdicao) ")
			.append(this.obterQueryAdminJurisdicao(true));
					
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ppe.in_intima_pessoal = false AND ppe.id_procuradoria = " + idProcuradoria + " ")
				// Restrições relacionadas ao procurador da jurisdição.
			.append("AND pproc.id_pessoa = " + idPessoa + " ");
		
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryExpedientesDestinatario(Integer idPessoa) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_proc_parte_expediente ppe ")
			.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.cd_processo_status = 'D') ")
			.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
			.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ppe.id_pessoa_parte = " + idPessoa + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}

	/**
	 * 
	 * @param idPessoa
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryExpedientesRepresentanteDestinatario(Integer idPessoa) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_proc_parte_expediente ppe ")
			// Tabelas que identificam os destinatários e os representantes desses destinatários no processo.
			.append("INNER JOIN tb_processo_parte pp ON (pp.id_pessoa = ppe.id_pessoa_parte AND pp.id_processo_trf = ppe.id_processo_trf AND pp.in_situacao = 'A') ")
			.append("INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = " + Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)) + " AND ppr.in_situacao = 'A') ")
			// Tabelas que retornam resultado da consulta.
			.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.cd_processo_status = 'D') ")
			.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")	
			.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ppe.in_intima_pessoal = false  ")
			.append(" AND ppr.id_representante = " + idPessoa + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
			
		return queryPesquisa;
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryExpedientesAssistenteRepresentanteDestinatario(Integer idPessoa, Integer idLocalizacao) {
		StringBuilder strQueryFrom = new StringBuilder("FROM core.tb_usuario_localizacao ul ")
			// Tabelas necessárias para identificação da localização do usuário assistente.
			.append("INNER JOIN tb_pessoa_localizacao pl ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) ")
			// Tabelas que identificam os destinatários e os representantes desses destinatários no processo.
			.append("INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_representante=pl.id_pessoa AND ppr.id_tipo_representante = " 
					+ Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)) + " AND ppr.in_situacao = 'A') ")
			.append("INNER JOIN tb_processo_parte pp ON (ppr.id_processo_parte = pp.id_processo_parte AND pp.in_situacao = 'A') ")	
			// Tabelas que retornam o resultado.
			.append("INNER JOIN tb_proc_parte_expediente ppe ON (pp.id_pessoa = ppe.id_pessoa_parte  AND pp.id_processo_trf = ppe.id_processo_trf) ")
			.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.cd_processo_status = 'D') ")
			.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
			.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ppe.in_intima_pessoal = false ")
			.append(" AND ul.id_usuario = "+idPessoa+" ")
			.append(" AND ul.id_localizacao_fisica = "+idLocalizacao+" ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
			
		return queryPesquisa;
	}
	
	/**
	 * 
	 * @param idLocalizacao
	 * @param criteriosPesquisa
	 * @return
	 */
	private String obterQueryFromFiltrosExpedientes(Integer idLocalizacao, PesquisaExpedientesVO criteriosPesquisa) {
		StringBuilder strQuery = new StringBuilder();
		
		if(criteriosPesquisa.getIdCaixaAdvProc() != null || criteriosPesquisa.getApenasCaixasAtivas()) {
			strQuery.append(this.obterQueryFromFiltroCaixas(idLocalizacao));
		}else {
			if(criteriosPesquisa.getApenasSemCaixa()) {
				strQuery.append(this.obterQueryFromFiltroSemCaixa(idLocalizacao));
			}
		}
		
		if((criteriosPesquisa.getAssuntoJudicialObj() != null && criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto() > 0) || (criteriosPesquisa.getAssuntoJudicial() != null)) {
			strQuery.append(this.obterQueryFromAssuntos());
		}
		
		if(criteriosPesquisa.getPrioridadeObj() != null && criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso() > 0) {
			strQuery.append(this.obterQueryFromPrioridade());
		}
		
		if(criteriosPesquisa.getDataAutuacaoInicial() != null || criteriosPesquisa.getDataAutuacaoFinal() != null) {
			strQuery.append(this.obterQueryFromDataAutuacao());
		}
		
		if(criteriosPesquisa.getNomeDestinatario() != null) {
			strQuery.append(this.obterQueryFromDestinatario());
		}
		
		if(criteriosPesquisa.getCpfDestinatario() != null || criteriosPesquisa.getCnpjDestinatario() != null || criteriosPesquisa.getOutroDocumentoDestinatario() != null) {
			strQuery.append(this.obterQueryFromDocumentosIdentificacao(criteriosPesquisa));
		}
		
		if(criteriosPesquisa.getOabRepresentanteDestinatario() != null) {
			strQuery.append(this.obterQueryFromOabRepresentante());
		}
		
		return strQuery.toString();
	}

	private String obterQueryFromOabRepresentante() {
		StringBuilder strQuery = new StringBuilder();

		strQuery.append(" INNER JOIN client.tb_processo_parte fpp ON (fpp.in_situacao in ('A') AND fpp.in_parte_principal = true AND fpp.id_processo_trf = ptf.id_processo_trf) ")
			.append(" INNER JOIN client.tb_proc_parte_represntante fppr ON (fppr.in_situacao in ('A') AND fppr.id_processo_parte = fpp.id_processo_parte) ");
		
		strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao oabRepresentante ON ( ")
			.append(" oabRepresentante.in_ativo = true ")
			.append(" AND oabRepresentante.in_usado_falsamente = false ")
			.append(" AND oabRepresentante.cd_tp_documento_identificacao = 'OAB' ")
			.append(" AND oabRepresentante.id_pessoa = fppr.id_representante) ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromDocumentosIdentificacao(PesquisaExpedientesVO criteriosPesquisa) {
		StringBuilder strQuery = new StringBuilder();
		
		if(criteriosPesquisa.getCpfDestinatario() != null) {
			strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao cpf ON ( ")
				.append(" cpf.in_ativo = true ")
				.append(" AND cpf.in_usado_falsamente = false ")
				.append(" AND cpf.cd_tp_documento_identificacao = 'CPF' ")
				.append(" AND cpf.id_pessoa = ppe.id_pessoa_parte) ");
		}

		if(criteriosPesquisa.getCnpjDestinatario() != null) {
			strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao cnpj ON ( ")
				.append(" cnpj.in_ativo = true ")
				.append(" AND cnpj.in_usado_falsamente = false ")
				.append(" AND cnpj.cd_tp_documento_identificacao = 'CNPJ' ")
				.append(" AND cnpj.id_pessoa = ppe.id_pessoa_parte) ");
		}
		
		if(criteriosPesquisa.getOutroDocumentoDestinatario() != null) {
			strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao outroDocIdentificacao ON ( ")
				.append(" outroDocIdentificacao.in_ativo = true ")
				.append(" AND outroDocIdentificacao.in_usado_falsamente = false ")
				.append(" AND outroDocIdentificacao.cd_tp_documento_identificacao not in ('CPF', 'CNPJ') ")
				.append(" AND outroDocIdentificacao.id_pessoa = ppe.id_pessoa_parte) ");
		}

		return strQuery.toString();
	}
	
	private String obterQueryFromDestinatario() {
		StringBuilder strQuery = new StringBuilder();
		
		strQuery.append(" INNER JOIN acl.tb_usuario_login destinatario ON (destinatario.id_usuario = ppe.id_pessoa_parte) ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromDataAutuacao() {
		StringBuilder strQuery = new StringBuilder();
		
		strQuery.append(" INNER JOIN client.tb_processo_expediente fpe ON (fpe.id_processo_expediente = ppe.id_processo_expediente) ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromPrioridade() {
		StringBuilder strQuery = new StringBuilder();
		
		strQuery.append(" INNER JOIN client.tb_proc_prioridde_processo prioridade ON (prioridade.id_processo_trf = ptf.id_processo_trf) ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromAssuntos() {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" INNER JOIN client.tb_processo_assunto pass ON (pass.id_processo_trf = ptf.id_processo_trf) ")
			.append(" INNER JOIN client.tb_assunto_trf assunto ON (assunto.id_assunto_trf = pass.id_assunto_trf AND assunto.in_ativo = true) ");

		return strQuery.toString();
	}
	
	private String obterQueryFromFiltroCaixas(Integer idLocalizacao) {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" INNER JOIN tb_proc_parte_exp_caixa_adv_proc fcaixa_exp ON (ppe.id_processo_parte_expediente = fcaixa_exp.id_processo_parte_expediente) ")
			.append("INNER JOIN tb_caixa_adv_proc fcaixa ON (fcaixa.id_caixa_adv_proc = fcaixa_exp.id_caixa_adv_proc AND fcaixa.id_localizacao = "+idLocalizacao+") ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromFiltroSemCaixa(Integer idLocalizacao) {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" LEFT JOIN (SELECT caixa.id_caixa_adv_proc, fcaixa_exp.id_processo_parte_expediente FROM tb_proc_parte_exp_caixa_adv_proc fcaixa_exp ")
			.append(" INNER JOIN tb_caixa_adv_proc caixa ON (caixa.id_caixa_adv_proc = fcaixa_exp.id_caixa_adv_proc AND caixa.id_localizacao = "+idLocalizacao + ")")
			.append(" ) fcaixa ON (fcaixa.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		
		return strQuery.toString();
	}
	
	/**
	 * Contempla apenas campos existentes nas consultas básicas por expedientes:
	 * - jurisdição
	 * - dados da tabela: tb_cabecalho_processo
	 * 
	 * @param criteriosPesquisa
	 * @return
	 */
	private String obterFiltroExpedientes(PesquisaExpedientesVO criteriosPesquisa) {
		StringBuilder strQuery = new StringBuilder();
		
		if(criteriosPesquisa.getIdProcessoParteExpediente() != null) {
			strQuery.append(" AND ppe.id_processo_parte_expediente = "+criteriosPesquisa.getIdProcessoParteExpediente());
		}
		
		// busca pela jurisdicao
		if(criteriosPesquisa.getIdJurisdicao() != null) {
			strQuery.append(" AND jur.id_jurisdicao = "+criteriosPesquisa.getIdJurisdicao());
		}
		
		// busca pela caixa
		if(criteriosPesquisa.getIdCaixaAdvProc() != null) {
			strQuery.append(" AND fcaixa.id_caixa_adv_proc = " + criteriosPesquisa.getIdCaixaAdvProc());
		}else {
			if(criteriosPesquisa.getApenasSemCaixa()) {
				strQuery.append(" AND fcaixa.id_caixa_adv_proc IS NULL ");
			}else {
				// Elimina as caixas com período de inatividade atual
				if(criteriosPesquisa.getApenasCaixasAtivas()) {
					strQuery.append(" AND NOT EXISTS (")
						.append(" SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in WHERE cx_in.id_caixa_adv_proc = fcaixa.id_caixa_adv_proc ")
						.append(" AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim) ");			
				}
			}
		}
		
		// busca por um número do processo dado
		if(criteriosPesquisa.getNumeroProcesso() != null) {
			strQuery.append(" AND ptf.nr_processo = '"+ criteriosPesquisa.getNumeroProcesso() +"'");
		}else {
			if(criteriosPesquisa.getNumeroSequencia() != null) {
				strQuery.append(" AND ptf.nr_sequencia = "+criteriosPesquisa.getNumeroSequencia());
			}
			if(criteriosPesquisa.getDigitoVerificador() != null) {
				strQuery.append(" AND ptf.nr_digito_verificador = "+criteriosPesquisa.getDigitoVerificador());
			}
			if(criteriosPesquisa.getNumeroAno() != null) {
				strQuery.append(" AND ptf.nr_ano = "+criteriosPesquisa.getNumeroAno());
			}
			if(criteriosPesquisa.getNumeroOrgaoJustica() != null) {
				strQuery.append(" AND ptf.nr_identificacao_orgao_justica = "+criteriosPesquisa.getNumeroOrgaoJustica());
			}
			if(criteriosPesquisa.getNumeroOrigem() != null) {
				strQuery.append(" AND ptf.nr_origem_processo = "+criteriosPesquisa.getNumeroOrigem());
			}
		}
		
		// busca pelo assunto
		if(criteriosPesquisa.getAssuntoJudicialObj() != null && criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto() > 0) {
			strQuery.append(" AND assunto.id_processo_trf = " + criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto());
		}else {
			if(criteriosPesquisa.getAssuntoJudicial() != null) {
				strQuery
					.append(" AND ( ")
						.append("assunto.cd_assunto_trf = '" + criteriosPesquisa.getAssuntoJudicial() + "' ")
						.append(" OR assunto.ds_assunto_trf ilike '%" + criteriosPesquisa.getAssuntoJudicial() + "%'")
					.append(") ");
			}
		}
		
		// busca pela classe
		if(criteriosPesquisa.getClasseJudicialObj() != null && criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial() > 0) {
			strQuery.append(" AND ptf.id_classe_judicial = " + criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial());
		}else {
			if(criteriosPesquisa.getClasseJudicial() != null) {
				strQuery
					.append(" AND ( ")
						.append("ptf.cd_classe_judicial = '" + criteriosPesquisa.getClasseJudicial() + "' ")
						.append(" OR ptf.ds_classe_judicial_sigla = '" + criteriosPesquisa.getClasseJudicial() + "'")
						.append(" OR ptf.ds_classe_judicial ilike '%" + criteriosPesquisa.getClasseJudicial() + "%'")
					.append(") ");
			}
		}
		
		// prioridade
		if(criteriosPesquisa.getPrioridadeObj() != null && criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso() > 0) {
			strQuery.append(" AND prioridade.id_prioridade_processo = "+ criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso());
		}else {
			if(criteriosPesquisa.getApenasPrioridade()) {
				strQuery.append(" AND ptf.in_prioridade = true ");
			}
		}
		
		// Data da autuação
		if(criteriosPesquisa.getDataAutuacaoInicial() != null) {
			strQuery.append(" AND fpe.dt_criacao_expediente >= '"+new java.sql.Date(criteriosPesquisa.getDataAutuacaoInicial().getTime()) +"'");
		}
		if(criteriosPesquisa.getDataAutuacaoFinal() != null) {
			strQuery.append(" AND fpe.dt_criacao_expediente <= '"+ new java.sql.Date(criteriosPesquisa.getDataAutuacaoFinal().getTime()) +"'");
		}
		
		// Órgão jugador
		if(criteriosPesquisa.getOrgaoJulgadorObj() != null && criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador() > 0) {
			strQuery.append(" AND ptf.id_orgao_julgador = "+criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador());
		}

		// Órgão jugador colegiado
		if(criteriosPesquisa.getOrgaoJulgadorColegiadoObj() != null && criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado() > 0) {
			strQuery.append(" AND ptf.id_orgao_julgador_colegiado = "+criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado());
		}

		// destinatario
		if(criteriosPesquisa.getNomeDestinatario() != null) {
			strQuery.append(" AND destinatario.ds_nome ilike '%" + criteriosPesquisa.getNomeDestinatario() + "%'");
		}
		
		// documento de identificacao
		if(criteriosPesquisa.getCpfDestinatario() != null) {
			strQuery.append(" AND cpf.nr_documento_identificacao = '" + criteriosPesquisa.getCpfDestinatario() + "'");
		}
		if(criteriosPesquisa.getCnpjDestinatario() != null) {
			strQuery.append(" AND cnpj.nr_documento_identificacao = '" + criteriosPesquisa.getCnpjDestinatario() + "'");
		}
		if(criteriosPesquisa.getOutroDocumentoDestinatario() != null) {
			strQuery.append(" AND outroDocIdentificacao.nr_documento_identificacao = '" + criteriosPesquisa.getOutroDocumentoDestinatario() + "'");
		}
		
		// OAB representante
		if(criteriosPesquisa.getOabRepresentanteDestinatario() != null) {
			strQuery.append(" AND oabRepresentante.nr_documento_identificacao = '"+criteriosPesquisa.getOabRepresentanteDestinatario()+"'");
		}
		
		return strQuery.toString();
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param idProcuradoria
	 * @param criteriosPesquisa
	 * @return
	 */
	public Map<String, String> obterQueryRestricoesExpedientes(Integer idPessoa, Integer idLocalizacao, Integer idProcuradoria, PesquisaExpedientesVO criteriosPesquisa) {		
		StringBuilder strQueryFrom = new StringBuilder(
				this.obterQueryFromFiltrosExpedientes(idLocalizacao, criteriosPesquisa));
		
		StringBuilder strQueryWhere = new StringBuilder(
				this.obterQueryRestricoesVisibilidade(idPessoa, idProcuradoria))
				.append(this.limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()))
				.append(this.obterFiltroExpedientes(criteriosPesquisa));
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
			
		return queryPesquisa;
	}

	private String consolidarConsultaExpedientesIds(List<Map<String, String>> consultasExpedientes, Map<String, String> restricoes) {
		StringBuilder consultaConsolidada = new StringBuilder();

		consultaConsolidada.append("SELECT DISTINCT id_processo_parte_expediente FROM ( ");
		
		int count = 0;
		for (Map<String, String> consulta : consultasExpedientes) {
			if (count > 0) {
				consultaConsolidada.append(" UNION ALL ");
			}
			consultaConsolidada.append("SELECT ppe.id_processo_parte_expediente ");
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
		consultaConsolidada.append( ") consulta_consolidada");
		
		String consultaSQL = consultaConsolidada.toString();
		
		return consultaSQL;
	}

	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param tipoUsuarioExterno
	 * @param idProcuradoria
	 * @param isProcuradorGestor
	 * @param idJurisdicao
	 * @param criteriosPesquisa
	 * @return
	 */
	public Long getCountExpedientesJurisdicao(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisa, Search searchLocal) {

		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		
		return this.getCountExpedientes(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa, searchLocal);
	}
	
	public Long getCountExpedientesJurisdicaoCaixa(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, Integer idCaixa, PesquisaExpedientesVO criteriosPesquisa, Search searchLocal) {

		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		criteriosPesquisa.setIdCaixaAdvProc(idCaixa);
		criteriosPesquisa.setApenasSemCaixa(false);
		
		return this.getCountExpedientes(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa, searchLocal);
	}
	
	@SuppressWarnings("rawtypes")
	public Long getCountExpedientes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) {

		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>();

		StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT ppe.id_processo_parte_expediente) ");
		query.append(obterQueryUsuarioExterno(idPessoa, 
				  idLocalizacao, 
				  tipoUsuarioExterno, 
				  idProcuradoria, 
				  isProcuradorGestor, 
				  criteriosPesquisaGeral, 
				  parametrosPesquisa,
				  false));

		query = new StringBuilder(aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisaGeral, parametrosPesquisa));
		
		if(idProcuradoria != null && idProcuradoria > 0) {
			query.append(" AND ppe.in_intima_pessoal = false "); 
			query.append(" AND ppe.id_procuradoria = :idProcuradoria ");

			if(!parametrosPesquisa.containsKey("idProcuradoria")) {
				parametrosPesquisa.put("idProcuradoria", idProcuradoria);
			}
		}

		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());

		for(String key: parametrosPesquisa.keySet()){
			q.setParameter(key, parametrosPesquisa.get(key));
		}
		
		Long contagemTotal = 0L;
		List r = q.getResultList();
		if(r != null && r.get(0) != null) {
			contagemTotal = ((BigInteger)r.get(0)).longValue();			
		}
		return contagemTotal;
	}
	
	public List<ProcessoParteExpediente> getExpedientesJurisdicao(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal, boolean mni) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);
		
		return this.getExpedientesPainelExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisaGeral, searchLocal, mni);
	}
	
	public List<ProcessoParteExpediente> getExpedientesJurisdicoes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal, boolean mni) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);
		
		return this.getExpedientesPainelExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisaGeral, searchLocal, mni);
	}

	public List<ProcessoParteExpediente> getExpedientes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal, boolean mni) {
		
		return this.getExpedientesPainelExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisaGeral, searchLocal, mni);
	}

	public List<ProcessoParteExpediente> getExpedientesJurisdicaoCaixa(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, Integer idCaixa, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) {

		criteriosPesquisaGeral.setIdJurisdicao(idJurisdicao);
		criteriosPesquisaGeral.setIdCaixaAdvProc(idCaixa);
		criteriosPesquisaGeral.setApenasSemCaixa(false);
		
		return this.getExpedientesPainelExterno(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisaGeral, searchLocal, false);
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param tipoUsuarioExterno
	 * @param idProcuradoria
	 * @param isProcuradorGestor
	 * @param criteriosPesquisa
	 * @param search
	 * @return
	 */
	private List<ProcessoParteExpediente> getExpedientesPainelExterno(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal, boolean mni){
		
		Map<String, Order> orderBy = (searchLocal == null ? null : searchLocal.getOrders());
		int first = (searchLocal == null ? 0 : searchLocal.getFirst());
		int max = (searchLocal == null ? 0 : (searchLocal.getMax() == null ? 0 : searchLocal.getMax()));
		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>(0);
		
		StringBuilder query = new StringBuilder();

		query.append("SELECT DISTINCT ptf.id_processo_trf, ptf.nr_processo, ptf.nr_sequencia, ptf.nr_ano, ptf.dt_distribuicao, ptf.in_prioridade "); 
		query.append(", ptf.nm_pessoa_autor, ptf.qt_autor, ptf.nm_pessoa_reu, ptf.qt_reu, ptf.id_orgao_julgador_colegiado, ptf.ds_orgao_julgador_colegiado ");
		query.append(", ptf.id_orgao_julgador, ptf.ds_orgao_julgador, ptf.id_classe_judicial, ptf.ds_classe_judicial_sigla, ptf.ds_classe_judicial ");
		query.append(", ppe.id_processo_parte_expediente, ppe.dt_ciencia_parte, ppe.dt_prazo_legal_parte, ppe.in_fechado, ppe.nm_pessoa_ciencia, ppe.nm_pessoa_parte ");
		query.append(", ppe.qt_prazo_legal_parte, ppe.in_tipo_prazo ");
		query.append(", pe.id_processo_expediente, pe.dt_criacao_expediente, pe.in_meio_expedicao_expediente ");
		if(mni) {
			query.append(", rgi.id, rgi.dt_registro, rgi.in_resultado ");
			query.append(", rex.id as rex_id, rex.dt_registro as rex_dt_registro ");
			query.append(", tpd.id_tipo_processo_documento, tpd.ds_tipo_processo_documento ");
			query.append(", ptf.in_segredo_justica ");
			query.append(", ppe.id_pessoa_parte ");
			query.append(", ptf.cd_classe_judicial ");
			query.append(", ptf.nr_identificacao_orgao_justica ");
			query.append(", ptf.id_jurisdicao ");
			query.append(", ptf.id_competencia ");
			query.append(", p.in_tipo_pessoa ");
			query.append(", ul.ds_nome ");
			query.append(", (SELECT DISTINCT nr_documento_identificacao FROM tb_pess_doc_identificacao WHERE id_pessoa=ppe.id_pessoa_parte and cd_tp_documento_identificacao IN ('CPF', 'CPJ') and in_usado_falsamente = false and in_ativo = true and in_principal = true limit 1) as nr_documento_identificacao ");
		}
		else {
			query.append(", null AS id, null AS dt_registro, null AS in_resultado ");
			query.append(", rex.id as rex_id, rex.dt_registro as rex_dt_registro ");
			query.append(", tpd.id_tipo_processo_documento, tpd.ds_tipo_processo_documento ");
			query.append(", ptf.in_segredo_justica ");
			query.append(", ppe.id_pessoa_parte ");
			query.append(", ptf.cd_classe_judicial ");
			query.append(", ptf.nr_identificacao_orgao_justica ");
			query.append(", ptf.id_jurisdicao ");
			query.append(", ptf.id_competencia ");
			query.append(", null AS in_tipo_pessoa ");
			query.append(", ul.ds_nome ");
			query.append(", null as nr_documento_identificacao ");
		}
		query.append(", ptf.cd_assunto_principal ");
		query.append(", ptf.ds_assunto_principal ");
		query.append(", ptf.vl_causa ");
		query.append(", ptf.dt_autuacao ");
		query.append(", ptf.in_instancia_orgao_julgador ");
		query.append(", ptf.cd_ibge_orgao_julgador ");
		query.append(", ptf2.in_bloqueia_peticao ");		
		query.append(", ptf.cd_nivel_acesso ");
		query.append(", ptf.ds_ultimo_movimento ");
		query.append(", ptf.dt_ultimo_movimento ");
		query.append(", ppe.dt_encerrado_manualmente ");
		query.append(", ppe.in_enviado_cancelamento ");
		query.append(", ppe.in_cancelado ");
		query.append(", ptf2.in_bloqueio_migracao ");
		query.append(", ppe.in_enviado_domicilio ");

		query.append(obterQueryUsuarioExterno(idPessoa, 
											  idLocalizacao, 
											  tipoUsuarioExterno, 
											  idProcuradoria, 
											  isProcuradorGestor, 
											  criteriosPesquisaGeral, 
											  parametrosPesquisa,
											  mni));
		
		query = new StringBuilder(aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisaGeral, parametrosPesquisa));

		if(idProcuradoria != null && idProcuradoria > 0) {
			query.append(" AND ppe.in_intima_pessoal = false "); 
			query.append(" AND ppe.id_procuradoria = :idProcuradoria "); 
			if(!parametrosPesquisa.containsKey("idProcuradoria")) {
				parametrosPesquisa.put("idProcuradoria", idProcuradoria);
			}
		}
		query.append(limitarOrdenacao(orderBy));
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());
		
		// Limita a paginação da pesquisa.
		q.setFirstResult(first);
		if(max > 0){
			q.setMaxResults(max);
		}
		
		for(String key: parametrosPesquisa.keySet()){
			q.setParameter(key, parametrosPesquisa.get(key));
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();
		return new ProcessoParteExpedienteTransform().transformCollection(resultList);
	}

	public List<JurisdicaoVO> getJurisdicoesExpedientes(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, PesquisaExpedientesVO criteriosPesquisa) {
		
		List<JurisdicaoVO> jurisdicoes = new ArrayList<JurisdicaoVO>(0);
		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>(0);

		StringBuilder query = new StringBuilder("SELECT ptf.id_jurisdicao, ptf.ds_jurisdicao, MAX(in_admin_jurisdicao), COUNT(DISTINCT ppe.id_processo_parte_expediente) ");
		query.append(obterQueryUsuarioExterno(idPessoa, 
											  idLocalizacao, 
											  tipoUsuarioExterno, 
											  idProcuradoria, 
											  isProcuradorGestor, 
											  criteriosPesquisa, 
											  parametrosPesquisa,
											  false));
		
		query = new StringBuilder(aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisa, parametrosPesquisa));

		if(idProcuradoria != null && idProcuradoria > 0) {
			query.append(" AND ppe.in_intima_pessoal = false "); 
			query.append(" AND ppe.id_procuradoria = :idProcuradoria ");
			if(!parametrosPesquisa.containsKey("idProcuradoria")) {
				parametrosPesquisa.put("idProcuradoria", idProcuradoria);
			}
		}
		query.append(" GROUP BY ptf.id_jurisdicao, ptf.ds_jurisdicao ");
		query.append(" ORDER BY ptf.ds_jurisdicao");

		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());
				
		for(String key: parametrosPesquisa.keySet()){
			q.setParameter(key, parametrosPesquisa.get(key));
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> r = q.getResultList();
		for (Object[] o: r) {
			JurisdicaoVO vo = new JurisdicaoVO();
			boolean isAdmin = (o[2] != null && ((Integer)o[2]) == 1 ? true : false);
			vo.setId((Integer)o[0]);
			vo.setDescricao((String)o[1]);
			vo.setAdmin(isAdmin);
			vo.setContador((BigInteger)o[3]);
			jurisdicoes.add(vo);
		}
		return jurisdicoes;
	}

	public List<CaixaAdvogadoProcuradorVO> getCaixasExpedientesJurisdicao(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisa) {
		
		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>(0);
		final boolean apenasCaixasAtivas = criteriosPesquisa.getApenasCaixasAtivas();
		boolean isProcuradoria = idProcuradoria != null && idProcuradoria > 0;
		
		String situacaoExpediente = limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente());
		String procuradoriaExpediente = " AND ppe.in_intima_pessoal = false AND ppe.id_procuradoria = :idProcuradoria ";

		criteriosPesquisa.setTipoSituacaoExpediente(null);
		criteriosPesquisa.setIdJurisdicao(null);
		
		StringBuilder query = new StringBuilder();

		query.append("SELECT caixa.id_caixa_adv_proc, caixa.nm_caixa, caixa.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao, ");
		query.append("MAX(in_admin_jurisdicao) AS admin_caixa, ");
		if(apenasCaixasAtivas) {
			query.append(" TRUE AS in_ativo, ");
		}else {
			query.append(" CASE WHEN cx_inativa.id_caixa_adv_proc IS NULL THEN TRUE ELSE FALSE END in_ativo, ");
		}
		query.append("COUNT(ppe.id_processo_parte_expediente) ");
		query.append("FROM tb_caixa_adv_proc caixa ");
		query.append("INNER JOIN tb_jurisdicao jur ON (caixa.id_jurisdicao = jur.id_jurisdicao) ");
		if(!isProcuradoria || isProcuradorGestor) {
			query.append(this.obterQueryAdminJurisdicao(true));
		}
		else {
			query.append("INNER JOIN ");
			query.append("( ");
			query.append("  SELECT CASE WHEN COUNT(1) > 0 THEN 1 ELSE 0 END AS in_admin_jurisdicao ");
			query.append("  FROM tb_pess_proc_jurisdicao ppj ");
			query.append("  INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria AND pproc.id_procuradoria = :idProcuradoria AND pproc.id_pessoa = :idPessoa) "); 
			query.append("  WHERE ppj.in_ativo IS TRUE "); 
			query.append("  AND ppj.id_jurisdicao = :idJurisdicao ");
			query.append(") AS administracao_jurisdicao ON (1=1) ");

			if(!parametrosPesquisa.containsKey("idPessoa")) {
				parametrosPesquisa.put("idPessoa", idPessoa);
			}
		}
		String innerOuLeftJoin = criteriosPesquisa.getApenasCaixasComResultados() ? " INNER " : " LEFT ";
		
		query.append(innerOuLeftJoin + " JOIN tb_proc_parte_exp_caixa_adv_proc fcx_exp ON (fcx_exp.id_caixa_adv_proc = caixa.id_caixa_adv_proc) ");
		query.append(innerOuLeftJoin + " JOIN tb_proc_parte_expediente ppe ON (ppe.id_processo_parte_expediente = fcx_exp.id_processo_parte_expediente ");
		query.append(situacaoExpediente); 
		query.append((isProcuradoria ? procuradoriaExpediente : "") + ") ");
		query.append(innerOuLeftJoin + " JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf AND ptf.id_jurisdicao = caixa.id_jurisdicao) ");

		query.append(obterQueryCaixasUsuarioExterno(idPessoa, 
											  		idLocalizacao, 
											  		tipoUsuarioExterno, 
											  		idProcuradoria, 
											  		isProcuradorGestor,
											  		criteriosPesquisa, 
											  		parametrosPesquisa));

		if(apenasCaixasAtivas) {
			StringBuilder whereApenasCaixasAtivas = new StringBuilder();
			
			whereApenasCaixasAtivas.append(" AND NOT EXISTS ( ")
								   .append(" 		SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in ")
								   .append("		WHERE cx_in.id_caixa_adv_proc = caixa.id_caixa_adv_proc ")
								   .append("		AND ( ")
								   .append("			 (CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim ) ")
								   .append("             or (cx_in.dt_inicio < current_timestamp and cx_in.dt_fim is null) ")
								   .append("		) ")
								   .append(" ) ");
			
			query.append(whereApenasCaixasAtivas.toString());
			
		}else {
			StringBuilder join = new StringBuilder();

			join.append(" LEFT JOIN ");
			join.append(" (SELECT DISTINCT cx_in.id_caixa_adv_proc ");
			join.append("  FROM tb_periodo_inativ_caixa_rep cx_in ");
			join.append("  WHERE CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim ");
			join.append("  OR (cx_in.dt_inicio <= CURRENT_TIMESTAMP AND cx_in.dt_fim IS NULL) ) ");
			join.append(" AS cx_inativa ON (cx_inativa.id_caixa_adv_proc = caixa.id_caixa_adv_proc) ");

			query = new StringBuilder(StringUtil.appendBefore(query.toString(), join.toString(), "WHERE"));
		}
		query = new StringBuilder(aplicarFiltros(query.toString(), idLocalizacao, criteriosPesquisa, parametrosPesquisa));

		query.append(" AND caixa.id_localizacao = :idLocalizacao ");
		query.append(" AND caixa.id_jurisdicao = :idJurisdicao ");
		query.append(" GROUP BY caixa.id_caixa_adv_proc, caixa.nm_caixa, caixa.ds_caixa, jur.id_jurisdicao, jur.ds_jurisdicao ");
		if(!apenasCaixasAtivas) {
			query.append(" , cx_inativa.id_caixa_adv_proc ");
		}
		query.append(" ORDER BY caixa.nm_caixa");
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(query.toString());
		
		if(!parametrosPesquisa.containsKey("idLocalizacao")) {
			parametrosPesquisa.put("idLocalizacao", idLocalizacao);
		}

		if(!parametrosPesquisa.containsKey("idJurisdicao")) {
			parametrosPesquisa.put("idJurisdicao", idJurisdicao);
		}
		
		if(isProcuradoria) {
			if(!parametrosPesquisa.containsKey("idProcuradoria")) {
				parametrosPesquisa.put("idProcuradoria", idProcuradoria);
			}
		}

		for(String key: parametrosPesquisa.keySet()){
			q.setParameter(key, parametrosPesquisa.get(key));
		}

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

	private String obterQueryUsuarioExterno(Integer idPessoa, 
			Integer idLocalizacao, 
			TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, 
			boolean isProcuradorGestor, 
			PesquisaExpedientesVO criteriosPesquisa,
			Map<String, Object> params,
			boolean mni) {

		StringBuilder consultas = new StringBuilder();
		
		if (idProcuradoria != null && idProcuradoria != 0) {
			if (isProcuradorGestor) {
				consultas.append(obterQueryProcuradorGestor(idProcuradoria, params, mni));
			}
			else{
				consultas.append(obterQueryProcuradorPadrao(idProcuradoria, idLocalizacao, idPessoa, criteriosPesquisa, params, mni));
			}
		} 
		else {
			if (TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryAdvogado(idPessoa, criteriosPesquisa, params, mni));
			}
			else if(TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryAssistenteAdvogado(idPessoa, idLocalizacao, criteriosPesquisa, params, mni));
			}
			else {
				consultas.append(obterQueryDestinatario(idPessoa, criteriosPesquisa, params, mni));
			}
		}
		return consultas.toString();
	}

	private String obterQueryCaixasUsuarioExterno(Integer idPessoa, 
				Integer idLocalizacao, 
				TipoUsuarioExternoEnum tipoUsuarioExterno, 
				Integer idProcuradoria, 
				boolean isProcuradorGestor, 
				PesquisaExpedientesVO criteriosPesquisa,
				Map<String, Object> params) {

		StringBuilder consultas = new StringBuilder();
		
		if (idProcuradoria != null && idProcuradoria != 0) {
			if (isProcuradorGestor) {
				consultas.append(obterQueryCaixasProcuradorGestor());
			}
			else{
				consultas.append(obterQueryCaixasProcuradorPadrao(idLocalizacao, idPessoa, params));
			}
		} 
		else {
			if (TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryCaixasAdvogado(idPessoa, criteriosPesquisa, params));
			}
			else if(TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExterno)) {
				consultas.append(obterQueryCaixasAssistenteAdvogado(idPessoa, idLocalizacao, criteriosPesquisa, params));
			}
			else {
				consultas.append(obterQueryCaixasDestinatario(idPessoa, criteriosPesquisa, params));
			}
		}
		return consultas.toString();
	}

	private String getJoinExpedientes(boolean mni) {
		StringBuilder query = new StringBuilder();
		query.append("INNER JOIN tb_processo_expediente pe ON (pe.id_processo_expediente = ppe.id_processo_expediente) ");
		query.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf) ");	
		query.append("INNER JOIN tb_processo_trf ptf2 ON (ptf2.id_processo_trf = ppe.id_processo_trf) ");
		query.append("INNER JOIN tb_tipo_processo_documento tpd ON (tpd.id_tipo_processo_documento = pe.id_tipo_processo_documento) ");
		query.append("INNER JOIN tb_usuario_login ul ON (ppe.id_pessoa_parte = ul.id_usuario) ");
		query.append(this.obterQueryAdminJurisdicao(true));
		query.append("LEFT JOIN tb_resposta_expediente rex ON (rex.id = ppe.id_resposta) ");
		if(mni) {
			query.append("INNER JOIN tb_pessoa p on (p.id_pessoa = ul.id_usuario) ");
			query.append("LEFT JOIN ");
			query.append("( ");
			query.append("   SELECT rgi.id, rgi.dt_registro, rgi.nr_aviso_recebimento, rgi.in_resultado, rgi.id_processo_parte_expediente ");
			query.append("   FROM tb_registro_intimacao rgi ");
			query.append("   WHERE rgi.id = (SELECT MIN(id) FROM tb_registro_intimacao WHERE id_processo_parte_expediente = rgi.id_processo_parte_expediente) ");
			query.append(") AS rgi ON (rgi.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		}
		
		return query.toString();
	}

	private String obterQueryProcuradorGestor(Integer idProcuradoria, Map<String, Object> params, boolean mni) {
		StringBuilder query = new StringBuilder();		
		
		query.append("FROM tb_proc_parte_expediente ppe ");
		query.append(getJoinExpedientes(mni));
		query.append("WHERE (1=1) ");
		query.append("AND ppe.in_intima_pessoal = false AND ppe.id_procuradoria = :idProcuradoria ");

		if(params == null) {
			params = new HashMap<String, Object>(0);
		}
		else { 
			if(!params.containsKey("idProcuradoria")) {
				params.put("idProcuradoria", idProcuradoria);
			}
		}
		
		return query.toString();
	}

	private String obterQueryProcuradorPadrao(Integer idProcuradoria, Integer idLocalizacao, Integer idPessoa, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params, boolean mni) {
		String querySituacao = (criteriosPesquisa.getTipoSituacaoExpediente() ==  null ? "" : limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		StringBuilder query = new StringBuilder();		
		
		query.append("FROM tb_proc_parte_expediente ppe ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente, cx.id_jurisdicao, 0 AS in_admin_jurisdicao "); 
		query.append("  FROM tb_caixa_adv_proc cx ");
		query.append("  INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		query.append("  INNER JOIN tb_proc_parte_exp_caixa_adv_proc cx_exp ON (cx.id_caixa_adv_proc = cx_exp.id_caixa_adv_proc) ");
		query.append("  INNER JOIN tb_proc_parte_expediente ppe ON (ppe.id_processo_parte_expediente = cx_exp.id_processo_parte_expediente) ");				
		query.append("  WHERE ppe.id_procuradoria = :idProcuradoria AND ppe.in_intima_pessoal = false ");
		query.append("  AND cx_rep.id_pessoa_fisica = :idPessoa ");
		query.append("  AND cx.id_localizacao = :idLocalizacao ");
		query.append("  AND NOT EXISTS ( ");
		query.append("  SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in WHERE cx_in.id_caixa_adv_proc = cx.id_caixa_adv_proc ");
		query.append("  AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim) ");
		query.append(querySituacao);
		if(criteriosPesquisa.getIdCaixaAdvProc() == null)
			query.append("  UNION ALL");
		else
			query.append("  UNION ");
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente, ptf.id_jurisdicao, 1 AS in_admin_jurisdicao ");   
		query.append("  FROM tb_proc_parte_expediente ppe ");  
		query.append("  INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf) ");   
		query.append("  WHERE ppe.id_procuradoria = :idProcuradoria AND ppe.in_intima_pessoal = false "); 
		query.append("  AND EXISTS ");
		query.append("  ( ");
		query.append("  	SELECT 1 "); 
		query.append("  	FROM tb_pessoa_procuradoria pproc ");   
		query.append("  	INNER JOIN tb_pess_proc_jurisdicao ppj ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria AND ppj.in_ativo = true) "); 
		query.append("  	WHERE pproc.id_pessoa = :idPessoa ");
		query.append("  	AND pproc.id_procuradoria = :idProcuradoria "); 
		query.append("  	AND ppj.id_jurisdicao = ptf.id_jurisdicao ");
		query.append("  ) ");
		query.append(querySituacao);
		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append("INNER JOIN tb_processo_expediente pe ON (pe.id_processo_expediente = ppe.id_processo_expediente) ");
		query.append("INNER JOIN tb_cabecalho_processo ptf ON (ptf.id_processo_trf = ppe.id_processo_trf) ");
		query.append("INNER JOIN tb_processo_trf ptf2 ON (ptf2.id_processo_trf = ppe.id_processo_trf) ");
		query.append("INNER JOIN tb_tipo_processo_documento tpd ON (tpd.id_tipo_processo_documento = pe.id_tipo_processo_documento) ");
		query.append("INNER JOIN tb_usuario_login ul ON (ppe.id_pessoa_parte = ul.id_usuario) ");
		query.append("LEFT JOIN tb_resposta_expediente rex ON (rex.id = ppe.id_resposta) ");
		if(mni) {
			query.append("INNER JOIN tb_pessoa p on (p.id_pessoa = ul.id_usuario) ");
			query.append("LEFT JOIN ");
			query.append("( ");
			query.append("   SELECT rgi.id, rgi.dt_registro, rgi.nr_aviso_recebimento, rgi.in_resultado, rgi.id_processo_parte_expediente ");
			query.append("   FROM tb_registro_intimacao rgi ");
			query.append("   WHERE rgi.id = (SELECT MIN(id) FROM tb_registro_intimacao WHERE id_processo_parte_expediente = rgi.id_processo_parte_expediente) ");
			query.append(") AS rgi ON (rgi.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		}
		
		query.append("WHERE (1=1) ");

		if(!params.containsKey("idProcuradoria")) {
			params.put("idProcuradoria", idProcuradoria);
		}
		if(!params.containsKey("idLocalizacao")) {
			params.put("idLocalizacao", idLocalizacao);
		}
		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		return query.toString();
	}

	private String obterQueryDestinatario(Integer idPessoa, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params, boolean mni) {
		StringBuilder query = new StringBuilder();		

		query.append("FROM tb_proc_parte_expediente ppe ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append(obterSelectDestinatario(idPessoa, params));

		if(criteriosPesquisa.getTipoSituacaoExpediente() != null) {
			query.append(limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		}

		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append(getJoinExpedientes(mni));
		query.append("WHERE (1=1) ");
		return query.toString();
	}

	private String obterQueryAdvogado(Integer idPessoa, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params, boolean mni) {
		String querySituacao = (criteriosPesquisa.getTipoSituacaoExpediente() ==  null ? "" : limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		StringBuilder query = new StringBuilder();		

		query.append("FROM tb_proc_parte_expediente ppe ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append(obterSelectDestinatario(idPessoa, params));
		query.append(querySituacao);
		if(criteriosPesquisa.getIdCaixaAdvProc() == null)
			query.append("  UNION ALL");
		else
			query.append("  UNION ");
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente ");
		query.append("  FROM tb_proc_parte_expediente ppe "); 
		query.append("  INNER JOIN tb_processo_parte pp on (pp.id_pessoa = ppe.id_pessoa_parte AND ppe.id_processo_trf = pp.id_processo_trf) ");
		query.append("  INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado) ");
		query.append("  WHERE ppr.in_situacao = 'A' ");
		query.append("  AND ppe.in_intima_pessoal = false ");
		query.append("  AND ppr.id_representante = :idPessoa ");
		query.append(querySituacao);
		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append(getJoinExpedientes(mni));
		query.append("WHERE (1=1) ");

		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		if(!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado", Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}

		return query.toString();
	}

	private String obterQueryAssistenteAdvogado(Integer idPessoa, Integer idLocalizacao, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params, boolean mni) {
		String querySituacao = (criteriosPesquisa.getTipoSituacaoExpediente() ==  null ? "" : limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		StringBuilder query = new StringBuilder();	
		query.append("FROM tb_proc_parte_expediente ppe ");
		query.append("INNER JOIN ");
		query.append("( ");
		query.append(obterSelectDestinatario(idPessoa, params));
		query.append(querySituacao);
		if(criteriosPesquisa.getIdCaixaAdvProc() == null)
			query.append("  UNION ALL");
		else
			query.append("  UNION ");
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente ");
		query.append("  FROM tb_proc_parte_expediente ppe "); 
		query.append("  INNER JOIN tb_processo_parte pp on (pp.id_pessoa = ppe.id_pessoa_parte AND ppe.id_processo_trf = pp.id_processo_trf) ");
		query.append("  INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND ppr.in_situacao = 'A' AND ppe.in_intima_pessoal = false) ");
		query.append("  INNER JOIN tb_pessoa_localizacao pl ON (pl.id_pessoa = ppr.id_representante) ");
		query.append("  INNER JOIN tb_usuario_localizacao ul ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) ");
		query.append("  WHERE ul.id_usuario = :idPessoa ");
		query.append("  AND ul.id_localizacao_fisica = :idLocalizacao ");
		query.append(querySituacao);
		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append(getJoinExpedientes(mni));
		query.append("WHERE (1=1) ");
		
		if(!params.containsKey("idLocalizacao")) {
			params.put("idLocalizacao", idLocalizacao);
		}

		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		
		if(!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado", Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}
		
		return query.toString();
	}

	private String obterQueryCaixasProcuradorGestor() {
		//Nada a fazer. A consulta principal de recuperação das caixas já contempla as cláusulas
		return " WHERE (1=1) ";
	}

	private String obterQueryCaixasProcuradorPadrao(Integer idLocalizacao, Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();		
		
		query.append("INNER JOIN ");
		query.append("( ");
		query.append("  SELECT cx.id_caixa_adv_proc, cx.id_jurisdicao, cx.id_localizacao, cx_rep.id_pessoa_fisica "); 
		query.append("  FROM tb_caixa_adv_proc cx ");
		query.append("  INNER JOIN tb_caixa_representante cx_rep ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ");
		query.append("  WHERE cx_rep.id_pessoa_fisica = :idPessoa ");
		query.append("  AND cx.id_localizacao = :idLocalizacao ");
		query.append("  UNION "); 
		query.append("  SELECT cx.id_caixa_adv_proc, cx.id_jurisdicao, cx.id_localizacao, pproc.id_pessoa ");
		query.append("  FROM tb_pess_proc_jurisdicao ppj ");
		query.append("  INNER JOIN tb_pessoa_procuradoria pproc ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ");
		query.append("  INNER JOIN tb_caixa_adv_proc cx ON (cx.id_jurisdicao = ppj.id_jurisdicao) "); 
		query.append("  WHERE cx.id_localizacao = :idLocalizacao ");
		query.append("  AND pproc.id_pessoa = :idPessoa ");
		query.append(") AS vw ON (vw.id_caixa_adv_proc = caixa.id_caixa_adv_proc AND vw.id_jurisdicao = caixa.id_jurisdicao AND vw.id_localizacao = caixa.id_localizacao AND vw.id_pessoa_fisica = :idPessoa) ");
		query.append("WHERE (1=1) ");

		if(!params.containsKey("idLocalizacao")) {
			params.put("idLocalizacao", idLocalizacao);
		}
		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}

		return query.toString();
	}

	private String obterQueryCaixasDestinatario(Integer idPessoa, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();		

		query.append("LEFT JOIN ");
		query.append("( ");
		query.append(obterSelectDestinatario(idPessoa, params));

		if(criteriosPesquisa.getTipoSituacaoExpediente() != null) {
			query.append(limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		}
		
		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append("WHERE (1=1) ");
		return query.toString();
	}

	private String obterQueryCaixasAdvogado(Integer idPessoa, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params) {
		String querySituacao = (criteriosPesquisa.getTipoSituacaoExpediente() ==  null ? "" : limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));		
		StringBuilder query = new StringBuilder();		

		query.append(" LEFT JOIN ");
		query.append("( ");
		query.append(obterSelectDestinatario(idPessoa, params));
		query.append(querySituacao);
		query.append("  UNION "); 
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente ");
		query.append("  FROM tb_proc_parte_expediente ppe "); 
		query.append("  INNER JOIN tb_processo_parte pp on (pp.id_pessoa = ppe.id_pessoa_parte AND ppe.id_processo_trf = pp.id_processo_trf) ");
		query.append("  INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado) ");
		query.append("  WHERE ppr.in_situacao = 'A' AND ppe.in_intima_pessoal = false AND ppr.id_representante = :idPessoa "); 
		query.append(querySituacao);
		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append("WHERE (1=1) ");

		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		if(!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado", Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}

		return query.toString();
	}

	private String obterQueryCaixasAssistenteAdvogado(Integer idPessoa, Integer idLocalizacao, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params) {
		String querySituacao = (criteriosPesquisa.getTipoSituacaoExpediente() ==  null ? "" : limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		StringBuilder query = new StringBuilder();	
		query.append(" LEFT JOIN ");
		query.append("( ");
		query.append(obterSelectDestinatario(idPessoa, params));
		query.append(querySituacao);
		query.append("  UNION "); 
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente ");
		query.append("  FROM tb_proc_parte_expediente ppe "); 
		query.append("  INNER JOIN tb_processo_parte pp on (pp.id_pessoa = ppe.id_pessoa_parte AND ppe.id_processo_trf = pp.id_processo_trf) ");
		query.append("  INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte AND ppr.id_tipo_representante = :idTipoParteAdvogado AND ppr.in_situacao = 'A' AND ppe.in_intima_pessoal = false) ");
		query.append("  INNER JOIN tb_pessoa_localizacao pl ON (pl.id_pessoa = ppr.id_representante) ");
		query.append("  INNER JOIN tb_usuario_localizacao ul ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) ");
		query.append("  WHERE ul.id_usuario = :idPessoa ");
		query.append("  AND ul.id_localizacao_fisica = :idLocalizacao ");
		query.append(querySituacao);

		query.append(") AS vw ON (vw.id_processo_trf = ppe.id_processo_trf AND vw.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ");
		query.append("WHERE (1=1) ");
		
		if(!params.containsKey("idLocalizacao")) {
			params.put("idLocalizacao", idLocalizacao);
		}

		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		
		if(!params.containsKey("idTipoParteAdvogado")) {
			params.put("idTipoParteAdvogado", Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)));
		}
		
		return query.toString();
	}

	private String obterSelectDestinatario(Integer idPessoa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder();		
		query.append("  SELECT ppe.id_processo_trf, ppe.id_processo_parte_expediente "); 
		query.append("  FROM tb_proc_parte_expediente ppe ");
		query.append("  WHERE ppe.id_pessoa_parte = :idPessoa "); 

		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		return query.toString();
	}

	private String aplicarFiltros(String q, Integer idLocalizacao, PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params) {
		StringBuilder query = new StringBuilder(q);
		
		if(criteriosPesquisa.getTipoSituacaoExpediente() != null) {
			query.append(limitarExpedientesPorSituacao(criteriosPesquisa.getTipoSituacaoExpediente()));
		}
		
		if(criteriosPesquisa.getIdJurisdicao() != null) {
			query.append(" AND ptf.id_jurisdicao = :idJurisdicao ");
			if(!params.containsKey("idJurisdicao")) {
				params.put("idJurisdicao", criteriosPesquisa.getIdJurisdicao());
			}
		}
		
		// busca por um número do processo dado
		if(criteriosPesquisa.getNumeroProcesso() != null) {
			String or = criteriosPesquisa.getApenasCaixasComResultados() ? "" : " OR ptf.nr_processo IS NULL";
			query.append(" AND (ptf.nr_processo = :numeroProcesso " + or + ") ");
			if(!params.containsKey("numeroProcesso")) {
				params.put("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
			}
		}else {
			if(criteriosPesquisa.getNumeroSequencia() != null) {
				query.append(" AND ptf.nr_sequencia = :numeroSequencia ");
				if(!params.containsKey("numeroSequencia")) {
					params.put("numeroSequencia", criteriosPesquisa.getNumeroSequencia());
				}
			}else {
				if(!criteriosPesquisa.getIntervalosNumerosSequenciais().isEmpty()) {
					List<IntervaloNumeroSequencialProcessoVO> intervalosNumerosSequenciais = criteriosPesquisa.getIntervalosNumerosSequenciais();
					int numIntervaloSequencial = 0;
					StringBuilder queryIntervalosNrSeq = new StringBuilder(" AND ( 1=0 ");
	
					for(IntervaloNumeroSequencialProcessoVO intervaloNrSequenciais: intervalosNumerosSequenciais) {
						if(intervaloNrSequenciais.getIntervaloValido()){
							String nomeParamSeqTamanho = "tamanhoIntervalo"+numIntervaloSequencial;
							String nomeParamSeqInicio = "sequencialInicio"+numIntervaloSequencial;
							String nomeParamSeqTermino = "sequencialFim"+numIntervaloSequencial;
							queryIntervalosNrSeq.append(" OR CAST(SUBSTR(ptf.nr_sequencia, 1, :"+nomeParamSeqTamanho+") AS INTEGER) BETWEEN :"+nomeParamSeqInicio+" AND :"+nomeParamSeqTermino+"");
							
							if(!params.containsKey(nomeParamSeqTamanho)) {
								params.put(nomeParamSeqTamanho, intervaloNrSequenciais);
							}
							if(!params.containsKey(nomeParamSeqInicio)) {
								params.put(nomeParamSeqInicio, intervaloNrSequenciais);
							}
							if(!params.containsKey(nomeParamSeqTermino)) {
								params.put(nomeParamSeqTermino, intervaloNrSequenciais);
							}
							
							numIntervaloSequencial++;
						}
					}
					queryIntervalosNrSeq.append(" ) ");
					if(numIntervaloSequencial > 0) {
						query.append(queryIntervalosNrSeq);
					}
				}
			}

			if(criteriosPesquisa.getDigitoVerificador() != null) {
				query.append(" AND ptf.nr_digito_verificador = :digitoVerificador ");
				if(!params.containsKey("digitoVerificador")) {
					params.put("digitoVerificador", criteriosPesquisa.getDigitoVerificador());
				}
			}
			if(criteriosPesquisa.getNumeroAno() != null) {
				query.append(" AND ptf.nr_ano = :numeroAno ");
				if(!params.containsKey("numeroAno")) {
					params.put("numeroAno", criteriosPesquisa.getNumeroAno());
				}
			}
			if(criteriosPesquisa.getNumeroOrgaoJustica() != null) {
				query.append(" AND ptf.nr_identificacao_orgao_justica = :numeroOrgaoJustica ");
				if(!params.containsKey("numeroOrgaoJustica")) {
					params.put("numeroOrgaoJustica", criteriosPesquisa.getNumeroOrgaoJustica());
				}
			}
			if(criteriosPesquisa.getNumeroOrigem() != null) {
				query.append(" AND ptf.nr_origem_processo = :numeroOrigem ");
				if(!params.containsKey("numeroOrigem")) {
					params.put("numeroOrigem", criteriosPesquisa.getNumeroOrigem());
				}
			}
		}		
		
		if(criteriosPesquisa.getIdProcessoParteExpediente() != null && criteriosPesquisa.getIdProcessoParteExpediente() > 0) {
			query.append(" AND ppe.id_processo_parte_expediente = :idProcessoParteExpediente ");
			if(!params.containsKey("idProcessoParteExpediente")) {
				params.put("idProcessoParteExpediente", criteriosPesquisa.getIdProcessoParteExpediente());
			}
		}
		
		if(criteriosPesquisa.getApenasSemCaixa()) {
			query.append(" AND NOT EXISTS "); 
			query.append("(");
			query.append("	SELECT 1 ");
			query.append("	FROM tb_proc_parte_exp_caixa_adv_proc fcx_exp ");  
			query.append("	INNER JOIN tb_caixa_adv_proc caixa ON (caixa.id_caixa_adv_proc = fcx_exp.id_caixa_adv_proc AND caixa.id_localizacao = :idLocalizacao) "); 
			query.append("	WHERE fcx_exp.id_processo_parte_expediente = ppe.id_processo_parte_expediente ");
			query.append(") ");

			if(!params.containsKey("idLocalizacao")) {
				params.put("idLocalizacao", idLocalizacao);
			}
		}
		else if(criteriosPesquisa.getIdCaixaAdvProc() != null && criteriosPesquisa.getIdCaixaAdvProc() > 0) {
			String inner = "INNER JOIN (SELECT caixa.id_caixa_adv_proc, fcx_exp.id_processo_parte_expediente FROM tb_proc_parte_exp_caixa_adv_proc fcx_exp " +
			           " INNER JOIN tb_caixa_adv_proc caixa ON (caixa.id_caixa_adv_proc = fcx_exp.id_caixa_adv_proc AND caixa.id_localizacao = :idLocalizacao)" +
			           " ) fcaixa ON (fcaixa.id_processo_parte_expediente = ppe.id_processo_parte_expediente) ";

			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));

			if(!params.containsKey("idLocalizacao")) {
				params.put("idLocalizacao", idLocalizacao);
			}

			query.append(" AND fcaixa.id_caixa_adv_proc = :idCaixa ");
			if(!params.containsKey("idCaixa")) {
				params.put("idCaixa", criteriosPesquisa.getIdCaixaAdvProc());
			}
		}
		
		// busca pelo assunto
		if(criteriosPesquisa.getAssuntoJudicialObj() != null && criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto() > 0) {
			String inner = " INNER JOIN tb_processo_assunto pas ON (pas.id_processo_trf = ptf.id_processo_trf) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND pas.id_assunto_trf = :idAssunto ");
			if(!params.containsKey("idAssunto")) {
				params.put("idAssunto", criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto());
			}
		}else {
			if(!criteriosPesquisa.getAssuntoTrfList().isEmpty()) {
				String inner = " INNER JOIN tb_processo_assunto pas ON (pas.id_processo_trf = ptf.id_processo_trf) ";
				query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));

				int numAssunto = 0;
				StringBuilder queryListaAssuntos = new StringBuilder(" AND ( 1=10 ");
				for(AssuntoTrf assuntoTrf: criteriosPesquisa.getAssuntoTrfList()) {
					String nomeParamIdAssuntoTrf = "idAssunto"+numAssunto;
					queryListaAssuntos.append(" OR pas.id_assunto_trf = :"+nomeParamIdAssuntoTrf);
					if(!params.containsKey(nomeParamIdAssuntoTrf)) {
						params.put(nomeParamIdAssuntoTrf, assuntoTrf.getIdAssuntoTrf());
					}
					numAssunto++;
				}
				queryListaAssuntos.append(" ) ");
				if(numAssunto > 0 ) {
					query.append(queryListaAssuntos);
				}
			}
			else {
				if(criteriosPesquisa.getAssuntoJudicial() != null) {
					String inner = " INNER JOIN tb_processo_assunto pas ON (pas.id_processo_trf = ptf.id_processo_trf) " +
	                " INNER JOIN tb_assunto_trf assunto ON (pas.id_assunto_trf = assunto.id_assunto_trf) ";
	
					query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
	
					query.append(" AND ( ")
					.append("assunto.cd_assunto_trf = :codigoAssunto ")
					.append(" OR LOWER(TO_ASCII(assunto.ds_assunto_trf)) LIKE LOWER(TO_ASCII(:nomeAssunto)) ")
					.append(") ");
	
					if(!params.containsKey("codigoAssunto")) {
						params.put("codigoAssunto", criteriosPesquisa.getAssuntoJudicial());
					}
					if(!params.containsKey("nomeAssunto")) {
						params.put("nomeAssunto", "%" + criteriosPesquisa.getAssuntoJudicial() + "%");
					}
				}
			}
		}
		
		// busca pela classe
		if(criteriosPesquisa.getClasseJudicialObj() != null && criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial() > 0) {
			query.append(" AND ptf.id_classe_judicial = :idClasse ");
			if(!params.containsKey("idClasse")) {
				params.put("idClasse", criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial());
			}
		}else {
			if(!criteriosPesquisa.getClasseJudicialList().isEmpty()) {
				int numClasse = 0;
				StringBuilder queryListaClasses = new StringBuilder(" AND ( 1=20 ");
				for(ClasseJudicial classeJudicial: criteriosPesquisa.getClasseJudicialList()) {
					String nomeParamIdClasseJudicial = "idClasse"+numClasse;
					queryListaClasses.append(" OR ptf.id_classe_judicial = :"+nomeParamIdClasseJudicial);
					if(!params.containsKey(nomeParamIdClasseJudicial)) {
						params.put(nomeParamIdClasseJudicial, classeJudicial.getIdClasseJudicial());
					}
					
					numClasse++;
				}
				queryListaClasses.append(" ) ");
				if(numClasse > 0) {
					query.append(queryListaClasses);
				}
			}else {
				if(criteriosPesquisa.getClasseJudicial() != null) {
					query
						.append(" AND ( ")
						.append("ptf.cd_classe_judicial = :codigoClasse ")
						.append(" OR LOWER(ptf.ds_classe_judicial_sigla) = LOWER(:siglaClasse) ")
						.append(" OR LOWER(TO_ASCII(ptf.ds_classe_judicial)) LIKE LOWER(TO_ASCII(:nomeClasse)) ")
						.append(") ");
					if(!params.containsKey("codigoClasse")) {
						params.put("codigoClasse", criteriosPesquisa.getClasseJudicial());
					}
					if(!params.containsKey("siglaClasse")) {
						params.put("siglaClasse", criteriosPesquisa.getClasseJudicial());
					}
					if(!params.containsKey("nomeClasse")) {
						params.put("nomeClasse", "%" + criteriosPesquisa.getClasseJudicial() + "%");
					}
				}
			}
		}
		
		// prioridade
		if(criteriosPesquisa.getPrioridadeObj() != null && criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso() > 0) {
			String inner = " INNER JOIN tb_proc_prioridde_processo ppp ON (ppp.id_processo_trf = ptf.id_processo_trf) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND ppp.id_prioridade_processo = :idPrioridade ");
			if(!params.containsKey("idPrioridade")) {
				params.put("idPrioridade", criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso());
			}
			
		}else {
			if(criteriosPesquisa.getApenasPrioridade()) {
				query.append(" AND ptf.in_prioridade = true ");
			}
		}
		
		// Data de criação do expediente
		if(criteriosPesquisa.getDataCriacaoExpedienteInicial() != null) {
			query.append(" AND pe.dt_criacao_expediente >= :dataCriacaoInicio ");
			if(!params.containsKey("dataCriacaoInicio")) {
				params.put("dataCriacaoInicio", criteriosPesquisa.getDataCriacaoExpedienteInicial());
			}
		}

		if(criteriosPesquisa.getDataCriacaoExpedienteFinal() != null) {
			query.append(" AND pe.dt_criacao_expediente <= :dataCriacaoFim ");
			if(!params.containsKey("dataCriacaoFim")) {
				params.put("dataCriacaoFim", criteriosPesquisa.getDataCriacaoExpedienteFinal());
			}
		}
		
		// Órgão jugador
		if(criteriosPesquisa.getOrgaoJulgadorObj() != null && criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador() > 0) {
			query.append(" AND ptf.id_orgao_julgador = :idOrgaoJulgador ");
			if(!params.containsKey("idOrgaoJulgador")) {
				params.put("idOrgaoJulgador", criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador());
			}
		}

		// Órgão jugador colegiado
		if(criteriosPesquisa.getOrgaoJulgadorColegiadoObj() != null && criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado() > 0) {
			query.append(" AND ptf.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			if(!params.containsKey("idOrgaoJulgadorColegiado")) {
				params.put("idOrgaoJulgadorColegiado", criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado());
			}
		}

		// Destinatário
		if(criteriosPesquisa.getNomeDestinatario() != null) {
			query.append(" AND LOWER(TO_ASCII(ul.ds_nome)) LIKE LOWER(TO_ASCII(:nomeDestinatario)) ");
			if(!params.containsKey("nomeDestinatario")) {
				params.put("nomeDestinatario", "%" + criteriosPesquisa.getNomeDestinatario() + "%");
			}
		}
		
		query = new StringBuilder(adicionaFiltroNomeParte(criteriosPesquisa, params, query));
		query = new StringBuilder(adicionaFiltroDataNascimento(criteriosPesquisa, params, query));

		// CPF do destinatário
		if(criteriosPesquisa.getCpfDestinatario() != null) {
			String inner = " INNER JOIN tb_pess_doc_identificacao cpf ON (cpf.id_pessoa = ppe.id_pessoa_parte AND cpf.cd_tp_documento_identificacao = 'CPF' AND cpf.in_usado_falsamente = false AND cpf.in_ativo = true) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND cpf.nr_documento_identificacao = :numeroCpf ");
			if(!params.containsKey("numeroCpf")) {
				params.put("numeroCpf", criteriosPesquisa.getCpfDestinatario());
			}
		}

		// CNPJ do destinatário
		if(criteriosPesquisa.getCnpjDestinatario() != null) {
			String inner = " INNER JOIN tb_pess_doc_identificacao cnpj ON (cnpj.id_pessoa = ppe.id_pessoa_parte AND cnpj.cd_tp_documento_identificacao = 'CPJ' AND cnpj.in_usado_falsamente = false AND cnpj.in_ativo = true) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND cnpj.nr_documento_identificacao = :numeroCnpj ");
			if(!params.containsKey("numeroCnpj")) {
				params.put("numeroCnpj", criteriosPesquisa.getCnpjDestinatario());
			}
		}
		
		// Outros documentos de identificação do destinatário
		if(criteriosPesquisa.getOutroDocumentoDestinatario() != null) {
			String inner = " INNER JOIN tb_pess_doc_identificacao outroDoc ON (outroDoc.id_pessoa = ppe.id_pessoa_parte AND outroDoc.cd_tp_documento_identificacao NOT IN ('CPF','CPJ') AND outroDoc.in_usado_falsamente = false  AND outroDoc.in_ativo = true) ";
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner, "WHERE (1=1)"));
			query.append(" AND outroDoc.nr_documento_identificacao = :numeroOutroDocumento ");
			if(!params.containsKey("numeroOutroDocumento")) {
				params.put("numeroOutroDocumento", criteriosPesquisa.getOutroDocumentoDestinatario());
			}
		}
		
		// OAB representante
		if(criteriosPesquisa.getOabRepresentanteDestinatario() != null) {
			query.append(" AND EXISTS "); 
			query.append(" ( ");
			query.append("   SELECT 1 ");
			query.append(" 	 FROM tb_processo_parte pp ");  
			query.append(" 	 INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_processo_parte = pp.id_processo_parte) ");  
			query.append(" 	 INNER JOIN tb_pessoa_advogado pa ON (pa.id = ppr.id_representante) ");
			query.append(" 	 WHERE pp.id_processo_trf = ptf.id_processo_trf ");
			query.append(" 	 AND pa.nr_oab LIKE :numeroOab ");
			query.append(" ) ");  

			if(!params.containsKey("numeroOab")) {
				params.put("numeroOab", criteriosPesquisa.getOabRepresentanteDestinatario() + "%");
			}
		}
		
		if (criteriosPesquisa.getPessoaParteRepresentado() != null) {
			query.append(" AND ppe.id_pessoa_parte = :idPessoaParteRepresentado ");
			if(!params.containsKey("idPessoaParteRepresentado")) {
				params.put("idPessoaParteRepresentado", criteriosPesquisa.getPessoaParteRepresentado());
			}
		}
		
		if(criteriosPesquisa.getIdProcessoTrf() != null) {
			String or = criteriosPesquisa.getApenasCaixasComResultados() ? "" : " OR ptf.id_processo_trf IS NULL";
			query.append(" AND (ptf.id_processo_trf = :idProcessoTrf " + or + ") ");
			if(!params.containsKey("idProcessoTrf")) {
				params.put("idProcessoTrf", criteriosPesquisa.getIdProcessoTrf());
			}
		}
		return query.toString();
	}
	
	public StringBuilder adicionaFiltroNomeParte(PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params, StringBuilder query) {
		if (criteriosPesquisa.getNomeParte() != null) {
			StringBuilder inner = new StringBuilder();
			inner.append(" INNER JOIN client.tb_processo_parte tpp ON (ppe.id_processo_trf = tpp.id_processo_trf) ");
			inner.append(" INNER JOIN acl.tb_usuario_login tul ON (tpp.id_pessoa = tul.id_usuario) ");
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner.toString(), WHERE));
			query.append(" AND LOWER(TO_ASCII(tul.ds_nome)) LIKE LOWER(TO_ASCII(:nomeParte)) ");
			if (!params.containsKey("nomeParte")) {
				params.put("nomeParte", "%" + criteriosPesquisa.getNomeParte() + "%");
			}
		}
		return query;
	}
	
	public StringBuilder adicionaFiltroDataNascimento(PesquisaExpedientesVO criteriosPesquisa, Map<String, Object> params, StringBuilder query) {
		if (criteriosPesquisa.getDataNascimentoInicial() != null || criteriosPesquisa.getDataNascimentoFinal() != null) {
			StringBuilder inner = new StringBuilder();
			inner.append(" INNER JOIN client.tb_processo_parte tpp2 ON (ppe.id_processo_trf = tpp2.id_processo_trf) ");
			inner.append(" INNER JOIN tb_pessoa_fisica pf ON (tpp2.id_pessoa = pf.id_pessoa_fisica) ");
			query = new StringBuilder(StringUtil.appendBefore(query.toString(), inner.toString(), WHERE));
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
		return query;
	}

	public String limitarVisibilidade(Integer idPessoa, Integer idProcuradoria, Map<String, Object> params ) {
		StringBuilder query = new StringBuilder();
		query.append(" AND (ptf.in_segredo_justica = false OR EXISTS (");
		query.append(" SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_processo_trf = ptf.id_processo_trf ");
		query.append(" AND (vis.id_pessoa = :idPessoa ");
		if (idProcuradoria != null && idProcuradoria > 0) {
			query.append(" OR vis.id_procuradoria = :idProcuradoria ");
			if(!params.containsKey("idProcuradoria")) {
				params.put("idProcuradoria", idProcuradoria);
			}
		}
		else {
			query.append(" OR (vis.id_pessoa = pp.id_pessoa AND ppr.id_representante = :idPessoa) ");
		}
		query.append("))) ");

		if(!params.containsKey("idPessoa")) {
			params.put("idPessoa", idPessoa);
		}
		
		return query.toString();
	}

	/**
	 * Retorna true se existir expedientes abertos e enviados ao Domicílio Eletrônico do processo passado.
	 * 
	 * @param processo ProcessoTrf
	 * @return Booleano
	 */
	public Boolean isExisteExpedienteAbertoEnviadoAoDomicilioEletronico(ProcessoTrf processo) {
		Boolean resultado = Boolean.FALSE;
		
		if (processo != null) {
			StringBuilder hql = new StringBuilder();
			hql.append("SELECT COUNT(ppe) ");
			hql.append("FROM ProcessoParteExpediente AS ppe ");
			hql.append("WHERE ");
			hql.append("  ppe.processoJudicial = :processoJudicial and ");
			hql.append("  ppe.enviadoDomicilio = true and ");
			hql.append("  ppe.fechado = false");
			
			Query query = getEntityManager().createQuery(hql.toString());
			query.setParameter(ProcessoParteExpediente.ATTR.PROCESSO_JUDICIAL, processo);
	
			resultado = ((Long) EntityUtil.getSingleResult(query) > 0);
		}
		return resultado;
	}

	/**
	 * @return Lista de expedientes enviados ao Domicílio Eletrônico.
	 */
	public List<ProcessoParteExpediente> getAtosComunicacaoPendentesDomicilioEletronico() {
		StringBuilder query = new StringBuilder("SELECT ppe FROM ProcessoParteExpediente AS ppe WHERE ppe.fechado = false AND (ppe.tipoPrazo != 'S' AND ((ppe.dtPrazoLegal IS NOT NULL) OR (ppe.prazoLegal IS NOT NULL AND ppe.prazoLegal > 0))) AND ppe.enviadoDomicilio = true");
		Query q = entityManager.createQuery(query.toString());
		return q.getResultList();
	}
	
	/**
	 * Retorna true se existir expediente parte.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 * @return Booleano
	 */
	public Boolean isExisteParteExpediente(ProcessoParteExpediente parteExpediente) {
		Boolean resultado = Boolean.FALSE;
		
		if (parteExpediente != null) {
			StringBuilder hql = new StringBuilder();
			hql.append("SELECT COUNT(ppe) ");
			hql.append("FROM ProcessoParteExpediente AS ppe ");
			hql.append("WHERE ");
			hql.append("  ppe.idProcessoParteExpediente = :idProcParteExpediente");
			
			Query query = getEntityManager().createQuery(hql.toString());
			query.setParameter("idProcParteExpediente", parteExpediente.getIdProcessoParteExpediente());
	
			resultado = ((Long) EntityUtil.getSingleResult(query) > 0);

		}
		return resultado;
	}
	
	public Boolean existeExpedienteAberto(ProcessoTrf processoTrf) {
		Boolean resultado = Boolean.FALSE;

		if (processoTrf != null) {
			StringBuilder hql = new StringBuilder();
			hql.append("SELECT COUNT(ppe) ");
			hql.append("FROM ProcessoParteExpediente AS ppe ");
			hql.append("WHERE ");
			hql.append("  ppe.processoJudicial = :processoJudicial and ");
			hql.append("  ppe.fechado = false");

			Query query = getEntityManager().createQuery(hql.toString());
			query.setParameter(ProcessoParteExpediente.ATTR.PROCESSO_JUDICIAL, processoTrf);

			resultado = ((Long) EntityUtil.getSingleResult(query) > 0);
		}
		return resultado;
	}
}