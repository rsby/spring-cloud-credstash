package com.catalyticds.credstash;

/**
 * @author reesbyars on 9/30/17.
 */
public class DecryptedSecret {

    private final String table;
    private final String name;
    private final String version;
    private final String secret;

    DecryptedSecret(
            String table,
            String name,
            String version,
            String secret) {
        this.table = table;
        this.name = name;
        this.version = version;
        this.secret = secret;
    }

    public String getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecryptedSecret that = (DecryptedSecret) o;
        return table.equals(that.table) &&
                name.equals(that.name) &&
                version.equals(that.version) &&
                secret.equals(that.secret);
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + secret.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DecryptedSecret{" +
                "table='" + table + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
