package com.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@RestController
@EnableFeignClients
public class SampleZookeeperApplication {

	@Value("${spring.application.name}")
	private String appName;
	
	@Autowired
	private CuratorFramework curatorFramework;

	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	private DiscoveryClient discovery;

	@Autowired
	private Environment env;

	@Autowired
	private AppClient appClient;
	
	@Autowired
	private DependenciesClient dependenciesClient;
	
	@Bean
	public NodeCache getNodeCache(){
		NodeCache cache = new NodeCache(curatorFramework, "/config");
		try {
			cache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		cache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				ChildData childData = cache.getCurrentData();
				String data = new String(childData.getData());
				System.out.println(data);
			}
		});
		return cache;
	}

	@RequestMapping("/")
	public ServiceInstance lb() {
		return this.loadBalancer.choose(this.appName);
	}

	@RequestMapping("/hi")
	public String hi() {
		String lockString = "";
		try {
			CuratorZookeeperClient client = curatorFramework.getZookeeperClient();
			ZooKeeper zk = client.getZooKeeper();
			
			if(curatorFramework.checkExists().forPath("/znode") == null){
				String znodeCreateResult = curatorFramework.create().forPath("/znode", "znode".getBytes());
				System.out.println("znodeCreateResult:" + znodeCreateResult);
			}
			if(curatorFramework.checkExists().forPath("/test") == null){
				String testCreateResult = curatorFramework.create().forPath("/test", "test1".getBytes());
				System.out.println("testCreateResult:" + testCreateResult);
			}
			
			curatorFramework.delete().forPath("/test");
			
			//创建分布式锁, 锁空间的根节点路径为/lock
		    InterProcessMutex mutex = new InterProcessMutex(curatorFramework, "/lock");
		    if( mutex.acquire(10000L, TimeUnit.MILLISECONDS) ){
		    	System.out.println("do lock ...");
		    	lockString = "do lock ...";
		    } else {
		    	System.out.println("be locked ...");
		    	lockString = "be locked ...";
		    }
			
			String appName = curatorFramework.getData().forPath("/config/dev/app.name").toString();
			System.out.println("appName:" + appName);
			
			List<String> paths = zk.getChildren("/", false);
			paths.forEach(System.out::println);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lockString;
	}

	@RequestMapping("/selfFeign")
	public String self() {
		dependenciesClient.hi();
		return this.appClient.hi();
	}

	@RequestMapping("/myenv")
	public String env(@RequestParam("prop") String prop) {
		return new RelaxedPropertyResolver(this.env).getProperty(prop, "Not Found");
	}

	@FeignClient(value="${spring.application.name}")
	interface AppClient {
		@RequestMapping(path = "/hi", method = RequestMethod.GET)
		String hi();
	}
	
	@FeignClient(value="test1")
	interface DependenciesClient {
		@RequestMapping(path = "/hi", method = RequestMethod.GET)
		String hi();
	}

	@Autowired
	RestTemplate rest;

	@RequestMapping("/selfRest")
	public String rt() {
		return this.rest.getForObject("http://" + this.appName + "/hi", String.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleZookeeperApplication.class, args);
	}

	@Bean
	@LoadBalanced
	RestTemplate loadBalancedRestTemplate() {
		return new RestTemplate();
	}

}
