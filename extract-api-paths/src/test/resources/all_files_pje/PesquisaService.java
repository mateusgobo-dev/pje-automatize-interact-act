/**
 * 
 */
package br.jus.cnj.pje.servicos;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ElasticDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle do serviço de pesquisa por índices externos.
 * 
 * @author cristof
 *
 */
@Name(value="pesquisaService")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Startup(depends = { "org.jboss.seam.async.dispatcher", "indexador" })
@Install(dependencies = { "org.jboss.seam.async.dispatcher", "indexador" })
public class PesquisaService {
	
	@In(value=Parametros.ELASTICSEARCHIDXURL, required=false)
	private String indexURL;
	
	@In(value=Parametros.ELASTICSEARCHIDXNAME, required=false)
	private String indexName;
	
	@In
	private Indexador indexador;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	@In
	private Events events;

	@Logger
	private Log logger;
	
	private boolean active;
	
	@Create
	public void init(){
		active = false;
		if(indexURL == null || indexURL.isEmpty() || indexName == null || indexName.isEmpty()){
			logger.warn("O serviço de indexação automática de objetos não está ativo. Para ativá-lo,  por favor defina os parâmetros [{0}] e [{1}].",
					Parametros.ELASTICSEARCHIDXURL, Parametros.ELASTICSEARCHIDXNAME);
			return;
		}
		try {
			new URL(indexURL);
		} catch (MalformedURLException e) {
			logger.warn("O serviço de indexação automática de objetos está apontando para uma URL inválida ({0}). "
					+ "Por favor, corrija o parâmetro [{1}].", indexURL, Parametros.ELASTICSEARCHIDXURL);
			return;
		}
		active = indexador.isEnabled();
	}

	public void reindexarDocumentosIdentificacao() throws NoSuchFieldException{
		if(!active){
			logger.warn("O servio de indexao externa no est disponvel");
			return;
		}
		if(!Contexts.getApplicationContext().isSet("pje:reindexacao:documentosidentificacao:iniciada")){
			Contexts.getApplicationContext().set("pje:reindexacao:documentosidentificacao:iniciada", true);
		}else{
			return;
		}
		Search search = new Search(PessoaDocumentoIdentificacao.class);
		search.setRetrieveField("idDocumentoIdentificacao");
		search.addCriteria(
				Criteria.or(
						Criteria.equals("ativo", true),
						Criteria.equals("usadoFalsamente", false)
				)
		);
		Integer batchSize = 100;
		Long count = processoDocumentoManager.count(search);
		logger.info("Indexando {0} documentos em {1} chamadas.", count, (Math.ceil(count*1D/batchSize)));
		events.raiseAsynchronousEvent(Eventos.REINDEXAR_DOCUMENTOS_IDENTIFICACAO, count);
		logger.info("A indexao ser executada em outra thread.");
	}

	@Transactional
	public Integer elasticIndex(Integer offset, Integer size) throws NoSuchFieldException{
		Search search = new Search(ProcessoTrf.class);
		search.setRetrieveField("idProcessoTrf"); 
		search.addCriteria(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		search.setFirst(offset);
		search.setMax(size);
		List<Integer> ids = ComponentUtil.getProcessoJudicialManager().list(search);
		Integer ret = null;
		for(Integer id: ids){
			ComponentUtil.getComponent(Events.class).raiseAsynchronousEvent("INDEXAR_PROCESSO", id);
			ret = id;
		}
		return ret;
	}
	
	public void updateMappings(){
		indexador.updateMappings();
		if(FacesMessages.instance() != null){
			FacesMessages.instance().add(Severity.INFO, "Atualização concluída.");
		}
	}
	
	public JSONObject search_(String query) throws JSONException{
		ElasticDAO<ProcessoTrf> processoDAO = new ElasticDAO<ProcessoTrf>() {
		};
		processoDAO.setIndexador(indexador);
		Search s = new Search(ProcessoTrf.class);
		try {
			
			Criteria nomeCriteria = Criteria.contains("processoParteList.pessoa.nomesPessoa.nome", query);
			s.addCriteria(nomeCriteria);

			Criteria tipoNomeCriteria = Criteria.in("processoParteList.pessoa.nomesPessoa.tipo", new TipoNomePessoaEnum[] {TipoNomePessoaEnum.C, TipoNomePessoaEnum.A, TipoNomePessoaEnum.D } );
			s.addCriteria(tipoNomeCriteria);
			
			return processoDAO.search(s);
		} catch (NoSuchFieldException | PJeBusinessException e) {
			logger.warn("PesquisaService.search_ falha na consulta: {0}.", e, query);
		}
		return new JSONObject();
	}
	
}
