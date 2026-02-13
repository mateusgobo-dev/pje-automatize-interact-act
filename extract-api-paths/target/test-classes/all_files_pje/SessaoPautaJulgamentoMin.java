package br.jus.pje.nucleo.entidades.min;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Entity
@Immutable
@Table(name = SessaoPautaJulgamentoMin.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name = "gen_sessao_pauta_proc_trf", sequenceName = "sq_tb_sessao_pauta_proc_trf")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class SessaoPautaJulgamentoMin implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_sessao_pauta_proc_trf";

    @Id
    @GeneratedValue(generator = "gen_sessao_pauta_proc_trf")
    @Column(name = "id_sessao_pauta_processo_trf", unique = true, nullable = false)
    private Integer id;

    @JsonManagedReference(value="pautas-julgamento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessao", nullable = false)
    private SessaoJulgamentoMin sessaoJulgamento;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_processo_trf", nullable = false)
    private ProcessoTrf processoJudicial;

    public SessaoPautaJulgamentoMin() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SessaoJulgamentoMin getSessaoJulgamento() {
        return sessaoJulgamento;
    }

    public void setSessaoJulgamento(SessaoJulgamentoMin sessaoJulgamento) {
        this.sessaoJulgamento = sessaoJulgamento;
    }
    
    public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}
    
    public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
	}

}

