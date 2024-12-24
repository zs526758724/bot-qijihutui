package com.bot;

import com.bot.bota.bot.FzzfDealBot;
import com.bot.bota.bot.FzzfExecBot;
import com.bot.bota.entity.Fzzfmydata;
import com.bot.bota.service.FzzfmydataService;
import com.bot.botb.bot.BDealBot;
import com.bot.botb.bot.BExecBot;
import com.bot.botb.entity.Bmydata;
import com.bot.botb.service.BmydataService;
import com.bot.botc.bot.CDealBot;
import com.bot.botc.bot.CExecBot;
import com.bot.botc.entity.Cmydata;
import com.bot.botc.service.CmydataService;
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
@SpringBootApplication
@Slf4j
@MapperScan({"com.bot.botb.mapper","com.bot.bota.mapper"})
public class App {

    @Resource
    private DefaultBotOptions botOptions;
    @Resource
    private DefaultBotSession defaultBotSession;
    @Resource
    private Executor taskExecutor;
    @Resource
    private Executor taskExecutorsigle;
    //------------------------------------------------
    @Autowired
    private BmydataService bmydataService;
    @Autowired
    private BDealBot bDealBot;
    //------------------------------------------------
    @Autowired
    private FzzfmydataService fzzfmydataService;
    @Autowired
    private FzzfDealBot fzzfDealBot;
//------------------------------------------------
    @Autowired
    private CmydataService cmydataService;
    @Autowired
    private CDealBot cDealBot;

    //------------------------------------------------
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);

    }

    @PostConstruct
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
        //--------------------------------bot1
        try {
            FzzfExecBot execBot = new FzzfExecBot(botOptions, "qijihutuibot", "7343674936:AAFsHIoVNu7FMmxUgeJpBneqLzqHjEW6XdE", taskExecutor, taskExecutorsigle, fzzfDealBot);
            telegramBotsApi.registerBot(execBot);
        } catch (TelegramApiException e) {
            log.error("注册bot失败", e);
        }
        Fzzfmydata acdstatus = fzzfmydataService.getOneByMyKey("cdstatus");
        acdstatus.setMyvalus("0");
        fzzfmydataService.updateById(acdstatus);
        //--------------------------------bpt2
        try {
            BExecBot execBot = new BExecBot(botOptions, "hemahutuibot", "7595446020:AAHTq1ryoj7mIhRNULJZUwLlQJyMz1GUUJw", taskExecutor, taskExecutorsigle, bDealBot);
            telegramBotsApi.registerBot(execBot);
        } catch (TelegramApiException e) {
            log.error("注册bot失败", e);
        }
        Bmydata bcdstatus = bmydataService.getOneByMyKey("cdstatus");
        bcdstatus.setMyvalus("0");
        bmydataService.updateById(bcdstatus);
        //--------------------------------bot3
        try {
            CExecBot execBot = new CExecBot(botOptions, "chrngzhibot", "7871242383:AAFD_PtJzt5p5fttWRiywEvO8quHyHrScfQ", taskExecutor, taskExecutorsigle, cDealBot);
            telegramBotsApi.registerBot(execBot);
        } catch (TelegramApiException e) {
            log.error("注册bot失败", e);
        }
        Cmydata ccdstatus = cmydataService.getOneByMyKey("cdstatus");
        ccdstatus.setMyvalus("0");
        cmydataService.updateById(ccdstatus);
    }
}
