package com.demo.cluster.service.pub;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServicePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicePublisher.class);

    public void pub() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
            "127.0.0.1:2181",
            new ExponentialBackoffRetry(1000, 3)
        );
        client.start();
        ServiceRegistry serviceRegistry = new ServiceRegistry(client,"services");
        ServiceInstance<InstanceDetails> instance1 = ServiceInstance.<InstanceDetails>builder()
            .name("service1")
            .port(12345)
            .address("192.168.1.100")   //address不写的话，会取本地ip
            .id("192.168.1.100:12345")
            .payload(
                new InstanceDetails(
                    "192.168.1.100:12345/Test.Service1",
                    "192.168.1.100",
                    12345,
                    "Test.Service1"
                )
            )
            .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
            .build();
        serviceRegistry.registerService(instance1);
        LOGGER.info("service published.");
    }
}
