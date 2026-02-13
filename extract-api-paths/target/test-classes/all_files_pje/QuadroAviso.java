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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;


@Entity
@javax.persistence.Cacheable(true)
@Table(name = QuadroAviso.TABLENAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_quadro_aviso", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_quadro_aviso"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class QuadroAviso implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<QuadroAviso,Integer> {

	public static final String TABLENAME = "tb_quadro_aviso";
	private static final long serialVersionUID = 1L;

	private int idQuadroAviso;
	private String titulo;
	private String mensagem;
	private Usuario usuarioInclusao;
	private Date dataCadastro;
	private Date dataPublicacao;
	private Date dataExpiracao;
	private Boolean ativo;
	private Boolean topo = Boolean.FALSE;
	private Boolean exibeResponsavel = Boolean.TRUE;
	private Usuario usuarioUltimaAlteracao;
	private Date dataUltimaAlteracao;
	private List<QuadroAvisoPapel> quadroAvisoPapelList = new ArrayList<QuadroAvisoPapel>(0);

	@Id
	@GeneratedValue(generator = "gen_quadro_aviso")
	@Column(name = "id_quadro_aviso", unique = true, nullable = false)
	public int getIdQuadroAviso() {
		return idQuadroAviso;
	}

	public void setIdQuadroAviso(int idQuadroAviso) {
		this.idQuadroAviso = idQuadroAviso;
	}

	@Column(name = "ds_titulo", length = 255)
	@Length(max = 255)
	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String dsTitulo) {
		this.titulo = dsTitulo;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_mensagem", nullable = false)
	@NotNull
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String dsMensagem) {
		this.mensagem = dsMensagem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_inclusao")
	public Usuario getUsuarioInclusao() {
		return usuarioInclusao;
	}

	public void setUsuarioInclusao(Usuario usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro", nullable = false)
	@NotNull
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dtCadastro) {
		this.dataCadastro = dtCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_publicacao", nullable = false)
	@NotNull
	public Date getDataPublicacao() {
		return dataPublicacao;
	}

	public void setDataPublicacao(Date dtPublicacao) {
		if(dtPublicacao != null)
			this.dataPublicacao = dtPublicacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expiracao")
	public Date getDataExpiracao() {
		return dataExpiracao;
	}

	public void setDataExpiracao(Date dtExpiracao) {
		this.dataExpiracao = dtExpiracao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "in_topo", nullable = false)
    @NotNull
    public Boolean getTopo() {
        return topo;
    }

    public void setTopo(Boolean topo) {
        this.topo = topo;
    }
    
    @Column(name = "in_exibe_responsavel", nullable = false)
    @NotNull
    public Boolean getExibeResponsavel() {
		return exibeResponsavel;
	}

	public void setExibeResponsavel(Boolean exibeResponsavel) {
		this.exibeResponsavel = exibeResponsavel;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_ultima_alteracao")
	public Usuario getUsuarioUltimaAlteracao() {
		return usuarioUltimaAlteracao;
	}

	public void setUsuarioUltimaAlteracao(Usuario usuarioUltimaAlteracao) {
		this.usuarioUltimaAlteracao = usuarioUltimaAlteracao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_alteracao", nullable = false)
	@NotNull
	public Date getDataUltimaAlteracao() {
		return dataUltimaAlteracao;
	}

	public void setDataUltimaAlteracao(Date dataUltimaAlteracao) {
		this.dataUltimaAlteracao = dataUltimaAlteracao;
	}
	
	@OneToMany(orphanRemoval = true, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "quadroAviso")
	public List<QuadroAvisoPapel> getQuadroAvisoPapelList() {
		return quadroAvisoPapelList;
	}

	public void setQuadroAvisoPapelList(List<QuadroAvisoPapel> quadroAvisoPapelList) {
		this.quadroAvisoPapelList = quadroAvisoPapelList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof QuadroAviso)) {
			return false;
		}
		QuadroAviso other = (QuadroAviso) obj;
		if (getIdQuadroAviso() != other.getIdQuadroAviso()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdQuadroAviso();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends QuadroAviso> getEntityClass() {
		return QuadroAviso.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdQuadroAviso());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
