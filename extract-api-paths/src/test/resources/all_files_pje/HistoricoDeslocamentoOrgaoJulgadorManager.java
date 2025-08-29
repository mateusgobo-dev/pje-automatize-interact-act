/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.HistoricoDeslocamentoOrgaoJulgadorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.pje.jt.entidades.HistoricoDeslocamentoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * PJEII-3236
 * @author Frederico Carneiro
 *
 */
@Name("historicoDeslocamentoOrgaoJulgadorManager")
public class HistoricoDeslocamentoOrgaoJulgadorManager extends BaseManager<HistoricoDeslocamentoOrgaoJulgador>{

	@In
	private HistoricoDeslocamentoOrgaoJulgadorDAO historicoDeslocamentoOrgaoJulgadorDAO;

	@Override
	protected HistoricoDeslocamentoOrgaoJulgadorDAO getDAO(){
		return this.historicoDeslocamentoOrgaoJulgadorDAO;
	}
	
	/**
	 * @author José Borges - jose.borges@tst.jus.br
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retorna a lista de históricos de mudança de órgão judicial de um processo
	 */
	public List<HistoricoDeslocamentoOrgaoJulgador> obterListaHistorico(ProcessoTrf processoTrf)
	{
		return historicoDeslocamentoOrgaoJulgadorDAO.obterListaHistorico(processoTrf.getIdProcessoTrf());	
	}
	
	
	/**
	 * @author José Borges - jose.borges@tst.jus.br
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retorna as entradas para o processo em questão sem data de saída do plantão, mas com data de entrada
	 */
	public HistoricoDeslocamentoOrgaoJulgador obterHistoricoSemDataRetorno(ProcessoTrf processoTrf) throws PJeBusinessException
	{
		try {
			return historicoDeslocamentoOrgaoJulgadorDAO.obterHistoricoSemDataRetornoDefinida(processoTrf.getIdProcessoTrf());
		} catch (PJeException e) {
			throw new PJeBusinessException(e);
		}
	}
	
	/**
	 * @author José Borges - jose.borges@tst.jus.br
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retorna a entrada na tabela sem data de entrada ou saída do plantão. Se não houver entrada, retorna null. Se houver mais de uma entrada, dispara objeto PJeException
	 */
	public HistoricoDeslocamentoOrgaoJulgador obterHistoricoSemDatasDefinidas(ProcessoTrf processoTrf) throws PJeException
	{
		return historicoDeslocamentoOrgaoJulgadorDAO.obterHistoricoSemDatasDefinidas(processoTrf.getIdProcessoTrf());	
	}
	
	public Boolean verificaDeslocamentoOrgaoJulgadorEmAndamento(ProcessoTrf processoTrf)
	{
		List<HistoricoDeslocamentoOrgaoJulgador> deslocamentoOrgaoJulgadorEmAndamentoList = historicoDeslocamentoOrgaoJulgadorDAO.verificaDeslocamentoOrgaoJulgadorEmAndamento(processoTrf.getIdProcessoTrf());
		
		if(deslocamentoOrgaoJulgadorEmAndamentoList == null || deslocamentoOrgaoJulgadorEmAndamentoList.size() > 0)
			return true;
		
		return false; 
	}

	public HistoricoDeslocamentoOrgaoJulgador obtemUltimoDeslocamento(ProcessoTrf processoTrf) {
		return historicoDeslocamentoOrgaoJulgadorDAO.obtemUltimoDeslocamento(processoTrf.getIdProcessoTrf());
	}
}
