/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.ProcessoCaixaAdvogadoProcuradorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.MotivoMovimentacaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de gerenciamento negocial da entidade {@link ProcessoCaixaAdvogadoProcurador}.
 * 
 * @author cristof
 * @since 1.4.6.2.RC4
 *
 */
@Name(ProcessoCaixaAdvogadoProcuradorManager.NAME)
public class ProcessoCaixaAdvogadoProcuradorManager extends BaseManager<ProcessoCaixaAdvogadoProcurador> {
	public static final String NAME = "processoCaixaAdvogadoProcuradorManager";
	
	@In
	private ProcessoCaixaAdvogadoProcuradorDAO processoCaixaAdvogadoProcuradorDAO;
	
	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	@In
	private PessoaProcuradoriaJurisdicaoManager pessoaProcuradoriaJurisdicaoManager;
	
	@In
	private ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager;
	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	boolean msgExpNaoMovido = false;
	
	@Override
	protected ProcessoCaixaAdvogadoProcuradorDAO getDAO() {
		return processoCaixaAdvogadoProcuradorDAO;
	}
	
	public static ProcessoCaixaAdvogadoProcuradorManager instance() {
		return (ProcessoCaixaAdvogadoProcuradorManager)Component.getInstance(ProcessoCaixaAdvogadoProcuradorManager.NAME);
	}
		
	private void incluirEmCaixa(ProcessoTrf processo,
			CaixaAdvogadoProcurador caixaInclusao,
			CaixaAdvogadoProcurador caixaExclusao) throws PJeBusinessException {
				
		if (processo.getJurisdicao() != null && !caixaInclusao.getJurisdicao().equals(processo.getJurisdicao())) {			
			throw new PJeBusinessException("Não é possível incluir o processo em uma caixa de jurisdição diversa da sua.");		
			
		} 
		if (!caixaContem(caixaInclusao, processo)) {
			ProcessoCaixaAdvogadoProcurador tag = new ProcessoCaixaAdvogadoProcurador();
			tag.setProcessoTrf(processo);
			tag.setCaixaAdvogadoProcurador(caixaInclusao);
			persistAndFlush(tag);
			
			if (caixaExclusao != null) {
				//Exclui o processo da caixa antiga
				remover(caixaExclusao, false, processo);
			}		
		}		
	}
		
	public void incluirEmCaixa(CaixaAdvogadoProcurador caixaInclusao,
			CaixaAdvogadoProcurador caixaExclusao, ProcessoTrf... processos)
			throws PJeBusinessException {
				
		for (ProcessoTrf processo : processos) {
			try {				
				incluirEmCaixa(processo, caixaInclusao, caixaExclusao);
//				incluirExpedientesSemCaixa(processo, caixaInclusao, caixaExclusao);
			} catch (PJeBusinessException e) {				
				String msg = String.format("Falha ao movimentar processo %s para \"%s\": %s", 
						processo.getNumeroProcesso(),
						caixaInclusao.getNomeCaixaAdvogadoProcurador(), 
						e.getCode());
				throw new PJeBusinessException(msg);
			}
		}
		
		// definindo as operações para histórico de movimentações
		if (caixaExclusao != null) {
			// trata-se de redistribiução
			Events.instance().raiseEvent(
					Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA,
					Arrays.asList(processos), Calendar.getInstance(),
					caixaInclusao, MotivoMovimentacaoEnum.I,
					Authenticator.getUsuarioLogado());
			
		} else {
			// trata-se de distribuição
			Events.instance().raiseEvent(
					Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA,
					Arrays.asList(processos), Calendar.getInstance(),
					caixaInclusao, MotivoMovimentacaoEnum.D,
					Authenticator.getUsuarioLogado());
			
		}
		
	}

	/**
	 * Remove todos os processos indicados da caixa informada.
	 * 
	 * @param caixa
	 *            a caixa da qual os processos devem ser removidos.
	 * 
	 * @param registrarMovimentacao
	 *            Indicador para gravar registro de movimentação de devlução do
	 *            processo para a jurisdição
	 * 
	 * @param processos
	 *            os processos a serem removidos
	 * @throws PJeBusinessException
	 */
	public void remover(CaixaAdvogadoProcurador caixa,
			boolean registrarMovimentacao, ProcessoTrf... processos)
			throws PJeBusinessException {
		
		List<ProcessoTrf> pl = new ArrayList<ProcessoTrf>();
		for (ProcessoTrf processo : processos) {
			pl.add(processo);
		}

		processoCaixaAdvogadoProcuradorDAO.remover(caixa, processos);
		
			
		if (registrarMovimentacao) {		
			Events.instance().raiseEvent(
					Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA, pl,
					Calendar.getInstance(), caixa,
					MotivoMovimentacaoEnum.A, Authenticator.getUsuarioLogado());
		}
		
	}

	public boolean caixaContem(CaixaAdvogadoProcurador cx, ProcessoTrf proc) throws PJeBusinessException {
		Search s = new Search(ProcessoCaixaAdvogadoProcurador.class);
		addCriteria(s, Criteria.equals("caixaAdvogadoProcurador", cx));
		addCriteria(s, Criteria.equals("processoTrf", proc));
		return count(s) > 0;
	}
	
	/**
	 * Verifica os expedientes que não estejam em nenhuma caixa
	 * e os move para a mesma caixa da inclusão do processo.
	 * @param processo
	 * @param caixaInclusao
	 * @param caixaExclusao
	 * @param ppes
	 * @throws PJeBusinessException
	 */
	public void incluirExpedientesSemCaixa(ProcessoTrf processo,CaixaAdvogadoProcurador caixaInclusao,
			CaixaAdvogadoProcurador caixaExclusao) throws PJeBusinessException {
		List<ProcessoParteExpediente> processosExpediente = processoParteExpedienteManager.listExpUsuario(processo);
		List<ProcessoParteExpediente> listExpedCaixa = new ArrayList<ProcessoParteExpediente>();
		
		for (ProcessoParteExpediente procExp : processosExpediente) {
			if (!processoParteExpedienteCaixaAdvogadoProcuradorManager.contemExpedienteEmCaixa(procExp)) {
				listExpedCaixa.add(procExp);
			} else if (!msgExpNaoMovido) {
				msgExpNaoMovido = true;
			}
		}
		if(listExpedCaixa.size() > 0) {
			processoParteExpedienteCaixaAdvogadoProcuradorManager.incluirEmCaixa(caixaInclusao, caixaExclusao, (ProcessoParteExpediente[]) listExpedCaixa.toArray(new ProcessoParteExpediente[listExpedCaixa.size()]));
		}
	}
	
	/**
	 * Mtodo responsvel por recuperar a lista de Caixas de Advogados vinculadas ao Processo a partir do processo especificado
	 * @param processoTrf Dados do processo
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<ProcessoCaixaAdvogadoProcurador> recuperarPorProcesso(ProcessoTrf processoTrf) throws PJeBusinessException {
		Search s = new Search(ProcessoCaixaAdvogadoProcurador.class);
		addCriteria(s, Criteria.equals("processoTrf", processoTrf));
		return list(s);
	}
}