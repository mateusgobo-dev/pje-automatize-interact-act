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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.jt.enums.ResultadoVotacaoEnum;
import br.jus.pje.jt.enums.SituacaoAnaliseEnum;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;


@Entity
@Table(name = PautaSessao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pauta_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pauta_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PautaSessao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PautaSessao,Integer> {
    public static final String TABLE_NAME = "tb_pauta_sessao";
    private static final long serialVersionUID = 1L;
    private int idPautaSessao;
    private SessaoJT sessao;
    private ProcessoTrf processoTrf;
    private OrgaoJulgador orgaoJulgadorRevisor;
    private OrgaoJulgador orgaoJulgadorDeliberacao;
    private OrgaoJulgador orgaoJulgadorRedator;
    private TipoInclusaoEnum tipoInclusao;
    private Boolean sustentacaoOral;
    private Date dataPedidoSustentacaoOral;
    private String advogadoPedidoSustentacaoOral;
    private Boolean preferencia;
    private ResultadoVotacaoEnum resultadoVotacao;
    private TipoSituacaoPauta tipoSituacaoPauta;
    private Date dataSituacaoPauta;
    private Usuario usuarioSituacaoPauta;
    private SituacaoAnaliseEnum situacaoAnalise;
    private Date dataSituacaoAnalise;
    private Usuario usuarioSituacaoAnalise;
    private PessoaMagistrado magistradoRedator;
    private boolean checkBoxSelecionado;
    private boolean checkBoxDisabled;

    public PautaSessao() {
    	checkBoxDisabled = false;
    }

    @Id
    @GeneratedValue(generator = "gen_pauta_sessao")
    @Column(name = "id_pauta_sessao", unique = true, nullable = false)
    public int getIdPautaSessao() {
        return this.idPautaSessao;
    }

    public void setIdPautaSessao(int idPautaSessao) {
        this.idPautaSessao = idPautaSessao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessao", nullable = false)
    @NotNull
    public SessaoJT getSessao() {
        return sessao;
    }

    public void setSessao(SessaoJT sessao) {
        this.sessao = sessao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_processo_trf", nullable = false)
    @NotNull
    public ProcessoTrf getProcessoTrf() {
        return processoTrf;
    }

    public void setProcessoTrf(ProcessoTrf processoTrf) {
        this.processoTrf = processoTrf;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orgao_julgador_revisor")
    public OrgaoJulgador getOrgaoJulgadorRevisor() {
        return orgaoJulgadorRevisor;
    }

    public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
        this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orgao_julgador_deliberacao")
    public OrgaoJulgador getOrgaoJulgadorDeliberacao() {
        return orgaoJulgadorDeliberacao;
    }

    public void setOrgaoJulgadorDeliberacao(
        OrgaoJulgador orgaoJulgadorDeliberacao) {
        this.orgaoJulgadorDeliberacao = orgaoJulgadorDeliberacao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orgao_julgador_redator", nullable = false)
    @NotNull
    public OrgaoJulgador getOrgaoJulgadorRedator() {
        return orgaoJulgadorRedator;
    }

    public void setOrgaoJulgadorRedator(OrgaoJulgador orgaoJulgadorRedator) {
        this.orgaoJulgadorRedator = orgaoJulgadorRedator;
    }

    @Column(name = "in_tipo_inclusao", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    public TipoInclusaoEnum getTipoInclusao() {
        return tipoInclusao;
    }

    public void setTipoInclusao(TipoInclusaoEnum tipoInclusao) {
        this.tipoInclusao = tipoInclusao;
    }

    @Column(name = "in_sustentacao_oral", nullable = false)
    @NotNull
    public Boolean getSustentacaoOral() {
        return sustentacaoOral;
    }

    public void setSustentacaoOral(Boolean sustentacaoOral) {
        this.sustentacaoOral = sustentacaoOral;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_pedido_sustentacao_oral")
    public Date getDataPedidoSustentacaoOral() {
        return dataPedidoSustentacaoOral;
    }

    public void setDataPedidoSustentacaoOral(Date dataPedidoSustentacaoOral) {
        this.dataPedidoSustentacaoOral = dataPedidoSustentacaoOral;
    }

    @Column(name = "ds_adv_pedido_sustentacao_oral")
    public String getAdvogadoPedidoSustentacaoOral() {
        return advogadoPedidoSustentacaoOral;
    }

    public void setAdvogadoPedidoSustentacaoOral(
        String advogadoPedidoSustentacaoOral) {
        this.advogadoPedidoSustentacaoOral = advogadoPedidoSustentacaoOral;
    }

    @Column(name = "in_preferencia", nullable = false)
    @NotNull
    public Boolean getPreferencia() {
        return preferencia;
    }

    public void setPreferencia(Boolean preferencia) {
        this.preferencia = preferencia;
    }

    @Column(name = "in_resultado_votacao")
    @Enumerated(EnumType.STRING)
    public ResultadoVotacaoEnum getResultadoVotacao() {
        return resultadoVotacao;
    }

    public void setResultadoVotacao(ResultadoVotacaoEnum resultadoVotacao) {
        this.resultadoVotacao = resultadoVotacao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_situacao_pauta", nullable = false)
    @NotNull
    public TipoSituacaoPauta getTipoSituacaoPauta() {
        return tipoSituacaoPauta;
    }

    public void setTipoSituacaoPauta(TipoSituacaoPauta tipoSituacaoPauta) {
        this.tipoSituacaoPauta = tipoSituacaoPauta;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_situacao_pauta", nullable = false)
    @NotNull
    public Date getDataSituacaoPauta() {
        return dataSituacaoPauta;
    }

    public void setDataSituacaoPauta(Date dataSituacaoPauta) {
        this.dataSituacaoPauta = dataSituacaoPauta;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_situacao_pauta", nullable = false)
    @NotNull
    public Usuario getUsuarioSituacaoPauta() {
        return usuarioSituacaoPauta;
    }

    public void setUsuarioSituacaoPauta(Usuario usuarioSituacaoPauta) {
        this.usuarioSituacaoPauta = usuarioSituacaoPauta;
    }

    @Column(name = "in_situacao_analise", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    public SituacaoAnaliseEnum getSituacaoAnalise() {
        return situacaoAnalise;
    }

    public void setSituacaoAnalise(SituacaoAnaliseEnum situacaoAnalise) {
        this.situacaoAnalise = situacaoAnalise;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_situacao_analise")
    public Date getDataSituacaoAnalise() {
        return dataSituacaoAnalise;
    }

    public void setDataSituacaoAnalise(Date dataSituacaoAnalise) {
        this.dataSituacaoAnalise = dataSituacaoAnalise;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_situacao_analise")
    public Usuario getUsuarioSituacaoAnalise() {
        return usuarioSituacaoAnalise;
    }

    public void setUsuarioSituacaoAnalise(Usuario usuarioSituacaoAnalise) {
        this.usuarioSituacaoAnalise = usuarioSituacaoAnalise;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_magistrado_redator")
    public PessoaMagistrado getMagistradoRedator() {
        return magistradoRedator;
    }

    public void setMagistradoRedator(PessoaMagistrado magistradoRedator) {
        this.magistradoRedator = magistradoRedator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + getIdPautaSessao();

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

        if (!(obj instanceof PautaSessao)) {
            return false;
        }

        PautaSessao other = (PautaSessao) obj;

        if (getIdPautaSessao() != other.getIdPautaSessao()) {
            return false;
        }

        return true;
    }

    @Transient
    public boolean isCheckBoxSelecionado() {
        return checkBoxSelecionado;
    }

    public void setCheckBoxSelecionado(boolean checkBoxSelecionado) {
        this.checkBoxSelecionado = checkBoxSelecionado;
    }
    
    @Transient
    public boolean isCheckBoxDisabled() {
    	return checkBoxDisabled;
    }

    public void setCheckBoxDisabled(boolean checkBoxDisabled) {
        this.checkBoxDisabled = checkBoxDisabled;
    }
    
	@Override
	@javax.persistence.Transient
	public Class<? extends PautaSessao> getEntityClass() {
		return PautaSessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPautaSessao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
