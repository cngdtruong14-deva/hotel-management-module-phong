package module_phong.model;

public record Tang(String maTang, String tenTang) {
    public String toString() {
        return maTang + " · " + tenTang;
    }
}
