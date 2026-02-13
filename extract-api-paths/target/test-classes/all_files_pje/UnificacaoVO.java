package br.jus.pje.nucleo.entidades;

import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

/**
 * classe criada para facilitar a manutençao e controle de objetos envolvidos na unificacao
 * @author luiz.mendes
 *
 */
public class UnificacaoVO {
	private Pessoa pessoaPrincipal = null;
	private PessoaFisica pessoaFisicaPessoaPrincipal;
	private PessoaJuridica pessoaJuridicaPessoaPrincipal;
	private PessoaAutoridade pessoaAutoridadePessoaPrincipal;
	private Usuario usuarioPessoaPrincipal;
	private UsuarioLogin usuarioLoginPessoaPrincipal;
	private TipoPessoaEnum tipoRealPessoaPrincipal = null;
	private Usuario usuarioUnificador =  null;
	private List<Pessoa> pessoasSecundariasUnificacao = new ArrayList<Pessoa>(0);
	private List<Pessoa> pessoasConflitoPartesProcessuais = new ArrayList<Pessoa>(0);
	private List<LogAcesso> logsAcessoPessoaSecundarias = new ArrayList<LogAcesso>(0);	
	private List<CaracteristicaFisica> caracteristicasFisicasPessoaPrincipal = new ArrayList<CaracteristicaFisica>(0);
	private List<CaracteristicaFisica> caracteristicasFisicasPessoasSecundarias = new ArrayList<CaracteristicaFisica>(0);
	private List<CaracteristicaFisica> caracteristicasFisicasConflito = new ArrayList<CaracteristicaFisica>(0);
	private List<MeioContato> meiosContatosCadastradosPessoaSecundarias = new ArrayList<MeioContato>(0);
	private List<MeioContato> meiosContatosProprietariaPessoaPrincipal = new ArrayList<MeioContato>(0);	
	private List<MeioContato> meiosContatosProprietariasPessoasSecundarias = new ArrayList<MeioContato>(0);
	private List<MeioContato> meiosContatosConflito = new ArrayList<MeioContato>(0);
	private List<PessoaNomeAlternativo> nomesAlternativosCadastradosPessoaSecundarias = new ArrayList<PessoaNomeAlternativo>(0);
	private List<PessoaNomeAlternativo> nomesAlternativosProprietariaPessoaPrincipal = new ArrayList<PessoaNomeAlternativo>(0);
	private List<PessoaNomeAlternativo> nomesAlternativosProprietariasPessoasSecundarias = new ArrayList<PessoaNomeAlternativo>(0);
	private List<PessoaNomeAlternativo> nomesAlternativosConflito = new ArrayList<PessoaNomeAlternativo>(0);
	private List<ProcessoTrfConexao> conexoesPrevencaoPessoasSecundarias = new ArrayList<ProcessoTrfConexao>(0);
	private List<ProcessoSegredo> processoSegredoCadastradosPessoaSecundarias = new ArrayList<ProcessoSegredo>(0);
	private List<ProcessoParteSigilo> processosParteSigiloPessoasSecundarias = new ArrayList<ProcessoParteSigilo>(0);
	private List<CaixaRepresentante> caixasRepresentantesPessoaPrincipal = new ArrayList<CaixaRepresentante>(0);
	private List<CaixaRepresentante> caixasRepresentantesPessoasSecundarias = new ArrayList<CaixaRepresentante>(0);
	private List<CaixaRepresentante> caixasRepresentantesConflito = new ArrayList<CaixaRepresentante>(0);
	private List<SessaoEnteExterno> sessoesEntesExternosPessoaSecundarias = new ArrayList<SessaoEnteExterno>(0);
	private List<ProcessoTrfRedistribuicao> redistribuicoesProcessosPessoaSecundarias = new ArrayList<ProcessoTrfRedistribuicao>(0);
	private List<ProcessoParteHistorico> processosParteHistoricosPessoaSecundarias = new ArrayList<ProcessoParteHistorico>(0);
	private List<ProcessoTag> processosTagPessoaSecundarias = new ArrayList<ProcessoTag>(0);
	private List<Lembrete> lembretesPessoaSecundarias = new ArrayList<Lembrete>(0);
	private List<LembretePermissao> permissoesLembretesPessoaSecundarias = new ArrayList<LembretePermissao>(0);
	private List<Processo> processosPessoaSecundarias = new ArrayList<Processo>(0);
	private List<Parametro> parametrosPessoaSecundarias = new ArrayList<Parametro>(0);
	private List<EntityLog> entityLogPessoaSecundarias = new ArrayList<EntityLog>(0);
	private List<SolicitacaoNoDesvio> solicitacoesNoDesvioPessoaSecundarias = new ArrayList<SolicitacaoNoDesvio>(0);
	private List<SessaoPautaProcessoTrf> sessaoPautaProcessoPessoaSecundariasInclusoras = new ArrayList<SessaoPautaProcessoTrf>(0);
	private List<SessaoPautaProcessoTrf> sessaoPautaProcessoPessoaSecundariasExclusoras = new ArrayList<SessaoPautaProcessoTrf>(0);
	private List<Sessao> sessaoPessoaSecundariasInclusoras = new ArrayList<Sessao>(0);
	private List<Sessao> sessaoPessoaSecundariasExclusoras = new ArrayList<Sessao>(0);
	private List<QuadroAviso> avisosPessoaSecundarias = new ArrayList<QuadroAviso>(0);
	private List<ProcessoDocumentoFavorito> procsDocFavoritosPessoaSecundarias = new ArrayList<ProcessoDocumentoFavorito>(0);
	private List<NotaSessaoJulgamento> notasSessaoJulgamentoPessoaSecundarias = new ArrayList<NotaSessaoJulgamento>(0);
	private List<ModeloProclamacaoJulgamento> modelosProclamacaoJulgamentoPessoasSecundarias = new ArrayList<ModeloProclamacaoJulgamento>(0);
	private List<LogHistoricoMovimentacao> logsHistoricoMovimentacaoPessoasSecundarias = new ArrayList<LogHistoricoMovimentacao>(0);
	private List<VisibilidadePessoaDocumentoIdentificacao> visibilidadesDocumentoIdentificacaoPessoasSecundarias = new ArrayList<VisibilidadePessoaDocumentoIdentificacao>(0);
	
	public UnificacaoVO(Pessoa pessoaPrincipal, Usuario usuarioUnificador, TipoPessoaEnum tipoRealPessoaPrincipal) {
		this.pessoaPrincipal = pessoaPrincipal;
		this.usuarioUnificador = usuarioUnificador;
		this.tipoRealPessoaPrincipal = tipoRealPessoaPrincipal;
		meiosContatosProprietariaPessoaPrincipal = new ArrayList<MeioContato>(0);	
	}
	
	/**
	 * metodo utilizado para limpar as listas de objetos da unificacao sem no entanto, limpar as listas e objetos principais,
	 * como pessoas secundarias, usuario unificador, etc.
	 */
	public void resetObjetosUnificacao() {
		logsAcessoPessoaSecundarias = new ArrayList<LogAcesso>(0);
		caracteristicasFisicasPessoaPrincipal = new ArrayList<CaracteristicaFisica>(0);
		caracteristicasFisicasPessoasSecundarias = new ArrayList<CaracteristicaFisica>(0);
		caracteristicasFisicasConflito = new ArrayList<CaracteristicaFisica>(0);
		meiosContatosCadastradosPessoaSecundarias = new ArrayList<MeioContato>(0);	
		meiosContatosProprietariaPessoaPrincipal = new ArrayList<MeioContato>(0);	
		meiosContatosProprietariasPessoasSecundarias = new ArrayList<MeioContato>(0);
		meiosContatosConflito = new ArrayList<MeioContato>(0);
		nomesAlternativosCadastradosPessoaSecundarias = new ArrayList<PessoaNomeAlternativo>(0);
		nomesAlternativosProprietariaPessoaPrincipal = new ArrayList<PessoaNomeAlternativo>(0);
		nomesAlternativosProprietariasPessoasSecundarias = new ArrayList<PessoaNomeAlternativo>(0);
		nomesAlternativosConflito = new ArrayList<PessoaNomeAlternativo>(0);
		conexoesPrevencaoPessoasSecundarias = new ArrayList<ProcessoTrfConexao>(0);
		processoSegredoCadastradosPessoaSecundarias = new ArrayList<ProcessoSegredo>(0);
		processosParteSigiloPessoasSecundarias = new ArrayList<ProcessoParteSigilo>(0);
		caixasRepresentantesPessoaPrincipal = new ArrayList<CaixaRepresentante>(0);
		caixasRepresentantesPessoasSecundarias = new ArrayList<CaixaRepresentante>(0);
		caixasRepresentantesConflito = new ArrayList<CaixaRepresentante>(0);
		sessoesEntesExternosPessoaSecundarias = new ArrayList<SessaoEnteExterno>(0);
		redistribuicoesProcessosPessoaSecundarias = new ArrayList<ProcessoTrfRedistribuicao>(0);
		processosParteHistoricosPessoaSecundarias = new ArrayList<ProcessoParteHistorico>(0);
		processosTagPessoaSecundarias = new ArrayList<ProcessoTag>(0);
		lembretesPessoaSecundarias = new ArrayList<Lembrete>(0);
		permissoesLembretesPessoaSecundarias = new ArrayList<LembretePermissao>(0);
		processosPessoaSecundarias = new ArrayList<Processo>(0);
		parametrosPessoaSecundarias = new ArrayList<Parametro>(0);
		entityLogPessoaSecundarias = new ArrayList<EntityLog>(0);
		solicitacoesNoDesvioPessoaSecundarias = new ArrayList<SolicitacaoNoDesvio>(0);
		sessaoPautaProcessoPessoaSecundariasInclusoras = new ArrayList<SessaoPautaProcessoTrf>(0);
		sessaoPautaProcessoPessoaSecundariasExclusoras = new ArrayList<SessaoPautaProcessoTrf>(0);
		sessaoPessoaSecundariasInclusoras = new ArrayList<Sessao>(0);
		sessaoPessoaSecundariasExclusoras = new ArrayList<Sessao>(0);
		avisosPessoaSecundarias = new ArrayList<QuadroAviso>(0);
		procsDocFavoritosPessoaSecundarias = new ArrayList<ProcessoDocumentoFavorito>(0);
		notasSessaoJulgamentoPessoaSecundarias = new ArrayList<NotaSessaoJulgamento>(0);
		modelosProclamacaoJulgamentoPessoasSecundarias = new ArrayList<ModeloProclamacaoJulgamento>(0);
		logsHistoricoMovimentacaoPessoasSecundarias = new ArrayList<LogHistoricoMovimentacao>(0);
		visibilidadesDocumentoIdentificacaoPessoasSecundarias = new ArrayList<VisibilidadePessoaDocumentoIdentificacao>(0);
	}

	public Pessoa getPessoaPrincipal() {
		return pessoaPrincipal;
	}

	public void setPessoaPrincipal(Pessoa pessoaPrincipal) {
		this.pessoaPrincipal = pessoaPrincipal;
	}

	public Usuario getUsuarioUnificador() {
		return usuarioUnificador;
	}

	public void setUsuarioUnificador(Usuario usuarioUnificador) {
		this.usuarioUnificador = usuarioUnificador;
	}

	public List<Pessoa> getPessoasSecundariasUnificacao() {
		return pessoasSecundariasUnificacao;
	}

	public void setPessoasSecundariasUnificacao(List<Pessoa> pessoasSecundariasUnificacao) {
		this.pessoasSecundariasUnificacao = pessoasSecundariasUnificacao;
	}
	
	public List<Pessoa> getPessoasConflitoPartesProcessuais() {
		return pessoasConflitoPartesProcessuais;
	}

	public void setPessoasConflitoPartesProcessuais(List<Pessoa> pessoasConflitoPartesProcessuais) {
		this.pessoasConflitoPartesProcessuais = pessoasConflitoPartesProcessuais;
	}

	public TipoPessoaEnum getTipoRealPessoaPrincipal() {
		return tipoRealPessoaPrincipal;
	}

	public void setTipoRealPessoaPrincipal(TipoPessoaEnum tipoRealPessoaPrincipal) {
		this.tipoRealPessoaPrincipal = tipoRealPessoaPrincipal;
	}
	
	/**
	 * metodo responsavel por adicionar os logs de acesso das pessoas secundarias
	 * @param _logsAcesso
	 */
	public void addLogsAcesso(List<LogAcesso> _logsAcesso) {
		if (_logsAcesso != null && _logsAcesso.size() > 0) {
			for (LogAcesso logAcesso : _logsAcesso) {
				if (!logsAcessoPessoaSecundarias.contains(logAcesso)) {
					logsAcessoPessoaSecundarias.add(logAcesso);
				}
			}
		}
	}
	
	/**
	 * metodo responsavel por adicionar os meios de contato cadastrados pelas pessoas secundarias
	 * @param _meiosContato
	 */
	public void addMeiosContatoCadastrados(List<MeioContato> _meiosContato) {
		if (_meiosContato != null && _meiosContato.size() > 0) {
			for (MeioContato _meioContato : _meiosContato) {
				if (!meiosContatosCadastradosPessoaSecundarias.contains(_meioContato)) {
					meiosContatosCadastradosPessoaSecundarias.add(_meioContato);
				}
			}
		}
	}
	
	/**
	 * metodo responsavel por adicionar os nomes alternativos cadastrados pelas pessoas secundarias
	 * @param recuperaNomesAlternativosCadastrados
	 */
	public void addNomesAlternativosCadastrados(List<PessoaNomeAlternativo> _nomesAlternativos) {
		if (_nomesAlternativos != null && _nomesAlternativos.size() > 0) {
			for (PessoaNomeAlternativo _nomeAlternativo : _nomesAlternativos) {
				if (!nomesAlternativosCadastradosPessoaSecundarias.contains(_nomeAlternativo)) {
					nomesAlternativosCadastradosPessoaSecundarias.add(_nomeAlternativo);
				}
			}
		}
	}
	
	public void addSegredoProcessosCadastrados(List<ProcessoSegredo> _segProcs) {
		if (_segProcs != null && _segProcs.size() > 0) {
			for (ProcessoSegredo _processoSegredo : _segProcs) {
				if (!processoSegredoCadastradosPessoaSecundarias.contains(_processoSegredo)) {
					processoSegredoCadastradosPessoaSecundarias.add(_processoSegredo);
				}
			}
		}
	}
	
	public void addSessaoEnteExterno(List<SessaoEnteExterno> _sessaoEnteExternos) {
		if (_sessaoEnteExternos != null && _sessaoEnteExternos.size() > 0) {
			for (SessaoEnteExterno _sessEntExt : _sessaoEnteExternos) {
				if (!sessoesEntesExternosPessoaSecundarias.contains(_sessEntExt)) {
					sessoesEntesExternosPessoaSecundarias.add(_sessEntExt);
				}
			}
		}
	}
	
	public void addRedistribuicaoProcesso(List<ProcessoTrfRedistribuicao> _redistribuicoesProcessos) {
		if (_redistribuicoesProcessos != null && _redistribuicoesProcessos.size() > 0) {
			for (ProcessoTrfRedistribuicao _redistriProc : _redistribuicoesProcessos) {
				if (!redistribuicoesProcessosPessoaSecundarias.contains(_redistriProc)) {
					redistribuicoesProcessosPessoaSecundarias.add(_redistriProc);
				}
			}
		}
	}
	
	public void addProcessoParteHistorico(List<ProcessoParteHistorico> _processosParteHistoricos) {
		if (_processosParteHistoricos != null && _processosParteHistoricos.size() > 0) {
			for (ProcessoParteHistorico _procParteHist : _processosParteHistoricos) {
				if (!processosParteHistoricosPessoaSecundarias.contains(_procParteHist)) {
					processosParteHistoricosPessoaSecundarias.add(_procParteHist);
				}
			}
		}
	}
	
	public void addProcessoTag(List<ProcessoTag> _processosTag) {
		if (_processosTag != null && _processosTag.size() > 0) {
			for (ProcessoTag _procTag : _processosTag) {
				if (!processosTagPessoaSecundarias.contains(_procTag)) {
					processosTagPessoaSecundarias.add(_procTag);
				}
			}
		}
	}
	
	public void addLembrete(List<Lembrete> _lembretes) {
		if (_lembretes != null && _lembretes.size() > 0) {
			for (Lembrete _lembrete : _lembretes) {
				if (!lembretesPessoaSecundarias.contains(_lembrete)) {
					lembretesPessoaSecundarias.add(_lembrete);
				}
			}
		}
	}
	
	public void addLembretePermissao(List<LembretePermissao> _permissoesLembretes) {
		if (_permissoesLembretes != null && _permissoesLembretes.size() > 0) {
			for (LembretePermissao _permissaoLembrete : _permissoesLembretes) {
				if (!permissoesLembretesPessoaSecundarias.contains(_permissaoLembrete)) {
					permissoesLembretesPessoaSecundarias.add(_permissaoLembrete);
				}
			}
		}
	}
	
	public void addProcesso(List<Processo> _processos) {
		if (_processos != null && _processos.size() > 0) {
			for (Processo _processo : _processos) {
				if (!processosPessoaSecundarias.contains(_processo)) {
					processosPessoaSecundarias.add(_processo);
				}
			}
		}
	}
	
	public void addParametros(List<Parametro> _parametros) {
		if (_parametros != null && _parametros.size() > 0) {
			for (Parametro _parametro : _parametros) {
				if (!parametrosPessoaSecundarias.contains(_parametro)) {
					parametrosPessoaSecundarias.add(_parametro);
				}
			}
		}
	}
	
	public void addEntityLogs(List<EntityLog> _entityLogs) {
		if (_entityLogs != null && _entityLogs.size() > 0) {
			for (EntityLog _log : _entityLogs) {
				if (!entityLogPessoaSecundarias.contains(_log)) {
					entityLogPessoaSecundarias.add(_log);
				}
			}
		}
	}
	
	public void addSolicitacaoNoDesvio(List<SolicitacaoNoDesvio> _solicitacoesNoDesvio) {
		if (_solicitacoesNoDesvio != null && _solicitacoesNoDesvio.size() > 0) {
			for (SolicitacaoNoDesvio _solicitacao : _solicitacoesNoDesvio) {
				if (!solicitacoesNoDesvioPessoaSecundarias.contains(_solicitacao)) {
					solicitacoesNoDesvioPessoaSecundarias.add(_solicitacao);
				}
			}
		}
	}
	
	public void addSessaoPautaProcessoInclusora(List<SessaoPautaProcessoTrf> _sessoesProcessosPauta) {
		if (_sessoesProcessosPauta != null && _sessoesProcessosPauta.size() > 0) {
			for (SessaoPautaProcessoTrf _sessPautProc : _sessoesProcessosPauta) {
				if (!sessaoPautaProcessoPessoaSecundariasInclusoras.contains(_sessPautProc)) {
					sessaoPautaProcessoPessoaSecundariasInclusoras.add(_sessPautProc);
				}
			}
		}
	}
	
	public void addSessaoPautaProcessoExclusora(List<SessaoPautaProcessoTrf> _sessoesProcessoPauta) {
		if (_sessoesProcessoPauta != null && _sessoesProcessoPauta.size() > 0) {
			for (SessaoPautaProcessoTrf _sessPautProc : _sessoesProcessoPauta) {
				if (!sessaoPautaProcessoPessoaSecundariasExclusoras.contains(_sessPautProc)) {
					sessaoPautaProcessoPessoaSecundariasExclusoras.add(_sessPautProc);
				}
			}
		}
	}
	
	public void addSessaoInclusora(List<Sessao> _sessoes) {
		if (_sessoes != null && _sessoes.size() > 0) {
			for (Sessao _sessao : _sessoes) {
				if (!sessaoPessoaSecundariasInclusoras.contains(_sessao)) {
					sessaoPessoaSecundariasInclusoras.add(_sessao);
				}
			}
		}
	}
	
	public void addSessaoExclusora(List<Sessao> _sessoes) {
		if (_sessoes != null && _sessoes.size() > 0) {
			for (Sessao _sessao : _sessoes) {
				if (!sessaoPessoaSecundariasExclusoras.contains(_sessao)) {
					sessaoPessoaSecundariasExclusoras.add(_sessao);
				}
			}
		}
	}
	
	public void addQuadroAviso(List<QuadroAviso> _avisos) {
		if (_avisos != null && _avisos.size() > 0) {
			for (QuadroAviso _aviso : _avisos) {
				if (!avisosPessoaSecundarias.contains(_aviso)) {
					avisosPessoaSecundarias.add(_aviso);
				}
			}
		}
	}
	
	public void addProcessoDocumentoFavorito(List<ProcessoDocumentoFavorito> _procDocFavoritos) {
		if (_procDocFavoritos != null && _procDocFavoritos.size() > 0) {
			for (ProcessoDocumentoFavorito _procDocFav : _procDocFavoritos) {
				if (!procsDocFavoritosPessoaSecundarias.contains(_procDocFav)) {
					procsDocFavoritosPessoaSecundarias.add(_procDocFav);
				}
			}
		}
	}
	
	public void addNotaSessaoJulgamento(List<NotaSessaoJulgamento> _notasSessaoJulg) {
		if (_notasSessaoJulg != null && _notasSessaoJulg.size() > 0) {
			for (NotaSessaoJulgamento _notaSessJulg : _notasSessaoJulg) {
				if (!notasSessaoJulgamentoPessoaSecundarias.contains(_notaSessJulg)) {
					notasSessaoJulgamentoPessoaSecundarias.add(_notaSessJulg);
				}
			}
		}
	}
	
	public void addModeloProclamacaoJulgamento(List<ModeloProclamacaoJulgamento> _modelos) {
		if (_modelos != null && _modelos.size() > 0) {
			for (ModeloProclamacaoJulgamento _modelo : _modelos) {
				if (!modelosProclamacaoJulgamentoPessoasSecundarias.contains(_modelo)) {
					modelosProclamacaoJulgamentoPessoasSecundarias.add(_modelo);
				}
			}
		}
	}
	
	public void addLogHistMov(List<LogHistoricoMovimentacao> _logsHistMovimentacao) {
		if (_logsHistMovimentacao != null && _logsHistMovimentacao.size() > 0) {
			for (LogHistoricoMovimentacao _log : _logsHistMovimentacao) {
				if (!logsHistoricoMovimentacaoPessoasSecundarias.contains(_log)) {
					logsHistoricoMovimentacaoPessoasSecundarias.add(_log);
				}
			}
		}
	}
	
	public void addVisibilidadeDocumentosIdentificacao(List<VisibilidadePessoaDocumentoIdentificacao> _visibilidades) {
		if (_visibilidades != null && _visibilidades.size() > 0) {
			for (VisibilidadePessoaDocumentoIdentificacao _visibilidade : _visibilidades) {
				if (!visibilidadesDocumentoIdentificacaoPessoasSecundarias.contains(_visibilidade)) {
					visibilidadesDocumentoIdentificacaoPessoasSecundarias.add(_visibilidade);
				}
			}
		}
	}
	
	public void addMeiosContatoProprietarios(List<MeioContato> _meiosContato) {
		if(_meiosContato != null && _meiosContato.size() > 0) {
			for (MeioContato meioContatoPessSecundaria : _meiosContato) {
				if(!verificaConflitoMeioContato(meioContatoPessSecundaria)){
					if(!meiosContatosProprietariasPessoasSecundarias.contains(meioContatoPessSecundaria)) {
						meiosContatosProprietariasPessoasSecundarias.add(meioContatoPessSecundaria);
					}
				} else {
					meiosContatosConflito.add(meioContatoPessSecundaria);
				}
			}
		}
	}
	
	public void addNomeAlternativoProprietarios(List<PessoaNomeAlternativo> _nomesAlternativos) {
		if(_nomesAlternativos != null && _nomesAlternativos.size() > 0) {
			for (PessoaNomeAlternativo nomeAlternativoPessSecundaria : _nomesAlternativos) {
				if(!verificaConflitoNomeAlternativo(nomeAlternativoPessSecundaria)){
					if(!nomesAlternativosProprietariasPessoasSecundarias.contains(nomeAlternativoPessSecundaria)) {
						nomesAlternativosProprietariasPessoasSecundarias.add(nomeAlternativoPessSecundaria);
					}
				} else {
					nomesAlternativosConflito.add(nomeAlternativoPessSecundaria);
				}
			}
		}
	}
	
	public void addCaracteristicasFisicasPessoaPrincipal(List<CaracteristicaFisica> _caractFisPessPrincipal) {
		if(_caractFisPessPrincipal != null && _caractFisPessPrincipal.size() > 0) {
			caracteristicasFisicasPessoaPrincipal.addAll(_caractFisPessPrincipal);
		}
	}	
	
	public void addCaixaRepresentantePessoaPrincipal(List<CaixaRepresentante> _caixasRepresentantes) {
		if(_caixasRepresentantes != null && _caixasRepresentantes.size() > 0) {
			caixasRepresentantesPessoaPrincipal.addAll(_caixasRepresentantes);
		}
	}
	
	public void addMeioContatoProprietariaPessoaPrincipal(List<MeioContato> _meiosContatoPessPrincipal) {
		if(_meiosContatoPessPrincipal != null && _meiosContatoPessPrincipal.size() > 0) {
			meiosContatosProprietariaPessoaPrincipal.addAll(_meiosContatoPessPrincipal);
		}
	}
	
	public void addNomeAlternativoProprietariaPessoaPrincipal(List<PessoaNomeAlternativo> _nomesAlternativosPessPrincipal) {
		if(_nomesAlternativosPessPrincipal != null && _nomesAlternativosPessPrincipal.size() > 0) {
			nomesAlternativosProprietariaPessoaPrincipal.addAll(_nomesAlternativosPessPrincipal);
		}
	}
	
	/**
	 * metodo responsavel por adicionar na lista de caracteristicasFisicasPessoasSecundarias as caracteristicas fisicas
	 * passadas em parametro.
	 * verifica antes da adicao se existe conflito com alguma caracteristica da pessoa principal ou com alguma caracteristica
	 * já adicionada na lista.
	 * caso exista conflito, adiciona a caracteristicas na lista de conflitos.
	 * caso nao exista conflito, adiciona a caracteristica na lista de caracteristicas para unificacao.
	 * @param _caractFisPessSecundaria
	 */
	public void addCaracteristicasFisicasPessoaSecundaria(List<CaracteristicaFisica> _caractFisPessSecundaria) {
		if(_caractFisPessSecundaria != null && _caractFisPessSecundaria.size() > 0) {
			for (CaracteristicaFisica caracteristicaFisica : _caractFisPessSecundaria) {
				if(!verificaConflitoCaracteristicasFisicas(caracteristicaFisica)){
					if(!caracteristicasFisicasPessoasSecundarias.contains(caracteristicaFisica)) {
						caracteristicasFisicasPessoasSecundarias.add(caracteristicaFisica);
					}
				} else {
					caracteristicasFisicasConflito.add(caracteristicaFisica);
				}
			}
		}
	}
	
	/**
	 * metodo responsavel por adicionar na lista de conexoesProcessosPessoasSecundarias as validacoes de provençoes
	 * passadas em parametro.
	 * 
	 * @param recuperaConexoesPrevencoes
	 */
	public void addConexaoPrevencaoProcessoPessoaSecundaria(List<ProcessoTrfConexao> _conexaoesPrevencoes) {
		if(_conexaoesPrevencoes != null && _conexaoesPrevencoes.size() > 0) {
			conexoesPrevencaoPessoasSecundarias.addAll(_conexaoesPrevencoes);
		}
	}
	
	public void addProcessoParteSigiloPessoaSecundaria(List<ProcessoParteSigilo> _procParteSigilos) {
		if(_procParteSigilos != null && _procParteSigilos.size() > 0) {
			processosParteSigiloPessoasSecundarias.addAll(_procParteSigilos);
		}
	}
	
	public void addCaixasRepresentantesPessoaSecundaria(List<CaixaRepresentante> _caixasRepresentantes) {
		if(_caixasRepresentantes != null && _caixasRepresentantes.size() > 0) {
			for (CaixaRepresentante caixa : _caixasRepresentantes) {
				if(!verificaConflitoCaixasRepresentantes(caixa)){
					if(!caixasRepresentantesPessoasSecundarias.contains(caixa)) {
						caixasRepresentantesPessoasSecundarias.add(caixa);
					}
				} else {
					caixasRepresentantesConflito.add(caixa);
				}
			}
		}
	}
	
	/**
	 * metodo responsavel pro retornar uma lista com os logs de acesso separados para unificacao
	 * da pessoa secundaria passada em parametro.
	 * @param _pessoaSecundaria
	 * @return
	 */
	public List<LogAcesso> procuraLogAcesso(Pessoa _pessoaSecundaria) {
		List<LogAcesso> retorno = new ArrayList<LogAcesso>(0);
		if (logsAcessoPessoaSecundarias != null && logsAcessoPessoaSecundarias.size() > 0) {
			for (LogAcesso logAcesso : logsAcessoPessoaSecundarias) {
				if(logAcesso.isUsuarioEquals(_pessoaSecundaria)) {
					retorno.add(logAcesso);
				}
			}
		}
		return retorno;
	}
	
	/**
	 * metodo responsavel por retornar uma lista com as caracteristicas fisicas separadas para unificacao 
	 * da pessoa secundaria passada em parametro.
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<CaracteristicaFisica> procuraCaractFisica(Pessoa _pessoaSecundaria) {
		List<CaracteristicaFisica> retorno = new ArrayList<CaracteristicaFisica>(0);
		if (caracteristicasFisicasPessoasSecundarias != null && caracteristicasFisicasPessoasSecundarias.size() > 0) {
			for (CaracteristicaFisica objeto : caracteristicasFisicasPessoasSecundarias) {
				if(objeto.getPessoaFisica().getIdUsuario().equals(_pessoaSecundaria.getIdUsuario())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoTrfConexao> procuraConexoesPrevencoes(Pessoa _pessoaSecundaria) {
		List<ProcessoTrfConexao> retorno = new ArrayList<ProcessoTrfConexao>(0);
		if (conexoesPrevencaoPessoasSecundarias != null && conexoesPrevencaoPessoasSecundarias.size() > 0) {
			for (ProcessoTrfConexao objeto : conexoesPrevencaoPessoasSecundarias) {
				if(objeto.getPessoaFisica().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoSegredo> procuraSegredosProcessos(Pessoa _pessoaSecundaria) {
		List<ProcessoSegredo> retorno = new ArrayList<ProcessoSegredo>(0);
		if (processoSegredoCadastradosPessoaSecundarias != null && processoSegredoCadastradosPessoaSecundarias.size() > 0) {
			for (ProcessoSegredo objeto : processoSegredoCadastradosPessoaSecundarias) {
				if(objeto.getUsuarioLogin().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoParteSigilo> procuraProcessosParteSigilo(Pessoa _pessoaSecundaria) {
		List<ProcessoParteSigilo> retorno = new ArrayList<ProcessoParteSigilo>(0);
		if (processosParteSigiloPessoasSecundarias != null && processosParteSigiloPessoasSecundarias.size() > 0) {
			for (ProcessoParteSigilo objeto : processosParteSigiloPessoasSecundarias) {
				if(objeto.getUsuarioCadastro().getIdPessoa().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	/**
	 * metodo responsavel por retornar uma lista com os meios de contato cadastrados pela pessoa passada em parametro
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<MeioContato> procuraMeiosContatosCadastrados(Pessoa _pessoaSecundaria) {
		List<MeioContato> retorno = new ArrayList<MeioContato>(0);
		if (meiosContatosCadastradosPessoaSecundarias != null && meiosContatosCadastradosPessoaSecundarias.size() > 0) {
			for (MeioContato objeto : meiosContatosCadastradosPessoaSecundarias) {
				if(objeto.getUsuarioCadastrador().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	/**
	 * metodo responsavel por retonar uma lista com os nomes alternativos cadastrados pela pessoa passada em parametro
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<PessoaNomeAlternativo> procuraNomesAlternativosCadastrados(Pessoa _pessoaSecundaria) {
		List<PessoaNomeAlternativo> retorno = new ArrayList<PessoaNomeAlternativo>(0);
		if (nomesAlternativosCadastradosPessoaSecundarias != null && nomesAlternativosCadastradosPessoaSecundarias.size() > 0) {
			for (PessoaNomeAlternativo objeto : nomesAlternativosCadastradosPessoaSecundarias) {
				if(objeto.getUsuarioCadastrador().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<MeioContato> procuraMeiosContatosProprietaria(Pessoa _pessoaSecundaria) {
		List<MeioContato> retorno = new ArrayList<MeioContato>(0);
		if (meiosContatosProprietariasPessoasSecundarias != null && meiosContatosProprietariasPessoasSecundarias.size() > 0) {
			for (MeioContato objeto : meiosContatosProprietariasPessoasSecundarias) {
				if(objeto.getPessoa().getIdPessoa().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<PessoaNomeAlternativo> procuraNomesAlternativosProprietaria(Pessoa _pessoaSecundaria) {
		List<PessoaNomeAlternativo> retorno = new ArrayList<PessoaNomeAlternativo>(0);
		if (nomesAlternativosProprietariasPessoasSecundarias != null && nomesAlternativosProprietariasPessoasSecundarias.size() > 0) {
			for (PessoaNomeAlternativo objeto : nomesAlternativosProprietariasPessoasSecundarias) {
				if(objeto.getPessoa().getIdPessoa().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<CaixaRepresentante> procuraCaixasRepresentantes(Pessoa _pessoaSecundaria) {
		List<CaixaRepresentante> retorno = new ArrayList<CaixaRepresentante>(0);
		if (caixasRepresentantesPessoasSecundarias != null && caixasRepresentantesPessoasSecundarias.size() > 0) {
			for (CaixaRepresentante objeto : caixasRepresentantesPessoasSecundarias) {
				if(objeto.getRepresentante().getIdPessoa().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<SessaoEnteExterno> procuraSessaoEnteExternos(Pessoa _pessoaSecundaria) {
		List<SessaoEnteExterno> retorno = new ArrayList<SessaoEnteExterno>(0);
		if (sessoesEntesExternosPessoaSecundarias != null && sessoesEntesExternosPessoaSecundarias.size() > 0) {
			for (SessaoEnteExterno objeto : sessoesEntesExternosPessoaSecundarias) {
				if(objeto.getPessoaAcompanhaSessao().getIdPessoa().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoTrfRedistribuicao> procuraProcessosRedistribuicao(Pessoa _pessoaSecundaria) {
		List<ProcessoTrfRedistribuicao> retorno = new ArrayList<ProcessoTrfRedistribuicao>(0);
		if (redistribuicoesProcessosPessoaSecundarias != null && redistribuicoesProcessosPessoaSecundarias.size() > 0) {
			for (ProcessoTrfRedistribuicao objeto : redistribuicoesProcessosPessoaSecundarias) {
				if(objeto.getUsuario().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoParteHistorico> procuraProcessosParteHistoricos(Pessoa _pessoaSecundaria) {
		List<ProcessoParteHistorico> retorno = new ArrayList<ProcessoParteHistorico>(0);
		if (processosParteHistoricosPessoaSecundarias != null && processosParteHistoricosPessoaSecundarias.size() > 0) {
			for (ProcessoParteHistorico objeto : processosParteHistoricosPessoaSecundarias) {
				if(objeto.getUsuarioLogin().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoTag> procuraProcessosTag(Pessoa _pessoaSecundaria) {
		List<ProcessoTag> retorno = new ArrayList<ProcessoTag>(0);
		if (processosTagPessoaSecundarias != null && processosTagPessoaSecundarias.size() > 0) {
			for (ProcessoTag objeto : processosTagPessoaSecundarias) {
				if(objeto.getIdUsuarioInclusao().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<Lembrete> procuraLembretes(Pessoa _pessoaSecundaria) {
		List<Lembrete> retorno = new ArrayList<Lembrete>(0);
		if (lembretesPessoaSecundarias != null && lembretesPessoaSecundarias.size() > 0) {
			for (Lembrete objeto : lembretesPessoaSecundarias) {
				if(objeto.getUsuarioLocalizacao() != null && objeto.getUsuarioLocalizacao().getIdUsuarioLocalizacao() == _pessoaSecundaria.getUsuarioLocalizacaoInicial().getIdUsuarioLocalizacao()) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<LembretePermissao> procuraPermissoesLembretes(Pessoa _pessoaSecundaria) {
		List<LembretePermissao> retorno = new ArrayList<LembretePermissao>(0);
		if (permissoesLembretesPessoaSecundarias != null && permissoesLembretesPessoaSecundarias.size() > 0) {
			for (LembretePermissao objeto : permissoesLembretesPessoaSecundarias) {
				if(objeto.getUsuario().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<Processo> procuraProcessos(Pessoa _pessoaSecundaria) {
		List<Processo> retorno = new ArrayList<Processo>(0);
		if (processosPessoaSecundarias != null && processosPessoaSecundarias.size() > 0) {
			for (Processo objeto : processosPessoaSecundarias) {
				if(objeto.getUsuarioCadastroProcesso().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<Parametro> procuraParametros(Pessoa _pessoaSecundaria) {
		List<Parametro> retorno = new ArrayList<Parametro>(0);
		if (parametrosPessoaSecundarias != null && parametrosPessoaSecundarias.size() > 0) {
			for (Parametro objeto : parametrosPessoaSecundarias) {
				if(objeto.getUsuarioModificacao().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<EntityLog> procuraEntityLogs(Pessoa _pessoaSecundaria) {
		List<EntityLog> retorno = new ArrayList<EntityLog>(0);
		if (entityLogPessoaSecundarias != null && entityLogPessoaSecundarias.size() > 0) {
			for (EntityLog objeto : entityLogPessoaSecundarias) {
				if(objeto.getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<SolicitacaoNoDesvio> procuraSolicitacoesNoDesvio(Pessoa _pessoaSecundaria) {
		List<SolicitacaoNoDesvio> retorno = new ArrayList<SolicitacaoNoDesvio>(0);
		if (solicitacoesNoDesvioPessoaSecundarias != null && solicitacoesNoDesvioPessoaSecundarias.size() > 0) {
			for (SolicitacaoNoDesvio objeto : solicitacoesNoDesvioPessoaSecundarias) {
				if(objeto.getUsuario().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<SessaoPautaProcessoTrf> procuraSessaoPautaProcesso(Pessoa _pessoaSecundaria, boolean isPessoaInclusora) {
		List<SessaoPautaProcessoTrf> retorno = new ArrayList<SessaoPautaProcessoTrf>(0);
		if(isPessoaInclusora) {
			if (sessaoPautaProcessoPessoaSecundariasInclusoras != null && sessaoPautaProcessoPessoaSecundariasInclusoras.size() > 0) {
				for (SessaoPautaProcessoTrf objeto : sessaoPautaProcessoPessoaSecundariasInclusoras) {
					if(objeto.getUsuarioInclusao().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
						retorno.add(objeto);
					}
				}
			}
		}else {
			if (sessaoPautaProcessoPessoaSecundariasExclusoras != null && sessaoPautaProcessoPessoaSecundariasExclusoras.size() > 0) {
				for (SessaoPautaProcessoTrf objeto : sessaoPautaProcessoPessoaSecundariasExclusoras) {
					if(objeto.getUsuarioExclusao().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
						retorno.add(objeto);
					}
				}
			}
		}
		return retorno;
	}
	
	public List<Sessao> procuraSessao(Pessoa _pessoaSecundaria, boolean isPessoaInclusora) {
		List<Sessao> retorno = new ArrayList<Sessao>(0);
		if(isPessoaInclusora) {
			if (sessaoPessoaSecundariasInclusoras != null && sessaoPessoaSecundariasInclusoras.size() > 0) {
				for (Sessao objeto : sessaoPessoaSecundariasInclusoras) {
					if(objeto.getUsuarioInclusao().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
						retorno.add(objeto);
					}
				}
			}
		}else {
			if (sessaoPessoaSecundariasExclusoras != null && sessaoPessoaSecundariasExclusoras.size() > 0) {
				for (Sessao objeto : sessaoPessoaSecundariasExclusoras) {
					if(objeto.getUsuarioExclusao().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
						retorno.add(objeto);
					}
				}
			}
		}
		return retorno;
	}
	
	public List<QuadroAviso> procuraAvisoQuadroAviso(Pessoa _pessoaSecundaria) {
		List<QuadroAviso> retorno = new ArrayList<QuadroAviso>(0);

		if (avisosPessoaSecundarias != null && avisosPessoaSecundarias.size() > 0) {
			for (QuadroAviso objeto : avisosPessoaSecundarias) {
				if(objeto.getUsuarioInclusao().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ProcessoDocumentoFavorito> procuraProcessoDocumentoFavorito(Pessoa _pessoaSecundaria) {
		List<ProcessoDocumentoFavorito> retorno = new ArrayList<ProcessoDocumentoFavorito>(0);

		if (procsDocFavoritosPessoaSecundarias != null && procsDocFavoritosPessoaSecundarias.size() > 0) {
			for (ProcessoDocumentoFavorito objeto : procsDocFavoritosPessoaSecundarias) {
				if(objeto.getUsuario().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<NotaSessaoJulgamento> procuraNotasSessaoJulgamento(Pessoa _pessoaSecundaria) {
		List<NotaSessaoJulgamento> retorno = new ArrayList<NotaSessaoJulgamento>(0);

		if (notasSessaoJulgamentoPessoaSecundarias != null && notasSessaoJulgamentoPessoaSecundarias.size() > 0) {
			for (NotaSessaoJulgamento objeto : notasSessaoJulgamentoPessoaSecundarias) {
				if(objeto.getUsuarioCadastro().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<ModeloProclamacaoJulgamento> procuraModelosProclamacaoJulgamento(Pessoa _pessoaSecundaria) {
		List<ModeloProclamacaoJulgamento> retorno = new ArrayList<ModeloProclamacaoJulgamento>(0);

		if (modelosProclamacaoJulgamentoPessoasSecundarias != null && modelosProclamacaoJulgamentoPessoasSecundarias.size() > 0) {
			for (ModeloProclamacaoJulgamento objeto : modelosProclamacaoJulgamentoPessoasSecundarias) {
				if(objeto.getUsuario().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<LogHistoricoMovimentacao> procuraLogsHistoricoMovimentacao(Pessoa _pessoaSecundaria) {
		List<LogHistoricoMovimentacao> retorno = new ArrayList<LogHistoricoMovimentacao>(0);

		if (logsHistoricoMovimentacaoPessoasSecundarias != null && logsHistoricoMovimentacaoPessoasSecundarias.size() > 0) {
			for (LogHistoricoMovimentacao objeto : logsHistoricoMovimentacaoPessoasSecundarias) {
				if(objeto.getUsuario().getIdUsuario().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	public List<VisibilidadePessoaDocumentoIdentificacao> procuraVisibilidadesDocumentoIdentificacao(Pessoa _pessoaSecundaria) {
		List<VisibilidadePessoaDocumentoIdentificacao> retorno = new ArrayList<VisibilidadePessoaDocumentoIdentificacao>(0);

		if (visibilidadesDocumentoIdentificacaoPessoasSecundarias != null && visibilidadesDocumentoIdentificacaoPessoasSecundarias.size() > 0) {
			for (VisibilidadePessoaDocumentoIdentificacao objeto : visibilidadesDocumentoIdentificacaoPessoasSecundarias) {
				if(objeto.getPessoa().getIdPessoa().equals(_pessoaSecundaria.getIdPessoa())) {
					retorno.add(objeto);
				}
			}
		}
		return retorno;
	}
	
	/**
	 * metodo responsavel por verificar a existencia de conflito entre caracteristicas fisicas das pessoas da unificacao.
	 * logica:
	 * compara a caracteristica fisica passada em parametro com todas as caracterisiticas fisicas da pessoa principal e com
	 * as caracteristicas fisicas das pessoas secundarias já adicionadas. caso o atributo 'caracteristicaFisica' seja igual entre
	 * as duas caracteristicas fisicas, existe conflito.
	 * as caracteristicas fisicas da pessoa principal nunca estao em conflito.
	 * @param _caractFis
	 * @return true se conflito / false
	 */
	private boolean verificaConflitoCaracteristicasFisicas(CaracteristicaFisica _caractFis) {
		boolean conflito = false;
		for (CaracteristicaFisica caractFisPessPrincipal : caracteristicasFisicasPessoaPrincipal) {
			conflito = _caractFis.getCaracteristicaFisica().equals(caractFisPessPrincipal.getCaracteristicaFisica());
			if(conflito) {
				return conflito;
			}
		}
		for (CaracteristicaFisica caractFisPessSecundaria : caracteristicasFisicasPessoasSecundarias) {
			conflito = _caractFis.getCaracteristicaFisica().equals(caractFisPessSecundaria.getCaracteristicaFisica());
			if(conflito) {
				return conflito;
			}
		}
		return conflito;
	}
	
	private boolean verificaConflitoMeioContato(MeioContato _meioContato) {
		boolean conflito = false;
		for (MeioContato meioContatoPessPrincipal : meiosContatosProprietariaPessoaPrincipal) {
			conflito = _meioContato.getTipoContato().equals(meioContatoPessPrincipal.getTipoContato()) && 
					_meioContato.getValorMeioContato().equals(meioContatoPessPrincipal.getValorMeioContato());
			
			if(conflito) {
				return conflito;
			}
		}
		for (MeioContato meioContatoPessSecundaria : meiosContatosProprietariasPessoasSecundarias) {
			conflito = _meioContato.getTipoContato().equals(meioContatoPessSecundaria.getTipoContato()) && 
					_meioContato.getValorMeioContato().equals(meioContatoPessSecundaria.getValorMeioContato());
			
			if(conflito) {
				return conflito;
			}
		}
		return conflito;
	}
	
	private boolean verificaConflitoNomeAlternativo(PessoaNomeAlternativo _nomeAlternativo) {
		boolean conflito = false;
		for (PessoaNomeAlternativo nomeAlternPessPrincipal : nomesAlternativosProprietariaPessoaPrincipal) {
			conflito = _nomeAlternativo.getPessoaNomeAlternativo().equals(nomeAlternPessPrincipal.getPessoaNomeAlternativo());
			if(conflito) {
				return conflito;
			}
		}
		for (PessoaNomeAlternativo nomeAlternPessSecundaria : nomesAlternativosProprietariasPessoasSecundarias) {
			conflito = _nomeAlternativo.getPessoaNomeAlternativo().equals(nomeAlternPessSecundaria.getPessoaNomeAlternativo());
			if(conflito) {
				return conflito;
			}
		}
		return conflito;
	}
	
	private boolean verificaConflitoCaixasRepresentantes(CaixaRepresentante _caixa) {
		boolean conflito = false;
		for (CaixaRepresentante caixaPessPrincipal : caixasRepresentantesPessoaPrincipal) {
			conflito = _caixa.getCaixaAdvogadoProcurador().getIdCaixaAdvogadoProcurador().equals(
					caixaPessPrincipal.getCaixaAdvogadoProcurador().getIdCaixaAdvogadoProcurador());
			
			if(conflito) {
				return conflito;
			}
		}
		for (CaixaRepresentante caixaPessSecundaria : caixasRepresentantesPessoasSecundarias) {
			conflito = _caixa.getCaixaAdvogadoProcurador().getIdCaixaAdvogadoProcurador().equals(
					caixaPessSecundaria.getCaixaAdvogadoProcurador().getIdCaixaAdvogadoProcurador());
			
			if(conflito) {
				return conflito;
			}
		}
		return conflito;
	}

	public List<LogAcesso> getLogsAcessoPessoaSecundarias() {
		return logsAcessoPessoaSecundarias;
	}
	
	/**
	 * metodo responsavel por retornar as caracteristicas fisicas da pessoa principal.
	 * estas caracteristicas nao serão unificadas nem alteradas.
	 * @return
	 */
	public List<CaracteristicaFisica> getCaracteristicaFisicaPessoaPrincipal() {
		return caracteristicasFisicasPessoaPrincipal;
	}
	
	/**
	 * metodo responsavel por retornar as caracteristicas fisicas das pessoas secundarias.
	 * estas caracteristicas seram alteradas e unificadas à pessoa principal.
	 * @return
	 */
	public List<CaracteristicaFisica> getCaracteristicaFisicaPessoaSecundaria() {
		return caracteristicasFisicasPessoasSecundarias;
	}
	
	/**
	 * metodo responsavel por retornar as caracteristicas fisicas das pessoas secundarias que estao em conflito,
	 * com caracteristicas fisicas da pessoa principal ou com alguma das caracteristicas fisicas das pessoas secundarias já 
	 * acionadas.
	 * @return
	 */
	public List<CaracteristicaFisica> getCaracteristicaFisicaConflito() {
		return caracteristicasFisicasConflito;
	}
	
	public PessoaFisica getPessoaFisicaPessoaPrincipal() {
		return pessoaFisicaPessoaPrincipal;
	}
	
	public void setUsuarioPessoaPrincipal(Usuario usuarioPessoaPrincipal) {
		this.usuarioPessoaPrincipal = usuarioPessoaPrincipal;
	}
	
	public Usuario getUsuarioPessoaPrincipal() {
		return usuarioPessoaPrincipal;
	}
	
	public UsuarioLogin getUsuarioLoginPessoaPrincipal() {
		return usuarioLoginPessoaPrincipal;
	}

	public void setUsuarioLoginPessoaPrincipal(UsuarioLogin usuarioLoginPessoaPrincipal) {
		this.usuarioLoginPessoaPrincipal = usuarioLoginPessoaPrincipal;
	}
	
	public void setPessoaFisicaPessoaPrincipal(PessoaFisica _pessoaFisica) {
		this.pessoaFisicaPessoaPrincipal = _pessoaFisica;
	}

	public PessoaJuridica getPessoaJuridicaPessoaPrincipal() {
		return pessoaJuridicaPessoaPrincipal;
	}

	public void setPessoaJuridicaPessoaPrincipal(PessoaJuridica _pessoaJuridica) {
		this.pessoaJuridicaPessoaPrincipal = _pessoaJuridica;
	}

	public PessoaAutoridade getPessoaAutoridadePessoaPrincipal() {
		return pessoaAutoridadePessoaPrincipal;
	}

	public void setPessoaAutoridadePessoaPrincipal(PessoaAutoridade _pessoaAutoridade) {
		this.pessoaAutoridadePessoaPrincipal = _pessoaAutoridade;
	}

	public List<MeioContato> getMeiosContatosCadastradosPessoaSecundarias() {
		return meiosContatosCadastradosPessoaSecundarias;
	}
	
	public List<MeioContato> getMeiosContatosProprietariasPessoasSecundarias() {
		return meiosContatosProprietariasPessoasSecundarias;
	}

	public List<MeioContato> getMeiosContatosConflito() {
		return meiosContatosConflito;
	}

	public List<PessoaNomeAlternativo> getNomesAlternativosCadastradosPessoasSecundarias() {
		return nomesAlternativosCadastradosPessoaSecundarias;
	}

	public List<PessoaNomeAlternativo> getNomesAlternativosProprietariasPessoasSecundarias() {
		return nomesAlternativosProprietariasPessoasSecundarias;
	}

	public List<PessoaNomeAlternativo> getNomesAlternativosConflito() {
		return nomesAlternativosConflito;
	}

	public List<ProcessoTrfConexao> getConexoesPrevencaoPessoasSecundarias() {
		return conexoesPrevencaoPessoasSecundarias;
	}

	public List<ProcessoSegredo> getProcessoSegredoCadastradosPessoaSecundarias() {
		return processoSegredoCadastradosPessoaSecundarias;
	}

	public List<ProcessoParteSigilo> getProcessosParteSigiloPessoasSecundarias() {
		return processosParteSigiloPessoasSecundarias;
	}

	public List<CaixaRepresentante> getCaixasRepresentantesPessoasSecundarias() {
		return caixasRepresentantesPessoasSecundarias;
	}

	public List<CaixaRepresentante> getCaixasRepresentantesConflito() {
		return caixasRepresentantesConflito;
	}

	public List<SessaoEnteExterno> getSessoesEntesExternosPessoaSecundarias() {
		return sessoesEntesExternosPessoaSecundarias;
	}
	
	public List<ProcessoTrfRedistribuicao> getRedistribuicoesProcessosPessoaSecundarias() {
		return redistribuicoesProcessosPessoaSecundarias;
	}

	public List<ProcessoParteHistorico> getProcessosParteHistoricosPessoaSecundarias() {
		return processosParteHistoricosPessoaSecundarias;
	}
	
	public List<ProcessoTag> getProcessosTagPessoaSecundarias() {
		return processosTagPessoaSecundarias;
	}

	public List<Lembrete> getLembretesPessoaSecundarias() {
		return lembretesPessoaSecundarias;
	}
	
	public List<LembretePermissao> getPermissoesLembretesPessoaSecundarias() {
		return permissoesLembretesPessoaSecundarias;
	}

	public List<Processo> getProcessosPessoaSecundarias() {
		return processosPessoaSecundarias;
	}

	public List<Parametro> getParametrosPessoaSecundarias() {
		return parametrosPessoaSecundarias;
	}

	public List<EntityLog> getEntityLogPessoaSecundarias() {
		return entityLogPessoaSecundarias;
	}

	public List<SolicitacaoNoDesvio> getSolicitacoesNoDesvioPessoaSecundarias() {
		return solicitacoesNoDesvioPessoaSecundarias;
	}
	
	public List<SessaoPautaProcessoTrf> getSessaoPautaProcessoPessoaSecundarias() {
		return sessaoPautaProcessoPessoaSecundariasInclusoras;
	}

	public List<SessaoPautaProcessoTrf> getSessaoPautaProcessoPessoaSecundariasExclusoras() {
		return sessaoPautaProcessoPessoaSecundariasExclusoras;
	}
	
	public List<Sessao> getSessaoPessoaSecundariasInclusoras() {
		return sessaoPessoaSecundariasInclusoras;
	}

	public List<Sessao> getSessaoPessoaSecundariasExclusoras() {
		return sessaoPessoaSecundariasExclusoras;
	}
	
	public List<QuadroAviso> getAvisosPessoaSecundarias() {
		return avisosPessoaSecundarias;
	}

	public List<ProcessoDocumentoFavorito> getProcsDocFavoritosPessoaSecundarias() {
		return procsDocFavoritosPessoaSecundarias;
	}
	
	public List<NotaSessaoJulgamento> getNotasSessaoJulgamentoPessoaSecundarias() {
		return notasSessaoJulgamentoPessoaSecundarias;
	}

	public List<ModeloProclamacaoJulgamento> getModelosProclamacaoJulgamentoPessoasSecundarias() {
		return modelosProclamacaoJulgamentoPessoasSecundarias;
	}
	
	public List<LogHistoricoMovimentacao> getLogsHistoricoMovimentacaoPessoasSecundarias() {
		return logsHistoricoMovimentacaoPessoasSecundarias;
	}

	public List<VisibilidadePessoaDocumentoIdentificacao> getVisibilidadesDocumentoIdentificacaoPessoasSecundarias() {
		return visibilidadesDocumentoIdentificacaoPessoasSecundarias;
	}
	
}