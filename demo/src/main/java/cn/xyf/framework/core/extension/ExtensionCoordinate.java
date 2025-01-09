package cn.xyf.framework.core.extension;

public class ExtensionCoordinate {
    private String extensionPointName;
    private String bizScenarioUniqueIdentity;
    private Class extensionPointClass;
    private IBizScenario bizScenario;

    public String getExtensionPointName() {
        return this.extensionPointName;
    }

    public void setExtensionPointName(String extensionPointName) {
        this.extensionPointName = extensionPointName;
    }

    public String getBizScenarioUniqueIdentity() {
        return this.bizScenarioUniqueIdentity;
    }

    public void setBizScenarioUniqueIdentity(String bizScenarioUniqueIdentity) {
        this.bizScenarioUniqueIdentity = bizScenarioUniqueIdentity;
    }


    public Class getExtensionPointClass() {
        return this.extensionPointClass;
    }

    public void setExtensionPointClass(Class extensionPointClass) {
        this.extensionPointClass = extensionPointClass;
    }

    public IBizScenario getBizScenario() {
        return this.bizScenario;
    }

    public void setBizScenario(IBizScenario bizScenario) {
        this.bizScenario = bizScenario;
    }

    public static ExtensionCoordinate valueOf(Class extPtClass, IBizScenario bizScenario) {
        return new ExtensionCoordinate(extPtClass, bizScenario);
    }

    public ExtensionCoordinate(Class extPtClass, IBizScenario bizScenario) {
        this.extensionPointClass = extPtClass;
        this.extensionPointName = extPtClass.getName();
        this.bizScenario = bizScenario;
        this.bizScenarioUniqueIdentity = bizScenario.getUniqueIdentity();
    }


    public ExtensionCoordinate(String extensionPoint, String bizScenario) {
        this.extensionPointName = extensionPoint;
        this.bizScenarioUniqueIdentity = bizScenario;
    }


    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + ((this.bizScenarioUniqueIdentity == null) ? 0 : this.bizScenarioUniqueIdentity.hashCode());
        result = 31 * result + ((this.extensionPointName == null) ? 0 : this.extensionPointName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExtensionCoordinate other = (ExtensionCoordinate) obj;
        if (this.bizScenarioUniqueIdentity == null) {
            if (other.bizScenarioUniqueIdentity != null) {
                return false;
            }
        } else if (!this.bizScenarioUniqueIdentity.equals(other.bizScenarioUniqueIdentity)) {
            return false;
        }
        if (this.extensionPointName == null) {
            if (other.extensionPointName != null) {
                return false;
            }
        } else if (!this.extensionPointName.equals(other.extensionPointName)) {
            return false;
        }
        return true;
    }


    public String toString() {
        return "ExtensionCoordinate [extensionPointName=" + this.extensionPointName + ", bizScenarioUniqueIdentity=" + this.bizScenarioUniqueIdentity + "]";
    }
}



