/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;

import br.jus.pje.jt.entidades.AssistenteAdmissibilidade;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.editor.filters.AnotacaoFilter;
import br.jus.pje.nucleo.entidades.editor.filters.ProcessoDocumentoEstruturadoFilter;
import br.jus.pje.nucleo.util.Crypto;

@Entity
@Table(name=ProcessoDocumentoEstruturado.TABLE_NAME)
@FilterDef(name = ProcessoDocumentoEstruturadoFilter.FILTER_PROCESSO_TOPICO_ATIVO)
public class ProcessoDocumentoEstruturado implements Serializable {

	public static final String TABLE_NAME = "tb_proc_doc_estruturado";

	private static final long serialVersionUID = 1L;

	private Integer idProcessoDocumentoEstruturado;
	private EstruturaDocumento estruturaDocumento;
	private ProcessoDocumentoTrfLocal processoDocumentoTrfLocal;
	private ProcessoTrf processoTrf;
	private List<ProcessoDocumentoEstruturadoTopico> processoDocumentoEstruturadoTopicoList;
	private PessoaMagistrado magistrado;
	private AssistenteAdmissibilidade assistenteAdmissibilidade;
	private List<Anotacao> anotacaoList;

	public ProcessoDocumentoEstruturado() {
		processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		processoDocumento.setProcessoDocumentoBin(new ProcessoDocumentoBin());
		processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
		processoDocumentoEstruturadoTopicoList = new ArrayList<ProcessoDocumentoEstruturadoTopico>(0);
		anotacaoList = new ArrayList<Anotacao>(0);
	}

	@Id
	@Column(name = "id_proc_doc_estruturado", unique = true, nullable = false, updatable = false)
	public Integer getIdProcessoDocumentoEstruturado() {
		return idProcessoDocumentoEstruturado;
	}

	public void setIdProcessoDocumentoEstruturado(Integer idProcessoDocumentoEstruturado) {
		this.idProcessoDocumentoEstruturado = idProcessoDocumentoEstruturado;
	}

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name="id_proc_doc_estruturado", updatable=false)
	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfLocal() {
		return processoDocumentoTrfLocal;
	}

	public void setProcessoDocumentoTrfLocal(ProcessoDocumentoTrfLocal processoDocumentoTrfLocal) {
		this.processoDocumentoTrfLocal = processoDocumentoTrfLocal;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado")
	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}

	public void setMagistrado(PessoaMagistrado magistrado) {
		this.magistrado = magistrado;
	}

	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "processoDocumentoEstruturado")
	@OrderBy(value="ordem")
	@Filter(name = ProcessoDocumentoEstruturadoFilter.FILTER_PROCESSO_TOPICO_ATIVO, condition = ProcessoDocumentoEstruturadoFilter.CONDITION_PROCESSO_TOPICO_ATIVO)
	public List<ProcessoDocumentoEstruturadoTopico> getProcessoDocumentoEstruturadoTopicoList() {
		if (processoDocumentoEstruturadoTopicoList != null) {
			Iterator<ProcessoDocumentoEstruturadoTopico> it = processoDocumentoEstruturadoTopicoList.iterator();
			while  (it.hasNext()) {
				if (!it.next().isAtivo()) {
					it.remove();
				}
			}
		}
		return processoDocumentoEstruturadoTopicoList;
	}

	public void setProcessoDocumentoEstruturadoTopicoList(List<ProcessoDocumentoEstruturadoTopico> processoDocumentoEstruturadoTopicoList) {
		this.processoDocumentoEstruturadoTopicoList = processoDocumentoEstruturadoTopicoList;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura_documento")
	public EstruturaDocumento getEstruturaDocumento() {
		return estruturaDocumento;
	}

	public void setEstruturaDocumento(EstruturaDocumento estruturaDocumento) {
		this.estruturaDocumento = estruturaDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf"/* nullable = false */)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assis_admis")
	public AssistenteAdmissibilidade getAssistenteAdmissibilidade() {
		return assistenteAdmissibilidade;
	}

	public void setAssistenteAdmissibilidade(AssistenteAdmissibilidade assistenteAdmissibilidade) {
		this.assistenteAdmissibilidade = assistenteAdmissibilidade;
	}
	
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "documento")
	@Filter(name = AnotacaoFilter.FILTER_ANOTACAO_NAO_EXCLUIDA, condition = AnotacaoFilter.CONDITION_ANOTACAO_NAO_EXCLUIDA)
	@OrderBy("dataCriacao")
	public List<Anotacao> getAnotacaoList() {
		return anotacaoList;
	}
	
	public void setAnotacaoList(List<Anotacao> anotacaoList) {
		this.anotacaoList = anotacaoList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoEstruturado();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if(getIdProcessoDocumentoEstruturado() == null){
			return false;
		}
		ProcessoDocumentoEstruturado other = (ProcessoDocumentoEstruturado) obj;
		if (!idProcessoDocumentoEstruturado.equals(other.getIdProcessoDocumentoEstruturado()))
			return false;
		return true;
	}

	public void atualizaProcessoDocumento() {
		StringBuilder texto = new StringBuilder();
		List<ProcessoDocumentoEstruturadoTopico> pdTopicoList = getProcessoDocumentoEstruturadoTopicoList();
		for (ProcessoDocumentoEstruturadoTopico pdTopico : pdTopicoList) {
			if (texto.length() > 0) {
				texto.append('\n');
			}
			texto.append(pdTopico.getTitulo());
			texto.append('\n');
			texto.append(pdTopico.getConteudo());
		}
		setModeloDocumento(texto.toString());
	}

	@Transient
	public void setModeloDocumento(String modeloDocumento) {
		ProcessoDocumentoBin pdBin = getProcessoDocumentoBin();
		pdBin.setModeloDocumento(modeloDocumento);
	}

	@Transient
	public String getModeloDocumento() {
		return getProcessoDocumentoBin().getModeloDocumento();
	}

	@Transient
	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return getProcessoDocumento().getProcessoDocumentoBin();
	}

	@Transient
	public ProcessoDocumento getProcessoDocumento() {
		return getProcessoDocumentoTrfLocal().getProcessoDocumento();
	}

	@Transient
	public Processo getProcesso() {
		return getProcessoDocumento().getProcesso();
	}

	@Transient
	public Cabecalho getCabecalho() {
		return getEstruturaDocumento().getCabecalho();
	}
	
	@Transient
	public List<ProcessoDocumentoEstruturadoTopico> getProcessoDocumentoEstruturadoTopicoListAtivosEInativos() {
		return processoDocumentoEstruturadoTopicoList;
	}


}
