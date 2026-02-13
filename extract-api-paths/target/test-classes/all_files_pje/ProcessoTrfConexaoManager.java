package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoTrfConexaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.enums.PrevencaoEnum;

@Name(ProcessoTrfConexaoManager.NAME)
public class ProcessoTrfConexaoManager extends BaseManager<ProcessoTrfConexao> implements Serializable {

	private static final long serialVersionUID = -6480942720657917481L;

	public static final String NAME = "processoTrfConexaoManager";

    @In
    private ProcessoTrfConexaoDAO processoTrfConexaoDAO;

    @Override
    protected ProcessoTrfConexaoDAO getDAO() {
        return processoTrfConexaoDAO;
    }
    
    public static ProcessoTrfConexaoManager instance() {
        return ComponentUtil.getComponent(NAME);
    }

    /**
     * PJEII-3755
     * @param processoTrf Processo PRINCIPAL
     * @param processoTrfConexo Processo CONEXO
     * @return Flag indicando se já existe uma associação entre o processo
     * PRINCIPAL e o CONEXO
     */
    public boolean verificaDuplicidade(ProcessoTrf processoTrf, ProcessoTrf processoTrfConexo) {
        ProcessoTrfConexao ptc = getDAO().getProcessoTrfConexao(processoTrf, processoTrfConexo);

        if (ptc != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * PJEII-3755
     * @param entity ProcessoTrfConexao a ser persistido
     * @return ProcessoTrfConexao persistido
     * @throws PJeBusinessException Se ocorrer alguma exceção
     */
    @Override
    public ProcessoTrfConexao persist(ProcessoTrfConexao entity) throws PJeBusinessException {
        return persist(entity, null);
    }

    /**
     * PJEII-3755 - Cadastra o ProcessoTrfConexao e lanca movimentacao
     * @param entity ProcessoTrfConexao a ser persistido
     * @param codEvento Codigo do evento de movimentação a ser lançado
     * @return ProcessoTrfConexao persistido
     * @throws PJeBusinessException Se ocorrer alguma exceção
     */
    public ProcessoTrfConexao persist(ProcessoTrfConexao entity, String codEvento) throws PJeBusinessException {
        ProcessoTrfConexao ret = super.persist(entity);

        if (codEvento != null) {
            // lança um movimento de Apensamento
            ProcessoEvento movimentoProcesso = MovimentoAutomaticoService.preencherMovimento().
                    deCodigo(codEvento).comComplementoDeNome("numero_do_processo").preencherComTexto(ret.getProcessoTrfConexo().getNumeroProcesso()).
                    associarAoProcesso(entity.getProcessoTrf()).lancarMovimento();

            if (movimentoProcesso == null) {
                throw new AplicationException("Não foi possível lançar o movimento de apensamento!");
            }
        }

        return ret;
    }

    /**
     * PJEII-3755
     * @param entity ProcessoTrfConexao a ser removido
     * @throws PJeBusinessException Se ocorrer alguma exceção
     */
    @Override
    public void remove(ProcessoTrfConexao entity) throws PJeBusinessException {
        remove(entity, null);
    }

    /**
     * PJEII-3755
     * Método que remove o ProcessoTrfConexao passado como parâmetro. Além
     * disso, também remove a associação inversa existente entre o processo
     * PRINCIPAL e CONEXO
     *
     * @param entity ProcessoTrfConexao a ser removido
     * @param codEvento Código do evento de movimentação a ser lançado
     * @throws PJeBusinessException Se ocorrer alguma exceção
     */
    public void remove(ProcessoTrfConexao entity, String codEvento) throws PJeBusinessException {
        ProcessoTrf processoPrincipal = entity.getProcessoTrf();
        ProcessoTrf processoConexo = entity.getProcessoTrfConexo();

        // remove a associacao do Processo PRINCIPAL com o Processo CONEXO
        super.remove(entity);

        // depois remove a associacao inversa do Processo CONEXO com o Processo PRINCIPAL
        ProcessoTrfConexao ptc = getDAO().getProcessoTrfConexao(processoConexo, processoPrincipal);
        if (ptc != null) {
            super.remove(ptc);
        }

        if (codEvento != null) {
            // lança um movimento de Desapensamento para o processo PRINCIPAL
            ProcessoEvento movimentoProcesso = MovimentoAutomaticoService.preencherMovimento().
                    deCodigo(codEvento).comComplementoDeNome("numero_do_processo").preencherComTexto(processoConexo.getNumeroProcesso()).
                    associarAoProcesso(processoPrincipal).lancarMovimento();

            if (movimentoProcesso == null) {
                throw new AplicationException("Não foi possível lançar o movimento de desapensamento!");
            }

            if (ptc != null) {
                // lança um movimento de Desapensamento para o processo CONEXO
                movimentoProcesso = MovimentoAutomaticoService.preencherMovimento().
                        deCodigo(codEvento).comComplementoDeNome("numero_do_processo").preencherComTexto(processoPrincipal.getNumeroProcesso()).
                        associarAoProcesso(processoConexo).lancarMovimento();

                if (movimentoProcesso == null) {
                    throw new AplicationException("Não foi possível lançar o movimento de desapensamento!");
                }
            }
        }

        flush();
    }

    /**
     * PJEII-6051
     */
	public List<ProcessoTrfConexao> getProcessosPreventos(Integer idProcessoTrf) {
		return this.processoTrfConexaoDAO.getProcessosPreventos(idProcessoTrf);
	}

	public int getQuantidadeProcessosPreventos(Integer idProcessoTrf) {
		return this.processoTrfConexaoDAO.getQuantidadeProcessosPreventos(idProcessoTrf);
	}
	
    public List<ProcessoTrfConexao> getProcessosPreventosPendentesAnalise(Integer idProcessoTrf) {
        return this.processoTrfConexaoDAO.getProcessosPreventosPendentesAnalise(idProcessoTrf);
    }

    public int getQuantidadeProcessosPreventosPendentesAnalise(Integer idProcessoTrf) {
        return this.processoTrfConexaoDAO.getQuantidadeProcessosPreventosPendentesAnalise(idProcessoTrf);
    }	
    
	public void defineTipoPrevencao(Integer idProcessoTrfConexao, PrevencaoEnum prevencao, ProcessoDocumento processoDocumento) {
		ProcessoTrfConexao processoTrfConexao = this.processoTrfConexaoDAO.find(idProcessoTrfConexao);
		if (processoTrfConexao != null) {
			processoTrfConexao.setPrevencao(prevencao);
			processoTrfConexao.setDtValidaPrevencao(new Date());
			if(processoDocumento != null) {
				processoTrfConexao.setProcessoDocumento(processoDocumento);
				ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
				if(processoDocumentoBin != null) {
					processoTrfConexao.setDataRegistro(processoDocumentoBin.getDataAssinatura());
				}
			}
			this.processoTrfConexaoDAO.merge(processoTrfConexao);
		}		
	}
	
	/**
     * Retorna lista de processos associados (ativos e inativos)
     * 
     * @param Integer idProcessoTrf
     * @return List<ProcessoTrfConexao>
     */
	public List<ProcessoTrfConexao>  getListProcessosAssociados(Integer idProcessoTrf) {
		return this.getListProcessosAssociados(idProcessoTrf, Boolean.FALSE);
	}
	
	/**
	 * Retorna lista de processos associados validando o atributo "ativo".
	 * @param Integer idProcessoTrf
	 * @param Boolean somenteAtivos
	 * @return List<ProcessoTrfConexao>
	 */
	public List<ProcessoTrfConexao> getListProcessosAssociados(Integer idProcessoTrf, Boolean somenteAtivos) {
		return this.processoTrfConexaoDAO.getListProcessosAssociados(idProcessoTrf, somenteAtivos);
	}

	/**
	 * Retorna um processoTrfConexao que tenha o processoTrf e processoConexo informado no parâmetro
	 * @param processoPrincipal processo 
	 * @param processoConexo processo conexo
	 * @return
	 */
	public ProcessoTrfConexao findByProcessoEConexo(ProcessoTrf processoPrincipal, ProcessoTrf processoConexo) {
		return this.processoTrfConexaoDAO.getProcessoTrfConexao(processoPrincipal, processoConexo);
	}

	/** 
	 * reucpera todas as validacoes de prevencao realizadas pela pessoa passada em parametro
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<ProcessoTrfConexao> recuperaConexoesPrevencoes(Pessoa _pessoa) {
		return processoTrfConexaoDAO.recuperaConexoesPrevencoes(_pessoa);
	}
	
	public void removeDocumento(int idDocumento) {
		processoTrfConexaoDAO.removeDocumento(idDocumento);
	}
	
	public int getQuantidadeProcessosAssociados(Integer idProcessoTrf) {
		return this.processoTrfConexaoDAO.getQuantidadeProcessosAssociados(idProcessoTrf);
}
}
