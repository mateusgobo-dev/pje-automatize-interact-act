package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.MotivoIsencaoGuiaDAO;
import br.jus.cnj.pje.extensao.ConexaoExternaException;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.VerificadorCustasProcessuais;
import br.jus.cnj.pje.extensao.auxiliar.PontoExtensaoResposta;
import br.jus.cnj.pje.extensao.auxiliar.ProcessoOrigem;
import br.jus.cnj.pje.extensao.auxiliar.custas.CodigoRespostaGuiaRecolhimentoEnum;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.MotivoIsencaoGuia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.SituacaoGuiaRecolhimentoEnum;

@Name(CustasJudiciaisHome.NAME)
@Scope(ScopeType.CONVERSATION)
public class CustasJudiciaisHome extends AbstractHome<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "custasJudiciaisHome";

	@In(create = true, required = false)
	private VerificadorCustasProcessuais verificadorCustasProcessuais;

	private String numeroGuia;

	private MotivoIsencaoGuia motivoSelecionado;

	@In(create = true, required = false, value = "motivoIsencaoGuiaDAO")
	private MotivoIsencaoGuiaDAO motivoIsencaoGuiaDAO;

	public boolean isPossuiModuloVerificadorCustas() {
		return verificadorCustasProcessuais != null;
	}

	public static CustasJudiciaisHome instance() {
		return ComponentUtil.getComponent(CustasJudiciaisHome.NAME);
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	public void carregarDadosCustasDaInicial() {
		if (numeroGuia == null || numeroGuia.isEmpty()) {
			try {
				ProcessoDocumento documentoPeticaoInicial = getProcessoDocumentoPeticaoInicial();

				numeroGuia = documentoPeticaoInicial.getNumeroGuia();

				if (numeroGuia == null || numeroGuia.isEmpty()) {
					try {
						motivoSelecionado = documentoPeticaoInicial.getMotivoIsencaoGuia();
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
			}
		}
	}

	public String getNumeroGuia() {
		return numeroGuia;
	}

	public void setNumeroGuia(String numeroGuia) {
		this.numeroGuia = numeroGuia;
	}

	public MotivoIsencaoGuia getMotivoSelecionado() {
		return motivoSelecionado;
	}

	public void setMotivoSelecionado(MotivoIsencaoGuia motivoSelecionado) {
		this.motivoSelecionado = motivoSelecionado;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getEnderecoSistemaCustasJudiciais() {
		if (!isPossuiModuloVerificadorCustas()) {
			return null;
		}

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		try {
			Map parametrosInformacaoCustas = new HashMap<String, String>();
			parametrosInformacaoCustas.put("idProcesso", processoTrf.getIdProcessoTrf());
			parametrosInformacaoCustas.put("codClasse", processoTrf.getClasseJudicial().getCodClasseJudicial());
			parametrosInformacaoCustas.put("instancia", processoTrf.getInstancia());
			parametrosInformacaoCustas.put("processoStatus", processoTrf.getProcessoStatus().toString());
			parametrosInformacaoCustas.put("isUsuarioExterno", Authenticator.isUsuarioExterno());
			parametrosInformacaoCustas.put("isUsuarioInterno", Authenticator.isUsuarioInterno());

			if (processoTrf.getValorCausa() != null) {
				parametrosInformacaoCustas.put("valorCausa", processoTrf.getValorCausa().toString());
			}

			return verificadorCustasProcessuais.resolverUrlSistemaCustas(parametrosInformacaoCustas).getMensagem();
		} catch (PontoExtensaoException e) {
			e.printStackTrace();

			FacesMessages.instance().add(Severity.ERROR,
					String.format("Erro ao montar URL do sistema de custas: Motivo: %s", e.getLocalizedMessage()));

			return null;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	public boolean isAtivarCustas() {
		if (!isPossuiModuloVerificadorCustas()) {
			return false;
		}

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		if (processoTrf == null || processoTrf.getClasseJudicial() == null || processoTrf.getInstancia() == null) {
			return false;
		} else if (processoTrf.getJusticaGratuita()) {
			return false;
		} else if (processoTrf.getClasseJudicial().getPossuiCusta() == null
				|| !processoTrf.getClasseJudicial().getPossuiCusta()) {
			return false;
		} else if (ProcessoTrfHome.instance().getCadastraProcessoConsumidorGovBr()) {
			return false;
		}
		return true;
	}

	@Observer(Eventos.EVENTO_ATUALIZAR_GUIA_RECOLHIMENTO_POS_PROTOCOLAR)
	public void atualizarGuiaRecolhimentoPosProtocoloProcesso(ProcessoTrf processoTrf, String numeroGuia) {
		if (!isPossuiModuloVerificadorCustas()) {
			return;
		}

		if (numeroGuia != null && !numeroGuia.isEmpty()) {
			String numeroProcesso = processoTrf.getProcesso().getNumeroProcesso();

			if (!(processoTrf.getJusticaGratuita())) {
				try {
					verificadorCustasProcessuais.atualizarPosProtocoloProcesso(numeroGuia, numeroProcesso);
				} catch (ConexaoExternaException e) {
				} catch (PontoExtensaoException e) {
				}
			}
		}
	}

	@Observer(Eventos.EVENTO_ATUALIZAR_GUIA_RECOLHIMENTO_POS_JUNTADA)
	public void atualizarGuiaRecolhimentoPosJuntadaProcesso(ProcessoTrf processoTrf, String numeroGuia) {
		if (!isPossuiModuloVerificadorCustas()) {
			return;
		}

		if (numeroGuia != null && !numeroGuia.isEmpty()) {
			String numeroProcesso = processoTrf.getProcesso().getNumeroProcesso();

			if (processoTrf.isNumerado()) {
				try {
					verificadorCustasProcessuais.atualizarPosJuntadaDocumento(numeroGuia, numeroProcesso);
				} catch (ConexaoExternaException e) {
				} catch (PontoExtensaoException e) {
				}
			}
		}
	}

	public PontoExtensaoResposta salvarDadosCustasProtocolar() throws PJeBusinessException {
		if (!isPossuiModuloVerificadorCustas()) {
			return new PontoExtensaoResposta(
					"Não há conector de custas instanciado para salvar dados da guia de recolhimento ao protocolar.");
		}

		if (ProcessoTrfHome.instance().getCadastraProcessoConsumidorGovBr()) {
			return new PontoExtensaoResposta("Cadastro no ProcessoConsumidorGovBr ativado.");
		}

		ProcessoDocumento peticaoInicialAssinada = null;
		ProcessoTrf processoTrf = null;

		try {
			peticaoInicialAssinada = getProcessoDocumentoPeticaoInicial();
			processoTrf = ProcessoTrfHome.instance().getInstance();

			if (numeroGuia == null || numeroGuia.isEmpty()) {
				String numeroGuiaSalva = peticaoInicialAssinada.getNumeroGuia();

				if (numeroGuiaSalva != null && !numeroGuiaSalva.isEmpty()) {
					numeroGuia = numeroGuiaSalva;
				}
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());

			return new PontoExtensaoResposta(CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel(),
					e.getLocalizedMessage());
		}

		if (processoTrf.getClasseJudicial().getPossuiCusta() != null
				&& processoTrf.getClasseJudicial().getPossuiCusta()) {
			if (!processoTrf.getJusticaGratuita()) {
				if (!isMotivoSelecionadoIsentaCustas()) {
					try {
						PontoExtensaoResposta pontoExtensaoResposta = validarDadosCustasProtocolar();

						if (CodigoRespostaGuiaRecolhimentoEnum.VALIDO.getLabel()
								.equals(pontoExtensaoResposta.getCodigo())) {
							processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.GA);
							peticaoInicialAssinada.setNumeroGuia(numeroGuia);
							peticaoInicialAssinada.setMotivoIsencaoGuia(null);

							EntityUtil.getEntityManager().merge(processoTrf);
							EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
							EntityUtil.flush();

							FacesMessages.instance().add(Severity.INFO,
									"Guia de recolhimento salva no documento do processo com sucesso.");

							return pontoExtensaoResposta;
						} else if (CodigoRespostaGuiaRecolhimentoEnum.GERADO.getLabel()
								.equals(pontoExtensaoResposta.getCodigo())) {
							processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.GG);
							peticaoInicialAssinada.setNumeroGuia(numeroGuia);
							peticaoInicialAssinada.setMotivoIsencaoGuia(null);

							EntityUtil.getEntityManager().merge(processoTrf);
							EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
							EntityUtil.flush();

							FacesMessages.instance().add(Severity.INFO,
									"Guia de recolhimento salva no documento do processo com sucesso.");

							return pontoExtensaoResposta;
						} else {
							processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.NV);
							peticaoInicialAssinada.setNumeroGuia(null);
							peticaoInicialAssinada.setMotivoIsencaoGuia(null);

							EntityUtil.getEntityManager().merge(processoTrf);
							EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
							EntityUtil.flush();

							FacesMessages.instance().add(Severity.ERROR, pontoExtensaoResposta.getMensagem());

							return pontoExtensaoResposta;
						}
					} catch (Exception e) {
						processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.GA);
						peticaoInicialAssinada.setNumeroGuia(numeroGuia);
						peticaoInicialAssinada.setMotivoIsencaoGuia(null);

						EntityUtil.getEntityManager().merge(processoTrf);
						EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
						EntityUtil.flush();

						FacesMessages.instance().add(Severity.INFO,
								"Como não foi possível validar a guia de recolhimento, ela será salva e a validação ocorrerá posteriormente.");

						return new PontoExtensaoResposta(
								"Como não foi possível validar a guia de recolhimento, ela será salva e a validação ocorrerá posteriormente.");
					}
				} else if (motivoSelecionado.getInControlaIsencao()) {
					numeroGuia = null;

					processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.NG);
					peticaoInicialAssinada.setNumeroGuia(null);
					peticaoInicialAssinada.setMotivoIsencaoGuia(motivoSelecionado);

					EntityUtil.getEntityManager().merge(processoTrf);
					EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
					EntityUtil.flush();

					FacesMessages.instance().add(Severity.INFO, "Motivo de isenção salvo no processo com sucesso.");

					return new PontoExtensaoResposta("Motivo de isenção salvo no processo com sucesso.");
				} else {
					numeroGuia = null;

					processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.MI);
					peticaoInicialAssinada.setNumeroGuia(null);
					peticaoInicialAssinada.setMotivoIsencaoGuia(motivoSelecionado);

					EntityUtil.getEntityManager().merge(processoTrf);
					EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
					EntityUtil.flush();

					FacesMessages.instance().add(Severity.INFO, "Motivo de isenção salvo no processo com sucesso.");

					return new PontoExtensaoResposta("Motivo de isenção salvo no processo com sucesso.");
				}
			} else {
				numeroGuia = null;

				processoTrf.setSituacaoGuiaRecolhimento(null);
				peticaoInicialAssinada.setNumeroGuia(null);
				peticaoInicialAssinada.setMotivoIsencaoGuia(null);

				EntityUtil.getEntityManager().merge(processoTrf);
				EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
				EntityUtil.flush();

				return new PontoExtensaoResposta("Justiça gratuita selecionada.");
			}
		} else {
			numeroGuia = null;

			processoTrf.setSituacaoGuiaRecolhimento(null);
			peticaoInicialAssinada.setNumeroGuia(null);
			peticaoInicialAssinada.setMotivoIsencaoGuia(null);

			EntityUtil.getEntityManager().merge(processoTrf);
			EntityUtil.getEntityManager().merge(peticaoInicialAssinada);
			EntityUtil.flush();

			return new PontoExtensaoResposta("A classe judicial selecionada não possui custas.");
		}
	}

	public void salvarDadosCustasTelaProtocolar() {
		if (!isPossuiModuloVerificadorCustas()) {
			return;
		}

		try {
			salvarDadosCustasProtocolar();
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}

	public PontoExtensaoResposta validarDadosCustasJuntada(ProcessoDocumento processoDocumento) {
		try {
			return validarDadosCustasJuntadaParaSalvar(processoDocumento);
		} catch (Exception e) {
			String msg = "Como não foi possível validar a guia de recolhimento, ela será salva e a validação ocorrerá posteriormente.";

			FacesMessages.instance().add(Severity.INFO, msg);

			return new PontoExtensaoResposta(msg);
		}
	}

	private PontoExtensaoResposta validarDadosCustasJuntadaParaSalvar(ProcessoDocumento processoDocumento)
			throws ConexaoExternaException, PontoExtensaoException {
		if (!isPossuiModuloVerificadorCustas()) {
			return new PontoExtensaoResposta(
					"Não há conector de custas instanciado ao validar dados de custas na juntada.");
		}

		numeroGuia = processoDocumento.getNumeroGuia();

		if (numeroGuia != null && !numeroGuia.isEmpty()) {
			ProcessoTrf processoTrf = getProcessoTrf(processoDocumento);

			String numeroProcesso = processoTrf.getNumeroProcesso();

			if (numeroProcesso == null || numeroProcesso.isEmpty()) {
				processoTrf = processoDocumento.getProcessoTrf();

				numeroProcesso = processoTrf.getNumeroProcesso();
			}

			if (numeroProcesso != null) {
				Integer codigoClasse = Integer.parseInt(processoTrf.getClasseJudicial().getCodClasseJudicial());
				ProcessoOrigem processoOrigem = new ProcessoOrigem();

				processoOrigem.setIdProcessoOrigem(processoTrf.getIdProcessoTrf());
				processoOrigem.setNumeroProcessoOrigem(processoTrf.getNumeroProcesso());
				processoOrigem.setCodClasse(codigoClasse);

				return verificadorCustasProcessuais.validarGuiaRecolhimento(numeroGuia, processoOrigem);
			}

			return new PontoExtensaoResposta("Não há critérios para validar dados de custas na juntada.");
		} else {
			return new PontoExtensaoResposta("O número da guia é nulo ou vazio.");
		}
	}

	private ProcessoTrf getProcessoTrf(ProcessoDocumento processoDocumento) {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		if (processoTrf.getNumeroProcesso() == null || processoTrf.getNumeroProcesso().isEmpty()) {
			processoTrf = processoDocumento.getProcessoTrf();
		}

		return processoTrf;
	}

	public PontoExtensaoResposta salvarDadosCustasJuntada(ProcessoDocumento processoDocumento) {
		if (!isPossuiModuloVerificadorCustas()) {
			return new PontoExtensaoResposta(
					"Não há conector de custas instanciado ao salvar dados de custas na juntada.");
		}

		numeroGuia = processoDocumento.getNumeroGuia();

		if (numeroGuia != null && !numeroGuia.isEmpty()) {
			if (processoDocumento.getTipoProcessoDocumento().getPossuiCustas()) {
				ProcessoTrf processoTrf = getProcessoTrf(processoDocumento);

				String numeroProcesso = processoTrf.getNumeroProcesso();

				if (numeroProcesso != null) {
					try {
						PontoExtensaoResposta pontoExtensaoResposta = validarDadosCustasJuntadaParaSalvar(
								processoDocumento);

						if (CodigoRespostaGuiaRecolhimentoEnum.VALIDO.getLabel()
								.equals(pontoExtensaoResposta.getCodigo())) {
							processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.GA);
							processoDocumento.setNumeroGuia(numeroGuia);
							processoDocumento.setMotivoIsencaoGuia(null);

							EntityUtil.getEntityManager().merge(processoTrf);
							EntityUtil.getEntityManager().merge(processoDocumento);
							EntityUtil.flush();

							FacesMessages.instance().add(Severity.INFO,
									"Guia de recolhimento salva no documento do processo com sucesso.");

							return pontoExtensaoResposta;
						} else if (CodigoRespostaGuiaRecolhimentoEnum.GERADO.getLabel()
								.equals(pontoExtensaoResposta.getCodigo())) {
							processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.GG);
							processoDocumento.setNumeroGuia(numeroGuia);
							processoDocumento.setMotivoIsencaoGuia(null);

							EntityUtil.getEntityManager().merge(processoTrf);
							EntityUtil.getEntityManager().merge(processoDocumento);
							EntityUtil.flush();

							FacesMessages.instance().add(Severity.INFO,
									"Guia de recolhimento salva no documento do processo com sucesso.");

							return pontoExtensaoResposta;
						} else {
							processoDocumento.setNumeroGuia(null);
							processoDocumento.setMotivoIsencaoGuia(null);

							EntityUtil.getEntityManager().merge(processoDocumento);
							EntityUtil.flush();

							FacesMessages.instance().add(Severity.ERROR, pontoExtensaoResposta.getMensagem());

							return pontoExtensaoResposta;
						}
					} catch (Exception e) {
						processoTrf.setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum.GA);
						processoDocumento.setNumeroGuia(numeroGuia);
						processoDocumento.setMotivoIsencaoGuia(null);

						EntityUtil.getEntityManager().merge(processoTrf);
						EntityUtil.getEntityManager().merge(processoDocumento);
						EntityUtil.flush();

						String msg = "Como não foi possível validar a guia de recolhimento, ela será salva e a validação ocorrerá posteriormente.";

						FacesMessages.instance().add(Severity.INFO, msg);

						return new PontoExtensaoResposta(msg);
					}
				}
			} else {
				numeroGuia = null;

				processoDocumento.setNumeroGuia(null);
				processoDocumento.setMotivoIsencaoGuia(null);

				EntityUtil.getEntityManager().merge(processoDocumento);
				EntityUtil.flush();

				return new PontoExtensaoResposta("O tipo de documento não possui custas.");
			}
		} else {
			numeroGuia = null;

			processoDocumento.setNumeroGuia(null);
			processoDocumento.setMotivoIsencaoGuia(null);

			EntityUtil.getEntityManager().merge(processoDocumento);
			EntityUtil.flush();

			return new PontoExtensaoResposta("O número da guia é nulo ou vazio.");
		}

		return new PontoExtensaoResposta("Não há critérios para salvar dados de custas na juntada.");
	}

	public List<MotivoIsencaoGuia> getAllMotivosIsencaoAtivos() {
		return motivoIsencaoGuiaDAO.findMotivos(true);
	}

	public boolean isMotivoSelecionadoIsentaCustas() {
		return motivoSelecionado != null;
	}

	private PontoExtensaoResposta validarDadosCustasProtocolar() throws PJeBusinessException {
		if (!isPossuiModuloVerificadorCustas()) {
			return new PontoExtensaoResposta(
					"Não há conector de custas instanciado para a validação dos dados de custas.");
		}

		if (motivoSelecionado == null || motivoSelecionado.getId() == null) {
			ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

			if (!processoTrf.getJusticaGratuita()) {
				String codClasse = processoTrf.getClasseJudicial().getCodClasseJudicial();

				if (numeroGuia == null || numeroGuia.isEmpty()) {
					return new PontoExtensaoResposta(CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel(),
							"O número da guia é obrigatório.");
				} else if ((processoTrf.getValorCausa() == null || processoTrf.getValorCausa() <= 0)) {
					return new PontoExtensaoResposta(CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel(),
							"O valor da causa do processo é obrigatório.");
				} else if (processoTrf.getIdProcessoTrf() == 0) {
					return new PontoExtensaoResposta(CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel(),
							"O identificador do processo é obrigatório.");
				} else if (codClasse == null || codClasse.isEmpty()) {
					return new PontoExtensaoResposta(CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel(),
							"O código da classe judicial é obrigatório.");
				}

				Integer codigoClasse = Integer.parseInt(codClasse);

				ProcessoOrigem processoOrigem = new ProcessoOrigem();
				processoOrigem.setIdProcessoOrigem(processoTrf.getIdProcessoTrf());
				processoOrigem.setCodClasse(codigoClasse);

				try {
					return verificadorCustasProcessuais.validarGuiaRecolhimento(numeroGuia, processoOrigem);
				} catch (ConexaoExternaException e) {
					throw new PJeBusinessException(e.getLocalizedMessage());
				} catch (PontoExtensaoException e) {
					throw new PJeBusinessException(e.getLocalizedMessage());
				}
			}
		}

		return new PontoExtensaoResposta("Não há critérios para a validação dos dados de custas.");
	}

	private ProcessoDocumento getProcessoDocumentoPeticaoInicial() throws PJeBusinessException {
		boolean achou = false;
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class,
				ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());

		TipoProcessoDocumento tipoPeticaoInicial = processoTrf.getClasseJudicial().getTipoProcessoDocumentoInicial();

		// [PJEII-1243] Padronizado para recuperar o id do TipoProcessoDocumento
		// configurado na tabela de parâmetros como Petição inicial
		List<Integer> idTipoProcessoDocumentoList = new ArrayList<Integer>();
		if (ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaExecucao() != null) {
			idTipoProcessoDocumentoList.add(ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaExecucao()
					.getIdTipoProcessoDocumento());
		}

		if (ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaLiquidacao() != null) {
			idTipoProcessoDocumentoList.add(ParametroUtil.instance().getTipoProcessoDocumentoTermoAberturaLiquidacao()
					.getIdTipoProcessoDocumento());
		}

		idTipoProcessoDocumentoList.add(tipoPeticaoInicial.getIdTipoProcessoDocumento());

		String msgExigenciaGravarGuia = "";

		if (processoTrf.getClasseJudicial().getPossuiCusta() != null
				&& processoTrf.getClasseJudicial().getPossuiCusta()) {
			if (!processoTrf.getJusticaGratuita()) {
				msgExigenciaGravarGuia = "Para gravar o número da guia se faz necessária uma petição inicial assinada.";
			}
		}

		String msgErroPeticaoInicialNaoAssinada = "A petição inicial não está assinada. " + msgExigenciaGravarGuia;
		String msgErroDocumentoPeticaoInicialNaoEncontrado = "Não foi encontrado um documento de petição inicial salvo. "
				+ msgExigenciaGravarGuia;

		for (ProcessoDocumento pd : processoTrf.getProcesso().getProcessoDocumentoList()) {
			for (Integer idTipoProcessoDocumento : idTipoProcessoDocumentoList) {
				if (pd.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoProcessoDocumento.intValue()) {
					achou = true;
					if (verificarFaltaAssinatura(pd.getIdProcessoDocumento()) == false) {
						return pd;
					} else {
						throw new PJeBusinessException(msgErroPeticaoInicialNaoAssinada);
					}
				}
			}
		}

		if (!achou) {
			throw new PJeBusinessException(msgErroDocumentoPeticaoInicialNaoEncontrado);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	Boolean verificarFaltaAssinatura(int idProcessoDocumento) {
		Boolean faltaAssinatura = Boolean.FALSE;

		StringBuffer sb = new StringBuffer();
		sb.append("select pdb.ds_cert_chain, pdb.ds_signature from tb_processo_documento pd inner join ");
		sb.append("tb_processo_documento_bin pdb ");
		sb.append("on(pd.id_processo_documento_bin = pdb.id_processo_documento_bin) ");
		sb.append("where pd.id_processo_documento = :idProcessoDocumento");

		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idProcessoDocumento", idProcessoDocumento);
		List<Object[]> objectList = q.getResultList();

		for (Object[] obj : objectList) {
			if (Strings.isEmpty((String) obj[0])) {
				faltaAssinatura = Boolean.TRUE;
				break;
			}
		}

		return faltaAssinatura;
	}
}