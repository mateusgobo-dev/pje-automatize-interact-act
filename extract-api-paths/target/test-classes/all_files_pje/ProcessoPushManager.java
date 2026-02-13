/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoPushDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

/**
 * Componente de gerenciamento dos registros de push.
 * 
 * @author cristof
 *
 */
@Name("processoPushManager")
public class ProcessoPushManager extends BaseManager<ProcessoPush>{
	
	@In
	private ProcessoPushDAO processoPushDAO;
	
	@In
	private PessoaAdvogadoManager pessoaAdvogadoManager;

	@Override
	protected ProcessoPushDAO getDAO() {
		return processoPushDAO;
	}
	
	public static ProcessoPushManager instance() {
		return ComponentUtil.getComponent("processoPushManager");
	}
	
	/**
	 * Método responsável por reativar os processos da lista de push do usuário os quais foram excluídos.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @return Quantidade de processos reativados.
	 * @throws PJeBusinessException Caso algum erro ocorra durante a gravação.
	 */
	public int reativarAcompanhamento(Pessoa pessoa) throws PJeBusinessException {
		int count = 0;
		List<ProcessoPush> processosPush = this.processoPushDAO.recuperarProcessosPush(pessoa, false);
		for (ProcessoPush processoPush : processosPush) {
			processoPush.setDtExclusao(null);
			persist(processoPush);
			count++;
		}
		EntityUtil.getEntityManager().flush();
		return count;
	}
	
	/**
	 * Método responsável por incluir processos na lista de push do usuário.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param processosTrf {@link ProcessoTrf}.
	 * @return Quantidade de processos incluídos na lista de push do usuário.
	 * @throws PJeBusinessException Caso algum erro ocorra durante a gravação.
	 */
	public int acompanharProcessos(Pessoa pessoa, List<ProcessoTrf> processosTrf) throws PJeBusinessException {
		int count = 0;
		ProcessoPush processoPush = null;
		Date dataAtual = DateService.instance().getDataHoraAtual();
		for (ProcessoTrf processoTrf : processosTrf) {
			processoPush = new ProcessoPush(pessoa, processoTrf);
			processoPush.setDtInclusao(dataAtual);
			persist(processoPush);
			count++;
		}
		EntityUtil.getEntityManager().flush();
		return count;
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoPush} associado à {@link Pessoa}.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param processoTrf {@link ProcessoPush}.
	 * @return {@link ProcessoPush} associado à {@link Pessoa}.
	 */
	public ProcessoPush recuperarProcessoPush(Pessoa pessoa, ProcessoTrf processoTrf) {
		return this.processoPushDAO.recuperarProcessoPush(pessoa, processoTrf);
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoPush} associado à {@link pessoaPush}.
	 * 
	 * @param pessoaPush {@link pessoaPush}.
	 * @param processoTrf {@link ProcessoPush}.
	 * @return {@link ProcessoPush} associado à {@link pessoaPush}.
	 */
	public ProcessoPush recuperarProcessoPush(PessoaPush pessoaPush, ProcessoTrf processoTrf) {
		return this.processoPushDAO.recuperarProcessoPush(pessoaPush, processoTrf);
	}
	
	/**
	 * Método responsável por recuperar uma lista de {@link ProcessoPush} associados à {@link Pessoa}.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param ativo Indica que o {@link ProcessoPush} foi (ou não foi) excluído.
	 * @return Lista de {@link ProcessoPush} associados à {@link Pessoa}.
	 */
	public List<ProcessoPush> recuperarProcessosPush(Pessoa pessoa, Boolean ativo) {
		return this.processoPushDAO.recuperarProcessosPush(pessoa, ativo);
	}
	
	/**
	 * Método responsável por recuperar uma lista de {@link ProcessoPush} associados à {@link PessoaPush}.
	 * 
	 * @param pessoaPush {@link PessoaPush}.
	 * @param ativo Indica que o {@link ProcessoPush} foi (ou não foi) excluído.
	 * @return Lista de {@link ProcessoPush} associados à {@link PessoaPush}.
	 */
	public List<ProcessoPush> recuperarProcessosPush(PessoaPush pessoaPush, Boolean ativo) {
		return this.processoPushDAO.recuperarProcessosPush(pessoaPush, ativo);
	}
	
	public List<ProcessoPush> recuperarProcessoPushPorProcesso(ProcessoTrf processoTrf) {
		return this.processoPushDAO.recuperarProcessoPushPorProcesso(processoTrf);
	}

	public void inserirNoPush(List<ProcessoParte> advogados) {
		for (ProcessoParte processoParte : advogados) {
			inserirNoPush(processoParte);
		}
	}

	public void inserirNoPush(ProcessoParte processoParte) {		
		if (processoParte != null) {
			PessoaAdvogado pessoaAdvogado = pessoaAdvogadoManager.getPessoaAdvogado(processoParte.getPessoa().getIdPessoa());
			if(pessoaAdvogado != null && Boolean.TRUE.equals(pessoaAdvogado.getIncluirProcessoPush())) {
				try {
					inserirNoPush(processoParte.getProcessoTrf(), processoParte.getPessoa(), null, null);
				} catch (PJeBusinessException e) {
					// do nothing.
				}
			}
		}
	}
	
	public void inserirNoPush(ProcessoTrf processoTrf, Pessoa pessoa, PessoaPush pessoaPush, String observacao) throws PJeBusinessException {
		ProcessoPush processoPush = null;
		if (processoTrf != null && ProcessoStatusEnum.D.equals(processoTrf.getProcessoStatus()) && (pessoa != null || pessoaPush != null)) {
			if (pessoa != null) {
				processoPush = recuperarProcessoPush(pessoa, processoTrf);
			} else {
				processoPush = recuperarProcessoPush(pessoaPush, processoTrf);
			}		
			if (processoPush == null) {
				persist(criarProcessoPush(processoTrf, pessoa, pessoaPush, observacao));
			} else if (processoPush.getDtExclusao() != null) {
				merge(atualizarDados(processoPush, observacao));
			} else {
				throw new PJeBusinessException(
					"O processo " + processoTrf.getNumeroProcesso() + " já encontra-se na sua lista de processos do Push.");
			}
		}
	}
	
	private ProcessoPush criarProcessoPush(ProcessoTrf processoTrf, Pessoa pessoa, PessoaPush pessoaPush, String observacao) {
		ProcessoPush processoPush = new ProcessoPush();
		Date dataAtual = DateService.instance().getDataHoraAtual();
		processoPush.setPessoa(pessoa);
		processoPush.setPessoaPush(pessoaPush);
		processoPush.setProcessoTrf(processoTrf);
		processoPush.setDsObservacao(observacao);
		processoPush.setDtInclusao(dataAtual);
		return processoPush;
	}
	
	private ProcessoPush atualizarDados(ProcessoPush processoPush, String observacao) {
		Date dataAtual = DateService.instance().getDataHoraAtual();
		processoPush.setDsObservacao(observacao);
		processoPush.setDtInclusao(dataAtual);
		processoPush.setDtExclusao(null);
		return processoPush;
	}

	public void removeProcessoPush(ProcessoTrf processo, Pessoa pessoa) throws PJeBusinessException {
		ProcessoPush pp = instance().recuperarProcessoPush(pessoa, processo);
		if(pp != null && pp.getDtExclusao() == null) {
			pp.setDtExclusao(Calendar.getInstance().getTime());
			instance().flush();
		}
	}
	
	public void removeProcessoPush(ProcessoPush processoPush) throws PJeBusinessException {
		if(processoPush != null) {
			processoPush.setDtExclusao(Calendar.getInstance().getTime());
			instance().flush();
		}
	}

}
