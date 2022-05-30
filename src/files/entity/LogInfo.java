package files.entity;

import java.io.Serializable;

public class LogInfo implements Serializable {

    private static final long serialVersionUID = 1;

    private String eTag;

    private Long pos;

    private Long contentLength;

    public LogInfo(String eTag, Long pos, Long contentLength) {
        this.eTag = eTag;
        this.pos = pos;
        this.contentLength = contentLength;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public Long getPos() {
        return pos;
    }

    public void setPos(Long pos) {
        this.pos = pos;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "eTag='" + eTag + '\'' +
                ", pos=" + pos +
                ", contentLength=" + contentLength +
                '}';
    }
}
