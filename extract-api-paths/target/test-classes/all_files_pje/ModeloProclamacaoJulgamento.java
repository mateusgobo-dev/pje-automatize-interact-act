package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_modelo_proclamacao_julgamento")
@org.hibernate.annotations.GenericGenerator(name = "gen_modelo_proclamacao_julgamento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_modelo_proclamacao_julgamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ModeloProclamacaoJulgamento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ModeloProclamacaoJulgamento,Integer> {

	private static final long serialVersionUID = -8355821021197940186L;
	
	private int id;
	private String nomeModelo;
	private String descricaoModelo;
	private Date dataAtualizacao;
	private Usuario usuario;
	private Boolean ativo;
	
	@Id
	@GeneratedValue(generator = "gen_modelo_proclamacao_julgamento")
	@Column(name = "id_modelo_proclamacao_julgamento")
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@NotNull
	@Length(max = 100)
	@Column(name = "nm_modelo", nullable = false, unique = true, length = 100)
	public String getNomeModelo() {
		return nomeModelo;
	}
	
	public void setNomeModelo(String nomeModelo) {
		this.nomeModelo = nomeModelo;
	}
	
	@Lob
	@NotNull
	@Type(type = "org.hibernate.type.TextType")
	@Basic(fetch=FetchType.LAZY)
	@Column(name = "ds_modelo", nullable = false)
	public String getDescricaoModelo() {
		return descricaoModelo;
	}
	
	public void setDescricaoModelo(String descricaoModelo) {
		this.descricaoModelo = descricaoModelo;
	}
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao", nullable = false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}
	
	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = true)
	public Usuario getUsuario() {
		return usuario;
	}
	
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@NotNull
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ModeloProclamacaoJulgamento> getEntityClass() {
		return ModeloProclamacaoJulgamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
