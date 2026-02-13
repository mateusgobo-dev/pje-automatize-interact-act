package br.com.infox.editor.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.enums.editor.NivelVisibilidadeAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAcolhidoAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusCienciaAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoAnotacao;

public class AnotacaoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idAnotacao;
	private String conteudo;
	private String nomePessoaCriacao;
	private Boolean destaque;
	private TipoAnotacao tipoAnotacao;
	private StatusAnotacao statusAnotacao;
	private Integer idDocumento;
	private Integer idTopico;
	private Boolean podeEditar;
	private NivelVisibilidadeAnotacao nivelVisibilidadeAnotacao;
	private Boolean podeReabrir;
	private Boolean podeRetirar;
	private Boolean podeLiberar;
	private Boolean podeConcluir;
	private Boolean podeExcluir;
	private Boolean podeDestacar;
	private Boolean podeCriarDivergencia;
	private String data;
	private String titulo;
	private String observacao;
	private Boolean podeMostrarAcoesDivergenciaRelator;
	private Integer idOrgaoJulgador;
	private Boolean podeManterDivergencia;
	private Boolean podeMarcarDivergenciaComoCiente;
	private StatusAcolhidoAnotacao statusAcolhidoAnotacao;
	private StatusCienciaAnotacao statusCienciaAnotacao;
	private Boolean topicoAssociadoExcluido;
	private Date dataCriacao;
	
	public AnotacaoBean() {
	}

	public AnotacaoBean(Anotacao anotacao) {
		this.idAnotacao = anotacao.getCodigoIdentificador();
		this.conteudo = anotacao.getConteudo();
		this.destaque = anotacao.getDestaque();
		
		this.tipoAnotacao = anotacao.getTipoAnotacao();
		this.statusAnotacao = anotacao.getStatusAnotacao();
		this.idDocumento = anotacao.getDocumento().getIdProcessoDocumentoEstruturado();
		this.idTopico = anotacao.getTopico().getCodIdentificador();
		
		this.nivelVisibilidadeAnotacao = anotacao.getNivelVisibilidadeAnotacao();
		this.data = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(anotacao.getDataAlteracaoStatus());
		this.dataCriacao = anotacao.getDataCriacao();
	}

	public Integer getIdAnotacao() {
		return idAnotacao;
	}

	public void setIdAnotacao(Integer idAnotacao) {
		this.idAnotacao = idAnotacao;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public String getNomePessoaCriacao() {
		return nomePessoaCriacao;
	}

	public void setNomePessoaCriacao(String nomePessoaCriacao) {
		this.nomePessoaCriacao = nomePessoaCriacao;
	}

	public Boolean getDestaque() {
		return destaque;
	}

	public void setDestaque(Boolean destaque) {
		this.destaque = destaque;
	}

	public TipoAnotacao getTipoAnotacao() {
		return tipoAnotacao;
	}

	public void setTipoAnotacao(TipoAnotacao tipoAnotacao) {
		this.tipoAnotacao = tipoAnotacao;
	}

	public StatusAnotacao getStatusAnotacao() {
		return statusAnotacao;
	}

	public void setStatusAnotacao(StatusAnotacao statusAnotacao) {
		this.statusAnotacao = statusAnotacao;
	}

	public Integer getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(Integer idDocumento) {
		this.idDocumento = idDocumento;
	}

	public Integer getIdTopico() {
		return idTopico;
	}

	public void setIdTopico(Integer idTopico) {
		this.idTopico = idTopico;
	}
	
	public Boolean getPodeEditar() {
		return podeEditar;
	}
	
	public NivelVisibilidadeAnotacao getNivelVisibilidadeAnotacao() {
		return nivelVisibilidadeAnotacao;
	}
	
	public void setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao nivelVisibilidadeAnotacao) {
		this.nivelVisibilidadeAnotacao = nivelVisibilidadeAnotacao;
	}
	
	public void setPodeEditar(Boolean podeEditar) {
		this.podeEditar = podeEditar;
	}
	
	public Boolean getPodeReabrir() {
		return podeReabrir;
	}
	
	public void setPodeReabrir(Boolean podeReabrir) {
		this.podeReabrir = podeReabrir;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public Boolean getPodeRetirar() {
		return podeRetirar;
	}

	public void setPodeRetirar(Boolean podeRetirar) {
		this.podeRetirar = podeRetirar;
	}

	public Boolean getPodeLiberar() {
		return podeLiberar;
	}

	public void setPodeLiberar(Boolean podeLiberar) {
		this.podeLiberar = podeLiberar;
	}

	public Boolean getPodeConcluir() {
		return podeConcluir;
	}

	public void setPodeConcluir(Boolean podeConcluir) {
		this.podeConcluir = podeConcluir;
	}

	public Boolean getPodeExcluir() {
		return podeExcluir;
	}

	public void setPodeExcluir(Boolean podeExcluir) {
		this.podeExcluir = podeExcluir;
	}
	
	public Boolean getPodeDestacar() {
		return podeDestacar;
	}
	
	public void setPodeDestacar(Boolean podeDestacar) {
		this.podeDestacar = podeDestacar;
	}
	
	public Boolean getPodeCriarDivergencia() {
		return podeCriarDivergencia;
	}
	
	public void setPodeCriarDivergencia(Boolean podeCriarDivergencia) {
		this.podeCriarDivergencia = podeCriarDivergencia;
	}
	
	public String getTitulo() {
		return titulo;
	}
	
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	
	public String getObservacao() {
		return observacao;
	}
	
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	
	public Boolean getPodeMostrarAcoesDivergenciaRelator() {
		return podeMostrarAcoesDivergenciaRelator;
	}
	
	public void setPodeMostrarAcoesDivergenciaRelator(Boolean podeMostrarAcoesDivergenciaRelator) {
		this.podeMostrarAcoesDivergenciaRelator = podeMostrarAcoesDivergenciaRelator;
	}
	
	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}
	
	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}
	
	public Boolean getPodeManterDivergencia() {
		return podeManterDivergencia;
	}
	
	public void setPodeManterDivergencia(Boolean podeManterDivergencia) {
		this.podeManterDivergencia = podeManterDivergencia;
	}
	
	public Boolean getPodeMarcarDivergenciaComoCiente() {
		return podeMarcarDivergenciaComoCiente;
	}
	
	public void setPodeMarcarDivergenciaComoCiente(
			Boolean podeMarcarDivergenciaComoCiente) {
		this.podeMarcarDivergenciaComoCiente = podeMarcarDivergenciaComoCiente;
	}
	
	public StatusAcolhidoAnotacao getStatusAcolhidoAnotacao() {
		return statusAcolhidoAnotacao;
	}
	
	public void setStatusAcolhidoAnotacao(StatusAcolhidoAnotacao statusAcolhidoAnotacao) {
		this.statusAcolhidoAnotacao = statusAcolhidoAnotacao;
	}
	
	public StatusCienciaAnotacao getStatusCienciaAnotacao() {
		return statusCienciaAnotacao;
	}
	
	public void setStatusCienciaAnotacao(StatusCienciaAnotacao statusCienciaAnotacao) {
		this.statusCienciaAnotacao = statusCienciaAnotacao;
	}
	
	public Boolean getTopicoAssociadoExcluido() {
		return topicoAssociadoExcluido;
	}
	
	public void setTopicoAssociadoExcluido(Boolean topicoAssociadoExcluido) {
		this.topicoAssociadoExcluido = topicoAssociadoExcluido;
	}
	
	public Date getDataCriacao() {
		return dataCriacao;
	}
	
	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((idAnotacao == null) ? 0 : idAnotacao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnotacaoBean other = (AnotacaoBean) obj;
		if (idAnotacao == null) {
			if (other.idAnotacao != null)
				return false;
		} else if (!idAnotacao.equals(other.idAnotacao))
			return false;
		return true;
	}
}
