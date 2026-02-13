package br.jus.cnj.pje.business.dao;


import br.com.itx.util.EntityUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.dto.SessaoJulgamentoFiltroDTO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import br.com.itx.util.ComponentUtil;

/**
 * Componente de acesso a dados da entidade {@link SessaoPautaProcessoTrf}.
 * 
 * @author cristof
 *
 */
@Name("sessaoPautaProcessoTrfDAO")
public class SessaoPautaProcessoTrfDAO extends BaseDAO<SessaoPautaProcessoTrf> {

	@Override
	public Integer getId(SessaoPautaProcessoTrf entradaPauta) {
		return entradaPauta.getIdSessaoPautaProcessoTrf();
	}
	
	public static SessaoPautaProcessoTrfDAO instance() {
		return ComponentUtil.getComponent(SessaoPautaProcessoTrfDAO.class);
	}

	/**
	 * Retorna uma lista dos anos das sessões de julgamento.
	 * 
	 * @return Lista dos anos das sessões de julgamento.
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getAnosSessaoJulgamento() {
		StringBuilder sb = new StringBuilder();
		sb.append("select YEAR(o.sessao.dataSessao) from SessaoPautaProcessoTrf o ");
		sb.append("group by YEAR(o.sessao.dataSessao) ");
		sb.append("order by 1 desc");
		Query query = getEntityManager().createQuery(sb.toString());
		return query.getResultList();
	}
	
	/**
	 * Retorna uma lista dos processos publicados de uma sessao
	 * @param processo O processo(SessaoPautaProcessoTrf) que sera verificado as datas
	 * em que foi publicado
	 * @return Lista dos processos publicados
	 */
	public ProcessoParteExpediente recuperarExpedientePublicadoDje(SessaoPautaProcessoTrf sessaoPautaProcesso) {
		StringBuilder sql = new StringBuilder().
				append("SELECT ppe ").
				append("  FROM SessaoPautaProcessoTrf AS spp,").
				append("       Sessao AS s,").
				append("       ProcessoExpediente AS pe,").
				append("       ProcessoParteExpediente AS ppe,").
				append("       PublicacaoDiarioEletronico AS dje,").
				append("       ProcessoTrf AS p").
				append(" WHERE spp.sessao.idSessao = s.idSessao").
				append("   AND spp.processoTrf.idProcessoTrf = p.idProcessoTrf").
				append("   AND pe.processoTrf.idProcessoTrf = p.idProcessoTrf").
				append("   AND pe.idProcessoExpediente = ppe.processoExpediente.idProcessoExpediente").
				append("   AND ppe.idProcessoParteExpediente = dje.processoParteExpediente.idProcessoParteExpediente").
				append("   AND pe.sessao.idSessao = s.idSessao").
				append("   AND pe.meioExpedicaoExpediente = 'P'").
				append("   AND dje.reciboPublicacaoDiarioEletronico IS NOT NULL").
				append("   AND s.idSessao = :idSessao").
				append("   AND p.idProcessoTrf = :idProcessoTrf").
				append(" ORDER BY pe.dtCriacao DESC");
		
		Query query = getEntityManager().createQuery(sql.toString());
		query.setMaxResults(1);
		query.setParameter("idSessao", sessaoPautaProcesso.getSessao().getIdSessao());
		query.setParameter("idProcessoTrf", sessaoPautaProcesso.getProcessoTrf().getIdProcessoTrf());
		
		ProcessoParteExpediente ppe = null;
		
		try {
			ppe = (ProcessoParteExpediente) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		
		return ppe;
	}

	/**
	 * Retorna uma lista dos anos das sessões de julgamento de um determinado processo.
	 * Retorna uma lista das sessões de julgamento com situação não julgada de
	 * um determinado processo ordenadas por apelido.
	 * 
	 * @param tipoAdiamento
	 *            Tipo / Motivo do adiamento da sessão não julgada
	 * @return List<SessaoPautaProcessoTrf> Lista das sessões de julgamentos do
	 *         processo.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> getSessoesNaoJulgadasPautadas(
			AdiadoVistaEnum tipoAdiamento, ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder(
				"select o from SessaoPautaProcessoTrf o ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("and o.situacaoJulgamento = :situacaoJulgamento ");
		sb.append("and o.adiadoVista = :adiadoVista ");
		sb.append("and o.dataExclusaoProcessoTrf is null ");
		sb.append("order by o.sessao.apelido ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		query.setParameter("adiadoVista", tipoAdiamento);
		query.setParameter("situacaoJulgamento", TipoSituacaoPautaEnum.NJ);

		return query.getResultList();
	}

	/**
	 * Retorna uma lista das sessões de julgamento de um determinado processo
	 * ordenadas por apelido. Caso a situação seja nula então este parâmentro
	 * será retirado da consulta retornando todoas sessões em que o processo não
	 * foi excluído da sessão.
	 * 
	 * @param situacao
	 *            Tipo de sistuação da sessão.
	 * @return List<SessaoPautaProcessoTrf> Lista das sessões de julgamentos do
	 *         processo.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> getSessoesJulgamentoPautados(
			TipoSituacaoPautaEnum situacao, ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder(
				"select o from SessaoPautaProcessoTrf o ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");

		if (situacao != null) {
			sb.append("and o.situacaoJulgamento = :situacaoJulgamento ");
		}

		sb.append("and o.dataExclusaoProcessoTrf is null ");
		sb.append("order by o.sessao.apelido ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());

		if (situacao != null) {
			query.setParameter("situacaoJulgamento", situacao);
		}

		return query.getResultList();
	}
	
	/**
	 * Metodo responsavel por retornar a ordem do processo dentro da sessao
	 * @param Sessao sessao
	 * @return List<Integer>
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> buscaOrdemProcessoSessao (Sessao sessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT numeroOrdem FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE sessao = :sessao ");
		sb.append("ORDER BY numeroOrdem ASC");
				
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessao);
		List<Integer> resultado = q.getResultList();
		return resultado;
	}

	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessosTrf(Integer idSessao, SessaoJulgamentoFiltroDTO sessaoJulgamentoFiltroDTO) throws Exception {
		return recuperarSessaoPautaProcessosTrf(idSessao, sessaoJulgamentoFiltroDTO, false, false, false);
		
	}
	
	/**
	 * metodo que recupera todos as SessaoPautaProcessoTrf 
	 * com o id de sessao e os filtros passados em parametro.
	 * 
	 * @param idSessao
	 * @param sessaoJulgamentoFiltroDTO
	 * @return List<SessaoPautaProcessoTrf>
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessosTrf(Integer idSessao, SessaoJulgamentoFiltroDTO sessaoJulgamentoFiltroDTO, 
			boolean excluirprocessosblocos, boolean especificarJulgados, boolean julgados) throws Exception {

		StringBuilder sb = new StringBuilder()
			.append("SELECT o FROM SessaoPautaProcessoTrf o ")
			.append("JOIN FETCH o.consultaProcessoTrf p ")
			.append("JOIN FETCH p.processoTrf q ")
			.append("JOIN FETCH q.processo ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf IS null ");
		
		if(especificarJulgados) {
			if(julgados) {
				sb.append("AND (o.situacaoJulgamento in('JG','NJ') OR o.adiadoVista in ('AD', 'PV')) ");
			} else {
				sb.append("AND (o.situacaoJulgamento not in ('JG', 'NJ') and o.adiadoVista is null)  ");
			}
		}
		
		//numero do processo
		if(sessaoJulgamentoFiltroDTO.getNumeroProcesso() != null && sessaoJulgamentoFiltroDTO.getNumeroProcesso().length() > 0) {
			sb.append("AND o.processoTrf.processo.numeroProcesso like '%"+sessaoJulgamentoFiltroDTO.getNumeroProcesso()+"%' ");
		}
		
		//assunto
		if(sessaoJulgamentoFiltroDTO.getCampoAssunto() != null) {
			sb.append("AND o.processoTrf in (Select processoTrf from ProcessoAssunto where assuntoTrf = :assunto) ");
		}
		
		//classe judicial
		if(sessaoJulgamentoFiltroDTO.getCampoClasse() != null) {
			sb.append("AND o.processoTrf.classeJudicial = :classeJudicial ");
		}
		
		//prioridade
		if(sessaoJulgamentoFiltroDTO.getPrioridade() != null) {
			sb.append("AND o.processoTrf in (Select processoTrf from ProcessoPrioridadeProcesso where prioridadeProcesso = :prioridadeProcesso) ");
		}
		
		//orgao julgador
		if(sessaoJulgamentoFiltroDTO.getOrgaoFiltro() != null) {
			sb.append("AND o.processoTrf.orgaoJulgador = :orgaoJulgador ");
		}
		
		//data autuacao (inicio e fim)
		if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() != null && sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() != null) {
			sb.append("AND o.processoTrf.dataDistribuicao between :dataInicio and :dataFinal ");
		}

		//data autuacao (inicio)
		else if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() != null && sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() == null) {
			sb.append("AND o.processoTrf.dataDistribuicao >= :dataInicio ");
		}
		
		//data autuacao (fim)
		else if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() == null && sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() != null) {
			sb.append("AND o.processoTrf.dataDistribuicao <= :dataFinal ");
		}
		
		//nome da parte
		if(sessaoJulgamentoFiltroDTO.getNomeParte() != null && !StringUtil.fullTrim(sessaoJulgamentoFiltroDTO.getNomeParte()).isEmpty()){
			sb.append("AND o.processoTrf in (SELECT processoTrf FROM ProcessoParte AS pp WHERE pp.pessoa.nome like '%")
					.append(StringUtil.fullTrim(sessaoJulgamentoFiltroDTO.getNomeParte().toUpperCase())).append("%' ) ");
		}
		
		//filtro CPF/CNPJ
		if (sessaoJulgamentoFiltroDTO.getCodigoIMF() != null && !sessaoJulgamentoFiltroDTO.getCodigoIMF().isEmpty()) { 
			InscricaoMFUtil.InscricaoMF inscricaoMF = InscricaoMFUtil.criarInscricaoMF(sessaoJulgamentoFiltroDTO.getCodigoIMF(), "CPF");
			sb.append("AND o.processoTrf in (");
				sb.append("SELECT processoTrf FROM ProcessoParte AS pp ");
				sb.append("WHERE pp.pessoa in (");
					sb.append("SELECT pessoa FROM PessoaDocumentoIdentificacao AS pdi ");
					sb.append("WHERE pdi.numeroDocumento = '"+inscricaoMF.inscricao+"' ");
					sb.append("AND pdi.ativo = true ");
					sb.append("AND pdi.usadoFalsamente = false ");
					sb.append("AND (pdi.tipoDocumento.codTipo = 'CPF' OR pdi.tipoDocumento.codTipo = 'CPJ')");
				sb.append(")");
			sb.append(") ");
		}
		
		//filtro codigoOAB
		if(sessaoJulgamentoFiltroDTO.getCodigoOAB() != null && !sessaoJulgamentoFiltroDTO.getCodigoOAB().isEmpty()){
			String oab = StringUtil.fullTrim(sessaoJulgamentoFiltroDTO.getCodigoOAB()).replaceAll(" ", "%");
			sb.append("AND o.processoTrf in (");
			sb.append("SELECT processoTrf FROM ProcessoParte AS pp ");
			sb.append("WHERE pp.pessoa in (");
				sb.append("SELECT pessoa FROM PessoaDocumentoIdentificacao AS pdi ");
				sb.append("WHERE pdi.numeroDocumento ='"+oab+"' ");
				sb.append("AND pdi.ativo = true ");
				sb.append("AND pdi.usadoFalsamente = false ");
				sb.append("AND pdi.tipoDocumento.codTipo = 'OAB' ");
			sb.append(")");
		sb.append(") ");
		}
		
		//tipo voto do relator
		if(sessaoJulgamentoFiltroDTO.getTipoVotoRelator() != null) {
			sb.append("AND o.processoTrf.idProcessoTrf in (:processosTipoVotoRelator)");
		}
		
		//sessaoResultadoVotacaoEnum
		if(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum() != null) {
			switch(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum()) {
			
				case UN:
					sb.append("AND o.idSessaoPautaProcessoTrf in (:processosVotoRelatorUnanime)");
					break;
				case NR:
					sb.append("AND o.idSessaoPautaProcessoTrf in (:processosVotoNaoRelatorVencedor)");
					break;
				case NU:
					sb.append("AND o.idSessaoPautaProcessoTrf in (:processosVotoRelatorMaioria)");
					break;
			}
		}
		
		//tipo de inclusão
		if(sessaoJulgamentoFiltroDTO.getTipoInclusaoEnum() != null) {
			sb.append("AND o.tipoInclusao = :tipoInclusao ");
		}
		
		if(sessaoJulgamentoFiltroDTO.getSituacaoProcessoSessaoEnum() != null) {
				switch (sessaoJulgamentoFiltroDTO.getSituacaoProcessoSessaoEnum()) {
				case AD:
					sb.append("AND o.adiadoVista = 'AD' ");
					sb.append("AND o.retiradaJulgamento = false ");
					break;
				case AJ:
					sb.append("AND o.situacaoJulgamento = 'AJ' ");
					break;
				case AN:
					sb.append("AND EXISTS (");
						sb.append("SELECT 1 FROM NotaSessaoJulgamento AS n  ");
						sb.append("WHERE n.processoTrf = o.processoTrf ");
						sb.append("AND n.ativo = true ");
						sb.append("AND n.sessao = o.sessao) ");
					break;
				case EJ:
					sb.append("AND o.situacaoJulgamento = 'EJ' ");
					break;
				case JG:
					sb.append("AND o.situacaoJulgamento = 'JG' ");
					break;
				case PR:
					sb.append("AND o.preferencia = true ");
					break;
				case PV:
					sb.append("AND o.adiadoVista = 'PV' ");
					break;
				case RJ:
					sb.append("AND o.situacaoJulgamento = 'NJ' ");
					sb.append("AND o.retiradaJulgamento = true ");
					break;
				case SO:
					sb.append("AND o.sustentacaoOral = true ");
					break;
				case JC:
					sb.append("AND o.maioriaDetectada = true ");
					break;	
				case DD:
					sb.append("AND exists (");
					sb.append("SELECT 1 FROM SessaoProcessoDocumentoVoto spdv ");
					sb.append("WHERE spdv.sessao = o.sessao ");
					sb.append("AND spdv.processoTrf = o.processoTrf ");
					sb.append("AND spdv.destaqueSessao = true) ");
					break;
			}
		}
		
		if(sessaoJulgamentoFiltroDTO.getPossuiProclamacaoAntecipada() != null) {
			String not = sessaoJulgamentoFiltroDTO.getPossuiProclamacaoAntecipada() ? " " : " not ";
			sb.append(" AND ");
			sb.append(" "+  not + " exists (");
				sb.append("SELECT 1 FROM SessaoProcessoDocumentoVoto spdv ");
				sb.append("WHERE spdv.sessao = o.sessao ");
				sb.append(" AND spdv.processoTrf = o.processoTrf ");
				sb.append("AND spdv.textoProclamacaoJulgamento is not null) ");
		}
		
		if(sessaoJulgamentoFiltroDTO.getTipoPessoa() != null) {
			sb.append("AND exists (");
				sb.append("SELECT 1 FROM ProcessoParte AS pp ");
				sb.append("WHERE pp.pessoa.tipoPessoa = :tipoPessoa AND pp.processoTrf = o.processoTrf ");
				sb.append("AND pp.inSituacao = 'A') ");
		}
		
		if(excluirprocessosblocos) {
			sb.append("AND not exists (");
			sb.append("SELECT 1 FROM ProcessoBloco processoBloco ");
			sb.append("WHERE processoBloco.bloco.sessao = o.sessao ");
			sb.append("AND processoBloco.processoTrf = o.processoTrf and processoBloco.ativo = true and processoBloco.bloco.ativo = true) ");
		}

		sb.append("ORDER BY o.numeroOrdem ASC");
		
		Query q = EntityUtil.createQuery(getEntityManager(), sb, false, true, "SessaoPautaProcessoTrfDAO.recuperarSessaoPautaProcessosTrf: " + sb);
		
		q.setParameter("idSessao", idSessao);
		if(sessaoJulgamentoFiltroDTO.getCampoAssunto() != null) {
			q.setParameter("assunto", sessaoJulgamentoFiltroDTO.getCampoAssunto());
		}
		if(sessaoJulgamentoFiltroDTO.getCampoClasse() != null) {
			q.setParameter("classeJudicial", sessaoJulgamentoFiltroDTO.getCampoClasse());
		}
		if(sessaoJulgamentoFiltroDTO.getPrioridade() != null) {
			q.setParameter("prioridadeProcesso", sessaoJulgamentoFiltroDTO.getPrioridade());
		}
		if(sessaoJulgamentoFiltroDTO.getOrgaoFiltro() != null) {
			q.setParameter("orgaoJulgador", sessaoJulgamentoFiltroDTO.getOrgaoFiltro());
		}
		if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() != null) {
			q.setParameter("dataInicio", sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao());
		}		
		if(sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() != null) {
			q.setParameter("dataFinal", sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao());
		}
		if(sessaoJulgamentoFiltroDTO.getTipoVotoRelator() != null) {
			List<Integer> processoList = obtemProcessosVotoRelator(idSessao, sessaoJulgamentoFiltroDTO.getTipoVotoRelator());
			q.setParameter("processosTipoVotoRelator", processoList.isEmpty() ? Arrays.asList(-1) : processoList);
		}
		if(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum() != null) {
			switch(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum()) {
			
				case UN:
					q.setParameter("processosVotoRelatorUnanime", obtemIdSessaoPautaProcessoVotacaoUnanime(idSessao));
					break;
				case NR:
					q.setParameter("processosVotoNaoRelatorVencedor", obtemIdSessaoPautaProcessoRelatorMinoria(idSessao));
					break;
				case NU:
					q.setParameter("processosVotoRelatorMaioria", obtemIdSessaoPautaProcessoRelatorMaioria(idSessao));
					break;
			}
		}
		if(sessaoJulgamentoFiltroDTO.getTipoInclusaoEnum() != null) {
			q.setParameter("tipoInclusao", sessaoJulgamentoFiltroDTO.getTipoInclusaoEnum());
		}
		if(sessaoJulgamentoFiltroDTO.getTipoPessoa() != null) {
			q.setParameter("tipoPessoa", sessaoJulgamentoFiltroDTO.getTipoPessoa());
		}

		return q.getResultList();
	}
	
	/**
	 * metodo que recupera todos as SessaoPautaProcessoTrf 
	 * com o id de sessao e os filtros passados em parametro.
	 * 
	 * @deprecated
	 * <p> Usar o método {@link SessaoPautaProcessoTrfDAO#recuperarSessaoPautaProcessosTrf(Integer, SessaoJulgamentoFiltroDTO, boolean, boolean, boolean)}
	 * 
	 * @param idSessao
	 * @param sessaoJulgamentoFiltroDTO
	 * @return List<SessaoPautaProcessoTrf>
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessosTrfJulgados(Integer idSessao, SessaoJulgamentoFiltroDTO sessaoJulgamentoFiltroDTO, boolean excluirprocessosblocos) throws Exception {
		List<SessaoPautaProcessoTrf> resultado = null;
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE sessao.idSessao = :idSessao ");
		sb.append("AND o.dataExclusaoProcessoTrf IS null AND o.situacaoJulgamento = 'JG' ");
		
		//numero do processo
		if(sessaoJulgamentoFiltroDTO.getNumeroProcesso() != null && sessaoJulgamentoFiltroDTO.getNumeroProcesso().length() > 0) {
			sb.append("AND processoTrf.processo.numeroProcesso like '%"+sessaoJulgamentoFiltroDTO.getNumeroProcesso()+"%' ");
		}
		
		//assunto
		if(sessaoJulgamentoFiltroDTO.getCampoAssunto() != null) {
			sb.append("AND processoTrf in (Select processoTrf from ProcessoAssunto where assuntoTrf = :assunto) ");
		}
		
		//classe judicial
		if(sessaoJulgamentoFiltroDTO.getCampoClasse() != null) {
			sb.append("AND processoTrf.classeJudicial = :classeJudicial ");
		}
		
		//prioridade
		if(sessaoJulgamentoFiltroDTO.getPrioridade() != null) {
			sb.append("AND processoTrf in (Select processoTrf from ProcessoPrioridadeProcesso where prioridadeProcesso = :prioridadeProcesso) ");
		}
		
		//orgao julgador
		if(sessaoJulgamentoFiltroDTO.getOrgaoFiltro() != null) {
			sb.append("AND processoTrf.orgaoJulgador = :orgaoJulgador ");
		}
		
		//data autuacao (inicio e fim)
		if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() != null && sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() != null) {
			sb.append("AND processoTrf.dataDistribuicao between :dataInicio and :dataFinal ");
		}

		//data autuacao (inicio)
		else if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() != null && sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() == null) {
			sb.append("AND processoTrf.dataDistribuicao >= :dataInicio ");
		}
		
		//nome da parte
		if(sessaoJulgamentoFiltroDTO.getNomeParte() != null && !StringUtil.fullTrim(sessaoJulgamentoFiltroDTO.getNomeParte()).isEmpty()){
			sb.append("AND processoTrf in ("
					+ "SELECT processoTrf FROM ProcessoParte AS pp "
					+ "WHERE pp.pessoa.nome like '%"+StringUtil.fullTrim(sessaoJulgamentoFiltroDTO.getNomeParte())+"%' "
					+ ") ");
		}
		
		//filtro CPF/CNPJ
		if (sessaoJulgamentoFiltroDTO.getCodigoIMF() != null && !sessaoJulgamentoFiltroDTO.getCodigoIMF().isEmpty()) { 
			InscricaoMFUtil.InscricaoMF inscricaoMF = InscricaoMFUtil.criarInscricaoMF(sessaoJulgamentoFiltroDTO.getCodigoIMF(), "CPF");
			sb.append("AND processoTrf in (");
				sb.append("SELECT processoTrf FROM ProcessoParte AS pp ");
				sb.append("WHERE pp.pessoa in (");
					sb.append("SELECT pessoa FROM PessoaDocumentoIdentificacao AS pdi ");
					sb.append("WHERE pdi.numeroDocumento = '"+inscricaoMF.inscricao+"' ");
					sb.append("AND pdi.ativo = true ");
					sb.append("AND pdi.usadoFalsamente = false ");
					sb.append("AND (pdi.tipoDocumento.codTipo = 'CPF' OR pdi.tipoDocumento.codTipo = 'CPJ')");
				sb.append(")");
			sb.append(") ");
		}
		
		//filtro codigoOAB
		if(sessaoJulgamentoFiltroDTO.getCodigoOAB() != null && !sessaoJulgamentoFiltroDTO.getCodigoOAB().isEmpty()){
			String oab = StringUtil.fullTrim(sessaoJulgamentoFiltroDTO.getCodigoOAB()).replaceAll(" ", "%");
			sb.append("AND processoTrf in (");
			sb.append("SELECT processoTrf FROM ProcessoParte AS pp ");
			sb.append("WHERE pp.pessoa in (");
				sb.append("SELECT pessoa FROM PessoaDocumentoIdentificacao AS pdi ");
				sb.append("WHERE pdi.numeroDocumento ='"+oab+"' ");
				sb.append("AND pdi.ativo = true ");
				sb.append("AND pdi.usadoFalsamente = false ");
				sb.append("AND pdi.tipoDocumento.codTipo = 'OAB' ");
			sb.append(")");
		sb.append(") ");
		}
		
		//tipo voto do relator
		if(sessaoJulgamentoFiltroDTO.getTipoVotoRelator() != null) {
			sb.append("AND processoTrf.idProcessoTrf in (:processosTipoVotoRelator)");
		}
		
		//sessaoResultadoVotacaoEnum
		if(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum() != null) {
			switch(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum()) {
			
				case UN:
					sb.append("AND idSessaoPautaProcessoTrf in (:processosVotoRelatorUnanime)");
					break;
				case NR:
					sb.append("AND idSessaoPautaProcessoTrf in (:processosVotoNaoRelatorVencedor)");
					break;
				case NU:
					sb.append("AND idSessaoPautaProcessoTrf in (:processosVotoRelatorMaioria)");
					break;
			}
		}
		
		//tipo de inclusão
		if(sessaoJulgamentoFiltroDTO.getTipoInclusaoEnum() != null) {
			sb.append("AND tipoInclusao = :tipoInclusao ");
		}
		
		if(sessaoJulgamentoFiltroDTO.getSituacaoProcessoSessaoEnum() != null) {
				switch (sessaoJulgamentoFiltroDTO.getSituacaoProcessoSessaoEnum()) {
				case AD:
					sb.append("AND o.adiadoVista = 'AD' ");
					break;
				case AJ:
					sb.append("AND o.situacaoJulgamento = 'AJ' ");
					break;
				case AN:
					sb.append("AND EXISTS (");
						sb.append("SELECT 1 FROM NotaSessaoJulgamento AS n  ");
						sb.append("WHERE n.processoTrf = o.processoTrf ");
						sb.append("AND n.ativo = true ");
						sb.append("AND n.sessao = o.sessao) ");
					break;
				case EJ:
					sb.append("AND o.situacaoJulgamento = 'EJ' ");
					break;
				case JG:
					sb.append("AND o.situacaoJulgamento = 'JG' ");
					break;
				case PR:
					sb.append("AND o.preferencia = true ");
					break;
				case PV:
					sb.append("AND o.adiadoVista = 'PV' ");
					break;
				case RJ:
					sb.append("AND o.situacaoJulgamento = 'NJ' ");
					break;
				case SO:
					sb.append("AND o.sustentacaoOral = true ");
					break;
				case JC:
					sb.append("AND o.maioriaDetectada = true ");
					break;	
				case DD:
					sb.append("AND exists (");
					sb.append("SELECT 1 FROM SessaoProcessoDocumentoVoto spdv ");
					sb.append("WHERE spdv.sessao = o.sessao ");
					sb.append("AND spdv.processoTrf = o.processoTrf ");
					sb.append("AND spdv.destaqueSessao = true) ");
					break;
			}
		}
		
		if(sessaoJulgamentoFiltroDTO.getPossuiProclamacaoAntecipada() != null) {
			String not = sessaoJulgamentoFiltroDTO.getPossuiProclamacaoAntecipada() ? " " : " not ";
			sb.append(" AND ");
			sb.append(" "+  not + " exists (");
				sb.append("SELECT 1 FROM SessaoProcessoDocumentoVoto spdv ");
				sb.append("WHERE spdv.sessao = o.sessao ");
				sb.append(" AND spdv.processoTrf = o.processoTrf ");
				sb.append("AND spdv.textoProclamacaoJulgamento is not null) ");
		}
		
		if(excluirprocessosblocos) {
			sb.append("AND not exists (");
			sb.append("SELECT 1 FROM ProcessoBloco processoBloco ");
			sb.append("WHERE processoBloco.bloco.sessao = o.sessao ");
			sb.append("AND processoBloco.processoTrf = o.processoTrf and processoBloco.ativo = true and processoBloco.bloco.ativo = true) ");
		}

		sb.append("ORDER BY numeroOrdem ASC");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		if(sessaoJulgamentoFiltroDTO.getCampoAssunto() != null) {
			q.setParameter("assunto", sessaoJulgamentoFiltroDTO.getCampoAssunto());
		}
		if(sessaoJulgamentoFiltroDTO.getCampoClasse() != null) {
			q.setParameter("classeJudicial", sessaoJulgamentoFiltroDTO.getCampoClasse());
		}
		if(sessaoJulgamentoFiltroDTO.getPrioridade() != null) {
			q.setParameter("prioridadeProcesso", sessaoJulgamentoFiltroDTO.getPrioridade());
		}
		if(sessaoJulgamentoFiltroDTO.getOrgaoFiltro() != null) {
			q.setParameter("orgaoJulgador", sessaoJulgamentoFiltroDTO.getOrgaoFiltro());
		}
		if(sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao() != null) {
			q.setParameter("dataInicio", sessaoJulgamentoFiltroDTO.getDataInicialDistribuicao());
		}		
		if(sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao() != null) {
			q.setParameter("dataFinal", sessaoJulgamentoFiltroDTO.getDataFinalDistribuicao());
		}
		if(sessaoJulgamentoFiltroDTO.getTipoVotoRelator() != null) {
			q.setParameter("processosTipoVotoRelator", obtemProcessosVotoRelator(idSessao, sessaoJulgamentoFiltroDTO.getTipoVotoRelator()));
		}
		if(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum() != null) {
			switch(sessaoJulgamentoFiltroDTO.getSessaoResultadoVotacaoEnum()) {
			
				case UN:
					q.setParameter("processosVotoRelatorUnanime", obtemIdSessaoPautaProcessoVotacaoUnanime(idSessao));
					break;
				case NR:
					q.setParameter("processosVotoNaoRelatorVencedor", obtemIdSessaoPautaProcessoRelatorMinoria(idSessao));
					break;
				case NU:
					q.setParameter("processosVotoRelatorMaioria", obtemIdSessaoPautaProcessoRelatorMaioria(idSessao));
					break;
			}
		}
		if(sessaoJulgamentoFiltroDTO.getTipoInclusaoEnum() != null) {
			q.setParameter("tipoInclusao", sessaoJulgamentoFiltroDTO.getTipoInclusaoEnum());
		}
		
		try{
			resultado = q.getResultList();
		}catch (NoResultException ex) {
			resultado = null;
		}
		return resultado;
	}
	
	/**
	 * metodo para obter os idSessaoPautaProcessoTrf onde a votacao foi unanime.
	 * @param idSessao
	 * @return lista com os ids das SessaoPautaProcessoTrf. se nao encontrar nenhum resultado, retorna (-1).
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> obtemIdSessaoPautaProcessoVotacaoUnanime(Integer idSessao) {
		List<Integer> resultado = new ArrayList<Integer>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.idSessaoPautaProcessoTrf FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.dataExclusaoProcessoTrf IS NULL ");
		sb.append("AND o.sessao.idSessao = :idSessao ");
		sb.append("AND NOT EXISTS (");
			sb.append("SELECT s.idSessaoProcessoDocumento FROM SessaoProcessoDocumentoVoto s  ");
			sb.append("WHERE o.sessao = s.sessao and s.processoTrf = o.processoTrf ");
			sb.append("AND s.ojAcompanhado != o.processoTrf.orgaoJulgador ");
			sb.append("AND s.liberacao = true) ");
		sb.append("AND EXISTS (");
			sb.append("SELECT s.idSessaoProcessoDocumento FROM SessaoProcessoDocumentoVoto s  ");
			sb.append("WHERE o.sessao = s.sessao ");
			sb.append("AND s.processoTrf = o.processoTrf ");
			sb.append("AND s.ojAcompanhado = o.processoTrf.orgaoJulgador ");
			sb.append("AND s.liberacao = true) ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		resultado = (List<Integer>) q.getResultList();
		if(resultado.isEmpty()) {	
			resultado.add(-1);
		}
		return resultado;
	}
	
	
	
	/**
	 * metodo para obter os idSessaoPautaProcessoTrf onde durante a votacao o relator foi vencido.
	 * @param idSessao
	 * @return lista com os ids das SessaoPautaProcessoTrf. se nao encontrar nenhum resultado, retorna (-1).
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> obtemIdSessaoPautaProcessoRelatorMinoria(Integer idSessao) {
		List<Integer> resultado = new ArrayList<Integer>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.idSessaoPautaProcessoTrf FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.dataExclusaoProcessoTrf IS NULL ");
		sb.append("AND o.sessao.idSessao = :idSessao ");
		sb.append("AND (");
			sb.append("SELECT COUNT(s1.idSessaoProcessoDocumento) FROM SessaoProcessoDocumentoVoto s1  ");
			sb.append("WHERE o.sessao = s1.sessao ");
			sb.append("AND s1.processoTrf = o.processoTrf ");
			sb.append("AND s1.ojAcompanhado != o.processoTrf.orgaoJulgador ");
			sb.append("AND s1.liberacao = true) ");
		sb.append(" > ");
			sb.append("(SELECT COUNT(s2.idSessaoProcessoDocumento) FROM SessaoProcessoDocumentoVoto s2 ");
			sb.append("WHERE o.sessao = s2.sessao ");
			sb.append("AND s2.processoTrf = o.processoTrf ");
			sb.append("AND s2.ojAcompanhado = o.processoTrf.orgaoJulgador ");
			sb.append("AND s2.liberacao = true) ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		resultado = (List<Integer>) q.getResultList();
		if(resultado.isEmpty()) {	
			resultado.add(-1);
		}
		return resultado;
	}

	/**
	 * metodo para obter os idSessaoPautaProcessoTrf onde durante a votacao o relator foi vencedor.
	 * @param idSessao
	 * @return lista com os ids das SessaoPautaProcessoTrf. se nao encontrar nenhum resultado, retorna (-1).
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> obtemIdSessaoPautaProcessoRelatorMaioria(Integer idSessao) {
		List<Integer> resultado = new ArrayList<Integer>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.idSessaoPautaProcessoTrf FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.dataExclusaoProcessoTrf IS NULL ");
		sb.append("AND o.sessao.idSessao = :idSessao ");
		sb.append("AND EXISTS (");
			sb.append("SELECT s.idSessaoProcessoDocumento FROM SessaoProcessoDocumentoVoto s  ");
			sb.append("WHERE o.sessao = s.sessao ");
			sb.append("AND s.processoTrf = o.processoTrf ");
			sb.append("AND s.ojAcompanhado != o.processoTrf.orgaoJulgador ");
			sb.append("AND s.liberacao = true)");
		sb.append("AND   (");
			sb.append("SELECT COUNT(s1.idSessaoProcessoDocumento) FROM SessaoProcessoDocumentoVoto s1  ");
			sb.append("WHERE o.sessao = s1.sessao ");
			sb.append("AND s1.processoTrf = o.processoTrf ");
			sb.append("AND s1.ojAcompanhado = o.processoTrf.orgaoJulgador ");
			sb.append("AND s1.liberacao = true) ");
		sb.append(" > ");
			sb.append("(SELECT COUNT(s2.idSessaoProcessoDocumento) FROM SessaoProcessoDocumentoVoto s2 ");
			sb.append("WHERE o.sessao = s2.sessao ");
			sb.append("AND s2.processoTrf = o.processoTrf ");
			sb.append("AND s2.ojAcompanhado != o.processoTrf.orgaoJulgador ");
			sb.append("AND s2.liberacao = true) ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		resultado = (List<Integer>) q.getResultList();
		if(resultado.isEmpty()) {	
			resultado.add(-1);
		}
		return resultado;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> obtemProcessosVotoRelator(Integer idSessao, TipoVoto tipoVotoRelator) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pdv.id_processo_trf FROM client.tb_sessao_proc_doc_voto AS pdv "
				+ "INNER JOIN client.tb_sessao_proc_documento AS spd "
				+ "ON spd.id_sessao_processo_documento = pdv.id_sessao_proc_documento_voto "
				+ "WHERE "
				+ "spd.id_sessao = :idSessao AND "
				+ "pdv.id_tipo_voto = :idTipoVoto "
				+ "AND id_orgao_julgador = id_oj_acompanhado");
						
		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idTipoVoto", tipoVotoRelator.getIdTipoVoto());
		List<Integer> resultado = (List<Integer>) q.getResultList();
		return resultado;
	}

	/**
	 * metodo responsavel por retornar a SessaoPautaProcessoTrf do processoTrf e da Sessao passadas em parametro.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public SessaoPautaProcessoTrf recuperarSessaoPautaProcessoTrf(Integer idProcessoTrf, Integer idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		SessaoPautaProcessoTrf resultado = (SessaoPautaProcessoTrf) q.getSingleResult();
		return resultado;
	}

	/**
	 * metodo responsavel por trazer a situaçao do processo na sessao. 
	 * somente traz o campo situaçao do processo.
	 * 
	 * @param idProcessoTrf
	 * @return
	 */
	public Map<Integer,TipoSituacaoPautaEnum> recuperarTipoSituacaoEnum(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.situacaoJulgamento FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
				
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
				
		return mapearProcessoValor(q.getResultList());
	}
	
	/**
	 * metodo responsavel por trazer a situaçao do processo na sessao. 
	 * somente traz o campo situaçao do processo.
	 * 
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public TipoSituacaoPautaEnum recuperarTipoSituacaoEnum(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.situacaoJulgamento FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
				
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		TipoSituacaoPautaEnum resultado = (TipoSituacaoPautaEnum) q.getSingleResult();
		return resultado;
	}

	/**
	 *  metodo responsavel por trazer somente o campo AdiadoVista do processo na sessao. 
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public AdiadoVistaEnum buscarAdiadoVista(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.adiadoVista FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		AdiadoVistaEnum resultado = (AdiadoVistaEnum) q.getSingleResult();
		return resultado;
	}

	private static <T>Map<Integer,T> mapearProcessoValor(List resultList) {
		List<Object[]> objList = resultList;
		Map<Integer,T> map = new HashMap<>();
		for (Object[] rowObj: objList) {
			map.put((Integer)rowObj[0], (T)rowObj[1]);
		}
		return map;
	}
	
	/**
	 *  metodo responsavel por trazer somente o campo AdiadoVista do processo na sessao. 
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,AdiadoVistaEnum> buscarAdiadoVista(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.adiadoVista FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
				
		return mapearProcessoValor(q.getResultList());
	}

	/**
	 * metodo responsavel por trazer somente o campo retiradaJulgamento do processo na sessao. 
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public boolean buscarRetiradaJulgamento(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.retiradaJulgamento FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		boolean resultado = (Boolean) q.getSingleResult();
		return resultado;
	}

	/**
	 * metodo responsavel por trazer somente o campo retiradaJulgamento do processo na sessao. 
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,Boolean> buscarRetiradaJulgamento(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.retiradaJulgamento FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}

	/**
	 * metodo responsavel por trazer somente o campo orgaoJulgadorRetiradaJulgamento do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public OrgaoJulgador buscarOrgaoJulgadorRetiradaJulgamento(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.orgaoJulgadorRetiradaJulgamento FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		try{
			return (OrgaoJulgador) q.getSingleResult();
		}catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * metodo responsavel por trazer somente o campo orgaoJulgadorRetiradaJulgamento do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,OrgaoJulgador> buscarOrgaoJulgadorRetiradaJulgamento(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.orgaoJulgadorRetiradaJulgamento FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}
	
	/**
	 * metodo responsavel por trazer somente o campo orgaoJulgadorVencedor do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,OrgaoJulgador> buscarOrgaoJulgadorVencedorJulgamento(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.orgaoJulgadorVencedor FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}

	/**
	 * metodo responsavel por trazer somente o campo orgaoJulgadorVencedor do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public OrgaoJulgador buscarOrgaoJulgadorVencedorJulgamento(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.orgaoJulgadorVencedor FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		try{
			return (OrgaoJulgador) q.getSingleResult();
		}catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * metodo responsavel por trazer somente o campo preferencia do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Boolean buscarPreferencia(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.preferencia FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		Boolean resultado = (Boolean) q.getSingleResult();
		return resultado;
	}
	
	/**
	 * metodo responsavel por trazer somente o campo preferencia do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,Boolean> buscarPreferencia(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.preferencia FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}
	
	/**
	 * metodo responsavel por trazer somente o campo maioriaDetectada do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,Boolean> buscarMaioriaDetectada(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.maioriaDetectada FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}
	
	/**
	 * metodo responsavel por trazer somente o campo maioriaDetectada do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Boolean buscarMaioriaDetectada(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.maioriaDetectada FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		Boolean resultado = (Boolean) q.getSingleResult();
		return resultado;
	}
	
	/**
	 * metodo responsavel por trazer somente o campo sustentacaoOral do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Boolean buscarSustentacaoOral(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.sustentacaoOral FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		Boolean resultado = (Boolean) q.getSingleResult();
		return resultado;
	}

	/**
	 * metodo responsavel por trazer somente o campo sustentacaoOral do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,Boolean> buscarSustentacaoOral(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.sustentacaoOral FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}

	/**
	 * metodo responsavel por trazer somente o campo advogadoSustentacaoOral do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public String buscarAdvogadoSustentacaoOral(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.advogadoSustentacaoOral FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		String resultado = (String) q.getSingleResult();
		return resultado;
	}

	/**
	 * metodo responsavel por trazer somente o campo advogadoSustentacaoOral do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,String> buscarAdvogadoSustentacaoOral(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.advogadoSustentacaoOral FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}

	/**
	 * metodo responsavel por trazer somente o campo proclamacaoDecisao do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public String buscarProclamacaoDecisaoOral(int idProcessoTrf, int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.proclamacaoDecisao FROM SessaoPautaProcessoTrf o ");
		sb.append("WHERE o.sessao.idSessao = :idSessao ");
		sb.append("AND o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", idSessao);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		String resultado = (String) q.getSingleResult();
		return resultado;
	}
	
	/**
	 * metodo responsavel por trazer somente o campo proclamacaoDecisao do processo na sessao.
	 * @param idProcessoTrf
	 * @param idSessao
	 * @return
	 */
	public Map<Integer,String> buscarProclamacaoDecisaoOral(int idSessao) {
		StringBuilder sb = new StringBuilder(300)
			.append("SELECT o.processoTrf.idProcessoTrf, o.proclamacaoDecisao FROM SessaoPautaProcessoTrf o ")
			.append("WHERE o.sessao.idSessao = :idSessao ")
			.append("AND o.dataExclusaoProcessoTrf is null ");
		
		Query q = getEntityManager().createQuery(sb.toString())
				.setHint("org.hibernate.fetchSize", 500)
				.setParameter("idSessao", idSessao);
		
		return mapearProcessoValor(q.getResultList());
	}
	
	/**
	 * metodo responsavel por recuperar todos as sessoesPautaProcessoTrf da pessoa passada em parametro.
	 * @param pessoaInclusora
	 * @param isBuscaPessoaInclusora - flag que indica se a busca é por pessoa inclusora(true) ou por pessoa exclusora(false)
	 * @return
	 * @throws Exception 
	 */
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcesso(Pessoa _pessoa, boolean isBuscaPessoaInclusora) throws Exception {
		List<SessaoPautaProcessoTrf> resultado = null;
		Search search = new Search(SessaoPautaProcessoTrf.class);
		try {
			if(isBuscaPessoaInclusora) {
				search.addCriteria(Criteria.equals("usuarioInclusao.idUsuario", _pessoa.getIdPessoa()));			
			}else {
				search.addCriteria(Criteria.equals("usuarioExclusao.idUsuario", _pessoa.getIdPessoa()));	
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar as sessoes Pauta Processos da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
	
	@SuppressWarnings("unchecked")
	public SessaoPautaProcessoTrf recuperaUltimaPautaProcessoNaoExcluido(ProcessoTrf processoTrf){
		List<SessaoPautaProcessoTrf> processos = new ArrayList<SessaoPautaProcessoTrf>(0);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct o FROM SessaoPautaProcessoTrf o where o.processoTrf = :processo " );
		sql.append("and o.dataExclusaoProcessoTrf is null order by o.idSessaoPautaProcessoTrf desc ");
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("processo", processoTrf);
		processos = q.getResultList();
        return processos.isEmpty() ? null : processos.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperarRemovidos(Sessao sessao){
		List<ProcessoTrf> processos = new ArrayList<ProcessoTrf>(0);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct o.processoTrf FROM SessaoPautaProcessoTrf o where o.sessao = :sessao " );
		sql.append("and o.dataExclusaoProcessoTrf is not null and o.situacaoJulgamento = 'CP' ");
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("sessao", sessao);
		processos = q.getResultList();
        return processos;
	}

	@SuppressWarnings("unchecked")
	public List <SessaoPautaProcessoTrf> recuperar(BlocoJulgamento bloco) {
		List<SessaoPautaProcessoTrf> processos = new ArrayList<SessaoPautaProcessoTrf>(0);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct o FROM SessaoPautaProcessoTrf o where o.processoTrf in (select processobloco.processoTrf " );
		sql.append("from ProcessoBloco processobloco where processobloco.bloco.idBlocoJulgamento = :idBloco and " );
		sql.append("processobloco.bloco.sessao.idSessao = :idSessao) and o.sessao.idSessao = :idSessao and o.dataExclusaoProcessoTrf is null ");
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("idBloco", bloco.getIdBlocoJulgamento());
		q.setParameter("idSessao", bloco.getSessao().getIdSessao());
		processos = q.getResultList();
		return processos;
	}

	/**
	 * Obtém a data em que processo foi julgado em sessão pela última vez, ou seja,
	 * a data mais recente de julgamento do processo.
	 * @param processoTrf O processo que se deseja consultar a data de julgamento.
	 * @return A data de julgamento em sessão mais recente do processo.
	 */
	public Date obterUltimaDataSessaoJulgamentoProcesso(ProcessoTrf processoTrf) {
		StringBuilder hql = new StringBuilder(1000)
				.append("select max(spp.sessao.dataSessao) ")
				.append("  from SessaoPautaProcessoTrf spp ")
				.append(" where spp.processoTrf = :processoTrf ")
				.append("   and spp.situacaoJulgamento = 'JG' ")
				.append("   and spp.dataExclusaoProcessoTrf is null ")
				;
		
		Query query = getEntityManager().createQuery(hql.toString())
				.setParameter("processoTrf", processoTrf);
		Date data = EntityUtil.getSingleResult(query);
		return data;
	}
}