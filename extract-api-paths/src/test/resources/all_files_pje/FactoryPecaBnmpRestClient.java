package br.jus.cnj.pje.webservice.client.bnmp;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;


@Name(FactoryPecaBnmpRestClient.NAME)
@Scope(ScopeType.EVENT)
public class FactoryPecaBnmpRestClient {
	
	public static final String NAME = "factoryPecaBnmpRestClient";
	
	@In(create = true)
	private AlvaraSolturaRestClient alvaraSolturaRestClient;
	@In(create = true)
	private CertidaoArquivamentoGuiaRestClient certidaoArquivamentoGuiaRestClient;
	@In(create = true)
	private CertidaoCumprimentoMandadoInternacaoRestClient certidaoCumprimentoMandadoInternacaoRestClient;
	@In(create = true)
	private CertidaoCumprimentoMandadoPrisaoRestClient certidaoCumprimentoMandadoPrisaoRestClient;
	@In(create = true)
	private CertidaoExtincaoPunibilidadesRestClient certidaoExtincaoPunibilidadesRestClient;
	@In(create = true)
	private ContramandadoRestClient contramandadoRestClient ;
	@In(create = true)
	private GuiaInternacaoRestClient guiaInternacaoRestClient;
	@In(create = true)
	private GuiaRecolhimentoRestClient guiaRecolhimentoRestClient;
	@In(create = true)
	private MandadoInternacaoRestClient mandadoInternacaoRestClient;
	@In(create = true)
	private MandadoPrisaoRestClient mandadoPrisaoRestClient;
	@In(create = true)
	private OrdemDesinternacaoRestClient ordemDesinternacaoRestClient;
	
	private Map<TipoProcessoDocumentoBNMP, PecaBnmpRestClient<PecaDTO>> pecas = new LinkedHashMap<TipoProcessoDocumentoBNMP, PecaBnmpRestClient<PecaDTO>>();

	
	@Create
	public void init(){
		pecas.put(TipoProcessoDocumentoBNMP.ALVARA_SOLTURA, getAlvaraSolturaRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.CERTIDAO_ARQUIVAMENTO_GUIA, getCertidaoArquivamentoGuiaRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.CERTIDAO_CUMPRIMENTO_MANDADO_INTERNACAO, getCertidaoCumprimentoMandadoInternacaoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.CERTIDAO_CUMPRIMENTO_MANDADO_PRISAO, getCertidaoCumprimentoMandadoPrisaoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.CERTIDAO_EXTINCAO_PUNIBILIDADE_MORTE, getCertidaoExtincaoPunibilidadesRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.CONTRAMANDADO, getContramandadoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.GUIA_INTERNACAO, getGuiaInternacaoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.GUIA_RECOLHIMENTO, getGuiaRecolhimentoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.MANDADO_INTERNACAO, getMandadoInternacaoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.MANDADO_PRISAO, getMandadoPrisaoRestClient());
		pecas.put(TipoProcessoDocumentoBNMP.ORDEM_DESINTERNACAO, getOrdemDesinternacaoRestClient());
	}
	
	public PecaBnmpRestClient<PecaDTO> getPecaBnmpRestClientPor(TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP){

		if(pecas.containsKey(tipoProcessoDocumentoBNMP)) {
			return pecas.get(tipoProcessoDocumentoBNMP);
		}
		return null;
	}
	
	
	public AlvaraSolturaRestClient getAlvaraSolturaRestClient() {
		return alvaraSolturaRestClient;
	}
	public void setAlvaraSolturaRestClient(AlvaraSolturaRestClient alvaraSolturaRestClient) {
		this.alvaraSolturaRestClient = alvaraSolturaRestClient;
	}
	public CertidaoArquivamentoGuiaRestClient getCertidaoArquivamentoGuiaRestClient() {
		return certidaoArquivamentoGuiaRestClient;
	}
	public void setCertidaoArquivamentoGuiaRestClient(
			CertidaoArquivamentoGuiaRestClient certidaoArquivamentoGuiaRestClient) {
		this.certidaoArquivamentoGuiaRestClient = certidaoArquivamentoGuiaRestClient;
	}
	public CertidaoCumprimentoMandadoInternacaoRestClient getCertidaoCumprimentoMandadoInternacaoRestClient() {
		return certidaoCumprimentoMandadoInternacaoRestClient;
	}
	public void setCertidaoCumprimentoMandadoInternacaoRestClient(
			CertidaoCumprimentoMandadoInternacaoRestClient certidaoCumprimentoMandadoInternacaoRestClient) {
		this.certidaoCumprimentoMandadoInternacaoRestClient = certidaoCumprimentoMandadoInternacaoRestClient;
	}
	public CertidaoCumprimentoMandadoPrisaoRestClient getCertidaoCumprimentoMandadoPrisaoRestClient() {
		return certidaoCumprimentoMandadoPrisaoRestClient;
	}
	public void setCertidaoCumprimentoMandadoPrisaoRestClient(
			CertidaoCumprimentoMandadoPrisaoRestClient certidaoCumprimentoMandadoPrisaoRestClient) {
		this.certidaoCumprimentoMandadoPrisaoRestClient = certidaoCumprimentoMandadoPrisaoRestClient;
	}
	public CertidaoExtincaoPunibilidadesRestClient getCertidaoExtincaoPunibilidadesRestClient() {
		return certidaoExtincaoPunibilidadesRestClient;
	}
	public void setCertidaoExtincaoPunibilidadesRestClient(
			CertidaoExtincaoPunibilidadesRestClient certidaoExtincaoPunibilidadesRestClient) {
		this.certidaoExtincaoPunibilidadesRestClient = certidaoExtincaoPunibilidadesRestClient;
	}
	public ContramandadoRestClient getContramandadoRestClient() {
		return contramandadoRestClient;
	}
	public void setContramandadoRestClient(ContramandadoRestClient contramandadoRestClient) {
		this.contramandadoRestClient = contramandadoRestClient;
	}
	public GuiaInternacaoRestClient getGuiaInternacaoRestClient() {
		return guiaInternacaoRestClient;
	}
	public void setGuiaInternacaoRestClient(GuiaInternacaoRestClient guiaInternacaoRestClient) {
		this.guiaInternacaoRestClient = guiaInternacaoRestClient;
	}
	public GuiaRecolhimentoRestClient getGuiaRecolhimentoRestClient() {
		return guiaRecolhimentoRestClient;
	}
	public void setGuiaRecolhimentoRestClient(GuiaRecolhimentoRestClient guiaRecolhimentoRestClient) {
		this.guiaRecolhimentoRestClient = guiaRecolhimentoRestClient;
	}
	public MandadoInternacaoRestClient getMandadoInternacaoRestClient() {
		return mandadoInternacaoRestClient;
	}
	public void setMandadoInternacaoRestClient(MandadoInternacaoRestClient mandadoInternacaoRestClient) {
		this.mandadoInternacaoRestClient = mandadoInternacaoRestClient;
	}
	public MandadoPrisaoRestClient getMandadoPrisaoRestClient() {
		return mandadoPrisaoRestClient;
	}
	public void setMandadoPrisaoRestClient(MandadoPrisaoRestClient mandadoPrisaoRestClient) {
		this.mandadoPrisaoRestClient = mandadoPrisaoRestClient;
	}
	public OrdemDesinternacaoRestClient getOrdemDesinternacaoRestClient() {
		return ordemDesinternacaoRestClient;
	}
	public void setOrdemDesinternacaoRestClient(OrdemDesinternacaoRestClient ordemDesinternacaoRestClient) {
		this.ordemDesinternacaoRestClient = ordemDesinternacaoRestClient;
	}
}
