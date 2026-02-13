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
package br.jus.pje.mni.entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.jus.pje.nucleo.entidades.ManifestacaoProcessual;

/**
 * Entidade que representa um objeto de download de informações binárias dentro
 * do modelo nacional de interoperabilidade (MNI).
 * 
 * @author CNJ
 *
 */
@Entity
@Table(name = DownloadBinario.NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_download_binario", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_download_binario"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DownloadBinario implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "tb_download_binario";
	
	@Id
	@Column(name = "id_download_binario")
	@GeneratedValue(generator = "gen_download_binario")
	private Integer id;
	
	@Column(name = "numero_processo", nullable = false)
	private String numeroProcesso;
	
	@Column(name = "data_insercao")
	@Temporal(TemporalType.DATE)
	private Date dataInsercao;

	@OneToMany(mappedBy = "downloadBinario", cascade = CascadeType.REMOVE)
	private List<DownloadBinarioArquivo> arquivos;
	
	@ManyToOne
	@JoinColumn(name = "id_manifestacao_processual")
	private ManifestacaoProcessual manifestacaoProcessual;
	
	/**
	 * Recupera o identificador desta entidade.
	 * 
	 * @return o identificador
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Atribui a esta entidade um identificador.
	 * Em razão do mapeamento JPA, não deve ser utilizado pelo desenvolvedor.
	 * 
	 * @param id o identificador a ser atribuído.
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Recupera o número do processo a que está vinculado este download.
	 * 
	 * @return o número do processo
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * Atribui a este download um número de processo vinculado.
	 * 
	 * @param numeroProcesso o número a ser atribuído
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	/**
	 * Recupera a data de inserção deste registro no banco de dados.
	 * 
	 * @return a data de inserção
	 */
	public Date getDataInsercao() {
		return dataInsercao;
	}

	/**
	 * Atribui a este registro uma data de inserção.
	 * 
	 * @param dataInsercao a data a ser atribuída
	 */
	public void setDataInsercao(Date dataInsercao) {
		this.dataInsercao = dataInsercao;
	}

	/**
	 * Recupera a lista de metadados de arquivos que fazem parte deste download.
	 * 
	 * @return a lista de informações de arquivos
	 */
	public List<DownloadBinarioArquivo> getArquivos() {
		return arquivos;
	}

	/**
	 * Atribui a este download uma lista de arquivos que fazem parte da operação.
	 * 
	 * Não deve ser invocada diretamente.
	 * 
	 * @param arquivos a lista a ser atribuída
	 */
	public void setArquivos(List<DownloadBinarioArquivo> arquivos) {
		this.arquivos = arquivos;
	}

	/**
	 * Recupera a {@link ManifestacaoProcessual} associada com este download.
	 * 
	 * @return a manifestação processual vinculada
	 */
	public ManifestacaoProcessual getManifestacaoProcessual() {
		return manifestacaoProcessual;
	}

	/**
	 * Atribui a este download uma {@link ManifestacaoProcessual} associada.
	 * 
	 * @param manifestacaoProcessual a {@link ManifestacaoProcessual} a ser atribuída
	 */
	public void setManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual) {
		this.manifestacaoProcessual = manifestacaoProcessual;
	}
	
}
