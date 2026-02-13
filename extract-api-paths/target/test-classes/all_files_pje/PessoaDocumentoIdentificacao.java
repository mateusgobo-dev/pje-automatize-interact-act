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

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.search.bridge.NumeroDocumentoBridge;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.search.bridge.NumeroDocumentoBridge;

@Entity
@Table(name = PessoaDocumentoIdentificacao.TABLE_NAME)
@Indexed
@IndexedEntity(id = "idDocumentoIdentificacao", value = "documentoidentificacao",
        mappings = {
                @Mapping(beanPath = "numeroDocumento", mappedPath = "numero"),
                @Mapping(beanPath = "tipoDocumento.codTipo", mappedPath = "codigo"),
                @Mapping(beanPath = "pessoa.nome", mappedPath = "nome"),
                @Mapping(beanPath = "pessoa.idPessoa", mappedPath = "pessoa")})
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_doc_identificacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_doc_identificacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaDocumentoIdentificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaDocumentoIdentificacao,Integer>{

	public static final String TABLE_NAME = "tb_pess_doc_identificacao";
	private static final long serialVersionUID = 1L;

	private int idDocumentoIdentificacao;
	private TipoDocumentoIdentificacao tipoDocumento;
	private String numeroDocumento;
	private String numeroDocumentoAnterior;
	private Date dataExpedicao;
	private String nome;
	private String orgaoExpedidor;
	private Estado estado;
	private Boolean usadoFalsamente = Boolean.FALSE;
	private Date dataUsadoFalsamente;
	private Boolean documentoPrincipal = Boolean.FALSE;
	private Pessoa pessoa;
	private Integer idPessoa;
	private Boolean ativo = true;
	private Pais pais;
	private Usuario usuarioCadastrador;
	private PessoaAdvogadoTipoInscricaoEnum letraOAB;
	private Boolean temporario = Boolean.FALSE;
	private String nomeMae;
	private String nomePai;
	private Date dataNascimento;
	/**
	 * Atributo usado exclusivamente para análise de prevenção.
	 * O valor deste atributo corresponde ao valor do atributo 'nome' da entidade 'UsuarioLogin'.
	 */
	private String nomeUsuarioLogin;

	public PessoaDocumentoIdentificacao() {

	}

	@Id
	@GeneratedValue(generator = "gen_pess_doc_identificacao")
	@Column(name = "id_pessoa_doc_identificacao", unique = true, nullable = false)
	public int getIdDocumentoIdentificacao() {
		return idDocumentoIdentificacao;
	}

	public void setIdDocumentoIdentificacao(int idDocumentoIdentificacao) {
		this.idDocumentoIdentificacao = idDocumentoIdentificacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cd_tp_documento_identificacao", nullable = false)
	@NotNull
	public TipoDocumentoIdentificacao getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumentoIdentificacao tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	@Column(name = "nr_documento_identificacao", nullable = false, length = 40)
	@NotNull
	@Length(max = 40)
	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	@Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO, name = "numeroDocumento")
	@FieldBridge(impl = NumeroDocumentoBridge.class)
	@Transient
	public String getTipoNumeroDocumento() {
		return this.tipoDocumento.getCodTipo().trim() + "+" + this.numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;		
	}

	@Column(name = "dt_expedicao", nullable = true)
	public Date getDataExpedicao() {
		return dataExpedicao;
	}

	public void setDataExpedicao(Date dataExpedicao) {
		this.dataExpedicao = dataExpedicao;
	}

	@Column(name = "ds_nome_pessoa", nullable = false, length = 255)
	@NotNull
	@Length(max = 255)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_orgao_expedidor", length = 50)
	@Length(max = 50)
	public String getOrgaoExpedidor() {
		return orgaoExpedidor;
	}

	public void setOrgaoExpedidor(String orgaoExpedidor) {
		this.orgaoExpedidor = orgaoExpedidor;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_estado_expedidor", nullable = true)
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@Column(name = "in_usado_falsamente", nullable = false)
	@NotNull
	public Boolean getUsadoFalsamente() {
		return usadoFalsamente;
	}

	public void setUsadoFalsamente(Boolean usadoFalsamente) {
		this.usadoFalsamente = usadoFalsamente;
	}

	@Column(name = "dt_usado_falsamente", nullable = true)
	public Date getDataUsadoFalsamente() {
		return dataUsadoFalsamente;
	}

	public void setDataUsadoFalsamente(Date dataUsadoFalsamente) {
		this.dataUsadoFalsamente = dataUsadoFalsamente;
	}

	@Column(name = "in_principal", nullable = false)
	@NotNull
	public Boolean getDocumentoPrincipal() {
		return documentoPrincipal;
	}

	public void setDocumentoPrincipal(Boolean documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobregarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pais")
	public Pais getPais() {
		return pais;
	}

	public void setPais(Pais pais) {
		this.pais = pais;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastrador")
	public Usuario getUsuarioCadastrador() {
		return this.usuarioCadastrador;
	}

	public void setUsuarioCadastrador(Usuario usuarioCadastrador) {
		this.usuarioCadastrador = usuarioCadastrador;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getNumeroDocumento() == null) ? 0 : numeroDocumento.hashCode());
		result = prime * result + ((getPessoa() == null) ? 0 : pessoa.hashCode());
		result = prime * result + ((getTipoDocumento() == null) ? 0 : tipoDocumento.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaDocumentoIdentificacao)) {
			return false;
		}
		PessoaDocumentoIdentificacao other = (PessoaDocumentoIdentificacao) obj;
		if (getNumeroDocumento() == null) {
			if (other.getNumeroDocumento() != null) {
				return false;
			}
		} else if (!numeroDocumento.equals(other.getNumeroDocumento())) {
			return false;
		}
		if (getPessoa() == null) {
			if (other.getPessoa() != null) {
				return false;
			}
		} else if (!pessoa.equals(other.getPessoa())) {
			return false;
		}
		if (getTipoDocumento() == null) {
			if (other.getTipoDocumento() != null) {
				return false;
			}
		} else if (!tipoDocumento.equals(other.getTipoDocumento())) {
			return false;
		}
		if (getAtivo() == null) {
			if (other.getAtivo() != null) {
				return false;
			}
		} else if (!ativo.equals(other.getAtivo())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.nome + " - " + this.numeroDocumento;
	}

	@Transient
	public PessoaAdvogadoTipoInscricaoEnum getLetraOAB() {
		return letraOAB;
	}

	public void setLetraOAB(PessoaAdvogadoTipoInscricaoEnum letraOAB) {
		this.letraOAB = letraOAB;
	}

	@Column(name = "nr_documento_identificacao", nullable = false, length = 30, updatable=false, insertable=false)
	public String getNumeroDocumentoAnterior() {
		return numeroDocumentoAnterior;
	}

	public void setNumeroDocumentoAnterior(String numeroDocumentoAnterior) {
		this.numeroDocumentoAnterior = numeroDocumentoAnterior;
	}

	/**
	 * Indica se um documento de identificacao pode (ou nao) ser manipulado 
	 * (alterado ou deletado) pelo usuario externo que o incluiu.
	 * 
	 * @return Verdadeiro, o documento de identificacao pode ser manipulado pelo 
	 * usuario externo que o incluiu. Falso, o documento de identificacao 
	 * não pode ser manipulado pelo usuario externo que o incluiu.
	 */	
	@Column(name = "in_temporario", nullable = false)
	public Boolean getTemporario() {
		return temporario;
	}

	public void setTemporario(Boolean temporario) {
		this.temporario = temporario;
	}

	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = "pessoa.nome")
	@Column(name = "ds_nome_usuario_login", nullable = false, length = 255)
	public String getNomeUsuarioLogin() {
		return nomeUsuarioLogin;
	}

	public void setNomeUsuarioLogin(String nomeUsuarioLogin) {
		this.nomeUsuarioLogin = nomeUsuarioLogin;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaDocumentoIdentificacao> getEntityClass() {
		return PessoaDocumentoIdentificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDocumentoIdentificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
	
	@Column(name = "nm_pai", nullable = true)
	public String getNomePai() {
		return nomePai;
	}
	
	public void setNomePai(String nomePai) {
		this.nomePai = nomePai;
	}
	
	@Column(name = "nm_mae", nullable = true)
	public String getNomeMae() {
		return nomeMae;
	}
	
	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}
	
	@Column(name = "dt_nascimento", nullable = true)
	public Date getDataNascimento() {
		return dataNascimento;
	}
	
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	@Column(name="id_pessoa", insertable=false, updatable=false, nullable = false)
	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}
}