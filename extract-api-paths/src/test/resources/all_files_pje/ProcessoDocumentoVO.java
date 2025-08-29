/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.jus.je.pje.entity.vo;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * [PJEII-4914]
 * Classe criada para encapsular informa??es ligadas a decis?o.
 *
 * @author marcio.jesus
 */
public class ProcessoDocumentoVO implements Serializable {
    
	private static final long serialVersionUID = 1L;
	private int idProcessoDocumento;
    private TipoProcessoDocumento tipoProcessoDocumento;    
    private Date dataJuntada;
    private String processoDocumento;
    private Boolean ativo;
    private Boolean recursoInterno;
    private Integer idProcDoc;
    private Date dataInicial;
    private Date dataFinal;
    private Date dataInicialJulgamento;
    private Date dataFinalJulgamento;
    
    public ProcessoDocumentoVO(){
    	this.tipoProcessoDocumento = new TipoProcessoDocumento();
    }
    
    public ProcessoDocumentoVO(int idProcessoDocumento, TipoProcessoDocumento tipoProcessoDocumento, Date dataJuntada) {
        this.idProcessoDocumento = idProcessoDocumento;
        this.tipoProcessoDocumento = tipoProcessoDocumento;
        this.dataJuntada = dataJuntada;
    }

    public ProcessoDocumentoVO(int idProcessoDocumento, TipoProcessoDocumento tipoProcessoDocumento, Date dataJuntada,
			String processoDocumento, Boolean ativo, Boolean recursoInterno, Integer idProcDoc) {
		super();
		this.idProcessoDocumento = idProcessoDocumento;
		this.tipoProcessoDocumento = tipoProcessoDocumento;
		this.dataJuntada = dataJuntada;
		this.processoDocumento = processoDocumento;
		this.ativo = ativo;
		this.recursoInterno = recursoInterno; 
		this.idProcDoc = idProcDoc;
	}

	public int getIdProcessoDocumento() {
        return idProcessoDocumento;
    }

    public void setIdProcessoDocumento(int idProcessoDocumento) {
        this.idProcessoDocumento = idProcessoDocumento;
    }
        
    public TipoProcessoDocumento getTipoProcessoDocumento() {
        return tipoProcessoDocumento;
    }

    public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
        this.tipoProcessoDocumento = tipoProcessoDocumento;
    }

    public String getDataJuntada() {
    	if(this.dataJuntada != null)
    		return new SimpleDateFormat("dd/MM/yyyy").format(dataJuntada);
    	return "";
    }

    public void setDataJuntada(Date dataJuntada) {
        this.dataJuntada = dataJuntada;
    }

	public String getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(String processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Boolean getRecursoInterno() {
		return recursoInterno;
	}

	public void setRecursoInterno(Boolean recursoInterno) {
		this.recursoInterno = recursoInterno;
	}

	public Integer getIdProcDoc() {
		return idProcDoc;
	}

	public void setIdProcDoc(Integer idProcDoc) {
		this.idProcDoc = idProcDoc;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Date getDataInicialJulgamento() {
		return dataInicialJulgamento;
	}

	public void setDataInicialJulgamento(Date dataInicialJulgamento) {
		this.dataInicialJulgamento = dataInicialJulgamento;
	}

	public Date getDataFinalJulgamento() {
		return dataFinalJulgamento;
	}

	public void setDataFinalJulgamento(Date dataFinalJulgamento) {
		this.dataFinalJulgamento = dataFinalJulgamento;
	}
	
}
