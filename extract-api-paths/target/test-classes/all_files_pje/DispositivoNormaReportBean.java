package br.com.infox.cliente.home;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jsoup.Jsoup;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.NormaPenal;

@Name("dispositivoNormaReportBean")
@BypassInterceptors
public class DispositivoNormaReportBean {

	private NormaPenal normaPenal;

	private EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	public void prepare(Integer id) {
		if (id != null) {
			normaPenal = getEntityManager().find(NormaPenal.class, id);
		}
	}

	public NormaPenal getNormaPenal() {
		return normaPenal;
	}

	public void setNormaPenal(NormaPenal normaPenal) {
		this.normaPenal = normaPenal;
	}

	@SuppressWarnings("unchecked")
	public List<DispositivoNorma> getDispositivos() {
		List<DispositivoNorma> dispositivos = new ArrayList<DispositivoNorma>(0);
		Query queryRoots = getEntityManager().createQuery(
				" from DispositivoNorma d "
						+ "where d.normaPenal.idNormaPenal = ? and d.dispositivoNormaPai.idDispositivoNorma is null");
		queryRoots.setParameter(1, normaPenal.getIdNormaPenal());
		List<DispositivoNorma> roots = queryRoots.getResultList();
		for (DispositivoNorma dispositivoNorma : roots) {
			dispositivos.addAll(getHierarquia(dispositivoNorma));
		}

		return dispositivos;
	}

	private List<DispositivoNorma> getHierarquia(DispositivoNorma root) {

		List<DispositivoNorma> hierarquia = new ArrayList<DispositivoNorma>(0);
		hierarquia.add(root);

		for (DispositivoNorma dispositivoNorma : root.getDispositivoNormaList()) {

			if (!dispositivoNorma.getDispositivoNormaList().isEmpty()) {
				hierarquia.addAll(getHierarquia(dispositivoNorma));
			} else {
				hierarquia.add(dispositivoNorma);
			}
		}

		return hierarquia;
	}

	public Integer ident(DispositivoNorma dispositivoNorma) {
		Integer returnValue = 0;
		DispositivoNorma dispositivoPai = dispositivoNorma.getDispositivoNormaPai();
		while (dispositivoPai != null) {
			returnValue += 10;
			dispositivoPai = dispositivoPai.getDispositivoNormaPai();
		}
		return returnValue;
	}

	public void fillAndDownloadReport(Integer id) {
		prepare(id);
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();

		if (normaPenal != null) {
			InputStream inputStream = FacesContext.getCurrentInstance().getExternalContext()
					.getResourceAsStream("/NormaPenal/normaPenal.xls");
			try {
				HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
				HSSFSheet sheet = workbook.getSheetAt(0);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

				// Norma Penal
				HSSFRow row = sheet.createRow(2);
				row.createCell(0).setCellValue(normaPenal.getTipoNormaPenal().getDescricao());
				row.createCell(1).setCellValue(normaPenal.getNrNorma());
				row.createCell(2).setCellValue(normaPenal.getNormaPenal());
				row.createCell(3).setCellValue(normaPenal.getDsSigla());

				row.createCell(4).setCellValue(sdf.format(normaPenal.getDataInicioVigencia()));
				if (normaPenal.getDataFimVigencia() != null) {
					row.createCell(5).setCellValue(sdf.format(normaPenal.getDataFimVigencia()));
				}
				row.createCell(6).setCellValue(normaPenal.getAtivo() ? "Ativo" : "Inativo");

				// Dispositivos
				int rowIndex = 9;

				for (DispositivoNorma dispositivo : getDispositivos()) {
					row = sheet.createRow(rowIndex);
					row.createCell(0).setCellValue(dispositivo.getDsSimbolo());
					row.createCell(1).setCellValue(dispositivo.getDsIdentificador());
					row.createCell(2).setCellValue(sdf.format(dispositivo.getDtInicioVigencia()));
					if (dispositivo.getDtFimVigencia() != null) {
						row.createCell(3).setCellValue(sdf.format(dispositivo.getDtFimVigencia()));
					}
					row.createCell(4).setCellValue(htmlTrim(dispositivo.getDsTextoDispositivo()));
					if (dispositivo.getUsoDispositivo() != null) {
						row.createCell(5).setCellValue(dispositivo.getUsoDispositivo().getLabel());
					} else {
						row.createCell(5).setCellValue("Nenhum dos Tipos");
					}
					row.createCell(6).setCellValue(dispositivo.getInHediondo() ? "Sim" : "Não");
					if (dispositivo.getDtHediondo() != null) {
						row.createCell(7).setCellValue(sdf.format(dispositivo.getDtHediondo()));
					}
					row.createCell(8).setCellValue(dispositivo.getInPrevisaoPenaRestritiva() ? "Sim" : "Não");

					if (dispositivo.getNrPenaMinimaAnos() != null) {
						row.createCell(9).setCellValue(dispositivo.getNrPenaMinimaAnos());
					}

					if (dispositivo.getNrPenaMinimaMeses() != null) {
						row.createCell(10).setCellValue(dispositivo.getNrPenaMinimaMeses());
					}

					if (dispositivo.getNrPenaMinimaDias() != null) {
						row.createCell(11).setCellValue(dispositivo.getNrPenaMinimaDias());
					}

					if (dispositivo.getNrPenaMaximaAnos() != null) {
						row.createCell(12).setCellValue(dispositivo.getNrPenaMaximaAnos());
					}

					if (dispositivo.getNrPenaMaximaMeses() != null) {
						row.createCell(13).setCellValue(dispositivo.getNrPenaMaximaMeses());
					}

					if (dispositivo.getNrPenaMaximaDias() != null) {
						row.createCell(14).setCellValue(dispositivo.getNrPenaMaximaDias());
					}

					row.createCell(15).setCellValue(dispositivo.getInPrevisaoPenaMulta() ? "Sim" : "Não");

					if (dispositivo.getMultaPenaPrivativa() != null) {
						row.createCell(16).setCellValue(dispositivo.getMultaPenaPrivativa().getDsMultaPenaPrivativa());
					}

					row.createCell(17).setCellValue(dispositivo.getAtivo() ? "Ativo" : "Inativo");

					row.createCell(18).setCellValue(dispositivo.getPermitirAssociacaoMultipla() ? "Sim" : "Não");

					if (dispositivo.getAssuntoTrf() != null) {
						row.createCell(19).setCellValue(dispositivo.getAssuntoTrf().getAssuntoCompleto());
					}

					if (dispositivo.getAssuntoAtoInfracional() != null) {
						row.createCell(20).setCellValue(dispositivo.getAssuntoAtoInfracional().getAssuntoCompleto());
					}

					if (dispositivo.getDispositivoNormaPai() != null) {
						row.createCell(21).setCellValue(
								(dispositivo.getDispositivoNormaPai().getDsSimbolo() == null ? "" : dispositivo
										.getDispositivoNormaPai().getDsSimbolo())
										+ " "
										+ dispositivo.getDispositivoNormaPai().getDsIdentificador());
					}

					rowIndex++;
				}

				// Download do arquivo
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ServletOutputStream out = response.getOutputStream();
				workbook.write(baos);

				response.setHeader("Expires", "0");
				response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
				response.setHeader("Pragma", "public");

				response.setHeader("Content-Disposition", "attachment;filename=dispositivos.xls");

				response.setContentLength(baos.size());
				baos.writeTo(out);
				out.flush();

				FacesContext.getCurrentInstance().responseComplete();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String htmlTrim(String source) {
		// String nohtml = source.toString().replaceAll("\\<.*?>","");
		return Jsoup.parse(source).text();
	}
}
