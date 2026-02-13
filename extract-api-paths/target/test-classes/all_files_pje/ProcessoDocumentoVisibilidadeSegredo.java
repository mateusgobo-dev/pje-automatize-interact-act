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
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;


@Entity
@Table(name = "tb_proc_doc_visibi_segredo")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_doc_vsbldde_segredo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_doc_vsbldde_segredo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@IndexedEntity(
		value="visualizador", 
		id="idProcessoDocumentoVisibilidadeSegredo", 
		owners={"processoDocumento"},
		mappings={
				@Mapping(beanPath="pessoa.idPessoa", mappedPath="id_pessoa"),
				@Mapping(beanPath="pessoa.nome", mappedPath="nome_pessoa"),
				@Mapping(beanPath="pessoa.inTipoPessoa", mappedPath="tipo_pessoa")
				
})
public class ProcessoDocumentoVisibilidadeSegredo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoVisibilidadeSegredo,Integer> {

	private static final long serialVersionUID = 1L;
	private int idProcessoDocumentoVisibilidadeSegredo;
	private ProcessoDocumento processoDocumento;
	private Pessoa pessoa;
	private Procuradoria procuradoria;

	@Id
	@GeneratedValue(generator = "gen_proc_doc_vsbldde_segredo")
	@Column(name = "id_proc_dcmnto_vsbldde_segredo", nullable = false)
	public int getIdProcessoDocumentoVisibilidadeSegredo() {
		return idProcessoDocumentoVisibilidadeSegredo;
	}

	public void setIdProcessoDocumentoVisibilidadeSegredo(int idProcessoDocumentoVisibilidadeSegredo) {
		this.idProcessoDocumentoVisibilidadeSegredo = idProcessoDocumentoVisibilidadeSegredo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoVisibilidadeSegredo)) {
			return false;
		}
		ProcessoDocumentoVisibilidadeSegredo other = (ProcessoDocumentoVisibilidadeSegredo) obj;
		if (getIdProcessoDocumentoVisibilidadeSegredo() != other.getIdProcessoDocumentoVisibilidadeSegredo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoVisibilidadeSegredo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoVisibilidadeSegredo> getEntityClass() {
		return ProcessoDocumentoVisibilidadeSegredo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoVisibilidadeSegredo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
