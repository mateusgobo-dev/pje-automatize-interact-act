package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.infox.pje.manager.PessoaPushManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoPessoaFisica;
import br.jus.cnj.certificado.CertificadoPessoaJuridica;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.OficialJusticaCentralMandadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAssistenteAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAssistenteProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaOficialJusticaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaPeritoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaServidorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPushManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

@Name(PessoaService.NAME)
public class PessoaService extends BaseService{

	public static final String NAME = "pessoaService";

	@Logger
	private Log logger;
	
	@In
	private EstadoManager estadoManager;
	
	@In
	private PapelService papelService;
	
	@In
	private ParametroService parametroService;
	
	@In
	private PessoaFisicaService pessoaFisicaService;

	@In
	private PessoaJuridicaService pessoaJuridicaService;
	
	@In
	private PessoaAdvogadoManager pessoaAdvogadoManager;
	
	@In
	private PessoaAssistenteAdvogadoManager pessoaAssistenteAdvogadoManager;
	
	@In
	private PessoaAssistenteProcuradoriaManager pessoaAssistenteProcuradoriaManager;
	
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	private PessoaManager pessoaManager;

	@In
	private PessoaOficialJusticaManager pessoaOficialJusticaManager;
	
	@In
	private PessoaPeritoManager pessoaPeritoManager;
	
	@In
	private PessoaServidorManager pessoaServidorManager;
	
	@In
	private ProcuradoriaManager procuradoriaManager;
	
	@In
	private ProcessoPushManager processoPushManager;
	
	@In
	private PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private ProcuradorManager procuradorManager;
	
	@In
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;
	
	@In
	private TipoDocumentoIdentificacaoManager tipoDocumentoIdentificacaoManager;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager;
	
	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	@In(required=false)
	private Identity identity;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private PessoaPushManager pessoaPushManager;
	
	@In
	private CadastroTempPushManager cadastroTempPushManager;
	
	@In
	private OficialJusticaCentralMandadoManager oficialJusticaCentralMandadoManager;
	
	public Pessoa findByInscricaoMF(String inscricaoMF, String inscricaoConsulente, boolean exigeReceita) throws PJeBusinessException{
		String imf = InscricaoMFUtil.retiraMascara(inscricaoMF);
		int size = imf.length();
		Pessoa p = null;
		try {
			if (size == 8 || size == 14) {
				p = this.pessoaJuridicaService.findByCNPJ(imf, inscricaoConsulente, exigeReceita);
			} else if (size == 11) {
				p = this.pessoaFisicaService.findByCPF(imf, inscricaoConsulente, exigeReceita);
			}

		} catch (PJeBusinessException pbex) {
			throw pbex;
		} catch (Exception ex) {
			p = null;
		}

		if (p == null) {
			throw new PJeBusinessException("pje.pessoaService.error.numeroInscricaoInvalido", null, imf);
		}
		return p;
	}
	
	public Pessoa findByInscricaoMF(String inscricaoMF, CertificadoICP certificado) throws PJeBusinessException{
		Pessoa pessoa = null;
		if (certificado instanceof CertificadoPessoaFisica) {
			pessoa = findByInscricaoMF(inscricaoMF, certificado.getInscricaoMF(), true);
		} else {
			pessoa = findByInscricaoMF(inscricaoMF, ((CertificadoPessoaJuridica) certificado).getInscricaoMFResponsavel(), true);
		}
		return pessoa;
	}
	
	public Pessoa findByInscricaoMF(String inscricaoMF, String inscricaoConsulente) throws PJeBusinessException{
		return findByInscricaoMF(inscricaoMF, inscricaoConsulente, true);
	}
	
	public Pessoa findByInscricaoMF(String inscricaoMF) throws PJeBusinessException {
		return findByInscricaoMF(inscricaoMF, true);
	}
	
	public Pessoa findByInscricaoMF(String inscricaoMF, boolean exigeReceita) throws PJeBusinessException {
		Usuario u = usuarioService.getUsuarioLogado();
		Pessoa usuarioLogado = findById(u.getIdUsuario());
		if(usuarioLogado == null){
			throw new PJeBusinessException("pje.pessoaService.error.usuarioAusente");
		}
		Usuario usuarioSistema = Authenticator.getUsuarioSistema();
		if(usuarioSistema.equals(u)) {
			return usuarioLogado;
		}
		String inscricao = null;
		if(PessoaFisica.class.isAssignableFrom(usuarioLogado.getClass())){
			inscricao = ((PessoaFisica) usuarioLogado).getNumeroCPF();
		}else if(PessoaJuridica.class.isAssignableFrom(usuarioLogado.getClass())){
			inscricao = ((PessoaJuridica) usuarioLogado).getNumeroCpfResponsavel();
		}
		
		if(inscricao == null && exigeReceita){
			throw new PJeBusinessException("pje.pessoaService.error.semNumeroCPF");
		}
		else {
			inscricao = inscricaoMF;
		}
		return findByInscricaoMF(inscricaoMF, inscricao, exigeReceita);
	}
	
	public List<Pessoa> findByName(String name){
		return pessoaManager.findByName(name);
	}

	public Pessoa findById(Object id) throws PJeBusinessException{
		return pessoaManager.findById(id);
	}

	public Pessoa persist(Pessoa pessoa) throws PJeBusinessException{
		
		pessoaManager.removerRegistroIncompleto(pessoa);
		
		if(pessoa instanceof PessoaFisica){
			pessoa = pessoaFisicaService.persist((PessoaFisica) pessoa);
		}else if(pessoa instanceof PessoaJuridica){
			pessoa = pessoaJuridicaService.persist((PessoaJuridica)pessoa);
		}else{
			pessoaManager.persistAndFlush(pessoa);
		}
		
		if (pessoa != null && pessoa.getIdPessoa() == null) {
			pessoa.setIdPessoa(pessoa.getIdUsuario());
		}
		
		return pessoa;
	}

	/**
	 * Recupera a lista de procuradorias que representam uma pessoa.
	 * 
	 * @param pessoa a pessoa pretensamente representada por procuradorias
	 * @return a lista de procuradorias que representam a pessoa
	 */
	public List<Procuradoria> obtemOrgaosRepresentantes(Pessoa pessoa){
		return pessoaManager.getOrgaosRepresentantes(pessoa);
	}
	
	/**
	 * Indica se uma determinada pessoa está representada por alguma procuradoria.
	 * 
	 * @param p a pessoa possivelmente representada
	 * @return true, se houver pelo menos uma procuradoria com procuradores cadastrados
	 * que representem a pessoa 
	 */
	public boolean temRepresentantes(Pessoa pessoa){
		return pessoaManager.temRepresentantes(pessoa);
	}

	public List<Pessoa> pesquisarPessoasSemMandados(Integer idProcessoTrf, String nome, String cpf)
			throws PJeBusinessException{
		try{
			return pessoaManager.pesquisarPessoasSemMandados(idProcessoTrf, nome, cpf);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		}
	}
	
	/**
	 * Permite a realização de consulta dinâmica por pessoas no sistema, sejam elas físicas, jurídicas ou autoridades.
	 * 
	 * @param valor sequência de caracteres destinados à pesquisa. Quando se tratar de CPF (11 dígitos) ou CNPJ (8 ou 14 dígitos), a pesquisa será
	 *            feita por esses documentos.
	 * 
	 * @return a lista de pessoas que podem ser identificadas pelo valor dado.
	 */
	public List<Pessoa> pesquisaPessoas(String valor) throws PJeBusinessException{
		String strippedTxt = InscricaoMFUtil.retiraMascara(valor);
		List<Pessoa> ret = new ArrayList<Pessoa>();
		if (strippedTxt.matches("\\d*")
			&& (strippedTxt.length() == 8 || strippedTxt.length() == 11 || strippedTxt.length() == 14)){
			ret.add(this.findByInscricaoMF(valor));
		}
		else if (valor.length() >= 3){
			ret.addAll(this.findByName(valor));
		}
		return ret;
	}
	
	/**
	 * [PJEII-4244] Correção de erro ao logar como assistente de advogado ou assistente de procuradoria, 
	 * devido a falha na inicialização de lista "lazy"
	 * @param representante
	 * @return
	 */
	public List<Pessoa> getRepresentados(Pessoa representante) throws PJeBusinessException{
		List<Pessoa> representados = new ArrayList<Pessoa>();
		representados.add(representante);
		UsuarioLocalizacao localizacaoAtual = usuarioService.getLocalizacaoAtual();
		
		// Verifica condicoes para Assistente de Procurador
		if (Pessoa.instanceOf(representante, PessoaAssistenteProcuradoria.class)
				&& (identity.hasRole(Papeis.PJE_ASSISTENTE_PROCURADOR)	|| identity.hasRole("assistProcurador"))) {
			
			// Verifica se o usuario possui localizacao e se ele eh um objeto AssistenteProcuradoriaLoca
			if (localizacaoAtual != null && PessoaAssistenteProcuradoriaLocal.class.isAssignableFrom(localizacaoAtual.getClass())) {
				PessoaAssistenteProcuradoriaLocal vinculoProcuradoria = (PessoaAssistenteProcuradoriaLocal) localizacaoAtual;
				Procuradoria proc = vinculoProcuradoria.getProcuradoria();
				if (proc != null){
					List<Pessoa> repsProcuradoria = procuradoriaManager.getPessoasRepresentadas(proc);
					representados.addAll(repsProcuradoria);
				}
			}
			
		// Verifica condicoes para Procurador
		} else if (Pessoa.instanceOf(representante, PessoaProcurador.class) && identity.hasRole(Papeis.PJE_REPRESENTANTE_PROCESSUAL)) {
			Procuradoria proc = Authenticator.getProcuradoriaAtualUsuarioLogado();
			if (proc != null) {
				List<Pessoa> repsProcurador = procuradorManager.getPessoasRepresentadas(proc, representante);
				if (repsProcurador.size() > 0){
					representados.addAll(repsProcurador);
				} else {
					List<Pessoa> repsProcuradoria = procuradoriaManager.getPessoasRepresentadas(proc);
					representados.addAll(repsProcuradoria);
				}
			}
		}
		
		return representados;
	}

	public String getIdsRepresentados(Pessoa representante) throws PJeBusinessException{
		StringBuilder ids = new StringBuilder();
		List<Pessoa> reps = getRepresentados(representante);
		for(int i = 0; i < reps.size(); i++){
			ids.append(reps.get(i).getIdUsuario());
			if(i != (reps.size() - 1)){
				ids.append(",");
			}
		}
		return ids.toString();
	}
	
	/**
	 * Identifica se o usuário indicado tem uma das especializações possíveis.
	 *  
	 * @param p a pessoa a ser verificada
	 * @param clazz a classe a ser investigada
	 * @return true, se houver a especialização
	 */
	public static <T extends PessoaFisicaEspecializada> boolean instanceOf(UsuarioLogin p, Class<T> clazz){
		/**
		 * PJEII-5080 PJE-JT Antonio Lucas
		 * Verifica se o objeto passado como parametro não é nulo
		 * para evitar NullPointerException
		 */
		if (p != null){
			if(!(PessoaFisica.class.isAssignableFrom(p.getClass()))){
				return false;
			}
			PessoaFisica pessoa = (PessoaFisica) p;
			if(clazz.isAssignableFrom(PessoaAdvogado.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.ADV) == PessoaFisica.ADV;
			}else if(clazz.isAssignableFrom(PessoaAssistenteAdvogado.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.ASA) == PessoaFisica.ASA;
			}else if(clazz.isAssignableFrom(PessoaAssistenteProcuradoria.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.ASP) == PessoaFisica.ASP;
			}else if(clazz.isAssignableFrom(PessoaMagistrado.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.MAG) == PessoaFisica.MAG;
			}else if(clazz.isAssignableFrom(PessoaOficialJustica.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.OFJ) == PessoaFisica.OFJ;
			}else if(clazz.isAssignableFrom(PessoaPerito.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.PER) == PessoaFisica.PER;
			}else if(clazz.isAssignableFrom(PessoaProcurador.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.PRO) == PessoaFisica.PRO;
			}else if(clazz.isAssignableFrom(PessoaServidor.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.SER) == PessoaFisica.SER;
			}
		} 
		return false;
	}
	
	/**
	 * Especializa a pessoa indicada.
	 * 
	 * @param p a pessoa a ser especializada
	 * @param especializacoes as classes especializadas a serem respeitadas
	 * @return a pessoa especializada
	 * @throws PJeBusinessException caso tenha havido algum erro na persistência
	 */
	public <T extends PessoaFisicaEspecializada> Pessoa especializa(Pessoa p, Class<T>...especializacoes) throws PJeBusinessException{
		return especializa(p, null, especializacoes);
	}
	
	/**
	 * Especializa a pessoa indicada.
	 * 
	 * @param p a pessoa a ser especializada
	 * @param inscricaoConsulente o número da inscrição no Ministério da Fazenda da pessoa responsável pela especialização
	 * @param especializacoes as classes especializadas a serem respeitadas
	 * @return a pessoa especializada
	 * @throws PJeBusinessException caso tenha havido algum erro na persistência
	 */
	public <T extends PessoaFisicaEspecializada> Pessoa especializa(Pessoa p, String inscricaoConsulente, Class<T>...especializacoes) throws PJeBusinessException{
		if(especializacoes.length > 0 && !(p instanceof PessoaFisica)){
			throw new IllegalArgumentException("Não é possível especializar pessoas diversas das pessoas físicas.");
		}
		if(p instanceof PessoaAutoridade){
			logger.debug("Iniciando o cadastro de uma autoridade: {0}", p.getNome());
			throw new UnsupportedOperationException("Este método ainda não está preparado para persistir PessoaAutoridade");
		}else if(p instanceof PessoaJuridica){
			logger.debug("Iniciando o cadastro de uma pessoa jurídica: {0}", p.getNome() != null ? p.getNome() : p.getDocumentoCpfCnpj());
			throw new UnsupportedOperationException("Este método ainda não está preparado para persistir PessoaJuridica");
		}else if(p instanceof PessoaFisica){
			logger.debug("Iniciando o cadastro de uma pessoa física: {0}", p.getNome() != null ? p.getNome() : p.getDocumentoCpfCnpj());
			PessoaFisica pessoa = null;
			if(p.getIdUsuario() != null){
				pessoa = pessoaFisicaService.find(p.getIdUsuario());
			}else if(((PessoaFisica)p).getNumeroCPFAtivo() != null){
				boolean exigeReceita = !PjeUtil.instance().isMockReceitaEnabled();
				if(inscricaoConsulente != null){
					pessoa = (PessoaFisica) findByInscricaoMF(((PessoaFisica) p).getNumeroCPFAtivo(), inscricaoConsulente, exigeReceita);
				}else{
					pessoa = (PessoaFisica) findByInscricaoMF(((PessoaFisica) p).getNumeroCPFAtivo(), exigeReceita);
				}
				persist(pessoa);
			}else{
				throw new PJeBusinessException("Não é possível especializar uma pessoa não identificada.");
			}

			if (especializacoes != null && especializacoes.length > 0) {
				for(Class<T> especializada: especializacoes){
					especializaPessoaFisica(pessoa, especializada);
				}
				desabilitarUsuarioPush(pessoa);
				pessoaManager.flush();
			}

			return pessoa;
		} else {
			throw new UnsupportedOperationException("Não é possível criar uma pessoa do tipo indicado: " + p.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Método responsável por desabilitar um usuário push. A desabilitação consiste em atribuir os processos 
	 * da lista de push de usuário push para o usuário do sistema e posteriormente remover o usuário push.
	 * 
	 * @param pessoa {@link Pesssoa}.  
	 * @throws PJeBusinessException Caso ocorra algum erro.
	 */
	public void desabilitarUsuarioPush(Pessoa pessoa) throws PJeBusinessException {
		PessoaPush pessoaPush = this.pessoaPushManager.recuperarPessoaPushByLogin(pessoa.getDocumentoCpfCnpj());
		if (pessoaPush != null) {
			List<ProcessoPush> processosPush = this.processoPushManager.recuperarProcessosPush(pessoaPush, null);
			for (ProcessoPush processoPush : processosPush) {
				processoPush.setPessoaPush(null);
				processoPush.setPessoa(pessoa);
				processoPushManager.persist(processoPush);
			}
			this.pessoaPushManager.remove(pessoaPush);
		}
	}
	
	/**
	 * Especializa a pessoa física dada para um dos oito perfis especializados.
	 * 
	 * @param pessoa a pessoa a ser especializada
	 * @param clazz a classe de especialização
	 * @throws PJeBusinessException caso tenha havido algum erro na persistência
	 */
	private <T extends PessoaFisicaEspecializada> void especializaPessoaFisica(PessoaFisica pessoa, Class<T> clazz) throws PJeBusinessException{
		if(clazz.isAssignableFrom(PessoaAdvogado.class) && !Pessoa.instanceOf(pessoa, PessoaAdvogado.class)){
			pessoaAdvogadoManager.especializa(pessoa);
			String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoPessoaFisica(pessoa);
			Papel adv = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_ADVOGADO));
			usuarioService.acrescentaLocalizacaoPessoal(nomeLocalizacao, pessoa, adv);
		}else if(clazz.isAssignableFrom(PessoaAssistenteAdvogado.class) && !Pessoa.instanceOf(pessoa, PessoaAssistenteAdvogado.class)){
			pessoaAssistenteAdvogadoManager.especializa(pessoa);
		}else if(clazz.isAssignableFrom(PessoaAssistenteProcuradoria.class) && !Pessoa.instanceOf(pessoa, PessoaAssistenteProcuradoria.class)){
			pessoaAssistenteProcuradoriaManager.especializa(pessoa);
		}else if(clazz.isAssignableFrom(PessoaMagistrado.class) && !Pessoa.instanceOf(pessoa, PessoaMagistrado.class)){
			pessoaMagistradoManager.especializa(pessoa);
		}else if(clazz.isAssignableFrom(PessoaOficialJustica.class) && !Pessoa.instanceOf(pessoa, PessoaOficialJustica.class)){
			pessoaOficialJusticaManager.especializa(pessoa);
		}else if(clazz.isAssignableFrom(PessoaPerito.class) && !Pessoa.instanceOf(pessoa, PessoaPerito.class)){
			pessoaPeritoManager.especializa(pessoa);
			String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoPessoaFisica(pessoa);
			Papel per = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_PERITO));
			if(per != null){
				usuarioService.acrescentaLocalizacaoPessoal(nomeLocalizacao, pessoa, per);
			}
		}else if(clazz.isAssignableFrom(PessoaProcurador.class) && !Pessoa.instanceOf(pessoa, PessoaProcurador.class)){
			procuradorManager.especializa(pessoa);
			/*
			 * Reativa localizações de PessoaProcuradoria (Procuradorias/Defensorias associadas a esta especialização)
			 */
			
			List<PessoaProcuradoria> procuradorias = pessoaProcuradoriaManager
					.getProcuradorias(pessoa);
			
			if (!procuradorias.isEmpty()){
				for(PessoaProcuradoria pessoaProcuradoria : procuradorias){
				 Papel papel = null;
				 String nomeLocalizacao = "";
				 if(pessoaProcuradoria.getChefeProcuradoria()){
					papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL_GESTOR);
				 }else{
					papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL);
				 }
				 nomeLocalizacao = pessoaProcuradoria.getProcuradoria().getTipo().getLabel()+" - "+pessoaProcuradoria.getProcuradoria().getNome();
				 usuarioService.acrescentaLocalizacaoPessoal(nomeLocalizacao, pessoa, papel);
				}
			}
			
		}else if(clazz.isAssignableFrom(PessoaServidor.class) && !Pessoa.instanceOf(pessoa, PessoaServidor.class)){
			pessoaServidorManager.especializa(pessoa);
			
			
		}
		
		Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_CADASTRO_SSO_USUARIO, pessoa.getIdUsuario());
	}
	
	/**
	 * Desespecializa a pessoa indicada
	 * @param p
	 * @param especializacoes
	 * @return
	 * @throws PJeBusinessException
	 */
	public <T extends PessoaFisicaEspecializada> Pessoa desespecializa(Pessoa p, Class<T>...especializacoes) throws PJeBusinessException{
		return desespecializa(p, null, especializacoes);
	}
	
	/**
	 * Desespecializa a pessoa indicada
	 * @param p
	 * @param inscricaoConsulente
	 * @param especializacoes
	 * @return
	 * @throws PJeBusinessException
	 */
	public <T extends PessoaFisicaEspecializada> Pessoa desespecializa(Pessoa p, String inscricaoConsulente, Class<T>...especializacoes) throws PJeBusinessException{
		if(p instanceof PessoaFisica){
			logger.debug("Iniciando supressão de perfil de uma pessoa física: {0}", p.getNome() != null ? p.getNome() : p.getDocumentoCpfCnpj());
			PessoaFisica pessoa = null;
			if(p.getIdUsuario() != null){
				pessoa = (PessoaFisica) findById(p.getIdUsuario());
			}else if(((PessoaFisica)p).getNumeroCPFAtivo() != null){
				if(inscricaoConsulente != null){
					pessoa = (PessoaFisica) findByInscricaoMF(((PessoaFisica) p).getNumeroCPFAtivo(), inscricaoConsulente);
				}else{
					pessoa = (PessoaFisica) findByInscricaoMF(((PessoaFisica) p).getNumeroCPFAtivo());
				}
				persist(pessoa);
			}else{
				throw new PJeBusinessException("Não é possível desespecializar uma pessoa não identificada.");
			}
			boolean flush = false;
			for(Class<T> especializada: especializacoes){
				desespecializaPessoaFisica(pessoa, especializada);
				flush = true;
			}
			try {
				if(flush) pessoaManager.flush();
				return pessoa;	
			} catch(Exception e) {
				if (pessoa.getPessoaMagistrado() != null) {
					throw new PJeBusinessException("O magistrado não pode ser inativado por ter processos relacionados a ele.");	
				} else {
					throw new PJeBusinessException("Ocorreu um erro de persistência. " + e.getMessage());	
				}				
			}						
		}else{
			throw new UnsupportedOperationException("Não é possível desespecializar uma pessoa do tipo indicado: " + p.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Desespecializa a pesssoa física indicada
	 * @param pessoa
	 * @param especializacao
	 * @throws PJeBusinessException
	 */
	public <T extends PessoaFisicaEspecializada> void desespecializaPessoaFisica(PessoaFisica pessoa, Class<T>especializacao) throws PJeBusinessException{
		if(pessoa != null){
			if(especializacao.isAssignableFrom(PessoaAdvogado.class) && Pessoa.instanceOf(pessoa, PessoaAdvogado.class)){
				pessoaAdvogadoManager.desespecializa(pessoa);
				String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoPessoaFisica(pessoa);
				Papel adv = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_ADVOGADO));
				usuarioService.excluiLocalizacaoPessoal(nomeLocalizacao, pessoa, adv);
			}else if(especializacao.isAssignableFrom(PessoaAssistenteAdvogado.class) && Pessoa.instanceOf(pessoa, PessoaAssistenteAdvogado.class)){
				pessoaAssistenteAdvogadoManager.desespecializa(pessoa);
				for(Localizacao loc : pessoa.getPessoaAssistenteAdvogado().getLocalizacoes()){
					Papel asa = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_ASSISTENTE_ADVOGADO));
					usuarioService.excluiLocalizacaoPessoal(loc.getLocalizacao(), pessoa, asa);					
				}
			}else if(especializacao.isAssignableFrom(PessoaAssistenteProcuradoria.class) && Pessoa.instanceOf(pessoa, PessoaAssistenteProcuradoria.class)){
				pessoaAssistenteProcuradoriaManager.desespecializa(pessoa);
				for(Localizacao loc : pessoa.getPessoaAssistenteProcuradoria().getLocalizacoes()){
					Papel asp = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_ASSISTENTE_PROCURADORIA));
					usuarioService.excluiLocalizacaoPessoal(loc.getLocalizacao(), pessoa, asp);					
				}
			}else if(especializacao.isAssignableFrom(PessoaMagistrado.class) && Pessoa.instanceOf(pessoa, PessoaMagistrado.class)){
				pessoaMagistradoManager.desespecializa(pessoa);
				PessoaMagistrado magistrado = pessoa.getPessoaMagistrado();
				List<UsuarioLocalizacao> usuarioLocalizacaoList = usuarioService.consultarLocalizacoesDeMagistradoAtuante(magistrado);
				usuarioService.exlcuiLocalizacoesMagistradoServidor(usuarioLocalizacaoList);
			}else if(especializacao.isAssignableFrom(PessoaOficialJustica.class) && Pessoa.instanceOf(pessoa, PessoaOficialJustica.class)){
				desespecializarOficialJustica(pessoa);
			}else if(especializacao.isAssignableFrom(PessoaPerito.class) && Pessoa.instanceOf(pessoa, PessoaPerito.class)){
				pessoaPeritoManager.desespecializa(pessoa);
				String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoPessoaFisica(pessoa);
				Papel per = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_PERITO));
				usuarioService.excluiLocalizacaoPessoal(nomeLocalizacao, pessoa, per);
			}else if(especializacao.isAssignableFrom(PessoaProcurador.class) && Pessoa.instanceOf(pessoa, PessoaProcurador.class)){
				procuradorManager.desespecializa(pessoa);
				
				Papel pro = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_PROCURADOR));
				
				String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoProcurador(pessoa);
				usuarioService.excluiLocalizacaoPessoal(nomeLocalizacao, pessoa, pro);
				
				PessoaProcurador procurador = pessoa.getPessoaProcurador();
				List<PessoaProcuradoria> pessoasProcuradorias = pessoaProcuradoriaManager.getProcuradorias(procurador);
				for(PessoaProcuradoria pessoaProcuradoria : pessoasProcuradorias) {
					String nomeLocalizacaoProcuradoria = LocalizacaoUtil.formataLocalizacaoProcuradoria(pessoaProcuradoria.getProcuradoria());
					
					if(pessoaProcuradoria.getChefeProcuradoria()) {
						pro = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_PROCURADOR_CHEFE));					
					} else {
						pro = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_PROCURADOR));
					}
					
					usuarioService.excluiLocalizacaoPessoal(nomeLocalizacaoProcuradoria, pessoa, pro);
				}
			}else if(especializacao.isAssignableFrom(PessoaServidor.class) && Pessoa.instanceOf(pessoa, PessoaServidor.class)){
				pessoaServidorManager.desespecializa(pessoa);
				List<UsuarioLocalizacao> usuarioLocalizacaoList = usuarioService.getUsuarioLocalizacaoServidor(pessoa.getPessoaServidor());
				usuarioService.exlcuiLocalizacoesMagistradoServidor(usuarioLocalizacaoList);
			}
		}
		Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_CADASTRO_SSO_USUARIO, pessoa.getIdUsuario());
	}

	/**
	 * Método responsável por desespecializar uma {@link PessoaOficialJustica}
	 * com papéis de {@link Parametros.ID_PAPEL_OFICIAL_JUSTICA} e
	 * {@link Parametros.ID_PAPEL_OFICIAL_JUSTICA_DISTRIBUIDOR}
	 * 
	 * @param pessoaOficialJustica
	 *            a pessoa oficial de justiça com os papéis citados acima
	 * @throws PJeBusinessException
	 */
	private void desespecializarOficialJustica(PessoaFisica pessoaOficialJustica) throws PJeBusinessException {
		pessoaOficialJusticaManager.desespecializa(pessoaOficialJustica);
		Papel oficialJustica = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_OFICIAL_JUSTICA));
		Papel oficialJusticaDist = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_OFICIAL_JUSTICA_DISTRIBUIDOR));
		List<Papel> papeis = Arrays.asList(oficialJustica, oficialJusticaDist);
		removerLocalizacao(pessoaOficialJustica, papeis);
	}

	/**
	 * Método responsável por remover a localização da {@link Pessoa}
	 * 
	 * @param pessoa
	 *            a pessoa que se deseja remover a localização
	 * @param papeis
	 *            os papéis em que essas localizações se encontram
	 * @throws PJeBusinessException
	 */
	private void removerLocalizacao(PessoaFisica pessoa, List<Papel> papeis) throws PJeBusinessException {
		if (CollectionUtilsPje.isNotEmpty(papeis)) {
			List<UsuarioLocalizacao> localizacoesAtivas = usuarioService.getLocalizacoesAtivas(pessoa, papeis);
			for(UsuarioLocalizacao usuarioLocalizacao : localizacoesAtivas){
				oficialJusticaCentralMandadoManager.remove(usuarioLocalizacao);
			}
		}
	}
	
	/**
	 * Indica que a pessoa dada passará a acompanhar seus processos pelo sistema push.
	 * 
	 * @param pessoa a pessoa que passará a acompanhar os processos
	 * @return o número de processos afetados pela chamada.
	 */
	public int acompanharEmPush(Pessoa pessoa){
		int qtdProcessos = 0;
		try {
			qtdProcessos = this.processoPushManager.reativarAcompanhamento(pessoa); 
			
			qtdProcessos += this.processoPushManager.acompanharProcessos(
				pessoa, this.processoTrfManager.recuperarProcessosRelacionados(pessoa));

		} catch (PJeBusinessException ex) {
			ex.printStackTrace();
		}
		return qtdProcessos;
	}
	
	/**
	 * Inclui uma localização de jus postulandi para a pessoa dada. 
	 * Caso essa pessoa seja um usuário push desabilita-o.
	 * 
	 * @param pessoa A pessoa indicada.
	 * @throws PJeBusinessException caso tenha havido algum erro na persistência
	 */
	public void tornaJusPostulandi(Pessoa pessoa) throws PJeBusinessException {
		String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoJusPostulandi(pessoa);
		Papel papel = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_JUSPOSTULANDI));
		usuarioService.acrescentaLocalizacaoPessoal(nomeLocalizacao, pessoa, papel);
		desabilitarUsuarioPush(pessoa);
	}

	/**
	 * Exclui a localização de jus postulandi para a pessoa dada.
	 * 
	 * @param pessoa a pessoa que terá o papel excluído
	 * @throws PJeBusinessException caso haja algum erro quando da exclusão
	 */
	public void excluirJusPostulandi(PessoaFisica pessoa) throws PJeBusinessException {
		String nomeLocalizacao = LocalizacaoUtil.formataLocalizacaoJusPostulandi(pessoa);
		Papel jpd = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_JUSPOSTULANDI));
		usuarioService.excluiLocalizacaoPessoal(nomeLocalizacao, pessoa, jpd);
	}
	
	/**
	 * Acrescenta um documento relativo à inscrição no cadastro de contribuintes brasileiros para a pessoa.
	 * O método resolverá, pelo tipo de pessoa, se deve acrescentar um CNPJ ou um CPF.
	 * 
	 * @param pessoa a pessoa a quem será atribuído o novo documento
	 * @param inscricaoMF o número da inscrição a ser inserida
	 * @return true, se houve o acréscimo, ou false, se o documento já fazia parte dos documentos da pessoa
	 * @throws PJeBusinessException
	 */
	public boolean adicionaInscricaoMF(Pessoa pessoa, String inscricaoMF) throws PJeBusinessException{
		if(pessoa instanceof PessoaFisica){
			return adicionaCPF((PessoaFisica) pessoa, inscricaoMF);
		}else if(pessoa instanceof PessoaJuridica){
			return adicionaCNPJ((PessoaJuridica) pessoa, inscricaoMF);
		}else{
			throw new PJeBusinessException("pje.error.documentoidentificador.incompativel", null, inscricaoMF);
		}
	}
	
	/**
	 * Adiciona um CPF a uma pessoa física.
	 * 
	 * @param pessoa a pessoa a quem será atribuído o CPF
	 * @param inscricaoMF o número do CPF (com ou sem máscara)
	 * @return true, se houve o acréscimo
	 * @throws PJeBusinessException
	 */
	private boolean adicionaCPF(PessoaFisica pessoa, String inscricaoMF) throws PJeBusinessException{
		String cpf = InscricaoMFUtil.retiraMascara(inscricaoMF);
		if(!InscricaoMFUtil.verificaCPF(cpf)){
			throw new PJeBusinessException("O número do CPF não é válido.");
		}
		TipoDocumentoIdentificacao tipoCPF = tipoDocumentoIdentificacaoManager.findById("CPF");
		PessoaDocumentoIdentificacao cpfAtivo = pessoaDocumentoIdentificacaoManager.recuperaDocumento(cpf, tipoCPF);
		if(cpfAtivo != null){
			return false;
		}else{
			cpfAtivo = new PessoaDocumentoIdentificacao();
			cpfAtivo.setAtivo(true);
			cpfAtivo.setDataExpedicao(null);
			cpfAtivo.setDataUsadoFalsamente(null);
			cpfAtivo.setDocumentoPrincipal(true);
			cpfAtivo.setNome(pessoa.getNome());
			cpfAtivo.setNumeroDocumento(cpf);
			cpfAtivo.setOrgaoExpedidor("Secretaria da Receita Federal do Brasil");
			cpfAtivo.setPessoa(pessoa);
			cpfAtivo.setTipoDocumento(tipoCPF);
			cpfAtivo.setUsadoFalsamente(false);
			cpfAtivo.setUsuarioCadastrador(usuarioService.getUsuarioLogado());
			pessoa.getPessoaDocumentoIdentificacaoList().add(cpfAtivo);
			return true;
		}
	}
	
	/**
	 * Adiciona um CNPJ a uma pessoa jurídica.
	 * 
	 * @param pessoa a pessoa a quem será atribuído o CNPJ
	 * @param inscricaoMF o número do CNPJ (com ou sem máscara)
	 * @return true, se houve o acréscimo
	 * @throws PJeBusinessException
	 */
	private boolean adicionaCNPJ(PessoaJuridica pessoa, String inscricaoMF) throws PJeBusinessException{
		String numeroDocumento = InscricaoMFUtil.retiraMascara(inscricaoMF);
		if(!InscricaoMFUtil.verificaCNPJ(numeroDocumento)){
			throw new PJeBusinessException("O número do CNPJ não é válido.");
		}
		TipoDocumentoIdentificacao tipo = tipoDocumentoIdentificacaoManager.findById("CPJ");
		PessoaDocumentoIdentificacao doc = pessoaDocumentoIdentificacaoManager.recuperaDocumento(numeroDocumento, tipo);
		if(doc != null){
			return false;
		}else{
			doc = new PessoaDocumentoIdentificacao();
			doc.setAtivo(true);
			doc.setDataExpedicao(null);
			doc.setDataUsadoFalsamente(null);
			doc.setDocumentoPrincipal(true);
			doc.setNome(pessoa.getNome());
			doc.setNumeroDocumento(numeroDocumento);
			doc.setOrgaoExpedidor("Secretaria da Receita Federal do Brasil");
			doc.setPessoa(pessoa);
			doc.setTipoDocumento(tipo);
			doc.setUsadoFalsamente(false);
			doc.setUsuarioCadastrador(usuarioService.getUsuarioLogado());
			pessoa.getPessoaDocumentoIdentificacaoList().add(doc);
			return true;
		}
	}

	/**
	 * Adiciona um documento inscrição na OAB a partir de dados recolhidos do CNA/OAB.
	 * 
	 * @param pessoa a pessoa a quem será atribuído o documento
	 * @param insc os dados de inscrição previamente recuperados do CNA/OAB.
	 * @throws PJeBusinessException
	 */
	public void adicionaInscricaoOAB(PessoaFisica pessoa, DadosAdvogadoOAB insc) throws PJeBusinessException {
		TipoDocumentoIdentificacao tipo = tipoDocumentoIdentificacaoManager.findById("OAB");
		Estado estado = estadoManager.findBySigla(insc.getUf());
		PessoaDocumentoIdentificacao inscricao = new PessoaDocumentoIdentificacao();
		if(insc.getSituacaoInscricao().equalsIgnoreCase("regular")){
			inscricao.setAtivo(true);
		}else{
			inscricao.setAtivo(false);
		}
		inscricao.setDataExpedicao(insc.getDataCadastro());
		inscricao.setDocumentoPrincipal(false);
		inscricao.setEstado(estado);
		inscricao.setNome(insc.getNome());
		inscricao.setNumeroDocumento(insc.getNumInscricao());
		inscricao.setOrgaoExpedidor("OAB-" + estado.getCodEstado());
		inscricao.setPessoa(pessoa);
		inscricao.setTipoDocumento(tipo);
		inscricao.setUsadoFalsamente(false);
		inscricao.setUsuarioCadastrador(usuarioService.getUsuarioLogado());
		pessoa.getPessoaDocumentoIdentificacaoList().add(inscricao);
	}
	
	
	/**
	 * Este método encontra-se descontinuado.
	 * Favor utilizar {@link AtoComunicacaoService#verificarCadastroPessoa(Pessoa)} ou 
	 * {@link AtoComunicacaoService#verificarCadastroPessoa(Pessoa, Integer) 
	 */
	@Deprecated
	public boolean aptoIntimacaoEletronica(Pessoa p){
		if(!p.getAtivo()){
			return false;
		}
		return pessoaManager.aptoIntimacaoEletronica(p);
	}
	
	/**
	 * Método responsável por recuperar a lista de pessoas que têm um nome e documento identificador especificado
	 * 
	 * @param nome Nome a ser pesquisado
	 * @param tipoDocumentoIdentificacao Tipo de documento de identificação
	 * @param documentoIdentificacao Texto do documento a ser pesquisado
	 * @return Lista de pessoas
	 */
	public List<Pessoa> findByNomeAndDocumentoIdentificacao(String nome, String tipoDocumentoIdentificacao, String documentoIdentificacao) {
		return pessoaManager.findByNomeAndDocumentoIdentificacao(nome, tipoDocumentoIdentificacao, documentoIdentificacao);
	}
	
 	/**
 	 * Método responsável por verificar se a pessoa está configurada como fiscal da lei
 	 * @param pessoa Pessoa a ser verificada
 	 * @return true, se a pessoa for o fiscal da lei.
 	 */
 	public boolean isFiscalDaLei(Pessoa pessoa){
		boolean resultado = false;
		List<Pessoa> listaFiscaisDaLei = ParametroUtil.instance().getFiscaisDaLei();
		if (CollectionUtils.isNotEmpty(listaFiscaisDaLei)) {
			resultado = listaFiscaisDaLei.contains(pessoa);
		}
		return resultado;
 	}
 	
 	/**
	 * Método para retorno do cpf/cnpj na coluna de destinatários do PAC
	 * 
	 * @param pessoa
	 * @return
	 */
	public String obtemCpfCnpj(Pessoa pessoa) {
		String ret = "" ;
		
		if (Pessoa.instanceOf(pessoa, PessoaAdvogado.class) && ((PessoaFisica) pessoa).getPessoaAdvogado() != null) {
			String strOab = ((PessoaFisica) pessoa).getPessoaAdvogado().getOabFormatado();
			
			if (strOab != null) {
				ret = "OAB: " + strOab;
			} else {
				ret = "CPF: " + pessoa.getDocumentoCpfCnpj();
			}
		} else if (pessoa instanceof PessoaFisica && pessoa.getDocumentoCpfCnpj() != null){
			ret = "CPF: " + pessoa.getDocumentoCpfCnpj();
		} else if ( pessoa instanceof PessoaJuridica && pessoa.getDocumentoCpfCnpj() != null){
			ret = "CNPJ: " + pessoa.getDocumentoCpfCnpj();
		}
	
		return ret; 
	}
	
	

}
