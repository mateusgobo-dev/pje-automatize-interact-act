package br.jus.pje.nucleo.entidades.lancadormovimento;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.IEntidade;
import br.jus.pje.nucleo.enums.TipoComplementoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_tipo_complemento")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tp_tipo_complemento", discriminatorType = DiscriminatorType.STRING, length = 1)
@DiscriminatorValue(value = " ")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_complemento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_complemento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoComplemento implements IEntidade<TipoComplemento, Long> {

	private static final long serialVersionUID = 1L;

	private Long idTipoComplemento;
	private String nome;
	private String codigo;
	private String descricaoGlossario;
	private String orgaoCriador;
	private String validacao;
	private String label;
	private String mensagemErro;
	private Boolean ativo;
	private List<AplicacaoComplemento> aplicacaoComplementoList;
	private TipoComplementoEnum tipoComplemento;

	public TipoComplemento() {
		this.aplicacaoComplementoList = new ArrayList<AplicacaoComplemento>();
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_complemento")
	@Column(name = "id_tipo_complemento", nullable = false)
	public Long getIdTipoComplemento() {
		return idTipoComplemento;
	}

	public void setIdTipoComplemento(Long idTipoComplemento) {
		this.idTipoComplemento = idTipoComplemento;
	}

	@Column(name = "ds_nome", unique = true)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "cd_tipo_complemento")
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "ds_glossario")
	public String getDescricaoGlossario() {
		return descricaoGlossario;
	}

	public void setDescricaoGlossario(String descricaoGlossario) {
		this.descricaoGlossario = descricaoGlossario;
	}

	@Column(name = "ds_orgao_criador")
	public String getOrgaoCriador() {
		return orgaoCriador;
	}

	public void setOrgaoCriador(String orgaoCriador) {
		this.orgaoCriador = orgaoCriador;
	}

	@Column(name = "ds_validacao")
	public String getValidacao() {
		return validacao;
	}

	public void setValidacao(String validacao) {
		this.validacao = validacao;
	}

	@Column(name = "ds_label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name = "ds_mensagem_erro")
	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "tipoComplemento")
	public List<AplicacaoComplemento> getAplicacaoComplementoList() {
		return aplicacaoComplementoList;
	}

	public void setAplicacaoComplementoList(List<AplicacaoComplemento> aplicacaoComplementoList) {
		this.aplicacaoComplementoList = aplicacaoComplementoList;
	}

	protected static final String TIPO_COMPLEMENTO_DINAMICO = "I";
	protected static final String TIPO_COMPLEMENTO_COM_DOMINIO = "D";
	protected static final String TIPO_COMPLEMENTO_LIVRE = "L";

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idTipoComplemento == null) ? 0 : idTipoComplemento.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		TipoComplemento other = (TipoComplemento) obj;
		if (idTipoComplemento == null) {
			if (other.getIdTipoComplemento() != null)
				return false;
		} else if (!idTipoComplemento.equals(other.getIdTipoComplemento()))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this.getCodigo() + " - " + this.getNome();
	}
	
	@Transient
	public TipoComplementoEnum getTipoComplemento() {
		if(this instanceof TipoComplementoComDominio){
			this.tipoComplemento = TipoComplementoEnum.D;
		} else if(this instanceof TipoComplementoDinamico){
			this.tipoComplemento = TipoComplementoEnum.I;
		} else if(this instanceof TipoComplementoLivre) {
			this.tipoComplemento = TipoComplementoEnum.L;
		}
		return this.tipoComplemento;
	}

	public void setTipoComplemento(TipoComplementoEnum tipoComplemento) {
		this.tipoComplemento = tipoComplemento;
	}

	@Override
	@Transient
	public Class<? extends TipoComplemento> getEntityClass() {
		return TipoComplemento.class;
	}

	@Override
	@Transient
	public Long getEntityIdObject() {
		return idTipoComplemento;
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return true;
	}
	
}