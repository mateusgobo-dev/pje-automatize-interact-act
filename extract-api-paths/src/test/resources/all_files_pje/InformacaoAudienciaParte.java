package br.jus.cnj.pje.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InformacaoAudienciaParte {
	private String nome;
	private String qualificacao;
	private List<InformacaoAudienciaDocumentoParte> documentos = new ArrayList<InformacaoAudienciaDocumentoParte>(0);
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getQualificacao() {
		return qualificacao;
	}

	public void setQualificacao(String qualificacao) {
		this.qualificacao = qualificacao;
	}
	
	public List<InformacaoAudienciaDocumentoParte> getDocumentos() {
		return documentos;
	}
	
	public void setDocumentos(List<InformacaoAudienciaDocumentoParte> documentos) {
		this.documentos = documentos;
	}
}
