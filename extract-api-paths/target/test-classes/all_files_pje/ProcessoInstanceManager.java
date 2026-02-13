/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.business.dao.ProcessoInstanceDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente de gerenciamento negocial da entidade {@link ProcessoInstance}.
 * 
 * @author cristof
 *
 */
@Name("processoInstanceManager")
public class ProcessoInstanceManager extends BaseManager<ProcessoInstance> {
	
	@In
	private ProcessoInstanceDAO processoInstanceDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoInstanceDAO getDAO() {
		return processoInstanceDAO;
	}
	
	/**
	 * Recupera a lista de todas as instâncias de fluxo ativas para um dado processo judicial.
	 * 
	 * @param pj o processo judicial cujas instâncias de fluxo ativas se pretende recuperar
	 * @return a lista de instâncias de fluxo ativas para o processo judicial dado
	 */
	public List<ProcessoInstance> recuperaAtivas(ProcessoTrf processoJudicial) throws PJeBusinessException{
		return recuperaAtivas(processoJudicial, null, null);
	}
	
	/**
	 * Recupera a lista de instâncias de fluxo ativas para um dado processo judicial.
	 * 
	 * @param processoJudicial o processo judicial cujas instâncias de fluxo ativas se pretende recuperar
	 * @param first inteiro indicativo do primeiro resultado que se pretende recuperar (ou nulo, para o inicial)
	 * @param maxResults inteiro indicativo do número máximo de resultados que se pretende recuperar (ou nulo, para todos)
	 * @return a lista de instâncias de fluxo ativas para o processo judicial dado
	 */
	public List<ProcessoInstance> recuperaAtivas(ProcessoTrf processoJudicial, Integer first, Integer maxResults) throws PJeBusinessException{
		return processoInstanceDAO.recuperaAtivas(processoJudicial, first, maxResults);
	}
	
	public List<Integer> getIdsProcessosOrgao(Integer idOrgaoJulgador) throws PJeBusinessException{
		if(idOrgaoJulgador == null){
			throw new PJeBusinessException("Nada");
		}
		
		OrgaoJulgadorManager ojManager = (OrgaoJulgadorManager) Component.getInstance(OrgaoJulgadorManager.class);
		OrgaoJulgador oj = ojManager.findById(idOrgaoJulgador);
		if(oj == null || oj.getLocalizacao() == null) {
			throw new PJeBusinessException("Nada");
		}
		Integer idLocalizacao = oj.getLocalizacao().getIdLocalizacao();

		LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance(LocalizacaoManager.class);
		List<Localizacao> localizacaoFisicaList = localizacaoManager.getArvoreDescendente(idLocalizacao, true);
		String idsLocalizacoesFisicas = LocalizacaoUtil.converteLocalizacoesList(localizacaoFisicaList);
		List<Integer> idsLocalizacoes = CollectionUtilsPje.convertStringToIntegerList(idsLocalizacoesFisicas);
		
		List<Integer> ret = processoInstanceDAO.getIdsProcessoLocalizacao(idsLocalizacoes, null, null);
		if(ret.isEmpty()){
			ret.add(-1);
		}
		return ret;
	}
	
	public boolean existeProcessoInstancePorLocalizacaoPessoa(ProcessoTrf processoTrf, List<Integer> idsLocalizacoesFisicasList, OrgaoJulgadorColegiado orgaoJulgadorColegiado, boolean isServidorExclusivoOJC) {
		Integer idProcessoTrf = null;
		if(processoTrf != null) {
			idProcessoTrf = processoTrf.getIdProcessoTrf();
		}
		Integer idOrgaoJulgadorColegiado = null;
		if(orgaoJulgadorColegiado != null) {
			idOrgaoJulgadorColegiado = orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado();
		}
		return processoInstanceDAO.existeProcessoInstancePorLocalizacaoPessoa(idProcessoTrf, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
	}

}
