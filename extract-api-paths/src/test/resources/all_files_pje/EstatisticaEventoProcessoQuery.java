package br.com.infox.pje.query;

public interface EstatisticaEventoProcessoQuery {

	static final String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	static final String QUERY_PARAMETER_COMPETENCIA = "competencia";
	static final String QUERY_PARAMETER_DATA_INCIO = "dataInicio";
	static final String QUERY_PARAMETER_DATA_FIM = "dataFim";
	static final String QUERY_PARAMETER_COD_ESTADO = "codEstado";
	static final String QUERY_PARAMETER_DT_TRAMITACAO_STR = "dataTramitacaoStr";
	static final String QUERY_PARAMETER_DATA_REMANESCENTE_STR = "dataRemanescenteStr";
	static final String QUERY_PARAMETER_EVENTO1 = "evento1";
	static final String QUERY_PARAMETER_EVENTO2 = "evento2";
	static final String QUERY_PARAMETER_EVENTO3 = "evento3";
	static final String QUERY_PARAMETER_EVENTO_DISTRIBUICAO = "evento4";
	static final String QUERY_PARAMETER_EVENTO_JULGAMENTO = "evento5";
	static final String QUERY_PARAMETER_EVENTO_JULGADO = "evento6";
	static final String QUERY_PARAMETER_EVENTO_ARQUIVAMENTO = "evento6";

	static final String LIST_COMPETENCIA_EST_BY_ORGAO_JULGADOR_QUERY = "select ep.competencia from EstatisticaEventoProcesso ep "
			+ "where ep.competencia != null and ep.orgaoJulgador like :" + QUERY_PARAMETER_ORGAO_JULGADOR
			+ " group by ep.competencia order by ep.competencia";

	static final String LIST_VARAS_SECAO_PROCESSOS_QUERY = "select eep.orgaoJulgador, count(*) from EstatisticaEventoProcesso eep"
			+ " where eep.codEstado = :" + QUERY_PARAMETER_COD_ESTADO + " and to_char(eep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM + " and to_char(eep.dataInclusao,'yyyy-MM') >= :" + QUERY_PARAMETER_DATA_INCIO
			+ " and (eep.codEvento = :" + QUERY_PARAMETER_EVENTO1 + "		 or eep.codEvento = :" + QUERY_PARAMETER_EVENTO2
			+ "	  	 or eep.codEvento = :" + QUERY_PARAMETER_EVENTO3 + ")" + " group by eep.orgaoJulgador"
			+ " order by count(*)";

	static final String LIST_VARAS_SECAO_PROCESSOS_DISTRIBUIDOS_QUERY = "select eep.orgaoJulgador from EstatisticaEventoProcesso eep"
			+ " where eep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and to_char (eep.dataInclusao,'yyyy-MM-dd') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and to_char (eep.dataInclusao,'yyyy-MM-dd') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and eep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " group by eep.orgaoJulgador"
			+ " order by eep.orgaoJulgador";

	static final String LIST_PROC_VARAS_SECAO_PROCESSOS_DISTRIBUIDOS_QUERY = "select distinct (eep.numeroProcesso), eep.classeJudicial, eep.dataInclusao, eep.orgaoJulgador from EstatisticaEventoProcesso eep"
			+ " where eep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and to_char (eep.dataInclusao,'yyyy-MM-dd') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and to_char (eep.dataInclusao,'yyyy-MM-dd') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and eep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " and eep.orgaoJulgador =:"
			+ QUERY_PARAMETER_ORGAO_JULGADOR + " order by eep.orgaoJulgador";

	static final String LIST_GROUP_BY_ESTADO_QUERY = "select o.codEstado from EstatisticaEventoProcesso o group by o.codEstado "
			+ "order by o.codEstado";

	static final String SECAO_JUDICIARIA_QUERY = "select o.cdSecaoJudiciaria, o.secaoJudiciaria from SecaoJudiciaria o order by o.cdSecaoJudiciaria";

	static final String LIST_GROUP_BY_COMPETENCIA = "listGroupByCompetencia";
	static final String LIST_GROUP_BY_COMPETENCIA_QUERY = "select o.competencia from EstatisticaEventoProcesso o group by o.competencia "
			+ "order by o.competencia";

	static final String LIST_PROCESSOS_REMA_DIS_JUL_ARQ_TRAMITACAO_QUERY = "select distinct ep.codEstado, ep.orgaoJulgador, ep.jurisdicao, "
			+

			// distribuidos
			"	 (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "	  where iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_DISTRIBUICAO
			+ " and "
			+ "	 	    iep.codEstado = ep.codEstado and "
			+ "           iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "	 	    iep.jurisdicao = ep.jurisdicao and "
			+ "	 	    to_char(iep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "	  		to_char(iep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "), "
			+

			// julgados
			"	 (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "	  where iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_JULGAMENTO
			+ " and "
			+ "	 	    iep.codEstado = ep.codEstado and "
			+ "	 	    iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "	 	    iep.jurisdicao = ep.jurisdicao and "
			+ "	 	    to_char(iep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "	  		to_char(iep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "), "
			+

			// arquivados
			"	  (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "		  where (iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_ARQUIVAMENTO
			+ " or "
			+ "		  		 iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " or "
			+ "		  		 iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO3
			+ ") and "
			+ "		 	     iep.codEstado = ep.codEstado and "
			+ "		 	     iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "		 	     iep.jurisdicao = ep.jurisdicao and "
			+ "	 	    	 to_char(iep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "	  			 to_char(iep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "), "
			+

			// tramitacao
			"	  (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "		where iep.codEvento !=  :"
			+ QUERY_PARAMETER_EVENTO1
			+ " and "
			+ "             iep.codEvento !=  :"
			+ QUERY_PARAMETER_EVENTO2
			+ " and "
			+ "    	  	  iep.codEstado = ep.codEstado and "
			+ " 	          iep.orgaoJulgador = ep.orgaoJulgador and "
			+ " 	          iep.jurisdicao = ep.jurisdicao and "
			+ "			  to_char(iep.dataInclusao, 'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "             iep.idEstatisticaProcesso in "
			+ "             (select max(e2.idEstatisticaProcesso) from EstatisticaEventoProcesso e2 "
			+ "                 where iep.codEstado = ep.codEstado and iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "                 to_char(e2.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "                 e2.numeroProcesso = iep.numeroProcesso)), "
			+

			// remanescentes
			"	  (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "		where iep.codEvento is null and "
			+ "    	  	  iep.codEstado = ep.codEstado and "
			+ " 	          iep.orgaoJulgador = ep.orgaoJulgador and "
			+ " 	          iep.jurisdicao = ep.jurisdicao and "
			+ "			  to_char(iep.dataInclusao, 'yyyy-MM-dd') = :"
			+ QUERY_PARAMETER_DATA_REMANESCENTE_STR
			+ ") "
			+

			"	from EstatisticaEventoProcesso ep where "
			+ "	   to_char(ep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ "	  and to_char(ep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "	group by ep.codEstado, ep.orgaoJulgador, ep.jurisdicao "
			+ "	order by ep.codEstado asc, ep.orgaoJulgador asc";

	static final String LIST_PROCESSOS_REMA_DIS_JUL_ARQ_TRAMITACAO_COM_SESSAO_QUERY = "select ep.codEstado, ep.orgaoJulgador, ep.jurisdicao, "
			+

			// distribuidos
			"	 (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "	  where iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_DISTRIBUICAO
			+ " and "
			+ "	 	    iep.codEstado = ep.codEstado and "
			+ "           iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "	 	    iep.jurisdicao = ep.jurisdicao and "
			+ "	 	    to_char(iep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "	  		to_char(iep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "), "
			+

			// julgados
			"	 (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "	  where iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_JULGAMENTO
			+ " and "
			+ "	 	    iep.codEstado = ep.codEstado and "
			+ "	 	    iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "	 	    iep.jurisdicao = ep.jurisdicao and "
			+ "	 	    to_char(iep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "	  		to_char(iep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "), "
			+

			// arquivados
			"	  (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "		  where (iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_ARQUIVAMENTO
			+ " or "
			+ "		  		 iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " or "
			+ "		  		 iep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO3
			+ ") and "
			+ "		 	     iep.codEstado = ep.codEstado and "
			+ "		 	     iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "		 	     iep.jurisdicao = ep.jurisdicao and "
			+ "	 	    	 to_char(iep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "	  			 to_char(iep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "), "
			+

			// tramitacao
			"	  (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "		where iep.codEvento !=  :"
			+ QUERY_PARAMETER_EVENTO1
			+ " and "
			+ "             iep.codEvento !=  :"
			+ QUERY_PARAMETER_EVENTO2
			+ " and "
			+ "    	  	  iep.codEstado = ep.codEstado and "
			+ " 	          iep.orgaoJulgador = ep.orgaoJulgador and "
			+ " 	          iep.jurisdicao = ep.jurisdicao and "
			+ "			  to_char(iep.dataInclusao, 'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "             iep.idEstatisticaProcesso in "
			+ "             (select max(e2.idEstatisticaProcesso) from EstatisticaEventoProcesso e2 "
			+ "                 where iep.codEstado = ep.codEstado and iep.orgaoJulgador = ep.orgaoJulgador and "
			+ "                 to_char(e2.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and "
			+ "                 e2.numeroProcesso = iep.numeroProcesso)), "
			+

			// remanescentes
			"	  (select count(distinct iep.numeroProcesso) from EstatisticaEventoProcesso iep "
			+ "		where iep.codEvento is null and "
			+ "    	  	  iep.codEstado = ep.codEstado and "
			+ " 	          iep.orgaoJulgador = ep.orgaoJulgador and "
			+ " 	          iep.jurisdicao = ep.jurisdicao and "
			+ "			  to_char(iep.dataInclusao, 'yyyy-MM-dd') = :"
			+ QUERY_PARAMETER_DATA_REMANESCENTE_STR
			+ ") "
			+

			"	from EstatisticaEventoProcesso ep where "
			+ " 		  to_char(ep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ "	  and to_char(ep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ "     and ep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ "	group by ep.codEstado, ep.orgaoJulgador, ep.jurisdicao "
			+ "	order by ep.codEstado asc, ep.orgaoJulgador asc";

	static final String LIST_ULTIMO_EVENTO_PROCESSOS_QUERY = "select distinct eep.numeroProcesso, "
			+ "(select max(ee.idEstatisticaProcesso) " + "from EstatisticaEventoProcesso ee "
			+ "where ee.numeroProcesso = eep.numeroProcesso and " + "ee.dataInclusao = (select max(ie.dataInclusao) "
			+ "					from EstatisticaEventoProcesso ie " + "					where ie.numeroProcesso = eep.numeroProcesso)) "
			+ "from EstatisticaEventoProcesso eep " + "where eep.dataInclusao <= :" + QUERY_PARAMETER_DATA_FIM
			+ " and " + "	  eep.dataInclusao >= :" + QUERY_PARAMETER_DATA_INCIO + " " + "order by eep.numeroProcesso";

	static final String LIST_TIPO_VARA_BY_ESTADO_QUERY = "select new map(o.competencia as competencia) "
			+ "from EstatisticaEventoProcesso o " + "where o.codEvento = '193' " + " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO + " and to_char(o.dataInclusao,'yyyy-MM') >= :" + QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :" + QUERY_PARAMETER_DATA_FIM + " and o.competencia != null"
			+ " group by o.competencia";

	static final String LIST_VARA_BY_TIPO_VARA_QUERY = "select new map(o.orgaoJulgador as orgaoJulgador) "
			+ "from EstatisticaEventoProcesso o " + "where o.codEvento = '193' " + " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO + " and o.competencia = :" + QUERY_PARAMETER_COMPETENCIA
			+ " and to_char(o.dataInclusao,'yyyy-MM') >= :" + QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :" + QUERY_PARAMETER_DATA_FIM + " group by o.orgaoJulgador";

	static final String LIST_QUANTIDADE_PROCESSOS_MENSAL_BY_VARA_QUERY = "select new map(extract(MONTH from o.dataInclusao) as mes, count(distinct o.numeroProcesso) as numProcessos) "
			+ "from EstatisticaEventoProcesso o "
			+ "where o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and o.competencia = :"
			+ QUERY_PARAMETER_COMPETENCIA
			+ " and o.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and to_char(o.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " group by extract(MONTH from o.dataInclusao)";

	static final String LIST_PROCESSOS_BY_ESTADO_ORGAO_JULGADOR_QUERY = "select distinct eep.numeroProcesso, "
			+ "eep.classeJudicial, " + "eep.dataInclusao, "
			+ "(select max(ep.idEstatisticaProcesso) from EstatisticaEventoProcesso ep "
			+ "where ep.numeroProcesso = eep.numeroProcesso and cast(ep.dataInclusao as date) <= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and ep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ ") as remessa, "
			+ "eep.documentoApelacao, "
			+ "(select count(distinct f.numeroProcesso) from EstatisticaEventoProcesso f where cast(f.dataInclusao as date) <= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and f.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_JULGADO
			+ " and eep.numeroProcesso = f.numeroProcesso ) as julgados "
			+ "from EstatisticaEventoProcesso eep "
			+ "where eep.codEvento not in (:"
			+ QUERY_PARAMETER_EVENTO2
			+ ", :"
			+ QUERY_PARAMETER_EVENTO3
			+ ")"
			+ " and cast(eep.dataInclusao as date) <= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and eep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and eep.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and eep.idEstatisticaProcesso in (select max(o.idEstatisticaProcesso)"
			+ " from EstatisticaEventoProcesso o where"
			+ " cast(o.dataInclusao as date) <= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and eep.numeroProcesso = o.numeroProcesso)"
			+ " order by eep.numeroProcesso";

	static final String COUNT_PROCESSOS_BY_ESTADO_ORGAO_JULGADOR_QUERY = "select count(distinct eep.numeroProcesso) "
			+ "from EstatisticaEventoProcesso eep where " + "eep.codEvento = :" + QUERY_PARAMETER_EVENTO_JULGADO
			+ " and eep.codEstado = :" + QUERY_PARAMETER_COD_ESTADO + " and eep.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR + " and cast(eep.dataInclusao as date) <= :" + QUERY_PARAMETER_DATA_INCIO
			+ " and exists (select p.idEstatisticaProcesso from " + " EstatisticaEventoProcesso p where "
			+ " p.codEvento not in (:" + QUERY_PARAMETER_EVENTO1 + ", :" + QUERY_PARAMETER_EVENTO2 + ")"
			+ " and cast(p.dataInclusao as date) <= :" + QUERY_PARAMETER_DATA_INCIO
			+ " and p.idEstatisticaProcesso in (select max(ep.idEstatisticaProcesso) "
			+ " from EstatisticaEventoProcesso ep where " + " cast(ep.dataInclusao as date) <= :"
			+ QUERY_PARAMETER_DATA_INCIO + " and eep.numeroProcesso = ep.numeroProcesso))";

	static final String LIST_VARAS_SECAO_PROC_TRAMIT_QUERY = "select new map(eep.orgaoJulgador as orgaoJulgador, eep.jurisdicao as jurisdicao,"
			+ " count(distinct eep.numeroProcesso) as qtd)"
			+ " from EstatisticaEventoProcesso eep"
			+ " where eep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and eep.codEvento != :"
			+ QUERY_PARAMETER_EVENTO1
			+ "	and eep.codEvento != :"
			+ QUERY_PARAMETER_EVENTO2
			+ " and to_char(eep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and eep.idEstatisticaProcesso in (select max(ep.idEstatisticaProcesso) "
			+ " from EstatisticaEventoProcesso ep where "
			+ "	to_char(ep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and ep.numeroProcesso = eep.numeroProcesso)"
			+ " group by eep.orgaoJulgador, eep.jurisdicao" + " order by count(distinct eep.numeroProcesso)";

	static final String LIST_QUANTIDADE_PROCESSOS_TRAMIT_MENSAL_BY_VARA_QUERY = "select new map(extract(MONTH from o.dataInclusao) as mes,"
			+ " extract(YEAR from o.dataInclusao) as ano, count(distinct o.numeroProcesso) as numProcessos)"
			+ " from EstatisticaEventoProcesso o"
			+ " where o.codEvento != :"
			+ QUERY_PARAMETER_EVENTO1
			+ " and o.codEvento != :"
			+ QUERY_PARAMETER_EVENTO2
			+ " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and o.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and o.idEstatisticaProcesso in (select max(ep.idEstatisticaProcesso) from "
			+ " EstatisticaEventoProcesso ep where "
			+ " to_char(ep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and ep.numeroProcesso = o.numeroProcesso)"
			+ " group by extract(MONTH from o.dataInclusao), extract(YEAR from o.dataInclusao)";

	static final String LIST_VARAS_SECAO_PROC_ARQ_QUERY = "select new map(eep.orgaoJulgador as orgaoJulgador, eep.jurisdicao as jurisdicao,"
			+ " count(distinct eep.numeroProcesso) as qtd)"
			+ " from EstatisticaEventoProcesso eep"
			+ " where eep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and (eep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ "	     or eep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO2
			+ "	     or eep.codEvento = :"
			+ QUERY_PARAMETER_EVENTO3
			+ ")"
			+ " and to_char(eep.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " and to_char(eep.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " group by eep.orgaoJulgador, eep.jurisdicao" + " order by count(distinct eep.numeroProcesso)";

	static final String LIST_QUANTIDADE_PROCESSOS_ARQ_MENSAL_BY_VARA_QUERY = "select new map(extract(MONTH from o.dataInclusao) as mes,"
			+ " extract(YEAR from o.dataInclusao) as ano, count(distinct o.numeroProcesso) as numProcessos)"
			+ " from EstatisticaEventoProcesso o"
			+ " where (o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ "        or o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO2
			+ "        or o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO3
			+ ")"
			+ " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and o.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and to_char(o.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " group by extract(MONTH from o.dataInclusao), extract(YEAR from o.dataInclusao)";

	static final String LIST_QUANTIDADE_PROCESSOS_DISTRIBUIDOS_MENSAL_BY_VARA_QUERY = "select new map(extract(MONTH from o.dataInclusao) as mes, count(distinct o.numeroProcesso) as numProcessos) "
			+ "from EstatisticaEventoProcesso o "
			+ "where o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_DISTRIBUICAO
			+ " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and o.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and to_char(o.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " group by extract(MONTH from o.dataInclusao)";

	static final String LIST_VARA_BY_SECAO_QUERY = "select new map(o.orgaoJulgador as orgaoJulgador, "
			+ "o.jurisdicao as jurisdicao) " + "from EstatisticaEventoProcesso o " + "where o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO_DISTRIBUICAO + " and o.codEstado = :" + QUERY_PARAMETER_COD_ESTADO
			+ " and to_char(o.dataInclusao,'yyyy-MM') >= :" + QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :" + QUERY_PARAMETER_DATA_FIM
			+ " group by o.orgaoJulgador, o.jurisdicao";

	static final String LIST_QUANTIDADE_PROCESSOS_JUL_MENSAL_BY_VARA_QUERY = "select new map(extract(MONTH from o.dataInclusao) as mes,"
			+ " extract(YEAR from o.dataInclusao) as ano, count(distinct o.numeroProcesso) as numProcessos)"
			+ " from EstatisticaEventoProcesso o"
			+ " where o.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " and o.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO
			+ " and o.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and to_char(o.dataInclusao,'yyyy-MM') >= :"
			+ QUERY_PARAMETER_DATA_INCIO
			+ " and to_char(o.dataInclusao,'yyyy-MM') <= :"
			+ QUERY_PARAMETER_DATA_FIM
			+ " group by extract(MONTH from o.dataInclusao), extract(YEAR from o.dataInclusao)";

	static final String LIST_VARAS_SECAO_PROC_JUL_QUERY = "select new map(eep.orgaoJulgador as orgaoJulgador,"
			+ " eep.jurisdicao as jurisdicao)" + " from EstatisticaEventoProcesso eep" + " where eep.codEstado = :"
			+ QUERY_PARAMETER_COD_ESTADO + " and eep.codEvento = :" + QUERY_PARAMETER_EVENTO1
			+ " and to_char(eep.dataInclusao,'yyyy-MM') <= :" + QUERY_PARAMETER_DATA_FIM
			+ " and to_char(eep.dataInclusao,'yyyy-MM') >= :" + QUERY_PARAMETER_DATA_INCIO
			+ " group by eep.orgaoJulgador, eep.jurisdicao";
}