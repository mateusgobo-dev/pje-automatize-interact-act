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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;


@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = Sessao.TABLE_NAME)
@IndexedEntity(id="idSessao", value="sessao", 
	mappings={
		@Mapping(beanPath="apelido", mappedPath="nome"),
		@Mapping(beanPath="tipoSessao.tipoSessao", mappedPath="tipo"),
		@Mapping(beanPath="dataSessao", mappedPath="data"),
		@Mapping(beanPath="dataAberturaSessao", mappedPath="abertura"),
		@Mapping(beanPath="dataFechamentoPauta", mappedPath="fechamentopauta"),
		@Mapping(beanPath="dataFechamentoSessao", mappedPath="fechamento"),
		
})
@org.hibernate.annotations.GenericGenerator(name = "gen_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Sessao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Sessao,Integer> {

	public static final String TABLE_NAME = "tb_sessao";
	private static final long serialVersionUID = 1L;

	private int idSessao;
	private TipoSessao tipoSessao;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Usuario usuarioExclusao;
	private SalaHorario orgaoJulgadorColegiadoSalaHorario;
	private Date dataSessao;
	private Date horarioInicio;
	private Date dataExclusao;
	private Date dataAberturaSessao;
	private Date dataFechamentoPauta;
	private Date dataFechamentoSessao;
	private Date dataRealizacaoSessao;
	private Date dataRegistroEvento;
	private Date dataMaxIncProcPauta;
	private Boolean iniciar;
	private Usuario usuarioInclusao;
	private String procurador;
	private String apelido;
	private String observacao;
	private List<SessaoPautaProcessoTrf> sessaoPautaProcessoTrfList = new ArrayList<SessaoPautaProcessoTrf>(0);
	private List<SessaoComposicaoOrdem> sessaoComposicaoOrdemList = new ArrayList<SessaoComposicaoOrdem>(0);
	private List<SessaoProcessoDocumento> sessaoProcessoDocumentoList = new ArrayList<SessaoProcessoDocumento>(0);
	private List<DocumentoSessao> documentoSessaoList = new ArrayList<DocumentoSessao>(0);
	private PessoaProcurador pessoaProcurador;
	private Boolean usarBlocos = false;

	// secretário que iniciou a sessão
	private PessoaServidor secretarioIniciou;
	
	private DocumentoSessao documentoSessao = new DocumentoSessao();

	private Boolean continua = Boolean.FALSE;
	
	private Date dataFimSessao;
	
	public Sessao() {
	}

	@Id
	@GeneratedValue(generator = "gen_sessao")
	@Column(name = "id_sessao", unique = true, nullable = false)
	public int getIdSessao() {
		return this.idSessao;
	}

	public void setIdSessao(int idSessao) {
		this.idSessao = idSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_sessao")
	public TipoSessao getTipoSessao() {
		return this.tipoSessao;
	}

	public void setTipoSessao(TipoSessao tipoSessao) {
		this.tipoSessao = tipoSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_inclusao")
	public Usuario getUsuarioInclusao() {
		return this.usuarioInclusao;
	}

	public void setUsuarioInclusao(Usuario usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return this.orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_exclusao")
	public Usuario getUsuarioExclusao() {
		return this.usuarioExclusao;
	}

	public void setUsuarioExclusao(Usuario usuarioExclusao) {
		this.usuarioExclusao = usuarioExclusao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sala_horario")
	public SalaHorario getOrgaoJulgadorColegiadoSalaHorario() {
		return this.orgaoJulgadorColegiadoSalaHorario;
	}

	public void setOrgaoJulgadorColegiadoSalaHorario(SalaHorario orgaoJulgadorColegiadoSalaHorario) {
		this.orgaoJulgadorColegiadoSalaHorario = orgaoJulgadorColegiadoSalaHorario;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sessao", nullable = false)
	@NotNull
	public Date getDataSessao() {
		return dataSessao;
	}

	public void setDataSessao(Date dataSessao) {
		this.dataSessao = dataSessao;
	}
	
	@Temporal(TemporalType.TIME)
	@Column(name = "nr_horario_inicio")
	public Date getHorarioInicio() {
		return horarioInicio;
	}

	public void setHorarioInicio(Date horarioInicio) {
		this.horarioInicio = horarioInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao")
	public Date getDataExclusao() {
		return dataExclusao;
	}

	public void setDataExclusao(Date dataExclusao) {
		this.dataExclusao = dataExclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_abertura_sessao")
	public Date getDataAberturaSessao() {
		return dataAberturaSessao;
	}

	public void setDataAberturaSessao(Date dataAberturaSessao) {
		this.dataAberturaSessao = dataAberturaSessao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fechamento_pauta")
	public Date getDataFechamentoPauta() {
		return dataFechamentoPauta;
	}

	public void setDataFechamentoPauta(Date dataFechamentoPauta) {
		this.dataFechamentoPauta = dataFechamentoPauta;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fechamento_sessao")
	public Date getDataFechamentoSessao() {
		return dataFechamentoSessao;
	}

	public void setDataFechamentoSessao(Date dataFechamentoSessao) {
		this.dataFechamentoSessao = dataFechamentoSessao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_realizacao_sessao")
	public Date getDataRealizacaoSessao() {
		return dataRealizacaoSessao;
	}

	public void setDataRealizacaoSessao(Date dataRealizacaoSessao) {
		this.dataRealizacaoSessao = dataRealizacaoSessao;
	}

	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY, mappedBy = "sessao")
	public List<SessaoPautaProcessoTrf> getSessaoPautaProcessoTrfList() {
		return sessaoPautaProcessoTrfList;
	}

	public void setSessaoPautaProcessoTrfList(List<SessaoPautaProcessoTrf> sessaoPautaProcessoTrfList) {
		this.sessaoPautaProcessoTrfList = sessaoPautaProcessoTrfList;
	}

	@Column(name = "ds_procurador", length = 100)
	@Length(max = 100)
	public String getProcurador() {
		return procurador;
	}

	public void setProcurador(String procurador) {
		this.procurador = procurador;
	}

	@Column(name = "ds_apelido", length = 200)
	@Length(max = 200)
	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}

	@Column(name = "ds_observacao", length = 4000)
	@Length(max = 4000)
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Column(name = "in_iniciar", nullable = false)
	@NotNull
	public Boolean getIniciar() {
		return iniciar;
	}

	public void setIniciar(Boolean iniciar) {
		this.iniciar = iniciar;
	}
	
	@Column(name = "in_usar_blocos")
	public Boolean getUsarBlocos() {
		return usarBlocos;
	}

	public void setUsarBlocos(Boolean usarBlocos) {
		this.usarBlocos = usarBlocos;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_registro_evento")
	public Date getDataRegistroEvento() {
		return dataRegistroEvento;
	}

	public void setDataRegistroEvento(Date dataRegistroEvento) {
		this.dataRegistroEvento = dataRegistroEvento;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sessao")
	public List<SessaoComposicaoOrdem> getSessaoComposicaoOrdemList() {
		return sessaoComposicaoOrdemList;
	}

	public void setSessaoComposicaoOrdemList(List<SessaoComposicaoOrdem> sessaoComposicaoOrdemList) {
		this.sessaoComposicaoOrdemList = sessaoComposicaoOrdemList;
	}

	public void setSessaoProcessoDocumentoList(List<SessaoProcessoDocumento> sessaoProcessoDocumentoList) {
		this.sessaoProcessoDocumentoList = sessaoProcessoDocumentoList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sessao")
	public List<SessaoProcessoDocumento> getSessaoProcessoDocumentoList() {
		return sessaoProcessoDocumentoList;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procurador")
	public PessoaProcurador getPessoaProcurador() {
		return pessoaProcurador;
	}

	public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
		this.pessoaProcurador = pessoaProcurador;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Sessao)) {
			return false;
		}
		Sessao other = (Sessao) obj;
		if (getIdSessao() != other.getIdSessao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSessao();
		return result;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_max_inclusao_proc_pauta")
	public Date getDataMaxIncProcPauta() {
		return dataMaxIncProcPauta;
	}

	public void setDataMaxIncProcPauta(Date dataMaxIncProcPauta) {
		this.dataMaxIncProcPauta = dataMaxIncProcPauta;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_servidor")
	public PessoaServidor getSecretarioIniciou() {
		return secretarioIniciou;
	}

	public void setSecretarioIniciou(PessoaServidor secretarioIniciou) {
		this.secretarioIniciou = secretarioIniciou;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sessao")
	public List<DocumentoSessao> getDocumentoSessaoList() {
		return documentoSessaoList;
	}
	
	public void setDocumentoSessaoList(List <DocumentoSessao> documentoSessaoList) {
		this.documentoSessaoList = documentoSessaoList;
	}
	
	@Transient
	public DocumentoSessao getDocumentoSessao() {
		if( getDocumentoSessaoList() != null && !getDocumentoSessaoList().isEmpty()) {
			documentoSessao = getDocumentoSessaoList().get(0);
		}
		return documentoSessao;
	}

	public void setDocumentoSessao(DocumentoSessao documentoSessao) {
		this.documentoSessao = documentoSessao;
	}

	@Transient
	public Date getMomentoInicio(){
		Date retorno = null;
		Date horarioInicial = getHorarioInicio();
		if(horarioInicial == null) {
			horarioInicial = getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial();
		}
		Calendar cal = GregorianCalendar.getInstance();
		Calendar aux = GregorianCalendar.getInstance();
		if(horarioInicial != null){
			aux.setTime(horarioInicial);
			cal.setTime(getDataSessao());
			cal.set(Calendar.HOUR_OF_DAY, aux.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, aux.get(Calendar.MINUTE));
			retorno = cal.getTime();
		}
		
		return retorno;
	}

	@Transient
	public List<SessaoComposicaoOrdem> getComposicoesPresentes() {
		List<SessaoComposicaoOrdem> composicoesPresentes = new ArrayList<SessaoComposicaoOrdem>();
		for (SessaoComposicaoOrdem cp : getSessaoComposicaoOrdemList()) {
			if (cp.getPresenteSessao()) {
				composicoesPresentes.add(cp);
			}
		}
		return composicoesPresentes;
	}
	
	@Transient
	public List<OrgaoJulgador> getOrgaosJulgadoresPresentes() {
		List<OrgaoJulgador> orgaosJulgadoresPresentes = new ArrayList<OrgaoJulgador>();
		for (SessaoComposicaoOrdem sco : getComposicoesPresentes()) {
			orgaosJulgadoresPresentes.add(sco.getOrgaoJulgador());
		}
		return orgaosJulgadoresPresentes;
	}
	
	@Column(name = "in_continua", nullable = false)
	@NotNull
	public Boolean getContinua() {
		return continua;
	}

	public void setContinua(Boolean continua) {
		this.continua = continua;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_sessao")
	public Date getDataFimSessao() {
		return dataFimSessao;
	}
	
	public void setDataFimSessao(Date dataFimSessao) {
		this.dataFimSessao = dataFimSessao;
	}
	
	/**
	 * Método responsável por recuperar o Presidente da sessão de julgamento
	 * @return Presidente da sessão de julgamento
	 */
	@Transient
	public PessoaMagistrado getPresidenteSessao() {
		List<SessaoComposicaoOrdem> composicaoOrdem = getSessaoComposicaoOrdemList();

		if (!composicaoOrdem.isEmpty()) {
			for (SessaoComposicaoOrdem composicao: composicaoOrdem) {
				if (composicao.getPresidente().equals(Boolean.TRUE)) {
					if (composicao.getMagistradoSubstitutoSessao() !=null) {
						return composicao.getMagistradoSubstitutoSessao();
					} else if (composicao.getMagistradoPresenteSessao() !=null) {
						return composicao.getMagistradoPresenteSessao();		
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Sessao> getEntityClass() {
		return Sessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(200)
			.append("Sessao(#").append(getIdSessao()).append(' ')
			.append(getDataSessao()).append(' ')
			.append(getApelido()!=null ? getApelido() : "")
			.append(')');
		return sb.toString();
	}
	
}