package br.jus.cnj.pje.nucleo.manager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteCaixaAdvogadoProcuradorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.MotivoMovimentacaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.NAME)
public class ProcessoParteExpedienteCaixaAdvogadoProcuradorManager extends BaseManager<ProcessoParteExpedienteCaixaAdvogadoProcurador>{
	public static final String NAME = "processoParteExpedienteCaixaAdvogadoProcuradorManager";
	
	@In
	private ProcessoParteExpedienteCaixaAdvogadoProcuradorDAO processoParteExpedienteCaixaAdvogadoProcuradorDAO;
	
	@In
	private  ProcessoCaixaAdvogadoProcuradorManager processoCaixaAdvogadoProcuradorManager;
	
	@In
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager;
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;
	
	@In
	private AtoComunicacaoService atoComunicacaoService;
	
	@In
	private JurisdicaoManager jurisdicaoManager;
	
	@Override
	protected BaseDAO<ProcessoParteExpedienteCaixaAdvogadoProcurador> getDAO() {
		return processoParteExpedienteCaixaAdvogadoProcuradorDAO;
	}
	
	public static ProcessoParteExpedienteCaixaAdvogadoProcuradorManager instance() {
		return (ProcessoParteExpedienteCaixaAdvogadoProcuradorManager)Component.getInstance(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.NAME);
	}

	private void incluirEmCaixa(ProcessoParteExpediente ppe,
			CaixaAdvogadoProcurador caixaInclusao,
			CaixaAdvogadoProcurador caixaExclusao) throws PJeBusinessException {
				
		if (!caixaInclusao.getJurisdicao().equals(ppe.getProcessoJudicial().getJurisdicao())) {			
			throw new PJeBusinessException("Não é possível incluir o expediente em uma caixa de jurisdição diversa da sua.");
		}
		
		if (!caixaContem(caixaInclusao, ppe)) {
			ProcessoParteExpedienteCaixaAdvogadoProcurador tag = new ProcessoParteExpedienteCaixaAdvogadoProcurador();
			tag.setProcessoParteExpediente(ppe);
			tag.setCaixaAdvogadoProcurador(caixaInclusao);
			persistAndFlush(tag);

			if (caixaExclusao != null) {
			 	remover(caixaExclusao, false, ppe);
			}		
		}		
	}
		
	public void incluirEmCaixa(CaixaAdvogadoProcurador caixaInclusao,
			CaixaAdvogadoProcurador caixaExclusao, ProcessoParteExpediente... ppes)
			throws PJeBusinessException {
				
		for (ProcessoParteExpediente ppe : ppes) {			
			try {				
				incluirEmCaixa(ppe, caixaInclusao, caixaExclusao);
			} catch (PJeBusinessException e) {				
				String msg = String.format("Falha ao movimentar processo %s para \"%s\": %s", 
						ppe.getProcessoJudicial().getNumeroProcesso(),
						caixaInclusao.getNomeCaixaAdvogadoProcurador(), 
						e.getCode());
				throw new PJeBusinessException(msg);
			}
		}
		
		// definindo as operações para histórico de movimentações
		if (caixaExclusao != null) {
			// trata-se de redistribiução
			Events.instance().raiseEvent(
					Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA_EXPEDIENTE,
					Arrays.asList(ppes), Calendar.getInstance(),
					caixaInclusao, MotivoMovimentacaoEnum.I,
					Authenticator.getUsuarioLogado());
			
		} else {
			// trata-se de distribuição
			Events.instance().raiseEvent(
					Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA_EXPEDIENTE,
					Arrays.asList(ppes), Calendar.getInstance(),
					caixaInclusao, MotivoMovimentacaoEnum.D,
					Authenticator.getUsuarioLogado());
			
		}

	}
	
	/**
	 * Remove todos os processos indicados da caixa informada.
	 * 
	 * @param caixa
	 *            a caixa da qual os processos devem ser removidos.
	 * 
	 * @param registrarMovimentacao
	 *            Indicador para gravar registro de movimentação de devlução do
	 *            processo para a jurisdição
	 * 
	 * @param processos
	 *            os processos a serem removidos
	 * @throws PJeBusinessException
	 */
	
	public void remover(CaixaAdvogadoProcurador caixa,
			boolean registrarMovimentacao, ProcessoParteExpediente... expedientes)
			throws PJeBusinessException {
		remover(caixa,registrarMovimentacao,true,expedientes);
	}
	
	public void remover(CaixaAdvogadoProcurador caixa,
			boolean registrarMovimentacao, Boolean validaJurisdicao, ProcessoParteExpediente... expedientes)
			throws PJeBusinessException {
		
		List<ProcessoParteExpediente> ppel = new ArrayList<ProcessoParteExpediente>();
		for (ProcessoParteExpediente ppe : expedientes) {
			ppel.add(ppe);
		}

		this.processoParteExpedienteCaixaAdvogadoProcuradorDAO.remover(caixa, expedientes);
			
		if (registrarMovimentacao) {		
			Events.instance().raiseEvent(
					Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA_EXPEDIENTE, ppel,
					Calendar.getInstance(), caixa,
					MotivoMovimentacaoEnum.A, Authenticator.getUsuarioLogado());
		}
		
	}

	public boolean caixaContem(CaixaAdvogadoProcurador cx, ProcessoParteExpediente ppe) throws PJeBusinessException {
		Search s = new Search(ProcessoParteExpedienteCaixaAdvogadoProcurador.class);
		addCriteria(s, Criteria.equals("caixaAdvogadoProcurador", cx));
		addCriteria(s, Criteria.equals("processoParteExpediente", ppe));
		return count(s) > 0;
	}
	
	/**
	 * Verifica se existe alguma caixa com determinado expediente
	 * @param ppe
	 * @return
	 */
	public boolean contemExpedienteEmCaixa(ProcessoParteExpediente ppe){
		Search s = new Search(ProcessoParteExpedienteCaixaAdvogadoProcurador.class);
		addCriteria(s, Criteria.equals("processoParteExpediente", ppe));
		return count(s) > 0;
	}
	
	public List<ProcessoParteExpedienteCaixaAdvogadoProcurador> listExpedienteEmCaixa(ProcessoParteExpediente ppe){
		Search s = new Search(ProcessoParteExpedienteCaixaAdvogadoProcurador.class);
		addCriteria(s, Criteria.equals("processoParteExpediente", ppe));
		return list(s);
	}
	
	@SuppressWarnings("unchecked")
	public void somarContadorCaixa(CaixaAdvogadoProcurador caixa, int qtdProcessos, boolean naoSigiloso){
		Map<Integer,BigInteger> contadores = (Map<Integer,BigInteger>)Contexts.getApplicationContext().get("contadorExpedientesCaixas");
		somarContadorCaixa(caixa,qtdProcessos,contadores);
		if(naoSigiloso){
			Map<Integer,BigInteger> contadoresNaoSigilosos = (Map<Integer,BigInteger>)Contexts.getApplicationContext().get("contadorExpedientesCaixasNaoSigilosos");
			somarContadorCaixa(caixa,qtdProcessos,contadoresNaoSigilosos);
		}
	}
	
	private void somarContadorCaixa(CaixaAdvogadoProcurador caixa,int qtdProcessos,Map<Integer,BigInteger> contadores){
		BigInteger bi = BigInteger.valueOf(qtdProcessos);
		if(contadores.get(caixa.getIdCaixaAdvogadoProcurador())!= null){
			
			contadores.put(caixa.getIdCaixaAdvogadoProcurador().intValue(), contadores.get(caixa.getIdCaixaAdvogadoProcurador()).add(bi));
		}
		else{
			contadores.put(caixa.getIdCaixaAdvogadoProcurador().intValue(), bi);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void subtrairContadorCaixa(CaixaAdvogadoProcurador caixa, int qtdProcessos,boolean naoSigiloso){
		Map<Integer,BigInteger> contadores = (Map<Integer,BigInteger>)Contexts.getApplicationContext().get("contadorExpedientesCaixas");
		subtrairContadorCaixa(caixa,qtdProcessos,contadores);
		if(naoSigiloso){
			Map<Integer,BigInteger> contadoresNaoSigilosos = (Map<Integer,BigInteger>)Contexts.getApplicationContext().get("contadorExpedientesCaixasNaoSigilosos");
			subtrairContadorCaixa(caixa,qtdProcessos,contadoresNaoSigilosos);
		}
	}
	
	private void subtrairContadorCaixa(CaixaAdvogadoProcurador caixa,int qtdProcessos,Map<Integer,BigInteger> contadores){
		 BigInteger count = contadores.get(caixa.getIdCaixaAdvogadoProcurador());
		if(count != null && count.intValue() > 0){
			BigInteger bi = BigInteger.valueOf(qtdProcessos);
			contadores.put(caixa.getIdCaixaAdvogadoProcurador().intValue(), count.subtract(bi));
		}
	}
	
	/**
	 * Verifica se o processo originário está em alguma caixa,
	 * caso esteja também inclui o expediente. 
	 * @param expedientes
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("java:S3776")
	public void verificarVinculoCaixa(List<ProcessoExpediente> expedientes) throws PJeBusinessException{
		Set<Pessoa> representantes;
		for(ProcessoExpediente exp: expedientes){
			for(ProcessoParteExpediente procExp: exp.getProcessoParteExpedienteList()){
				if(ComponentUtil.getProcessoParteManager().findProcessoParte(procExp.getProcessoJudicial(), procExp.getPessoaParte(), true) == null) {
                    ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class).distribuirUtilizandoFiltro(procExp.getProcessoJudicial());
                }
				// Verifica se é Procurador/Defensor
				if(procExp.getProcuradoria() != null){
					List<CaixaAdvogadoProcurador> listCaixas = caixaAdvogadoProcuradorManager.list(exp.getProcessoTrf().getJurisdicao(), procExp.getProcuradoria().getLocalizacao());
					for(CaixaAdvogadoProcurador cxAdvProc: listCaixas){
						if(caixaAdvogadoProcuradorManager.isCaixaAtiva(cxAdvProc.getIdCaixaAdvogadoProcurador()) && processoCaixaAdvogadoProcuradorManager.caixaContem(cxAdvProc, exp.getProcessoTrf())){
							incluirEmCaixa(procExp,cxAdvProc, null);
						}
					}
				} else {
					// O expediente é enviado ao representante. Buscar caixas desses.
					representantes = new HashSet<Pessoa>();
					for (ProcessoParte processoParte: exp.getProcessoTrf().getProcessoParteList()) {
						if (processoParte.getPessoa() == procExp.getPessoaParte()) {
							if (processoParte.getProcessoParteRepresentanteList().isEmpty()) {
								representantes.add(procExp.getPessoaParte());
							} else {
								for (ProcessoParteRepresentante ppr : processoParte.getProcessoParteRepresentanteList()) {
									representantes.add(ppr.getParteRepresentante().getPessoa());
								}
							}
						}
					}
					for (Pessoa representante: representantes) {
						UsuarioLogin usuLogin = usuarioManager.findById(representante.getIdPessoa());
						UsuarioLocalizacao usuLocJus = usuarioLocalizacaoManager.getLocalizacoesAtuais(representante, ParametroUtil.instance().getPapelJusPostulandi()).size() > 0 ? usuarioLocalizacaoManager.getLocalizacoesAtuais(representante, ParametroUtil.instance().getPapelJusPostulandi()).get(0) : null;
						UsuarioLocalizacao usuLocAdv = usuarioLocalizacaoManager.getLocalizacoesAtuais(representante, ParametroUtil.instance().getPapelAdvogado()).size() > 0 ? usuarioLocalizacaoManager.getLocalizacoesAtuais(representante, ParametroUtil.instance().getPapelAdvogado()).get(0) : null;

						// Caso não seja Procurador/Defensor se é advogado ou JusPostulandi e podem receber expediente via sistema
						Boolean primeiro = (Pessoa.instanceOf(usuLogin, PessoaAdvogado.class));
						Boolean segundo = atoComunicacaoService.recuperarMeiosComunicacao(representante, exp.getProcessoTrf(), (exp.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.S ? true : false), procExp.getProcuradoria()).contains(ExpedicaoExpedienteEnum.E); 
						if(primeiro && segundo){
							for(CaixaAdvogadoProcurador cxAdvProc :caixaAdvogadoProcuradorManager.list(exp.getProcessoTrf().getJurisdicao(),usuLocAdv.getLocalizacaoFisica())){
								if(caixaAdvogadoProcuradorManager.isCaixaAtiva(cxAdvProc.getIdCaixaAdvogadoProcurador()) && processoCaixaAdvogadoProcuradorManager.caixaContem(cxAdvProc, exp.getProcessoTrf())){
									incluirEmCaixa(procExp,cxAdvProc, null);
								}
							}
						}
						if((usuLocJus != null && atoComunicacaoService.recuperarMeiosComunicacao(representante, exp.getProcessoTrf(), (exp.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.S ? true : false), procExp.getProcuradoria()).contains(ExpedicaoExpedienteEnum.E))){
							for(CaixaAdvogadoProcurador cxAdvProc :caixaAdvogadoProcuradorManager.list(exp.getProcessoTrf().getJurisdicao(),usuLocJus.getLocalizacaoFisica())){
								if(caixaAdvogadoProcuradorManager.isCaixaAtiva(cxAdvProc.getIdCaixaAdvogadoProcurador()) && processoCaixaAdvogadoProcuradorManager.caixaContem(cxAdvProc, exp.getProcessoTrf())){
									incluirEmCaixa(procExp,cxAdvProc, null);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param pessoa Pessoa
	 * @return ID da pessoa ou null.
	 */
	protected Integer getIdPessoa(Pessoa pessoa) {
		return (pessoa != null ? pessoa.getIdPessoa() : null);
	}
}
