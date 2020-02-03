package async.demo.service;

import async.demo.entity.User;
import async.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Async
    public CompletableFuture<List<User>> saveUsers(MultipartFile multipartFile) throws Exception {
        long start=System.currentTimeMillis();
        List<User> users = parseCSVFILE(multipartFile);
        logger.info("Saving list of user of size {}", users.size()+""+ Thread.currentThread().getName());
        users=userRepository.saveAll(users);
        long end=System.currentTimeMillis();
        logger.info("Total time {}", (end-start));
        return CompletableFuture.completedFuture(users);
    }

    @Async
    public CompletableFuture<List<User>> findAllUsers() {
        logger.info("Get list of users by"+Thread.currentThread().getName());
        List<User> users = userRepository.findAll();
        return CompletableFuture.completedFuture(users);
    }

    private List<User> parseCSVFILE(final MultipartFile multipartFile) throws Exception {
        final List<User> users = new ArrayList<>();
        try {
            try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
                String line;
                while ((line=bufferedReader.readLine()) != null) {
                    final String[] data = line.split(",");
                    final User user = new User();
                    user.setName(data[0]);
                    user.setEmail(data[1]);
                    user.setGender(data[2]);
                    users.add(user);
                }
            }
            return users;
        } catch (final Exception exc) {
            logger.error("Failed to parse CSV file {}", exc);
            throw new Exception("Failed to parse CSV file {}", exc);
        }
    }
}
