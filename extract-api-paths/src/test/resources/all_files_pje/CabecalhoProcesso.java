package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.util.StringUtil;


public class CabecalhoProcesso implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTaskInstance;
    private Long idTaskInstanceProximo;
    private String poloAtivo;
    private String poloPassivo;
    private Long idProcesso;
    private String numeroProcesso;
    private String classeJudicial;
    private Integer idOrgaoJulgador;
    private String orgaoJulgador;
    private String orgaoJulgadorColegiado;
    private Date dataChegada;
    private boolean conferido;
    private String chaveAcesso;
    private String tags;
    private String nomeTarefa;
    private List<ArquivoAssinatura> arquivos = new ArrayList<ArquivoAssinatura>();
    private String tipoDocumentoAssinatura;
    private Boolean sigiloso;
    private Boolean prioridade;
    private Boolean podeAssinar;
    private Date dataAlteracaoDocumentoAssinatura;
    private String nomeUsuarioAlteracaoDocumentoAssinatura;
    private List<EtiquetaProcesso> tagsProcessoList = new ArrayList<EtiquetaProcesso>();
    private String loginResponsavelTarefa;
    private String nomeResponsavelTarefa;
    private Boolean podeMovimentarEmLote;
    private Boolean podeMinutarEmLote;
    private Boolean podeIntimarEmLote;
    private Boolean podeDesignarAudienciaEmLote;
    private Boolean podeDesignarPericiaEmLote;
    private Boolean podeRenajudEmLote;
    private String assuntoPrincipal;
    private String status;
    private List<LembreteDTO> lembretes = new ArrayList<LembreteDTO>();
    private String cargoJudicial;
    private String jurisdicao;
    private Date dataDistribuicao;
    private Date ultimoMovimento;
    private String descricaoUltimoMovimento;
    private String modulo;
    private String eleicao;
    private String municipioUfEleicao;
      private Integer idPessoaCabecaAcao;
	private String nomePessoaCabecaAcao;
	private Integer numeroVara;
	private Integer numeroOrigemProcesso;
	private int nivelAcesso;
	private Boolean podeInserirProcessoSessaoEmLote;    
    
    public String getPoloPassivo() {
        return poloPassivo;
    }

    public void setPoloPassivo(String poloPassivo) {
        this.poloPassivo = poloPassivo;
    }

    public String getPoloAtivo() {
        return poloAtivo;
    }

    public void setPoloAtivo(String poloAtivo) {
        this.poloAtivo = poloAtivo;
    }

    public Date getDataChegada() {
        return dataChegada;
    }

    public void setDataChegada(Date dataChegada) {
        this.dataChegada = dataChegada;
    }

    public Long getIdTaskInstance() {
        return idTaskInstance;
    }

    public void setIdTaskInstance(Long idTaskInstance) {
        this.idTaskInstance = idTaskInstance;
    }

    public boolean isConferido() {
        return conferido;
    }

    public void setConferido(boolean conferido) {
        this.conferido = conferido;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNomeTarefa() {
        return nomeTarefa;
    }

    public void setNomeTarefa(String nomeTarefa) {
        this.nomeTarefa = nomeTarefa;
    }


    public List<ArquivoAssinatura> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<ArquivoAssinatura> arquivos) {
        this.arquivos = arquivos;
    }


    public Long getIdTaskInstanceProximo() {
        return idTaskInstanceProximo;
    }

    public void setIdTaskInstanceProximo(Long idTaskInstanceProximo) {
        this.idTaskInstanceProximo = idTaskInstanceProximo;
    }

    public String getTipoDocumentoAssinatura() {
        return tipoDocumentoAssinatura;
    }

    public void setTipoDocumentoAssinatura(String tipoDocumentoAssinatura) {
        this.tipoDocumentoAssinatura = tipoDocumentoAssinatura;
    }

	public Boolean getSigiloso() {
		return sigiloso;
	}

	public void setSigiloso(Boolean sigiloso) {
		this.sigiloso = sigiloso;
	}

	public Boolean getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(Boolean prioridade) {
		this.prioridade = prioridade;
	}

    public Boolean getPodeAssinar() {
        return podeAssinar;
    }

    public void setPodeAssinar(Boolean podeAssinar) {
        this.podeAssinar = podeAssinar;
    }

    public Date getDataAlteracaoDocumentoAssinatura() {
        return dataAlteracaoDocumentoAssinatura;
    }

    public void setDataAlteracaoDocumentoAssinatura(Date dataAlteracaoDocumentoAssinatura) {
        this.dataAlteracaoDocumentoAssinatura = dataAlteracaoDocumentoAssinatura;
    }

    public String getNomeUsuarioAlteracaoDocumentoAssinatura() {
        return nomeUsuarioAlteracaoDocumentoAssinatura;
    }

    public void setNomeUsuarioAlteracaoDocumentoAssinatura(String nomeUsuarioAlteracaoDocumentoAssinatura) {
        this.nomeUsuarioAlteracaoDocumentoAssinatura = nomeUsuarioAlteracaoDocumentoAssinatura;
    }

    public String getLoginResponsavelTarefa() {
        return loginResponsavelTarefa;
    }

    public void setLoginResponsavelTarefa(String loginResponsavelTarefa) {
        this.loginResponsavelTarefa = loginResponsavelTarefa;
    }

    public String getNomeResponsavelTarefa() {
        return nomeResponsavelTarefa;
    }

    public void setNomeResponsavelTarefa(String nomeResponsavelTarefa) {
        this.nomeResponsavelTarefa = nomeResponsavelTarefa;
    }

    public Boolean getPodeMovimentarEmLote() {
        return podeMovimentarEmLote;
    }

    public void setPodeMovimentarEmLote(Boolean podeMovimentarEmLote) {
        this.podeMovimentarEmLote = podeMovimentarEmLote;
    }
    
    public Boolean getPodeMinutarEmLote() {
		return podeMinutarEmLote;
	}

	public void setPodeMinutarEmLote(Boolean podeMinutarEmLote) {
		this.podeMinutarEmLote = podeMinutarEmLote;
	}

    public Boolean getPodeIntimarEmLote() {
		return podeIntimarEmLote;
	}

	public void setPodeIntimarEmLote(Boolean podeIntimarEmLote) {
		this.podeIntimarEmLote = podeIntimarEmLote;
	}
	
    public Boolean getPodeDesignarAudienciaEmLote() {
		return podeDesignarAudienciaEmLote;
	}

	public void setPodeDesignarAudienciaEmLote(Boolean podeDesignarAudienciaEmLote) {
		this.podeDesignarAudienciaEmLote = podeDesignarAudienciaEmLote;
	}

	public Boolean getPodeDesignarPericiaEmLote() {
		return podeDesignarPericiaEmLote;
	}

	public void setPodeDesignarPericiaEmLote(Boolean podeDesignarPericiaEmLote) {
		this.podeDesignarPericiaEmLote = podeDesignarPericiaEmLote;
	}

	public Boolean getPodeRenajudEmLote() {
		return podeRenajudEmLote;
	}

	public void setPodeRenajudEmLote(Boolean podeRenajudEmLote) {
		this.podeRenajudEmLote = podeRenajudEmLote;
	}

	public String getAssuntoPrincipal() {
        return assuntoPrincipal;
    }

    public void setAssuntoPrincipal(String assuntoPrincipal) {
        this.assuntoPrincipal = assuntoPrincipal;
    }

	public List<EtiquetaProcesso> getTagsProcessoList() {
		return tagsProcessoList;
	}

	public void setTagsProcessoList(List<EtiquetaProcesso> tagsProcessoList) {
		this.tagsProcessoList = tagsProcessoList;
	}
	
	public String getChaveAcesso() {
		return chaveAcesso;
	}
	
	public void setChaveAcesso(String chaveAcesso) {
		this.chaveAcesso = chaveAcesso;
	}

	public Long getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Long idProcesso) {
		this.idProcesso = idProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public List<LembreteDTO> getLembretes() {
		return lembretes;
	}
	
	public void setLembretes(List<LembreteDTO> lembretes) {
		this.lembretes = lembretes;
	}
	
	public String getCargoJudicial() {
		return cargoJudicial;
	}
	
	public void setCargoJudicial(String cargoJudicial) {
		this.cargoJudicial = cargoJudicial;
	}
	
	public String getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(String jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	public Date getUltimoMovimento() {
		return ultimoMovimento;
	}

	public void setUltimoMovimento(Date ultimoMovimento) {
		this.ultimoMovimento = ultimoMovimento;
	}

	public String getDescricaoUltimoMovimento() {
		return descricaoUltimoMovimento;
	}

	public void setDescricaoUltimoMovimento(String descricaoUltimoMovimento) {
		this.descricaoUltimoMovimento = descricaoUltimoMovimento;
	}
	
	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public String getEleicao() {
		return eleicao;
	}

	public void setEleicao(String ano, String tipo) {
		String eleicao = "Eleições: "; 
		if (StringUtil.isEmpty(ano) && StringUtil.isEmpty(tipo)) {
			eleicao = "";
		}
		eleicao = (ano != null)? eleicao+ano: "";
		eleicao = (tipo != null)? eleicao+" - "+tipo: "";
		this.eleicao = eleicao;
	}

	public String getMunicipioUfEleicao() {
		return municipioUfEleicao;
	}

	public void setMunicipioUfEleicao(String municipio, String uf) {
		String municipioUfEleicao = (municipio != null)? " - "+ municipio: "";
		municipioUfEleicao = (uf != null)? municipioUfEleicao+"/"+uf: "";
		if (StringUtil.isNotEmpty(getEleicao())) {
			this.eleicao = getEleicao() + municipioUfEleicao;
		}
		this.municipioUfEleicao = municipioUfEleicao;
	}
	
	public Integer getIdPessoaCabecaAcao() {
		return idPessoaCabecaAcao;
	}
	public void setIdPessoaCabecaAcao(Integer idPessoaCabecaAcao) {
		this.idPessoaCabecaAcao = idPessoaCabecaAcao;
	}
	public String getNomePessoaCabecaAcao() {
		return nomePessoaCabecaAcao;
	}
	public void setNomePessoaCabecaAcao(String nomePessoaCabecaAcao) {
		this.nomePessoaCabecaAcao = nomePessoaCabecaAcao;
	}
	public Integer getNumeroVara() {
		return numeroVara;
	}
	public void setNumeroVara(Integer numeroVara) {
		this.numeroVara = numeroVara;
	}
	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}
	
	public int getNivelAcesso() {
		return nivelAcesso;
	}

	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}
	
	public Boolean getPodeInserirProcessoSessaoEmLote() {
		return podeInserirProcessoSessaoEmLote;
	}

	public void setPodeInserirProcessoSessaoEmLote(Boolean podeInserirProcessoSessaoEmLote) {
		this.podeInserirProcessoSessaoEmLote = podeInserirProcessoSessaoEmLote;
	}	

}
