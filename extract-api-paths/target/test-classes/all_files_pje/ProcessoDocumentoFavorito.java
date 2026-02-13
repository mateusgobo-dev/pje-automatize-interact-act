package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_proc_documento_favorito")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_documento_favorito", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_documento_favorito"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoFavorito implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoFavorito,Integer> {

	private static final long serialVersionUID = 102265620486523292L;

	private int idProcessoDocumentoFavorito;
	private ProcessoDocumento processoDocumento;
	private Usuario usuario;
	private int indice;

	@Id
	@GeneratedValue(generator = "gen_proc_documento_favorito")
	@Column(name = "id_proc_documento_favorito", unique = true, nullable = false)
	@NotNull
	public int getIdProcessoDocumentoFavorito() {
		return this.idProcessoDocumentoFavorito;
	}

	public void setIdProcessoDocumentoFavorito(int idProcessoDocumentoFavorito) {
		this.idProcessoDocumentoFavorito = idProcessoDocumentoFavorito;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return this.processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "vl_indice")
	public int getIndice() {
		return indice;
	}

	public void setIndice(int indice) {
		this.indice = indice;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoFavorito> getEntityClass() {
		return ProcessoDocumentoFavorito.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoFavorito());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
