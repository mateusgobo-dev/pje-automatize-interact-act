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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.util.DateUtil;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = OrgaoJulgador.TABLE_NAME)
@IndexedEntity(id="idOrgaoJulgador", value="orgaojulgador",
	mappings={
		@Mapping(beanPath="orgaoJulgador", mappedPath="descricao"),
		@Mapping(beanPath="sigla", mappedPath="sigla"),
		@Mapping(beanPath="codigoOrigem", mappedPath="codigoorigem")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_orgao_julgador", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_orgao_julgador"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgador implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgador,Integer> {

	private static final long serialVersionUID = 4688753832035518164L;

	public static final String TABLE_NAME = "tb_orgao_julgador";

	private int idOrgaoJulgador;
	private AplicacaoClasse aplicacaoClasse;
	private Localizacao localizacao;
	private String instancia;
	private String orgaoJulgador;
	private String dddTelefone;
	private String numeroTelefone;
	private String dddFax;
	private String numeroFax;
	private String email;
	private String atoCriacao;
	private String sigla;
	private String codigoOrigem;
	private Date numeroTempoAudiencia;
	private Date dtCriacao;
	private boolean selecionado;
	private Boolean ativo;
	private Boolean novoOrgaoJulgador;
	private Jurisdicao jurisdicao;	
	private List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadorColegiadoOrgaoJulgadorList = new ArrayList<OrgaoJulgadorColegiadoOrgaoJulgador>(
			0);
	private List<OrgaoJulgadorCompetencia> orgaoJulgadorCompetenciaList = new ArrayList<OrgaoJulgadorCompetencia>(0);
	private List<OrgaoJulgadorPessoaPerito> orgaoJulgadorPessoaPeritoList = new ArrayList<OrgaoJulgadorPessoaPerito>(0);
	private List<EstatisticaProcessoJusticaFederal> orgaoJulgadorEstatisticaProcessoJusticaFederalList = new ArrayList<EstatisticaProcessoJusticaFederal>(
			0);
	private List<Sala> salaList = new ArrayList<Sala>(0);
	private Integer numeroVara;
	private OrgaoJulgador ojRevisor;

	private List<OrgaoJulgadorCargo> orgaoJulgadorCargoList = new ArrayList<OrgaoJulgadorCargo>(0);
	private List<TempoAudienciaOrgaoJulgador> tempoAudienciaOrgaoJulgadorList = new ArrayList<TempoAudienciaOrgaoJulgador>(
			0);
	
	private String presuncaoCorreios;
	private Boolean postoAvancado;
	private List<OrgaoJulgador> varasAtendidas = new ArrayList<OrgaoJulgador>();
	private List<PrazoMinimoMarcacaoAudiencia> prazoMinimoMarcacaoAudienciaList = new ArrayList<PrazoMinimoMarcacaoAudiencia>(0);
	/**
	 * Variável que deve ser utilizada na clausula SQL ORDER BY quando se deseja uma ordenação por nome de órgão julgador.
	 */
	private String orgaoJulgadorOrdemAlfabetica;
	
	
	private Integer codigoCorporativo;
	
	@Id
	@GeneratedValue(generator = "gen_orgao_julgador")
	@Column(name = "id_orgao_julgador", unique = true, nullable = false)
	public int getIdOrgaoJulgador() {
		return this.idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(int idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}
		
	public OrgaoJulgador() {
	}
	
	@Column(name = "nr_presuncao_correios")
	public String getPresuncaoCorreios() {
		return this.presuncaoCorreios;
	}

	public void setPresuncaoCorreios(String presuncaoCorreios) {
		this.presuncaoCorreios = presuncaoCorreios;
	}

	@Column(name = "nr_vara")
	public Integer getNumeroVara() {
		return numeroVara;
	}

	public void setNumeroVara(Integer numeroVara) {
		this.numeroVara = numeroVara;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao", nullable = false)
	@NotNull
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name = "ds_orgao_julgador", length = 200)
	@Length(max = 200)
	public String getOrgaoJulgador() {
		return this.orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	@Column(name = "ds_orgao_julgador_ordem_alfabetica", length = 200)
	@Length(max = 200)
	public String getOrgaoJulgadorOrdemAlfabetica() {
		return this.orgaoJulgadorOrdemAlfabetica;
	}
	
	public void setOrgaoJulgadorOrdemAlfabetica(String orgaoJulgadorOrdemAlfabetica) {
		this.orgaoJulgadorOrdemAlfabetica = orgaoJulgadorOrdemAlfabetica;
	}	

	@Transient
	public String getOrgaoJulgadorComId(){
		return getOrgaoJulgador()+" ("+getIdOrgaoJulgador()+")";
	}

	@Column(name = "ds_ato_criacao", length = 100)
	@Length(max = 100)
	public String getAtoCriacao() {
		return this.atoCriacao;
	}

	public void setAtoCriacao(String atoCriacao) {
		this.atoCriacao = atoCriacao;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	@Column(name = "ds_sigla", length = 30, unique = true)
	@Length(max = 30)
	public String getSigla() {
		return sigla;
	}

	@Column(name = "ds_codigo_origem", length = 4, unique = true)
	@Length(max = 4)
	public String getCodigoOrigem() {
		return codigoOrigem;
	}

	public void setCodigoOrigem(String codigoOrigem) {
		this.codigoOrigem = codigoOrigem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicacao_classe")
	public AplicacaoClasse getAplicacaoClasse() {
		return this.aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	@Column(name = "nr_telefone", length = 15)
	@Length(max = 15)
	public String getNumeroTelefone() {
		return this.numeroTelefone;
	}

	public void setNumeroTelefone(String numeroTelefone) {
		this.numeroTelefone = numeroTelefone;
	}

	@Column(name = "nr_fax", length = 15)
	@Length(max = 15)
	public String getNumeroFax() {
		return this.numeroFax;
	}

	public void setNumeroFax(String numeroFax) {
		this.numeroFax = numeroFax;
	}

	@Column(name = "ds_email", length = 100)
	@Length(max = 100)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "nr_ddd_telefone", length = 2)
	@Length(max = 2)
	public String getDddTelefone() {
		return this.dddTelefone;
	}

	public void setDddTelefone(String dddTelefone) {
		this.dddTelefone = dddTelefone;
	}

	@Column(name = "nr_ddd_fax", length = 2)
	@Length(max = 2)
	public String getDddFax() {
		return this.dddFax;
	}

	public void setDddFax(String dddFax) {
		this.dddFax = dddFax;
	}

	@Column(name = "in_instancia", length = 1)
	@Length(max = 1)
	public String getInstancia() {
		return this.instancia;
	}
	
	@Transient
	public int getInstanciaAsInt(){
		if(this.instancia != null){
			return Integer.parseInt(this.instancia.trim());				
		} else {
			return 0;
		}
	}	

	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_novo_orgao_julgador", nullable = false)
	@NotNull
	public Boolean getNovoOrgaoJulgador() {
		return this.novoOrgaoJulgador;
	}

	public void setNovoOrgaoJulgador(Boolean novoOrgaoJulgador) {
		this.novoOrgaoJulgador = novoOrgaoJulgador;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<OrgaoJulgadorCompetencia> getOrgaoJulgadorCompetenciaList() {
		return this.orgaoJulgadorCompetenciaList;
	}

	public void setOrgaoJulgadorCompetenciaList(List<OrgaoJulgadorCompetencia> orgaoJulgadorCompetenciaList) {
		this.orgaoJulgadorCompetenciaList = orgaoJulgadorCompetenciaList;
	}

	@Override
	public String toString() {
		return orgaoJulgador;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao", nullable = false)
	public Date getDtCriacao() {
		return this.dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	@Column(name = "nr_tempo_audiencia")
	@Temporal(TemporalType.TIME)
	public Date getNumeroTempoAudiencia() {
		return this.numeroTempoAudiencia;
	}

	public void setNumeroTempoAudiencia(Date numeroTempoAudiencia) {
		this.numeroTempoAudiencia = numeroTempoAudiencia;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<OrgaoJulgadorPessoaPerito> getOrgaoJulgadorPessoaPeritoList() {
		return orgaoJulgadorPessoaPeritoList;
	}

	public void setOrgaoJulgadorPessoaPeritoList(List<OrgaoJulgadorPessoaPerito> orgaoJulgadorPessoaPeritoList) {
		this.orgaoJulgadorPessoaPeritoList = orgaoJulgadorPessoaPeritoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> getOrgaoJulgadorColegiadoOrgaoJulgadorList() {
		return this.orgaoJulgadorColegiadoOrgaoJulgadorList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<OrgaoJulgadorCargo> getOrgaoJulgadorCargoList() {
		return orgaoJulgadorCargoList;
	}

	public void setOrgaoJulgadorColegiadoOrgaoJulgadorList(
			List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadorColegiadoOrgaoJulgadorList) {
		this.orgaoJulgadorColegiadoOrgaoJulgadorList = orgaoJulgadorColegiadoOrgaoJulgadorList;
	}

	@OneToMany(mappedBy = "orgaoJulgador")
	public List<TempoAudienciaOrgaoJulgador> getTempoAudienciaOrgaoJulgadorList() {
		return tempoAudienciaOrgaoJulgadorList;
	}

	public void setTempoAudienciaOrgaoJulgadorList(List<TempoAudienciaOrgaoJulgador> tempoAudienciaOrgaoJulgadorList) {
		this.tempoAudienciaOrgaoJulgadorList = tempoAudienciaOrgaoJulgadorList;
	}

	public void setOrgaoJulgadorCargoList(List<OrgaoJulgadorCargo> orgaoJulgadorCargoList) {
		this.orgaoJulgadorCargoList = orgaoJulgadorCargoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<EstatisticaProcessoJusticaFederal> getOrgaoJulgadorEstatisticaProcessoJusticaFederalList() {
		return orgaoJulgadorEstatisticaProcessoJusticaFederalList;
	}

	public void setOrgaoJulgadorEstatisticaProcessoJusticaFederalList(
			List<EstatisticaProcessoJusticaFederal> orgaoJulgadorEstatisticaProcessoJusticaFederalList) {
		this.orgaoJulgadorEstatisticaProcessoJusticaFederalList = orgaoJulgadorEstatisticaProcessoJusticaFederalList;
	}

	@Transient
	public String getTempoAudiencia() {
		Date tempo = getNumeroTempoAudiencia();
		if (tempo != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(tempo);
			int horafinal = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
			return String.valueOf(horafinal);
		} else {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		} else if (obj instanceof OrgaoJulgador) {
			OrgaoJulgador oj = (OrgaoJulgador) obj;

			if (idOrgaoJulgador == 0 && orgaoJulgador != null) {
				return orgaoJulgador.equals(oj.getOrgaoJulgador());
			} else {
				return idOrgaoJulgador == oj.getIdOrgaoJulgador();
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgador();
		return result;
	}

	@Transient
	public boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
	}

	@Transient
	public OrgaoJulgador getOjRevisor() {
		return ojRevisor;
	}

	public void setOjRevisor(OrgaoJulgador ojRevisor) {
		this.ojRevisor = ojRevisor;
	}

	@Transient
	public List<OrgaoJulgadorCompetencia> getOrgaoJulgadorCompetenciaAtivoList() {
		Date dataAtual = DateUtil.getBeginningOfDay(new Date());
		List<OrgaoJulgadorCompetencia> orgaoJulgadorCompetenciaAtivoList = new ArrayList<OrgaoJulgadorCompetencia>(0);
		for (OrgaoJulgadorCompetencia ojc : this.orgaoJulgadorCompetenciaList) {
			if (ojc.getOrgaoJulgador().getAtivo() && DateUtil.isDataMenorIgual(ojc.getDataInicio(), dataAtual) && 
					(ojc.getDataFim() == null || DateUtil.isDataMaiorIgual(ojc.getDataFim(), dataAtual))) {
				
				orgaoJulgadorCompetenciaAtivoList.add(ojc);
			}
		}
		return orgaoJulgadorCompetenciaAtivoList;
	}

	@Transient
	public String getNumeroTelefoneFormatado() {
		StringBuilder sb = new StringBuilder();
		sb.append(dddTelefone == null || dddTelefone.isEmpty() ? "" : "(" + this.dddTelefone + ") ");
		sb.append(numeroTelefone == null || numeroTelefone.isEmpty() ? "" : this.numeroTelefone);
		return sb.toString();
	}

	public void setSalaList(List<Sala> salaList) {
		this.salaList = salaList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<Sala> getSalaList() {
		return salaList;
	}

	@Column(name = "in_posto_avancado")
	public Boolean getPostoAvancado() {
		return postoAvancado;
	}

	public void setPostoAvancado(Boolean postoAvancado) {
		this.postoAvancado = postoAvancado;
	}
	
	@OneToMany
	@JoinTable(name = "tb_posto_avancado", joinColumns = @JoinColumn(name = "id_oj_posto"), inverseJoinColumns = @JoinColumn(name = "id_oj_vara_atendida"))
	public List<OrgaoJulgador> getVarasAtendidas() {
		return varasAtendidas;
	}

	public void setVarasAtendidas(List<OrgaoJulgador> varasAtendidas) {
		this.varasAtendidas = varasAtendidas;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "orgaoJulgador")
	public List<PrazoMinimoMarcacaoAudiencia> getPrazoMinimoMarcacaoAudienciaList() {
		return prazoMinimoMarcacaoAudienciaList;
	}
	
	public void setPrazoMinimoMarcacaoAudienciaList(
			List<PrazoMinimoMarcacaoAudiencia> prazoMinimoMarcacaoAudienciaList) {
		this.prazoMinimoMarcacaoAudienciaList = prazoMinimoMarcacaoAudienciaList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgador> getEntityClass() {
		return OrgaoJulgador.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgador());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Column(name = "nr_codigo_corporativo")
	public Integer getCodigoCorporativo() {
		return codigoCorporativo;
	}

	public void setCodigoCorporativo(Integer codigoCorporativo) {
		this.codigoCorporativo = codigoCorporativo;
	}

}
