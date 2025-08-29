package br.jus.cnj.pje.nucleo.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.InformacaoCriminalRascunhoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.EventoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.IncidenciaPenalBean;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminalEnum;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.StringUtil;

@Name(InformacaoCriminalRascunhoManager.NAME)
public class InformacaoCriminalRascunhoManager extends BaseManager<InformacaoCriminalRascunho>{

	public static final String NAME = "informacaoCriminalRascunhoManager";

	@In
	private InformacaoCriminalRascunhoDAO informacaoCriminalRascunhoDAO;
	
	@Override
	protected BaseDAO<InformacaoCriminalRascunho> getDAO() {
		return this.informacaoCriminalRascunhoDAO;
	}
	
	public InformacaoCriminalRascunho findByIdProcessoTrfAndIdProcessoParte(Integer idProcessoJudicial, Long idProcessoParte){
		return this.informacaoCriminalRascunhoDAO.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, idProcessoParte);
	}
	
	public List<InformacaoCriminalRascunho> findAllByIdProcessoTrf(Integer idProcessoJudicial){
		return this.informacaoCriminalRascunhoDAO.findAllByIdProcessoTrf(idProcessoJudicial);
	}
	
	public Boolean excluirInformacaoCriminalRascunho(Integer idProcessoJudicial, Long idProcessoParte){
		Boolean ret = Boolean.FALSE;
		
		InformacaoCriminalRascunho icRascunho = this.findByIdProcessoTrfAndIdProcessoParte(idProcessoJudicial, idProcessoParte);
		if(icRascunho != null){
			this.informacaoCriminalRascunhoDAO.remove(icRascunho);
			ret = Boolean.TRUE;
		}
		
		return ret;
	}
	
	public void configurarEventoInicial(ConteudoInformacaoCriminalBean conteudoInformacaoCriminalBean,
			EventoCriminalBean eventoCriminalBean, ProcessoTrf processoTrf) {
		TipoEventoCriminalEnum codigoTipoIcInformado = obterTipoEventoCriminalEnum(eventoCriminalBean);
		TipoEventoCriminalEnum codigoTipoIcClasse = converterEmTipoEventoCriminalEnum(processoTrf.getClasseJudicial().getTipoEventoCriminalInicial());

		if(codigoTipoIcInformado == null && codigoTipoIcClasse == null){
			return;
		}
		
		if (codigoTipoIcInformado == null && codigoTipoIcClasse != null) {
			throw new RuntimeException(
					String.format("A classe judicial '%s' exige o cadastro do evento criminal inicial: %s",
							processoTrf.getClasseJudicial(), codigoTipoIcClasse.getLabel()));
		}

		if (codigoTipoIcInformado != null && codigoTipoIcClasse == null) {
			throw new RuntimeException(
					String.format("A classe judicial '%s' não permite o cadastro de evento criminal inicial.",
							processoTrf.getClasseJudicial()));
		}

		if (codigoTipoIcInformado != null && codigoTipoIcClasse != null
				&& !codigoTipoIcInformado.equals(codigoTipoIcClasse)) {
			throw new RuntimeException(String.format(
					"A classe judicial '%s' não permite o cadastro desse evento criminal inicial: %s. Evento criminal esperado: %s",
					processoTrf.getClasseJudicial(), codigoTipoIcInformado.getLabel(), codigoTipoIcClasse.getLabel()));
		}

		switch (codigoTipoIcInformado) {
		case OFD:
		case OFR:
			conteudoInformacaoCriminalBean.setOferecimentoDenuncia(Arrays.asList(eventoCriminalBean));
			break;
		case IND:
			conteudoInformacaoCriminalBean.setIndiciamento(Arrays.asList(eventoCriminalBean));
			break;
		default:
			break;
		}
	}
	
	private TipoEventoCriminalEnum converterEmTipoEventoCriminalEnum(String tipoEventoCriminalInicial) {
		if (StringUtil.isEmpty(tipoEventoCriminalInicial)) {
			return null;
		}
		try {
			return TipoEventoCriminalEnum.valueOf(tipoEventoCriminalInicial);
		} catch (Exception e) {
			throw new RuntimeException("Tipo de evento criminal sem correlação com o 'TipoEventoCriminalEnum': " + tipoEventoCriminalInicial);
		}
	}

	private TipoEventoCriminalEnum obterTipoEventoCriminalEnum(EventoCriminalBean eventoCriminalBean) {
		return (eventoCriminalBean == null || eventoCriminalBean.getTipoInformacaoCriminal() == null) ? null
				: eventoCriminalBean.getTipoInformacaoCriminal().getCodTipoIc();
	}

	public List<IncidenciaPenalBean> getIncidenciasPenais(ConteudoInformacaoCriminalBean conteudoInformacaoCriminal,
			ProcessoTrf processoTrf) throws PJeBusinessException {
		EventoCriminalBean eventoCriminalInicial = getEventoCriminalInicial(conteudoInformacaoCriminal, processoTrf);

		if (eventoCriminalInicial == null || CollectionUtilsPje.isEmpty(eventoCriminalInicial.getTipificacoes())) {
			return Collections.emptyList();
		}
		return eventoCriminalInicial.getTipificacoes();
	}
	
	public EventoCriminalBean getEventoCriminalInicial(ConteudoInformacaoCriminalBean conteudoInformacaoCriminal, ProcessoTrf processoTrf) throws PJeBusinessException {
		
		if(conteudoInformacaoCriminal == null){
			return null;
		}
		
		String eventoCriminalInicialClasse = processoTrf.getClasseJudicial().getTipoEventoCriminalInicial();

		if(StringUtil.isNotEmpty(eventoCriminalInicialClasse)) {
			TipoEventoCriminalEnum criminalEnum = TipoEventoCriminalEnum.valueOf(eventoCriminalInicialClasse);
			List<EventoCriminalBean> eventoCriminal = null;
			switch (criminalEnum) {
				case OFD:
				case OFR:
					eventoCriminal = conteudoInformacaoCriminal.getOferecimentoDenuncia();
					break;
				case IND:
					eventoCriminal = conteudoInformacaoCriminal.getIndiciamento();	
					break;
				default:
					break;
			}
			if(CollectionUtilsPje.isNotEmpty(eventoCriminal)) {
				return eventoCriminal.get(0);
			}
		}
		return null;
	}
	
	
}
