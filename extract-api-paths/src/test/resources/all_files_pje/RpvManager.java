package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.RpvDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.BaseCalculoIr;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Rpv;
import br.jus.pje.nucleo.entidades.RpvParteDeducao;
import br.jus.pje.nucleo.entidades.RpvParteRepresentante;
import br.jus.pje.nucleo.entidades.RpvParteValorCompensar;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.EspecieRequisicaoEnum;
import br.jus.pje.nucleo.enums.NaturezaCreditoEnum;
import br.jus.pje.nucleo.enums.RpvPessoaParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.RpvPrecatorioEnum;
import br.jus.pje.nucleo.enums.RpvTipoCessaoEnum;
import br.jus.pje.nucleo.enums.RpvTipoFormaHonorarioEnum;
import br.jus.pje.nucleo.enums.RpvTipoRestricaoPagamentoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(RpvManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class RpvManager extends GenericManager implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "rpvManager";

	@In
	private RpvDAO rpvDAO;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private BaseCalculoIrManager baseCalculoIrManager;	
	
	public Rpv criaNovaInstanciaRpv(ProcessoTrf processoTrf ) {
		Rpv rpv = new Rpv();
		rpv.setInRpvPrecatorio(RpvPrecatorioEnum.R);
		rpv.setInOposicaoEmbargos(false);
		rpv.setInDesapropriacao(false);
		rpv.setInDesapropriacaoUnicoImovel(false);
		rpv.setInValorCompensar(false);
		rpv.setInNaturezaCredito(NaturezaCreditoEnum.A);
		rpv.setInEspecieRequisicao(EspecieRequisicaoEnum.O);
		rpv.setInReembolsoHonorariosSecao(false);
		rpv.setInPagamentoDiretoPerito(false);
		rpv.setInCreditoSomenteAdvogado(false);
		rpv.setInRessarcimentoCustas(false);
		rpv.setInMultaAstreintes(false);
		rpv.setInCessionario(false);
		rpv.setTipoCessao(RpvTipoCessaoEnum.P);
		rpv.setInTipoFormaHonorario(RpvTipoFormaHonorarioEnum.R);
		rpv.setInTipoRestricaoPagamento(RpvTipoRestricaoPagamentoEnum.SR);
		rpv.setProcessoTrf(processoTrf);
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvEmElaboracao());
		return rpv;
	}
	
	public void persistRpv(Rpv rpv) throws RpvException{
		if(rpv.getInMultaAstreintes() && !isAssuntoAstreintes(rpv.getProcessoTrf())){
			AssuntoTrf assuntoMultaAstreintes = ParametroUtil.instance().getAssuntoMultaAstreintes();
			rpv.setAssuntoPrincipal(assuntoMultaAstreintes);
		}
		if(validarRpv(rpv)){
			rpvDAO.persist(rpv);
		}
	}
	
	public void updateRpv(Rpv rpv) throws RpvException{
		if(rpv.getInMultaAstreintes() && !isAssuntoAstreintes(rpv.getProcessoTrf())){
			AssuntoTrf assuntoMultaAstreintes = ParametroUtil.instance().getAssuntoMultaAstreintes();
			rpv.setAssuntoPrincipal(assuntoMultaAstreintes);
		}
		if(validarRpv(rpv)){
			
			rpvDAO.update(rpv);
			
			if(!rpv.getInCessionario()){
				List<RpvPessoaParte> listCessionarioByRpv = listCessionarioByRpv(rpv);
				if(listCessionarioByRpv.size() > 0){
					for (RpvPessoaParte rpvPessoaParte : listCessionarioByRpv) {
						rpv.getRpvParteList().remove(rpvPessoaParte);
						removeCessionario(rpvPessoaParte);
					}
				}			
			}
			
			if(!rpv.getInPagamentoDiretoPerito()){
				TipoParte tipoPartePerito = ParametroUtil.instance().getTipoPartePerito();
				List<RpvPessoaParte> peritosRemovidos = new ArrayList<RpvPessoaParte>(0);
				for (RpvPessoaParte perito : rpv.getListaParteTerceiro()) {
					if(perito.getTipoParte().equals(tipoPartePerito)){
						remove(perito);
						peritosRemovidos.add(perito);
					}
				}
				rpv.getRpvParteList().removeAll(peritosRemovidos);
			}
		}
	}
	
	private boolean validarRpv(Rpv rpv) throws RpvException{
		if(rpv == null){
			throw new RpvException("Rpv está vazia!");
		} 
		
		if(rpv.getAssuntoPrincipal() == null){
			throw new RpvException("É obrigatório selecionar o assunto principal.");
		}
		
		if(rpv.getBeneficiario() == null){
			throw new RpvException("É obrigatório selecionar um beneficiário.");
		}

		if(rpv.getAutorCabecaAcao() == null){
			throw new RpvException("O Autor Cabeça é obrigatório.");
		}
		
		Date dataDistribuicao = toOnlyDate(rpv.getProcessoTrf().getDataDistribuicao());		
		//Validações de tipo de requisição O("Originária"), C("Complementar"), S("Suplementar")
		if(!rpv.getInEspecieRequisicao().equals(EspecieRequisicaoEnum.O)){
			Date dataExecucao = rpv.getDataExecucao();
			if(dataExecucao != null){
				dataExecucao = toOnlyDate(dataExecucao);
				if(!dataExecucao.after(dataDistribuicao)){
					throw new RpvException("A Data de Execução deve ser maior que à Data de Ajuizamento do Processo.");
				}			
			}
			
			if(rpv.getInEspecieRequisicao().equals(EspecieRequisicaoEnum.C)){
				List<Rpv> rpvOriginariaByProcessoTrf = 
					rpvDAO.listRpvOriginariaByRpv(rpv);
				if(rpvOriginariaByProcessoTrf.size() == 0){
					throw new RpvException("Para requisiçães Complementares é obrigatório" +
										   " à existencia de uma Requisição Originária não" +
						 				   " Canelada ou Rejeitada no processo.");					
				}
			} else if (rpv.getInEspecieRequisicao().equals(EspecieRequisicaoEnum.S)){
				List<Rpv> rpvParcialByProcessoTrf = 
					rpvDAO.listRpvParcialByRpv(rpv);
				if(rpvParcialByProcessoTrf.size() == 0){
					throw new RpvException("Para requisiçães Suplementares é obrigatório" +
										   " à existencia de uma Requisição Originária não" +
										   " Canelada ou Rejeitada no processo.");					
				}				
			}
		} else {
			if(rpvDAO.listRpvOriginariaByRpv(rpv).size() > 0){
				throw new RpvException("Já existe um requisitório originário para este beneficiário.");
			}
			
			if(rpv.getInEspecieRequisicao().equals(EspecieRequisicaoEnum.O) && rpv.getDataExecucao() != null
				&& rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.R)){
				rpv.setDataExecucao(null);
			}
			
		}
		
		if(!validaDataBaseCalculo(rpv.getDataBaseCalculo(), dataDistribuicao)){
			throw new RpvException("A Data Base de Cálculo deve ser maior ou igual à Data de Ajuizamento do Processo.");			
		}		
		
		validaDataTransitoJulgado(rpv);

		return true;
	}
	
	/**
	 * Valida de a Data Base de Calculo é maior ou igual a data de ajuizamento
	 * @param dataBaseCalculo
	 * @param dataAjuizamento
	 * @return Se sim retorna true se não retorna false.   
	 */
	private boolean validaDataBaseCalculo(Date dataBaseCalculo, Date dataAjuizamento){
		dataBaseCalculo = toOnlyDate(dataBaseCalculo);
        dataAjuizamento = toOnlyDate(dataAjuizamento);
		return dataBaseCalculo.after(dataAjuizamento) || dataBaseCalculo.equals(dataAjuizamento);
	}	
	
    /** 
     * Zera todas as referencias de hora, minuto, segundo e milesegundo da 
     * data. 
     * @param date a ser modificado. 
     */  
    public Date toOnlyDate(Date date) {
    	Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);  
        calendar.set(Calendar.MINUTE, 0);  
        calendar.set(Calendar.SECOND, 0);  
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }  	
	
	/**
	 * Valida a data de transito julgado se ela é maior que a data de ajuizamento
	 * e se houver oposição de embargos será verificado também se a data de transito  
	 * julgado é menor que a data de trânsito em embargos
	 * @param rpv
	 * @throws RpvException 
	 */
	private void validaDataTransitoJulgado(Rpv rpv) throws RpvException{
		Date dataTransitoJulgado = toOnlyDate(rpv.getDataTransitoJulgado());
		Date dataDistribuicao = toOnlyDate(rpv.getProcessoTrf().getDataDistribuicao());
		
		if(dataTransitoJulgado.before(dataDistribuicao)){
			throw new RpvException( "A data de trânsito julgado do processo de " +
									"conhecimento deve ser maior que a data de ajuizamento.");
		}
		if(rpv.getInOposicaoEmbargos()){
			Date dataTransitoEmbargos = toOnlyDate(rpv.getDataTransitoEmbargos());
			if(!dataTransitoJulgado.before(dataTransitoEmbargos)){
				throw new RpvException("A data de trânsito em julgado do processo de " +
						"conhecimento deve ser menor que a data de trânsito em embargos.");
			}
		}
	}

	/**
	 * Método que verifica se nos assuntos do processo tem o assunto multa astreintes
	 * @param processoTrf
	 * @return
	 */
	public boolean isAssuntoAstreintes(ProcessoTrf processoTrf){
		AssuntoTrf assuntoMultaAstreintes = ParametroUtil.instance().getAssuntoMultaAstreintes();
		return processoTrf.getAssuntoTrfList().contains(assuntoMultaAstreintes);
	}
	
	/**
	 * Método usado para validar as partes que irão fazer parte da rpv
	 * @param rpv
	 * @return true ou false
	 * @throws lança uma RpvException caso algum dado não seja validado
	 */
	public boolean validarPartesRpv(Rpv rpv) throws RpvException{
		if(rpv == null){
			throw new RpvException("Rpv está vazia!");
		} 
		//Validação de Perito
		if(rpv.getInPagamentoDiretoPerito()){
			boolean existePerito = false;
			TipoParte tipoPerito = ParametroUtil.instance().getTipoPartePerito();
			for (RpvPessoaParte pessoaParte : rpv.getRpvParteList()) {
				if(pessoaParte.getTipoParte() == tipoPerito){
					existePerito = true;
					break;
				}
			}
			if(!existePerito){
				 throw new RpvException("Em pagamento de Honorários a ser efetuado diretamente " +
										"a Perito/Intérprete é obrigatório selecionar ao " +
										"menos 1 Perito ou Interprete");
			}
		}
		
		//Validação de Cessionário
		if(rpv.getInCessionario()){
			List<RpvPessoaParte> listCessionarioByRpv = listCessionarioByRpv(rpv);
			if(!(listCessionarioByRpv.size() > 0)){
				throw new RpvException("É obrigatório selecionar ao menos um Cessionário.");
			}
		} 
		
		if(rpv.getReu() == null){
			throw new RpvException("É obrigatório selecionar ao menos uma parte passiva.");
		}
		
		if(rpv.getInTipoFormaHonorario().equals(RpvTipoFormaHonorarioEnum.P)){
			TipoParte tipoAdv = ParametroUtil.instance().getTipoParteAdvogado();
			for (RpvPessoaParte adv : rpv.getListaParteAtivo()) {
				if(adv.getTipoParte().equals(tipoAdv) && !(adv.getValorPercentualHonorario() > 0 && adv.getValorPercentualHonorario() < 100)){
					throw new RpvException("O valor percentual do advogado tem de ser maior que zero e menor que 100.");
				}
			}
		}		
		
		return true;
	}
	
	/**
	 * Verifica se há outros requisitórios do tipo 'apenas ressarcimento de
	 * custas' para o mesmo beneficiário.
	 * 
	 * @param rpv RPV a ser testada.
	 * @throws RpvException
	 */
	public void validarOutrosRessarcimentoCustas(Rpv rpv) throws RpvException {
		if (rpv.getInRessarcimentoCustas()) {
			List<Rpv> listRpvRessarcimentoCustas = rpvDAO
					.listRpvRessarcimentoCustasByRpv(rpv);
			if (listRpvRessarcimentoCustas.size() > 0) {
				throw new RpvException("Já foi expedido RPV/Precatório com ressarcimento de custas para o beneficiário "
								+ rpv.getBeneficiario());
			}
		}
	}
	
	/**
	 * Retorna lista de outros requisitórios do tipo 'apenas ressarcimento de
	 * custas' para o mesmo beneficiário.
	 * 
	 * @param rpv RPV base da listagem.
	 */
	public List<Rpv> listRpvRessarcimentoCustas(Rpv rpv) {
		return rpvDAO.listRpvRessarcimentoCustasByRpv(rpv);
	}
	
	/**
	 * Método que verifica se a pessoa é menor de idade
	 * @param pessoa
	 * @return true ou false
	 */
	public boolean isMenor(Pessoa pessoa){
		PessoaFisica pessoaFisica;
		if(pessoa instanceof PessoaFisica){
			pessoaFisica = 
			EntityUtil.getEntityManager().find(PessoaFisica.class, pessoa.getIdUsuario());
			if(pessoaFisica.getDataNascimento() == null){
				return false;
			}
			return DateUtil.getIdade(pessoaFisica.getDataNascimento()) < 18 ; 
		}
		
		if (EntityUtil.isHibernateProxy(pessoa.getClass())) {
			try {
				pessoaFisica = EntityUtil.find(PessoaFisica.class, pessoa.getIdUsuario());
			} catch (EntityNotFoundException e) {
				return false;
			}
			if(pessoaFisica != null){
				if(pessoaFisica.getDataNascimento() == null){
					return false;
				}
				return DateUtil.getIdade(pessoaFisica.getDataNascimento()) < 18 ;
			}
		}		
		
		return false;
	}	
	
	/**
	 * Método que verifica se a pessoa tem data de obito
	 * @param pessoa
	 * @return true ou false
	 */
	public boolean isObito(Pessoa pessoa){
		PessoaFisica pessoaFisica;
		if(pessoa instanceof PessoaFisica){
			pessoaFisica = 
				EntityUtil.getEntityManager().find(PessoaFisica.class, pessoa.getIdUsuario());
			return pessoaFisica.getDataObito() != null;
		}
		
		if (EntityUtil.isHibernateProxy(pessoa.getClass())) {
			try {
				pessoaFisica = EntityUtil.find(PessoaFisica.class, pessoa.getIdUsuario());
			} catch (EntityNotFoundException e) {
				return false;
			}
			if(pessoaFisica != null){
				return pessoaFisica.getDataObito() != null;
			}
		}
		
		return false;
	}
	
	/**
	 * Método que verifica se uma pessoa é incapaz
	 * @param pessoa
	 * @return true ou false
	 */
	public boolean isInCapaz(Pessoa pessoa){
		if (pessoa != null) {
			PessoaFisica pessoaFisica;
			if(pessoa instanceof PessoaFisica){
				pessoaFisica = 
					EntityUtil.getEntityManager().find(PessoaFisica.class, pessoa.getIdUsuario());
				return pessoaFisica.getIncapaz();
			}
			
			if (EntityUtil.isHibernateProxy(pessoa.getClass())) {
				try {
					pessoaFisica = EntityUtil.find(PessoaFisica.class, pessoa.getIdUsuario());
				} catch (EntityNotFoundException e) {
					return false;
				}
				
				if(pessoaFisica != null){
					return pessoaFisica.getIncapaz();
				}
			}
		}
		return false;
	}
	
	/**
	 * Retorna uma lista de RpvPessoaParte com os Cessionarios da RPV
	 * @param rpv
	 * @return lista de RpvPessoaParte
	 */
	public List<RpvPessoaParte> listCessionarioByRpv(Rpv rpv){
		return rpvDAO.listCessionarioByRpv(rpv);
	}
	
	public void addAdvogadosOuProcuradoresReu(RpvPessoaParte rpvParteReu){
		ProcessoTrf processoTrf = rpvParteReu.getRpv().getProcessoTrf();
		Pessoa reu = rpvParteReu.getPessoa();
		List<ProcessoParteRepresentante> representanteList = 
			processoTrfManager.getListRepresentanteByPessoaAndProcessoTrf(processoTrf, reu);
		for (ProcessoParteRepresentante representante : representanteList) {
			TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
			TipoParte tipoParteProcurador = ParametroUtil.instance().getTipoParteProcurador();
			if(representante.getParteRepresentante().getTipoParte().equals(tipoParteAdvogado) ||
			   representante.getParteRepresentante().getTipoParte().equals(tipoParteProcurador)){
				RpvPessoaParte parteAP = new RpvPessoaParte();
				parteAP.setRpv(rpvParteReu.getRpv());
				parteAP.setPessoa(representante.getParteRepresentante().getPessoa());
				parteAP.setTipoParte(representante.getParteRepresentante().getTipoParte());
				parteAP.setInParticipacao(RpvPessoaParteParticipacaoEnum.P);
				persist(parteAP);
				rpvParteReu.getRpv().getRpvParteList().add(parteAP);
				
				RpvParteRepresentante repAP = new RpvParteRepresentante();
				repAP.setRpvPessoaParte(rpvParteReu);
				repAP.setRpvPessoaRepresentante(parteAP);
				persist(repAP);
				rpvParteReu.getRpvRepresentanteList().add(repAP);
			}
		}
		
	}
	
	public void removeAdvogadosOuProcuradoresReu(RpvPessoaParte rpvParteReu){
		List<RpvPessoaParte> rpvParteRemovidos = new ArrayList<RpvPessoaParte>(0);
		List<RpvParteRepresentante> advOrProcRemovidos = new ArrayList<RpvParteRepresentante>(0);
		for (RpvParteRepresentante advOrProc : rpvParteReu.getRpvRepresentanteList()) {
			remove(advOrProc);
			advOrProcRemovidos.add(advOrProc);
			remove(advOrProc.getRpvPessoaRepresentante());
			rpvParteRemovidos.add(advOrProc.getRpvPessoaRepresentante());
		}
		rpvParteReu.getRpvRepresentanteList().removeAll(advOrProcRemovidos);
		rpvParteReu.getRpv().getRpvParteList().removeAll(rpvParteRemovidos);
	}
	
	public void atualizaParteReu(Rpv rpv){
		Pessoa reu = rpv.getReu();
		RpvPessoaParte rpvParteReu = null;
		
		//Adiciona o reu como parte da rpv quando não existe algum reu já associado
		if(rpv.getListaPartePassivo().isEmpty()){
			rpvParteReu = new RpvPessoaParte();
			ProcessoParte parteReu = 
				processoTrfManager.getParteByPessoaPoloAndProcesso(reu, rpv.getProcessoTrf(), "P");
			rpvParteReu.setPessoa(parteReu.getPessoa());
			rpvParteReu.setInParticipacao(RpvPessoaParteParticipacaoEnum.P);
			rpvParteReu.setRpv(rpv);
			rpvParteReu.setTipoParte(parteReu.getTipoParte());
			persist(rpvParteReu);
			rpv.getRpvParteList().add(rpvParteReu);
			
		} else {
			//Se já houver um reu cadastrado verifica se houve alteração no reu
			for (RpvPessoaParte parte : rpv.getListaPartePassivo()) {
				if(parte.getPessoa().equals(reu)){
					rpvParteReu = parte;
				}
			}

			//Se for o mesmo reu atualiza seus dados
			if(rpvParteReu != null){
				removeAdvogadosOuProcuradoresReu(rpvParteReu);
				addAdvogadosOuProcuradoresReu(rpvParteReu);
			} else {
				//Se for outro reu remove os dados do antigo e cria o novo
				RpvPessoaParte reuAntigo = rpv.getListaPartePassivo().get(0);
				removeAdvogadosOuProcuradoresReu(reuAntigo);
				remove(reuAntigo);
				rpv.getRpvParteList().remove(reuAntigo);
				
				rpvParteReu = new RpvPessoaParte();
				ProcessoParte parteReu = 
					processoTrfManager.getParteByPessoaPoloAndProcesso(reu, rpv.getProcessoTrf(), "P");
				
				rpvParteReu.setPessoa(parteReu.getPessoa());
				rpvParteReu.setInParticipacao(RpvPessoaParteParticipacaoEnum.P);
				rpvParteReu.setRpv(rpv);
				rpvParteReu.setTipoParte(parteReu.getTipoParte());
				persist(rpvParteReu);
				rpv.getRpvParteList().add(rpvParteReu);				
				addAdvogadosOuProcuradoresReu(rpvParteReu);
			}
		}
		
	}
	
	/**
	 * Adiciona cessionário as partes da rpv
	 * @param rpv
	 * @param pessoa
	 */
	public void addCessionario(Rpv rpv, Pessoa pessoa){
		RpvPessoaParte rpvParte = new RpvPessoaParte();
		rpvParte.setPessoa(pessoa);
		rpvParte.setRpv(rpv);
		rpvParte.setInParticipacao(RpvPessoaParteParticipacaoEnum.T);
		TipoParte parteCessionario = ParametroUtil.instance().getTipoParteCessionario();
		rpvParte.setTipoParte(parteCessionario);
		rpvDAO.persist(rpvParte);
		rpv.getRpvParteList().add(rpvParte);
	}
	
	/**
	 * Remove um cessionario da rpvPessoaParte
	 * @param pessoaParte
	 */
	public void removeCessionario(RpvPessoaParte pessoaParte){
		rpvDAO.remove(pessoaParte);
		pessoaParte.getRpv().getRpvParteList().remove(pessoaParte);
	}	
	
	public void removePartesPoloAtivo(Rpv rpv){
		List<RpvPessoaParte> partesRemovidas = new ArrayList<RpvPessoaParte>(0);
		for (RpvPessoaParte parte : rpv.getRpvParteList()) {
			if(parte.getPessoa().equals(rpv.getBeneficiario()) || 
				parte.getPessoa().equals(ParametroUtil.instance().getPessoaSecaoJudiciaria())){
				removeParteCompleto(parte,partesRemovidas);
			}
		}
		rpv.getRpvParteList().removeAll(partesRemovidas);
	}	
	
	public void removeParteCompleto(RpvPessoaParte rpvPessoaParte, List<RpvPessoaParte> partesRemovidas) {
		List<RpvParteRepresentante> rpvRepresentanteList = rpvPessoaParte.getRpvRepresentanteList();
		List<RpvParteRepresentante> representantesRemovidos = new ArrayList<RpvParteRepresentante>(0);
		for (RpvParteRepresentante rpvParteRepresentante : rpvRepresentanteList) {
			RpvPessoaParte rpvPessoaRepresentante = rpvParteRepresentante.getRpvPessoaRepresentante();
			representantesRemovidos.add(rpvParteRepresentante);
			rpvDAO.remove(rpvParteRepresentante);
			removeParteCompleto(rpvPessoaRepresentante,partesRemovidas);
		}
		if(rpvPessoaParte.getRpvParteDeducao() != null){
			super.remove(rpvPessoaParte.getRpvParteDeducao());
			rpvPessoaParte.setRpvParteDeducao(null);
		}
		
		List<RpvParteValorCompensar> parteCompensarsRemovidas = new ArrayList<RpvParteValorCompensar>(0);
		for (RpvParteValorCompensar parteValorCompensar : rpvPessoaParte.getRpvParteValorCompensarList()) {
			parteCompensarsRemovidas.add(parteValorCompensar);
			rpvDAO.remove(parteValorCompensar);
		}
		
		rpvDAO.remove(rpvPessoaParte);
		rpvPessoaParte.getRpvRepresentanteList().removeAll(representantesRemovidos);
		partesRemovidas.add(rpvPessoaParte);
	}

	public void addPeritoRpv(Pessoa pessoa, Rpv rpv){
		RpvPessoaParte parte = new RpvPessoaParte();
		parte.setPessoa(pessoa);
		parte.setRpv(rpv);
		parte.setInParticipacao(RpvPessoaParteParticipacaoEnum.T);
		TipoParte tipoPartePerito = ParametroUtil.instance().getTipoPartePerito();
		parte.setTipoParte(tipoPartePerito);
		rpvDAO.persist(parte);
		rpv.getRpvParteList().add(parte);
	}

	public void removeCessionariosRpv(Rpv rpv){
		List<RpvPessoaParte> listCessionarioByRpv = listCessionarioByRpv(rpv);
		if(listCessionarioByRpv.size() > 0){
			for (RpvPessoaParte rpvPessoaParte : listCessionarioByRpv) {
				rpvDAO.remove(rpvPessoaParte);
			}
			rpv.getRpvParteList().removeAll(listCessionarioByRpv);
		}
	}		
	
	public void removePeritosRpv(Rpv rpv){
		List<RpvPessoaParte> parteList = rpv.getRpvParteList();
		TipoParte tipoPartePerito = ParametroUtil.instance().getTipoPartePerito();
		List<RpvPessoaParte> peritos = new ArrayList<RpvPessoaParte>(0);
		for (RpvPessoaParte rpvPessoaParte : parteList) {
			if(rpvPessoaParte.getTipoParte() == tipoPartePerito){
				peritos.add(rpvPessoaParte); 
			}
		}

		for (RpvPessoaParte perito : peritos) {
			rpv.getRpvParteList().remove(perito);
			rpvDAO.remove(perito);
		}
	}	
	
	public void persisteRpvParte(RpvPessoaParte pessoaParte){
		rpvDAO.persist(pessoaParte);
	}
	
	public void persisteRpvRepresentante(RpvParteRepresentante representante){
		rpvDAO.persist(representante);
	}
	
	public String getStatusOab(PessoaAdvogado pessoaAdvogado){
		if(Strings.isEmpty(pessoaAdvogado.getNumeroOAB())){
			return "";
		}
		String cpf = pessoaAdvogado.getNumeroCPF().replaceAll("\\.", "").replaceAll("-", "");
		String statusOabAdvogado = rpvDAO.getStatusOabAdvogado(cpf, pessoaAdvogado.getUfOAB().getCodEstado());
		return Strings.isEmpty(statusOabAdvogado) ? "" : statusOabAdvogado; 
	}
	
	public double calculaLimiteValorCompensarPRC(RpvPessoaParte rpvPessoaParte, boolean tipoBeneficiario) throws RpvException{
		BaseCalculoIr bCI = baseCalculoIrManager.getBaseCalculoIr();
		boolean inInsentoIr = rpvPessoaParte.getInInsentoIr();
		RpvParteDeducao rpvParteDeducao = rpvPessoaParte.getRpvParteDeducao();
		Double valorPagoContratual = rpvPessoaParte.getValorPagoContratual();
		Double valorBeneficiario = rpvPessoaParte.getValorPagoPessoa();
		Integer numeroMeses = rpvPessoaParte.getNumeroMesExercicioAnterior();
		Double pss = rpvPessoaParte.getValorPss();
		
		Double limiteValorCompensar = 0.0;
		Double valorDeducoes = 0.0;

		if(rpvParteDeducao != null){
			valorDeducoes += rpvParteDeducao.getValorDespesaJudicial() != null ? rpvParteDeducao.getValorDespesaJudicial() : 0;
			valorDeducoes += rpvParteDeducao.getValorPensaoAlimenticia() != null ? rpvParteDeducao.getValorPensaoAlimenticia() : 0;
		}
		
		if(bCI == null){
			throw new RpvException("Não existe base de cálculo cadastrada, favor cadastrar.");			

		}
		
		if (!tipoBeneficiario) {
			if (pss == null) {
				pss = 0.00;
			}
			
			if (valorDeducoes == null) {
				valorDeducoes = 0.00;
			}			
			if(valorPagoContratual - pss - valorDeducoes > 1637.11){
				bCI = null;
				bCI = baseCalculoIrManager.getBaseCalculoIrByValor(valorPagoContratual);
				if(bCI == null){
					throw new RpvException("Favor cadastrar a base de cálculo de IR no Menu: Cadastros Básicos/Base de Cálculos IR");
				}
			}
			
			if (inInsentoIr) {
				limiteValorCompensar = valorPagoContratual;
			} else {
				Double percLimiteValorCompensar = Double.parseDouble(ParametroUtil.instance().getPercLimiteValorCompsar());
				limiteValorCompensar = valorPagoContratual - (percLimiteValorCompensar * valorPagoContratual);
			}
		}
		
		if (tipoBeneficiario) {
			if (pss == null) {
				pss = 0.00;
			}	
			if (valorDeducoes == null) {
				valorDeducoes = 0.00;
			}
			
			if(getBaseIr(valorBeneficiario, pss, valorDeducoes) > 1637.11){
				if(numeroMeses == null || numeroMeses == 0){
					bCI = null;
					bCI = baseCalculoIrManager.getBaseCalculoIrByValor(getBaseIr(valorBeneficiario, pss, valorDeducoes));
				}else{
					bCI = null;
					bCI = baseCalculoIrManager.getBaseCalculoIrByValor(getBaseIr(valorBeneficiario, pss, valorDeducoes)/numeroMeses);
				}
				if(bCI == null){
					throw new RpvException("Favor cadastrar a base de cálculo de IR no Menu: Cadastros Básicos/Base de Cálculos IR");
				}
			}
			
			if (inInsentoIr) {
				limiteValorCompensar = valorBeneficiario - pss;
			} else {
				if (numeroMeses == null || numeroMeses == 0) {
					Double percLimiteValorCompensar = Double.parseDouble(ParametroUtil.instance().getPercLimiteValorCompsar());
					limiteValorCompensar = (valorBeneficiario - pss) - (percLimiteValorCompensar * (valorBeneficiario - pss));
				} else {
					limiteValorCompensar = getValorAliquota(valorBeneficiario, pss, numeroMeses, valorDeducoes, tipoBeneficiario);
				}
			}
		}
		
		return limiteValorCompensar;
	}
	
	/**
	 * Método que calcula o valor da aliquota para o calculo do taltal de valor a compensar
	 * @param valorBeneficiario
	 * @param pss
	 * @param numeroMeses
	 * @param valorDeducoes
	 * @param tipoBeneficiario
	 * @return valorAliquota
	 */
	private Double getValorAliquota(double valorBeneficiario, double pss, int numeroMeses,
									double valorDeducoes, boolean tipoBeneficiario) {
		Double baseIr = getBaseIr(valorBeneficiario, pss, valorDeducoes)/numeroMeses;
		BaseCalculoIr baseCalculoIr = baseCalculoIrManager.getBaseCalculoIrByValor(baseIr);
		if (baseCalculoIr != null) { 
			return valorBeneficiario -( pss + getIr(getBaseIr(valorBeneficiario, pss, valorDeducoes), baseCalculoIr, numeroMeses));
		} else {
			return valorBeneficiario - pss;
		}
	}	
	
	/**
	 * Método que retorna o valor do imposto de renda para o limite do valor a compensar
	 * @param valor
	 * @param baseCalculoIr
	 * @param numeroMeses
	 * @return IR
	 */
	private Double getIr(Double valor, BaseCalculoIr baseCalculoIr, int numeroMeses) {
		return ((valor * baseCalculoIr.getVlAliquota()/100) - (baseCalculoIr.getVlParcelaADeduzir() * numeroMeses));
	}	
	
	/**
	 * Retorna o valor base de calculo para o imposto de renda
	 * @param valorBeneficiario
	 * @param pss
	 * @param valorDeducoes
	 * @return valor base IR
	 */
	private Double getBaseIr(double valorBeneficiario, double  pss, double  valorDeducoes) {
		return valorBeneficiario - pss - valorDeducoes;
	}	
	
	public class RpvException extends Exception{

		private static final long serialVersionUID = 1L;

		public RpvException(String message, Throwable cause) {
			super(message, cause);
		}

		public RpvException(String message) {
			super(message);
		}

		public RpvException(Throwable cause) {
			super(cause);
		}
	}
	
}
