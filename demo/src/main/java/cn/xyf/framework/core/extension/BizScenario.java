package cn.xyf.framework.core.extension;

public class BizScenario
        implements IBizScenario {
    public static final String DEFAULT_TENANT = "defaultTenant";
    public static final String DEFAULT_BUSINESS = "defaultBusiness";
    public static final String DEFAULT_PARTNER = "defaultPartner";
    private static final String DOT_SEPARATOR = ".";
    private String tenant = "defaultTenant";


    private String business = "defaultBusiness";


    private String partner = "defaultPartner";


    public String getUniqueIdentity() {
        return this.tenant + "." + this.business +
                "." + this.partner;
    }


    public static BizScenario valueOf(String tenant, String business, String partner) {
        BizScenario bizScenario = new BizScenario();
        bizScenario.tenant = tenant;
        bizScenario.business = business;
        bizScenario.partner = partner;
        return bizScenario;
    }

    public static BizScenario valueOf(String tenant) {
        return valueOf(tenant, "defaultBusiness", "defaultPartner");
    }

    public static BizScenario valueOf(String tenant, String business) {
        return valueOf(tenant, business, "defaultPartner");
    }

    public static BizScenario newDefault() {
        return valueOf("defaultTenant", "defaultBusiness", "defaultPartner");
    }
}



