package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteMin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name("processoParteDAO")
public class ProcessoParteDAO extends BaseDAO<ProcessoParte>{

	@Override
	public Integer getId(ProcessoParte e){
		return e.getIdProcessoParte();
	}

	/**
	 * Obtem a primeira parte ativa ou passiva de um processo que não seja advogado atraves do processoTrf e da participação (Ativo ou Passivo) e que
	 * não sejam advogados.
	 * 
	 * @param procTrf a se obter a parte
	 * @paran inParticipacao participaçao da parte (Ativo ou Passivo)
	 * @return ProcessoParte.
	 */
	public ProcessoParte getProcessoParteByProcessoTrf(ProcessoTrf procTrf, String inParticipacao){

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("processoTrf", procTrf);
		parameters.put("inParticipacao", inParticipacao);
		parameters.put("tipoParteAdvogado", ParametroUtil.instance().getTipoParteAdvogado());

		Query query = getEntityManager().createQuery(
				"select o from ProcessoParte o " + " where o.inParticipacao = :inParticipacao "
					+ " and o.processoTrf = :processoTrf " + " and o.tipoParte != :tipoParteAdvogado "
					+ " order by o.idProcessoParte desc");

		query.setParameter("inParticipacao", inParticipacao);
		query.setParameter("processoTrf", procTrf);
		query.setParameter("tipoParteAdvogado", ParametroUtil.instance().getTipoParteAdvogado());

		@SuppressWarnings("unchecked")
		List<ProcessoParte> result = query.getResultList();
		if(result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	/**
	 * Retorna o ProcessoParte baseado nos parametros.
	 * 
	 * @param processoJudicial processoTrf
	 * @param tipoParte tipo da parte
	 * @param pessoa Pessoa
	 * @return A parte referente a pesquisa
	 */
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, TipoParte tipoParte, Pessoa pessoa){
		return findProcessoParte(processoJudicial, tipoParte, pessoa, null, false);
	}
	
	/**
	 * Retorna o ProcessoParte baseado nos parametros.
	 * 
	 * @param processoJudicial processoTrf
	 * @param tipoParte tipo da parte
	 * @param pessoa Pessoa
	 * @param inParticipacao Ativo, Passivo
	 * @return A parte referente a pesquisa
	 */
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, TipoParte tipoParte, Pessoa pessoa, ProcessoParteParticipacaoEnum inParticipacao, boolean excluiInativas) {
		return this.findProcessoParte(processoJudicial, tipoParte, pessoa, inParticipacao, excluiInativas, false);
	}
	
	/**
	 * Retorna o ProcessoParte baseado nos parametros.
	 * 
	 * @param processoJudicial processoTrf
	 * @param tipoParte tipo da parte
	 * @param pessoa Pessoa
	 * @param inParticipacao Ativo, Passivo
	 * @param verificaSeProcurador
	 * @return A parte referente a pesquisa
	 */
	public ProcessoParte findProcessoParte(ProcessoTrf processoJudicial, TipoParte tipoParte, Pessoa pessoa, 
			ProcessoParteParticipacaoEnum inParticipacao, boolean excluiInativas, boolean verificaSeProcurador) {
		
		StringBuilder hql = new StringBuilder();
		hql.append(" SELECT procParte from ProcessoParte procParte");
		if(verificaSeProcurador) {
			hql.append(" LEFT JOIN procParte.procuradoria procuradoria ");
			hql.append(" LEFT JOIN procuradoria.pessoaProcuradoriaList pessoaProcuradoria ");
		}
		hql.append(" WHERE procParte.processoTrf = :processoTrf");
		
		if(tipoParte != null){
			hql.append(" AND procParte.tipoParte = :tipoParte");
		}
		if(inParticipacao != null){
			hql.append(" AND procParte.inParticipacao = :inParticipacao ");
		}
		if(excluiInativas){
			hql.append(" AND procParte.inSituacao in ('A','B','S') ");
		}
		hql.append(" AND (");
		hql.append(" procParte.pessoa = :pessoa");
		if(verificaSeProcurador) {
			hql.append(" OR (");
			hql.append(" pessoaProcuradoria.pessoa = :pessoa ");
			hql.append(" ) ");
		}
		hql.append(" ) ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("processoTrf", processoJudicial);
		query.setParameter("pessoa", pessoa);
		if(tipoParte != null){
			query.setParameter("tipoParte", tipoParte);
		}
		if(inParticipacao != null ){
			query.setParameter("inParticipacao", inParticipacao);
		}

		@SuppressWarnings("unchecked")
		List<ProcessoParte> result = query.getResultList();
		if(!CollectionUtilsPje.isEmpty(result)) {
			return result.get(0);
		}
		return null;
	}
	
	/**
	 * Indica se uma pessoa compõe o processo judicial como uma parte de um ou mais tipos dados.
	 * 
	 * @param processoJudicial o processo judicial a respeito do qual se pretende buscar a informação
	 * @param pessoa a pessoa que se pretende identificar como parte
	 * @param polo o polo de interesse, ou null, se indiferente essa verificação
	 * @param tipoParte os tipos de parte que se pretende investigar
	 * @return true, se a pessoa for uma parte do tipo dado no processo judicial indicado
	 */
	public boolean isParte(ProcessoTrf processoJudicial, Pessoa pessoa, ProcessoParteParticipacaoEnum polo, TipoParte...tipoParte){
		StringBuilder jpql = new StringBuilder("SELECT COUNT(pp) FROM ProcessoParte AS pp ");
		jpql.append("WHERE pp.pessoa = :pessoa ");
		jpql.append("AND pp.inSituacao = 'A' ");
		jpql.append("AND pp.processoTrf = :processoJudicial ");

		if (tipoParte != null && tipoParte.length > 0) {
			jpql.append("AND pp.tipoParte IN (:tipoParte) ");
		}
		if(polo != null){
			jpql.append("AND pp.polo = :polo");
		}
		
		Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("pessoa", pessoa);
		query.setParameter("processoJudicial", processoJudicial);
		
		if (tipoParte != null && tipoParte.length > 0) {
			query.setParameter("tipoParte", Arrays.asList(tipoParte));
		}
		if(polo != null){
			query.setParameter("polo", polo);
		}
		Number count = (Number) query.getSingleResult();
		return count.intValue() > 0;
	}
	
	/**
	 * Recupera o número de partes sigilosas em um dado processo judicial.
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar apenas as partes ativas
	 * @return o número de partes sigilosas
	 * @see ProcessoParteSituacaoEnum.A
	 */
	public long contagemPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas){
		return contagemPartesSigilosas(processo, somenteAtivas, ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
	}

	/**
	 * Recupera uma lista com todas as partes sigilosas de um dado processo judicial.
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes sigilosas
	 * @see ProcessoParteSituacaoEnum.A
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperaPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults, ProcessoParteParticipacaoEnum... polo) {
		StringBuilder query = new StringBuilder("SELECT p FROM ProcessoParte AS p " +
				"	WHERE p.parteSigilosa = true " +
				"		AND p.processoTrf = :processo");
		if(somenteAtivas){
			query.append(" AND p.inSituacao = 'A'");
		}
		Query q = null;
		if(polo != null && polo.length > 0){
			query.append(" AND p.inParticipacao IN (:polo) ");
			q = entityManager.createQuery(query.toString());
			q.setParameter("polo", Arrays.asList(polo));
		}else{
			q = entityManager.createQuery(query.toString());
		}
		q.setParameter("processo", processo);
		if(first != null && first.intValue() > 0){
			q.setFirstResult(first);
		}
		if(maxResults != null && maxResults > 0){
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}

	/**
	 * Recupera o número de partes existentes no processo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se quer a informação
	 * @param somenteAtivas marca indicativa de que a contagem deve se limitar às partes ativas.
	 * @return o número de partes
	 */
	public long contagemPartes(ProcessoTrf processo, boolean somenteAtivas) {
		return contagemPartes(processo, somenteAtivas, ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
	}

	/**
	 * Recupera uma lista com todas as partes de um dado processo judicial.
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperaPartes(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults, ProcessoParteParticipacaoEnum... polo) {
		StringBuilder query = new StringBuilder("SELECT DISTINCT p FROM ProcessoParte AS p " +
				"	WHERE p.processoTrf = :processo");
		if(somenteAtivas){
			query.append("	AND p.inSituacao = 'A' ");
		}
		Query q = null;
		if(polo != null && polo.length > 0){
			query.append(" AND p.inParticipacao IN (:polo) ");
			q = entityManager.createQuery(query.toString());
			q.setParameter("polo", Arrays.asList(polo));
		}else{
			q = entityManager.createQuery(query.toString());
		}
		q.setParameter("processo", processo);
		if(first != null && first.intValue() > 0){
			q.setFirstResult(first);
		}
		if(maxResults != null && maxResults.intValue() > 0){
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}
	
	/**
	 * Recupera o número de partes existentes no processo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se quer a informação
	 * @param somenteAtivas marca indicativa de que a contagem deve se limitar às partes ativas.
	 * @return o número de partes
	 */
	public long contagemPartes(ProcessoTrf processo, boolean somenteAtivas, ProcessoParteParticipacaoEnum... polo) {
		StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT p.idProcessoParte) FROM ProcessoParte AS p " +
				"	WHERE p.processoTrf = :processo AND p.partePrincipal = true ");
		Query q = null; 
		if(somenteAtivas){
			query.append(" AND p.inSituacao = 'A'");
		}
		if(polo != null && polo.length > 0){
			query.append(" AND p.inParticipacao IN (:polos) ");
			q = entityManager.createQuery(query.toString());
			q.setParameter("polos", Arrays.asList(polo));
		}else{
			q = entityManager.createQuery(query.toString());
		}
		q.setParameter("processo", processo);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.longValue();
	}
	
	/**
	 * Recupera o numero de partes existentes no processo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se quer a informacao
	 * @param somenteAtivas marca indicativa de que a contagem deve se limitar as partes ativas.
	 * @return o numero de partes [0] - total / [1] - sigilosas
	 */
	public long[] contagemPartesComSigilosas(ProcessoTrf processo, boolean somenteAtivas, ProcessoParteParticipacaoEnum... polo) {
		StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT p.idProcessoParte), SUM(CASE WHEN p.parteSigilosa = TRUE THEN 1 ELSE 0 END)"
				+ " FROM ProcessoParte AS p "
				+ " WHERE p.processoTrf = :processo AND p.partePrincipal = true ");
		Query q = null; 
		if(somenteAtivas){
			query.append(" AND p.inSituacao = 'A'");
		}
		if(polo != null && polo.length > 0){
			query.append(" AND p.inParticipacao IN (:polos) ");
			q = entityManager.createQuery(query.toString());
			q.setParameter("polos", Arrays.asList(polo));
		}else{
			q = entityManager.createQuery(query.toString());
		}
		q.setParameter("processo", processo);
		Object[] valores = (Object[]) q.getSingleResult();
		return new long[]{(Long)valores[0], (valores[1] == null ? 0L : (Long)valores[1])};
	}

	/**
	 * Recupera o número de partes sigilosas em um dado processo judicial e que compõem os polos dados..
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar apenas as partes ativas
	 * @return o número de partes sigilosas
	 * @see ProcessoParteSituacaoEnum.A
	 */
	public long contagemPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas, ProcessoParteParticipacaoEnum... polo){
		StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT p.idProcessoParte) FROM ProcessoParte AS p " +
				"	WHERE p.parteSigilosa = true " +
				"		AND p.processoTrf = :processo ");
		if(somenteAtivas){
			query.append(" AND p.inSituacao = 'A'");
		}
		Query q = null;
		if(polo != null && polo.length > 0){
			query.append(" AND p.inParticipacao IN (:polos) ");
			q = entityManager.createQuery(query.toString());
			q.setParameter("polos", Arrays.asList(polo));
		}else{
			q = entityManager.createQuery(query.toString());
		}
		q.setParameter("processo", processo);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.longValue();
	}
	
	/**
	 * Recupera a primeira parte principal e ativa de um dado processo no polo indicado.
	 * 
	 * @param processo o processo judicial
	 * @param polo o polo de interesse
	 * @return a primeira parte ou null, se inexistentes partes no polo
	 */
	public ProcessoParte recuperaCabeca(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo){
		String query = "SELECT p FROM ProcessoParte AS p " +
				"	WHERE p.processoTrf = :processo " +
				"		AND p.inSituacao != 'I' " +
				"		AND p.inParticipacao = :polo " +
				"		AND p.partePrincipal = true " +
				"		ORDER BY p.idProcessoParte ASC ";
		Query q = entityManager.createQuery(query);
		q.setParameter("processo", processo);
		q.setParameter("polo", polo);
		q.setMaxResults(1);
		try{
			return (ProcessoParte) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}
	}

	/**
	 * Indica se uma pessoa compõe o processo judicial como uma parte.
	 * 
	 * @param idProcessoTrf o processo judicial a respeito do qual se pretende buscar a informação
	 * @param usuario a pessoa que se pretende identificar como parte
	 * @param idProcuradoria Procuradoria atual do usuário
	 * @return true, se a pessoa for uma parte do tipo dado no processo judicial indicado
	 */
	public boolean isParte(Integer idProcessoTrf, Usuario usuario, Integer idProcuradoria){
		
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT COUNT(pp) FROM ProcessoParte AS pp ");
		hql.append("	WHERE ");
		hql.append("	pp.inSituacao = 'A' AND ");
		hql.append("	pp.processoTrf.idProcessoTrf = :idProcessoTrf AND ");
		hql.append("	(pp.pessoa.idUsuario = :idUsuario or pp.procuradoria.idProcuradoria = :idProcuradoria)");
		
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("idProcessoTrf", idProcessoTrf);
		query.setParameter("idUsuario", usuario.getIdUsuario());
		query.setParameter("idProcuradoria", idProcuradoria);
		
		Number count = (Number) query.getSingleResult();
		return count.intValue() > 0;
	}
	
	/**
	 * Método responsável por recuperar os advogados que atuam nos polos ativo e passivo do processo.
	 * 
	 * @param processoTrf Processo.
	 * @return Lista de advogados ativos que atuam no processo.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperarAdvogados(ProcessoTrf processoTrf) {
		Query query = getEntityManager().createQuery("FROM ProcessoParte o WHERE o.processoTrf = :processoTrf "
			+ "AND (o.inParticipacao = :participacaoAtivo OR o.inParticipacao = :participacaoPassivo)"
			+ "AND o.tipoParte = :tipoParte AND o.inSituacao = :situacaoAtivo ");
		
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("tipoParte", ParametroUtil.instance().getTipoParteAdvogado());
		query.setParameter("participacaoAtivo", ProcessoParteParticipacaoEnum.A);
		query.setParameter("participacaoPassivo", ProcessoParteParticipacaoEnum.P);
		query.setParameter("situacaoAtivo", ProcessoParteSituacaoEnum.A);
		
		return query.getResultList();
	}
	
	/**
	 * Recuperar as partes que sao representadas por determinado pessoa no processo.
	 * 
	 * @param representante Pessoa que representa a parte
	 * @param processoTrf ProcessoTrf
	 * @return Partes
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperarRepresentados(Pessoa representante, ProcessoTrf processoTrf) {
		StringBuilder hql = new StringBuilder();
		
		hql.append(" SELECT procParteRepresentante.processoParte FROM ProcessoParteRepresentante procParteRepresentante");
		hql.append(" JOIN procParteRepresentante.parteRepresentante parteRepresentante");
		hql.append(" JOIN parteRepresentante.processoTrf processoTrf");
		hql.append(" WHERE processoTrf = :processoTrf AND procParteRepresentante.inSituacao = :situacaoAtivo");
		hql.append(" AND parteRepresentante.pessoa = :representante");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("representante", representante);
		query.setParameter("situacaoAtivo", ProcessoParteSituacaoEnum.A);
		return query.getResultList();
	}

	/**
	 * Recuperar as Partes do Processo baseado nos parametros passados, a pessoa e tipo da parte pode ser null.
	 * 
	 * @param somentePartes True vai restringir os (Advogados, Curadores, Procuradores)
	 * @param processoTrf ProcessoTrf
	 * @param pessoa Pessoa que e representante
	 * @param tipoParte Tipo da Parte 
	 * @param tipoParticipacao Ativo, Passivo ou Todos
	 * @return ProcessoPartes
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperar(boolean somentePartes, ProcessoTrf processoTrf, Pessoa pessoa, TipoParte tipoParte,
			ProcessoParteParticipacaoEnum tipoParticipacao) {
		StringBuilder hql = new StringBuilder();
		
		hql.append(" SELECT ppa FROM ProcessoParte ppa");
		hql.append(" WHERE ppa.inParticipacao = :inParticipacao");
		hql.append(" AND ppa.inSituacao = :situacao");
		hql.append(" AND ppa.processoTrf = :processoTrf");
		if (tipoParte != null) {
			hql.append(" AND ppa.tipoParte <> :tipoParte");
		}
		if(pessoa != null){
			hql.append(" AND ppa NOT IN (");
			hql.append(" 	SELECT ppr.parteRepresentante FROM ProcessoParteRepresentante ppr");
			hql.append(" 	WHERE ppr.processoParte.pessoa = :pessoa");
			hql.append(" )");
		}
		if(somentePartes){
			hql.append(" AND ppa.tipoParte.idTipoParte NOT IN (3,7,9))");
		}
		hql.append(" ORDER BY ppa.pessoa.nome ");

		Query query = EntityUtil.getEntityManager().createQuery(hql.toString());
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("inParticipacao", tipoParticipacao);
		if (tipoParte != null) {
			query.setParameter("tipoParte", tipoParte);
		}
		if(pessoa != null){
			query.setParameter("pessoa", pessoa);
		}
		query.setParameter("situacao", ProcessoParteSituacaoEnum.A);
		
		return query.getResultList();
	}

	/**
	 * Recupera uma lista das partes de um dado processo judicial a ser usada em situações de leitura (detached).
	 * 
	 * @param processo o processo judicial
	 * @param excluiInativas marca indicativa de que se pretende excluir as partes inativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperaPartesParaExibicao(Integer idProcesso, boolean excluiInativas, Integer first, Integer maxResults) {
		boolean restringeInicio = (first != null && first.intValue() > 0);
		boolean restringeMaximo = (maxResults != null && maxResults.intValue() > 0);
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT pp.id_processo_parte, pp.in_participacao, pp.in_parte_principal, pp.id_pessoa, ul.ds_nome AS nome_pessoa, ps.in_tipo_pessoa, pf.dt_nascimento, "); 
		sql.append("       (SELECT nr_documento_identificacao ");
		sql.append("       FROM tb_pess_doc_identificacao ");
		sql.append("       WHERE id_pessoa=pp.id_pessoa ");
		sql.append("       AND cd_tp_documento_identificacao IN ('CPF','CPJ') ");
		sql.append("       AND in_usado_falsamente = false ");
		sql.append("       AND in_ativo = true ");
		sql.append("       AND in_principal = true ");
		sql.append("       LIMIT 1) AS nr_cpf_cnpj, "); 
		sql.append("       tp.id_tipo_parte, tp.ds_tipo_parte, pp.id_procuradoria, pr.ds_nome AS nome_procuradoria, pp.id_processo_trf, pp.in_situacao, pr.in_tipo_procuradoria AS tipo_procuradoria, pp.in_segredo ");
		sql.append("FROM tb_processo_parte pp ");
		sql.append("INNER JOIN tb_tipo_parte tp ON tp.id_tipo_parte = pp.id_tipo_parte ");
		sql.append("INNER JOIN tb_usuario_login ul ON ul.id_usuario = pp.id_pessoa ");
		sql.append("INNER JOIN tb_pessoa ps ON ps.id_pessoa = ul.id_usuario ");
		sql.append("LEFT JOIN tb_pessoa_fisica pf ON pf.id_pessoa_fisica = ps.id_pessoa ");
		sql.append("LEFT JOIN tb_procuradoria pr ON pr.id_procuradoria = pp.id_procuradoria ");
		sql.append("WHERE pp.id_processo_trf = :idProcesso ");

		if(excluiInativas){
			sql.append("AND pp.in_situacao in ('A','B','S') ");
		}

		sql.append("ORDER BY pp.nr_ordem");
		
		Query q = entityManager.createNativeQuery(sql.toString());
		q.setParameter("idProcesso", idProcesso);
		
		if(restringeInicio) {
			q.setFirstResult(first);
		}
		
		if(restringeMaximo) {
			q.setMaxResults(maxResults);
		}
		
		/**
		 * -------------------------------
		 * Ordem dos campos no resultList:
		 * -------------------------------
		 * 0 - id_processo_parte
		 * 1 - in_participacao
		 * 2 - in_parte_principal
		 * 3 - id_pessoa
		 * 4 - nome_pessoa
		 * 5 - in_tipo_pessoa
		 * 6 - dt_nascimento
		 * 7 - nr_cpf_cnpj
		 * 8 - id_tipo_parte
		 * 9 - ds_tipo_parte
		 * 10 - id_procuradoria
		 * 11 - nome_procuradoria
		 * 12 - id_processo_trf
		 * 13 - in_situacao (parte)
		 * 14 - tipo_procuradoria (P - Procuradoria/D - Defensoria)
		 * 15 - in_segredo
		 */
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes: resultList) {
			ProcessoParte pp = new ProcessoParte();
			ProcessoTrf ptf = new ProcessoTrf();
			PessoaDocumentoIdentificacao pdi = new PessoaDocumentoIdentificacao();
			TipoParte tp = new TipoParte();
			Procuradoria pro = null;

			Pessoa pes = new Pessoa();
			Character inTipoPessoa = (Character)borderTypes[5];
			
			if(inTipoPessoa == 'F') {
				pes = new PessoaFisica();
				pes.setInTipoPessoa(TipoPessoaEnum.F);
				Date dataNascimento = (Date)borderTypes[6];
				((PessoaFisica) pes).setDataNascimento(dataNascimento);
			}
			else if(inTipoPessoa == 'J') {
				pes = new PessoaJuridica();
				pes.setInTipoPessoa(TipoPessoaEnum.J);
			}
			else {
				pes = new PessoaAutoridade();
				pes.setInTipoPessoa(TipoPessoaEnum.A);
			}
			
			pp.setIdProcessoParte(((Integer)borderTypes[0]));
			Character participacao = (Character)borderTypes[1];
			pp.setInParticipacao(ProcessoParteParticipacaoEnum.valueOf(participacao.toString()));
			pp.setPartePrincipal(((Boolean)borderTypes[2]));
			Character situacao = (Character)borderTypes[13];
			pp.setInSituacao(ProcessoParteSituacaoEnum.valueOf(situacao.toString()));
			pes.setIdPessoa(((Integer)borderTypes[3]));
			pes.setIdUsuario(((Integer)borderTypes[3]));
			pes.setNome(((String)borderTypes[4]));
			pdi.setNumeroDocumento(((String)borderTypes[7]));
			tp.setIdTipoParte(((Integer)borderTypes[8]));
			tp.setTipoParte(((String)borderTypes[9]));
			if(borderTypes[10] != null) {
				pro = new Procuradoria();
				pro.setIdProcuradoria((Integer)borderTypes[10]);
				pro.setNome(((String)borderTypes[11]));
				pro.setTipo(TipoProcuradoriaEnum.valueOf(String.valueOf(borderTypes[14])));
			}
			ptf.setIdProcessoTrf(((Integer)borderTypes[12]));
			pp.setParteSigilosa((Boolean) borderTypes[15]);

			if(pdi.getNumeroDocumento() != null) {
				if(InscricaoMFUtil.retiraMascara(pdi.getNumeroDocumento()).length() > 11) {
					TipoDocumentoIdentificacao cnpj = new TipoDocumentoIdentificacao();
					cnpj.setTipoPessoa(pes.getInTipoPessoa());
					cnpj.setCodTipo("CPJ");
					pdi.setTipoDocumento(cnpj);
					if(pes.getInTipoPessoa().equals(TipoPessoaEnum.J)) {
						((PessoaJuridica) pes).setNumeroCNPJ(pdi.getNumeroDocumento());
					}
				}
				else {
					TipoDocumentoIdentificacao cpf = new TipoDocumentoIdentificacao();
					cpf.setTipoPessoa(pes.getInTipoPessoa());
					cpf.setCodTipo("CPF");
					pdi.setTipoDocumento(cpf);
					if(pes.getInTipoPessoa().equals(TipoPessoaEnum.F)) {
						((PessoaFisica) pes).setNumeroCPF(pdi.getNumeroDocumento());
					}
				}
			}
			
			pes.getPessoaDocumentoIdentificacaoList().add(pdi);
			pp.setPessoa(pes);
			pp.setTipoParte(tp);
			pp.setProcessoTrf(ptf);
			pp.setProcuradoria(pro);
			
			processoParteList.add(pp);
			
		}

		return processoParteList;
	}
	
	public List<ProcessoParte> recuperaPartesParaExibicao(Integer idProcesso, boolean somenteAtivas, Integer first, Integer maxResults, Boolean somentePartePrincipal) {
		return this.recuperaPartesParaExibicao(idProcesso, somenteAtivas, first, maxResults, null, somentePartePrincipal);
	}

	/**
	 * Método responsável por verificar se o assistente de advogado faz parte do
	 * processo.
	 * 
	 * @param idPapelAdvogado
	 *            id papel advogado
	 * @param idProcesso
	 *            id processo
	 * @param idTipoParte
	 *            id tipo parte advogado
	 * @param idUsuarioLocalizacao
	 *            id usuário localização
	 * 
	 * @return <code>Boolean</code>, <code>true</code> caso faça parte.
	 */
	public boolean isAssistenteAdvogadoProcesso(Integer idPapelAdvogado, Integer idProcesso, Integer idTipoParte, Integer idUsuarioLocalizacao) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COUNT(1) FROM tb_processo_parte AS pp ");
		sb.append(" INNER JOIN tb_usuario_localizacao AS ul ON ul.id_usuario = pp.id_pessoa AND ul.id_papel = :idPapelAdvogado ");
		sb.append(" WHERE pp.id_processo_trf = :idProcesso ");
		sb.append("  AND pp.id_tipo_parte = :idTipoParte ");
		sb.append("  AND ul.id_localizacao_fisica = (SELECT id_localizacao_fisica FROM tb_usuario_localizacao WHERE id_usuario_localizacao = :idUsuarioLocalizacao)");

		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("idPapelAdvogado", idPapelAdvogado);
		query.setParameter("idProcesso", idProcesso);
		query.setParameter("idTipoParte", idTipoParte);
		query.setParameter("idUsuarioLocalizacao", idUsuarioLocalizacao);

		Number count = (Number) query.getSingleResult();
		return (count.intValue() > 0);
	}
	
	public ProcessoParteMin findProcessoParteMinById(Long idProcessoParte){
		StringBuilder hql = new StringBuilder();
		hql.append(" SELECT pp from ProcessoParteMin pp ");
		hql.append(" WHERE pp.id = :idProcessoParte");
			
		Query query = getEntityManager().createQuery(hql.toString());
		
		query.setParameter("idProcessoParte", idProcessoParte);
		
		try {
			return (ProcessoParteMin) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		
	}
	
	/**
	 * Recupera uma lista das partes de um dado processo judicial a ser usada em situações de leitura (detached).
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperaPartesParaExibicao(Integer idProcesso, boolean somenteAtivas, Integer first, Integer maxResults, ProcessoParteParticipacaoEnum tipoParticipacao, Boolean somentePartePrincipal) {
		boolean restringeInicio = (first != null && first.intValue() > 0);
		boolean restringeMaximo = (maxResults != null && maxResults.intValue() > 0);
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT pp.id_processo_parte, pp.in_participacao, pp.in_parte_principal, pp.id_pessoa, ul.ds_nome AS nome_pessoa, ps.in_tipo_pessoa, pf.dt_nascimento, "); 
		sql.append("       (SELECT nr_documento_identificacao ");
		sql.append("       FROM tb_pess_doc_identificacao ");
		sql.append("       WHERE id_pessoa=pp.id_pessoa ");
		sql.append("       AND cd_tp_documento_identificacao IN ('CPF','CPJ') ");
		sql.append("       AND in_usado_falsamente = false ");
		sql.append("       AND in_ativo = true ");
		sql.append("       AND in_principal = true ");
		sql.append("       LIMIT 1) AS nr_cpf_cnpj, "); 
		sql.append("       tp.id_tipo_parte, tp.ds_tipo_parte, pp.id_procuradoria, pr.ds_nome AS nome_procuradoria, pp.id_processo_trf, pp.in_situacao, pr.in_tipo_procuradoria AS tipo_procuradoria, pf.ds_nome_social, pp.in_segredo, pna.ds_pessoa_nome_alternativo ");
		sql.append("FROM tb_processo_parte pp ");
		sql.append("INNER JOIN tb_tipo_parte tp ON tp.id_tipo_parte = pp.id_tipo_parte ");
		sql.append("INNER JOIN tb_usuario_login ul ON ul.id_usuario = pp.id_pessoa ");
		sql.append("INNER JOIN tb_pessoa ps ON ps.id_pessoa = ul.id_usuario ");
		sql.append("LEFT JOIN tb_pessoa_fisica pf ON pf.id_pessoa_fisica = ps.id_pessoa ");
		sql.append("LEFT JOIN tb_procuradoria pr ON pr.id_procuradoria = pp.id_procuradoria ");
		sql.append("LEFT JOIN tb_pessoa_nome_alternativo pna ON pna.id_pessoa_nome_alternativo = pp.id_pessoa_nome_alternativo ");
		sql.append("WHERE pp.id_processo_trf = :idProcesso ");

		if(somenteAtivas){
			sql.append("AND pp.in_situacao in ('A','B','S') ");
		}
		
		if(tipoParticipacao != null){
			sql.append("AND pp.in_participacao = :participacao ");
		}

		if(somentePartePrincipal != null && somentePartePrincipal) {
			sql.append("AND pp.in_parte_principal = true ");
		}
		
		sql.append("ORDER BY pp.id_processo_parte");
		
		Query q = entityManager.createNativeQuery(sql.toString());
		q.setParameter("idProcesso", idProcesso);
		
		if(tipoParticipacao != null) {
			q.setParameter("participacao", tipoParticipacao.name());
		}
		
		if(restringeInicio) {
			q.setFirstResult(first);
		}
		
		if(restringeMaximo) {
			q.setMaxResults(maxResults);
		}
		
		/**
		 * -------------------------------
		 * Ordem dos campos no resultList:
		 * -------------------------------
		 * 0 - id_processo_parte
		 * 1 - in_participacao
		 * 2 - in_parte_principal
		 * 3 - id_pessoa
		 * 4 - nome_pessoa
		 * 5 - in_tipo_pessoa
		 * 6 - dt_nascimento
		 * 7 - nr_cpf_cnpj
		 * 8 - id_tipo_parte
		 * 9 - ds_tipo_parte
		 * 10 - id_procuradoria
		 * 11 - nome_procuradoria
		 * 12 - id_processo_trf
		 * 13 - in_situacao (parte)
		 * 14 - tipo_procuradoria (P - Procuradoria/D - Defensoria)
		 * 15 - ds_pessoa_nome_alternativo
		 * 16 - in_segredo
		 * 17 - ds_pessoa_nome_alternativo
		 */
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes: resultList) {
			ProcessoParte pp = new ProcessoParte();
			ProcessoTrf ptf = new ProcessoTrf();
			PessoaDocumentoIdentificacao pdi = new PessoaDocumentoIdentificacao();
			TipoParte tp = new TipoParte();
			Procuradoria pro = null;

			Pessoa pes = new Pessoa();
			Character inTipoPessoa = (Character)borderTypes[5];
			
			if(inTipoPessoa == 'F') {
				pes = new PessoaFisica();
				pes.setInTipoPessoa(TipoPessoaEnum.F);
				Date dataNascimento = (Date)borderTypes[6];
				((PessoaFisica) pes).setDataNascimento(dataNascimento);
				String nomeSocial = (String)borderTypes[15];
				((PessoaFisica) pes).setNomeSocial(nomeSocial);
			}
			else if(inTipoPessoa == 'J') {
				pes = new PessoaJuridica();
				pes.setInTipoPessoa(TipoPessoaEnum.J);
			}
			else {
				pes = new PessoaAutoridade();
				pes.setInTipoPessoa(TipoPessoaEnum.A);
			}
			
			pp.setIdProcessoParte(((Integer)borderTypes[0]));
			Character participacao = (Character)borderTypes[1];
			pp.setInParticipacao(ProcessoParteParticipacaoEnum.valueOf(participacao.toString()));
			pp.setPartePrincipal(((Boolean)borderTypes[2]));
			Character situacao = (Character)borderTypes[13];
			pp.setInSituacao(ProcessoParteSituacaoEnum.valueOf(situacao.toString()));
			pes.setIdPessoa(((Integer)borderTypes[3]));
			pes.setIdUsuario(((Integer)borderTypes[3]));
			pes.setNome(((String)borderTypes[4]));
			pdi.setNumeroDocumento(((String)borderTypes[7]));
			tp.setIdTipoParte(((Integer)borderTypes[8]));
			tp.setTipoParte(((String)borderTypes[9]));
			if(borderTypes[10] != null) {
				pro = new Procuradoria();
				pro.setIdProcuradoria((Integer)borderTypes[10]);
				pro.setNome(((String)borderTypes[11]));
				pro.setTipo(TipoProcuradoriaEnum.valueOf(String.valueOf(borderTypes[14])));
			}
			ptf.setIdProcessoTrf(((Integer)borderTypes[12]));
			pp.setParteSigilosa((Boolean) borderTypes[16]);

			if(pdi.getNumeroDocumento() != null) {
				if(InscricaoMFUtil.retiraMascara(pdi.getNumeroDocumento()).length() > 11) {
					TipoDocumentoIdentificacao cnpj = new TipoDocumentoIdentificacao();
					cnpj.setTipoPessoa(pes.getInTipoPessoa());
					cnpj.setCodTipo("CPJ");
					pdi.setTipoDocumento(cnpj);
					if(pes.getInTipoPessoa().equals(TipoPessoaEnum.J)) {
						((PessoaJuridica) pes).setNumeroCNPJ(pdi.getNumeroDocumento());
					}
				}
				else {
					TipoDocumentoIdentificacao cpf = new TipoDocumentoIdentificacao();
					cpf.setTipoPessoa(pes.getInTipoPessoa());
					cpf.setCodTipo("CPF");
					pdi.setTipoDocumento(cpf);
					if(pes.getInTipoPessoa().equals(TipoPessoaEnum.F)) {
						((PessoaFisica) pes).setNumeroCPF(pdi.getNumeroDocumento());
					}
				}
			}
			
			pes.getPessoaDocumentoIdentificacaoList().add(pdi);
			pp.setPessoa(pes);
			pp.setTipoParte(tp);
			pp.setProcessoTrf(ptf);
			pp.setProcuradoria(pro);
			
			if(borderTypes[17] != null) {
				PessoaNomeAlternativo pessoaNomeAlternativo = new PessoaNomeAlternativo();
				pessoaNomeAlternativo.setPessoaNomeAlternativo((String) borderTypes[17]);
				pp.setPessoaNomeAlternativo(pessoaNomeAlternativo);
			}
			processoParteList.add(pp);
			
		}

		return processoParteList;
	}
	
	public boolean possuiParteInativa(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum inParticipacao) {
		StringBuilder jpql = new StringBuilder("SELECT COUNT(*) FROM tb_processo_parte ")
				.append("WHERE id_processo_trf = :idProcessoTrf ")
				.append("AND in_participacao = :inParticipacao ")
				.append("AND in_situacao != :inSituacao");
		
		Query query = entityManager.createNativeQuery(jpql.toString())
				.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf())
				.setParameter("inParticipacao", inParticipacao.toString())
				.setParameter("inSituacao", ProcessoParteSituacaoEnum.A.toString());
		
		return ((BigInteger) query.getSingleResult()).intValue() > 0;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> procurarAdvogadosProcessoParte(Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal) {
		StringBuilder sb = new StringBuilder(0);
		sb.append( " SELECT distinct ul.ds_nome as nome_advogado, ppr.id_representante, pp.in_participacao, pp.in_situacao, adv.nr_oab, est.cd_estado, doc.nr_documento_identificacao, pp.in_endereco_desconhecido, ");
		sb.append( " ende.id_endereco, ulparterepresentada.ds_nome as nome_parte  FROM client.tb_proc_parte_represntante ppr ");
		sb.append(" INNER 	JOIN 	client.tb_processo_parte 					parterepresentada 		ON ppr.id_processo_parte = parterepresentada.id_processo_parte ");
		sb.append(" INNER 	JOIN 	acl.tb_usuario_login 				ulparterepresentada 		ON parterepresentada.id_pessoa 			= ulparterepresentada.id_usuario ");
		sb.append(" INNER 	JOIN 	client.tb_processo_parte 					pp 		ON ppr.id_parte_representante = pp.id_processo_parte ");
		sb.append(" INNER 	JOIN 	acl.tb_usuario_login 				ul 		ON pp.id_pessoa 			= ul.id_usuario ");
		sb.append(" INNER 	JOIN 	client.tb_pessoa_advogado 			adv 	ON adv.id 					= ul.id_usuario ");
		sb.append(" INNER	JOIN 	core.tb_estado 						est 	ON est.id_estado 			= adv.id_uf_oab ");
		sb.append(" INNER 	JOIN 	client.tb_pess_doc_identificacao 	doc 	ON doc.id_pessoa 			= ul.id_usuario AND doc.in_ativo = 't' AND doc.cd_tp_documento_identificacao = 'CPF' ");
		sb.append(" LEFT 	JOIN   	client.tb_processo_parte_endereco 	ende 	ON ende.id_processo_parte 	= pp.id_processo_parte ");
		sb.append(" INNER 	JOIN	client.tb_processo_Trf p on pp.id_processo_trf = p.id_processo_trf " );
		sb.append( " WHERE p.cd_processo_status = 'D' and p.in_segredo_justica = 'f' and p.nr_sequencia =  :numeroSequencia ");
		sb.append( " AND p.nr_digito_verificador =  :digitoVerificador ");
		sb.append( " AND p.nr_ano =  :ano ");
		sb.append( " AND p.nr_origem_processo =  :numeroOrigem ");
		sb.append( " AND p.nr_identificacao_orgao_justica =  :orgaoJustica ");
		sb.append( " AND pp.id_tipo_parte = :tipoParte "); 
		sb.append( " AND pp.in_participacao in ('A', 'P')");
		sb.append( " ORDER BY nome_parte ");
		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("numeroSequencia", numeroSequencia);
		query.setParameter("digitoVerificador", digitoVerificador);
		query.setParameter("ano", ano);
		query.setParameter("numeroOrigem", numeroOrigem);
		query.setParameter("orgaoJustica", Integer.parseInt(ramoJustica + respectivoTribunal));
		query.setParameter("tipoParte", ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte());
		return query.getResultList();	
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> procurarAdvogadosProcessoParte(Integer idPessoa) {
		StringBuilder sb = new StringBuilder(0);
		sb.append( " SELECT distinct ul.ds_nome as nome_advogado, ppr.id_representante, pp.in_participacao, pp.in_situacao, adv.nr_oab, est.cd_estado, doc.nr_documento_identificacao, pp.in_endereco_desconhecido, ");
		sb.append( " ende.id_endereco, ulparterepresentada.ds_nome as nome_parte  FROM client.tb_proc_parte_represntante ppr ");
		sb.append(" INNER 	JOIN 	client.tb_processo_parte 					parterepresentada 		ON ppr.id_processo_parte = parterepresentada.id_processo_parte ");
		sb.append(" INNER 	JOIN 	acl.tb_usuario_login 				ulparterepresentada 		ON parterepresentada.id_pessoa 			= ulparterepresentada.id_usuario ");
		sb.append(" INNER 	JOIN 	client.tb_processo_parte 					pp 		ON ppr.id_parte_representante = pp.id_processo_parte ");
		sb.append(" INNER 	JOIN 	acl.tb_usuario_login 				ul 		ON pp.id_pessoa 			= ul.id_usuario ");
		sb.append(" INNER 	JOIN 	client.tb_pessoa_advogado 			adv 	ON adv.id 					= ul.id_usuario ");
		sb.append(" INNER	JOIN 	core.tb_estado 						est 	ON est.id_estado 			= adv.id_uf_oab ");
		sb.append(" INNER 	JOIN 	client.tb_pess_doc_identificacao 	doc 	ON doc.id_pessoa 			= ul.id_usuario AND doc.in_ativo = 't' AND doc.cd_tp_documento_identificacao = 'CPF' ");
		sb.append(" LEFT 	JOIN   client.tb_processo_parte_endereco 	ende 	ON ende.id_processo_parte 	= pp.id_processo_parte ");
		sb.append(" WHERE  pp.id_tipo_parte = :tipoParte " );
		sb.append(" and ulparterepresentada.id_usuario = :idPessoa ");
		sb.append(" AND pp.in_participacao in ('A', 'P') ");
		sb.append( " ORDER BY nome_parte ");
		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("tipoParte", ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte());
		query.setParameter("idPessoa", idPessoa);
		return query.getResultList();		
	}
}
