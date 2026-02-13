package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

@Entity
@Table(name = "tb_processo_parte")
@IndexedEntity(id="idProcessoParte", value="parte", owners={"processoTrf"},
mappings={
	@Mapping(beanPath="pessoa", mappedPath="pessoa"),
	@Mapping(beanPath="tipoParte.tipoParte", mappedPath="tipo"),
	@Mapping(beanPath="inParticipacao.label", mappedPath="polo"),
	@Mapping(beanPath="parteSigilosa", mappedPath="sigilosa")
})
@SequenceGenerator(allocationSize = 1, name = "gen_processo_parte", sequenceName = "sq_tb_processo_parte")
public class ProcessoParteMin  implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private Long idProcessoTrf;
	private Long idPessoa;
	private Long idTipoParte;
	private Boolean partePrincipal = Boolean.FALSE;
	private Boolean isEnderecoDesconhecido = Boolean.FALSE;
	private ProcessoParteSituacaoEnum situacao = ProcessoParteSituacaoEnum.A;

	public ProcessoParteMin() {
		super();
	}

	public ProcessoParteMin(Long id, Long idProcessoTrf, Long idPessoa, ProcessoParteSituacaoEnum situacao, Long idTipoParte, Boolean partePrincipal, Boolean isEnderecoDesconhecido) {
		super();
		this.id = id;
		this.idProcessoTrf = idProcessoTrf;
		this.idPessoa = idPessoa;
		this.situacao = situacao;
		this.idTipoParte = idTipoParte;
		this.partePrincipal = partePrincipal;
		this.isEnderecoDesconhecido = isEnderecoDesconhecido; 
	}

	@Id
	@GeneratedValue(generator = "gen_processo_parte")
	@Column(name = "id_processo_parte", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "id_processo_trf", nullable = false)
	public Long getIdProcessoTrf() {
		return idProcessoTrf;
	}
	
	public void setIdProcessoTrf(Long idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}
	
	@Column(name = "id_pessoa", nullable = false)
	public Long getIdPessoa() {
		return idPessoa;
	}
	
	public void setIdPessoa(Long idPessoa) {
		this.idPessoa = idPessoa;
	}
	
	@Column(name = "in_situacao", length = 1, nullable = false)
	@Enumerated(EnumType.STRING)
	public ProcessoParteSituacaoEnum getSituacao() {
		return situacao;
	}
	
	public void setSituacao(ProcessoParteSituacaoEnum situacao) {
		this.situacao = situacao;
	}

	/**
	 * @return idTipoParte.
	 */
	@Column(name = "id_tipo_parte", nullable = false)
	public Long getIdTipoParte() {
		return idTipoParte;
	}

	/**
	 * @param idTipoParte Atribui idTipoParte.
	 */
	public void setIdTipoParte(Long idTipoParte) {
		this.idTipoParte = idTipoParte;
	}
	
	@Column(name = "in_parte_principal", nullable = false)
	@NotNull
	public Boolean getPartePrincipal() {
		return this.partePrincipal;
	}

	public void setPartePrincipal(Boolean partePrincipal) {
		this.partePrincipal = partePrincipal;
	}
	
	@Column(name = "in_endereco_desconhecido", nullable = false)
	@NotNull
	public Boolean getIsEnderecoDesconhecido() {
		return isEnderecoDesconhecido;
	}

	public void setIsEnderecoDesconhecido(Boolean isEnderecoDesconhecido) {
		this.isEnderecoDesconhecido = isEnderecoDesconhecido;
	}
}
