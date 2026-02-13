package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name="tb_tp_doc_estruturado")
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_doc_estruturado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_doc_estruturado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoDocumentoEstruturado implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoDocumentoEstruturado,Integer>{
	
	private static final long serialVersionUID = -919690449681472193L;
	
	private int idTipoDocumentoEstruturado;
	
	private String descricao;
	
	private String xsd;
	
	private String namespace;

	@Id
	@GeneratedValue(generator = "gen_tp_doc_estruturado")
	@Column(name = "id_tp_doc_estruturado", unique = true, nullable = false)
	@NotNull
	public int getIdTipoDocumentoEstruturado() {
		return idTipoDocumentoEstruturado;
	}

	public void setIdTipoDocumentoEstruturado(int idTipoDocumentoEstruturado) {
		this.idTipoDocumentoEstruturado = idTipoDocumentoEstruturado;
	}

	@Column(name="ds_tp_doc_estruturado")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="ds_xsd")
	@Type(type="br.jus.pje.nucleo.type.SQLXMLType")
	public String getXsd() {
		return xsd;
	}

	public void setXsd(String xsd) {
		this.xsd = xsd;
	}
	
	@Column(name="ds_namespace")
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoDocumentoEstruturado> getEntityClass() {
		return TipoDocumentoEstruturado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoDocumentoEstruturado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
