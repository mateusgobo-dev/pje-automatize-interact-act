package br.com.jt.pje.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.jt.entidades.HabilitacaoAutos;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("habilitacaoAutosDAO")
public class HabilitacaoAutosDAO extends BaseDAO<HabilitacaoAutos>{

	@Override
	public Object getId(HabilitacaoAutos e){
		return e.getIdHabilitacaoAutos();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> getProcessoParteListByPoloByProcessoTrf(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum polo) {
		StringBuilder hql = new StringBuilder();
		hql.append("select ppa from ProcessoParte ppa ");
		hql.append("where ppa.inParticipacao = :polo and ppa.inSituacao != 'I' ");
		hql.append("and ppa not in ");
		hql.append("(select distinct ppa2 from ProcessoParteRepresentante ppr inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2 = ppa)");
		hql.append("and ppa.processoTrf.idProcessoTrf = :idProcessoTrf");
		
		Query query = this.getEntityManager().createQuery(hql.toString());
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		query.setParameter("polo", polo);
		
		return query.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> getProcessoHabilitacaoPendente(ProcessoTrf processoTrf) {
		StringBuilder hql = new StringBuilder();
		hql.append("select ha from HabilitacaoAutos ha ");
		hql.append("where ha not in ");
		hql.append("(select ha2 from HabilitacaoAutos ha2 ");
		hql.append(" join ha2.representados rep ");
		hql.append(" where ha.processo = ha2.processo )");
		hql.append(" and ha.processo.idProcessoTrf = :idProcessoTrf");
		hql.append(" and ha.metodoHabilitacao <> 'M' ");
		
		Query query = this.getEntityManager().createQuery(hql.toString());
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		return query.getResultList();
	}
	
	
	
	/**
	 * Retorna Lista de ProcessoDocumentoPeticaoNaoLida para processos com pedido de Habilitação nos Autos
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLidaHabilitacaoAutos()
	{
		
		List<ProcessoDocumentoPeticaoNaoLida> resultList = null;
		
		try{

			StringBuilder sb = new StringBuilder();
			// Busca as petições não lidas relacionadas aos pedidos de Habilitação nos Autos com situação "Ativa" 
			sb.append("SELECT pd FROM ProcessoDocumentoPeticaoNaoLida pd " +
							  "INNER JOIN pd.habilitacaoAutos ha " +
							  "INNER JOIN ha.processo trf " +
							  "WHERE pd.retirado = 'N' " +
							  "AND ha.situacaoHabilitacao = 'A' ");
						
			// Início validações de Filtros
			// Verifica órgão julgador
			if(Authenticator.getOrgaoJulgadorAtual() != null) {
				sb.append("AND trf.orgaoJulgador.idOrgaoJulgador = #{orgaoJulgadorAtual.idOrgaoJulgador}  ");
			}
			
			// Verifica órgão julgador colegiado
			if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null)
			{
				sb.append("AND trf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual.idOrgaoJulgadorColegiado} ");
			}
			
			// Verifica Segredo de Justiça para o processo
			sb.append(" AND (trf.segredoJustica = 'N' "
					+ "OR exists (select 1 from "
					+ "ProcessoVisibilidadeSegredo pvs where pvs.pessoa = #{pessoaLogada} and pvs.processo.idProcesso = trf.idProcessoTrf) "
					+ ") ");
			
			// Verifica Segredo de Justiça para o processoDocumento
			sb.append("AND ((pd.processoDocumento.documentoSigiloso = true and exists(select 1 from ProcessoDocumentoVisibilidadeSegredo pdvs" +
					" where pdvs.processoDocumento.idProcessoDocumento = pd.processoDocumento.idProcessoDocumento" +
					" and pdvs.pessoa.idUsuario = #{processoDocumentoHome.usuarioLogado.idUsuario})" +
					" or #{identificadorPapelAtual.equals('magistrado')} = true" +
					" or (pd.processoDocumento.usuarioInclusao.idUsuario = #{usuarioLogado.idUsuario}))" +
					" or pd.processoDocumento.documentoSigiloso = false) ");
			// Fim validações de filtros

			
			Query q = getEntityManager().createQuery(sb.toString());
			
			resultList = q.getResultList();
			
		
		} catch (Exception e){
			
			e.printStackTrace();
			
		}
		
		return  resultList;
		
	}
	
	public void clear(){
		
		getEntityManager().clear();
	}

	/**
	 * Recupera os pedidos habilitação nos autos relativos ao processo informado.
	 * @param processo ProcessoTrf para a pesquisa
	 * @return Lista de habilitações nos autos encontradas para o processo
	 */
	@SuppressWarnings("unchecked")
	public List<HabilitacaoAutos> findByProcessoTrf(ProcessoTrf processo) {
		
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT ha FROM HabilitacaoAutos ha WHERE ha.processo = :processo");
		Query query = this.getEntityManager().createQuery(hql.toString());
		query.setParameter("processo", processo);
		 try {
			 return (List<HabilitacaoAutos>) query.getResultList();
		 } catch (Exception e) {
			 e.printStackTrace();
			 return null; 
		 }
	}
	
	
}
