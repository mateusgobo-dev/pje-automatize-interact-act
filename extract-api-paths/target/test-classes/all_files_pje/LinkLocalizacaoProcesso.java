package br.jus.cnj.pje.entidades.vo;

/**
 * @author t317549- Antonio Francisco Osorio Jr/TJDFT
 * 
 * PJEII-18841
 * 
 * Classe responsável por conter os atributos dos links que permitirão:
 * 	<ul>
 * 		<li>Abrir os detalhes do processo, ou</li>
 * 		<li>Abrir a tarefa a qual o processo se encontra.</li>
 *	</ul>
 */
public class LinkLocalizacaoProcesso implements Comparable<LinkLocalizacaoProcesso> {

	private Integer idProcesso;
	private Integer numeroSequencia;
	private String nomeTarefa;
	private Integer idCaixa;
	private String nomeCaixa;
	private String mensagem;
	private Integer idTarefa;

	
	public LinkLocalizacaoProcesso() {
	}
	
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getNomeCaixa() {
		return nomeCaixa;
	}

	public void setNomeCaixa(String nomeCaixa) {
		this.nomeCaixa = nomeCaixa;
	}

	public Integer getIdCaixa() {
		return idCaixa;
	}

	public void setIdCaixa(Integer idCaixa) {
		this.idCaixa = idCaixa;
	}

	public String getNomeTarefa() {
		return nomeTarefa;
	}

	public void setNomeTarefa(String nomeTarefa) {
		this.nomeTarefa = nomeTarefa;
	}

	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Override
	public int compareTo(LinkLocalizacaoProcesso o) {
		return this.nomeTarefa.compareTo(o.getNomeTarefa());
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia=numeroSequencia;	
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setIdTarefa(Integer idTarefa) {
		this.idTarefa=idTarefa;
	}

	public Integer getIdTarefa() {
		return idTarefa;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idCaixa == null) ? 0 : idCaixa.hashCode());
		result = prime * result
				+ ((idProcesso == null) ? 0 : idProcesso.hashCode());
		result = prime * result
				+ ((idTarefa == null) ? 0 : idTarefa.hashCode());
		result = prime * result
				+ ((mensagem == null) ? 0 : mensagem.hashCode());
		result = prime * result
				+ ((nomeCaixa == null) ? 0 : nomeCaixa.hashCode());
		result = prime * result
				+ ((nomeTarefa == null) ? 0 : nomeTarefa.hashCode());
		result = prime * result
				+ ((numeroSequencia == null) ? 0 : numeroSequencia.hashCode());
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
		LinkLocalizacaoProcesso other = (LinkLocalizacaoProcesso) obj;
		if (idCaixa == null) {
			if (other.idCaixa != null)
				return false;
		} else if (!idCaixa.equals(other.idCaixa))
			return false;
		if (idProcesso == null) {
			if (other.idProcesso != null)
				return false;
		} else if (!idProcesso.equals(other.idProcesso))
			return false;
		if (idTarefa == null) {
			if (other.idTarefa != null)
				return false;
		} else if (!idTarefa.equals(other.idTarefa))
			return false;
		if (mensagem == null) {
			if (other.mensagem != null)
				return false;
		} else if (!mensagem.equals(other.mensagem))
			return false;
		if (nomeCaixa == null) {
			if (other.nomeCaixa != null)
				return false;
		} else if (!nomeCaixa.equals(other.nomeCaixa))
			return false;
		if (nomeTarefa == null) {
			if (other.nomeTarefa != null)
				return false;
		} else if (!nomeTarefa.equals(other.nomeTarefa))
			return false;
		if (numeroSequencia == null) {
			if (other.numeroSequencia != null)
				return false;
		} else if (!numeroSequencia.equals(other.numeroSequencia))
			return false;
		return true;
	}

}
