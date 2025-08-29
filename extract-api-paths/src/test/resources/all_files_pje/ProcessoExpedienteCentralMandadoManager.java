package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoExpedienteCentralMandadoDAO;
import br.jus.cnj.pje.business.dao.HistoricoProcessoExpedienteCentralMandadoDAO;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.HistoricoProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;
import br.jus.pje.search.Search;

@Name(ProcessoExpedienteCentralMandadoManager.NAME)
@Scope(ScopeType.EVENT)
public class ProcessoExpedienteCentralMandadoManager extends BaseManager<ProcessoExpedienteCentralMandado> {
	public static final String NAME = "processoExpedienteCentralMandadoManager";

	
	@In
	private ProcessoExpedienteCentralMandadoDAO processoExpedienteCentralMandadoDAO;
	@In
	private HistoricoProcessoExpedienteCentralMandadoDAO historicoProcessoExpedienteCentralMandadoDAO;
	
	/**
	 * Método que retorna uma lista de mandados que devem ser redistribuidos de acordo com:
	 * 
	 *  <li>Árvore de Localização do Usuário;
	 *  <li>Criterios definidos no {@link search}.
	 * 
	 * @param search
	 * @param idsLocalizacoesFilhasInt
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public List<ProcessoExpedienteCentralMandado> listMandadosParaRedistribuicao(
			Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		
		return processoExpedienteCentralMandadoDAO.listProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.R, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}
	
	/**
	 * Método que retorna o total de mandados que devem ser redistribuidos de acordo com:
	 * 
	 *  <li>Árvore de Localização do Usuário;
	 *  <li>Criterios definidos no {@link search}.
	 * 
	 * @param search
	 * @param idsLocalizacoesFilhasInt
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return Long
	 */
	public Long countMandadosParaRedistribuicao(Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		return processoExpedienteCentralMandadoDAO.countProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.R, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}
	
	/**
	 * Método que retorna uma lista de mandados que foram distribuídos para o oficial de justiça informado via parâmetro (idUsuario) de acordo com:
	 * 
	 *  <li>Criterios definidos no {@link search};
	 *  <li>Nome da Parte (recebido como parâmetro).
	 *  
	 * @param search
	 * @param idUsuario
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public List<ProcessoExpedienteCentralMandado> listMandadosParaCumprimento(Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhas, Integer  idUsuario, String nomeParte){
		return processoExpedienteCentralMandadoDAO.listProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.A, search, null, usuarioLocalizacao, idsLocalizacoesFilhas, idUsuario, nomeParte);
	}

	
	/**
	 * Método que retorna o total de mandados que foram distribuídos para o oficial de justiça informado via parâmetro (idUsuario) de acordo com:
	 * 
	 *  <li>Criterios definidos no {@link search};
	 *  <li>Nome da Parte (recebido como parâmetro).
	 *  
	 * @param search
	 * @param idUsuario
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public Long countMandadosParaCumprimento(Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhas, Integer  idUsuario, String nomeParte){
		return processoExpedienteCentralMandadoDAO.countProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.A, search, null, usuarioLocalizacao, idsLocalizacoesFilhas, idUsuario, nomeParte);
	}
	
	/**
	 * Método que retorna o total de mandados que foram encerrados para o distribuidor
	 * 
	 *  <li>Árvore de Localização do Usuário;
	 *  <li>Criterios definidos no {@link search};
	 *  <li>Nome da Parte (recebido como parâmetro).
	 *  
	 * @param search
	 * @param idsLocalizacoesFilhasInt
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public Long countMandadosEncerradosDistribuidor(Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		return processoExpedienteCentralMandadoDAO.countProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.C, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}
		
	/**
	 * Método que retorna uma lista de mandados que foram encerrados
	 * 
	 *  <li>Árvore de  Localização do Usuário;
	 *  <li>Criterios definidos no {@link search};
	 *  <li>Nome da Parte (recebido como parâmetro).
	 *  
	 * @param search
	 * @param idsLocalizacoesFilhasInto
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public List<ProcessoExpedienteCentralMandado> listMandadosEncerradosDistribuidor(
			Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		
		return processoExpedienteCentralMandadoDAO.listProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.C, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}

	/**
	 * Método que retorna uma lista de mandados que foram distribuídos e estão em andamento de acordo com:
	 * 
	 *  <li>Árvore de  Localização do Usuário;
	 *  <li>Criterios definidos no {@link search};
	 *  <li>Nome da Parte (recebido como parâmetro).
	 *  
	 * @param search
	 * @param idsLocalizacoesFilhasInto
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public List<ProcessoExpedienteCentralMandado> listMandadosJaDistribuidos(
			Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		
		return processoExpedienteCentralMandadoDAO.listProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.A, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}
	
	/**
	 * Método que retorna o total de mandados que foram distribuídos e estão em andamento de acordo com:
	 * 
	 *  <li>Árvore de Localização do Usuário;
	 *  <li>Criterios definidos no {@link search};
	 *  <li>Nome da Parte (recebido como parâmetro).
	 *  
	 * @param search
	 * @param idsLocalizacoesFilhasInt
	 * @param nomeParte, caso o nome da parte esteja nulo não será considerado na consulta.
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public Long countMandadosJaDistribuidos(Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		return processoExpedienteCentralMandadoDAO.countProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.A, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}
	
	/**
	 * Método que retorna uma lista de mandados que devem ser distribuídos de acordo com:
	 * 
	 *  <li>Árvore de Localização do Usuário;
	 *  <li>Criterios definidos no {@link search}.
	 * 
	 * @param search
	 * @param idsLocalizacoesFilhasInt
	 * @return List<{@link ProcessoExpedienteCentralMandado}>
	 */
	public List<ProcessoExpedienteCentralMandado> listMandadosParaDistribuicao(
			Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		
		return processoExpedienteCentralMandadoDAO.listProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.N, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}	
	
	/**
	 * Método que retorna o total de mandados que devem ser distribuídos de acordo com:
	 * 
	 *  <li>Árvore de Localização do Usuário;
	 *  <li>Criterios definidos no {@link search}.
	 * 
	 * @param search
	 * @param idsLocalizacoesFilhasInt
	 * @return Long
	 */
	public Long countMandadosParaDistribuicao(Search search, UsuarioLocalizacao usuarioLocalizacao, List<Integer> idsLocalizacoesFilhasInt, String nomeParte){
		return processoExpedienteCentralMandadoDAO.countProcessoExpedienteCentralMandado(
				ProcessoExpedienteCentralMandadoStatusEnum.N, search, null, usuarioLocalizacao, idsLocalizacoesFilhasInt, null, nomeParte);
	}
	
	@Override
	protected BaseDAO<ProcessoExpedienteCentralMandado> getDAO() {
		return processoExpedienteCentralMandadoDAO;
	}
	
	public String recuperaNomeCentralExpediente(ProcessoExpediente processoExpediente) {
		ProcessoExpedienteCentralMandado processoExpedienteCM = this.processoExpedienteCentralMandadoDAO.obterPorProcessoExpediente(processoExpediente);
		
		String textoCentralMandado = " Central de Mandado não encontrada.";
		if ((processoExpedienteCM != null) && (processoExpedienteCM.getCentralMandado() != null)) {
			textoCentralMandado = processoExpedienteCM.getCentralMandado().getCentralMandado();
		}
		if (textoCentralMandado.contains(ExpedicaoExpedienteEnum.M.getLabel())) {
			return textoCentralMandado;
		}
		
		return ExpedicaoExpedienteEnum.M.getLabel() + ": " + textoCentralMandado;
	}
	
	public List<String> recuperaHistoricoCentralMandado(ProcessoExpediente processoExpediente) {
		List<String> lista = new ArrayList<>();
		if (processoExpediente.getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M)) {
			List<HistoricoProcessoExpedienteCentralMandado> historicos = this.historicoProcessoExpedienteCentralMandadoDAO.obterPorProcessoExpedienteCentralMandado(processoExpediente);
			
			for (HistoricoProcessoExpedienteCentralMandado historico : historicos) {
				lista.add("Redirecionamento: De " + historico.getCentralMandadoAnterior().getCentralMandado() + " para " + historico.getCentralMandadoNova().getCentralMandado());
			}
		}
		
		
		return lista;
	}
}
