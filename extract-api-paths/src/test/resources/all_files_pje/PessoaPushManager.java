package br.com.infox.pje.manager;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.mindrot.jbcrypt.BCrypt;

import br.com.infox.trf.webservice.ConsultaClienteWebService;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.PessoaPushDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.TipoPessoaManager;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoa;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

@Name(PessoaPushManager.NAME)
@AutoCreate
public class PessoaPushManager extends BaseManager<PessoaPush>{

	public static final String NAME = "pessoaPushManager";
	
	@In
	private PessoaPushDAO pessoaPushDAO;

  	@In
  	private CadastroTempPushManager cadastroTempPushManager;
  
  	@In
  	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;
  	
  	@In
  	private TipoPessoaManager tipoPessoaManager;
  	
  	@In
  	private PessoaManager pessoaManager;
  	

	/**
	 * Método responsável por recuperar o objeto {@link PessoaPush} pelo login.
	 * 
	 * @param login Login.
	 * @return {@link PessoaPush}.
	 */
	public PessoaPush recuperarPessoaPushByLogin(String login) {
		return pessoaPushDAO.recuperarPessoaPushByLogin(login);
	}
	
	/**
	 * Método responsável por recuperar o objeto {@link PessoaPush} pelo código do hash. 
	 * 
	 * @param cdHash Código do hash.
	 * @return {@link PessoaPush}.
	 */
	public PessoaPush recuperarPessoaPushByHash(String cdHash){
		return pessoaPushDAO.recuperarPessoaPushByHash(cdHash);
	}
	
	/**
	 * Método responsável por autenticar o usuario push.
	 * 
	 * @param pessoaPush {@link PessoaPush}.
	 * @param password Senha.
	 * @return Verdadeiro se a senha fornecida é válida. Falso, caso contrário. 
	 */
	public boolean authenticate(PessoaPush pessoaPush, String password){
		return BCrypt.checkpw(password, pessoaPush.getSenha());
	}

	@Override
	protected PessoaPushDAO getDAO() {
		return pessoaPushDAO;
	}

	/**
 	 * Método responsável por tornar a {@link PessoaPush}, seja {@link PessoaFisica} ou
 	 * {@link PessoaJuridica}, através do {@link DadosReceitaPessoa}.
 	 * 
 	 * @param pessoaPush
 	 *            a {@link PessoaPush} que se deseja migrar
 	 * @throws Exception
 	 *             caso não seja possível consultar os dados via
 	 *             {@link ConsultaClienteWebService}
 	 */
  	public void tornarPessoaFisicaJuridica(PessoaPush pessoaPush) throws Exception {
  		String documento = pessoaPush.getNrDocumento();
  		TipoPessoaEnum tipoPessoa = tipoPessoaManager.tipoPessoaCondizenteComDocumento(documento);
  		boolean migrado = false;
  		if (tipoPessoa.equals(TipoPessoaEnum.F) && InscricaoMFUtil.verificaCPF(documento)) {
  			DadosReceitaPessoaFisica dadosReceitaPF = (DadosReceitaPessoaFisica) ConsultaClienteWebService.instance().consultaDados(tipoPessoa, documento, true);
  			logger.info("Iniciando migração para pessoa física de {0} ({1}).", dadosReceitaPF.getNome(), dadosReceitaPF.getNumCPF());
  			
  			logger.info("Criando pessoa física.");
  			pessoaManager.criarPessoaPelaReceita(dadosReceitaPF);
  						
  			migrado = true;
  		} else if (tipoPessoa.equals(TipoPessoaEnum.J) && InscricaoMFUtil.verificaCNPJ(documento)) {
  			DadosReceitaPessoaJuridica dadosReceitaPJ = (DadosReceitaPessoaJuridica) ConsultaClienteWebService.instance().consultaDados(tipoPessoa, documento, true);
  			logger.info("Iniciando migração para pessoa jurídica de {0} ({1}).", dadosReceitaPJ.getNomeFantasia(), dadosReceitaPJ.getNumCNPJ());
  			
  			logger.info("Criando pessoa jurídica.");
  			pessoaManager.criarPessoaPelaReceita(dadosReceitaPJ);
  			
  			migrado = true;
  		} else {
  			throw new PJeBusinessException("pessoaPush.atualizacaoCadastral.erro", Messages.instance().get("pessoaPush.atualizacaoCadastral.verificarDocumento"));
  		}
  		
  		if (migrado) {
  			removerPessoaPush(pessoaPush);
  			removerCadastroTempPush(pessoaPush);
  			logger.info("Removida pessoa push e cadastro temporário.");
  		}
  	}
  
  	/**
  	 * Método responsável por remover o cadastro temporário da
  	 * {@link PessoaPush} que foi migrada.
  	 * 
  	 * @param pessoaPush
  	 *            a pessoa push que se deseja remover o cadastro temporário
  	 */
  	private void removerCadastroTempPush(PessoaPush pessoaPush) throws PJeBusinessException {
  		CadastroTempPush cadastroTempPush = cadastroTempPushManager.recuperarCadastroTempPushByLogin(pessoaPush.getNrDocumento());
  		if (cadastroTempPush != null) {
  			cadastroTempPushManager.remove(cadastroTempPush);			
  			cadastroTempPushManager.flush();
  			logger.info("Removido cadastro temporário Push.");
  		}
  	}
  
 	/**
 	 * Método responsável por remover o registro de {@link PessoaPush} do
 	 * usuário que foi migrado.
 	 * 
 	 * @param pessoaPush
 	 *            o registro Push que se deseja remover após a migração.
 	 * @throws Exception
 	 */
  	private void removerPessoaPush(PessoaPush pessoaPush) throws PJeBusinessException {
  		if (pessoaPush != null) {
  			EntityManager em = EntityUtil.getEntityManager();
  			if(em.contains(pessoaPush)) {
  				remove(pessoaPush);
  			} else {
  				remove(em.merge(pessoaPush));
  			}
  			flush();
  			logger.info("Removido pessoa Push.");
  		}
  	}
}
