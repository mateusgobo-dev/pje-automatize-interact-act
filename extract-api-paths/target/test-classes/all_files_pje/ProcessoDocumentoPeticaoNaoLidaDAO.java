/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name("processoDocumentoPeticaoNaoLidaDAO")
public class ProcessoDocumentoPeticaoNaoLidaDAO extends BaseDAO<ProcessoDocumentoPeticaoNaoLida>{

	@Override
	public Object getId(ProcessoDocumentoPeticaoNaoLida e){
		return e.getIdProcessoDocumentoPeticaoNaoLida();
	}
	
	public ProcessoDocumentoPeticaoNaoLida obterProcessoDocumentoPeticaoNaoLida(ProcessoDocumento processoDocumento){
	
		Query q = getEntityManager().createQuery("select o from ProcessoDocumentoPeticaoNaoLida o where o.processoDocumento = :processoDocumento");
		q.setParameter("processoDocumento", processoDocumento);
	
		return EntityUtil.getSingleResult(q);
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> obterProcessoDocumentoPeticaoNaoLida(){
		return obterProcessoDocumentoPeticaoNaoLida(true);
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> obterProcessoDocumentoPeticaoNaoLidaPeticaoAvulsa(){
		return obterProcessoDocumentoPeticaoNaoLida(false);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumentoPeticaoNaoLida> obterProcessoDocumentoPeticaoNaoLida(boolean habilitacaoAutomatica){
		List<ProcessoDocumentoPeticaoNaoLida> resultList = null;
		try{
			
			String consulta = "SELECT pd FROM ProcessoDocumentoPeticaoNaoLida pd, ProcessoTrf trf " +  
					  "WHERE trf.idProcessoTrf = pd.processoDocumento.processo.idProcesso " +
					  "AND trf.processoStatus in ('D','V') " +
					  "AND pd.retirado = false ";
			if (habilitacaoAutomatica) {
				consulta += " and pd.habilitacaoAutos != null";
			}
			else {
				consulta += " and pd.habilitacaoAutos = null";
			}

			Query q = getEntityManager().createQuery(consulta);
			
			resultList = q.getResultList();
		} catch (Exception e){
			
			e.printStackTrace();
			
		}
		
		return  resultList;
	}
	
	public Long countAgrupadorPeticoesAvulsas() {
		StringBuilder sb = new StringBuilder();

		sb = new StringBuilder();
		sb.append("	select count(pd) from ProcessoDocumentoPeticaoNaoLida pd, ProcessoTrf trf ");
		sb.append("	where trf.idProcessoTrf = pd.processoDocumento.processo.idProcesso ");
		sb.append("	and trf.processoStatus in ('D','V') ");
		sb.append("	and pd.retirado = false ");
		sb.append("	and pd.habilitacaoAutos = null ");

		Query q = getEntityManager().createQuery(sb.toString());
		return EntityUtil.getSingleResultCount(q);

	}

	public void retirarDestaque(ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida){
		
		processoDocumentoPeticaoNaoLida.setRetirado(true);
		getEntityManager().merge(processoDocumentoPeticaoNaoLida);
		EntityUtil.flush();
	}

	/**
	 * Resgata os documentos petição não lida do processo   
	 * 
	 *  
	 * @param ProcessoTrf que será resgatado os documentos
	 * @return uma <b>Lista</b> de objetos contendo as petições
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumentoPeticaoNaoLida> obterProcessoDocumentoPeticaoNaoLida(ProcessoTrf processoTrf) {
		try{
			StringBuilder consulta = new StringBuilder();
			consulta.append("select o from ProcessoDocumentoPeticaoNaoLida o where o.retirado = false");
			consulta.append(" and not exists (select m from ManifestacaoProcessualDocumento m where m.processoDocumento = o.processoDocumento) ");
			consulta.append(" and o.processoDocumento.processoTrf = :processoTrf");  
			Query q = getEntityManager().createQuery(consulta.toString());
			q.setParameter("processoTrf", processoTrf);
			return q.getResultList();
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Encontra o registro de Petição Não Lida com base no documento informado, se houver,   
	 *  diferenciando petição avulsa e habilitação nos autos.
	 *  
	 * @param processoDocumento
	 * @param habilitacaoAutos
	 * @author lucas.raw
	 * @since 09/06/2015
	 * @return a petição
	 */
	public ProcessoDocumentoPeticaoNaoLida getProcessoDocumentoPeticaoNaoLidaByDocumento(ProcessoDocumento processoDocumento, Boolean habilitacaoAutos) {
		
		StringBuilder consulta = new StringBuilder();
		consulta.append("SELECT o FROM ProcessoDocumentoPeticaoNaoLida o WHERE o.processoDocumento = :idDoc ");
		if (habilitacaoAutos) {
			consulta.append("AND o.habilitacaoAutos IS NOT NULL");
		} else {
			consulta.append("AND o.habilitacaoAutos IS NULL");
		}
		
		try{
			Query q = getEntityManager().createQuery(consulta.toString());
			q.setParameter("idDoc", processoDocumento);
			return EntityUtil.getSingleResult(q);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}