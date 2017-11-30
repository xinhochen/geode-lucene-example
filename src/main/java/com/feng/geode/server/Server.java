package com.feng.geode.server;

import com.feng.geode.domain.EmployeeData;
import org.apache.geode.cache.*;
import org.apache.geode.cache.lucene.LuceneService;
import org.apache.geode.cache.lucene.LuceneServiceProvider;
import org.apache.geode.cache.server.CacheServer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.util.Properties;

public class Server implements Runnable {
    private Cache cache;

    private Server() {
        init();
    }

    private void init() {
        Properties gemfireProperties = getGemfireProperties();

        cache = new CacheFactory(gemfireProperties).create();

        createLuceneIndex(cache);

        createRegion(cache);
    }

    private void startCachServer() {
        CacheServer cacheServer = cache.addCacheServer();
        cacheServer.setPort(40404);
        try {
            cacheServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRegion(Cache cache) {
        RegionFactory<Integer, EmployeeData> regionFactory = cache.createRegionFactory();
        regionFactory.setDataPolicy(DataPolicy.PARTITION);
        //regionFactory.setStatisticsEnabled(true);
        //regionFactory.setScope(Scope.DISTRIBUTED_ACK);

        Region<Integer, EmployeeData> region = regionFactory.create("example-region");
        System.out.printf("Region '%1$s' created in Cache '%2$s.%n", region.getFullPath(), cache.getName());
    }

    private Properties getGemfireProperties() {
        Properties gemfireProperties = new Properties();
        gemfireProperties.setProperty("name", "server");
        gemfireProperties.setProperty("mcast-port", "0");
        gemfireProperties.setProperty("log-level", "warning");
        gemfireProperties.setProperty("jmx-manager", "true");
        gemfireProperties.setProperty("jmx-manager-port", "1099");
        gemfireProperties.setProperty("jmx-manager-start", "true");
        //gemfireProperties.setProperty("enable-time-statistics", "true");
        //gemfireProperties.setProperty("statistic-archive-file", "lucene.gfs");
        gemfireProperties.setProperty("start-locator", "localhost[10334]");
        return gemfireProperties;
    }

    private void createLuceneIndex(Cache cache) {
        LuceneService luceneService = LuceneServiceProvider.get(cache);

        luceneService.createIndexFactory().addField("firstName").addField("lastName")
            .create("simpleIndex", "example-region");

        luceneService.createIndexFactory().addField("lastName", new StandardAnalyzer())
            .addField("email", new KeywordAnalyzer())
            .create("analyzerIndex", "example-region");
    }

    @Override
    public void run() {
        startCachServer();
    }

    public static void main(String[] args) {
        new Server().run();
    }
}
