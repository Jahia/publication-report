package org.jahia.modules.publicationreport;

import java.io.Serializable;

public class ResultMessage implements Serializable {

    String nodePath;
    String uuid;
    String publishStatus;
    String author;
    String lastModified;
    String locale;

    private static final long serialVersionUID = -6552128415414065542L;

    /**     
     * Constructor for ResultMessage
     */
    public ResultMessage(String nodePath, String publishStatus, String author, String lastModfied, String locale, String uuid) {
        this.nodePath = nodePath;
        this.uuid = uuid;
        this.publishStatus = publishStatus;
        this.author = author;
        this.lastModified = lastModfied;
        this.locale = locale;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
