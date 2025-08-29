package br.com.infox.cliente.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.component.suggest.PessoaAutoridadeSuggestBean;
import br.com.infox.cliente.component.suggest.ProfissaoSuggestBean;
import br.com.infox.cliente.exception.AdvogadoNaoEncontradoException;
import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.cliente.home.PessoaAutoridadeHome;
import br.com.infox.cliente.home.PessoaDocumentoIdentificacaoHome;
import br.com.infox.cliente.home.PessoaFisicaHome;
import br.com.infox.cliente.home.PessoaHome;
import br.com.infox.cliente.home.PessoaJuridicaHome;
import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.RamoAtividadeHome;
import br.com.infox.cliente.home.TipoDocumentoIdentificacaoHome;
import br.com.infox.cliente.home.TipoPessoaHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.OrgaoPublicoList;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.infox.trf.webservice.ConsultaClienteWebService;
import br.com.infox.trf.webservice.WebserviceReceitaException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteConfiguracaoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Pais;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.RamoAtividade;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;
import br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaFisicaReceita;
import br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaJuridicaReceita;

@Name("preCadastroPessoaBean")
@Scope(ScopeType.CONVERSATION)
public class PreCadastroPessoaBean implements Serializable{

	private static final long serialVersionUID = 5023230382793295441L;
	private static final String tipoInscricaoSuplementar = "SUPLEMENTAR";
	private static final String tipoInscricaoEstagiario = "ESTAGIARIO";
	
	@Logger
	private Log logger;

	@RequestParameter
	private String tipoPessoa;

	@RequestParameter
	private String isBrasileiro;

	private PessoaFisica pessoaFisica;
	private PessoaJuridica pessoaJuridica;
	private PessoaAutoridade pessoaAutoridade;
	private PessoaAdvogado pessoaAdvogado;

	private String strPessoaFisicaEspecializada = "br.jus.pje.nucleo.entidades.PessoaFisica";
	private TipoPessoaEnum inTipoPessoa = TipoPessoaEnum.F;
	private Boolean brasileiro = true;
	private String nrDocumentoPrincipal;
	private String nomePessoaJuridica; // Utilizado para pesquisar o nome do órgão público
	private PessoaDocumentoIdentificacao documentoAlternativo;
	private String nomeAlcunhaPessoaSuja; // Pessoa sem documentos de nenhum tipo

	private Boolean isPessoaNaoIndividualizada = false;
	private Boolean hasDocumentoAlternativo;
	private Boolean exibeBotaoCriarAutoridade = false;

	private Boolean hasPessoaFisica = Boolean.FALSE;

	private Boolean hasPessoaJuridica = Boolean.FALSE;

	@SuppressWarnings("unused")
	private Boolean hasAdvogado = Boolean.FALSE;

	@SuppressWarnings("unused")
	private Boolean hasPessoaAutoridade = Boolean.FALSE;

	private Date dtNascimentoAbertura;

	private Boolean pessoaEncontradaBanco = Boolean.FALSE;
	private Boolean pessoaEncontradaReceita = Boolean.FALSE;
	private String destino;
	private String destinoInterno;

	private Boolean isConfirmado = Boolean.FALSE;
	private Boolean isOrgaoPublico = Boolean.FALSE;
	private Boolean isConciliador = Boolean.FALSE;
	private Boolean isPartes = Boolean.FALSE;

	private Boolean ocorreuErroWsReceita = false;
	private Boolean confirmouCadastroErroWsReceita = false;
	private String nomePessoaErroWsReceita;
	private Boolean liberaLocalizacao = false;
	
	private Pais pais;
	
	private Boolean permiteEdicaoDocumentosPrincipais = true;
	
	private List<TipoPessoaEnum> tipoPessoaItemsCache = new ArrayList<TipoPessoaEnum>();
	
	//incluído para permitir a consulta de advogados por número da ordem
	private String nrOAB;
	private Estado ufOAB;
	
	//Parâmetro para mostrar ou não as informações da procuradoria
	private Boolean procInformacao = false;
	
	private Object orgaoPubSelec;
	private Boolean informarNomeSocial = Boolean.FALSE;
	
	/**
	 * Utilizado em cadastroPartePessoaMeioContato para identificar se este componente de visão tem 
	 * uma instância de pessoa sendo manipulada.
	 * 
	 * @return true, se houver pessoa física, jurídica ou autoridade sendo manipulada pelo componente.
	 */
	@Deprecated
	public boolean isExistePessoa(){
		return getHasPessoaFisica() || getHasAdvogado() || getHasPessoaJuridica() || getHasPessoaAutoridade();
	}

	/**
	 * Recupera o parâmetro de request do contexto JSF.
	 * 
	 * @param nomeParametro o nome do parâmetro a ser recuperado
	 * @return o valor, como String, do parâmetro, ou nulo se ele estiver vazio.
	 */
	private String getParametro(String nomeParametro){

		String param = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter(nomeParametro);

		if (param == null || param.equalsIgnoreCase("")){
			return null;
		}

		return param;
	}

	/**
	 * Indica se este componente é utilizado para inclusão de partes em processo judicial.
	 * 
	 * @return true, se o cadastro tratado for de parte em um processo judicial.
	 */
	public Boolean getIsPartes(){
		return isPartes;
	}

	/**
	 * Permite indicar que este componente será utilizado para inclusão de partes em um processo.
	 * 
	 * @param isPartes a marca quanto à utilização para cadastro de parte.
	 */
	public void setIsPartes(Boolean isPartes){
		this.isPartes = isPartes;
	}

	@SuppressWarnings("unchecked")
	private Pessoa persistirPessoa(Pessoa pessoa){
		if (inTipoPessoa == TipoPessoaEnum.F){
			if (pessoa.getTipoPessoa() == null){
				pessoa.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
			}

			if (Pessoa.instanceOf(pessoa, PessoaAdvogado.class)){
				pessoa.setTipoPessoa(ParametroUtil.instance().getTipoAdvogado());
			}

		} else{
			if (pessoa.getTipoPessoa() == null){
				pessoa.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
			}
		}

		if (Authenticator.getOrgaoJulgadorAtual() != null){
			pessoa.setOrgaoJulgadorInclusao(Authenticator.getOrgaoJulgadorAtual());
		}
		if (inTipoPessoa == TipoPessoaEnum.F && getParametro("tipoEspecializado") != null){
			Class<?> tipoEspecializado;
			try{
				tipoEspecializado = Class.forName(getParametro("tipoEspecializado"));
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				pessoaFisica = (PessoaFisica) pessoaService.especializa(pessoaFisica, (Class<PessoaFisicaEspecializada>) tipoEspecializado);
				pessoa = pessoaFisica;
			} catch (ClassNotFoundException e){
				throw new RuntimeException(e);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				return pessoaFisica;
			}
		}

		if (isConciliador && pessoa instanceof PessoaFisica){
			PessoaFisicaHome home = PessoaFisicaHome.instance();
			home.setInstance((PessoaFisica) pessoa);
			home.persistConciliador();
			home.newInstance();
		}else if (pessoa instanceof PessoaAutoridade){
			PessoaAutoridadeHome home = PessoaAutoridadeHome.instance();

			pessoa.setLogin(UUID.randomUUID() + "");
			pessoa.setAtivo(true);
			pessoa.setBloqueio(false);
			pessoa.setProvisorio(false);

			home.setInstance((PessoaAutoridade) pessoa);
			home.persist();
			home.newInstance();

		}else if (isConciliador){
			PessoaFisicaHome home = PessoaFisicaHome.instance();
			home.setInstance((PessoaFisica) pessoa);
			home.persistConciliador();
			home.newInstance();
		}else{
			PessoaHome pHome = PessoaHome.instance();
			pHome.setInstance(pessoa);
			pHome.persist();
		}
		return pessoa;

	}

	private void redirectPessoa(Pessoa pessoa){
		destino = getParametro("destino");

		isConfirmado = Boolean.TRUE;
		if(pessoa != null) {
			if (pessoa.getInTipoPessoa() == TipoPessoaEnum.F && !this.getHasAdvogado()){
				this.pessoaFisica = (PessoaFisica) pessoa;
			} else if (pessoa.getInTipoPessoa() == TipoPessoaEnum.J){
				this.pessoaJuridica = (PessoaJuridica) pessoa;
			} else if (pessoa.getInTipoPessoa() == TipoPessoaEnum.A){
				this.pessoaAutoridade = (PessoaAutoridade) pessoa;
			}

			if (destino != null && !destino.equals("")){

				Redirect redirect = Redirect.instance();
				redirect.setViewId(destino);
				redirect.setParameter("id", pessoa.getIdUsuario());
				redirect.setParameter("tab", "form");
				redirect.setConversationPropagationEnabled(false);
				redirect.execute();

			}
			else{
				if (pessoa.getInTipoPessoa() == TipoPessoaEnum.F && !this.getHasAdvogado()){
					PessoaFisicaHome.instance().setInstance((PessoaFisica) pessoa);
				}
				else if (pessoa.getInTipoPessoa() == TipoPessoaEnum.F && this.getHasAdvogado()){
					PessoaAdvogadoHome.instance().setInstance(((PessoaFisica) pessoa).getPessoaAdvogado());
				}
				else if (pessoa.getInTipoPessoa() == TipoPessoaEnum.J){
					PessoaJuridicaHome.instance().setInstance((PessoaJuridica) pessoa);
				}
				else if ((pessoa.getInTipoPessoa() == TipoPessoaEnum.A) || getHasPessoaAutoridade()){
					PessoaAutoridadeHome.instance().setInstance((PessoaAutoridade) pessoa);
					PessoaJuridicaHome.instance().setInstance(null);
					PessoaAdvogadoHome.instance().setInstance(null);
					PessoaFisicaHome.instance().setInstance(null);
				}
				Contexts.removeFromAllContexts("pessoaDocumentoIdentificacaoPreCadastroGrid");
				Contexts.removeFromAllContexts("processoParteVinculoPessoaEnderecoGrid");
				Contexts.removeFromAllContexts("processoParteVinculoPessoaMeioContatoGrid");
			}
		}

	}

	private void adicionarMensagemErro(String mensagem){
		this.adicionarMensagemErro("errosPreCadastro", mensagem);
	}

	private void adicionarMensagemErro(String campo, String mensagem){
		StatusMessages.instance().addToControlFromResourceBundle(campo, Severity.ERROR, mensagem);				
	}
	
	private void preencherPessoaFisicaPelaReceita(DadosReceitaPessoaFisica dadosReceita){
		pessoaFisica = new PessoaFisica();
		pessoaFisica.setNome(dadosReceita.getNome());
		pessoaFisica.setDataNascimento(dadosReceita.getDataNascimento());
		pessoaFisica.setSexo(dadosReceita.getSexo().equals("1") ? SexoEnum.M : SexoEnum.F);
		pessoaFisica.setNomeGenitora(dadosReceita.getNomeMae());

		// Documentos de identificacao
		pessoaFisica.setNumeroCPF(adicionarMascaraCPF(dadosReceita.getNumCPF()));
		pessoaFisica.setDataCPF(dadosReceita.getDataAtualizacao());
		pessoaFisica.setNumeroTituloEleitor(dadosReceita.getNumTituloEleitor());

		
		// Endereco
		Criteria criteria = HibernateUtil.getSession().createCriteria(Cep.class);
		criteria.add(Restrictions.eq("numeroCep", this.formatarCEP(dadosReceita.getNumCEP())));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Cep cep = (Cep)criteria.uniqueResult();
		if (cep != null){
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setNomeBairro(dadosReceita.getBairro());
			endereco.setNomeLogradouro(dadosReceita.getLogradouro());
			endereco.setNumeroEndereco(dadosReceita.getNumLogradouro());
			endereco.setComplemento(dadosReceita.getComplemento());
			endereco.setUsuario(pessoaFisica);

			pessoaFisica.getEnderecoList().add(endereco);
		}

		/* DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA */
		pessoaFisica.setLogin(InscricaoMFUtil.retiraMascara(pessoaFisica.getNumeroCPF()));
		pessoaFisica.setAtivo(true);
		pessoaFisica.setBloqueio(false);
		pessoaFisica.setProvisorio(false);

	}

	private String calcularCnpjBase(String raizCnpj){
		if (raizCnpj.length() != 8){
			throw new IllegalArgumentException("Raiz do CNPJ com número de dígitos diferente de 8.");
		}
		String cnpj = raizCnpj + "0001";
		int soma = 0;
		for (int i = 0; i < 4; i++){
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (5 - i);
		}
		for (int i = 4; i < 12; i++){
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (13 - i);
		}
		int dv1 = 11 - (soma % 11);
		if (dv1 >= 10){
			dv1 = 0;
		}
		soma = 0;
		for (int i = 0; i < 5; i++){
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (6 - i);
		}
		for (int i = 5; i < 12; i++){
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (14 - i);
		}
		soma = soma + dv1 * 2;
		int dv2 = 11 - (soma % 11);
		if (dv2 >= 10){
			dv2 = 0;
		}
		String ret = cnpj + Integer.toString(dv1) + Integer.toString(dv2);
		return InscricaoMFUtil.mascaraCnpj(ret);
	}

	private void preencherPessoaJuridicaPelaReceita(DadosReceitaPessoaJuridica dadosReceita){
		preencherPessoaJuridicaPelaReceita(dadosReceita, null);
	}
	
	private void preencherPessoaJuridicaPelaReceita(DadosReceitaPessoaJuridica dadosReceita, PessoaJuridica matriz){
		pessoaJuridica = new PessoaJuridica();

		// Documentos de identificacao
		pessoaJuridica.setNome(dadosReceita.getRazaoSocial());
		pessoaJuridica.setNomeFantasia(dadosReceita.getNomeFantasia());
		pessoaJuridica.setDataAbertura(dadosReceita.getDataRegistro());
		pessoaJuridica.setNumeroCNPJ(InscricaoMFUtil.mascaraCnpj(this.nrDocumentoPrincipal));
		pessoaJuridica.setNumeroCpfResponsavel(dadosReceita.getNumCpfResponsavel());
		pessoaJuridica.setPessoaJuridicaMatriz(matriz);
		
		if(matriz != null){
			pessoaJuridica.setMatriz(Boolean.FALSE);
		} else {
			pessoaJuridica.setMatriz(Boolean.TRUE);
		}

		// Endereco
		Criteria criteria = HibernateUtil.getSession().createCriteria(Cep.class);
		criteria.add(Restrictions.eq("numeroCep", formatarCEP(dadosReceita.getNumCep())));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Cep cep = (Cep)criteria.uniqueResult();
		
		if (cep != null){
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setNomeBairro(dadosReceita.getDescricaoBairro());
			endereco.setNomeLogradouro(dadosReceita.getDescricaoLogradouro());
			endereco.setNumeroEndereco(dadosReceita.getNumLogradouro());
			endereco.setComplemento(dadosReceita.getDescricaoComplemento());
			endereco.setUsuario(pessoaJuridica);

			pessoaJuridica.getEnderecoList().add(endereco);
		}

		/* DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA */
		pessoaJuridica.setLogin(InscricaoMFUtil.retiraMascara(pessoaJuridica.getDocumentoCpfCnpj()));
		pessoaJuridica.setAtivo(true);
		pessoaJuridica.setBloqueio(false);
		pessoaJuridica.setProvisorio(false);
		
		TipoPessoa tipoPessoa = TipoPessoaHome.instance().buscarPorCodigo(dadosReceita.getCodigoNaturezaJuridica());
		if(tipoPessoa != null) {
			pessoaJuridica.setTipoPessoa(tipoPessoa);
			TipoPessoa tmpTP = tipoPessoa;
			do{
				if(tmpTP.getCodTipoPessoa() != null && tmpTP.getCodTipoPessoa().equals("ADMP")){
					pessoaJuridica.setOrgaoPublico(true);
					break;
				}
			}while((tmpTP = tmpTP.getTipoPessoaSuperior()) != null);
		}
		else {
			pessoaJuridica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
		}
		
		RamoAtividade ramoAtividade = RamoAtividadeHome.instance().buscarPorCodigo(dadosReceita.getCodigoCnaeFiscal());
		pessoaJuridica.setRamoAtividade(ramoAtividade);
	}
	
	private PessoaJuridica montaPessoaJuridicaMatriz(DadosReceitaPessoaJuridica dadosReceita){
		
		PessoaJuridica matriz = new PessoaJuridica();

		// Documentos de identificacao
		matriz.setNome(dadosReceita.getRazaoSocial());
		matriz.setNomeFantasia(dadosReceita.getNomeFantasia());
		matriz.setDataAbertura(dadosReceita.getDataRegistro());
		matriz.setNumeroCNPJ(InscricaoMFUtil.mascaraCnpj(dadosReceita.getNumCNPJ()));
		matriz.setNumeroCpfResponsavel(dadosReceita.getNumCpfResponsavel());
		matriz.setMatriz(Boolean.TRUE);

		// Endereco
		Criteria criteria = HibernateUtil.getSession().createCriteria(Cep.class);
		criteria.add(Restrictions.eq("numeroCep", formatarCEP(dadosReceita.getNumCep())));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Cep cep = (Cep)criteria.uniqueResult();
		
		if (cep != null){
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setNomeBairro(dadosReceita.getDescricaoBairro());
			endereco.setNomeLogradouro(dadosReceita.getDescricaoLogradouro());
			endereco.setNumeroEndereco(dadosReceita.getNumLogradouro());
			endereco.setComplemento(dadosReceita.getDescricaoComplemento());
			endereco.setUsuario(matriz);

			matriz.getEnderecoList().add(endereco);
		}
		
		/* DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA */
		matriz.setLogin(InscricaoMFUtil.retiraMascara(matriz.getDocumentoCpfCnpj()));
		matriz.setAtivo(true);
		matriz.setStatusSenha(StatusSenhaEnum.I);
		matriz.setBloqueio(false);
		matriz.setProvisorio(false);
		matriz.setInTipoPessoa(TipoPessoaEnum.J);
		
		TipoPessoa tipoPessoa = TipoPessoaHome.instance().buscarPorCodigo(dadosReceita.getCodigoNaturezaJuridica());
		if(tipoPessoa != null) {
			matriz.setTipoPessoa(tipoPessoa);
			TipoPessoa tmpTP = tipoPessoa;
			do{
				if(tmpTP.getCodTipoPessoa() != null && tmpTP.getCodTipoPessoa().equals("ADMP")){
					matriz.setOrgaoPublico(true);
					break;
				}
			}while((tmpTP = tmpTP.getTipoPessoaSuperior()) != null);
		}
		else {
			matriz.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
		}
		RamoAtividade ramoAtividade = RamoAtividadeHome.instance().buscarPorCodigo(dadosReceita.getCodigoCnaeFiscal());
		matriz.setRamoAtividade(ramoAtividade);
		
		return matriz;
	}

	private Boolean compararStrings(String s1, String s2){
		if (s1 == null && s2 == null){
			return true;
		}
		if (s1 == null || s2 == null){
			return false;
		}
		return s1.equalsIgnoreCase(s2);
	}

	private Boolean compararEnderecos(Endereco end1, Endereco end2){

		return (compararStrings(end1.getCep().getNumeroCep(), end2.getCep().getNumeroCep())
			&& compararStrings(end1.getNomeEstado(), end2.getNomeEstado())
			&& compararStrings(end1.getNomeCidade(), end2.getNomeCidade())
			&& compararStrings(end1.getNomeBairro(), end2.getNomeBairro())
			&& compararStrings(end1.getNomeLogradouro(), end2.getNomeLogradouro()) && compararStrings(
				end1.getNumeroEndereco(), end2.getNumeroEndereco()));

	}

	private Boolean compararEstados(Estado e1, Estado e2){
		if (e1 == null && e2 == null){
			return true;
		}
		if (e1 == null || e2 == null){
			return false;
		}
		return compararStrings(e1.getCodEstado(), e2.getCodEstado());
	}

	private Boolean compararDocumentos(PessoaDocumentoIdentificacao doc1, PessoaDocumentoIdentificacao doc2){

		return (compararStrings(doc1.getNumeroDocumento(), doc2.getNumeroDocumento())
			&& compararStrings(doc1.getTipoDocumento().getCodTipo(), doc2.getTipoDocumento().getCodTipo()) && compararEstados(
				doc1.getEstado(), doc2.getEstado()));

	}

	private String adicionaZeroEsquerda(String s){

		String ZEROS = "00000000000";
		return ZEROS.substring(s.length()) + s;

	}

	private String adicionarMascaraCPF(String ret){

		if (ret.length() < 11){
			ret = adicionaZeroEsquerda(ret);
		}
		if (ret == null || ret.length() != 11){
			return ret;
		}
		String p1 = ret.substring(0, 3);
		String p2 = ret.substring(3, 6);
		String p3 = ret.substring(6, 9);
		String p4 = ret.substring(9, 11);
		return p1 + "." + p2 + "." + p3 + "-" + p4;
	}

	private String formatarCEP(String cep){

		cep = cep.replaceAll(".-", "");

		if (cep.length() == 8){
			return cep.substring(0, 5) + "-" + cep.substring(5);
		}
		return null;
	}

	private PessoaFisica montaPessoa(List<DadosAdvogadoOAB> inscricoes) throws AdvogadoNaoEncontradoException, PJeBusinessException{
		DadosAdvogadoOAB inscricaoPrincipal = recuperaPrincipal(inscricoes);
		if(inscricaoPrincipal == null){
			throw new AdvogadoNaoEncontradoException();
		}
		PessoaFisica ret = new PessoaFisica();
		ret.setAtivo(true);
		ret.setBrasileiro(true);
		ret.setNumeroCelular(inscricaoPrincipal.getTelefone());
		ret.setEmail(inscricaoPrincipal.getEmail());
		ret.setNome(inscricaoPrincipal.getNome());
		ret.setNomeGenitor(inscricaoPrincipal.getNomePai());
		ret.setNomeGenitora(inscricaoPrincipal.getNomeMae());
		ret.setNumeroCPF(inscricaoPrincipal.getNumCPF());
		ret.setLogin(InscricaoMFUtil.retiraMascara(inscricaoPrincipal.getNumCPF()));
		ret.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
		return ret;
	}
	
	private DadosAdvogadoOAB recuperaPrincipal(List<DadosAdvogadoOAB> inscricoes){
		if (inscricoes.size() > 1 && StringUtils.isNotBlank(nrOAB)) {
			Optional<DadosAdvogadoOAB> dadosAdvogadoOABOptional = inscricoes.stream().filter(dadosAdvogadoOAB -> {
				String inscricaoAdvogado = StringUtil.retiraZerosEsquerda(dadosAdvogadoOAB.getNumInscricao())
						.replaceAll("\\W", "");

				String inscricaoBuscada = StringUtil.retiraZerosEsquerda(nrOAB).replaceAll("\\W", "");
				return dadosAdvogadoOAB.getSituacaoInscricao().equalsIgnoreCase("regular")
						&& inscricaoBuscada.equalsIgnoreCase(inscricaoAdvogado);
			}).findFirst();

			if (dadosAdvogadoOABOptional.isPresent()) {
				return dadosAdvogadoOABOptional.get();
			}
		}

		DadosAdvogadoOAB ret = null;
		for(DadosAdvogadoOAB insc: inscricoes){
			if(insc.getSituacaoInscricao().equalsIgnoreCase("regular") && insc.getTipoInscricao().equalsIgnoreCase("advogado")){
				return insc;
			}else if(ret == null && insc.getSituacaoInscricao().equalsIgnoreCase("regular") && insc.getTipoInscricao().equalsIgnoreCase("suplementar")){
				ret = insc;
			}
		}
		return ret;
	}

	private PessoaAdvogado preencherPessoaAdvogadoByListOAB(List<DadosAdvogadoOAB> listOABAdvogado){
		PessoaAdvogado advogado = new PessoaAdvogado();
		TipoDocumentoIdentificacao tipoOAB = TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao(
				TipoDocumentoIdentificacaoHome.TIPOOAB);

		for (DadosAdvogadoOAB dadosAdvogadoOAB : listOABAdvogado){

			if (advogado.getNome() == null){

				advogado.setNome(dadosAdvogadoOAB.getNome());
				advogado.setNomeGenitor(dadosAdvogadoOAB.getNomePai());
				advogado.setNomeGenitora(dadosAdvogadoOAB.getNomeMae());
				advogado.setNumeroCPF(adicionarMascaraCPF(dadosAdvogadoOAB.getNumCPF()));
				advogado.setDataCadastro(dadosAdvogadoOAB.getDataCadastro());
				advogado.setEmail(dadosAdvogadoOAB.getEmail());
				advogado.setDddComercial(dadosAdvogadoOAB.getDdd() != null && dadosAdvogadoOAB.getDdd().length() > 1 ? dadosAdvogadoOAB.getDdd().substring(0, 2)
						: null);
				advogado.setNumeroComercial(dadosAdvogadoOAB.getTelefone());

				advogado.setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum.A);
				if (dadosAdvogadoOAB.getTipoInscricao().equalsIgnoreCase(tipoInscricaoSuplementar)){
					advogado.setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum.S);
				}
				else if (dadosAdvogadoOAB.getTipoInscricao().equalsIgnoreCase(tipoInscricaoEstagiario)){
					advogado.setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum.E);
				}
				advogado.setValidado(true);
				advogado.setDataValidacao(new Date());
				if (!dadosAdvogadoOAB.getSituacaoInscricao().equals("REGULAR")){
					adicionarMensagemErro("preCadastroPessoaBean.advogado_irregular_oab");
				}

			}

			this.adicionarDocumentoAdvogado(advogado, dadosAdvogadoOAB, tipoOAB,
					advogado.getPessoaDocumentoIdentificacaoList());
			this.adicionarEnderecoAdvogado(advogado, dadosAdvogadoOAB, advogado.getEnderecoList());

			/*
			 * Pega a primeira OAB e coloca na tabela pessoa_advogado. TODO - O ideal é retirar estes dados desta tabela e usar somente os dados da
			 * OAB na tabela documento_identificacao
			 */
			for (PessoaDocumentoIdentificacao docAdv : advogado.getPessoaDocumentoIdentificacaoList()){

				if (docAdv.getTipoDocumento().getCodTipo().equals(TipoDocumentoIdentificacaoHome.TIPOOAB)){

					advogado.setNumeroOAB(docAdv.getNumeroDocumento());
					advogado.setUfOAB(docAdv.getEstado());
					break;

				}

			}

		}
		try{
			DadosReceitaPessoaFisica dadosReceita = (DadosReceitaPessoaFisica) ConsultaClienteWebService.instance()
					.consultaDados(TipoPessoaEnum.F, advogado.getDocumentoCpfCnpj(), true);

			this.preencherPessoaFisicaPelaReceita(dadosReceita);
			this.mesclarEnderecosPessoa(advogado.getEnderecoList(), this.pessoaFisica.getEnderecoList());
			for (Endereco e : advogado.getEnderecoList()){
				e.setUsuario(advogado.getPessoa());
			}
			this.mesclarDocumentosPessoa(advogado.getPessoaDocumentoIdentificacaoList(),
					this.pessoaFisica.getPessoaDocumentoIdentificacaoList());
			for (PessoaDocumentoIdentificacao pdi : advogado.getPessoaDocumentoIdentificacaoList()){
				pdi.setPessoa(advogado);
			}
			advogado.setDataNascimento(this.pessoaFisica.getDataNascimento());
			advogado.setSexo(this.pessoaFisica.getSexo());
			advogado.setNomeGenitora(this.pessoaFisica.getNomeGenitora());
		} catch (Exception e){
			e.printStackTrace();
		}

		return advogado;

	}

	/**
	 * Adiciona um documento do tipo OAB a uma pessoa
	 * 
	 * @param pessoa Instancia da pessoa que recebera o novo documento
	 * @param dadosAdvogadoOAB Dados vindos do WS da OAB
	 * @param tipoOAB O tipo de documento OAB cadastrado
	 * @param listaAtual A lista de documentos atual desta pessoa
	 */
	private void adicionarDocumentoAdvogado(PessoaAdvogado advogado, DadosAdvogadoOAB dadosAdvogadoOAB,
			TipoDocumentoIdentificacao tipoOAB, Set<PessoaDocumentoIdentificacao> listaAtual){

		String sql = "select o from Estado o where  o.codEstado = :codEstado";
		Query qEstado = EntityUtil.getEntityManager().createQuery(sql);
		qEstado.setParameter("codEstado", dadosAdvogadoOAB.getUf());

		PessoaDocumentoIdentificacao oabTemp = new PessoaDocumentoIdentificacao();
		oabTemp.setNumeroDocumento(dadosAdvogadoOAB.getNumInscricao());
		oabTemp.setNome(dadosAdvogadoOAB.getNome());
		oabTemp.setOrgaoExpedidor(dadosAdvogadoOAB.getOrganizacao());
		oabTemp.setEstado((Estado) qEstado.getResultList().get(0));
		oabTemp.setDocumentoPrincipal(false);
		oabTemp.setAtivo(true);
		oabTemp.setUsadoFalsamente(false);
		oabTemp.setTipoDocumento(tipoOAB);
		oabTemp.setPessoa(advogado);

		listaAtual.add(oabTemp);

	}

	/**
	 * Adiciona um endereco ao advogado
	 * 
	 * @param advogado Advogado que receberá o endereço
	 * @param dadosAdvogadoOAB Dados vindos da OAB
	 * @param listaAtual Lista de endereços atual
	 */
	private void adicionarEnderecoAdvogado(PessoaAdvogado advogado, DadosAdvogadoOAB dadosAdvogadoOAB, List<Endereco> listaAtual){

		if (dadosAdvogadoOAB.getCep() != null) {
			String s = "SELECT c FROM Cep AS c where replace(c.numeroCep,'-','') = :numeroCep AND c.ativo = true";
			Query q = EntityUtil.getEntityManager().createQuery(s);
			q.setParameter("numeroCep", dadosAdvogadoOAB.getCep());
			q.setMaxResults(1);
			try {
				Cep cep = (Cep) (q.getSingleResult());

				Endereco endereco = new Endereco();
				endereco.setCep(cep);
				endereco.setNomeBairro(dadosAdvogadoOAB.getBairro());
				endereco.setNomeLogradouro(dadosAdvogadoOAB.getLogadouro());
				endereco.setUsuario(advogado.getPessoa());

				listaAtual.add(endereco);
			} catch (NoResultException no) {
			}
		}
	}

	/**
	 * Mescla endereços de uma pessoa
	 * 
	 * @param enderecosAtuais Lista que será atualizada
	 * @param enderecosEncontrados
	 */
	private void mesclarEnderecosPessoa(List<Endereco> enderecosAtuais, List<Endereco> enderecosEncontrados){

		List<Endereco> novosEnderecos = new ArrayList<Endereco>(0);
		for (Endereco enderecoEncontrado : enderecosEncontrados){

			Boolean isNewEndereco = true;
			for (Endereco enderecoPessoa : enderecosAtuais){

				if (this.compararEnderecos(enderecoEncontrado, enderecoPessoa)){
					isNewEndereco = false;
					break;
				}
			}

			if (isNewEndereco)
				novosEnderecos.add(enderecoEncontrado);

		}

		enderecosAtuais.addAll(novosEnderecos);

	}

	private void mesclarDocumentosPessoa(Set<PessoaDocumentoIdentificacao> documentosAtuais,
			Set<PessoaDocumentoIdentificacao> documentosEncontrados){

		List<PessoaDocumentoIdentificacao> novosDocumentos = new ArrayList<PessoaDocumentoIdentificacao>(0);
		for (PessoaDocumentoIdentificacao docEncontrado : documentosEncontrados){

			Boolean isNewDocumento = true;
			for (PessoaDocumentoIdentificacao docAtual : documentosAtuais){

				if (this.compararDocumentos(docEncontrado, docAtual)){

					docAtual.setNome(docEncontrado.getNome());
					docAtual.setOrgaoExpedidor(docEncontrado.getOrgaoExpedidor());

					isNewDocumento = false;
					break;
				}
			}

			if (isNewDocumento)
				novosDocumentos.add(docEncontrado);

		}

		documentosAtuais.addAll(novosDocumentos);

	}

	protected PessoaAdvogado getPessoaAdvogadoFromWSOAB(String nrCPF) throws Exception{

		ConsultaClienteOAB consultaOAB = (ConsultaClienteOAB) Component.getInstance("consultaClienteOAB");

		try{

			consultaOAB.consultaDados(nrCPF, false);

		} catch (Exception e){
			throw e;
		}

		List<DadosAdvogadoOAB> listOABAdvogado = consultaOAB.getDadosAdvogadoList();

		if (listOABAdvogado.size() == 0)
			throw new AdvogadoNaoEncontradoException();

		return this.preencherPessoaAdvogadoByListOAB(listOABAdvogado);

	}

	protected Boolean validarDocumentoAlternativo(){

		if (documentoAlternativo.getTipoDocumento() == null){
			adicionarMensagemErro("preCadastroPessoaBean.tipo_documento_branco");
			return false;
		}

		if (documentoAlternativo.getNumeroDocumento() == null
			|| documentoAlternativo.getNumeroDocumento().trim().equalsIgnoreCase("")){
			adicionarMensagemErro("preCadastroPessoaBean.numero_documento_branco");
			return false;
		}

		if (documentoAlternativo.getNome() == null || documentoAlternativo.getNome().trim().equalsIgnoreCase("")){
			adicionarMensagemErro("preCadastroPessoaBean.nome_documento_branco");
			return false;
		}

		if (documentoAlternativo.getTipoDocumento().isDataExpedicaoObrigatorio() && documentoAlternativo.getDataExpedicao() == null){
			adicionarMensagemErro("preCadastroPessoaBean.data_expedicao_documento_branco=");
			return false;
		}

		if (documentoAlternativo.getTipoDocumento().isOrgaoExpedidorObrigatorio()
				&& (documentoAlternativo.getOrgaoExpedidor() == null
						|| documentoAlternativo.getOrgaoExpedidor().trim().equalsIgnoreCase(""))) {
			adicionarMensagemErro("preCadastroPessoaBean.orgao_expedidor_documento_branco");
			return false;
		}

		return true;
	}
	
		
	/** PJEII-3455 - Método criado para alterar o pré-cadastro de pessoas especializadas 
	 * @author Rafael Barros
	 * @since 1.4.5	 
	 * @return String
	 * Utilizado no método: confirmarPessoa()
	 * Depende das configurações realizadas no método:
	 */
	@SuppressWarnings("unchecked")
	public boolean preCadastroPessoas(){
		
		String tipoDocumento = "CPF";
		pessoaFisica = null;
		pessoaJuridica = null;
		pessoaAdvogado = null;
		pessoaEncontradaBanco = false;
		pessoaEncontradaReceita = false;

		if (this.inTipoPessoa == TipoPessoaEnum.F){
			if (!this.brasileiro){
				tipoDocumento = "PAS";
			}
		}else{
			tipoDocumento = "CPJ";
		}
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = buscarDocumentoIdentificacao(this.nrDocumentoPrincipal, tipoDocumento, getPais(), true, this.inTipoPessoa);
		if (pessoaDocumentoIdentificacao != null){
			if (inTipoPessoa == TipoPessoaEnum.F){
				PessoaFisicaManager pessoaFisicaManager = (PessoaFisicaManager)Component.getInstance("pessoaFisicaManager");
				try {
					pessoaFisica = pessoaFisicaManager.findById(pessoaDocumentoIdentificacao.getPessoa().getIdPessoa());
				} catch (PJeBusinessException e1) {
					e1.printStackTrace();
					return false;
				}

				if (getParametro("tipoEspecializado") != null){
					Class<?> tipoEspecializado;
					try{
						tipoEspecializado = Class.forName(getParametro("tipoEspecializado"));
						PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
						pessoaFisica = (PessoaFisica) pessoaService.especializa(pessoaFisica, (Class<PessoaFisicaEspecializada>) tipoEspecializado);
					} catch (ClassNotFoundException e){
						throw new RuntimeException(e);
					} catch (PJeBusinessException e) {
						e.printStackTrace();
						return false;
					}
				}
			}else{
				pessoaJuridica = ((PessoaJuridica) EntityUtil.removeProxy(pessoaDocumentoIdentificacao.getPessoa()));
			}		
		}
		return true;
	}
	
	/**
	 * Quando o procurador insere a parte(Pessoa Física, Jurídica ou Ente e autoridade)  
	 * no polo ativo e a ela tem mais de uma representação, sendo que umas dessas é a
	 * mesma procuradoria do procurador logado. Deve-se setar na parte a procuradoria dele
	 * e não apresentar a combo com as opções para selecionar as procuradorias.
	 * @return true ou false
	 */
	public boolean isVinculoProcuradorLogadoPoloAtivo(Object pessoa){
		setProcInformacao(Boolean.FALSE);
		ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
		Procuradoria procUsuarioLogado = procuradoriaManager.recuperaPorLocalizacao(Authenticator.getLocalizacaoAtual());
		if(procUsuarioLogado!= null && Authenticator.isProcurador() && ProcessoParteHome.POLO_ATIVO.equals(ProcessoParteHome.instance().getPolo()) 
				&& procUsuarioLogado.getTipo().equals(TipoProcuradoriaEnum.P) && pessoa != null && pessoa instanceof Pessoa) {
			List<Procuradoria> listProcuradorias = procuradoriaManager.getlistProcuradorias((Pessoa)pessoa);
			for(Procuradoria procuradoria: listProcuradorias)
				if(procuradoria.getNome().equals(Authenticator.getProcuradoriaAtualUsuarioLogado().getNome())){
					//Seta a Procuradoria na parte informada.
					ProcessoParteHome.instance().getInstance().setProcuradoria(procuradoria);
					//Seta a procInformacao para mostrar somente as informações da procuradoria
					setProcInformacao(Boolean.TRUE);
					return true;
				}
			return false;
		}
		return false;
	}
	
	/**
	 * Faz as validações para apresentar ou não a combo de 
	 * defensoria nas Partes (Polo Ativo, Passivo e Outros Participantes)
	 * @return true ou false
	 */
	public boolean isVinculoDefensoria(){
		if(getPessoa().getIdPessoa() == null && getHasPessoaFisica()){
			return true;
		}else if(getlistProcuradorias(getPessoa())!=null && getHasPessoaFisica() && getlistProcuradorias(getPessoa()).size() ==0){
			return true;
		}
		return false;
	}
	
	/**
	 * Retorna a lista de procuradorias de acordo com a pessoa informada
	 * 
	 * @param	pessoa
	 * @return 	retorna uma lista de procuradorias conforme parametro. Se o parametro pessoa ou idPessoa estiverem null,
	 * 			o metodo retornara uma lista de Procuradoria instanciada e vazia.
	 */
	public List<Procuradoria> getlistProcuradorias(Pessoa pessoa){
		if(pessoa != null && pessoa.getIdPessoa() != null){
			ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
			return procuradoriaManager.getlistProcuradorias(pessoa);
		}
		return new ArrayList<Procuradoria>();
	}
	
	/**
	 * Retorna a lista de representantes para a 
	 * seleção no cadastro de parte do processo
	 * @param pessoa representada
	 * @return lista de representantes possíveis
	 */
	public List<Procuradoria> getListRepresentantes(Pessoa pessoa) {
		List<Procuradoria> listaRepresentantes = new ArrayList<Procuradoria>();

		listaRepresentantes = getlistProcuradorias(pessoa);
		if (listaRepresentantes != null && listaRepresentantes.isEmpty() && this.inTipoPessoa != TipoPessoaEnum.A) {
			ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
			listaRepresentantes.addAll(procuradoriaManager.getlistDefensorias());
		}

		Procuradoria procuradoriaAtual = ProcessoParteHome.instance().getInstance().getProcuradoria();
		if (procuradoriaAtual != null && !listaRepresentantes.contains(procuradoriaAtual)) {
			listaRepresentantes.add(procuradoriaAtual);
		}

		return listaRepresentantes;
	}
	
	/**
	 * Verifica se a combo de representantes será renderizada
	 * @return TRUE se for para renderizar a combo
	 */
	public Boolean renderizaComboRepresentantes() {
		Boolean ret = Boolean.FALSE;
		if (!isPessoaTransient()) {
			ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
			Integer count = procuradoriaManager.getListProcuradoriasCount(getPessoa(), TipoProcuradoriaEnum.P);
			if (count > 1) {
				ret = Boolean.TRUE;
			} else if (count == 1 && ProcessoParteHome.instance().getInstance().getProcuradoria() != null) {
				ret = !procuradoriaManager.getlistProcuradorias(getPessoa(), TipoProcuradoriaEnum.P)
						.contains(ProcessoParteHome.instance().getInstance().getProcuradoria());
			} else if (count == 0 && this.inTipoPessoa != TipoPessoaEnum.A) {
				ret = procuradoriaManager.getListDefensoriasCount() > 0;
			}
		}
		return ret;
	}
	
	/**
	 * Verifica se a informação do representante processual deve ser exibida
	 * @return True se deve renderizar
	 */
	public Boolean renderizaProcInformacao() {
		Boolean ret = Boolean.FALSE;
		if (!isPessoaTransient()) {
			ret = !renderizaComboRepresentantes();
		}
		return ret;
	}
	
	public Boolean isPessoaTransient(){
		return (getPessoa() != null && getPessoa().getIdPessoa() == null) ? Boolean.TRUE : Boolean.FALSE;
	}
	
	/**
	 * REtorna o nome do representante processual a ser exibido na tela de inclusão de parte
	 * @return Nome da procuradoria
	 */
	public String getNomeRepresentante(){
		if(ProcessoParteHome.instance().getInstance().getProcuradoria() != null){
			return ProcessoParteHome.instance().getInstance().getProcuradoria().getNome();
		} else {
			if(!getlistProcuradorias(getPessoa()).isEmpty()) {
				return getlistProcuradorias(getPessoa()).get(0).getNome();
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Método responsável por iniciar o processo de criação de uma nova entidade
	 * ou autoridade.
	 * 
	 * Basicamente 'zera' os valores para PessoaAutoridade iniciando com null e
	 * inicia o método de criação de criação de pessoas
	 * 
	 * Utilizado em preCadastroPessoa.xhtml
	 */
	public void criarNovoEnteAutoridade() {
		setPessoaAutoridade(null);
		confirmarPessoa();
	}
	
	public void limparPessoaHome() {
		
		this.pessoaFisica = null;
		this.pessoaJuridica = null;
		this.pessoaAutoridade = null;
		
		PessoaFisicaHome pFisHome = (PessoaFisicaHome) Component.getInstance("PessoaFisicaHome");  
		if(pFisHome != null && pFisHome.getInstance() != null)
			pFisHome.newInstance();
		
		PessoaJuridicaHome pJurHome = (PessoaJuridicaHome) Component.getInstance("PessoaJuridicaHome");  
		if(pJurHome != null && pJurHome.getInstance() != null)
			pJurHome.newInstance();
		
		PessoaAutoridadeHome pAutHome = (PessoaAutoridadeHome) Component.getInstance("PessoaAutoridadeHome");  
		if(pAutHome != null && pAutHome.getInstance() != null)
			pAutHome.newInstance();
	
	}

	@SuppressWarnings("unchecked")
	public void confirmarPessoa(){
		
		/* [PJEII-6431] Erro no cadastro de Documentos de Identificação da parte  
	 	* Força novo entity na home pessoaDocumentoIdentificacaoHome para evitar que sejam carregados documentos utilizados em edição anterior  
	 	*/  
	 	PessoaDocumentoIdentificacaoHome pessoaDocumentoIdentificacaoHome = (PessoaDocumentoIdentificacaoHome) Component.getInstance("pessoaDocumentoIdentificacaoHome");  
	 	pessoaDocumentoIdentificacaoHome.newInstance(); 
	 	
		PessoaDocumentoIdentificacao pessoaDocIdent = null;
		String cpf = null;
		Pessoa pessoa = null;
		if(pessoaAdvogado != null && pessoaFisica == null){
			pessoaFisica = pessoaAdvogado.getPessoa();
			
			if (this.nrDocumentoPrincipal == null) {
				this.nrDocumentoPrincipal = pessoaAdvogado.getNumeroCPF();
			}
		}
		if (tipoPessoa != null && !tipoPessoa.trim().isEmpty()){
			inTipoPessoa = TipoPessoaEnum.valueOf(getParametro("tipoPessoa"));
		}
		if (isBrasileiro != null && !isBrasileiro.trim().isEmpty()){
			brasileiro = Boolean.valueOf(getParametro("isBrasileiro"));
		}
		
		if (!getIsPartes()){
			if (null != pessoaEncontradaBanco && pessoaEncontradaBanco){
				//PJEII-3455 - Caso a confirmação de pessoa seja um pré-cadastro de pessoas especializadas (oficial de justiça, assistente de procuradoria, etc.)
				//PJEII-3455 - Neste momento o método preCadastroPessoa() tenta especializar a pessoa física.
				//PJEII-3455 - Se a pessoa já é especializada pára a execução aqui e retorna a mensagem de erro
				if (preCadastroPessoas()==false)
					return;
			}
		}
		/**
		 * adicionada verificação se a pessoaFisica não é nula, 
		 * pois quando se adiciona uma pessoa sem documentos, 
		 * não faz a pesquisa para preencher essa variavel e 
		 * com isso não estava permitindo cadastrar pessoas Físicas sem cpf  
		 */
		if (inTipoPessoa == TipoPessoaEnum.F && (pessoaFisica != null) && !(Pessoa.instanceOf(pessoaFisica, PessoaAdvogado.class))){
			//PJEII-3190] Criação da chamada para persistencia da Pessoa Documento Identificacao.
			//criação de chamada para consulta de Pessoa Fisica e Pessoa Documento Identificação
			PessoaFisica pessoaTemp = null;
			cpf = pessoaFisica.getNumeroCPF();
			if (cpf != null) {
				// Verifica se já existe um usuário com o CPF informado servindo como login
				// Caso exista, inclui como documento CPF desse usuário o CPF indicado
				// FIXME Tal como foi preparado o algoritmo na PJEII-3199, o sistema estaria permitindo que duas pessoas tenham o mesmo CPF. Tem que unificar.
				UsuarioService usuarioService = ComponentUtil.getComponent(UsuarioService.class);
				Usuario usuario = usuarioService.findByLogin(cpf);
				try{
					if (usuario != null && pessoaFisica.getIdUsuario() != null) {
							//[PJEII-3199] Caso o usuario exista, recupera a pessoaFisica a partir do usuario
						PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
						pessoaTemp = (PessoaFisica) pessoaService.findById(usuario.getIdUsuario());
						//[PJEII-3199] Recupera a pessoa Documento Identificação a partir do cpf.
						PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent(PessoaDocumentoIdentificacaoManager.class);
						pessoaDocIdent = pessoaDocumentoIdentificacaoManager.findByCPF(cpf);
					}
				} catch (PJeBusinessException e){
					adicionarMensagemErro(e.getLocalizedMessage());
					return;
				}
			}
			//[PJEII-3199] Caso não exista Pessoa Documento Identificação e exista a Pessoa Fisica, insere-se a PDI.
			if (pessoaDocIdent == null && pessoaTemp != null) {
				pessoaDocIdent = new PessoaDocumentoIdentificacao();
				PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent(PessoaDocumentoIdentificacaoManager.class);
				pessoaDocIdent = pessoaDocumentoIdentificacaoManager.preencherPessoaDocumentoIdentificacao(pessoaDocIdent, pessoaTemp, cpf);
				try {
					PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
					pessoaService.adicionaInscricaoMF(pessoaTemp, cpf);
					pessoa = pessoaService.findById(pessoaDocIdent.getPessoa().getIdUsuario());
				} catch (PJeBusinessException e) {
					FacesMessages.instance().addToControl("errosPreCadastro", "Erro ao persistir Pessoa Documento Identificação.");
					e.printStackTrace();
				}
			//[PJEII-3493] Verifica se a Pessoa Fisica não esta nula e não existe na base dados, caso seja verdadeiro
			//Insere-se a pessoa física.
			} else if (pessoaFisica != null){
				pessoaFisica = (PessoaFisica) persistirPessoa(pessoaFisica);
			}
		} else if (inTipoPessoa == TipoPessoaEnum.J){
			if (pessoaJuridica != null){
				pessoaJuridica = EntityUtil.refreshEntity(pessoaJuridica);
				this.persistirPessoa(pessoaJuridica);
			}

		}
		Boolean isPessoaFisica = (inTipoPessoa == TipoPessoaEnum.F);
		isConfirmado = Boolean.FALSE;

		if (getHasPessoaFisica() && pessoa == null) {
			pessoa = pessoaFisica;
		} else if (getHasPessoaJuridica() && pessoa == null) {
			pessoa = pessoaJuridica;
		} else if (getHasPessoaAutoridade() && pessoa == null) {
			pessoa = pessoaAutoridade;
			pessoa.setInTipoPessoa(TipoPessoaEnum.A);
			inTipoPessoa = pessoa.getInTipoPessoa();
		} else if (pessoa == null) {
			if (inTipoPessoa == TipoPessoaEnum.F){
				if (confirmouCadastroErroWsReceita && inTipoPessoa == TipoPessoaEnum.F){
					Class<? extends PessoaFisicaEspecializada> pessoaEspecializada = null;
					try{
						if (getParametro("tipoEspecializado") != null){
							this.strPessoaFisicaEspecializada = getParametro("tipoEspecializado");
							pessoaEspecializada = (Class<? extends PessoaFisicaEspecializada>) Class.forName(this.strPessoaFisicaEspecializada);
							PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
							pessoaFisica = (PessoaFisica) pessoaService.especializa(pessoaFisica, (Class<PessoaFisicaEspecializada>) pessoaEspecializada);
							pessoa = pessoaFisica;
						}else{
							pessoa = new PessoaFisica();
						}
					} catch (ClassNotFoundException e){
						e.printStackTrace();
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
					/*
					 * DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA
					 */
					pessoa.setLogin(InscricaoMFUtil.retiraMascara(nrDocumentoPrincipal));
					pessoa.setAtivo(true);
					pessoa.setBloqueio(false);
					pessoa.setProvisorio(false);

					if (Pessoa.instanceOf(pessoa, PessoaMagistrado.class)){
						((PessoaFisica) pessoa).getPessoaMagistrado().setMatricula("");
					}

					if (Pessoa.instanceOf(pessoa, PessoaAssistenteAdvogado.class)){
						((PessoaFisica) pessoa).getPessoaAssistenteAdvogado().setDataCadastro(new Date());
					}

					if (Pessoa.instanceOf(pessoa, PessoaAssistenteProcuradoria.class)){
						((PessoaFisica) pessoa).getPessoaAssistenteProcuradoria().setDataCadastro(new Date());
					}
				} else{
					pessoa = new PessoaFisica();
				}
			}else if (inTipoPessoa == TipoPessoaEnum.J){
				pessoa = new PessoaJuridica();
			}else if (inTipoPessoa == TipoPessoaEnum.A){
				pessoa = new PessoaAutoridade();
			}
		}

		if (isPessoaNaoIndividualizada){

			pessoa.setPessoaIndividualizada(Boolean.FALSE);

			if (hasDocumentoAlternativo){

				if (!this.validarDocumentoAlternativo()){
					return;
				}

				Usuario usuarioLogado = Authenticator.getUsuarioLogado();
				if (usuarioLogado != null){
					documentoAlternativo.setUsuarioCadastrador(usuarioLogado);
				}
				documentoAlternativo.setAtivo(Boolean.TRUE);
				documentoAlternativo.setPessoa(pessoa);
				documentoAlternativo.setDocumentoPrincipal(Boolean.FALSE);
				pessoa.getPessoaDocumentoIdentificacaoList().add(documentoAlternativo);
				pessoa.setNome(documentoAlternativo.getNome()); // Seta como nome na tabela usuario o nome informado no documento

				if (isPessoaFisica){
					((PessoaFisica) pessoa).setDataNascimento(dtNascimentoAbertura);
				} else {
					((PessoaJuridica) pessoa).setDataAbertura(dtNascimentoAbertura);
				}

			} else{

				if (nomeAlcunhaPessoaSuja == null){
					adicionarMensagemErro("preCadastroPessoaBean.nome_alcunha_branco");
					return;
				}

				PessoaNomeAlternativo pessoaNomeAlternativo = new PessoaNomeAlternativo();
				pessoaNomeAlternativo.setPessoaNomeAlternativo(nomeAlcunhaPessoaSuja);
				pessoaNomeAlternativo.setPessoa(pessoa);

				pessoa.setNome(nomeAlcunhaPessoaSuja);
				pessoa.getPessoaNomeAlternativoList().add(pessoaNomeAlternativo);
			}
			pessoa = this.persistirPessoa(pessoa);

		} else if (this.confirmouCadastroErroWsReceita){
			if (this.nomePessoaErroWsReceita == null || this.nomePessoaErroWsReceita.trim().equals("")){
				adicionarMensagemErro("preCadastroPessoaBean.nome_pessoa_erro_ws_receita_branco");
				return;
			}
			pessoa.setNome(this.nomePessoaErroWsReceita);

			PessoaDocumentoIdentificacao documentoAux = new PessoaDocumentoIdentificacao();
			documentoAux.setAtivo(Boolean.TRUE);
			documentoAux.setPessoa(pessoa);
			documentoAux.setNome(pessoa.getNome());
			documentoAux.setNumeroDocumento(this.nrDocumentoPrincipal);
			documentoAux.setUsadoFalsamente(false);
			documentoAux.setDocumentoPrincipal(true);
			documentoAux.setAtivo(true);
			if (inTipoPessoa.equals(TipoPessoaEnum.J)){
				documentoAux.setTipoDocumento(TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao(
				TipoDocumentoIdentificacaoHome.tipoCPJ));
				documentoAux.setOrgaoExpedidor("Secretaria da Receita Federal");
			} else {
				if (brasileiro){
	
					documentoAux.setTipoDocumento(TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao(
							TipoDocumentoIdentificacaoHome.TIPOCPF));
					documentoAux.setOrgaoExpedidor("Secretaria da Receita Federal");
	
				}
				else{
					documentoAux.setTipoDocumento(TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao(
							TipoDocumentoIdentificacaoHome.TIPOPASSAPORTE));
					documentoAux.setOrgaoExpedidor(getPais().getDescricao());
					/*[PJEII-4394] - @author patrick.nascimento. 
					 * ds_login de estrangeiros agora é
					 * a concatenação do codigo iso com o num. do passaporte*/
					String login = getPais().getCodigo() + documentoAux.getNumeroDocumento() + "";
					documentoAux.getPessoa().setLogin(login);
					documentoAux.setPais(getPais());
					((PessoaFisica)pessoa).setBrasileiro(brasileiro);
				}
			}
			pessoa.getPessoaDocumentoIdentificacaoList().add(documentoAux);
			pessoa = this.persistirPessoa(pessoa);

			// verificar se a PessoaFisica é de um tipo especializado,
			// se não, completar a hierarquia
		}

		PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome");
		pessoaHome.setInstance(pessoa);
		if (pessoa instanceof PessoaFisica){
			PessoaFisicaHome.instance().setId(pessoa.getIdUsuario());
			if (PessoaFisicaHome.instance().getInstance()!= null && PessoaFisicaHome.instance().getInstance().getProfissao() != null){
				ProfissaoSuggestBean profissaoSuggest = ComponentUtil.getComponent("profissaoSuggest");
				profissaoSuggest.setDefaultValue(PessoaFisicaHome.instance().getInstance().getProfissao().getProfissao());
			}
		}
		
		selecionaRepresentantePadrao(pessoa);
		selecionaParteSigilosaPadrao();
		redirectPessoa(pessoa);
		EntityUtil.flush();
	}
	
	/**
	 * Efetua selecao automatica para a combo de parte sigilosa (sim/nao)
	 * com base na configuracao do tipo de parte.
	 */
	private void selecionaParteSigilosaPadrao() {
		ProcessoParte processoParte = ProcessoParteHome.instance().getInstance();
		TipoParte tipoParte = processoParte.getTipoParte();
		if (tipoParte != null) {
			TipoParteConfiguracaoManager tipoParteConfiguracaoManager = ComponentUtil.getComponent(TipoParteConfiguracaoManager.class);
			List<TipoParteConfiguracao> configs = tipoParteConfiguracaoManager.recuperarPorTipoPartePadrao(tipoParte, true);
			if (CollectionUtilsPje.isNotEmpty(configs)) {
				TipoParteConfiguracao tipoParteConfiguracao = configs.get(0);
				processoParte.setParteSigilosa(Boolean.TRUE.equals(tipoParteConfiguracao.getParteSigilosa()));
			}
		}
	}
	
	/**
	 * Efetua seleção automática para a combo de representantes,
	 * caso o usuário logado seja de uma representação possível para
	 * a entidade, e o polo seja Ativo
	 * @param pessoa
	 */
	private void selecionaRepresentantePadrao(Pessoa pessoa){
		if(Authenticator.getProcuradoriaAtualUsuarioLogado() != null){
			List<Procuradoria> representantesPossiveis = getListRepresentantes(pessoa);
			if(podeSelecionarRepresentante(representantesPossiveis)){
				ProcessoParteHome.instance().getInstance().setProcuradoria(Authenticator.getProcuradoriaAtualUsuarioLogado());
			}
		}
	}
	
	/**
	 * Verifica se o usuário logado
	 * está elegível a preenchimento automático
	 * do representante
	 * @param representantesPossiveis
	 * @return true se deve selecionar
	 */
	private Boolean podeSelecionarRepresentante(List<Procuradoria> representantesPossiveis){
		Boolean ret = Boolean.FALSE;
		if(representantesPossiveis != null 
				&& !representantesPossiveis.isEmpty() 
				&& representantesPossiveis.contains(Authenticator.getProcuradoriaAtualUsuarioLogado())
				&& ProcessoParteHome.instance().getInstance().getInParticipacao() == ProcessoParteParticipacaoEnum.A){
			ret = Boolean.TRUE;
		}
		
		return ret;
	}	

	public void confirmarPessoaAutoridade(){

		PessoaAutoridadeSuggestBean suggest = (PessoaAutoridadeSuggestBean) Component.getInstance(PessoaAutoridadeSuggestBean.NAME);

		if ((suggest.getInstance() == null) && (this.getPessoaAutoridade() == null)){
			adicionarMensagemErro("preCadastroPessoaBean.pessoa_autoridade_nula");
			return;
		}

		setPessoaAutoridade((suggest.getInstance() != null) ? suggest.getInstance() : getPessoaAutoridade());
		setIsConfirmado(true);
		ProcessoParteHome.instance().inserir();

	}

	/**
	 * Método que busca um objeto <code>PessoaDocumentoIdentificacao</code> de acordo com um número de
	 * documento, um tipo de documento, o país de emissão, uma flag ativo/inativo e um tipo de pessoa
	 * associada ao documento. Quando a flag ativo/inativo e o tipo de pessoa estão nulos, o comportamento 
	 * do método é idêntico ao comportamento originalmente implementado (antes da inclusão dos parâmetros
	 * <code>ativo</code> e <code>tipoPessoa</code>).
	 * Quando a flag ativo/inativo não é nula, essa característica é considerada na busca. O mesmo ocorre
	 * para o tipo de pessoa associada ao documento, quando esse parâmetro não é nulo.
	 * @param numeroDocumento	número do documento buscado
	 * @param tipoDocumento		tipo do documento buscado
	 * @param pais				país emissor do documento buscado (pode ser nulo)
	 * @param ativo				flag indicando se apenas documentos ativos/inativos devem ser considerados 
	 * 							na busca (pode ser nulo)
	 * @param tipoPessoa		enumerador indicando que tipo de pessoa associada ao documento é o tipo
	 * 							esperado (pode ser nulo)
	 * @return	a primeira ocorrência de documento encontrada, de acordo com os parâmetros passados e 
	 * 			restrições consideradas.
	 *
	 */
	@SuppressWarnings("unchecked")
	private PessoaDocumentoIdentificacao buscarDocumentoIdentificacao(String numeroDocumento, String tipoDocumento,
			Pais pais, Boolean ativo, TipoPessoaEnum tipoPessoa){
		
		StringBuilder s = new StringBuilder("select o from PessoaDocumentoIdentificacao o ")
				.append("where o.numeroDocumento = :nrDocumentoPrincipal and o.tipoDocumento.codTipo = :tipoDocumento and o.pessoa.unificada = false");
		
		if (ativo != null) {
			s.append(" and o.ativo = :ativo ");
		}
		if (pais != null && pais.getId() != null) {
			s.append(" and o.pais.id = :idPais ");
		}

		Query q = EntityUtil.getEntityManager().createQuery(s.toString());
		q.setParameter("nrDocumentoPrincipal", numeroDocumento);
		q.setParameter("tipoDocumento", tipoDocumento);
		
		if (ativo != null) {
			q.setParameter("ativo", ativo);
		}
		if (pais != null && pais.getId() != null) {
			q.setParameter("idPais", pais.getId());
		}

		List<PessoaDocumentoIdentificacao> pessoaIdentificacaoList = q.getResultList();
		if (pessoaIdentificacaoList.size() > 0) {
			if (tipoPessoa == null) {
				return pessoaIdentificacaoList.get(0);
			} else {
				for (PessoaDocumentoIdentificacao pdi : pessoaIdentificacaoList) {
					if (tipoPessoa.equals(TipoPessoaEnum.J)) {
						PessoaJuridica psJuridica = EntityUtil.find(PessoaJuridica.class, pdi.getPessoa().getIdUsuario());
						if (psJuridica != null) {
							return pdi;
						}
					} else if(tipoPessoa.equals(TipoPessoaEnum.F)) {
						PessoaFisica psFisica = EntityUtil.find(PessoaFisica.class, pdi.getPessoa().getIdUsuario());
						if (psFisica != null) {
							return pdi;
						}
					} else if(tipoPessoa.equals(TipoPessoaEnum.A)) {
						PessoaAutoridade psAutoridade = EntityUtil.find(PessoaAutoridade.class,pdi.getPessoa().getIdPessoa());
						if (psAutoridade != null) {
							return pdi;
						}
					}
				}
				return null;
			}
		} else {
			return null;
		}
	}

	public Pessoa pesquisaPessoa(TipoPessoaEnum tipoPessoa, String documento) {
		this.setInTipoPessoa(tipoPessoa);
		this.setNrDocumentoPrincipal(documento);
		
		this.pesquisarPorDocumento();
		this.confirmarPessoa();
		
		return this.getPessoa();
	}

	public void pesquisarPorDocumento(){
		resetarVariaveisPesquisa();
		if(tipoPessoa != null && !tipoPessoa.trim().isEmpty()){
			inTipoPessoa = TipoPessoaEnum.valueOf(tipoPessoa);
		}
		if(isBrasileiro != null && !isBrasileiro.trim().isEmpty()){
			brasileiro = Boolean.valueOf(isBrasileiro);
		}
		String inscricaoMF = InscricaoMFUtil.retiraMascara(nrDocumentoPrincipal);
		if(brasileiro && inTipoPessoa == TipoPessoaEnum.F && (!InscricaoMFUtil.isCpfValido(inscricaoMF))){
			adicionarMensagemErro("preCadastroPessoaBean.cpf_cnpj_invalido");
			return;
		} else if (inTipoPessoa == TipoPessoaEnum.J && inscricaoMF != null && 
				!inscricaoMF.isEmpty() && !InscricaoMFUtil.isCnpjValido(inscricaoMF)) {
			
			adicionarMensagemErro("preCadastroPessoaBean.cpf_cnpj_invalido");
			return;
		}

		String tipoDocumento = null;
		pessoaFisica = null;
		pessoaJuridica = null;
		pessoaAdvogado = null;
		pessoaEncontradaBanco = false;
		pessoaEncontradaReceita = false;
		if (inTipoPessoa == TipoPessoaEnum.F){
			if (!brasileiro){
				tipoDocumento = "PAS";
			}else{
				tipoDocumento = "CPF";
				try {
					PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
					pessoaFisica = (PessoaFisica) pessoaService.findByInscricaoMF(inscricaoMF, !PjeUtil.instance().isMockReceitaEnabled());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
			}
		} else{
			tipoDocumento = "CPJ";
		}
		
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = 
				buscarDocumentoIdentificacao(this.nrDocumentoPrincipal, tipoDocumento, getPais(), true, this.inTipoPessoa);
		
		if (pessoaDocumentoIdentificacao != null){
			pessoaEncontradaBanco = true;
			
			Pessoa obPessoa = (Pessoa) EntityUtil.removeProxy(pessoaDocumentoIdentificacao.getPessoa());
			
			if( obPessoa instanceof PessoaFisica ){
				pessoaFisica = ((PessoaFisica) obPessoa);
			}else{			
				pessoaJuridica = ((PessoaJuridica) obPessoa);
			}
		} else {
			ocorreuErroWsReceita = false;
			try{
				if (!brasileiro){
					confirmouCadastroErroWsReceita = true;
					return;
				}
				if (inTipoPessoa == TipoPessoaEnum.F){
					DadosReceitaPessoaFisica dadosReceita = (DadosReceitaPessoaFisica) ConsultaClienteWebService.instance().consultaDados(TipoPessoaEnum.F, nrDocumentoPrincipal, true);

					if (dadosReceita == null){
						adicionarMensagemErro("preCadastroPessoaBean.pessoa_fisica_nao_encontrada");
						return;
					}

					if (existeImpedimentos(dadosReceita)){
						return;
					}

					pessoaEncontradaReceita = true;
					preencherPessoaFisicaPelaReceita(dadosReceita);
				} else {
					/* PJEII-7863 e PJEII-16850
					 * 
					 * Se chegou aqui é porque não encontrou documento ativo para o cnpj informado. 
					 * Então busca por documentos ativos ou inativos para o cnpj base. 
					 * Caso encontre algum documento é porque já existe cadastro da Pessoa Jurídica (PJ),
					 * porém, se o documento estiver inativo, o mesmo será ativado se os dados estiverem
					 * de acordo com os obtidos na Receita Federal.
					 * Caso não possua documento segue o fluxo criando nova PJ com seu documento.
					 * (Ver regras de negócio RN524 e RN528)
					 */
					
					String nrCnpjInformado = this.nrDocumentoPrincipal;
					String nrCnpjMatriz = calcularCnpjBase(InscricaoMFUtil.retiraMascara(nrCnpjInformado).substring(0, 8));
					PessoaDocumentoIdentificacao documentoIdentificacao = null;
					
					if(nrCnpjInformado.equals(nrCnpjMatriz)){
						// O CNPJ informado é o da matriz
						DadosReceitaPessoaJuridica dadosPJ = (DadosReceitaPessoaJuridica) ConsultaClienteWebService
								.instance().consultaDados(TipoPessoaEnum.J, this.nrDocumentoPrincipal, true);
						
						if (dadosPJ == null) {
							adicionarMensagemErro("preCadastroPessoaBean.pessoa_juridica_nao_encontrada");
							return;
						}
						
						documentoIdentificacao = buscarDocumentoIdentificacao(nrCnpjMatriz, tipoDocumento, getPais(), null, this.inTipoPessoa);
						
						if(documentoIdentificacao != null){
							pessoaEncontradaBanco = true;
							this.pessoaJuridica = (PessoaJuridica) EntityUtil.removeProxy(documentoIdentificacao.getPessoa());

							// PJEII-7863 : Obtém dados da receita e ativa documento caso o Nome esteja de acordo com a Razão Social ou Nome Fantasia
							if (!documentoIdentificacao.getAtivo()) {
								//Verifica se o nome da PJ está de acordo com o nome obtido na Receita Federal e nesse caso ativa o documento
								if (this.pessoaJuridica.getNome()!=null && !this.pessoaJuridica.getNome().trim().equals("") 
										&& (this.pessoaJuridica.getNome().equals(dadosPJ.getRazaoSocial()) 
												|| this.pessoaJuridica.getNome().equals(dadosPJ.getNomeFantasia()))){
									documentoIdentificacao.setAtivo(true);
								}
							}							
						} else {
							if (existeImpedimentos(dadosPJ)){
								return;
							}
							this.preencherPessoaJuridicaPelaReceita(dadosPJ);							
						}
						pessoaJuridica.setMatriz(Boolean.TRUE);
					} else {
						// O CNPJ informado é o de uma filial
						PessoaJuridica matriz = null;
						DadosReceitaPessoaJuridica dadosReceitaFilial = (DadosReceitaPessoaJuridica) ConsultaClienteWebService
								.instance().consultaDados(TipoPessoaEnum.J, this.nrDocumentoPrincipal, true);
						
						if(dadosReceitaFilial != null){
						
							// Tenta inserir o relacionamento entre a pessoa jurídica filial e sua matriz 
							DadosReceitaPessoaJuridica dadosReceitaMatriz = (DadosReceitaPessoaJuridica) ConsultaClienteWebService
									.instance().consultaDados(TipoPessoaEnum.J, nrCnpjMatriz, true);
							if(dadosReceitaMatriz != null){
								PessoaDocumentoIdentificacao diMatriz = buscarDocumentoIdentificacao(nrCnpjMatriz, tipoDocumento, getPais(), null, this.inTipoPessoa);
								
								if(diMatriz == null){
									matriz = montaPessoaJuridicaMatriz(dadosReceitaMatriz);
								} else {
									matriz = EntityUtil.find(PessoaJuridica.class, diMatriz.getPessoa().getIdPessoa());
								}
							}
							
							PessoaDocumentoIdentificacao diFilial = buscarDocumentoIdentificacao(this.nrDocumentoPrincipal, tipoDocumento, getPais(), null, this.inTipoPessoa);
							
							if(diFilial != null){
								pessoaEncontradaBanco = true;
								this.pessoaJuridica = (PessoaJuridica) EntityUtil.removeProxy(diFilial.getPessoa());

								// PJEII-7863 : Obtém dados da receita e ativa documento caso o Nome esteja de acordo com a Razão Social ou Nome Fantasia
								if (!diFilial.getAtivo()) {
									//Verifica se o nome da PJ está de acordo com o nome obtido na Receita Federal e nesse caso ativa o documento
									if (this.pessoaJuridica.getNome()!=null && !this.pessoaJuridica.getNome().trim().equals("") 
											&& (this.pessoaJuridica.getNome().equals(dadosReceitaFilial.getRazaoSocial()) 
													|| this.pessoaJuridica.getNome().equals(dadosReceitaFilial.getNomeFantasia()))){
										diFilial.setAtivo(true);
									}
								}									
							} else {
								if (existeImpedimentos(dadosReceitaFilial)){
									return;
								}
								this.preencherPessoaJuridicaPelaReceita(dadosReceitaFilial, matriz);									
							}
							pessoaJuridica.setMatriz(Boolean.FALSE);
						} else {
							adicionarMensagemErro("preCadastroPessoaBean.pessoa_juridica_nao_encontrada");
							return;
						}
					}
				}

				// FacesMessages.instance().clear();
				pessoaEncontradaReceita = true;
				return;

			} catch (WebserviceReceitaException rfbe){
				Identity identity = ComponentUtil.getComponent(Identity.class);
				if (identity.hasRole("advogado")){
					adicionarMensagemErro("preCadastroPessoaBean.erro_ws_receita_advogado");
				}
				else{
					adicionarMensagemErro("preCadastroPessoaBean.erro_ws_receita");
					adicionarMensagemErro(rfbe.getMessage());
				}

				ocorreuErroWsReceita = true;
				setLiberaLocalizacao(true);

			} catch (Exception e){
				adicionarMensagemErro(e.getLocalizedMessage());
				logger.error(e.getMessage(), e);
			}

		}
		if((pessoaFisica != null && pessoaFisica.getNomeSocial() != null) || (pessoaAdvogado != null && pessoaAdvogado.getPessoa().getNomeSocial() != null ) ) {
			informarNomeSocial = true;
		}
	}

	/**
	 * Metodo que verifica se existe alguma Pessoa cadastrada no polo.
	 * 
	 * @param Documento principal
	 * @return true se ja existir alguma Pessoa no polo.
	 */

	/**
	 * Insere um registro na tabela do tipoEspecializado, completando a hierarquia de pessoa física
	 * 
	 * @param pessoaFisica
	 * @param tipoEspecializado
	 * @param tipoInscricaoAdvogado Se estiver especializando para PessoaAdvogado, tem que informar este campo obrigatório
	 * @return
	 */

	/**
	 * O sistema deve IMPEDIR o cadastramento de pessoa FÍSICA caso a situação dela na receita for 5, 8 ou 9 e de pessoa JURÍDICA quando for 01 ou 08,
	 * emitindo mensagem para o usuário de acordo com a situação encontrada.
	 * 
	 * Nos demais casos em que a situação seja diferente de 0(zero) para PF ou 02(dois) para PJ, o sistema permite o cadastro mas exibe mensagem
	 * informando sobre a situação do cadastro. CPF: 0 = Regular 1 = Cancelada por Encerramento de Espólio 2 = Suspensa 3 = Cancelada por Óbito sem
	 * Espolio 4 = Pendente de Regularização 5 = Cancelada por Multiplicidade 8 = Nula 9 = Cancelada de Ofício
	 * 
	 * CNPJ: 01 NULA, 02 ATIVA, 03 SUSPENSA, 04 INAPTA, 08 BAIXADA.
	 */
	public boolean existeImpedimentos(DadosReceitaPessoaFisica dados){
		Identity identity = ComponentUtil.getComponent(Identity.class);
		Boolean isServidor = identity.hasRole("servidor");
		int situacaoCadastral = Integer.parseInt(dados.getSituacaoCadastral());

		switch (situacaoCadastral){

		case SituacaoCadastroPessoaFisicaReceita.CANCELADA_ENCERRAMENTO_ESPOLIO:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_cancelada_encerramento_espolio");
			break;

		case SituacaoCadastroPessoaFisicaReceita.SUSPENSA:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_suspensa");
			break;

		case SituacaoCadastroPessoaFisicaReceita.CANCELADA_OBITO_SEM_ESPOLIO:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_cancelada_obito_sem_espolio");
			break;

		case SituacaoCadastroPessoaFisicaReceita.PENDENTE_REGULARIZACAO:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_pendente_regularizacao");
			break;

		case SituacaoCadastroPessoaFisicaReceita.CANCELADA_MULTIPLICIDADE:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_cancelada_multiplicidade");
			if (!isServidor)
				return true;
			break;

		case SituacaoCadastroPessoaFisicaReceita.NULA:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_nula");
			if (!isServidor)
				return true;
			break;

		case SituacaoCadastroPessoaFisicaReceita.CANCELADA_DE_OFICIO:
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_fisica_receita_cancelada_de_oficio");
			if (!isServidor)
				return true;
			break;

		default:
			break;
		}

		return false;

	}

	public boolean existeImpedimentos(DadosReceitaPessoaJuridica dados){
		Identity identity = ComponentUtil.getComponent(Identity.class);
		Boolean isServidor = identity.hasRole("servidor");
		String situacaoCadastral = dados.getDescricaoSituacaoCadastral();

		if (situacaoCadastral.equals(SituacaoCadastroPessoaJuridicaReceita.NULA)){
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_juridica_receita_nula");
			if (!isServidor)
				return true;
		}

		if (situacaoCadastral.equals(SituacaoCadastroPessoaJuridicaReceita.SUSPENSA)){
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_juridica_receita_suspensa");
		}

		if (situacaoCadastral.equals(SituacaoCadastroPessoaJuridicaReceita.INAPTA)){
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_juridica_receita_inapta");
		}

		if (situacaoCadastral.equals(SituacaoCadastroPessoaJuridicaReceita.BAIXADA)){
			adicionarMensagemErro("preCadastroPessoaBean.situacao_cadastro_pessoa_juridica_receita_baixada");
			if (!isServidor)
				return true;
		}

		return false;

	}

	/**
	 * Ao se pesquisar um advogado, os seguintes fluxos podem acontecer:
	 * 
	 * 1) O advogado já estava cadastrado como PessoaAdvogado 1.1) Deixar o cadastro do advogado como estava na base? 1.2) Atualizar dados da pessoa
	 * do advogado com o que vem da OAB?(Ex: Nome pai, mae, telefone) 1.3) Atualizar dados de endereço do advogado com o que vem da OAB?(Remover os
	 * existentes e inserir o que vem da OAB?, Tentar atualizar o endereço, utilizando como chave de comparação o CEP?) 1.4) Atualizar os documentos
	 * do tipo OAB do advogado com o que vem da OAB? (Atualizar as informações de acordo com a chave NR OAB + UF OAB + TIPO OAB?)
	 * 
	 * 2) O advogado já estava cadastrado como outro tipo de pessoa(PessoaFisica, PessoaProcurador, PessoaServidor,...) 2.1) Se PessoaFisica, migrar
	 * chave para a tabela PessoaAdvogado para permitir que vire um advogado? 2.2) Se PessoaProcurador ou outro tipo específico de PessoaFisica,
	 * remover registro da tabela específica e migrar chave para PessoaAdvogado? (Se optarmos por desativar, e não deletar, o registro da tabela
	 * específica, como o hibernate saberia qual tabela específica utilizar, já que teria o mesmo ID de pessoa em PessoaProcurador e PessoaAdvogado
	 * por ex?) 2.3) Atualizar dados com o que vem da OAB?(Conforme questionamentos 1.1,1.2,1.3) 2.4) Ativar o registro de usuário deste advogado?
	 * 2.5) Adicionar os documentos OAB ao advogado
	 * 
	 * 3) O advogado não estava cadastrado na base e foi encontrado na OAB 3.1) Cadastrá-lo com PessoaAdvogado normalmente, inserindo todas as
	 * informações que vem da OAB 3.2) Permitir que a pessoa selecione a OAB que o advogado está utilizando? Mostrar todas ou só as ativas? A
	 * revogação é para todas as OABs ou pode ser por documento? 3.2) O advogado pode atuar no estado com qualquer OAB ou tem que ser a do estado?
	 * Como funciona na JF? 3.3) O que fazer se o registro do advogado na OAB estiver suspenso ou algo assim?
	 * 
	 * 4) O advogado não estava cadastrado na base e não foi encontrado na OAB 4.1) Emitir mensagem 'Não foi possível identificar a pessoa como um
	 * advogado' ou similar?
	 * @throws PJeBusinessException 
	 */
	public void pesquisarAdvogado() throws PJeBusinessException{
		if(isPesquisaValida()){
			if(nrDocumentoPrincipal != null) {
				pesquisarAdvogadoPorCPF();
			} else {
				pesquisarAdvogadoPorOAB();
			}
		}
		if(pessoaAdvogado != null && pessoaAdvogado.getPessoa().getNomeSocial() != null ) {
			informarNomeSocial = true;
		}
	}
	
	/**
	 * Verifica os parametros da pesquisa para saber se a esta valida.
	 * 
	 * @return True se estiver valida a pesquisa
	 */
	private boolean isPesquisaValida(){
		boolean retorno = true;
		if(nrDocumentoPrincipal != null && InscricaoMFUtil.retiraMascara(nrDocumentoPrincipal).equals("")) {
			nrDocumentoPrincipal = null;
			retorno = false;
		}
		if (nrDocumentoPrincipal == null && (nrOAB == null || ufOAB == null)){
			adicionarMensagemErro("Favor informar um parâmetro de pesquisa");
			retorno = false;
		}
		return retorno;
	}

	private void pesquisarAdvogadoPorCPF() throws PJeBusinessException{
		pesquisarAdvogadoPorCPF(null);
	}
	
	@SuppressWarnings("unchecked")
	private void pesquisarAdvogadoPorCPF(List<DadosAdvogadoOAB> inscricoes) throws PJeBusinessException{
		pessoaFisica = null;
		pessoaJuridica = null;
		pessoaAdvogado = null;
		pessoaEncontradaBanco = false;
		pessoaEncontradaReceita = false;
		PessoaAdvogado adv = null;
		
		/* VALIDAR FORMATO DO NUMERO DOS DOCUMENTOS */
		if (nrDocumentoPrincipal == null){
			adicionarMensagemErro("Favor informar o número do CPF");
			return;
		}else if(!InscricaoMFUtil.isCpfValido(InscricaoMFUtil.retiraMascara(nrDocumentoPrincipal))){
			adicionarMensagemErro("O número do CPF não é válido.");
			return;
		}
		PessoaFisica pessoa = null;
		ConsultaClienteOAB consultaOAB = (ConsultaClienteOAB) Component.getInstance("consultaClienteOAB");
		boolean mesclar = false;
		boolean fallback = false;
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
		try{
			// Recupera a pessoa já cadastrada no sistema ou já efetua a consulta à Receita para realizar esse cadastro			
			pessoa = (PessoaFisica) pessoaService.findByInscricaoMF(InscricaoMFUtil.retiraMascara(nrDocumentoPrincipal));
			if(!Pessoa.instanceOf(pessoa, PessoaAdvogado.class)){
				//o advogado ainda não foi consultado no serviço da OAB	
				if(inscricoes == null) {
					// A pessoa já foi localizada, mas ainda não tem a especialização para atuar como advogado.
					consultaOAB.consultaDados(nrDocumentoPrincipal, false);
					inscricoes = consultaOAB.getDadosAdvogadoList();
					if(inscricoes.size() == 0){
						// Não há registros na OAB quanto à pessoa. Dispara exceção.
						throw new AdvogadoNaoEncontradoException();
					}
				}
				pessoa = (PessoaFisica) pessoaService.especializa(pessoa, PessoaAdvogado.class);
				adv = pessoa.getPessoaAdvogado();
				mesclar = true;
			}
		}catch(PJeBusinessException e){
			if(!e.getCode().equals("pje.consultaClienteReceitaPFCNJ.error.erroGenerico")){
				// Erro diverso do relativo à consulta da Receita Federal. Abortando.
				String mensagem = "Erro ao tentar cadastrar o usuário. Erro: %s";
				adicionarMensagemErro(String.format(mensagem, e.getLocalizedMessage()));
				return;
			}else{
				fallback = true;
			}
		}catch (AdvogadoNaoEncontradoException e) {
			adicionarMensagemErro("preCadastroPessoaBean.advogado_nao_encontrado");
			return;
		}catch (Throwable e) {
			FacesMessages.instance().clear();
			adicionarMensagemErro("preCadastroPessoaBean.erro_ws_oab");
			return;
		}
		if(fallback){
			// Outro erro quando da consulta à Receita. Insistindo em tentar cadastrar utilizando dados do CNA/OAB.
			try{
				if(inscricoes == null) {
					consultaOAB.consultaDados(nrDocumentoPrincipal, false);
					inscricoes = consultaOAB.getDadosAdvogadoList();
					if(inscricoes.size() == 0){
						throw new AdvogadoNaoEncontradoException();
					}
				}
				pessoa = montaPessoa(inscricoes);
				pessoa = (PessoaFisica) pessoaService.persist(pessoa);
				pessoa = (PessoaFisica) pessoaService.especializa(pessoa, PessoaAdvogado.class);
				adv = pessoa.getPessoaAdvogado();
				mesclar = true;
			}catch (AdvogadoNaoEncontradoException e) {
				adicionarMensagemErro("preCadastroPessoaBean.advogado_nao_encontrado");
				return;
			}catch (Throwable e) {
				FacesMessages.instance().clear();
				adicionarMensagemErro("preCadastroPessoaBean.erro_ws_oab");
				return;
			}
		}
		// A essa altura, o advogado tem que existir
		if(mesclar){
			DadosAdvogadoOAB inscricaoPrincipal = recuperaPrincipal(inscricoes);
			for(DadosAdvogadoOAB insc: inscricoes){
				if(inscricaoPrincipal == null){
					inscricaoPrincipal = insc;
				}
				pessoaService.adicionaInscricaoOAB(pessoa, insc);
			}
			if(inscricaoPrincipal != null){
				//** aqui estava duplicando o CPF **
				//pessoaService.adicionaInscricaoMF(pessoa, InscricaoMFUtil.retiraMascara(inscricaoPrincipal.getNumCPF()));
				adicionarEnderecoAdvogado(adv, inscricaoPrincipal, pessoa.getEnderecoList());
				EstadoManager estadoManager = (EstadoManager) Component.getInstance("estadoManager");
				adv.setUfOAB(estadoManager.findBySigla(inscricaoPrincipal.getUf()));
				adv.setNumeroOAB(inscricaoPrincipal.getNumInscricao());
				if (StringUtils.isNotBlank(inscricaoPrincipal.getLetra())) {
					adv.setLetraOAB(inscricaoPrincipal.getLetra());
				} else if (StringUtils.isNotBlank(inscricaoPrincipal.getNumInscricao())) {
					PessoaAdvogado pa = ComponentUtil.getComponent(PessoaAdvogadoManager.class).recuperarInscricao(inscricaoPrincipal);
					if (pessoa != null && pessoa.getPessoaAdvogado() != null) {
						if (StringUtils.isNotBlank(pa.getLetraOAB())) {
							adv.setLetraOAB(pa.getLetraOAB());
							pessoa.getPessoaAdvogado().setLetraOAB(pa.getLetraOAB());
						}
						if (StringUtils.isNotBlank(pa.getNumeroOAB())) {
							pessoa.getPessoaAdvogado().setNumeroOAB(pa.getNumeroOAB());
						}
					}
				}
				adv.setDataCadastro(inscricaoPrincipal.getDataCadastro());
				if(inscricaoPrincipal.getTipoInscricao().equalsIgnoreCase("advogado")){
					adv.setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum.A);
				}else if(inscricaoPrincipal.getTipoInscricao().equalsIgnoreCase("suplementar")){
					adv.setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum.S);
				}else{
					adv.setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum.E);
				}
			}
			EntityUtil.flush();
		}
		pessoaAdvogado = pessoa.getPessoaAdvogado();
		pessoaEncontradaBanco = true;
		pessoaEncontradaReceita = true;
		
	}

	private void pesquisarAdvogadoPorOAB() throws PJeBusinessException{
		if (nrOAB == null || ufOAB == null){
			adicionarMensagemErro("Favor informar o número da OAB");
			return;
		}
		PessoaFisica pessoa = null;
		ConsultaClienteOAB consultaOAB = (ConsultaClienteOAB) Component.getInstance("consultaClienteOAB");
		List<DadosAdvogadoOAB> inscricoes = new ArrayList<DadosAdvogadoOAB>(0);
		try{			
			inscricoes.addAll(consultaOAB.consultaDados(null, nrOAB, ufOAB.getCodEstado(), false));
			if(inscricoes.size() == 0){
				// Não há registros na OAB quanto à pessoa. Dispara exceção.
				throw new AdvogadoNaoEncontradoException();
			}
			pessoa = montaPessoa(inscricoes);
		}catch(PJeBusinessException e){
			adicionarMensagemErro("Erro ao tentar cadastrar o usuário: " + e.getLocalizedMessage());
			return;
		}catch (AdvogadoNaoEncontradoException e) {
			if(e.getErrorMsg() != null){
				adicionarMensagemErro(e.getErrorMsg());
			}else{
				adicionarMensagemErro("preCadastroPessoaBean.advogado_nao_encontrado");
			}
			return;
		}catch (Throwable e) {
			FacesMessages.instance().clear();
			adicionarMensagemErro("preCadastroPessoaBean.erro_ws_oab");
			return;
		}
		nrDocumentoPrincipal = pessoa.getNumeroCPF();
		//passa os dados do advogado para evitar um novo acesso ao serviço da OAB 
		pesquisarAdvogadoPorCPF(inscricoes);
	}

	public PessoaDocumentoIdentificacao pesquisarPessoaPorDocumento(PessoaDocumentoIdentificacao p){

		Criteria criteria = HibernateUtil.getSession().createCriteria(PessoaDocumentoIdentificacao.class);
		if (p.getNumeroDocumento() != null){
			criteria.add(Restrictions.eq("numeroDocumento", p.getNumeroDocumento()));
		}
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		PessoaDocumentoIdentificacao pessoa = (PessoaDocumentoIdentificacao)criteria.uniqueResult();
		return pessoa;
	}

	@SuppressWarnings("unchecked")
	public List<TipoDocumentoIdentificacao> getTipoDocumentoAlternativo(){

		if (getParametro("tipoPessoa") != null){
			this.inTipoPessoa = TipoPessoaEnum.valueOf(getParametro("tipoPessoa"));
		}

		String s = "select o from TipoDocumentoIdentificacao o " + "where o.identificador = false "
			+ "and o.tipoPessoa = :tipoPessoa and o.ativo = true";
		Query q = EntityUtil.getEntityManager().createQuery(s);
		q.setParameter("tipoPessoa", inTipoPessoa);
		List<TipoDocumentoIdentificacao> tipoDocIdentificacaoList = q.getResultList();

		return tipoDocIdentificacaoList;

	}

	public void resetarBean(){

		this.setPessoaFisica((PessoaFisica) null);
		this.setPessoaJuridica(null);
		this.setPessoaAutoridade(null);
		this.setPessoaAdvogado(null);
		this.setIsBrasileiro(Boolean.TRUE);
		this.setNrDocumentoPrincipal(null);
		this.setDocumentoAlternativo(null);
		this.setNomeAlcunhaPessoaSuja(null);
		this.setNomePessoaErroWsReceita(null);
		this.setIsPessoaNaoIndividualizada(false);
		this.setHasDocumentoAlternativo(null);
		this.setHasPessoaFisica(null);
		this.setHasPessoaJuridica(null);
		this.setHasAdvogado(null);
		this.setDtNascimentoAbertura(null);
		this.setPessoaEncontradaBanco(Boolean.FALSE);
		this.setPessoaEncontradaReceita(false);
		this.setIsConfirmado(false);
		this.setPais(null);
		this.setOcorreuErroWsReceita(Boolean.FALSE);
		this.setOrgaoPubSelec(null);
		this.setNomePessoaJuridica(null);
		this.confirmouCadastroErroWsReceita = Boolean.FALSE;
		
		Contexts.removeFromAllContexts(PessoaAutoridadeSuggestBean.NAME);
		Contexts.removeFromAllContexts(OrgaoPublicoList.NAME);
	}

	public void setInTipoPessoa(TipoPessoaEnum inTipoPessoa){

		// Se estiver mudando o tipo, retira as instancias da pessoa ja encontrada
		if (inTipoPessoa != this.getInTipoPessoa()){
			this.resetarBean();
		}

		this.inTipoPessoa = inTipoPessoa;

	}

	public TipoPessoaEnum getInTipoPessoa(){
		if(!getTipoPessoaItems().isEmpty()){
			verificarTipoPessoa();
		}
		return inTipoPessoa;
	}
	
	/**
	 * Verifica o TipoPessoa pois, se um TipoParte tiver uma configuração que contém
	 * um tipoPessoa diferente de outra. Ao mudar a opção na combo retornaria os dados
	 * da anterior. 
	 */
	public void verificarTipoPessoa(){
		List<TipoPessoaEnum>  tipoPessoas = getTipoPessoaItems();
		isContemTipoPessoa(inTipoPessoa, tipoPessoas);
		if(!isContemTipoPessoa(inTipoPessoa, tipoPessoas)){
			inTipoPessoa = tipoPessoas.get(0);
		}
	}

	/**
	 * Confere se o tipoPessoaSetada está contigo na lista de opçãos carregada.
	 * @param tipoPessoaSetada
	 * @param tipoPessoas
	 * @return
	 */
	private boolean isContemTipoPessoa(TipoPessoaEnum tipoPessoaSetada, List<TipoPessoaEnum> tipoPessoas) {
		boolean contem = Boolean.FALSE;
		for(TipoPessoaEnum tipoPessoa : tipoPessoas){
			if(tipoPessoaSetada.equals(tipoPessoa)){
				contem = Boolean.TRUE;
			}
		}
		return contem;
	}

	public List<TipoPessoaEnum> getTipoPessoaItems(){
		if (!tipoPessoaItemsCache.isEmpty()) {
			return tipoPessoaItemsCache;
		} else {
			TipoParte tipoParte = ProcessoParteHome.instance().getInstance().getTipoParte();
			List<TipoPessoaEnum> tipos = new ArrayList<TipoPessoaEnum>(0);
			
			ClasseJudicial classeJudicial = ProcessoTrfHome.instance().getInstance().getClasseJudicial();
			
			TipoParteConfigClJudicialManager tipoParteConfigClJudicialManager = ComponentUtil.getComponent(TipoParteConfigClJudicialManager.class);
			List<TipoParteConfigClJudicial> partesClasseJudicial = tipoParteConfigClJudicialManager.recuperarTipoParteConfiguracao(classeJudicial);
			
			TipoParteConfiguracao conf = new TipoParteConfiguracao();
			
			for(TipoParteConfigClJudicial tpParteConfigClJudicial : partesClasseJudicial){
				if(tpParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().equals(tipoParte)){
					conf = tpParteConfigClJudicial.getTipoParteConfiguracao();
					break;
				}
			}
			
			return processarTipoPessoa(tipos, conf);
		}
	}
	
	/**
	 * Processa o TipoPessoa para devida apresentação na tela.
	 * @param tipos
	 * @param conf
	 * @return
	 */
	private List<TipoPessoaEnum> processarTipoPessoa(List<TipoPessoaEnum> tipos, TipoParteConfiguracao conf) {
		if(isTipoPessoa(conf,TipoPessoaEnum.F)){
			tipos.add(TipoPessoaEnum.F);
		}
		if(isTipoPessoa(conf,TipoPessoaEnum.J)){
			tipos.add(TipoPessoaEnum.J);
		}
		if(isTipoPessoa(conf,TipoPessoaEnum.A)){
			tipos.add(TipoPessoaEnum.A);
		}
		
		return tipos;
	}
	
	public void onCompleteSelectTipoParte() {
		resetarBean();
		if (ParametroJtUtil.instance().justicaFederal()) {
			configurarOpcoesTipoPessoa();
		}
	}
	
	private void configurarOpcoesTipoPessoa() {
		setTipoPessoaItemsCache(new ArrayList<TipoPessoaEnum>());
		List<TipoPessoaEnum> tipoPessoaItems = getTipoPessoaItems();
		if (tipoPessoaItems.contains(TipoPessoaEnum.J)) {
			setInTipoPessoa(TipoPessoaEnum.J);
			setIsOrgaoPublico(Boolean.TRUE);
		} else if (tipoPessoaItems.contains(TipoPessoaEnum.F)) {
			setInTipoPessoa(TipoPessoaEnum.F);
			setIsOrgaoPublico(Boolean.FALSE);
		} else {
			setInTipoPessoa(TipoPessoaEnum.A);
			setIsOrgaoPublico(Boolean.FALSE);
		}
		setTipoPessoaItemsCache(tipoPessoaItems);
	}
	
	/**
	 * Retorna o TipoPessoa
	 * @param tipoParteConfiguracao
	 * @param tipoPessoaEnum
	 * @return
	 */
	private boolean isTipoPessoa(TipoParteConfiguracao tipoParteConfiguracao, TipoPessoaEnum tipoPessoaEnum) {
		if(tipoPessoaEnum.equals(TipoPessoaEnum.F)){
			return isTipoPessoaFisica(tipoParteConfiguracao);
		}if(tipoPessoaEnum.equals(TipoPessoaEnum.J)){
			return isTipoPessoaJuridica(tipoParteConfiguracao);
		}if(tipoPessoaEnum.equals(TipoPessoaEnum.A)){
			return isTipoPessoaEnteAutoridade(tipoParteConfiguracao);
		}
		return Boolean.FALSE;
		
	}
	
	private boolean isTipoPessoaFisica(TipoParteConfiguracao tipoParteConfiguracao){
		return tipoParteConfiguracao.getTipoPessoaFisica()  != null && tipoParteConfiguracao.getTipoPessoaFisica();
	}
	
	private boolean isTipoPessoaJuridica(TipoParteConfiguracao tipoParteConfiguracao){
		return tipoParteConfiguracao.getTipoPessoaJuridica() != null && tipoParteConfiguracao.getTipoPessoaJuridica();
	}
	
	private boolean isTipoPessoaEnteAutoridade(TipoParteConfiguracao tipoParteConfiguracao){
		return tipoParteConfiguracao.getEnteAutoridade() != null && tipoParteConfiguracao.getEnteAutoridade();
	}
	

	public void setIsBrasileiro(Boolean isBrasileiro){
		this.brasileiro = isBrasileiro;
	}

	public Boolean getIsBrasileiro(){
		return brasileiro;
	}

	public void setNrDocumentoPrincipal(String nrDocumentoPrincipal){
		this.nrDocumentoPrincipal = nrDocumentoPrincipal;
	}

	public String getNrOAB() {
		return nrOAB;
	}

	public void setNrOAB(String nrOAB) {
		this.nrOAB = nrOAB;
	}

	public Estado getUfOAB() {
		return ufOAB;
	}

	public void setUfOAB(Estado ufOAB) {
		this.ufOAB = ufOAB;
	}

	public String getNrDocumentoPrincipal(){
		return nrDocumentoPrincipal;
	}

	public void setPessoaFisica(PessoaFisica pessoaFisica){
		this.pessoaFisica = pessoaFisica;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaFisica(PessoaFisica)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaFisica(PessoaFisicaEspecializada pessoa){
		setPessoaFisica(pessoa != null ? pessoa.getPessoa() : (PessoaFisica) null);
	}

	public PessoaFisica getPessoaFisica(){
		return pessoaFisica;
	}

	public void setHasPessoaFisica(Boolean hasPessoaFisica){
		this.hasPessoaFisica = hasPessoaFisica;
	}

	public Boolean getHasPessoaFisica(){
		return (this.pessoaFisica instanceof PessoaFisica);
	}

	public void setHasPessoaJuridica(Boolean hasPessoaJuridica){
		this.hasPessoaJuridica = hasPessoaJuridica;
	}

	public Boolean getHasPessoaJuridica(){
		return (this.pessoaJuridica instanceof PessoaJuridica);
	}

	public void setHasPessoaAutoridade(Boolean hasPessoaAutoridade){
		this.hasPessoaAutoridade = hasPessoaAutoridade;
	}

	public Boolean getHasPessoaAutoridade(){
		return (this.pessoaAutoridade instanceof PessoaAutoridade);
	}

	public void setNomeAlcunhaPessoaSuja(String nomeAlcunhaPessoaSuja){
		this.nomeAlcunhaPessoaSuja = nomeAlcunhaPessoaSuja;
	}

	public String getNomeAlcunhaPessoaSuja(){
		return nomeAlcunhaPessoaSuja;
	}

	public PessoaJuridica getPessoaJuridica(){
		return pessoaJuridica;
	}

	public void setPessoaJuridica(PessoaJuridica pessoaJuridica){
		this.pessoaJuridica = pessoaJuridica;
	}

	public void setDestino(String destino){

		this.destino = destino;
	}

	public String getDestino(){
		return destino;
	}

	public void setDocumentoAlternativo(PessoaDocumentoIdentificacao documentoAlternativo){
		this.documentoAlternativo = documentoAlternativo;
	}

	public PessoaDocumentoIdentificacao getDocumentoAlternativo(){

		if (documentoAlternativo == null){
			documentoAlternativo = new PessoaDocumentoIdentificacao();
		}
		return documentoAlternativo;
	}

	public void setPessoaEncontradaBanco(Boolean pessoaEncontradaBanco){
		this.pessoaEncontradaBanco = pessoaEncontradaBanco;
	}

	public Boolean getPessoaEncontradaBanco(){
		return pessoaEncontradaBanco;
	}

	public void setPessoaEncontradaReceita(Boolean pessoaEncontradaReceita){
		this.pessoaEncontradaReceita = pessoaEncontradaReceita;
	}

	public Boolean getPessoaEncontradaReceita(){
		return pessoaEncontradaReceita;
	}

	public void setHasDocumentoAlternativo(Boolean hasDocumentoAlternativo){
		this.hasDocumentoAlternativo = hasDocumentoAlternativo;
	}

	public Boolean getHasDocumentoAlternativo(){
		return hasDocumentoAlternativo;
	}

	public void setDtNascimentoAbertura(Date dtNascimentoAbertura){
		this.dtNascimentoAbertura = dtNascimentoAbertura;
	}

	public Date getDtNascimentoAbertura(){
		return dtNascimentoAbertura;
	}

	public void setIsConfirmado(Boolean isConfirmado){
		this.isConfirmado = isConfirmado;
	}

	public Boolean getIsConfirmado(){
		return isConfirmado;
	}

	public void setIsPessoaNaoIndividualizada(Boolean isPessoaNaoIndividualizada){
		this.isPessoaNaoIndividualizada = isPessoaNaoIndividualizada;
	}

	public Boolean getIsPessoaNaoIndividualizada(){
		return isPessoaNaoIndividualizada;
	}

	public void setDestinoInterno(String destinoInterno){
		this.destinoInterno = destinoInterno;
	}

	public String getDestinoInterno(){
		return destinoInterno;
	}

	public void setHasAdvogado(Boolean hasAdvogado){
		this.hasAdvogado = hasAdvogado;
	}

	public Boolean getHasAdvogado(){
		return (this.pessoaAdvogado instanceof PessoaAdvogado);
	}

	public void setPessoaAdvogado(PessoaAdvogado pessoaAdvogado){
		this.pessoaAdvogado = pessoaAdvogado;
	}

	public PessoaAdvogado getPessoaAdvogado(){
		return pessoaAdvogado;
	}

	public void setStrPessoaFisicaEspecializada(String strPessoaFisicaEspecializada){
		this.strPessoaFisicaEspecializada = strPessoaFisicaEspecializada;
	}

	public String getStrPessoaFisicaEspecializada(){
		return strPessoaFisicaEspecializada;
	}

	public Pessoa getPessoa(){
		if (this.getHasAdvogado()){
			return pessoaAdvogado.getPessoa();
		}
		else if (this.getHasPessoaFisica()){
			return pessoaFisica;
		}
		else if (this.getHasPessoaJuridica()){
			return pessoaJuridica;
		}
		else if (this.getHasPessoaAutoridade()){
			return pessoaAutoridade;
		} else if (this.orgaoPubSelec instanceof Pessoa) {
			return (Pessoa)this.orgaoPubSelec;
		}
		return null;
	}

	public void setPessoaAutoridade(PessoaAutoridade pessoaAutoridade){
		this.pessoaAutoridade = pessoaAutoridade;
	}

	public PessoaAutoridade getPessoaAutoridade(){
		return this.pessoaAutoridade;
	}

	public void cadastrarSemWS(){
		ocorreuErroWsReceita = false;
		this.confirmouCadastroErroWsReceita = true;
	}

	public Boolean getOcorreuErroWsReceita(){
		return ocorreuErroWsReceita;
	}

	public void setOcorreuErroWsReceita(Boolean ocorreuErroWsReceita){
		this.ocorreuErroWsReceita = ocorreuErroWsReceita;
	}

	public Boolean getConfirmouCadastroErroWsReceita(){
		return confirmouCadastroErroWsReceita;
	}

	public String getNomePessoaErroWsReceita(){
		return nomePessoaErroWsReceita;
	}

	public void setNomePessoaErroWsReceita(String nomePessoaErroWsReceita){
		this.nomePessoaErroWsReceita = nomePessoaErroWsReceita;
	}

	public Pais getPais(){
		return pais;
	}

	public void setPais(Pais pais){
		this.pais = pais;
	}

	@SuppressWarnings("unchecked")
	public List<Pais> getPaises(){
		if (getParametro("tipoPessoa") != null){
			this.inTipoPessoa = TipoPessoaEnum.valueOf(getParametro("tipoPessoa"));
		}

		String s = "select o from Pais o order by descricao";
		Query q = EntityUtil.getEntityManager().createQuery(s);
		List<Pais> paises = q.getResultList();
		return paises;
	}

	public void resetarVariaveisPesquisa(){
		confirmouCadastroErroWsReceita = false;
		pessoaEncontradaBanco = false;
		pessoaEncontradaReceita = false;

		this.setPessoaFisica((PessoaFisica) null);
		this.setPessoaJuridica(null);
		this.setPessoaAutoridade(null);
		this.setNomePessoaJuridica(null);

		if (getInTipoPessoa().equals(TipoPessoaEnum.F)){
			setIsOrgaoPublico(Boolean.FALSE);
		}
		FacesMessages.instance().clear();
	}
	
	public void limparVariaveis() {
		this.nrDocumentoPrincipal = null;
		resetarVariaveisPesquisa();
	}
	
	public void limparVariaveisPJ() {
		this.nomePessoaJuridica = null;
		this.pessoaJuridica = null;
		this.orgaoPubSelec = null;
	}

	public Boolean getIsOrgaoPublico(){
		return isOrgaoPublico;
	}

	public void setIsOrgaoPublico(Boolean isOrgaoPublico){
		this.isOrgaoPublico = isOrgaoPublico;
	}

	public void setNomePessoaJuridica(String nomePessoaJuridica){
		this.nomePessoaJuridica = nomePessoaJuridica;
	}

	public String getNomePessoaJuridica(){
		return nomePessoaJuridica;
	}

	public void setarOrgao(){
		setNomePessoaJuridica(null);
		if (getInTipoPessoa().equals(TipoPessoaEnum.F)){
			setIsOrgaoPublico(Boolean.FALSE);
		}

	}

	public Boolean getIsConciliador(){
		return isConciliador;
	}

	public void setIsConciliador(Boolean isConciliador){
		this.isConciliador = isConciliador;
	}

	public void setLiberaLocalizacao(Boolean liberaLocalizacao){
		this.liberaLocalizacao = liberaLocalizacao;
	}

	public Boolean getLiberaLocalizacao(){
		return liberaLocalizacao;
	}

	public Boolean mostrarDivOrgaoPublico(){
		return Strings.isEmpty(nomePessoaJuridica);
	}

	public static PreCadastroPessoaBean instance(){
		return ComponentUtil.getComponent("preCadastroPessoaBean");
	}

	public void newInstance(){
		if (ParametroJtUtil.instance().justicaFederal()){
			inTipoPessoa = TipoPessoaEnum.F;
			isOrgaoPublico = true;
		}
		else{
			if(inTipoPessoa == TipoPessoaEnum.J)
			{
				inTipoPessoa = TipoPessoaEnum.J;
				isOrgaoPublico = false;
			}
			else if(inTipoPessoa == TipoPessoaEnum.F)
			{
				inTipoPessoa = TipoPessoaEnum.F;
			}
		}
		brasileiro = Boolean.TRUE;
		nomePessoaJuridica = null;
		isPessoaNaoIndividualizada = Boolean.FALSE;
		isConfirmado = Boolean.FALSE;
		isPartes = Boolean.FALSE;
		isConciliador = Boolean.FALSE;
		Contexts.removeFromAllContexts("profissaoSuggest");
	}

	public void limpar(){
		newInstance();
		pessoaFisica = null;
		pessoaJuridica = null;
		pessoaEncontradaBanco = null;
		pessoaEncontradaReceita = null;
		nrDocumentoPrincipal = null;
		ocorreuErroWsReceita = Boolean.FALSE;
		setLiberaLocalizacao(false);
		isPessoaNaoIndividualizada = isPartes;
		confirmouCadastroErroWsReceita = false;
		PessoaFisicaHome.instance().setLocalizacaoFisica(null);
		Contexts.removeFromAllContexts("profissaoSuggest");
	}

	public void limpar(Boolean inicializarOrgaoPublico){
		limpar();
		setIsOrgaoPublico(inicializarOrgaoPublico);
	}

	public void inserirPessoaConfirmada(Boolean isPartes){
		setIsPartes(isPartes);
		confirmarPessoa();
		Contexts.removeFromAllContexts("processoParteVinculoPessoaEnderecoGrid");
		Contexts.removeFromAllContexts("processoParteVinculoPessoaMeioContatoGrid");
	}
	
	public void initCadastroPessoaFisica(){
		inTipoPessoa = TipoPessoaEnum.F;
		isOrgaoPublico = Boolean.FALSE;
	}

	public void initCadastroPessoaJuridica(){
		inTipoPessoa = TipoPessoaEnum.J;
		isOrgaoPublico = Boolean.FALSE;
	}
	
	public Boolean isPermiteEdicaoDocumentosPrincipais(){
		return permiteEdicaoDocumentosPrincipais;
	}
	
	
	public void setPermiteEdicaoDocumentosPrincipais(Boolean permiteEdicaoDocumentosPrincipais){
		this.permiteEdicaoDocumentosPrincipais = permiteEdicaoDocumentosPrincipais;
	}

	public Boolean getProcInformacao() {
		return procInformacao;
	}

	public void setProcInformacao(Boolean procInformacao) {
		this.procInformacao = procInformacao;
	}

	public Object getOrgaoPubSelec() {
		return orgaoPubSelec;
	}

	public void setOrgaoPubSelec(Object orgaoPubSelec) {
		this.orgaoPubSelec = orgaoPubSelec;
	}

	public void limparDados() {
		setOrgaoPubSelec(null);
		ProcessoParteHome.instance().getInstance().setProcuradoria(null);
	}
	
	/**
	 * metodo responsavel pela implemetaçao da regra implementada na issue PJEII-18819:
	 * "...No cadastro de um novo processo e na tela de retificação dos dados de um processo, 
	 * ao ser inserida uma entidade (pessoa física, jurídica ou ente ou autoridade) que seja representada 
	 * por apenas uma Procuradoria, o sistema deverá exibir esta Procuradoria na inserção dos dados da pessoa, 
	 * se ela for representada por mais de uma Procuradoria, deverá ser exibida uma lista de seleção para a identificação 
	 * (obrigatória) de qual Procuradoria fará a representação desta entidade neste processo".
	 * @return true/false para ser utilizado no atributo 'required' na tela de cadastro de pessoas(mpAssociarPArteProcesso.xhtml)
	 */
	public boolean exibeObrigatoriedadeSelecaoComboProcuradoria() {
		if(getlistProcuradorias(this.getPessoa()).size() > 1 ) {
			return true;
		}
		return false;
	}

	public Boolean getExibeBotaoCriarAutoridade() {
		return exibeBotaoCriarAutoridade;
	}
	
	public void setExibeBotaoCriarAutoridade(Boolean exibeBotaoCriarAutoridade){
		this.exibeBotaoCriarAutoridade = exibeBotaoCriarAutoridade;
	}

	
	/**
	 * Método responsável por realizar a verificação se exibe ou não o botao de Criar ente ou autoridade 
	 * com base na selecao da pesquisa suggest na tela.
	 * 
	 * @param nenhumResultado
	 * @param showTipoPessoa
	 * @param tipoPessoa
	 */
	public void validaExibicaoBotaoCriarAutoridade(Boolean nenhumResultado, Boolean showTipoPessoa, Character tipoPessoa) {
		Boolean exibeBotaoCriarAutoridade = Boolean.FALSE;
		
		if(nenhumResultado != null && nenhumResultado){
			if(showTipoPessoa != null && !showTipoPessoa){
				if(tipoPessoa != null && tipoPessoa == 'A'){
					exibeBotaoCriarAutoridade = Boolean.TRUE;
				}
			}else if(inTipoPessoa != null && inTipoPessoa == TipoPessoaEnum.A){
				exibeBotaoCriarAutoridade = Boolean.TRUE;
			}
		}else{
			exibeBotaoCriarAutoridade = Boolean.FALSE;
		}
		
		setExibeBotaoCriarAutoridade(exibeBotaoCriarAutoridade);
		if(exibeBotaoCriarAutoridade){
			this.pessoaAutoridade = null;
		}
	}
	
	public PessoaAutoridadeSuggestBean getPessoaAutoridadeSuggestBean() {
		PessoaAutoridadeSuggestBean suggest = (PessoaAutoridadeSuggestBean) Component.getInstance(PessoaAutoridadeSuggestBean.NAME);
		suggest.setPesquisarApenasComOrgaoVinculacao(true);
		return suggest;
	}
	
	public boolean mostrarComponentePessoaNaoIndividualizada(boolean showPessoaSuja, boolean showTipoPessoa) {
		return (!ProcessoTrfHome.instance().getInstance().getClasseJudicial().getExigeDocumentoIdentificacao() || 
					Authenticator.instance().isPermiteCadastrarParteSemDocumento()) && (((showPessoaSuja && showTipoPessoa) && 
						!(Boolean.TRUE.equals(this.hasPessoaFisica) || Boolean.TRUE.equals(this.hasPessoaJuridica)) && Boolean.FALSE.equals(this.isOrgaoPublico)) ||
							(ProcessoParteHome.instance().getTipoParte() != null && ProcessoParteHome.instance().getTipoParte().getIdTipoParte() == ParametroUtil.instance().getIdTipoParteLitisconsorte()));
	}

	public List<TipoPessoaEnum> getTipoPessoaItemsCache() {
		return tipoPessoaItemsCache;
	}

	public void setTipoPessoaItemsCache(List<TipoPessoaEnum> tipoPessoaItemsCache) {
		this.tipoPessoaItemsCache = tipoPessoaItemsCache;
	}
	
	public void atribuirNomeSocial() {
		if(this.pessoaAdvogado != null) {
			if(informarNomeSocial) {
				if( ComponentUtil.getComponent(PessoaAdvogadoHome.class).getInstance().getIdUsuario() != null ) {
					ComponentUtil.getComponent(PessoaAdvogadoHome.class).getInstance().getPessoa().setNomeSocial(ComponentUtil.getComponent(PessoaAdvogadoHome.class).getInstance().getNome());
				}
				this.pessoaAdvogado.getPessoa().setNomeSocial(this.pessoaAdvogado.getNome());
			} else {
				if(ComponentUtil.getComponent(PessoaAdvogadoHome.class).getInstance().getIdUsuario() != null) {
					ComponentUtil.getComponent(PessoaAdvogadoHome.class).getInstance().getPessoa().setNomeSocial(null);
				}
				this.pessoaAdvogado.getPessoa().setNomeSocial(null);
			}
		} else {
			if(!nomeSocialJaCadastrado()) {
				if(informarNomeSocial) {
					if(ComponentUtil.getComponent(PessoaFisicaHome.class).getInstance().getIdUsuario() != null) {
						ComponentUtil.getComponent(PessoaFisicaHome.class).getInstance().setNomeSocial(ComponentUtil.getComponent(PessoaFisicaHome.class).getInstance().getNome());
					}
					this.pessoaFisica.setNomeSocial(this.pessoaFisica.getNome());
				} else {
					if(ComponentUtil.getComponent(PessoaFisicaHome.class).getInstance().getIdUsuario() != null) {
						ComponentUtil.getComponent(PessoaFisicaHome.class).getInstance().setNomeSocial(null);
					}
					this.pessoaFisica.setNomeSocial(null);
				}
			}
		} 
	}

	public Boolean getInformarNomeSocial() {
		return informarNomeSocial;
	}

	public void setInformarNomeSocial(Boolean informarNomeSocial) {
		this.informarNomeSocial = informarNomeSocial;
	}

	public boolean podeCadastrarNomeSocial() {
		boolean retorno = false;
		if(getPessoa() instanceof PessoaFisica) {
			retorno = ComponentUtil.getPessoaFisicaManager().podeCadastrarNomeSocial((PessoaFisica)getPessoa());
		}
		return retorno;
	}

	
	public boolean nomeSocialJaCadastrado() {
		boolean retorno = false;
		if(getPessoa()!=null && getPessoa() instanceof PessoaFisica) {
			PessoaFisica pf = (PessoaFisica)getPessoa();
			if(pf!=null && pf.getIdPessoa()!=null && pf.getIdPessoa()>0) {
				try {
					pf = ComponentUtil.getPessoaFisicaManager().refresh(pf);
				} catch (Exception e) {
					logger.error("Erro ao fazer refresh na entidade PessoaFisica para verificar se o nome social já está cadastrado.");
					e.printStackTrace();
				}
				if(pf.getNomeSocial()!=null) {
					retorno = true;
				}
			}
		}
		return retorno;
	}


	private PessoaNomeAlternativo getPessoaNomeAltenativoById(Integer id)  {
		PessoaNomeAlternativoManager pessoaNomeAlternativoManager = ComponentUtil.getComponent(PessoaNomeAlternativoManager.class);
		try {
			return pessoaNomeAlternativoManager.findById(id);
		} catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<PessoaNomeAlternativo> obterNomesParte() throws PJeBusinessException {
		List<PessoaNomeAlternativo> nomesPessoa = new ArrayList<PessoaNomeAlternativo>();		
    	PessoaNomeAlternativoManager pessoaNomeAlternativoManager = (PessoaNomeAlternativoManager)Component.getInstance("pessoaNomeAlternativoManager");
    	List<PessoaNomeAlternativo> pessoaNomeAlternativoList = pessoaNomeAlternativoManager.recuperaNomesAlternativosProprietarios(pessoaFisica!=null?pessoaFisica:pessoaJuridica, TipoNomeAlternativoEnum.O);
		if( !pessoaNomeAlternativoList.isEmpty() ){
			nomesPessoa = pessoaNomeAlternativoList.
				stream()
				.filter( nomeAlternativo -> nomeAlternativo.getTipoNomeAlternativo() == TipoNomeAlternativoEnum.O )
				.map( nomeAlternativo -> getPessoaNomeAltenativoById(nomeAlternativo.getIdPessoaNomeAlternativo()))
				.collect(Collectors.toList());
		}
		return nomesPessoa;
	}
}
