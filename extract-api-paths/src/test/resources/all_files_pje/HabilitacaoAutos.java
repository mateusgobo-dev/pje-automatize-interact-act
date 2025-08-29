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
package br.jus.pje.jt.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.SituacaoHabilitacaoEnum;
import br.jus.pje.jt.enums.TipoDeclaracaoEnum;
import br.jus.pje.jt.enums.TipoMetodoHabilitacaoEnum;
import br.jus.pje.jt.enums.TipoSolicitacaoHabilitacaoEnum;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author Sérgio Pacheco
 * @since 1.4.6
 * @see
 * @class HabilitacaoAutos
 * @description Entidade que representa uma solicitação de habilitação nos autos 
 *              de um processo.
 */
@Entity
@Table(name = HabilitacaoAutos.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_habilitacao_autos", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_habilitacao_autos"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HabilitacaoAutos implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HabilitacaoAutos,Integer> {
	public static final String TABLE_NAME = "tb_habilitacao_autos";
	private static final long serialVersionUID = 1L;
	
	private Integer idHabilitacaoAutos;
	private PessoaAdvogado advogado;
	private Usuario     usuarioSolicitante;
	private ProcessoTrf processo;
	private List<ProcessoParte> representados = new ArrayList<ProcessoParte>(0);
	
	private List<ProcessoParteRepresentante> representantesRemovidos = new ArrayList<ProcessoParteRepresentante>(0);
	private List<Procuradoria> defensoriasRemovidas = new ArrayList<Procuradoria>(0);
	
	private TipoDeclaracaoEnum tipoDeclaracao;
	private SituacaoHabilitacaoEnum situacaoHabilitacao = SituacaoHabilitacaoEnum.A;
	private Date dataHora = new Date();
	private Date dataHoraAvaliacao;
	private Usuario usuarioAvaliador;
	private List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>(0);
	private TipoMetodoHabilitacaoEnum metodoHabilitacao;
	private TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao;
	private Procuradoria procuradoria;
		
	

	@Id
	@GeneratedValue(generator = "gen_habilitacao_autos")
	@Column(name = "id_habilitacao_autos", unique = true, nullable = false)
	public Integer getIdHabilitacaoAutos() {
		return idHabilitacaoAutos;
	}

	public void setIdHabilitacaoAutos(Integer idHabilitacaoAutos) {
		this.idHabilitacaoAutos = idHabilitacaoAutos;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_advogado" )
	@ForeignKey(name = "fk_habilitacao_advogado")	
	public PessoaAdvogado getAdvogado() {
		return advogado;
	}

	public void setAdvogado(PessoaAdvogado advogado) {
		this.advogado = advogado;
	}
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_solicitante", nullable = true)
	public Usuario getUsuarioSolicitante() {
		return usuarioSolicitante;
	}

	public void setUsuarioSolicitante(Usuario usuarioSolicitante) {
		this.usuarioSolicitante = usuarioSolicitante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@ForeignKey(name = "fk_habilitacao_processo_trf")
	@NotNull
	public ProcessoTrf getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}

	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_habilitacao_representados", 
		joinColumns = @JoinColumn(name = "id_habilitacao_autos", nullable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_processo_parte", nullable = false, updatable = false)
	)
	@NotNull
	public List<ProcessoParte> getRepresentados() {
		return representados;
	}

	public void setRepresentados(List<ProcessoParte> representados) {
		this.representados = representados;
	}
		
	@Column(name = "cd_tipo_declaracao", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.csjt.pje.commons.model.type.TipoDeclaracaoType")
	public TipoDeclaracaoEnum getTipoDeclaracao() {
		return tipoDeclaracao;
	}

	public void setTipoDeclaracao(TipoDeclaracaoEnum tipoDeclaracao) {
		this.tipoDeclaracao = tipoDeclaracao;
	}

	@Column(name = "cd_situacao_habilitacao", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.csjt.pje.commons.model.type.SituacaoHabilitacaoType")
	public SituacaoHabilitacaoEnum getSituacaoHabilitacao() {
		return situacaoHabilitacao;
	}

	public void setMetodoHabilitacao(TipoMetodoHabilitacaoEnum metodoHabilitacao) {
		this.metodoHabilitacao = metodoHabilitacao;
	}
	
	@Column(name = "cd_metodo_habilitacao", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoMetodoHabilitacaoEnum getMetodoHabilitacao() {
		return metodoHabilitacao;
	}
	
	@Column(name = "cd_tipo_solicitacao", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoSolicitacaoHabilitacaoEnum getTipoSolicitacaoHabilitacao() {
		return tipoSolicitacaoHabilitacao;
	}

	public void setTipoSolicitacaoHabilitacao(
			TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao) {
		this.tipoSolicitacaoHabilitacao = tipoSolicitacaoHabilitacao;
	}

	public void setSituacaoHabilitacao(SituacaoHabilitacaoEnum situacaoHabilitacao) {
		this.situacaoHabilitacao = situacaoHabilitacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_habilitacao")
	public Date getDataHora() {
		return dataHora;
	}

	public void setDataHora(Date dataHora) {
		this.dataHora = dataHora;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_avaliacao")
	public Date getDataHoraAvaliacao() {
		return dataHoraAvaliacao;
	}

	public void setDataHoraAvaliacao(Date dataHoraAvaliacao) {
		this.dataHoraAvaliacao = dataHoraAvaliacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = true)
	@ForeignKey(name = "fk_habilitacao_usuario")
	public Usuario getUsuarioAvaliador() {
		return usuarioAvaliador;
	}

	public void setUsuarioAvaliador(Usuario usuarioAvaliador) {
		this.usuarioAvaliador = usuarioAvaliador;
	}

	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_habilitacao_documentos", 
		joinColumns = @JoinColumn(name = "id_habilitacao_autos", nullable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_processo_documento", nullable = false, updatable = false)
	)
	public List<ProcessoDocumento> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<ProcessoDocumento> documentos) {
		this.documentos = documentos;
	}
	
	
	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_habilitacao_representantes_substituidos", 
		joinColumns = @JoinColumn(name = "id_habilitacao_autos", nullable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_processo_parte_representante", nullable = false, updatable = false)
	)
	@NotNull
	public List<ProcessoParteRepresentante> getRepresentantesRemovidos() {
		return representantesRemovidos;
	}

	public void setRepresentantesRemovidos(List<ProcessoParteRepresentante> representantesRemovidos) {
		this.representantesRemovidos = representantesRemovidos;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_habilitacao_defensorias_substituidas", 
		joinColumns = @JoinColumn(name = "id_habilitacao_autos", nullable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_procuradoria", nullable = false, updatable = false)
	)
	@NotNull
	public List<Procuradoria> getDefensoriasRemovidas() {
		return defensoriasRemovidas;
	}

	public void setDefensoriasRemovidas(List<Procuradoria> defensoriaRemovidaList) {
		this.defensoriasRemovidas = defensoriaRemovidaList;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdHabilitacaoAutos() == null) ? 0 : getIdHabilitacaoAutos().hashCode());
		result = prime * result + ((getProcesso() == null) ? 0 : getProcesso().hashCode());
		result = prime * result + ((getAdvogado() == null) ? 0 : getAdvogado().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HabilitacaoAutos))
			return false;
		HabilitacaoAutos other = (HabilitacaoAutos) obj;
		if (getIdHabilitacaoAutos() == null) {
			if (other.getIdHabilitacaoAutos() != null)
				return false;
		} else if (getIdHabilitacaoAutos().equals(other.getIdHabilitacaoAutos()))
			return true;
		if (getProcesso() == null) {
			if (other.getProcesso() != null)
				return false;
		} else if (!getProcesso().equals(other.getProcesso()))
			return false;
		if (getAdvogado() == null) {
			if (other.getAdvogado() != null)
				return false;
		} else if (!getAdvogado().equals(other.getAdvogado()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HabilitacaoAutos> getEntityClass() {
		return HabilitacaoAutos.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdHabilitacaoAutos();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
