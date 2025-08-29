package br.com.infox.cliente.home;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.component.tree.TipoPessoaPJParteTerceiroTreeHandler;
import br.com.infox.cliente.component.tree.TipoPessoaPJTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.EnderecoHome;
import br.com.infox.ibpm.home.LocalizacaoHome;
import br.com.infox.ibpm.home.UsuarioLocalizacaoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.View;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("pessoaHome")
public class PessoaHome extends AbstractPessoaHome<Pessoa>{

	public static final String PESSOA_LOGADA_VAR = "pessoaLogada";
	public static Logger logger = Logger.getLogger(PessoaHome.class.getCanonicalName());
	public static final String TIPO_JUSTICA = ComponentUtil.getComponent("tipoJustica");

	private static final long serialVersionUID = 1L;

	private String numeroOab;
	private String numeroCpf;
	private String signature;
	private TipoPessoa tipoPessoa;
	
	private Boolean buscaPessoaFisica = new Boolean(true);

	public PessoaHome(){
	}

	public void carregaAbaEntidades(){
		Contexts.removeFromAllContexts("pessoaProcuradorProcuradoriaEntidadeGrid");
		Contexts.removeFromAllContexts("pessoaProcuradorProcuradoriaGrid");
		newInstance();
	}

	public List<Pessoa> pesquisaPessoas(Object valor){
		try{
			PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
			return pessoaService.pesquisaPessoas((String) valor);
		} catch (PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR,
					"Houve um erro ao buscar a pessoa pelo número de inscrição no Ministério da Fazenda: {0}",
					e.getLocalizedMessage());
			// Como o action do suggestionBox só é executado na fase JSF RENDER_RESPONSE,
			// devemos executar este método utilitário para repassar o FacesMessage adiante.
			FacesUtil.refreshFacesMessages();

		}
		return null;
	}

	public List<Localizacao> getLocalizacoes(Pessoa pessoa){
		try {
			UsuarioService usuarioService = ComponentUtil.getComponent(UsuarioService.class);
			return usuarioService.getLocalizacoes(pessoa);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar recuperar as localizações de {0}.", pessoa.getNome());
			return Collections.emptyList();
		}
	}
	
	public List<Localizacao> getLocalizacoes(PessoaFisicaEspecializada o){
		return getLocalizacoes(o.getPessoa());
	}

	@Override
	protected boolean beforePersistOrUpdate(){
		if (getInstance().getNome().length() >= 64){
			String nome = getInstance().getNome().substring(0, 64);
			getInstance().setLogin(nome + UUID.randomUUID());
		}

		return true;
	}

	@Override
	public String persist(){
		if(this.beforePersistOrUpdate()){
			try {
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				Pessoa p = pessoaService.persist(getInstance());
				setInstance(p);		
				return afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}
		
		return null;
	}

	@Override
	public String update(){
		Pessoa p;
		try {
			beforePersistOrUpdate();
			PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
			p = pessoaService.persist(getInstance());
			setInstance(p);			
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Endereço alterado com sucesso.");
			refreshGrid("enderecoGrid");
			getEntityManager().refresh(EnderecoHome.instance().getInstance());
			return afterPersistOrUpdate("persisted");
		} catch (PJeBusinessException e) {
			reportMessage(e);
			return null;			
		}
	}

	public boolean isUsuario(){
		return isManaged() && getInstance().getLogin() != null;
	}

	public void newInstanceUsuarioLocalizacao(){
		UsuarioLocalizacaoHome localizacaoHome = getComponent("usuarioLocalizacaoHome");
		localizacaoHome.newInstance();
		localizacaoHome.getInstance().setUsuario(getInstance());
	}

	@Observer(Identity.EVENT_LOGIN_SUCCESSFUL)
	public void setPessoaLogadaContext(){
		Context session = Contexts.getSessionContext();
		Pessoa pessoa = (Pessoa) session.get("usuarioLogado");
		session.set(PESSOA_LOGADA_VAR, pessoa);

		UsuarioLocalizacao usuarioLocalizacao = (UsuarioLocalizacao) session
				.get(Authenticator.USUARIO_LOCALIZACAO_ATUAL);

		if (usuarioLocalizacao != null && usuarioLocalizacao.getLocalizacaoFisica().getEnderecoCompleto() != null){
			Endereco enderecoCompleto = usuarioLocalizacao.getLocalizacaoFisica().getEnderecoCompleto();
			session.set("usuarioLogadoEndereco", enderecoCompleto);
			session.set("usuarioLogadoCep", enderecoCompleto.getCep());
		}
		if (pessoa != null)
			session.set("pessoaLogadaTipo", pessoa.getTipoPessoa());
	}

	public String gravarMeioContato(){
		MeioContatoHome.instance().getInstance().setUsuarioCadastrador(instance);
		MeioContatoHome.instance().getInstance().setPessoa(instance);
		String output = MeioContatoHome.instance().persist();
		return output;
	}

	public String gravarEndereco(){
		return this.gravarEndereco(true);
	}

	/**
	 * [PJEII-2659] - [PJEII-2766]
	 * 
	 * Incluindo condição para retirar a possibilidade de nullPointer na query que verifica cep duplicado
	 * 
	 * [PJEII-]
	 * Criação da condição para validaçã de campos obrigatórios não preenchidos. 
	 * 
	 * @param ignoraAvisos
	 * @return
	 */
	public String gravarEndereco(boolean ignoraAvisos){
		if (EnderecoHome.instance().getInstance().getNomeLogradouro() == null || EnderecoHome.instance().getInstance()
				.getNomeLogradouro().isEmpty() || EnderecoHome.instance().getInstance().getNomeBairro() == null 
					|| EnderecoHome.instance().getInstance().getNomeBairro().isEmpty()) {
			FacesMessages.instance().clear();
			StringBuilder mensagem = new StringBuilder("Campo Obrigatório não preenchido:");
			if (EnderecoHome.instance().getInstance().getNomeBairro() == null || EnderecoHome.instance().getInstance()
				.getNomeBairro().isEmpty()) {
				mensagem.append(" Bairro");
			}
			if (EnderecoHome.instance().getInstance().getNomeLogradouro() == null || EnderecoHome.instance()
					.getInstance().getNomeLogradouro().isEmpty()) {
				mensagem.append(" e Logradouro");
			}
			mensagem.append(".\n");
			FacesMessages.instance().add(Severity.ERROR, mensagem.toString());
			return "";
		} else {
			EnderecoHome.instance().getInstance().setUsuario(instance);
			if (PreCadastroPessoaBean.instance().getPessoa() == null || PreCadastroPessoaBean.instance().getPessoa()
					.getIdUsuario() == null || PreCadastroPessoaBean.instance().getPessoa().getIdUsuario().equals(0)) {
				PreCadastroPessoaBean.instance().setPessoaAdvogado(new PessoaAdvogado());
				PreCadastroPessoaBean.instance().getPessoa().setIdUsuario(instance.getIdUsuario());
			}
			//Bernardo Gouvea
			//[PJEII-3419] Seleciona a imagem do Radio Buton
			GridQuery gq = ComponentUtil.getComponent("processoParteVinculoPessoaEnderecoGrid");
			gq.setSelectedRow(EnderecoHome.instance().getInstance());
			gq.refresh();
			//[PJEII-3419] Tira a seleção do checkBox 'Endereço Desconhecido' se este estiver marcado.
			ProcessoParteHome pph = ComponentUtil.getComponent("processoParteHome");
			pph.getInstance().setIsEnderecoDesconhecido(false);
			
			return EnderecoHome.instance().persist(ignoraAvisos);
		}
	}

	/*
	 * PJEII-3153
	 * 09/10/2012 
	 * patricknasc
	 * 
	 * description: atualiza uma lista auxiliar de "Outros Nomes" que indicará quais destes
	 * itens deverão exibir o botão "excluir" na grid correspondente.
	 * 
	 * */
	public String gravarPessoaNomeAlternativo(){
		PessoaNomeAlternativoHome.instance().getInstance().setPessoa(instance);
		PessoaNomeAlternativoHome.instance().getInstance().getListaExclusaoOutrosNomes().add(new String(PessoaNomeAlternativoHome.instance().getInstance().toString()));
		String output = PessoaNomeAlternativoHome.instance().persist();
		refreshGrid("pessoaNomeAlternativoGrid");
		refreshGrid("pessoaNomeAlternativoPreGrid");
		return output;
	}

	@Observer(Identity.EVENT_LOGGED_OUT)
	public void clearPessoaLogadaContext() {
		Contexts.removeFromAllContexts(PESSOA_LOGADA_VAR);
		Contexts.removeFromAllContexts("usuarioLogado");
		Contexts.removeFromAllContexts("usuarioLogadoEndereco");
		Contexts.removeFromAllContexts("usuarioLogadoCep");
		Contexts.removeFromAllContexts(Authenticator.USUARIO_LOCALIZACAO_ATUAL);
		Contexts.removeFromAllContexts("pessoaLogadaTipo");
		Contexts.removeFromAllContexts("pessoaNomeAlternativo");
	}

	public void setNumeroOab(String numeroOab){
		this.numeroOab = numeroOab;
	}

	public String getNumeroOab(){
		return numeroOab;
	}

	public void setNumeroCpf(String numeroCpf){
		this.numeroCpf = numeroCpf;
	}

	public String getNumeroCpf(){
		return numeroCpf;
	}

	/**
	 * Atalho para o pessoa logada
	 * 
	 * @return
	 */
	public static Pessoa getPessoaLogada(){
		Pessoa resultado = null;
		Context sessionContext = Contexts.getSessionContext();
		if (sessionContext != null){
			resultado = (Pessoa) Contexts.getSessionContext().get(PESSOA_LOGADA_VAR);
		}
		return resultado;
	}

	public Integer testeTestemunhaGrid(){
		if (Strings.isEmpty(PessoaFisicaHome.instance().getInstance().getNumeroCPF())
			&& Strings.isEmpty(PessoaFisicaHome.instance().getInstance().getNome())
			&& Strings.isEmpty(PessoaFisicaHome.instance().getInstance().getNomeGenitora()))
			return -1;
		else
			return null;
	}

	public static PessoaHome instance(){
		return ComponentUtil.getComponent("pessoaHome");
	}

	/**
	 * Limpa campos de filtragem da tela de consulta de pessoas
	 */
	public void limparFormularioConsultaPessoas(){
		this.getInstance().setNome(null);
		this.clearCpfCnpj();
	}
	
	@Observer("tipoPessoaCadastroParteTreeSelected")
	public void limparCamposTreeSelected(){
		// REMOVIDO - PESSOANEW
		// PessoaFisicaHome.instance().getInstance().setNumeroCPF(null);
		// PessoaJuridicaHome.instance().getInstance().setNumeroCNPJ(null);
	}

	public void limparCamposPessoaFisicaSelected(){
		PessoaHome.instance().getInstance().setTipoPessoa(null);
		TipoPessoaPJTreeHandler tree = getComponent("tipoPessoaPJTree");
		tree.clearTree();
		TipoPessoaPJParteTerceiroTreeHandler treeTerceiro = getComponent("tipoPessoaPJParteTerceiroTree");
		treeTerceiro.clearTree();

		// PessoaJuridicaHome.instance().getInstance().setNumeroCNPJ(null);
		// //REMOVIDO - PESSOANEW
	}

	public void limparCamposPessoaJuridicaSelected(){

		PessoaJuridicaHome.instance().getInstance().setNumeroCNPJ(null);
	}

	/*
	 * public void limparCamposPessoaJuridicaSelected(){ PessoaHome.instance().getInstance().setTipoPessoa(null); TipoPessoaPJTreeHandler tree =
	 * getComponent("tipoPessoaPJTree"); tree.clearTree(); TipoPessoaPJParteTerceiroTreeHandler treeTerceiro =
	 * getComponent("tipoPessoaPJParteTerceiroTree"); treeTerceiro.clearTree(); // PessoaFisicaHome.instance().getInstance().setNumeroCPF(null); //
	 * //REMOVIDO - PESSOANEW PessoaFisicaHome.instance().getInstance().setNumeroCPF(null); }
	 */

	public void setSignature(String signature){
		this.signature = signature;
	}

	public String getSignature(){
		return signature;
	}

	// Se for pessoa Física, retorna true, senão, será consequentemente pessoa
	// Jurídica e irá retornar false
	public boolean verificarPessoaFisicaJuridica(){
		if (getInstance() instanceof PessoaFisica)
			return true;
		return false;
	}
	
	// Se for pessoa Física, retorna true, senão, será consequentemente pessoa
	// Jurídica e irá retornar false
	public View verificarPessoaFisicaJuridicaAutoridade() {
		if (getInstance() instanceof PessoaFisica) {
			return (View) getComponent("pessoaFisicaView");
		}
		else if(getInstance() instanceof PessoaJuridica) {
			return (View) getComponent("pessoaJuridicaView");
		}
		else if(getInstance() instanceof PessoaAutoridade) {
			return (View) getComponent("pessoaAutoridadeView");
		}
		return null;
	}
	
	/**
	 * Método responsável por designar a view que será renderizada
	 * na tela de Consulta Pessoa.
	 * 
	 * @return 
	 * 		{@link View} de acordo com a instância da pessoa selecionada.
	 * 
	 *  @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-20214">PJEII-20214</a>
	 */	
	public View consultarPessoaFisicaJuridicaAutoridadeView() {
		if (getInstance() instanceof PessoaFisica) {
			return (View) getComponent("dadosPessoaFisicaView");
		} else if (getInstance() instanceof PessoaJuridica) {
			return (View) getComponent("dadosPessoaJuridicaView");
		} else if (getInstance() instanceof PessoaAutoridade) {
			return (View) getComponent("dadosPessoaAutoridadeView");
		}
		return null;
	}

	public TipoPessoa getTipoPessoa(){
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa){
		this.tipoPessoa = tipoPessoa;
	}

	public void atualizarNomeLocalizacao(Pessoa pessoa){
		if (pessoa != null && pessoa.getPessoaDocumentoIdentificacaoList() != null && !pessoa.getPessoaDocumentoIdentificacaoList().isEmpty()) {
			if(!verificarExistenciaCpfCnpj(pessoa.getPessoaDocumentoIdentificacaoList())){
				return;
			}			
			EntityManager em = getEntityManager();
			Query query = em.createQuery("select o from UsuarioLocalizacao o where o.localizacaoFisica.localizacao like '%' || :documentoIdentificacao || '%'");
			query.setParameter("documentoIdentificacao", 
					pessoa instanceof PessoaFisica ? ((PessoaFisica)pessoa).getNumeroCPF() : ((PessoaJuridica)pessoa).getNumeroCNPJ());
			
			String nomeLocalizacao = null;
			@SuppressWarnings("unchecked")
			List<UsuarioLocalizacao> usuarioLocalizacoes = query.getResultList();
			if (!usuarioLocalizacoes.isEmpty()) {
				for (UsuarioLocalizacao usuarioLocalizacao : usuarioLocalizacoes) {
					if (usuarioLocalizacao.getPapel().equals(ParametroUtil.instance().getPapelJusPostulandi())) {
						nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoJusPostulandi(pessoa);
					} else if (usuarioLocalizacao.getPapel().equals(ParametroUtil.instance().getPapelProcurador())) {
						if (pessoa instanceof PessoaFisica) {
							nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoProcurador((PessoaFisica) pessoa);
						} else {
							nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoProcurador((PessoaJuridica) pessoa);
						}
					} else {
						nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoPessoa(pessoa);
					}
					usuarioLocalizacao.getLocalizacaoFisica().setLocalizacao(nomeLocalizacao);
					em.merge(usuarioLocalizacao);
				}
				EntityUtil.flush(em);
			}
		}
	}

	private boolean verificarExistenciaCpfCnpj(Set<PessoaDocumentoIdentificacao> docsIdentificacao) {
		boolean possuiCpfCpj = false;
		if (ProjetoUtil.isNotVazio(docsIdentificacao)) {
			for (PessoaDocumentoIdentificacao doc : docsIdentificacao) {
				if (doc.getTipoDocumento().getCodTipo().equals("CPF")
						|| doc.getTipoDocumento().getCodTipo().equals("CPJ")) {
					possuiCpfCpj = true;
					break;
				}
			}
		}
		return possuiCpfCpj;
	}

	/**
	 * Metodo para criar a localização que representa a pessoa
	 * 
	 * @param pessoa
	 * @param papel
	 */
	public void criarLocalizacaoPessoa(Pessoa pessoa, Papel papel){
		EntityManager em = getEntityManager();
		LocalizacaoHome.instance().newInstance();
		Localizacao localizacao = LocalizacaoHome.instance().getInstance();
		localizacao.setLocalizacaoPai(null);
		localizacao.setAtivo(true);

		if (pessoa instanceof PessoaFisica){
			PessoaFisica pessoaFisica = (PessoaFisica) pessoa;
			localizacao.setLocalizacao(LocalizacaoUtil.formataLocalizacaoPessoaFisica(pessoaFisica));
		} else if (pessoa instanceof PessoaJuridica){
			PessoaJuridica pessoaJuridica = (PessoaJuridica) pessoa;
			localizacao.setLocalizacao(LocalizacaoUtil.formataLocalizacaoPessoaJuridica(pessoaJuridica));
		}
		em.persist(localizacao);

		PessoaLocalizacao pl = new PessoaLocalizacao();
		pl.setLocalizacao(localizacao);
		pl.setPessoa(pessoa);
		em.persist(pl);

		UsuarioLocalizacao ul = new UsuarioLocalizacao();
		ul.setLocalizacaoFisica(localizacao);
		ul.setUsuario(pessoa);
		ul.setResponsavelLocalizacao(false);
		ul.setPapel(papel);
		em.persist(ul);
	}

	public boolean isMagistrado(){
		Pessoa pessoa = (Pessoa) Contexts.getSessionContext().get("usuarioLogado");
		return Pessoa.instanceOf(pessoa, PessoaMagistrado.class);
	}

	public boolean isServidor(){
		Pessoa pessoa = (Pessoa) Contexts.getSessionContext().get("usuarioLogado");
		return Pessoa.instanceOf(pessoa, PessoaServidor.class);
	}
	
	public boolean isUsuarioExterno(Usuario usuario) {
		boolean retorno = false;
		Criteria criteria = HibernateUtil.getSession().createCriteria(UsuarioLocalizacao.class);
		criteria.add(Restrictions.eq("usuario", usuario));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		UsuarioLocalizacao usuarioLocalizacao = (UsuarioLocalizacao) criteria.uniqueResult();
		
		if(usuarioLocalizacao != null) {
			retorno = isUsuarioExterno(usuarioLocalizacao.getPapel());
		}
		
		return retorno;
	}
	
	public boolean isUsuarioExterno(Papel papel) {
		return Authenticator.isUsuarioExterno(papel);
	}
	
	public boolean isProcurador(Papel papel) {
		return Authenticator.isProcurador();
	}

	public boolean estaoMesmoPolo(ProcessoTrf processo, Pessoa p1, Pessoa p2){
		ProcessoParte pp1 = null;
		ProcessoParte pp2 = null;
		for (ProcessoParte pp : processo.getProcessoParteList()){
			if (pp.getPessoa().equals(p1)){
				pp1 = pp;
			}
			if (pp.getPessoa().equals(p2)){
				pp2 = pp;
			}
		}
		if (pp1 == null || pp2 == null){
			return false;
		}
		else if (pp1.getInParticipacao().equals(pp2.getInParticipacao())){
			return true;
		}
		return false;
	}

	public Boolean getBuscaPessoaFisica(){
		return buscaPessoaFisica;
	}

	public void setBuscaPessoaFisica(Boolean buscaPessoaFisica){
		this.buscaPessoaFisica = buscaPessoaFisica;
	}

	public void clearCpfCnpj(){
		this.limparCamposPessoaFisicaSelected();
		PessoaFisicaHome.instance().getInstance().setNumeroCPF(null);

		this.limparCamposPessoaJuridicaSelected();
	}

	public void onClickSearchTab(AbstractHome<?> home){
		PessoaDocumentoIdentificacaoHome.instance().newInstance();
		EnderecoHome.instance().newInstance();
		MeioContatoHome.instance().newInstance();
		PessoaNomeAlternativoHome.instance().newInstance();
		home.onClickSearchTab();
	}

	public void limparConsultaPessoa(){
		PessoaFisicaHome.instance().getInstance().setNumeroCPF("");
		PessoaJuridicaHome.instance().getInstance().setNumeroCNPJ("");
	}
}
