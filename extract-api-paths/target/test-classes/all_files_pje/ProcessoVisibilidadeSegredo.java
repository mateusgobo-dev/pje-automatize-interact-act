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


@Entity
@Table(name = "tb_proc_visibilida_segredo")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_visiblidade_segredo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_visiblidade_segredo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoVisibilidadeSegredo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoVisibilidadeSegredo,Integer> {

	private static final long serialVersionUID = 1L;
	private int idProcessoVisibilidadeSegredo;
	private Processo processo;
	private Pessoa pessoa;
	private Integer idPessoa;
	private Procuradoria procuradoria;

	@Id
	@GeneratedValue(generator = "gen_proc_visiblidade_segredo")
	@Column(name = "id_proc_visibilidade_segredo", nullable = false)
	public int getIdProcessoVisibilidadeSegredo() {
		return idProcessoVisibilidadeSegredo;
	}

	public void setIdProcessoVisibilidadeSegredo(int idProcessoVisibilidadeSegredo) {
		this.idProcessoVisibilidadeSegredo = idProcessoVisibilidadeSegredo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	@Column(name="id_pessoa", insertable=false, updatable=false)
	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}
	
	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoVisibilidadeSegredo)) {
			return false;
		}
		ProcessoVisibilidadeSegredo other = (ProcessoVisibilidadeSegredo) obj;
		if (getIdProcessoVisibilidadeSegredo() != other.getIdProcessoVisibilidadeSegredo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoVisibilidadeSegredo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoVisibilidadeSegredo> getEntityClass() {
		return ProcessoVisibilidadeSegredo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoVisibilidadeSegredo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
