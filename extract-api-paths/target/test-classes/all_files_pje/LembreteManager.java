package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.LembreteDAO;
import br.jus.cnj.pje.entidades.vo.LembreteVO;
import br.jus.pje.nucleo.entidades.Lembrete;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(LembreteManager.NAME)
public class LembreteManager extends BaseManager<Lembrete> {
	
	public static final String NAME = "lembreteManager";
	
	@In
	LembreteDAO lembreteDAO;
	
	@Override
	protected BaseDAO<Lembrete> getDAO() {
		return lembreteDAO;
	}
	
	/**
	 * Metodo que recupera uma lista de lembretes pelo id do processo, 
	 * com a data de visibilidade menor que a data atual e
	 * não tenham data de exclusão
	 * @param idProcessoTrf
	 * @return lista com as lembretes que atenderam as condições dos filtros
	 */
	public List<Lembrete> recuperarLembretesVisiveisAtivosPorIdProcesso(Integer idProcessoTrf){
		Search search = new Search(Lembrete.class);
		addCriteria(search, Criteria.equals("processoTrf.idProcessoTrf", idProcessoTrf));
		criterioVisivelAtivo(search);
		return list(search);
	}
	
	/**
	 * Metodo que recupera uma lista de lembretes pelo id do processo, 
	 * com a data de visibilidade menor que a data atual e
	 * não tenham data de exclusão
	 * @param idProcessoTrf
	 * @return lista com as lembretes que atenderam as condições dos filtros
	 */
	public List<Lembrete> recuperarLembretesVisiveisAtivosPorIdProcessoDocumento(Integer idProcessoDocumento){
		Search search = new Search(Lembrete.class);
		addCriteria(search, Criteria.equals("processoDocumento.idProcessoDocumento", idProcessoDocumento));
		criterioVisivelAtivo(search);
		return list(search);
	}
	
	/**
	 * Metodo auxiliar que inclui os filtros para data de exclusão nula
	 * e data de vizibilidade ate a data atual
	 * @param search
	 */
	private void criterioVisivelAtivo(Search search) {
		addCriteria(search, Criteria.equals("ativo",true));
		addCriteria(search, Criteria.lessOrEquals("dataVisivelAte", new Date()));
	}
	
	/**
	 * Metodo que recupera lista formatada de lembretes do usuário com parametros 
	 * @param Lembrete
	 * @param dataInicial
	 * @param dataFinal
	 * @return List<LembreteVO> lista formatada de lembretes
	 */
	public List<LembreteVO> recuperarLembretes(Lembrete lembrete,
			Date dataInicial, Date dataFinal){
		return lembreteDAO.recuperaLembretes(lembrete,dataInicial,dataFinal);
	}
	
	/**
	 * Metodo que recupera lista de lembretes formatada pelo usuário, IdProcessoDocumento
	 * e que não tenham sido excluídas e que estejam visível
	 * @param idProcessoDocumento
	 * @return List<LembreteVO> lista formatada de lembretes
	 */
	public List<LembreteVO> recuperarLembretesPorSituacaoPorIdProcessoDocumento(
			Boolean ativo, Integer idProcessoDocumento) {
		return lembreteDAO.recuperarLembretesPorSituacaoPorIdProcessoDocumento(ativo,idProcessoDocumento);
	}
	
	/**
	 * Metodo que recupera lista de lembretes formatada pelo usuário, idProcessoTrf
	 * e que não tenham sido excluídas e que estejam visível
	 * @param ativo
	 * @param idProcessoTrf
	 * @return List<LembreteVO> lista formatada de lembretes
	 */
	public List<LembreteVO> recuperarLembretesPorSituacaoPorIdProcessoTrf(Boolean ativo, Integer idProcessoTrf) {
		return lembreteDAO.recuperarLembretesPorSituacaoPorIdProcessoTrf(ativo, idProcessoTrf);
	}
	
	/**
	 * Metodo que recupera lista de todos os lembretes formatada pelo usuário, idProcessoTrf
	 * e que não tenham sido excluídas e que estejam visível
	 * @param ativo
	 * @param idProcessoTrf
	 * @param todosDocumentos
	 * @return List<LembreteVO> lista formatada de lembretes
	 */
	public List<LembreteVO> recuperarLembretesPorSituacaoPorIdProcessoTrf(Boolean ativo, Integer idProcessoTrf, Boolean todosDocumentos) {
		return lembreteDAO.recuperarLembretesPorSituacaoPorIdProcessoTrf(ativo, idProcessoTrf, todosDocumentos);
	}
	
	/**
	 * Metodo que verifica se existe lemprete visível para o processo
	 * @param idProcessoTrf
	 * @return Boolean (Verdadeiro ou Falso)
	 */
	public Boolean verificaLembretesPorSituacaoPorIdProcessoTrf(
			 Boolean ativo, Integer idProcessoTrf) {
		return lembreteDAO.verificaLembretesPorSituacaoPorIdProcessoTrf(ativo, idProcessoTrf);
	}
	
	/**
	 * Metodo que verifica se existe lemprete visível para o documento
	 * @param idProcessoDocumento
	 * @return Boolean (Verdadeiro ou Falso)
	 */
	public Boolean verificaLembretesPorSituacaoPorIdProcessoDocumento(
			Boolean ativo, Integer idProcessoDocumento) {
		return lembreteDAO.verificaLembretesPorSituacaoPorIdProcessoDocumento(ativo,idProcessoDocumento);
	}
	
	/**
	 * Metodo responsável por inativar lembrete pelo id
	 * @param idLembrete
	 */
	public void inativaLembretesPorId(Integer idLembrete) {
		lembreteDAO.inativaLembretesPorId(idLembrete);
	}
	
	/**
	 * Metodo responsável por inativar lembrete por documento
	 * @param idLembrete
	 */
	public void inativaLembretesPorDocumento(ProcessoDocumento documento) {
		lembreteDAO.inativaLembretesPorDocumento(documento);
	}
	
	/**
	 * Metodo que retorna a quantidade de lempretes visível para o documento.
	 * 
	 * @param idProcessoDocumento
	 * @param ativo
	 * @return Integer - Quantidade de lembretes dos parametros
	 */
	public Integer retornaQuantidadeLembretesPorSituacaoPorIdProcessoDocumento(Boolean ativo, Integer idProcessoDocumento) {
		return lembreteDAO.retornaQuantidadeLembretesPorSituacaoPorIdProcessoDocumento(ativo, idProcessoDocumento);
	}
	
	/**
	 * Metodo que retorna a quantidade de lempretes visível para o processo.
	 * 
	 * @param idProcessoTrf
	 * @param ativo
	 * @return Integer - Quantidade de lembretes dos parametros
	 */
	public Integer retornaQuantidadeLembretesPorSituacaoPorIdProcesso(Boolean ativo, Integer idProcesso) {
		return lembreteDAO.retornaQuantidadeLembretesPorSituacaoPorIdProcesso(ativo, idProcesso);
	}
	
	/**
	 * metodo para recuperar todos os lembretes criados pela pessoa passada em parametro
	 * @param pessoaSecundaria
	 * @return
	 * @throws Exception 
	 */
	public List<Lembrete> recuperarLembretes(Pessoa _pessoa) throws Exception {
		return lembreteDAO.recuperarLembretes(_pessoa);
	}

}
