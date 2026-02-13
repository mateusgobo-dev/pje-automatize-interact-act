package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Entity
@Table(name = "tb_lembrete_permissao")
public class LembretePermissao implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8175543444683909152L;

	@org.hibernate.annotations.GenericGenerator(name = "gen_lembrete_permissao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_lembrete_permissao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "gen_lembrete_permissao")
	@Column(name = "id_lembrete_permissao", unique = true, nullable = false)
	private Integer idLembretePermissao;
	
	@ManyToOne(fetch = FetchType.LAZY, optional=false)
	@JoinColumn(name = "id_lembrete", nullable=false)
	private Lembrete lembrete;
	
	@ManyToOne(fetch = FetchType.EAGER, optional=false)
	@JoinColumn(name = "id_localizacao", nullable=false)
	private Localizacao localizacao;
	
	@ManyToOne(fetch = FetchType.EAGER, optional=true)
	@JoinColumn(name = "id_papel", nullable=true)
	private Papel papel;
	
	@ManyToOne(fetch = FetchType.EAGER, optional=true)
	@JoinColumn(name = "id_usuario", nullable=true)
	private Usuario usuario;
	
	@ManyToOne(fetch = FetchType.EAGER, optional=true)
	@JoinColumn(name = "id_orgao_julgador", nullable=true)
	private OrgaoJulgador orgaoJulgador;
	
	@ManyToOne(fetch = FetchType.EAGER, optional=true)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable=true)
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	
	public LembretePermissao() {
		super();
	}

	public Integer getIdLembretePermissao() {
		return idLembretePermissao;
	}

	public void setIdLembretePermissao(Integer idLembretePermissao) {
		this.idLembretePermissao = idLembretePermissao;
	}

	public Lembrete getLembrete() {
		return lembrete;
	}

	public void setLembrete(Lembrete lembrete) {
		this.lembrete = lembrete;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idLembretePermissao == null) ? 0 : idLembretePermissao.hashCode());
		result = prime * result + ((lembrete == null) ? 0 : lembrete.hashCode());
		result = prime * result + ((localizacao == null) ? 0 : localizacao.hashCode());
		result = prime * result + ((papel == null) ? 0 : papel.hashCode());
		result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
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
		LembretePermissao other = (LembretePermissao) obj;
		if (idLembretePermissao == null) {
			if (other.idLembretePermissao != null)
				return false;
		} else if (!idLembretePermissao.equals(other.idLembretePermissao))
			return false;
		if (lembrete == null) {
			if (other.lembrete != null)
				return false;
		} else if (!lembrete.equals(other.lembrete))
			return false;
		if (localizacao == null) {
			if (other.localizacao != null)
				return false;
		} else if (!localizacao.equals(other.localizacao))
			return false;
		if (papel == null) {
			if (other.papel != null)
				return false;
		} else if (!papel.equals(other.papel))
			return false;
		if (usuario == null) {
			if (other.usuario != null)
				return false;
		} else if (!usuario.equals(other.usuario))
			return false;
		return true;
	}
	
	
	
}
