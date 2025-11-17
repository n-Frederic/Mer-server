package com.example.demo.repository;

import com.example.demo.entity.LogTaskId;
import com.example.demo.entity.Log_Task;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>{

}
