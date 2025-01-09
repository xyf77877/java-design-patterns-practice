package cn.xyf.framework.core.extension;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExtensionRepository {
    private Map<ExtensionCoordinate, IExtensionPoint> extensionRepo = new HashMap<>();

    public Map<ExtensionCoordinate, IExtensionPoint> getExtensionRepo() {
        return this.extensionRepo;
    }

}



