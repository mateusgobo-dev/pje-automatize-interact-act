package br.jus.pje.nucleo.entidades;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Entity
@Table(name = VinculacaoUsuario.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "sequence_generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_vinculacao_usuario"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class VinculacaoUsuario implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<VinculacaoUsuario,Integer> {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_vinculacao_usuario";

	private Integer idVinculacaoUsuario;
	
	private Usuario usuario;
 	private Usuario usuarioVinculado;
 	private TipoVinculacaoUsuarioEnum tipoVinculacaoUsuario;
	private Localizacao localizacao;
 	private Papel papel;  	
	private Date dataCriacao;
	
	/**
	 * Recupera o identificador da vinculação de usuário
	 * @return o identificador
	 */
	@Id
	@Column(name = "id_vinculacao_usuario", unique = true, nullable = false, updatable = false)
	@NotNull
	@GeneratedValue(generator = "sequence_generator")
	public Integer getIdVinculacaoUsuario() {
		return idVinculacaoUsuario;
	}
	
	/**
	 * Atribui um identificador para a vinculação de usuário.
	 * @param idVinculacaoUsuario a ser atribuído
	 */
	public void setIdVinculacaoUsuario(Integer idVinculacaoUsuario) {
		this.idVinculacaoUsuario = idVinculacaoUsuario;
	}

	/**
	 * Recupera a o usuário da vinculação
	 * 
	 * @return o usuário
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return usuario;
	}
	
	/**
	 * Atribui o usuário.
	 * @param usuario referente a vinculação
	 */
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	/**
	 * Recupera a o usuário vinculado referente a essa vinculação
	 * 
	 * @return o usuário vinculado
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_vinculado", nullable = false)
	@NotNull
	public Usuario getUsuarioVinculado() {
		return usuarioVinculado;
	}
	
	/**
	 * Atribui o usuário vinculado referente a essa vinculação.
	 * @param usuarioVinculado a ser vinculado na vinculação
	 */
	public void setUsuarioVinculado(Usuario usuarioVinculado) {
		this.usuarioVinculado = usuarioVinculado;
	}
 	
 	/**
 	 * Recupera a localização relacionada a vinculação para fins de lotação automatica de usuário
 	 * - corresponde à localização modelo que deve ser padrão entre os OJs em que o titular poderá se vincular
 	 * 
 	 * @return a localização relacionada
 	 */
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "id_localizacao", nullable = true)
 	public Localizacao getLocalizacao() {
 		return localizacao;
 	}
 	
 	/**
 	 * Atribui a localização referente a essa vinculação para fins de lotação automatica de usuário
 	 * - corresponde à localização modelo que deve ser padrão entre os OJs em que o titular poderá se vincular
 	 * @param localizacao a ser relacionada a vinculação
 	 */
 	public void setLocalizacao(Localizacao localizacao) {
 		this.localizacao = localizacao;
 	}
 	
 	/**
 	 * Recupera o papel relacionado a vinculação para fins de lotação automatica de usuário
 	 * 
 	 * @return o papel relacionado
 	 */
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "id_papel", nullable = true)
 	public Papel getPapel() {
 		return papel;
 	}
 	
 	/**
 	 * Atribui o papel referente a essa vinculação para fins de lotação automatica de usuário
 	 * @param papel a ser relacionado à vinculação
 	 */
 	public void setPapel(Papel papel) {
 		this.papel = papel;
 	}	
 	

	/**
	 * Recupera o tipo de vinculação de usuário
	 * @return o tipo de vinculação
	 */
	@Column(name = "tp_vinculacao_usuario", length = 3)
	@Enumerated(EnumType.STRING)
	public TipoVinculacaoUsuarioEnum getTipoVinculacaoUsuario() {
		return tipoVinculacaoUsuario;
	}

	/**
	 * Atribui o tipo de vinculação
	 * @param tipoVinculacaoUsuario a ser atribuído
	 */
	public void setTipoVinculacaoUsuario(TipoVinculacaoUsuarioEnum tipoVinculacaoUsuario) {
		this.tipoVinculacaoUsuario = tipoVinculacaoUsuario;
	}
	
	/**
	 * Recupera a data de criação da vinculação
	 * @return a referida data de criação.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao", nullable = false)
	@NotNull
	public Date getDataCriacao() {
		return dataCriacao;
	}
	
	/**
	 * Atribui a data de criação da vinculação
	 * @param dataCriacao
	 */
	public void setDataCriacao(Date dataCriacao){
		this.dataCriacao = dataCriacao;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VinculacaoUsuario)) {
			return false;
		}
		
		VinculacaoUsuario other = (VinculacaoUsuario) obj;
		return (getIdVinculacaoUsuario() == other.getIdVinculacaoUsuario());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdVinculacaoUsuario() == null) ? 0 : getIdVinculacaoUsuario()); 
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends VinculacaoUsuario> getEntityClass() {
		return VinculacaoUsuario.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdVinculacaoUsuario();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
