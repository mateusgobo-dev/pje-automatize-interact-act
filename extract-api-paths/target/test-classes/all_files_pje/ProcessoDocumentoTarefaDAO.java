package br.jus.cnj.pje.business.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.util.StringUtil;
@Name(ProcessoDocumentoTarefaDAO.NAME)
public class ProcessoDocumentoTarefaDAO extends BaseDAO<ProcessoDocumento> {
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
	
	public static final String NAME = "processoDocumentoTarefaDAO";
    @SuppressWarnings("unchecked")
	public List<Object[]> carregarListaTarefasUsuario(
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
			Boolean cargoAuxiliar,
			Integer idTipoProcessoDocumento,
			String meioComunicacao, Date dtInicio, Date dtFim, String docImpresso, Date dtInicioExpediente, Date dtFimExpediente) {
	    	
	    	if(CollectionUtilsPje.isEmpty(idsLocalizacoesFisicasList)){
	    		if(idLocalizacaoFisica != null && idLocalizacaoFisica > 0) {
	    			idsLocalizacoesFisicasList.add(idLocalizacaoFisica);
	    		}else {
	    			idsLocalizacoesFisicasList.add(-1);
	    		}
	    	}

	        StringBuilder sb = new StringBuilder();
	        
	        Map<String, Object> params = new HashMap<String,Object>(0);
	        sb.append("SELECT distinct pd.id_processo_documento, pdptrf.nr_processo,tpdoc.ds_tipo_processo_documento, pd.dt_juntada, pex.in_meio_expedicao_expediente, pd.id_processo_documento_bin, pex.dt_criacao_expediente, ppex.id_processo_parte_expediente FROM core.tb_processo_documento pd ");
	        sb.append("INNER JOIN core.tb_tipo_processo_documento tpdoc ON tpdoc.id_tipo_processo_documento = pd.id_tipo_processo_documento ");
	        sb.append("LEFT JOIN client.tb_processo_expediente pex ON  pd.id_processo_documento = pex.id_processo_documento ");
	        sb.append("LEFT JOIN client.tb_proc_parte_expediente ppex ON  pex.id_processo_expediente = ppex.id_processo_expediente ");
	        sb.append("INNER JOIN core.tb_processo pdptrf ON pdptrf.id_processo = pd.id_processo WHERE EXISTS (");
	        sb.append("SELECT ptar ");
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
			
		    if(idTipoProcessoDocumento != 0){
				sb.append("AND tpdoc.id_tipo_processo_documento = :idTipoProcessoDocumento ");
				
			    if(!params.containsKey("idTipoProcessoDocumento")) {
					params.put("idTipoProcessoDocumento", idTipoProcessoDocumento);
				} 
			}
		    if(StringUtil.isNotEmpty(meioComunicacao)){
				sb.append("AND pex.in_meio_expedicao_expediente = :meioComunicacao ");
				
			    if(!params.containsKey("meioComunicacao")) {
					params.put("meioComunicacao", meioComunicacao);
				} 
			}
	
			if(!params.containsKey("idLocalizacaoModelo")) {
				params.put("idLocalizacaoModelo", idLocalizacaoModelo);
			}
			

			if(!params.containsKey("idPapel")) {
				params.put("idPapel", idPapel);
			}

			if(StringUtil.isNotEmpty(numeroProcesso)){
				sb.append("AND p.nr_processo LIKE '%' || :numeroProcesso || '%' ");

				if(!params.containsKey("numeroProcesso")) {
					params.put("numeroProcesso", numeroProcesso);
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

				sb.append("  ) ");
			}
			
			if(dtInicioExpediente != null && dtFimExpediente != null) {
				sb.append("AND pex.dt_criacao_expediente >= :dtInicioExpediente ");
				sb.append("AND pex.dt_criacao_expediente <= :dtFimExpediente ");
				
				Calendar calInicio = Calendar.getInstance();
				if(!params.containsKey("dtInicioExpediente")) {
					calInicio.setTime(dtInicioExpediente);
					params.put("dtInicioExpediente", calInicio.getTime());
				}
				Calendar calFim = Calendar.getInstance();
				if(!params.containsKey("dtFimExpediente")) {
					calFim.setTime(dtFimExpediente);
					calFim.set(Calendar.HOUR_OF_DAY, 23);
					calFim.set(Calendar.MINUTE, 59);
					
					params.put("dtFimExpediente", calFim.getTime());
				}
			}
			
			if(dtInicio != null && dtFim != null) {
				sb.append("AND pd.dt_juntada >= :dtInicio ");
				sb.append("AND pd.dt_juntada <= :dtFim ");
				
				Calendar calInicio = Calendar.getInstance();
				if(!params.containsKey("dtInicio")) {
					calInicio.setTime(dtInicio);

					params.put("dtInicio", calInicio.getTime());
				}
				Calendar calFim = Calendar.getInstance();
				if(!params.containsKey("dtFim")) {
					calFim.setTime(dtFim);
					calFim.set(Calendar.HOUR_OF_DAY, 23);
					calFim.set(Calendar.MINUTE, 59);
					
					params.put("dtFim", calFim.getTime());
				}
			}
			
			if(docImpresso.equals("N")){
				sb.append("AND NOT EXISTS (SELECT 1 FROM client.tb_proc_trf_doc_impresso impr where impr.id_processo_documento = pd.id_processo_documento ) ");
			}
			if(docImpresso.equals("S")){
				sb.append("AND EXISTS (SELECT 1 FROM client.tb_proc_trf_doc_impresso impr where impr.id_processo_documento = pd.id_processo_documento ) ");
			}
			sb.append("AND pdptrf.id_processo = ptar.id_processo_trf)");
	
			Query q = entityManager.createNativeQuery(sb.toString());

			for(String key: params.keySet()){
				q.setParameter(key, params.get(key));
			}

			
			List<Object[]> resultList = q.getResultList();
		
			return resultList;
		}
    
		private void appendFiltroNivelSigilo(Integer idUsuario, Integer nivelAcessoSigilo, StringBuilder sb, Map<String, Object> params) {
			sb.append("AND (ptar.in_segredo_justica = false OR ");
			sb.append("			(ptar.in_segredo_justica = true AND ptar.cd_nivel_acesso <= :nivelAcessoUsuario) OR");
			sb.append("			EXISTS (SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = ptar.id_processo_trf)");
			sb.append("		) ");
			params.put("nivelAcessoUsuario", nivelAcessoSigilo);
			params.put("idUsuario", idUsuario);
		}
	
		@Override
		public Object getId(ProcessoDocumento e) {
			// TODO Auto-generated method stub
			return e.getIdProcessoDocumento();
		}
}