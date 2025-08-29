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

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = ProcessoTrfManifestacaoProcessual.TABLE_NAME)
public class ProcessoTrfManifestacaoProcessual implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfManifestacaoProcessual,Integer> {

	public static final String TABLE_NAME = "tb_processo_trf_manifestacao";
	private static final long serialVersionUID = 1L;

	private Integer id;
	private ProcessoTrf processoTrf;
	private EnderecoWsdl enderecoWsdl;
	private String numeroProcessoManifestacao;

	/**
	 * @return id.
	 */
	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_trf_manifestacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_processo_trf_manifestacao", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            id.
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return processoTrf.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	/**
	 * @param processoTrf
	 *            processoTrf.
	 */
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	/**
	 * @return enderecoWsdl.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_endereco_wsdl")
	public EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}

	/**
	 * @param enderecoWsdl
	 *            enderecoWsdl.
	 */
	public void setEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
	}

	/**
	 * @return numeroProcessoManifestacao.
	 */
	@Column(name = "nr_processo_manifestacao", length = 30, nullable = false)
	@Length(max = 30)
	public String getNumeroProcessoManifestacao() {
		return numeroProcessoManifestacao;
	}

	/**
	 * @param numeroProcessoManifestacao
	 *            numeroProcessoManifestacao.
	 */
	public void setNumeroProcessoManifestacao(String numeroProcessoManifestacao) {
		this.numeroProcessoManifestacao = numeroProcessoManifestacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfManifestacaoProcessual> getEntityClass() {
		return ProcessoTrfManifestacaoProcessual.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
