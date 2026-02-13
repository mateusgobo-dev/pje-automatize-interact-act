/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.VinculacaoDependenciaEleitoralDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;

/**
 * Componente de controle negocial da entidade {@link VinculacaoDependenciaEleitoral}.
 * 
 * @author eduardo.pereira
 *
 */
@Name("vinculacaoDependenciaEleitoralManager")
public class VinculacaoDependenciaEleitoralManager extends BaseManager<VinculacaoDependenciaEleitoral> {
	
	@In
	private VinculacaoDependenciaEleitoralDAO vinculacaoDependenciaEleitoralDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected VinculacaoDependenciaEleitoralDAO getDAO() {
		return vinculacaoDependenciaEleitoralDAO;
	}
	
	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição, município e processo.
	 * 
	 * @param eleicao a eleição vinculada
	 * @param municipio o município de origem
	 * @return a vinculação eleitoral, se existente para esse cargo, ou null, caso não exista uma vinculação em tais condições
	 * @throws PJeBusinessException
	 */
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoral(Eleicao eleicao, Municipio municipio, ProcessoTrf processo) throws PJeBusinessException{
		return vinculacaoDependenciaEleitoralDAO.recuperaVinculacaoDependenciaEleitoral(eleicao, municipio, processo);
	}
	
	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição e município.
	 * 
	 * @param eleicao
	 * @param municipio
	 * @return
	 * @throws PJeBusinessException
	 */
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoralPorEleicao(Eleicao eleicao, Municipio municipio) throws PJeBusinessException{
		return vinculacaoDependenciaEleitoralDAO.recuperaVinculacaoDependenciaEleitoralPorEleicaoMunicipio(eleicao, municipio);
	}

	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição, estado e processo.
	 * 
	 * @param eleicao a eleição vinculada
	 * @param estado o estado de origem
	 * @return a vinculação eleitoral, se existente para esse cargo, ou null, caso não exista uma vinculação em tais condições
	 * @throws PJeBusinessException
	 */
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoral(Eleicao eleicao, Estado estado, ProcessoTrf processo) throws PJeBusinessException{
		return vinculacaoDependenciaEleitoralDAO.recuperaVinculacaoDependenciaEleitoral(eleicao, estado, processo);
	}
	
	/**
	 * Recupera uma vinculação única de dependência eleitoral para um dado cargo, eleição e estado.
	 * 
	 * @param eleicao
	 * @param estado
	 * @return
	 * @throws PJeBusinessException
	 */
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependenciaEleitoralPorEleicao(Eleicao eleicao, Estado estado) throws PJeBusinessException{
		return vinculacaoDependenciaEleitoralDAO.recuperaVinculacaoDependenciaEleitoralPorEleicaoEstado(eleicao, estado);
	}

	/**
	 * Recupera, se existente, a vinculação de pendência existente para o processo dado.
	 * 
	 * @param processoJudicial o processo a partir do qual será extraída a informação
	 * @return a vinculação, se existente, ou nulo
	 * @throws PJeBusinessException
	 */
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependencia(ProcessoTrf processoJudicial) throws PJeBusinessException {
		ComplementoProcessoJE complemento = processoJudicial.getComplementoJE();
		VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral = null;
		if(complemento != null){
			Eleicao eleicao = processoJudicial.getComplementoJE().getEleicao();
			if(eleicao != null){
	 			if(eleicao.isGeral()){
	 				vinculacaoDependenciaEleitoral = recuperaVinculacaoDependenciaEleitoral(eleicao, complemento.getEstadoEleicao() , processoJudicial);
	 			}else{
	 				vinculacaoDependenciaEleitoral = recuperaVinculacaoDependenciaEleitoral(eleicao, complemento.getMunicipioEleicao(), processoJudicial);
	 			}
			}
		}
		return vinculacaoDependenciaEleitoral;
	}
	
	public VinculacaoDependenciaEleitoral recuperaVinculacaoDependencia(ComplementoProcessoJE complemento) throws PJeBusinessException {
		VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral = null;
		if(complemento == null){
			throw new PJeBusinessException("dados eleitorais nao encontrados.");
		}
		Eleicao eleicao = complemento.getEleicao();
		if(eleicao != null){
 			if(eleicao.isGeral()){
 				vinculacaoDependenciaEleitoral = recuperaVinculacaoDependenciaEleitoralPorEleicao(eleicao, complemento.getEstadoEleicao());
 			}else{
 				vinculacaoDependenciaEleitoral = recuperaVinculacaoDependenciaEleitoralPorEleicao(eleicao, complemento.getMunicipioEleicao());
 			}
 		}
		return vinculacaoDependenciaEleitoral;
	}
	
	/**
	 * Recupera os processos que estão vinculados a um determinado Vinculo de Dependencia Eleitoral (processos vinculados a uma cadeia especifica).
	 * @param vinculacaoDependenciaEleitoral
	 * @return List<ProcessoTrf> processos pertencentes a uma cadeia.
	 * @throws PJeBusinessException
	 */
	public List<ProcessoTrf> recuperarProcessosAssociadosVinculacaoDependencia(VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral) throws PJeBusinessException{
		return vinculacaoDependenciaEleitoralDAO.recuperarProcessosAssociadosVinculacaoDependencia(vinculacaoDependenciaEleitoral);
	}
}