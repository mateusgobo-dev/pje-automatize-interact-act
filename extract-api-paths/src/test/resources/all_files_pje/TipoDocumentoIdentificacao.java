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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = TipoDocumentoIdentificacao.TABLE_NAME)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class TipoDocumentoIdentificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoDocumentoIdentificacao,String> {

	public static final String TABLE_NAME = "tb_tipo_doc_identificacao";
	private static final long serialVersionUID = 1L;

	private String codTipo;
	private String tipoDocumento;
	private Boolean identificador;
	private TipoPessoaEnum tipoPessoa;
	private String mascara;
	private Boolean ativo = Boolean.TRUE;
	private Boolean somenteLeitura;
	private boolean permiteMultiplosAtivos = false;
	private boolean orgaoExpedidorObrigatorio = false;
	private boolean dataExpedicaoObrigatorio = false;

	public TipoDocumentoIdentificacao() {}

	public TipoDocumentoIdentificacao(String codTipo) {
		setCodTipo(codTipo);
	}

	@Id
	@Column(name = "cd_tp_documento_identificacao", unique = true, nullable = false)
	@Length(max = 3)
	public String getCodTipo() {
		return this.codTipo;
	}

	public void setCodTipo(String codTipo) {
		this.codTipo = codTipo;
	}

	@Column(name = "ds_tp_documento_identificacao", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getTipoDocumento() {
		return this.tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		if (tipoDocumento != null) {
			this.tipoDocumento = tipoDocumento.toUpperCase();
		} else {
			this.tipoDocumento = tipoDocumento;
		}
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "tp_tipo_pessoa", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public TipoPessoaEnum getTipoPessoa() {
		return this.tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoaEnum tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	/**
	 * Metodo Get do atributo identificador.
	 * 
	 * este atributo se refere à categoria deste tipo de documento.
	 * ele será TRUE se a categoria for identificativa, como CPF, CNPJ ou PASSAPORTE
	 * ele será FALSE se a categoria for secundaria, como TITULO, CERTIDAO DE CASAMENTO, ETC
	 *  
	 * @return true se for do tipo identificativo / false
	 */
	@Column(name = "in_documento_principal", nullable = false)
	@NotNull
	public Boolean getIdentificador() {
		return this.identificador;
	}

	public void setIdentificador(Boolean identificador) {
		this.identificador = identificador;
	}

	@Override
	public String toString() {
		return tipoDocumento;
	}

	public void setMascara(String mascara) {
		this.mascara = mascara;
	}

	@Column(name = "ds_mascara_campo", length = 30)
	@Length(max = 30)
	public String getMascara() {
		return mascara;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoDocumentoIdentificacao)) {
			return false;
		}
		TipoDocumentoIdentificacao other = (TipoDocumentoIdentificacao) obj;

		if( this != null && this.getCodTipo()!=null ) {
			if (!this.getCodTipo().equals(other.getCodTipo())) {
				return false;
			}
		} else {
			if( other.getCodTipo()!=null ) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCodTipo() == null) ? 0 : getCodTipo().hashCode());
		return result;
	}
	
	@Column(name = "in_somente_leitura", nullable = false)
	@NotNull
	public Boolean getSomenteLeitura() {
		return this.somenteLeitura;
	}

	public void setSomenteLeitura(Boolean somenteLeitura) {
		this.somenteLeitura = somenteLeitura;
	}
	
	/**
	 * Metodo GET do atributo permiteMultiplosAtivos
	 * 
	 * refere-se à capacidade deste tipo de documento permitir multiplos ativos, ou seja, se este tipo permite que existam outros documentos do mesmo
	 * tipo ativos simultaneamente na mesma pessoa.
	 * 
	 * Os documentos que são unicos por pessoa, tem este atributo como false(ex. CPF)
	 * 
	 * Os documentos onde uma pessoa possam ter varios ao mesmo tempo, com numeraçao diferente, tem o atributo como true (ex. CNPJ, titulos, identidades, etc)
	 * 
	 * @return true / false
	 */
	@Column(name = "in_permite_multiplos_ativos")
	public boolean getPermiteMultiplosAtivos() {
		return permiteMultiplosAtivos;
	}
			public void setPermiteMultiplosAtivos(boolean permiteMultiplosAtivos) {
		this.permiteMultiplosAtivos = permiteMultiplosAtivos;
	}

	@Column(name = "orgao_expedidor_obrigatorio")
	@NotNull
	public boolean isOrgaoExpedidorObrigatorio() {
		return orgaoExpedidorObrigatorio;
	}

	public void setOrgaoExpedidorObrigatorio(boolean orgaoExpedidorObrigatorio) {
		this.orgaoExpedidorObrigatorio = orgaoExpedidorObrigatorio;
	}
	
	@Column(name = "in_data_expedicao_obrigatorio")
 	@NotNull
 	public boolean isDataExpedicaoObrigatorio() {
 		return dataExpedicaoObrigatorio;
 	}
 
 	public void setDataExpedicaoObrigatorio(boolean dataExpedicaoObrigatorio) {
 		this.dataExpedicaoObrigatorio = dataExpedicaoObrigatorio;
 	}
 	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoDocumentoIdentificacao> getEntityClass() {
		return TipoDocumentoIdentificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public String getEntityIdObject() {
		return getCodTipo();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
