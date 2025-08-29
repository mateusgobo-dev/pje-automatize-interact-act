package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class InformacaoSessaoProcesso implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2499977347109456674L;
	
	private Integer idJulgamento;
	private Integer sequencial;
	private String classe;
	private Integer idRelator;
	private String relator;
	private String pedidoSustentacaoOral;
	private String nrProcesso;
	private String link;
	private String categoriaJulgamento;
	private String situacaoJulgamento;
	private String proclamacao;
	private String vencedor;
	private String origem;
	private List<InformacaoSessaoProcessoPlacar> placar = new ArrayList<InformacaoSessaoProcessoPlacar>(0);
	
	@XmlElement(name="idJulgamento")
	public Integer getIdJulgamento() {
		return idJulgamento;
	}

	public void setIdJulgamento(Integer idJulgamento) {
		this.idJulgamento = idJulgamento;
	}

	@XmlElement(name="nrProcesso")
	public String getNrProcesso() {
		return nrProcesso;
	}
	
	public void setNrProcesso(String nrProcesso) {
		this.nrProcesso = nrProcesso;
	}
	
	@XmlElement(name="link")
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	@XmlElement(name="categoriaJulgamento")
	public String getCategoriaJulgamento() {
		return categoriaJulgamento;
	}
	public void setCategoriaJulgamento(String categoriaJulgamento) {
		this.categoriaJulgamento = categoriaJulgamento;
	}
	
	@XmlElement(name="sequencial")
	public Integer getSequencial() {
		return sequencial;
	}
	public void setSequencial(Integer sequencial) {
		this.sequencial = sequencial;
	}
	
	@XmlElement(name="classe")
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	
	@XmlElement(name="idRelator")	
	public Integer getIdRelator() {
		return idRelator;
	}
	public void setIdRelator(Integer idRelator) {
		this.idRelator = idRelator;
	}

	@XmlElement(name="relator")
	public String getRelator() {
		return relator;
	}
	public void setRelator(String relator) {
		this.relator = relator;
	}
	
	@XmlElement(name="pedidoSustentacaoOral")
	public String getPedidoSustentacaoOral() {
		return pedidoSustentacaoOral;
	}
	public void setPedidoSustentacaoOral(String pedidoSustentacaoOral) {
		this.pedidoSustentacaoOral = pedidoSustentacaoOral;
	}
	
	@XmlElement(name="situacaoJulgamento")
	public String getSituacaoJulgamento() {
		return situacaoJulgamento;
	}
	
	public void setSituacaoJulgamento(String situacaoJulgamento) {
		this.situacaoJulgamento = situacaoJulgamento;
	}
	
	@XmlElement(name="proclamacao")
	public String getProclamacao() {
		return proclamacao;
	}
	
	public void setProclamacao(String proclamacao) {
		this.proclamacao = proclamacao;
	}
	
	@XmlElement(name="vencedor")
	public String getVencedor() {
		return vencedor;
	}
	
	public void setVencedor(String vencedor) {
		this.vencedor = vencedor;
	}
	
	@XmlElement(name="placar")
	public List<InformacaoSessaoProcessoPlacar> getPlacar() {
		return placar;
	}
	
	public void setPlacar(List<InformacaoSessaoProcessoPlacar> placar) {
		this.placar = placar;
	}
	
	@XmlElement(name="origem") public String getOrigem()
	{ return origem; }

	public void setOrigem(String origem) { 
		this.origem = origem; 
	} 
}
