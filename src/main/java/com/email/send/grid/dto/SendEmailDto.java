package com.email.send.grid.dto;

import java.util.Set;

public class SendEmailDto {
    public String body;
    public String subject;
    public String testEmail;
    public String bulkEmail;
    public String fromEmail;
    public String emailType;
    public Integer limit;
    public String offer;
    public String domain;
    public String dataFile;
    public String accessId;
    public String accessKey;
    public Integer limitToSend;
    public Integer sleepyTime;
    public String zone;
    public String apiKey;
    public String fromName;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTestEmail() {
        return testEmail;
    }

    public void setTestEmail(String testEmail) {
        this.testEmail = testEmail;
    }

    public String getBulkEmail() {
        return bulkEmail;
    }

    public void setBulkEmail(String bulkEmail) {
        this.bulkEmail = bulkEmail;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Integer getLimitToSend() {
        return limitToSend;
    }

    public void setLimitToSend(Integer limitToSend) {
        this.limitToSend = limitToSend;
    }

    public Integer getSleepyTime() {
        return sleepyTime;
    }

    public void setSleepyTime(Integer sleepyTime) {
        this.sleepyTime = sleepyTime;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    @Override
    public String toString() {
        return "SendEmailDto{" +
                "body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", testEmail='" + testEmail + '\'' +
                ", bulkEmail='" + bulkEmail + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", emailType='" + emailType + '\'' +
                ", limit=" + limit +
                ", offer='" + offer + '\'' +
                ", domain='" + domain + '\'' +
                ", dataFile='" + dataFile + '\'' +
                ", accessId='" + accessId + '\'' +
                ", key='" + accessKey + '\'' +
                ", limitToSend=" + limitToSend +
                ", sleepyTime=" + sleepyTime +
                ", zone='" + zone + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", fromName='" + fromName + '\'' +
                '}';
    }
}
