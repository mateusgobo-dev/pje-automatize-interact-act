package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CriterioPesquisa implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer idProcessoTrf;
	private String numeroProcesso;
    private String classe;

    private String[] tags;
    private Integer idTagPai;

    private String tagsString;
    private String poloAtivo;
    private String poloPassivo;
    private String orgao;
    private String ordem;
    private Integer page;
    private Integer maxResults;
    private Long idTaskInstance;
    private String apelidoSessao;

    private Integer idTipoSessao;

    private Date dataSessao;

    private Boolean somenteFavoritas;
    private String objeto;
    private Boolean semEtiqueta;
    private String assunto;
    private Date dataAutuacao;
    private String nomeParte;
    private String nomeFiltro;
    private String numeroDocumento;
    private String competencia;
    private String relator;
    private Integer orgaoJulgador;
    private Boolean somenteLembrete;
    private Boolean somenteSigiloso;
    private Integer eleicao;
    private Integer estado;
    private Integer municipio;
    private Integer prioridadeProcesso;
    private Boolean conferidos = Boolean.FALSE;
    private Long idCargoJudicial;
    private Integer orgaoJulgadorColegiado;
    private Boolean naoLidos = Boolean.FALSE;
    private Integer tipoProcessoDocumento;
    private List<ExigibilidadeAssinaturaEnum> exigibilidadeAssinatura;
    
    public boolean isNumeroDocumentoCPF() {
        return StringUtils.isNotBlank(numeroDocumento) && numeroDocumento.length() <= 14;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
    
    public Integer getIdTagPai() {
		return idTagPai;
	}

	public void setIdTagPai(Integer idTagPai) {
		this.idTagPai = idTagPai;
	}

	public String getTagsString() {
        return tagsString;
    }

    public void setTagsString(String tagsString) {
        this.tagsString = tagsString;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public void setIdTaskInstance(Long idTaskInstance) {
        this.idTaskInstance = idTaskInstance;
    }

    public Long getIdTaskInstance() {
        return idTaskInstance;
    }

    public String getApelidoSessao() {
        return apelidoSessao;
    }

    public void setApelidoSessao(String apelidoSessao) {
        this.apelidoSessao = apelidoSessao;
    }

    public Date getDataSessao() {
        return dataSessao;
    }

    public void setDataSessao(Date dataSessao) {
        this.dataSessao = dataSessao;
    }

    public Integer getIdTipoSessao() {
        return idTipoSessao;
    }

    public void setIdTipoSessao(Integer idTipoSessao) {
        this.idTipoSessao = idTipoSessao;
    }

    public String getPoloAtivo() {
        return poloAtivo;
    }

    public void setPoloAtivo(String poloAtivo) {
        this.poloAtivo = poloAtivo;
    }

    public String getPoloPassivo() {
        return poloPassivo;
    }

    public void setPoloPassivo(String poloPassivo) {
        this.poloPassivo = poloPassivo;
    }

    public String getOrgao() {
        return orgao;
    }

    public void setOrgao(String orgao) {
        this.orgao = orgao;
    }

    public String getOrdem() {
        return ordem;
    }

    public void setOrdem(String ordem) {
        this.ordem = ordem;
    }

    public Boolean getSomenteFavoritas() {
        return somenteFavoritas;
    }

    public void setSomenteFavoritas(Boolean somenteFavoritas) {
        this.somenteFavoritas = somenteFavoritas;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public Boolean getSemEtiqueta() {
        return semEtiqueta;
    }

    public void setSemEtiqueta(Boolean semEtiqueta) {
        this.semEtiqueta = semEtiqueta;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public Date getDataAutuacao() {
        return dataAutuacao;
    }

    public void setDataAutuacao(Date dataAutuacao) {
        this.dataAutuacao = dataAutuacao;
    }

    public String getNomeParte() {
        return nomeParte;
    }

    public void setNomeParte(String nomeParte) {
        this.nomeParte = nomeParte;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getCompetencia() {
        return competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public String getRelator() {
        return relator;
    }

    public void setRelator(String relator) {
        this.relator = relator;
    }

    public Boolean getSomenteLembrete() {
        return somenteLembrete;
    }

    public void setSomenteLembrete(Boolean somenteLembrete) {
        this.somenteLembrete = somenteLembrete;
    }

    public Boolean getSomenteSigiloso() {
        return somenteSigiloso;
    }

    public void setSomenteSigiloso(Boolean somenteSigiloso) {
        this.somenteSigiloso = somenteSigiloso;
    }

    public Integer getOrgaoJulgador() {
        return orgaoJulgador;
    }

    public void setOrgaoJulgador(Integer orgaoJulgador) {
        this.orgaoJulgador = orgaoJulgador;
    }

	public Integer getEleicao() {
		return eleicao;
	}

	public void setEleicao(Integer eleicao) {
		this.eleicao = eleicao;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public Integer getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Integer municipio) {
		this.municipio = municipio;
	}

	public Integer getPrioridadeProcesso() {
		return prioridadeProcesso;
	}

	public void setPrioridadeProcesso(Integer prioridadeProcesso) {
		this.prioridadeProcesso = prioridadeProcesso;
	}
	
	public String getNomeFiltro() {
		return nomeFiltro;
	}
	
	public void setNomeFiltro(String nomeFiltro) {
		this.nomeFiltro = nomeFiltro;
	}
	
	public Boolean getConferidos() {
		return conferidos;
	}
	
	public void setConferidos(Boolean conferidos) {
		this.conferidos = conferidos;
	}

	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	public Long getIdCargoJudicial() {
		return idCargoJudicial;
	}

	public void setIdCargoJudicial(Long idCargoJudicial) {
		this.idCargoJudicial = idCargoJudicial;
	}
	
	public Boolean getNaoLidos() {
		return this.naoLidos;
	}

	public void setNaoLidos(Boolean naoLidos) {
		this.naoLidos = naoLidos;
	}

	public Integer getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(Integer tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public List<ExigibilidadeAssinaturaEnum> getExigibilidadeAssinatura() {
		return exigibilidadeAssinatura;
	}

	public void setExigibilidadeAssinatura(List<ExigibilidadeAssinaturaEnum> exigibilidadeAssinatura) {
		this.exigibilidadeAssinatura = exigibilidadeAssinatura;
	}

}
