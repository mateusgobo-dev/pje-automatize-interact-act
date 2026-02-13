/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

/**
 * @author cristof
 *
 */
@Name("processoPautadoDAO")
public class ProcessoPautadoDAO extends BaseDAO<SessaoPautaProcessoTrf> {

	@Override
	public Integer getId(SessaoPautaProcessoTrf e) {
		return e.getIdSessaoPautaProcessoTrf();
	}
	
	public int getTotalProcessosIncluidos(Sessao sessao){
		String query = "SELECT count(p.idSessaoPautaProcessoTrf) FROM SessaoPautaProcessoTrf AS p " +
				"	WHERE p.dataExclusaoProcessoTrf IS NOT NULL";
		Query q = entityManager.createQuery(query);
		Long contagem = (Long) q.getSingleResult();
		return contagem.intValue();
	}
	
	/**
	 * Método responsável por verificar se um {@link ProcessoTrf} foi pautado
	 * numa sessão
	 * 
	 * @param idProcessoJudicial
	 *            id do processo que se deseja verificar
	 * @return <code>Boolean</code>, <code>true</code> se o processo foi pautado
	 *         numa sessão e a mesma teve sua pauta encerrada e o processo em
	 *         questão tenha sua situação como aguardando julgamento ou em julgamento.
	 */
	public boolean isProcessoPautado(Integer idProcessoJudicial) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(sppt) FROM SessaoPautaProcessoTrf AS sppt ");
		sb.append("WHERE sppt.processoTrf.idProcessoTrf = :idProcessoJudicial ");
		sb.append("AND sppt.sessao.dataFechamentoPauta IS NOT NULL ");
		sb.append("AND (sppt.situacaoJulgamento = :aguardandoJulgamento OR sppt.situacaoJulgamento = :emJulgamento) ");
		
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcessoJudicial", idProcessoJudicial);
		query.setParameter("aguardandoJulgamento", TipoSituacaoPautaEnum.AJ);
		query.setParameter("emJulgamento", TipoSituacaoPautaEnum.EJ);
		
		Long count = (Long) query.getSingleResult();
		
		return count > 0;
	}

}