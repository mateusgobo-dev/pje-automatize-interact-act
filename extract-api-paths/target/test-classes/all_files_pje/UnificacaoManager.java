package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.UnificacaoDAO;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.Lembrete;
import br.jus.pje.nucleo.entidades.LembretePermissao;
import br.jus.pje.nucleo.entidades.LogHistoricoMovimentacao;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoFavorito;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoEnteExterno;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SolicitacaoNoDesvio;
import br.jus.pje.nucleo.entidades.Unificacao;
import br.jus.pje.nucleo.entidades.UnificacaoPessoas;
import br.jus.pje.nucleo.entidades.UnificacaoPessoasObjeto;
import br.jus.pje.nucleo.entidades.UnificacaoVO;
import br.jus.pje.nucleo.entidades.VisibilidadePessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.enums.TiposObjetosUnificadosEnum;

@Name(UnificacaoManager.NAME)
public class UnificacaoManager extends BaseManager<Unificacao>{

	public static final String NAME = "unificacaoManager";
	
	@In
	private UnificacaoDAO unificacaoDAO;

	@In(create=true)
	private TipoPessoaManager tipoPessoaManager;
	
	@In(create=true)
	private LogAcessoManager logAcessoManager;
	
	private Unificacao unificacao;
	UnificacaoPessoas unificacaoPessoaTemp;
	
	/**
	 * metodo responsavel por gerenciar a criacao dos objetos da unificacao e unificar as pessoas principal e secundarias
	 * @param unificacaoVO 
	 * @return true se unificacao OK / false
	 * @throws Exception 
	 */
	public void finalizarUnificacao(UnificacaoVO unificacaoVO) throws Exception {
		List<UnificacaoPessoas> unificacoesPessoas = new ArrayList<UnificacaoPessoas>(0);
		
		unificacao = new Unificacao(unificacaoVO.getPessoaPrincipal(), unificacaoVO.getUsuarioUnificador());
	
		alteraPessoaPrincipal();
	
		for (Pessoa pessoaSecundaria : unificacaoVO.getPessoasSecundariasUnificacao()) {
		
			unificacaoPessoaTemp = new UnificacaoPessoas(unificacao, alteraPessoaSecundaria(pessoaSecundaria));
			
			gerarObjetosUnificados(unificacaoVO, pessoaSecundaria);

			unificacoesPessoas.add(unificacaoPessoaTemp);
		}
	
		unificacao.getUnificacaoPessoasList().addAll(unificacoesPessoas);
		
		persisteAlteracoesUnificacao(unificacao);
	}	
	
	/**
	 * metodo responsavel por solicitar a criacao dos objetos da unificacao
	 * @param pessoaSecundaria 
	 * @param unificacaoVO 
	 */
	private void gerarObjetosUnificados(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		gerarObjetosLogAcesso(unificacaoVO, pessoaSecundaria);
		gerarObjetosCaracteristicasFisicas(unificacaoVO, pessoaSecundaria);
		gerarObjetosMeioContatoCadastrados(unificacaoVO, pessoaSecundaria);
		gerarObjetosMeioContatoProprietarias(unificacaoVO, pessoaSecundaria);
		gerarObjetosNomesAlternativosCadastrados(unificacaoVO, pessoaSecundaria);
		gerarObjetosNomesAlternativosProprietarias(unificacaoVO, pessoaSecundaria);
		gerarObjetosConexoesPrevencoes(unificacaoVO, pessoaSecundaria);
		gerarObjetosSegredoProcesso(unificacaoVO, pessoaSecundaria);
		gerarObjetosProcessoParteSigilo(unificacaoVO, pessoaSecundaria);
		gerarObjetosCaixasRepresentantes(unificacaoVO, pessoaSecundaria);
		gerarObjetosSessaoEnteExternos(unificacaoVO, pessoaSecundaria);
		gerarObjetosRedistribuicaoProcessos(unificacaoVO, pessoaSecundaria);
		gerarObjetosProcessosParteHistoricos(unificacaoVO, pessoaSecundaria);
		gerarObjetosProcessosTags(unificacaoVO, pessoaSecundaria);
		gerarObjetosLembretes(unificacaoVO, pessoaSecundaria);
		gerarObjetosLembretesPermissoes(unificacaoVO, pessoaSecundaria);
		gerarObjetosProcessos(unificacaoVO, pessoaSecundaria);
		gerarObjetosParametros(unificacaoVO, pessoaSecundaria);
		gerarObjetosEntityLogs(unificacaoVO, pessoaSecundaria);
		gerarObjetosSolicitacaoNoDesvio(unificacaoVO, pessoaSecundaria);
		gerarObjetosSessaoPautaProcessoInclusora(unificacaoVO, pessoaSecundaria);
		gerarObjetosSessaoPautaProcessoExclusora(unificacaoVO, pessoaSecundaria);
		gerarObjetosSessaoInclusora(unificacaoVO, pessoaSecundaria);
		gerarObjetosSessaoExclusora(unificacaoVO, pessoaSecundaria);
		gerarObjetosQuadroAvisos(unificacaoVO, pessoaSecundaria);
		gerarObjetosProcessosDocumentosFavoritos(unificacaoVO, pessoaSecundaria);
		gerarObjetosNotasSessaoJulgamento(unificacaoVO, pessoaSecundaria);
		gerarObjetosModelosProclamacaoJulgamento(unificacaoVO, pessoaSecundaria);
		gerarObjetosLogHistoricoMovimentacao(unificacaoVO, pessoaSecundaria);
		gerarObjetosVisibilidadeDocumentoIdentificacao(unificacaoVO, pessoaSecundaria);
	}

	/**
 	 * metodo que persiste as alteracoes realizadas no objeto unificacao
 	 * @param unif
 	 * @throws Exception
 	 */
 	public void persisteAlteracoesUnificacao(Unificacao unif) throws Exception {
 		unificacaoDAO.persisteAlteracoesUnificacao(unif);
 	}

	/**
	 * Metodo responsavel por alterar a pessoa principal da unificacao contendo as alterações básicas da unificacao
	 * 
	 * OBS: alguns cadastros no banco estao com o tipo de pessoa como null. este registro é obrigatorio. para resolver este problema,
	 * verificamos qual o tipo da passoa pelo TipoPessoaEnum e inserimos o tipo de pessoa correspondente.
	 * 
	 */
	private void alteraPessoaPrincipal() {
		unificacao.getPessoaPrincipal().setAtivo(Boolean.TRUE);
		unificacao.getPessoaPrincipal().setBloqueio(Boolean.FALSE);
		unificacao.getPessoaPrincipal().setUnificada(Boolean.FALSE);
		unificacao.getPessoaPrincipal().setNome(unificacao.getPessoaPrincipal().getNome().toUpperCase());
		if(unificacao.getPessoaPrincipal().getTipoPessoa() == null) {
			unificacao.getPessoaPrincipal().setTipoPessoa(tipoPessoaManager.obtemTipoPessoa(unificacao.getPessoaPrincipal().getInTipoPessoa()));
		}
	}
	
	/**
	 * Metodo responsavel por alterar a pessoa secundaria da unificacao contendo as alterações básicas da unificacao
	 * @param pessoaSecundaria
	 * @return pessoaSecundaria alterada
	 */
	private Pessoa alteraPessoaSecundaria(Pessoa pessoaSecundaria) {
		pessoaSecundaria.setAtivo(false);
		pessoaSecundaria.setBloqueio(true);
		pessoaSecundaria.setUnificada(true);
		pessoaSecundaria.setNome(pessoaSecundaria.getNome().toUpperCase());
		if(pessoaSecundaria.getTipoPessoa() == null) {
			pessoaSecundaria.setTipoPessoa(tipoPessoaManager.obtemTipoPessoa(pessoaSecundaria.getInTipoPessoa()));
		}
		pessoaSecundaria.setLogin(pessoaSecundaria.getLogin()+"--Pessoa unificada em:" + new Date());
		return pessoaSecundaria;
	}

	@Override
	protected UnificacaoDAO getDAO() {
		return unificacaoDAO;
	}
	
	/**
	 * metodo auxiliar que cria os objetos da unificacao do tipo de log de acesso.
	 * @param unificacaoVO
	 * @param pessoaSecundaria
	 */
	private void gerarObjetosLogAcesso(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<LogAcesso> logsAcessoPessoa = unificacaoVO.procuraLogAcesso(pessoaSecundaria);
		if(logsAcessoPessoa.size() > 0) {
			for (LogAcesso logAcesso : logsAcessoPessoa) {
				logAcesso.setUsuarioLogin(unificacaoVO.getPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.LOG_ACESSO, 
								logAcesso));
			}
		}	
	}
	
	/**
	 * metodo auxiliar que cria os objetos da unificacao do tipo de caracteristicas fisicas.
	 * @param unificacaoVO
	 * @param pessoaSecundaria
	 */
	private void gerarObjetosCaracteristicasFisicas(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<CaracteristicaFisica> lista = unificacaoVO.procuraCaractFisica(pessoaSecundaria);
		if(lista.size() > 0) {
			for (CaracteristicaFisica objeto : lista) {
				objeto.setPessoaFisica(unificacaoVO.getPessoaFisicaPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.CARACTERISTICA_FISICA, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosConexoesPrevencoes(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoTrfConexao> lista = unificacaoVO.procuraConexoesPrevencoes(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoTrfConexao objeto : lista) {
				objeto.setPessoaFisica(unificacaoVO.getPessoaFisicaPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.CONEXOES_PREVENCAO, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosSegredoProcesso(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoSegredo> lista = unificacaoVO.procuraSegredosProcessos(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoSegredo objeto : lista) {
				objeto.setUsuarioLogin(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SEGREDO_PROCESSO, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosProcessoParteSigilo(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoParteSigilo> lista = unificacaoVO.procuraProcessosParteSigilo(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoParteSigilo objeto : lista) {
				objeto.setUsuarioCadastro(unificacaoVO.getPessoaFisicaPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SIGILO_PROCESSO_PARTE, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosMeioContatoCadastrados(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<MeioContato> lista = unificacaoVO.procuraMeiosContatosCadastrados(pessoaSecundaria);
		if(lista.size() > 0) {
			for (MeioContato objeto : lista) {
				objeto.setUsuarioCadastrador(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.MEIO_CONTATO_CADASTRADOS, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosCaixasRepresentantes(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<CaixaRepresentante> lista = unificacaoVO.procuraCaixasRepresentantes(pessoaSecundaria);
		if(lista.size() > 0) {
			for (CaixaRepresentante objeto : lista) {
				objeto.setRepresentante(unificacaoVO.getPessoaFisicaPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.CAIXA_REPRESENTANTE, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosSessaoEnteExternos(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<SessaoEnteExterno> lista = unificacaoVO.procuraSessaoEnteExternos(pessoaSecundaria);
		if(lista.size() > 0) {
			for (SessaoEnteExterno objeto : lista) {
				objeto.setPessoaAcompanhaSessao(unificacaoVO.getPessoaPrincipal());
				objeto.setNomePessoa(unificacaoVO.getPessoaPrincipal().getNome());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SESSAO_ENTE_EXTERNO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosRedistribuicaoProcessos(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoTrfRedistribuicao> lista = unificacaoVO.procuraProcessosRedistribuicao(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoTrfRedistribuicao objeto : lista) {
				objeto.setUsuario(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PROCESSO_REDISTRIBUICAO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosProcessosParteHistoricos(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoParteHistorico> lista = unificacaoVO.procuraProcessosParteHistoricos(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoParteHistorico objeto : lista) {
				objeto.setUsuarioLogin(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PROCESSO_PARTE_HISTORICO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosProcessosTags(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoTag> lista = unificacaoVO.procuraProcessosTag(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoTag objeto : lista) {
				objeto.setIdUsuarioInclusao(unificacaoVO.getPessoaPrincipal().getIdPessoa());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PROCESSO_TAG, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosLembretes(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<Lembrete> lista = unificacaoVO.procuraLembretes(pessoaSecundaria);
		if(lista.size() > 0) {
			for (Lembrete objeto : lista) {
				objeto.setUsuarioLocalizacao(unificacaoVO.getUsuarioPessoaPrincipal().getUsuarioLocalizacaoInicial());
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.LEMBRETE, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosLembretesPermissoes(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<LembretePermissao> lista = unificacaoVO.procuraPermissoesLembretes(pessoaSecundaria);
		if(lista.size() > 0) {
			for (LembretePermissao objeto : lista) {
				objeto.setUsuario(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PERMISSAO_LEMBRETE, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosProcessos(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<Processo> lista = unificacaoVO.procuraProcessos(pessoaSecundaria);
		if(lista.size() > 0) {
			for (Processo objeto : lista) {
				objeto.setUsuarioCadastroProcesso(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PROCESSOS_PROTOCOLADOS, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosParametros(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<Parametro> lista = unificacaoVO.procuraParametros(pessoaSecundaria);
		if(lista.size() > 0) {
			for (Parametro objeto : lista) {
				objeto.setUsuarioModificacao(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PARAMETROS_ALTERADOS, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosEntityLogs(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<EntityLog> lista = unificacaoVO.procuraEntityLogs(pessoaSecundaria);
		if(lista.size() > 0) {
			for (EntityLog objeto : lista) {
				objeto.setIdUsuario(unificacaoVO.getPessoaPrincipal().getIdPessoa());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.ENTITY_LOGS, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosSolicitacaoNoDesvio(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<SolicitacaoNoDesvio> lista = unificacaoVO.procuraSolicitacoesNoDesvio(pessoaSecundaria);
		if(lista.size() > 0) {
			for (SolicitacaoNoDesvio objeto : lista) {
				objeto.setUsuario(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SOLICITACAO_NO_DESVIO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosSessaoPautaProcessoInclusora(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<SessaoPautaProcessoTrf> lista = unificacaoVO.procuraSessaoPautaProcesso(pessoaSecundaria, true);
		if(lista.size() > 0) {
			for (SessaoPautaProcessoTrf objeto : lista) {
				objeto.setUsuarioInclusao(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SESSAO_PAUTA_PROC_INCLUSORA, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosSessaoPautaProcessoExclusora(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<SessaoPautaProcessoTrf> lista = unificacaoVO.procuraSessaoPautaProcesso(pessoaSecundaria, false);
		if(lista.size() > 0) {
			for (SessaoPautaProcessoTrf objeto : lista) {
				objeto.setUsuarioExclusao(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SESSAO_PAUTA_PROC_EXCLUSORA, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosSessaoInclusora(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<Sessao> lista = unificacaoVO.procuraSessao(pessoaSecundaria, true);
		if(lista.size() > 0) {
			for (Sessao objeto : lista) {
				objeto.setUsuarioInclusao(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SESSAO_INCLUSORA, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosSessaoExclusora(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<Sessao> lista = unificacaoVO.procuraSessao(pessoaSecundaria, false);
		if(lista.size() > 0) {
			for (Sessao objeto : lista) {
				objeto.setUsuarioExclusao(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.SESSAO_EXCLUSORA, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosQuadroAvisos(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<QuadroAviso> lista = unificacaoVO.procuraAvisoQuadroAviso(pessoaSecundaria);
		if(lista.size() > 0) {
			for (QuadroAviso objeto : lista) {
				objeto.setUsuarioInclusao(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.QUADRO_AVISO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosProcessosDocumentosFavoritos(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ProcessoDocumentoFavorito> lista = unificacaoVO.procuraProcessoDocumentoFavorito(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ProcessoDocumentoFavorito objeto : lista) {
				objeto.setUsuario(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.PROCESSO_DOCUMENTO_FAVORITO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosNotasSessaoJulgamento(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<NotaSessaoJulgamento> lista = unificacaoVO.procuraNotasSessaoJulgamento(pessoaSecundaria);
		if(lista.size() > 0) {
			for (NotaSessaoJulgamento objeto : lista) {
				objeto.setUsuarioCadastro(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.NOTAS_SESSAO_JULG, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosModelosProclamacaoJulgamento(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<ModeloProclamacaoJulgamento> lista = unificacaoVO.procuraModelosProclamacaoJulgamento(pessoaSecundaria);
		if(lista.size() > 0) {
			for (ModeloProclamacaoJulgamento objeto : lista) {
				objeto.setUsuario(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.MODELOS_PROCLAMACAO_JULG, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosLogHistoricoMovimentacao(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<LogHistoricoMovimentacao> lista = unificacaoVO.procuraLogsHistoricoMovimentacao(pessoaSecundaria);
		if(lista.size() > 0) {
			for (LogHistoricoMovimentacao objeto : lista) {
				objeto.setUsuario(unificacaoVO.getUsuarioLoginPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.LOG_HIST_MOVIMENTACAO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosVisibilidadeDocumentoIdentificacao(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<VisibilidadePessoaDocumentoIdentificacao> lista = unificacaoVO.procuraVisibilidadesDocumentoIdentificacao(pessoaSecundaria);
		if(lista.size() > 0) {
			for (VisibilidadePessoaDocumentoIdentificacao objeto : lista) {
				objeto.setPessoa(unificacaoVO.getPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.VISIBILIDADE_DOC_IDENTIFICACAO, 
								objeto));
			}
		}
	}
	
	private void gerarObjetosNomesAlternativosCadastrados(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<PessoaNomeAlternativo> lista = unificacaoVO.procuraNomesAlternativosCadastrados(pessoaSecundaria);
		if(lista.size() > 0) {
			for (PessoaNomeAlternativo objeto : lista) {
				objeto.setUsuarioCadastrador(unificacaoVO.getUsuarioPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.NOMES_ALTERNATIVOS_CADASTRADOS, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosMeioContatoProprietarias(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<MeioContato> lista = unificacaoVO.procuraMeiosContatosProprietaria(pessoaSecundaria);
		if(lista.size() > 0) {
			for (MeioContato objeto : lista) {
				objeto.setPessoa(unificacaoVO.getPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.MEIO_CONTATO_PROPRIETARIA, 
								objeto));
			}
		}	
	}
	
	private void gerarObjetosNomesAlternativosProprietarias(UnificacaoVO unificacaoVO, Pessoa pessoaSecundaria) {
		List<PessoaNomeAlternativo> lista = unificacaoVO.procuraNomesAlternativosProprietaria(pessoaSecundaria);
		if(lista.size() > 0) {
			for (PessoaNomeAlternativo objeto : lista) {
				objeto.setPessoa(unificacaoVO.getPessoaPrincipal());
				
				unificacaoPessoaTemp.getUnificacaoPessoasObjetos().add(
						new UnificacaoPessoasObjeto(
								unificacaoVO.getPessoaPrincipal(), 
								pessoaSecundaria,
								unificacaoPessoaTemp,
								TiposObjetosUnificadosEnum.NOMES_ALTERNATIVOS_PROPRIETARIA, 
								objeto));
			}
		}	
	}
	
}