package br.jus.pje.nucleo.beans.criminal;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConteudoInformacaoCriminalBean extends BaseBean{

	private static final long serialVersionUID = 1L;

	
	private List<EventoCriminalBean> indiciamento  = new ArrayList<EventoCriminalBean>();

	private String nomePrincipal;
	private List<PrisaoBean> prisoes;
	private List<SolturaBean> solturas;
	private List<FugaBean> fugas;
	private List<String> filiacoes;
	private List<DocumentoIdentificacaoBean> documentos;
	private List<CaracteristicaFisicaBean> caracteristicasFisicas;
	private List<String> nomes;
	
	private List<EventoCriminalBean> oferecimentoDenuncia  = new ArrayList<EventoCriminalBean>();
	private List<EventoCriminalBean> recebimentoDenuncia  = new ArrayList<EventoCriminalBean>();

	public ConteudoInformacaoCriminalBean() {
		super();
	}

	public ConteudoInformacaoCriminalBean(
			String nomePrincipal,
			List<PrisaoBean> prisoes, 
			List<SolturaBean> solturas, 
			List<FugaBean> fugas,
			List<String> filiacoes,
			List<DocumentoIdentificacaoBean> documentos,
			List<CaracteristicaFisicaBean> caracteristicasFisicas,
			List<String> nomes) {
		
		this.nomePrincipal = nomePrincipal;
		this.prisoes = prisoes;
		this.solturas = solturas;
		this.fugas = fugas;
		this.filiacoes = filiacoes;
		this.documentos = documentos;
		this.caracteristicasFisicas = caracteristicasFisicas;
		this.nomes = nomes;
	}
	
	public ConteudoInformacaoCriminalBean(String id, 
			String nomePrincipal,
			List<IncidenciaPenalBean> incidenciasPenais,
			List<PrisaoBean> prisoes, 
			List<SolturaBean> solturas, 
			List<FugaBean> fugas,
			List<String> filiacoes,
			List<DocumentoIdentificacaoBean> documentos,
			List<CaracteristicaFisicaBean> caracteristicasFisicas,
			List<String> nomes) {
		super(id);
		this.nomePrincipal = nomePrincipal;
		this.prisoes = prisoes;
		this.solturas = solturas;
		this.fugas = fugas;
		this.filiacoes = filiacoes;
		this.documentos = documentos;
		this.caracteristicasFisicas = caracteristicasFisicas;
		this.nomes = nomes;
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	public List<PrisaoBean> getPrisoes() {
		return prisoes;
	}

	public void setPrisoes(List<PrisaoBean> prisoes) {
		this.prisoes = prisoes;
	}

	public List<SolturaBean> getSolturas() {
		return solturas;
	}

	public void setSolturas(List<SolturaBean> solturas) {
		this.solturas = solturas;
	}

	public List<FugaBean> getFugas() {
		return fugas;
	}

	public void setFugas(List<FugaBean> fugas) {
		this.fugas = fugas;
	}

	public String getNomePrincipal() {
		return nomePrincipal;
	}

	public void setNomePrincipal(String nomePrincipal) {
		this.nomePrincipal = nomePrincipal;
	}

	public List<String> getFiliacoes() {
		return filiacoes;
	}

	public void setFiliacoes(List<String> filiacoes) {
		this.filiacoes = filiacoes;
	}

	public List<DocumentoIdentificacaoBean> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<DocumentoIdentificacaoBean> documentos) {
		this.documentos = documentos;
	}

	public List<CaracteristicaFisicaBean> getCaracteristicasFisicas() {
		return caracteristicasFisicas;
	}

	public void setCaracteristicasFisicas(List<CaracteristicaFisicaBean> caracteristicasFisicas) {
		this.caracteristicasFisicas = caracteristicasFisicas;
	}

	public List<String> getNomes() {
		return nomes;
	}

	public void setNomes(List<String> nomes) {
		this.nomes = nomes;
	}

	public List<EventoCriminalBean> getIndiciamento() {
		return indiciamento;
	}

	public void setIndiciamento(List<EventoCriminalBean> indiciamento) {
		this.indiciamento = indiciamento;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((caracteristicasFisicas == null) ? 0 : caracteristicasFisicas.hashCode());
		result = prime * result + ((documentos == null) ? 0 : documentos.hashCode());
		result = prime * result + ((filiacoes == null) ? 0 : filiacoes.hashCode());
		result = prime * result + ((fugas == null) ? 0 : fugas.hashCode());
		result = prime * result + ((indiciamento == null) ? 0 : indiciamento.hashCode());
		result = prime * result + ((nomePrincipal == null) ? 0 : nomePrincipal.hashCode());
		result = prime * result + ((nomes == null) ? 0 : nomes.hashCode());
		result = prime * result + ((prisoes == null) ? 0 : prisoes.hashCode());
		result = prime * result + ((solturas == null) ? 0 : solturas.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConteudoInformacaoCriminalBean other = (ConteudoInformacaoCriminalBean) obj;
		if (caracteristicasFisicas == null) {
			if (other.caracteristicasFisicas != null)
				return false;
		} else if (!caracteristicasFisicas.equals(other.caracteristicasFisicas))
			return false;
		if (documentos == null) {
			if (other.documentos != null)
				return false;
		} else if (!documentos.equals(other.documentos))
			return false;
		if (filiacoes == null) {
			if (other.filiacoes != null)
				return false;
		} else if (!filiacoes.equals(other.filiacoes))
			return false;
		if (fugas == null) {
			if (other.fugas != null)
				return false;
		} else if (!fugas.equals(other.fugas))
			return false;
		if (indiciamento == null) {
			if (other.indiciamento != null)
				return false;
		} else if (!indiciamento.equals(other.indiciamento))
			return false;
		if (nomePrincipal == null) {
			if (other.nomePrincipal != null)
				return false;
		} else if (!nomePrincipal.equals(other.nomePrincipal))
			return false;
		if (nomes == null) {
			if (other.nomes != null)
				return false;
		} else if (!nomes.equals(other.nomes))
			return false;
		if (prisoes == null) {
			if (other.prisoes != null)
				return false;
		} else if (!prisoes.equals(other.prisoes))
			return false;
		if (solturas == null) {
			if (other.solturas != null)
				return false;
		} else if (!solturas.equals(other.solturas))
			return false;
		return true;
	}

	public List<EventoCriminalBean> getOferecimentoDenuncia() {
		return oferecimentoDenuncia;
	}

	public void setOferecimentoDenuncia(List<EventoCriminalBean> oferecimentoDenuncia) {
		this.oferecimentoDenuncia = oferecimentoDenuncia;
	}

	public List<EventoCriminalBean> getRecebimentoDenuncia() {
		return recebimentoDenuncia;
	}

	public void setRecebimentoDenuncia(List<EventoCriminalBean> recebimentoDenuncia) {
		this.recebimentoDenuncia = recebimentoDenuncia;
	}


}
