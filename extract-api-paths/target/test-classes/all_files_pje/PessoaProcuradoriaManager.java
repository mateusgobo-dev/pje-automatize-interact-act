/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaProcuradoriaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaJurisdicao;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaProcuradoriaManager.NAME)
@Scope(ScopeType.EVENT)
public class PessoaProcuradoriaManager extends BaseManager<PessoaProcuradoria> {

	@Logger
	private Log log;

	@In
	private PessoaProcuradoriaDAO pessoaProcuradoriaDAO;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager = (UsuarioLocalizacaoManager) Component.getInstance("usuarioLocalizacaoManager");
	
	public static final String  NAME = "pessoaProcuradoriaManager";
	
	@Override
	protected BaseDAO<PessoaProcuradoria> getDAO() {
		return pessoaProcuradoriaDAO;
	}
	
	public List<PessoaProcuradoria> getProcuradorias(PessoaProcurador p){
		Search s = new Search(PessoaProcuradoria.class);
		
		try {
			s.addCriteria(Criteria.equals("pessoa", p));
			
			if (Authenticator.isPermissaoCadastroTodosPapeis()){
				return list(s);
			}
			
			if (Authenticator.getPapelAtual().getIdentificador().equals(Papeis.REPRESENTANTE_PROCESSUAL)
				||Authenticator.getPapelAtual().getIdentificador().equals(Papeis.REPRESENTANTE_PROCESSUAL_GESTOR)){

				s.addCriteria(Criteria.equals("procuradoria.localizacao", Authenticator.getLocalizacaoAtual()));
			}else{
				s.addCriteria(Criteria.equals("true", false));
			}
			
			
		} catch (Exception e) {
			return null;
		}
		return list(s);
	}
	
	/**
	 * Verifica se a pessoa física indicada é procuradora em alguma das procuradorias indicadas na lista e retorna a primeira procuradoria encontrada
	 * @param pf
	 * @param procuradoriaList
	 * @return
	 */
	public Procuradoria recuperaProcuradoriaDoProcurador(PessoaFisica pf, List<Procuradoria> procuradoriaList) {
		List<PessoaProcuradoria> pessoaProcuradoriaList = this.getProcuradorias(pf);
		if(CollectionUtilsPje.isNotEmpty(pessoaProcuradoriaList) && CollectionUtilsPje.isNotEmpty(procuradoriaList)) {
			for (PessoaProcuradoria pessoaProcuradoria : pessoaProcuradoriaList) {
				for (Procuradoria procuradoria : procuradoriaList) {
					if(procuradoria.getIdProcuradoria() == pessoaProcuradoria.getProcuradoria().getIdProcuradoria()) {
						return procuradoria;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Dada uma pessoa física, busca as procuradorias nas quais essa pessoa é procurador
	 * @param p
	 * @return
	 */
	public List<PessoaProcuradoria> getProcuradorias(PessoaFisica p) {
		Search s = new Search(PessoaProcuradoria.class);

		try {
			s.addCriteria(Criteria.equals("pessoa", p.getPessoaProcurador()));
			return list(s);
		} catch (Exception e) {
			log.error("Erro ao consultar procuradorias da pessoa fisica.", e);
			return null;
		}
	}

	/*
	 * Insere nova PessoaProcuradoria
	 */
	public void inserirNovo(PessoaProcuradoria pessoaProcuradoria) throws PJeBusinessException{
		PapelService papelService = (PapelService) Component.getInstance("papelService");
		Papel papel = null;
				
		if(pessoaProcuradoria.getAtuacao() == RepresentanteProcessualTipoAtuacaoEnum.G){
			papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL_GESTOR);
			pessoaProcuradoria.setChefeProcuradoria(true);
		}else{
			papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL);
			pessoaProcuradoria.setChefeProcuradoria(false);
			
		}

		verificaRemoveLocalizacaoPadraProcurador(pessoaProcuradoria);

		UsuarioLocalizacao ul = usuarioLocalizacaoManager.getLocalizacao(pessoaProcuradoria.getPessoa().getPessoa(), pessoaProcuradoria.getProcuradoria().getLocalizacao(), papel);
		if (ul == null) {
			criarLocalizacaoPessoa(pessoaProcuradoria.getPessoa(), papel,pessoaProcuradoria.getProcuradoria().getLocalizacao());
		}
		persistAndFlush(pessoaProcuradoria);
	}
	
	/*
	 * Insere nova PessoaProcuradoria com Jurisdição (Distribuidor)
	 * O método recebe a lista de Jurisdições selecianda pelo usuário e 
	 * atualiza PessoaProcuradoria com a lista de jurisdições
	 */
	public void inserirNovo(PessoaProcuradoria pessoaProcuradoria, List<String> jurisdicoes) throws PJeBusinessException{
		List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicaoList = preparaListaPessoaProcuradoriaJurisdicao(pessoaProcuradoria, jurisdicoes);
		pessoaProcuradoria.setPessoaProcuradoriaJurisdicaoList(pessoaProcuradoriaJurisdicaoList);
		this.inserirNovo(pessoaProcuradoria);
	}
	
	public List<PessoaProcuradoriaJurisdicao> preparaListaPessoaProcuradoriaJurisdicao (PessoaProcuradoria pessoaProcuradoria, List<String> jurisdicoes){
		List<PessoaProcuradoriaJurisdicao> listaPessoaProcuradoriaJurisdicao = new ArrayList<PessoaProcuradoriaJurisdicao>();
		for(String idJurisdicao : jurisdicoes){
			JurisdicaoManager jm = (JurisdicaoManager) Component.getInstance("jurisdicaoManager");
			PessoaProcuradoriaJurisdicao ppj = new PessoaProcuradoriaJurisdicao();
			ppj.setPessoaProcuradoria(pessoaProcuradoria);
			ppj.setAtivo(true);
			ppj.setJurisdicao(jm.findByIdJurisdicao(Integer.parseInt(idJurisdicao)));
			listaPessoaProcuradoriaJurisdicao.add(ppj);			
		}
		return listaPessoaProcuradoriaJurisdicao;
	}

	public void criarLocalizacaoPessoa(PessoaProcurador procurador, Papel papel,Localizacao l) throws PJeBusinessException {
		UsuarioLocalizacao ul = new UsuarioLocalizacao();
		ul.setLocalizacaoFisica(l);
		ul.setUsuario(procurador.getPessoa());
		ul.setResponsavelLocalizacao(false);
		ul.setPapel(papel);
		usuarioLocalizacaoManager.persistAndFlush(ul);
	}
	
	public Boolean verificaPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria) {
		boolean retorno = false;
		List<PessoaProcuradoria> pessoaProcuradoriaList = recuperaPessoaProcuradoriaList(pessoaProcuradoria.getPessoa().getIdUsuario(), pessoaProcuradoria.getProcuradoria().getIdProcuradoria(), pessoaProcuradoria.getIdPessoaProcuradoria());
		for (PessoaProcuradoria pp : pessoaProcuradoriaList) {
			if (pp.getAtuacaoReal().equals(pessoaProcuradoria.getAtuacao())) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}
	
	public PessoaProcuradoria recuperaPessoaProcuradoria(
			Integer idProcurador, Integer idProcuradoria) {
		
		Search s = new Search(PessoaProcuradoria.class);
		addCriteria(s, Criteria.equals("pessoa.idUsuario", idProcurador));
		addCriteria(s, Criteria.equals("procuradoria.idProcuradoria", idProcuradoria));
		
		List<PessoaProcuradoria> ret = list(s);
		if (ret.size() > 0) {
			return ret.get(0);
		}
		
		return null;
	}
	
	public PessoaProcuradoria recuperaPessoaProcuradoria(
			PessoaProcurador procurador, Procuradoria procuradoria) {
	
		List<PessoaProcuradoria> list = recuperaPessoaProcuradoriaList(procurador.getIdUsuario(),
				procuradoria.getIdProcuradoria());
		
		if(list != null && list.size() > 0) {
			return list.get(0);
		}
		
		return null;
	}
	
	public PessoaProcuradoria getPessoaProcuradoria(Integer idProcurador, Integer idProcuradoria) {
		List<PessoaProcuradoria> list = recuperaPessoaProcuradoriaList(idProcurador, idProcuradoria);
		
		if(list != null && list.size() > 0) {
			return list.get(0);
		}
		
		return null;
	}	
	
	public List<PessoaProcuradoria> recuperaPessoaProcuradoriaList(Integer idProcurador, Integer idProcuradoria) {
		return recuperaPessoaProcuradoriaList(idProcurador, idProcuradoria, null);
	}
	
	public List<PessoaProcuradoria> recuperaPessoaProcuradoriaList(Integer idProcurador, Integer idProcuradoria, Integer idPessoaProcuradoria) {
		Search s = new Search(PessoaProcuradoria.class);
		if (idProcurador != null) {
			addCriteria(s, Criteria.equals("pessoa.idUsuario", idProcurador));
		}
		if (idProcuradoria != null) {
			addCriteria(s, Criteria.equals("procuradoria.idProcuradoria", idProcuradoria));
		}
		if (idPessoaProcuradoria != null && idPessoaProcuradoria != 0) {
			addCriteria(s, Criteria.notEquals("idPessoaProcuradoria", idPessoaProcuradoria));
		}
		return list(s);
	}	
	
	public Boolean existeOutraPessoaProcuradoriaVinculado(PessoaProcuradoria pessoaProcuradoria, Boolean chefeProcuradoriaValorAntigo) {
		Search s = new Search(PessoaProcuradoria.class);
		addCriteria(s, Criteria.notEquals("idPessoaProcuradoria", pessoaProcuradoria.getIdPessoaProcuradoria()));
		addCriteria(s, Criteria.equals("pessoa.idUsuario", pessoaProcuradoria.getPessoa().getIdUsuario()));
		addCriteria(s, Criteria.equals("procuradoria.localizacao.idLocalizacao", pessoaProcuradoria.getProcuradoria().getLocalizacao().getIdLocalizacao()));
		addCriteria(s, Criteria.equals("chefeProcuradoria", chefeProcuradoriaValorAntigo));
		return list(s) != null && list(s).size() > 0;
	}
	
	public List<PessoaProcuradoria> recuperaPessoaProcuradoria(Procuradoria procuradoria) {
		return recuperaPessoaProcuradoriaList(null, procuradoria.getIdProcuradoria());
	}

	public List<PessoaProcuradoria> recuperaPessoaProcuradoria(PessoaProcurador procurador) {
		return recuperaPessoaProcuradoriaList(procurador.getIdUsuario(), null);
	}	
	
	public void remove(PessoaProcuradoria pessoaProcuradoria) {
		Papel papel = getPapelPessoaProcuradoria(pessoaProcuradoria);
			
		UsuarioLocalizacao usuarioLocalizacao = usuarioLocalizacaoManager.getLocalizacao(
				pessoaProcuradoria.getPessoa().getPessoa(), pessoaProcuradoria.getProcuradoria().getLocalizacao(), papel);
		
		try {
			if (usuarioLocalizacao != null){
				PessoaFisica pessoa = pessoaProcuradoria.getPessoa().getPessoa();
				if (pessoa.getUsuarioLocalizacaoInicial() != null && pessoa.getUsuarioLocalizacaoInicial().equals(usuarioLocalizacao)) {
					pessoa.setUsuarioLocalizacaoInicial(null);
				}					
				if (!existeOutraPessoaProcuradoriaVinculado(pessoaProcuradoria, pessoaProcuradoria.getChefeProcuradoria())) {
					usuarioLocalizacaoManager.remove(usuarioLocalizacao);
				}
				super.remove(pessoaProcuradoria);
			}
		} catch (PJeBusinessException e) {
			new PJeBusinessException("Error ao remover PessoaProcuradoria");
		}
	}
	
	/**
	 * Verifica se determinado procurador possui cadastro de gestor em uma
	 * procuradoria.
	 * 
	 * @param idProcurador
	 * @param idProcuradoria
	 * @return
	 */
	public boolean isProcuradorGestor(Integer idProcurador,Integer idProcuradoria) {
		PessoaProcuradoria pessoaProcuradoria = getPessoaProcuradoria(idProcurador, idProcuradoria);
		if (pessoaProcuradoria != null && pessoaProcuradoria.getChefeProcuradoria()) {
			return true;
		}
		return false;
	}
		
	public List<PessoaProcuradoria> getListOrgaoRepresentacoesByTipo(PessoaProcurador p, TipoProcuradoriaEnum tipo){
		Search s = new Search(PessoaProcuradoria.class);
		
		try {
			s.addCriteria(Criteria.equals("pessoa", p));
			s.addCriteria(Criteria.equals("procuradoria.tipo", tipo));
		} catch (Exception e) {
			return null;
		}
		return list(s);
	}

	public void atualizaPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria, UsuarioLocalizacao usuarioLocalizacao) throws PJeBusinessException{

		if (pessoaProcuradoria.getAtuacao() == RepresentanteProcessualTipoAtuacaoEnum.D 
				&& pessoaProcuradoria.getPessoaProcuradoriaJurisdicaoList().size() <= 0){
				throw new PJeBusinessException("Nenhuma Jurisdição selecionada.");
		}
		Boolean chefeProcuradoriaValorAntigo = pessoaProcuradoria.getChefeProcuradoria();
		if ((pessoaProcuradoria.getAtuacao() == RepresentanteProcessualTipoAtuacaoEnum.G)){
			pessoaProcuradoria.setChefeProcuradoria(true);
		}else{
			pessoaProcuradoria.setChefeProcuradoria(false);
		}
		//Se não for Distribuidor e Possuir jurisdições, removê-las.
		if(pessoaProcuradoria.getAtuacao() != RepresentanteProcessualTipoAtuacaoEnum.D && 
				pessoaProcuradoria.getPessoaProcuradoriaJurisdicaoList().size() > 0){
			removeJurisdicoesPessoaProcuradoria(pessoaProcuradoria);
			pessoaProcuradoria.setPessoaProcuradoriaJurisdicaoList(new ArrayList<PessoaProcuradoriaJurisdicao>());
		}
		
		Papel papelAtual = getPapelPessoaProcuradoria(pessoaProcuradoria);
		if (pessoaProcuradoria.getChefeProcuradoria() != chefeProcuradoriaValorAntigo) {
			UsuarioLocalizacao ul = usuarioLocalizacaoManager.getLocalizacao(pessoaProcuradoria.getPessoa().getPessoa(), pessoaProcuradoria.getProcuradoria().getLocalizacao(), papelAtual);
			if (existeOutraPessoaProcuradoriaVinculado(pessoaProcuradoria, chefeProcuradoriaValorAntigo)) {
				if (ul == null) {
					criarLocalizacaoPessoa(pessoaProcuradoria.getPessoa(), papelAtual, pessoaProcuradoria.getProcuradoria().getLocalizacao());
				}
			} else {
				if (ul == null) {
					atualizaUsuarioLocalizacao(usuarioLocalizacao, papelAtual, pessoaProcuradoria.getProcuradoria().getLocalizacao());	
				} else {
					usuarioLocalizacaoManager.remove(usuarioLocalizacao);
				}
			}
		}
		
		this.persistAndFlush(pessoaProcuradoria);
	}
	
	public void atualizaPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria, UsuarioLocalizacao usuarioLocalizacao, List<String> jurisdicoes) throws PJeBusinessException{
		List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicaoList = preparaListaPessoaProcuradoriaJurisdicao(pessoaProcuradoria, jurisdicoes);
		pessoaProcuradoria.setPessoaProcuradoriaJurisdicaoList(pessoaProcuradoriaJurisdicaoList);
		this.atualizaPessoaProcuradoria(pessoaProcuradoria, usuarioLocalizacao);
	}
	
	private void atualizaUsuarioLocalizacao(UsuarioLocalizacao usuarioLocalizacao, Papel papel, Localizacao localizacaoFisica) throws PJeBusinessException {
		UsuarioLocalizacaoManager usuarioLocalizacaoManager = (UsuarioLocalizacaoManager) Component.getInstance("usuarioLocalizacaoManager");
		usuarioLocalizacao.setPapel(papel);
		usuarioLocalizacao.setLocalizacaoFisica(localizacaoFisica);
		usuarioLocalizacaoManager.persist(usuarioLocalizacao);
	}
	
	private void verificaRemoveLocalizacaoPadraProcurador(PessoaProcuradoria pessoaProcuraodoria) throws PJeBusinessException {
		LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance("localizacaoManager");
		PapelService papelService = (PapelService) Component.getInstance("papelService");
		Papel papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL);

		Localizacao local = localizacaoManager.getLocalizacaoExistente(papel+" - "+pessoaProcuraodoria.getPessoa().getNome()+" ("+pessoaProcuraodoria.getPessoa().getDocumentoCpfCnpj()+")");
		UsuarioLocalizacao ul = usuarioLocalizacaoManager.getLocalizacao(pessoaProcuraodoria.getPessoa().getPessoa(), local, papel);
		
		if (ul != null){
			try {
				usuarioLocalizacaoManager.remove(ul);
			} catch (PJeBusinessException e) {
				new PJeBusinessException("Error ao remover Localização Padrão");
			}
		}
			
	}
	
	private Papel getPapelPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria){
		PapelService papelService = (PapelService) Component.getInstance("papelService");
		Papel papel = null;
		
		if (pessoaProcuradoria.getChefeProcuradoria())
			papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL_GESTOR);
		else
		    papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL);
		
		return papel;
	}
	
	public UsuarioLocalizacao getUsuarioLocalizacaoPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria){
		Papel papel = getPapelPessoaProcuradoria(pessoaProcuradoria);
		
		UsuarioLocalizacao usuarioLocalizacao = usuarioLocalizacaoManager.getLocalizacao(pessoaProcuradoria.getPessoa().getPessoa(),
				  pessoaProcuradoria.getProcuradoria().getLocalizacao(), papel);
		
		return usuarioLocalizacao;
	}
	
	public void removeJurisdicoesPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria){
		PessoaProcuradoriaJurisdicaoManager pessoaProcuradoriaJurisdicaoManager = (PessoaProcuradoriaJurisdicaoManager) Component.getInstance("pessoaProcuradoriaJurisdicaoManager");
		List<PessoaProcuradoriaJurisdicao> ppj =  pessoaProcuradoria.getPessoaProcuradoriaJurisdicaoList();
		for(PessoaProcuradoriaJurisdicao jur : ppj){
			try {
				pessoaProcuradoriaJurisdicaoManager.remove(jur);
			} catch (PJeBusinessException e) {
				new PJeBusinessException("Error ao remover Jurisdição");
			}
		}
	}
	
	public List getGridAssociacoes(String inTipoProcuradoria, String idPessoa){
		return pessoaProcuradoriaDAO.getGridAssociacoes(inTipoProcuradoria, idPessoa); 
	}
}
