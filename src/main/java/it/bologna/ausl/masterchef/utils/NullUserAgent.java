package it.bologna.ausl.masterchef.utils;

import java.io.InputStream;
import org.springframework.util.StringUtils;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;


public class NullUserAgent extends ITextUserAgent {

    public NullUserAgent(float dotsPerPoint, int dotsPerPixel) {
        super(new ITextOutputDevice(dotsPerPoint), dotsPerPixel);
    }

    @Override
    public String resolveURI(String uri) {
        if (StringUtils.hasText(uri) && uri.startsWith("jar:file")) {
            return super.resolveURI(uri);
        } else {
            return null;
        }
    }

    @Override
    protected InputStream resolveAndOpenStream(String filepath) {
        return super.resolveAndOpenStream(filepath);
    }
}