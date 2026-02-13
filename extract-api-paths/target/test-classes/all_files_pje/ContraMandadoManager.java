package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.ContraMandadoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ContraMandado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;

@Name("contraMandadoManager")
public class ContraMandadoManager extends ProcessoExpedienteCriminalManager<ContraMandado, ContraMandadoDAO>{

	@In
	private ContraMandadoDAO contraMandadoDAO;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@Override
	protected ContraMandadoDAO getDAO(){
		return contraMandadoDAO;
	}

	@Override
	public ContraMandado getExpediente(){
		ContraMandado contra = super.getExpediente();
		ProcessoDocumento processoDocumento = documentoJudicialService.getDocumento();
		processoDocumento.setDocumentoSigiloso(false);
		processoDocumento.setTipoProcessoDocumento(getTipoProcessoDocumento());
		contra.setProcessoDocumento(processoDocumento);
		// contra.setDocumentoExistente(false);
		return contra;
	}

	@Override
	public ContraMandado persist(ContraMandado entity) throws PJeBusinessException{
		if (entity.getMandadoPrisao() == null){
			throw new PJeBusinessException("pje.contraMandadoManager.error.mandadoNaoInformado");
		}

		return super.persist(entity);
	}

	@Override
	protected void setarSituacaoExpediente(ContraMandado expediente){
		if(expediente.getSituacaoExpedienteCriminal() == null){
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.PA);// Pendente de assinatura
		}else{
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.CP);// Cumprido
		}
	}
}
