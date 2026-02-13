package br.com.jt.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.service.PautaJulgamentoService;
import br.com.jt.pje.manager.ComposicaoProcessoSessaoManager;
import br.com.jt.pje.manager.ComposicaoSessaoManager;
import br.com.jt.pje.manager.VotoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


public abstract class AbstractPautaAction implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4962407021605159106L;
    private SessaoJT sessao;
    private List<ProcessoTrf> listProcesso = new ArrayList<ProcessoTrf>();
    private List<String> listOJSemTitular = new ArrayList<String>();
    @In
    private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
    @In
    protected VotoManager votoManager;
    @In
    protected ComposicaoSessaoManager composicaoSessaoManager;
    @In
    protected ComposicaoProcessoSessaoManager composicaoProcessoSessaoManager;
    @In
    private PautaJulgamentoService pautaJulgamentoService;
    
	@Logger
	private Log log;

    public void inserirPautaJulgamento()  {
        boolean erro = false;
        for (ProcessoTrf processoTrf : getListProcesso()) {
        	
        	try{
				
        		erro = pautaJulgamentoService.incluirPauta( processoTrf, sessao, getTipoInclusaoEnum());
				
			} catch (Exception e){

				e.printStackTrace();
				if(processoTrf != null)
					log.error("[INSERIR PAUTA] Erro ao inserir processo " + processoTrf.getNumeroProcesso() + " na pauta", e);
				else {
					log.error("[INSERIR PAUTA] Erro ao inserir processo na pauta", e);
				}
				
			}
          
        }
        
        setListProcesso(new ArrayList<ProcessoTrf>());

        if(!erro){
	        FacesMessages.instance()
	                     .add(Severity.INFO, "Registro(s) inserido(s) com sucesso.");
        }
    }

	

    protected abstract TipoInclusaoEnum getTipoInclusaoEnum();

    protected abstract List<ProcessoTrf> getProcessoList();

    public void addProcesso(ProcessoTrf row) {
        if (getListProcesso().contains(row)) {
            getListProcesso().remove(row);

            if (getListProcesso().isEmpty()) {
                getListOJSemTitular().clear();
            }
        } else {
            getListProcesso().add(row);

            addOJSemTitular();
        }
    }

    private void addOJSemTitular() {
        if (getListOJSemTitular().isEmpty()) {
            for (ComposicaoSessao cs : composicaoSessaoManager.composicaoSessaoListBySessao(
                    getSessao())) {
                if (cs.getMagistradoPresente() == null) {
                    getListOJSemTitular()
                        .add(cs.getOrgaoJulgador().getOrgaoJulgador());
                }
            }
        }
    }

    public void addAllProcess() {
        if (getListProcesso().size() == 0) {
            getListProcesso().addAll(getProcessoList());
        } else {
            for (ProcessoTrf processoTrf : getProcessoList()) {
                if (!getListProcesso().contains(processoTrf)) {
                    getListProcesso().add(processoTrf);
                }
            }
        }

        addOJSemTitular();
    }

    public void removeAllProcess() {
        setListProcesso(new ArrayList<ProcessoTrf>(0));
        setListOJSemTitular(new ArrayList<String>());
    }

    public Voto votoMagistrado(ProcessoTrf processoTrf) {
        return votoManager.getVotoProcessoSemSessaoByOrgaoJulgador(processoTrf,
            Authenticator.getOrgaoJulgadorAtual());
    }

    public Voto votoOJSessao(ProcessoTrf processoTrf)
        throws InstantiationException, IllegalAccessException {
        if (Authenticator.getOrgaoJulgadorAtual() != null) {
            return votoManager.getUltimoVotoByOrgaoJulgadorProcessoSessao(Authenticator.getOrgaoJulgadorAtual(),
                processoTrf, sessao);
        }

        return votoManager.getUltimoVotoByOrgaoJulgadorProcessoSessao(processoTrf.getOrgaoJulgador(),
            processoTrf, sessao);
    }

    public void newInstance() {
        setSessao(null);
        setListProcesso(new ArrayList<ProcessoTrf>());
        setListOJSemTitular(new ArrayList<String>());
    }

    public String getRelator(OrgaoJulgador row) {
        return usuarioLocalizacaoMagistradoServidorManager.getRelator(row);
    }

    public String getPolos(ProcessoTrf processoTrf) {
        return getPoloAtivo(processoTrf) + " X " + getPoloPassivo(processoTrf);
    }

    public String getPoloAtivo(ProcessoTrf processoTrf) {
        return getParte(processoTrf.getListaParteAtivo());
    }

    public String getPoloPassivo(ProcessoTrf processoTrf) {
        return getParte(processoTrf.getListaPartePassivo());
    }

    public boolean sessaoEncerradaOuFechada() {
        return getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.E) ||
        getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.F);
    }

    private String getParte(List<ProcessoParte> partes) {
        String documento = "";

        if (partes.size() == 1) {
            documento = partes.get(0).getPessoa().getDocumentoCpfCnpj();
            documento = Strings.isEmpty(documento) ? "" : (" - " + documento);

            return partes.get(0).getNomeParte() + documento;
        }

        List<ProcessoParte> listParte = new ArrayList<ProcessoParte>();

        for (ProcessoParte parte : partes) {
            if (!parte.getTipoParte().getTipoParte()
                          .matches(("ADVOGADO|PROCURADOR"))) {
                listParte.add(parte);
            }
        }

        String nome = "";
        String sufixo = "";

        if (!listParte.isEmpty()) {
            nome = listParte.get(0).getNomeParte();
            documento = listParte.get(0).getPessoa().getDocumentoCpfCnpj();

            int tam = listParte.size();

            if (tam == 2) {
                sufixo = " e outro";
            } else if (tam > 2) {
                sufixo = " e outros";
            }
        }

        nome = Strings.isEmpty(nome) ? "" : nome;
        documento = Strings.isEmpty(documento) ? "" : (" - " + documento);

        return nome + sufixo + documento;
    }

    /*
     * inicio dos gets e sets
    */
    public void setSessao(SessaoJT sessao) {
        this.sessao = sessao;
    }

    public SessaoJT getSessao() {
        return sessao;
    }

    public void setListProcesso(List<ProcessoTrf> listProcesso) {
        this.listProcesso = listProcesso;
    }

    public List<ProcessoTrf> getListProcesso() {
        return listProcesso;
    }

    public List<String> getListOJSemTitular() {
        return listOJSemTitular;
    }

    public void setListOJSemTitular(List<String> listOJSemTitular) {
        this.listOJSemTitular = listOJSemTitular;
    }
    
    public String getDetalhesProcesso(ProcessoTrf processoTrf) {
		StringBuilder detalhesProcesso = new StringBuilder();
		
		if (processoTrf.getOrgaoJulgadorColegiado() != null) {
			detalhesProcesso.append("<span style='font-weight: bold;'>");
			detalhesProcesso.append(processoTrf.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado());
			
			/* [PJEII-7079] Erro no nome do relator ao inserir o processo em pauta.
			 *  Antes utilizava a lógica de encontrar o primeiro magistrado entre os cargos que recebem distribuição do OJ. 
			 *  O problema da abordagem anterior é que pode existir mais de um magistrado vinculado ao cargo que recebe distribuição e nesse caso era retornado o primeiro magistrado que nem sempre correspodia ao relator do processo.
			 *  No novo comportamento implementado está utilizando a informação do relator presente em ProcessoTrf.
			 *  Caso não tenha nenhum relator apresenta a mensagem "(Sem relator)". Essa situação é improvável, pois ao liberar para pauta o processo já deve ter relator, mas de qualquer forma fica como prática defensiva para alertar eventual situação inconsistente.   
			 */
			detalhesProcesso.append("/");
			if (processoTrf.getPessoaRelator() != null) {
				detalhesProcesso.append(processoTrf.getPessoaRelator().getNome());
			}else{
				detalhesProcesso.append("(Sem relator)");
			}
		}
		
		
		detalhesProcesso.append("</span>");
		
		detalhesProcesso.append("<br />");
		
		detalhesProcesso.append(processoTrf.getClasseJudicial().getClasseJudicialSigla());
		detalhesProcesso.append("&nbsp;");
		detalhesProcesso.append(processoTrf.getNumeroProcesso());
		detalhesProcesso.append("<br /><span style='font-size: 0.9em;'>");
		
		detalhesProcesso.append(getPolos(processoTrf));
		
		detalhesProcesso.append("</span>");
		
		return detalhesProcesso.toString();
	}
}
