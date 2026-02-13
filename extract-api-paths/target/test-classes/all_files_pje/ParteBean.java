package br.jus.pje.nucleo.beans.criminal;

import java.util.List;

public class ParteBean extends BaseBean{
	
	private static final long serialVersionUID = 1L;

	private String nome;
	private String[] filiacoes;
	private List<DocumentoIdentificacaoBean> documentos;
	private List<CaracteristicaFisicaBean> caracteristicasFisicas;
	private Integer idProcessoParteLegacy;

	public ParteBean() {
		super();
	}

	public ParteBean(String id, String nome, String[] filiacoes, List<DocumentoIdentificacaoBean> documentos,
			List<CaracteristicaFisicaBean> caracteristicasFisicas, Integer idProcessoParteLegacy) {
		super(id);
		this.nome = nome;
		this.filiacoes = filiacoes;
		this.documentos = documentos;
		this.caracteristicasFisicas = caracteristicasFisicas;
		this.idProcessoParteLegacy = idProcessoParteLegacy;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String[] getFiliacoes() {
		return filiacoes;
	}

	public void setFiliacoes(String[] filiacoes) {
		this.filiacoes = filiacoes;
	}

	public List<DocumentoIdentificacaoBean> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<DocumentoIdentificacaoBean> documentos) {
		this.documentos = documentos;
	}

	public List<CaracteristicaFisicaBean> getCaracteristicasFisicas() {
		return caracteristicasFisicas;
	}

	public void setCaracteristicasFisicas(List<CaracteristicaFisicaBean> caracteristicasFisicas) {
		this.caracteristicasFisicas = caracteristicasFisicas;
	}
	
	public Integer getIdProcessoParteLegacy() {
		return idProcessoParteLegacy;
	}
	
	public void setIdProcessoParteLegacy(Integer idProcessoParteLegacy) {
		this.idProcessoParteLegacy = idProcessoParteLegacy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ParteBean other = (ParteBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
