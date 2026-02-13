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
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_endereco")
@org.hibernate.annotations.GenericGenerator(name = "gen_endereco", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_endereco"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@Cacheable
public class Endereco implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Endereco,Integer> {

	private static final long serialVersionUID = 1L;

	private int idEndereco;
	private Cep cep;
	private Procuradoria procuradoria;
	private Usuario usuario;
	private Usuario usuarioCadastrador;
	private String nomeLogradouro;
	private String nomeBairro;

	private String nomeCidade;
	private String nomeEstado;
	private String numeroEndereco;

	private String complemento;
	private Boolean correspondencia;
	private Date dataAlteracao;

	private List<Localizacao> localizacaoList = new ArrayList<Localizacao>(0);

	public Endereco() {
	}

	@Id
	@GeneratedValue(generator = "gen_endereco")
	@Column(name = "id_endereco", unique = true, nullable = false)
	public int getIdEndereco() {
		return this.idEndereco;
	}

	public void setIdEndereco(int idEndereco) {
		this.idEndereco = idEndereco;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_cep", nullable = false)
	@NotNull
	public Cep getCep() {
		return this.cep;
	}

	public void setCep(Cep cep) {
		this.cep = cep;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "nm_logradouro", length = 200)
	@Length(max = 200)
	public String getNomeLogradouro() {
		return this.nomeLogradouro;
	}

	public void setNomeLogradouro(String nomeLogradouro) {
		this.nomeLogradouro = nomeLogradouro;
	}

	@Column(name = "nr_endereco", length = 15)
	@Length(max = 15)
	public String getNumeroEndereco() {
		if(cep != null && cep.getNumeroEndereco() != null && (this.numeroEndereco == null || this.numeroEndereco.isEmpty())) {
			return cep.getNumeroEndereco();
		} else {
			return this.numeroEndereco;
		}
	}

	public void setNumeroEndereco(String numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}

	@Column(name = "ds_complemento", length = 100)
	@Length(max = 100)
	public String getComplemento() {
		if(cep != null && cep.getComplemento() != null && (this.complemento == null || this.complemento.isEmpty())) {
			return cep.getComplemento();
		}else {
			return this.complemento;
		}
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	@Column(name = "nm_bairro", length = 100)
	@Length(max = 100)
	public String getNomeBairro() {
		return this.nomeBairro;
	}

	public void setNomeBairro(String nomeBairro) {
		this.nomeBairro = nomeBairro;
	}

	@Transient
	public String getNomeCidade() {
		if (this.nomeCidade != null) {
			return this.nomeCidade;
		}
		if (cep != null && cep.getMunicipio() != null) {
			return cep.getMunicipio().getMunicipio();
		}
		return null;
	}

	public void setNomeCidade(String nomeCidade) {
		this.nomeCidade = nomeCidade;
	}

	@Transient
	public String getNomeEstado() {
		if (this.nomeEstado != null) {
			return this.nomeEstado;
		}
		if (cep != null && cep.getMunicipio() != null && cep.getMunicipio().getEstado() != null) {
			return cep.getMunicipio().getEstado().getEstado();
		}
		return null;
	}

	public void setNomeEstado(String nomeEstado) {
		this.nomeEstado = nomeEstado;
	}

	@Column(name = "in_correspondencia")
	public Boolean getCorrespondencia() {
		return this.correspondencia;
	}

	public void setCorrespondencia(Boolean correspondencia) {
		this.correspondencia = correspondencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao_endereco")
	public Date getDataAlteracao() {
		return this.dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "endereco")
	public List<Localizacao> getLocalizacaoList() {
		return this.localizacaoList;
	}

	public void setLocalizacaoList(List<Localizacao> localizacaoList) {
		this.localizacaoList = localizacaoList;
	}

	@Override
	public String toString() {
		if (nomeLogradouro != null)
			return nomeLogradouro;
		else
			return "";
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastrador")
	public Usuario getUsuarioCadastrador() {
		return this.usuarioCadastrador;
	}

	public void setUsuarioCadastrador(Usuario usuarioCadastrador) {
		this.usuarioCadastrador = usuarioCadastrador;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCep() == null) ? 0 : getCep().hashCode());
		result = prime * result + ((getComplemento() == null) ? 0 : getComplemento().hashCode());
		result = prime * result + ((getNomeBairro() == null) ? 0 : getNomeBairro().hashCode());
		result = prime * result + ((getNomeLogradouro() == null) ? 0 : getNomeLogradouro().hashCode());
		result = prime * result + ((getNumeroEndereco() == null) ? 0 : getNumeroEndereco().hashCode());
		result = prime * result + ((getUsuario() == null) ? 0 : getUsuario().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Endereco)) {
			return false;
		}
		Endereco other = (Endereco) obj;
		if (getIdEndereco() != other.getIdEndereco()) {
			return false;
		}
		return true;
	}

	@Transient
	public String getEnderecoCompleto() {
		StringBuilder sb = new StringBuilder();
		sb.append(nomeLogradouro == null || nomeLogradouro.isEmpty() ? "" : this.nomeLogradouro);
		sb.append(numeroEndereco == null || numeroEndereco.isEmpty() ? "" : ", " + this.numeroEndereco);
		sb.append(complemento == null || complemento.isEmpty() ? "" : ", " + this.complemento);
		sb.append(nomeBairro == null || nomeBairro.isEmpty() ? "" : ", " + this.nomeBairro);
		if (this.cep != null) {
			if (this.cep.getMunicipio() != null) {
				sb.append(cep.getMunicipio() == null || cep.getMunicipio().toString().isEmpty() ? "" : ", " + this.cep.getMunicipio());
				sb.append(this.cep.getMunicipio().getEstado() == null ? "" : " - "
						+ this.cep.getMunicipio().getEstado().getCodEstado());
			}
			sb.append(" - CEP: " + this.cep.getNumeroCep());
		}
		return sb.toString();
	}

	/**
 	 * propiedade que indica a procuradoria/defensoria proprietária deste endereco.
 	 * @return Procuradoria
 	 */
 	@OneToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "id_procuradoria", nullable = true)
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	@Transient
 	public String getNomeProprietarioEndereco() {
 		String retorno = "";
 		if(usuario != null) {
 			retorno = usuario.getNome();
 		} else if(procuradoria != null) {
 			retorno = procuradoria.getNome();
 		}
 		return retorno;
 	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Endereco> getEntityClass() {
		return Endereco.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEndereco());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
