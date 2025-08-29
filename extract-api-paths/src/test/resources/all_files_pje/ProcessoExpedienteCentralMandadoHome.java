package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.ProcessoExpedienteCentralMandadoDistribuicaoList;
import br.com.infox.pje.list.ProcessoExpedienteCentralMandatoList;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.CentralMandadoLocalizacao;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaGrupoOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.DistribuirRedistribuirEnum;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;

@Name("processoExpedienteCentralMandadoHome")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoExpedienteCentralMandadoHome extends
		AbstractProcessoExpedienteCentralMandadoHome<ProcessoExpedienteCentralMandado>{

	private static final long serialVersionUID = 1L;
	private List<ProcessoExpediente> list = new ArrayList<ProcessoExpediente>(0);
	private List<ProcessoExpedienteCentralMandado> listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>(0);

	private String numProcesso;
	private String tipoProcessoDocumento;
	private String oficialJustica;
	private String grupoOficialJustica;
	private Boolean checkBox;
	private GrupoOficialJustica grupoOficial;
	private PessoaOficialJustica pessoaOficialJustica;
	private Date dataInicio;
	private Date dataFim;
	private TipoProcessoDocumento tpProcessoDocumento;
	private DistribuirRedistribuirEnum distribuir;
	private CentralMandado centralMandado;
	private Boolean checkAllDistribuir = Boolean.FALSE;
	private Boolean checkAllRedistribuir = Boolean.FALSE;
	private String nomeParte;

	/**
	 * PJE-JT: Rodrigo Cartaxo / Rafael Barros : PJE-1.4.2-jt
	 * 
	 * Alterações feitas pela JT para correção de bug.
	 */
	private List<ProcessoExpedienteCentralMandado> listDistribuidos = new ArrayList<ProcessoExpedienteCentralMandado>(0);
	private List<ProcessoExpedienteCentralMandado> listNaoDistribuidos = new ArrayList<ProcessoExpedienteCentralMandado>(0);
	private List<String> erros = new ArrayList<String>();
	
	/**
	 * PJE-JT Fim
	 */

	public static ProcessoExpedienteCentralMandadoHome instance(){
		return ComponentUtil.getComponent("processoExpedienteCentralMandadoHome");
	}

	@Override
	public void newInstance(){
		list = new ArrayList<ProcessoExpediente>(0);
		super.newInstance();
	}

	/*
	 * Essa inserção ocorre quando o meio de envio do ProcessoExpediente é "Central de Mandado"
	 */
	public void inserirEnvioCentral(ProcessoExpediente obj) throws Exception{
		if (existeDistribuidor()){
			getInstance().setProcessoExpediente(obj);
			getInstance().setUrgencia(obj.getUrgencia());
			getInstance().setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.A);
			// PJE-JT:Desenvolvedor Haroldo Arouca :PJE-109 2011-09-01:Alteracoes
			// feitas pela JT
			if (centralMandado == null){
				centralMandado = findCentralMandado(Authenticator.getOrgaoJulgadorAtual().getLocalizacao());
			}
			// PJE-JT:Fim
			getInstance().setCentralMandado(centralMandado);

			persist();
		}
		else{
			throw new Exception("Não existem distribuidores para esta Central de Mandado");
		}
	}

	private boolean existeDistribuidor() throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacao o ");
		sb.append("where o.papel.identificador = :papel ");
		sb.append("and o.localizacaoFisica.localizacao = :localizacaoCentral ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("papel", Papeis.OFICIAL_JUSTICA_DISTRIBUIDOR);
		q.setParameter("localizacaoCentral", getLocalizacaoCentral().getLocalizacao().getLocalizacao());
		long resultado = (Long) q.getSingleResult();
		return resultado > 0;

	}

	private CentralMandadoLocalizacao getLocalizacaoCentral() throws Exception{

		if (centralMandado == null){
			throw new Exception("Não há central de mandados definida para busca da localização central.");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select o from CentralMandadoLocalizacao o ");
		sb.append("where o.centralMandado = :centralMandado ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("centralMandado", centralMandado);

		List list = q.getResultList();
		if (list.size() > 0){
			return (CentralMandadoLocalizacao) list.get(0);
		}

		throw new Exception("Não foi encontrada nenhuma localização para a central de mandados definida");

	}

	public CentralMandado findCentralMandado(Localizacao localizacao){

		EntityManager em = EntityUtil.getEntityManager();
		String sql = "select o.centralMandado from CentralMandadoLocalizacao o " + "where o.localizacao = :localizacao";
		Query query = em.createQuery(sql);
		query.setParameter("localizacao", localizacao);
		query.setMaxResults(1);
		
		try {
			return (CentralMandado) query.getSingleResult();
		} catch (NoResultException e) {
		}

		return null;
	}

	public void criarLista(ProcessoExpediente obj){
		if (obj.getCheckado()){
			list.add(obj);
		}
		else{
			list.remove(obj);
		}
	}

	/**
	 * Cria uma Lista de expedientes para distribuição
	 * 
	 * @param obj - Objeto do tipo ProcessoExpedienteCentralMandado
	 */
	public void criarListaDistribuir(ProcessoExpedienteCentralMandado obj){
		if (!listDistribuir.contains(obj)){
			listDistribuir.add(obj);
		}
		else if (listDistribuir.contains(obj)){
			listDistribuir.remove(obj);
		}
	}

	public Boolean verificaDistribuirExpediente(){
		if (this.listDistribuir.size() > 0){
			return true;
		}
		return false;
	}
	
	public void limpaPesquisa(){
		setNomeParte(null);
		setGrupoOficial(null);
		setTpProcessoDocumento(null);
	}

	/**
	 * Marca todos os checkbox da grid distribuirExpedienteGrid
	 */
	@SuppressWarnings("unchecked")
	public void checkAll(String grid){
		GridQuery gq = getComponent(grid);

		// Joga os valores da Grid em uma lista
		List<ProcessoExpedienteCentralMandado> listExpediente = new ArrayList<ProcessoExpedienteCentralMandado>(gq.getFullList());

		for (ProcessoExpedienteCentralMandado processoExpedienteCentralMandado : listExpediente) {
			processoExpedienteCentralMandado.setCheck(checkBox);
			criarListaDistribuir(processoExpedienteCentralMandado);
		}
	}

	/**
	 * Faz a distribuição dos expedientes para os oficiais de justiça
	 */
	public void distribuir(String form){

		listDistribuidos = new ArrayList<ProcessoExpedienteCentralMandado>(0);
		listNaoDistribuidos = new ArrayList<ProcessoExpedienteCentralMandado>(listDistribuir);
		ProcessoExpedienteCentralMandado pecmErro = null;
		
		try{
			if(grupoOficial == null) {
				FacesMessages.instance().add(Severity.ERROR, "O grupo é obrigatório!");
				return;
			}
				
			Date data = DateService.instance().getDataHoraAtual();
			List<PessoaGrupoOficialJustica> pgList;
			boolean ok = false;

	
			pgList = getPessoaGrupoOficialJustica();

			if (pgList.size() <= 0){
				FacesMessages.instance().add(Severity.ERROR, "Não existe Oficial de Justiça para o grupo " + grupoOficial.getGrupoOficialJustica());
				return;
			}
				
			//Caso o oficial de justiça não tenha sido escolhido a distribuição será aleatória.
			if (pessoaOficialJustica == null){
				double qtd = getQtdExpedienteDivisao(listDistribuir.size(), pgList.size());

				int aleatorio;
				Random random = new Random();
				
				for (ProcessoExpedienteCentralMandado pecm : listNaoDistribuidos){
					pecmErro = pecm;
					aleatorio = random.nextInt(pgList.size());
					
					int pgListIdPGOJ = pgList.get(aleatorio).getIdPessoaGrupoOficialJustica();
					
					
					// Se os ID's forem iguais o sistema procura outro oficial de justiça
					if (pecm.getPessoaGrupoOficialJustica() != null && 
						pecm.getPessoaGrupoOficialJustica().getIdPessoaGrupoOficialJustica() == pgListIdPGOJ){
						
						int controle = pgList.size();
						while (pecm.getPessoaGrupoOficialJustica().getIdPessoaGrupoOficialJustica() == pgListIdPGOJ && controle > -1){
							aleatorio = random.nextInt(pgList.size());
							pgListIdPGOJ = pgList.get(aleatorio).getIdPessoaGrupoOficialJustica();
							controle--;
						}
					} 
					
					if (pgList.get(aleatorio).getQtdProcessos() >= qtd){
						int controle = pgList.size();
						while (pgList.get(aleatorio).getQtdProcessos() >= qtd && controle > -1){
							aleatorio = random.nextInt(pgList.size());
							controle--;
						}						
					}				
					
					// Se existir um ProcessoExpedienteCentralMandado anterior ele será redistríbuido!
					if (pecm.getProcessoExpedienteCentralMandadoAnterior() != null || pecm.getPessoaGrupoOficialJustica() != null){
						ok = redistribuir(pecm, pgList.get(aleatorio), data);
					}else{//Senão é uma distribuição
						ok = distribuir(pecm, pgList.get(aleatorio), data);
					}
					pgList.get(aleatorio).setQtdProcessos(pgList.get(aleatorio).getQtdProcessos() + 1);
				}
				
			}else{
				
				for (ProcessoExpedienteCentralMandado pecm : listNaoDistribuidos){
					pecmErro = pecm;
					if (pecm.getProcessoExpedienteCentralMandadoAnterior() != null) {
						ok = redistribuir(pecm, pgList.get(0),data);
					}else{
						ok = distribuir(pecm, pgList.get(0),data);
					}
					
				}
			}
				
			if (ok){
				escolherViewResultado(form);
				grupoOficial = null;
				pessoaOficialJustica = null;
				ok = false;
			}
				
		} catch (Exception e){
			adicionarMensagemErro(pecmErro, e);
		}
	}

	private boolean redistribuir(ProcessoExpedienteCentralMandado pecm, PessoaGrupoOficialJustica pessoaGrupoOficialJustica, Date data){
			
		 
		 super.newInstance();
		 
		 instance.setCentralMandado(pecm.getCentralMandado());
		 instance.setDtDistribuicaoExpediente(data);
		 instance.setProcessoExpedienteCentralMandadoAnterior(pecm);
		 instance.setEnviadoScm(pecm.getEnviadoScm());
		 instance.setProcessoExpediente(pecm.getProcessoExpediente());
		 instance.setPessoaGrupoOficialJustica(pessoaGrupoOficialJustica);
		 instance.setUrgencia(pecm.getUrgencia());
		 instance.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.A);
		 persist();
		 
		 pecm.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.C);
		 getEntityManager().merge(pecm);
		 getEntityManager().flush();
		 pecm.setPessoaGrupoOficialJustica(pessoaGrupoOficialJustica);
		 setInstance(pecm);
		 
		 /**
		  * PJEII-17871 - Geração automática de movimentação no momento da distribuição do mandado ao oficial pelo oficial-distribuidor 
		  */
		 if (ParametroUtil.instance().isRegistrarMovimentacaoDistribuicaoMandado()) {
			try{
				lancarMovimentoRecebimento(pecm);
			} catch (Exception e){
				adicionarMensagemErro(pecm, e);
			}
		 }
		 
		 listDistribuidos.add(pecm);
		 listDistribuir.remove(pecm);
		 update();
		 return true;
	}
		
	private boolean distribuir(ProcessoExpedienteCentralMandado pecm, PessoaGrupoOficialJustica pessoaGrupoOficialJustica, Date data){
		
		pecm.setPessoaGrupoOficialJustica(pessoaGrupoOficialJustica);
		pecm.setDtDistribuicaoExpediente(data);
		setInstance(pecm);
		
		/**
		 * PJEII-17871 - Geração automática de movimentação no momento da distribuição do mandado ao oficial pelo oficial-distribuidor 
		 */
		if (ParametroUtil.instance().isRegistrarMovimentacaoDistribuicaoMandado()) {
			try{
				lancarMovimentoRecebimento(pecm);
			} catch (Exception e){
				adicionarMensagemErro(pecm, e);
			}
		}	
		persist();						
		listDistribuidos.add(pecm);
		listDistribuir.remove(pecm);
	
		return true;
	}

	

	private void escolherViewResultado(String form) {
		if (form.equals("distribuirExpedienteForm")){
			setCheckBox(false);
			Redirect.instance().setViewId("/DistribuirExpediente/listViewDistribuidos.seam");
			Redirect.instance().execute();
		} else if (form.equals("redistribuirExpedienteForm")){
			setCheckBox(false);
			refreshGrid("redistribuirExpedienteGrid");
			Redirect.instance().setViewId("/RedistribuirExpediente/listViewDistribuidos.seam");
			Redirect.instance().execute();
		} else if (form.equals("distribuirExpedientePainelForm")){
			Redirect.instance().setViewId("/Painel/painel_usuario/Paniel_Usuario_Oficial_Justica/listViewDistribuidos.seam");
			Redirect.instance().execute();
		} else if (form.equals("redistribuirExpedientePainelForm")){
			Redirect.instance().setViewId("/Painel/painel_usuario/Paniel_Usuario_Oficial_Justica/listViewRedistribuidos.seam");
			Redirect.instance().execute();
		}
	}

	private void adicionarMensagemErro(ProcessoExpedienteCentralMandado pecm, Exception e) {
		String msg = "Erro durante o lançamento do movimento de recebimento para o processo %s (erro: %s)";
		String msgFormatada = String.format(msg, pecm.getProcessoExpediente().getProcessoTrf(), e.getMessage());
		
		/*
		 * Adicionando no FacesMessages, pois o código exibe os erros em uma popup 
		 * (controle feito através da variável ok no método distribuir)
		 */
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.ERROR, msgFormatada);
		
		erros.add(msgFormatada);
	}
	
	/**
	 * Pega a quantidade de processos marcados para distribuição, divide pela quantidade de oficiais escolhidos e arredonda para cima, caso o valor
	 * seja fracionado, para que seja distribuida igualmente a quantidade de processos para cada oficial, isso no caso de haver mais de um oficial
	 * escolhido
	 * 
	 * @param nrMarcados
	 * @param nrOficiais
	 * @return resultado da divisão de nrMarcados por nrOficiais arredondado para cima
	 */
	private double getQtdExpedienteDivisao(int nrMarcados, int nrOficiais){
		Double nm1 = new Double(nrMarcados);
		Double no1 = new Double(nrOficiais);
		if (nm1 % no1 == 0){
			nm1 = nm1 / no1;
		}
		else{
			nm1 = (double) Math.round((nm1 / no1) + 0.5d);
		}
		return nm1;

		// BigDecimal nm = new BigDecimal(nrMarcados);
		// BigDecimal no = new BigDecimal(nrOficiais);
		// nm = nm.divide(no);
		// nm = nm.setScale(0, BigDecimal.ROUND_HALF_UP);
		// return nm.doubleValue();
	}

	/**
	 * Procura um objeto PessoaGrupoOficialJustica através dos objetos GrupoOficialJustica e PessoaOficialJustica
	 * 
	 * @return Lista de PessoaGrupoOficialJustica
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaGrupoOficialJustica> getPessoaGrupoOficialJustica(){
		List<PessoaGrupoOficialJustica> pgList = new ArrayList<PessoaGrupoOficialJustica>();

		StringBuffer query = new StringBuffer();

		query.append("select o from PessoaGrupoOficialJustica o ");
		query.append("where o.grupoOficialJustica = :grupoOficial and o.pessoa.nome like :oficialJustica ");
		query.append("and o.ativo = true");

		Query q = getEntityManager().createQuery(query.toString());
		q.setParameter("grupoOficial", getGrupoOficial());

		if (pessoaOficialJustica != null){
			q.setParameter("oficialJustica", getPessoaOficialJustica().getNome());
		}
		else{
			q.setParameter("oficialJustica", "%");
		}

		List<PessoaGrupoOficialJustica> result = q.getResultList();
		
		for (int i = 0; i < result.size(); i++){
			PessoaGrupoOficialJustica pg = result.get(i);
			pgList.add(pg);
		}
		return pgList;
	}

	public void inserirVarios(){
		CentralMandado centralMandado = findCentralMandado(Authenticator.getLocalizacaoFisicaAtual());

		for (ProcessoExpediente lista : list){
			getInstance().setCentralMandado(centralMandado);
			getInstance().setProcessoExpediente(lista);
			persist(getInstance());
		}
		refreshGrid("recebeExpedienteFisicoGrid");
	}

	public List<ProcessoExpediente> getList(){
		return list;
	}

	public void setList(List<ProcessoExpediente> list){
		this.list = list;
	}

	public List<ProcessoExpedienteCentralMandado> getListDistribuir(){
		return listDistribuir;
	}

	public void setListDistribuir(List<ProcessoExpedienteCentralMandado> listDistribuir){
		this.listDistribuir = listDistribuir;
	}

	public String getNumProcesso(){
		return numProcesso;
	}

	public void setNumProcesso(String numProcesso){
		this.numProcesso = numProcesso;
	}

	public String getTipoProcessoDocumento(){
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(String tipoProcessoDocumento){
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public String getOficialJustica(){
		return oficialJustica;
	}

	public void setOficialJustica(String oficialJustica){
		this.oficialJustica = oficialJustica;
	}

	public String getGrupoOficialJustica(){
		return grupoOficialJustica;
	}

	public void setGrupoOficialJustica(String grupoOficialJustica){
		this.grupoOficialJustica = grupoOficialJustica;
	}

	public Boolean getCheckBox(){
		return checkBox;
	}

	public void setCheckBox(Boolean checkBox){
		this.checkBox = checkBox;
	}

	public GrupoOficialJustica getGrupoOficial(){
		return grupoOficial;
	}

	public void setGrupoOficial(GrupoOficialJustica grupoOficial){
		this.grupoOficial = grupoOficial;
	}

	public PessoaOficialJustica getPessoaOficialJustica(){
		return pessoaOficialJustica;
	}

	public void setPessoaOficialJustica(PessoaOficialJustica pessoaOficialJustica){
		this.pessoaOficialJustica = pessoaOficialJustica;
	}

	public Date getDataInicio(){
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio){
		this.dataInicio = dataInicio;
	}

	public Date getDataFim(){
		return dataFim;
	}

	public void setDataFim(Date dataFim){
		this.dataFim = dataFim;
	}

	public TipoProcessoDocumento getTpProcessoDocumento(){
		return tpProcessoDocumento;
	}

	public void setTpProcessoDocumento(TipoProcessoDocumento tpProcessoDocumento){
		this.tpProcessoDocumento = tpProcessoDocumento;
	}

	public DistribuirRedistribuirEnum getDistribuir(){
		return distribuir;
	}

	public void setDistribuir(DistribuirRedistribuirEnum distribuir){
		this.distribuir = distribuir;
	}

	public Integer getIdPECM(){
		return getInstance().getIdProcessoExpedienteCentralMandado();
	}

	public String getNumeroProcesso(){
		return getInstance().getProcessoExpediente().getProcessoTrf().getProcesso().getNumeroProcesso();
	}

	public void setCentralMandado(CentralMandado centralMandado){
		this.centralMandado = centralMandado;
	}

	public CentralMandado getCentralMandado(){
		return centralMandado;
	}

	public void setCheckAllDistribuir(Boolean checkAllDistribuir){
		this.checkAllDistribuir = checkAllDistribuir;
	}

	public Boolean getCheckAllDistribuir(){
		return checkAllDistribuir;
	}
	
	public void setCheckAllRedistribuir(Boolean checkAllRedistribuir){
		this.checkAllRedistribuir = checkAllRedistribuir;
	}

	public Boolean getCheckAllRedistribuir(){
		return checkAllRedistribuir;
	}

	public String getNomeParte(){
		return nomeParte;
	}

	public void setNomeParte(String nomeParte){
		this.nomeParte = nomeParte;
	}

	public void checkAllListDistribuir(){
		ProcessoExpedienteCentralMandadoDistribuicaoList gq = ComponentUtil.getComponent("processoExpedienteCentralMandadoDistribuicaoList");
		List<ProcessoExpedienteCentralMandado> listExpediente = new ArrayList<ProcessoExpedienteCentralMandado>();

		// Joga os valores da Grid em uma lista
		List<ProcessoExpedienteCentralMandado> result = gq.getResultList();
		for (int i = 0; i < result.size(); i++){
			listExpediente.add(result.get(i));
		}
		
		if (checkAllDistribuir){
			listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>();
			for (int i = 0; i < listExpediente.size(); i++){
				ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = listExpediente.get(i);
				processoExpedienteCentralMandado.setCheck(true);
				criarListaDistribuir(processoExpedienteCentralMandado);
			}
			checkAllDistribuir = Boolean.TRUE;
		}
		else{
			for (int i = 0; i < listExpediente.size(); i++){
				ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = listExpediente.get(i);
				processoExpedienteCentralMandado.setCheck(false);				
			}
			listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>();
			checkAllDistribuir = Boolean.FALSE;
		}

	}

	public void checkAllListRedistribuir(){
		ProcessoExpedienteCentralMandatoList gq = ComponentUtil.getComponent("processoExpedienteCentralMandatoList");
		List<ProcessoExpedienteCentralMandado> listExpediente = new ArrayList<ProcessoExpedienteCentralMandado>();

		// Joga os valores da Grid em uma lista
		List<ProcessoExpedienteCentralMandado> result = gq.getResultList();
		for (int i = 0; i < result.size(); i++){
			listExpediente.add(result.get(i));
		}

		if (checkAllRedistribuir){
			listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>();
			for (int i = 0; i < listExpediente.size(); i++){
				ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = listExpediente.get(i);
				processoExpedienteCentralMandado.setCheck(true);
				criarListaDistribuir(processoExpedienteCentralMandado);
			}
			checkAllRedistribuir = Boolean.TRUE;
		}
		else{
			for (int i = 0; i < listExpediente.size(); i++){
				ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = listExpediente.get(i);
				processoExpedienteCentralMandado.setCheck(false);				
			}
			listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>();
			checkAllRedistribuir = Boolean.FALSE;
		}
		
	}

	@SuppressWarnings("unchecked")
	public void setarDocumentoPrincipal(){
		String sql = "select o.processoDocumento.processoDocumentoBin from ProcessoDocumentoExpediente o "
			+ "where o.processoExpediente.idProcessoExpediente in (select p.processoExpediente.idProcessoExpediente from ProcessoExpedienteCentralMandado p )"
			+ "and o.processoExpediente = :processoExpediente "
			+ "and o.anexo = false";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processoExpediente", instance().getInstance().getProcessoExpediente());
		q.setMaxResults(1);
		List<ProcessoDocumentoBin> result = q.getResultList();
		if (result != null && !result.isEmpty()){
			ProcessoDocumentoBinHome.instance().setInstance((ProcessoDocumentoBin) result.get(0));
		}
	}

	public void atualizarEnvioCentral(ProcessoExpediente processoExpediente) throws Exception{
		removerMandadoExpediente(processoExpediente);
		inserirEnvioCentral(processoExpediente);

	}

	@SuppressWarnings("unchecked")
	private void removerMandadoExpediente(ProcessoExpediente processoExpediente){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoExpedienteCentralMandado o ");
		sb.append("where o.processoExpediente = :pe ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pe", processoExpediente);
		List<ProcessoExpedienteCentralMandado> pecmList = (List<ProcessoExpedienteCentralMandado>) q.getResultList();
		for (ProcessoExpedienteCentralMandado processoExpedienteCentralMandado : pecmList) {
			if (processoExpedienteCentralMandado != null){
				getEntityManager().remove(processoExpedienteCentralMandado);
				getEntityManager().flush();
			}
		}
	}

	/**
	 * Método que lança o movimento de recebimento do mandado por parte do oficial de justiça.
	 * 
	 * @author Rodrigo Cartaxo / Rafael Barros
	 * @since versão 1.4.2-jt
	 * @category PJE-JT
	 * @param pecm
	 */
	private void lancarMovimentoRecebimento(ProcessoExpedienteCentralMandado pecm) throws AplicationException {
		Processo processo = pecm.getProcessoExpediente().getProcessoTrf().getProcesso();

		String codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_MANDADO_RECEBIDO;

		if (codigoMovimento != null && !"".equals(codigoMovimento)) {
			ProcessoEvento movimentoProcesso = MovimentoAutomaticoService.preencherMovimento().deCodigo(codigoMovimento)
					.associarAoProcesso(processo)
					.lancarMovimento();

			if (movimentoProcesso == null){
				throw new AplicationException("Não foi possível lançar o movimento de recebimento de mandado para cumprimento");
			}
		}
	}

	/**
	 * Método que retorna um objeto da classe Evento para um dado objeto da classe Evento.
	 * 
	 * @author Rodrigo Cartaxo / Rafael Barros
	 * @since versão 1.4.2-jt
	 * @category PJE-JT
	 * @param evento
	 * @return
	 */
	private Evento convertToEventoProcessual(Evento evento){

		Evento eventoProcessual = null;

		StringBuilder sb = new StringBuilder();
		sb.append("select o from Evento o ").append("where o.idEvento = :idEvento ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idEvento", evento.getIdEvento());
		eventoProcessual = (Evento) EntityUtil.getSingleResult(q);

		return eventoProcessual;
	}
	
	public boolean haProcessoExpedienteCentralMandado(ProcessoExpediente pe){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoExpedienteCentralMandado o ");
		sb.append("where o.processoExpediente = :pe ");
		sb.append("and o.centralMandado is not null ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pe", pe);
		
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException e) {
			return false;
		}
	}
	
	/*
	 * [PJEII-1809] PJE-JT: Ronny Paterson : PJE-1.4.4
	 * Criação de método que verifica previamente se o usuário logado está cadastrado 
	 * em algum grupo de oficiais de justiça.  
	 */
	public boolean isOficialEmGrupo() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from GrupoOficialJustica o where ");
		sb.append("o.ativo = true and "); 
		sb.append("o.centralMandado IN ( ");
		sb.append("select o.grupoOficialJustica.centralMandado from PessoaGrupoOficialJustica o "); 
		sb.append("where o.pessoa.idUsuario = #{usuarioLogado.idUsuario})");
		Query q = getEntityManager().createQuery(sb.toString());
		
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException e) {
			return false;
		}
	}

	/**
	 * @author Rodrigo Cartaxo / Rafael Barros
	 * @since versão 1.4.2-jt
	 * @category PJE-JT Alterações feitas pela JT para correção de bugs.
	 */
	public List<ProcessoExpedienteCentralMandado> getListDistribuidos(){
		return listDistribuidos;
	}

	public void setListDistribuidos(List<ProcessoExpedienteCentralMandado> listDistribuidos){
		this.listDistribuidos = listDistribuidos;
	}

	public List<String> getErros() {
		return erros;
	}

	public void setErros(List<String> erros) {
		this.erros = erros;
	}
	/**
	 * PJE-JT Fim
	 */
	
    public ProcessoExpedienteCentralMandadoStatusEnum[] getProcessoExpedienteCentralMandadoStatusEnumValues(){
        return ProcessoExpedienteCentralMandadoStatusEnum.values();
    }   

    public ProcessoDocumento getProcessoDocumentoExpediente(ProcessoExpedienteCentralMandado pecm) {
        String sql = "select o.processoDocumento from ProcessoDocumentoExpediente o where o.anexo = 'N' and o.processoExpediente = :processoExpediente";
        Query q = getEntityManager().createQuery(sql);
        q.setParameter("processoExpediente", pecm.getProcessoExpediente());
        q.setMaxResults(1);
        try {
            return (ProcessoDocumento) q.getSingleResult();
        } catch (NoResultException no) {
            return null;
        }
    }
	
}
