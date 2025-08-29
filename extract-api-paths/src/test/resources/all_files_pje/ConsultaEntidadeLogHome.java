/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.bean.ConsultaEntidadeLog;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Scope(ScopeType.CONVERSATION)
@Name("consultaEntidadeLogHome")
@BypassInterceptors
public class ConsultaEntidadeLogHome implements Serializable {

	private static final long serialVersionUID = 1L;
	private ConsultaEntidadeLog instance = new ConsultaEntidadeLog();
	private String nomeClasse;
	private String nomePackage;
	private String idEntidade;
	private Integer idPesquisa;

	public Integer getIdPesquisa() {
		return idPesquisa;
	}

	public void setIdPesquisa(Integer idFluxoPesquisa) {
		this.idPesquisa = idFluxoPesquisa;
	}

	public ConsultaEntidadeLog getInstance() {
		return instance;
	}

	public void setInstance(ConsultaEntidadeLog instance) {
		this.instance = instance;
	}

	public boolean isEditable() {
		return true;
	}

	public EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	public void limparTela() {
		instance = new ConsultaEntidadeLog();
		setIdPesquisa(null);
		setNomeClasse(null);
		setIdEntidade(null);
		setNomePackage(null);
	}

	public TipoOperacaoLogEnum[] getTipoOperacaoLogEnumValues() {
		return TipoOperacaoLogEnum.values();
	}

	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}

	public String getIdEntidade() {
		return idEntidade;
	}

	public void setIdEntidade(String idEntidade) {
		this.idEntidade = idEntidade;
	}

	public void setNomePackage(String nomePackage) {
		this.nomePackage = nomePackage;
	}

	public String getNomePackage() {
		return nomePackage;
	}

	/**
	 * Metodo que recebe um home e guarda o nome da classe e o id da entidade
	 * deste home. Essas valores são utilizados pela grid de log na aba de
	 * autidoria dos cadastros.
	 * 
	 * @param home
	 */
	public void setDadosPesquisaEntidade(AbstractHome<?> home) {
		instance = new ConsultaEntidadeLog();
		instance.setDataFim(null);
		instance.setDataInicio(null);
		setIdEntidade(home.getId().toString());
		setNomeClasse(home.getEntityClass().getSimpleName());
		setNomePackage(home.getEntityClass().getPackage().getName());
	}

	public String getHomeName() {
		return "consultaEntidadeLogHome";
	}

	public List<EntityLogDetail> getEntityLogDetailList(Integer idEntityLog) {
		EntityLog log = getEntityManager().find(EntityLog.class, idEntityLog);
		return log.getLogDetalheList();
	}

}
