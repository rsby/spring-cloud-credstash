package com.catalyticds.credstash;

import java.util.Map;
import java.util.Optional;

/**
 * @author reesbyars on 9/30/17.
 */
public class SecretRequest {

    private String table = "credential-store";
    private String secretName;
    private String version;
    private Map<String, String> context;

    public SecretRequest(String secretName) {
        this.secretName = secretName;
    }

    public SecretRequest withTable(String table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        this.table = table;
        return this;
    }

    public SecretRequest withVersion(String version) {
        this.version = version;
        return this;
    }

    public SecretRequest withContext(Map<String, String> context) {
        this.context = context;
        return this;
    }

    public String getTable() {
        return table;
    }

    public String getSecretName() {
        return secretName;
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    public Optional<Map<String, String>> getContext() {
        return Optional.ofNullable(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecretRequest that = (SecretRequest) o;
        return table.equals(that.table) &&
                secretName.equals(that.secretName) &&
                (version != null ? version.equals(that.version) : that.version == null) &&
                (context != null ? context.equals(that.context) : that.context == null);
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 31 * result + secretName.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SecretRequest{" +
                "table='" + table + '\'' +
                ", secretName='" + secretName + '\'' +
                ", version='" + version + '\'' +
                ", context=" + context +
                '}';
    }

}
