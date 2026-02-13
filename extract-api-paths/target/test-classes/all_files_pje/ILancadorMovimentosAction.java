package br.jus.cnj.pje.servicos;

import java.util.List;

import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.MovimentoBean;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Tarefa;

/**
 * Interface do componente Seam 'lancadorMovimentosAction', usada pelo fato de
 * que o projeto FI-BPM não enxerga o projeto PJE.
 * 
 * @author David, Kelly
 */
public interface ILancadorMovimentosAction {

	public static final String NAME = "lancadorMovimentosAction";

	/**
	 * Retorna um MovimentoBean preenchido com os ComplementoBeans para o Evento
	 * desejado.
	 * 
	 * @param evento
	 *            Evento processual donde serão buscados os complementos.
	 * @return MovimentoBean totalmente preenchido
	 * 
	 * @author David, Kelly
	 * 
	 */
	public MovimentoBean getMovimentoBeanPreenchido(Evento evento);

	/**
	 * Lança os Movimentos associados aos EventoBeans.
	 * 
	 * @param eventoBeanList
	 *            Lista de eventoBeans a serem lançados
	 * @param processoDocumento
	 *            Documento que está sendo anexado
	 * @param processo
	 *            Processo do jbpm
	 * @param idJbpmTask
	 *            Identificador da tarefa do jbpm
	 * @param idProcessInstance
	 *            Identificador do processInstance
	 * @param tarefa
	 *            Tarefa do jbpm (nó de tarefa)
	 * @param podeRegistrarMovimentosTemporarios 
	 * 			  Se o lançador de movimentos que invoca esse método pode lançar movimentos temporários
	 * @param agrupamentosInstance 
	 * 			  Lista de ids, separados por vírgula, de EventoAgrupamentos
	 * 
	 * @author David, Kelly
	 */
	public void lancarMovimentos(List<EventoBean> eventoBeanList, ProcessoDocumento processoDocumento,
			Processo processo, Long idJbpmTask, Long idProcessInstance, Tarefa tarefa, boolean podeRegistrarMovimentosTemporarios, Integer agrupamentosInstance);
	
	/**
	 * Lança movimento quando a operação associada não estiver relacionada a execução de um fluxo
	 * @param eventoBeanList
	 * @param processoDocumento
	 * @param processo
	 */
	public void lancarMovimentosSemFluxo(List<EventoBean> eventoBeanList,
			ProcessoDocumento processoDocumento, Processo processo);

	
	/**
	 * @return Se, na configuração de fluxo, foi definido que deve ser gravado temporariamente o lançamento
	 */
	public abstract boolean deveGravarTemporariamente();
	
}
