-- Users
INSERT INTO user (name, email) VALUES ('João Silva', 'joao@email.com');
INSERT INTO user (name, email) VALUES ('Maria Costa', 'maria@email.com');
INSERT INTO user (name, email) VALUES ('Carlos Souza', 'carlos@email.com');

-- Rooms
INSERT INTO room (name, capacity) VALUES ('Sala A', 10);
INSERT INTO room (name, capacity) VALUES ('Sala B', 20);
INSERT INTO room (name, capacity) VALUES ('Sala de Reunião', 8);

-- Bookings
INSERT INTO booking (room_id, user_id, start_time, end_time)
VALUES (1, 1, '2026-06-20 09:00:00', '2026-06-20 10:00:00');

INSERT INTO booking (room_id, user_id, start_time, end_time)
VALUES (2, 2, '2026-06-20 14:00:00', '2026-06-20 15:30:00');