package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.vo.ConsultaPublicacaoSessaoVO;
import br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoLiberacaoEnum;
import br.jus.pje.nucleo.enums.TipoDecisaoPublicacaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(LiberacaoPublicacaoDecisaoDAO.NAME)
public class LiberacaoPublicacaoDecisaoDAO extends BaseDAO<LiberacaoPublicacaoDecisao> {
	public final static String NAME = "liberacaoPublicacaoDecisaoDAO";
	
	@In(required = true)
	private ParametroUtil parametroUtil;

	@Override
	public Integer getId(LiberacaoPublicacaoDecisao liberacaoPublicacaoDecisao) {
		return liberacaoPublicacaoDecisao.getIdLiberacaoPublicacaoDecisao();
	}

	public LiberacaoPublicacaoDecisao obterPorIdDocumento(Integer idDocumento) {
		LiberacaoPublicacaoDecisao retorno = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT liberacao FROM LiberacaoPublicacaoDecisao liberacao ");
			query.append(" WHERE liberacao.processoDocumento.idProcessoDocumento = :idDocumento");
			query.append(" ORDER BY liberacao.idLiberacaoPublicacaoDecisao DESC ");

			Query hql = getEntityManager().createQuery(query.toString());
			hql.setMaxResults(1);
			hql.setParameter("idDocumento", idDocumento);
			retorno = (LiberacaoPublicacaoDecisao) hql.getSingleResult();
		} catch (NoResultException e) {
			logger.info("Nenhum resultado encontrado para o id = [{0}]", idDocumento);
		}
		return retorno;
	}
	
	@SuppressWarnings("unchecked")
	public List<LiberacaoPublicacaoDecisao> pesquisar(ConsultaPublicacaoSessaoVO vo) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT liberacao FROM LiberacaoPublicacaoDecisao liberacao ");
		query.append(" JOIN liberacao.processoDocumento documento");
		query.append(" JOIN documento.processoTrf procTrf");
		query.append(" WHERE liberacao.situacaoPublicacaoLiberacao IN (:situacoes) ");

		processarCamposPesquisaProcesso(vo, query);

		if (vo.getDataSessao() != null) {
			query.append(" AND liberacao.dataSessao = :dataSessao ");
		}
		if (vo.getDataPublicacao() != null) {
			query.append(" AND liberacao.dataPublicacao = :dataPublicacao");
		}
		if (vo.getTipoPublicacao() != null) {
			query.append(" AND liberacao.tipoPublicacao = :tipoPublicacao");
		}
		if (vo.getOrgaoJulgador() != null) {
			query.append(" AND procTrf.orgaoJulgador = :orgaoJulgador");
		}

		Query querie = entityManager.createQuery(query.toString());
		querie.setParameter("situacoes", vo.getSituacaoLiberacao());

		montarParametrosPesquisaProcesso(vo, querie);

		if (vo.getDataSessao() != null) {
			querie.setParameter("dataSessao", vo.getDataSessao());
		}
		if (vo.getDataPublicacao() != null) {
			querie.setParameter("dataPublicacao", vo.getDataPublicacao());
		}
		if (vo.getTipoPublicacao() != null) {
			querie.setParameter("tipoPublicacao", vo.getTipoPublicacao());
		}
		if (vo.getOrgaoJulgador() != null) {
			querie.setParameter("orgaoJulgador", vo.getOrgaoJulgador());
		}

		List<LiberacaoPublicacaoDecisao> resultList = querie.getResultList();
		return resultList;
	}

	private void processarCamposPesquisaProcesso(ConsultaPublicacaoSessaoVO vo, StringBuilder query) {
		if (vo.getNumeroSequencia() != null && vo.getNumeroSequencia() > 0) {
			query.append(" AND procTrf.numeroSequencia = :nrSequencia");
		}
		if (vo.getDigitoVerificador() != null && vo.getDigitoVerificador() > 0) {
			query.append(" AND procTrf.numeroDigitoVerificador = :nrDigitoVerificador");
		}
		if (vo.getAno() != null && vo.getAno() > 0) {
			query.append(" AND procTrf.ano = :ano");
		}
		if (vo.getNumeroOrigem() != null && vo.getNumeroOrigem() > 0) {
			query.append(" AND procTrf.numeroOrigem = :nrOrigem");
		}
		if (StringUtils.isNotBlank(vo.getRamoJustica()) && StringUtils.isNotBlank(vo.getRespectivoTribunal())) {
			query.append(" AND procTrf.numeroOrgaoJustica = :nrOrgaoJustica");
		}
	}

	private void montarParametrosPesquisaProcesso(ConsultaPublicacaoSessaoVO vo, Query querie) {
		if (vo.getNumeroSequencia() != null && vo.getNumeroSequencia() > 0) {
			querie.setParameter("nrSequencia", vo.getNumeroSequencia());
		}
		if (vo.getDigitoVerificador() != null && vo.getDigitoVerificador() > 0) {
			querie.setParameter("nrDigitoVerificador", vo.getDigitoVerificador());
		}
		if (vo.getAno() != null && vo.getAno() > 0) {
			querie.setParameter("ano", vo.getAno());
		}
		if (vo.getNumeroOrigem() != null && vo.getNumeroOrigem() > 0) {
			querie.setParameter("nrOrigem", vo.getNumeroOrigem());
		}
		if (StringUtils.isNotBlank(vo.getRamoJustica()) && StringUtils.isNotBlank(vo.getRespectivoTribunal())) {
			querie.setParameter("nrOrgaoJustica", Integer.parseInt(vo.getRamoJustica() + vo.getRespectivoTribunal()));
		}
	}

	@SuppressWarnings("unchecked")
	public List<LiberacaoPublicacaoDecisao> pesquisarRelatorioDecisoesMonocraticasEmSessao(ConsultaPublicacaoSessaoVO consultaVO) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT DISTINCT liberacao FROM LiberacaoPublicacaoDecisao liberacao ");
		query.append(" 		JOIN FETCH liberacao.processoDocumento documento");
		query.append(" 		JOIN documento.processoTrf procTrf");
		query.append(" WHERE liberacao.situacaoPublicacaoLiberacao IN (:situacoes)");
		query.append(" AND liberacao.tipoPublicacao = :tpPublicacao");

		if (consultaVO.getDataSessao() != null) {
			query.append(" AND liberacao.dataSessao = :dataSessao");
		}
		if (consultaVO.getDataPublicacao() != null) {
			query.append(" AND liberacao.dataPublicacao = :dataPublicacao");
		}
		if (consultaVO.getOrgaoJulgador() != null) {
			query.append(" AND procTrf.orgaoJulgador = :orgaoJulgador");
		}

		query.append(" AND liberacao.tipoDecisaoPublicacao = :tpDecisao");
		query.append(" ORDER BY liberacao.numeroProcesso DESC");

		Query querie = getEntityManager().createQuery(query.toString());

		if (consultaVO.getDataSessao() != null) {
			querie.setParameter("dataSessao", consultaVO.getDataSessao());
		}
		if (consultaVO.getDataPublicacao() != null) {
			querie.setParameter("dataPublicacao", consultaVO.getDataPublicacao());
		}
		if (consultaVO.getOrgaoJulgador() != null) {
			querie.setParameter("orgaoJulgador", consultaVO.getOrgaoJulgador());
		}

		querie.setParameter("situacoes", SituacaoPublicacaoLiberacaoEnum.getListSituacaoPerfilGravarPublicacao());
		querie.setParameter("tpPublicacao", consultaVO.getTipoPublicacao());
		querie.setParameter("tpDecisao", TipoDecisaoPublicacaoEnum.MONOCRATICA);
		return querie.getResultList();
	}

	public LiberacaoPublicacaoDecisao recuperar(Integer idDocumento) {
		LiberacaoPublicacaoDecisao retorno = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT liberacao FROM LiberacaoPublicacaoDecisao liberacao ");
			query.append(" WHERE liberacao.processoDocumento.idProcessoDocumento = :idDocumento");
			query.append(" ORDER BY liberacao.idLiberacaoPublicacaoDecisao DESC ");

			Query querie = getEntityManager().createQuery(query.toString());
			querie.setMaxResults(1);
			querie.setParameter("idDocumento", idDocumento);
			retorno = (LiberacaoPublicacaoDecisao) querie.getSingleResult();
		} catch (NoResultException e) {
			logger.info("Nenhum resultado encontrado para o id = [{0}]", idDocumento);
		}
		return retorno;
	}

	/**
    * Metodo que identifica cria o objeto LiberacaoPublicacaoDecisao baseado na consulta de certidoes de publicacao deliberadas nas sessoes.
    * -Certidao pode ser um documento certidao (57) com o nome do documento contendo o texto 'julgamento'
    * -Certidao pode ser o id (145) que aponta para a certidao de julgamento
    * 
    * @param dataSessao
    * @return Lista de objetos criados
	*/
	@SuppressWarnings("unchecked")
	public List<LiberacaoPublicacaoDecisao> obterDecisoesJulgadasEmSessao(Date dataSessao) {
		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT new br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao (");
		sql.append(
				" 	sessaoJulgamento.dataSessao as dataSessao, sessaoJulgamento.dataSessao as dataPublicacao, processo.numeroProcesso as numeroProcesso, MAX (documento) as processoDocumento, sessaoJulgamento as sessao");
		sql.append(" )");
		sql.append(" FROM SessaoPautaProcessoTrf pautados");
		sql.append(" JOIN pautados.sessao sessaoJulgamento");
		sql.append(" JOIN pautados.processoTrf trf");
		sql.append(" JOIN trf.processo processo");
		sql.append(" JOIN processo.processoDocumentoList documento");
		sql.append(" JOIN documento.tipoProcessoDocumento tp_documento");
		sql.append(" WHERE pautados.situacaoJulgamento = :situacaoJulgamento");
		sql.append(" AND sessaoJulgamento.dataSessao IS NOT NULL");

		sql.append(
				" AND ((documento.processoDocumento LIKE lower(concat('%', TO_ASCII(:descJulgamento), '%')) AND documento.tipoProcessoDocumento.idTipoProcessoDocumento = :codDocCertidao)");
		sql.append("   OR   documento.tipoProcessoDocumento.idTipoProcessoDocumento = :codDocCertJulgamento");
		sql.append("     )");

		sql.append(" AND documento.idProcessoDocumento NOT IN (");
		sql.append(
				" 	SELECT publicacao.processoDocumento.idProcessoDocumento FROM LiberacaoPublicacaoDecisao publicacao");
		sql.append("    JOIN publicacao.processoDocumento subdoc");
		sql.append(" 	WHERE publicacao.dataSessao = sessaoJulgamento.dataSessao");
		sql.append(" ) ");
		sql.append(" AND sessaoJulgamento.dataSessao = :dataSessao");

		sql.append(" GROUP BY sessaoJulgamento.dataSessao, sessaoJulgamento.dataSessao, processo.numeroProcesso, sessaoJulgamento");

		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("situacaoJulgamento", TipoSituacaoPautaEnum.JG);
		query.setParameter("descJulgamento", "julgamento");
		query.setParameter("codDocCertidao", parametroUtil.getTipoProcessoDocumentoCertidao());
		query.setParameter("codDocCertJulgamento", parametroUtil.getTipoProcessoDocumentoCertidaoJulgamento().getIdTipoProcessoDocumento());
		query.setParameter("dataSessao", dataSessao);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<LiberacaoPublicacaoDecisao> obterDecisoesPendentesPublicacao() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT DISTINCT liberacao FROM LiberacaoPublicacaoDecisao liberacao ");
		query.append(" WHERE liberacao.situacaoPublicacaoLiberacao = :situacao");
		query.append(" 		AND liberacao.dataPublicacao <= :dataPublicacao");
		query.append(" ORDER BY liberacao.numeroProcesso DESC");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("dataPublicacao", DateUtil.getDataSemHora(new Date()));
		querie.setParameter("situacao", SituacaoPublicacaoLiberacaoEnum.PENDENTE_DE_PUBLICACAO);

		return querie.getResultList();
	}

	/**
	 * Metodo que retorna a lista de Datas do tipo de publicacao "Em Sessao"
	 * 
	 * @return Lista de Datas
	 */
	@SuppressWarnings("unchecked")
	public List<Date> obterDatasSessoesLiberacao() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT DISTINCT liberacao.dataSessao FROM LiberacaoPublicacaoDecisao liberacao ");
		query.append(" WHERE liberacao.situacaoPublicacaoLiberacao IN (:situacoes)");
		query.append(" ORDER BY liberacao.dataSessao DESC");

		Query querie = getEntityManager().createQuery(query.toString());
		querie.setParameter("situacoes", SituacaoPublicacaoLiberacaoEnum.getListSituacaoPerfilPublicar());

		return querie.getResultList();
	}
	
	public boolean verificarExistenciaLiberacao(ProcessoDocumento processoDocumento) {
		boolean retorno = false;
		StringBuilder query = new StringBuilder();
		query.append(" SELECT count(liberacao) FROM LiberacaoPublicacaoDecisao liberacao ");
		query.append(" WHERE liberacao.processoDocumento = :processoDocumento");
		Query hql = getEntityManager().createQuery(query.toString());
		hql.setParameter("processoDocumento", processoDocumento);
		hql.setMaxResults(1);
		Number qtd = (Number) hql.getSingleResult();
		retorno = qtd.intValue() > 0;
		return retorno;
	}
}
