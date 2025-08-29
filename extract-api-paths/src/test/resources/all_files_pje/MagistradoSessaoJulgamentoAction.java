package br.com.jt.pje.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.manager.AnotacaoManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.infox.utils.ItensLegendas;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;


@Name(MagistradoSessaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class MagistradoSessaoJulgamentoAction
    extends AbstractSessaoJulgamentoAction {
    public static final String NAME = "magistradoSessaoJulgamentoAction";
    private static final long serialVersionUID = 1L;
    private PautaSessao pautaProcessoApregoado;
    private boolean controleAberturaToogle = false;
    @In
    private AssinaturaDocumentoService assinaturaDocumentoService;
    @In
    private ModalComposicaoProcessoAction modalComposicaoProcessoAction;
	@In
	private ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
	@In
	private AnotacaoManager anotacaoManager;

    @Override
    public void iniciarLegenda() {
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[0], false);	// Divergência
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[1], false);	// Destaque
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[3], false);	// Julgados
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[4], false);	// Deliberação em Sessão
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[5], false);	// Retirados de Pauta
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[6], false);	// Pendentes
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[7], false);	// Sustentação oral
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[8], false);	// Preferência
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[9], false);	// Meus Acórdãos Assinados
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[10], false);	// Meus Acórdãos Não Assinados
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[11], false);	// Voto não elaborado
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[12], false);	// Voto elaborado e não liberado
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[13], false);	// Voto elaborado e liberado
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[14], false);	// Voto do relator não liberado
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[15], false);	// Julgados sem pendência de lançamentos
        getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[16], false);	// Julgados com pendência de lançamentos
		
		/**
		 * (fernando.junior - 21/01/2013) Adição de novas legendas
		 */
		getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[17], false);	// Divergência com análise pendente
		getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[18], false);	// Divergência não concluída/liberada
		getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[19], false);	// Destaque não concluído/liberado
		getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[20], false);	// Anotação
		getMapLegenda().put(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[21], false);	// Anotação não concluída
    }

    /* [PJEII-2783] Correção para o bug que assinava a versão anterior do acórdão quando alterado.
     * Action do botão Gravar em tabsDocumentos.xhtml
     * Esse método/action foi criado para substituir a EL que estava diretamente no action do botão. Além de retirar a lógica da apresentação, foi necessário para acrescentar o método para composição da íntegra do acórdão, pois o action não suporta a execução de mais de uma expressão.
     * */
    public void gravar() {
        if (super.getVoto().getIdVoto() == 0) {
            persist();
        } else {
            update();
        }

        //Força a construção da íntegra do voto depois de gravar para evitar assinar voto/acordão anterior no caso de alterações
        construirModeloIntegra();
    }

    public void update() {
        super.update();

        ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class,
                getDocumentoVotoDispDecisorio().getIdProcessoDocumento());

        if (assinaturaDocumentoService.isProcessoDocumentoAssinado(pd)) {
            assinaturaDocumentoService.removeAllSignature(pd);
        }
        FacesMessages.instance()
                     .add(Severity.INFO, "Registro alterado com sucesso.");
    }

    @Override
    protected char getSiglaPainel() {
        return 'M';
    }

    public void pesquisarProcessos(String sigla) {
        if (!Strings.isEmpty(sigla)) {
            if (sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[0]) || 
            		sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[17]) ||
            		sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[18])) {
            	filtroDivergencia(sigla);
            } else if (sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[1]) || 
            		sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[19])) {
            	filtroDestaque(sigla);
            } else if (sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[20]) || 
            		sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[21])) {
            	filtroAnotacao(sigla);
            } else if (sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[11]) ||
                    sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[12]) ||
                    sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[13]) ||
                    sigla.equals(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[14])) {
                filtroVoto(sigla);
            }

            super.pesquisarProcessos(sigla);
        }
    }
    
    public void pesquisarProcessosSessao() {
    	super.pesquisarProcessos(null);
    }

    public int getIdSessaoByIdPautaSessao(int id) {
        PautaSessao ps = EntityUtil.find(PautaSessao.class, id);

        try {
            return ps.getSessao().getIdSessao();
        } catch (Exception e) {
            return 0;
        }
    }

    public PautaSessao getPautaSessao(int id) {
        PautaSessao ps = EntityUtil.find(PautaSessao.class, id);
        return ps;
    }

    public void addAllPodeAssinarAcordao() {
        for (PautaSessao pautaSessao : getElaboracaoVotoList().getResultList()) {
            if (!getListPautaSessao().contains(pautaSessao)) {
                if (podeAssinarAcordaoMagistrado(pautaSessao)) {
                    getListPautaSessao().add(pautaSessao);
					pautaSessao.setCheckBoxSelecionado(true);
                }
            }
        }

        if (getListPautaSessao().size() == 0) {
            FacesMessages.instance()
                         .add(Severity.INFO,
                "Não existem documentos a serem selecionados no momento.");
        }
    }

    /**
     *  Funcionalidade utilizada pelo magistrado da sessao na tela botoesAddRemoveAllMagistrado.xhtml.
     *
     */
    public void addRemoveAllPautaSessaoMagistrado() {
        super.setListPautaSessao(new ArrayList<PautaSessao>());
        this.setProcessoTrf(null);

        if (this.isCheckBoxSelecionarTodos()) {
            addAllPautaSessaoMagistrado();
        } else {
            removeAllPautaSessao();
        }
    }

    public void addAllPautaSessaoMagistrado() {
        for (PautaSessao pautaSessao : super.getListaPautaSessaoGrid()) {
            if (podeAssinarAcordaoMagistrado(pautaSessao) && ! pautaSessao.isCheckBoxDisabled() ) {
                pautaSessao.setCheckBoxSelecionado(true);
                getListPautaSessao().add(pautaSessao);
            }
        }

        if (getListPautaSessao().size() == 0) {
            FacesMessages.instance()
                         .add(Severity.INFO,
                "Não existem documentos a serem selecionados no momento.");
        }
    }

    @Factory(value = "countProcessosPodeAssinarMagistrado", scope = ScopeType.EVENT)
    public Integer countProcessosPodeAssinarMagistrado() {
        Integer countProcessosPodeAssinar = 0;

        for (PautaSessao pautaSessao : getElaboracaoVotoList().getResultList()) {
            if (podeAssinarAcordaoMagistrado(pautaSessao)) {
                countProcessosPodeAssinar++;
            }
        }

        return countProcessosPodeAssinar;
    }

    @Override
    public void assinar() {
        update();
        FacesMessages.instance().clear();
        assinarProcessoDocumento();
        // PJEII-4718
        // Após assinar movimenta para Default Transition
        movimentarAposAssinatura(getProcessoTrf());
        FacesMessages.instance()
                     .add(Severity.INFO,
            "Acórdão criado e assinado com sucesso.");
    }

    @Override
    public void assinarEmLote() {
        super.assinarEmLote();
        this.setListPautaSessao(new ArrayList<PautaSessao>());
        FacesMessages.instance()
                     .add(Severity.INFO,
            "Acórdão(s) criado(s) e assinado(s) com sucesso.");
    }

    public String[][] getItemsLegenda() {
        if (sessaoEncerrada() || sessaoFechada()) {
            return ItensLegendas.LEGENDAS_MAGISTRADO_SESSAO_ENCERRADA_ARRAY;
        }

        return ItensLegendas.LEGENDAS_MAGISTRADO_ARRAY;
    }

    public boolean verificaAlteracaoProcessoApregoado() {
        PautaSessao processoApregoado = pautaSessaoManager.getPautaProcessoApregoadoBySessao(getSessao());

        if (((getProcessoTrf() != null) && (processoApregoado == null)) ||
                ((getProcessoTrf() == null) && (processoApregoado != null))) {
            pautaProcessoApregoado = processoApregoado;

            return true;
        }

        if ((processoApregoado != null) &&
                !getProcessoTrf().equals(processoApregoado.getProcessoTrf())) {
            pautaProcessoApregoado = processoApregoado;

            return true;
        } else {
            return false;
        }
    }

    public void atualizarProcessoApregoado() {
    	// Atualizar a grid de processos
    	EntityUtil.getEntityManager().clear();
    	super.carregarListaPautaSessaoGrid();
    	
    	if(!verificaAlteracaoProcessoApregoado()) {
    		return;
    	}
    	
    	if (pautaProcessoApregoado != null) {
            setIdProcesso(pautaProcessoApregoado.getProcessoTrf()
                                                .getIdProcessoTrf());
            inicializar();
            carregarQuantidadeVotos();
        } else {
            setProcessoTrf(null);
            removeAll();
            carregarResultados();
        }
    }

    @Override
    protected void persistVoto() {
        getVoto().setSessao(getSessao());
        super.persistVoto();
    }

    public void abreFechaToogle() {
        if (this.controleAberturaToogle) {
            this.controleAberturaToogle = false;
        } else {
            this.controleAberturaToogle = true;
        }
    }

    public void carregaModalComposicaoProcesso() {
        modalComposicaoProcessoAction.carregarComposicao()
                                     .doProcesso(getProcessoTrf())
                                     .daPauta(getPautaSessao())
                                     .naSessao(getSessao()).executar();
    }

    /*
     * inicio get e set
     */
    public boolean getControleAberturaToogle() {
        return controleAberturaToogle;
    }

    public void setControleAberturaToogle(boolean controleAberturaToogle) {
        this.controleAberturaToogle = controleAberturaToogle;
    }

    @SuppressWarnings("unchecked")
    public List<ProcessoDocumentoBinPessoaAssinatura> getListProcessoDocumentoBinPessoaAssinatura() {
        List<ProcessoDocumentoBinPessoaAssinatura> lista = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();

        if (getDocumentoVotoDispDecisorio() != null) {
            ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class,
                    getDocumentoVotoDispDecisorio().getIdProcessoDocumento());

            if (pd != null) {
                lista.addAll(processoDocumentoBinPessoaAssinaturaManager.getAssinaturaDocumento(
                        pd.getProcessoDocumentoBin()));
            } else {
                return Collections.EMPTY_LIST;
            }

            return lista;
        }

        return lista;
    }
    
    public boolean isProcessoNaTarefa(ProcessoTrf processoTrf, String tarefa){
    	Integer idTarefa = Integer.valueOf(ParametroUtil.getParametro(tarefa));
    	ProcessoJudicialService pjs =  br.com.itx.util.ComponentUtil.getComponent("processoJudicialService");
    	Long count =  pjs.existeIdTarefaNoProcesso(processoTrf, idTarefa);
    	return count.longValue() > 0L ? true:false; 
    }
    
    public String msgProcessoForaAssinarAcordao(ProcessoTrf processoTrf){
    	if(processoTrf != null && !Authenticator.isPapelPermissaoSecretarioSessao()){

	    	 if( ! this.isProcessoNaTarefa(processoTrf,"idTarefaAssinarAcordao")  ){
	    		 return "Existem Pendências: Processo não está na tarefa 'Assinar Acórdão'";
	    	 }
	    	 return "Tarefa Atual: 'Assinar Acórdão'";
    	}
    	return "";
    }
    
    public String msgProcessoForaAssinarAcordao(PautaSessao ps, Voto voto){
    	Integer quantidade = countAcordaoAssinado(ps,voto);
    	String retorno = "";
    	if(! Authenticator.isPapelPermissaoSecretarioSessao() ){
    		if( !(quantidade != null && quantidade > 0) ){
    			if( ! this.isProcessoNaTarefa(ps.getProcessoTrf(),"idTarefaAssinarAcordao")  ){
    				retorno = "Existem Pendências: Processo não está na tarefa 'Assinar Acórdão'";
    			} else {
    				retorno =  "Tarefa Atual: 'Assinar Acórdão'";
    			}
    		}
    	}
    	return retorno;
    }
    

/**
	 * Verifica a existência de divergências para o documento, retornando um nº indicando o tipo de divergência encontrada (se houver):
	 * 0 - O documento não possui divergências
	 * 1 - Divergência pendente
	 * 2 - Divergência não concluída/liberada
	 * 3 - Divergência
	 * 
	 * @author fernando.junior (17/01/2013) 
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeDivergencia(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if ( documento != null && documento.getIdProcessoDocumentoEstruturado() != null ) {
			return anotacaoManager.temDivergencias(documento);
		}
		
		return 0;
	}
	
	/**
	 * Verifica a existência de destaques para o documento, retornando um nº indicando o tipo de destaque encontrado (se houver):
	 * 0 - O documento não possui destaques
	 * 1 - Destaque não concluído/liberado
	 * 2 - Destaque
	 * 
	 * @author fernando.junior (17/01/2013)
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeDestaque(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if (documento != null && documento.getIdProcessoDocumentoEstruturado() != null) {
			return anotacaoManager.temDestaques(documento);
		}
		
		return 0;
	}
	
	/**
	 * Verifica a existência de anotação para o documento, retornando um nº indicando o tipo de anotação:
	 * 0 - O documento não possui anotações
	 * 1 - Anotação não concluída
	 * 2 - Anotação
	 * 
	 * @author fernando.junior (17/01/2013)
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeAnotacao(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if (documento != null && documento.getIdProcessoDocumentoEstruturado() != null) {
			return anotacaoManager.temAnotacoes(documento);
		}
		
		return 0;
	}
    
}

