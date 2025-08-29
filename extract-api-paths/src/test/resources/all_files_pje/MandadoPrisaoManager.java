package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.MandadoPrisaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("mandadoPrisaoManager")
public class MandadoPrisaoManager extends MandadoAlvaraManager<MandadoPrisao, MandadoPrisaoDAO> {

	@In
	private MandadoPrisaoDAO mandadoPrisaoDAO;

	@In
	private DocumentoJudicialService documentoJudicialService;

	/*
	 * @In(create = true) private IcrSentencaCondenatoriaManager icrSCOManager;
	 */

	@Override
	protected MandadoPrisaoDAO getDAO(){
		return this.mandadoPrisaoDAO;
	}

	@Override
	public MandadoPrisao persist(MandadoPrisao entity) throws PJeBusinessException{

		if (entity.getRecaptura() == null){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.isRecaptura");
		}

		if (entity.getPublicacaoRestrita()){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.isPublicacaoRestrita");
		}

		if (entity.getRecaptura() == true && entity.getMandadoPrisaoOrigemRecaptura() == null){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.mandadoPrisaoOrigem");
		}

		if (entity.getTipoPrisao() == null){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.tipoPrisao");
		}

		if ((entity.getTipoPrisao() == TipoPrisaoEnum.PRV)){
			if(entity.getPrazoPrisao() == null){
				throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.prazoPrisao", null, entity.getTipoPrisao().getLabel());
			}
		}else{
			entity.setPrazoPrisao(null);
		}

		if (!(entity.getTipoPrisao() == TipoPrisaoEnum.PRV || entity.getTipoPrisao() == TipoPrisaoEnum.TMP)){
			entity.setPrisaoFlagrante(false);
		}else if (entity.getPrisaoFlagrante() == null){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.flagrante", null, TipoPrisaoEnum.PRV.getLabel(), TipoPrisaoEnum.TMP.getLabel());
		}

/*		if (entity.getTipoPrisao() == TipoPrisaoEnum.PRVDM && !entity.isCamposPenaTotalOk()){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.penaTotal", null, TipoPrisaoEnum.PRVDM.getLabel());
		}*/
		
		//RI130
		if(DateUtil.isDataMenor(entity.getDataValidade(), new Date())){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.dataValidadePrisaoInferior", null, entity.getNumeroExpediente());
		}
		
		//RN159
		if (entity.getTipoPrisao() == TipoPrisaoEnum.DEF && !entity.isCamposPenaTotalOk()){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.warn.penaTotal", null, TipoPrisaoEnum.DEF.getLabel());
		}

		if (entity.getRecaptura() == false){
			entity.setMandadoPrisaoOrigemRecaptura(null);
		}

		// RN179
		if (entity.getPublicacaoRestrita() == null && entity.getProcessoTrf().getSegredoJustica()){
			entity.setPublicacaoRestrita(entity.getProcessoTrf().getSegredoJustica());
		}
		
		if(entity.getTipoPrisao() != TipoPrisaoEnum.DEF){
			entity.setTipoPena(null);
			entity.setRegimePena(null);
			entity.setAnosPena(null);
			entity.setMesesPena(null);
			entity.setDiasPena(null);
			entity.setHorasPena(null);
		}

		if (entity.getPublicacaoRestrita()){
			entity.setInSigiloso(true);
		}

		return super.persist(entity);
	}

	@Override
	public MandadoPrisao getExpediente(){
		MandadoPrisao mandado = super.getExpediente();
		mandado.setRecaptura(false);
		mandado.setPublicacaoRestrita(true);
		mandado.setPrisaoFlagrante(false);
		ProcessoDocumento processoDocumento = documentoJudicialService.getDocumento();
		processoDocumento.setDocumentoSigiloso(true);
		processoDocumento.setTipoProcessoDocumento(getTipoProcessoDocumento());
		mandado.setProcessoDocumento(processoDocumento);
		return mandado;
	}

	public List<MandadoPrisao> recuperarDemaisMandadosDoProcesso(ProcessoTrf processo, Pessoa pessoa,
			MandadoPrisao mandadoPrisao, Boolean naoCumpridos) throws PJeBusinessException{
		try{
			return getDAO().recuperarDemaisMandadosDoProcesso(processo, pessoa, mandadoPrisao, naoCumpridos);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		}
	}

	public List<MandadoPrisao> recuperarMandadosPessoa(Pessoa pessoa, Boolean naoCumpridos) throws PJeBusinessException{
		try{
			return getDAO().recuperarMandadosPessoa(pessoa, naoCumpridos);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		}
	}

	public List<MandadoPrisao> recuperarMandados(Integer numero, Pessoa pessoa, SituacaoExpedienteCriminalEnum sitExpCrim) throws PJeBusinessException{

		if (pessoa == null){
			throw new PJeBusinessException("pje.mandadoPrisao.warning.recuperarMandados");
		}
		
		if (sitExpCrim == null){
			sitExpCrim = SituacaoExpedienteCriminalEnum.CP;
		}

		return getDAO().recuperarMandados(numero, pessoa, sitExpCrim);
	}

	@Override
	protected void setarSituacaoExpediente(MandadoPrisao expediente){
		if (expediente.getContraMandados() != null && !expediente.getContraMandados().isEmpty()) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.RV);// revogado
		}

		if (expediente.getDataCumprimento() != null) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.CP);// cumprido
		}

		if (expediente.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.PA) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.PC);// Pendente de cumprimento
		}

		if (expediente.getSituacaoExpedienteCriminal() == null) {
			expediente.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.PA);// Pendente de assinatura
		}
	}
	
	public MandadoPrisao gravarRevogacao(MandadoPrisao mandadoPrisao) throws PJeBusinessException{
		if(mandadoPrisao.getSituacaoExpedienteCriminal() == null || mandadoPrisao.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.PA){
			throw new PJeBusinessException("pje.mandadoPrisaoManager.error.revogarMandadoPrisao");
		}
		
		setarSituacaoExpediente(mandadoPrisao);
		
		return persist(mandadoPrisao);
	}
}
