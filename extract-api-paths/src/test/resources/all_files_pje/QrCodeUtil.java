package br.jus.pje.nucleo.util;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

public class QrCodeUtil {
	/**
	 * Método responsável por gerar um QrCode no formato base64 para a url especificada
	 * @param url Url para a qual será gerado o QrCode
	 * @return QrCode no formato base64 para a url especificada
	 */
	public static String gerarQrCodeBase64(String url) {
		ByteArrayOutputStream byteArray = QRCode.from(url.toString()).to(ImageType.PNG).withSize(125, 125).stream();
		
		return "<img src=\"data:image/jpeg;base64," + 
				Base64.getEncoder().encodeToString(byteArray.toByteArray())
				+ "\"/>";
	}

	/**
	 * Método responsável por gerar um QrCode no formato de magem para a url especificda
	 * @param url Url para a qual será gerado o QrCode
	 * @return QrCode no formato de imagem para a url especificada
	 * @throws IOException
	 */
	public static Image gerarQrCodeImagem(String url) throws IOException {
		return gerarQrCodeImagem(url, 125, 125);
	}
	
	/**
	 * Método responsável por gerar um QrCode no formato de magem para a url especificda
	 * @param url Url para a qual será gerado o QrCode
	 * @return QrCode no formato de imagem para a url especificada
	 * @throws IOException
	 */
	public static Image gerarQrCodeImagem(String url, int largura, int altura) throws IOException {
		ByteArrayOutputStream byteArray = QRCode.from(url.toString()).to(ImageType.PNG).withSize(largura, altura).stream();
		return ImageIO.read(new ByteArrayInputStream(byteArray.toByteArray()));
	}
}
