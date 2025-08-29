package br.jus.csjt.pje.business.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.Transformer;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoDocumentoPeticaoNaoLidaHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.HabilitacaoAutosManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoPeticaoNaoLidaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.CustomJbpmTransactional;
import br.jus.cnj.pje.util.CustomJbpmTransactionalClass;
import br.jus.pje.jt.entidades.HabilitacaoAutos;
import br.jus.pje.jt.enums.SituacaoHabilitacaoEnum;
import br.jus.pje.jt.enums.TipoDeclaracaoEnum;
import br.jus.pje.jt.enums.TipoMetodoHabilitacaoEnum;
import br.jus.pje.jt.enums.TipoSolicitacaoHabilitacaoEnum;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.DateUtil;


@Name(HabilitacaoAutosService.NAME)
@CustomJbpmTransactionalClass
public class HabilitacaoAutosService extends BaseService {
	
	public static final String NAME = "habilitacaoAutosService"; 

	@In(create = true)
	private HabilitacaoAutosManager habilitacaoAutosManager;
	
	@In(create = true)
	private ProcessoDocumentoHome processoDocumentoHome;
	
	@In(create = true)
	private AnexarDocumentos anexarDocumentos;
	
	@In(create = true)
	ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager;
	
	@In
	ProcessoParteManager processoParteManager;
	
	@In
	ProcessoExpedienteManager processoExpedienteManager;
	
	@Logger
	private Log log;
	

	/**
	 * Instancia um objeto HabilitacaoAutos devidamente preenchido com as informações da tela.
	 * @return o objeto HabilitacaoAutos criado.
	 */
	public HabilitacaoAutos instanciarHabilitacaoAutos(PessoaAdvogado pessoaAdvogado, 
													   Usuario        usuarioSolicitante,
													   ProcessoTrf processoTrf, 
													   List<ProcessoParte> processoParteList, 
													   TipoDeclaracaoEnum tipoDeclaracao,
													   TipoSolicitacaoHabilitacaoEnum tipoDeHabilitacao) {
		
		ProcessoParte parteAdvogado = null;
		TipoParte     tipoParteAdv  = ParametroUtil.instance().getTipoParteAdvogado();
		
		HabilitacaoAutos habilitacaoAutos = new HabilitacaoAutos();

		habilitacaoAutos.setDataHora(new Date());
		habilitacaoAutos.setAdvogado(pessoaAdvogado);
		habilitacaoAutos.setUsuarioSolicitante(usuarioSolicitante);
		habilitacaoAutos.setProcesso(processoTrf);
		habilitacaoAutos.setRepresentados(processoParteList);
		habilitacaoAutos.setSituacaoHabilitacao(SituacaoHabilitacaoEnum.A);
		habilitacaoAutos.setTipoDeclaracao(tipoDeclaracao);
		habilitacaoAutos.setMetodoHabilitacao(TipoMetodoHabilitacaoEnum.A);
		habilitacaoAutos.setTipoSolicitacaoHabilitacao(tipoDeHabilitacao);

		
		for(ProcessoParte pp : processoTrf.getListaPartePoloObj(true, processoParteList.get(0).getInParticipacao())){
			
			if(pp.getIdPessoa().equals(pessoaAdvogado.getIdUsuario()) && pp.getTipoParte().equals(tipoParteAdv)){
				parteAdvogado = pp;
				parteAdvogado.setInSituacao(ProcessoParteSituacaoEnum.A);
				break;
			}
		}
		
		if(parteAdvogado == null){
		
			// criar ProcessoParte do Advogado
			parteAdvogado = new ProcessoParte();
			
			parteAdvogado.setInParticipacao(processoParteList.get(0).getInParticipacao());
			parteAdvogado.setInSituacao(ProcessoParteSituacaoEnum.A);
			parteAdvogado.setPessoa(pessoaAdvogado.getPessoa());
			parteAdvogado.setProcessoTrf(processoTrf);
			parteAdvogado.setTipoParte(ParametroUtil.instance().getTipoParteAdvogado());
			parteAdvogado.setParteSigilosa(false);
			parteAdvogado.setPartePrincipal(false);
	
			// endereço do advogado
			if ((pessoaAdvogado.getEnderecoList() != null) && (pessoaAdvogado.getEnderecoList().size() > 0)) {
				ProcessoParteEndereco processoParteEndereco = new ProcessoParteEndereco();
				processoParteEndereco.setProcessoParte(parteAdvogado);
	
				Endereco endereco = null;
				Endereco enderecoCadastrado;
	
				for (int i = 0; (i < pessoaAdvogado.getEnderecoList().size()) && (endereco == null); i++) {
					enderecoCadastrado = pessoaAdvogado.getEnderecoList().get(i);
					if ((enderecoCadastrado.getCorrespondencia() != null) && (enderecoCadastrado.getCorrespondencia())) {
						endereco = enderecoCadastrado;
					}
				}
	
				if (endereco == null) {
					endereco =  pessoaAdvogado.getEnderecoList().get(0);
	
					// pega o endereço com data de atualização mais recente ou, caso não exista,
					// o endereço com maior ID
					for (int i = 1; i < pessoaAdvogado.getEnderecoList().size(); i++) {
						enderecoCadastrado = pessoaAdvogado.getEnderecoList().get(i);
	
						if (endereco.getDataAlteracao() == null) {
							if (enderecoCadastrado.getIdEndereco() > endereco.getIdEndereco()) {
								endereco = enderecoCadastrado;
							}
						}
						else if (enderecoCadastrado.getDataAlteracao() != null) {
							if (enderecoCadastrado.getDataAlteracao().after(endereco.getDataAlteracao())) {
								endereco = enderecoCadastrado;
							}
						}
					}
				}
	
				processoParteEndereco.setEndereco(endereco);
				List<ProcessoParteEndereco> processoParteEnderecoList = new ArrayList<ProcessoParteEndereco>(1);
				processoParteEnderecoList.add(processoParteEndereco);
	
				parteAdvogado.setProcessoParteEnderecoList(processoParteEnderecoList);
			}
		}


		ProcessoParteRepresentanteManager processoParteRepresentanteManager  = ComponentUtil.getComponent("processoParteRepresentanteManager");
		
		// associar os representados com o representante
		for (ProcessoParte processoParte : processoParteList) {
			
			ProcessoParteRepresentante processoParteRepresentante = processoParteRepresentanteManager.consultarProcessoParteRepresentante(parteAdvogado.getPessoa(), processoParte.getPessoa(), processoTrf);

			if(processoParteRepresentante == null){
				processoParteRepresentante = new ProcessoParteRepresentante();
				processoParteRepresentante.setParteRepresentante(parteAdvogado);
				processoParteRepresentante.setProcessoParte(processoParte);
				processoParteRepresentante.setRepresentante(pessoaAdvogado.getPessoa());
				processoParteRepresentante.setTipoRepresentante(parteAdvogado.getTipoParte());
			}else{
				if(processoParteRepresentante.getInSituacao() != ProcessoParteSituacaoEnum.A){
					processoParteRepresentante.setInSituacao(ProcessoParteSituacaoEnum.A);
				}
			}

			processoParte.getProcessoParteRepresentanteList().add(processoParteRepresentante);
		}
		
		return habilitacaoAutos;
	}
	
	//
	public HabilitacaoAutos instanciarHabilitacaoAutosDefensoria(PessoaAdvogado pessoaAdvogado, Usuario usuarioSolicitante, ProcessoTrf processoTrf, List<ProcessoParte> processoParteList,
			   TipoDeclaracaoEnum tipoDeclaracao, TipoSolicitacaoHabilitacaoEnum tipoDeHabilitacao,List<ProcessoParteRepresentante> representanteRemovidoList,
			   Procuradoria procuradoria, Boolean isSalvarAutomatico,List<Procuradoria> defensoriaRemovidaList) throws PJeBusinessException{
		
		HabilitacaoAutos habilitacaoAutos = new HabilitacaoAutos();

		habilitacaoAutos.setDataHora(new Date());
		habilitacaoAutos.setAdvogado(pessoaAdvogado);
		habilitacaoAutos.setUsuarioSolicitante(usuarioSolicitante);
		habilitacaoAutos.setProcesso(processoTrf);
		if(isSalvarAutomatico){
			habilitacaoAutos.setRepresentados(processoParteList);
			habilitacaoAutos.setSituacaoHabilitacao(SituacaoHabilitacaoEnum.A);
			habilitacaoAutos.setTipoDeclaracao(tipoDeclaracao);
			habilitacaoAutos.setMetodoHabilitacao(TipoMetodoHabilitacaoEnum.A);
			habilitacaoAutos.setTipoSolicitacaoHabilitacao(tipoDeHabilitacao);
			
		}else{
			habilitacaoAutos.setRepresentados(new ArrayList<ProcessoParte>(0));
			habilitacaoAutos.setSituacaoHabilitacao(SituacaoHabilitacaoEnum.A);
			habilitacaoAutos.setMetodoHabilitacao(TipoMetodoHabilitacaoEnum.M);
			habilitacaoAutos.setTipoSolicitacaoHabilitacao(null);
		}
		habilitacaoAutos.setProcuradoria(procuradoria);
		habilitacaoAutos.setRepresentantesRemovidos(getRepresentantesRemovidosInativados(processoTrf, representanteRemovidoList, usuarioSolicitante, procuradoria));
		habilitacaoAutos.setDefensoriasRemovidas(defensoriaRemovidaList);
		habilitacaoAutos.getDocumentos().addAll(obterDocumentosSalvosHabilitacaoAutos());
		
		return habilitacaoAutos;
	}
	
	public HabilitacaoAutos instanciarHabilitacaoAutosManual(PessoaAdvogado pessoaAdvogado, 
															 Usuario usuarioSolicitante,
															 ProcessoTrf processoTrf) {

		HabilitacaoAutos habilitacaoAutos = new HabilitacaoAutos();

		habilitacaoAutos.setDataHora(new Date());
		habilitacaoAutos.setAdvogado(pessoaAdvogado);
		habilitacaoAutos.setUsuarioSolicitante(usuarioSolicitante);
		habilitacaoAutos.setProcesso(processoTrf);
		habilitacaoAutos.setRepresentados(new ArrayList<ProcessoParte>(0));
		habilitacaoAutos.setSituacaoHabilitacao(SituacaoHabilitacaoEnum.A);
		habilitacaoAutos.setTipoSolicitacaoHabilitacao(null);
		habilitacaoAutos.setMetodoHabilitacao(TipoMetodoHabilitacaoEnum.M);


		return habilitacaoAutos;
	}

	public List<ProcessoDocumento> obterDocumentosSalvosHabilitacaoAutos(){
	
		return anexarDocumentos.getDocumentosSalvos();
	}
	

	@CustomJbpmTransactional
	public void assinarFinalizarHabilitacaoAutos(PessoaAdvogado pessoaAdvogado,
												 Usuario        usuarioSolicitante, 
												 ProcessoTrf processoTrf,
												 List<ProcessoParte> processoParteList,
												 List<ProcessoParteRepresentante> representanteRemovidoList,
												 TipoDeclaracaoEnum tipoDeclaracao,
												 TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao) throws PJeBusinessException {
				
		try{
			
			try{
				
				processoDocumentoHome.assinarDocumentoHabilitacaoAutos();
				FacesMessages.instance().clear();
			
			} catch (Exception e){
					
					log.error("Erro ao assinar documentos ao efetuar a habilitação nos autos.", e);
					throw new PJeBusinessException("Erro ao assinar documentos ao efetuar a habilitação nos autos.", e);
					
			}
			
			salvarHabilitacaoAutomatica(pessoaAdvogado, usuarioSolicitante, processoTrf, processoParteList, representanteRemovidoList, tipoDeclaracao, tipoSolicitacaoHabilitacao);
		
		} catch (Exception e){
			
			log.error("Erro ao persirtir documentos ao efetuar a habilitação nos autos.", e);
			throw new PJeBusinessException("Erro ao persirtir documentos ao efetuar a habilitação nos autos.", e);
			
		}
		
	}
	
	public void assinarFinalizarHabilitacaoAutosMultiplusDocumentos(PessoaAdvogado pessoaAdvogado,
																	Usuario usuarioSolicitante,  
																	ProcessoTrf processoTrf, 
																	List<ProcessoParte> processoParteList,
																	List<ProcessoParteRepresentante> representanteRemovidoList,
																	TipoDeclaracaoEnum tipoDeclaracao,
																	TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao) throws PJeBusinessException {
		
		 
		
		try{
		
			salvarHabilitacaoAutomatica(pessoaAdvogado, usuarioSolicitante, processoTrf, processoParteList, representanteRemovidoList, tipoDeclaracao, tipoSolicitacaoHabilitacao);
		
		} catch (Exception e){
		
			habilitacaoAutosManager.clear();
			
			//Remove os documentos inseridos
			List<ProcessoDocumento> documentosSalvos = obterDocumentosSalvosHabilitacaoAutos();
			for (ProcessoDocumento processoDocumento : documentosSalvos){
				
				
				//Remove do agrupador
				ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida = processoDocumentoPeticaoNaoLidaManager.obterProcessoDocumentoPeticaoNaoLida(processoDocumento);
				
				if(processoDocumentoPeticaoNaoLida != null){
					processoDocumentoPeticaoNaoLidaManager.remove(processoDocumentoPeticaoNaoLida);
				}
				
				//Remove documento
				anexarDocumentos.removerPdf(processoDocumento);
			}
			
			log.error("Erro ao persirtir documentos ao efetuar a habilitação nos autos.", e);
			throw new PJeBusinessException("Erro ao persirtir documentos ao efetuar a habilitação nos autos.", e);
			
		}
	}

	/**
	 * Grava no sistema uma solicitação de habilitação nos autos, essa solicitação não faz alterações no processo.<br>
	 * 
	 * Cria uma nova instância do fluxo de petição de incidental.
	 * 
	 * @param pessoaAdvogado Advogado que solicitou a habilitação nos autos.
	 * @param processoTrf Processo no qual o advogado será habilitado.
	 * @return HabilitacaoAutos Entidade que presenta a habilitação nos autos.
	 * @throws PJeBusinessException
	 */
	public HabilitacaoAutos salvarHabilitacaoManual(PessoaAdvogado pessoaAdvogado, ProcessoTrf processoTrf) throws PJeBusinessException{
		
		HabilitacaoAutos habilitacaoAutos = instanciarHabilitacaoAutosManual(pessoaAdvogado, 
																			 pessoaAdvogado.getPessoa(), 
																			 processoTrf);
		
		List<ProcessoDocumento> documentosSalvos = obterDocumentosSalvosHabilitacaoAutos();
		 
		setDataJuntada(documentosSalvos, processoTrf);
		habilitacaoAutos.getDocumentos().addAll(documentosSalvos);		
		
		habilitacaoAutosManager.persistAndFlush(habilitacaoAutos);
		
		ProcessoDocumentoPeticaoNaoLida processo = ProcessoDocumentoPeticaoNaoLidaHome.instance().getInstance();
		processo.setRetificado(false);
		processo.setRetirado(false);
		processo.setHabilitacaoAutos(habilitacaoAutos);
		
		for (ProcessoDocumento processoDocumento : documentosSalvos) {
			if(processoDocumento.getDocumentoPrincipal() == null) {
				processo.setProcessoDocumento(processoDocumento);
				 if (processoDocumento.getTipoProcessoDocumento().getFluxo() != null) {
					 Events.instance().raiseAsynchronousEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, processoDocumento.getIdProcessoDocumento());
				 }
				break;
			}
		}
		
		processoDocumentoPeticaoNaoLidaManager.persistAndFlush(processo);
		
		return habilitacaoAutos;
	}

	public HabilitacaoAutos salvarHabilitacaoManualDefensoria(PessoaAdvogado pessoaAdvogado, 
			Usuario usuarioSolicitante,
			ProcessoTrf processoTrf, 
			List<ProcessoParte> processoParteList,
			List<ProcessoParteRepresentante> representanteRemovidoList,
			TipoDeclaracaoEnum tipoDeclaracao,
			TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao,
			Procuradoria procuradoria,
			List<Procuradoria> defensoriaRemovidaList) throws PJeBusinessException{
		
		HabilitacaoAutos habilitacaoAutos = instanciarHabilitacaoAutosDefensoria(pessoaAdvogado, usuarioSolicitante, processoTrf, processoParteList,
				tipoDeclaracao, tipoSolicitacaoHabilitacao, representanteRemovidoList, procuradoria, false, defensoriaRemovidaList);
		
		List<ProcessoDocumento> documentosSalvos = obterDocumentosSalvosHabilitacaoAutos();
		 
		setDataJuntada(documentosSalvos, processoTrf);
		habilitacaoAutos.getDocumentos().addAll(documentosSalvos);		
		
		habilitacaoAutosManager.persistAndFlush(habilitacaoAutos);
		
		ProcessoDocumentoPeticaoNaoLida processo = initPeticaoNaoLidaProcesso(habilitacaoAutos);
		
		processoDocumentoPeticaoNaoLidaManager.persistAndFlush(processo);
		
		return habilitacaoAutos;
	}

	/**
	 * Realiza uma habilitação nos autos de acordo com os parâmetros informados. <br>
	 * Os advogados que forem habilitados por substabelecimento ou substituídos serão notificados por meio de expediente no próprio sistema, 
	 * caso os mesmos não ainda não possuam acesso ao PJE será disparado um fluxo (a ser definido pelo parâmetro: pje:fluxo:notificarHabilitacaoAutos)
	 * para que o orgão julgado faça a comunicação de outra forma. <br>
	 * 
	 * Ao final da habilitação nos autos será disparado o fluxo de petição de incidental para o processo.
	 * 
	 * 
	 * @param pessoaAdvogado Advogado que deverá ser habilitado nos autos.
	 * @param usuarioSolicitante Advogado que solicitou a habilitação nos autos.
	 * @param processoTrf Processo no qual o advogado será habilitado.
	 * @param processoParteList Partes que serão representadas pelo advogado.
	 * @param representanteRemovidoList Advogados substituídos, os expedientes destes advogados no processo serão transferidos para o novo advogado.
	 * @param tipoDeclaracao Tipo de declaração.
	 * @param tipoSolicitacaoHabilitacao Tipo de habilitação (Simples ou Substabelecimento)
	 * @return HabilitacaoAutos Entidade que presenta a habilitação nos autos.
	 * @throws PJeBusinessException
	 */
	public HabilitacaoAutos salvarHabilitacaoAutomatica(PessoaAdvogado pessoaAdvogado, 
														Usuario        usuarioSolicitante,
														ProcessoTrf processoTrf, 
														List<ProcessoParte> processoParteList,
														List<ProcessoParteRepresentante> representanteRemovidoList,
														TipoDeclaracaoEnum tipoDeclaracao,
														TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao) throws PJeBusinessException{
		
		
			//Verificando se houve substituição de advogado e redefinindo o tipo de solicitação de habilitação.
			if(representanteRemovidoList != null && representanteRemovidoList.size() > 0){
				if(tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.S){
					tipoSolicitacaoHabilitacao = TipoSolicitacaoHabilitacaoEnum.R;
				}else if(tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.I){
					tipoSolicitacaoHabilitacao = TipoSolicitacaoHabilitacaoEnum.T;
				}
			}
			
			HabilitacaoAutos habilitacaoAutos = instanciarHabilitacaoAutos(pessoaAdvogado, 
																		   usuarioSolicitante, 
																		   processoTrf,
																		   processoParteList, 
																		   tipoDeclaracao, 
																		   tipoSolicitacaoHabilitacao
																		   );

			
			List<ProcessoDocumento> documentosSalvos = obterDocumentosSalvosHabilitacaoAutos();
			 
			//ISSUE PJEII-17314
			setDataJuntada(documentosSalvos, processoTrf);
			//FIM PJEII-17314
			habilitacaoAutos.getDocumentos().addAll(documentosSalvos);		
			
			if(representanteRemovidoList != null && representanteRemovidoList.size() > 0){
				inativarRepresentante(processoTrf, representanteRemovidoList, usuarioSolicitante, pessoaAdvogado, documentosSalvos.get(0),null);
				habilitacaoAutos.setRepresentantesRemovidos(representanteRemovidoList);
			}
			
			habilitacaoAutosManager.persistAndFlush(habilitacaoAutos);
			
			if (representanteRemovidoList!=null) {
				@SuppressWarnings("unchecked")
				Collection<Pessoa> colecaoTmp = CollectionUtilsPje.collect(representanteRemovidoList, new Transformer() { 
																			public Pessoa transform(Object input) {
																				return ((ProcessoParteRepresentante)input).getRepresentante();}
																			});
	
				//Gerar expedientes para os advogados removidos.
				gerarAtoDeComunicacao(processoTrf.getIdProcessoTrf(), documentosSalvos.get(0).getIdProcessoDocumento(), (PessoaFisica[]) colecaoTmp.toArray(new PessoaFisica[0]));
			}
			
			if (documentosSalvos.size()>0) {
			
				//Em caso de habilitação nos autos por substabelecimento comunicar o novo adovgado.
				if(tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.I || tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.T){
					gerarAtoDeComunicacao(processoTrf.getIdProcessoTrf(), documentosSalvos.get(0).getIdProcessoDocumento(), pessoaAdvogado.getPessoa());
				}
				
				ProcessoDocumentoPeticaoNaoLida processo = ProcessoDocumentoPeticaoNaoLidaHome.instance().getInstance();
				processo.setRetificado(false);
				processo.setRetirado(false);
				processo.setHabilitacaoAutos(habilitacaoAutos);
				for (ProcessoDocumento processoDocumento : documentosSalvos) {
					if(processoDocumento.getDocumentoPrincipal() == null) {
						processo.setProcessoDocumento(processoDocumento);
						 if (processoDocumento.getTipoProcessoDocumento().getFluxo() != null) {
							 Events.instance().raiseAsynchronousEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, processoDocumento.getIdProcessoDocumento());
						 }
						break;
					}
				}
				processoDocumentoPeticaoNaoLidaManager.persistAndFlush(processo);
			}
			
			return habilitacaoAutos;
	}
	
	
	public HabilitacaoAutos salvarHabilitacaoAutomaticaDefensoria(PessoaAdvogado pessoaAdvogado, 
			Usuario usuarioSolicitante,
			ProcessoTrf processoTrf, 
			List<ProcessoParte> processoParteList,
			List<ProcessoParteRepresentante> representanteRemovidoList,
			TipoDeclaracaoEnum tipoDeclaracao,
			TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao,
			Procuradoria procuradoria,
			List<Procuradoria> defensoriaRemovidaList) throws PJeBusinessException{
		
		HabilitacaoAutos habilitacaoAutos = instanciarHabilitacaoAutosDefensoria(pessoaAdvogado, usuarioSolicitante, processoTrf, processoParteList, 
				tipoDeclaracao, tipoSolicitacaoHabilitacao, representanteRemovidoList, procuradoria, true, defensoriaRemovidaList);
		
		setDataJuntada(obterDocumentosSalvosHabilitacaoAutos(), processoTrf);
		
		habilitacaoAutosManager.persistAndFlush(habilitacaoAutos);
		
		gerarAtoParaRepresentantes(representanteRemovidoList, processoTrf, tipoSolicitacaoHabilitacao, procuradoria);
		
		gerarAtoParaDefensorias(defensoriaRemovidaList, processoTrf, tipoSolicitacaoHabilitacao, processoParteList);
		
		ProcessoDocumentoPeticaoNaoLida processo = initPeticaoNaoLidaProcesso(habilitacaoAutos);
		processoDocumentoPeticaoNaoLidaManager.persistAndFlush(processo);
		
		associarRepresentanteComDefensoria(processoParteList, procuradoria);
		
		return habilitacaoAutos;		
	}
	// associar os representados com o representante
	public void associarRepresentanteComDefensoria(List<ProcessoParte> processoParteList, Procuradoria procuradoria) throws PJeBusinessException{
		for(ProcessoParte processoParte : processoParteList){
			processoParte.setProcuradoria(procuradoria);
			processoParteManager.merge(processoParte);
		}
		processoParteManager.flush();
	}
	
	public List<ProcessoParteRepresentante> getRepresentantesRemovidosInativados(ProcessoTrf processoTrf, List<ProcessoParteRepresentante> representanteRemovidoList, 
			Usuario usuarioSolicitante, Procuradoria procuradoria) throws PJeBusinessException{
			
		if(isRepresentanteRemovidoList(representanteRemovidoList)){
			inativarRepresentante(processoTrf, representanteRemovidoList, usuarioSolicitante, null, obterDocumentosSalvosHabilitacaoAutos().get(0),procuradoria);
		
		}
		return representanteRemovidoList;
	}
	
	public boolean isRepresentanteRemovidoList(List<ProcessoParteRepresentante> representanteRemovidoList){
		return representanteRemovidoList != null && representanteRemovidoList.size() > 0;
	}
	
	public boolean isDefensoriaRemovidaList(List<Procuradoria> defensoriaRemovidaList){
		return defensoriaRemovidaList != null && defensoriaRemovidaList.size() > 0;
	}
	
	
	@SuppressWarnings("unchecked")
	public void gerarAtoParaRepresentantes(List<ProcessoParteRepresentante> representanteRemovidoList, 
			ProcessoTrf processoTrf, TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao, Procuradoria procuradoria)  throws PJeBusinessException{
		if(!representanteRemovidoList.isEmpty() && representanteRemovidoList.size() > 0){
			
			Collection<Pessoa> colecaoTmp = CollectionUtilsPje.collect(representanteRemovidoList, new Transformer() { 
				public Pessoa transform(Object input) {
					return ((ProcessoParteRepresentante)input).getRepresentante();}
			});
			
			//Gerar expedientes para os advogados removidos.
			gerarAtoDeComunicacao(processoTrf.getIdProcessoTrf(), obterDocumentosSalvosHabilitacaoAutos().get(0).getIdProcessoDocumento(), (PessoaFisica[]) colecaoTmp.toArray(new PessoaFisica[0]));
			
			//Em caso de habilitação nos autos por substabelecimento comunicar o novo adovgado.
			if(tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.I || tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.T){
				gerarAtoDeComunicacao(processoTrf.getIdProcessoTrf(), obterDocumentosSalvosHabilitacaoAutos().get(0).getIdProcessoDocumento(), procuradoria.getPessoaJuridica());
			}
		}
			
	}
	
	@SuppressWarnings("unchecked")
	public void gerarAtoParaDefensorias(final List<Procuradoria> defensoriaRemovidaList, 
			ProcessoTrf processoTrf, TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao, final List<ProcessoParte> processoParteList)  throws PJeBusinessException{
		
		if(!defensoriaRemovidaList.isEmpty() && defensoriaRemovidaList.size() > 0){
			
			Collection<Pessoa> colecaoTmp = CollectionUtilsPje.collect(processoParteList, new Transformer() { 
				public Pessoa transform(Object input) {
					
					for(ProcessoParte parte : processoParteList){
						if(defensoriaRemovidaList.contains(parte.getProcuradoria())){
							return parte.getPessoa() ;
						}
					}
					return null;
				}
			});
			
			//Gerar expedientes para procuradoria removida.
			gerarAtoDeComunicacaoDefensoria(processoTrf.getIdProcessoTrf(), obterDocumentosSalvosHabilitacaoAutos().get(0).getIdProcessoDocumento(), (PessoaFisica[]) colecaoTmp.toArray(new PessoaFisica[0]));
			
		}
			
	}
	
	public ProcessoDocumentoPeticaoNaoLida initPeticaoNaoLidaProcesso(HabilitacaoAutos habilitacaoAutos){
		ProcessoDocumentoPeticaoNaoLida processo = ProcessoDocumentoPeticaoNaoLidaHome.instance().getInstance();
		processo.setRetificado(false);
		processo.setRetirado(false);
		processo.setHabilitacaoAutos(habilitacaoAutos);

		for (ProcessoDocumento processoDocumento : obterDocumentosSalvosHabilitacaoAutos()) {
			if(processoDocumento.getDocumentoPrincipal() == null) {
				processo.setProcessoDocumento(processoDocumento);
				 if (processoDocumento.getTipoProcessoDocumento().getFluxo() != null) {
					 Events.instance().raiseAsynchronousEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, processoDocumento.getIdProcessoDocumento());
				 }
				break;
			}
		}
		
		return processo;
	}

	/**
	 * Caso a pessoa possua cadastro de advogado valido no PJE gera um expediente eletrônico sem prazo, 
	 * caso contrario instancia novo fluxo para que a secretária judiciaria notifique o advogado.
	 * 
	 * @param idProcessoTrf
	 * @param idProcessoDocumento
	 * @param pessoas advogado(s) que será(ão) notificado(s).
	 */
	private void gerarAtoDeComunicacao(int idProcessoTrf,
									   int idProcessoDocumento,
									   Pessoa... pessoas) {
		
		
		AtoComunicacaoService atoComunicacaoService = (AtoComunicacaoService) Component.getInstance(AtoComunicacaoService.class);

		
		for (Pessoa pessoa : pessoas) {
			
			if(atoComunicacaoService.verificarCadastroPessoa(pessoa, PessoaFisica.ADV)){
				
				atoComunicacaoService.intimarDestinatarioEletronicamente(idProcessoTrf, 
																		 pessoa.getIdPessoa(), 
																	 	 TipoPrazoEnum.S,
																	 	 0,//Prazo
																	 	 idProcessoDocumento);
				
			}else{
				 Events.instance().raiseEvent(Eventos.INICIAR_FLUXO_NOTIFICAR_HABILITACAO_AUTOS, idProcessoTrf, pessoa.getIdPessoa());
			}
			
		}		
	}
	
	private void gerarAtoDeComunicacaoDefensoria(int idProcessoTrf,int idProcessoDocumento,Pessoa... pessoas) {

		AtoComunicacaoService atoComunicacaoService = (AtoComunicacaoService) Component.getInstance(AtoComunicacaoService.class);
			
			for (Pessoa pessoa : pessoas) {
			
				if(pessoa != null ){
					atoComunicacaoService.intimarDestinatarioEletronicamente(idProcessoTrf, pessoa.getIdPessoa(),TipoPrazoEnum.S,0,idProcessoDocumento);
				}		
			}
			
	}

	/**
	 * Inativa uma lista de representantes, trocando todos expedientes que não sejam pessoais do advogado substituido para o advogado habilitado.
	 * @param processoTrf
	 * @param representanteRemovidoList
	 * @param usuarioSolicitante
	 * @param advogado
	 * @param processoDocumento
	 */
	private void inativarRepresentante(ProcessoTrf processoTrf, 
									   List<ProcessoParteRepresentante> representanteRemovidoList, 
									   Usuario usuarioSolicitante, 
									   PessoaAdvogado advogado,
									   ProcessoDocumento processoDocumento,
									   Procuradoria procuradoria) {
		
		ProcessoParteRepresentanteManager processoParteRepresentanteManager  = ComponentUtil.getComponent("processoParteRepresentanteManager");
		ProcessoParteExpedienteManager processoParteExpedienteManager 		 = ComponentUtil.getComponent("processoParteExpedienteManager");
		
		String justificativa = "Representante baixado pela solicitação de habilitação nos autos por substituição solicitada por %s em %s, em nome de %s.";
		String nomeNovoHabilitado = "";
		
		if(advogado != null){
			nomeNovoHabilitado = advogado.getNome();
		}else if(procuradoria.getNome() != null){
			nomeNovoHabilitado = procuradoria.getNome();
		}else{
			nomeNovoHabilitado = usuarioSolicitante.getNome() +" - "+ Authenticator.instance().getLocalizacaoUsuarioLogado();
		}
		justificativa = String.format(justificativa, usuarioSolicitante.getNome(), DateUtil.dateToString(new Date()), nomeNovoHabilitado);


		for (ProcessoParteRepresentante processoParteRepresentante : representanteRemovidoList) {
			
			//Migrar todos expedientes que não sejam pessoais do advogado substituido para o advogado habilitado.
			for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteManager.recuperaExpedientesAbertosPorProcessoParte(processoParteRepresentante.getParteRepresentante())) {
				if(!processoParteExpediente.getIntimacaoPessoal()){
					if(advogado != null){
						processoParteExpediente.setPessoaParte(advogado);
					}else{
						processoParteExpediente.setProcuradoria(procuradoria);
					}
				}
			}	
			
			//inativar representantes.
			processoParteRepresentanteManager.inativarRepresentante(processoParteRepresentante, ProcessoParteSituacaoEnum.B, justificativa, usuarioSolicitante);
		}
			
	}

	public List<ProcessoParte> getProcessoPartePoloAtivoList(ProcessoTrf processoTrf) {
		return habilitacaoAutosManager.getProcessoPartePoloAtivoList(processoTrf);
	}

	public List<ProcessoParte> getProcessoPartePoloPassivoList(ProcessoTrf processoTrf) {
		return habilitacaoAutosManager.getProcessoPartePoloPassivoList(processoTrf);
	}

	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLidaList() {
		return processoDocumentoPeticaoNaoLidaManager.obterProcessoDocumentoPeticaoNaoLida();
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLidaHabilitacaoAutosList()
	{
		return habilitacaoAutosManager.getProcessoDocumentoPeticaoNaoLidaHabilitacaoAutos();
	}	
	
	public ProcessoTrf getProcessoTrfByProcesso(Processo processo) {
		return habilitacaoAutosManager.getProcessoTrfByProcesso(processo);
	}
	
	public void retirarDestaque(ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida){
		
		processoDocumentoPeticaoNaoLidaManager.retirarDestaque(processoDocumentoPeticaoNaoLida);
	}
	
	/** Criado para resolver ISSUE PJEII-17314
	 *  Metodo responsável por setar a data de juntada em uma lista de documentos. 
	 * 
	 */
	public void setDataJuntada(List<ProcessoDocumento> documentosAssinar, ProcessoTrf processoTrf) {
		for (ProcessoDocumento pd : documentosAssinar) {
			if(processoTrf == null || processoTrf.getProcesso().getIdProcesso() != pd.getProcesso().getIdProcesso())
				processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
			if(processoTrf.getProcessoStatus() == ProcessoStatusEnum.D) {
				pd.setDataJuntada(new Date());
				if (pd.getInstancia() == null || pd.getInstancia().isEmpty())
					pd.setInstancia(String.valueOf(processoTrf.getInstancia()));
			}
		}
		
	}
	/**
	 * FIM PJEII-17314
	 */	
	
}
