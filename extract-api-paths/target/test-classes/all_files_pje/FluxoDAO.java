/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaDTO;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * @author cristof
 *
 */
@Name(FluxoDAO.NAME)
public class FluxoDAO extends BaseDAO<Fluxo> {
	
	private static final Map<String, List<String>> mapaTarefas = new HashMap<String, List<String>>();
	public static final String NAME = "fluxoDAO";

	@Override
	public Integer getId(Fluxo e) {
		return e.getIdFluxo();
	}

	public void changeFluxoName(String oldName, String newName) {
		String q = 	"UPDATE jbpm_processdefinition SET name_ = ? " +
				"WHERE id_ = 	(SELECT MAX(id_) FROM jbpm_processdefinition WHERE name_ like ?)";
		Query query = EntityUtil.createNativeQuery(entityManager, q, "jbpm_processdefinition");
		query.setParameter(1, newName);
		query.setParameter(2, oldName);
		query.executeUpdate();
	}

	public Fluxo findByCodigo(String codigo) {
		String q = "SELECT f FROM Fluxo AS f WHERE f.codFluxo = :codigo";
		Query query = entityManager.createQuery(q).setParameter("codigo", codigo);
		Fluxo ret = null;
		try {
			ret = (Fluxo) query.getSingleResult();
		} catch (NonUniqueResultException e) {
			e.printStackTrace();
		} catch (NoResultException e) {
			logger.warn("Não há resultado para Fluxo com codigo = [{0}]", codigo);
		}
		return ret;
	}
	
	/**
	 * Verifica se existe algum fluxo processual em curso para um processo dado que tenha a variável 
	 * informada com valor não nulo.
	 * 
	 * @param processoJudicial o processo sob análise
	 * @param nomeVariavel o nome da variável a verificar
	 * @return true, se houver ao menos um fluxo ativo com a variável informada definida
	 * @throws PJeBusinessException
	 */
	public boolean existeFluxoComVariavel(Integer idProcesso, String nomeVariavel) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(pi0.id_) from jbpm_processinstance pi0 inner join jbpm_variableinstance vi0 on vi0.processinstance_ = pi0.id_ ");
		sql.append("		inner join ( ");
		sql.append("			select situacaopr0_.id_process_instance ");
		sql.append("			from tb_processo_tarefa situacaopr0_, tb_processo_trf processotr1_ ");
		sql.append("			where  ");
		sql.append("				situacaopr0_.id_processo_trf=processotr1_.id_processo_trf and "); 
		sql.append("				(processotr1_.id_processo_trf = :idProcesso) ");
		sql.append("		) tb on pi0.id_ = tb.id_process_instance ");
		sql.append("	where  ");
		sql.append("		pi0.end_ is null and ");
		sql.append("		vi0.name_ = :nomeVariavel ");
				
		Query query = entityManager.createNativeQuery(sql.toString());
		query.setParameter("nomeVariavel", nomeVariavel);
		query.setParameter("idProcesso", idProcesso);
		Number cont = (Number) query.getSingleResult();
		return cont.longValue() > 0;
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> recuperaFluxosComVariavel(Integer idProcesso, String nomeVariavel){
		String query = 	"select distinct procins.id_ " + 
				"from tb_processo_instance proc " + 
				"inner join jbpm_processinstance procins on proc.id_proc_inst = procins.id_ " + 
				"inner join jbpm_variableinstance varins on varins.processinstance_ = procins.id_ " + 
				"where varins.name_ = :nomeVariavel " + 
				"and proc.id_processo = :idProcesso " +
				"and proc.in_ativo = true"; 
		Query q = entityManager.createNativeQuery(query);
		q.setParameter("nomeVariavel", nomeVariavel);
		q.setParameter("idProcesso", idProcesso);
		List<Number> rec = (List<Number>) q.getResultList();
		Set<Long> ids = new HashSet<Long>();
		for(Number n: rec){
			ids.add(n.longValue());
		}
		return new ArrayList<Long>(ids);
	}

	/**
	 * Verifica se o processo está, ou esteve, em um fluxo com o nome especificado
	 * @param idProcesso Identificador do processo
	 * @param nomeFluxo Nome do fluxo
	 * @return true caso o processo exista no fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxo(Integer idProcesso, String nomeFluxo) {
		String sql = "select count(*)" +
				     "  from jbpm_processinstance as pi" +
				     "       join jbpm_processdefinition as pd on (pi.processdefinition_ = pd.id_)" +
				     "       join jbpm_variableinstance as vi on (pi.id_ = vi.processinstance_)" +
				     " where vi.name_ = 'processo'" +
				     "   and vi.longvalue_ = :idProcesso" +
				     "   and pd.name_ = :nomeFluxo";
		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("idProcesso", idProcesso);
		query.setParameter("nomeFluxo", nomeFluxo);
		Number cont = (Number) query.getSingleResult();
		return cont.longValue() > 0;
	}
	
	/**
	 * Verifica se o processo está, ou esteve, em um fluxo com o nome especificado
	 * e se o mesmo está em execução.
	 * 
	 * @param idProcesso Identificador do processo
	 * @param nomeFluxo Nome do fluxo
	 * @param idLocalizacao Identificador da localizacao que pode ter o fluxo
	 * @return true caso o processo exista no fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxoEmExecucao(Integer idProcesso, List<Integer> idsLocalizacoes, String nomeFluxo) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) ")
			.append(" from client.tb_processo_tarefa ptar ")
			.append(" WHERE ptar.id_processo_trf = :idProcesso")
			.append(" AND ptar.nm_fluxo = :nomefluxo ");
		if(CollectionUtilsPje.isNotEmpty(idsLocalizacoes)) {
			sql.append(" AND ptar.id_localizacao IN (:idsLocalizacoes)");
		}
						
		Query query = entityManager.createNativeQuery(sql.toString());
		query.setParameter("idProcesso", idProcesso);
		query.setParameter("nomefluxo", nomeFluxo);
		if(CollectionUtilsPje.isNotEmpty(idsLocalizacoes)) {
			query.setParameter("idsLocalizacoes", idsLocalizacoes);
		}
		Number cont = (Number) query.getSingleResult();
		return cont.longValue() > 0;
	}
	
	/**
	 * Verifica se o processo está, ou esteve, em um fluxo com o nome especificado
	 * e se o mesmo está em execução.
	 * 
	 * @param idProcesso Identificador do processo
	 * @param nomeFluxo Nome do fluxo
	 * @return true caso o processo exista no fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxoEmExecucao(Integer idProcesso, String nomeFluxo) {
		return this.existeProcessoNoFluxoEmExecucao(idProcesso, null, nomeFluxo);
	}
	
	public void iniciarFluxoProcesso(Processo processo, Fluxo fluxo, Map<String, Object> parametros) {
		BusinessProcess.instance().createProcess(fluxo.getFluxo(), false);

		if(parametros != null && !parametros.isEmpty()) {
			for(Entry<String, Object> entry: parametros.entrySet()) {
				ProcessInstance.instance().getContextInstance().setVariable(entry.getKey(), entry.getValue());
			}
		}
		ProcessInstance.instance().getContextInstance().setVariable(Variaveis.VARIAVEL_PROCESSO, processo.getIdProcesso());
		
		ProcessoInstance processoInstance = new ProcessoInstance();
		processoInstance.setIdProcesso(processo.getIdProcesso());
		processoInstance.setIdProcessoInstance(BusinessProcess.instance().getProcessId());

		entityManager.persist(processoInstance);
		entityManager.flush();
		
		ProcessInstance.instance().signal();

		// Codigo replicado da ProcessoHome.iniciarProcesso
		Collection<org.jbpm.taskmgmt.exe.TaskInstance> taskInstances = ProcessInstance.instance().getTaskMgmtInstance().getTaskInstances();
		if (taskInstances != null && !taskInstances.isEmpty()) {
			BusinessProcess.instance().setTaskId(Long.valueOf(taskInstances.iterator().next().getId()));
			BusinessProcess.instance().startTask();
		}

		SwimlaneInstance swimlaneInstance = TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		Set<String> pooledActors = LocalizacaoAssignment.instance().getPooledActors(actorsExpression);
		String[] actorIds = pooledActors.toArray(new String[pooledActors.size()]);
		swimlaneInstance.setPooledActors(actorIds);
	}
	
	/**
	 * Verifica se o processo está em um fluxo ativo com o sigla especificado
	 * @param idProcesso Identificador do processo
	 * @param siglaFluxo Sigla do fluxo
	 * @param idOrgaoJulgador Identificador do Orgao Julgador
	 * @return true caso o processo não estja com uma instancia ativa do fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxoSigla(Integer idProcesso, String siglaFluxo, String idsLocalizacoes) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(*)");
		sql.append("		FROM jbpm_processdefinition  flx");
		sql.append(" 			JOIN jbpm_processinstance pi ON (pi.processdefinition_ = flx.id_)");
		sql.append("			JOIN tb_processo_instance proc ON proc.id_proc_inst = pi.id_");
		sql.append("			JOIN jbpm_task t ON (flx.id_ = t.processdefinition_)");
		sql.append("			JOIN jbpm_taskinstance ti ON (ti.task_ = t.id_ AND ti.procinst_ = pi.id_)");
		sql.append("			JOIN tb_tarefa_jbpm tj ON (tj.id_jbpm_task = t.id_)");
		sql.append("			JOIN tb_tarefa tt ON (tt.id_tarefa = tj.id_tarefa)");
		sql.append("			JOIN tb_fluxo tfl ON (tfl.id_fluxo = tt.id_fluxo)");
		sql.append("	WHERE 1=1");
		sql.append("		AND (pi.start_ IS NOT NULL AND pi.end_ IS NULL AND pi.issuspended_ = 'f')");
		sql.append("		AND (ti.start_ IS NOT NULL AND ti.isopen_ = 't')");
		sql.append("		AND proc.id_processo = :idProcesso ");
		sql.append("		AND proc.id_localizacao IN (:idsLocalizacoes) ");
		sql.append("		AND tfl.cd_fluxo = :siglaFluxo");
		
		Query query = entityManager.createNativeQuery(sql.toString());
		
		query.setParameter("idProcesso", idProcesso);
		query.setParameter("siglaFluxo", siglaFluxo);
		query.setParameter("idsLocalizacoes", idsLocalizacoes);
		
		Number cont = (Number) query.getSingleResult();
		
		return cont.longValue() > 0;
	}


	/**
	 * Com a solução dada pelo uso correto da configuração de localizações, a solução que utilizava o parâmetro 
	 * "papelNaoFiltravel" foi descontinuada - o tribunal deve configurar seus fluxos e servidores na árvore
	 * 	de localizações corretamente do tribunal
	 * 
	 * @param idOrgaoJulgador
	 * @param idOrgaoJulgadorColegiado
	 * @param idsOrgaoJulgadorCargo
	 * @param idUsuario
	 * @param idLocalizacaoFisica
	 * @param idLocalizacaoModelo
	 * @param idPapel
	 * @param visualizaSigiloso
	 * @param somenteFavoritas
	 * @param numeroProcesso
	 * @param competencia
	 * @param etiquetasList
	 * @param cargoAuxiliar
	 * @param papelNaoFiltravel
	 * @return
	 */
	@Deprecated
	public Map<String, Long> carregarListaTarefasUsuario(
        Integer idOrgaoJulgador,
        Integer idOrgaoJulgadorColegiado,
        List<Integer> idsOrgaoJulgadorCargo,
        Integer idUsuario,
        Integer idLocalizacaoFisica,
        Integer idLocalizacaoModelo,
        Integer idPapel,
        Boolean visualizaSigiloso,
        Integer nivelAcessoSigilo,
        Boolean somenteFavoritas,
        String numeroProcesso,
        String competencia,
        List<String> etiquetasList,
		Boolean cargoAuxiliar,
		Boolean papelNaoFiltravel) {
		
		List<Integer> idsLocalizacoesFisicasList = new ArrayList<>();
		idsLocalizacoesFisicasList.add(idLocalizacaoFisica);

		boolean isServidorExclusivoOJC = (idOrgaoJulgador == null && idOrgaoJulgadorColegiado != null);
		
    	return this.carregarListaTarefasUsuario(idOrgaoJulgadorColegiado, isServidorExclusivoOJC, idsOrgaoJulgadorCargo, 
    			idUsuario, idsLocalizacoesFisicasList, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, 
    			visualizaSigiloso, nivelAcessoSigilo, somenteFavoritas, numeroProcesso, competencia, etiquetasList, cargoAuxiliar);
    }
	
	@Deprecated
	public Map<String, Long> carregarListaTarefasUsuario(
	        Integer idOrgaoJulgador,
	        Integer idOrgaoJulgadorColegiado,
	        List<Integer> idsOrgaoJulgadorCargo,
	        Integer idUsuario,
	        Integer idLocalizacaoFisica,
	        Integer idLocalizacaoModelo,
	        Integer idPapel,
	        Boolean visualizaSigiloso,
	        Integer nivelAcessoSigilo,
	        Boolean somenteFavoritas,
	        String numeroProcesso,
	        String competencia,
	        List<String> etiquetasList,
			Boolean cargoAuxiliar) {

		List<Integer> idsLocalizacoesFisicasList = new ArrayList<>();
		idsLocalizacoesFisicasList.add(idLocalizacaoFisica);
		
		boolean isServidorExclusivoOJC = (idOrgaoJulgador == null && idOrgaoJulgadorColegiado != null);

		return this.carregarListaTarefasUsuario(idOrgaoJulgadorColegiado, isServidorExclusivoOJC, idsOrgaoJulgadorCargo, 
				idUsuario, idsLocalizacoesFisicasList, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, 
				visualizaSigiloso, nivelAcessoSigilo, somenteFavoritas, numeroProcesso, competencia, etiquetasList, cargoAuxiliar);
	}
    
	/**
	 * 
	 * @param idOrgaoJulgadorColegiado
	 * @param idsOrgaoJulgadorCargo
	 * @param idUsuario
	 * @param idsLocalizacoesFisicas
	 * @param idLocalizacaoFisica
	 * @param idLocalizacaoModelo
	 * @param idPapel
	 * @param visualizaSigiloso
	 * @param somenteFavoritas
	 * @param numeroProcesso
	 * @param competencia
	 * @param etiquetasList
	 * @param cargoAuxiliar
	 * @return
	 */
    @SuppressWarnings("unchecked")
	public Map<String, Long> carregarListaTarefasUsuario(
        Integer idOrgaoJulgadorColegiado,
        boolean isServidorExclusivoOJC,
        List<Integer> idsOrgaoJulgadorCargo,
        Integer idUsuario,
        List<Integer> idsLocalizacoesFisicasList,
        Integer idLocalizacaoFisica,
        Integer idLocalizacaoModelo,
        Integer idPapel,
        Boolean visualizaSigiloso,
        Integer nivelAcessoSigilo,
        Boolean somenteFavoritas,
        String numeroProcesso,
        String competencia,
        List<String> etiquetasList,
		Boolean cargoAuxiliar) {
    	
    	if(CollectionUtilsPje.isEmpty(idsLocalizacoesFisicasList)){
    		if(idLocalizacaoFisica != null && idLocalizacaoFisica > 0) {
    			idsLocalizacoesFisicasList.add(idLocalizacaoFisica);
    		}else {
    			idsLocalizacoesFisicasList.add(-1);
    		}
    	}

        StringBuilder sb = new StringBuilder();
        
        Map<String, Object> params = new HashMap<String,Object>(0);
        
		sb.append("SELECT ptar.nm_tarefa, count(0), max(ptar.id_task) ");
		sb.append("FROM tb_processo_tarefa ptar ");
		sb.append("INNER JOIN tb_processo_trf proctrf ON proctrf.id_processo_trf = ptar.id_processo_trf ");

        if(StringUtil.isNotEmpty(numeroProcesso)){
			sb.append("INNER JOIN core.tb_processo p ON proctrf.id_processo_trf= p.id_processo ");
		}

        if (StringUtil.isNotEmpty(competencia)) {
        	sb.append("INNER JOIN client.tb_competencia comp ON proctrf.id_competencia = comp.id_competencia ");
        }

		sb.append("WHERE EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("		         WHERE tl.id_processo = proctrf.id_processo_trf ");
		sb.append("              AND tl.id_task_jbpm = ptar.id_task ");
		sb.append("              AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("              AND tl.id_papel = :idPapel) ");
		
		if(!params.containsKey("idLocalizacaoModelo")) {
			params.put("idLocalizacaoModelo", idLocalizacaoModelo);
		}

		if(!params.containsKey("idPapel")) {
			params.put("idPapel", idPapel);
		}

		if(StringUtil.isNotEmpty(numeroProcesso)) {
			if (NumeroProcessoUtil.numeroProcessoValido(numeroProcesso)) {
				sb.append("AND regexp_replace(p.nr_processo, '\\D', '', 'g') = :numeroProcesso ");
			} else {				
				sb.append("AND regexp_replace(p.nr_processo, '\\D', '', 'g') LIKE '%' || :numeroProcesso || '%' ");
			}

			if(!params.containsKey("numeroProcesso")) {
				params.put("numeroProcesso", NumeroProcessoUtil.retiraMascaraNumeroProcesso(numeroProcesso));
			}
		}
		
		if (StringUtil.isNotEmpty(competencia)) {
			sb.append(" AND comp.ds_competencia ILIKE '%' || :paramCompetencia || '%'");

			if(!params.containsKey("paramCompetencia")) {
				params.put("paramCompetencia", competencia);
			}
		}

		if(cargoAuxiliar != null && cargoAuxiliar){
			sb.append("AND ( ");
			sb.append("		( NOT EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm where ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) "); 
			sb.append("		OR ");
			sb.append("     ( EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm where ptlm.id_processo_trf = proctrf.id_processo_trf and ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append(") ");
			
			if(!params.containsKey("usuLoc")) {
				params.put("usuLoc", Authenticator.getIdUsuarioLocalizacaoMagistradoServidorAtual());
			}
		}

		if(!isServidorExclusivoOJC) {
			sb.append("AND ptar.id_localizacao IN (:idsLocalizacoesFisicas) ");
			
			if(!params.containsKey("idsLocalizacoesFisicas")) {
				params.put("idsLocalizacoesFisicas", idsLocalizacoesFisicasList);
			}
		}

		if (idOrgaoJulgadorColegiado != null && idOrgaoJulgadorColegiado > 0) {
			sb.append("AND ptar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
			
			if(!params.containsKey("idOrgaoJulgadorColegiado")) {
				params.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
			}
		}

		if (idsOrgaoJulgadorCargo != null && idsOrgaoJulgadorCargo.size() > 0) {
			sb.append("AND ptar.id_orgao_julgador_cargo IN (:idOrgaoJulgadorCargo) ");

			if(!params.containsKey("idOrgaoJulgadorCargo")) {
				params.put("idOrgaoJulgadorCargo", idsOrgaoJulgadorCargo);
			}
		}

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			sb.append("AND (proctrf.in_segredo_justica = false OR EXISTS "
					+ "(SELECT 1 FROM tb_proc_visibilida_segredo vis "
					+ "	WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctrf.id_processo_trf)) ");
			
			if(!params.containsKey("idUsuario")) {
				params.put("idUsuario", idUsuario);
			}
		} else {
			appendFiltroNivelSigilo(idUsuario, nivelAcessoSigilo, sb, params);
		}

		if(somenteFavoritas != null && somenteFavoritas){
			sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag pt " +
						"INNER JOIN tb_tag t on pt.id_tag = t.id " +
						"INNER JOIN tb_tag_favorita tf on tf.id_tag = pt.id_tag " +
						"WHERE tf.id_usuario = :idUsuarioFavorito and pt.id_processo = proctrf.id_processo_trf ");
			
			if(!params.containsKey("idUsuarioFavorito")) {
				params.put("idUsuarioFavorito", idUsuario);
			}


			if(idLocalizacaoFisica != null && idLocalizacaoFisica > 0){
				sb.append("AND t.id_localizacao = :idLocalizacaoFisica ");
				
				if(!params.containsKey("idLocalizacaoFisica")) {
					params.put("idLocalizacaoFisica", idLocalizacaoFisica);
				}
			}

			sb.append(") ");
		}
		
		if(etiquetasList != null && etiquetasList.size() > 0){
			sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag pt " +
					"INNER JOIN tb_tag t on pt.id_tag = t.id " +
					"WHERE pt.id_processo = proctrf.id_processo_trf AND LOWER(TO_ASCII(t.ds_tag)) in (:tagsList)");

			if(!params.containsKey("tagsList")) {
				params.put("tagsList", etiquetasList.stream().map(p -> StringUtil.normalize(p).toLowerCase())
						.collect(Collectors.toList()));
			}

			if(idLocalizacaoFisica != null && idLocalizacaoFisica > 0){
				sb.append("AND t.id_localizacao = :idLocalizacaoFisica ");
				
				if(!params.containsKey("idLocalizacaoFisica")) {
					params.put("idLocalizacaoFisica", idLocalizacaoFisica);
				}
			}

			sb.append(") ");
		}

		sb.append("GROUP BY ptar.nm_tarefa");

		
		Query q = entityManager.createNativeQuery(sb.toString());

		for(String key: params.keySet()){
			q.setParameter(key, params.get(key));
		}

		Map<String, Long> retorno = new TreeMap<String, Long>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			retorno.put(((String) borderTypes[0]).concat(":").concat(((BigInteger) borderTypes[2]).toString()),
					((BigInteger) borderTypes[1]).longValue());
		}
		return retorno;
	}
    
    /**
     * Retorna a lista de tarefas de usuário, de fluxos ativos, disponíveis para a localização modelo do usuário (ex: Secretaria).
     * A lista inclui tanto as tarefas de usuário que possuem processos a ela vinculados, quanto as vazias. 
     * @param idLocalizacaoModelo
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<TarefaDTO> carregarListaTarefasLocalizacao(Integer idLocalizacaoModelo) {
    	StringBuilder hql = new StringBuilder();
    	hql.append("select t.id_, t.name_ ");
    	hql.append("from core.tb_fluxo f ");
    	hql.append("inner join "); 
    	hql.append("( ");
    	hql.append("   select max(id_) as id_, name_ ");
    	hql.append("   from jbpm_processdefinition ");
    	hql.append("   group by name_ ");
    	hql.append(") as p on p.name_ = f.ds_fluxo ");
    	hql.append("inner join jbpm_task t on t.processdefinition_ = p.id_ ");
    	hql.append("where f.in_ativo = true ");
    	hql.append("and EXISTS "); 
    	hql.append("( ");
    	hql.append("   SELECT 1 FROM core.tb_proc_localizacao_ibpm tl "); 
    	hql.append("   WHERE tl.id_task_jbpm = t.id_ "); 
    	hql.append("   AND tl.id_localizacao = :idLocalizacaoModelo ");
    	hql.append(")"); 
    	hql.append("order by t.name_");
    	Query q = entityManager.createNativeQuery(hql.toString());
		q.setParameter("idLocalizacaoModelo", idLocalizacaoModelo);
		
		List<Object[]> list = q.getResultList();
		List<TarefaDTO> listTarefas = new ArrayList<TarefaDTO>();
		
		for (Object[] tupla: list) {
			listTarefas.add(new TarefaDTO(new Long(tupla[0].toString()), tupla[1].toString()));
		}
		
		return listTarefas;
    }

    /**
     * Monta o painel do usuario de acordo com as permissoes de sigilo do usurio, nao permitindo que este visualize 
     * processos que estao com nivel de acesso maior do que o do usurio logado
     */
	private void appendFiltroNivelSigilo(Integer idUsuario, Integer nivelAcessoSigilo, StringBuilder sb, Map<String, Object> params) {
		sb.append("AND (ptar.in_segredo_justica = false OR ");
		sb.append("			(ptar.in_segredo_justica = true AND ptar.cd_nivel_acesso <= :nivelAcessoUsuario) OR");
		sb.append("			EXISTS (SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = ptar.id_processo_trf)");
		sb.append("		) ");
		params.put("nivelAcessoUsuario", nivelAcessoSigilo);
		params.put("idUsuario", idUsuario);
	}
    
    public void limparListaTarefas() {
    	mapaTarefas.clear();
    }
    
    @SuppressWarnings("unchecked")
	public List<String> carregarListaTarefas(
            Integer idLocalizacaoModelo,
            Integer idPapel) {
    	
    		String chaveMapa = idLocalizacaoModelo+"-"+idPapel;
    		
    		List<String> tarefas = mapaTarefas.get(chaveMapa);
    		
    		if ( tarefas==null ) {
    			StringBuilder sb = new StringBuilder();
    			
    			sb.append(" select distinct task.name_ ");
    			sb.append(" from core.tb_proc_localizacao_ibpm pli ");
    			sb.append(" inner join jbpm_task task on task.id_ = pli.id_task_jbpm ");
    			sb.append(" where pli.id_localizacao = :idLocalizacaoModelo ");
    			sb.append(" and pli.id_papel = :idPapel ");
    			sb.append(" order by task.name_ ");
    			
    			Query q = entityManager.createNativeQuery(sb.toString());
    			q.setParameter("idLocalizacaoModelo", idLocalizacaoModelo);
    			q.setParameter("idPapel", idPapel);
    			
    			tarefas = q.getResultList();
    			
    			mapaTarefas.put(chaveMapa, tarefas);
    		}
    		
    		return tarefas;

	}
    
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public Map<String,Long> recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento(
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, List<Integer> idsOrgaoJulgadorCargo,
			Integer idUsuario, List<Integer> idsLocalizacoesFisicasList, Integer idLocalizacaoModelo, Integer idPapel, 
			Boolean visualizaSigiloso, Integer nivelAcessoSigilo, 
			boolean incluiIdTipoDocumento, List<String> tag, Boolean cargoAuxiliar){
		
        Map<String, Object> params = new HashMap<String,Object>(0);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT tpd.ds_tipo_processo_documento, count(DISTINCT pd.id_processo_documento), tpd.id_tipo_processo_documento ");
		sb.append("FROM tb_processo_tarefa ptar ");
		sb.append("INNER JOIN jbpm_task task ON task.id_ = ptar.id_task ");
		sb.append("INNER JOIN tb_processo_trf proctrf ON proctrf.id_processo_trf= ptar.id_processo_trf ");
		sb.append("INNER JOIN jbpm_variableinstance vii ON vii.processinstance_ = ptar.id_process_instance AND vii.name_ IN ('"+ 
					Variaveis.MINUTA_EM_ELABORACAO+"', '"+Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO+"') ");
		sb.append("INNER JOIN tb_processo_documento pd ON (pd.id_processo_documento = CAST(vii.longvalue_ as integer) AND pd.dt_juntada IS NULL) ");
		sb.append("INNER JOIN tb_tipo_processo_documento tpd ON tpd.id_tipo_processo_documento = pd.id_tipo_processo_documento ");
		sb.append("INNER JOIN tb_tipo_proc_doc_papel tpdp ON tpdp.id_tipo_processo_documento = tpd.id_tipo_processo_documento ");
		sb.append("WHERE task.priority_ = 4 ");
		sb.append("AND EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("		       WHERE tl.id_processo = proctrf.id_processo_trf ");
		sb.append("            AND tl.id_task_jbpm = ptar.id_task ");
		sb.append("            AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("            AND tl.id_papel = :idPapel) ");
		sb.append("AND tpdp.id_papel = :idPapel AND tpdp.in_exigibilidade != :in_exigibilidadeSemAssinatura ");
		
		if(!params.containsKey("idLocalizacaoModelo")) {
			params.put("idLocalizacaoModelo", idLocalizacaoModelo);
		}

		if(!params.containsKey("idPapel")) {
			params.put("idPapel", idPapel);
		}

		if(!params.containsKey("in_exigibilidadeSemAssinatura")) {
			params.put("in_exigibilidadeSemAssinatura", ExigibilidadeAssinaturaEnum.N.toString());
		}
		
		if(cargoAuxiliar != null && cargoAuxiliar){
			sb.append("AND ( ");
			sb.append("		( NOT EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) "); 
			sb.append("		OR ");
			sb.append("     ( EXISTS (SELECT NULL FROM tb_proc_trf_lcliz_mgstrado ptlm WHERE ptlm.id_processo_trf = proctrf.id_processo_trf AND ptlm.id_usu_loc_magistrado_servidor = :usuLoc) ) ");
			sb.append(") "); 

			if(!params.containsKey("usuLoc")) {
				params.put("usuLoc", Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual().getIdUsuarioLocalizacaoMagistradoServidor());
			}
		}
		
		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicasList)) {
			sb.append("AND ptar.id_localizacao IN (:idsLocalizacoesFisicas) ");

			if(!params.containsKey("idsLocalizacoesFisicas")) {
				params.put("idsLocalizacoesFisicas", idsLocalizacoesFisicasList);
			}
		}

		if (idOrgaoJulgadorColegiado != null && idOrgaoJulgadorColegiado > 0) {
			sb.append("AND ptar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");

			if(!params.containsKey("idOrgaoJulgadorColegiado")) {
				params.put("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
			}
		}

		if (idsOrgaoJulgadorCargo != null && idsOrgaoJulgadorCargo.size() > 0) {
			sb.append("AND ptar.id_orgao_julgador_cargo in (:idOrgaoJulgadorCargo) ");

			if(!params.containsKey("idOrgaoJulgadorCargo")) {
				params.put("idOrgaoJulgadorCargo", idsOrgaoJulgadorCargo);
			}
		}

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			sb.append("AND (proctrf.in_segredo_justica = false OR EXISTS "
					+ "(SELECT 1 FROM tb_proc_visibilida_segredo vis "
					+ "	WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctrf.id_processo_trf)) ");

			if(!params.containsKey("idUsuario")) {
				params.put("idUsuario", idUsuario);
			}
		} else {
			sb.append("AND (ptar.in_segredo_justica = false OR (");
			sb.append("			ptar.in_segredo_justica = true AND proctrf.cd_nivel_acesso <= :nivelAcessoUsuario)");
			sb.append("		) ");
			params.put("nivelAcessoUsuario", nivelAcessoSigilo);
		}

		if(tag != null && !tag.isEmpty()){
			sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag tags WHERE ds_tag in (:tags) AND tags.id_processo = ptar.id_processo_trf) ");

			if(!params.containsKey("tags")) {
				params.put("tags", tag);
			}
		}

		sb.append("GROUP BY tpd.ds_tipo_processo_documento, tpd.id_tipo_processo_documento");
		
		Query q = entityManager.createNativeQuery(sb.toString());

		for(String key: params.keySet()){
			q.setParameter(key, params.get(key));
		}

		Map<String, Long> retorno = new TreeMap<String, Long>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			if (!incluiIdTipoDocumento){
				retorno.put((String) borderTypes[0],
						((BigInteger) borderTypes[1]).longValue());
			}else{
				retorno.put(((String) borderTypes[0]).concat(":").concat(((Integer) borderTypes[2]).toString()),
						((BigInteger) borderTypes[1]).longValue());
			}
			
		}
		return retorno;
	}
	

    /**
     * Obtém o fluxo de um processo pelo ID do processo.
     *
     * @param idProcesso id do processo que está vinculado ao fluxo.
     *
     * @return Fluxo que o processo pertence.
     */
    public Fluxo obterFluxoDoProcesso(Long idProcesso) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select ");
        sql.append(" fl_.id_fluxo, ");
        sql.append(" fl_.cd_fluxo, ");
        sql.append(" fl_.ds_fluxo, ");
        sql.append(" fl_.ds_xml, ");
        sql.append(" fl_.in_publicado, ");
        sql.append(" fl_.in_ativo, ");
        sql.append(" fl_.dt_fim_publicacao, ");
        sql.append(" fl_.dt_inicio_publicacao, ");
        sql.append(" fl_.qt_prazo, ");
        sql.append(" fl_.dt_ultima_publicacao, ");
        sql.append(" fl_.id_usuario_publicacao ");
        sql.append(" from core.tb_fluxo fl_ ");
        sql.append(" inner join core.tb_processo pr_ on pr_.id_fluxo = fl_.id_fluxo ");
        sql.append(" where pr_.id_processo = :idProcesso  ");

        Query query = getEntityManager().createNativeQuery(sql.toString(), Fluxo.class);
        query.setParameter("idProcesso", idProcesso);

        return (Fluxo) query.getSingleResult();
    }
	
    public Long recuperarTaskInstancePorVariavel(String nomeVariavel, String valorVariavel) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ti.id_ ");
		sql.append("from jbpm_variableinstance vi  ");
		sql.append("inner join jbpm_taskinstance ti on (ti.procinst_ = vi.processinstance_) ");
		sql.append("where vi.name_ = :nomeVariavel ");
		sql.append("and vi.stringvalue_ = :valorVariavel ");
		sql.append("and ti.end_ is null ");
		Query query = entityManager.createNativeQuery(sql.toString());
		query.setParameter("nomeVariavel", nomeVariavel);
		query.setParameter("valorVariavel", valorVariavel);
		Number idTaskInstance = (Number) query.getSingleResult();
		return idTaskInstance.longValue();
    }	
    
    public void finalizaFluxoManualmente(Long idTarefa ) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE public.jbpm_taskinstance ");
		sql.append("SET isopen_ = false ");
		sql.append("WHERE procinst_ IN ( ");
		sql.append("SELECT tpi.id_proc_inst ");
		sql.append("FROM core.tb_processo_instance tpi ");
		sql.append("WHERE tpi.id_proc_inst = :idTarefa ) ");
		sql.append("AND isopen_ = true ");

		Query query = entityManager.createNativeQuery(sql.toString());
		query.setParameter("idTarefa", idTarefa);		
		query.executeUpdate();
	}
}
