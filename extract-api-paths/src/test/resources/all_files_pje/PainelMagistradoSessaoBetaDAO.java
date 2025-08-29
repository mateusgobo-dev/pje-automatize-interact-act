/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.DocumentoAssinaturaVO;
import br.jus.cnj.pje.entidades.vo.PesquisaProcessoVO;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * DAO LogHistoricoMovimentacaoDAO
 * 
 * @author Carlos Lisboa
 * @since 1.7.1
 */
@Name(PainelMagistradoSessaoBetaDAO.NAME)
public class PainelMagistradoSessaoBetaDAO extends BaseDAO<ProcessoDocumento> {
	
	public final static String NAME = "painelMagistradoSessaoBetaDAO";

	public static final String FILTRO_PAINEL_MAGISTRADO_BETA = "br.jus.cnj.pje.view";
	
	@Override
	public Object getId(ProcessoDocumento e) {
		return null;
	}
		
	@SuppressWarnings("unchecked")
	public List<ConsultaProcessoVO> carregarListaProcessosTarefas(
			List<Integer> idsLocalizacoesFisicas, Integer idOrgaoJulgadorColegiado,
			boolean isServidorExclusivoOJC, Integer idOrgaoJulgadorCargo,
			Integer idUsuario, Integer idLocalizacaoFisica, Integer idLocalizacaoModelo, Integer idPapel, Boolean visualizaSigiloso, 
			String nomeTarefa,Integer idCaixa, PesquisaProcessoVO criteriosPesquisa){
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT proctar.id_processo_trf, cabproc.nr_processo, proctar.dt_create_task, proctar.id_processo_tarefa, cabproc.ds_classe_judicial_sigla, ");
		sb.append("cabproc.ds_orgao_julgador, cabproc.ds_orgao_julgador_colegiado, cabproc.nm_pessoa_autor, cabproc.nm_pessoa_reu, ");
		sb.append(adicionaQueryTags(idLocalizacaoFisica));
		sb.append("FROM tb_processo_tarefa proctar ");
		sb.append("INNER JOIN tb_cabecalho_processo cabproc ON cabproc.id_processo_trf = proctar.id_processo_trf ");
		sb.append("WHERE proctar.nm_tarefa = :nomeTarefa ");
		sb.append("AND EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("		       WHERE tl.id_processo = proctar.id_processo_trf ");
		sb.append("            AND tl.id_task_jbpm = proctar.id_task ");
		sb.append("            AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("            AND tl.id_papel = :idPapel) ");

		if(!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)){
			sb.append("and proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
		}
		
		if(idOrgaoJulgadorColegiado != null ){
			sb.append("and proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
		}
		    
		if(idOrgaoJulgadorCargo != null ){
			sb.append("and proctar.id_orgao_julgador_cargo = :idOrgaoJulgadorCargo ");
		}

		if(visualizaSigiloso != null && !visualizaSigiloso){
			sb.append("AND (proctar.in_segredo_justica = false OR EXISTS "
					+ "(SELECT 1 FROM tb_proc_visibilida_segredo vis "
					+ "	WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)) ");
		}
		
		if(idCaixa != null){
			sb.append("AND proctar.id_caixa = :idCaixa ");
		}
		
		if(criteriosPesquisa != null){
			if(StringUtil.isNotEmpty(criteriosPesquisa.getNumeroProcesso())){
				sb.append("AND cabproc.nr_processo like '%'|| :numeroProcesso || '%' ");
			}
			if(StringUtil.isNotEmpty(criteriosPesquisa.getClasse())){
				sb.append("AND (cabproc.ds_classe_judicial like '%'|| :classeJudicial || '%' OR cabproc.ds_classe_judicial_sigla like '%'|| :classeJudicial  || '%') ");
			}
			if(criteriosPesquisa.getTags() != null && criteriosPesquisa.getTags().length > 0){
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag tags WHERE ds_tag in (:tags) AND tags.id_processo = proctar.id_processo_trf) ");
			}
		}

		Query q= entityManager.createNativeQuery(sb.toString());
		q.setParameter("idLocalizacaoModelo",idLocalizacaoModelo);
		q.setParameter("idPapel",idPapel);
		q.setParameter("nomeTarefa",nomeTarefa);
		
		if(!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)){
			q.setParameter("idsLocalizacoesFisicas",idsLocalizacoesFisicas);
		}
		
		if(idLocalizacaoFisica != null) {
			q.setParameter("idLocalizacaoFisica", idLocalizacaoFisica);
		}
		
		if(idOrgaoJulgadorColegiado != null ){
			q.setParameter("idOrgaoJulgadorColegiado",idOrgaoJulgadorColegiado);
		}
		    
		if(idOrgaoJulgadorCargo != null ){
			q.setParameter("idOrgaoJulgadorCargo",idOrgaoJulgadorCargo);
		}
		
		if(visualizaSigiloso != null && !visualizaSigiloso){
			q.setParameter("idUsuario",idUsuario);
		}
		
		if(idCaixa != null){
			q.setParameter("idCaixa",idCaixa);
		}
		
		if(criteriosPesquisa != null){
			if(StringUtil.isNotEmpty(criteriosPesquisa.getNumeroProcesso())){
				q.setParameter("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
			}
			if(StringUtil.isNotEmpty(criteriosPesquisa.getClasse())){
				q.setParameter("classeJudicial", criteriosPesquisa.getClasse());
			}
			if(criteriosPesquisa.getTags() != null && criteriosPesquisa.getTags().length > 0){
				q.setParameter("tags", Arrays.asList(criteriosPesquisa.getTags()));
			}
		}
		
		List<ConsultaProcessoVO> retorno = new ArrayList<ConsultaProcessoVO>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			ConsultaProcessoVO vo = new ConsultaProcessoVO();
			vo.setIdProcessoTrf((Integer) borderTypes[0]);
			vo.setNumeroProcesso((String) borderTypes[1]);
			vo.setDataChegada((Date) borderTypes[2]);
			vo.setIdTaskInstance(((BigInteger)borderTypes[3]).longValue());
			vo.setClasseJudicial((String) borderTypes[4]);
			

			vo.setOrgaoJulgador((String) borderTypes[5]);
			vo.setOrgaoJulgadorColegiado((String) borderTypes[6]);
			
			vo.setAutor((String) borderTypes[7]);
			vo.setReu((String) borderTypes[8]);
			
			vo.setTags((String) borderTypes[9]);
			retorno.add(vo);
		}
		return retorno;
	}

	@SuppressWarnings("unchecked")
	public List<ConsultaProcessoVO> carregarListaProcessosAssinatura(
			List<Integer> idsLocalizacoesFisicas, Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC,
			Integer idOrgaoJulgadorCargo,
			Integer idUsuario, Integer idLocalizacaoFisica, Integer idLocalizacaoModelo, Integer idPapel, 
			Boolean visualizaSigiloso,Integer idTipoDocumento, Integer idCaixa, PesquisaProcessoVO criteriosPesquisa){
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT cabproc.id_processo_trf, cabproc.nr_processo, proctar.dt_create_task, proctar.id_processo_tarefa, cabproc.ds_classe_judicial_sigla, viii.stringvalue_, ");
		sb.append("pd.id_processo_documento, pd.dt_inclusao, pdb.ds_md5_documento, pdb.in_binario, ");
		sb.append("cabproc.ds_orgao_julgador, cabproc.ds_orgao_julgador_colegiado, ");
		sb.append("cabproc.nm_pessoa_autor, nm_pessoa_reu, ");
		sb.append(adicionaQueryTags(idLocalizacaoFisica));
		sb.append("FROM tb_processo_tarefa proctar ");
		sb.append("INNER JOIN tb_cabecalho_processo cabproc ON cabproc.id_processo_trf = proctar.id_processo_trf ");
		sb.append("INNER JOIN jbpm_variableinstance vi ON vi.taskinstance_ = proctar.id_processo_tarefa AND vi.name_ IN ('frame:Processo_Fluxo_revisarMinuta','frame:Processo_Fluxo_revisarMinutaBeta') ");
		sb.append("INNER JOIN jbpm_variableinstance vii ON vii.processinstance_ = vi.processinstance_ AND vii.name_ IN ('"+Variaveis.MINUTA_EM_ELABORACAO+"') ");
		sb.append("INNER JOIN tb_processo_documento pd ON pd.id_processo_documento = CAST(vii.longvalue_ AS INTEGER) ");
		sb.append("INNER JOIN tb_processo_documento_bin pdb ON pdb.id_processo_documento_bin = pd.id_processo_documento_bin ");
		sb.append("INNER JOIN tb_tipo_processo_documento tpd ON tpd.id_tipo_processo_documento = pd.id_tipo_processo_documento ");
		sb.append("LEFT JOIN jbpm_variableinstance viii ON viii.taskinstance_ = proctar.id_processo_tarefa AND viii.name_ = '"+ Variaveis.CONFERIR_PROCESSO_ASSINATURA +"' ");
		sb.append("WHERE EXISTS (SELECT 1 FROM tb_proc_localizacao_ibpm tl ");
		sb.append("		         WHERE tl.id_processo = proctar.id_processo_trf ");
		sb.append("              AND tl.id_task_jbpm = proctar.id_task ");
		sb.append("              AND tl.id_localizacao = :idLocalizacaoModelo ");
		sb.append("              AND tl.id_papel = :idPapel) ");

		if (idTipoDocumento != null) {
			sb.append("AND tpd.id_tipo_processo_documento = :idTipoDocumento ");
		}
		
		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			sb.append("AND proctar.id_localizacao IN (:idsLocalizacoesFisicas) ");
		}

		if (idOrgaoJulgadorColegiado != null) {
			sb.append("AND proctar.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
		}

		if (idOrgaoJulgadorCargo != null) {
			sb.append("AND proctar.id_orgao_julgador_cargo = :idOrgaoJulgadorCargo ");
		}

		if(visualizaSigiloso != null && !visualizaSigiloso){
			sb.append("AND (proctar.in_segredo_justica = false OR EXISTS "
					+ "(SELECT 1 FROM tb_proc_visibilida_segredo vis "
					+ "	WHERE vis.id_pessoa = :idUsuario AND vis.id_processo_trf = proctar.id_processo_trf)) ");
		}
		
		if(idCaixa != null){
			sb.append("AND proctar.id_caixa = :idCaixa ");
		}
		
		if(criteriosPesquisa != null){
			if(StringUtil.isNotEmpty(criteriosPesquisa.getNumeroProcesso())){
				sb.append("AND cabproc.nr_processo like '%'|| :numeroProcesso || '%' ");
			}
			if(StringUtil.isNotEmpty(criteriosPesquisa.getClasse())){
				sb.append("AND (cabproc.ds_classe_judicial like '%'|| :classeJudicial || '%' OR cabproc.ds_classe_judicial_sigla like '%'|| :classeJudicial  || '%') ");
			}
			if(criteriosPesquisa.getTags() != null && criteriosPesquisa.getTags().length > 0){
				sb.append("AND EXISTS (SELECT 1 FROM tb_processo_tag tags WHERE ds_tag in (:tags) AND tags.id_processo = proctar.id_processo_trf) ");
			}
		}

		Query q = entityManager.createNativeQuery(sb.toString());
		q.setParameter("idLocalizacaoModelo", idLocalizacaoModelo);
		q.setParameter("idPapel", idPapel);

		if (idTipoDocumento != null) {
			q.setParameter("idTipoDocumento", idTipoDocumento);
		}
		
		if (!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			q.setParameter("idsLocalizacoesFisicas", idsLocalizacoesFisicas);
		}
		if(idLocalizacaoFisica != null) {
			q.setParameter("idLocalizacaoFisica", idLocalizacaoFisica);
		}

		if (idOrgaoJulgadorColegiado != null) {
			q.setParameter("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		}

		if (idOrgaoJulgadorCargo != null) {
			q.setParameter("idOrgaoJulgadorCargo", idOrgaoJulgadorCargo);
		}

		if (visualizaSigiloso != null && !visualizaSigiloso) {
			q.setParameter("idUsuario", idUsuario);
		}
		
		if(idCaixa != null){
			q.setParameter("idCaixa",idCaixa);
		}
		
		if(criteriosPesquisa != null){
			if(StringUtil.isNotEmpty(criteriosPesquisa.getNumeroProcesso())){
				q.setParameter("numeroProcesso", criteriosPesquisa.getNumeroProcesso());
			}
			if(StringUtil.isNotEmpty(criteriosPesquisa.getClasse())){
				q.setParameter("classeJudicial", criteriosPesquisa.getClasse());
			}
			if(criteriosPesquisa.getTags() != null && criteriosPesquisa.getTags().length > 0){
				q.setParameter("tags", Arrays.asList(criteriosPesquisa.getTags()));
			}
		}

		List<ConsultaProcessoVO> retorno = new ArrayList<ConsultaProcessoVO>();
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes : resultList) {
			ConsultaProcessoVO vo = new ConsultaProcessoVO();
			vo.setIdProcessoTrf((Integer) borderTypes[0]);
			vo.setNumeroProcesso((String) borderTypes[1]);
			vo.setDataChegada((Date) borderTypes[2]);
			vo.setIdTaskInstance(((BigInteger)borderTypes[3]).longValue());
			vo.setClasseJudicial((String) borderTypes[4]);
			String conferido = (String) borderTypes[5];
			Boolean conf = conferido != null && conferido.equals("T");
			vo.setConferido(conf);
			
			DocumentoAssinaturaVO docVO = new DocumentoAssinaturaVO();
			docVO.setIdProcessoDocumento((Integer) borderTypes[6]);
			docVO.setDataInclusao((Date) borderTypes[7]);
			docVO.setMd5((String) borderTypes[8]);
			docVO.setBinario((Boolean) borderTypes[9]);
			
			vo.setDocumentoAssinatura(docVO);
			
			vo.setOrgaoJulgador((String) borderTypes[10]);
			vo.setOrgaoJulgadorColegiado((String) borderTypes[11]);
			vo.setAutor((String) borderTypes[12]);
			vo.setReu((String) borderTypes[13]);
			vo.setTags((String) borderTypes[14]);
			
			retorno.add(vo);
		}
		return retorno;
	}
	
	
	private String adicionaQueryTags(Integer idLocalizacaoFisica) {
		StringBuilder sql = new StringBuilder();
		if(idLocalizacaoFisica != null){
			sql.append("(");
			sql.append("SELECT string_agg(ds_tag,',') ");
			sql.append("FROM tb_processo_tag tgs ");
			sql.append(" JOIN tb_tag tag ON (tgs.id_tag = tag.id)");
			sql.append("	WHERE tgs.id_processo = proc.id_processo ");
			sql.append("	AND tag.id_localizacao = :idLocalizacaoFisica");
			sql.append(") as tags ");
		}
		return sql.toString();
	}
}
