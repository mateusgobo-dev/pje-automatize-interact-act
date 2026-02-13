/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.ibpm.home;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import br.com.infox.ibpm.bean.ImportarFluxoFileBean;
import br.com.infox.ibpm.exception.ImportExportException;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.service.ExportarFluxoService;
import br.com.infox.ibpm.service.ImportarFluxoService;
import br.com.infox.ibpm.validator.LocalizacaoValidator;
import br.com.infox.ibpm.validator.PapelValidator;
import br.com.infox.ibpm.validator.RegistraEventoValidator;
import br.com.infox.ibpm.validator.VerificaEventoValidator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name(FluxoHome.NAME)
public class FluxoHome extends AbstractFluxoHome<Fluxo>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "fluxoHome";
	private List<Fluxo> fluxoList;

	private boolean importar = false;
	private boolean renderedPapel = false;
	private boolean renderedLocalizacao = false;
	private boolean renderedAgrupamento = false;
	private boolean validado;
	private String oldNomeFluxo;

	private List<Localizacao> localizacaoList;
	private List<Papel> papelList;
	private List<Agrupamento> agrupamentoList;

	private ImportarFluxoFileBean importarFluxoFileBean = new ImportarFluxoFileBean();

	@In
	private ExportarFluxoService exportarFluxoService;
	
	@In
	private ImportarFluxoService importarFluxoService;
	
	@In
	private FluxoManager fluxoManager;

	public static FluxoHome instance(){
		return (FluxoHome) Component.getInstance(NAME);
	}

	@Override
	public void setId(Object id){
		super.setId(id);
		if (instance == null){
			oldNomeFluxo = null;
		}
		else{
			oldNomeFluxo = instance.getFluxo();
		}
	}

	@Override
	public void onClickSearchTab(){
		super.onClickSearchTab();
		importar = false;
		importarFluxoFileBean.clear();
	}

	private void verificaPublicacao(){
		Date data = new Date();
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		String dataHoje = formato.format(data);
		String dataInicio = formato.format(getInstance().getDataInicioPublicacao());

		if (dataHoje.equals(dataInicio)){
			getInstance().setPublicado(Boolean.TRUE);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate(){
		verificaPublicacao();
		if (oldNomeFluxo != null && !oldNomeFluxo.equals(getInstance().getFluxo())){
			fluxoManager.changeNomeFluxo(instance, oldNomeFluxo);
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String update(){
		String ret = null;

		try{

			if (getInstance().getXml() != null){
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				InputSource is = new InputSource();
				is.setEncoding("iso-8859-1");
				is.setByteStream(new ByteArrayInputStream(getInstance().getXml().getBytes("iso-8859-1")));

				Document doc = docBuilder.parse(is);

				Node earth = doc.getFirstChild();

				NamedNodeMap earthAttributes = earth.getAttributes();

				Node namedItem = earthAttributes.getNamedItem("name");

				String processInstanceAnterior = namedItem.getNodeValue();
				namedItem.setNodeValue(getInstance().getFluxo());

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");

				StreamResult result = new StreamResult(new StringWriter());

				DOMSource source = new DOMSource(doc);
				transformer.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
				transformer.transform(source, result);

				String xmlString = result.getWriter().toString();
				// System.out.println(xmlString);

				String sql = "update jbpm_processdefinition pd set name_= '" + getInstance().getFluxo()
						+ "' where name_ = '" + processInstanceAnterior + "'";

				JbpmUtil.getJbpmSession().createSQLQuery(sql)
						.addSynchronizedQuerySpace("jbpm_processdefinition")
						.executeUpdate();

				getInstance().setXml(xmlString);

				List<Fluxo> fluxos = EntityUtil.getEntityList(Fluxo.class);
				for (Fluxo f : fluxos){
					if (f.getXml().contains("sub-process name=\"" + processInstanceAnterior)){
						f.setXml(f.getXml().replaceAll("sub-process name=\"" + processInstanceAnterior,
								"sub-process name=\"" + getInstance().getFluxo()));
						getEntityManager().persist(f);
					}
				}
			}

			ret = super.update();
		} catch (Exception e){
			System.out.println("Erro de restrição: possivelmente um campo foi duplicado.");
			throw new RuntimeException(e);
		}
		return ret;
	}

	/*
	 * Remoção Lógica. Se possuir algum processo vinculado, verifica se o msm está arquivado ou não.
	 */
	@Override
	public String remove(Fluxo obj){
		setInstance(obj);
		if (!possuiProcessoVinculado(obj) && !possuiAssuntoVinculado(obj)){
			obj.setAtivo(Boolean.FALSE);
			super.update();
			newInstance();
			refreshGrid("fluxoGrid");
		}
		else{
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Este registro está em uso e não poderá ser excluído.");
		}
		return "updated";
	}

	private boolean possuiProcessoVinculado(Fluxo fluxo){
		String query = "select count(o) from Processo o " +
				"where o.fluxo = :fluxo2 ";
		Query q = getEntityManager().createQuery(query).setMaxResults(1);
		q.setParameter("fluxo2", fluxo);
		return (Long) q.getSingleResult() > 0;
	}

	private boolean possuiAssuntoVinculado(Fluxo fluxo){
		String query = "select count(o) from Assunto o " +
				"where o.fluxo = :fluxo2 ";
		Query q = getEntityManager().createQuery(query).setMaxResults(1);
		q.setParameter("fluxo2", fluxo);
		return (Long) q.getSingleResult() > 0;
	}

	@SuppressWarnings("unchecked")
	public List<Fluxo> getFluxoList(){
		if (fluxoList == null){
			fluxoList = new ArrayList<Fluxo>();
			for (Fluxo f : (List<Fluxo>) getEntityManager().createQuery(
					"select f from Fluxo f where f.ativo = true order by f.fluxo").getResultList()){
				if (Identity.instance().hasRole("/fluxo/" + f.getFluxo())){
					fluxoList.add(f);
				}
			}
		}
		return fluxoList;
	}

	/**
	 * Exporta o fluxo e permite o download dos xmls compactados.
	 */
	public void exportarFluxo(){
		String xml = getInstance().getXml();
		try{
			exportarFluxoService.exportar(xml, instance.getCodFluxo(), instance.getFluxo());
		} catch (ImportExportException e){
			FacesMessages.instance().add(Severity.WARN, "Nenhum fluxo definido.");
		}
	}

	/**
	 * Valida o arquivo enviado pelo usuário e inicia a importação se o mesmo estiver correto.
	 */
	public void validateUploadedZip(){
		if (importarFluxoFileBean.getFileName() != null){
			if (importarFluxoFileBean.getFileName().startsWith("Fluxo_")){
				try{
					importarFluxoService.importar(instance, importarFluxoFileBean.getData());
					setValidado(true);
				} catch (ImportExportException e){
					if (e.getDescription().equals(ImportarFluxoService.READING_FILE_MESSAGE)){
						FacesMessages.instance().add(Severity.ERROR, ImportarFluxoService.READING_FILE_MESSAGE);
					}
					else{
						createListsToPersist(importarFluxoService.getWarningList());
					}
				}
			}
			else{
				FacesMessages.instance().add(Severity.ERROR, "Arquivo fora do padrão a ser importado.");
			}
		}
		else{
			FacesMessages.instance().add(Severity.ERROR, "Favor adicionar o zip contendo o fluxo exportado.");
		}
	}

	/**
	 * Cria as listas para que sejam incluídos os valores necessários para finalizar a importação do fluxo.
	 * 
	 * @param warningList
	 */
	private void createListsToPersist(List<String> warningList){
		renderedAgrupamento = false;
		renderedLocalizacao = false;
		renderedPapel = false;
		for (String s : warningList){
			int indexDescricao = s.indexOf("=") + 1;
			if (s.startsWith(LocalizacaoValidator.class.getSimpleName())){
				Localizacao l = new Localizacao();
				l.setLocalizacao(s.substring(indexDescricao));
				l.setAtivo(true);
				if (!renderedLocalizacao){
					renderedLocalizacao = true;
					localizacaoList = new ArrayList<Localizacao>();
				}
				localizacaoList.add(l);
			}
			else if (s.startsWith(PapelValidator.class.getSimpleName())){
				Papel p = new Papel();
				p.setIdentificador(s.substring(indexDescricao));
				if (!renderedPapel){
					renderedPapel = true;
					papelList = new ArrayList<Papel>();
				}
				papelList.add(p);
			}
			else if (s.startsWith(RegistraEventoValidator.class.getSimpleName())
					|| s.startsWith(VerificaEventoValidator.class.getSimpleName())){
				Agrupamento a = new Agrupamento();
				a.setAgrupamento(s.substring(indexDescricao));
				a.setAtivo(true);
				if (!renderedAgrupamento){
					renderedAgrupamento = true;
					agrupamentoList = new ArrayList<Agrupamento>();
				}
				agrupamentoList.add(a);
			}
		}
	}

	public void importarFluxo(){
		if (isManaged()){
			update();
		}
		else{
			persist();
		}
		importar = false;
		validado = false;
	}

	/**
	 * Popula a base com os registros que estão faltando para a importação do fluxo.
	 */
	public void inserirDependencias(){
		if (renderedLocalizacao){
			for (Localizacao l : localizacaoList){
				getEntityManager().persist(l);
			}
			renderedLocalizacao = false;
		}
		if (renderedPapel){
			for (Papel p : papelList){
				getEntityManager().persist(p);
			}
			renderedPapel = false;
		}
		if (renderedAgrupamento){
			for (Agrupamento a : agrupamentoList){
				getEntityManager().persist(a);
			}
			renderedAgrupamento = false;
		}
		getEntityManager().flush();
		FacesMessages.instance().add("Registros inseridos, por favor valide a importação novamente.");
	}

	public void changeForm(){
		if (importar){
			importar = false;
		}
		else{
			importar = true;
		}
	}

	public void setImportar(boolean importar){
		this.importar = importar;
	}

	public boolean getImportar(){
		return importar;
	}

	public void setRenderedPapel(boolean renderedPapel){
		this.renderedPapel = renderedPapel;
	}

	public boolean getRenderedPapel(){
		return renderedPapel;
	}

	public void setRenderedLocalizacao(boolean renderedLocalizacao){
		this.renderedLocalizacao = renderedLocalizacao;
	}

	public boolean getRenderedLocalizacao(){
		return renderedLocalizacao;
	}

	public void setRenderedAgrupamento(boolean renderedAgrupamento){
		this.renderedAgrupamento = renderedAgrupamento;
	}

	public boolean getRenderedAgrupamento(){
		return renderedAgrupamento;
	}

	public void setLocalizacaoList(List<Localizacao> localizacaoList){
		this.localizacaoList = localizacaoList;
	}

	public List<Localizacao> getLocalizacaoList(){
		return localizacaoList;
	}

	public void setPapelList(List<Papel> papelList){
		this.papelList = papelList;
	}

	public List<Papel> getPapelList(){
		return papelList;
	}

	public void setAgrupamentoList(List<Agrupamento> agrupamentoList){
		this.agrupamentoList = agrupamentoList;
	}

	public List<Agrupamento> getAgrupamentoList(){
		return agrupamentoList;
	}

	public void setValidado(boolean validado){
		this.validado = validado;
	}

	public boolean getValidado(){
		return validado;
	}

	public void setImportarFluxoFileBean(ImportarFluxoFileBean importarFluxoFileBean){
		this.importarFluxoFileBean = importarFluxoFileBean;
	}

	public ImportarFluxoFileBean getImportarFluxoFileBean(){
		return importarFluxoFileBean;
	}

}