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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Localizacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_localizacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_localizacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Localizacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Localizacao,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_localizacao";

	private int idLocalizacao;
	private Endereco endereco;
	private String localizacao;
	private Boolean ativo;
	private Localizacao localizacaoPai;
	private Localizacao estruturaFilho;
	private Boolean estrutura = Boolean.FALSE;
	private Integer faixaInferior;
	private Integer faixaSuperior;

	private List<ItemTipoDocumento> itemTipoDocumentoList = new ArrayList<ItemTipoDocumento>(0);
	private List<UsuarioLocalizacao> usuarioLocalizacaoList = new ArrayList<UsuarioLocalizacao>(0);
	private List<Localizacao> localizacaoList = new ArrayList<Localizacao>(0);

	public Localizacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_localizacao")
	@Column(name = "id_localizacao", unique = true, nullable = false)
	public int getIdLocalizacao() {
		return this.idLocalizacao;
	}

	public void setIdLocalizacao(int idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_endereco")
	public Endereco getEndereco() {
		return this.endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	@Column(name = "ds_localizacao", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao_pai")
	public Localizacao getLocalizacaoPai() {
		return this.localizacaoPai;
	}

	public void setLocalizacaoPai(Localizacao localizacaoPai) {
		this.localizacaoPai = localizacaoPai;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "localizacao")
	public List<ItemTipoDocumento> getItemTipoDocumentoList() {
		return this.itemTipoDocumentoList;
	}

	public void setItemTipoDocumentoList(List<ItemTipoDocumento> itemTipoDocumentoList) {
		this.itemTipoDocumentoList = itemTipoDocumentoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "localizacaoFisica")
	public List<UsuarioLocalizacao> getUsuarioLocalizacaoList() {
		return this.usuarioLocalizacaoList;
	}

	public void setUsuarioLocalizacaoList(List<UsuarioLocalizacao> usuarioLocalizacaoList) {
		this.usuarioLocalizacaoList = usuarioLocalizacaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "localizacaoPai")
	@OrderBy("faixaInferior")
	public List<Localizacao> getLocalizacaoList() {
		return this.localizacaoList;
	}

	public void setLocalizacaoList(List<Localizacao> localizacaoList) {
		this.localizacaoList = localizacaoList;
	}

	@Column(name = "in_estrutura", nullable = false)
	@NotNull
	public Boolean getEstrutura() {
		return estrutura;
	}

	public void setEstrutura(Boolean estrutura) {
		this.estrutura = estrutura;
	}

	@Column(name = "nr_faixa_inferior")
	public Integer getFaixaInferior() {
		return this.faixaInferior;
	}

	public void setFaixaInferior(Integer faixaInferior) {
		this.faixaInferior = faixaInferior;
	}

	@Column(name = "nr_faixa_superior")
	public Integer getFaixaSuperior() {
		return this.faixaSuperior;
	}

	public void setFaixaSuperior(Integer faixaSuperior) {
		this.faixaSuperior = faixaSuperior;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura")
	public Localizacao getEstruturaFilho() {
		return estruturaFilho;
	}

	public void setEstruturaFilho(Localizacao estruturaFilho) {
		this.estruturaFilho = estruturaFilho;
	}
	
	/**
	 * Assume que a indicação de faixaInferior e faixaSuperior esteja funcionando corretamente para todas 
	 * as localizações que fazem parte de uma hierarquia
	 * @return
	 */
	@Transient
	public boolean isLocalizacaoFolha() {
		if(this.faixaInferior != null && this.faixaSuperior != null) {
			return ((this.faixaInferior + 1) == this.faixaSuperior);
		}
		return true;
	}

	@Transient
	public Endereco getEnderecoCompleto() {
		if (getEndereco() == null) {
			for (Localizacao pai : getListLocalizacaoAtePai()) {
				if (pai.getEndereco() != null) {
					return pai.getEndereco();
				}
			}
			return null;
		} else {
			return getEndereco();
		}
	}

	@Transient
	public List<Localizacao> getListLocalizacaoAtePai() {
		List<Localizacao> listLocalizacaoAtePai = new ArrayList<Localizacao>();
		Localizacao pai = getLocalizacaoPai();
		while (pai != null) {
			listLocalizacaoAtePai.add(pai);
			pai = pai.getLocalizacaoPai();
		}
		return listLocalizacaoAtePai;
	}

	@Transient
	public String getCaminho() {
		StringBuilder sb = new StringBuilder();
		sb.append(getLocalizacao());
		if (!getListLocalizacaoAtePai().isEmpty()) {
			sb.append(" ").append(getListLocalizacaoAtePai());
		}
		return sb.toString();
	}
		
	@Override
	public String toString() {
	  	return localizacao.replaceAll("<[^>]*>", "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Localizacao)) {
			return false;
		}
		Localizacao other = (Localizacao) obj;
		if (getIdLocalizacao() != other.getIdLocalizacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdLocalizacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Localizacao> getEntityClass() {
		return Localizacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdLocalizacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
