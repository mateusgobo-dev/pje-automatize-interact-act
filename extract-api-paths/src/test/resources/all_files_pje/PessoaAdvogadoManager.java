/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.DadosAdvogadoOABDAO;
import br.jus.cnj.pje.business.dao.PessoaAdvogadoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

/**
 * @author cristof
 *
 */
@Name(PessoaAdvogadoManager.NAME)
public class PessoaAdvogadoManager
		extends
		AbstractPessoaFisicaEspecializadaManager<PessoaAdvogado, PessoaAdvogadoDAO> {
	
	public static final String NAME = "pessoaAdvogadoManager";
	
	@In
	private PessoaAdvogadoDAO pessoaAdvogadoDAO;
	
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected PessoaAdvogadoDAO getDAO() {
		return pessoaAdvogadoDAO;
	}
	
	/**
	 * Atribui a uma pessoa dada, se já não tiver, um perfil de advogado.
	 * 
	 * @param pessoa a pessoa física a quem se pretende atribuir o perfil
	 * @return a {@link PessoaAdvogado} já vinculada à pessoa física.
	 */
	public PessoaAdvogado especializa(PessoaFisica pessoa) throws PJeBusinessException {
		PessoaAdvogado adv = pessoa.getPessoaAdvogado();
		if(adv == null){
			 adv = pessoaAdvogadoDAO.especializa(pessoa);
			pessoa.setPessoaAdvogado(adv);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.ADV);
		return adv;
	}
	
	/**
	 * Suprime de uma pessoa física a especialização de advogado.
	 * @param pessoa
	 * @return
	 * @throws PJeBusinessException
	 */
	public PessoaAdvogado desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaAdvogado adv = pessoa.getPessoaAdvogado();
		if(adv != null){
			adv = pessoaAdvogadoDAO.desespecializa(pessoa);
			pessoa.setPessoaAdvogado(adv);
		}
		return adv;
	}
	
	public PessoaAdvogado complementaCadastro(PessoaAdvogado adv, Estado estado, String numeroInscricao) throws PJeBusinessException{
		return complementaCadastro(adv, estado, numeroInscricao, null);
	}
	
	public PessoaAdvogado complementaCadastro(PessoaAdvogado adv, Estado estado, String numeroInscricao, String letraOab) throws PJeBusinessException{
		letraOab = StringUtils.isNotBlank(adv.getLetraOAB()) ? adv.getLetraOAB() : letraOab;
		
		adv.setUfOAB(estado);
		adv.setDataCadastro(new Date());
		adv.setNumeroOAB(numeroInscricao);
		adv.setLetraOAB(letraOab);
		adv.setValidado(true);
		adv.setAtivo(true);
		return persist(adv);
	}

	
	/**
	 * PJEII-18039. Exibe a OAB do advogado junto com o nome e CPF. 
	 * 
	 * @param processoParte
	 * @return advogado 
	 */
	public PessoaAdvogado getPessoaAdvogado(int id){
			PessoaAdvogado advogado = (PessoaAdvogado) pessoaAdvogadoDAO.find(id);
			return advogado;
	}
	
	
	
	@Override
	public PessoaAdvogado persist(PessoaAdvogado entity)
			throws PJeBusinessException {
	
		if(entity.getDataCadastro() == null){
			entity.setDataCadastro(new Date());
		}

		
		return super.persist(entity);
	}
	
	/**
     * Retorna a lista de advogados ativos do processo informado como parmetro.
     * 
     * @param idProcesso Identificador do processo.
     * @return Lista de advogados ativos do processo informado como parmetro.
	 */
	public List<PessoaAdvogado> findByProcesso(int idProcesso){       
		return pessoaAdvogadoDAO.findByProcesso(idProcesso, null, null, true);
	}
	 
	/**
     * Retorna a lista de advogados ativos do processo informado como parmetro, no polo indicado.
     * 
     * @param idProcesso Identificador do processo.
     * @param polo Polo processual.
     * @return Lista de advogados ativos do processo informado como parmetro, no polo indicado.
	 */
	public List<PessoaAdvogado> findByProcesso(int idProcesso, ProcessoParteParticipacaoEnum polo){       
		return pessoaAdvogadoDAO.findByProcesso(idProcesso, polo, null, true);
	}
	 
	/**
     * Retorna a lista de advogados ativos do processo informado como parmetro, no polo e tipo de inscrio indicados.
     * 
     * @param idProcesso Identificador do processo.
     * @param polo Polo processual.
     * @param tipoInscricao Tipo de inscrio.
     * @return Lista de advogados ativos do processo informado como parmetro, no polo e tipo de inscrio indicados.
	 */
    public List<PessoaAdvogado> findByProcesso(int idProcesso, ProcessoParteParticipacaoEnum polo, 
    		PessoaAdvogadoTipoInscricaoEnum tipoInscricao){
    	
        return pessoaAdvogadoDAO.findByProcesso(idProcesso, polo, tipoInscricao, true);
    }

    /**
     * Retorna a lista de advogados do processo informado como parmetro, no polo, tipo de inscrio e status indicados.
     * 
     * @param idProcesso Identificador do processo.
     * @param polo Polo processual.
     * @param tipoInscricao Tipo de inscrio.
     * @param isAtivo Indica se o advogado est ativo ou no. 
     * @return Lista de advogados do processo informado como parmetro, no polo, tipo de inscrio e status indicados.
     */
    public List<PessoaAdvogado> findByProcesso(int idProcesso, ProcessoParteParticipacaoEnum polo, 
    		PessoaAdvogadoTipoInscricaoEnum tipoInscricao, boolean isAtivo){
    	
        return pessoaAdvogadoDAO.findByProcesso(idProcesso, polo, tipoInscricao, isAtivo);
    }
	
	/**
	 * Retorna situação do advogado de acordo com informação provenida da OAB
	 * @param processoParte 
	 * @return situação do advogado
	 */
	public String situacaoAdvogado(ProcessoParte processoParte) {
		String result = processoParte.getTipoParte().toString();
		
		if (processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
			PessoaAdvogado pessoaAdvogado = this.pessoaAdvogadoDAO.findByProcessoParte(processoParte);
			if (pessoaAdvogado != null) {
				if (Authenticator.isUsuarioInterno()) {
					List<DadosAdvogadoOAB> dadosAdvogado = ComponentUtil.getComponent(DadosAdvogadoOABDAO.class)
						.findByPessoaAdvogado(pessoaAdvogado);
					
					if (dadosAdvogado == null || dadosAdvogado.isEmpty()) {
						result = "ADVOGADO - (Dados da OAB não encontrados)";
					} else {
						result = "ADVOGADO - (" + dadosAdvogado.get(0).getSituacaoInscricao() + ")";
					}
				} else {
					result = "ADVOGADO";
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Recupera um advogado de acordo com os parâmetros informados.
	 * 
	 * @param ufOab UF.
	 * @param numeroOab Número da OAB.
	 * @param letraOab Letra.
	 * @return Um advogado de acordo com os parâmetros informados.
	 * @throws PJeBusinessException
	 */
	public PessoaAdvogado recuperarAdvogado(Estado ufOab, String numeroOab, String letraOab) throws PJeBusinessException {
		PessoaAdvogado resultado = null;
		
		ArrayList<Criteria> criteriasAdvogado = new ArrayList<Criteria>();
		criteriasAdvogado.add(Criteria.equals("ufOAB.idEstado", ufOab.getIdEstado()));
		criteriasAdvogado.add(Criteria.equals("numeroOAB", numeroOab));

		if(StringUtils.isNotBlank(letraOab)){
			criteriasAdvogado.add(Criteria.equals("letraOAB", letraOab));
		}
		
		try {
			Search searchAdvogado = new Search(PessoaAdvogado.class);
			searchAdvogado.addCriteria(criteriasAdvogado);
			
			List<PessoaAdvogado> advogados = list(searchAdvogado);
			if (advogados != null && !advogados.isEmpty()) {
				resultado = advogados.get(0);
			}
		} catch (NoSuchFieldException ex) {
			throw new PJeBusinessException("Ocorreu um erro: " + ex.getMessage());
		}
		return resultado;
	}

	public PessoaAdvogado recuperarInscricao(DadosAdvogadoOAB dadosadvogadoOAB) throws PJeBusinessException {
		PessoaAdvogado retorno = new PessoaAdvogado();
		
		String regex = "^(\\D{2})?(\\d{1,7})?(\\D{1})?$";
		
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(dadosadvogadoOAB.getNumInscricao());
		
		// Se o padrão do número OAB for condizente com a respectiva regra os dados do advogado serão atualizados.
		if (matcher.find()) {
			String numeroInscricao = "";
			String ufOab = matcher.group(1);
			String numeroOab = StringUtil.retiraZerosEsquerda(matcher.group(2));
			if (StringUtils.isNotBlank(ufOab)) {
				numeroInscricao = numeroInscricao.concat(ufOab);
			}
			if (StringUtils.isNotBlank(numeroOab)) {
				numeroInscricao = numeroInscricao.concat(numeroOab);
			}
			if (StringUtils.isNotBlank(numeroInscricao)) {
				retorno.setNumeroOAB(numeroInscricao);
			}
			retorno.setLetraOAB(matcher.group(3));
		}
		return retorno;		
	}

}
