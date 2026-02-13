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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.ChildList;
import br.jus.pje.nucleo.anotacoes.HierarchicalPath;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.anotacoes.Parent;
import br.jus.pje.nucleo.anotacoes.PathDescriptor;
import br.jus.pje.nucleo.anotacoes.Recursive;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.RestricaoProtocoloCompetenciaEnum;
import br.jus.pje.nucleo.enums.TipoCalculoMeioComunicacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Competencia.TABLE_NAME)
@Recursive
@IndexedEntity(id="idCompetencia", value="competencia",
	mappings = {
		@Mapping(beanPath="competencia", mappedPath="descricao")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_competencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_competencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Competencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Competencia,Integer> {

	public static final String TABLE_NAME = "tb_competencia";
	private static final long serialVersionUID = 1L;

	private int idCompetencia;
	private String competencia;
	private Boolean ativo;
	private Competencia competenciaPai;
	private String caminhoCompleto;
	private Boolean especializada = Boolean.FALSE;
	private RestricaoProtocoloCompetenciaEnum restricaoProtocoloServidorInteno = RestricaoProtocoloCompetenciaEnum.TD;
	private Boolean usuariosExternosPodemProtocolar = Boolean.TRUE;
	private Boolean indicacaoOrgaoJulgadorObrigatoria = Boolean.FALSE;
	private List<CompetenciaClasseAssunto> competenciaClasseAssuntoList = new ArrayList<CompetenciaClasseAssunto>(0);
	private List<OrgaoJulgadorCompetencia> orgaoJulgadorCompetenciaList = new ArrayList<OrgaoJulgadorCompetencia>(0);
	private List<Competencia> competenciaList = new ArrayList<Competencia>(0);
	private List<OrgaoJulgadorColegiadoCompetencia> orgaoJulgadorColegiadoCompetenciaList = new ArrayList<OrgaoJulgadorColegiadoCompetencia>(0);
	private DimensaoAlcada dimensaoAlcada;
	private List<DimensaoPessoal> dimensaoPessoalList = new ArrayList<DimensaoPessoal>(0);
	private List<DimensaoFuncional> dimensaoFuncionalList = new ArrayList<DimensaoFuncional>(0);
	private List<EstatisticaProcessoJusticaFederal> estatisticaProcessoJusticaFederalList = new ArrayList<EstatisticaProcessoJusticaFederal>(0);
	private CategoriaPrazoEnum categoriaPrazoCiencia = CategoriaPrazoEnum.C;
	private CategoriaPrazoEnum categoriaPrazoProcessual = CategoriaPrazoEnum.C;
	private TipoCalculoMeioComunicacaoEnum tipoCalculoMeioComunicacao = TipoCalculoMeioComunicacaoEnum.CD;
	private List<ClasseJudicial> classeJudicialAtendimentoPlantaoList = new ArrayList<ClasseJudicial>(0);

	public Competencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_competencia")
	@Column(name = "id_competencia", unique = true, nullable = false)
	public int getIdCompetencia() {
		return this.idCompetencia;
	}

	public void setIdCompetencia(int idCompetencia) {
		this.idCompetencia = idCompetencia;
	}

	@Column(name = "ds_competencia", nullable = false, length = 200, unique = true)
	@NotNull
	@Length(max = 200)
	@PathDescriptor
	public String getCompetencia() {
		return this.competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "competencia")
	public List<CompetenciaClasseAssunto> getCompetenciaClasseAssuntoList() {
		return this.competenciaClasseAssuntoList;
	}

	public void setCompetenciaClasseAssuntoList(List<CompetenciaClasseAssunto> competenciaClasseAssuntoList) {
		this.competenciaClasseAssuntoList = competenciaClasseAssuntoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "competencia")
	public List<OrgaoJulgadorCompetencia> getOrgaoJulgadorCompetenciaList() {
		return this.orgaoJulgadorCompetenciaList;
	}

	public void setOrgaoJulgadorCompetenciaList(List<OrgaoJulgadorCompetencia> orgaoJulgadorCompetenciaList) {
		this.orgaoJulgadorCompetenciaList = orgaoJulgadorCompetenciaList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "competencia")
	public List<EstatisticaProcessoJusticaFederal> getEstatisticaProcessoJusticaFederalList() {
		return estatisticaProcessoJusticaFederalList;
	}

	public void setEstatisticaProcessoJusticaFederalList(
			List<EstatisticaProcessoJusticaFederal> estatisticaProcessoJusticaFederalList) {
		this.estatisticaProcessoJusticaFederalList = estatisticaProcessoJusticaFederalList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "competencia")
	public List<OrgaoJulgadorColegiadoCompetencia> getOrgaoJulgadorColegiadoCompetenciaList() {
		return this.orgaoJulgadorColegiadoCompetenciaList;
	}

	public void setOrgaoJulgadorColegiadoCompetenciaList(
			List<OrgaoJulgadorColegiadoCompetencia> orgaoJulgadorColegiadoCompetenciaList) {
		this.orgaoJulgadorColegiadoCompetenciaList = orgaoJulgadorColegiadoCompetenciaList;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_dimensao_alcada")
	public DimensaoAlcada getDimensaoAlcada() {
		return dimensaoAlcada;
	}

	public void setDimensaoAlcada(DimensaoAlcada dimensaoAlcada) {
		this.dimensaoAlcada = dimensaoAlcada;
	}

	@Override
	public String toString() {
		return competencia;
	}

	@Transient
	public List<AssuntoTrf> getCompetenciaClasseAssuntoListAssunto() {
		List<AssuntoTrf> list = new ArrayList<AssuntoTrf>();
		for (CompetenciaClasseAssunto comp : competenciaClasseAssuntoList) {
			list.add(comp.getAssuntoTrf());
		}
		return list;
	}

	@Transient
	public List<AplicacaoClasse> getCompetenciaClasseAssuntoListAplicacaoClasse() {
		List<AplicacaoClasse> list = new ArrayList<AplicacaoClasse>();
		for (CompetenciaClasseAssunto comp : competenciaClasseAssuntoList) {
			list.add(comp.getClasseAplicacao().getAplicacaoClasse());
		}
		return list;
	}

	@Transient
	public List<OrgaoJulgadorCompetencia> getOrgaoJulgadorCompetenciaAtivoList() {
		Date dataAtual = DateUtil.getBeginningOfDay(new Date());
		List<OrgaoJulgadorCompetencia> orgaoJulgadorCompetenciaAtivoList = new ArrayList<OrgaoJulgadorCompetencia>();
		for (OrgaoJulgadorCompetencia ojc : getOrgaoJulgadorCompetenciaList()) {
			if (ojc.getOrgaoJulgador() != null && ojc.getOrgaoJulgador().getAtivo() && 
					ojc.getDataInicio().compareTo(dataAtual) <= 0 && (ojc.getDataFim() == null || ojc.getDataFim().compareTo(dataAtual) >= 0)) {
				
				orgaoJulgadorCompetenciaAtivoList.add(ojc);
			}
		}
		return orgaoJulgadorCompetenciaAtivoList;
	}

	@Transient
	public List<OrgaoJulgadorColegiadoCompetencia> getOrgaoJulgadorColegiadoCompetenciaAtivoList() {
		Date dataAtual = DateUtil.getBeginningOfDay(new Date());
		List<OrgaoJulgadorColegiadoCompetencia> orgaoJulgadorColegiadoCompetenciaAtivoList = new ArrayList<OrgaoJulgadorColegiadoCompetencia>();
		for (OrgaoJulgadorColegiadoCompetencia ojc : getOrgaoJulgadorColegiadoCompetenciaList()) {
			if (ojc.getOrgaoJulgadorColegiado() != null && ojc.getOrgaoJulgadorColegiado().getAtivo() && 
					ojc.getDataInicio().compareTo(dataAtual) <= 0 && (ojc.getDataFim() == null || ojc.getDataFim().compareTo(dataAtual) >= 0)) {
				
				orgaoJulgadorColegiadoCompetenciaAtivoList.add(ojc);
			}
		}
		return orgaoJulgadorColegiadoCompetenciaAtivoList;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "tb_competencia_dpessoal", joinColumns = @JoinColumn(name = "id_competencia"), inverseJoinColumns = @JoinColumn(name = "id_dimensao_pessoal"))
	public List<DimensaoPessoal> getDimensaoPessoalList() {
		return dimensaoPessoalList;
	}

	public void setDimensaoPessoalList(List<DimensaoPessoal> dimensaoPessoalList) {
		this.dimensaoPessoalList = dimensaoPessoalList;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "tb_competencia_dfuncional", joinColumns = @JoinColumn(name = "id_competencia"), inverseJoinColumns = @JoinColumn(name = "id_dimensao_funcional"))
	public List<DimensaoFuncional> getDimensaoFuncionalList() {
		return dimensaoFuncionalList;
	}

	public void setDimensaoFuncionalList(List<DimensaoFuncional> dimensaoFuncionalList) {
		this.dimensaoFuncionalList = dimensaoFuncionalList;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_competencia_pai")
	@Parent
	public Competencia getCompetenciaPai() {
		return competenciaPai;
	}

	public void setCompetenciaPai(Competencia competenciaPai) {
		this.competenciaPai = competenciaPai;
	}

	@Column(name = "ds_competencia_completo")
	@HierarchicalPath
	public String getCaminhoCompleto() {
		return caminhoCompleto;
	}

	public void setCaminhoCompleto(String caminhoCompleto) {
		this.caminhoCompleto = caminhoCompleto;
	}

	@Transient
	public Boolean getEspecializada() {
		if (competenciaPai != null) {
			especializada = true;
		}
		return especializada;
	}

	public void setEspecializada(Boolean especializada) {
		this.especializada = especializada;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "competenciaPai")
	@OrderBy("competencia")
	@ChildList
	public List<Competencia> getCompetenciaList() {
		return this.competenciaList;
	}

	public void setCompetenciaList(List<Competencia> competenciaList) {
		this.competenciaList = competenciaList;
	}
	

	@Column(name = "ds_tipo_servidor_protocola", nullable = false)
	@Enumerated(EnumType.STRING)
	public RestricaoProtocoloCompetenciaEnum getRestricaoProtocoloServidorInteno() {
		return this.restricaoProtocoloServidorInteno;
	}

	public void setRestricaoProtocoloServidorInteno(
			RestricaoProtocoloCompetenciaEnum restricaoProtocoloServidorInteno) {
		this.restricaoProtocoloServidorInteno = restricaoProtocoloServidorInteno;
	}

	@Column(name = "in_usuario_externo_protocola", nullable = false)
	public Boolean getUsuariosExternosPodemProtocolar() {
		return this.usuariosExternosPodemProtocolar;
	}

	public void setUsuariosExternosPodemProtocolar(
			Boolean usuariosExternosPodemProtocolar) {
		this.usuariosExternosPodemProtocolar = usuariosExternosPodemProtocolar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idCompetencia;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Competencia)) {
			return false;
		}
		Competencia other = (Competencia) obj;
		if (getIdCompetencia() != other.getIdCompetencia()) {
			return false;
		}
		return true;
	}

	public boolean estaNestaCompetencia(ProcessoTrf processo) {
		return false;
	}

	@Column(name="in_categoria_prazo_ciencia", nullable=false)
	@Enumerated(EnumType.STRING)	
	public CategoriaPrazoEnum getCategoriaPrazoCiencia() {
		return categoriaPrazoCiencia;
	}

	public void setCategoriaPrazoCiencia(CategoriaPrazoEnum categoriaPrazoCiencia) {
		this.categoriaPrazoCiencia = categoriaPrazoCiencia;
	}
	
	@Column(name="in_categoria_prazo_processual", nullable=false)
	@Enumerated(EnumType.STRING)	
	public CategoriaPrazoEnum getCategoriaPrazoProcessual() {
		return categoriaPrazoProcessual;
	}

	public void setCategoriaPrazoProcessual(CategoriaPrazoEnum categoriaPrazoProcessual) {
		this.categoriaPrazoProcessual = categoriaPrazoProcessual;
	}
	
	@Column(name = "cod_tipo_calc_meio_comunicacao", length = 3)
	@Enumerated(EnumType.STRING)
	public TipoCalculoMeioComunicacaoEnum getTipoCalculoMeioComunicacao() {
		return tipoCalculoMeioComunicacao;
	}

	public void setTipoCalculoMeioComunicacao(TipoCalculoMeioComunicacaoEnum tipoCalculoMeioComunicacao) {
		this.tipoCalculoMeioComunicacao = tipoCalculoMeioComunicacao;
	}
	
	@Column(name = "in_usuario_protocola_orgao_julgador", nullable = false)
	public Boolean getIndicacaoOrgaoJulgadorObrigatoria() {
		return indicacaoOrgaoJulgadorObrigatoria;
	}

	public void setIndicacaoOrgaoJulgadorObrigatoria(Boolean indicacaoOrgaoJulgadorObrigatoria) {
		this.indicacaoOrgaoJulgadorObrigatoria = indicacaoOrgaoJulgadorObrigatoria;
	}

	/**
	 * Recupera a lista de classes judiciais vinculadas a esta competência e que estão habilitadas para atendimento em plantão judiciário.
	 * 
	 * @return a lista de classes judiciais vinculadas a esta competência e que estão habilitadas para atendimento em plantão judiciário
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_competencia_cl_atend_plantao", joinColumns = { @JoinColumn(name = "id_competencia", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false) })
	public List<ClasseJudicial> getClasseJudicialAtendimentoPlantaoList() {
		return classeJudicialAtendimentoPlantaoList;
	}
	
	public void setClasseJudicialAtendimentoPlantaoList(List<ClasseJudicial> classeJudicialAtendimentoPlantaoList) {
		this.classeJudicialAtendimentoPlantaoList = classeJudicialAtendimentoPlantaoList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Competencia> getEntityClass() {
		return Competencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCompetencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}