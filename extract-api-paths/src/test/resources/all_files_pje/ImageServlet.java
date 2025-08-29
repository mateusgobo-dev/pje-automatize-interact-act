package br.com.infox.pje.servlet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class ImageServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String codigoPareamento = request.getParameter("codigo");
		String cpf = request.getParameter("cpf");
		String email = request.getParameter("email");
		String sistema = request.getParameter("sistema");
		String contexto = request.getParameter("contexto");
		String url = request.getParameter("url");
		
		if(codigoPareamento == null || codigoPareamento.isEmpty()){
			return;
		}
		
		if((cpf == null || cpf.isEmpty())){
			return;
		}
		
		if((email == null || email.isEmpty())){
			return;
		}
		
		if((sistema == null || sistema.isEmpty())){
			return;
		}
		
		if((url == null || url.isEmpty())){
			return;
		}
		
		byte[] entry = null;
		
		StringBuilder uriOtp = new StringBuilder("otpauth://totp/");
		uriOtp.append(URLEncoder.encode( ConfiguracaoIntegracaoCloud.getAppName().toUpperCase().replace("-", " "), "UTF-8"));
		uriOtp.append(":");
		uriOtp.append(email);
		uriOtp.append("?secret=");
		uriOtp.append(codigoPareamento);
		uriOtp.append("&issuer=");
		uriOtp.append(URLEncoder.encode(ConfiguracaoIntegracaoCloud.getAppName().toUpperCase().replace("-", " "), "UTF-8"));
		uriOtp.append("&algorithm=SHA1&digits=6&period=30");
		uriOtp.append("&cpf=");
		uriOtp.append(cpf);
		uriOtp.append("&contexto=");
		uriOtp.append(contexto);
		uriOtp.append("&sistema=");
		uriOtp.append(URLEncoder.encode(sistema, "UTF-8"));
		uriOtp.append("&url=");
		uriOtp.append(URLEncoder.encode(url, "UTF-8"));
		
		entry = generateQrCode(uriOtp.toString().replace("+", "%20"));
		
		if (entry == null || entry.length == 0) {
			return;
		}

		ServletOutputStream out = null;
		response.reset();

		out = response.getOutputStream();
		out.write(entry);
		close(out);
		
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	// Helpers (can be refactored to public utility class)
	// ----------------------------------------
	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				//
				e.printStackTrace();
			}
		}
	}

	private byte[] generateQrCode(String codigoPareamento) {

		String myCodeText = codigoPareamento;
		int size = 250;
		String fileType = "png";
		byte[] fileByte = null;

		try {

			Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

			// Now with zxing version 3.2.1 you could change border size (white
			// border size to just 1)
			hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
			int CrunchifyWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < CrunchifyWidth; i++) {
				for (int j = 0; j < CrunchifyWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, fileType, baos);
			baos.flush();
			fileByte = baos.toByteArray();
			baos.close();

		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileByte;
	}
}
