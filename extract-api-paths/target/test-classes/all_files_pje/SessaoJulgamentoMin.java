package br.jus.pje.nucleo.entidades.min;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import br.jus.pje.nucleo.entidades.TipoSessao;

@Entity
@Table(name = SessaoJulgamentoMin.TABLE_NAME)
@Immutable
@SequenceGenerator(allocationSize = 1, name = "gen_sessao", sequenceName = "sq_tb_sessao")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class SessaoJulgamentoMin implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_sessao";

    @Id
    @GeneratedValue(generator = "gen_sessao")
    @Column(name = "id_sessao", unique = true, nullable = false)	
    private Integer id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_sessao")    
    private TipoSessao tipoSessao;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
    @NotNull    
    private OrgaoJulgadorColegiadoMin orgaoJulgadorColegiado;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_sessao", nullable = false)
    @NotNull    
    private Date dataSessao;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_exclusao")    
    private Date dataExclusao;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_abertura_sessao")    
    private Date dataAberturaSessao;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_fechamento_pauta")    
    private Date dataFechamentoPauta;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_fechamento_sessao")    
    private Date dataFechamentoSessao;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_realizacao_sessao")    
    private Date dataRealizacaoSessao;
    
    @Column(name = "ds_apelido", length = 200)    
    private String apelido;
    
    @Column(name = "in_continua", nullable = false)
    @NotNull    
    private Boolean continua = Boolean.FALSE;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_fim_sessao")    
    private Date dataFimSessao;
    
    @JsonBackReference(value="pautas-julgamento")
    @OneToMany(mappedBy = "sessaoJulgamento", fetch = FetchType.LAZY)
    private List<SessaoPautaJulgamentoMin> pautasJulgamento;

    public SessaoJulgamentoMin() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TipoSessao getTipoSessao() {
        return this.tipoSessao;
    }

    public void setTipoSessao(TipoSessao tipoSessao) {
        this.tipoSessao = tipoSessao;
    }

    public OrgaoJulgadorColegiadoMin getOrgaoJulgadorColegiado() {
        return this.orgaoJulgadorColegiado;
    }

    public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiadoMin orgaoJulgadorColegiado) {
        this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
    }

    public Date getDataSessao() {
        return dataSessao;
    }

    public void setDataSessao(Date dataSessao) {
        this.dataSessao = dataSessao;
    }

    public Date getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }


    public Date getDataAberturaSessao() {
        return dataAberturaSessao;
    }

    public void setDataAberturaSessao(Date dataAberturaSessao) {
        this.dataAberturaSessao = dataAberturaSessao;
    }

    public Date getDataFechamentoPauta() {
        return dataFechamentoPauta;
    }

    public void setDataFechamentoPauta(Date dataFechamentoPauta) {
        this.dataFechamentoPauta = dataFechamentoPauta;
    }

    public Date getDataFechamentoSessao() {
        return dataFechamentoSessao;
    }

    public void setDataFechamentoSessao(Date dataFechamentoSessao) {
        this.dataFechamentoSessao = dataFechamentoSessao;
    }

    public Date getDataRealizacaoSessao() {
        return dataRealizacaoSessao;
    }

    public void setDataRealizacaoSessao(Date dataRealizacaoSessao) {
        this.dataRealizacaoSessao = dataRealizacaoSessao;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public Boolean getContinua() {
        return continua;
    }

    public void setContinua(Boolean continua) {
        this.continua = continua;
    }

    public Date getDataFimSessao() {
        return dataFimSessao;
    }

    public void setDataFimSessao(Date dataFimSessao) {
        this.dataFimSessao = dataFimSessao;
    }

    public List<SessaoPautaJulgamentoMin> getPautasJulgamento() {
        return pautasJulgamento;
    }

    public void setPautasJulgamento(List<SessaoPautaJulgamentoMin> pautasJulgamento) {
        this.pautasJulgamento = pautasJulgamento;
    }
    
}