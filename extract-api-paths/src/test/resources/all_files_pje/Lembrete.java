package br.jus.pje.nucleo.entidades;

import java.util.Date;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "tb_lembrete")
public class Lembrete implements java.io.Serializable {
	
	private static final long serialVersionUID = -6528159792332128149L;

	@org.hibernate.annotations.GenericGenerator(name = "gen_lembrete", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_lembrete"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "gen_lembrete")
	@Column(name = "id_lembrete", unique = true, nullable = false)
	private Integer idLembrete;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	private ProcessoTrf processoTrf;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento")
	private ProcessoDocumento processoDocumento;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_localizacao")
	private UsuarioLocalizacao usuarioLocalizacao;
	
	@Column(name = "ds_lembrete", length = 250, nullable = false)
	private String descricao;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_visivel_ate", nullable = true)
	private Date dataVisivelAte; 
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", nullable = false)
	private Date  dataInclusao;
	
	@Column(name = "in_ativo", nullable = true)
	private Boolean ativo;
	
	@OneToMany(mappedBy="lembrete", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LembretePermissao> lembretePermissaos;
	
	public Lembrete() {
		super();
	}

	public Integer getIdLembrete() {
		return idLembrete;
	}

	public void setIdLembrete(Integer idLembrete) {
		this.idLembrete = idLembrete;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}
	
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public UsuarioLocalizacao getUsuarioLocalizacao() {
		return usuarioLocalizacao;
	}

	public void setUsuarioLocalizacao(UsuarioLocalizacao usuarioLocalizacao) {
		this.usuarioLocalizacao = usuarioLocalizacao;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getDataVisivelAte() {
		return dataVisivelAte;
	}

	public void setDataVisivelAte(Date dataVisivelAte) {
		this.dataVisivelAte = dataVisivelAte;
	}

	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public List<LembretePermissao> getLembretePermissaos() {
		return lembretePermissaos;
	}

	public void setLembretePermissaos(List<LembretePermissao> lembretePermissaos) {
		this.lembretePermissaos = lembretePermissaos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idLembrete == null) ? 0 : idLembrete.hashCode());
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
		Lembrete other = (Lembrete) obj;
		if (idLembrete == null) {
			if (other.idLembrete != null)
				return false;
		} else if (!idLembrete.equals(other.idLembrete))
			return false;
		return true;
	}

}
