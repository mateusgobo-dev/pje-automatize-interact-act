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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

/**
 * Entidade representativa de um arquivo que faz parte de um download de documentos
 * em uma operação do modelo nacional de interoperabilidade (MNI).
 * 
 * @author CNJ
 *
 */
@Entity
@Table(name = DownloadBinarioArquivo.NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_download_binario_arquivo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_download_binario_arquivo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DownloadBinarioArquivo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "tb_download_binario_arquivo";
	
	@Id
	@Column(name = "id_download_binario_arquivo")
	@GeneratedValue(generator = "gen_download_binario_arquivo")
	private Integer id;
	
	@Column(name = "id_processo_documento_bin", nullable = false)
	private Integer idProcessoDocumentoBin;
	
	@Column(name = "id_arquivo_origem", nullable = false)
	private String idArquivoOrigem;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_download_binario")
	private DownloadBinario downloadBinario;
	
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
	 * Recupera o identificador interno do binário associado.
	 * 
	 * @return o identificador do {@link ProcessoDocumentoBin} associado
	 */
	public Integer getIdProcessoDocumentoBin() {
		return idProcessoDocumentoBin;
	}

	/**
	 * Atribui a este registro o identificador do {@link ProcessoDocumentoBin} associado.
	 * 
	 * @param idProcessoDocumentoBin o identificador a ser atribuído
	 */
	public void setIdProcessoDocumentoBin(Integer idProcessoDocumentoBin) {
		this.idProcessoDocumentoBin = idProcessoDocumentoBin;
	}

	/**
	 * Recupera o identificador do arquivo originário.
	 * 
	 * @return o identificador do arquivo originário
	 */
	public String getIdArquivoOrigem() {
		return idArquivoOrigem;
	}

	/**
	 * Atribui a este registro um identificador do arquivo originário.
	 * 
	 * @param idArquivoOrigem o identificador a ser atribuído
	 */
	public void setIdArquivoOrigem(String idArquivoOrigem) {
		this.idArquivoOrigem = idArquivoOrigem;
	}

	/**
	 * Recupera o {@link DownloadBinario} associado a este arquivo.
	 * 
	 * @return o {@link DownloadBinario} associado
	 */
	public DownloadBinario getDownloadBinario() {
		return downloadBinario;
	}

	/**
	 * Atribui a este arquivo um {@link DownloadBinario} associado.
	 * 
	 * @param downloadBinario o {@link DownloadBinario} a ser atribuído
	 */
	public void setDownloadBinario(DownloadBinario downloadBinario) {
		this.downloadBinario = downloadBinario;
	}
	
}
