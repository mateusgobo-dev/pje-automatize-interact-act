/**
 * ProcessoCriminalMNIParaColecaoInforamacaoCriminalDTOConverter.java
 * 
 * Data de criação: 18/01/2018
 */
package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v223.criminal.CaracteristicaFisica;
import br.jus.cnj.intercomunicacao.v223.criminal.Denuncia;
import br.jus.cnj.intercomunicacao.v223.criminal.Dispositivo;
import br.jus.cnj.intercomunicacao.v223.criminal.Parte;
import br.jus.cnj.intercomunicacao.v223.criminal.Processo;
import br.jus.cnj.pje.intercomunicacao.v223.servico.CorrelacaoParteCriminalPartePje;
import br.jus.cnj.pje.intercomunicacao.v223.servico.CorrelacaoParteCriminalPartePjeService;
import br.jus.cnj.pje.intercomunicacao.v223.servico.IntercomunicacaoService;
import br.jus.cnj.pje.intercomunicacao.v223.util.ConversorUtil;
import br.jus.cnj.pje.nucleo.manager.InformacaoCriminalRascunhoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.beans.criminal.CaracteristicaFisicaBean;
import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.EventoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.IncidenciaPenalBean;
import br.jus.pje.nucleo.dto.DispositivoDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.ProcessoParteMin;
import br.jus.pje.nucleo.entidades.ProcessoRascunho;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Conversor de Processo (criminal mni) para List de InformacaoCriminalDTO.
 * 
 * @author Adriano Pamplona
 */
@Name(ProcessoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter.NAME)
public class ProcessoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter
		extends
		IntercomunicacaoConverterAbstrato<Processo, List<InformacaoCriminalRascunho>> {

	public static final String NAME = "processoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter";
	
	/**
	 * @see ProcessoCriminalMNIParaColecaoInformacaoCriminalRascunhoConverter#converter(Processo, ProcessoTrf)
	 */
	@Override
	@Deprecated
	public List<InformacaoCriminalRascunho> converter(Processo processo) {
		// Usar converter(Processo, ProcessoTrf)
		return null;
	}
	
	/**
	 * Converte Processo criminal para List de InformacaoCriminalRascunho.
	 * OBS: SOMENTE PARTES DO POLO PASSIVO
	 * @param processoCriminalDTO 
	 * @param processo
	 * @param processoTrf
	 * @return List de InformacaoCriminalRascunho
	 * 
	 */
	public List<InformacaoCriminalRascunho> converter(ProcessoCriminalDTO processoCriminalDTO, Processo processo, ProcessoTrf processoTrf, ProcessoRascunho processoRascunho) {
		List<InformacaoCriminalRascunho> resultado = null;
		
		if (isNotNull(processo)) {
			resultado = new ArrayList<>();
			List<Parte> partesCriminal = processo.getPartes();
			
			CorrelacaoParteCriminalPartePjeService correlacaoParteService = IntercomunicacaoService.getInstance().getCorrelacaoParteService();
			correlacaoParteService.adicionarPartesCriminaisNaCorrelacaoPorNomeOuDocumento(partesCriminal);
		
			for (CorrelacaoParteCriminalPartePje correlacao : correlacaoParteService.getListaCorrelacao()) {
				ProcessoParteMin processoParteMin = new ProcessoParteMin(
						Long.valueOf(correlacao.getPartePje().getIdProcessoParte()),
						Long.valueOf(processoTrf.getIdProcessoTrf()),
						Long.valueOf(correlacao.getPartePje().getPessoa().getIdPessoa()),
						correlacao.getPartePje().getInSituacao(),
						Long.valueOf(correlacao.getPartePje().getTipoParte().getIdTipoParte()),
						correlacao.getPartePje().getPartePrincipal(),
						correlacao.getPartePje().getIsEnderecoDesconhecido());
				EventoCriminalBean eventoCriminalBean = obterColecaoEventoCriminalBean(correlacao.getParteCriminal(), processoCriminalDTO);
				ConteudoInformacaoCriminalBean conteudoInformacaoCriminalBean = new ConteudoInformacaoCriminalBean();
				conteudoInformacaoCriminalBean.setNomePrincipal(correlacao.getPartePje().getNomeParte());
				conteudoInformacaoCriminalBean.setCaracteristicasFisicas(obterColecaoCaracteristicaFisicaBean(correlacao.getParteCriminal()));
				
				ComponentUtil.getComponent(InformacaoCriminalRascunhoManager.class)
						.configurarEventoInicial(conteudoInformacaoCriminalBean, eventoCriminalBean, processoTrf);

				InformacaoCriminalRascunho rascunho = new InformacaoCriminalRascunho();
				rascunho.setProcessoParte(processoParteMin);
				rascunho.setProcessoRascunho(processoRascunho);
				rascunho.setInformacaoCriminal(conteudoInformacaoCriminalBean);
				
				resultado.add(rascunho);
			}

			if(processoTrf.getListaPartePrincipalPassivo().size() != resultado.size()){
				throw new RuntimeException("A lista de partes do polo passivo do cabeçalho do processo não corresponde à lista encontrada nas informações criminais.");
			}
		}
		
		return resultado;
	}


	/**
	 * Retorna uma coleção de características físicas.
	 * 
	 * @param parte Parte.
	 * @return Lista de CaracteristicaFisicaBean.
	 */
	private List<CaracteristicaFisicaBean> obterColecaoCaracteristicaFisicaBean(Parte parte) {
		List<CaracteristicaFisicaBean> resultado = new ArrayList<CaracteristicaFisicaBean>();
		List<CaracteristicaFisica> caracteristicasPessoais = parte.getCaracteristicasPessoais();
		for (CaracteristicaFisica caracteristicaFisica : caracteristicasPessoais) {
			CaracteristicaFisicaBean caracteristicaFisicaBean = new CaracteristicaFisicaBean();
			caracteristicaFisicaBean.setDsCaracteristica(caracteristicaFisica.value());
			
			resultado.add(caracteristicaFisicaBean);
		}
		
		return resultado;
	}

	/**
	 * Retorna uma lista de EventoCriminalBean.
	 * 
	 * @param parte Parte.
	 * @param processoCriminalDTO 
	 * @return Lista de EventoCriminalBean.
	 */
	private EventoCriminalBean obterColecaoEventoCriminalBean(Parte parte, ProcessoCriminalDTO processoCriminalDTO) {
		EventoCriminalBean eventoCriminalBean = null;
		
		List<Denuncia> denuncias = parte.getDenuncias();

		if (CollectionUtilsPje.isNotEmpty(denuncias)) {
			eventoCriminalBean = new EventoCriminalBean();
			eventoCriminalBean.setTipificacoes(new ArrayList<IncidenciaPenalBean>());
	
			if (denuncias.size() > 1) {
				throw new RuntimeException("É permitido apenas um tipo de denúncia por parte. Parte: " + parte.getNome());
			}
	
			Denuncia denuncia = denuncias.get(0);
			Date dataDenuncia = ConversorUtil.converterParaDate(denuncia.getData(), false);
			List<IncidenciaPenalBean> incidencias = converterEmIncidencias(dataDenuncia, denuncia.getDispositivos());
			eventoCriminalBean.getTipificacoes().addAll(incidencias);
			eventoCriminalBean.setTipoInformacaoCriminal(
					new TipoDenunciaParaTipoInformacaoCriminalConverter(processoCriminalDTO).converter(denuncia.getTipo()));
			eventoCriminalBean.setObservacao(denuncia.getObservacao());
		}

		return eventoCriminalBean;
	}

	private List<IncidenciaPenalBean> converterEmIncidencias(Date dtDato, List<Dispositivo> dispositivos) {
		List<IncidenciaPenalBean> incidencias = new ArrayList<IncidenciaPenalBean>();
		for (Dispositivo dispositivo : dispositivos) {
			IncidenciaPenalBean incidenciaPenalBean = new IncidenciaPenalBean();
			incidenciaPenalBean.setDispositivo(converterEmDispositivo(dispositivo));
			incidenciaPenalBean.setDtFato(dtDato);
			incidencias.add(incidenciaPenalBean);
		}
		return incidencias;
	}

	private DispositivoDTO converterEmDispositivo(Dispositivo dispositivo) {
		DispositivoDTO resultado = null;
		if (dispositivo != null && dispositivo.getId() != null) {
			
			Integer idDispositivo = null;
			try {
				idDispositivo = converterParaInt(dispositivo.getId());
			} catch (Exception e) {
				throw new RuntimeException("Não é um ID de dispositivo válido: " + dispositivo.getId());
			}
			
			resultado = new DispositivoDTO();
			resultado.setId(idDispositivo);
			
		}
		return resultado;
	}

}
