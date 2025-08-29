package br.com.infox.editor.bean;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;

public class DadosTopico implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String titulo;
	private String conteudo;
	private Boolean habilitado;
	private Boolean opcional;
	private Integer ordem;
	private Integer nivel;
	private Boolean podeEditarTitulo;
	private Boolean podeEditarConteudo;
	private Boolean podeMoverParaCima;
	private Boolean podeMoverParaBaixo;
	private Boolean podeAdicionarTopico;
	private Boolean podeRemoverTopico;
	private Boolean podeMudarHabilitacao;
	private Boolean podeRecarregarTopico;
	private Boolean numerado;
	private Boolean exibirTitulo;
	private String numeracaoFormatada;
	private Boolean inBloco;
	private Boolean somenteLeitura;
	private TipoTopicoEnum tipoTopico;

	public DadosTopico() {
	}

	public DadosTopico(ProcessoDocumentoEstruturadoTopico topico) {
		this.id = topico.getCodIdentificador();
		this.titulo = topico.getTitulo();
		this.conteudo = topico.getConteudo();
		this.habilitado = topico.isHabilitado();
		this.opcional = topico.getTopico().isOpcional();
		this.ordem = topico.getOrdem();
		this.nivel = topico.getNivel();
		this.exibirTitulo = topico.isExibirTitulo();
		this.numerado = topico.isNumerado();
		this.inBloco = topico.getProcessoDocumentoEstruturadoBloco() != null || topico.getTopico().getItemTopico() != null;
		this.somenteLeitura = topico.getTopico().isSomenteLeitura();
		this.tipoTopico = topico.getTopico().getTipoTopico();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public Boolean getHabilitado() {
		return habilitado;
	}

	public void setHabilitado(Boolean habilitado) {
		this.habilitado = habilitado;
	}

	public Boolean getOpcional() {
		return opcional;
	}

	public void setOpcional(Boolean opcional) {
		this.opcional = opcional;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	public Boolean getPodeEditarTitulo() {
		return podeEditarTitulo;
	}

	public void setPodeEditarTitulo(Boolean podeEditarTitulo) {
		this.podeEditarTitulo = podeEditarTitulo;
	}

	public Boolean getPodeEditarConteudo() {
		return podeEditarConteudo;
	}

	public void setPodeEditarConteudo(Boolean podeEditarConteudo) {
		this.podeEditarConteudo = podeEditarConteudo;
	}

	public Boolean getPodeMoverParaCima() {
		return podeMoverParaCima;
	}

	public void setPodeMoverParaCima(Boolean podeMoverParaCima) {
		this.podeMoverParaCima = podeMoverParaCima;
	}

	public Boolean getPodeMoverParaBaixo() {
		return podeMoverParaBaixo;
	}

	public void setPodeMoverParaBaixo(Boolean podeMoverParaBaixo) {
		this.podeMoverParaBaixo = podeMoverParaBaixo;
	}

	public Boolean getPodeAdicionarTopico() {
		return podeAdicionarTopico;
	}

	public void setPodeAdicionarTopico(Boolean podeAdicionarTopico) {
		this.podeAdicionarTopico = podeAdicionarTopico;
	}

	public Boolean getPodeRemoverTopico() {
		return podeRemoverTopico;
	}
	
	public void setPodeRemoverTopico(Boolean podeRemoverTopico) {
		this.podeRemoverTopico = podeRemoverTopico;
	}
	
	public Boolean getNumerado() {
		return numerado;
	}
	
	public void setNumerado(Boolean numerado) {
		this.numerado = numerado;
	}
	
	public Boolean getExibirTitulo() {
		return exibirTitulo;
	}
	
	public void setExibirTitulo(Boolean exibirTitulo) {
		this.exibirTitulo = exibirTitulo;
	}
	
	public String getNumeracaoFormatada() {
		return numeracaoFormatada;
	}
	
	public void setNumeracaoFormatada(String numeracaoFormatada) {
		this.numeracaoFormatada = numeracaoFormatada;
	}
	
	public Boolean getInBloco() {
		return inBloco;
	}
	
	public void setInBloco(Boolean inBloco) {
		this.inBloco = inBloco;
	}
	
	public Boolean getSomenteLeitura() {
		return somenteLeitura;
	}
	
	public void setSomenteLeitura(Boolean somenteLeitura) {
		this.somenteLeitura = somenteLeitura;
	}
	
	public TipoTopicoEnum getTipoTopico() {
		return tipoTopico;
	}
	
	public void setTipoTopico(TipoTopicoEnum tipoTopico) {
		this.tipoTopico = tipoTopico;
	}
	
	public Boolean getPodeMudarHabilitacao() {
		return podeMudarHabilitacao;
	}
	
	public void setPodeMudarHabilitacao(Boolean podeMudarHabilitacao) {
		this.podeMudarHabilitacao = podeMudarHabilitacao;
	}
	
	public Boolean getPodeRecarregarTopico() {
		return podeRecarregarTopico;
	}
	
	public void setPodeRecarregarTopico(Boolean podeRecarregarTopico) {
		this.podeRecarregarTopico = podeRecarregarTopico;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ordem == null) ? 0 : ordem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DadosTopico))
			return false;
		DadosTopico other = (DadosTopico) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (ordem == null) {
			if (other.ordem != null)
				return false;
		} else if (!ordem.equals(other.ordem))
			return false;
		return true;
	}
}