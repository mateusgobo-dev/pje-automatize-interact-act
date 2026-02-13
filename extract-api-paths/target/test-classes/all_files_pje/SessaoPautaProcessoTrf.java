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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.JulgamentoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = SessaoPautaProcessoTrf.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_sessao_pauta_proc_trf", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao_pauta_proc_trf"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoPautaProcessoTrf implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoPautaProcessoTrf,Integer> {

	private static final long serialVersionUID = 6070524554958499476L;

	public static final String TABLE_NAME = "tb_sessao_pauta_proc_trf";

	private Integer idSessaoPautaProcessoTrf;
	private Sessao sessao;
	private ProcessoTrf processoTrf;
	private ConsultaProcessoTrfSemFiltro consultaProcessoTrf;
	private Usuario usuarioInclusao;
	private Usuario usuarioExclusao;
	private Date dataInclusaoProcessoTrf;
	private Date dataExclusaoProcessoTrf;
	private TipoInclusaoEnum tipoInclusao;
	private AdiadoVistaEnum adiadoVista;
	private Boolean sustentacaoOral = Boolean.FALSE;
	private Boolean preferencia = Boolean.FALSE;
	private Boolean retiradaJulgamento = Boolean.FALSE;
	private TipoSituacaoPautaEnum situacaoJulgamento = TipoSituacaoPautaEnum.AJ;
	private String proclamacaoDecisao;
	private String advogadoSustentacaoOral;
	private OrgaoJulgador orgaoJulgadorUsuarioInclusao;
	private Boolean check;	
	private OrgaoJulgador orgaoJulgadorRelator;
	private OrgaoJulgador orgaoJulgadorVencedor;
	private OrgaoJulgador orgaoJulgadorPedidoVista;
	private OrgaoJulgadorCargo orgaoJulgadorCargoPedidoVista;
	private OrgaoJulgador orgaoJulgadorRetiradaJulgamento;
	private Integer numeroOrdem;
	private Boolean intimavel;
	private Boolean maioriaDetectada;
	private List<SessaoPautaProcessoComposicao> sessaoPautaProcessoComposicaoList = new ArrayList<SessaoPautaProcessoComposicao>(); 
	private PessoaMagistrado presidente;
	private boolean julgamentoFinalizado = Boolean.FALSE;
	private JulgamentoEnum julgamentoEnum = JulgamentoEnum.M;
	
	public SessaoPautaProcessoTrf() {}

	@Id
	@GeneratedValue(generator = "gen_sessao_pauta_proc_trf")
	@Column(name = "id_sessao_pauta_processo_trf", unique = true, nullable = false)
	public Integer getIdSessaoPautaProcessoTrf() {
		return this.idSessaoPautaProcessoTrf;
	}

	public void setIdSessaoPautaProcessoTrf(Integer idSessaoPautaProcessoTrf) {
		this.idSessaoPautaProcessoTrf = idSessaoPautaProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_sessao", nullable = false)
	@NotNull
	public Sessao getSessao() {
		return this.sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false,insertable=false,updatable=false)
	public ConsultaProcessoTrfSemFiltro getConsultaProcessoTrf() {
		return this.consultaProcessoTrf;
	}

	public void setConsultaProcessoTrf(ConsultaProcessoTrfSemFiltro consultaProcessoTrf) {
		this.consultaProcessoTrf = consultaProcessoTrf;
	}
	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_inclusao", nullable = false)
	@NotNull
	public Usuario getUsuarioInclusao() {
		return this.usuarioInclusao;
	}

	public void setUsuarioInclusao(Usuario usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_exclusao", nullable = true)
	public Usuario getUsuarioExclusao() {
		return this.usuarioExclusao;
	}

	public void setUsuarioExclusao(Usuario usuarioExclusao) {
		this.usuarioExclusao = usuarioExclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao_processo", nullable = false)
	@NotNull
	public Date getDataInclusaoProcessoTrf() {
		return this.dataInclusaoProcessoTrf;
	}

	public void setDataInclusaoProcessoTrf(Date dataInclusaoProcessoTrf) {
		this.dataInclusaoProcessoTrf = dataInclusaoProcessoTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao_processo", nullable = true)
	public Date getDataExclusaoProcessoTrf() {
		return this.dataExclusaoProcessoTrf;
	}

	public void setDataExclusaoProcessoTrf(Date dataExclusaoProcessoTrf) {
		this.dataExclusaoProcessoTrf = dataExclusaoProcessoTrf;
	}

	@Column(name = "tp_inclusao", length = 2, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoInclusaoEnum getTipoInclusao() {
		return this.tipoInclusao;
	}

	public void setTipoInclusao(TipoInclusaoEnum tipoInclusao) {
		this.tipoInclusao = tipoInclusao;
	}

	@Column(name = "in_adiado_vista", length = 2)
	@Enumerated(EnumType.STRING)
	public AdiadoVistaEnum getAdiadoVista() {
		return this.adiadoVista;
	}

	public void setAdiadoVista(AdiadoVistaEnum adiadoVista) {
		this.adiadoVista = adiadoVista;
	}

	@Column(name = "in_sustentacao_oral", nullable = false)
	@NotNull
	public Boolean getSustentacaoOral() {
		return sustentacaoOral;
	}

	public void setSustentacaoOral(Boolean sustentacaoOral) {
		this.sustentacaoOral = sustentacaoOral;
	}

	@Column(name = "in_preferencia", nullable = false)
	@NotNull
	public Boolean getPreferencia() {
		return preferencia;
	}

	public void setPreferencia(Boolean preferencia) {
		this.preferencia = preferencia;
	}

	@Column(name = "in_retirado_julgamento", nullable = false)
	@NotNull
	public Boolean getRetiradaJulgamento() {
		return retiradaJulgamento;
	}

	public void setRetiradaJulgamento(Boolean retiradaJulgamento) {
		this.retiradaJulgamento = retiradaJulgamento;
	}

	@Column(name = "tp_situacao_julgamento", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoSituacaoPautaEnum getSituacaoJulgamento() {
		return situacaoJulgamento;
	}

	public void setSituacaoJulgamento(TipoSituacaoPautaEnum situacaoJulgamento) {
		this.situacaoJulgamento = situacaoJulgamento;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_proclamacao_decisao")
	public String getProclamacaoDecisao() {
		return proclamacaoDecisao;
	}
	
	@Transient
	public String getProclamacaoDecisaoFormatada() {
		String retorno = "";
		if(getProclamacaoDecisao() != null) {
			retorno = StringUtil.replace(getProclamacaoDecisao(), "\n","<br/>");
		}
		return retorno;
	}

	public void setProclamacaoDecisao(String proclamacaoDecisao) {
		this.proclamacaoDecisao = proclamacaoDecisao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_relator")
	public OrgaoJulgador getOrgaoJulgadorRelator() {
		return orgaoJulgadorRelator;
	}

	public void setOrgaoJulgadorRelator(OrgaoJulgador orgaoJulgadorRelator) {
		this.orgaoJulgadorRelator = orgaoJulgadorRelator;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_vencedor")
	public OrgaoJulgador getOrgaoJulgadorVencedor() {
		return orgaoJulgadorVencedor;
	}

	public void setOrgaoJulgadorVencedor(OrgaoJulgador orgaoJulgadorVencedor) {
		this.orgaoJulgadorVencedor = orgaoJulgadorVencedor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_retirada_julg")
	public OrgaoJulgador getOrgaoJulgadorRetiradaJulgamento() {
		return orgaoJulgadorRetiradaJulgamento;
	}

	public void setOrgaoJulgadorRetiradaJulgamento(OrgaoJulgador orgaoJulgadorRetiradaJulgamento) {
		this.orgaoJulgadorRetiradaJulgamento = orgaoJulgadorRetiradaJulgamento;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_pv")
	public OrgaoJulgador getOrgaoJulgadorPedidoVista() {
		return orgaoJulgadorPedidoVista;
	}

	public void setOrgaoJulgadorPedidoVista(OrgaoJulgador orgaoJulgadorPedidoVista) {
		this.orgaoJulgadorPedidoVista = orgaoJulgadorPedidoVista;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_cargo_pv")
	public OrgaoJulgadorCargo getOrgaoJulgadorCargoPedidoVista() {
		return orgaoJulgadorCargoPedidoVista;
	}
	
	public void setOrgaoJulgadorCargoPedidoVista(OrgaoJulgadorCargo orgaoJulgadorCargoPedidoVista) {
		this.orgaoJulgadorCargoPedidoVista = orgaoJulgadorCargoPedidoVista;
	}

	@Column(name = "ds_adv_sustentacao_oral", length = 500)
	@Length(max = 500)
	public String getAdvogadoSustentacaoOral() {
		return advogadoSustentacaoOral;
	}

	public void setAdvogadoSustentacaoOral(String advogadoSustentacaoOral) {
		this.advogadoSustentacaoOral = advogadoSustentacaoOral;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_usuario_inclusao")
	public OrgaoJulgador getOrgaoJulgadorUsuarioInclusao() {
		return orgaoJulgadorUsuarioInclusao;
	}

	public void setOrgaoJulgadorUsuarioInclusao(OrgaoJulgador orgaoJulgadorUsuarioInclusao) {
		this.orgaoJulgadorUsuarioInclusao = orgaoJulgadorUsuarioInclusao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SessaoPautaProcessoTrf)) {
			return false;
		}
		SessaoPautaProcessoTrf other = (SessaoPautaProcessoTrf) obj;
		if (getIdSessaoPautaProcessoTrf() != other.getIdSessaoPautaProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSessaoPautaProcessoTrf();
		return result;
	}

	@Column(name = "nr_ordem")
	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}

	@Column(name = "in_permite_intimacao")
	public Boolean getIntimavel() {
		return intimavel;
	}

	public void setIntimavel(Boolean intimavel) {
		this.intimavel = intimavel;
	}

	@Column(name = "in_maioria")
	public Boolean getMaioriaDetectada() {
		return maioriaDetectada;
	}

	public void setMaioriaDetectada(Boolean maioriaDetectada) {
		this.maioriaDetectada = maioriaDetectada;
	}
	
	@OneToMany(mappedBy="sessaoPautaProcessoTrf", cascade=CascadeType.ALL, orphanRemoval=true)
	public List<SessaoPautaProcessoComposicao> getSessaoPautaProcessoComposicaoList() {
		return sessaoPautaProcessoComposicaoList;
	}

	public void setSessaoPautaProcessoComposicaoList(List<SessaoPautaProcessoComposicao> sessaoPautaProcessoComposicaoList) {
		this.sessaoPautaProcessoComposicaoList = sessaoPautaProcessoComposicaoList;
	}

	@Transient
	public List<SessaoPautaProcessoComposicao> getComposicoesVotantes() {
		List<SessaoPautaProcessoComposicao> composicoesVotantes = new ArrayList<SessaoPautaProcessoComposicao>();
		for (SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao : getSessaoPautaProcessoComposicaoList()) {
			if (sessaoPautaProcessoComposicao.getPresente()) {
				composicoesVotantes.add(sessaoPautaProcessoComposicao);
			}
		}
		return composicoesVotantes;
	}
	
	@Transient
	public List<OrgaoJulgador> getOrgaosJulgadoresVotantes() {
		List<OrgaoJulgador> orgaosJulgadoresVotantes = new ArrayList<OrgaoJulgador>();
		for (SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao : getComposicoesVotantes()) {
			orgaosJulgadoresVotantes.add(sessaoPautaProcessoComposicao.getOrgaoJulgador());
		}
		return orgaosJulgadoresVotantes;
	}
	
	public boolean getParticipaVotacao(OrgaoJulgador orgaoJulgador) {
		boolean retorno = false;
		for (SessaoPautaProcessoComposicao sppc : getComposicoesVotantes()) {
			if (sppc.getOrgaoJulgador().equals(orgaoJulgador)) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}
	
	/**
	 * Verifica se existe um magistrado vencedor, diferente do originário
	 * @return
	 */
	@Transient
	public Boolean isVencedorDiverso() {
		return !getProcessoTrf().getOrgaoJulgador().equals(getOrgaoJulgadorVencedor());
	}	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_presidente")
	public PessoaMagistrado getPresidente() {
		return presidente;
	}

	public void setPresidente(PessoaMagistrado presidente) {
		this.presidente = presidente;
	}

	@Column(name = "in_julgamento")
	@Enumerated(EnumType.STRING)
	public JulgamentoEnum getJulgamentoEnum() {
		return julgamentoEnum;
	}

	public void setJulgamentoEnum(JulgamentoEnum julgamentoEnum) {
		this.julgamentoEnum = julgamentoEnum;
	}

	/**
	 * Método responsável por verificar se o processo está pendente de julgamento
	 * @return Retorna "true" caso o processo esteja pendente de julgamento e "false" caso contrário
	 */
	@Transient
	public Boolean isProcessoPendenteJulgamento() {
		return TipoSituacaoPautaEnum.AJ.equals(getSituacaoJulgamento()) || TipoSituacaoPautaEnum.EJ.equals(getSituacaoJulgamento());
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoPautaProcessoTrf> getEntityClass() {
		return SessaoPautaProcessoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessaoPautaProcessoTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(300)
			.append("SessaoProcessoDocumento(#").append(getIdSessaoPautaProcessoTrf()).append(' ').append(getTipoInclusao());
		
		if (getOrgaoJulgadorRelator()!=null) {
			sb.append(", ").append(getOrgaoJulgadorRelator().getIdOrgaoJulgador()).append('=').append(getOrgaoJulgadorRelator().getOrgaoJulgador());
		}
		
		if (getSessao()!=null) {
			sb.append(", ").append(getSessao());
		}
		
		if (getProcessoTrf()!=null) {
			sb.append(", ").append(getProcessoTrf().getIdProcessoTrf()).append('=').append(getProcessoTrf().getNumeroProcesso());
		}
		
		sb.append(')');
		return sb.toString();
	}
	
	@Column(name = "in_julgamento_finalizado", nullable = false)
	@NotNull
	public boolean isJulgamentoFinalizado() {
		return julgamentoFinalizado;
	}

	public void setJulgamentoFinalizado(boolean julgamentoFinalizado) {
		this.julgamentoFinalizado = julgamentoFinalizado;
	}
}
