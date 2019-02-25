package com.catalyticds.credstash;

/**
 * @author reesbyars on 2/25/19.
 */
public class SecretPropertySourceConfig {

    private String secretName;
    private boolean required = true;

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return "SecretPropertySourceConfig{" +
                "secretName='" + secretName + '\'' +
                ", required=" + required +
                '}';
    }
}
