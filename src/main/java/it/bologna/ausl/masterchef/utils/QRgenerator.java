/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.masterchef.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;



public class QRgenerator {

	public static BufferedImage generate(String content,int width,int height) throws WriterException{
		
		QRCodeWriter qw = new QRCodeWriter();
		HashMap <EncodeHintType,Object>conf=new HashMap<EncodeHintType,Object>();
		conf.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		BitMatrix matrix=qw.encode(content, BarcodeFormat.QR_CODE, width, height,conf);
		//BitMatrix matrix = Encoder.encode(content, ErrorCorrectionLevel.H, qrcode);
	    //return matrix;
		return MatrixToImageWriter.toBufferedImage(matrix);
		
	}
	
	public static void main(String[] args) throws WriterException, IOException
	{
		//MatrixToImageWriter.writeToFile(generate("culo precipitevolissimevolmente",200,200),"png",new File("test.png"));
		
		
	}
}
