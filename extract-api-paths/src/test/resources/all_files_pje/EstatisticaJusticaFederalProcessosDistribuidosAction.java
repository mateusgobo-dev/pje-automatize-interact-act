package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.securitytoken.TokenManager;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoArquivadoMudancaClasseReentr;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoArquivadoSemBaixa;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoBaixaDefinitiva;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoDevolvido;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoDistribuido;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoMudancaClasseBaixa;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoReativado;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoRedistribuidos;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoRemanescente;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoRemetidoTrf;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoSituacao;
import br.com.infox.pattern.strategy.ProcessoDistribuido.ProcessoDistribuidoAnaliticoAssuntoSuspenso;
import br.com.infox.pje.bean.EstatisticaJFProcessosDistribuidosClasseEntidade;
import br.com.infox.pje.bean.EstatisticaJFProcessosDistribuidosClasses;
import br.com.infox.pje.bean.EstatisticaJFProcessosDistribuidosEntidades;
import br.com.infox.pje.bean.EstatisticaJFProcessosDistribuidosVara;
import br.com.infox.pje.bean.EstatisticaJusticaFederalProcessosDistribuidosBean;
import br.com.infox.pje.bean.EstatisticaProcessoDistribuidoAnaliticoAssuntoBean;
import br.com.infox.pje.list.AnaliticoAssuntoArquivadoSemBaixaList;
import br.com.infox.pje.list.AnaliticoAssuntoBaixaDefinitivaList;
import br.com.infox.pje.list.AnaliticoAssuntoDevolvidoTrfList;
import br.com.infox.pje.list.AnaliticoAssuntoDistribuidoList;
import br.com.infox.pje.list.AnaliticoAssuntoMudancaClasseBaixaList;
import br.com.infox.pje.list.AnaliticoAssuntoMudancaClasseReentrList;
import br.com.infox.pje.list.AnaliticoAssuntoReativadoList;
import br.com.infox.pje.list.AnaliticoAssuntoRedistribuidoList;
import br.com.infox.pje.list.AnaliticoAssuntoRemanescenteList;
import br.com.infox.pje.list.AnaliticoAssuntoRemetidoTrfList;
import br.com.infox.pje.list.AnaliticoAssuntoSuspensaoList;
import br.com.infox.pje.list.EstatisticaJFClassesProcessosList;
import br.com.infox.pje.list.EstatisticaJFProcessosArquivadosList;
import br.com.infox.pje.list.EstatisticaJFProcessosBaixadosList;
import br.com.infox.pje.list.EstatisticaJFProcessosDevolvidosList;
import br.com.infox.pje.list.EstatisticaJFProcessosDistribuidosEEList;
import br.com.infox.pje.list.EstatisticaJFProcessosDistribuidosList;
import br.com.infox.pje.list.EstatisticaJFProcessosMudClasBaixList;
import br.com.infox.pje.list.EstatisticaJFProcessosReativadosList;
import br.com.infox.pje.list.EstatisticaJFProcessosRedistribuidosList;
import br.com.infox.pje.list.EstatisticaJFProcessosRemanescentesList;
import br.com.infox.pje.list.EstatisticaJFProcessosRemetidosList;
import br.com.infox.pje.list.EstatisticaJFProcessosSuspensosList;
import br.com.infox.pje.list.EstatisticaJusticaFederalProcessosDistribuidosList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.OpcaoRelatorioEnum;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/JusticaFederal
 * ProcessosDistribuidos/
 * @author Rafael
 *
 */
@Name(value=EstatisticaJusticaFederalProcessosDistribuidosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaJusticaFederalProcessosDistribuidosAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaJusticaFederalProcessosDistribuidosAction";

	private EstatisticaJusticaFederalProcessosDistribuidosList processosDistribuidosList;
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcessoJusticaFederal/ProcessosDistribuidos/processosDistribuidosTemplate.xls";
	private static final String TEMPLATE_XLS_EE = "/EstatisticaProcessoJusticaFederal/ProcessosDistribuidos/processosDistribuidosEETemplate.xls";
	private static final String TEMPLATE_XLS_AC = "/EstatisticaProcessoJusticaFederal/ProcessosDistribuidos/processosDistribuidosACTemplate.xls";
	private static final String TEMPLATE_XLS_AA = "/EstatisticaProcessoJusticaFederal/ProcessosDistribuidos/processosDistribuidosAATemplate.xls";

	private static final String DOWNLOAD_XLS_NAME = "ProcessosDistribuidos.xls";
	
	@In
	private RelatorioLogManager relatorioLogManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private ProcessoTrfManager processoTrfManager;
	
	private SecaoJudiciaria secao;
	private OrgaoJulgador orgaoJulgador;
	private String dataInicioStr;
	private String dataFimStr;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private Cargo cargoJuiz;
	private ClasseJudicial classeJudicial;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
	private Competencia competencia;
	private EstatisticaJusticaFederalProcessosDistribuidosBean distribuidosBean;
	private OpcaoRelatorioEnum opcaoRelatorio = OpcaoRelatorioEnum.SC;
	private List<EstatisticaProcessoDistribuidoAnaliticoAssuntoBean> estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList = new ArrayList<EstatisticaProcessoDistribuidoAnaliticoAssuntoBean>();
	private int totalRem;
	private int totalDis;
	private int totalDev;
	private int totalRea;
	private int totalMCR;
	private int totalMCB;
	private int totalBai;
	private int totalRed;
	private int totalReT;
	private int totalSus;
	private int totalASB;
	private String token;
	private int totalAA;
	
	public static EstatisticaJusticaFederalProcessosDistribuidosAction intance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	private void limpaTotais(){
		totalRem=0;
		totalDis=0;
		totalDev=0;
		totalRea=0;
		totalMCR=0;
		totalMCB=0;
		totalBai=0;
		totalRed=0;
		totalReT=0;
		totalSus=0;
		totalASB=0;
	}
	
	public EstatisticaJusticaFederalProcessosDistribuidosBean estatisticaJFProcessosDistribuidosList() {
		if(opcaoRelatorio == OpcaoRelatorioEnum.SC){
			distribuidosBean = buildProcessosDistriuidosSC();
		}else if(opcaoRelatorio == OpcaoRelatorioEnum.AC){
			distribuidosBean = buildProcessosDistribuidos();
		}else if(opcaoRelatorio == OpcaoRelatorioEnum.EE){
			distribuidosBean = buildProcessosDistriuidosEE();
		}
		
		return distribuidosBean;
	}
	
	public List<EstatisticaProcessoDistribuidoAnaliticoAssuntoBean> getListaProcessosDistriuidosAA() {
			limpaTotais();
			if (getDataInicioStr() != null) {
				setDataInicioFormatada(formatarAnoMes(getDataInicioStr(),true));
				setDataFimFormatada(formatarAnoMes(getDataInicioStr(),false));
				setDataFimStr(dataInicioStr);
				estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList = new ArrayList<EstatisticaProcessoDistribuidoAnaliticoAssuntoBean>();
				
				Set<Map<String, ProcessoTrf>> mapList = new HashSet<Map<String,ProcessoTrf>>();
				mapList.addAll(new AnaliticoAssuntoRemanescenteList().getResultList());
				mapList.addAll(new AnaliticoAssuntoDistribuidoList().getResultList());
				mapList.addAll(new AnaliticoAssuntoDevolvidoTrfList().getResultList());
				mapList.addAll(new AnaliticoAssuntoReativadoList().getResultList());
				mapList.addAll(new AnaliticoAssuntoMudancaClasseReentrList().getResultList());
				mapList.addAll(new AnaliticoAssuntoMudancaClasseBaixaList().getResultList());
				mapList.addAll(new AnaliticoAssuntoBaixaDefinitivaList().getResultList());
				mapList.addAll(new AnaliticoAssuntoRedistribuidoList().getResultList());
				mapList.addAll(new AnaliticoAssuntoRemetidoTrfList().getResultList());
				mapList.addAll(new AnaliticoAssuntoSuspensaoList().getResultList());
				mapList.addAll(new AnaliticoAssuntoArquivadoSemBaixaList().getResultList());
				
				
				for(Map<String, ProcessoTrf> mapProcesso : mapList){
					String chave = mapProcesso.keySet().iterator().next();
					ProcessoTrf ptrf = (ProcessoTrf) mapProcesso.get(chave);
					List<AssuntoTrf> assuntoList = ptrf.getAssuntoTrfList();
					for(AssuntoTrf atrf : assuntoList){
						boolean achou = false;
						int i = 0;
						EstatisticaProcessoDistribuidoAnaliticoAssuntoBean epdaab = new EstatisticaProcessoDistribuidoAnaliticoAssuntoBean();
						while(!achou && i < estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList.size()){
							if(!estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList.isEmpty() && estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList.get(i).getAssuntoTrf().equals(atrf)){
								achou = true;
								epdaab = estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList.get(i);
							}else{
								i++;
							}
						}
						
						carregaListaStrategy(chave, ptrf, epdaab);
						
						if(!achou){
							epdaab.setAssuntoTrf(atrf);
							estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList.add(epdaab);
						}
					}
					
				}
			}
			geraTotaisAnaliticoSintetico();
			
			return estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList;
	}
	
	private void carregaListaStrategy(String chave, ProcessoTrf ptrf,
			EstatisticaProcessoDistribuidoAnaliticoAssuntoBean epdaab) {
		if(chave.equals("delvovidos")){
			new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoDevolvido(epdaab)).eventoProcessoDistribuido(ptrf);
		}else{
			if(chave.equals("reativado")){
				new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoReativado(epdaab)).eventoProcessoDistribuido(ptrf);
			}else{
				if(chave.equals("distribuido")){
					new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoDistribuido(epdaab)).eventoProcessoDistribuido(ptrf);
				}else{
					if(chave.equals("remanescente")){
						new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoRemanescente(epdaab)).eventoProcessoDistribuido(ptrf);
					}else{
						if(chave.equals("mudancaClasseReentr")){
							new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoArquivadoMudancaClasseReentr(epdaab)).eventoProcessoDistribuido(ptrf);
						}else{
							if(chave.equals("mudancaClasseBaixa")){
								new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoMudancaClasseBaixa(epdaab)).eventoProcessoDistribuido(ptrf);
							}else{
								if(chave.equals("baixaDefinitiva")){
									new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoBaixaDefinitiva(epdaab)).eventoProcessoDistribuido(ptrf);
								}else{
									if(chave.equals("redistribuido")){
										new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoRedistribuidos(epdaab)).eventoProcessoDistribuido(ptrf);
									}else{
										if(chave.equals("remetidoTrf")){
											new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoRemetidoTrf(epdaab)).eventoProcessoDistribuido(ptrf);
										}else{
											if(chave.equals("suspenso")){
												new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoSuspenso(epdaab)).eventoProcessoDistribuido(ptrf);
											}else{
												if(chave.equals("arquivadoSemBaixa")){
													new ProcessoDistribuidoAnaliticoAssuntoSituacao(new ProcessoDistribuidoAnaliticoAssuntoArquivadoSemBaixa(epdaab)).eventoProcessoDistribuido(ptrf);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void geraTotaisAnaliticoSintetico() {
		for(EstatisticaProcessoDistribuidoAnaliticoAssuntoBean o : estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList){
			totalRem += o.getListaRemanescentes().size();
			totalDis += o.getListaDistribuidos().size();
			totalDev += o.getListaDevolvidos().size();
			totalRea += o.getListaReativados().size();
			totalMCR += o.getListaClasseReentr().size();
			totalMCB += o.getListaClassesBaixa().size();
			totalBai += o.getListaBaixasDefinitiva().size();
			totalRed += o.getListaRedistribuidos().size();
			totalReT += o.getListaRemetidos().size();
			totalSus += o.getListaSuspensos().size();
			totalASB += o.getListaArquivadosSemBaixa().size();
		}
		setTotalAA((totalRem + totalDis + totalDev + totalRea + totalMCR)
				 - (totalMCB + totalBai + totalRed + totalReT + totalSus + totalASB));
	}
	
	private EstatisticaJusticaFederalProcessosDistribuidosBean buildProcessosDistriuidosSC() {
		if (getDataInicioFormatada() != null && getDataFimFormatada() != null) {
			setDataInicioFormatada(formatarAnoMes(getDataInicioStr(),true));
			setDataFimFormatada(formatarAnoMes(getDataFimStr(),false));
			processosDistribuidosList = new EstatisticaJusticaFederalProcessosDistribuidosList();
			List<Map<String, Object>> mapList = getProcessosDistribuidosList().getResultList();
			EstatisticaJusticaFederalProcessosDistribuidosBean bean = new EstatisticaJusticaFederalProcessosDistribuidosBean();
			EstatisticaJFProcessosDistribuidosVara vara = new EstatisticaJFProcessosDistribuidosVara();
			List<EstatisticaJFProcessosDistribuidosClasses> listClasses = new ArrayList<EstatisticaJFProcessosDistribuidosClasses>();
			bean.setSecao(secao.getSecaoJudiciaria());
			vara.setVara(orgaoJulgador.getOrgaoJulgador());
			for (Map<String, Object> map: mapList) {
				EstatisticaJFProcessosDistribuidosClasses classe = new EstatisticaJFProcessosDistribuidosClasses();
				
				classe.setClasse((ClasseJudicial) map.get("classe"));
				classe.setTotalEventoRem(Integer.valueOf(map.get("numProcessRem").toString()));
				classe.setTotalEventoDistr(Integer.valueOf(map.get("numProcessDistr").toString()));
				classe.setTotalEventoDevolv(Integer.valueOf(map.get("numProcessDevolv").toString()));
				classe.setTotalEventoReativ(Integer.valueOf(map.get("numProcessReativ").toString()));
				classe.setTotalEventoMudClassRee(Integer.valueOf(map.get("numProcessMudClassRee").toString()));
				classe.setTotalEventoMudClassBaixa(Integer.valueOf(map.get("numProcessMudClassBaixa").toString()));
				classe.setTotalEventoBaixad(Integer.valueOf(map.get("numProcessBaixad").toString()));
				classe.setTotalEventoRedistrib(Integer.valueOf(map.get("numProcessRedistr").toString()));
				classe.setTotalEventoRemet(Integer.valueOf(map.get("numProcessRemet").toString()));
				classe.setTotalEventoSusp(Integer.valueOf(map.get("numProcessSusp").toString()));
				classe.setTotalEventoArq(Integer.valueOf(map.get("numProcessArq").toString()));
				
				classe.setTotalClasse((classe.getTotalEventoRem()+classe.getTotalEventoDistr()+classe.getTotalEventoDevolv()+
				classe.getTotalEventoReativ()+classe.getTotalEventoMudClassRee())-(classe.getTotalEventoMudClassBaixa()+
				classe.getTotalEventoBaixad()+classe.getTotalEventoRedistrib()+classe.getTotalEventoRemet()));
				classe.setTotalClasseAjustado(classe.getTotalClasse() - (classe.getTotalEventoSusp() + classe.getTotalEventoArq()));
				listClasses.add(classe);	
				bean.setTotalGeral(bean.getTotalGeral()+classe.getTotalClasse());
				
				vara.setTotalRemGeral(vara.getTotalRemGeral()+classe.getTotalEventoRem());
				vara.setTotalArqGeral(vara.getTotalArqGeral()+classe.getTotalEventoArq());
				vara.setTotalBaixadGeral(vara.getTotalBaixadGeral()+classe.getTotalEventoBaixad());
				vara.setTotalDevolvGeral(vara.getTotalDevolvGeral()+classe.getTotalEventoDevolv());
				vara.setTotalDistrGeral(vara.getTotalDistrGeral()+classe.getTotalEventoDistr());
				vara.setTotalMudBaixaGeral(vara.getTotalMudBaixaGeral()+classe.getTotalEventoMudClassBaixa());
				vara.setTotalMudReeGeral(vara.getTotalMudReeGeral()+classe.getTotalEventoMudClassRee());
				vara.setTotalReativGeral(vara.getTotalReativGeral()+classe.getTotalEventoReativ());
				vara.setTotalRedistrGeral(vara.getTotalRedistrGeral()+classe.getTotalEventoRedistrib());
				vara.setTotalRemetGeral(vara.getTotalRemetGeral()+classe.getTotalEventoRemet());
				vara.setTotalSuspGeral(vara.getTotalSuspGeral()+classe.getTotalEventoSusp());
				vara.setTotalAjustGeral(vara.getTotalAjustGeral()+classe.getTotalClasseAjustado());
			}
			vara.setSubListClasse(listClasses);
			
			bean.setVara(vara);
			return bean;
		}
		return null;
	}
	
	private EstatisticaJusticaFederalProcessosDistribuidosBean buildProcessosDistriuidosEE() {
		setDataInicioFormatada(formatarAnoMes(getDataInicioStr(),true));
		setDataFimFormatada(formatarAnoMes(getDataFimStr(),false));
		processosDistribuidosList = new EstatisticaJusticaFederalProcessosDistribuidosList();
		EstatisticaJFProcessosDistribuidosEEList processosDistribuidosEEList = new EstatisticaJFProcessosDistribuidosEEList();
		List<Map<String, Object>> mapList = processosDistribuidosEEList.getResultList();
		EstatisticaJusticaFederalProcessosDistribuidosBean bean = new EstatisticaJusticaFederalProcessosDistribuidosBean();
		EstatisticaJFProcessosDistribuidosVara vara = new EstatisticaJFProcessosDistribuidosVara();
		List<EstatisticaJFProcessosDistribuidosEntidades> listEntidades = new ArrayList<EstatisticaJFProcessosDistribuidosEntidades>();
		bean.setSecao(secao.getSecaoJudiciaria());
		vara.setVara(orgaoJulgador.getOrgaoJulgador());
		EstatisticaJFProcessosDistribuidosEntidades entidade = new EstatisticaJFProcessosDistribuidosEntidades();
		for (Map<String, Object> map: mapList) {
			if(!map.get("entidade").toString().equals(entidade.getEntidade())){
				entidade = new EstatisticaJFProcessosDistribuidosEntidades();
				entidade.setEntidade(map.get("entidade").toString());
				entidade.setTotalEventoRem(Integer.valueOf(map.get("numProcessRem").toString()));
				entidade.setTotalEventoDistr(Integer.valueOf(map.get("numProcessDistr").toString()));
				entidade.setTotalEventoDevolv(Integer.valueOf(map.get("numProcessDevolv").toString()));
				entidade.setTotalEventoReativ(Integer.valueOf(map.get("numProcessReativ").toString()));
				entidade.setTotalEventoBaixad(Integer.valueOf(map.get("numProcessBaixad").toString()));
				entidade.setTotalEventoRedistrib(Integer.valueOf(map.get("numProcessRedistr").toString()));
				entidade.setTotalEventoRemet(Integer.valueOf(map.get("numProcessRemet").toString()));
				
				entidade.setTotalEntidade((entidade.getTotalEventoRem()+entidade.getTotalEventoDistr()+entidade.getTotalEventoDevolv()+
				entidade.getTotalEventoReativ())-(entidade.getTotalEventoBaixad()+entidade.getTotalEventoRedistrib()+entidade.getTotalEventoRemet()));
				listEntidades.add(entidade);	
				bean.setTotalGeral(bean.getTotalGeral()+entidade.getTotalEntidade());
				
				vara.setTotalRemGeral(vara.getTotalRemGeral()+entidade.getTotalEventoRem());
				vara.setTotalBaixadGeral(vara.getTotalBaixadGeral()+entidade.getTotalEventoBaixad());
				vara.setTotalDevolvGeral(vara.getTotalDevolvGeral()+entidade.getTotalEventoDevolv());
				vara.setTotalDistrGeral(vara.getTotalDistrGeral()+entidade.getTotalEventoDistr());
				vara.setTotalReativGeral(vara.getTotalReativGeral()+entidade.getTotalEventoReativ());
				vara.setTotalRedistrGeral(vara.getTotalRedistrGeral()+entidade.getTotalEventoRedistrib());
				vara.setTotalRemetGeral(vara.getTotalRemetGeral()+entidade.getTotalEventoRemet());
			}
		}
		vara.setSubListEntidade(listEntidades);
		
		bean.setVara(vara);
		return bean;
	}
	
	/**
	 * Retorna todas as varas de uma determinada seção, caso o usauário tenha OJ retorna o dele.
	 * @return
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorItems() {
		List<OrgaoJulgador> items = new ArrayList<OrgaoJulgador>();
		if (ParametroUtil.instance().isPrimeiroGrau() && (Authenticator.getOrgaoJulgadorAtual() != null)) {
			items.add(Authenticator.getOrgaoJulgadorAtual());
			return items;
		}else{
			for(OrgaoJulgador s : estatisticaProcessoJusticaFederalManager.buscaListaOrgaoJulgador(getSecao().getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}
	
	/**
	 * Retorna todas as competencias de um determinado orgao julgador.
	 * @return
	 */
	public List<Competencia> listCompetenciaItems(){
		List<Competencia> items = new ArrayList<Competencia>();
		for(Competencia s : estatisticaProcessoJusticaFederalManager.buscaListaCompetenciaOrgaoJulgador(getOrgaoJulgador())) {
			if (s != null) {
				items.add(s);
			}
		}
		return items;
	}
	
	public List<ClasseJudicial> getClasseJudicialList() {
		classeJudicialList.clear();
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		for (ClasseJudicial classe : tree.getSelectedTree()) {
			classeJudicialList.add(classe);
		}
		if(classeJudicialList.isEmpty() && classeJudicial != null){
			   classeJudicialList.add(classeJudicial);
		}  
		return classeJudicialList;
	}
	
	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * @param registros quantidade de registros da lista
	 */
	public void gravarLogRelatorio(){
		distribuidosBean = null;
        if(estatisticaJFProcessosDistribuidosList() != null || getListaProcessosDistriuidosAA() != null){ 
        	relatorioLogManager.persist("Estatística de Processos Distribuídos", Authenticator.getUsuarioLogado());
        }	
	}
	
	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta 
	 * retorne registros
	 * @param registros total de registros da consulta
	 */
	public void exportarProcessosDistribuidosXLS(){
		String template = null;
		if(opcaoRelatorio == OpcaoRelatorioEnum.SC){
			template = TEMPLATE_XLS_PATH;
		}else if(opcaoRelatorio == OpcaoRelatorioEnum.EE){
			template = TEMPLATE_XLS_EE;
		}else if(opcaoRelatorio == OpcaoRelatorioEnum.AC){
			template = TEMPLATE_XLS_AC;
		}else if(opcaoRelatorio == OpcaoRelatorioEnum.AA){
			template = TEMPLATE_XLS_AA;
		}
		try {
			if(getDistribuidosBean() != null || estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList != null){
			  exportarXLS(template);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (ExcelExportException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public void exportarXLS(String template) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + template;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), DOWNLOAD_XLS_NAME);
	}
	
	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("titulo", Messages.instance().get("estatisticaJusticaFederalProcessosDistribuidos.relatorio"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("sessaoJudiciaria", getSecao());
		if(opcaoRelatorio != OpcaoRelatorioEnum.AA){
			List<EstatisticaJusticaFederalProcessosDistribuidosBean> list = new ArrayList<EstatisticaJusticaFederalProcessosDistribuidosBean>();
			list.add(getDistribuidosBean());
			map.put("processosDistribuidosBean", list);
		}else{
			map.put("estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList", estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList);
			map.put("totalASB", totalASB);
			map.put("totalBai", totalBai);
			map.put("totalDev", totalDev);
			map.put("totalDis", totalDis);
			map.put("totalMCB", totalMCB);
			map.put("totalMCR", totalMCR);
			map.put("totalRea", totalRea);
			map.put("totalRed", totalRed);
			map.put("totalRem", totalRem);
			map.put("totalReT", totalReT);
			map.put("totalSus", totalSus);
			map.put("totalGeral", totalASB + totalBai + totalDev + totalDis + totalMCB + totalMCR + totalRea + totalRed + totalRem + totalReT + totalSus);
		}
		map.put("dataInicio", getDataInicioStr());
		map.put("dataFim", getDataFimStr());
		map.put("diretorVara", getDiretorVara() != null ? getDiretorVara() : "" );
		map.put("juizFederal", getJuizFederal() != null ? getJuizFederal() : "");
		map.put("cargoJuiz", getCargoJuiz() != null ? getCargoJuiz() : "");
		map.put("classe", getClasseJudicial() != null ? getClasseJudicial() : "");
		map.put("natureza", getCompetencia() != null ? getCompetencia() : "");
		map.put("vara", getOrgaoJulgador() != null ? getOrgaoJulgador() : "");
		map.put("opcao", getOpcaoRelatorio().getLabel());
		return map;
	}	
	
	public void limparFiltros(){
		dataFimStr = null;
		dataInicioStr = null;
		orgaoJulgador = null;
		cargoJuiz = null;
		classeJudicial = null;
		classeJudicialList = new ArrayList<ClasseJudicial>();
		competencia = null;
		opcaoRelatorio = OpcaoRelatorioEnum.SC;
	}
	
	public int getRowspan(){
		int rowspan= 0;
		if(distribuidosBean != null){
			if(opcaoRelatorio == OpcaoRelatorioEnum.SC){
				rowspan = distribuidosBean.getVara().getSubListClasse().size();
			}else if(opcaoRelatorio == OpcaoRelatorioEnum.EE){
				rowspan = distribuidosBean.getVara().getSubListEntidade().size();
			}else if(opcaoRelatorio == OpcaoRelatorioEnum.AC){
				rowspan = (distribuidosBean.getVara().getSubList().size() * 12) + 3;
				return rowspan;
			}
		}
		return rowspan+2;
	}
	
	/**
	 * Traz o nome do Diretor da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */
	public String getDiretorVara(){
		Usuario diretorVara = processoTrfManager.getDiretorVara(orgaoJulgador);
		return diretorVara != null ? diretorVara.getNome() : "";
	}

	/**
	 * Traz o nome do Juiz Federal da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */	
	public String getJuizFederal(){
		Usuario juizFederal = processoTrfManager.getJuizFederal(orgaoJulgador);
		return juizFederal != null ? juizFederal.getNome() : "";
	}
	
	private EstatisticaJusticaFederalProcessosDistribuidosBean buildProcessosDistribuidos() {
			setDataInicioFormatada(formatarAnoMes(getDataInicioStr(),true));
			setDataFimFormatada(formatarAnoMes(getDataInicioStr(),false));
			setDataFimStr(dataInicioStr);
			EstatisticaJusticaFederalProcessosDistribuidosBean bean = new EstatisticaJusticaFederalProcessosDistribuidosBean();
			EstatisticaJFProcessosDistribuidosVara vara = new EstatisticaJFProcessosDistribuidosVara();
			Map<ClasseJudicial, EstatisticaJFProcessosDistribuidosClasseEntidade> subList = new HashMap<ClasseJudicial, 
					EstatisticaJFProcessosDistribuidosClasseEntidade>();
			bean.setSecao(secao.getSecaoJudiciaria());
			vara.setVara(getOrgaoJulgador().getOrgaoJulgador());
			
			//setando classes
			EstatisticaJFClassesProcessosList classesList = new EstatisticaJFClassesProcessosList();
			List<Map<String, Object>> c = classesList.getResultList();
			for (Map<String, Object> entry : c) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				if (!subList.containsKey(classe)) {
					EstatisticaJFProcessosDistribuidosClasseEntidade entidade = new EstatisticaJFProcessosDistribuidosClasseEntidade();
					entidade.setClasse(classe);
					subList.put(classe, entidade);
				}
			}
			
			//setando remanescente
			EstatisticaJFProcessosRemanescentesList processosRemanescentesList = new EstatisticaJFProcessosRemanescentesList();
			List<Map<String, Object>> banco = processosRemanescentesList.getResultList();
			for (Map<String, Object> entry : banco) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessRem().add(processo);
					vara.setTotalRemGeral(vara.getTotalRemGeral() + 1);
				}
			}
			
			//setando distribuidos
			EstatisticaJFProcessosDistribuidosList processosDistribuidosList = new EstatisticaJFProcessosDistribuidosList();
			List<Map<String, Object>> bancod = processosDistribuidosList.getResultList();
			for (Map<String, Object> entry : bancod) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessDistr().add(processo);
					vara.setTotalDistrGeral(vara.getTotalDistrGeral() + 1);
				}
			}
			
			  
			//setando devolvidos
			EstatisticaJFProcessosDevolvidosList processosDevolvidosList = new EstatisticaJFProcessosDevolvidosList();
			List<Map<String, Object>> bancode = processosDevolvidosList.getResultList();
			for (Map<String, Object> entry : bancode) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessDevolv().add(processo);
					vara.setTotalDevolvGeral(vara.getTotalDevolvGeral() + 1);
				}
			}
			
			//setando reativados
			EstatisticaJFProcessosReativadosList processosReativadosList = new EstatisticaJFProcessosReativadosList();
			List<Map<String, Object>> bancor = processosReativadosList.getResultList();
			for (Map<String, Object> entry : bancor) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessReativ().add(processo);
					vara.setTotalReativGeral(vara.getTotalReativGeral() + 1);
				}
			}
			
			//setando mud. classe baix.
			EstatisticaJFProcessosMudClasBaixList processosMudClasBaixList = new EstatisticaJFProcessosMudClasBaixList();
			List<Map<String, Object>> bancomb = processosMudClasBaixList.getResultList();
			for (Map<String, Object> entry : bancomb) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessMudClassBaixa().add(processo);
					vara.setTotalMudBaixaGeral(vara.getTotalMudBaixaGeral() + 1);
				}
			}
			
			//setando mud. classe ree.
			EstatisticaJFProcessosMudClasBaixList processosMudClasReeList = new EstatisticaJFProcessosMudClasBaixList();
			List<Map<String, Object>> bancomr = processosMudClasReeList.getResultList();
			for (Map<String, Object> entry : bancomr) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessMudClassRee().add(processo);
					vara.setTotalRemGeral(vara.getTotalRemGeral() + 1);
				}
			}
			
			//setando baixados
			EstatisticaJFProcessosBaixadosList processosBaixadosList = new EstatisticaJFProcessosBaixadosList();
			List<Map<String, Object>> bancob = processosBaixadosList.getResultList();
			for (Map<String, Object> entry : bancob) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessBaixados().add(processo);
					vara.setTotalBaixadGeral(vara.getTotalBaixadGeral() + 1);
				}
			}
			
			//setando redistribuidos
			EstatisticaJFProcessosRedistribuidosList processosRedistribuidosList = new EstatisticaJFProcessosRedistribuidosList();
			List<Map<String, Object>> bancord = processosRedistribuidosList.getResultList();
			for (Map<String, Object> entry : bancord) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessRedistr().add(processo);
					vara.setTotalRedistrGeral(vara.getTotalRedistrGeral() + 1);
				}
			}
			
			//setando remetidos
			EstatisticaJFProcessosRemetidosList processosRemetidosList = new EstatisticaJFProcessosRemetidosList();
			List<Map<String, Object>> bancorm = processosRemetidosList.getResultList();
			for (Map<String, Object> entry : bancorm) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessRemetidos().add(processo);
					vara.setTotalRemetGeral(vara.getTotalRemetGeral() + 1);
				}
			}
			
			//setando suspensos
			EstatisticaJFProcessosSuspensosList processosSuspensosList = new EstatisticaJFProcessosSuspensosList();
			List<Map<String, Object>> bancos = processosSuspensosList.getResultList();
			for (Map<String, Object> entry : bancos) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessSusp().add(processo);
					vara.setTotalSuspGeral(vara.getTotalSuspGeral() + 1);
				}
			}
			
			//setando arquivados
			EstatisticaJFProcessosArquivadosList processosArquivadosList = new EstatisticaJFProcessosArquivadosList();
			List<Map<String, Object>> bancoa = processosArquivadosList.getResultList();
			for (Map<String, Object> entry : bancoa) {
				ClasseJudicial classe = (ClasseJudicial) entry.get("classe");
				ProcessoTrf processo = (ProcessoTrf) entry.get("processo");
				
				EstatisticaJFProcessosDistribuidosClasseEntidade estatisticaJFProcessosDistribuidosClasseEntidade = subList.get(classe);
				if (estatisticaJFProcessosDistribuidosClasseEntidade != null) {
					estatisticaJFProcessosDistribuidosClasseEntidade.getListProcessArquivados().add(processo);
					vara.setTotalArqGeral(vara.getTotalArqGeral() + 1);
				}
			}
			// ---
			List<EstatisticaJFProcessosDistribuidosClasseEntidade> list = new ArrayList<EstatisticaJFProcessosDistribuidosClasseEntidade>(subList.values());
			vara.setSubList(list);	
			for(EstatisticaJFProcessosDistribuidosClasseEntidade e : list){
				bean.setTotalGeral(bean.getTotalGeral()+e.getQtdTotal());
			}
			bean.setVara(vara);
			return bean;
	}
	
	public void createToken(){
		try {
			if(secao.getUrlAplicacao() != null && !secao.getUrlAplicacao().isEmpty()){
				token = TokenManager.instance().getRemoteToken(secao.getUrlAplicacao()) ;
			}else{
				FacesMessages.instance().add(Severity.ERROR, MessageFormat.format(
						"URL da aplicação não está definida para a seção escolhida: {0}",
						secao.getSecaoJudiciaria()));
			}
		} catch (IOException e) {
			String msgErro = MessageFormat.format(
					"URL do Webservice não esta acessivel no estado {0}: {1}",
					secao.getSecaoJudiciaria(), e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void validarToken(){
		TokenManager.instance().validateToken(token);
	}
	
	
	/**
	 * Método que recebe a data e retorna a data de início: "yyyy-MM-01" e
	 * a data final: "yyyy-MM-dd (último dia do mês)
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(String data, boolean inicio) {
		String mes = data.substring(0,2);
		String ano = data.substring(3);
		Calendar dataTemp = Calendar.getInstance();
		if (!inicio) {
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,1);
			Integer dia = dataTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,dia);
		}else{
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,1);
		}
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(dataTemp.getTime());
	}
	
	
	/*
	 * Inicio - Getters and Setters 
	 */
	
	public SecaoJudiciaria getSecao() {
		if(ParametroUtil.instance().isPrimeiroGrau()){
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secao = (SecaoJudiciaria) si.getSingleResult();
		}
		return secao;
	}

	public void setSecao(SecaoJudiciaria secao) {
		this.secao = secao;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if (ParametroUtil.instance().isPrimeiroGrau() && (Authenticator.getOrgaoJulgadorAtual() != null)) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	public Cargo getCargoJuiz() {
		return cargoJuiz;
	}

	public void setCargoJuiz(Cargo cargoJuiz) {
		this.cargoJuiz = cargoJuiz;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	
	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}


	public EstatisticaJusticaFederalProcessosDistribuidosList getProcessosDistribuidosList() {
		return processosDistribuidosList;
	}
	
	public void setProcessosDistribuidosList(
			EstatisticaJusticaFederalProcessosDistribuidosList processosDistribuidosList) {
		this.processosDistribuidosList = processosDistribuidosList;
	}
	
	public EstatisticaJusticaFederalProcessosDistribuidosBean getDistribuidosBean() {
		return distribuidosBean;
	}
	
	public void setDistribuidosBeanList(EstatisticaJusticaFederalProcessosDistribuidosBean distribuidosBean) {
		this.distribuidosBean = distribuidosBean;
	}
	
	
	public String getDataInicioStr() {
		return dataInicioStr;
	}

	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public String getDataFimStr() {
		return dataFimStr;
	}

	public void setDataFimStr(String dataFimStr) {
		this.dataFimStr = dataFimStr;
	}
	
	public OpcaoRelatorioEnum getOpcaoRelatorio() {
		return opcaoRelatorio;
	}

	public void setOpcaoRelatorio(OpcaoRelatorioEnum opcaoRelatorio) {
		this.opcaoRelatorio = opcaoRelatorio;
	}
	
	public OpcaoRelatorioEnum[] getOpcaoEnumValues(){
		return OpcaoRelatorioEnum.values();
	}
	
	public List<EstatisticaProcessoDistribuidoAnaliticoAssuntoBean> getEstatisticaProcessoDistribuidoAnalliticoAssuntoBeanList(){
		return estatisticaProcessoDistribuidoAnalliticoAssuntoBeanList;
	}

	public void setTotalRem(int totalRem) {
		this.totalRem = totalRem;
	}

	public int getTotalRem() {
		return totalRem;
	}

	public void setTotalDis(int totalDis) {
		this.totalDis = totalDis;
	}

	public int getTotalDis() {
		return totalDis;
	}

	public void setTotalDev(int totalDev) {
		this.totalDev = totalDev;
	}

	public int getTotalDev() {
		return totalDev;
	}

	public void setTotalRea(int totalRea) {
		this.totalRea = totalRea;
	}

	public int getTotalRea() {
		return totalRea;
	}

	public void setTotalMCR(int totalMCR) {
		this.totalMCR = totalMCR;
	}

	public int getTotalMCR() {
		return totalMCR;
	}

	public void setTotalMCB(int totalMCB) {
		this.totalMCB = totalMCB;
	}

	public int getTotalMCB() {
		return totalMCB;
	}

	public void setTotalBai(int totalBai) {
		this.totalBai = totalBai;
	}

	public int getTotalBai() {
		return totalBai;
	}

	public void setTotalRed(int totalRed) {
		this.totalRed = totalRed;
	}

	public int getTotalRed() {
		return totalRed;
	}

	public void setTotalReT(int totalReT) {
		this.totalReT = totalReT;
	}

	public int getTotalReT() {
		return totalReT;
	}

	public void setTotalSus(int totalSus) {
		this.totalSus = totalSus;
	}

	public int getTotalSus() {
		return totalSus;
	}

	public void setTotalASB(int totalASB) {
		this.totalASB = totalASB;
	}

	public int getTotalASB() {
		return totalASB;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public String getDataInicioFormatada() {
		if (dataInicioStr != null) {
			dataInicioFormatada = formatarAnoMes(dataInicioStr, true);
		}
		return dataInicioFormatada;
	}

	public void setDataInicioFormatada(String dataInicioFormatada) {
		this.dataInicioFormatada = dataInicioFormatada;
	}

	public String getDataFimFormatada() {
		if (dataFimStr != null) {
			dataFimFormatada = formatarAnoMes(dataFimStr, false);
		}
		return dataFimFormatada;
	}

	public void setDataFimFormatada(String dataFimFormatada) {
		this.dataFimFormatada = dataFimFormatada;
	}

	public void setTotalAA(int totalAA) {
		this.totalAA = totalAA;
	}

	public int getTotalAA() {
		return totalAA;
	}
	
}