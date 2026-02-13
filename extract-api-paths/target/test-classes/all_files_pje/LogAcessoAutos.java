package br.jus.pje.nucleo.entidades.log;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.sql.Timestamp;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import br.jus.pje.nucleo.entidades.IEntidade;

@Ignore
@Entity
@Table(name = "tb_log_acesso_autos")
@javax.persistence.Cacheable(false)
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class LogAcessoAutos implements IEntidade<LogAcessoAutos, Long> {

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
	    @Parameter(name = "sequence", value = "sq_tb_log_acesso_autos"), 
        @Parameter(name = "allocationSize", value = "-1")})
	@GeneratedValue(generator = "generator", strategy = GenerationType.AUTO)
	@Column(name = "id_log_acesso_autos", unique = true, nullable = false, updatable = false)
    private Long idLogAcessoAutos;
    
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_usuario_localizacao", nullable = false)
    private Long idUsuarioLocalizacao;

    @Column(name = "id_processo", nullable = false)
    private Long idProcesso;

    @Column(name = "ip", nullable = false, length = 64)
    private String ip;

    @Column(name = "sigiloso", nullable = false)
    private Boolean sigiloso;

    @Column(name = "nivel_acesso", nullable = false)
    private Integer nivelAcesso;

    @Column(name = "tipo_acesso", nullable = false, length = 3)
    private String tipoAcesso = "WEB";

    @Column(name = "hora", nullable = false)
    private Timestamp hora = new Timestamp(System.currentTimeMillis());

    // Getters and Setters
    public Long getIdLogAcessoAutos() {
        return idLogAcessoAutos;
    }

    public void setIdLogAcessoAutos(Long idLogAcessoAutos) {
        this.idLogAcessoAutos = idLogAcessoAutos;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdUsuarioLocalizacao() {
        return idUsuarioLocalizacao;
    }

    public void setIdUsuarioLocalizacao(Long idUsuarioLocalizacao) {
        this.idUsuarioLocalizacao = idUsuarioLocalizacao;
    }

    public Long getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(Long idProcesso) {
        this.idProcesso = idProcesso;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getSigiloso() {
        return sigiloso;
    }

    public void setSigiloso(Boolean sigiloso) {
        this.sigiloso = sigiloso;
    }

    public Integer getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(Integer nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public String getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(String tipoAcesso) {
        this.tipoAcesso = (tipoAcesso != null) ? tipoAcesso : "WEB";
    }

    public Timestamp getHora() {
        return hora;
    }

    public void setHora(Timestamp hora) {
        this.hora = hora;
    }

	@Override
	@Transient
	public Class<LogAcessoAutos> getEntityClass() {
		return LogAcessoAutos.class;
	}

	@Override
	@Transient
	public Long getEntityIdObject() {
		return getIdLogAcessoAutos();
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return false;
	}
}