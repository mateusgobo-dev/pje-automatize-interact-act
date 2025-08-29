/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.HierarchicEntity;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;

/**
 * Componente de acesso a dados da entidade {@link ProcessoEvento}.
 * 
 * @author cristof
 */
@Name("processoEventoDAO")
public class ProcessoEventoDAO extends BaseDAO<ProcessoEvento> {

	@Override
	public Integer getId(ProcessoEvento e) {
		return e.getIdProcessoEvento();
	}
	
	/**
	 * Recupera a lista de movimentações de processos da instalação que não foram
	 * contabilizadas do ponto de vista estatístico.
	 * 
	 * @return lista de movimentações não contabilizadas
	 * @see ProcessoEvento#isProcessado()
	 * @see ProcessoEvento#isVerificadoProcessado()
	 * 
	 * @author Daniel (Infox)
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoEvento> recuperaNaoContabilizadas() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT processoEvento FROM ProcessoEvento AS processoEvento ");
		query.append("WHERE processoEvento.processado = false ");
		query.append("AND processoEvento.verificadoProcessado = false ");
		return entityManager.createQuery(query.toString()).getResultList();
	}

	/**
	 * Recupera a movimentação mais moderna ocorrida em um dado processo judicial antes de uma
	 * determinada data.
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual se pretende obter a movimentação
	 * @param data a data antes da qual se pretende identificar a movimentação mais moderna
	 * @return a movimentação imediatamente anterior à data dada, ou nulo, se ela não existir.
	 */
	public ProcessoEvento recuperaUltimaMovimentacao(ProcessoTrf processoJudicial, Date data) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT processoEvento FROM ProcessoEvento AS processoEvento ");
		query.append("WHERE processoEvento.processo.idProcesso = :idProcesso ");
		query.append("AND processoEvento.dataAtualizacao <= :data ");
		query.append("AND processoEvento.ativo = true ");
		query.append("ORDER BY processoEvento.dataAtualizacao DESC ");
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", processoJudicial.getProcesso().getIdProcesso());
		q.setParameter("data", data);
		q.setMaxResults(1);
		try{
			return (ProcessoEvento) q.getSingleResult();
		}catch(NoResultException nre){
			return null;
		}
	}
	
	/**
	 * Recupera a movimentação mais moderna ocorrida em um dado processo judicial antes de uma
	 * determinada data para consulta publica.
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual se pretende obter a movimentação
	 * @param data a data antes da qual se pretende identificar a movimentação mais moderna
	 * @return a movimentação imediatamente anterior à data dada, ou nulo, se ela não existir.
	 */
	public ProcessoEvento recuperaUltimaMovimentacaoPublica(Integer idProcesso, Date data) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT processoEvento FROM ProcessoEvento AS processoEvento LEFT JOIN processoEvento.processoDocumento procDoc ");
		query.append("WHERE processoEvento.processo.idProcesso = :idProcesso ");
		query.append("AND processoEvento.dataAtualizacao <= :data ");
		query.append("AND processoEvento.ativo = true ");
		query.append("AND ( ");
		query.append("		(procDoc is null AND processoEvento.evento.segredoJustica = false AND processoEvento.visibilidadeExterna = true) ");
		query.append("		OR (procDoc is not null and procDoc.dataJuntada is not null and procDoc.documentoSigiloso = false and processoEvento.visibilidadeExterna = true) ");
		query.append("	  ) ");
		query.append("ORDER BY processoEvento.dataAtualizacao DESC ");
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", idProcesso);
		q.setParameter("data", data);
		q.setMaxResults(1);
		try{
			return (ProcessoEvento) q.getSingleResult();
		}catch(NoResultException nre){
			return null;
		}
	}
	
	/**
	 * Recupera o primeiro movimento processual que teve por documento
	 * o indicado.
	 * 
	 * @param documento o documento vinculado ao movimento que se pretende identificar
	 * @return o primeiro movimento processual que teve o documento dado como vinculado,
	 * ou nulo, se não há movimento processual com esse documento
	 */
	public ProcessoEvento findByDocumento(ProcessoDocumento documento) {
		ProcessoEvento resultado = null;
		
		List<ProcessoEvento> processoEventos = recuperar(documento);
		if (CollectionUtilsPje.isNotEmpty(processoEventos)) {
			resultado = processoEventos.get(0);
		}
		
		return resultado;
	}
	
	/**
	 * 
	 * @param documento
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoEvento> recuperar(ProcessoDocumento processoDocumento){
		List<ProcessoEvento> resultado = null;
		
		if (processoDocumento != null) {
			StringBuilder jpql = new StringBuilder("SELECT o FROM ProcessoEvento AS o ")
					.append("WHERE o.processoDocumento.idProcessoDocumento = :idProcessoDocumento ")
					.append("ORDER BY o.dataAtualizacao ASC");
			
			Query query = entityManager.createQuery(jpql.toString())
					.setParameter("idProcessoDocumento", processoDocumento.getIdProcessoDocumento());
			
			resultado = query.getResultList();
		}
		
		return resultado;
	}

	/**
	 * Identifica se um dado processo teve lançada uma movimentação do tipo dado.
	 * 
	 * @param processo o processo a ser pesquisado
	 * @param movimento o tipo de movimentação cujo lançamento se pretende identificar
	 * @return true, se houve pelo menos um lançamento de movimentação no tipo dado.
	 * @since 1.4.8
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento) {
		return temMovimento(processo, movimento, null);
	}
	
	/**
	 * Identifica se um dado processo teve lançada uma movimentação do tipo dado após uma data limite.
	 * 
	 * @param processo o processo a ser pesquisado
	 * @param movimento o tipo de movimentação cujo lançamento se pretende identificar
	 * @param dataLimite a data a partir da qual a pesquisa deve ser feita, ou null, para ignorar esse critério
	 * @return true, se houve pelo menos um lançamento de movimentação no tipo dado.
	 * @since 1.4.8
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento, Date dataLimite){
		return temMovimento(processo.getIdProcessoTrf(), movimento, dataLimite);
	}

	/**
	 * Identifica se um dado processo teve lançada uma movimentação do tipo dado após uma data limite.
	 * 
	 * @param processo o processo a ser pesquisado
	 * @param movimento o tipo de movimentação cujo lançamento se pretende identificar
	 * @param dataLimite a data a partir da qual a pesquisa deve ser feita, ou null, para ignorar esse critério
	 * @return true, se houve pelo menos um lançamento de movimentação no tipo dado.
	 * @since 1.4.8
	 */
	public boolean temMovimento(Integer idProcesso, Evento movimento, Date dataLimite){
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(processoEvento.idProcessoEvento) FROM ProcessoEvento AS processoEvento ");
		query.append("WHERE processoEvento.processo.idProcesso = :idProcesso ");
		query.append("AND processoEvento.processoEventoExcludente IS NULL ");
		query.append("AND processoEvento.ativo = true ");
		query.append("AND (processoEvento.evento.idEvento = :idEvento OR processoEvento.evento.breadcrumb like :breadcrumb) ");
		if(dataLimite != null){
			query.append("AND processoEvento.dataAtualizacao >= :dataLimite ");
		}
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", idProcesso);
		q.setParameter("idEvento", movimento.getIdEvento());
		q.setParameter("breadcrumb", movimento.getBreadcrumb().concat(HierarchicEntity.BREADCRUMB_SEPARATOR).concat("%"));
		if(dataLimite != null){
			q.setParameter("dataLimite", dataLimite);
		}
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;
	}
	
	public boolean temAlgumMovimento(Integer idProcesso, Date dataLimite, Evento... movimento) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(processoEvento.idProcessoEvento) FROM ProcessoEvento AS processoEvento ");
		query.append("WHERE processoEvento.processo.idProcesso = :idProcesso ");
		query.append("AND processoEvento.processoEventoExcludente IS NULL ");
		query.append("AND processoEvento.ativo = true ");
		if(dataLimite != null){
			query.append("AND processoEvento.dataAtualizacao >= :dataLimite ");
		}
		
		if (movimento.length>0) {
			query.append("AND (");
			for (int idxEvento=0; idxEvento<movimento.length; idxEvento++) {
				if (idxEvento>0)
					query.append(" OR ");
				query.append("processoEvento.evento.idEvento = :idEvento").append(idxEvento)
						.append(" OR processoEvento.evento.breadcrumb like :breadcrumb").append(idxEvento);
			}
			query.append(")");
		}
		
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", idProcesso);
		
		for (int idxEvento=0; idxEvento<movimento.length; idxEvento++) {
			Evento evento = movimento[idxEvento];
			q.setParameter("idEvento" + idxEvento, evento.getIdEvento());
			q.setParameter("breadcrumb" + idxEvento, evento.getBreadcrumb().concat(HierarchicEntity.BREADCRUMB_SEPARATOR).concat("%"));
		}
		
		if(dataLimite != null){
			q.setParameter("dataLimite", dataLimite);
		}
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;
	}
			

	/**
	 * Identifica se um dado processo judicial teve, a partir de uma data informada, lançada alguma 
	 * movimentação do tipo e com os complementos dados.
	 * 
	 * @param processo o processo judicial objeto da pesquisa
	 * @param movimento o movimento esperado
	 * @param dataLimite a data limite a partir da qual será feita a pesquisa
	 * @param complementos a lista de complementos que devem constar na movimentação
	 * @return true, se houver pelo menos uma movimentação no período dado
	 * @since 1.4.8
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento, Date dataLimite, Map<TipoComplemento, String> complementos) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(processoEvento.idProcessoEvento) FROM ProcessoEvento AS processoEvento ");
		query.append("JOIN processoEvento.complementoSegmentadoList AS complementos ");
		query.append("WHERE processoEvento.processo.idProcesso = :idProcesso ");
		query.append("AND processoEvento.ativo = true ");
		query.append("AND processoEvento.processoEventoExcludente IS NULL ");
		query.append("AND processoEvento.dataAtualizacao >= :dataLimite ");
		if (complementos != null) {
			for(TipoComplemento tipoComplemento: complementos.keySet()){
				String valor = complementos.get(tipoComplemento);
				if(valor == null || valor.isEmpty()) {
					query.append("AND complementos.tipoComplemento.codigo = ");
					query.append(tipoComplemento.getCodigo());
				} else {
					query.append(String.format("AND (comp.tipoComplemento.codigo = '%s' AND comp.valorComplemento = '%s') ", 
							tipoComplemento.getCodigo(), valor));
				}
			}
		}
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", processo.getProcesso().getIdProcesso());
		q.setParameter("dataLimite", dataLimite);
		Number cont = (Number) q.getSingleResult();
		return cont.intValue() > 0;
	}

	public ProcessoEvento persist(ProcessoEvento mov, Integer idUsuario){
		String sqlUsuario = "SELECT u FROM Usuario u WHERE u.idUsuario = :id";
		Query q = entityManager.createQuery(sqlUsuario);

		try{
			q.setParameter("id", idUsuario.intValue());
			Usuario u = (Usuario) q.getSingleResult();
			return persist(mov, u);

		} catch (NoResultException e){
			throw new PJeDAOException("pje.movimentoProcesso.error.usuarioNaoLocalizado", e, idUsuario);
		} catch (NonUniqueResultException e){
			throw new PJeDAOException("pje.movimentoProcesso.error.usuarioNaoLocalizado", e, idUsuario);
		}
	}

	public ProcessoEvento persist(ProcessoEvento mov, Usuario usuario) throws PJeDAOException{
		mov.setUsuario(usuario);
		return super.persist(mov);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoEvento> getMovimentosLancadosParaProcesso(Evento evento, SessaoJT sessaoJT, Processo processo) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT processoEvento FROM ProcessoEvento AS processoEvento ");
		query.append("WHERE processoEvento.evento.caminhoCompleto like :caminhoCompleto || '%' ");
		query.append("AND processoEvento.ativo = true ");
		query.append("AND processoEvento.dataAtualizacao >= :data ");
		query.append("AND processoEvento.processo.idProcesso = :idProcesso ");
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("caminhoCompleto", evento.getCaminhoCompleto());
		q.setParameter("data", sessaoJT.getDataSituacaoSessao());
		q.setParameter("idProcesso", processo.getIdProcesso());
		return q.getResultList();
	}
	
	/**
	 * Identifica se um dado processo possui o movimento indicado, ainda ativo, 
	 * a partir da data informada e vinculado ao documento passado como parâmetro. 
	 * 
	 * @param processo {@link ProcessoTrf} o processo a ser pesquisado
	 * @param movimento {@link Evento} o tipo de movimentação cujo lançamento se pretende identificar
	 * @param dataLimite {@link Date} a data a partir da qual a pesquisa deve ser feita, ou null, para ignorar esse critério
	 * @param processoDocumento {@link ProcessoDocumento} o documento vinculado à movimentação pesquisada
	 * 
	 * @return true ou false
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento, Date dataLimite, ProcessoDocumento processoDocumento){
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(idProcessoEvento) FROM ProcessoEvento AS processoEvento ");
		query.append("WHERE processoEvento.processo.idProcesso = :idProcesso ");
		query.append("AND processoEvento.processoEventoExcludente IS NULL ");
		query.append("AND processoEvento.ativo = true ");
		query.append("AND (processoEvento.evento.idEvento = :idMovimento OR processoEvento.evento.breadcrumb like :breadcrumb) ");
		if(dataLimite != null){
			query.append("AND processoEvento.dataAtualizacao >= :dataLimite ");
		}
		if (processoDocumento != null){
			query.append("AND processoEvento.processoDocumento.idProcessoDocumento = :idProcessoDocumento ");
		}
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("idProcesso", processo.getProcesso().getIdProcesso());
		q.setParameter("idMovimento", movimento.getIdEvento());
		q.setParameter("breadcrumb", movimento.getBreadcrumb().concat(HierarchicEntity.BREADCRUMB_SEPARATOR).concat("%"));
		if(dataLimite != null){
			q.setParameter("dataLimite", dataLimite);
		}
		if (processoDocumento != null){
			q.setParameter("idProcessoDocumento", processoDocumento.getIdProcessoDocumento());
		}		
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;		
	}	

	public ProcessoEvento recuperaUltimaMovimentacao(ProcessoTrf processoJudicial, String[] codigoEvento) {
		String query = " SELECT m FROM ProcessoEvento AS m" +
				" WHERE m.processo.idProcesso = :idProcesso " +
				" AND m.evento.codEvento IN (:codigoEvento)" +
				" AND m.ativo = true" +
				" ORDER BY m.dataAtualizacao DESC ";
		Query q = entityManager.createQuery(query);
		q.setParameter("idProcesso", processoJudicial.getProcesso().getIdProcesso());
		q.setParameter("codigoEvento", Arrays.asList(codigoEvento));
		q.setMaxResults(1);
		
		try{
			return (ProcessoEvento) q.getSingleResult();
		}catch(NoResultException nre){
			return null;
		}
	}
	
public Boolean existeConclusaoAberta(ProcessoTrf processoTrf) {
		
		StringBuilder query = new StringBuilder();
		
		query.append("with ultimaConclusao as ( ");
		query.append("select max(dt_atualizacao) dataConclusao from core.tb_processo_evento tpe  ");
		query.append("join core.tb_evento te on te.id_evento  = tpe.id_evento ");
		query.append("where id_processo  = :idProcesso  ");
		query.append("and tpe.in_ativo = true ");
		query.append("and te.cd_evento = '51') ");

		query.append("select count(1) as contador from ultimaConclusao, core.tb_processo_evento tpe  ");
		query.append("join core.tb_evento te on te.id_evento  = tpe.id_evento ");
		query.append("where id_processo  = :idProcesso ");
		query.append("and te.cd_evento  in (SELECT cd_evento ");
		query.append( "          FROM public.fn_hierarquia_eventos(ARRAY['1'])) ");
		query.append("and tpe.dt_atualizacao >= dataConclusao ");
		query.append("and tpe.in_ativo = true ");

		
		Query q = entityManager.createNativeQuery(query.toString());
		q.setParameter("idProcesso", processoTrf.getProcesso().getIdProcesso());

		
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() == 0;
	}
	
	public Boolean existeConclusaoLancada(ProcessoTrf processoTrf) {

		StringBuilder query = new StringBuilder();

		query.append("select count(1) existe from core.tb_processo_evento tpe  ");
		query.append("join core.tb_evento te on te.id_evento  = tpe.id_evento ");
		query.append("where id_processo  = :idProcesso  ");
		query.append("and tpe.in_ativo = true ");
		query.append("and te.cd_evento = '51' ");

		Query q = entityManager.createNativeQuery(query.toString());
		q.setParameter("idProcesso", processoTrf.getProcesso().getIdProcesso());

		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;
	}
}
