package com.bot;

import com.bot.bota.bot.FzzfDealBot;
import com.bot.bota.bot.FzzfExecBot;
import com.bot.bota.entity.Fzzfmydata;
import com.bot.bota.service.FzzfmydataService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executor;


@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
@Slf4j
@MapperScan("com.bot.botservice.fzzf.mapper")
public class App {

    @Resource
    private DefaultBotOptions botOptions;
    @Resource
    private DefaultBotSession defaultBotSession;
    @Resource
    private Executor taskExecutor;
    @Resource
    private Executor taskExecutorsigle;
    @Autowired
    private FzzfmydataService fzzfmydataService;
    @Autowired
    private FzzfDealBot fzzfDealBot;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);

    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
            FzzfExecBot execBot = new FzzfExecBot(botOptions, "qijihutuibot", "7343674936:AAFsHIoVNu7FMmxUgeJpBneqLzqHjEW6XdE", taskExecutor, taskExecutorsigle, fzzfDealBot);
            telegramBotsApi.registerBot(execBot);
        } catch (TelegramApiException e) {
            log.error("注册bot失败", e);
        }
        Fzzfmydata cdstatus = fzzfmydataService.getOneByMyKey("cdstatus");
        cdstatus.setMyvalus("0");
        fzzfmydataService.updateById(cdstatus);
    }
}
