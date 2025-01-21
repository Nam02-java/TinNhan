mysql> show tables;
+--------------------+
| Tables_in_datachat |
+--------------------+
| messages           |
| socket_connections |
| user_sessions      |
| users              |
+--------------------+
4 rows in set (0.00 sec)

mysql> describe users;
+----------+--------------+------+-----+---------+----------------+
| Field    | Type         | Null | Key | Default | Extra          |
+----------+--------------+------+-----+---------+----------------+
| id       | bigint       | NO   | PRI | NULL    | auto_increment |
| username | varchar(255) | NO   | UNI | NULL    |                |
+----------+--------------+------+-----+---------+----------------+
2 rows in set (0.00 sec)

mysql> describe messages;
+-----------------+-----------------------+------+-----+-------------------+-------------------+
| Field           | Type                  | Null | Key | Default           | Extra             |
+-----------------+-----------------------+------+-----+-------------------+-------------------+
| id              | bigint                | NO   | PRI | NULL              | auto_increment    |
| sender_id       | bigint                | NO   | MUL | NULL              |                   |
| recipient_id    | bigint                | NO   | MUL | NULL              |                   |
| message_content | text                  | NO   |     | NULL              |                   |
| sent_at         | timestamp             | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED |
| status          | enum('seen','unread') | NO   |     | unread            |                   |
| seen_at         | timestamp             | YES  |     | NULL              |                   |
+-----------------+-----------------------+------+-----+-------------------+-------------------+
7 rows in set (0.00 sec)

mysql> describe user_sessions;
+----------------+--------------------------+------+-----+-------------------+-----------------------------------------------+
| Field          | Type                     | Null | Key | Default           | Extra
     |
+----------------+--------------------------+------+-----+-------------------+-----------------------------------------------+
| id             | bigint                   | NO   | PRI | NULL              | auto_increment
     |
| user_id        | bigint                   | NO   | MUL | NULL              |
     |
| session_token  | varchar(255)             | NO   | UNI | NULL              |
     |
| device_info    | varchar(255)             | YES  |     | NULL              |
     |
| ip_address     | varchar(45)              | YES  |     | NULL              |
     |
| status         | enum('online','offline') | NO   |     | offline           |
     |
| created_at     | datetime                 | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED
     |
| last_active_at | datetime                 | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP |
+----------------+--------------------------+------+-----+-------------------+-----------------------------------------------+
8 rows in set (0.00 sec)

mysql> describe socket_connections
    -> ;
+-------------+----------+------+-----+-------------------+-------------------+
| Field       | Type     | Null | Key | Default           | Extra             |
+-------------+----------+------+-----+-------------------+-------------------+
| id          | bigint   | NO   | PRI | NULL              | auto_increment    |
| session_id  | bigint   | NO   | MUL | NULL              |                   |
| socket_info | text     | YES  |     | NULL              |                   |
| created_at  | datetime | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED |
+-------------+----------+------+-----+-------------------+-------------------+
