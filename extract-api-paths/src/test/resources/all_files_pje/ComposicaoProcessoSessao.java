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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.PessoaMagistrado;


@Entity
@Table(
		name = "tb_composicao_proc_sessao", 
		uniqueConstraints = @UniqueConstraint(columnNames =  {"id_composicao_sessao", "id_pauta_sessao"})
)
@org.hibernate.annotations.GenericGenerator(name = "gen_composicao_proc_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_composicao_proc_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComposicaoProcessoSessao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComposicaoProcessoSessao,Integer> {
    private static final long serialVersionUID = 1L;
    private Integer idComposicaoProcessoSessao;
    private ComposicaoSessao composicaoSessao;
    private PautaSessao pautaSessao;
    private PessoaMagistrado magistradoSubstituto;
    private Boolean presidente;
    private PessoaMagistrado magistradoRelator;

    @Id
    @GeneratedValue(generator = "gen_composicao_proc_sessao")
    @Column(name = "id_composicao_proc_sessao", unique = true, nullable = false)
    public Integer getIdComposicaoProcessoSessao() {
        return idComposicaoProcessoSessao;
    }

    public void setIdComposicaoProcessoSessao(
        Integer idComposicaoProcessoSessao) {
        this.idComposicaoProcessoSessao = idComposicaoProcessoSessao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_composicao_sessao", nullable = false)
    @NotNull
    public ComposicaoSessao getComposicaoSessao() {
        return composicaoSessao;
    }

    public void setComposicaoSessao(ComposicaoSessao composicaoSessao) {
        this.composicaoSessao = composicaoSessao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pauta_sessao", nullable = false)
    @NotNull
    public PautaSessao getPautaSessao() {
        return pautaSessao;
    }

    public void setPautaSessao(PautaSessao pautaSessao) {
        this.pautaSessao = pautaSessao;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_magistrado_substituto")
    public PessoaMagistrado getMagistradoSubstituto() {
        return magistradoSubstituto;
    }

    public void setMagistradoSubstituto(PessoaMagistrado magistradoSubstituto) {
        this.magistradoSubstituto = magistradoSubstituto;
    }

    @Column(name = "in_presidente", nullable = false)
    @NotNull
    public Boolean getPresidente() {
        return presidente;
    }

    public void setPresidente(Boolean presidente) {
        this.presidente = presidente;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_magistrado_relator")
    public PessoaMagistrado getMagistradoRelator() {
        return magistradoRelator;
    }

    public void setMagistradoRelator(PessoaMagistrado magistradoRelator) {
        this.magistradoRelator = magistradoRelator;
    }
    
	@Override
	@javax.persistence.Transient
	public Class<? extends ComposicaoProcessoSessao> getEntityClass() {
		return ComposicaoProcessoSessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdComposicaoProcessoSessao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
