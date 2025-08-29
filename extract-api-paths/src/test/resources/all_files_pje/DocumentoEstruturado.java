package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name="tb_documento_estruturado")
@org.hibernate.annotations.GenericGenerator(name = "gen_documento_estruturado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento_estruturado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DocumentoEstruturado implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DocumentoEstruturado,Integer>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4215129654180939838L;

	private int idDocumentoEstruturado;
	
	private String xml;
	
	private TipoDocumentoEstruturado tipoDocumentoEstruturado;

	@Id
	@GeneratedValue(generator = "gen_documento_estruturado")
	@Column(name = "id_documento_estruturado", unique = true, nullable = false)
	@NotNull
	public int getIdDocumentoEstruturado() {
		return idDocumentoEstruturado;
	}

	public void setIdDocumentoEstruturado(int idDocumentoEstruturado) {
		this.idDocumentoEstruturado = idDocumentoEstruturado;
	}

	@Column(name="ds_xml")
	@Type(type="br.jus.pje.nucleo.type.SQLXMLType")
	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	@ManyToOne
	@JoinColumn(name="id_tp_doc_estruturado")
	public TipoDocumentoEstruturado getTipoDocumentoEstruturado() {
		return tipoDocumentoEstruturado;
	}

	public void setTipoDocumentoEstruturado(
			TipoDocumentoEstruturado tipoDocumentoEstruturado) {
		this.tipoDocumentoEstruturado = tipoDocumentoEstruturado;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DocumentoEstruturado> getEntityClass() {
		return DocumentoEstruturado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDocumentoEstruturado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
