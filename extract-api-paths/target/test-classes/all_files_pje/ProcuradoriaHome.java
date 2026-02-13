package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.LocalizacaoHome;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name(ProcuradoriaHome.NAME)
@BypassInterceptors
public class ProcuradoriaHome extends AbstractProcuradoriaHome<Procuradoria> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "procuradoriaHome";
	private static final LogProvider log = Logging.getLogProvider(ProcuradoriaHome.class);
	private String cnpjPesquisa;
	private PessoaJuridica orgaoVinculadoPesquisa;
	private PessoaJuridica orgaoVinculacao;
	
	private String tipoProcuradoria;

	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	private PessoaJuridicaManager pessoaJuridicaManager;
	
	private ProcuradoriaManager procuradoriaManager;

	public static ProcuradoriaHome instance() {
		return ComponentUtil.getComponent(ProcuradoriaHome.NAME);
	}

	@Override
	public String persist() {		

			if(getInstance().getTipo() == null){
				instance.setTipo(this.getTipoProcuradoria());
			}

			// [PJEII-3965] Correção de erro no cadastro de Procuradoria no 1º grau (fernando.junior - 19/11/2012) 
			if (instance.getAcompanhaSessao() == null) {
				instance.setAcompanhaSessao(false);
			}
			
			String ret = super.persist();
			refreshGrid("procuradoriaGrid");
			refreshGrid("defensoriaGrid");
			
			return ret;
	}
	
	public void persistir(){
		persist();
	}

	public Localizacao obterLocalizacao(String textoLocalizacao) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(Localizacao.class);
		criteria.add(Restrictions.eq("localizacao", textoLocalizacao));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Localizacao localizacao = (Localizacao)criteria.uniqueResult();
		if (localizacao == null) {
			localizacao = new Localizacao();
			localizacao.setLocalizacao(textoLocalizacao);
			localizacao.setAtivo(true);
			localizacao.setEstrutura(true);

			LocalizacaoHome.instance().setInstance(localizacao);
			LocalizacaoHome.instance().persist();

		}
		return localizacao;
	}

	public Procuradoria obterProcuradoria(int id) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(Procuradoria.class);
		criteria.add(Restrictions.eq("idProcuradoria", id));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (Procuradoria) criteria.uniqueResult();
	}

	public void removerLocalizacao() {
		EntityManager em = getEntityManager();
		em.remove(obterLocalizacao(LocalizacaoUtil.formataLocalizacaoProcuradoria(getInstance())));
	}

	@Override
	public String remove() {
		removerLocalizacao();
		return super.remove();
	}

	@Override
	public String update() {
		getEntityManager().merge(getInstance());
		
		return super.update();
	}

	public void atualizar(){
		update();
	}	

	/**
	 * Refactor ResultList.
	 * 
	 * @param idProcuradoria
	 * @return Localizacao
	 */
	public Localizacao pegarLocalizacaoProcuradoria(int idProcuradoria) {
		StringBuilder sql = new StringBuilder(" select o from Localizacao o ");
		sql.append(" where o in (select p.localizacao ");
		sql.append(" from Procuradoria p where p.idProcuradoria = :idProcuradoria)");
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("idProcuradoria", idProcuradoria);
		q.setMaxResults(1);
		try {
			Localizacao resultado = (Localizacao)q.getSingleResult();
			return resultado;
		} catch (NoResultException no) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void verificarLocalizacaoProcuradoria() {
		ArrayList<String> listaProcuradoria = new ArrayList<String>(0);
		String sqlListaProcuradoria = "select o.nome from Procuradoria o";
		Query q = getEntityManager().createQuery(sqlListaProcuradoria);

		for (String string : (ArrayList<String>) q.getResultList()) {
			string = "Procuradoria - " + string;
			listaProcuradoria.add(string);
		}

		for (String string : listaProcuradoria) {
			String sqlLocalizacao = "select o from Localizacao o " + "where o.localizacao = :string";
			Query ql = getEntityManager().createQuery(sqlLocalizacao);
			ql.setParameter("string", string);

			if (ql.getResultList().size() == 0) {
				LocalizacaoHome.instance().newInstance();
				Localizacao localizacao = LocalizacaoHome.instance().getInstance();
				localizacao.setLocalizacaoPai(null);
				localizacao.setAtivo(true);
				localizacao.setLocalizacao(string);

				getEntityManager().persist(localizacao);
				EntityUtil.flush();
			}
		}
	}

	@Override
	public String inactive(Procuradoria procuradoria) {
		setId(procuradoria.getIdProcuradoria());

		if (podeExcluir(procuradoria)) {
			super.inactive(procuradoria);
		}
		
		return "update";
	}
	
	private boolean podeExcluir(Procuradoria procuradoria) {
		final String msg = "Não é possível inativar este órgão de representação. ";

		if (existeProcuradoresAtivos(procuradoria)) {
			FacesMessages.instance().add(Severity.INFO, msg + "Existe um ou mais representantes ativos.");
			return false;
		}
		
		if(existeExpedientesSemRespostaEnviadosViaSistema(procuradoria)){
			FacesMessages.instance().add(Severity.INFO, msg + "Existe um ou mais expedientes pendentes de resposta enviados via sistema (Expedição eletrônica).");
			return false;
		}
		
		return true;
	}
	
	private boolean existeProcuradoresAtivos(Procuradoria procuradoria) {
		List<PessoaProcuradoria> pessoaProcuradoriaList = getPessoaProcuradoriaManager().recuperaPessoaProcuradoria(procuradoria); 
		
		for(PessoaProcuradoria pessoaProcuradoria : pessoaProcuradoriaList) {
			if(pessoaProcuradoria.getPessoa().getProcuradorAtivo() != null
				&& pessoaProcuradoria.getPessoa().getProcuradorAtivo()) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean existeExpedientesSemRespostaEnviadosViaSistema(Procuradoria procuradoria) {
		ProcessoParteExpedienteManager processoParteExpedienteManager = ComponentUtil.getComponent("processoParteExpedienteManager");
		List<ProcessoParteExpediente> processoParteExpedienteList = processoParteExpedienteManager.getExpedientesSemRespostaEnviadoViaSistema(procuradoria);
		return processoParteExpedienteList != null && !processoParteExpedienteList.isEmpty();
	}
	
	public TipoProcuradoriaEnum[] getTipoProcuradoriaValues() {
		Localizacao localizacaoAtual = Authenticator.getLocalizacaoAtual();
		Integer usuarioLogado = Authenticator.getUsuarioLogado().getIdUsuario();
		
		Procuradoria procuradoriaAtual = getProcuradoriaManager().recuperaPorLocalizacao(localizacaoAtual);
		
		if(procuradoriaAtual != null && usuarioLogado != null) {
			return new TipoProcuradoriaEnum[]{procuradoriaAtual.getTipo()};
		}
		return TipoProcuradoriaEnum.values();
	}

	public String getCnpjPesquisa() {
		return cnpjPesquisa;
	}
	
	public void setCnpjPesquisa(String cnpjPesquisa) {
		this.cnpjPesquisa = cnpjPesquisa;
	}
	
	/**
	 * metodo responsavel por limpar o campo de pesquisa do CNPJ do orgao de vinculacao para pesquisa
	 */
	private void limparCampoPesquisaCNPJOrgaoVinculadoPesquisa() {
		cnpjPesquisa = null;		
	}

	/**
	 * metodo responsavel por limpar o campo de pesquisa da pessoa juridica do orgao de vinculacao para pesquisa
	 */
	private void limparCampoPesquisaOrgaoVinculadoPesquisa() {
		orgaoVinculadoPesquisa = null;		
	}	

	/**
	 * Metodo responsável para limpar os campos do orgao de vinculacao
	 */
	public void limparPesquisaOrgaoVinculacao(){
		limparCampoPesquisaOrgaoVinculadoPesquisa();
		limparCampoPesquisaCNPJOrgaoVinculadoPesquisa();
		setOrgaoVinculacao(null);
		getInstance().setPessoaJuridica(null);
	}

	
	public PessoaJuridica getOrgaoVinculadoPesquisa() {
		return orgaoVinculadoPesquisa;
	}

	public void setOrgaoVinculadoPesquisa(PessoaJuridica orgaoVinculadoPesquisa) {
		this.orgaoVinculadoPesquisa = orgaoVinculadoPesquisa;
	}	
	

	/**
	 * Método responsável pesquisar o órgao de de vinculação pelo CNPJ fornecido. Caso o órgão tenha sido recuperado do suggest, o método apenas 
	 * atribui os dados a partir do objeto recuperado do suggest
	 * 
	 */
	public void pesquisarOrgaoVinculacao(){
		PessoaJuridica pessoaJuridica = null;
		if(verificaPreenchimentoCamposPesquisa()) {
			if( getCnpjPesquisa() != null ) {
				pessoaJuridica = getPessoaJuridicaManager().findByCNPJ(getCnpjPesquisa());
				
				if( pessoaJuridica == null ) {
					if(Authenticator.isLogouComCertificado()) {
						String nrCnpjMatriz = InscricaoMFUtil.getCNPJBase(InscricaoMFUtil.retiraMascara(getCnpjPesquisa()).substring(0, 8));
					
						if((InscricaoMFUtil.retiraMascara(getCnpjPesquisa())).equals(nrCnpjMatriz)){
							// O CNPJ informado é o da matriz
							try{
								pessoaJuridica = getPessoaJuridicaManager().recuperaPessoaJuridicaPelaReceita(getCnpjPesquisa(), null);
							} catch(Exception e ) {
								FacesMessages.instance().clear();
								FacesMessages.instance().add(
										StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "preCadastroPessoaBean.pessoa_juridica_nao_encontrada"));
								return;
							}					
						} else {
							// O CNPJ informado é o de uma filial
							PessoaJuridica matriz = null;
							try{
								matriz = getPessoaJuridicaManager().recuperaPessoaJuridicaPelaReceita(nrCnpjMatriz, null);
							} catch(Exception e ) {
								FacesMessages.instance().clear();
								FacesMessages.instance().add(
										StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "preCadastroPessoaBean.pessoa_juridica_matriz_nao_encontrada"));
								return;
							}
							if( matriz != null ) {	
								try{
									pessoaJuridica = getPessoaJuridicaManager().recuperaPessoaJuridicaPelaReceita(getCnpjPesquisa(), matriz);
								} catch(Exception e ) {
									FacesMessages.instance().clear();
									FacesMessages.instance().add(
											StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "preCadastroPessoaBean.pessoa_juridica_nao_encontrada"));
									return;
								}
							}	
						}
					}else {
						FacesMessages.instance().clear();
						FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível realizar a consulta pelo CNPJ {0}. Esta funcionalidade é permitida apenas para usuários que tenham realizado o acesso ao sistema com certificado digital", getCnpjPesquisa());
					}
				}
			} else {
				pessoaJuridica = getOrgaoVinculadoPesquisa();
			}
			
			if(pessoaJuridica != null){
				if(validaPessoaJuridicaVinculada(pessoaJuridica)){
					getInstance().setPessoaJuridica(pessoaJuridica);
					setOrgaoVinculacao(pessoaJuridica);			
				}
			}else{
				getInstance().setPessoaJuridica(null);
				setOrgaoVinculacao(null);
			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Forneça um nome ou um CNPJ");
		}
	}	
	
	public PessoaJuridica getOrgaoVinculacao() {
		return orgaoVinculacao;
	}
	
	public void setOrgaoVinculacao(PessoaJuridica orgaoVinculacao) {
		this.orgaoVinculacao = orgaoVinculacao;
	}	

	/**
	 * metodo responsavel por verificar se os campos de pesquisa estao preenchidos
	 * @return true se pelo menos um dos campos estiver preenchido / false
	 */
	private boolean verificaPreenchimentoCamposPesquisa() {
		boolean retorno = true;
		if((getCnpjPesquisa() == null) && (getOrgaoVinculadoPesquisa() == null)) {
			retorno = false;
		}
		return retorno;
	}

	/**
	 * Retorna o Enum do tipo de órgão de representação (Procuradoria ou Defensoria)
	 * 
	 * @return TipoProcuradoriaEnum
	 */
	public TipoProcuradoriaEnum getTipoProcuradoria() {
		for(TipoProcuradoriaEnum tipo : TipoProcuradoriaEnum.values()){
		  if(tipoProcuradoria.equals(tipo.getLabel())){
			  return tipo;
		  }
		}
		return null;		 
	}

	/**
	 * Método utilizado para definir a partir da view listView.page.xml da procuradora e defensoria
	 * qual o tipo do órgão de representação 
	 * 
	 * @param tipoProcuradoria
	 */
	public void setTipoProcuradoria(String tipoProcuradoria) {
		this.tipoProcuradoria = tipoProcuradoria;
	}
	
	private Boolean validaPessoaJuridicaVinculada(PessoaJuridica pessoaJuridica){
		Procuradoria orgaoRepresentacao = getProcuradoriaManager().findByPessoaJuridica(pessoaJuridica);
		
		if (orgaoRepresentacao != null){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Não é possível associar esta Pessoa Jurídica!");
			FacesMessages.instance().add(Severity.INFO, "O CNPJ {0} encontra-se vinculado ao órgão de representação {1}.", pessoaJuridica.getDocumentoCpfCnpj(), orgaoRepresentacao.getNome());
			return false;
		}
		
		return true;
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		if (getInstance().getLocalizacao() == null) {
			getInstance().setLocalizacao(obterLocalizacao(LocalizacaoUtil.formataLocalizacaoProcuradoria(getInstance())));
		} else {
            atualizarNomeLocalizacao(getInstance());
        }
		if (getInstance().getAtivo()) {
			if (!getInstance().getLocalizacao().getAtivo()) {
				getInstance().getLocalizacao().setAtivo(true);
			}
		} else {
			if (podeExcluir(getInstance())) {
				getInstance().getLocalizacao().setAtivo(getInstance().getAtivo());
			} else {
				getInstance().setAtivo(true);
			}
		}
		if (getInstance().getPessoaJuridica() != null) {
			getInstance().getPessoaJuridica().setAssociarPapelParaRemessa(true);
		}
		return true;
	}

	/**
	 * @return pessoaProcuradoriaManager.
	 */
	public PessoaProcuradoriaManager getPessoaProcuradoriaManager() {
		if (pessoaProcuradoriaManager == null) {
			pessoaProcuradoriaManager = ComponentUtil.getComponent(PessoaProcuradoriaManager.class);
		}
		return pessoaProcuradoriaManager;
	}

	/**
	 * @return pessoaJuridicaManager.
	 */
	public PessoaJuridicaManager getPessoaJuridicaManager() {
		if (pessoaJuridicaManager == null) {
			pessoaJuridicaManager = ComponentUtil.getComponent(PessoaJuridicaManager.class);
		}
		return pessoaJuridicaManager;
	}

	/**
	 * @return procuradoriaManager.
	 */
	public ProcuradoriaManager getProcuradoriaManager() {
		if (procuradoriaManager == null) {
			procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
		}
		return procuradoriaManager;
	}
	
	/**
	 * Método responsável por atualizar o nome da localização da procuradoria especificada.
	 * @param procuradoria Procuradoria
	 */
    private void atualizarNomeLocalizacao(Procuradoria procuradoria) {
        LocalizacaoManager localizacaoManager = ComponentUtil.getComponent(LocalizacaoManager.class);
        Localizacao localizacao = localizacaoManager.getLocalizacaoExistente(LocalizacaoUtil.formataLocalizacaoProcuradoria(procuradoria));
        if (localizacao == null) {
            localizacao = procuradoria.getLocalizacao();
            localizacao.setLocalizacao(LocalizacaoUtil.formataLocalizacaoProcuradoria(procuradoria));
            try {
                localizacaoManager.persistAndFlush(localizacao);
            } catch (PJeBusinessException e) {
            	log.error(e.getLocalizedMessage());
            }
        }
    }
    
    @Override
	protected String afterPersistOrUpdate(String ret) {
    	UsuarioLocalizacaoManager manager = UsuarioLocalizacaoManager.instance();
    	manager.associarLocalizacaoParaRemessa(getInstance(), true);

		return ret;
	}
}