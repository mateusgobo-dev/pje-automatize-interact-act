package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ClasseJudicialDAO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.ClasseJudicialDTO;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name(ClasseJudicialManager.NAME)
public class ClasseJudicialManager extends BaseManager<ClasseJudicial>{
	public static final String NAME="classeJudicialManager";
	
	@In
	private ClasseJudicialDAO classeJudicialDAO;

	@Override
	protected ClasseJudicialDAO getDAO() {
		return classeJudicialDAO;
	}
	
	/**
	 * @return ClasseJudicialManager
	 */
	public static ClasseJudicialManager instance() {
		return (ClasseJudicialManager)Component.getInstance(ClasseJudicialManager.NAME);
	}
	
	/**
	 * Recupera a lista de classes judiciais que fazem parte do(s) agrupamento(s) dados.
	 * 
	 * @param agrupamentos a lista de agrupamentos cujas classes se pretende recuperar
	 * @return a lista de classes que fazem parte do agrupamento dado
	 */
	public List<ClasseJudicial> getClassesAgrupadas(AgrupamentoClasseJudicial...agrupamentos){
		if(agrupamentos == null || agrupamentos.length == 0){
			return Collections.emptyList();
		}
		return classeJudicialDAO.getClassesAgrupadas(agrupamentos);
	}
	
	
	public ClasseJudicial findByCodigo(String codigo){
		boolean apenasClassesAtivas = true;
		return classeJudicialDAO.findByCodigo(codigo, apenasClassesAtivas);
	}
	
	
	public ClasseJudicial findByCodigo(String codigo, boolean apenasClassesAtivas){
		return classeJudicialDAO.findByCodigo(codigo, apenasClassesAtivas);
	}
	
	/**
	 * Recupera classes judiciais iniciais.
	 * @param idJurisdicao
	 * @return List<ClasseJudicial>
	 */
	public List<ClasseJudicial> recuperarClassesJudiciaisIniciais(int idJurisdicao) {
		return getDAO().recuperarClassesJudiciais(idJurisdicao, true, false, 0, 0);
	}
	
	public List<ClasseJudicial> recuperarClassesJudiciaisIniciais(int idJurisdicao, int idCompetencia) {
		return getDAO().recuperarClassesJudiciais(idJurisdicao, true, false, idCompetencia, 0);
	}
	
	public List<ClasseJudicial> recuperarClassesJudiciaisIniciais(int idJurisdicao, int idCompetencia, int idClasseJudicial) {
		return getDAO().recuperarClassesJudiciais(idJurisdicao, true, false, idCompetencia, idClasseJudicial);
	}
	
	/**
	 * Recupera classes judiciais incidentais.
	 * @param idJurisdicao
	 * @return List<ClasseJudicial>
	 */
	public List<ClasseJudicial> recuperarClassesJudiciaisIncidentais(int idJurisdicao) {
		return getDAO().recuperarClassesJudiciais(idJurisdicao, false, true);
	}
	
	public List<ClasseJudicial> recuperarClassesJudiciaisIncidentais(int idJurisdicao, int idCompetencia){
		return getDAO().recuperarClassesJudiciais(idJurisdicao, false, true, idCompetencia, 0);
	}
	
	public List<ClasseJudicial> recuperarClassesJudiciaisIncidentais(int idJurisdicao, int idCompetencia, int idClasseJudicial){
		return getDAO().recuperarClassesJudiciais(idJurisdicao, false, true, idCompetencia, idClasseJudicial);
	}

	public List<ClasseJudicial> recuperarClassesJudiciaisIncidentais(int idJurisdicao, int idCompetencia, int idClasseJudicial, boolean somenteIncidental){
		return getDAO().recuperarClassesJudiciais(idJurisdicao, false, true, idCompetencia, 0, somenteIncidental);
	}
	
	/**
	 * Recupera classes judiciais retificação de autos.
	 * @param idJurisdicao
	 * @return List<ClasseJudicial>
	 */
	public List<ClasseJudicial> recuperarClassesJudiciaisRetificacaoAutos(int idJurisdicao) {
		return getDAO().recuperarClassesJudiciaisRetificacaoAutos(idJurisdicao);
	}
	
	/**
	 * Recupera classes judiciais para cadastro de um novo processo em primeiro grau.
	 * @param jusPostulandi
	 * @param idJurisdicao
	 * @param classeJudicialFiltro
	 * @return  List<ClasseJudicial>
	 */
	public List<ClasseJudicial> recuperarClassesJudiciaisNovoProcessoPrimeiroGrau(
			Integer idAreaDireito, Boolean jusPostulandi, int idJurisdicao, String classeJudicialFiltro) {
		
		return getDAO().recuperarClassesJudiciaisNovoProcessoPrimeiroGrau(idAreaDireito, jusPostulandi, idJurisdicao, classeJudicialFiltro);
	}
	
	/**
	 * Recupera se existem classes judiciais retificação de autos baseados nos parâmetros  idJurisdicao e idClasseJudicial.
	 * @param idJurisdicao Id da jurisdição.
	 * @return Boolean existemClassesJudiciaisRetificacaoAutos
	 */
	public boolean isExistemClassesJudiciaisRetificacaoAutos(int idJurisdicao, Integer idClasse) {
		return getDAO().isExistemClassesJudiciaisRetificacaoAutos(idJurisdicao, idClasse);
	}	
	
	public List<ClasseJudicialDTO> findAllClasseJudicialDTO(){
		return this.getDAO().findAllClasseJudicialDTO();
	}

	public boolean isClasseJudicialIncidentalValida(Jurisdicao jurisdicao, OrgaoJulgador orgaoJulgador,
			ClasseJudicial classeJudicial) {
		return classeJudicialDAO.isClasseJudicialIncidentalValida(jurisdicao, orgaoJulgador, classeJudicial);
	}
		
	public boolean isClasseCriminal(ClasseJudicial classeJudicial) {
		return classeJudicialDAO.isClasseCriminal(classeJudicial);
	}

	
	public boolean isClasseInfracional(ClasseJudicial classeJudicial) {
		return classeJudicialDAO.isClasseInfracional(classeJudicial);
	}
	
	public boolean isClasseCriminalOuInfracional(ClasseJudicial classeJudicial) {
		return isClasseCriminal(classeJudicial) || isClasseInfracional(classeJudicial);
	}

	public List<ClasseJudicial> findByCompetencia(int idCompetencia) {
		return classeJudicialDAO.findByCompetencia(idCompetencia);
	}
	
	public List<ClasseJudicial> getByCompetencia (List<Competencia> competencias) {
		return this.getDAO().getClassesDisponiveis(competencias, null, null, null, null, null);
	}	


	/**
	 * @param classeJudicial ClasseJudicial
	 * @return True se a classe for de execução fiscal.
	 */
	public boolean isClasseExecucaoFiscal(ClasseJudicial classeJudicial) {
		return classeJudicialDAO.isClasseExecucaoFiscal(classeJudicial);
	}
}
