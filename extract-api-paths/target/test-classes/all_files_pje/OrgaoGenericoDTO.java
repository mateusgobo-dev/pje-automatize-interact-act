package br.jus.cnj.pje.webservice.client.corporativo;

import java.util.ArrayList;
import java.util.List;

public class OrgaoGenericoDTO {

	private String codigoOrgao = null;
	private String codigoOrgaoPai = null;
	private String codigoTipoOrgao = null;
	private String nomeOrgao = null;
	private String nomeTipoOrgao = null;
	private Integer numeroDeOrdem = null;
	private List<OrgaoGenericoDTO> organizacaoInterna = null;
	private String segmentoJustica = null;
	private TribunalDTO tribunal = null;

	public OrgaoGenericoDTO codigoOrgao(String codigoOrgao) {
		this.codigoOrgao = codigoOrgao;
		return this;
	}

	public String getCodigoOrgao() {
		return codigoOrgao;
	}

	public void setCodigoOrgao(String codigoOrgao) {
		this.codigoOrgao = codigoOrgao;
	}

	public OrgaoGenericoDTO codigoOrgaoPai(String codigoOrgaoPai) {
		this.codigoOrgaoPai = codigoOrgaoPai;
		return this;
	}

	public String getCodigoOrgaoPai() {
		return codigoOrgaoPai;
	}

	public void setCodigoOrgaoPai(String codigoOrgaoPai) {
		this.codigoOrgaoPai = codigoOrgaoPai;
	}

	public OrgaoGenericoDTO codigoTipoOrgao(String codigoTipoOrgao) {
		this.codigoTipoOrgao = codigoTipoOrgao;
		return this;
	}

	public String getCodigoTipoOrgao() {
		return codigoTipoOrgao;
	}

	public void setCodigoTipoOrgao(String codigoTipoOrgao) {
		this.codigoTipoOrgao = codigoTipoOrgao;
	}

	public OrgaoGenericoDTO nomeOrgao(String nomeOrgao) {
		this.nomeOrgao = nomeOrgao;
		return this;
	}

	public String getNomeOrgao() {
		return nomeOrgao;
	}

	public void setNomeOrgao(String nomeOrgao) {
		this.nomeOrgao = nomeOrgao;
	}

	public OrgaoGenericoDTO nomeTipoOrgao(String nomeTipoOrgao) {
		this.nomeTipoOrgao = nomeTipoOrgao;
		return this;
	}

	public String getNomeTipoOrgao() {
		return nomeTipoOrgao;
	}

	public void setNomeTipoOrgao(String nomeTipoOrgao) {
		this.nomeTipoOrgao = nomeTipoOrgao;
	}

	public OrgaoGenericoDTO numeroDeOrdem(Integer numeroDeOrdem) {
		this.numeroDeOrdem = numeroDeOrdem;
		return this;
	}

	public Integer getNumeroDeOrdem() {
		return numeroDeOrdem;
	}

	public void setNumeroDeOrdem(Integer numeroDeOrdem) {
		this.numeroDeOrdem = numeroDeOrdem;
	}

	public OrgaoGenericoDTO organizacaoInterna(List<OrgaoGenericoDTO> organizacaoInterna) {
		this.organizacaoInterna = organizacaoInterna;
		return this;
	}

	public OrgaoGenericoDTO addOrganizacaoInternaItem(OrgaoGenericoDTO organizacaoInternaItem) {
		if (this.organizacaoInterna == null) {
			this.organizacaoInterna = new ArrayList<>();
		}
		this.organizacaoInterna.add(organizacaoInternaItem);
		return this;
	}

	public List<OrgaoGenericoDTO> getOrganizacaoInterna() {
		return organizacaoInterna;
	}

	public void setOrganizacaoInterna(List<OrgaoGenericoDTO> organizacaoInterna) {
		this.organizacaoInterna = organizacaoInterna;
	}

	public OrgaoGenericoDTO segmentoJustica(String segmentoJustica) {
		this.segmentoJustica = segmentoJustica;
		return this;
	}

	public String getSegmentoJustica() {
		return segmentoJustica;
	}

	public void setSegmentoJustica(String segmentoJustica) {
		this.segmentoJustica = segmentoJustica;
	}

	public OrgaoGenericoDTO tribunal(TribunalDTO tribunal) {
		this.tribunal = tribunal;
		return this;
	}

	public TribunalDTO getTribunal() {
		return tribunal;
	}

	public void setTribunal(TribunalDTO tribunal) {
		this.tribunal = tribunal;
	}

}
