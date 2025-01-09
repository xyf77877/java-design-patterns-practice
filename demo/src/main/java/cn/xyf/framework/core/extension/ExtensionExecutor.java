package cn.xyf.framework.core.extension;

import cn.xyf.framework.core.boot.AbstractComponentExecutor;
import cn.xyf.framework.core.exception.framework.FrameworkException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExtensionExecutor extends AbstractComponentExecutor {
    private Logger logger = LoggerFactory.getLogger(ExtensionExecutor.class);

    @Autowired
    private ExtensionRepository extensionRepository;


    protected <C> C locateComponent(Class<C> targetClz, IBizScenario bizScenario) {
        C extension = locateExtension(targetClz, bizScenario);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("[Located Extension]: " + extension.getClass().getSimpleName());
        }
        return extension;
    }


    protected <Ext> Ext locateExtension(Class<Ext> targetClz, IBizScenario bizScenario) {
        checkNull(bizScenario);


        String bizScenarioUniqueIdentity = bizScenario.getUniqueIdentity();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("BizScenario in locateExtension is : {}", bizScenarioUniqueIdentity);
        }


        Ext extension = firstTry(targetClz, bizScenarioUniqueIdentity);
        if (extension != null) {
            return extension;
        }


        extension = loopTry(targetClz, bizScenarioUniqueIdentity);
        if (extension != null) {
            return extension;
        }

        throw new FrameworkException("Can not find extension with ExtensionPoint: " + targetClz + " BizScenario:" + bizScenarioUniqueIdentity);
    }

    private <Ext> Ext firstTry(Class<Ext> targetClz, String bizScenario) {
        return (Ext) this.extensionRepository.getExtensionRepo().get(new ExtensionCoordinate(targetClz.getName(), bizScenario));
    }


    private <Ext> Ext loopTry(Class<Ext> targetClz, String bizScenario) {
        if (bizScenario == null) {
            return null;
        }


        int lastDotIndex = bizScenario.lastIndexOf(".");
        String suffix = "";
        while (lastDotIndex != -1) {
            Ext tmpExt = loopTry(targetClz, bizScenario, suffix);
            if (tmpExt != null) {
                return tmpExt;
            }
            bizScenario = bizScenario.substring(0, lastDotIndex);
            suffix = StringUtils.join((Object[]) new String[]{".", "default", suffix});
            String str = bizScenario + suffix;
            Ext ext1 = (Ext) this.extensionRepository.getExtensionRepo().get(new ExtensionCoordinate(targetClz.getName(), str));
            if (ext1 != null) {
                return ext1;
            }
            lastDotIndex = bizScenario.lastIndexOf(".");
        }

        String defaultBizScenario = "default" + suffix;
        Ext extension = (Ext) this.extensionRepository.getExtensionRepo().get(new ExtensionCoordinate(targetClz.getName(), defaultBizScenario));

        return extension;
    }


    private <Ext> Ext loopTry(Class<Ext> targetClz, String bizScenario, String suffix) {
        if (bizScenario == null) {
            return null;
        }

        int lastDotIndex = bizScenario.lastIndexOf(".");
        boolean isFirst = true;
        while (lastDotIndex != -1) {
            if (isFirst) {
                suffix = bizScenario.substring(lastDotIndex) + suffix;
                bizScenario = bizScenario.substring(0, lastDotIndex);
                lastDotIndex = bizScenario.lastIndexOf(".");
                isFirst = false;

                continue;
            }
            bizScenario = bizScenario.substring(0, lastDotIndex);
            suffix = StringUtils.join((Object[]) new String[]{".", "default", suffix});
            String str = bizScenario + suffix;
            Ext ext = (Ext) this.extensionRepository.getExtensionRepo().get(new ExtensionCoordinate(targetClz.getName(), str));
            if (ext != null) {
                return ext;
            }
            lastDotIndex = bizScenario.lastIndexOf(".");
        }

        if (isFirst) {
            return null;
        }

        String defaultBizScenario = "default" + suffix;
        Ext extension = (Ext) this.extensionRepository.getExtensionRepo().get(new ExtensionCoordinate(targetClz.getName(), defaultBizScenario));
        return extension;
    }


    private void checkNull(IBizScenario bizScenario) {
        if (bizScenario == null) throw new FrameworkException("BizScenario can not be null for extension");
    }
}



