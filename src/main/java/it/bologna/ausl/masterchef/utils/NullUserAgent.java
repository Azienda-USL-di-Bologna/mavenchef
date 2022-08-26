package it.bologna.ausl.masterchef.utils;

import java.io.InputStream;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;


public class NullUserAgent extends ITextUserAgent {

    public NullUserAgent(float dotsPerPoint) {
        super(new ITextOutputDevice(dotsPerPoint));
    }

    @Override
    public String resolveURI(String uri) {
        return null;
    }

    @Override
    protected InputStream resolveAndOpenStream(String filepath) {
        return null;
    }
}