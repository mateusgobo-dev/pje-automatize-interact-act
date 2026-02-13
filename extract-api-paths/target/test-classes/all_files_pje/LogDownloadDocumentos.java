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
@Table(name = "tb_log_download_documentos")
@javax.persistence.Cacheable(false)
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class LogDownloadDocumentos  implements IEntidade<LogDownloadDocumentos, Long>{

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
	    @Parameter(name = "sequence", value = "sq_tb_log_download_documentos"), 
        @Parameter(name = "allocationSize", value = "-1")})
	@GeneratedValue(generator = "generator", strategy = GenerationType.AUTO)
	@Column(name = "id_log_download_documentos", unique = true, nullable = false, updatable = false)
    private Long idLogDownloadDocumentos;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_usuario_localizacao", nullable = false)
    private Long idUsuarioLocalizacao;

    @Column(name = "id_processo", nullable = false)
    private Long idProcesso;

    @Column(name = "foi_pdf_unificado", nullable = false)
    private Boolean foiPdfUnificado;

    @Column(name = "id_processo_documento")
    private Long idProcessoDocumento;

    @Column(name = "primeiro_id_processo_documento")
    private Long primeiroIdProcessoDocumento;

    @Column(name = "ultimo_id_processo_documento")
    private Long ultimoIdProcessoDocumento;

    @Column(name = "url", nullable = false, length = 512)
    private String url;

    @Column(name = "ip", nullable = false, length = 64)
    private String ip;

    @Column(name = "hora", nullable = false)
    private Timestamp hora = new Timestamp(System.currentTimeMillis());

    // Getters and Setters
    public Long getIdLogDownloadDocumentos() {
        return idLogDownloadDocumentos;
    }

    public void setIdLogDownloadDocumentos(Long idLogDownloadDocumentos) {
        this.idLogDownloadDocumentos = idLogDownloadDocumentos;
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

    public Boolean getFoiPdfUnificado() {
        return foiPdfUnificado;
    }

    public void setFoiPdfUnificado(Boolean foiPdfUnificado) {
        this.foiPdfUnificado = foiPdfUnificado;
    }

    public Long getIdProcessoDocumento() {
        return idProcessoDocumento;
    }

    public void setIdProcessoDocumento(Long idProcessoDocumento) {
        this.idProcessoDocumento = idProcessoDocumento;
    }

    public Long getPrimeiroIdProcessoDocumento() {
        return primeiroIdProcessoDocumento;
    }

    public void setPrimeiroIdProcessoDocumento(Long primeiroIdProcessoDocumento) {
        this.primeiroIdProcessoDocumento = primeiroIdProcessoDocumento;
    }

    public Long getUltimoIdProcessoDocumento() {
        return ultimoIdProcessoDocumento;
    }

    public void setUltimoIdProcessoDocumento(Long ultimoIdProcessoDocumento) {
        this.ultimoIdProcessoDocumento = ultimoIdProcessoDocumento;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Timestamp getHora() {
        return hora;
    }

    public void setHora(Timestamp hora) {
        this.hora = hora;
    }

	@Override
	@Transient
	public Class<LogDownloadDocumentos> getEntityClass() {
		return LogDownloadDocumentos.class;
	}

	@Override
	@Transient
	public Long getEntityIdObject() {
		return getIdLogDownloadDocumentos();
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return false;
	}
}