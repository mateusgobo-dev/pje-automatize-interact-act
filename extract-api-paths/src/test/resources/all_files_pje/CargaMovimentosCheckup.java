package br.jus.cnj.pje.util.checkup.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.util.checkup.spi.AbstractCheckupWorker;
import br.jus.cnj.pje.util.checkup.spi.CheckupError;
import br.jus.cnj.pje.util.checkup.spi.CheckupErrorImpl;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoComDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoDinamico;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoLivre;

@Name("cargaMovimentosCheckup")
@Scope(ScopeType.CONVERSATION)
public class CargaMovimentosCheckup extends AbstractCheckupWorker {

	private static final long serialVersionUID = 1L;
	
	@In(create = true)
	private LancadorMovimentosService lancadorMovimentosService;
	
	@In(create = true)
	private EventoManager eventoManager;
	
	@Override
	public List<CheckupError> work() {
		return verificaConfiguracaoBanco();
	}
	
	public String getTitle() {
		return "Verificador da carga de movimentos processuais.";
	}

	@Override
	public String getDescription() {
		return "Verifica as tabelas de movimentos e complementos à procura de inconsistências na configuração que possam levar a erros no sistema.";
	}

	private EntityManager em = ComponentUtil.getComponent("entityManager");
	private Boolean configuracaoValidaExclusao = Boolean.TRUE;

	@Logger
	private Log log;

	public Boolean getConfiguracaoValidaExclusao() {
		return configuracaoValidaExclusao;
	}

	/**
	 * Retorna mensagem de erro se há algum erro na configuração do movimento de
	 * exclusao, em caso contrário, retorna null movimento de exclusão estiver
	 * inválida.
	 */
	public CheckupError getMessagemErroConfiguracaoExclusao() {
		Evento movimentoExclusao = eventoManager.recuperarMovimentoExclusao();	
		if (movimentoExclusao == null) {
			configuracaoValidaExclusao = false;
			return new CheckupErrorImpl(this, "Movimento de exclusão de movimentos não foi encontrado.");
		}

		AplicacaoMovimento aplicacaoMovimentoExclusao = lancadorMovimentosService.getAplicacaoMovimentoByEvento(movimentoExclusao);
		if (aplicacaoMovimentoExclusao == null) {
			configuracaoValidaExclusao = false;
			return new CheckupErrorImpl(this, "Não há aplicabilidade cadastrada para o movimento de exclusão de movimentos.");
		}

		boolean encontrouMovimentoExclusao = false;
		boolean encontrouDataHoraExclusao = false;

		for (AplicacaoComplemento aplicacaoComplemento : aplicacaoMovimentoExclusao.getAplicacaoComplementoList()) {
			if (lancadorMovimentosService.isTipoComplementoDataHoraExcluido(aplicacaoComplemento.getTipoComplemento())) {
				if (!encontrouMovimentoExclusao) {
					encontrouMovimentoExclusao = true;
				} else {
					configuracaoValidaExclusao = false;
					return new CheckupErrorImpl(this, "Mais de um complemento 'movimento_excluido' encontrado para o movimento de exclusão de movimentos.");
				}
			} else if (lancadorMovimentosService.isTipoComplementoMovimentoExcluido(aplicacaoComplemento.getTipoComplemento())) {
				if (!encontrouDataHoraExclusao) {
					encontrouDataHoraExclusao = true;
				} else {
					configuracaoValidaExclusao = false;
					return new CheckupErrorImpl(this, "Mais de um complemento 'data_hora_excluido' encontrado para o movimento de exclusão de movimentos.");
				}
			} else {
				configuracaoValidaExclusao = false;
				return new CheckupErrorImpl(this, "Complemento do movimento de exclusão de movimentos não esperado('" + aplicacaoComplemento.getTipoComplemento().getNome() + "').");
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public List<CheckupError> verificaConfiguracaoBanco() {
		List<CheckupError> erros = new ArrayList<CheckupError>();
		try {
			if (isEmpty(ParametroUtil.instance().getTipoJustica())) {
				erros.add(new CheckupErrorImpl(this, "!!ERRO GRAVE!!: Parâmetro 'tipoJustica' não encontrado"));
			}
			if (isEmpty(ParametroUtil.instance().getInstancia())) {
				erros.add(new CheckupErrorImpl(this, "!!ERRO GRAVE!!: Parâmetro 'aplicacaoSistema' não encontrado"));
			}
			CheckupError erroExclusao = getMessagemErroConfiguracaoExclusao();
			if (erroExclusao != null) {
				erros.add(erroExclusao);
			}
			Pattern patternELTextoParametrizado = Pattern.compile("(.*?#\\{(.*?)\\})+?");
			Query q = em.createQuery("from Evento ep where ep.ativo = :true").setParameter("true", Boolean.TRUE);
			List<Evento> eventoProcessualList = null;
			try {
				eventoProcessualList = q.getResultList();
				for (Evento eventoProcessual : eventoProcessualList) {
					boolean isFolha = eventoProcessual.getEventoList().size() == 0;
					if (isFolha) {
						try {
							AplicacaoMovimento aplicacaoMovimento = lancadorMovimentosService.getAplicacaoMovimentoByEvento(eventoProcessual);
							if (aplicacaoMovimento != null && aplicacaoMovimento.getAplicacaoComplementoList().size() == 0) {
								if (aplicacaoMovimento.getTextoParametrizado() != null && aplicacaoMovimento.getTextoParametrizado().contains("#{")) {
									StringBuilder sb = new StringBuilder();
									sb.append("AplicacaoMovimento possui texto parametrizado com EL, porém não possui aplicacaoComplemento. Evento '");
									sb.append(eventoProcessual.getEvento());
									sb.append("' - Código: ");
									sb.append(eventoProcessual.getCodEvento());
									sb.append(".");
									erros.add(new CheckupErrorImpl(this, sb.toString()));
								}
							} else {
								Matcher m = patternELTextoParametrizado.matcher(aplicacaoMovimento.getTextoParametrizado());
								while (m.find()) {
									String nomeDentroDaEL = m.group(2);
									boolean achouComplementoNomeIgual = false;
									for (AplicacaoComplemento aplicacaoComplemento : aplicacaoMovimento
											.getAplicacaoComplementoList()) {
										if (aplicacaoComplemento.getTipoComplemento().getNome().equals(nomeDentroDaEL)) {
											achouComplementoNomeIgual = true;
										}
									}
									if (!achouComplementoNomeIgual) {
										StringBuilder sb = new StringBuilder();
										sb.append("O texto parametrizado contém um nome de complemento('");
										sb.append(nomeDentroDaEL);
										sb.append("') que não possui correspondência aos complementos do evento '");
										sb.append(eventoProcessual.getEvento());
										sb.append("' - Código: ");
										sb.append(eventoProcessual.getCodEvento());
										sb.append(".");
										erros.add(new CheckupErrorImpl(this, sb.toString()));
									}
								}
								for (AplicacaoComplemento aplicacaoComplemento : aplicacaoMovimento.getAplicacaoComplementoList()) {
									if (aplicacaoComplemento.getMultivalorado() == null) {
										StringBuilder sb = new StringBuilder();
										sb.append("É necessário definir se o objeto aplicacaoComplemento é multivalorada para o evento '");
										sb.append(eventoProcessual.getEvento());
										sb.append("' - Código: ");
										sb.append(eventoProcessual.getCodEvento());
										sb.append(".");
										erros.add(new CheckupErrorImpl(this, sb.toString()));
									}
									if (aplicacaoComplemento.getVisibilidadeExterna() == null) {
										StringBuilder sb = new StringBuilder();
										sb.append("É necessário definir se o objeto aplicacaoComplemento possui visibilidadeExterna para o evento '");
										sb.append(eventoProcessual.getEvento());
										sb.append("' - Código: ");
										sb.append(eventoProcessual.getCodEvento());
										sb.append(".");
										erros.add(new CheckupErrorImpl(this, sb.toString()));
									}
									TipoComplemento tp = aplicacaoComplemento.getTipoComplemento();
									tp = HibernateUtil.deproxy(tp, TipoComplemento.class);
									if (isEmpty(tp.getLabel())) {
										StringBuilder sb = new StringBuilder();
										sb.append("É necessário adicionar um label para um complemento do evento '");
										sb.append(eventoProcessual.getEvento());
										sb.append("' - Código: ");
										sb.append(eventoProcessual.getCodEvento());
										sb.append(".");
										erros.add(new CheckupErrorImpl(this, sb.toString()));
									}
									if (isEmpty(tp.getNome())) {
										StringBuilder sb = new StringBuilder();
										sb.append("É necessário adicionar um nome para um complemento do evento '");
										sb.append(eventoProcessual.getEvento());
										sb.append("' - Código: ");
										sb.append(eventoProcessual.getCodEvento());
										sb.append(".");
										erros.add(new CheckupErrorImpl(this, sb.toString()));
									}
									if (tp instanceof TipoComplementoLivre) {
										if (!isEmpty(tp.getValidacao()) && isEmpty(tp.getMensagemErro())) {
											StringBuilder sb = new StringBuilder();
											sb.append("É necessário adicionar uma mensagem de erro para um complemento do evento '");
											sb.append(eventoProcessual.getEvento());
											sb.append("' - Código: ");
											sb.append(eventoProcessual.getCodEvento());
											sb.append(".");
											erros.add(new CheckupErrorImpl(this, sb.toString()));
										}
										if (!isEmpty(tp.getValidacao())) {
											try {
												Pattern.compile(tp.getValidacao());
											} catch (Exception e) {
												StringBuilder sb = new StringBuilder();
												sb.append("Regex de validação inválida para um complemento do evento '");
												sb.append(eventoProcessual.getEvento());
												sb.append("' - Código: ");
												sb.append(eventoProcessual.getCodEvento());
												sb.append(".");
												erros.add(new CheckupErrorImpl(this, sb.toString()));
											}
										}
									} else if (tp instanceof TipoComplementoDinamico) {
										if (isEmpty(((TipoComplementoDinamico) tp).getExpressaoBusca())) {
											StringBuilder sb = new StringBuilder();
											sb.append("É necessário adicionar uma expressão de busca para um complemento do evento '");
											sb.append(eventoProcessual.getEvento());
											sb.append("' - Código: ");
											sb.append(eventoProcessual.getCodEvento());
											sb.append(".");
											erros.add(new CheckupErrorImpl(this, sb.toString()));
										}
									} else if (tp instanceof TipoComplementoComDominio) {
										List<AplicacaoDominio> listaAplicacaoDominio = ((TipoComplementoComDominio) tp).getAplicacaoDominioList();
										if (listaAplicacaoDominio == null || listaAplicacaoDominio.size() == 0) {
											StringBuilder sb = new StringBuilder();
											sb.append("Nenhuma AplicacaoDominio foi declarada para o evento '");
											sb.append(eventoProcessual.getEvento());
											sb.append("' - Código: ");
											sb.append(eventoProcessual.getCodEvento());
											sb.append(".");
											erros.add(new CheckupErrorImpl(this, sb.toString()));
										} else {
											AplicacaoDominio aplicacaoDominio = lancadorMovimentosService.getAplicacaoDominio((TipoComplementoComDominio) tp);
											if (aplicacaoDominio.getDominio().getAtivo()) {
												if (aplicacaoDominio.getDominio().getElementoDominioList().size() == 0) {
													StringBuilder sb = new StringBuilder();
													sb.append("Não há lista de valores de domínio para um complemento do evento '");
													sb.append(eventoProcessual.getEvento());
													sb.append("' - Código: ");
													sb.append(eventoProcessual.getCodEvento());
													sb.append(".");
													erros.add(new CheckupErrorImpl(this, sb.toString()));
												}
											}
											for (ElementoDominio elementoDominio : aplicacaoDominio.getDominio().getElementoDominioList()) {
												if (isEmpty(elementoDominio.getCodigoGlossario())) {
													StringBuilder sb = new StringBuilder();
													sb.append("O atributo codigoGlossario de elementoDominio está vazio para um complemento do evento '");
													sb.append(eventoProcessual.getEvento());
													sb.append("' - Código: ");
													sb.append(eventoProcessual.getCodEvento());
													sb.append(".");
													erros.add(new CheckupErrorImpl(this, sb.toString()));
												}
												if (isEmpty(elementoDominio.getValor())) {
													StringBuilder sb = new StringBuilder();
													sb.append("O atributo valor de elementoDominio está vazio para um complemento do evento '");
													sb.append(eventoProcessual.getEvento());
													sb.append("' - Código: ");
													sb.append(eventoProcessual.getCodEvento());
													sb.append(".");
													erros.add(new CheckupErrorImpl(this, sb.toString()));
												}
											}
										}
									}
								}
							}
						} catch (NonUniqueResultException e) {
							StringBuilder sb = new StringBuilder();
							sb.append("Mais de um AplicacaoMovimento encontrado para o evento '");
							sb.append(eventoProcessual.getEvento());
							sb.append("' - Código: ");
							sb.append(eventoProcessual.getCodEvento());
							sb.append(".");
							erros.add(new CheckupErrorImpl(this, sb.toString()));
						} catch (NoResultException e) {
							StringBuilder sb = new StringBuilder();
							sb.append("Nenhum AplicacaoMovimento encontrado para o evento '");
							sb.append(eventoProcessual.getEvento());
							sb.append("' - Código: ");
							sb.append(eventoProcessual.getCodEvento());
							sb.append(".");
							erros.add(new CheckupErrorImpl(this, sb.toString()));
						}
					}
				}
			} catch (NoResultException e) {
				log.error(e);
			}
		} catch (Exception exception) {
			log.error(exception);
		}
		return erros;
	}

	private boolean isEmpty(String valor) {
		return valor == null || valor.trim().equals("");
	}

	// Mostra os movimentos configurados
	@SuppressWarnings("unchecked")
	public boolean mostraMovimentos() {
		boolean retorno = false;
		List<String> relatorio = new ArrayList<String>();

		Query q = em.createQuery("from Evento ep where ep.ativo = :true").setParameter("true", Boolean.TRUE);
		List<Evento> eventoProcessualList = null;
		try {
			eventoProcessualList = q.getResultList();
			for (Evento ep : eventoProcessualList) {
				String linha = " Movimento : " + ep.getEvento() + " - " + ep.getCodEvento() + " "
						+ (ep.getAtivo() ? "" : "Inativo") + " >> " + ep.getCaminhoCompleto();
				relatorio.add(linha);
				mostraComplementos(ep, relatorio);
			}
			if (relatorio.size() > 0) {
				LogProvider log = Logging.getLogProvider(CargaMovimentosCheckup.class);
				log.info("-------------> Inicio da lista de movimentos atualmente na base");
				for (String linha : relatorio) {
					log.info(linha);
				}
				log.info("-------------> Final da lista dos movimentos atualmente na base");
			}
		} catch (Exception e) {
			throw new AplicationException("Problemas ao mostrar os movimentos atualmente configurados.");
		}
		return retorno;
	}

	//
	// Carrega os complementos de um evento processual na lista log
	@SuppressWarnings("unchecked")
	private void mostraComplementos(Evento ep, List<String> log) {
		Query q = em.createQuery(
				"select am.aplicacaoComplementoList from AplicacaoMovimento am where am.eventoProcessual = :ep ))")
				.setParameter("ep", ep);
		List<AplicacaoComplemento> aplicacaoComplementoList = null;
		try {
			aplicacaoComplementoList = q.getResultList();
			if (aplicacaoComplementoList != null && aplicacaoComplementoList.size() > 0) {
				for (AplicacaoComplemento ac : aplicacaoComplementoList) {
					String linha = " Complemento : " + ac.getTipoComplemento().getNome() + " - "
							+ ac.getTipoComplemento().getCodigo();
					log.add(linha);
				}
			}
		} catch (Exception e) {
			throw new AplicationException("Problemas ao mostrar os complementos do movimento " + ep.getCodEvento());
		}
		return;
	}

}
