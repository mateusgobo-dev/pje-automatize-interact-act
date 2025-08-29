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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.Papel;



@Entity
@Table(name = "tb_usu_local_mgtdo_servdor")
public class UsuarioLocalizacaoMagistradoServidor implements Serializable {
    private static final long serialVersionUID = 1L;
    private int idUsuarioLocalizacaoMagistradoServidor;
    private Date dtInicio;
    private Date dtFinal;
    private String norma;
    private UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
    private OrgaoJulgadorCargo orgaoJulgadorCargo;
    private OrgaoJulgadorColegiadoCargo orgaoJulgadorColegiadoCargo;
    private OrgaoJulgador orgaoJulgador;
    private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
    private Boolean magistradoTitular = Boolean.FALSE;
    private VinculacaoUsuario vinculacaoUsuario;
    private OrgaoJulgadorCargo orgaoJulgadorCargoVinculacao;
    private List<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> processoMagistradoList =
        new ArrayList<ProcessoTrfUsuarioLocalizacaoMagistradoServidor>();
    private List<UsuarioLocalizacaoVisibilidade> usuarioLocalizacaoVisibilidadeList =
        new ArrayList<UsuarioLocalizacaoVisibilidade>(0);

    public UsuarioLocalizacaoMagistradoServidor() {
    }

    @OneToMany(cascade =  {
        CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}
    , fetch = FetchType.LAZY, mappedBy = "usuarioLocalizacaoMagistradoServidor")
    public List<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> getProcessoMagistradoList() {
        return processoMagistradoList;
    }

    public void setProcessoMagistradoList(
        List<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> processoMagistradoList) {
        this.processoMagistradoList = processoMagistradoList;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "usuarioLocalizacaoMagistradoServidor")
    public List<UsuarioLocalizacaoVisibilidade> getUsuarioLocalizacaoVisibilidadeList() {
        return usuarioLocalizacaoVisibilidadeList;
    }

    public void setUsuarioLocalizacaoVisibilidadeList(
        List<UsuarioLocalizacaoVisibilidade> usuarioLocalizacaoVisibilidadeList) {
        this.usuarioLocalizacaoVisibilidadeList = usuarioLocalizacaoVisibilidadeList;
    }

    @Id
    @Column(name = "id_usu_local_mgstrado_servidor", unique = true, nullable = false, updatable = false)
    public int getIdUsuarioLocalizacaoMagistradoServidor() {
        return idUsuarioLocalizacaoMagistradoServidor;
    }

    public void setIdUsuarioLocalizacaoMagistradoServidor(
        int idUsuarioLocalizacaoMagistradoServidor) {
        this.idUsuarioLocalizacaoMagistradoServidor = idUsuarioLocalizacaoMagistradoServidor;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_usu_local_mgstrado_servidor")
    public UsuarioLocalizacao getUsuarioLocalizacao() {
        return usuarioLocalizacao;
    }

    public void setUsuarioLocalizacao(UsuarioLocalizacao usuarioLocalizacao) {
        this.usuarioLocalizacao = usuarioLocalizacao;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_inicio")
    public Date getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(Date dtInicio) {
        this.dtInicio = dtInicio;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt_final")
    public Date getDtFinal() {
        return dtFinal;
    }

    public void setDtFinal(Date dtFinal) {
        this.dtFinal = dtFinal;
    }

    @Column(name = "ds_norma", length = 200)
    @Length(max = 200)
    public String getNorma() {
        return norma;
    }

    public void setNorma(String norma) {
        this.norma = norma;
    }
    
    @ManyToOne
    @JoinColumn(name = "id_orgao_julgador_cargo_vinc")
    public OrgaoJulgadorCargo getOrgaoJulgadorCargoVinculacao() {
		return orgaoJulgadorCargoVinculacao;
	}

	public void setOrgaoJulgadorCargoVinculacao(
			OrgaoJulgadorCargo orgaoJulgadorCargoVinculacao) {
		this.orgaoJulgadorCargoVinculacao = orgaoJulgadorCargoVinculacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orgao_julgador_cargo")
    public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
        return orgaoJulgadorCargo;
    }

    public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
        this.orgaoJulgadorCargo = orgaoJulgadorCargo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        

        boolean possuiOjOuOjc = Boolean.FALSE;
        if (orgaoJulgadorColegiado != null) {
            sb.append(orgaoJulgadorColegiado).append(" / ");
            possuiOjOuOjc = Boolean.TRUE;
        }

        if (orgaoJulgador != null) {
            sb.append(orgaoJulgador).append(" / ");
            possuiOjOuOjc = Boolean.TRUE;
        }

        if (usuarioLocalizacao != null) {
            if(usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("administrador")) {
            	sb.append(" ");
            }
        	if(orgaoJulgadorCargo != null) {
        		sb.append(orgaoJulgadorCargo.getDescricao());
        	}
        	else {
        		if(!possuiOjOuOjc) {
        			sb.append(usuarioLocalizacao.getLocalizacaoFisica()).append(" / ");
        		}
        		if(usuarioLocalizacao.getLocalizacaoModelo() != null) {
        			sb.append(usuarioLocalizacao.getLocalizacaoModelo()).append(" / ");
        		}
        		sb.append(usuarioLocalizacao.getPapel());
        	}
            return sb.toString();
        } else {
            return super.toString();
        }
    }
    
    @Transient
    public String getInformacoesAssentamentoMagistrado(){
        StringBuilder sb = new StringBuilder();

        if (orgaoJulgadorColegiado != null) {
            sb.append(orgaoJulgadorColegiado).append(" / ");
        }
        
        if (usuarioLocalizacao != null) {
        	sb.append(usuarioLocalizacao.getUsuario().getNome()).append(" / ");
        }
        
        if (orgaoJulgadorCargo != null){
        	sb.append(orgaoJulgadorCargo.getDescricao());
        }	
        
        return sb.toString();        
    }
    
    @Transient
    public String getInformacoesAssentamentoMagistradoComData(){    	    	    
    	StringBuilder sb = new StringBuilder(getInformacoesAssentamentoMagistrado());
    	if (dtInicio != null) {
    		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("pt", "BR"));
    		sb.append(" / (").append(dateFormat.format(dtInicio));
    		if (dtFinal != null) {
    			sb.append(" - ").append(dateFormat.format(dtFinal));
    		}
    		sb.append(")");
    	}    	
    	return sb.toString();
    }

    @ManyToOne
    @JoinColumn(name = "id_orgao_julgador")
    public OrgaoJulgador getOrgaoJulgador() {
        return orgaoJulgador;
    }

    public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
        this.orgaoJulgador = orgaoJulgador;
    }

    @ManyToOne
    @JoinColumn(name = "id_orgao_julgador_colegiado")
    public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
        return orgaoJulgadorColegiado;
    }

    public void setOrgaoJulgadorColegiado(
        OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
        this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
    }

    @Transient
    public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
        if (usuarioLocalizacao == null) {
            usuarioLocalizacao = new UsuarioLocalizacao();
        }

        usuarioLocalizacao.setLocalizacaoFisica(localizacaoFisica);
    }

    @Transient
    public Localizacao getLocalizacaoFisica() {
        return (usuarioLocalizacao != null)
        ? usuarioLocalizacao.getLocalizacaoFisica() : null;
    }

    @Transient
    public void setPapel(Papel papel) {
        if (usuarioLocalizacao == null) {
            usuarioLocalizacao = new UsuarioLocalizacao();
        }

        usuarioLocalizacao.setPapel(papel);
    }

    @Transient
    public Papel getPapel() {
        return (usuarioLocalizacao != null)
        ? usuarioLocalizacao.getPapel() : null;
    }

    @Transient
    public void setLocalizacaoModelo(Localizacao localizacaoModelo) {
        if (usuarioLocalizacao == null) {
            usuarioLocalizacao = new UsuarioLocalizacao();
        }

        usuarioLocalizacao.setLocalizacaoModelo(localizacaoModelo);
    }

    @Transient
    public Localizacao getLocalizacaoModelo() {
        return (usuarioLocalizacao != null)
        ? usuarioLocalizacao.getLocalizacaoModelo() : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ojc_cargo")
    public OrgaoJulgadorColegiadoCargo getOrgaoJulgadorColegiadoCargo() {
        return orgaoJulgadorColegiadoCargo;
    }

    public void setOrgaoJulgadorColegiadoCargo(
        OrgaoJulgadorColegiadoCargo orgaoJulgadorColegiadoCargo) {
        this.orgaoJulgadorColegiadoCargo = orgaoJulgadorColegiadoCargo;
    }

    @Column(name = "in_magistrado_titular", nullable = false)
    @NotNull
    public Boolean getMagistradoTitular() {
        return magistradoTitular;
    }

    public void setMagistradoTitular(Boolean magistradoTitular) {
        this.magistradoTitular = magistradoTitular;
    }

    /**
     * Recupera a vinculação de usuário que originou essa lotação automática.
     * @return null caso a lotação não tenha sido originada por uma lotação automática baseada em vinculação de usuaŕios.
     * Caso contrário, retorna a vinculação de usuários que originou tal lotação.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vinculacao_usuario")
    public VinculacaoUsuario getVinculacaoUsuario() {
        return vinculacaoUsuario;
    }

    /**
     * Atribui a vinculação de usuário que gerou essa lotação.
     * @param vinculacaoUsuario vinculação de usuário a ser atribuída
     */
    public void setVinculacaoUsuario(VinculacaoUsuario vinculacaoUsuario) {
        this.vinculacaoUsuario = vinculacaoUsuario;
    }    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof UsuarioLocalizacaoMagistradoServidor)) {
            return false;
        }

        UsuarioLocalizacaoMagistradoServidor other = (UsuarioLocalizacaoMagistradoServidor) obj;

        if (getIdUsuarioLocalizacaoMagistradoServidor() != other.getIdUsuarioLocalizacaoMagistradoServidor()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) +
            getIdUsuarioLocalizacaoMagistradoServidor();

        return result;
    }
}
