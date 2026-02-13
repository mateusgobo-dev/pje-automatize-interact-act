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

import javax.persistence.Basic;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.ChildList;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.anotacoes.Parent;
import br.jus.pje.nucleo.anotacoes.PathDescriptor;
import br.jus.pje.nucleo.enums.ClasseComposicaoJulgamentoEnum;
import br.jus.pje.nucleo.enums.ProcessoReferenciaEnum;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = ClasseJudicial.TABLE_NAME)
@IndexedEntity(id="codClasseJudicial", value="classe",
	mappings={
		@Mapping(beanPath="classeJudicialSigla", mappedPath="sigla"),
		@Mapping(beanPath="classeJudicial", mappedPath="nome")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_classe_judicial", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_classe_judicial"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ClasseJudicial implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ClasseJudicial,Integer> {

	private static final long serialVersionUID = 4648747659751321151L;

	public static final String TABLE_NAME = "tb_classe_judicial";

	private int idClasseJudicial;
	private String codClasseJudicial;
	private String codClasseOutro;
	private String classeJudicial;
	private String classeJudicialSigla;
	private String natureza;
	private String norma;
	private String leiArtigo;
	private String lei;
	private Boolean inicial;
	private Boolean recursal;
	private Boolean incidental;
	private Boolean ativo = true;
	private Boolean segredoJustica = false;
	private Boolean complementar = false;
	private Boolean exigeAutoridade = false;
	private Boolean permiteAutoridade = false;
	private ClasseJudicial classeJudicialPai;
	private Double valorPeso = 1.0d;
	private TipoAudiencia tipoPrimeiraAudiencia;
	private String classeJudicialGlossario;
	private Boolean ignoraPrevencao;
	private Boolean ignoraCompensacao;
	private Boolean possuiCusta;
	private Boolean pauta;
	private Boolean pautaAntecRevisao = Boolean.FALSE;	
	private SimNaoFacultativoEnum exigeRevisor;
	private ClasseComposicaoJulgamentoEnum composicaoJulgamento;
		
	/*
	 * Campo para verificação se uma classe judicial precisa
	 * de um fiscal da lei durante o cadastro de um processo
	 */
	private Boolean exigeFiscalLei = Boolean.FALSE;
	private ProcessoReferenciaEnum processoReferencia = ProcessoReferenciaEnum.NE;
	private Fluxo fluxo;
	private Boolean possuiFilhos = Boolean.FALSE;
	private String classeJudicialCompleto;
	private String mensagem;
	private Boolean controlaValorCausa = Boolean.FALSE;
	private Double pisoValorCausa;
	private Double tetoValorCausa;
	private Boolean designacaoAudienciaErroValorCausa = Boolean.TRUE;
	private List<TipoParte> tipoParteList = new ArrayList<TipoParte>(0);
	private List<TipoPichacao> tipoPichacaoList = new ArrayList<TipoPichacao>(0);

	private List<TipoParteConfigClJudicial> tipoParteConfigClJudicial = new ArrayList<TipoParteConfigClJudicial>(0);
	private List<TipoPichacaoClasseJudicial> tipoPichacaoClasseJudicialList = new ArrayList<TipoPichacaoClasseJudicial>(0);
	private List<ClasseJudicialTipoCertidao> tipoCertidaoClasseJudicialList = new ArrayList<ClasseJudicialTipoCertidao>(0);

	private List<Peticao> peticaoList = new ArrayList<Peticao>(0);
	private List<ClasseAplicacao> classeAplicacaoList = new ArrayList<ClasseAplicacao>(0);
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>(0);
	private List<ProcessoTrf> processoTrfList = new ArrayList<ProcessoTrf>(0);
	private List<ClasseJudicialAgrupamento> agrupamentos = new ArrayList<ClasseJudicialAgrupamento>(0);
	private List<FormularioExterno> formulariosExternos = new ArrayList<>(0);
	private Boolean jusPostulandi;
	private String icone;

	private Boolean reclamaPoloPassivo = Boolean.TRUE;
	private Boolean exigeNumeracaoPropria = Boolean.TRUE;
	private Boolean permiteNumeracaoManual = Boolean.FALSE;
	private Boolean habilitarMascaraProcessoReferencia = Boolean.FALSE;
	private Boolean sessaoContinua = Boolean.TRUE;
	private Boolean remessaInstancia;
	private Boolean publico;
	private Boolean exigeDocumentoIdentificacao = Boolean.FALSE;
	private Boolean exigeDocumentoIdentificacaoMNI = Boolean.FALSE;
	private TipoProcessoDocumento tipoProcessoDocumentoInicial;
	
	private Integer nivel;
	private Integer faixaInferior;
	private Integer faixaSuperior;
	private Boolean padraoSgt = Boolean.FALSE;
	private String motivoInativacao;
	private Boolean designarAudienciaEmFluxo = false;
	private String tipoEventoCriminalInicial;

	public ClasseJudicial() {
	}

	@Id
	@GeneratedValue(generator = "gen_classe_judicial")
	@Column(name = "id_classe_judicial", unique = true, nullable = false)
	public int getIdClasseJudicial() {
		return this.idClasseJudicial;
	}

	public void setIdClasseJudicial(int idClasseJudicial) {
		this.idClasseJudicial = idClasseJudicial;
	}

	@Column(name = "cd_classe_judicial", nullable = false, length = 15, unique = true)
	@NotNull
	@Length(max = 15)
	public String getCodClasseJudicial() {
		return this.codClasseJudicial;
	}

	public void setCodClasseJudicial(String codClasseJudicial) {
		this.codClasseJudicial = codClasseJudicial;
	}

	@Column(name = "cd_classe_outro", length = 15)
	@Length(max = 15)
	public String getCodClasseOutro() {
		return this.codClasseOutro;
	}

	public void setCodClasseOutro(String codClasseOutro) {
		this.codClasseOutro = codClasseOutro;
	}

	@Column(name = "ds_classe_judicial", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	@PathDescriptor
	public String getClasseJudicial() {
		return this.classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		if (classeJudicial != null) {
			this.classeJudicial = classeJudicial.toUpperCase();
		} else {
			this.classeJudicial = classeJudicial;
		}
	}

	@Column(name = "ds_mensagem", length = 100)
	@Length(max = 100)
	public String getMensagem() {
		return this.mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	@Column(name = "ds_classe_judicial_sigla", length = 30)
	@Length(max = 30)
	public String getClasseJudicialSigla() {
		return this.classeJudicialSigla;
	}

	public void setClasseJudicialSigla(String classeJudicialSigla) {
		this.classeJudicialSigla = classeJudicialSigla;
	}

	@Column(name = "in_ignora_prevencao")
	public Boolean getIgnoraPrevencao() {
		return this.ignoraPrevencao;
	}

	public void setIgnoraPrevencao(Boolean ignoraPrevencao) {
		this.ignoraPrevencao = ignoraPrevencao;
	}

	@Column(name = "in_ignora_compensacao")
	public Boolean getIgnoraCompensacao() {
		return this.ignoraCompensacao;
	}

	public void setIgnoraCompensacao(Boolean ignoraCompensacao) {
		this.ignoraCompensacao = ignoraCompensacao;
	}

	@Column(name = "in_possui_custa")
	public Boolean getPossuiCusta() {
		return this.possuiCusta;
	}

	public void setPossuiCusta(Boolean possuiCusta) {
		this.possuiCusta = possuiCusta;
	}

	@Column(name = "in_exige_pauta")
	public Boolean getPauta() {
		return this.pauta;
	}

	public void setPauta(Boolean pauta) {
		this.pauta = pauta;
	}

	@Column(name = "in_pauta_antec_revisao", nullable = false)
	public Boolean getPautaAntecRevisao() {
		return pautaAntecRevisao;
	}

	public void setPautaAntecRevisao(Boolean pautaAntecRevisao) {
		this.pautaAntecRevisao = pautaAntecRevisao;
	}

	@Column(name = "tp_processo_referencia", length = 2, nullable = false)
	@Enumerated(EnumType.STRING)
	public ProcessoReferenciaEnum getProcessoReferencia() {
		return this.processoReferencia;
	}

	public void setProcessoReferencia(ProcessoReferenciaEnum processoReferencia) {
		this.processoReferencia = processoReferencia;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_lei")
	public String getLei() {
		return this.lei;
	}

	public void setLei(String lei) {
		this.lei = lei;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_lei_artigo")
	public String getLeiArtigo() {
		return this.leiArtigo;
	}

	public void setLeiArtigo(String leiArtigo) {
		this.leiArtigo = leiArtigo;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_classe_judicial_glossario")
	public String getClasseJudicialGlossario() {
		return this.classeJudicialGlossario;
	}

	public void setClasseJudicialGlossario(String classeJudicialGlossario) {
		this.classeJudicialGlossario = classeJudicialGlossario;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fluxo")
	public Fluxo getFluxo() {
		return fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_recursal")
	public Boolean getRecursal() {
		return recursal;
	}

	public void setRecursal(Boolean recursal) {
		this.recursal = recursal;
	}

	@Column(name = "in_incidental")
	public Boolean getIncidental() {
		return incidental;
	}

	public void setIncidental(Boolean incidental) {
		this.incidental = incidental;
	}

	@Column(name = "in_inicial")
	public Boolean getInicial() {
		return inicial;
	}

	public void setInicial(Boolean inicial) {
		this.inicial = inicial;
	}

	@Column(name = "in_complementar", nullable = false)
	public Boolean getComplementar() {
		return this.complementar;
	}

	public void setComplementar(Boolean complementar) {
		this.complementar = complementar;
	}

	@Column(name = "in_segredo_justica", nullable = false)
	public Boolean getSegredoJustica() {
		return this.segredoJustica;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_tipo_parte_config_cl_judicial", joinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = true) }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_parte_configuracao", nullable = false, updatable = true) })
	public List<TipoParte> getTipoParteList() {
		return this.tipoParteList;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial_pai")
	@Parent
	public ClasseJudicial getClasseJudicialPai() {
		return classeJudicialPai;
	}

	public void setClasseJudicialPai(ClasseJudicial classeJudicialPai) {
		this.classeJudicialPai = classeJudicialPai;
	}

	public void setTipoParteList(List<TipoParte> tipoParteList) {
		this.tipoParteList = tipoParteList;
	}

	@Column(name="in_exige_autoridade", nullable=false)
	public Boolean getExigeAutoridade() {
		return exigeAutoridade;
	}

	public void setExigeAutoridade(Boolean exigeAutoridade) {
		this.exigeAutoridade = exigeAutoridade;
	}

	@Column(name="in_permite_autoridade", nullable=false)
	public Boolean getPermiteAutoridade() {
		return permiteAutoridade;
	}

	public void setPermiteAutoridade(Boolean permiteAutoridade) {
		this.permiteAutoridade = permiteAutoridade;
	}
	
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_tp_pichacao_cl_judicial", joinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_pichacao", nullable = false, updatable = false) })
	public List<TipoPichacao> getTipoPichacaoList() {
		return this.tipoPichacaoList;
	}

	public void setTipoPichacaoList(List<TipoPichacao> tipoPichacaoList) {
		this.tipoPichacaoList = tipoPichacaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeJudicial")
	public List<TipoParteConfigClJudicial> getTipoParteConfigClJudicial() {
		return this.tipoParteConfigClJudicial;
	}

	public void setTipoParteConfigClJudicial(List<TipoParteConfigClJudicial> tipoParteClasseJudicialList) {
		this.tipoParteConfigClJudicial = tipoParteClasseJudicialList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeJudicial")
	public List<TipoPichacaoClasseJudicial> getTipoPichacaoClasseJudicialList() {
		return this.tipoPichacaoClasseJudicialList;
	}

	public void setTipoPichacaoClasseJudicialList(List<TipoPichacaoClasseJudicial> tipoPichacaoClasseJudicialList) {
		this.tipoPichacaoClasseJudicialList = tipoPichacaoClasseJudicialList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeJudicial")
	public List<ClasseJudicialTipoCertidao> getTipoCertidaoClasseJudicialList() {
		return this.tipoCertidaoClasseJudicialList;
	}

	public void setTipoCertidaoClasseJudicialList(List<ClasseJudicialTipoCertidao> tipoCertidaoClasseJudicialList) {
		this.tipoCertidaoClasseJudicialList = tipoCertidaoClasseJudicialList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_peticao_classe_judicial", joinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_peticao", nullable = false, updatable = false) })
	public List<Peticao> getPeticaoList() {
		return this.peticaoList;
	}

	public void setPeticaoList(List<Peticao> peticaoList) {
		this.peticaoList = peticaoList;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "classeJudicial")
	public List<ClasseAplicacao> getClasseAplicacaoList() {
		return this.classeAplicacaoList;
	}

	public void setClasseAplicacaoList(List<ClasseAplicacao> classeAplicacaoList) {
		this.classeAplicacaoList = classeAplicacaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeJudicialPai")
	@OrderBy("classeJudicial")
	@ChildList
	public List<ClasseJudicial> getClasseJudicialList() {
		return this.classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	@Override
	public String toString() {
		return classeJudicial + " (" + codClasseJudicial + ")";
	}

	@Column(name = "ds_natureza", length = 200)
	@Length(max = 200)
	public String getNatureza() {
		return this.natureza;
	}

	public void setNatureza(String natureza) {
		this.natureza = natureza;
	}

	@Column(name = "ds_norma", length = 200)
	@Length(max = 200)
	public String getNorma() {
		return this.norma;
	}

	public void setNorma(String norma) {
		this.norma = norma;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="classe")
	public List<ClasseJudicialAgrupamento> getAgrupamentos() {
		return agrupamentos;
	}

	public void setAgrupamentos(List<ClasseJudicialAgrupamento> agrupamentos) {
		this.agrupamentos = agrupamentos;
	}

	@Column(name = "in_jus_postulandi")
	public Boolean getJusPostulandi() {
		return this.jusPostulandi;
	}

	public void setJusPostulandi(Boolean jusPostulandi) {
		this.jusPostulandi = jusPostulandi;
	}

	@Column(name = "ds_icone", length = 32)
	@Length(max = 32)
	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeJudicial")
	public List<ProcessoTrf> getProcessoTrfList() {
		return this.processoTrfList;
	}

	public void setProcessoTrfList(List<ProcessoTrf> processoTrfList) {
		this.processoTrfList = processoTrfList;
	}

	@Column(name = "in_reclama_polo_passivo")
	@NotNull
	public Boolean getReclamaPoloPassivo() {
		return this.reclamaPoloPassivo;
	}

	public void setReclamaPoloPassivo(Boolean reclamaPoloPassivo) {
		this.reclamaPoloPassivo = reclamaPoloPassivo;
	}

	@Column(name = "in_possui_filhos")
	public Boolean getPossuiFilhos() {
		return this.possuiFilhos;
	}

	public void setPossuiFilhos(Boolean possuiFilhos) {
		this.possuiFilhos = possuiFilhos;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_classe_judicial_completo")
	public String getClasseJudicialCompleto() {
		return classeJudicialCompleto;
	}

	public void setClasseJudicialCompleto(String classeJudicialCompleto) {
		this.classeJudicialCompleto = classeJudicialCompleto;
	}

	@Transient
	public List<ClasseJudicial> getListClasseJudicialAtePai() {
		List<ClasseJudicial> list = new ArrayList<ClasseJudicial>();
		ClasseJudicial classePai = this.classeJudicialPai;
		while (classePai != null) {
			list.add(classePai);
			classePai = classePai.getClasseJudicialPai();
		}
		return list;
	}

	public void setValorPeso(Double valorPeso) {
		this.valorPeso = valorPeso;
	}

	@Column(name = "vl_peso", nullable = false)
	@NotNull
	public Double getValorPeso() {
		return valorPeso;
	}
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "id_tipo_audiencia")
	public TipoAudiencia getTipoPrimeiraAudiencia() {
		return tipoPrimeiraAudiencia;
	}

	public void setTipoPrimeiraAudiencia(TipoAudiencia tipoPrimeiraAudiencia) {
		this.tipoPrimeiraAudiencia = tipoPrimeiraAudiencia;
	}

	@Column(name="in_controla_valor_causa", nullable=false)
	@NotNull
	public Boolean getControlaValorCausa() {
		return controlaValorCausa;
	}

	public void setControlaValorCausa(Boolean controlaValorCausa) {
		this.controlaValorCausa = controlaValorCausa;
	}

	@Column(name="nr_piso_valor_causa")
	public Double getPisoValorCausa() {
		return pisoValorCausa;
	}

	public void setPisoValorCausa(Double pisoValorCausa) {
		this.pisoValorCausa = pisoValorCausa;
	}

	@Column(name="nr_teto_valor_causa", nullable=true)
	public Double getTetoValorCausa() {
		return tetoValorCausa;
	}

	public void setTetoValorCausa(Double tetoValorCausa) {
		this.tetoValorCausa = tetoValorCausa;
	}

	@Column(name="in_designa_aud_errovc")
	@NotNull
	public Boolean getDesignacaoAudienciaErroValorCausa() {
		return designacaoAudienciaErroValorCausa;
	}

	public void setDesignacaoAudienciaErroValorCausa(Boolean designacaoAudienciaErroValorCausa) {
		this.designacaoAudienciaErroValorCausa = designacaoAudienciaErroValorCausa;
	}

	@Column(name="in_exige_numeracao_propria")
	@NotNull
	public Boolean getExigeNumeracaoPropria() {
		return exigeNumeracaoPropria;
	}

	public void setExigeNumeracaoPropria(Boolean exigeNumeracaoPropria) {
		this.exigeNumeracaoPropria = exigeNumeracaoPropria;
	}
	
	@Column(name = "in_exige_fiscal_lei")
	public Boolean getExigeFiscalLei() {
		return exigeFiscalLei;
	}

	public void setExigeFiscalLei(Boolean exigeFiscalLei) {
		this.exigeFiscalLei = exigeFiscalLei;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tp_exige_revisor", nullable=false)
	public SimNaoFacultativoEnum getExigeRevisor() {
		return exigeRevisor;
	}

	public void setExigeRevisor(SimNaoFacultativoEnum exigeRevisor) {
		this.exigeRevisor = exigeRevisor;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "tp_composicao_julgamento", nullable=false)
	public ClasseComposicaoJulgamentoEnum getComposicaoJulgamento() {
		return composicaoJulgamento;
	}

	public void setComposicaoJulgamento(ClasseComposicaoJulgamentoEnum composicaoJulgamento) {
		this.composicaoJulgamento = composicaoJulgamento;
	}

	@Column(name = "cd_tipo_evento_criminal_inicial", nullable = true)
	public String getTipoEventoCriminalInicial() {
		return tipoEventoCriminalInicial;
	}

	public void setTipoEventoCriminalInicial(String tipoEventoCriminalInicial) {
		this.tipoEventoCriminalInicial = tipoEventoCriminalInicial;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ClasseJudicial)) {
			return false;
		}
		ClasseJudicial other = (ClasseJudicial) obj;
		if (getIdClasseJudicial() != other.getIdClasseJudicial()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdClasseJudicial();
		return result;
	}
	
	/**
	 * Indica se o número do processo de referência recebe ou não máscara.
	 * 
	 * @return true para máscara habilitada e false caso contrário.
	 */
	@Column(name="in_habilitar_mascara_proc_ref")
	@NotNull
	public Boolean getHabilitarMascaraProcessoReferencia() {
		return habilitarMascaraProcessoReferencia;
	}

	public void setHabilitarMascaraProcessoReferencia(Boolean habilitarMascaraProcessoReferencia) {
		this.habilitarMascaraProcessoReferencia = habilitarMascaraProcessoReferencia;
	}

	@Column(name="in_permite_numeracao_manual")
	@NotNull
	public Boolean getPermiteNumeracaoManual() {
		return permiteNumeracaoManual;
	}

	public void setPermiteNumeracaoManual(Boolean permiteNumeracaoManual) {
		this.permiteNumeracaoManual = permiteNumeracaoManual;
	}
	
	@Column(name="in_sessao_continua")
	@NotNull
	public Boolean getSessaoContinua() {
		return sessaoContinua;
	}

	public void setSessaoContinua(Boolean sessaoContinua) {
		this.sessaoContinua = sessaoContinua;

	}	

	@Column(name="in_remessa_instancia")
	@NotNull
	public Boolean getRemessaInstancia() {
		return remessaInstancia;
	}

	public void setRemessaInstancia(Boolean remessaInstancia) {
		this.remessaInstancia = remessaInstancia;
	}	
	
	@Column(name="in_publico")
	@NotNull
	public Boolean getPublico() {
		return publico;
	}

	public void setPublico(Boolean publico) {
		this.publico = publico;
	}

	@Column(name="in_exige_doc_identificacao", nullable=false)
	public Boolean getExigeDocumentoIdentificacao() {
		return exigeDocumentoIdentificacao;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumentoInicial() {
		return tipoProcessoDocumentoInicial;
	}
	
	public void setTipoProcessoDocumentoInicial(TipoProcessoDocumento tipoProcessoDocumentoInicial) {
		this.tipoProcessoDocumentoInicial = tipoProcessoDocumentoInicial;
	}

	public void setExigeDocumentoIdentificacao(Boolean exigeDocumentoIdentificacao) {
		this.exigeDocumentoIdentificacao = exigeDocumentoIdentificacao;
	}
	
	@Column(name="in_exige_doc_identificacao_mni", nullable=false)
	public Boolean getExigeDocumentoIdentificacaoMNI() {
		return exigeDocumentoIdentificacaoMNI;
	}
	
	public void setExigeDocumentoIdentificacaoMNI(Boolean exigeDocumentoIdentificacaoMNI) {
		this.exigeDocumentoIdentificacaoMNI = exigeDocumentoIdentificacaoMNI;
	}
	
	@Column(name="nr_nivel", nullable=true)
	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	@Column(name="nr_faixa_inferior", nullable=true)
	public Integer getFaixaInferior() {
		return faixaInferior;
	}

	public void setFaixaInferior(Integer faixaInferior) {
		this.faixaInferior = faixaInferior;
	}

	@Column(name="nr_faixa_superior", nullable=true)
	public Integer getFaixaSuperior() {
		return faixaSuperior;
	}

	public void setFaixaSuperior(Integer faixaSuperior) {
		this.faixaSuperior = faixaSuperior;
	}
	
	@Column(name="in_padrao_sgt", nullable=false)
	public Boolean getPadraoSgt() {
		return padraoSgt;
	}

	public void setPadraoSgt(Boolean padraoSgt) {
		this.padraoSgt = padraoSgt;
	}

	@Column(name="ds_motivo_inativacao", nullable=true)
	public String getMotivoInativacao() {
		return motivoInativacao;
	}

	public void setMotivoInativacao(String motivoInativacao) {
		this.motivoInativacao = motivoInativacao;
	}
	
	@ManyToMany(mappedBy = "classesJudiciaisFormulario")
	public List<FormularioExterno> getFormulariosExternos() {
		return formulariosExternos;
	}
	
	public void setFormulariosExternos(List<FormularioExterno> formulariosExternos) {
		this.formulariosExternos = formulariosExternos;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends ClasseJudicial> getEntityClass() {
		return ClasseJudicial.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdClasseJudicial());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Column(name = "in_designar_audiencia_fluxo")
	public Boolean getDesignarAudienciaEmFluxo() {
		return designarAudienciaEmFluxo;
	}

	public void setDesignarAudienciaEmFluxo(Boolean designarAudienciaEmFluxo) {
		this.designarAudienciaEmFluxo = designarAudienciaEmFluxo;
	}
}
