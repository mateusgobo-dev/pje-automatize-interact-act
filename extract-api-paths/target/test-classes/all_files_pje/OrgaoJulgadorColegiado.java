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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
import br.jus.pje.nucleo.enums.RelatorRevisorEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = OrgaoJulgadorColegiado.TABLE_NAME)
@Recursive

@IndexedEntity(
		value="colegiado", id="idOrgaoJulgadorColegiado",
		mappings={
				@Mapping(beanPath="orgaoJulgadorColegiado", mappedPath="designacao")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_orgao_julgador_colegiado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_orgao_julgador_colegiado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorColegiado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorColegiado,Integer>{

	public static final String TABLE_NAME = "tb_orgao_julgador_colgiado";
	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorColegiado;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoPai;
	private Localizacao localizacao;
	private AplicacaoClasse aplicacaoClasse;
	private String instancia;
	private String orgaoJulgadorColegiado;
	private String dddTelefone;
	private String numeroTelefone;
	private String dddFax;
	private String numeroFax;
	private Boolean novoOrgaoJulgadorColegiado;
	private String email;
	private Date dtCriacao;
	private String atoCriacao;
	private Integer minimoParticipante;
	private Integer maximoProcessoPauta;
	private Integer minimoParticipanteDistribuicao;
	private Integer diaCienciaInclusaoPauta;
	private Integer diaRetiradaAdiada;
	private Boolean intimacaoAutomatica = Boolean.TRUE;
	private Boolean fechamentoAutomatico = Boolean.TRUE;
	private Integer prazoTermino;
	private Integer prazoDisponibilizaJulgamento;
	private Boolean ativo;
	private String orgaoJulgadorColegiadoCompleto;
	private Jurisdicao jurisdicao;
	private boolean presidenteRelacao;
	private Boolean pautaAntecRevisao = Boolean.FALSE;
	private RelatorRevisorEnum relatorRevisor = RelatorRevisorEnum.REV;
	private List<OrgaoJulgadorColegiadoCompetencia> orgaoJulgadorColegiadoCompetenciaList = new ArrayList<>(
			0);
	private List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoList = new ArrayList<>(0);
	private List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadorColegiadoOrgaoJulgadorList = new ArrayList<>(
			0);
	private List<OrgaoJulgadorColegiadoCargo> orgaoJulgadorColegiadoCargoList = new ArrayList<>(
			0);

	private Integer quantidadeJulgadoresComposicaoReduzida;
	private Integer quantidadeJulgadoresComposicaoIntegral;
	private OrgaoJulgador orgaoJulgadorPresidente;
	private Integer numeroCnj;
	
	public OrgaoJulgadorColegiado() {

	}

	@Id
	@GeneratedValue(generator = "gen_orgao_julgador_colegiado")
	@Column(name = "id_orgao_julgador_colegiado", unique = true, nullable = false)
	public int getIdOrgaoJulgadorColegiado() {
		return idOrgaoJulgadorColegiado;
	}

	public void setIdOrgaoJulgadorColegiado(int idOrgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_org_julgador_colegiado_pai")
	@Parent
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoPai() {
		return this.orgaoJulgadorColegiadoPai;
	}

	public void setOrgaoJulgadorColegiadoPai(OrgaoJulgadorColegiado orgaoJulgadorColegiadoPai) {
		this.orgaoJulgadorColegiadoPai = orgaoJulgadorColegiadoPai;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao", nullable = false)
	@NotNull
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicacao_classe")
	public AplicacaoClasse getAplicacaoClasse() {
		return aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	@Column(name = "in_instancia", nullable = false, length = 1)
	@Length(max = 1)
	@NotNull
	public String getInstancia() {
		return instancia;
	}

	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}

	@Column(name = "ds_orgao_julgador_colegiado", length = 200, unique = true)
	@Length(max = 200)
	@PathDescriptor
	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Transient
	public String getOrgaoJulgadorColegiadoComId(){
		return getOrgaoJulgadorColegiado()+" ("+getIdOrgaoJulgadorColegiado()+")";
	}

	@Column(name = "nr_ddd_telefone", length = 2)
	@Length(max = 2)
	public String getDddTelefone() {
		return dddTelefone;
	}

	public void setDddTelefone(String dddTelefone) {
		this.dddTelefone = dddTelefone;
	}

	@Column(name = "nr_telefone", length = 15)
	@Length(max = 15)
	public String getNumeroTelefone() {
		return numeroTelefone;
	}

	public void setNumeroTelefone(String numeroTelefone) {
		this.numeroTelefone = numeroTelefone;
	}

	@Column(name = "nr_ddd_fax", length = 2)
	@Length(max = 2)
	public String getDddFax() {
		return dddFax;
	}

	public void setDddFax(String dddFax) {
		this.dddFax = dddFax;
	}

	@Column(name = "nr_fax", length = 15)
	@Length(max = 15)
	public String getNumeroFax() {
		return numeroFax;
	}

	public void setNumeroFax(String numeroFax) {
		this.numeroFax = numeroFax;
	}

	@Column(name = "in_novo_org_julgador_colegiado", nullable = false)
	@NotNull
	public Boolean getNovoOrgaoJulgadorColegiado() {
		return novoOrgaoJulgadorColegiado;
	}

	public void setNovoOrgaoJulgadorColegiado(Boolean novoOrgaoJulgadorColegiado) {
		this.novoOrgaoJulgadorColegiado = novoOrgaoJulgadorColegiado;
	}

	@Column(name = "ds_email", length = 100)
	@Length(max = 100)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	public Date getDtCriacao() {
		return dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	@Column(name = "ds_ato_criacao", length = 100)
	@Length(max = 100)
	public String getAtoCriacao() {
		return atoCriacao;
	}

	public void setAtoCriacao(String atoCriacao) {
		this.atoCriacao = atoCriacao;
	}

	@Column(name = "nr_min_participantes")
	public Integer getMinimoParticipante() {
		return minimoParticipante;
	}

	public void setMinimoParticipante(Integer minimoParticipante) {
		this.minimoParticipante = minimoParticipante;
	}
	
	@Column(name = "nr_min_participantes_dist")
	public Integer getMinimoParticipanteDistribuicao() {
		return minimoParticipanteDistribuicao;
	}
	
	public void setMinimoParticipanteDistribuicao(
			Integer minimoParticipanteDistribuicao) {
		this.minimoParticipanteDistribuicao = minimoParticipanteDistribuicao;
	}

	@Column(name = "nr_max_processos")
	public Integer getMaximoProcessoPauta() {
		return maximoProcessoPauta;
	}

	public void setMaximoProcessoPauta(Integer maximoProcessoPauta) {
		this.maximoProcessoPauta = maximoProcessoPauta;
	}

	@Column(name = "nr_dias_ciencia_inclusao_pauta")
	public Integer getDiaCienciaInclusaoPauta() {
		return diaCienciaInclusaoPauta;
	}

	public void setDiaCienciaInclusaoPauta(Integer diaCienciaInclusaoPauta) {
		this.diaCienciaInclusaoPauta = diaCienciaInclusaoPauta;
	}

	@Column(name = "nr_dias_retirar_adiados")
	public Integer getDiaRetiradaAdiada() {
		return diaRetiradaAdiada;
	}

	public void setDiaRetiradaAdiada(Integer diaRetiradaAdiada) {
		this.diaRetiradaAdiada = diaRetiradaAdiada;
	}

	@Column(name = "in_intimacao_automatica", nullable = false)
	@NotNull
	public Boolean getIntimacaoAutomatica() {
		return intimacaoAutomatica;
	}

	public void setIntimacaoAutomatica(Boolean intimacaoAutomatica) {
		this.intimacaoAutomatica = intimacaoAutomatica;
	}

	@Column(name = "in_fechamento_automatico", nullable = false)
	@NotNull
	public Boolean getFechamentoAutomatico() {
		return fechamentoAutomatico;
	}

	public void setFechamentoAutomatico(Boolean fechamentoAutomatico) {
		this.fechamentoAutomatico = fechamentoAutomatico;
	}

	@Column(name = "nr_prazo_termino")
	public Integer getPrazoTermino() {
		return prazoTermino;
	}

	public void setPrazoTermino(Integer prazoTermino) {
		this.prazoTermino = prazoTermino;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgadorColegiadoPai")
	@ChildList
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoList() {
		return this.orgaoJulgadorColegiadoList;
	}

	public void setOrgaoJulgadorColegiadoList(List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoList) {
		this.orgaoJulgadorColegiadoList = orgaoJulgadorColegiadoList;
	}

	@Basic(fetch=FetchType.LAZY)
	@Column(name = "ds_org_julg_colegiado_completo")
	@HierarchicalPath
	public String getOrgaoJulgadorColegiadoCompleto() {
		return orgaoJulgadorColegiadoCompleto;
	}

	public void setOrgaoJulgadorColegiadoCompleto(String orgaoJulgadorColegiadoCompleto) {
		this.orgaoJulgadorColegiadoCompleto = orgaoJulgadorColegiadoCompleto;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_jurisdicao", nullable = false)
	@NotNull
	public Jurisdicao getJurisdicao() {
		return this.jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@Transient
	public List<OrgaoJulgadorColegiado> getListOrgaoJulgadorColegiadoAtePai() {
		List<OrgaoJulgadorColegiado> list = new ArrayList<>();
		OrgaoJulgadorColegiado ojColegiadoPai = getOrgaoJulgadorColegiadoPai();
		while (ojColegiadoPai != null) {
			list.add(ojColegiadoPai);
			ojColegiadoPai = ojColegiadoPai.getOrgaoJulgadorColegiadoPai();
		}
		return list;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgadorColegiado")
	public List<OrgaoJulgadorColegiadoCompetencia> getOrgaoJulgadorColegiadoCompetenciaList() {
		return orgaoJulgadorColegiadoCompetenciaList;
	}

	public void setOrgaoJulgadorColegiadoCompetenciaList(
			List<OrgaoJulgadorColegiadoCompetencia> orgaoJulgadorColegiadoCompetenciaList) {
		this.orgaoJulgadorColegiadoCompetenciaList = orgaoJulgadorColegiadoCompetenciaList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgadorColegiado")
	@OrderBy("ordem ASC")
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> getOrgaoJulgadorColegiadoOrgaoJulgadorList() {
		return this.orgaoJulgadorColegiadoOrgaoJulgadorList;
	}

	public void setOrgaoJulgadorColegiadoOrgaoJulgadorList(
			List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadorColegiadoOrgaoJulgadorList) {
		this.orgaoJulgadorColegiadoOrgaoJulgadorList = orgaoJulgadorColegiadoOrgaoJulgadorList;
	}

	@Column(name = "nr_disponibilizar_pauta")
	public Integer getPrazoDisponibilizaJulgamento() {
		return prazoDisponibilizaJulgamento;
	}

	public void setPrazoDisponibilizaJulgamento(Integer prazoDisponibilizaJulgamento) {
		this.prazoDisponibilizaJulgamento = prazoDisponibilizaJulgamento;
	}

	@Column(name = "in_presidente_relacao", nullable = false)
	@NotNull
	public Boolean getPresidenteRelacao() {
		return presidenteRelacao;
	}

	public void setPresidenteRelacao(Boolean presidenteRelacao) {
		this.presidenteRelacao = presidenteRelacao;
	}

	@Column(name = "in_relator_revisor", length = 3)
	@Enumerated(EnumType.STRING)
	public RelatorRevisorEnum getRelatorRevisor() {
		return relatorRevisor;
	}

	public void setRelatorRevisor(RelatorRevisorEnum relatorRevisor) {
		this.relatorRevisor = relatorRevisor;
	}

	@Column(name = "in_pauta_antec_revisao")
	public Boolean getPautaAntecRevisao() {
		return pautaAntecRevisao;
	}

	public void setPautaAntecRevisao(Boolean pautaAntecRevisao) {
		this.pautaAntecRevisao = pautaAntecRevisao;
	}
	
	@Transient
	public List<OrgaoJulgadorColegiadoCompetencia> getOrgaoJulgadorColegiadoCompetenciaAtivoList() {
		Date dataAtual = DateUtil.getBeginningOfDay(new Date());
		List<OrgaoJulgadorColegiadoCompetencia> orgaoJulgadorColegiadoCompetenciaAtivoList = new ArrayList<>();
		for (OrgaoJulgadorColegiadoCompetencia ojc : this.orgaoJulgadorColegiadoCompetenciaList) {
			if (ojc.getDataFim() == null
					&& (ojc.getOrgaoJulgadorColegiado().getAtivo())
					&& (ojc.getDataInicio().before(dataAtual) || ojc.getDataInicio().equals(dataAtual))
					|| ojc.getDataFim() != null
					&& DateUtil.isBetweenDates(dataAtual, ojc.getDataInicio(), ojc.getDataFim())) {
				orgaoJulgadorColegiadoCompetenciaAtivoList.add(ojc);
			}
		}
		return orgaoJulgadorColegiadoCompetenciaAtivoList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		} else if (obj instanceof OrgaoJulgadorColegiado) {
			OrgaoJulgadorColegiado ojc = (OrgaoJulgadorColegiado) obj;

			if (idOrgaoJulgadorColegiado == 0 && orgaoJulgadorColegiado != null) {
				return orgaoJulgadorColegiado.equals(ojc.getOrgaoJulgadorColegiado());
			} else {
				return idOrgaoJulgadorColegiado == ojc.getIdOrgaoJulgadorColegiado();
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorColegiado();
		return result;
	}

	@Override
	public String toString() {
		return orgaoJulgadorColegiado;
	}

	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgadorColegiado")
	public List<OrgaoJulgadorColegiadoCargo> getOrgaoJulgadorColegiadoCargoList() {
		return orgaoJulgadorColegiadoCargoList;
	}

	public void setOrgaoJulgadorColegiadoCargoList(List<OrgaoJulgadorColegiadoCargo> orgaoJulgadorColegiadoCargoList) {
		this.orgaoJulgadorColegiadoCargoList = orgaoJulgadorColegiadoCargoList;
	}

	@Column(name="nr_quant_julgs_comp_reduzida", nullable=false)
	public Integer getQuantidadeJulgadoresComposicaoReduzida() {
		return quantidadeJulgadoresComposicaoReduzida;
	}

	public void setQuantidadeJulgadoresComposicaoReduzida(Integer quantidadeJulgadoresComposicaoReduzida) {
		this.quantidadeJulgadoresComposicaoReduzida = quantidadeJulgadoresComposicaoReduzida;
	}

	@Column(name="nr_quant_julgs_comp_integral", nullable=false)
	public Integer getQuantidadeJulgadoresComposicaoIntegral() {
		return quantidadeJulgadoresComposicaoIntegral;
	}
	
	public void setQuantidadeJulgadoresComposicaoIntegral(Integer quantidadeJulgadoresComposicaoIntegral) {
		this.quantidadeJulgadoresComposicaoIntegral = quantidadeJulgadoresComposicaoIntegral;
	}
	
	@Column(name="numero_cnj")
	public Integer getNumeroCnj() {
		return numeroCnj;
	}

	public void setNumeroCnj(Integer numeroCnj) {
		this.numeroCnj = numeroCnj;
	}
	
	@ManyToOne(optional=true)
	@JoinColumn(name="id_orgao_julgador_presidente")
	public OrgaoJulgador getOrgaoJulgadorPresidente() {
		return orgaoJulgadorPresidente;
	}

	public void setOrgaoJulgadorPresidente(OrgaoJulgador orgaoJulgadorPresidente) {
		this.orgaoJulgadorPresidente = orgaoJulgadorPresidente;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorColegiado> getEntityClass() {
		return OrgaoJulgadorColegiado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorColegiado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
