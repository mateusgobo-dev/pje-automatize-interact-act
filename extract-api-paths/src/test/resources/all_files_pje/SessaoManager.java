package br.com.jt.pje.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.dao.SessaoDAO;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.dto.SessaoJulgamentoFiltroDTO;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoSessao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import java.util.ArrayList;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.view.fluxo.ProcessoJudicialAction;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(SessaoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class SessaoManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "sessaoManager";
	
	@In
	private SessaoDAO sessaoDAO;
	
	private Date adicionaDias(Date date, int numDias){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, numDias);
		return calendar.getTime();
	}
	
	private Integer diaSemanaInt(DiaSemana obj){
		if (obj.getDiaSemana().equals("Domingo"))
			return 0;
		if (obj.getDiaSemana().equals("Segunda"))
			return 1;
		if (obj.getDiaSemana().equals("Terça"))
			return 2;
		if (obj.getDiaSemana().equals("Quarta"))
			return 3;
		if (obj.getDiaSemana().equals("Quinta"))
			return 4;
		if (obj.getDiaSemana().equals("Sexta"))
			return 5;
		if (obj.getDiaSemana().equals("Sábado"))
			return 6;
		return null;
	}
	
	public boolean existeSessao(Date date, SalaHorario salaHorario){
		if(date == null || salaHorario == null){
			return false;
		}
		return this.sessaoDAO.existeSessao(date, salaHorario);
	}
	
	@SuppressWarnings("deprecation")
	public SessaoJT persist(OrgaoJulgadorColegiado ojc, Date dataInicial, Date dataFinal, boolean repetir,
							List<SalaHorario> listaSalaHorario, TipoSessao tipoSessao, Usuario usuario){
		if(ojc == null){
			throw new NegocioException("Usuário precisa possuir um Órgão Julgador Colegiado.");
		}
		
		if(listaSalaHorario == null || listaSalaHorario.size() == 0){
			throw new NegocioException("Selecione pelo menos um dos horários disponíveis.");
		}
		
		if(!repetir){
			dataFinal = null;
		}
		
		if (dataFinal == null){
			dataFinal = new Date(dataInicial.getTime());
		}
		dataFinal = adicionaDias(dataFinal, 1);
		
		if (dataInicial.after(dataFinal)){
			throw new NegocioException("A Data Final deve ser maior que a Data Inicial.");
		}
		
		SessaoJT sessaoManaged = null;
		
		int sessoesPersistidas = 0;
		int diaSelecionadoInvalido = 0;
		int salasReservadas = 0;
		while (dataFinal.after(dataInicial)){
			for (SalaHorario salaHorario : listaSalaHorario) {
				if (diaSemanaInt(salaHorario.getDiaSemana()) == dataInicial.getDay()){
					SessaoJT sessao = new SessaoJT();
					sessao.setOrgaoJulgadorColegiado(ojc);
					sessao.setSalaHorario(salaHorario);
					sessao.setTipoSessao(tipoSessao);
					sessao.setDataSessao(new Date(dataInicial.getTime()));
					sessao.setDataSituacaoSessao(new Date());
					sessao.setSituacaoSessao(SituacaoSessaoEnum.A);
					sessao.setUsuarioSituacaoSessao(usuario);
					
					if (ojc != null && ojc.getFechamentoAutomatico()){
						Calendar dataFechamento = Calendar.getInstance();
						dataFechamento.setTime(dataInicial);
						dataFechamento.add(Calendar.DAY_OF_MONTH, - (ojc.getDiaCienciaInclusaoPauta() + ojc.getPrazoDisponibilizaJulgamento()));
						sessao.setDataFechamentoPauta(dataFechamento.getTime());
					}
					
					if (!existeSessao(dataInicial, salaHorario)){
						persist(sessao);
						sessaoManaged = sessao;
						sessoesPersistidas++;
					}else{
						salasReservadas++;
					}
				}else{
					diaSelecionadoInvalido++;
				}
				salaHorario.setSelecionado(false);
				
			}
			dataInicial.setDate(dataInicial.getDate() + 1);
		}
		verificarMensagens(sessoesPersistidas, diaSelecionadoInvalido, salasReservadas);
		return sessaoManaged;
	}
	
	private void verificarMensagens(int sessoesPersistidas, int diaSelecionadoInvalido, int salasReservadas){
		if(sessoesPersistidas == 0){
			if(diaSelecionadoInvalido > 0 && salasReservadas == 0){
				throw new NegocioException("O dia selecionado não é igual ao dia da data informada");
			}else{
				throw new NegocioException("A sala já esta reservada para o dia solicitado");
			}
		}
	}
	
	public List<SessaoJT> getSessoesComDataFechamentoPautaDiaCorrente(){
		return this.sessaoDAO.getSessoesComDataFechamentoPautaDiaCorrente();
	}
	
	public void atualizarSituacaoSessao(SessaoJT sessao, SituacaoSessaoEnum situacaoSessaoEnum, Usuario usuario){
		sessao.setSituacaoSessao(situacaoSessaoEnum);
		sessao.setDataSituacaoSessao(new Date());
		sessao.setUsuarioSituacaoSessao(usuario);
		
		update(sessao);
	}
	
	public boolean existemVariasSessoes(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		return this.sessaoDAO.existemVariasSessoes(orgaoJulgador, orgaoJulgadorColegiado);
	}
	
	public int getIdSessaoDoDia(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		return this.sessaoDAO.getIdSessaoDoDia(orgaoJulgadorColegiado);
	}
	
	/**
	 * [PJEII-4329]
	 * @param OJC orgão julgador colegiado
	 * @param OJ orgão julgador
	 * @return Uma lista das sessões que estão em andamento a partir de um orgão julgador e um orgão julgador colegiado
	 */
	public List<Sessao> getSessoesJulgamento(OrgaoJulgadorColegiado ojc, OrgaoJulgador oj){
        Date dataMinimaSessao = DateUtil.getDataAtual();
        return this.sessaoDAO.getSessoesJulgamento(ojc, oj, dataMinimaSessao);
    }
    
    /**
	 * [PJEII-4329] - Faz o mesmo que o metodo getSessoesJulgamento(), porem
     * considera os dias de fechamento da Pauta configurado no Orgao Colegiado
     * 
	 * @param OJC orgão julgador colegiado
	 * @param OJ orgão julgador
	 * @return Uma lista das sessões que estão em andamento a partir de um orgão julgador e um orgão julgador colegiado
     */
	public List<Sessao> getSessoesJulgamentoComDiasFechamentoPautaOrgaoColegiado(OrgaoJulgadorColegiado ojc, OrgaoJulgador oj){
        // adiciona a data minima da sessao os dias para fechamento da pauta configurado no Orgao Colegiado
        Date dataMinimaSessao = DateUtil.getDataAtual();
        Integer diasPrazoTermino = (ojc.getPrazoTermino() != null ? ojc.getPrazoTermino() : 0);
        dataMinimaSessao = DateUtil.dataMaisDias(dataMinimaSessao, diasPrazoTermino);

        return this.sessaoDAO.getSessoesJulgamento(ojc, oj, dataMinimaSessao);
    }
    
    /**
     * Criada na solicitação [PJEII-4330]
     * 
     * @param sessao A sessão de julgamento
     * @return Lista de Orgaos Julgadores da Sessao selecionada
     */
    public List<OrgaoJulgador> getOrgaosJulgadoresDaSessao(Sessao sessao) {
        return this.sessaoDAO.getOrgaosJulgadoresDaSessao(sessao);
    }        
    
    /**
     * Retorna todas as sessões de julgamento de determinado ano ordenadas pelo apelido da sessão.
     * 
     * @param ano Representa o ano das sessões de julgamento a ser pesquisada.
     * @return Todas as sessões de julgamento de determinado ano ordenadas pelo apelido da sessão.
     */
    public List<Sessao> getSessoesJulgamento(Integer ano){
    	return this.sessaoDAO.getSessoesJulgamento(ano);
    }
    
	/**
	 * Recupera a data da última sessão de julgamento do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return A data da ultima sessão de julgamento do processo.
	 */
    public Sessao getUltimaSessaoProcesso(Integer idProcessoTrf){
    	return this.sessaoDAO.getUltimaSessaoProcesso(idProcessoTrf);
    }
    
	/**
	 * Recupera as sessões de julgamento de um processo.
	 * 
	 * @return Lista de {@link Sessao}.
	 */
    public List<Sessao> getSessoesProcesso(Integer idProcessoTrf){
    	return this.sessaoDAO.getSessoesProcesso(idProcessoTrf);
    }
    
    /**
     * Recupera uma sessão pelo seu id
     * @param id O id da sessão
     * @return 
     */
	public Sessao recuperarPorId(Integer id) {
		return this.sessaoDAO.recuperarPorId(id); 
	}    
	
	public Fluxo getFluxoPedidoVista(){
		String codigoFluxo = ComponentUtil.getParametroDAO().valueOf(Variaveis.VARIAVEL_FLUXO_PEDIDO_VISTA);
        Fluxo fluxo = null;
        if (codigoFluxo != null && !codigoFluxo.trim().isEmpty()) {
            fluxo = ComponentUtil.getFluxoDAO().findByCodigo(codigoFluxo);
        }
        return fluxo;
    }
	
	/**
	 * Método responsável por verificar se a pauta está aberta
	 * 
	 * @param sessao
	 *            na qual se deseja verificar a pauta
	 *            
	 * @return <code>Boolean</code>, <code>true</code> caso a pauta esteja ainda
	 *         em elaboração, aberta.
	 */
	private boolean isPautaAberta(Sessao sessao) {
		return (sessao.getDataFechamentoPauta() == null) || DateUtil.isDataComHoraMenorIgual(new Date(), sessao.getDataFechamentoPauta());
	}
	
	/**
	 * Método responsável por verificar se a pauta da sessão está fechada
	 * 
	 * @return code>Boolean</code>, <code>true</code> caso a pauta da sessão
	 *         esteja fechada.
	 */
	public boolean isPautaFechada(Sessao sessao) {
		return (sessao.getDataFechamentoPauta() != null) && DateUtil.isDataComHoraMaiorIgual(new Date(), sessao.getDataFechamentoPauta());
	}
	
	/**
	 * Método responsável por verificar se é permitido alterar a ordem da sessão
	 * 
	 * @param sessao
	 *            na qual se necessita verificar a permissão de alteração
	 * 
	 * @return <code>Boolean</code>, <code>true</code> caso for permitido
	 *         alterar a ordem
	 */
	public boolean podeAlterarOrdenacaoPautaSessao(Sessao sessao) {
		return (sessao.getDataRealizacaoSessao() == null && (Identity.instance().hasRole(Papeis.PERMITE_ORDENAR_PAUTA_SESSAO) || isPautaAberta(sessao)));
	}

		
	 /**
     * Recupera a próxima sessão do processo informado
     * @param processoTrf
     * @return Sessao
	 * @throws NoSuchFieldException 
     */
	public Sessao getProximaSessaoProcesso(ProcessoTrf processoTrf) throws NoSuchFieldException{
		return this.sessaoDAO.getProximaSessaoProcesso(processoTrf);
	}

	/**
	 * Metodo recupera as datas das sessoes nao realizadas
	 */
    public List<Sessao> getSessoesNaoFinalizadas(Date dataReferencia) {
		return this.sessaoDAO.getSessoesNaoFinalizadas(dataReferencia);
	}

    /**
     * Recupera as sessoes nao finalizadas. 
     * 
     * @return
     */
    public List<Date> getDatasSessoesNaoFinalizadas(){
		return this.sessaoDAO.getDatasSessoesNaoFinalizadas();
	}

	/**
	 * metodo responsavel por recuperar todas as sessoes incluidas pela pessoa passada em parametro.
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<Sessao> recuperarSessaoPessoaInclusora(Pessoa pessoaInclusora) {
		return this.sessaoDAO.recuperarSessaoPessoa(pessoaInclusora, true);
	}

	public List<Sessao> recuperarSessaoPessoaExclusora(Pessoa pessoaExclusora) {
		return this.sessaoDAO.recuperarSessaoPessoa(pessoaExclusora, false);
	}
    
    public Sessao recuperarSessao(Sessao sessao) {
		Sessao retorno = sessao;
		if(sessao == null) {
			retorno = SessaoHome.instance().getInstance();
		}
		return retorno;
	}
    
    public Sessao recuperarUltimaSessao(Sessao sessao, ProcessoTrf processo) {
		Sessao retorno = recuperarSessao(sessao);
		if(retorno == null || retorno.getApelido() == null) {
			SessaoPautaProcessoTrf sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperaUltimaPautaProcessoNaoExcluido(processo);
			if(sessaoPauta != null) {
				retorno = sessaoPauta.getSessao();
			}
		}
		return retorno;
	}

    public String getDataHoraSessao(Sessao sessao){
        if (sessao != null){
            return montaDataSessaoStr(sessao);
        }
        else{
            return "N<E3>o existe sess<E3>o";
        }
    }   
	
    @SuppressWarnings("deprecation")
    private String montaDataSessaoStr(Sessao sessao){
        String ret = "";
        if (sessao.getDataSessao() != null && sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial() != null
            && sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial() != null){
            Calendar c = new GregorianCalendar();
            c.setTime(sessao.getDataSessao());
            c.set(Calendar.HOUR_OF_DAY, sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial().getHours());
            c.set(Calendar.MINUTE, sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial().getMinutes());
            ret = DateUtil.getDataFormatada(c.getTime(), "dd/MM/yyyy HH:mm");
        }
        return ret;
    }
    
    public List<Sessao> getSessoesJulgamentoFuturas(OrgaoJulgadorColegiado ojc){
        Date dataMinimaSessao = DateUtil.getDataAtual();
        Integer diasPrazoTermino = (ojc.getPrazoTermino() != null ? ojc.getPrazoTermino() : 0);
        dataMinimaSessao = DateUtil.dataMaisDias(dataMinimaSessao, diasPrazoTermino);
        return this.sessaoDAO.getSessoesJulgamento(ojc, dataMinimaSessao);
    }
    
    public List<ProcessoTrf> recuperarProcessosAcordaoNaoAssinado(Integer idSessao) {
    	return this.sessaoDAO.recuperarProcessosJulgadoNaoAssinado(idSessao, ParametroUtil.instance().getTipoProcessoDocumentoAcordao());
    }
    
    public List<ProcessoTrf> recuperarProcessosCertidaoJulgamentoNaoAssinado(Integer idSessao) {
    	return this.sessaoDAO.recuperarProcessosJulgadoNaoAssinado(idSessao, ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() != null ? 
        	ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() : ParametroUtil.instance().getTipoProcessoDocumentoCertidao());
    }
    
    public List<ProcessoTrf> recuperarProcessosSemMovimentacaoJulgamento(Integer idSessao, Date dataLimite) {
    	return this.sessaoDAO.recuperarProcessosSemMovimentacaoJulgamento(idSessao, dataLimite);
    }
    
    public String recuperarProcessosPautados(Sessao sessao) {
		return recuperarProcessosPautados(sessao, true);
	}
	
	
	public String recuperarProcessosPautados(Sessao sessao, boolean incluirProcessos) {
		String retorno = ""; 
		sessao = this.recuperarSessao(sessao);
		if(sessao != null && sessao.getIdSessao() > 0) {
			try {
				List <SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarSessaoPautaProcessosTrf(sessao.getIdSessao(), new SessaoJulgamentoFiltroDTO());
				StringBuilder sb = new StringBuilder();
				for(SessaoPautaProcessoTrf processoPautado: processosPautados) {
					ProcessoTrf processo = processoPautado.getProcessoTrf();
					if(incluirProcessos) {
						sb.append(processo.getClasseJudicialStr());
						sb.append(" N ");
						sb.append(processo.getNumeroProcesso());
						sb.append("<br/>");
						if(processo.getComplementoJE() != null) {
							sb.append("PROCEDENCIA: ");
							sb.append(processo.getComplementoJEUfMunicipioExtenso());
							sb.append("<br/>");
						}
						sb.append("RELATOR(A): ");
						sb.append(processo.getOrgaoJulgador().getOrgaoJulgador());
						sb.append("<br/>");
						ProcessoHome.instance().setInstance(processo.getProcesso());
						ProcessoTrfHome.instance().setProcessoTrf(processo);
						ProcessoTrfHome.instance().setInstance(processo);
						ProcessoJudicialAction.instance().setProcessoJudicial(processo);
						sb.append(ProcessoJudicialAction.instance().recuperarParteFormatada(false, true, ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P));
						
						sb.append("OBJETO: " );
						if(processo.getObjeto() != null) {
							sb.append(processo.getObjeto());
						}
						ProcessoBloco processoBloco = ComponentUtil.getProcessoBlocoManager().recuperarProcessoBloco(sessao, processo);
						if(processoBloco != null) {
							sb.append("<br/>");
							sb.append("Bloco: " );
							sb.append(processoBloco.getBloco().getBlocoJulgamento());
						}
						sb.append("<br/>");
					}
					sb.append("<br/><br/>");
				}
				retorno = sb.toString();
			} catch (Exception e) {
				retorno = "Nao foi possivel recuperar os processos da sessao: " + e.getLocalizedMessage() + " - " + e.getMessage();
				e.printStackTrace();
			}
		}
		return retorno;
    }
    
    public List<String> listarEventosDeliberacaoSessao() {
		List<String> retorno = new ArrayList<>();
		retorno.add(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_MERITO);
		retorno.add(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_LIMINAR);
		retorno.add(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_QUESTAO_ORDEM);
		retorno.add(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_ADIADO);
		retorno.add(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_PEDIDO_VISTA);
		retorno.add(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA);
		return retorno;
	}
    
	public boolean isEventosDeliberacaoSessaoConfigurados() {
		boolean retorno = true;

		for (String codEvento : listarEventosDeliberacaoSessao()) {
			try {
				ComponentUtil.getComponent(EventoManager.class).findByCodigoCNJ(codEvento);
			} catch (Exception e) {
				retorno = false;
				break;
			}
		}

		return retorno;
	}
    
    public String recuperarProcessosProclamados(Sessao sessao) {
		String retorno = ""; 
		sessao = this.recuperarSessao(sessao);
		if(sessao != null && sessao.getIdSessao() > 0) {
			try {
				List <SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarSessaoPautaProcessosTrf(sessao.getIdSessao(), new SessaoJulgamentoFiltroDTO());
				StringBuilder sb = new StringBuilder();
				for(SessaoPautaProcessoTrf processoPautado: processosPautados) {
					ProcessoTrf processo = processoPautado.getProcessoTrf();
					sb.append(processo.getClasseJudicialStr());
					sb.append(" N ");
					sb.append(processo.getNumeroProcesso());
					sb.append("<br/>");
					if(processo.getComplementoJE() != null) {
						sb.append("PROCEDÊNCIA: ");
						sb.append(processo.getComplementoJEUfMunicipioExtenso());
						sb.append("<br/>");
					}
					sb.append("RELATOR: ");
					sb.append(processo.getOrgaoJulgador().getOrgaoJulgador());
					sb.append("<br/>");
					ProcessoHome.instance().setInstance(processo.getProcesso());
					ProcessoTrfHome.instance().setProcessoTrf(processo);
					ProcessoTrfHome.instance().setInstance(processo);
					if(processoPautado.getProclamacaoDecisao() != null) {
						sb.append("Decisão: " );
						sb.append(processoPautado.getProclamacaoDecisao());
					} else {
						if(processoPautado.getAdiadoVista() != null) {
							if(processoPautado.getAdiadoVista().equals(AdiadoVistaEnum.AD) && processoPautado.getRetiradaJulgamento() != null
									&& processoPautado.getRetiradaJulgamento()) {
								sb.append( "Retirado de julgamento" );
							} else if(processoPautado.getAdiadoVista().equals(AdiadoVistaEnum.AD)) {
								sb.append( "Processo adiado" );
							} else {
								sb.append( "Pedido de vista registrado por " + processoPautado.getOrgaoJulgadorPedidoVista().getOrgaoJulgador());
							}
						} else {
							sb.append("Processo retirado de pauta" );
						}
					}
					sb.append("<br/><br/>");
				}
				retorno = sb.toString();
			} catch (Exception e) {
				retorno = "No foi possvel recuperar os processos da sesso: " + e.getLocalizedMessage() + " - " + e.getMessage(); 
			}
		}
		return retorno;
	}

	public String getSessaoObservacao(Integer idProcessoTrf){
		return this.sessaoDAO.getSessaoObservacao(idProcessoTrf);
	}
}
