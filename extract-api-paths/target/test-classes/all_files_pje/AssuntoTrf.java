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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.ChildList;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.anotacoes.Parent;
import br.jus.pje.nucleo.anotacoes.PathDescriptor;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = AssuntoTrf.TABLE_NAME)
@IndexedEntity(id="codAssuntoTrf", value="assunto",
	mappings={
		@Mapping(beanPath="assuntoTrf", mappedPath="assunto")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_assunto_trf", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_assunto_trf"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AssuntoTrf implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AssuntoTrf,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4325727577694667580L;

	public static final String TABLE_NAME = "tb_assunto_trf";

	private int idAssuntoTrf;
	private String codAssuntoTrf;
	private String codAssuntoTrfOutro;
	private String assuntoTrf;
	private String norma;
	private String leiArtigo;
	private String lei;
	private String assuntoTrfGlossario;

	private Boolean ativo;
	private Boolean ignoraPrevencao;
	private Boolean ignoraCompensacao;
	private Boolean mantemProcesso;
	private Boolean segredoJustica;
	private Boolean complementar = Boolean.FALSE;

	private Boolean possuiFilhos;
	private String assuntoCompleto;

	private Boolean pss;
	private Boolean exigeAssuntoAntecedente;
	private boolean inExigeNM;

	private AssuntoTrf assuntoTrfSuperior;
	private List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);
	private List<CompetenciaClasseAssunto> competenciaClasseAssuntoList = new ArrayList<CompetenciaClasseAssunto>(0);
	private List<ProcessoAssunto> processoAssuntoList = new ArrayList<ProcessoAssunto>(0);

	private Double valorPeso;
	private String tituloCodigoAssunto;
	private Integer nivel;
	private Integer faixaInferior;
	private Integer faixaSuperior;
	private Boolean padraoSgt = Boolean.FALSE;
	private String motivoInativacao;

	public AssuntoTrf() {
	}

	@Id
	@GeneratedValue(generator = "gen_assunto_trf")
	@Column(name = "id_assunto_trf", unique = true, nullable = false)
	public int getIdAssuntoTrf() {
		return this.idAssuntoTrf;
	}

	public void setIdAssuntoTrf(int idAssuntoTrf) {
		this.idAssuntoTrf = idAssuntoTrf;
	}

	@Column(name = "cd_assunto_trf", length = 30, nullable = false)
	@NotNull
	@Length(max = 30)
	public String getCodAssuntoTrf() {
		return this.codAssuntoTrf;
	}

	public void setCodAssuntoTrf(String codAssuntoTrf) {
		this.codAssuntoTrf = codAssuntoTrf;
	}

	@Column(name = "cd_assunto_trf_outro", length = 30)
	@Length(max = 30)
	public String getCodAssuntoTrfOutro() {
		return this.codAssuntoTrfOutro;
	}

	public void setCodAssuntoTrfOutro(String codAssuntoTrfOutro) {
		this.codAssuntoTrfOutro = codAssuntoTrfOutro;
	}

	@Column(name = "ds_assunto_trf", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	@PathDescriptor
	public String getAssuntoTrf() {
		return this.assuntoTrf;
	}

	public void setAssuntoTrf(String assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
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

	@Column(name = "in_mantem_processo")
	public Boolean getMantemProcesso() {
		return this.mantemProcesso;
	}

	public void setMantemProcesso(Boolean mantemProcesso) {
		this.mantemProcesso = mantemProcesso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_trf_superior")
	@Parent
	public AssuntoTrf getAssuntoTrfSuperior() {
		return this.assuntoTrfSuperior;
	}

	public void setAssuntoTrfSuperior(AssuntoTrf assuntoTrfSuperior) {
		this.assuntoTrfSuperior = assuntoTrfSuperior;
	}

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "assuntoTrfSuperior")
	@ChildList
	public List<AssuntoTrf> getAssuntoTrfList() {
		return this.assuntoTrfList;
	}

	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}

	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "assuntoTrf")
	public List<ProcessoAssunto> getProcessoAssuntoList() {
		return this.processoAssuntoList;
	}

	public void setProcessoAssuntoList(List<ProcessoAssunto> processoAssuntoList) {
		this.processoAssuntoList = processoAssuntoList;
	}

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "assuntoTrf")
	public List<CompetenciaClasseAssunto> getCompetenciaClasseAssuntoList() {
		return this.competenciaClasseAssuntoList;
	}

	public void setCompetenciaClasseAssuntoList(List<CompetenciaClasseAssunto> competenciaClasseAssuntoList) {
		this.competenciaClasseAssuntoList = competenciaClasseAssuntoList;
	}

	@Override
	public String toString() {
		return assuntoTrf + " (" + codAssuntoTrf + ")";
	}

	@Column(name = "ds_norma", length = 200)
	@Length(max = 200)
	public String getNorma() {
		return this.norma;
	}

	public void setNorma(String norma) {
		this.norma = norma;
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
	@Column(name = "ds_assunto_trf_glossario")
	public String getAssuntoTrfGlossario() {
		return this.assuntoTrfGlossario;
	}

	public void setAssuntoTrfGlossario(String assuntoTrfGlossario) {
		this.assuntoTrfGlossario = assuntoTrfGlossario;
	}

	@Column(name = "in_segredo_justica", nullable = false)
	@NotNull
	public Boolean getSegredoJustica() {
		return this.segredoJustica;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@Column(name = "in_complementar", nullable = false)
	@NotNull
	public Boolean getComplementar() {
		return this.complementar;
	}

	public void setComplementar(Boolean complementar) {
		this.complementar = complementar;
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
	@Column(name = "ds_assunto_completo")
	public String getAssuntoCompleto() {
		return this.assuntoCompleto;
	}

	public void setAssuntoCompleto(String assuntoCompleto) {
		this.assuntoCompleto = assuntoCompleto;
	}

	@Column(name = "in_pss")
	public Boolean getPss() {
		return pss;
	}

	public void setPss(Boolean pss) {
		this.pss = pss;
	}

	@Column(name = "in_crime_antecedente")
	public Boolean getExigeAssuntoAntecedente() {
		return exigeAssuntoAntecedente;
	}

	public void setExigeAssuntoAntecedente(Boolean exigeAssuntoAntecedente) {
		this.exigeAssuntoAntecedente = exigeAssuntoAntecedente;
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

	@Transient
	public String getAssuntoTrfCodigo() {
		return assuntoTrf + " (" + codAssuntoTrf + ")";
	}

	@Transient
	public String getAssuntoTrfCompleto() {
		AssuntoTrf assuntoPai = assuntoTrfSuperior;
		if (assuntoPai == null) {
			return null;
		}
		List<AssuntoTrf> list = getListAssuntoAtePai();
		StringBuilder sb = new StringBuilder();
		for (int i = list.size() - 1; i >= 0; i--) {
			sb.append(list.get(i));
			sb.append(" / ");
		}
		return sb.toString();
	}

	@Transient
	public List<AssuntoTrf> getListAssuntoAtePai() {
		List<AssuntoTrf> list = new ArrayList<AssuntoTrf>();
		AssuntoTrf assuntoPai = assuntoTrfSuperior;
		while (assuntoPai != null) {
			list.add(assuntoPai);
			assuntoPai = assuntoPai.getAssuntoTrfSuperior();
		}
		return list;
	}

	@Transient
	public List<AssuntoTrf> getListAssuntoTrfAtePai() {
		List<AssuntoTrf> list = new ArrayList<AssuntoTrf>();
		AssuntoTrf assuntoTrfPai = this.assuntoTrfSuperior;
		while (assuntoTrfPai != null) {
			list.add(assuntoTrfPai);
			assuntoTrfPai = assuntoTrfPai.getAssuntoTrfSuperior();
		}
		return list;
	}

	@Column(name = "vl_peso", nullable = false)
	@NotNull
	public Double getValorPeso() {
		return valorPeso;
	}

	public void setValorPeso(Double valorPeso) {
		this.valorPeso = valorPeso;
	}

	/*
	 * Adicionado p/ permitir a pesquisa tanto pela descricao quanto pelo codigo
	 * Usado inicialmente no cadastro do Dispositivo da Norma Penal
	 */
	@Formula("ds_assunto_trf||' ('||cd_assunto_trf||')'")
	public String getTituloCodigoAssunto() {
		return tituloCodigoAssunto;
	}

	public void setTituloCodigoAssunto(String tituloCodigoAssunto) {
		this.tituloCodigoAssunto = tituloCodigoAssunto;
	}

	@Column(name = "in_exige_nm")
	public boolean getInExigeNM() {
		return inExigeNM;
	}

	public void setInExigeNM(boolean inExigeNM) {
		this.inExigeNM = inExigeNM;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AssuntoTrf)) {
			return false;
		}
		AssuntoTrf other = (AssuntoTrf) obj;
		if (getIdAssuntoTrf() != other.getIdAssuntoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAssuntoTrf();
		return result;
	}
	
	@Transient
	public String getAssuntoCompletoFormatado(){
		String str= getAssuntoCompleto();
		str = str.replace("|","/");
		return str.substring(0, str.length()-1);
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AssuntoTrf> getEntityClass() {
		return AssuntoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAssuntoTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
