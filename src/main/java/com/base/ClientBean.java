package com.base;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.support.PbService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.zookeeper.CreateMode.EPHEMERAL;
import static org.apache.zookeeper.CreateMode.PERSISTENT;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/2/12 16:04
 */
@Component
@Slf4j
public class ClientBean implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Value("${zookeeper.address}")
    private String zkAddress;

    private ZKUtil zkUtil = new ZKUtil();

    private Map<String, List<String>> serviceProviderMap = Maps.newHashMap();
    /**
     * 远程服务名称集合
     */
    private Set<String> remoteServiceNames = Sets.newHashSet();

    private Map<String, Set<Field>> serviceFields = Maps.newHashMap();

    private Map<Field, Object> fieldObjectNames = Maps.newHashMap();

    private CuratorFramework client;

    /**
     * 服务的路径
     * /pbrpc
     * /service名称
     * /provider           /consumer
     * /ip：port            /ip：port
     * /ip：port            /ip：port
     * /ip：port            /ip：port
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("client afterPropertiesSet");
        loadLocalService();
        registryClient();
        findService();
        createJdkProxy();
    }

    private void createJdkProxy() throws IllegalAccessException {
        for (String remoteServiceName : remoteServiceNames) {
            Set<Field> fields = serviceFields.get(remoteServiceName);
            List<String> providers = serviceProviderMap.get(remoteServiceName);
            for (Field field : fields) {
                JdkProxy jdkProxy = new JdkProxy(providers);
                field.set(fieldObjectNames.get(field), jdkProxy.newProxy(field.getType()));
            }
        }
    }

    /**
     * 发现本地服务
     */
    private void loadLocalService() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object o = applicationContext.getBean(beanName);
            Field[] fields = o.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PbService.class)) {
                    fieldObjectNames.put(field, o);
                    String remoteServiceName = field.getAnnotation(PbService.class).name();
                    remoteServiceNames.add(remoteServiceName);
                    if (CollectionUtils.isEmpty(serviceFields.get(remoteServiceName))) {
                        serviceFields.put(remoteServiceName, Sets.newHashSet(field));
                    } else {
                        serviceFields.get(remoteServiceName).add(field);
                    }
                    log.info(" Field field={}", field);
                }
            }
        }
    }

    /**
     * 扫描远程RPC根目录
     *
     * @throws Exception
     */
    private void findRoot() throws Exception {
        if (!zkUtil.isOpen()) {
            zkUtil.startZk(zkAddress);
        }
        client = zkUtil.getClient();
        if (client.checkExists().forPath("/pbrpc") == null) {
            client.create().forPath("/pbrpc");
        }
    }

    private void findService() throws Exception {
        for (String remoteServiceName : remoteServiceNames) {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, RegistryUtil.getChildPath(remoteServiceName, RegistryUtil.PROVIDER), true);
            pathChildrenCache.start();
            List<String> list = client.getChildren().forPath(RegistryUtil.getChildPath(remoteServiceName, RegistryUtil.PROVIDER));
            serviceProviderMap.put(remoteServiceName, list);
            log.info("find provider name={} list={}", remoteServiceName, list);
        }
    }

    private void registryClient() throws Exception {
        findRoot();
        for (String remoteServiceName : remoteServiceNames) {
            if (client.checkExists().forPath("/pbrpc/" + remoteServiceName) == null) {
                client.create().withMode(PERSISTENT).forPath("/pbrpc/" + remoteServiceName);
            }
            if (client.checkExists().forPath("/pbrpc/" + remoteServiceName + "/consumer") == null) {
                client.create().withMode(PERSISTENT).forPath("/pbrpc/" + remoteServiceName + "/consumer");
            }
            if (client.checkExists().forPath(RegistryUtil.getChildPath(remoteServiceName, RegistryUtil.CONSUMER) + "/" + Joiner.on(":").join(RegistryUtil.getLocalIp(), RegistryUtil.getPort())) == null) {
                client.create().withMode(EPHEMERAL).forPath(RegistryUtil.getChildPath(remoteServiceName, RegistryUtil.CONSUMER) + "/" + Joiner.on(":").join(RegistryUtil.getLocalIp(), RegistryUtil.getPort()));
            }
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
