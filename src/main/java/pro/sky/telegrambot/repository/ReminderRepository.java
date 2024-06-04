package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<NotificationTask, Long> {
    public List<NotificationTask> findAllByLocalDateTimeLessThan(LocalDateTime localDateTime);
}