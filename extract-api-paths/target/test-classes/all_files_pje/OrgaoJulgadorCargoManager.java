/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.OrgaoJulgadorCargoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("orgaoJulgadorCargoManager")
public class OrgaoJulgadorCargoManager extends BaseManager<OrgaoJulgadorCargo> {
	
	@In
	private OrgaoJulgadorCargoDAO orgaoJulgadorCargoDAO;

	public OrgaoJulgadorCargoManager(){
		if (orgaoJulgadorCargoDAO==null){
			orgaoJulgadorCargoDAO = new OrgaoJulgadorCargoDAO();
		}
	}
	protected OrgaoJulgadorCargoDAO getDAO() {
		return orgaoJulgadorCargoDAO;
	}
	public OrgaoJulgadorCargo getById(Integer id){
		return orgaoJulgadorCargoDAO.getById(id);
	}
	
	/**
	 * Recupera o menor valor do acumulador de distribuicao de uma lista da OrgaoJulgadorCargo
	 * @param cargosCompetentes
	 * @return Double menor valor do acumulador de distribuicao de uma lista da OrgaoJulgadorCargo
	 */
	public Double recuperaPisoDistribuicao(List<OrgaoJulgadorCargo> cargosCompetentes) {
		return orgaoJulgadorCargoDAO.recuperaPisoDistribuicao(cargosCompetentes);
	}
	
	/**
	 * Recupera a lista de cargos judiciais excluidos a partir de outra lista de cargos judiciais quando se aplica a regra da Distancia Maxima de Distribuição.
	 * 
	 * @param cargosCompetentes cargos que sofreram a regra de DMD
	 * @param teto 				valor mínimo do acumulador de peso processual para que um cargo seja excluído 
	 * @return List<OrgaoJulgadorCargo> com os cargos excluidos na regra de distancia maxima de distribuição
	 * @throws PJeBusinessException
	 */
	public List<OrgaoJulgadorCargo> recuperarExcluidos(List<OrgaoJulgadorCargo> cargosCompetentes, Double teto) throws PJeBusinessException{
		return orgaoJulgadorCargoDAO.recuperaSuperamTeto(teto, cargosCompetentes);
	}
	
	public void ajustarAcumuladores(OrgaoJulgadorCargo cargo, Double deltaAcumuladorProcesso, Double deltaAcumuladorDistribuicao) {
		orgaoJulgadorCargoDAO.ajustarAcumuladores(cargo, deltaAcumuladorProcesso, deltaAcumuladorDistribuicao);
	}
	
	/**
	 * Recupera a lista de cargos judiciais pertencentes à jurisdição informada que têm a competência.
	 * 
	 * @param jurisdicao a {@link Jurisdicao} na qual se pretende identificar os órgãos com a competência informada.
	 * @param competencia a competência a ser pesquisada
	 * @return a lista de cargos judiciais que têm, entre suas competências atuais ativas, a informada
	 */
	public List<OrgaoJulgadorCargo> recuperaCompetentes(Jurisdicao jurisdicao, Competencia competencia) throws PJeBusinessException {
		if(!competencia.getAtivo()){
			return Collections.emptyList();
		}
		Search search = new Search(OrgaoJulgadorCargo.class);
		search.setDistinct(true);
		addCriteria(search, 
				Criteria.equals("ativo", true), // Cargos ativos
				Criteria.equals("auxiliar", false), // não auxiliares
				Criteria.equals("recebeDistribuicao", true), // que recebem distribuição
				Criteria.greater("valorPeso", 0.0), // e que não têm divisor de peso zero
				Criteria.equals("orgaoJulgador.jurisdicao", jurisdicao), // pertencente à jurisdição passada
				Criteria.equals("orgaoJulgador.orgaoJulgadorCompetenciaList.competencia", competencia), // que tenha a competência dada e
				Criteria.or(Criteria.isNull("orgaoJulgador.orgaoJulgadorCompetenciaList.dataInicio"),
						Criteria.lessOrEquals("orgaoJulgador.orgaoJulgadorCompetenciaList.dataInicio", new Date())), // que a vinculação esteja ativa hoje
				Criteria.or(Criteria.isNull("orgaoJulgador.orgaoJulgadorCompetenciaList.dataFim"),
						Criteria.greaterOrEquals("orgaoJulgador.orgaoJulgadorCompetenciaList.dataFim", new Date()))
				);
		return list(search);
	}
	
	/**
	 * Recupera a lista de cargos judiciais ativos pertencentes a um dado órgão julgador.
	 *  
	 * @param orgaoJulgador o órgão julgador ao qual devem estar vinculados os cargos
	 * @param somenteDistribuiveis indica se devem ser retornados apenas os cargos judiciais que podem receber distribuição 
	 * @return a lista de cargos
	 */
	public List<OrgaoJulgadorCargo> recuperaAtivos(OrgaoJulgador orgaoJulgador, boolean somenteDistribuiveis){
		Search s = new Search(OrgaoJulgadorCargo.class);
		addCriteria(s, 
				Criteria.equals("auxiliar", false),
				Criteria.equals("orgaoJulgador", orgaoJulgador));
		if(somenteDistribuiveis){
			addCriteria(s, 
					Criteria.equals("recebeDistribuicao", true),
					Criteria.greater("valorPeso", 0d));
		}
		return list(s);
	}
	
	/**
	 * Método responsável por recuperar o cargo em exercício do órgão julgador especificado
	 * @param orgaoJulgador Dados do órgão julgador
	 * @return O cargo em exercício do órgão julgador especificado
	 */
	public OrgaoJulgadorCargo getOrgaoJulgadorCargoEmExercicio(OrgaoJulgador orgaoJulgador) {
		// Recupera o cargo titular.
		OrgaoJulgadorCargo orgaoJulgadorCargo = orgaoJulgadorCargoDAO.getOrgaoJulgadorCargo(orgaoJulgador, false);
		
		if (orgaoJulgadorCargo == null) {
			// Recupera o cargo auxiliar.
			orgaoJulgadorCargo = orgaoJulgadorCargoDAO.getOrgaoJulgadorCargo(orgaoJulgador, true);				
		}
		
		return orgaoJulgadorCargo;
	}
	
	public List<OrgaoJulgadorCargo> obterCargosJudiciais(OrgaoJulgador orgaoJulgador, Boolean auxiliar, Boolean recebeDistribuicao){
		return getDAO().obterCargosJudiciais(orgaoJulgador, auxiliar, recebeDistribuicao);
	}
	
}
