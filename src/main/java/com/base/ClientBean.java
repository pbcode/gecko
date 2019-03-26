package com.base;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.proxy.JdkProxy;
import com.registry.RegistryUtil;
import com.registry.ZKUtil;
import com.support.PbService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.zookeeper.CreateMode.EPHEMERAL;
import static org.apache.zookeeper.CreateMode.PERSISTENT;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Component
@Slf4j
public class ClientBean implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Value("${zookeeper.address}")
    private String zkAddress;

    private ZKUtil zkUtil = new ZKUtil();

    private Map<String, Set<String>> serviceProviderMap = Maps.newConcurrentMap();
    /**
     * 远程服务名称集合
     */
    private Set<String> remoteServiceNames = Sets.newHashSet();

    private Map<String, Set<Field>> serviceFields = Maps.newHashMap();

    private Map<Field, Object> fieldObjectNames = Maps.newHashMap();

    private CuratorFramework client;

    private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 100, 5l, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

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
            Set<String> providers = serviceProviderMap.get(remoteServiceName);
            for (Field field : fields) {
                JdkProxy jdkProxy = new JdkProxy(providers);
                field.set(fieldObjectNames.get(field), jdkProxy.newProxy(field.getType(), remoteServiceName, 1));
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
            addListener(pathChildrenCache);
            pathChildrenCache.start();
            List<String> list = client.getChildren().forPath(RegistryUtil.getChildPath(remoteServiceName, RegistryUtil.PROVIDER));
            serviceProviderMap.put(remoteServiceName, Sets.newHashSet(list));
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


    private void addListener(PathChildrenCache cache) {
        // a PathChildrenCacheListener is optional. Here, it's used just to log changes
        PathChildrenCacheListener listener = (client, event) -> {
            String path = event.getData().getPath();
            String[] paths = path.split("/");
            String remoteServiceName = paths[2].trim();
            String ip = paths[4].trim();
            Set<String> serviceIps = serviceProviderMap.get(remoteServiceName);
            switch (event.getType()) {
                case CHILD_ADDED: {
                    if (CollectionUtils.isEmpty(serviceIps)) {
                        serviceIps = Sets.newHashSet();
                    }
                    serviceIps.add(ip);
                    serviceProviderMap.put(remoteServiceName, serviceIps);
                    System.out.println("Node changed: " + serviceProviderMap);
                    break;
                }
                case CHILD_UPDATED: {
                    System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }
                case CHILD_REMOVED: {
                    serviceIps.remove(ip);
                    serviceProviderMap.put(remoteServiceName, serviceIps);
                    System.out.println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

}
