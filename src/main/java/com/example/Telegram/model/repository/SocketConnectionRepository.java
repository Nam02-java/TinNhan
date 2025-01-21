package com.example.Telegram.model.repository;

import com.example.Telegram.model.data.SocketConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocketConnectionRepository extends JpaRepository<SocketConnection, Long> {

    // Find socket runtime by string
    SocketConnection findBySocketInfo(String socketInfo);

    List<SocketConnection> findAll();

}

