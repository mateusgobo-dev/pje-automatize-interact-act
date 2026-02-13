package br.com.infox.bpm.taskPage.remessacnj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.Manager;
import br.jus.cnj.pje.nucleo.manager.TipoParteConfiguracaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(ManifestacaoProcessualMetaDataManager.NAME)
public class ManifestacaoProcessualMetaDataManager implements Serializable,
		Manager<ManifestacaoProcessualMetaData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "manifestacaoProcessualMetaDataManager";

	@In
	private transient ParametroService parametroService;
	@In(create = true)
	private transient ProcessoTrfManager processoTrfManager;
	@In(create = true)
	private transient TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	@In(create = true)
	private transient AgrupamentoClasseJudicialManager agrupamentoClasseJudicialManager;
	@In(create = true)
	private transient PessoaService pessoaService;
	@In(create = true)
	private transient EnderecoManager enderecoManager;
	@In
	private transient TramitacaoProcessualService tramitacaoProcessualService;
	@In(create = true)
	private transient TipoParteManager tipoParteManager;
	@In(create = true)
	private transient TipoParteConfigClJudicialManager tipoParteConfigClJudicialManager;
	
	
	@Logger
	private Log log;

	@Override
	public ManifestacaoProcessualMetaData persist(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData)
			throws PJeBusinessException {
		// validar //TODO adicionar resource bundle
		// MN181
		if (manifestacaoProcessualMetaData.getClasseJudicial() == null) {
			throw new PJeBusinessException("É preciso uma classe judicial.");
		}

		// MN177
		if (manifestacaoProcessualMetaData.getAssuntos().isEmpty()) {
			throw new PJeBusinessException(
					"Deve existir pelo menos um assunto associado.");
		}

		boolean existeDocumentoSelecionado = false;
		boolean existeDocumentoPrincipal = false;
		for (ProcessoDocumento processoDocumento : manifestacaoProcessualMetaData
				.getDocumentos()) {
			if (((ProcessoDocumentoRemessa) processoDocumento).getSelecionado()) {
				existeDocumentoSelecionado = true;
			}
			if (((ProcessoDocumentoRemessa) processoDocumento)
					.getFlagPrincipal()) {
				existeDocumentoPrincipal = true;
			}
		}

		// MN178
		if (!existeDocumentoSelecionado) {
			throw new PJeBusinessException(
					"Deve existir pelo menos um documento juntado.");
		}

		// MN182
		if (!existeDocumentoPrincipal) {
			throw new PJeBusinessException(
					"Pelo menos um documento juntado deve ser escolhido como 'documento principal'.");
		}

		// MNI182
		if (manifestacaoProcessualMetaData.getPoloAtivo().isEmpty()) {
			throw new PJeBusinessException(
					"Deve existir pelo menos uma parte no polo ativo.");
		}
		// MN174
		if (manifestacaoProcessualMetaData.getClasseJudicial()
				.getReclamaPoloPassivo()
				&& manifestacaoProcessualMetaData.getPoloPassivo().isEmpty()) {
			throw new PJeBusinessException(
					"Deve existir pelo menos uma parte no polo passivo.");
		}

		ProcessoTrf processoTrf = processoTrfManager.find(ProcessoTrf.class,
				manifestacaoProcessualMetaData.getIdProcessoTrfOrigem());
		// MNI189
		if (processoTrf.getClasseJudicial().equals(
				manifestacaoProcessualMetaData.getClasseJudicial())) {
			throw new PJeBusinessException("Classe judicial não permitida.");
		}

		tramitacaoProcessualService.gravaVariavel(
				Variaveis.VARIAVEL_REMESSA_MANIFESTACAO_PROCESSUAL,
				manifestacaoProcessualMetaData);

		return manifestacaoProcessualMetaData;
	}

	@Override
	public void remove(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData)
			throws PJeBusinessException {
		tramitacaoProcessualService
				.apagaVariavel(Variaveis.VARIAVEL_REMESSA_MANIFESTACAO_PROCESSUAL);
	}

	public ManifestacaoProcessualMetaData get() {
		return (ManifestacaoProcessualMetaData) tramitacaoProcessualService
				.recuperaVariavel(Variaveis.VARIAVEL_REMESSA_MANIFESTACAO_PROCESSUAL);
	}

	@Override
	public List<ManifestacaoProcessualMetaData> findAll()
			throws PJeBusinessException {
		throw new UnsupportedOperationException("Não implementado");
	}

	@Override
	public ManifestacaoProcessualMetaData findById(Object id)
			throws PJeBusinessException {
		throw new UnsupportedOperationException("Não implementado");
	}

	@Override
	public ManifestacaoProcessualMetaData refresh(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData)
			throws PJeBusinessException {
		// classe judicial
		manifestacaoProcessualMetaData.setClasseJudicial(EntityUtil.find(
				ClasseJudicial.class, manifestacaoProcessualMetaData
						.getClasseJudicial().getIdClasseJudicial()));

		// assuntos
		List<AssuntoTrf> assuntos = new ArrayList<AssuntoTrf>(
				manifestacaoProcessualMetaData.getAssuntos().size());
		for (AssuntoTrf assunto : manifestacaoProcessualMetaData.getAssuntos()) {
			assuntos.add(EntityUtil.find(AssuntoTrf.class,
					assunto.getIdAssuntoTrf()));
		}
		manifestacaoProcessualMetaData.setAssuntos(assuntos);

		// documentos processuais
		for (ProcessoDocumento processoDocumentoRemessa : manifestacaoProcessualMetaData
				.getDocumentos()) {
			((ProcessoDocumentoRemessa) processoDocumentoRemessa)
					.setOriginal(EntityUtil.find(ProcessoDocumento.class,
							processoDocumentoRemessa.getIdProcessoDocumento()));
		}

		// partes
		for (ProcessoParte processoParte : manifestacaoProcessualMetaData
				.getPolos()) {
			processoParte.setPessoa(pessoaService.findById(processoParte
					.getPessoa().getIdUsuario()));
			for (ProcessoParteEndereco processoParteEndereco : processoParte
					.getProcessoParteEnderecoList()) {
				processoParteEndereco.setEndereco(enderecoManager
						.findById(processoParteEndereco.getEndereco()
								.getIdEndereco()));
			}
		}
		return manifestacaoProcessualMetaData;
	}

	public ManifestacaoProcessualMetaData create(Integer idProcessoTrf,
			ClasseJudicial classeJudicial, String codigoAgrupamentoAssunto)
			throws Exception {

		ProcessoTrf processoTrf = processoTrfManager.find(ProcessoTrf.class,
				idProcessoTrf);

		ManifestacaoProcessualMetaData manifestacaoProcessualMetaData = new ManifestacaoProcessualMetaData();

		manifestacaoProcessualMetaData.setClasseJudicial(classeJudicial);

		manifestacaoProcessualMetaData.setIdProcessoTrfOrigem(processoTrf
				.getIdProcessoTrf());
		manifestacaoProcessualMetaData.setSigiloso(processoTrf
				.getSegredoJustica());

		// adicionar os assuntos do processo original contidos no
		// agrupamento
		for (AssuntoTrf assuntoProcesso : processoTrf.getAssuntoTrfList()) {
			if (agrupamentoClasseJudicialManager.pertence(assuntoProcesso,
					codigoAgrupamentoAssunto)) {
				manifestacaoProcessualMetaData.getAssuntos().add(
						assuntoProcesso);
			}
		}

		loadProcessoDocumentoList(manifestacaoProcessualMetaData, processoTrf
				.getProcesso().getProcessoDocumentoList());

		if (classeJudicial != null) {
			loadPartes(manifestacaoProcessualMetaData,
					processoTrf.getProcessoParteList());
		}

		return manifestacaoProcessualMetaData;
	}

	private void loadProcessoDocumentoList(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData,
			List<ProcessoDocumento> originais) throws Exception {
		Set<ProcessoDocumento> documentosPrincipais = new HashSet<ProcessoDocumento>(
				0);
		manifestacaoProcessualMetaData.getDocumentos().clear();

		for (ProcessoDocumento processoDocumento : originais) {
			ProcessoDocumentoRemessa processoDocumentoRemessa = new ProcessoDocumentoRemessa();
			PropertyUtils.copyProperties(processoDocumentoRemessa,
					processoDocumento);
			processoDocumentoRemessa.setOriginal(processoDocumento);
			if (processoDocumentoRemessa.getDocumentoPrincipal() != null) {
				documentosPrincipais.add(processoDocumentoRemessa
						.getDocumentoPrincipal());
			}

			String idStringAplicacaoClasseEspecial = parametroService
					.valueOf(Parametros.ID_APLICACAO_CLASSE_ESPECIAL);
			
			Integer idAplicacaoClasseEspecial = null;
			
			if(idStringAplicacaoClasseEspecial != null){
				idAplicacaoClasseEspecial = Integer.parseInt(idStringAplicacaoClasseEspecial);
			}
			
			if(idAplicacaoClasseEspecial == null){
				log.warn("Parâmetro " + Parametros.ID_APLICACAO_CLASSE_ESPECIAL + " não configurado corretamente!");
				return;
			}
			
			TipoProcessoDocumento tipoDocumentoSTF = null;
			List<TipoProcessoDocumento> tiposProcessoDocumento = tipoProcessoDocumentoManager
					.findByAplicacaoClasse(idAplicacaoClasseEspecial,
							"%");

			for (TipoProcessoDocumento tipoProcessoDocumento : tiposProcessoDocumento) {
				if (tipoProcessoDocumento.getTipoProcessoDocumento()
						.equalsIgnoreCase(
								processoDocumentoRemessa
										.getTipoProcessoDocumento()
										.getTipoProcessoDocumento())) {
					tipoDocumentoSTF = tipoProcessoDocumento;
					break;
				}
			}

			processoDocumentoRemessa.setTipoDocumentoRemessa(tipoDocumentoSTF);
			manifestacaoProcessualMetaData.getDocumentos().add(
					processoDocumentoRemessa);

		}

		for (ProcessoDocumento processoDocumento : manifestacaoProcessualMetaData
				.getDocumentos()) {
			if (documentosPrincipais
					.contains(((ProcessoDocumentoRemessa) processoDocumento)
							.getOriginal())) {
				((ProcessoDocumentoRemessa) processoDocumento)
						.setFlagPrincipal(true);
			}
		}
	}

	public void loadPartes(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData,
			List<ProcessoParte> partes) {

		manifestacaoProcessualMetaData.getPoloAtivo().clear();
		manifestacaoProcessualMetaData.getPoloPassivo().clear();
		
		
		for (ProcessoParte parte : partes) {
			TipoParte tipoParte = getTipoParte(manifestacaoProcessualMetaData,	parte);
			
			/*
			 * loadPartes:1 - Não incluir partes inativas ou que o seu tipo não seja suportado na configuração da classe vinculada à manifestação.
			 */
			if (!parte.getIsAtivo() || tipoParte == null) {
				continue;
			}
			
			ProcessoParte novaParte = new ProcessoParte();
			novaParte.setPessoa(parte.getPessoa());
			novaParte.setTipoParte(tipoParte);

			novaParte.setInParticipacao(parte.getInParticipacao());
			for (ProcessoParteEndereco processoParteEndereco : parte
					.getProcessoParteEnderecoList()) {
				ProcessoParteEndereco novoProcessoParteEndereco = new ProcessoParteEndereco();
				novoProcessoParteEndereco.setProcessoParte(novaParte);
				novoProcessoParteEndereco.setEndereco(processoParteEndereco
						.getEndereco());
				novaParte.getProcessoParteEnderecoList().add(
						novoProcessoParteEndereco);
			}

			if (parte.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
				manifestacaoProcessualMetaData.getPoloAtivo().add(novaParte);
			} else if (parte.getInParticipacao() == ProcessoParteParticipacaoEnum.P
					&& manifestacaoProcessualMetaData.getClasseJudicial()
							.getReclamaPoloPassivo()) {
				manifestacaoProcessualMetaData.getPoloPassivo().add(novaParte);
			}
		}

		// percorrer novamente a lista para atualizar representantes das partes 
		for (ProcessoParte parteProcesso : partes) {
			for (ProcessoParteRepresentante processoParteRepresentante : parteProcesso
					.getProcessoParteRepresentanteList()) {

				ProcessoParteRepresentante newProcessoParteRepresentante = new ProcessoParteRepresentante();
				ProcessoParte representado = processoParteRepresentante
						.getProcessoParte();
				ProcessoParte representante = processoParteRepresentante
						.getParteRepresentante();
				
				

				List<ProcessoParte> todasPartesMetaData = new ArrayList<ProcessoParte>(
						manifestacaoProcessualMetaData.getPoloAtivo());

				if (manifestacaoProcessualMetaData.getClasseJudicial()
						.getReclamaPoloPassivo()) {
					todasPartesMetaData.addAll(manifestacaoProcessualMetaData
							.getPoloPassivo());
				}
				
				for (ProcessoParte parteManifestacao : todasPartesMetaData) {
					// representados (partes principais)
					if (representado.getPessoa().equals(
							parteManifestacao.getPessoa())
							&& parteManifestacao.getTipoParte()
									.getTipoPrincipal()) {

						newProcessoParteRepresentante
								.setProcessoParte(parteManifestacao);
						parteManifestacao.getProcessoParteRepresentanteList()
								.add(newProcessoParteRepresentante);
					}
					// representantes (partes não principais)
					else if (representante.getPessoa().equals(
							parteManifestacao.getPessoa())
							&& !parteManifestacao.getTipoParte()
									.getTipoPrincipal()) {
						newProcessoParteRepresentante
								.setParteRepresentante(parteManifestacao);
						newProcessoParteRepresentante
								.setTipoRepresentante(parteManifestacao
										.getTipoParte());
						parteManifestacao.getProcessoParteRepresentanteList2()
								.add(newProcessoParteRepresentante);
					}
					/**/
				}
				
				/*
				 * loadPartes:2 remover processoParteRepresentante inconsistentes. Motivo: "loadPartes:1"
				 */
				
			
				if(newProcessoParteRepresentante.getParteRepresentante() == null){
					newProcessoParteRepresentante.getProcessoParte().getProcessoParteRepresentanteList().remove(newProcessoParteRepresentante);
				}
				if(newProcessoParteRepresentante.getProcessoParte() == null){
					newProcessoParteRepresentante.getParteRepresentante().getProcessoParteRepresentanteList2().remove(newProcessoParteRepresentante);
				}
			}
		}
	}

	// recuperar o tipo de parte da classe judicial Selecionada.
	private TipoParte getTipoParte(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData,
			ProcessoParte processoParte) {
		
		if (processoParte.getTipoParte().getTipoPrincipal()) {
			if (processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
				return tipoParteManager.tipoPartePorClasseJudicial(manifestacaoProcessualMetaData.getClasseJudicial(), ProcessoParteParticipacaoEnum.A);
			} else {
				return tipoParteManager.tipoPartePorClasseJudicial(manifestacaoProcessualMetaData.getClasseJudicial(), ProcessoParteParticipacaoEnum.P);
			}
		} else if (manifestacaoProcessualMetaData.getClasseJudicial()
				.getTipoParteList().contains(processoParte.getTipoParte())) {
			return processoParte.getTipoParte();
		}
		
		return null;
	}

	public void invertPolos(ManifestacaoProcessualMetaData manifestacaoProcessualMetaData) {
		List<ProcessoParte> aux = new ArrayList<ProcessoParte>(manifestacaoProcessualMetaData.getPoloAtivo());
		manifestacaoProcessualMetaData.setPoloAtivo(manifestacaoProcessualMetaData.getPoloPassivo());
		manifestacaoProcessualMetaData.setPoloPassivo(aux);

		List<TipoParteConfigClJudicial> tpParteConfigClJudiciais = tipoParteConfigClJudicialManager
				.recuperarTipoParteConfiguracao(manifestacaoProcessualMetaData.getClasseJudicial());

		for (ProcessoParte parteAtiva : manifestacaoProcessualMetaData.getPoloAtivo()) {
			for (TipoParteConfigClJudicial tipoParteConfigClJudicial : tpParteConfigClJudiciais) {
				if (tipoParteConfigClJudicial.getTipoParteConfiguracao() != null && tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null	&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(parteAtiva.getTipoParte())) {
					if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(parteAtiva.getTipoParte())) {
						if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo()) {
							parteAtiva.setInParticipacao(ProcessoParteParticipacaoEnum.A);
							parteAtiva
									.setTipoParte(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte());
						}
					}

				}
			}

			for (ProcessoParte partePassiva : manifestacaoProcessualMetaData.getPoloPassivo()) {
				for (TipoParteConfigClJudicial tipoParteConfigClJudicial : tpParteConfigClJudiciais) {
					if (tipoParteConfigClJudicial.getTipoParteConfiguracao() != null
							&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null
							&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte()
									.equals(partePassiva.getTipoParte())) {
						if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(partePassiva.getTipoParte())) {
							if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo()) {
								partePassiva.setInParticipacao(ProcessoParteParticipacaoEnum.A);
								partePassiva.setTipoParte(
										tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte());
							}
						}

					}
				}

			}
		}
	}

	public void duplicatePolos(ManifestacaoProcessualMetaData manifestacaoProcessualMetaData) throws Exception {
		List<ProcessoParte> poloAtivo = new ArrayList<ProcessoParte>(manifestacaoProcessualMetaData.getPoloAtivo());
		List<ProcessoParte> poloPassivo = new ArrayList<ProcessoParte>(manifestacaoProcessualMetaData.getPoloPassivo());

		List<TipoParteConfigClJudicial> recuperarTipoParteConfiguracao = tipoParteConfigClJudicialManager
				.recuperarTipoParteConfiguracao(manifestacaoProcessualMetaData.getClasseJudicial());

		for (ProcessoParte ativo : manifestacaoProcessualMetaData.getPoloAtivo()) {
			ProcessoParte replicaAtivo = new ProcessoParte();

			for (TipoParteConfigClJudicial tipoParteConfigClJudicial : recuperarTipoParteConfiguracao) {
				if (tipoParteConfigClJudicial.getTipoParteConfiguracao() != null
						&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null
						&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte()
								.equals(ativo.getTipoParte())) {
					if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(ativo.getTipoParte())) {
						PropertyUtils.copyProperties(replicaAtivo, ativo);
						if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo()) {
							replicaAtivo
									.setTipoParte(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte());
							poloPassivo.add(replicaAtivo);
						}
					}

				}
			}
		}

		for (ProcessoParte passivo : manifestacaoProcessualMetaData.getPoloPassivo()) {
			ProcessoParte replicaPassiva = new ProcessoParte();
			for (TipoParteConfigClJudicial tipoParteConfigClJudicial : recuperarTipoParteConfiguracao) {
				if (tipoParteConfigClJudicial.getTipoParteConfiguracao() != null
						&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null
						&& tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte()
								.equals(passivo.getTipoParte())) {
					if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(passivo.getTipoParte())) {
						PropertyUtils.copyProperties(replicaPassiva, passivo);
						if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo()) {
							replicaPassiva
									.setTipoParte(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte());
							poloAtivo.add(replicaPassiva);
						}
					}

				}
			}
		}

		manifestacaoProcessualMetaData.setPoloAtivo(poloAtivo);
		manifestacaoProcessualMetaData.setPoloPassivo(poloPassivo);

	}

	//Remove a parte e a adiciona novamente. Necessário para atualizar as partes vinculadas
	public void updateParte(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData,
			ProcessoParte processoParte, ProcessoParte... partesVinculadas) throws PJeBusinessException {
		
		int indexParte = -1;

				
		if (processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
			indexParte = manifestacaoProcessualMetaData.getPoloAtivo().indexOf(processoParte);
			
		} else {
			indexParte = manifestacaoProcessualMetaData.getPoloPassivo().indexOf(processoParte);
		}
		
		removeParte(manifestacaoProcessualMetaData, processoParte);
		addParte(manifestacaoProcessualMetaData, processoParte, indexParte, partesVinculadas);
	}
	
	public boolean removeParte(ManifestacaoProcessualMetaData manifestacaoProcessualMetaData, ProcessoParte processoParte) {

		// limpar representantes
		for (ProcessoParteRepresentante processoParteRepresentante : processoParte
				.getProcessoParteRepresentanteList()) {
			processoParteRepresentante.getParteRepresentante()
					.getProcessoParteRepresentanteList2()
					.remove(processoParteRepresentante);
			// caso o representante represente somente esta parte, excluí-lo
			// tambem
			if (processoParteRepresentante.getParteRepresentante()
					.getProcessoParteRepresentanteList2().isEmpty()
					) {
				removeParte(manifestacaoProcessualMetaData,
						processoParteRepresentante.getParteRepresentante());
			}
		}

		// limpar representados
		for (ProcessoParteRepresentante processoParteRepresentante : processoParte
				.getProcessoParteRepresentanteList2()) {
			processoParteRepresentante.getProcessoParte()
					.getProcessoParteRepresentanteList()
					.remove(processoParteRepresentante);
		}

		processoParte.getProcessoParteRepresentanteList().clear();

		// remover parte
		if (processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
			return manifestacaoProcessualMetaData.getPoloAtivo().remove(processoParte);
		} else {
			return manifestacaoProcessualMetaData.getPoloPassivo().remove(processoParte);
		}
		
		
	}

	private void addParte(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData,
			ProcessoParte processoParte, Integer index, ProcessoParte... partesVinculadas)
			throws PJeBusinessException {
		
		
		if (!processoParte.getTipoParte().getTipoPrincipal()) {
			if (partesVinculadas != null && !(partesVinculadas.length == 0)) {
				for (ProcessoParte parteVinculada : partesVinculadas) {
					ProcessoParteRepresentante processoParteRepresentante = new ProcessoParteRepresentante();
					processoParteRepresentante
							.setParteRepresentante(processoParte);
					processoParteRepresentante
							.setTipoRepresentante(processoParte.getTipoParte());
					processoParteRepresentante.setProcessoParte(parteVinculada);
					processoParte.getProcessoParteRepresentanteList2().add(
							processoParteRepresentante);
					parteVinculada.getProcessoParteRepresentanteList().add(
							processoParteRepresentante);
				}
			} else {
				throw new PJeBusinessException(
						"Representante não possui partes vinculadas!");

			}
		}

		List<ProcessoParte> partes = null;
		if (processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
			partes = manifestacaoProcessualMetaData.getPoloAtivo();
		} else {
			partes = manifestacaoProcessualMetaData.getPoloPassivo();
		}

		for (ProcessoParte parte : partes) {
			if (parte.getPessoa().equals(processoParte.getPessoa())
					&& parte.getTipoParte()
							.equals(processoParte.getTipoParte())) {
				throw new PJeBusinessException("Parte já adicionada");
			}
		}
		if(index != null){
			partes.add(index,processoParte);
		}
		else{
			partes.add(processoParte);
		}
	}
	
	public void addParte(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData,
			ProcessoParte processoParte, ProcessoParte... partesVinculadas)
			throws PJeBusinessException {
		addParte(manifestacaoProcessualMetaData, processoParte,null, partesVinculadas);
	}
}

