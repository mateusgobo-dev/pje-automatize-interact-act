package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.VotoAcompanhadoBean;
import br.com.infox.pje.list.VotoAcompanhadoList;
import br.com.infox.pje.service.SessaoJulgamentoService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.TipoVotoEnum;


@Name("abaVotarAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaVotarAction extends AbstractInteiroTeorProcesso
    implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -9129209113706236598L;
    private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_VOTO = ParametroUtil.instance()
                                                                                           .getTipoProcessoDocumentoVoto();
    private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto;
    private ProcessoDocumento processoDocumentoVoto;
    private Long acompanhaRelator;
    private Long acompanhaEmParte;
    private Long divergeDoRelator;
    private Long naoConhece;
    private Boolean visualizarProcesso;
    private VotoAcompanhadoList votoAcompanhadoList = new VotoAcompanhadoList();
    private List<VotoAcompanhadoBean> votoAcompanhadoBeanList;
    private String modeloDocumentoVoto;

    @In
    private SessaoJulgamentoService sessaoJulgamentoService;
    
	private SessaoComposicaoOrdem sessaoComposicaoOrdem;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private ParametroService parametroService;

    public ProcessoDocumento getProcessoDocumentoVoto() {
        return processoDocumentoVoto;
    }

    public void setProcessoDocumentoVoto(
        ProcessoDocumento processoDocumentoVoto) {
        this.processoDocumentoVoto = processoDocumentoVoto;
    }

    public List<ModeloDocumento> getModeloDocumentoList() {
        return getModeloDocumentoList(ParametroUtil.instance()
                                                   .getTipoProcessoDocumentoVoto());
    }

    public void setSessaoProcessoDocumentoVoto(
        SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
        this.sessaoProcessoDocumentoVoto = sessaoProcessoDocumentoVoto;
    }

    public Long getAcompanhaRelator() {
        if (this.acompanhaRelator == null) {
            acompanhaRelator = getQtdeVotoSessao(TipoVotoEnum.C);
        }

        return acompanhaRelator;
    }

    public void setAcompanhaRelator(Long acompanhaRelator) {
        this.acompanhaRelator = acompanhaRelator;
    }

    public Long getAcompanhaEmParte() {
        if (this.acompanhaEmParte == null) {
            acompanhaEmParte = getQtdeVotoSessao(TipoVotoEnum.P);
        }

        return acompanhaEmParte;
    }

    public Long getNaoConhece() {
        if (this.naoConhece == null) {
            naoConhece = getQtdeVotoSessao(TipoVotoEnum.N);
        }

        return naoConhece;
    }

    public void setNaoConhece(Long naoConhece) {
        this.naoConhece = naoConhece;
    }

    public Long getAcompanhaRelatorProcesso(ProcessoTrf processo) {
        if (this.acompanhaRelator == null) {
            acompanhaRelator = getQtdeVotoSessaoProcesso(TipoVotoEnum.C, processo);
        }

        return acompanhaRelator;
    }

    public Long getAcompanhaEmParteProcesso(ProcessoTrf processo) {
        if (this.acompanhaEmParte == null) {
            acompanhaEmParte = getQtdeVotoSessaoProcesso(TipoVotoEnum.P, processo);
        }

        return acompanhaEmParte;
    }

    public Long getDivergeDoRelatorProcesso(ProcessoTrf processo) {
        if (this.divergeDoRelator == null) {
            divergeDoRelator = getQtdeVotoSessaoProcesso(TipoVotoEnum.D, processo);
        }

        return divergeDoRelator;
    }

    public Long getNaoConheceProcesso(ProcessoTrf processo) {
        if (this.naoConhece == null) {
            naoConhece = getQtdeVotoSessaoProcesso(TipoVotoEnum.N, processo);
        }

        return naoConhece;
    }

    private Long getQtdeVotoSessao(TipoVotoEnum tipoVoto) {
        return sessaoJulgamentoService.qtdeVotoSessao(this.getSessao(),
            tipoVoto, getProcessoTrf().getOrgaoJulgador());
    }

    private Long getQtdeVotoSessaoProcesso(TipoVotoEnum tipoVoto, ProcessoTrf processo) {
        return sessaoJulgamentoService.qtdeVotoSessaoProcesso(this.getSessao(), tipoVoto, getProcessoTrf().getOrgaoJulgador(), processo);
    }

    public void setAcompanhaEmParte(Long acompanhaEmParte) {
        this.acompanhaEmParte = acompanhaEmParte;
    }

    public Long getDivergeDoRelator() {
        if (this.divergeDoRelator == null) {
            divergeDoRelator = getQtdeVotoSessao(TipoVotoEnum.D);
        }

        return divergeDoRelator;
    }

    public void setDivergeDoRelator(Long divergeDoRelator) {
        this.divergeDoRelator = divergeDoRelator;
    }

    /**
     * [PJEII-5874] Adição desse método para inicializar os valores do Placar
     */
    @Override
    public void newInstance() {
        super.newInstance();
        zerarPlacar();
    }
    
    @Override
    public boolean podeEditarComponente() {
        boolean ret = true;
        boolean existeDocumentoAssinado = (this.sessaoProcessoDocumentoVoto != null) &&
            (this.sessaoProcessoDocumentoVoto.getProcessoDocumento() != null) &&
            (this.sessaoProcessoDocumentoVoto.getIdSessaoProcessoDocumento() != 0) &&
            ProcessoDocumentoHome.instance()
                                 .isAssinado(this.sessaoProcessoDocumentoVoto.getProcessoDocumento());
        boolean sessaoAberta = (getSessao() != null) &&
            (getSessao().getDataRealizacaoSessao() == null) &&
            (getSessao().getDataFechamentoSessao() == null);

        if (((getProcessoTrf() != null) &&
                Authenticator.getOrgaoJulgadorAtual()
                                 .equals(getProcessoTrf().getOrgaoJulgador())) ||
                existeDocumentoAssinado || !sessaoAberta) {
            ret = false;
        }

        return ret;
    }

    @Override
    public boolean podeEditarConteudo() {
        boolean ret = true;
        boolean existeDocumentoAssinado = (this.sessaoProcessoDocumentoVoto != null) &&
            (this.sessaoProcessoDocumentoVoto.getIdSessaoProcessoDocumento() != 0) &&
            ProcessoDocumentoHome.instance()
                                 .isAssinado(this.sessaoProcessoDocumentoVoto.getProcessoDocumento());
        boolean sessaoAberta = getSessao().getDataFechamentoSessao() == null;

        if (Authenticator.getOrgaoJulgadorAtual()
                             .equals(getProcessoTrf().getOrgaoJulgador()) ||
                existeDocumentoAssinado || !sessaoAberta) {
            ret = false;
        }

        return ret;
    }

    @Override
    public void update() {
        if ((modeloDocumentoVoto == null) &&
                (sessaoProcessoDocumentoVoto.getProcessoDocumento() != null)) {
            FacesMessages.instance()
                         .add(Severity.ERROR,
                "Documento não atualizado. Favor preencher o voto.");

            return;
        }

        if (this.getVotoAcompanhadoBeanList() == null) {
            votoAcompanhadoBeanList = Collections.emptyList();
        }

        // Altera o órgão que acompanha de acordo com a escolha
        sessaoJulgamentoService.setOjAcompanhado(this.getProcessoTrf(),
            this.getSessaoProcessoDocumentoVoto(),
            this.getVotoAcompanhadoBeanList(),
            Authenticator.getOrgaoJulgadorAtual());

        if ((modeloDocumentoVoto != null) &&
                (sessaoProcessoDocumentoVoto.getProcessoDocumento() != null)) {
            sessaoProcessoDocumentoVoto.getProcessoDocumento()
                                       .getProcessoDocumentoBin()
                                       .setModeloDocumento(modeloDocumentoVoto);

            ProcessoDocumento pd = getSessaoProcessoDocumentoVoto()
                                       .getProcessoDocumento();
            pd.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
            pd.setNomeUsuarioAlteracao(Authenticator.getUsuarioLogado().getNome());
            pd.setDataAlteracao(new Date());
            pd.setPapel(Authenticator.getPapelAtual());
            pd.setLocalizacao(Authenticator.getLocalizacaoAtual());
        }

        sessaoProcessoDocumentoVotoManager.update(this.getSessaoProcessoDocumentoVoto());
        
        EntityUtil.flush();

        FacesMessages.instance()
                     .add(Severity.INFO, "Documento atualizado com sucesso!");

        zerarPlacar();
    }

    @Override
    public void assinarESalvar() {
        if ((modeloDocumentoVoto == null) || modeloDocumentoVoto.isEmpty()) {
            FacesMessages.instance()
                         .add(Severity.ERROR,
                "Para assinatura é necessário que tenha algum conteúdo no editor");

            return;
        }
        if(sessaoProcessoDocumentoVoto.getProcessoDocumento() == null){
        	ProcessoDocumento pd = documentoJudicialService.getDocumento();
        	sessaoProcessoDocumentoVoto.setProcessoDocumento(pd);
        }
        sessaoProcessoDocumentoVoto.getProcessoDocumento()
                                   .getProcessoDocumentoBin()
                                   .setModeloDocumento(modeloDocumentoVoto);
        super.assinarESalvar();
        zerarPlacar();
    }

    private void zerarPlacar() {
        acompanhaRelator = null;
        acompanhaEmParte = null;
        divergeDoRelator = null;
        naoConhece = null;
    }

    public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
        if (sessaoProcessoDocumentoVoto == null) {
            sessaoProcessoDocumentoVoto = getSessaoProcessoDocumentoVotoByTipoOj(Authenticator.getOrgaoJulgadorAtual());

            // Se não existe nenhum processodocumento na sessão persistido
            // cria-se um novo
            if ((sessaoProcessoDocumentoVoto != null) &&
                    sessaoProcessoDocumentoManager.documentoInclusoAposProcessoJulgado(
                        sessaoProcessoDocumentoVoto.getDtVoto(),
                        getProcessoTrf().getProcesso())) {
                if (sessaoProcessoDocumentoVoto.getProcessoDocumento() != null) {
                    setModeloDocumentoVoto(sessaoProcessoDocumentoVoto.getProcessoDocumento()
                                                                      .getProcessoDocumentoBin()
                                                                      .getModeloDocumento());
                }

                return sessaoProcessoDocumentoVoto;
            } else {
                criaNovoVoto();
            }
        }

        return sessaoProcessoDocumentoVoto;
    }

    private void criaNovoVoto() {
        sessaoProcessoDocumentoVoto = new SessaoProcessoDocumentoVoto();

        ProcessoDocumento processoDocumento = new ProcessoDocumento();
        processoDocumento.setProcessoDocumentoBin(new ProcessoDocumentoBin());
        sessaoProcessoDocumentoVoto.setProcessoDocumento(processoDocumento);
        sessaoProcessoDocumentoVoto.getProcessoDocumento()
                                   .setTipoProcessoDocumento(TIPO_PROCESSO_DOCUMENTO_VOTO);
        sessaoProcessoDocumentoVoto.getProcessoDocumento()
                                   .setProcessoDocumento("voto");         
    }

    /**
     * Ao clicar em uma lupa, altera o voto sendo exibido na tela
     *
     * @param sessaoProcessoDocumentoVoto
     *            voto sendo exibido atualmente
     */
    public void alteraProcessoExibido(
        SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
        this.setSessaoProcessoDocumentoVoto(sessaoProcessoDocumentoVoto);

        // TODO: Verificar porque foi necessário alterar diretamente
        // ProcessoDocumentoBin e não simplesmente alterar o objeto
        // SessaoProcessoDocumentoVoto
        this.getSessaoProcessoDocumentoVoto().getProcessoDocumento()
            .setProcessoDocumentoBin(sessaoProcessoDocumentoVoto.getProcessoDocumento()
                                                                .getProcessoDocumentoBin());

        // O usuário poderá visualizar o processo, caso relator, apenas após
        // clicar na lupa
        this.setVisualizarProcesso(Boolean.TRUE);
    }

    public List<TipoVoto> getTipoVotoList() {
        if (this.tipoVotoList == null) {
            this.tipoVotoList = sessaoJulgamentoService.listTipoVotoAtivoSemRelator();
        }

        return tipoVotoList;
    }

    public void setVisualizarProcesso(Boolean visualizarProcesso) {
        this.visualizarProcesso = visualizarProcesso;
    }

    public Boolean getVisualizarProcesso() {
        if (this.visualizarProcesso == null) {
            if (sessaoProcessoDocumentoManager.isRelator(
                        this.getProcessoTrf(),
                        Authenticator.getOrgaoJulgadorAtual())) {
                this.visualizarProcesso = Boolean.FALSE;
            } else {
                this.visualizarProcesso = Boolean.TRUE;
            }
        }

        return visualizarProcesso;
    }

    public VotoAcompanhadoList getVotoAcompanhadoList() {
        return votoAcompanhadoList;
    }

    public void setVotoAcompanhadoList(VotoAcompanhadoList votoAcompanhadoList) {
        this.votoAcompanhadoList = votoAcompanhadoList;
    }

    public List<VotoAcompanhadoBean> getVotoAcompanhadoBeanList() {
        return votoAcompanhadoBeanList;
    }

    public List<VotoAcompanhadoBean> votoAcompanhadoBeanList(int maxResults) {
    	votoAcompanhadoBeanList = new ArrayList<VotoAcompanhadoBean>();
    	List<SessaoProcessoDocumentoVoto> listVotoDivergente = votoAcompanhadoList.list(maxResults); 
            
        for (SessaoProcessoDocumentoVoto spdv : listVotoDivergente) {
        	VotoAcompanhadoBean vab = new VotoAcompanhadoBean(spdv, false);
        	if(spdv.getOrgaoJulgador().equals(sessaoProcessoDocumentoVoto.getOjAcompanhado())){
        		vab.setCheck(true);
        	}
        	else{
        		vab.setCheck(false);
        	}
        	votoAcompanhadoBeanList.add(vab);
        }
        return votoAcompanhadoBeanList;
    }

    public void setVotoAcompanhadoBeanList(
        List<VotoAcompanhadoBean> votoAcompanhadoBeanList) {
        this.votoAcompanhadoBeanList = votoAcompanhadoBeanList;
    }

    @Override
    public SessaoProcessoDocumento persist() {
        if ((modeloDocumentoVoto != null) &&
                (sessaoProcessoDocumentoVoto.getProcessoDocumento() == null)) {
            criarDocumentoPreenchido();
        }

        sessaoProcessoDocumentoVoto.setProcessoTrf(this.getProcessoTrf());

        if (this.getVotoAcompanhadoBeanList() == null) {
            votoAcompanhadoBeanList = Collections.emptyList();
        }
        

        sessaoJulgamentoService.setOjAcompanhado(this.getProcessoTrf(),
            this.getSessaoProcessoDocumentoVoto(),
            this.getVotoAcompanhadoBeanList(),
            Authenticator.getOrgaoJulgadorAtual());

        return super.persist();
    }

    private void criarDocumentoPreenchido() {
        ProcessoDocumento pd = new ProcessoDocumento();
        pd.setTipoProcessoDocumento(TIPO_PROCESSO_DOCUMENTO_VOTO);
        pd.setProcessoDocumento("voto");

        ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
        pdb.setModeloDocumento(modeloDocumentoVoto);
        pd.setProcessoDocumentoBin(pdb);
        sessaoProcessoDocumentoVoto.setProcessoDocumento(pd);
    }
	
	/**
	 * Seta o id do processo TRF na ProcessoTrfHome para permitir a utilização de expressões regulares com ProcessoTrfHome
	 * Processa o modelo documento e o grava na variável modeloDocumentoVoto.
	 */
	@Override
	public void onSelectModeloDocumento() {
		ProcessoTrfHome.instance().setId(getProcessoTrf().getIdProcessoTrf());	
		super.onSelectModeloDocumento();
		modeloDocumentoVoto = getSessaoProcessoDocumento().getProcessoDocumento()
				.getProcessoDocumentoBin().getModeloDocumento();
	}
    
    @Override
    public SessaoProcessoDocumento getSessaoProcessoDocumento() {
        return getSessaoProcessoDocumentoVoto();
    }

    @Override
    public void setSessaoProcessoDocumento(
        SessaoProcessoDocumento sessaoProcessoDocumento) {
        this.sessaoProcessoDocumentoVoto = (SessaoProcessoDocumentoVoto) sessaoProcessoDocumento;
    }

    @Override
    public TipoVotoManager getTipoVotoManager() {
        return tipoVotoManager;
    }

    public String getModeloDocumentoVoto() {
        return modeloDocumentoVoto;
    }

    public void setModeloDocumentoVoto(String modeloDocumentoVoto) {
        this.modeloDocumentoVoto = modeloDocumentoVoto;
    }
    
	public SessaoComposicaoOrdem getSessaoComposicaoOrdem() {
		return sessaoComposicaoOrdem;
	}

	public void setSessaoComposicaoOrdem(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		this.sessaoComposicaoOrdem = sessaoComposicaoOrdem;
	}
	
	public List<TipoVoto> listaVoto (TipoVoto tipo){
		OrgaoJulgador ojProcesso = getSessaoProcessoDocumentoVoto() != null 
				&& getSessaoProcessoDocumentoVoto().getProcessoTrf() != null ? 
						getSessaoProcessoDocumentoVoto().getProcessoTrf().getOrgaoJulgador() : null;
		OrgaoJulgador ojVoto = getSessaoComposicaoOrdem() != null ? getSessaoComposicaoOrdem().getOrgaoJulgador() : null;
		if (ojProcesso != null && ojVoto != null && ojProcesso.equals(ojVoto)){
			AbaVotoRelatorAction aba = ComponentUtil.getComponent("abaVotoRelatorAction");
			return aba.getTipoVotoList();
		}
		
		return getTipoVotoList();
	}
	
	public boolean isVotoRelator(SessaoProcessoDocumentoVoto voto, int idProcesso) {
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, idProcesso);
		if (voto != null &&
			processoTrf != null &&
			voto.getOrgaoJulgador().equals(processoTrf.getOrgaoJulgador())) {
			return true;
		}
		return false;
	}

	public void atualizarVoto(){
		try {
			if (sessaoProcessoDocumentoVoto.getProcessoDocumento() == null || sessaoProcessoDocumentoVoto.getProcessoDocumento().getIdProcessoDocumento() == 0) {	
				sessaoProcessoDocumentoVoto.setOrgaoJulgador(getSessaoComposicaoOrdem().getOrgaoJulgador());
				sessaoProcessoDocumentoVoto.setSessao(getSessaoComposicaoOrdem().getSessao());
				sessaoProcessoDocumentoVoto.setLiberacao(true);
			}
			if(sessaoProcessoDocumentoVoto.getTipoVoto().getIdTipoVoto() == Integer.parseInt(parametroService.valueOf(Parametros.ID_TIPO_VOTO_IMPEDIMENTO_SUSPEICAO))){
				sessaoProcessoDocumentoVoto.setOjAcompanhado(sessaoProcessoDocumentoVoto.getOrgaoJulgador());
				sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(true);
			} else if (sessaoProcessoDocumentoVoto.getTipoVoto().getContexto().equals("C")){
				ProcessoTrf ptr = processoJudicialService.findById(sessaoProcessoDocumentoVoto.getProcessoTrf().getIdProcessoTrf());
				sessaoProcessoDocumentoVoto.setOjAcompanhado(ptr.getOrgaoJulgador());
				sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(false);
			} else {
				int qtdCheck = 0;
				VotoAcompanhadoBean acompanhado = null;
				for (VotoAcompanhadoBean voto : votoAcompanhadoBeanList) {
					if (voto.getCheck()) {
						qtdCheck++;
						acompanhado = voto;
					}
				}
				if (qtdCheck == 1) {
					sessaoProcessoDocumentoVoto.setOjAcompanhado(acompanhado.getSessaoProcessoDocumentoVoto().getOrgaoJulgador());
				}else {
					sessaoProcessoDocumentoVoto.setOjAcompanhado(sessaoProcessoDocumentoVoto.getOrgaoJulgador());
				}
				sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(false);
			}
			if(sessaoProcessoDocumentoVoto.getProcessoDocumento() != null &&
					sessaoProcessoDocumentoVoto.getProcessoDocumento().getIdProcessoDocumento() == 0){
				sessaoProcessoDocumentoVoto.setProcessoDocumento(null);
			}
			EntityUtil.getEntityManager().merge(sessaoProcessoDocumentoVoto);
			EntityUtil.flush();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao atualizar o voto: {0}", e.getLocalizedMessage());
		}
	}

}
