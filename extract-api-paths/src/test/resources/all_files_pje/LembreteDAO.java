package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.LembreteVO;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.LembreteDTO;
import br.jus.pje.nucleo.entidades.Lembrete;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(LembreteDAO.NAME)
public class LembreteDAO extends BaseDAO<Lembrete> {

	public static final String NAME = "lembreteDAO";
	public static final String FROM_LEMBRETE = " FROM Lembrete n ";
	public static final String LEFT_JOIN_N_PROCESSO_DOCUMENTO_AS_PROCESSO_DOCUMENTO = " LEFT JOIN n.processoDocumento AS processoDocumento ";
	public static final String JOIN_N_USUARIO_LOCALIZACAO_AS_UL = " JOIN n.usuarioLocalizacao AS ul ";
	public static final String JOIN_UL_USUARIO_AS_U = " JOIN ul.usuario AS u ";
	public static final String JOIN_UL_PAPEL_AS_UP = " JOIN ul.papel AS up ";
	public static final String JOIN_N_LEMBRETE_PERMISSAOS_AS_LP = " JOIN n.lembretePermissaos AS lp ";
	public static final String AND_N_ATIVO_ATIVO = " AND n.ativo = :ativo ";
	public static final String AND_N_USUARIO_LOCALIZACAO_EQUALS = " AND ( n.usuarioLocalizacao = ";
	public static final String ORDER_BY_N_DATA_VISIVEL_ATE_ASC = " ORDER BY n.dataVisivelAte ASC ";
	public static final String OR_PARTENTESES = " OR (  ";
	public static final String ATIVO = "ativo";
	
	@Override
	public Object getId(Lembrete e) {
		return e.getIdLembrete();
	}
	
	/**
	 * Metodo que recupera lista formatada de lembretes do usuario com parametros.
	 * 
	 * @param Lembrete
	 * @param dataInicial
	 * @param dataFinal
	 * @return List<LembreteVO>
	 */
	@SuppressWarnings("unchecked")
	public List<LembreteVO> recuperaLembretes(Lembrete lembrete, Date dataInicial, Date dataFinal){
		inativaLembretesPorIdUsuario();
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT new br.jus.cnj.pje.entidades.vo.LembreteVO(")
			.append(" lembrete.idLembrete ")
			.append(" ,processoTrf ")
			.append(" ,processoDocumento ")
			.append(" ,usuario.nome || ' (' || papel.nome || ')'")
			.append(" ,lembrete.descricao ")
			.append(" ,lembrete.dataVisivelAte ")
			.append(" ,lembrete.dataInclusao")
			.append(" ,lembrete.ativo ")
			.append(" ,usuarioLocalizacao.idUsuarioLocalizacao) ")
			.append(" FROM Lembrete lembrete ")
			.append(" INNER JOIN lembrete.processoTrf AS processoTrf ")
			.append(" LEFT JOIN lembrete.processoDocumento AS processoDocumento ")
			.append(" JOIN lembrete.usuarioLocalizacao AS usuarioLocalizacao ")
 			.append(" JOIN usuarioLocalizacao.usuario usuario ")
 			.append(" JOIN usuarioLocalizacao.papel AS papel ")
			.append(" WHERE usuario.idUsuario = :idUsuario ")
			.append(" AND lembrete.ativo = :ativo ");
		
		if (dataInicial != null){
			jpql.append(" AND lembrete.dataVisivelAte >= :dataInicial ");
		}
		if (dataFinal != null){
			jpql.append(" AND lembrete.dataVisivelAte <= :dataFinal ");
		}
		if (StringUtils.isNotEmpty(lembrete.getDescricao())){
			jpql.append(" AND lower(lembrete.descricao) LIKE :descricao");
		}
		jpql.append(" ORDER BY lembrete.dataVisivelAte ASC ");
		
		Query query = getEntityManager().createQuery(jpql.toString()) ;
		query.setParameter("idUsuario", Authenticator.getIdUsuarioLogado());
		query.setParameter(ATIVO, lembrete.getAtivo());
		if (dataInicial != null){
			query.setParameter("dataInicial", dataInicial);
		}
		if (dataFinal != null){
			query.setParameter("dataFinal", dataFinal);
		}
		if (StringUtils.isNotEmpty(lembrete.getDescricao())){
			query.setParameter("descricao", "%"+lembrete.getDescricao().toLowerCase()+"%");
		}
		return query.getResultList();
	}

	/**
	 * Metodo responsavel por inativar os lembretes com a data de visibilidade vencida
	 */
	private void inativaLembretesPorIdUsuario() {
		String sql = "select l from Lembrete l where l.usuarioLocalizacao.idUsuarioLocalizacao = :idUsuarioLocalizacao "
				  + " and l.dataVisivelAte < '"+new Date()+"' ";
		List<Lembrete> resultados = getEntityManager().createQuery(sql).setParameter("idUsuarioLocalizacao",
				Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao()).getResultList();
		
		for( Lembrete resultado : resultados ){
			resultado.setAtivo(false);
			getEntityManager().merge(resultado);
		}
		
		if( !resultados.isEmpty() ) {
			getEntityManager().flush();
		}
		
	}
	
	/**
	 * Metodo responsavel por inativar lembrete pelo id
	 * @param idLembrete
	 */
	public void inativaLembretesPorId(Integer idLembrete) {
		Lembrete lembrete = getEntityManager().find(Lembrete.class, idLembrete);
		if (lembrete != null) {
			lembrete.setAtivo(false);
			getEntityManager().merge(lembrete);
			EntityUtil.flush(getEntityManager());
		}
	}
	

	/**
	 * Metodo responsavel por inativar lembrete por processoDocumento
	 * @param documento
	 */
	public void inativaLembretesPorDocumento(ProcessoDocumento documento) {
		String sql = "select l from Lembrete l where l.processoDocumento = :processoDocumento ";
		List<Lembrete> resultados = getEntityManager().createQuery(sql).setParameter("processoDocumento", documento).getResultList();
		for( Lembrete resultado : resultados ){
			resultado.setAtivo(false);
			getEntityManager().merge(resultado);
		}
		
		if( !resultados.isEmpty() ) {
			getEntityManager().flush();
		}
	}
	
	/**
	 * Metodo que recupera uma lista formatada de lembretes visivel por idProcessoTrf 
	 * verificando as permissoes de visibilidade em pessoa, papel e localizacao
	 * 
	 * @param idProcessoDocumento
	 * @param ativo
	 * @return List<LembreteVO>
	 */
	@SuppressWarnings("unchecked")
	public List<LembreteVO> recuperarLembretesPorSituacaoPorIdProcessoTrf(Boolean ativo, Integer idProcessoTrf) {
		return recuperarLembretesPorSituacaoPorIdProcessoTrf(ativo, idProcessoTrf, false);
	}

	/**
	 * Metodo responsável por retornar o HQL para pesquisa de lembretes de duas formas:
	 * <ul>
	 * 		<li> Por processo @param idProcesso ID do ProcessoTRF</li>
	 * 		<li> Por documento @param idProcessoDocumento ID do ProcessoDocumento</li>
	 * </ul>
	 * @return StringBuilder jpql
	 */
	private StringBuilder retornaHQLPesquisaProcesso() {
		int orgaoJulgador = Authenticator.getIdOrgaoJulgadorAtual() == null ? 0 : Authenticator.getIdOrgaoJulgadorAtual();
		int orgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual() == null ? 0 : Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.LembreteDTO(n.idLembrete, processoJudicial.idProcessoTrf ,processoDocumento.idProcessoDocumento,");
		jpql.append(" u.nome || ' (' || up.nome || ')', n.descricao ,n.dataVisivelAte, n.dataInclusao, n.ativo, ul.id) ");
		jpql.append(FROM_LEMBRETE);
		jpql.append(" JOIN n.processoTrf AS processoJudicial ");
		jpql.append(LEFT_JOIN_N_PROCESSO_DOCUMENTO_AS_PROCESSO_DOCUMENTO);
		jpql.append(JOIN_N_USUARIO_LOCALIZACAO_AS_UL);
		jpql.append(JOIN_UL_USUARIO_AS_U);
		jpql.append(JOIN_UL_PAPEL_AS_UP);
		jpql.append(JOIN_N_LEMBRETE_PERMISSAOS_AS_LP);
		jpql.append(" WHERE processoJudicial.idProcessoTrf in (:idProcessoTrf) ");
		jpql.append(AND_N_ATIVO_ATIVO);
		jpql.append(" AND ( n.dataVisivelAte >= :dataVisivelAte OR n.dataVisivelAte IS NULL ) ");
		jpql.append(AND_N_USUARIO_LOCALIZACAO_EQUALS + Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao() );
		jpql.append(OR_PARTENTESES);
		jpql.append("    (lp.orgaoJulgador.idOrgaoJulgador IS NULL  ");
		jpql.append("        and lp.papel.idPapel = " + Authenticator.getIdPapelAtual());
		jpql.append("        and lp.usuario.idUsuario = "+Authenticator.getIdUsuarioLogado());
		jpql.append("        and lp.localizacao.idLocalizacao = "+Authenticator.getLocalizacaoAtual().getIdLocalizacao());
		jpql.append("    ) or ");
		jpql.append("    ( lp.orgaoJulgador.idOrgaoJulgador = "+orgaoJulgador);
		jpql.append("        AND ( lp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado IS NULL or lp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+orgaoJulgadorColegiado+"  ) ");
		jpql.append("        AND ( lp.usuario.idUsuario is null OR lp.usuario.idUsuario = "+Authenticator.getIdUsuarioLogado()+" ) ");
		jpql.append("        AND lp.localizacao.idLocalizacao = "+Authenticator.getIdLocalizacaoAtual());
		jpql.append("        AND (lp.papel.idPapel is null OR lp.papel.idPapel = "+Authenticator.getIdPapelAtual()+" ) ");
		jpql.append("    )");
		jpql.append("    ) ");
		jpql.append(" ) ");
		jpql.append(ORDER_BY_N_DATA_VISIVEL_ATE_ASC);
		return jpql;
	}

	/**
	 * Metodo que recupera uma lista formatada de lembretes visivel por idProcessoTrf 
	 * verificando as permissoes de visibilidade em pessoa, papel e localizacao
	 * 
	 * @param idProcessoDocumento
	 * @param ativo
	 * @param todosDocumentos
	 * @return List<LembreteVO>
	 */
	@SuppressWarnings("unchecked")
	public List<LembreteVO> recuperarLembretesPorSituacaoPorIdProcessoTrf(Boolean ativo, Integer idProcessoTrf, Boolean todosDocumentos) {
		StringBuilder jpql = retornaHQLPesquisaProcessoDocumento(idProcessoTrf, null, todosDocumentos);
		Query query = getEntityManager().createQuery(jpql.toString()) ;
		query.setParameter("idProcessoTrf", idProcessoTrf);
		query.setParameter(ATIVO, (ativo == null || ativo));
		query.setParameter("dataVisivelAte", DateUtil.getDataAtual());
		return query.getResultList();
	}
	
	/**
	 * Metodo que verifica se existe lemprete visivel para o processo.
	 * 
	 * @param idProcessoTrf
	 * @param ativo
	 * @return Boolean (Verdadeiro ou Falso)
	 */
	@SuppressWarnings("unchecked")
	public Boolean verificaLembretesPorSituacaoPorIdProcessoTrf(Boolean ativo, Integer idProcessoTrf) {
		StringBuilder jpql = retornaHQLPesquisaProcessoDocumento(idProcessoTrf, null);
		Query query = getEntityManager().createQuery(jpql.toString()) ;
		query.setParameter("idProcessoTrf", idProcessoTrf);
		query.setParameter("ativo", (ativo == null || ativo) ? true : false);
		query.setParameter("dataVisivelAte", DateUtil.getDataAtual());
		query.setMaxResults(1);
		List<LembreteVO> rs = query.getResultList();
		return CollectionUtilsPje.isNotEmpty(rs);
	}
	
	/**
	 * Metodo que verifica se existe lemprete visivel para o documento.
	 * 
	 * @param idProcessoDocumento
	 * @param ativo
	 * @return Boolean (Verdadeiro ou Falso)
	 */
	@SuppressWarnings("unchecked")
	public Boolean verificaLembretesPorSituacaoPorIdProcessoDocumento(Boolean ativo, Integer idProcessoDocumento) {
		StringBuilder jpql = retornaHQLPesquisaProcessoDocumento(null, idProcessoDocumento);
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("idProcessoDocumento", idProcessoDocumento);
		query.setParameter("ativo", (ativo == null || ativo) ? true : false);
		query.setParameter("dataVisivelAte", DateUtil.getDataAtual());
		query.setMaxResults(1);
		List<LembreteVO> rs = query.getResultList();
		return CollectionUtilsPje.isNotEmpty(rs);
	}
	
	/**
	 * Metodo que recupera uma lista formatada de lembretes visivel por idProcessoDocumento e situa??o 
	 * verificando as permiss?es de visibilidade em pessoa, papel e localiza??o
	 * @param idProcessoDocumento
	 * @param ativo
	 * @return List<LembreteVO>
	 */
	@SuppressWarnings("unchecked")
	public List<LembreteVO> recuperarLembretesPorSituacaoPorIdProcessoDocumento(Boolean ativo, Integer idProcessoDocumento) {
		StringBuilder jpql = retornaHQLPesquisaProcessoDocumento(null, idProcessoDocumento);
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("idProcessoDocumento", idProcessoDocumento);
		query.setParameter("ativo", (ativo == null || ativo) ? true : false);
		query.setParameter("dataVisivelAte", DateUtil.getDataAtual());
		return query.getResultList();
	}

	/**
	 * Metodo responsavel por retornar o HQL para pesquisa de lembretes de duas formas:
	 * <ul>
	 * 		<li> Por processo @param idProcesso ID do ProcessoTRF</li>
	 * 		<li> Por documento @param idProcessoDocumento ID do ProcessoDocumento</li>
	 * </ul>
	 * @return StringBuilder jpql
	 */
	private StringBuilder retornaHQLPesquisaProcessoDocumento(Integer idProcesso, Integer idProcessoDocumento) {
		return retornaHQLPesquisaProcessoDocumento(idProcesso, idProcessoDocumento, false);
	}
	
	/**
	 * Metodo responsavel por retornar o HQL para pesquisa de lembretes de tres formas:
	 * <ul>
	 * 		<li> Por processo @param idProcesso ID do ProcessoTRF</li>
	 * 		<li> Por documento @param idProcessoDocumento ID do ProcessoDocumento</li>
	 * 		<li> Todos lembretes de todos documentos de um processo @param idProcesso ID do ProcessoTRF e @param todosDocumentos true</li>
	 * </ul>
	 * @return StringBuilder jpql
	 */
	private StringBuilder retornaHQLPesquisaProcessoDocumento(Integer idProcesso, Integer idProcessoDocumento, Boolean todosDocumentos) {
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT new br.jus.cnj.pje.entidades.vo.LembreteVO(n.idLembrete, processoTrf ,processoDocumento,");
		jpql.append(" u.nome || ' (' || up.nome || ')', n.descricao ,n.dataVisivelAte, n.dataInclusao, n.ativo, ul.idUsuarioLocalizacao, lp) ");
		jpql.append(" FROM Lembrete n ");
		jpql.append(" LEFT JOIN n.processoTrf AS processoTrf ");
		jpql.append(" LEFT JOIN n.processoDocumento AS processoDocumento ");
		jpql.append(" JOIN n.usuarioLocalizacao AS ul ");
 		jpql.append(" JOIN ul.usuario AS u ");
		jpql.append(" JOIN ul.papel AS up ");
		jpql.append(" JOIN n.lembretePermissaos AS lp ");
		if(idProcessoDocumento != null){
			jpql.append(" WHERE processoDocumento.idProcessoDocumento = :idProcessoDocumento ");
		}
		if(idProcesso != null){
			jpql.append(" WHERE processoTrf.idProcessoTrf = :idProcessoTrf ");
			if(todosDocumentos == null || !todosDocumentos){
				jpql.append(" AND processoDocumento.idProcessoDocumento IS NULL ");
			}

		}
		jpql.append(" AND n.ativo = :ativo ");
		jpql.append(" AND (n.dataVisivelAte >= :dataVisivelAte OR n.dataVisivelAte IS NULL) ");
		jpql.append(" AND ( n.usuarioLocalizacao = " + Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao() + " OR ( ");
		jpql.append(" ( lp.orgaoJulgador.idOrgaoJulgador IS NULL ");
		if (Authenticator.getIdOrgaoJulgadorAtual()!=null){
			jpql.append(" OR lp.orgaoJulgador.idOrgaoJulgador = "+Authenticator.getIdOrgaoJulgadorAtual()+" ");
		}
		
		if(Authenticator.getIdOrgaoJulgadorColegiadoAtual()!=null){
			jpql.append(" OR lp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+Authenticator.getIdOrgaoJulgadorColegiadoAtual()+" ");
		}
		jpql.append(" ) ");
		jpql.append(" AND ( lp.usuario.idUsuario is null OR lp.usuario.idUsuario = " + Authenticator.getIdUsuarioLogado() +" ) ");
		jpql.append(" AND ( lp.papel.idPapel is null OR lp.papel.idPapel = " + Authenticator.getIdPapelAtual() + " ) ");
		jpql.append(" AND ( lp.localizacao.idLocalizacao is null OR lp.localizacao.idLocalizacao = " + Authenticator.getIdLocalizacaoAtual() + " ) ) )");
		jpql.append(" ORDER BY n.dataVisivelAte ASC ");
		return jpql;
	}
	
	/**
	 * Metodo que retorna a quantidade de lempretes visivel para o documento.
	 * 
	 * @param idProcessoDocumento
	 * @param ativo
	 * @return Integer - Quantidade de lembretes dos parametros
	 */
	public Integer retornaQuantidadeLembretesPorSituacaoPorIdProcessoDocumento(Boolean ativo, Integer idProcessoDocumento) {
		StringBuilder jpql = retornaHQLContagemProcessoDocumento(null, idProcessoDocumento);
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("idProcessoDocumento", idProcessoDocumento);
		query.setParameter("ativo", (ativo == null || ativo) ? true : false);
		query.setParameter("dataVisivelAte", DateUtil.getDataAtual());
		return (Integer) query.getSingleResult();
	}
	
	/**
	 * Metodo que retorna a quantidade de lempretes visivel para o processo.
	 * 
	 * @param idProcessoTrf
	 * @param ativo
	 * @return Integer - Quantidade de lembretes dos parametros
	 */
	public Integer retornaQuantidadeLembretesPorSituacaoPorIdProcesso(Boolean ativo, Integer idProcesso) {
		StringBuilder jpql = retornaHQLContagemProcessoDocumento(null, idProcesso);
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("idProcessoTrf", idProcesso);
		query.setParameter("ativo", (ativo == null || ativo) ? true : false);
		query.setParameter("dataVisivelAte", DateUtil.getDataAtual());
		Integer qtd = (Integer) query.getSingleResult();
  		return qtd != null ? qtd : 0;
	}
	
	/**
	 * Metodo responsavel por retornar o HQL para a contagem de lembretes de duas formas:
	 * <ul>
	 * 		<li> Por processo @param idProcesso ID do ProcessoTRF</li>
	 * 		<li> Por documento @param idProcessoDocumento ID do ProcessoDocumento</li>
	 * </ul>
	 * @return StringBuilder jpql
	 */
	private StringBuilder retornaHQLContagemProcessoDocumento(Integer idProcesso, Integer idProcessoDocumento) {
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT cast(count(n.idLembrete) as integer) ");
		jpql.append(" FROM Lembrete n ");
		jpql.append(" LEFT JOIN n.processoTrf AS processoTrf ");
		jpql.append(" LEFT JOIN n.processoDocumento AS processoDocumento ");
		jpql.append(" JOIN n.usuarioLocalizacao AS ul ");
 		jpql.append(" JOIN ul.usuario AS u ");
		jpql.append(" JOIN ul.papel AS up ");
		jpql.append(" JOIN n.lembretePermissaos AS lp ");
		if(idProcessoDocumento != null){
			jpql.append(" WHERE processoDocumento.idProcessoDocumento = :idProcessoDocumento ");
		}
		if(idProcesso != null){
			jpql.append(" WHERE processoTrf.idProcessoTrf = :idProcessoTrf ");
			jpql.append(" AND processoDocumento.idProcessoDocumento IS NULL ");
		}
		jpql.append(" AND n.ativo = :ativo ");
		jpql.append(" AND (n.dataVisivelAte >= :dataVisivelAte OR n.dataVisivelAte IS NULL) ");
		jpql.append(" AND ( n.usuarioLocalizacao = " + Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao() + " OR ( ");
		jpql.append(" ( lp.orgaoJulgador.idOrgaoJulgador IS NULL ");
		if (Authenticator.getIdOrgaoJulgadorAtual()!=null){
			jpql.append(" OR lp.orgaoJulgador.idOrgaoJulgador = "+Authenticator.getIdOrgaoJulgadorAtual()+" ");
		}
		
		if(Authenticator.getIdOrgaoJulgadorColegiadoAtual()!=null){
			jpql.append(" OR lp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+Authenticator.getIdOrgaoJulgadorColegiadoAtual()+" ");
		}
		jpql.append(" ) ");
		jpql.append(" AND ( lp.usuario.idUsuario is null OR lp.usuario.idUsuario = " + Authenticator.getIdUsuarioLogado() +" ) ");
		jpql.append(" AND ( lp.papel.idPapel is null OR lp.papel.idPapel = " + Authenticator.getIdPapelAtual() + " ) ");
		jpql.append(" AND ( lp.localizacao.idLocalizacao is null OR lp.localizacao.idLocalizacao = " + Authenticator.getIdLocalizacaoAtual() + " ) ) )");
		return jpql;
	}
	
	/**
	 * Metodo responsável por retornar o HQL para pesquisa de lembretes de duas formas:
	 * <ul>
	 * 		<li> Por processo @param idProcesso ID do ProcessoTRF</li>
	 * 		<li> Por documento @param idProcessoDocumento ID do ProcessoDocumento</li>
	 * </ul>
	 * @return StringBuilder jpql
	 */
	private StringBuilder retornaHQLPesquisaProcessoDocumento() {
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.LembreteDTO(n.idLembrete, processoJudicial.idProcessoTrf ,processoDocumento.idProcessoDocumento,");
		jpql.append(" u.nome || ' (' || up.nome || ')', n.descricao ,n.dataVisivelAte, n.dataInclusao, n.ativo, ul.id) ");
		jpql.append(" FROM Lembrete n ");
		jpql.append(" LEFT JOIN n.processoTrf AS processoJudicial ");
		jpql.append(" LEFT JOIN n.processoDocumento AS processoDocumento ");
		jpql.append(" JOIN n.usuarioLocalizacao AS ul ");
 		jpql.append(" JOIN ul.usuario AS u ");
		jpql.append(" JOIN ul.papel AS up ");
		jpql.append(" JOIN n.lembretePermissaos AS lp ");
		jpql.append(" WHERE processoJudicial.idProcessoTrf in (:idProcessoTrf) ");
		jpql.append(" AND processoDocumento.idProcessoDocumento IS NULL ");
		jpql.append(" AND n.ativo = :ativo ");
		jpql.append(" AND (n.dataVisivelAte >= :dataVisivelAte OR n.dataVisivelAte IS NULL) ");
		jpql.append(" AND ( n.usuarioLocalizacao = " + Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao() + " OR ( ");
		jpql.append(" ( lp.orgaoJulgador.idOrgaoJulgador IS NULL ");
		if (Authenticator.getIdOrgaoJulgadorAtual()!=null){
			jpql.append(" OR lp.orgaoJulgador.idOrgaoJulgador = "+ Authenticator.getIdOrgaoJulgadorAtual() +" ");
		}
		jpql.append(" ) ");
		jpql.append(" AND ( lp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado IS NULL ");
		if(Authenticator.getIdOrgaoJulgadorColegiadoAtual()!=null){
			jpql.append(" OR lp.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+ Authenticator.getIdOrgaoJulgadorColegiadoAtual() +" ");
		}
		jpql.append(" ) ");
		jpql.append(" AND lp.usuario.idUsuario is null OR lp.usuario.idUsuario = " + Authenticator.getIdUsuarioLogado());
		jpql.append(" AND lp.localizacao.idLocalizacao = " + Authenticator.getIdLocalizacaoAtual());
		jpql.append(" AND lp.papel.idPapel is null OR lp.papel.idPapel = " + Authenticator.getIdPapelAtual() + " ) )");
		
		jpql.append(" ORDER BY n.dataVisivelAte ASC ");
		return jpql;
	}
	
	
	/**
	 * recupera todos os lembretes criados pela pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<Lembrete> recuperarLembretes(Pessoa _pessoa) throws Exception {
		List<Lembrete> resultado = null;
		Search search = new Search(Lembrete.class);
		try {
			search.addCriteria(Criteria.equals("usuarioLocalizacao.usuario.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os lembretes da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(".");
			sb.append(" Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
	
	/**
	 * Metodo que recupera uma lista formatada de lembretes visível por idsProcesso
	 * verificando as permissões de visibilidade em pessoa, papel e localização
	 * 
	 * @param idsProcessos
	 * @param ativo
	 * @return List<LembreteVO>
	 */
	@SuppressWarnings("unchecked")
	public List<LembreteDTO> recuperarLembretesPorSituacaoPorIdsProcesso(Boolean ativo, Set<Integer> idsProcessos) {
		List<LembreteDTO> lista = new ArrayList<>();
		if (idsProcessos != null && !idsProcessos.isEmpty()) {
			StringBuilder jpql = retornaHQLPesquisaProcesso();
			Query query = getEntityManager().createQuery(jpql.toString());
			query.setParameter("idProcessoTrf", idsProcessos);
			query.setParameter(ATIVO, (ativo == null || ativo));
			query.setParameter("dataVisivelAte", DateUtil.getBeginningOfToday());
			lista = query.getResultList();
		}
		return lista;
	}
	
	
}
