package br.jus.pje.nucleo.entidades;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_visualizadores_sigilo")
@org.hibernate.annotations.GenericGenerator(name = "gen_visualizadores_sigilo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_visualizadores_sigilo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class VisualizadoresSigilo implements IEntidade<VisualizadoresSigilo,Integer> {

	private static final long serialVersionUID = 1L;
	
	private int idVisualizadoresSigilo;
	private OrgaoJulgador orgaoJulgador;
	private PessoaServidor funcionario;
	private Date dtInicio;
	private Date dtFinal;
	private Date ultimaAlteracao;
	private Usuario usuarioAlteracao;
	

	@Id
	@GeneratedValue(generator = "gen_visualizadores_sigilo")
	@Column(name = "id_visualizadores_sigilo", unique = true, nullable = false)
	public int getIdVisualizadoresSigilo() {
		return idVisualizadoresSigilo;
	}


	public void setIdVisualizadoresSigilo(int idVisualizadoresSigilo) {
		this.idVisualizadoresSigilo = idVisualizadoresSigilo;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}


	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_servidor", nullable = false)
	public PessoaServidor getFuncionario() {
		return funcionario;
	}

	
	public void setFuncionario(PessoaServidor funcionario) {
		this.funcionario = funcionario;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio")
	@NotNull
	public Date getDtInicio() {
		return dtInicio;
	}


	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_final")
	public Date getDtFinal() {
		return dtFinal;
	}


	public void setDtFinal(Date dtFinal) {
		this.dtFinal = dtFinal;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_alteracao")
	public Date getUltimaAlteracao() {
		return ultimaAlteracao;
	}


	public void setUltimaAlteracao(Date ultimaAlteracao) {
		this.ultimaAlteracao = ultimaAlteracao;
	}


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_alteracao")
	public Usuario getUsuarioAlteracao() {
		return usuarioAlteracao;
	}


	public void setUsuarioAlteracao(Usuario usuarioAlteracao) {
		this.usuarioAlteracao = usuarioAlteracao;
	}



	@Override
	public int hashCode() {
		return Objects.hash(dtFinal, dtInicio, funcionario, idVisualizadoresSigilo, orgaoJulgador, ultimaAlteracao,
				usuarioAlteracao);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VisualizadoresSigilo other = (VisualizadoresSigilo) obj;
		return Objects.equals(dtFinal, other.dtFinal) && Objects.equals(dtInicio, other.dtInicio)
				&& Objects.equals(funcionario, other.funcionario)
				&& idVisualizadoresSigilo == other.idVisualizadoresSigilo
				&& Objects.equals(orgaoJulgador, other.orgaoJulgador)
				&& Objects.equals(ultimaAlteracao, other.ultimaAlteracao)
				&& Objects.equals(usuarioAlteracao, other.usuarioAlteracao);
	}


	@Transient
	@Override
	public Class<? extends VisualizadoresSigilo> getEntityClass() {
		return VisualizadoresSigilo.class;
	}

	@Transient
	@Override
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdVisualizadoresSigilo());
	}
	
	@Transient
	@Override
	public boolean isLoggable() {
		return true;
	}
	

}
