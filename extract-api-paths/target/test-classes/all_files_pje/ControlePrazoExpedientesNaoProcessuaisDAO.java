package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jbpm.context.exe.VariableInstance;

import br.jus.cnj.pje.nucleo.Variaveis;

/**
 * Classe responsável por gerenciar o acesso ao banco de dados, concentrando todos os metodos de acesso direto ao banco necessários para os procedimentos
 * relacionados à prazo de expedientes nao processuais
 *
 */
@Name("controlePrazoExpedientesNaoProcessuaisDAO")
public class ControlePrazoExpedientesNaoProcessuaisDAO extends BaseDAO<VariableInstance>{
	@Logger
	private Log log;
	
	@SuppressWarnings("unchecked")
	public List<Integer> recuperaIdProcessosTrfPrazoExpirado(Date dataExpiracao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT id_processo ");
		sb.append(" FROM tb_processo_instance pi");
		sb.append("	JOIN jbpm_variableinstance vi ON (pi.id_proc_inst = vi.processinstance_) ");
		sb.append("WHERE name_ = :nomeVariavel ");
		sb.append(" AND (dateValue_ <= :dataLimite OR (stringvalue_ IS NOT NULL AND CAST(stringvalue_ AS DATE) <= :dataLimite)) ");
		sb.append("AND processinstance_ IS NOT NULL ");
		sb.append("AND EXISTS (SELECT 1 FROM jbpm_variableinstance viaux ");
		sb.append(  		  " WHERE viaux.processinstance_ = vi.processinstance_ ");
		sb.append(			  " AND viaux.name_ = :nomeVariavelTarefaAguardar ");		
		sb.append(			  " AND viaux.taskinstance_ IS NOT NULL) ");
		sb.append("ORDER BY id_processo DESC ");
		
		try{
			Query query = entityManager.createNativeQuery(sb.toString());
			query.setParameter("nomeVariavel", Variaveis.NOME_VARIAVEL_DIA_PRAZO);
			query.setParameter("dataLimite", dataExpiracao);
			query.setParameter("nomeVariavelTarefaAguardar", Variaveis.NOME_VARIAVEL_AGUARDANDO_PRAZO);
			List<Integer> queryResult = query.getResultList();
			
			int queryResultSize = queryResult.size();
			
			if(queryResultSize == 0) {
				log.info("");
				log.info("|++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++|");
				log.info("|NÃO HÁ PROCESSOS PARA TRAMITAÇAO POR TÉRMINO DO PRAZO DE EXPEDIENTES|");	
				log.info("|++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++|");
				log.info("");
			} else {
				log.info("");
				log.info("|+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++|");
				log.info("|EXISTEM {0} PROCESSOS PARA TRAMITAÇAO POR TÉRMINO DO PRAZO DE EXPEDIENTES|", queryResultSize);	
				log.info("|+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++|");
				log.info("");
			}
					
			return queryResult;			
		} catch (Exception e) {
			log.error("ERRO AO BUSCAR POR PROCESSOS COM A DATA DO PRAZO DE EXPEDIENTES NAO PROCESSUAIS VENCIDA : {0}", e.getLocalizedMessage());
			return null;
		}
	}
	
	@Override
	public Object getId(VariableInstance e) {
		// TODO Auto-generated method stub
		return null;
	}

}