package br.jus.cnj.pje.auditoria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.entity.log.LogUtil;
import br.jus.cnj.pje.indexer.IndexVO;
import br.jus.cnj.pje.util.QueryUtils;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Name(PjeLogDB.NAME)
@Scope(ScopeType.EVENT)
public class PjeLogDB extends PjeLog {
	
	public static final String NAME = "pjeLogDB";
	
	@Override
	public void log(Object instance, TipoOperacaoLogEnum tipoOperacao) {
		if(LogUtil.isRequisicaoIntercomunicacaoRest()) {
			return;
		}
		EntityManager em = (EntityManager) Component.getInstance("entityManagerLog");
		try {
			EntityLog logEnt = LogUtil.createEntityLog(instance);
			logEnt.setTipoOperacao(tipoOperacao);

			em.persist(logEnt);

			EntityLogDetail detail = new EntityLogDetail();
			detail.setEntityLog(logEnt);
			detail.setValorAtual(LogUtil.toStringFields(instance));
			detail.setValorAnterior("");
			em.persist(detail);
			logEnt.getLogDetalheList().add(detail);
			em.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void log(Class<?> clazz, 
			 Object id,
			 Object[] oldState, 
			 Object[] state, 
			 String[] nomes, 
			 Integer idUsuario, 
			 String ip, 
			 String url, 
			 TipoOperacaoLogEnum tipoOperacao){
		if(LogUtil.isRequisicaoIntercomunicacaoRest()) {
			return;
		}
		EntityManager entityManager = (EntityManager) Component.getInstance("entityManagerLog");
		EntityLog log = LogUtil.createEntityLog(clazz, id, idUsuario, ip, url);
		log.setTipoOperacao(tipoOperacao);
		log = persist(log);
		List<EntityLogDetail> listOfEntityLogDetails = toListOfEntityLogDetails(clazz, id, oldState, state, nomes, log);
		try {	
			persist(listOfEntityLogDetails);
			entityManager.flush();
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Erro ao realizar o log da entidade: " + e.getLocalizedMessage());
		}
		
		if(ifEntityIsIndexable(clazz, listOfEntityLogDetails)){
			this.indexerQueueSender.sendMessage(new IndexVO(id, clazz));
		}
	}

	private boolean ifEntityIsIndexable(Class<?> clazz, List<EntityLogDetail> listaDetails) {
		boolean markForIndexing = false;
		for (EntityLogDetail entityLogDetail : listaDetails) {
			if(entityLogDetail != null){
				if(!markForIndexing && indexador != null && indexador.isIndexable(clazz, entityLogDetail.getNomeAtributo()) && indexador.isEnabled()){
					markForIndexing = true;
					break;
				}
			}
		}
		return markForIndexing;
	}
	
	private List<EntityLogDetail> toListOfEntityLogDetails(Class<?> clazz, Object id, Object[] oldState, Object[] state, String[] nomes, EntityLog log) {
		List<EntityLogDetail> detalhes = new ArrayList<EntityLogDetail>();
		for(int i = 0; i < nomes.length; i++){
			try{
				EntityLogDetail detail = createDetails(log, clazz, id, log.getTipoOperacao(), nomes[i], oldState != null ? oldState[i] : null, state != null ? state[i] : null);
				if(detail != null){
					detail.setEntityLog(log);
					detalhes.add(detail);
				}
			}catch(Exception e){
				logger.error("Erro ao realizar o log da entidade: " + e.getLocalizedMessage());
			}
		}
		return detalhes;
	}

	private List<EntityLogDetail> persist(List<EntityLogDetail> listEntityLogDetail) {
		EntityManager entityManager = (EntityManager) Component.getInstance("entityManagerLog");
		int sizeListaEntityLogDetail = listEntityLogDetail.size();
		StringBuilder sqlToCreateLogDetalheBuilder = new StringBuilder();
		sqlToCreateLogDetalheBuilder.append("insert ");
		sqlToCreateLogDetalheBuilder.append("into ");
		sqlToCreateLogDetalheBuilder.append("public.tb_log_detalhe ");
		sqlToCreateLogDetalheBuilder.append("(id_log_detalhe,");
		sqlToCreateLogDetalheBuilder.append("id_log,");
		sqlToCreateLogDetalheBuilder.append("nm_atributo,");
		sqlToCreateLogDetalheBuilder.append("ds_valor_anterior,");
		sqlToCreateLogDetalheBuilder.append("ds_valor_atual)");
		sqlToCreateLogDetalheBuilder.append("values ");
		List<Object> listaDeIdsDetails = entityManager.createNativeQuery("select nextval('sq_tb_log_detalhe') from generate_series(1,"+sizeListaEntityLogDetail+")").getResultList();
		for(int i = 0; i < sizeListaEntityLogDetail; i++){
				EntityLogDetail detail = listEntityLogDetail.get(i);
				detail.setIdLogDetalhe(Long.parseLong(listaDeIdsDetails.get(i).toString()));
				sqlToCreateLogDetalheBuilder.append("("+QueryUtils.toParameterOfNativeQuery(detail.getIdLogDetalhe())+",");
				sqlToCreateLogDetalheBuilder.append(""+QueryUtils.toParameterOfNativeQuery(detail.getEntityLog().getIdLog())+",");
				sqlToCreateLogDetalheBuilder.append(""+QueryUtils.toParameterOfNativeQuery(detail.getNomeAtributo())+",");
				sqlToCreateLogDetalheBuilder.append(""+QueryUtils.toParameterOfNativeQuery(detail.getValorAnterior())+",");
				sqlToCreateLogDetalheBuilder.append(""+QueryUtils.toParameterOfNativeQuery(detail.getValorAtual())+")");
				if(i == (sizeListaEntityLogDetail -1)) {
					entityManager.createNativeQuery(sqlToCreateLogDetalheBuilder.toString()).executeUpdate();						
				} else {
					sqlToCreateLogDetalheBuilder.append(",");
				}
		}
		return listEntityLogDetail;
	}

	private EntityLog persist(EntityLog log) {
		EntityManager entityManager = (EntityManager) Component.getInstance("entityManagerLog");
		Long idLog= Long.parseLong(entityManager.createNativeQuery("select nextval('sq_tb_log')").getSingleResult().toString());
		StringBuilder sqlToCreateLogBuilder = new StringBuilder();
		sqlToCreateLogBuilder.append("insert ");
		sqlToCreateLogBuilder.append("into ");
		sqlToCreateLogBuilder.append("public.tb_log (id_log,");
		sqlToCreateLogBuilder.append("id_usuario,");
		sqlToCreateLogBuilder.append("id_pagina,");
		sqlToCreateLogBuilder.append("ds_ip,");
		sqlToCreateLogBuilder.append("ds_entidade,");
		sqlToCreateLogBuilder.append("ds_id_entidade,");
		sqlToCreateLogBuilder.append("tp_operacao,");
		sqlToCreateLogBuilder.append("dt_log,");
		sqlToCreateLogBuilder.append("ds_package) ");
		sqlToCreateLogBuilder.append("values("+QueryUtils.toParameterOfNativeQuery(idLog)+",");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getIdUsuario())+",");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getUrlRequisicao())+",");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getIp())+",");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getNomeEntidade())+",");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getIdEntidade())+",");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getTipoOperacao().toString())+",");
		sqlToCreateLogBuilder.append("now(),");
		sqlToCreateLogBuilder.append(""+QueryUtils.toParameterOfNativeQuery(log.getNomePackage())+")");
		entityManager.createNativeQuery(sqlToCreateLogBuilder.toString()).executeUpdate();
		log.setIdLog(idLog);
		return log;
	}
	
	@Override
	public EntityLogDetail createDetails(EntityLog log, Class<?> clazz, Object id, TipoOperacaoLogEnum tipoOperacao, String name, Object old, Object actual) throws Exception{
		boolean dirty = tipoOperacao == TipoOperacaoLogEnum.U ? !LogUtil.compareObj(old, actual) : true;

		if(LogUtil.isValidForLog(clazz, name)
				&& dirty){
			EntityLogDetail detail = new EntityLogDetail();
			detail.setEntityLog(log);
			detail.setNomeAtributo(name);
			detail.setValorAtual(LogUtil.toStringForLogWithCatchNullPointer(actual, name));
			if(old != null){
				detail.setValorAnterior(LogUtil.toStringForLogWithCatchNullPointer(old, name));
			}else{
				detail.setValorAnterior(null);
			}
			return detail;
		}
		return null;
	}
}
