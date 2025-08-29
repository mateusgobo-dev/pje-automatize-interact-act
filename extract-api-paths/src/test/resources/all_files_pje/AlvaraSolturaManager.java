package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.AlvaraSolturaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.AlvaraSoltura;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;

@Name("alvaraSolturaManager")
public class AlvaraSolturaManager extends MandadoAlvaraManager<AlvaraSoltura, AlvaraSolturaDAO>{

	@In
	private AlvaraSolturaDAO alvaraSolturaDAO;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@Override
	protected AlvaraSolturaDAO getDAO(){
		return alvaraSolturaDAO;
	}

	@Override
	public AlvaraSoltura persist(AlvaraSoltura entity) throws PJeBusinessException{

		if (entity.getMandadosAlcancados() == null || entity.getMandadosAlcancados().isEmpty()){
			throw new PJeBusinessException("pje.alvaraSolturaManager.error.mandadosNaoInformados");
		}

		return super.persist(entity);
	}

	@Override
	public AlvaraSoltura getExpediente(){
		AlvaraSoltura avara = super.getExpediente();
		ProcessoDocumento processoDocumento = documentoJudicialService.getDocumento();
		processoDocumento.setDocumentoSigiloso(true);
		processoDocumento.setTipoProcessoDocumento(getTipoProcessoDocumento());
		avara.setProcessoDocumento(processoDocumento);
		// avara.setDocumentoExistente(false);
		return avara;
	}

	@Override
	protected void setarSituacaoExpediente(AlvaraSoltura expediente){		
		if (expediente.getDataCumprimento() != null) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.CP);// cumprido
		}

		if (expediente.getProcessoDocumento() != null && !expediente.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.PC);// Pendente de cumprimento
		}

		if ((expediente.getProcessoDocumento() == null)
				|| (expediente.getProcessoDocumento() != null && expediente.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty())) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.PA);// Pendente de assinatura
		}
	}
	
	@Override
	public AlvaraSoltura gravarCumprimento(AlvaraSoltura alvaraSoltura) throws PJeBusinessException{
		if(alvaraSoltura.getPessoaMagistradoCumprimento() == null ){
			throw new PJeBusinessException("pje.alvaraSolturaManager.error.autoridadeCumprimentoNaoInformada");
		}
		
		if(alvaraSoltura.getCumpridoComSultura() == null){
			throw new PJeBusinessException("pje.alvaraSolturaManager.error.condicaoSolturaNaoInformda");
		}
		
		return super.gravarCumprimento(alvaraSoltura);
	}
}
