package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.DocumentoSessaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.AssinaturaDigitalService;
import br.jus.pje.nucleo.entidades.DocumentoSessao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.enums.TipoDocumentoSessaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("documentoSessaoManager")
public class DocumentoSessaoManager extends BaseManager<DocumentoSessao>{

	@In(create = true)
	private DocumentoSessaoDAO documentoSessaoDAO; 
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private AssinaturaDigitalService assinaturaDigitalService;
	
	private static final LogProvider log = Logging.getLogProvider(DocumentoSessaoManager.class);
	
	@Override
	protected BaseDAO<DocumentoSessao> getDAO() {
		return documentoSessaoDAO;
	}
	
	public static DocumentoSessaoManager instance() {
		return ComponentUtil.getComponent(DocumentoSessaoManager.class);
	}
	
	public DocumentoSessao findBySessao(Sessao sessao){
		Search s = new Search(DocumentoSessao.class);
		try {
			s.addCriteria(Criteria.equals("sessao", sessao));
		} catch (NoSuchFieldException e) {
			log.error(e.getMessage());
			return null;
		}
		s.setMax(1);
		List<DocumentoSessao> lista = list(s);
		if(lista != null && !lista.isEmpty()){
			return lista.get(0);
		} else {
			return null;
		}
		
	}
	
	public DocumentoSessao find(Sessao sessao, TipoDocumentoSessaoEnum tipoDoc){
		DocumentoSessao retorno = null;
		Search s = new Search(DocumentoSessao.class);
		addCriteria(s,Criteria.equals("sessao", sessao), Criteria.equals("tipoDocumento", tipoDoc));
		s.setMax(1);
		List<DocumentoSessao> lista = list(s);
		if(lista != null && !lista.isEmpty()){
			retorno = lista.get(0);
		}
		return retorno;
	}

	public DocumentoSessao salvar(DocumentoSessao documentoSessao) throws PJeBusinessException {
		
		if (documentoSessao.getIdDocumentoSessao() == null) {
			persist(documentoSessao);
		}
		else {
			documentoSessao = merge(documentoSessao);
		}
		
		flush();
		
		return documentoSessao;
	}
	
	/**
	 * Metodo responsavel por verificar se ja existe uma ata de julgamento criada para a sessao, caso exista retorna 
	 * se nao existir cria uma nova ata de julgamento baseada no modelo de expediente. 
	 * @param sessao A sessão que a ata de julgamento pertence
	 * @return Uma nova ata de julgamento ou uma ata de julgamento previamente inserida para sessao informada
	 */
	public DocumentoSessao recuperarOuCriarNovaAtaJulgamentoPorSessao(Sessao sessao) {
		
		DocumentoSessao retorno = null;
		try {
			return recuperarOuCriarNovo(sessao, TipoDocumentoSessaoEnum.A, ParametroUtil.instance().getModeloAtaJulgamento());
		} catch (PJeBusinessException e) {
			log.error("Houve erro na traduo do modelo de documento da ata de julgamento");
		}
		return retorno;

	}
	
	public DocumentoSessao recuperarOuCriarNovo(Sessao sessao, TipoDocumentoSessaoEnum tipoDoc, ModeloDocumento modelo) throws PJeBusinessException {
		DocumentoSessao documento = find(sessao, tipoDoc);
		if(documento == null){
			documento = new DocumentoSessao();
			documento.setSessao(sessao);
			documento.setTipoDocumento(tipoDoc);
			if (modelo != null) {
				ModeloDocumentoLocal modeloLocal = ComponentUtil.getModeloDocumentoLocalManager().findById(modelo.getIdModeloDocumento());
				if(modeloLocal != null) {
					documento.setModeloDocumentoSessao(modeloDocumentoManager.traduzirModelo(modeloLocal.getTipoEditor(), modelo.getModeloDocumento()));
				}
			}
		}
				
		return documento;
	}

	public void concluirAssinaturaAtaJulgamento(DocumentoSessao ataJulgamento, Pessoa pessoaLogada) throws Exception {

		this.assinaturaDigitalService.validarAssinaturaDigitalEhPessoaLogada(
				ataJulgamento.getModeloDocumentoSessao().getBytes(), 
				ataJulgamento.getSignature(), 
				ataJulgamento.getCertChain(),
				SignatureAlgorithm.MD5withRSA.name(),
				pessoaLogada
		);
		
		ataJulgamento.setDataAssinatura(new Date());
		ataJulgamento.setUsuario(pessoaLogada);
		
		salvar(ataJulgamento);
	}
}