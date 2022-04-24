package rl.main;

public class ColumnConfig {

    private String name;
    private Length length;
    private boolean uuid;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUuid() {
        return uuid;
    }

    public void setUuid(boolean uuid) {
        this.uuid = uuid;
    }

    public Length getLength() {
        return length;
    }

    public void setLength(Length length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public boolean isGetDate() {
        return "getdate()".equals(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ColumnConfig{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", uuid=" + uuid +
                ", type='" + type + '\'' +
                '}';
    }
}
