package org.example.config;


import org.example.model.entity.Bot;
import org.example.model.entity.Model;
import org.example.model.entity.User;
import org.example.repository.BotRepository;
import org.example.repository.ModelRepository;
import org.example.repository.UserRepository;
import org.example.service.impl.UserServiceImpl;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Configuration
public class DatabaseInitializer {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    private User admin = null;

    private User testUser = null;

    private final UserRepository userRepository;

    private final ModelRepository modelRepository;

    private final BotRepository botRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserServiceImpl userService;

    private final JwtUtil jwtUtil;

    public DatabaseInitializer(UserRepository userRepository, ModelRepository modelRepository, BotRepository botRepository, PasswordEncoder passwordEncoder, UserServiceImpl userService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.modelRepository = modelRepository;
        this.botRepository = botRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            if ("create".equalsIgnoreCase(ddlAuto))
                initDatabase();
            generateJwt();
        };
    }


    public void initDatabase() {
        createAdmin();
        createTestUser();
        createOfficialBots();
    }

    public void createAdmin() {
        admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole(User.Role.ADMIN);
        admin.setCreatedAt(Timestamp.from(Instant.now()));
        admin.setUpdatedAt(Timestamp.from(Instant.now()));
        admin.setCredits(BigDecimal.valueOf(1000000));
        admin = userRepository.save(admin);
    }

    public void createTestUser() {
        testUser = new User();
        testUser.setUsername("test");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("test"));
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(Timestamp.from(Instant.now()));
        testUser.setUpdatedAt(Timestamp.from(Instant.now()));
        testUser.setCredits(BigDecimal.valueOf(1000));
        userRepository.save(testUser);
    }

    public void createOfficialBots() {
        createModelAndBot("gpt-3.5-turbo", "GPT-3.5-Turbo", "https://xiaoai.plus/v1/chat/completions",
                "sk-WXMk631iKpu1J2Ql6GnCVLRt2YwOeLXALEqx6X1BmPuIMsRg", 1);
        createModelAndBot("gpt-4o", "GPT-4O", "https://xiaoai.plus/v1/chat/completions",
                "sk-WXMk631iKpu1J2Ql6GnCVLRt2YwOeLXALEqx6X1BmPuIMsRg", 6);
        createModelAndBot("deepseek-ai/DeepSeek-V3", "DeepSeek-V3", "https://api.siliconflow.cn/v1/chat/completions",
                "sk-ejnacqjmzqfhqduzfhzzitmonumpzqnlnpszzpwowtzgaery", 3);
        createModelAndBot("deepseek-ai/DeepSeek-R1", "DeepSeek-R1", "https://api.siliconflow.cn/v1/chat/completions",
                "sk-ejnacqjmzqfhqduzfhzzitmonumpzqnlnpszzpwowtzgaery", 10);
    }

    private void createModelAndBot(String modelName, String botName, String apiUrl, String apiKey, Integer costRate) {
        Model model = new Model();
        model.setModelName(modelName);
        model.setApiUrl(apiUrl);
        model.setApiKey(apiKey);
        model.setCostRate(costRate);
        model.setCreatedAt(Timestamp.from(Instant.now()));
        model.setUpdatedAt(Timestamp.from(Instant.now()));
        model = modelRepository.save(model);

        Bot bot = new Bot();
        bot.setBotName(botName);
        bot.setCreator(admin);
        bot.setModel(model);
        bot.setBotType(Bot.BotType.OFFICIAL);
        bot.setCreatedAt(Timestamp.from(Instant.now()));
        bot.setUpdatedAt(Timestamp.from(Instant.now()));
        botRepository.save(bot);
    }

    private void generateJwt() {
        UserDetails adminDetails = userService.loadUserByUsername("admin");
        String jwt = jwtUtil.generateToken(adminDetails);
        System.out.println("Generated JWT for admin: " + jwt);
        UserDetails testUserDetails = userService.loadUserByUsername("test");
        jwt = jwtUtil.generateToken(testUserDetails);
        System.out.println("Generated JWT for test user: " + jwt);
    }
}
