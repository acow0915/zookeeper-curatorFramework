package com.java;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

	public static void main(String[] args){
		SpringApplication.run(Application.class, args);
		
//		CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(100, 3));
//		client.start();
//		
//		NodeCache cache = new NodeCache(client, "/config");
//		
//		try {
//			cache.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
//				
//		cache.getListenable().addListener(new NodeCacheListener() {
//			@Override
//			public void nodeChanged() throws Exception {
//				ChildData childData = cache.getCurrentData();
//				String data = new String(childData.getData());
//				System.out.println(data);
//			}
//		});
//		
//		
//		try {
//			Thread.sleep(100000L);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println("-------------------------");
//		new SpringApplicationBuilder(Application.class).web(true).run(args);
	}
}
