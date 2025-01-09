package cn.xyf.framework;

import cn.xyf.framework.core.boot.PipelineRegister;
import cn.xyf.framework.core.pipeline.PipelineRepository;
import com.google.common.collect.TreeMultimap;
import com.google.errorprone.annotations.Var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"cn.xyf.framework"})
@RestController
public class DemoApplication {
    @Resource
    private PipelineRepository pipelineRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("test11")
    public TreeMultimap test(){
        return pipelineRepository.getPipelineRepo();
    }

}
